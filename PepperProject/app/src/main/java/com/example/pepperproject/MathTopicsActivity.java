package com.example.pepperproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Say;

public class MathTopicsActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private QiContext qiContext;
    private Button additionButton;
    private Button subtractionButton;
    private Button multiplicationButton;
    private Button divisionButton;
    private Button mathPuzzlesButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_topics);

        // Register for robot lifecycle callbacks
        QiSDK.register(this, this);

        // Initialize UI elements
        initializeViews();

        // Set up button click listeners
        setupButtonListeners();
    }

    private void initializeViews() {
        additionButton = findViewById(R.id.additionButton);
        subtractionButton = findViewById(R.id.subtractionButton);
        multiplicationButton = findViewById(R.id.multiplicationButton);
        divisionButton = findViewById(R.id.divisionButton);
        mathPuzzlesButton = findViewById(R.id.mathPuzzlesButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupButtonListeners() {
        additionButton.setOnClickListener(v -> {
            explainAddition();
        });

        subtractionButton.setOnClickListener(v -> {
            explainSubtraction();
        });

        multiplicationButton.setOnClickListener(v -> {
            explainMultiplication();
        });

        divisionButton.setOnClickListener(v -> {
            Toast.makeText(this, "Division topic coming soon!", Toast.LENGTH_SHORT).show();
        });

        mathPuzzlesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MathTopicsActivity.this, MathPuzzlesActivity.class);
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void explainAddition() {
        if (qiContext == null) {
            Toast.makeText(this, "Robot not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable buttons during explanation
        setButtonsEnabled(false);
        
        // Show a toast to indicate the explanation is starting
        Toast.makeText(this, "Explaining addition...", Toast.LENGTH_SHORT).show();

        // Run the explanation in a background thread
        new Thread(() -> {
            try {
                // Create and run the Say action
                String explanation = "Addition is when we combine numbers together. " +
                        "For example, if you have 2 apples and I give you 3 more, " +
                        "you now have 2 plus 3, which equals 5 apples. " +
                        "The plus sign means we are adding numbers together.";
                
                Say say = SayBuilder.with(qiContext)
                        .withText(explanation)
                        .build();
                say.run();
                
                // Re-enable buttons on the UI thread after explanation is done
                runOnUiThread(() -> setButtonsEnabled(true));
            } catch (Exception e) {
                // Handle any errors
                runOnUiThread(() -> {
                    Toast.makeText(MathTopicsActivity.this, 
                            "Error: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    setButtonsEnabled(true);
                });
            }
        }).start();
    }
    private void explainSubtraction() {
        if (qiContext == null) {
            Toast.makeText(this, "Robot not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        setButtonsEnabled(false);
        Toast.makeText(this, "Explaining subtraction...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                String explanation = "Subtraction is when we take something away. " +
                        "If you have 5 candies and eat 2, you are left with 3. " +
                        "That's 5 minus 2 equals 3.";
                Say say = SayBuilder.with(qiContext).withText(explanation).build();
                say.run();

                runOnUiThread(() -> setButtonsEnabled(true));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MathTopicsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setButtonsEnabled(true);
                });
            }
        }).start();
    }
    private void explainMultiplication() {
        if (qiContext == null) {
            Toast.makeText(this, "Robot not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        setButtonsEnabled(false);
        Toast.makeText(this, "Explaining multiplication...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                String explanation = "Multiplication is repeated addition. " +
                        "If you have 3 bags and each bag has 4 apples, you have 3 times 4, which is 12 apples.";
                Say say = SayBuilder.with(qiContext).withText(explanation).build();
                say.run();

                runOnUiThread(() -> setButtonsEnabled(true));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MathTopicsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setButtonsEnabled(true);
                });
            }
        }).start();
    }
    private void setButtonsEnabled(boolean enabled) {
        additionButton.setEnabled(enabled);
        subtractionButton.setEnabled(enabled);
        multiplicationButton.setEnabled(enabled);
        divisionButton.setEnabled(enabled);
        mathPuzzlesButton.setEnabled(enabled);
        backButton.setEnabled(enabled);
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;
        
        // Welcome message
        new Thread(() -> {
            try {
                Say welcome = SayBuilder.with(qiContext)
                        .withText("Welcome to Math Topics! Choose a topic to learn about, or try some math puzzles.")
                        .build();
                welcome.run();
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MathTopicsActivity.this, 
                            "Error in welcome message: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    public void onRobotFocusLost() {
        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Handle focus refused
        Toast.makeText(this, "Robot focus refused: " + reason, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }
}
