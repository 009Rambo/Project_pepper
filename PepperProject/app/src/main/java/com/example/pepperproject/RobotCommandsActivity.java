package com.example.pepperproject;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.actuation.Transform;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;

import java.util.Arrays;
import java.util.List;

public class RobotCommandsActivity extends RobotActivity {

    private QiContext qiContext;
    private Button moveForwardButton, turnLeftButton, turnRightButton, speakButton, voiceCommandButton;
    private EditText speakInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_commands);

        // Register QiSDK
        QiSDK.register(this, this);

        // Initialize UI elements
        moveForwardButton = findViewById(R.id.moveForwardButton);
        turnLeftButton = findViewById(R.id.turnLeftButton);
        turnRightButton = findViewById(R.id.turnRightButton);
        speakButton = findViewById(R.id.speakButton);
        voiceCommandButton = findViewById(R.id.voiceCommandButton);
        speakInput = findViewById(R.id.speakInput);

        // Set up button listeners
        moveForwardButton.setOnClickListener(v -> moveForward());
        turnLeftButton.setOnClickListener(v -> turnLeft());
        turnRightButton.setOnClickListener(v -> turnRight());
        speakButton.setOnClickListener(v -> speakMessage());
        voiceCommandButton.setOnClickListener(v -> listenForCommands());
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;
    }

    @Override
    public void onRobotFocusLost() {
        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Handle focus refused case
    }

    /**
     * Moves the robot forward by a small distance.
     */
    private void moveForward() {
        if (qiContext != null) {
            Transform transform = Transform.makeTranslation(0.3f, 0f, 0f); // Move 30 cm forward
            GoTo goTo = GoToBuilder.with(qiContext).withFrame(qiContext.getActuation().robotFrame()).withTransform(transform).build();
            goTo.run();

            Say response = SayBuilder.with(qiContext).withText("I am moving forward!").build();
            response.run();
        }
    }

    /**
     * Rotates the robot 90 degrees to the left.
     */
    private void turnLeft() {
        if (qiContext != null) {
            Transform transform = Transform.makeRotation(1.57f, 0f, 0f); // Rotate left (approx. 90 degrees)
            GoTo goTo = GoToBuilder.with(qiContext).withFrame(qiContext.getActuation().robotFrame()).withTransform(transform).build();
            goTo.run();

            Say response = SayBuilder.with(qiContext).withText("Turning left!").build();
            response.run();
        }
    }

    /**
     * Rotates the robot 90 degrees to the right.
     */
    private void turnRight() {
        if (qiContext != null) {
            Transform transform = Transform.makeRotation(-1.57f, 0f, 0f); // Rotate right (approx. 90 degrees)
            GoTo goTo = GoToBuilder.with(qiContext).withFrame(qiContext.getActuation().robotFrame()).withTransform(transform).build();
            goTo.run();

            Say response = SayBuilder.with(qiContext).withText("Turning right!").build();
            response.run();
        }
    }

    /**
     * Makes the robot speak a user-defined message.
     */
    private void speakMessage() {
        if (qiContext != null) {
            String message = speakInput.getText().toString();
            Say say = SayBuilder.with(qiContext).withText(message).build();
            say.run();
        }
    }

    /**
     * Listens for voice commands and executes corresponding actions.
     */
    private void listenForCommands() {
        if (qiContext != null) {
            // Define possible voice commands
            List<String> commands = Arrays.asList("move forward", "turn left", "turn right", "say hello");

            // Build Listen action
            Listen listen = ListenBuilder.with(qiContext)
                    .withPhrases(commands)
                    .build();

            // Start listening
            ListenResult result = listen.run();
            String heardPhrase = result.getHeardPhrase().getText();

            // Determine action based on command
            switch (heardPhrase) {
                case "move forward":
                    moveForward();
                    break;
                case "turn left":
                    turnLeft();
                    break;
                case "turn right":
                    turnRight();
                    break;
                case "say hello":
                    Say helloSay = SayBuilder.with(qiContext).withText("Hello! How can I assist you?").build();
                    helloSay.run();
                    break;
                default:
                    Say unknownCommand = SayBuilder.with(qiContext).withText("I didn't understand. Please try again.").build();
                    unknownCommand.run();
                    break;
            }
        }
    }
}
