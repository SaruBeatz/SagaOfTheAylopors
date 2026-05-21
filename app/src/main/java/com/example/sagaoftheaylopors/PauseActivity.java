package com.example.sagaoftheaylopors;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sagaoftheaylopors.data.entities.Chapter;
import com.example.sagaoftheaylopors.data.entities.PlayerProgress;
import com.example.sagaoftheaylopors.data.repository.StoryRepository;
import com.example.sagaoftheaylopors.databinding.ActivityPauseStatsBinding;

import java.util.List;
import java.util.Locale;

/**
 * Pause screen activity with Continue, Save, Settings, and Exit options.
 * Plays pause_music.mp3 in a loop.
 */
public class PauseActivity extends AppCompatActivity {
    private static final String TAG = "PauseActivity";
    private ActivityPauseStatsBinding binding;
    private MusicManager musicManager;
    private StoryRepository storyRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPauseStatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        musicManager = MusicManager.getInstance();
        storyRepository = StoryRepository.getInstance(this);

        // Start pause music (looped)
        musicManager.playPauseMusic(this);
        refreshStatistics();

        // Resume Button - resumes current activity
        binding.resumeButton.setOnClickListener(v -> {
            Log.d(TAG, "Resume button clicked - resuming game");
            finish(); // Returns to previous activity (DialogueActivity or MapActivity)
        });

        // Save Game Button - saves current game state
        binding.saveButton.setOnClickListener(v -> {
            Log.d(TAG, "Save button clicked - saving game");
            saveGame();
            finish(); // Return to game after saving
        });

        // Settings Button - opens settings activity
        binding.settingsButton.setOnClickListener(v -> {
            Log.d(TAG, "Settings button clicked");
            Intent intent = new Intent(PauseActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Main Menu Button - saves game and returns to main menu
        binding.mainMenuButton.setOnClickListener(v -> {
            Log.d(TAG, "Main menu button clicked - saving and returning to main menu");
            saveGame();
            Intent intent = new Intent(PauseActivity.this, MainMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Save current game state to database.
     */
    private void saveGame() {
        try {
            com.example.sagaoftheaylopors.data.entities.PlayerProgress progress = 
                storyRepository.getProgress();
            if (progress != null) {
                storyRepository.saveProgress(progress);
                Log.d(TAG, "Game saved successfully");
            } else {
                Log.w(TAG, "No progress to save");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving game", e);
        }
    }

    private void refreshStatistics() {
        int totalChapters = 7;
        int completedCount = 0;
        List<Chapter> chapters = storyRepository.getAllChapters();
        if (chapters != null) {
            for (Chapter chapter : chapters) {
                if (chapter != null && chapter.isCompleted) {
                    completedCount++;
                }
            }
            totalChapters = Math.max(totalChapters, chapters.size());
        }
        binding.chaptersCompletedTextView.setText(
                getString(R.string.pause_chapters_completed, completedCount, totalChapters));

        PlayerProgress progress = storyRepository.getProgress();
        if (progress == null) {
            binding.statsTextView.setText("");
            return;
        }
        Locale locale = Locale.getDefault();
        String statsBody = String.format(locale,
                "%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s",
                getString(R.string.pause_stat_sociality, progress.sociality),
                getString(R.string.pause_stat_activity, progress.activity),
                getString(R.string.pause_stat_emotional, progress.emotionalSensitivity),
                getString(R.string.pause_stat_anxiety, progress.anxiety),
                getString(R.string.pause_stat_self_control, progress.selfControl),
                getString(R.string.pause_stat_impulsivity, progress.impulsivity),
                getString(R.string.pause_stat_ego, progress.egoFocus),
                getString(R.string.pause_stat_rigidity, progress.rigidity),
                getString(R.string.pause_stat_negative, progress.negativeAffect),
                getString(R.string.pause_stat_adaptability, progress.adaptability));
        binding.statsTextView.setText(statsBody);
    }

    @Override
    protected void onResume() {
        super.onResume();
        musicManager.resumeMusic();
        refreshStatistics();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Don't stop music here - let it continue playing
        // Music will be stopped when activity is destroyed or new music starts
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop pause music when leaving pause screen
        // (New activity will start its own music)
        musicManager.stopMusic();
    }

    @Override
    public void onBackPressed() {
        // Back button acts like Continue
        finish();
    }
}
