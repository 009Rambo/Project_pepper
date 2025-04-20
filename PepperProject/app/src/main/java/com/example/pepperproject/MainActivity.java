package com.example.pepperproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

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

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "MainActivity";
    private QiContext qiContext;
    // Store the Animate action to remove listeners later if needed.
    private Animate animate;
    private TextView statusTextView;
    private StartInteractionTask currentTask;

    // UI Elements
    private Button codingBasicsButton;
    private Button robotCommandsButton;
    private Button mathPuzzlesButton;
    private Button funFactsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register for robot lifecycle callbacks.
        QiSDK.register(this, this);

        // Initialize UI elements
        statusTextView = findViewById(R.id.statusTextView);

        // Initialize menu buttons
        codingBasicsButton = findViewById(R.id.codingBasicsButton);
        robotCommandsButton = findViewById(R.id.robotCommandsButton);
        mathPuzzlesButton = findViewById(R.id.mathPuzzlesButton);
        funFactsButton = findViewById(R.id.funFactsButton);

        // Set up button click listeners
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        codingBasicsButton.setOnClickListener(v -> {
            if (qiContext != null) {
                updateStatus(getString(R.string.status_starting_topic, getString(R.string.coding_basics_button)));
                startLearningTopic(getString(R.string.coding_basics_button), CodingBasicActivity.class);
            } else {
                updateStatus(getString(R.string.status_error_robot));
            }
        });

        robotCommandsButton.setOnClickListener(v -> {
            if (qiContext != null) {
                updateStatus(getString(R.string.status_starting_topic, getString(R.string.robot_commands_button)));
                startLearningTopic(getString(R.string.robot_commands_button), RobotCommandsActivity.class);
            } else {
                updateStatus(getString(R.string.status_error_robot));
            }
        });
        
        mathPuzzlesButton.setOnClickListener(v -> {
            if (qiContext != null) {

               /* updateStatus("Starting Math Puzzles...");
                startLearningTopic("Math Puzzles", MathPuzzlesActivity.class);
                 */
              
                updateStatus(getString(R.string.status_starting_topic, getString(R.string.math_topics_button)));
                // Start the MathTopicsActivity
                Intent intent = new Intent(MainActivity.this, MathTopicsActivity.class);
                startActivity(intent);

            } else {
                updateStatus(getString(R.string.status_error_robot));
            }
        });

        funFactsButton.setOnClickListener(v -> {
            if (qiContext != null) {
                updateStatus(getString(R.string.status_starting_topic, getString(R.string.fun_facts_button)));
                startLearningTopic(getString(R.string.fun_facts_button), null);
            } else {
                updateStatus(getString(R.string.status_error_robot));
            }
        });
    }

    private void startLearningTopic(String topic, Class<?> activityToStart) {
        // Disable all buttons during interaction
        setButtonsEnabled(false);

        // Use AsyncTask to perform operations off the main thread
        currentTask = new StartInteractionTask(topic, activityToStart);
        currentTask.execute();
    }

    private void setButtonsEnabled(boolean enabled) {
        codingBasicsButton.setEnabled(enabled);
        robotCommandsButton.setEnabled(enabled);
        mathPuzzlesButton.setEnabled(enabled);
        funFactsButton.setEnabled(enabled);
    }

    // Update status text on UI thread
    private void updateStatus(final String status) {
        runOnUiThread(() -> {
            statusTextView.setText("Status: " + status);
        });
    }

    // AsyncTask to handle operations off the main thread
    private class StartInteractionTask extends AsyncTask<Void, String, Boolean> {
        private String learningTopic;
        private Class<?> activityToStart;

        public StartInteractionTask(String topic, Class<?> activityToStart) {
            this.learningTopic = topic;
            this.activityToStart = activityToStart;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (values.length > 0) {
                updateStatus(values[0]);
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                handleLearningTopic(learningTopic);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error during interaction", e);
                publishProgress("Error: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success && activityToStart != null) {
                // Start the activity after the introduction
                Intent intent = new Intent(MainActivity.this, activityToStart);
                startActivity(intent);
            }
            
            // Re-enable buttons after interaction
            setButtonsEnabled(true);

            if (success) {
                updateStatus(getString(R.string.status_ready_for_next_topic));
            } else {
                updateStatus(getString(R.string.status_interaction_failed));
            }

            // Clear the current task reference
            currentTask = null;
        }

        // Method to be called to update progress
        public void updateProgress(String status) {
            publishProgress(status);
        }
    }

    // Method to handle different learning topics
    private void handleLearningTopic(String topic) {
        try {
            // Common introduction for any topic
            updateTaskProgress(getString(R.string.status_preparing_topic, topic));

            // Topic-specific content and animations would go here
            String topicContent = "";

            // Käytetään suoraan merkkijonojen vertailua
            String codingBasics = getString(R.string.coding_basics_button);
            String robotCommands = getString(R.string.robot_commands_button);
            String mathTopics = getString(R.string.math_topics_button);
            String funFacts = getString(R.string.fun_facts_button);

            if (topic.equals(codingBasics)) {
                topicContent = getString(R.string.coding_basics_content);
            } else if (topic.equals(robotCommands)) {
                topicContent = getString(R.string.robot_commands_content);
            } else if (topic.equals(mathTopics)) {
                topicContent = getString(R.string.math_puzzles_content);
            } else if (topic.equals(funFacts)) {
                String[] funFactsArray = {
                        getString(R.string.fun_fact_1),
                        getString(R.string.fun_fact_2),
                        getString(R.string.fun_fact_3)
                };
                // Generate a random index for the array
                int randomIndex = (int) (Math.random() * funFactsArray.length);
                topicContent = funFactsArray[randomIndex];
            }

            // Say the topic content
            Say topicSay = SayBuilder.with(qiContext)
                    .withText(topicContent)
                    .build();
            topicSay.run();

            // Only do animation for specific topics, not for the main menu
        //    if (!topic.equals("Main Menu")) {
        //        // Do a little dance or animation
        //        updateTaskProgress("Showing a fun animation...");
        //        Animation animation = AnimationBuilder.with(qiContext)
        //                .withResources(R.raw.nicereaction_a001)
        //                .build();

        //        animate = AnimateBuilder.with(qiContext)
        //                .withAnimation(animation)
        //                .build();

        //        animate.addOnStartedListener(() -> {
        //            Log.i(TAG, "Animation started.");
        //            updateTaskProgress("Pepper is animating...");
        //        });

        //        animate.run();
        //    }

            // Conclude the topic
         //   Say conclusion = SayBuilder.with(qiContext)
        //            .withText("That was fun! What would you like to learn next?")
        //            .build();
        //    conclusion.run();

            Log.i(TAG, "Learning topic completed: " + topic);
            updateTaskProgress(getString(R.string.status_completed_topic, topic));
        } catch (Exception e) {
            Log.e(TAG, "Error during learning topic", e);
            throw e;
        }
    }

    // Helper method to publish progress from the AsyncTask
    private void updateTaskProgress(String status) {
        if (currentTask != null) {
            currentTask.updateProgress(status);
        }
    }

    @Override
    protected void onDestroy() {
        // Unregister the robot lifecycle callbacks.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;
        updateStatus(getString(R.string.status_connected));
        Log.i(TAG, "Robot focus gained. You can now interact with the robot.");

        // Greet the user when the robot connection is established
        greetUser();
    }

    private void greetUser() {
        // Run this in a separate thread to avoid blocking the UI
        new Thread(() -> {
            try {
                // Wait a moment to make sure everything is ready
                Thread.sleep(2000);

                // Create and run the greeting
                Say greeting = SayBuilder.with(qiContext)
                        .withText(getString(R.string.robot_greeting))
                        .build();
                greeting.run();
            } catch (Exception e) {
                Log.e(TAG, "Error during greeting", e);
            }
        }).start();
    }

    @Override
    public void onRobotFocusLost() {
        // Remove listeners to prevent memory leaks.
        if (animate != null) {
            animate.removeAllOnStartedListeners();
        }
        qiContext = null;
        updateStatus(getString(R.string.status_disconnected));
        Log.i(TAG, "Robot focus lost.");
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        updateStatus(getString(R.string.status_connection_refused, reason));
        Log.e(TAG, "Robot focus refused: " + reason);
    }
}