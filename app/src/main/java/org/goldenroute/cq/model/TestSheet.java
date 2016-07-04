package org.goldenroute.cq.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class TestSheet implements Serializable {

    public static final String ARG_MODEL = "testModel";
    public static final String ARG_PATH = "filePath";
    public static final String ARG_RESTART = "restart";

    private static final String TAG = "TestSheet";
    private static final String SEPARATOR = "<newline>";

    private final Context context;
    private TestModel model;

    private String path;
    private String title;
    private Map<String, Question> questionBank;
    private List<String> questionList;
    private Question currentQuestion;

    private TestSheet(Context context, TestModel model, String path) {
        this.context = context;
        this.model = model;
        this.path = path;

        this.initialize();
    }

    private TestSheet(Context context, TestModel model, String path, Element leftContext, boolean restart) {
        this.context = context;
        this.model = model;
        this.path = path;

        this.initialize(leftContext, restart);
    }

    public TestModel getModel() {
        return this.model;
    }

    public String getTitle() {
        return this.title;
    }

    public String getPath() {
        return this.path;
    }

    public int getIndex() {
        return this.questionList.indexOf(this.currentQuestion.getIndex());
    }

    public int getTotal() {
        return this.questionList.size();
    }

    public Question getCurrent() {
        return this.currentQuestion;
    }

    public void previous() {
        int index = this.getIndex();
        if (index > 0) {
            this.currentQuestion = this.questionBank.get(this.questionList.get(index - 1));
        }
    }

    public void next() {
        int index = this.getIndex();
        if (index < this.getTotal() - 1) {
            this.currentQuestion = this.questionBank.get(this.questionList.get(index + 1));
        }
    }

    public boolean goTo(String position) {
        if (!TextUtils.isEmpty(position)) {
            position = position.trim();

            if (this.questionList.contains(position)) {
                this.currentQuestion = this.questionBank.get(position);
                return true;
            } else {
                int index;
                try {
                    index = Integer.parseInt(position);
                } catch (NumberFormatException e) {
                    index = -1;
                }
                if (index != -1 && index < this.questionList.size()) {
                    this.currentQuestion = this.questionBank.get(this.questionList.get(index));
                    return true;
                }
            }
        }
        return false;
    }

    public int submit() {
        int count = 0;
        for (String id : this.questionList) {
            Question question = this.questionBank.get(id);
            if (question.getCorrectAnswer().equalsIgnoreCase(question.getCurrentAnswer())) {
                count++;
            }
        }
        return count;
    }

    public boolean needReview() {
        for (String id : this.questionList) {
            Question question = this.questionBank.get(id);
            if (!question.getCorrectAnswer().equalsIgnoreCase(question.getCurrentAnswer()) ||
                    question.getWeight() > 0) {
                return true;
            }
        }
        return false;
    }

    public void review() {
        if (this.needReview()) {
            List<String> removedList = new ArrayList<>();
            for (String id : this.questionList) {
                Question question = this.questionBank.get(id);
                if (question.getCorrectAnswer().equalsIgnoreCase(question.getCurrentAnswer()) && question.getWeight() == 0) {
                    removedList.add(id);
                } else {
                    question.setWeight(question.getWeight() - 1);
                    question.setCurrentAnswer(null);
                }
            }
            for (String id : removedList) {
                this.questionList.remove(id);
            }
            this.currentQuestion = this.questionBank.get(this.questionList.get(0));
            this.model = TestModel.Review;
        }
    }

    public static TestSheet load(Context context, TestModel testModel, String path, boolean restart) {
        if (!TextUtils.isEmpty(path) && new File(path).exists()) {
            return new TestSheet(context, testModel, path);
        } else {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            String content = settings.getString(testModel.toString().toLowerCase(), "");

            if (!TextUtils.isEmpty(content)) {
                try {
                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                    InputSource inputSource = new InputSource();
                    inputSource.setCharacterStream(new StringReader(content));
                    Document xmlDocument = documentBuilder.parse(inputSource);

                    Element xmlRoot = xmlDocument.getDocumentElement();

                    if (xmlRoot.hasAttribute("path")) {
                        return new TestSheet(context, testModel, xmlRoot.getAttribute("path"), xmlRoot, restart);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    public void save() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document xmlDocument = documentBuilder.newDocument();

            Element xmlRoot = xmlDocument.createElement("root");

            xmlRoot.setAttribute("path", TextUtils.isEmpty(this.path) ? "" : this.path);
            xmlRoot.setAttribute("current", Integer.toString(this.getIndex()));
            xmlDocument.appendChild(xmlRoot);

            for (String id : this.questionList) {
                Question question = this.questionBank.get(id);

                Element xmlQuestion = xmlDocument.createElement("question");
                xmlQuestion.setAttribute("index", question.getIndex());

                String choice = "";
                String answer = question.getCurrentAnswer();
                if (!TextUtils.isEmpty(answer)) {
                    choice = question.getChoices().get(answer);
                }

                xmlQuestion.setAttribute("answer", Integer.toString(choice.hashCode()));
                xmlQuestion.setAttribute("weight", Integer.toString(question.getWeight()));

                xmlRoot.appendChild(xmlQuestion);
            }

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            Properties outFormat = new Properties();
            outFormat.setProperty(OutputKeys.INDENT, "yes");
            outFormat.setProperty(OutputKeys.METHOD, "xml");
            outFormat.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            outFormat.setProperty(OutputKeys.VERSION, "1.0");
            outFormat.setProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperties(outFormat);
            DOMSource domSource = new DOMSource(xmlDocument.getDocumentElement());
            OutputStream output = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(output);
            transformer.transform(domSource, result);
            String xmlString = output.toString();

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.context);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(this.model.toString().toLowerCase(), xmlString);
            editor.apply();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initialize() {
        File file = new File(this.path);

        if (file.exists()) {
            this.title = file.getName();
            this.questionBank = this.loadQuestionBank();
            this.questionList = new ArrayList<>();
            this.questionList.addAll(this.questionBank.keySet());

            if (model == TestModel.Test) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.context);
                AtomicReference<Integer> questionPerTest = new AtomicReference<>();
                if (!Utils.tryParse(settings.getString("pref_questions_per_test", "25"), questionPerTest)) {
                    questionPerTest.set(25);
                }

                Random randomGenerator = new Random(System.currentTimeMillis());
                List<String> testList = new ArrayList<>();
                int index;
                while (testList.size() < questionPerTest.get() && testList.size() < this.questionList.size()) {
                    do {
                        index = randomGenerator.nextInt(this.questionList.size());
                    }
                    while (testList.contains(this.questionList.get(index)));
                    testList.add(this.questionList.get(index));
                }
                this.questionList = testList;
            }
            if (this.questionList.size() > 0) {
                this.currentQuestion = this.questionBank.get(this.questionList.get(0));
            } else {
                this.currentQuestion = null;
            }
        } else {
            this.title = "";
            this.questionBank = new HashMap<>();
            this.questionList = new ArrayList<>();
            this.currentQuestion = null;
        }
    }

    private void initialize(Element leftContext, boolean restart) {
        File file = new File(this.path);

        if (file.exists()) {
            this.title = file.getName();
            this.questionBank = this.loadQuestionBank();
            this.questionList = new ArrayList<>();

            NodeList nodeList = leftContext.getElementsByTagName("question");

            for (int nodeIndex = 0; nodeIndex < nodeList.getLength(); nodeIndex++) {
                Element xmlElement = (Element) nodeList.item(nodeIndex);

                if (!xmlElement.hasAttribute("index") || !xmlElement.hasAttribute("answer") || !xmlElement.hasAttribute("weight")) {
                    continue;
                }

                String index = xmlElement.getAttribute("index");
                if (this.questionBank.containsKey(index)) {
                    this.questionList.add(index);

                    if (!restart) {
                        Question question = this.questionBank.get(index);
                        AtomicReference<Integer> hasCode = new AtomicReference<>();

                        if (Utils.tryParse(xmlElement.getAttribute("answer"), hasCode)) {
                            for (Map.Entry<String, String> choice : question.getChoices().entrySet()) {
                                if (choice.getValue().hashCode() == hasCode.get()) {
                                    question.setCurrentAnswer(choice.getKey());
                                    break;
                                }
                            }
                        }

                        AtomicReference<Integer> weight = new AtomicReference<>();
                        if (Utils.tryParse(xmlElement.getAttribute("weight"), weight)) {
                            question.setWeight(weight.get());
                        }
                    }
                }
            }

            AtomicReference<Integer> current = new AtomicReference<>();
            if (leftContext.hasAttribute("current")) {
                if (!Utils.tryParse(leftContext.getAttribute("current"), current)) {
                    current.set(0);
                }
            }
            if (this.questionList.size() > current.get()) {
                this.currentQuestion = this.questionBank.get(this.questionList.get(current.get()));
            } else {
                this.currentQuestion = null;
            }
        } else {
            this.title = "";
            this.questionBank = new HashMap<>();
            this.questionList = new ArrayList<>();
            this.currentQuestion = null;
        }
    }

    private Map<String, Question> loadQuestionBank() {
        Map<String, Question> questions = new LinkedHashMap<>();
        StringBuilder text = new StringBuilder(SEPARATOR);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(this.path)));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 1 && line.charAt(0) == 0xFEFF) {
                    // Skip BOM (Byte Order Mark) character.
                    continue;
                }
                if (!TextUtils.isEmpty(line)) {
                    text.append(line);
                    text.append(SEPARATOR);
                }
            }
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        String[] items = text.toString().split(Pattern.quote(SEPARATOR + "[I]"));

        for (String item : items) {
            Question question = this.loadQuestion("[I]" + item);
            if (question != null) {
                questions.put(question.getIndex(), question);
            }
        }
        return questions;
    }

    private Question loadQuestion(String content) {
        String[] items = content.split(Pattern.quote(SEPARATOR));

        String index = null;
        String description = null;
        Map<String, String> choices = new HashMap<>();

        for (String item : items) {
            if (item.startsWith("[I]")) {
                index = item.substring("[I]".length()).trim();
            } else if (item.startsWith("[Q]")) {
                description = item.substring("[Q]".length()).trim();
            } else if (item.startsWith("[A]")) {
                choices.put("A", item.substring("[A]".length()).trim());
            } else if (item.startsWith("[B]")) {
                choices.put("B", item.substring("[B]".length()).trim());
            } else if (item.startsWith("[C]")) {
                choices.put("C", item.substring("[C]".length()).trim());
            } else if (item.startsWith("[D]")) {
                choices.put("D", item.substring("[D]".length()).trim());
            } else if (item.startsWith("[E]")) {
                choices.put("E", item.substring("[E]".length()).trim());
            } else if (item.startsWith("[F]")) {
                choices.put("F", item.substring("[F]".length()).trim());
            } else {
                Log.v(TAG, String.format("Invalid line '%s'", item));
            }
        }

        if (!TextUtils.isEmpty(index) && !TextUtils.isEmpty(description) && choices.size() > 0) {
            List<String> confusedList = new ArrayList<>(choices.keySet());
            Collections.shuffle(confusedList);
            char label = 'A';
            Map<String, String> confusedChoices = new HashMap<>();
            for (String item : confusedList) {
                confusedChoices.put(String.format("%c", label), choices.get(item));
                label++;
            }
            return new Question(index, description, confusedChoices, null, String.format("%c", 'A' + confusedList.indexOf("A")));
        }
        return null;
    }
}