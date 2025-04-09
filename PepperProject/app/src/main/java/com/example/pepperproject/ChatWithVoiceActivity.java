package com.example.pepperproject;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;


import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.Say;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatWithVoiceActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private QiContext qiContext;
    private static final String TAG = "ChatWithVoice";
    private static final String OPENAI_API_KEY = BuildConfig.OPENAI_API_KEY;

    private EditText emulatorInput;
    private Button sendButton, backButton;
    private ToggleButton voiceToggle;
    private TextView chatResponseText;

    private boolean isVoiceEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_with_voice);

        emulatorInput = findViewById(R.id.emulatorInput);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);
        voiceToggle = findViewById(R.id.voiceToggle);
        chatResponseText = findViewById(R.id.chatResponseText);

        isVoiceEnabled = voiceToggle.isChecked();

        voiceToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isVoiceEnabled = isChecked;
            Toast.makeText(this, "Voice " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
        });

        backButton.setOnClickListener(v -> finish());

        if (isRunningOnPepper()) {
            emulatorInput.setVisibility(View.GONE);
            sendButton.setVisibility(View.GONE);
            QiSDK.register(this, this);
        } else {
            emulatorInput.setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.VISIBLE);
            sendButton.setOnClickListener(v -> {
                String input = emulatorInput.getText().toString().trim();
                if (!input.isEmpty()) {
                    chatResponseText.setText("You: " + input);
                    callChatGPTAsync(input);
                }
            });
        }
    }

    private boolean isRunningOnPepper() {
        return getPackageManager().hasSystemFeature("com.aldebaran.qi.sdk");
    }

    @Override
    public void onRobotFocusGained(QiContext context) {
        this.qiContext = context;
        if (isVoiceEnabled) {
            listenAndTalk();
        }
    }

    private void listenAndTalk() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String userText = "";
            String response;

            try {
                PhraseSet phraseSet = PhraseSetBuilder.with(qiContext)
                        .withTexts("Hello", "Hi", "What is coding", "Who are you", "How are you")
                        .build();

                Listen listen = ListenBuilder.with(qiContext)
                        .withPhraseSet(phraseSet)
                        .build();

                ListenResult result = listen.run();
                Phrase heardPhrase = result.getHeardPhrase();
                userText = heardPhrase.getText();

                Log.i(TAG, "User said: " + userText);
                response = callChatGPT(userText);

            } catch (Exception e) {
                Log.e(TAG, "Listening error", e);
                response = "Oops! I couldn't hear you.";
            }

            String finalUserText = userText;
            String finalResponse = response;

            handler.post(() -> {
                if (qiContext != null) {
                    Say say = SayBuilder.with(qiContext).withText(finalResponse).build();
                    say.run();
                }
                chatResponseText.setText("You: " + finalUserText + "\nPepper: " + finalResponse);

                if (isVoiceEnabled) {
                    listenAndTalk(); // Loop again
                }
            });
        });
    }


    private void callChatGPTAsync(String prompt) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String response = callChatGPT(prompt);

            handler.post(() -> {
                chatResponseText.setText(chatResponseText.getText() + "\nPepper: " + response);
                if (isVoiceEnabled && qiContext != null) {
                    Say say = SayBuilder.with(qiContext).withText(response).build();
                    say.run();
                }
            });
        });
    }


    private String callChatGPT(String prompt) {
        try {
            URL url = new URL("https://api.openai.com/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + OPENAI_API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "gpt-3.5-turbo");
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", "You are a friendly robot helping kids learn."));
            messages.put(new JSONObject().put("role", "user").put("content", prompt));
            jsonBody.put("messages", messages);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.toString().getBytes("utf-8"));
            }

            int statusCode = conn.getResponseCode();
            Log.d(TAG, "HTTP Status: " + statusCode);

            InputStream inputStream = (statusCode >= 200 && statusCode < 300) ?
                    conn.getInputStream() : conn.getErrorStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            Log.d(TAG, "API response: " + result.toString());

            if (statusCode >= 200 && statusCode < 300) {
                JSONObject jsonResponse = new JSONObject(result.toString());
                return jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            } else {
                return "Failed to get response from ChatGPT.";
            }

        } catch (Exception e) {
            Log.e(TAG, "API error", e);
            return "Sorry, something went wrong while contacting ChatGPT.";
        }
    }


    @Override
    public void onRobotFocusLost() {
        qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.e(TAG, "Focus refused: " + reason);
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }
}
