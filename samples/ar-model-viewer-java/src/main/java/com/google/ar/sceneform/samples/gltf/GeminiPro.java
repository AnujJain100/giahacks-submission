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

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Executor;

public class GeminiPro {
    public static void getResponse(ChatFutures chatModel, String query, Bitmap image, ResponseCallback callback){

        Content.Builder userContentBuilder = new Content.Builder();

        userContentBuilder.setRole("user");

        userContentBuilder.addText("You are a medical professional with knowledge on different symptoms and their corresponding injuries." +
                "Answer all my next prompts very concisely." +
                "\"The person in front of me suffered some sort of medical\" +\n" +
                "                \" emergency. Ask me questions about the symptoms that the injured person has sustained. " +
                "Once you think you have enough information, try to diagnose the condition. You have access to the information given to you. Assume 911 has already been called, your job is to help the user" +
                "treat the emergency by themselves until first responders arrive to the scene. " +
                "Once you make a diagnosis, give the user one actionable step at a time. ");
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
                BlockThreshold.NONE);
        SafetySetting medical = new SafetySetting(HarmCategory.DANGEROUS_CONTENT,
                BlockThreshold.NONE);

        // Create the generation config
        GenerationConfig. Builder configBuilder = new GenerationConfig. Builder();
        configBuilder.temperature = 0.9f;
        configBuilder.topP = 1f;
        configBuilder.maxOutputTokens = 8192;
        configBuilder.responseMimeType = "text/plain";
        GenerationConfig generationConfig = configBuilder.build();

        // Instantiate the GenerativeModel
        GenerativeModel model = new GenerativeModel(
                "gemini-1.5-pro",

                apiKey,
                generationConfig,
                Arrays.asList(harrassmentSafety, medical)

        );
        return GenerativeModelFutures.from(model);
    }
}