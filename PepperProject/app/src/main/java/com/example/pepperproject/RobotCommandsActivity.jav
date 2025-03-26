package com.example.pepperproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;

public class RobotCommandsActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private QiContext qiContext;
    private TextView statusTextView;
    private Button waveButton, moveForwardButton, speakButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_commands);

        // Register Pepper's lifecycle callbacks
        QiSDK.register(this, this);

        // Initialize UI elements
        statusTextView = findViewById(R.id.statusTextView);
        waveButton = findViewById(R.id.waveButton);
        moveForwardButton = findViewById(R.id.moveForwardButton);
        speakButton = findViewById(R.id.speakButton);

        // Set button actions
        waveButton.setOnClickListener(v -> waveHand());
        moveForwardButton.setOnClickListener(v -> moveForward());
        speakButton.setOnClickListener(v -> makePepperSpeak());
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;
        updateStatus("Connected to Pepper!");
    }

    @Override
    public void onRobotFocusLost() {
        this.qiContext = null;
        updateStatus("Disconnected from Pepper.");
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        updateStatus("Connection refused: " + reason);
    }

    private void updateStatus(String message) {
        runOnUiThread(() -> statusTextView.setText("Status: " + message));
    }

    private void waveHand() {
        if (qiContext != null) {
            updateStatus("Pepper is waving...");
            SayBuilder.with(qiContext)
                    .withText("Hello! I'm Pepper! Watch me wave!")
                    .build()
                    .run();
        } else {
            updateStatus("Error: Robot not connected");
        }
    }

    private void moveForward() {
        if (qiContext != null) {
            updateStatus("Pepper is moving forward...");
            SayBuilder.with(qiContext)
                    .withText("I am moving forward!")
                    .build()
                    .run();
        } else {
            updateStatus("Error: Robot not connected");
        }
    }

    private void makePepperSpeak() {
        if (qiContext != null) {
            updateStatus("Pepper is speaking...");
            SayBuilder.with(qiContext)
                    .withText("Hello! I can understand and respond to your commands!")
                    .build()
                    .run();
        } else {
            updateStatus("Error: Robot not connected");
        }
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }
}
