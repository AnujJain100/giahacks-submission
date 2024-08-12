package com.google.ar.sceneform.samples.gltf;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        findViewById(R.id.emergency_option).setOnClickListener(v -> {
            Intent intent = new Intent(LandingActivity.this, MainActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.practice_option).setOnClickListener(v -> {
            Intent intent = new Intent(LandingActivity.this, PracticeMode.class);
            startActivity(intent);
        });

        ImageView infoIcon = findViewById(R.id.info_icon);
        infoIcon.setOnClickListener(v -> showInstructionsPopup(v));
    }

    private void showInstructionsPopup(View anchorView) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_instructions, null);
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);

        ImageView closeIcon = popupView.findViewById(R.id.close_icon);
        closeIcon.setOnClickListener(v -> popupWindow.dismiss());

        popupWindow.showAtLocation(anchorView, 0, 0, 0);
    }
}