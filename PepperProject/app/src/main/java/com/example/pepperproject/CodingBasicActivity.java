package com.example.pepperproject;

import android.content.Intent;
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

public class CodingBasicActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "CodingBasicActivity";  // Tag for logging purposes
    private QiContext qiContext;  // QiContext instance to control Pepper robot's behavior

    // Views from the activity's layout
    private TextView questionTextView;  // Displays the current question
    private EditText answerEditText;  // Input field for the user to enter their answer
    private Button submitButton;  // Button to submit the answer
    private TextView feedbackTextView;  // Provides feedback on whether the answer is correct
    private Button nextQuestionButton;  // Button to move to the next question
    private Button backButton;  // Button to go back to the previous screen
    private Button playBlockGameButton;  // Button to launch a block-based game activity
    private Button chatGptButton;

    private String currentQuestion;  // Holds the current question
    private String currentAnswer;  // Holds the correct answer for the current question
    private CodingTask currentTask;  // AsyncTask that handles robot's speech output
    private Animate animate;  // Animation to be played on Pepper robot

    private Random random = new Random();  // Random object to pick a random question


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coding_basic);  // Set the layout for the activity

        QiSDK.register(this, this);  // Register the activity for robot lifecycle callbacks

        initializeViews();  // Initialize the views
        setupButtonListeners();  // Set up listeners for the buttons
        generateNewQuestion();  // Generate and display a new question
    }

    // Initializes the views by binding the corresponding UI components
    private void initializeViews() {
        questionTextView = findViewById(R.id.questionTextView);
        answerEditText = findViewById(R.id.answerEditText);
        submitButton = findViewById(R.id.submitButton);
        feedbackTextView = findViewById(R.id.feedbackTextView);
        nextQuestionButton = findViewById(R.id.nextQuestionButton);
        backButton = findViewById(R.id.backButton);
        playBlockGameButton = findViewById(R.id.playBlockGameButton);
        chatGptButton = findViewById(R.id.chatGptButton);
    }

    // Sets up the button listeners to handle user interactions
    private void setupButtonListeners() {
        submitButton.setOnClickListener(v -> checkAnswer());  // When the user submits their answer
        nextQuestionButton.setOnClickListener(v -> generateNewQuestion());  // When the user requests a new question
        backButton.setOnClickListener(v -> finish());  // When the user wants to exit the activity
        playBlockGameButton.setOnClickListener(v -> {
            // Launches a new activity for a block-based game
            Intent intent = new Intent(CodingBasicActivity.this, BlockGameActivity.class);
            startActivity(intent);
        });
        chatGptButton.setOnClickListener(v -> {
            // Launches a new activity for a chatGPT
            Intent intent = new Intent(CodingBasicActivity.this, ChatWithVoiceActivity.class);
            startActivity(intent);
        });
    }

    // Generates a new question and sets it up in the UI
    private void generateNewQuestion() {
        // Get questions and answers from string resources
        String[] questions = {
            getString(R.string.coding_question_1),
            getString(R.string.coding_question_2),
            getString(R.string.coding_question_3),
            getString(R.string.coding_question_4)
        };
        
        String[] answers = {
            getString(R.string.coding_answer_1),
            getString(R.string.coding_answer_2),
            getString(R.string.coding_answer_3),
            getString(R.string.coding_answer_4)
        };

        // Select a random question and corresponding answer
        int index = random.nextInt(questions.length);
        currentQuestion = questions[index];
        currentAnswer = answers[index];

        // Set the question on the UI and reset the answer field and feedback
        questionTextView.setText(currentQuestion);
        answerEditText.setText("");
        feedbackTextView.setVisibility(View.GONE);

        // If the QiContext is available, run a task to speak the question through Pepper
        if (qiContext != null) {
            currentTask = new CodingTask(currentQuestion);  // Create a new task for the current question
            currentTask.execute();  // Execute the task
        }
    }

    // Checks the user's answer and provides feedback
    private void checkAnswer() {
        String userAnswer = answerEditText.getText().toString().trim().toLowerCase();

        // If the answer field is empty, show a toast message and return
        if (TextUtils.isEmpty(userAnswer)) {
            Toast.makeText(this, getString(R.string.please_enter_answer), Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the user's answer matches the correct answer
        boolean isCorrect = userAnswer.equals(currentAnswer.toLowerCase());

        // Set the feedback text and color based on whether the answer is correct
        feedbackTextView.setText(isCorrect ? getString(R.string.correct_feedback) : getString(R.string.wrong_feedback, currentAnswer));
        feedbackTextView.setTextColor(getResources().getColor(isCorrect ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
        feedbackTextView.setVisibility(View.VISIBLE);

        // If the QiContext is available, speak the feedback text using Pepper
        if (qiContext != null) {
            currentTask = new CodingTask(isCorrect ? getString(R.string.correct_speech) : getString(R.string.wrong_speech, currentAnswer));
            currentTask.execute();  // Execute the task to speak the feedback

            // If the answer is correct, play an animation on the robot
            if (isCorrect) playPepperAnimation();
        }
    }

    // Plays an animation on Pepper when the answer is correct
    private void playPepperAnimation() {
        if (qiContext != null) {
            try {
                // Load an animation from the resources
                Animation animation = AnimationBuilder.with(qiContext)
                        .withResources(R.raw.nicereaction_a001)  // Load the specific animation
                        .build();

                // Create an Animate object to play the animation
                animate = AnimateBuilder.with(qiContext)
                        .withAnimation(animation)
                        .build();

                animate.run();  // Run the animation
            } catch (Exception e) {
                // Log any error that occurs during animation playback
                Log.e(TAG, "Error during animation", e);
            }
        }
    }

    // AsyncTask to handle speaking the text through Pepper
    private class CodingTask extends AsyncTask<Void, Void, Void> {
        private String text;  // The text to be spoken by Pepper

        public CodingTask(String text) {
            this.text = text;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // If the QiContext is available, create and run a Say object to speak the text
            if (qiContext != null) {
                Say say = SayBuilder.with(qiContext)
                        .withText(text)  // Set the text to be spoken
                        .build();
                say.run();  // Run the speech
            }
            return null;
        }
    }

    // Called when the robot focus is gained (connection to Pepper is successful)
    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;  // Store the QiContext for controlling Pepper
        Log.i(TAG, "Robot focus gained");
    }

    // Called when the robot focus is lost (connection to Pepper is lost)
    @Override
    public void onRobotFocusLost() {
        this.qiContext = null;  // Set QiContext to null as the connection is lost
        Log.i(TAG, "Robot focus lost");
    }

    // Called when the robot focus is refused (connection to Pepper was not successful)
    @Override
    public void onRobotFocusRefused(String reason) {
        Log.e(TAG, "Robot focus refused: " + reason);  // Log the reason for refusal
    }

    // Called when the activity is destroyed, unregister the robot lifecycle callbacks
    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);  // Unregister from the QiSDK
        super.onDestroy();
    }
}
