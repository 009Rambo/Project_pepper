package com.example.pepperproject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Say;

import java.util.Random;

public class MathPuzzlesActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "MathPuzzlesActivity";
    private QiContext qiContext;
    private Animate animate;

    // UI Elements
    private TextView puzzleQuestionTextView;
    private EditText answerEditText;
    private Button submitButton;
    private TextView feedbackTextView;
    private TextView correctCountTextView;
    private TextView totalAttemptsTextView;
    private Button nextPuzzleButton;
    private Button backButton;

    // Game state
    private int currentAnswer;
    private int correctCount = 0;
    private int totalAttempts = 0;
    private Random random = new Random();
    private MathPuzzleTask currentTask;

    // Puzzle difficulty levels
    private enum Difficulty {
        EASY, MEDIUM, HARD
    }
    private Difficulty currentDifficulty = Difficulty.EASY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_puzzles);

        // Register for robot lifecycle callbacks
        QiSDK.register(this, this);

        // Initialize UI elements
        initializeViews();

        // Set up button click listeners
        setupButtonListeners();

        // Generate first puzzle
        generateNewPuzzle();
    }

    private void initializeViews() {
        puzzleQuestionTextView = findViewById(R.id.puzzleQuestionTextView);
        answerEditText = findViewById(R.id.answerEditText);
        submitButton = findViewById(R.id.submitButton);
        feedbackTextView = findViewById(R.id.feedbackTextView);
        correctCountTextView = findViewById(R.id.correctCountTextView);
        totalAttemptsTextView = findViewById(R.id.totalAttemptsTextView);
        nextPuzzleButton = findViewById(R.id.nextPuzzleButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupButtonListeners() {
        submitButton.setOnClickListener(v -> {
            checkAnswer();
        });

        nextPuzzleButton.setOnClickListener(v -> {
            generateNewPuzzle();
        });

        backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void generateNewPuzzle() {
        // Reset UI state
        answerEditText.setText("");
        feedbackTextView.setVisibility(View.GONE);

        // Generate puzzle based on current difficulty
        String question;
        int num1, num2, num3;

        switch (currentDifficulty) {
            case EASY:
                // Simple addition or subtraction
                num1 = random.nextInt(10) + 1;  // 1-10
                num2 = random.nextInt(10) + 1;  // 1-10

                if (random.nextBoolean()) {
                    // Addition
                    question = "What is " + num1 + " + " + num2 + "?";
                    currentAnswer = num1 + num2;
                } else {
                    // Subtraction (ensure positive result)
                    if (num1 < num2) {
                        int temp = num1;
                        num1 = num2;
                        num2 = temp;
                    }
                    question = "What is " + num1 + " - " + num2 + "?";
                    currentAnswer = num1 - num2;
                }
                break;

            case MEDIUM:
                // Multiplication or division
                num1 = random.nextInt(10) + 1;  // 1-10
                num2 = random.nextInt(5) + 1;   // 1-5

                if (random.nextBoolean()) {
                    // Multiplication
                    question = "What is " + num1 + " × " + num2 + "?";
                    currentAnswer = num1 * num2;
                } else {
                    // Division (ensure clean division)
                    int product = num1 * num2;
                    question = "What is " + product + " ÷ " + num1 + "?";
                    currentAnswer = num2;
                }
                break;

            case HARD:
                // Mixed operations
                num1 = random.nextInt(10) + 1;  // 1-10
                num2 = random.nextInt(10) + 1;  // 1-10
                num3 = random.nextInt(5) + 1;   // 1-5

                int operation = random.nextInt(3);
                if (operation == 0) {
                    // Addition and multiplication
                    question = "What is " + num1 + " + " + num2 + " × " + num3 + "?";
                    currentAnswer = num1 + (num2 * num3);
                } else if (operation == 1) {
                    // Subtraction and multiplication (ensure positive result)
                    if (num1 < (num2 * num3)) {
                        num1 = (num2 * num3) + random.nextInt(10) + 1;
                    }
                    question = "What is " + num1 + " - " + num2 + " × " + num3 + "?";
                    currentAnswer = num1 - (num2 * num3);
                } else {
                    // Parentheses
                    question = "What is (" + num1 + " + " + num2 + ") × " + num3 + "?";
                    currentAnswer = (num1 + num2) * num3;
                }
                break;

            default:
                question = "What is 1 + 1?";
                currentAnswer = 2;
                break;
        }

        puzzleQuestionTextView.setText(question);

        // Have Pepper say the question
        if (qiContext != null) {
            currentTask = new MathPuzzleTask(question);
            currentTask.execute();
        }
    }

    private void checkAnswer() {
        String userAnswerStr = answerEditText.getText().toString().trim();

        if (TextUtils.isEmpty(userAnswerStr)) {
            Toast.makeText(this, "Please enter an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int userAnswer = Integer.parseInt(userAnswerStr);
            totalAttempts++;
            totalAttemptsTextView.setText(String.valueOf(totalAttempts));

            boolean isCorrect = (userAnswer == currentAnswer);

            if (isCorrect) {
                correctCount++;
                correctCountTextView.setText(String.valueOf(correctCount));

                feedbackTextView.setText("Correct! Great job!");
                feedbackTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

                // Adjust difficulty based on performance
                if (correctCount % 3 == 0) {
                    increaseDifficulty();
                }

                // Have Pepper celebrate
                if (qiContext != null) {
                    currentTask = new MathPuzzleTask("Correct! Great job!");
                    currentTask.execute();
                }
            } else {
                feedbackTextView.setText("Not quite. The answer is " + currentAnswer);
                feedbackTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

                // Have Pepper encourage
                if (qiContext != null) {
                    currentTask = new MathPuzzleTask("Not quite. The answer is " + currentAnswer + ". Let's try another one!");
                    currentTask.execute();
                }
            }

            feedbackTextView.setVisibility(View.VISIBLE);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    private void increaseDifficulty() {
        switch (currentDifficulty) {
            case EASY:
                currentDifficulty = Difficulty.MEDIUM;
                Toast.makeText(this, "Level up! Medium difficulty puzzles", Toast.LENGTH_SHORT).show();
                break;
            case MEDIUM:
                currentDifficulty = Difficulty.HARD;
                Toast.makeText(this, "Level up! Hard difficulty puzzles", Toast.LENGTH_SHORT).show();
                break;
            case HARD:
                // Already at max difficulty
                break;
        }
    }

    // AsyncTask to handle robot interactions
    private class MathPuzzleTask extends AsyncTask<Void, String, Boolean> {
        private String text;

        public MathPuzzleTask(String text) {
            this.text = text;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Create a say action
                Say say = SayBuilder.with(qiContext)
                        .withText(text)
                        .build();

                // Run the say action
                say.run();

                // If it's a correct answer, do a happy animation
                if (text.contains("Correct")) {
                    try {
                        // Create an animation from the raw resource
                        Animation animation = AnimationBuilder.with(qiContext)
                                .withResources(R.raw.nicereaction_a001)
                                .build();

                        // Create an animate action
                        animate = AnimateBuilder.with(qiContext)
                                .withAnimation(animation)
                                .build();

                        // Run the animate action
                        animate.run();
                    } catch (Exception e) {
                        Log.e(TAG, "Error during animation", e);
                    }
                }

                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error during robot interaction", e);
                return false;
            }
        }
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;
        Log.i(TAG, "Robot focus gained");
    }

    @Override
    public void onRobotFocusLost() {
        this.qiContext = null;
        Log.i(TAG, "Robot focus lost");
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.e(TAG, "Robot focus refused: " + reason);
    }

    @Override
    protected void onDestroy() {
        // Unregister the robot lifecycle callbacks
        QiSDK.unregister(this, this);
        super.onDestroy();
    }
}
