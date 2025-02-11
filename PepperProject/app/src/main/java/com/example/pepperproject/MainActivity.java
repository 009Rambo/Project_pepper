package com.example.pepperproject;

import android.os.Bundle;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.conversation.Say;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "MainActivity";
    private QiContext qiContext;
    // Store the Animate action to remove listeners later if needed.
    private Animate animate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register for robot lifecycle callbacks.
        QiSDK.register(this, this);
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

        // 1. Create a say action.
        Say say = SayBuilder.with(qiContext)
                .withText("Hello human! How are you?")
                .build();

        // Run the say action asynchronously using the async() method.
        Future<Void> sayFuture = say.async().run();

        // When the say action is complete, run the animation.
        sayFuture.thenConsume(sayResult -> {
            // 2. Build the animation using your imported dance_b001.qianim file.
            Animation animation = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.dance_b001) // Ensure the file is named "dance_b001.qianim" in res/raw.
                    .build();

            // 3. Create the animate action.
            animate = AnimateBuilder.with(qiContext)
                    .withAnimation(animation)
                    .build();

            // Add a listener to log when the animation starts.
            animate.addOnStartedListener(() -> Log.i(TAG, "Animation started."));

            // Run the animate action asynchronously.
            Future<Void> animateFuture = animate.async().run();

            // Log the result when the animation finishes.
            animateFuture.thenConsume(future -> {
                if (future.isSuccess()) {
                    Log.i(TAG, "Animation finished with success.");
                } else if (future.hasError()) {
                    Log.e(TAG, "Animation finished with error.", future.getError());
                }
            });
        });
    }

    @Override
    public void onRobotFocusLost() {
        // Remove listeners to prevent memory leaks.
        if (animate != null) {
            animate.removeAllOnStartedListeners();
        }
        qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.e(TAG, "Robot focus refused: " + reason);
    }
}