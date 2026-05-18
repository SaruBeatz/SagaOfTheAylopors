package com.example.sagaoftheaylopors;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sagaoftheaylopors.databinding.ActivityFinalScreenBinding;

/**
 * Final screen displayed after all 7 chapters are completed.
 * Shows thank you message, player statistics, and developer information.
 */
public class FinalScreenActivity extends AppCompatActivity {
    private static final String TAG = "FinalScreenActivity";
    private ActivityFinalScreenBinding binding;
    private MusicManager musicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFinalScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        musicManager = MusicManager.getInstance();
        
        // Play celebratory music (use pause music as placeholder for now)
        // Future: dedicated end-game music track
        musicManager.playPauseMusic(this);

        // Set developer info (already set in XML via string resources)
        // Developer info is already displayed via string resources in the layout

        // Set playtime (placeholder - can be enhanced to show actual playtime)
        // TODO: Calculate and display actual total playtime
        binding.totalPlaytimeTextView.setText(getString(R.string.final_screen_total_playtime, "00:00"));

        // Back to Main Menu Button - returns to main menu
        binding.backToMainMenuButton.setOnClickListener(v -> {
            Log.d(TAG, "Back to main menu button clicked");
            Intent intent = new Intent(FinalScreenActivity.this, MainMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop music when leaving final screen
        musicManager.stopMusic();
    }

    @Override
    public void onBackPressed() {
        // Back button returns to main menu
        Intent intent = new Intent(FinalScreenActivity.this, MainMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}