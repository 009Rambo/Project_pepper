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
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.Window;

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
    private boolean explanationShown = false;
    private boolean introductionShown = false;

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

        // Show explanation dialog
        showExplanationDialog();
    }

    private void showExplanationDialog() {
        if (explanationShown) {
            return;
        }
        
        // Create the custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_math_puzzle_explanation);
        
        // Set dialog width to match parent
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    android.view.WindowManager.LayoutParams.MATCH_PARENT,
                    android.view.WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
        
        // Set up the start button click listener
        Button startButton = dialog.findViewById(R.id.btnStartPuzzles);
        startButton.setOnClickListener(v -> {
            dialog.dismiss();
            
            // Have Pepper introduce the puzzles if available
            if (qiContext != null) {
                currentTask = new MathPuzzleTask(getString(R.string.intro_message));
                currentTask.execute();
            }
            
            // Set the introduction shown flag
            introductionShown = true;
            
            // Generate the first puzzle after the introduction
            generateNewPuzzle();
        });
        
        // Show the dialog
        dialog.show();
        explanationShown = true;
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
        // Clear previous feedback
        feedbackTextView.setText("");
        feedbackTextView.setVisibility(View.GONE);
        
        // Clear previous answer
        answerEditText.setText("");
        
        // Generate a new puzzle based on current difficulty
        int num1, num2, result;
        String operator;
        Random random = new Random();
        
        switch (currentDifficulty) {
            case EASY:
                // Simple addition and subtraction with numbers 1-10
                num1 = random.nextInt(10) + 1;
                num2 = random.nextInt(10) + 1;
                
                // Ensure subtraction doesn't result in negative numbers
                if (random.nextBoolean()) {
                    operator = "+";
                    result = num1 + num2;
                } else {
                    operator = "-";
                    // Swap if needed to avoid negative results
                    if (num1 < num2) {
                        int temp = num1;
                        num1 = num2;
                        num2 = temp;
                    }
                    result = num1 - num2;
                }
                break;
                
            case MEDIUM:
                // More operations and larger numbers
                int operation = random.nextInt(3); // 0: add, 1: subtract, 2: multiply
                
                if (operation == 0) { // Addition
                    num1 = random.nextInt(20) + 1;
                    num2 = random.nextInt(20) + 1;
                    operator = "+";
                    result = num1 + num2;
                } else if (operation == 1) { // Subtraction
                    num1 = random.nextInt(30) + 10; // 10-40
                    num2 = random.nextInt(num1); // Ensure positive result
                    operator = "-";
                    result = num1 - num2;
                } else { // Multiplication
                    num1 = random.nextInt(10) + 1; // 1-10
                    num2 = random.nextInt(5) + 1; // 1-5
                    operator = "×";
                    result = num1 * num2;
                }
                break;
                
            case HARD:
                // Complex operations
                int hardOperation = random.nextInt(4); // 0: add, 1: subtract, 2: multiply, 3: divide
                
                if (hardOperation == 0) { // Addition with larger numbers
                    num1 = random.nextInt(50) + 10;
                    num2 = random.nextInt(50) + 10;
                    operator = "+";
                    result = num1 + num2;
                } else if (hardOperation == 1) { // Subtraction with larger numbers
                    num1 = random.nextInt(100) + 20;
                    num2 = random.nextInt(num1);
                    operator = "-";
                    result = num1 - num2;
                } else if (hardOperation == 2) { // Multiplication
                    num1 = random.nextInt(12) + 1;
                    num2 = random.nextInt(12) + 1;
                    operator = "×";
                    result = num1 * num2;
                } else { // Division with clean results
                    result = random.nextInt(10) + 1; // 1-10
                    num2 = random.nextInt(5) + 2; // 2-6
                    num1 = result * num2; // Ensures clean division
                    operator = "÷";
                }
                break;
                
            default:
                // Default to easy
                num1 = random.nextInt(10) + 1;
                num2 = random.nextInt(10) + 1;
                operator = "+";
                result = num1 + num2;
                break;
        }
        
        // Store the current answer
        currentAnswer = result;
        
        // Set the question text
        String questionFormat = getString(R.string.puzzle_question_format);
        String questionText = String.format(questionFormat, num1, operator, num2);
        puzzleQuestionTextView.setText(questionText);
        
        // Have Pepper ask the question if available
        if (qiContext != null) {
            new QuestionTask(questionText).execute();
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
                // Replace operator symbols with their spoken equivalents
                String spokenText = getSpokenQuestionText(questionText);
                
                // Create and run the say action for the question
                Say say = SayBuilder.with(qiContext)
                        .withText(spokenText)
                        .build();
                say.run();
                
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error during question speech", e);
                return false;
            }
        }
    }
    
    // Helper method to convert visual operators to spoken words
    private String getSpokenQuestionText(String visualText) {
        String spokenText = visualText;
        
        // Replace operator symbols with their spoken equivalents
        spokenText = spokenText.replace("+", " " + getString(R.string.operator_plus) + " ");
        spokenText = spokenText.replace("-", " " + getString(R.string.operator_minus) + " ");
        spokenText = spokenText.replace("×", " " + getString(R.string.operator_multiply) + " ");
        spokenText = spokenText.replace("÷", " " + getString(R.string.operator_divide) + " ");
        
        return spokenText;
    }

    private void checkAnswer() {
        String userAnswerStr = answerEditText.getText().toString().trim();

        if (TextUtils.isEmpty(userAnswerStr)) {
            Toast.makeText(this, getString(R.string.please_enter_answer), Toast.LENGTH_SHORT).show();
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

                feedbackTextView.setText(getString(R.string.correct_feedback_puzzle));
                feedbackTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));

                // Adjust difficulty based on performance
                if (correctCount % 3 == 0) {
                    increaseDifficulty();
                }

                // Have Pepper celebrate
                if (qiContext != null) {
                    currentTask = new MathPuzzleTask(getString(R.string.correct_answer));
                    currentTask.execute();
                }
            } else {
                feedbackTextView.setText(getString(R.string.wrong_feedback_puzzle, currentAnswer));
                feedbackTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));

                // Have Pepper encourage
                if (qiContext != null) {
                    currentTask = new MathPuzzleTask(getString(R.string.wrong_answer_with_correct, currentAnswer));
                    currentTask.execute();
                }
            }

            feedbackTextView.setVisibility(View.VISIBLE);

        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.please_enter_valid_number), Toast.LENGTH_SHORT).show();
        }
    }

    private void increaseDifficulty() {
        switch (currentDifficulty) {
            case EASY:
                currentDifficulty = Difficulty.MEDIUM;
                Toast.makeText(this, getString(R.string.level_up_medium), Toast.LENGTH_SHORT).show();
                break;
            case MEDIUM:
                currentDifficulty = Difficulty.HARD;
                Toast.makeText(this, getString(R.string.level_up_hard), Toast.LENGTH_SHORT).show();
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
                if (text.contains(getString(R.string.correct_answer))) {
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
                else if (text.contains(getString(R.string.wrong_answer))) {
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
        
        // Only speak the current question if the introduction has been shown
        if (introductionShown) {
            String currentQuestion = puzzleQuestionTextView.getText().toString();
            if (!TextUtils.isEmpty(currentQuestion)) {
                new QuestionTask(currentQuestion).execute();
            }
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
