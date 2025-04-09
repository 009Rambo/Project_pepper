package com.example.pepperproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;

public class BlockGameActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private QiContext qiContext;

    private TextView blockQuestion;
    private RadioGroup answerGroup;
    private Button submitBlockAnswer;

    private String correctAnswer = "Repeat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_game);

        QiSDK.register(this, this);

        blockQuestion = findViewById(R.id.blockQuestion);
        answerGroup = findViewById(R.id.answerGroup);
        submitBlockAnswer = findViewById(R.id.submitBlockAnswer);

        blockQuestion.setText("Which block is used to repeat actions in coding?");

        submitBlockAnswer.setOnClickListener(v -> {
            int selectedId = answerGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selected = findViewById(selectedId);
            String answer = selected.getText().toString();

            boolean isCorrect = answer.equalsIgnoreCase(correctAnswer);
            String feedback = isCorrect ? "Correct! That's the repeat block!" : "Not quite. The correct answer is: Repeat";

            Toast.makeText(this, feedback, Toast.LENGTH_LONG).show();

            if (qiContext != null) {
                Say say = SayBuilder.with(qiContext)
                        .withText(feedback)
                        .build();
                say.run();
            }
        });
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;
    }

    @Override
    public void onRobotFocusLost() {
        qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }
}
