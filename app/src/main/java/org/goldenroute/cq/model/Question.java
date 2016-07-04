package org.goldenroute.cq.model;

import android.media.Image;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Question implements Serializable {

    private final String index;
    private final String description;
    private final Map<String, Image> images;
    private final Map<String, String> choices;
    private final String correctAnswer;
    private String currentAnswer;
    private int weight;

    public Question(String index, String description, Map<String, String> choices, Map<String, Image> images, String correctAnswer) {
        this.index = index;
        this.description = description;
        this.choices = new HashMap<>();
        this.images = new HashMap<>();
        this.choices.putAll(choices);

        if (images != null) {
            this.images.putAll(images);
        }
        this.correctAnswer = correctAnswer;
        this.currentAnswer = null;
        this.weight = 0;
    }

    public String getIndex() {
        return this.index;
    }

    public String getDescription() {
        return this.description;
    }

    public Map<String, String> getChoices() {
        return Collections.unmodifiableMap(this.choices);
    }

    public Map<String, Image> getImage() {
        return Collections.unmodifiableMap(this.images);
    }

    public String getCorrectAnswer() {
        return this.correctAnswer;
    }

    public String getCurrentAnswer() {
        return this.currentAnswer;
    }

    public void setCurrentAnswer(String value) {
        this.currentAnswer = value;
    }

    public int getWeight() {
        return this.weight;
    }

    public void setWeight(int value) {
        if (value >= 0) {
            this.weight = value;
        }
    }
}
