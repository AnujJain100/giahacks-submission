package com.google.ar.sceneform.samples.gltf;

import android.graphics.Bitmap;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.SafetySetting;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Collections;

public class GeminiPro {
    public static void getResponse(ChatFutures chatModel, String query, Bitmap image, ResponseCallback callback){

        Content.Builder userContentBuilder = new Content.Builder();
        userContentBuilder.setRole("user");
        userContentBuilder.addImage(image);
        userContentBuilder.addText(query);
        Content userContent = userContentBuilder.build();

        // Get the streaming response
        Publisher<GenerateContentResponse> streamingResponse = chatModel.sendMessageStream(userContent);

        // Subscribe to the Publisher
        streamingResponse.subscribe(new Subscriber<GenerateContentResponse>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                s.request(1); // Request one item to start with
            }

            @Override
            public void onNext(GenerateContentResponse generateContentResponse) {
                // Handle each received response chunk
                String resultText = generateContentResponse.getText();
                callback.onResponse(resultText);

                // Request the next item
                subscription.request(1);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                callback.onError(t);
            }

            @Override
            public void onComplete() {
                // Streaming is complete
                callback.onResponse("Streaming complete.");
            }
        });
    }

    // Other methods...

    public GenerativeModelFutures getModel() {
        String apiKey = BuildConfig.apiKey;

        SafetySetting harrassmentSafety = new SafetySetting(HarmCategory.HARASSMENT,
                BlockThreshold.ONLY_HIGH);

        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = 0.9f;
        configBuilder.topP = 1f;
        configBuilder.maxOutputTokens = 8192;
        configBuilder.responseMimeType = "text/plain";
        GenerationConfig generationConfig = configBuilder.build();

        GenerativeModel model = new GenerativeModel(
                "gemini-1.5-flash",
                apiKey,
                generationConfig,
                Collections.singletonList(harrassmentSafety)
        );
        return GenerativeModelFutures.from(model);
    }
}
