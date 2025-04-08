package com.example.pepperproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
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

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class MathPuzzlesActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "MathPuzzlesActivity";
    private static final int SPEECH_REQUEST_CODE = 100;
    private QiContext qiContext;
    private Animate animate;

    // UI Elements
    private TextView puzzleQuestionTextView;
    private EditText answerEditText;
    private Button submitButton;
    private Button voiceInputButton;
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
        voiceInputButton = findViewById(R.id.voiceInputButton);
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

        voiceInputButton.setOnClickListener(v -> {
            startVoiceRecognition();
        });

        nextPuzzleButton.setOnClickListener(v -> {
            generateNewPuzzle();
        });

        backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your answer...");
        
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition not supported on this device", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error starting voice recognition", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                processVoiceInput(spokenText);
            }
        }
    }
    
    private void processVoiceInput(String spokenText) {
        Log.d(TAG, "Voice input received: " + spokenText);
        
        // Try to convert the spoken text to a number
        try {
            // First try direct number conversion
            int userAnswer = parseSpokenNumber(spokenText);
            answerEditText.setText(String.valueOf(userAnswer));
            checkAnswer();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Could not understand the number. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
    
    private int parseSpokenNumber(String spokenText) throws NumberFormatException {
        // Remove any non-alphanumeric characters and convert to lowercase
        String cleanText = spokenText.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        
        // Try direct parsing first
        try {
            return Integer.parseInt(cleanText);
        } catch (NumberFormatException e) {
            // Handle word numbers
            switch (cleanText) {
                case "zero": return 0;
                case "one": return 1;
                case "two": return 2;
                case "three": return 3;
                case "four": return 4;
                case "five": return 5;
                case "six": return 6;
                case "seven": return 7;
                case "eight": return 8;
                case "nine": return 9;
                case "ten": return 10;
                case "eleven": return 11;
                case "twelve": return 12;
                case "thirteen": return 13;
                case "fourteen": return 14;
                case "fifteen": return 15;
                case "sixteen": return 16;
                case "seventeen": return 17;
                case "eighteen": return 18;
                case "nineteen": return 19;
                case "twenty": return 20;
                case "thirty": return 30;
                case "forty": return 40;
                case "fifty": return 50;
                case "sixty": return 60;
                case "seventy": return 70;
                case "eighty": return 80;
                case "ninety": return 90;
                default:
                    // Try to handle compound numbers like "twenty one"
                    if (cleanText.contains("twenty")) {
                        return handleCompoundNumber(cleanText, "twenty", 20);
                    } else if (cleanText.contains("thirty")) {
                        return handleCompoundNumber(cleanText, "thirty", 30);
                    } else if (cleanText.contains("forty")) {
                        return handleCompoundNumber(cleanText, "forty", 40);
                    } else if (cleanText.contains("fifty")) {
                        return handleCompoundNumber(cleanText, "fifty", 50);
                    } else if (cleanText.contains("sixty")) {
                        return handleCompoundNumber(cleanText, "sixty", 60);
                    } else if (cleanText.contains("seventy")) {
                        return handleCompoundNumber(cleanText, "seventy", 70);
                    } else if (cleanText.contains("eighty")) {
                        return handleCompoundNumber(cleanText, "eighty", 80);
                    } else if (cleanText.contains("ninety")) {
                        return handleCompoundNumber(cleanText, "ninety", 90);
                    }
                    throw new NumberFormatException("Could not parse: " + spokenText);
            }
        }
    }
    
    private int handleCompoundNumber(String text, String tens, int tensValue) {
        String remainder = text.replace(tens, "").trim();
        if (remainder.isEmpty()) {
            return tensValue;
        }
        
        try {
            int units = parseSpokenNumber(remainder);
            if (units < 10) {
                return tensValue + units;
            }
        } catch (NumberFormatException e) {
            // Ignore and throw the original exception
        }
        
        throw new NumberFormatException("Could not parse compound number: " + text);
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

        // Have Pepper say the question first, with priority
        if (qiContext != null) {
            // Create a special task that only speaks the question
            new QuestionTask(question).execute();
        }
    }

    // Special task class just for speaking the question
    private class QuestionTask extends AsyncTask<Void, String, Boolean> {
        private String questionText;

        public QuestionTask(String questionText) {
            this.questionText = questionText;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Create and run the say action for the question
                Say say = SayBuilder.with(qiContext)
                        .withText(questionText)
                        .build();
                say.run();
                
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error during question speech", e);
                return false;
            }
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
                feedbackTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));

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
                feedbackTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));

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
                // If it's a correct answer, do a happy animation first, then speak
                if (text.contains("Correct")) {
                    try {
                        // Create an animation from the raw resource
                        Animation animation = AnimationBuilder.with(qiContext)
                                .withResources(R.raw.nicereaction_a002)
                                .build();

                        // Create an animate action
                        animate = AnimateBuilder.with(qiContext)
                                .withAnimation(animation)
                                .build();

                        // Run the animate action first
                        animate.run();
                        
                        // Then create and run the say action
                        Say say = SayBuilder.with(qiContext)
                                .withText(text)
                                .build();
                        say.run();
                    } catch (Exception e) {
                        Log.e(TAG, "Error during animation", e);
                    }
                }
                // If it's a wrong answer, do a negation animation first, then speak
                else if (text.contains("Not quite")) {
                    try {
                        // Create an animation from the raw resource
                        Animation animation = AnimationBuilder.with(qiContext)
                                .withResources(R.raw.negation_both_hands_a002)
                                .build();

                        // Create an animate action
                        animate = AnimateBuilder.with(qiContext)
                                .withAnimation(animation)
                                .build();

                        // Run the animate action first
                        animate.run();
                        
                        // Then create and run the say action
                        Say say = SayBuilder.with(qiContext)
                                .withText(text)
                                .build();
                        say.run();
                    } catch (Exception e) {
                        Log.e(TAG, "Error during animation", e);
                    }
                }
                // For other messages, just say them without animation
                else {
                    // Create and run the say action
                    Say say = SayBuilder.with(qiContext)
                            .withText(text)
                            .build();
                    say.run();
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
        
        // Speak the current question when robot focus is first gained
        // This ensures the first question is spoken even if generateNewPuzzle was called before robot focus was gained
        String currentQuestion = puzzleQuestionTextView.getText().toString();
        if (!TextUtils.isEmpty(currentQuestion)) {
            new QuestionTask(currentQuestion).execute();
        }
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
