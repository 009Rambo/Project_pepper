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
                updateStatus("Starting Coding Basics...");
                // Start the CodingBasicActivity
                Intent intent = new Intent(MainActivity.this, CodingBasicActivity.class);
                startActivity(intent);
                startLearningTopic("Coding Basics");
            } else {
                updateStatus("Error: Robot not connected");
            }
        });

        robotCommandsButton.setOnClickListener(v -> {
            if (qiContext != null) {
                updateStatus("Starting Robot Commands...");
                startLearningTopic("Robot Commands");
            } else {
                updateStatus("Error: Robot not connected");
            }
        });

        mathPuzzlesButton.setOnClickListener(v -> {
            if (qiContext != null) {
                updateStatus("Starting Math Puzzles...");
                // Start the MathPuzzlesActivity
                Intent intent = new Intent(MainActivity.this, MathPuzzlesActivity.class);
                startActivity(intent);
            } else {
                updateStatus("Error: Robot not connected");
            }
        });

        funFactsButton.setOnClickListener(v -> {
            if (qiContext != null) {
                updateStatus("Starting Fun Facts...");
                startLearningTopic("Fun Facts");
            } else {
                updateStatus("Error: Robot not connected");
            }
        });
    }

    private void startLearningTopic(String topic) {
        // Disable all buttons during interaction
        setButtonsEnabled(false);

        // Use AsyncTask to perform operations off the main thread
        currentTask = new StartInteractionTask(topic);
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

        public StartInteractionTask(String topic) {
            this.learningTopic = topic;
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
            // Re-enable buttons after interaction
            setButtonsEnabled(true);

            if (success) {
                updateStatus("Ready for next topic");
            } else {
                updateStatus("Interaction failed");
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
            updateTaskProgress("Preparing to teach " + topic + "...");

            // Create a say action with topic-specific greeting
            String greeting = "Let's learn about " + topic + "! This is going to be fun!";
            Say say = SayBuilder.with(qiContext)
                    .withText(greeting)
                    .build();

            // Run the say action
            updateTaskProgress("Pepper is speaking...");
            say.run();

            // Topic-specific content and animations would go here
            // For now, just a placeholder with different messages per topic
            String topicContent = "";

            switch (topic) {
                case "Coding Basics":
                    topicContent = "Coding is like giving instructions to a computer. Let's learn how to write simple code!";
                    Say step1 = SayBuilder.with(qiContext)
                        .withText("Think of coding like making a sandwich. You give step-by-step instructions!")
                        .build();
                    step1.run();

                    Say step2 = SayBuilder.with(qiContext)
                        .withText("Step one: Take a slice of bread. Step two: Spread peanut butter. Step three: Place another slice on top. Now, we have a sandwich!")
                        .build();
                    step2.run();

                    Say step3 = SayBuilder.with(qiContext)
                        .withText("Similarly, in coding, you give instructions like print Hello World or move forward!")
                        .build();
                    step3.run();

                    Say quiz = SayBuilder.with(qiContext)
                        .withText("What will this code print? Print Hello, World! Is it A: Hello, World! or B: Error?")
                        .build();
                    quiz.run();
                    break;
                case "Robot Commands":
                    topicContent = "Robots like me understand special commands. Let me show you how to control robots!";
                    break;
                case "Math Puzzles":
                    topicContent = "Math can be really fun with puzzles! Let's solve some together!";
                    break;
                case "Fun Facts":
                    String[] funFacts = {
                            "The word 'robot' comes from a Czech word 'robota,' meaning forced labor.\n",
                                    " The first robot was created in 1921, and it was called 'R.U.R.'—short for Rossum’s Universal Robots. It was part of a play by Karel Čapek.\n",
                                    " Pepper is capable of understanding and responding to human emotions!\n",
                                    " Robots are being used to explore distant planets, like Mars.\n",
                                    " Some robots can fold laundry and even play musical instruments!"
                    };
                    // Generate a random index for the array
                    int randomIndex = (int) (Math.random() * funFacts.length);
                    topicContent = funFacts[randomIndex];
                    break;
            }

            // Say the topic content
            Say topicSay = SayBuilder.with(qiContext)
                    .withText(topicContent)
                    .build();
            topicSay.run();

            // Only do animation for specific topics, not for the main menu
            if (!topic.equals("Main Menu")) {
                // Do a little dance or animation
                updateTaskProgress("Showing a fun animation...");
                Animation animation = AnimationBuilder.with(qiContext)
                        .withResources(R.raw.nicereaction_a001)
                        .build();

                animate = AnimateBuilder.with(qiContext)
                        .withAnimation(animation)
                        .build();

                animate.addOnStartedListener(() -> {
                    Log.i(TAG, "Animation started.");
                    updateTaskProgress("Pepper is animating...");
                });

                animate.run();
            }

            // Conclude the topic
            Say conclusion = SayBuilder.with(qiContext)
                    .withText("That was fun! What would you like to learn next?")
                    .build();
            conclusion.run();

            Log.i(TAG, "Learning topic completed: " + topic);
            updateTaskProgress("Completed " + topic + "!");
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
        updateStatus("Connected to robot");
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
                        .withText("Hello there! Welcome to Pepper's Learning Adventure! What would you like to learn today?")
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
        updateStatus("Disconnected from robot");
        Log.i(TAG, "Robot focus lost.");
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        updateStatus("Connection refused: " + reason);
        Log.e(TAG, "Robot focus refused: " + reason);
    }
}