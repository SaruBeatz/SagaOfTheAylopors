package com.example.sagaoftheaylopors;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.sagaoftheaylopors.auth.AuthFlowHelper;
import com.example.sagaoftheaylopors.auth.SessionManager;
import com.example.sagaoftheaylopors.data.database.StoryDataInitializer;
import com.example.sagaoftheaylopors.databinding.ActivityMainMenuBinding;
import com.google.android.material.snackbar.Snackbar;

public class MainMenuActivity extends AppCompatActivity {

    private ActivityMainMenuBinding binding;
    private SessionManager sessionManager;
    private MediaPlayer backgroundMusic;
    private ObjectAnimator darkeningAnimator;
    private Handler animationHandler;
    private boolean cinematicRunning = false;

    private static final long DARKENING_DELAY_MS = 4500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        StoryDataInitializer.initializeChapter1(this);

        updateContinueButtonState();
        initializeBackgroundMusic();
        startDarkeningAnimation();

        binding.newGameButton.setOnClickListener(v -> startNewGameCinematic());

        binding.continueButton.setOnClickListener(v -> {
            if (sessionManager.isContinueEnabled()) {
                startActivity(new Intent(this, MapActivity.class));
            } else {
                Snackbar.make(
                        binding.getRoot(),
                        getString(R.string.continue_locked_message),
                        Snackbar.LENGTH_LONG
                ).setBackgroundTint(ContextCompat.getColor(this, R.color.color_primary))
                        .setTextColor(ContextCompat.getColor(this, R.color.color_accent))
                        .show();
            }
        });

        binding.settingsButton.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        binding.exitButton.setOnClickListener(v -> finish());
    }

    // ─── Cinematic "New Game" animation ──────────────────────────────────────

    private void startNewGameCinematic() {
        if (cinematicRunning) return;
        cinematicRunning = true;

        stopDarkeningAnimation();

        // Fade out secondary UI
        fadeOutViews(300, 0,
                binding.titleTextView,
                binding.continueButton,
                binding.continueLockedHintText,
                binding.settingsButton,
                binding.exitButton);

        // Fade out New Game button slightly later
        binding.newGameButton.animate()
                .alpha(0f)
                .setDuration(500)
                .setStartDelay(200)
                .start();

        // Zoom into background (camera push toward castle)
        binding.backgroundImageView.animate()
                .scaleX(1.45f)
                .scaleY(1.45f)
                .translationY(-80f)
                .setDuration(1600)
                .start();

        // Darken with cinematic overlay
        binding.cinematicFadeOverlay.setVisibility(View.VISIBLE);
        binding.cinematicFadeOverlay.setAlpha(0f);
        binding.cinematicFadeOverlay.animate()
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(800)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        AuthFlowHelper.startNewGameFlow(MainMenuActivity.this);
                        overridePendingTransition(android.R.anim.fade_in, 0);
                    }
                })
                .start();
    }

    private void fadeOutViews(long duration, long startDelay, View... views) {
        for (View v : views) {
            v.animate().alpha(0f).setDuration(duration).setStartDelay(startDelay).start();
        }
    }

    private void resetMenuState() {
        cinematicRunning = false;

        binding.backgroundImageView.animate().cancel();
        binding.backgroundImageView.setScaleX(1f);
        binding.backgroundImageView.setScaleY(1f);
        binding.backgroundImageView.setTranslationY(0f);

        binding.cinematicFadeOverlay.setAlpha(0f);
        binding.cinematicFadeOverlay.setVisibility(View.INVISIBLE);

        binding.titleTextView.setAlpha(1f);
        binding.newGameButton.setAlpha(1f);
        binding.settingsButton.setAlpha(1f);
        binding.exitButton.setAlpha(1f);
        binding.continueLockedHintText.setAlpha(1f);

        // continueButton alpha is managed by updateContinueButtonState
    }

    // ─── Continue button state ────────────────────────────────────────────────

    private void updateContinueButtonState() {
        boolean enabled = sessionManager.isContinueEnabled();

        binding.continueButton.setBackgroundResource(
                enabled ? R.drawable.button_primary_minimal : R.drawable.button_continue_locked);

        int textColorRes = enabled ? R.color.color_accent : android.R.color.white;
        binding.continueButton.setTextColor(getColor(textColorRes));
        binding.continueButton.setAlpha(enabled ? 1f : 0.75f);

        binding.continueButton.setContentDescription(getString(
                enabled ? R.string.continue_content_description_unlocked
                        : R.string.continue_content_description_locked));

        binding.continueLockedHintText.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }

    // ─── Background music ─────────────────────────────────────────────────────

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

    // ─── Atmospheric darkening loop ───────────────────────────────────────────

    private void startDarkeningAnimation() {
        animationHandler = new Handler(Looper.getMainLooper());
        scheduleDarkeningAnimation();
    }

    private void stopDarkeningAnimation() {
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }
        if (darkeningAnimator != null) {
            darkeningAnimator.cancel();
        }
        binding.darkOverlay.setAlpha(0f);
    }

    private void scheduleDarkeningAnimation() {
        animationHandler.postDelayed(() -> {
            View darkOverlay = binding.darkOverlay;
            darkOverlay.setAlpha(0.0f);
            darkeningAnimator = ObjectAnimator.ofFloat(darkOverlay, "alpha", 0.0f, 0.6f);
            darkeningAnimator.setDuration(1000);
            darkeningAnimator.start();

            animationHandler.postDelayed(() -> {
                if (darkeningAnimator != null && darkeningAnimator.isRunning()) {
                    darkeningAnimator.cancel();
                }
                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(darkOverlay, "alpha", 0.6f, 0.0f);
                fadeOut.setDuration(1000);
                fadeOut.start();
                animationHandler.postDelayed(this::scheduleDarkeningAnimation, 1000);
            }, 4000);
        }, DARKENING_DELAY_MS);
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    protected void onResume() {
        super.onResume();
        resetMenuState();
        updateContinueButtonState();

        if (sessionManager.consumeJustRegistered()) {
            Snackbar.make(
                    binding.getRoot(),
                    getString(R.string.continue_unlocked_snackbar),
                    Snackbar.LENGTH_LONG
            ).setBackgroundTint(ContextCompat.getColor(this, R.color.color_primary))
                    .setTextColor(ContextCompat.getColor(this, R.color.color_accent))
                    .show();
        }

        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.start();
        }

        startDarkeningAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopDarkeningAnimation();
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDarkeningAnimation();
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.release();
            backgroundMusic = null;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
