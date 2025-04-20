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
            explainMathOperation("addition");
        });

        subtractionButton.setOnClickListener(v -> {
            explainMathOperation("subtraction");
        });

        multiplicationButton.setOnClickListener(v -> {
            explainMathOperation("multiplication");
        });

        divisionButton.setOnClickListener(v -> {
            explainMathOperation("division");
        });

        mathPuzzlesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MathTopicsActivity.this, MathPuzzlesActivity.class);
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void explainMathOperation(String operation) {
        if (qiContext == null) {
            Toast.makeText(this, getString(R.string.status_error_robot), Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable buttons during explanation
        setButtonsEnabled(false);
        
        // Get the appropriate string resources based on the operation
        int explanationStartResId;
        int explanationTextResId;
        
        switch (operation) {
            case "subtraction":
                explanationStartResId = R.string.explaining_subtraction;
                explanationTextResId = R.string.subtraction_explanation;
                break;
            case "multiplication":
                explanationStartResId = R.string.explaining_multiplication;
                explanationTextResId = R.string.multiplication_explanation;
                break;
            case "division":
                explanationStartResId = R.string.explaining_division;
                explanationTextResId = R.string.division_explanation;
                break;
            case "addition":
            default:
                explanationStartResId = R.string.explaining_addition;
                explanationTextResId = R.string.addition_explanation;
                break;
        }
        
        // Show a toast to indicate the explanation is starting
        Toast.makeText(this, getString(explanationStartResId), Toast.LENGTH_SHORT).show();

        // Run the explanation in a background thread
        final int finalExplanationTextResId = explanationTextResId;
        new Thread(() -> {
            try {
                // Create and run the Say action
                String explanation = getString(finalExplanationTextResId);
                
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
                        .withText(getString(R.string.math_topics_welcome))
                        .build();
                welcome.run();
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MathTopicsActivity.this, 
                            getString(R.string.error_welcome_message, e.getMessage()), 
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
        Toast.makeText(this, getString(R.string.status_connection_refused, reason), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }
}
