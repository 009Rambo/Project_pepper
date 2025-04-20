package com.example.pepperproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.geometry.Quaternion;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.geometry.Vector3;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RobotCommandsActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private QiContext qiContext;
    private Button moveForwardButton, turnLeftButton, turnRightButton, speakButton, voiceCommandButton, backRbButton, danceButton;
    private EditText speakInput;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_commands);

        QiSDK.register(this, this); // Register for robot lifecycle

        moveForwardButton = findViewById(R.id.moveForwardButton);
        turnLeftButton = findViewById(R.id.turnLeftButton);
        turnRightButton = findViewById(R.id.turnRightButton);
        speakButton = findViewById(R.id.speakButton);
        voiceCommandButton = findViewById(R.id.voiceCommandButton);
        speakInput = findViewById(R.id.speakInput);
        danceButton = findViewById(R.id.danceButton);
        backRbButton = findViewById(R.id.backRbButton);

        moveForwardButton.setOnClickListener(v -> moveForward());
        turnLeftButton.setOnClickListener(v -> turnLeft());
        turnRightButton.setOnClickListener(v -> turnRight());
        speakButton.setOnClickListener(v -> speakMessage());
        voiceCommandButton.setOnClickListener(v -> listenForCommands());
        danceButton.setOnClickListener(v -> makePepperDance());
        backRbButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        executor.shutdown();
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
        Log.w("RobotCommandsActivity", "Robot focus refused: " + reason);
    }

    /**
     * Move Pepper 30 cm forward (Real Pepper).
     */
    private void moveForward() {
        if (qiContext == null) return;
        // Real Pepper execution
        executor.execute(() -> {
            try {
                Transform transform = TransformBuilder.create().fromTranslation(new Vector3(0.3, 0.0, 0.0));
                FreeFrame freeFrame = qiContext.getMapping().makeFreeFrame();
                Frame robotFrame = qiContext.getActuation().robotFrame();
                freeFrame.update(robotFrame, transform, System.nanoTime());
                Frame targetFrame = freeFrame.frame();

                Say say = SayBuilder.with(qiContext).withText(getString(R.string.moving_forward)).build();
                say.run();

                GoTo goTo = GoToBuilder.with(qiContext)
                        .withFrame(targetFrame)
                        .build();
                goTo.run();
            } catch (Exception e) {
                Say error = SayBuilder.with(qiContext)
                        .withText("Oops! I can't move right now.")
                        .build();
                error.run();
                e.printStackTrace();
            }
        });
    }

    /**
     * Turn Pepper 90° to the left.
     */
    private void turnLeft() {
        if (qiContext == null) return;
        executor.execute(() -> {
            try {
                double angle = Math.PI / 2; // 90° left
                Quaternion quaternion = getRotationQuaternion(angle);

                Transform transform = TransformBuilder.create().fromRotation(quaternion);
                FreeFrame freeFrame = qiContext.getMapping().makeFreeFrame();
                Frame robotFrame = qiContext.getActuation().robotFrame();
                freeFrame.update(robotFrame, transform, System.nanoTime());
                Frame targetFrame = freeFrame.frame();

                Say say = SayBuilder.with(qiContext).withText(getString(R.string.turning_left)).build();
                say.run();

                GoTo goTo = GoToBuilder.with(qiContext)
                        .withFrame(targetFrame)
                        .build();
                goTo.run();
            } catch (Exception e) {
                Say error = SayBuilder.with(qiContext)
                        .withText("Oops! I can't turn left now.")
                        .build();
                error.run();
                e.printStackTrace();
            }
        });
    }

    /**
     * Turn Pepper 90° to the right.
     */
    private void turnRight() {
        if (qiContext == null) return;
        executor.execute(() -> {
            try {
                double angle = -Math.PI / 2; // 90° right
                Quaternion quaternion = getRotationQuaternion(angle);

                Transform transform = TransformBuilder.create().fromRotation(quaternion);
                FreeFrame freeFrame = qiContext.getMapping().makeFreeFrame();
                Frame robotFrame = qiContext.getActuation().robotFrame();
                freeFrame.update(robotFrame, transform, System.nanoTime());
                Frame targetFrame = freeFrame.frame();

                Say say = SayBuilder.with(qiContext).withText(getString(R.string.turning_right)).build();
                say.run();

                GoTo goTo = GoToBuilder.with(qiContext)
                        .withFrame(targetFrame)
                        .build();
                goTo.run();
            } catch (Exception e) {
                Say error = SayBuilder.with(qiContext)
                        .withText("Oops! I can't turn right now.")
                        .build();
                error.run();
                e.printStackTrace();
            }
        });
    }

    private Quaternion getRotationQuaternion(double angle) {
        Vector3 axis = new Vector3(0.0, 1.0, 0.0);
        double w = Math.cos(angle / 2);
        double x = axis.getX() * Math.sin(angle / 2);
        double y = axis.getY() * Math.sin(angle / 2);
        double z = axis.getZ() * Math.sin(angle / 2);
        return new Quaternion(x, y, z, w);
    }

    /**
     * Run speech in a background thread to avoid blocking the UI.
     */
    private void speakMessage() {
        String message = speakInput.getText().toString();
        if (qiContext == null) return;
        executor.execute(() -> {
            try {
                Say say = SayBuilder.with(qiContext).withText(message).build();
                say.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void listenForCommands() {
        executor.execute(new VoiceCommandTask(this, qiContext));
    }

    /**
     * Play a preset dance animation.
     */
    private void makePepperDance() {
        if (qiContext == null) return;
        try {
            Animation animation = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.dance_b005)
                    .build();

            Animate animate = AnimateBuilder.with(qiContext)
                    .withAnimation(animation)
                    .build();

            animate.run();

            Say say = SayBuilder.with(qiContext)
                    .withText("Time to dance!")
                    .build();
            say.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // wave hand
    private void waveHand() {
        if (qiContext == null) return;
        try {
            Animation animation = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.hello_a001)
                    .build();

            Animate animate = AnimateBuilder.with(qiContext)
                    .withAnimation(animation)
                    .build();

            animate.run();

            Say say = SayBuilder.with(qiContext)
                    .withText(getString(R.string.hello_im_pepper))
                    .build();
            say.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Static class to avoid memory leaks
    private static class Voice
