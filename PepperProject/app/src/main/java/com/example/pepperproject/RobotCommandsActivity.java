package com.example.pepperproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
    private Button moveForwardButton, turnLeftButton, turnRightButton,
            speakButton, voiceCommandButton, backRbButton, danceButton;
    private EditText speakInput;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_commands);

        QiSDK.register(this, this); // Register for robot lifecycle

        moveForwardButton  = findViewById(R.id.moveForwardButton);
        turnLeftButton     = findViewById(R.id.turnLeftButton);
        turnRightButton    = findViewById(R.id.turnRightButton);
        speakButton        = findViewById(R.id.speakButton);
        voiceCommandButton = findViewById(R.id.voiceCommandButton);
        speakInput         = findViewById(R.id.speakInput);
        danceButton        = findViewById(R.id.danceButton);
        backRbButton       = findViewById(R.id.backRbButton);

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

    @Override public void onRobotFocusGained(QiContext qiContext) { this.qiContext = qiContext; }
    @Override public void onRobotFocusLost()                    { this.qiContext = null;      }
    @Override public void onRobotFocusRefused(String reason)    { Log.w("RobotCommands", "Focus refused: " + reason); }

    /** Move Pepper 30 cm forward (Real Pepper). */
    private void moveForward() {
        if (qiContext != null) {
            executor.execute(() -> {
                try {
                    Transform transform = TransformBuilder.create()
                            .fromTranslation(new Vector3(0.3, 0.0, 0.0));

                    FreeFrame freeFrame = qiContext.getMapping().makeFreeFrame();
                    Frame robotFrame    = qiContext.getActuation().robotFrame();
                    freeFrame.update(robotFrame, transform, System.nanoTime());
                    Frame targetFrame = freeFrame.frame();

                    SayBuilder.with(qiContext)
                            .withText(getString(R.string.moving_forward))
                            .build()
                            .run();

                    GoToBuilder.with(qiContext)
                            .withFrame(targetFrame)
                            .build()
                            .run();
                } catch (Exception e) {
                    SayBuilder.with(qiContext)
                            .withText("Oops! I can't move right now.")
                            .build()
                            .run();
                    e.printStackTrace();
                }
            });
        } else {
            Log.d("DEBUG", "Emulator fallback → moveForward()");
            Toast.makeText(this, "Simulated: Pepper would move forward", Toast.LENGTH_SHORT).show();
        }
    }

    /** Turn Pepper 90° to the left. */
    private void turnLeft() {
        if (qiContext != null) {
            executor.execute(() -> {
                try {
                    Quaternion q = getRotationQuaternion(Math.PI / 2);

                    Transform transform = TransformBuilder.create().fromRotation(q);

                    FreeFrame freeFrame = qiContext.getMapping().makeFreeFrame();
                    Frame robotFrame    = qiContext.getActuation().robotFrame();
                    freeFrame.update(robotFrame, transform, System.nanoTime());
                    Frame targetFrame = freeFrame.frame();

                    SayBuilder.with(qiContext)
                            .withText(getString(R.string.turning_left))
                            .build()
                            .run();

                    GoToBuilder.with(qiContext)
                            .withFrame(targetFrame)
                            .build()
                            .run();
                } catch (Exception e) {
                    SayBuilder.with(qiContext)
                            .withText("Oops! I can't turn left now.")
                            .build()
                            .run();
                    e.printStackTrace();
                }
            });
        } else {
            Log.d("DEBUG", "Emulator fallback → turnLeft()");
            Toast.makeText(this, "Simulated: Pepper would turn left", Toast.LENGTH_SHORT).show();
        }
    }

    /** Turn Pepper 90° to the right. */
    private void turnRight() {
        if (qiContext != null) {
            executor.execute(() -> {
                try {
                    Quaternion q = getRotationQuaternion(-Math.PI / 2);

                    Transform transform = TransformBuilder.create().fromRotation(q);

                    FreeFrame freeFrame = qiContext.getMapping().makeFreeFrame();
                    Frame robotFrame    = qiContext.getActuation().robotFrame();
                    freeFrame.update(robotFrame, transform, System.nanoTime());
                    Frame targetFrame = freeFrame.frame();

                    SayBuilder.with(qiContext)
                            .withText(getString(R.string.turning_right))
                            .build()
                            .run();

                    GoToBuilder.with(qiContext)
                            .withFrame(targetFrame)
                            .build()
                            .run();
                } catch (Exception e) {
                    SayBuilder.with(qiContext)
                            .withText("Oops! I can't turn right now.")
                            .build()
                            .run();
                    e.printStackTrace();
                }
            });
        } else {
            Log.d("DEBUG", "Emulator fallback → turnRight()");
            Toast.makeText(this, "Simulated: Pepper would turn right", Toast.LENGTH_SHORT).show();
        }
    }

    private Quaternion getRotationQuaternion(double angle) {
        Vector3 axis = new Vector3(0.0, 1.0, 0.0);
        double half = angle / 2;
        return new Quaternion(
                axis.getX() * Math.sin(half),
                axis.getY() * Math.sin(half),
                axis.getZ() * Math.sin(half),
                Math.cos(half)
        );
    }

    /** Run speech in a background thread to avoid blocking the UI. */
    private void speakMessage() {
        String message = speakInput.getText().toString();

        if (qiContext != null) {
            executor.execute(() -> {
                try {
                    SayBuilder.with(qiContext).withText(message).build().run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            Log.d("DEBUG", "Emulator fallback → speakMessage()");
            Toast.makeText(this, "Pepper says: " + message, Toast.LENGTH_SHORT).show();
        }
    }

    private void listenForCommands() {
        executor.execute(new VoiceCommandTask(this, qiContext));
    }

    private void makePepperDance() {
        if (qiContext == null) return;
        try {
            Animation animation = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.dance_b005)
                    .build();

            AnimateBuilder.with(qiContext)
                    .withAnimation(animation)
                    .build()
                    .run();

            SayBuilder.with(qiContext)
                    .withText("Time to dance!")
                    .build()
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void waveHand() {
        if (qiContext == null) return;
        try {
            Animation animation = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.hello_a001)
                    .build();

            AnimateBuilder.with(qiContext)
                    .withAnimation(animation)
                    .build()
                    .run();

            SayBuilder.with(qiContext)
                    .withText(getString(R.string.hello_im_pepper))
                    .build()
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class VoiceCommandTask implements Runnable {
        private final WeakReference<RobotCommandsActivity> activityRef;
        private final QiContext qiContext;

        VoiceCommandTask(RobotCommandsActivity activity, QiContext context) {
            this.activityRef = new WeakReference<>(activity);
            this.qiContext   = context;
        }

        @Override
        public void run() {
            RobotCommandsActivity activity = activityRef.get();
            if (activity == null) return;

            try {
                List<Phrase> phrases = Arrays.asList(
                        new Phrase(activity.getString(R.string.voice_command_move_forward)),
                        new Phrase(activity.getString(R.string.voice_command_turn_left)),
                        new Phrase(activity.getString(R.string.voice_command_turn_right)),
                        new Phrase(activity.getString(R.string.voice_command_say_hello))
                );

                PhraseSet phraseSet = PhraseSetBuilder.with(qiContext)
                        .withPhrases(phrases)
                        .build();

                Listen listen = ListenBuilder.with(qiContext)
                        .withPhraseSet(phraseSet)
                        .build();

                ListenResult result = listen.run();
                String heard = result.getHeardPhrase().getText().toLowerCase();

                activity.runOnUiThread(() -> {
                    switch (heard) {
                        case "move forward": activity.moveForward(); break;
                        case "turn left":    activity.turnLeft();   break;
                        case "turn right":   activity.turnRight();  break;
                        case "say hello":    activity.waveHand();   break;
                        default:
                            SayBuilder.with(activity.qiContext)
                                    .withText(activity.getString(R.string.voice_command_not_understood))
                                    .build()
                                    .run();
                            break;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
