package com.example.sagaoftheaylopors;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sagaoftheaylopors.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private static final int SPLASH_DELAY = 8000; // 8 seconds for animation
    private static final int ANIMATION_DURATION = 7000; // 7 seconds animation duration
    private AnimatorSet animatorSet;
    private FullscreenNavigationHelper navigationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup fullscreen navigation helper
        navigationHelper = new FullscreenNavigationHelper(this);
        
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Wait for layout to be measured before starting animation
        binding.backgroundImageView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        binding.backgroundImageView.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                        setupBackgroundImage();
                        startAnimation();
                    }
                });
    }

    private void setupBackgroundImage() {
        ImageView backgroundImage = binding.backgroundImageView;
        
        // Get image dimensions
        int imageWidth = backgroundImage.getDrawable().getIntrinsicWidth();
        int imageHeight = backgroundImage.getDrawable().getIntrinsicHeight();
        
        if (imageWidth <= 0 || imageHeight <= 0) {
            // Fallback if dimensions not available
            return;
        }
        
        // Get screen dimensions
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        
        // Calculate scale to fill width while maintaining aspect ratio
        float scale = (float) screenWidth / imageWidth;
        int scaledHeight = (int) (imageHeight * scale);
        
        // Set image view dimensions
        android.view.ViewGroup.LayoutParams params = backgroundImage.getLayoutParams();
        params.height = scaledHeight;
        params.width = screenWidth;
        backgroundImage.setLayoutParams(params);
        
        // Use matrix scale type for better control
        backgroundImage.setScaleType(ImageView.ScaleType.MATRIX);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        backgroundImage.setImageMatrix(matrix);
        
        // Set initial position at the bottom (showing bottom part of image)
        // Position so bottom of image aligns with bottom of screen
        float initialY = screenHeight - scaledHeight;
        backgroundImage.setTranslationY(initialY);
    }

    private void startAnimation() {
        ImageView backgroundImage = binding.backgroundImageView;
        View darkOverlay = binding.darkOverlay;
        View warmOverlay = binding.warmOverlay;
        
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int imageHeight = backgroundImage.getLayoutParams().height;
        
        if (imageHeight <= 0) {
            // Fallback: navigate immediately if setup failed
            navigateToMainMenu();
            return;
        }
        
        // Calculate pan distance
        // Start from bottom (showing bottom of image) and pan to show top
        float startY = backgroundImage.getTranslationY();
        float endY = 0; // Top of image aligns with top of screen
        
        // Create pan animation (upward movement)
        ObjectAnimator panAnimator = ObjectAnimator.ofFloat(
                backgroundImage, "translationY", startY, endY
        );
        panAnimator.setDuration(ANIMATION_DURATION);
        panAnimator.setInterpolator(new LinearInterpolator());
        
        // Create dark overlay fade out animation (brightening effect)
        // Start darker, fade to transparent
        darkOverlay.setAlpha(0.7f);
        ObjectAnimator darkFadeAnimator = ObjectAnimator.ofFloat(
                darkOverlay, "alpha", 0.7f, 0.0f
        );
        darkFadeAnimator.setDuration(ANIMATION_DURATION);
        darkFadeAnimator.setInterpolator(new LinearInterpolator());
        
        // Create warm overlay fade in animation (warming effect)
        warmOverlay.setAlpha(0.0f);
        ObjectAnimator warmFadeAnimator = ObjectAnimator.ofFloat(
                warmOverlay, "alpha", 0.0f, 0.5f
        );
        warmFadeAnimator.setDuration(ANIMATION_DURATION);
        warmFadeAnimator.setInterpolator(new LinearInterpolator());
        
        // Combine all animations
        animatorSet = new AnimatorSet();
        animatorSet.playTogether(panAnimator, darkFadeAnimator, warmFadeAnimator);
        animatorSet.start();
        
        // Delay navigation to MainMenuActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            navigateToMainMenu();
        }, SPLASH_DELAY);
    }
    
    private void navigateToMainMenu() {
        Intent intent = new Intent(SplashActivity.this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        if (navigationHelper != null) {
            navigationHelper.cleanup();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Ensure fullscreen is maintained
        if (navigationHelper != null) {
            navigationHelper.applyFullscreen();
        }
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && navigationHelper != null) {
            // Re-apply fullscreen when window gains focus
            navigationHelper.applyFullscreen();
        }
    }
}
