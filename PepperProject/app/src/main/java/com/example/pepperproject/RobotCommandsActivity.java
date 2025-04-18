package com.example.pepperproject;

import android.os.Bundle;
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
    private Button moveForwardButton, turnLeftButton, turnRightButton, speakButton, voiceCommandButton, backRbButton;
    private EditText speakInput;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Button danceButton;

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

        moveForwardButton.setOnClickListener(v -> moveForward());
        turnLeftButton.setOnClickListener(v -> turnLeft());
        turnRightButton.setOnClickListener(v -> turnRight());
        speakButton.setOnClickListener(v -> speakMessage());
        voiceCommandButton.setOnClickListener(v -> listenForCommands());
        danceButton = findViewById(R.id.danceButton);
        danceButton.setOnClickListener(v -> makePepperDance());
        Button backRbButton = findViewById(R.id.backRbButton);
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
    }

    private void moveForward() {
        if (qiContext != null) {
            // Real Pepper
            executor.execute(() -> {
                try {
                    Transform transform = TransformBuilder.create().fromTranslation(new Vector3(0.3, 0.0, 0.0));
                    FreeFrame freeFrame = qiContext.getMapping().makeFreeFrame();
                    Frame robotFrame = qiContext.getActuation().robotFrame();
                    freeFrame.update(robotFrame, transform, System.nanoTime());
                    Frame targetFrame = freeFrame.frame();

                    Say say = SayBuilder.with(qiContext).withText("Moving forward!").build();
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
        } else {
            // Fallback for emulator testing
            android.util.Log.d("DEBUG", "Emulator fallback → moveForward()");
            android.widget.Toast.makeText(this, "Simulated: Pepper would move forward", Toast.LENGTH_SHORT).show();
        }
    }

    private void turnLeft() {
        if (qiContext != null) {
            executor.execute(() -> {
                try {
                    // Move slightly forward and to the left
                    Transform transform = TransformBuilder.create().fromTranslation(new Vector3(0.0, 0.0, 0.0))
                            .fromRotation(getRotationQuaternion(Math.PI / 2)); // 90° left

                    FreeFrame freeFrame = qiContext.getMapping().makeFreeFrame();
                    Frame robotFrame = qiContext.getActuation().robotFrame();
                    freeFrame.update(robotFrame, transform, System.nanoTime());
                    Frame targetFrame = freeFrame.frame();

                    Say say = SayBuilder.with(qiContext).withText("Turning left!").build();
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
        } else {
            android.util.Log.d("DEBUG", "Emulator fallback → turnLeft()");
            android.widget.Toast.makeText(this, "Simulated: Pepper would turn left", Toast.LENGTH_SHORT).show();
        }
    }

    private void turnRight() {
        if (qiContext != null) {
            executor.execute(() -> {
                try {
                    // Move slightly forward and to the right
                    Transform transform = TransformBuilder.create().fromTranslation(new Vector3(0.0, 0.0, 0.0))
                            .fromRotation(getRotationQuaternion(-Math.PI / 2)); // 90° right

                    FreeFrame freeFrame = qiContext.getMapping().makeFreeFrame();
                    Frame robotFrame = qiContext.getActuation().robotFrame();
                    freeFrame.update(robotFrame, transform, System.nanoTime());
                    Frame targetFrame = freeFrame.frame();

                    Say say = SayBuilder.with(qiContext).withText("Turning right!").build();
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
        } else {
            android.util.Log.d("DEBUG", "Emulator fallback → turnRight()");
            android.widget.Toast.makeText(this, "Simulated: Pepper would turn right", Toast.LENGTH_SHORT).show();
        }
    }

    private Quaternion getRotationQuaternion(double angle) {
        Vector3 axis = new Vector3(0.0, 1.0, 0.0);
        double w = Math.cos(angle / 2);
        double x = axis.getX() * Math.sin(angle / 2);
        double y = axis.getY() * Math.sin(angle / 2);
        double z = axis.getZ() * Math.sin(angle / 2);
        return new Quaternion(x, y, z, w);
    }

    private void speakMessage() {
        String message = speakInput.getText().toString();

        if (qiContext != null) {
            // Run speech on a background thread to avoid crashing
            executor.execute(() -> {
                Say say = SayBuilder.with(qiContext).withText(message).build();
                say.run();
            });
        } else {
            android.util.Log.d("DEBUG", "Emulator fallback → " + message);
            android.widget.Toast.makeText(this, "Pepper says: " + message, Toast.LENGTH_SHORT).show();
        }
    }

    private void listenForCommands() {
        executor.execute(new VoiceCommandTask(this, qiContext));
    }

    // dance feature

    private void makePepperDance() {
        if (qiContext != null) {
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
    }

    // wave hand

    private void waveHand() {
        if (qiContext != null) {
            try {
                Animation animation = AnimationBuilder.with(qiContext)
                        .withResources(R.raw.hello_a001)
                        .build();

                Animate animate = AnimateBuilder.with(qiContext)
                        .withAnimation(animation)
                        .build();

                animate.run();

                Say say = SayBuilder.with(qiContext)
                        .withText("Hello! I'm Pepper!")
                        .build();
                say.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Static class to avoid memory leaks
    private static class VoiceCommandTask implements Runnable {
        private final WeakReference<RobotCommandsActivity> activityRef;
        private final QiContext qiContext;

        VoiceCommandTask(RobotCommandsActivity activity, QiContext context) {
            this.activityRef = new WeakReference<>(activity);
            this.qiContext = context;
        }

        @Override
        public void run() {
            try {
                List<Phrase> phrases = Arrays.asList(
                        new Phrase("move forward"),
                        new Phrase("turn left"),
                        new Phrase("turn right"),
                        new Phrase("say hello"));

                PhraseSet phraseSet = PhraseSetBuilder.with(qiContext)
                        .withPhrases(phrases)
                        .build();

                Listen listen = ListenBuilder.with(qiContext)
                        .withPhraseSet(phraseSet)
                        .build();

                ListenResult result = listen.run();
                String heardPhrase = result.getHeardPhrase().getText().toLowerCase();

                RobotCommandsActivity activity = activityRef.get();
                if (activity != null && activity.qiContext != null) {
                    activity.runOnUiThread(() -> {
                        switch (heardPhrase) {
                            case "move forward":
                                activity.moveForward();
                                break;
                            case "turn left":
                                activity.turnLeft();
                                break;
                            case "turn right":
                                activity.turnRight();
                                break;
                            case "say hello":
                                activity.waveHand(); // Call new method
                                break;

                            default:
                                Say unknownSay = SayBuilder.with(activity.qiContext)
                                        .withText("Sorry, I didn't understand.")
                                        .build();
                                unknownSay.run();
                                break;
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
