package com.example.sagaoftheaylopors;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sagaoftheaylopors.databinding.ActivityPauseStatsBinding;

public class PauseStatsActivity extends AppCompatActivity {

    private ActivityPauseStatsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPauseStatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Resume Button
        binding.resumeButton.setOnClickListener(v -> finish());

        // Save Button
        binding.saveButton.setOnClickListener(v -> {
            // TODO: Implement save functionality
            finish();
        });

        // Settings Button
        binding.settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(PauseStatsActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Main Menu Button
        binding.mainMenuButton.setOnClickListener(v -> {
            Intent intent = new Intent(PauseStatsActivity.this, MainMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}

