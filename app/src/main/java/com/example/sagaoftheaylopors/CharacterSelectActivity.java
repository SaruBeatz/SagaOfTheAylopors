package com.example.sagaoftheaylopors;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sagaoftheaylopors.auth.AuthRepository;
import com.example.sagaoftheaylopors.cloud.PlaythroughRepository;
import com.example.sagaoftheaylopors.databinding.ActivityCharacterSelectBinding;
import com.google.android.material.snackbar.Snackbar;

public class CharacterSelectActivity extends AppCompatActivity {

    private ActivityCharacterSelectBinding binding;
    private int selectedCharacterIndex = -1; // -1 means no character selected
    private static final int CAT_INDEX = 0; // Only Cat is selectable

    // Character data arrays (4 characters: Cat, King, Princess, Ogre)
    private final int[] characterNameResIds = {
            R.string.character_cat_name,
            R.string.character_king_name,
            R.string.character_princess_name,
            R.string.character_ogre_name
    };

    private final int[] characterDescResIds = {
            R.string.character_cat_desc,
            R.string.character_king_desc,
            R.string.character_princess_desc,
            R.string.character_ogre_desc
    };

    // Character background resources
    private final int[] characterBackgroundResIds = {
            R.drawable.cat_background,
            R.drawable.king_background,
            R.drawable.princess_backround,
            R.drawable.ogre_background
    };

    // Character full-height images
    private final int[] characterFullHeightResIds = {
            R.drawable.cat_full_height,
            R.drawable.unavailable_full_height, // King
            R.drawable.unavailable_full_height, // Princess
            R.drawable.unavailable_full_height  // Ogre
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCharacterSelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up character icon click listeners
        binding.characterIconContainer1.setOnClickListener(v -> onCharacterIconClicked(0));
        binding.characterIconContainer2.setOnClickListener(v -> onCharacterIconClicked(1));
        binding.characterIconContainer3.setOnClickListener(v -> onCharacterIconClicked(2));
        binding.characterIconContainer4.setOnClickListener(v -> onCharacterIconClicked(3));

        // Set up select button click listener
        binding.selectCompanionButton.setOnClickListener(v -> {
            if (selectedCharacterIndex == CAT_INDEX) {
                com.example.sagaoftheaylopors.data.repository.StoryRepository repository =
                        com.example.sagaoftheaylopors.data.repository.StoryRepository.getInstance(this);
                com.example.sagaoftheaylopors.data.entities.PlayerProgress progress = repository.getProgress();

                String characterName = "cat";
                if (progress != null) {
                    progress.selectedCharacter = characterName;
                    repository.saveProgress(progress);
                }

                AuthRepository authRepository = new AuthRepository();
                if (authRepository.isLoggedIn()) {
                    binding.selectCompanionButton.setEnabled(false);
                    PlaythroughRepository.getInstance(this)
                            .createPlaythrough(this, characterName)
                            .addOnCompleteListener(task -> {
                                binding.selectCompanionButton.setEnabled(true);
                                if (task.isSuccessful()) {
                                    goToMap();
                                } else {
                                    Snackbar.make(binding.getRoot(), R.string.cloud_save_failed, Snackbar.LENGTH_LONG).show();
                                }
                            });
                } else {
                    goToMap();
                }
            }
        });

        // Initial selection - show Cat
        onCharacterIconClicked(CAT_INDEX);
    }

    private void onCharacterIconClicked(int index) {
        // Animate the clicked icon (pop-out effect)
        View clickedIcon = getIconContainer(index);
        if (clickedIcon != null) {
            animateIconClick(clickedIcon);
        }

        // Update character background with smooth transition
        updateCharacterBackground(index);

        // Update character information
        updateCharacterInfo(index);

        // Update full-height image with animation
        updateFullHeightImage(index);

        // Update button state
        updateSelectButton(index);

        selectedCharacterIndex = index;
    }

    private void updateCharacterBackground(int index) {
        if (index < 0 || index >= characterBackgroundResIds.length) {
            return;
        }

        ImageView backgroundView = binding.characterBackgroundImageView;
        int newBackgroundResId = characterBackgroundResIds[index];

        // Create fade out animation
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(backgroundView, "alpha", 1.0f, 0.0f);
        fadeOut.setDuration(300);
        fadeOut.setInterpolator(new AccelerateDecelerateInterpolator());

        // Create fade in animation
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(backgroundView, "alpha", 0.0f, 1.0f);
        fadeIn.setDuration(300);
        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());

        // Chain animations
        fadeOut.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // Change background resource
                backgroundView.setImageResource(newBackgroundResId);
                // Start fade in
                fadeIn.start();
            }
        });

        // Start fade out
        fadeOut.start();
    }

    private void animateIconClick(View icon) {
        // Scale animation for pop-out effect
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(icon, "scaleX", 1.0f, 1.2f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(icon, "scaleY", 1.0f, 1.2f, 1.0f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    private void updateFullHeightImage(int index) {
        if (index < 0 || index >= characterFullHeightResIds.length) {
            return;
        }

        ImageView fullHeightView = binding.characterFullHeightImageView;
        int newImageResId = characterFullHeightResIds[index];

        // Set the new image resource first
        fullHeightView.setImageResource(newImageResId);
        fullHeightView.setContentDescription(getString(characterNameResIds[index]));

        // Wait for layout to be measured, then calculate positions
        fullHeightView.post(() -> {
            // Get layout positions (relative to parent)
            int imageLeft = fullHeightView.getLeft();
            int frameRight = binding.characterFrameCardView.getRight();
            
            // Calculate start position: right edge of frame relative to image's left edge
            // translationX is relative to the view's layout position
            float startX = frameRight - imageLeft;
            float endX = 0f; // Normal position (translationX = 0)

            // Set initial position (off-screen to the right)
            fullHeightView.setTranslationX(startX);

            // Animate slide-in from right
            ObjectAnimator slideIn = ObjectAnimator.ofFloat(fullHeightView, "translationX", startX, endX);
            slideIn.setDuration(500);
            slideIn.setInterpolator(new AccelerateDecelerateInterpolator());
            slideIn.start();
        });
    }

    private void updateCharacterInfo(int index) {
        if (index < 0 || index >= characterNameResIds.length) {
            return;
        }

        // Show the info container with animation
        if (binding.characterInfoContainer.getVisibility() == View.GONE || 
            binding.characterInfoContainer.getVisibility() == View.INVISIBLE) {
            binding.characterInfoContainer.setVisibility(View.VISIBLE);
            binding.characterInfoContainer.setAlpha(0f);
            binding.characterInfoContainer.animate()
                    .alpha(1.0f)
                    .setDuration(200)
                    .setListener(null);
        }

        // Update character name and description
        binding.characterInfoName.setText(characterNameResIds[index]);
        binding.characterInfoDescription.setText(characterDescResIds[index]);

        // Show/hide availability message
        if (index == CAT_INDEX) {
            // Cat is available
            binding.characterInfoAvailability.setVisibility(View.GONE);
        } else {
            // Other characters are unavailable
            binding.characterInfoAvailability.setVisibility(View.VISIBLE);
            binding.characterInfoAvailability.setText(R.string.character_unavailable);
        }
    }

    private void updateSelectButton(int index) {
        // Enable button only if Cat is selected
        boolean enabled = (index == CAT_INDEX);
        binding.selectCompanionButton.setEnabled(enabled);
        binding.selectCompanionButton.setClickable(enabled);
        
        // Visual feedback for enabled/disabled state
        binding.selectCompanionButton.animate()
                .alpha(enabled ? 1.0f : 0.5f)
                .setDuration(200)
                .setListener(null);
    }

    private void goToMap() {
        Intent intent = new Intent(CharacterSelectActivity.this, MapActivity.class);
        intent.putExtra("selected_character", selectedCharacterIndex);
        startActivity(intent);
    }

    private View getIconContainer(int index) {
        switch (index) {
            case 0:
                return binding.characterIconContainer1;
            case 1:
                return binding.characterIconContainer2;
            case 2:
                return binding.characterIconContainer3;
            case 3:
                return binding.characterIconContainer4;
            default:
                return null;
        }
    }

    @Override
    public void onBackPressed() {
        // Go back to Main Menu instead of previous screen
        Intent intent = new Intent(CharacterSelectActivity.this, MainMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
