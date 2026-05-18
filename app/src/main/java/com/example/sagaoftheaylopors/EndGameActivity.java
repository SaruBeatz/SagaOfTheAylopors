package com.example.sagaoftheaylopors;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sagaoftheaylopors.databinding.ActivityEndGameBinding;

/**
 * End-of-game completion screen displayed after all 7 chapters are completed.
 * Shows thank you message and developer information.
 */
public class EndGameActivity extends AppCompatActivity {
    private static final String TAG = "EndGameActivity";
    private ActivityEndGameBinding binding;
    private MusicManager musicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEndGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        musicManager = MusicManager.getInstance();
        
        // Play celebratory music (use pause music as placeholder for now)
        // Future: dedicated end-game music track
        musicManager.playPauseMusic(this);

        // Set developer info
        binding.developerNameTextView.setText("Дмитрий Гурлив ПИм-404");
        binding.developerEmailTextView.setText("dima.gurliv@gmail.com");

        // Main Menu Button - returns to main menu
        binding.mainMenuButton.setOnClickListener(v -> {
            Log.d(TAG, "Main menu button clicked");
            Intent intent = new Intent(EndGameActivity.this, MainMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop music when leaving end game screen
        musicManager.stopMusic();
    }

    @Override
    public void onBackPressed() {
        // Back button returns to main menu
        Intent intent = new Intent(EndGameActivity.this, MainMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
