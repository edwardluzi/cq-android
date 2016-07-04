package org.goldenroute.cq;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.goldenroute.cq.model.TestModel;
import org.goldenroute.cq.model.TestSheet;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SheetActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {
    private TestSheet mTestSheet = null;
    private boolean mSuppressCheckChangedEvent;

    @Bind(R.id.button_help)
    protected ImageButton mButtonHelp;

    @Bind(R.id.button_goto)
    protected ImageButton mButtonGoto;

    @Bind(R.id.button_previous)
    protected ImageButton mButtonPrevious;

    @Bind(R.id.button_next)
    protected ImageButton mButtonNext;

    @Bind(R.id.button_finish)
    protected ImageButton mButtonFinish;

    @Bind(R.id.checkbox_impediment)
    protected CheckBox mCheckBoxImpediment;

    @Bind(R.id.text_view_question)
    protected TextView mTextViewQuestion;

    @Bind(R.id.radio_group_choice)
    protected RadioGroup mRadioGroupChoice;

    @Bind(R.id.radio_button_choice1)
    protected RadioButton mRadioButtonChoice1;

    @Bind(R.id.radio_button_choice2)
    protected RadioButton mRadioButtonChoice2;

    @Bind(R.id.radio_button_choice3)
    protected RadioButton mRadioButtonChoice3;

    @Bind(R.id.radio_button_choice4)
    protected RadioButton mRadioButtonChoice4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet);

        ButterKnife.bind(this);

        mButtonHelp.setOnClickListener(this);
        mButtonGoto.setOnClickListener(this);
        mButtonPrevious.setOnClickListener(this);
        mButtonNext.setOnClickListener(this);
        mButtonFinish.setOnClickListener(this);

        mCheckBoxImpediment.setOnCheckedChangeListener(this);
        mRadioGroupChoice.setOnCheckedChangeListener(this);

        TestModel testModel = TestModel.fromInteger(getIntent().getIntExtra(TestSheet.ARG_MODEL, 0));
        String filePath = getIntent().getStringExtra(TestSheet.ARG_PATH);
        boolean restart = getIntent().getBooleanExtra(TestSheet.ARG_RESTART, false);

        mTestSheet = TestSheet.load(this, testModel, filePath, restart);

        if (testModel == TestModel.Review) {
            mButtonFinish.setVisibility(View.GONE);
        } else {
            mCheckBoxImpediment.setVisibility(View.GONE);
            mButtonHelp.setVisibility(View.GONE);
        }

        if (mTestSheet == null || mTestSheet.getCurrent() == null) {
            updateInvalid();
        } else {
            updateQuestion();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_help:
                help();
                break;
            case R.id.button_goto:
                goTo();
                break;
            case R.id.button_previous:
                previous();
                break;
            case R.id.button_next:
                next();
                break;
            case R.id.button_finish:
                submit();
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (!mSuppressCheckChangedEvent) {
            switch (checkedId) {
                case R.id.radio_button_choice1:
                    mTestSheet.getCurrent().setCurrentAnswer("A");
                    break;
                case R.id.radio_button_choice2:
                    mTestSheet.getCurrent().setCurrentAnswer("B");
                    break;
                case R.id.radio_button_choice3:
                    mTestSheet.getCurrent().setCurrentAnswer("C");
                    break;
                case R.id.radio_button_choice4:
                    mTestSheet.getCurrent().setCurrentAnswer("D");
                    break;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton v, boolean isChecked) {
        if (!mSuppressCheckChangedEvent) {
            switch (v.getId()) {
                case R.id.checkbox_impediment:
                    mTestSheet.getCurrent().setWeight(isChecked ? 3 : 0);
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
        if (mTestSheet.getTotal() > 0) {
            mTestSheet.save();
        }
    }


    private void updateQuestion() {
        mSuppressCheckChangedEvent = true;

        setTitle(MakeupTitle());

        mTextViewQuestion.setText(mTestSheet.getCurrent().getDescription());
        mCheckBoxImpediment.setChecked(mTestSheet.getCurrent().getWeight() > 0);

        mRadioButtonChoice1.setText(mTestSheet.getCurrent().getChoices().get("A"));
        mRadioButtonChoice2.setText(mTestSheet.getCurrent().getChoices().get("B"));
        mRadioButtonChoice3.setText(mTestSheet.getCurrent().getChoices().get("C"));
        mRadioButtonChoice4.setText(mTestSheet.getCurrent().getChoices().get("D"));

        mRadioGroupChoice.clearCheck();
        mRadioButtonChoice1.setChecked("A".equals(mTestSheet.getCurrent().getCurrentAnswer()));
        mRadioButtonChoice2.setChecked("B".equals(mTestSheet.getCurrent().getCurrentAnswer()));
        mRadioButtonChoice3.setChecked("C".equals(mTestSheet.getCurrent().getCurrentAnswer()));
        mRadioButtonChoice4.setChecked("D".equals(mTestSheet.getCurrent().getCurrentAnswer()));

        if (mTestSheet.getModel() == TestModel.Review) {
            mButtonPrevious.setEnabled(mTestSheet.getIndex() > 0);
            mButtonNext.setEnabled(true);
        } else {
            mButtonPrevious.setEnabled(mTestSheet.getIndex() > 0);
            mButtonNext.setEnabled(mTestSheet.getIndex() < mTestSheet.getTotal() - 1);
        }
        mSuppressCheckChangedEvent = false;
    }

    private void updateInvalid() {
        mButtonHelp.setEnabled(false);
        mButtonGoto.setEnabled(false);
        mButtonPrevious.setEnabled(false);
        mButtonNext.setEnabled(false);
        mButtonFinish.setEnabled(false);

        mCheckBoxImpediment.setEnabled(false);
        mRadioGroupChoice.setEnabled(false);
    }

    private String MakeupTitle() {
        return String.format("%d of %d : %s - %s", mTestSheet.getIndex() + 1, mTestSheet.getTotal(), mTestSheet.getCurrent().getIndex(), mTestSheet.getTitle());
    }

    private void previous() {
        mTestSheet.previous();
        updateQuestion();
    }

    private void next() {
        if (mTestSheet.getModel() == TestModel.Review) {
            if (mTestSheet.getIndex() < mTestSheet.getTotal() - 1) {
                mTestSheet.next();
                updateQuestion();
            } else {
                if (mTestSheet.needReview()) {

                    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    mTestSheet.review();
                                    updateQuestion();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    SheetActivity.this.finish();
                                    break;
                            }
                        }
                    };

                    MessageBox.show(this, getString(R.string.message_review_your_mistakes), MessageBox.Buttons.YesNo, listener);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.message_finished_the_review), Toast.LENGTH_LONG).show();
                    SheetActivity.this.finish();
                }
            }
        } else {
            mTestSheet.next();
            updateQuestion();
        }
    }

    private void submit() {
        int score = mTestSheet.submit();
        if (score == mTestSheet.getTotal()) {
            Toast.makeText(getApplicationContext(), getString(R.string.message_got_full_marks), Toast.LENGTH_LONG).show();
            SheetActivity.this.finish();
        } else {

            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            mTestSheet.review();
                            updateQuestion();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            SheetActivity.this.finish();
                            break;
                    }
                }
            };

            MessageBox.show(this, String.format(getString(R.string.message_your_score_is), score, mTestSheet.getTotal()), MessageBox.Buttons.YesNo, listener);
        }
    }

    private void help() {
        if (mTestSheet.getCurrent().getCorrectAnswer().equals(mTestSheet.getCurrent().getCurrentAnswer())) {
            Toast.makeText(getApplicationContext(), getString(R.string.message_you_are_right), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), String.format(getString(R.string.message_correct_answer_is), mTestSheet.getCurrent().getCorrectAnswer()), Toast.LENGTH_LONG).show();
        }
    }

    private void goTo() {

        MessageBox.OnClickListener listener = new MessageBox.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, String userInput) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (!mTestSheet.goTo(userInput)) {
                            Toast.makeText(getApplicationContext(), String.format(getString(R.string.message_invalid_question_index), userInput), Toast.LENGTH_LONG).show();
                        } else {
                            updateQuestion();
                        }
                        break;
                }
            }
        };

        MessageBox.prompt(this, getString(R.string.message_specify_question_index), listener);
    }
}
