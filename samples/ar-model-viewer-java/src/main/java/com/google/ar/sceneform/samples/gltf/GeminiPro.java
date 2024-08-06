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
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collections;
import java.util.concurrent.Executor;

public class GeminiPro {
    public static void getResponse(ChatFutures chatModel, String query, Bitmap image, ResponseCallback callback){

        Content.Builder userContentBuilder = new Content.Builder();

        userContentBuilder.setRole("user");
        userContentBuilder.addImage(image);
        userContentBuilder.addText(query);
        Content userContent = userContentBuilder.build();

        Executor executor = Runnable::run;

        ListenableFuture<GenerateContentResponse> response = chatModel.sendMessage(userContent);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                callback.onResponse(resultText);
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
                callback.onError(throwable);
            }
        }, executor);
    }

    public GenerativeModelFutures getModel(){
        String apiKey = BuildConfig2.apiKey;

        SafetySetting harrassmentSafety = new SafetySetting(HarmCategory.HARASSMENT,
                BlockThreshold.ONLY_HIGH);

        // Create the generation config
        GenerationConfig. Builder configBuilder = new GenerationConfig. Builder();
        configBuilder.temperature = 0.9f;
        configBuilder.topP = 1f;
        configBuilder.maxOutputTokens = 8192;
        configBuilder.responseMimeType = "text/plain";
        GenerationConfig generationConfig = configBuilder.build();

        // Instantiate the GenerativeModel
        GenerativeModel model = new GenerativeModel(
                "gemini-1.5-flash",

                apiKey,
                generationConfig,
                Collections.singletonList(harrassmentSafety)
        );
        return GenerativeModelFutures.from(model);
    }
}