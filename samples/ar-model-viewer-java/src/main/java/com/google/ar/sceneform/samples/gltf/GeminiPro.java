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
        userContentBuilder.addText("Medical information provided by the american heart association about aspirin dosage during a heart attack: Taking aspirin during a heart attack Aspirin can help reduceTrusted Source the severity of a heart attack. However, as the American Heart AssociationTrusted Source note, aspirin alone cannot treat a heart attack. Before taking an aspirin for a suspected heart attack, contact 911 or the local emergency number. The operator can advise whether to take aspirin and how much to take. If the operator does not suggest aspirin, the person may receive it in the emergency department. Learn more about what to do during a heart attack. Dose A person can take 160–325 milligrams (mg)Trusted Source of aspirin during a heart attack. The United Kingdom’s National Health Service recommends chewing a 300-mg tablet of aspirin while waiting for the ambulance to arrive. Uncoated aspirin is preferable, as it works faster, but a person can also chew an enteric-coated tablet if uncoated ones are unavailable.");

        userContentBuilder.addText("You are a medical professional with knowledge on different symptoms and their corresponding injuries." +
                "Answer the next prompts relatively concisely. The person in front of me suffered some sort of medical emergency relating to chest pain."
              );
//        userContentBuilder.addText("You are a medical professional with knowledge on different symptoms and their corresponding injuries." +
//                "Answer all my next prompts very concisely." +
//                "\"The person in front of me suffered some sort of medical\" +\n" +
//                "                \" emergency. Ask me questions about the symptoms that the injured person has sustained. " +
//                "Once you think you have enough information, try to diagnose the condition. You have access to the information given to you. Assume 911 has already been called, your job is to help the user" +
//                "treat the emergency by themselves until first responders arrive to the scene. " +
//                "Once you make a diagnosis, give the user one actionable step at a time. Only ask a few questions. ");
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
                "gemini-1.5-flash",

                apiKey,
                generationConfig,
                Arrays.asList(harrassmentSafety, medical)

        );
        return GenerativeModelFutures.from(model);
    }
}