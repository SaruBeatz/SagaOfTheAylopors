package com.example.sagaoftheaylopors;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sagaoftheaylopors.data.database.StoryDataInitializer;
import com.example.sagaoftheaylopors.databinding.ActivityMainMenuBinding;

public class MainMenuActivity extends AppCompatActivity {

    private ActivityMainMenuBinding binding;
    private MediaPlayer backgroundMusic;
    private ObjectAnimator darkeningAnimator;
    private Handler animationHandler;
    private static final long ANIMATION_DELAY = 4500; // 4.5 seconds
    private static final long ANIMATION_DURATION = 5000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize story database (Chapter 1 data)
        StoryDataInitializer.initializeChapter1(this);

        // Initialize background music
        initializeBackgroundMusic();

        // Start darkening animation loop
        startDarkeningAnimation();

        // New Game Button
        binding.newGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, CharacterSelectActivity.class);
            startActivity(intent);
        });

        // Continue Button - navigates to MapActivity for now
        binding.continueButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, MapActivity.class);
            startActivity(intent);
        });

        // Settings Button
        binding.settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Exit Button
        binding.exitButton.setOnClickListener(v -> finish());
    }

    private void initializeBackgroundMusic() {
        try {
            backgroundMusic = MediaPlayer.create(this, R.raw.chillmusic);
            if (backgroundMusic != null) {
                backgroundMusic.setLooping(true);
                backgroundMusic.setVolume(0.5f, 0.5f);
                backgroundMusic.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startDarkeningAnimation() {
        animationHandler = new Handler(Looper.getMainLooper());
        scheduleDarkeningAnimation();
    }

    private void scheduleDarkeningAnimation() {
        animationHandler.postDelayed(() -> {
            // Fade in dark overlay (darkening effect) - quick fade in
            View darkOverlay = binding.darkOverlay;
            darkOverlay.setAlpha(0.0f);
            darkeningAnimator = ObjectAnimator.ofFloat(darkOverlay, "alpha", 0.0f, 0.6f);
            darkeningAnimator.setDuration(1000); // 1 second to fade in
            darkeningAnimator.start();

            // After 5 seconds total (including fade in), fade back out
            animationHandler.postDelayed(() -> {
                if (darkeningAnimator != null && darkeningAnimator.isRunning()) {
                    darkeningAnimator.cancel();
                }
                ObjectAnimator fadeOutAnimator = ObjectAnimator.ofFloat(darkOverlay, "alpha", 0.6f, 0.0f);
                fadeOutAnimator.setDuration(1000); // 1 second to fade out
                fadeOutAnimator.start();

                // Schedule next animation cycle after fade out completes
                animationHandler.postDelayed(() -> {
                    scheduleDarkeningAnimation();
                }, 1000);
            }, 5000 - 1000); // 5 seconds total, minus fade in time
        }, ANIMATION_DELAY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }
        if (darkeningAnimator != null) {
            darkeningAnimator.cancel();
        }
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.release();
            backgroundMusic = null;
        }
    }

    @Override
    public void onBackPressed() {
        // Exit app from main menu
        finish();
    }
}

