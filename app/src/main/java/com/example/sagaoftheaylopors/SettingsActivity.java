package com.example.sagaoftheaylopors;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sagaoftheaylopors.databinding.ActivitySettingsBinding;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    private static final String PREFS_NAME = "SagaSettings";
    private static final String KEY_MUSIC_VOLUME = "music_volume";
    private static final String KEY_SOUND_VOLUME = "sound_volume";
    private static final String KEY_LANGUAGE = "language";
    
    private ActivitySettingsBinding binding;
    private MusicManager musicManager;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        musicManager = MusicManager.getInstance();
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load saved settings
        int savedMusicVolume = prefs.getInt(KEY_MUSIC_VOLUME, 70);
        int savedSoundVolume = prefs.getInt(KEY_SOUND_VOLUME, 80);
        String savedLanguage = prefs.getString(KEY_LANGUAGE, "ru");

        // Set initial values
        binding.musicVolumeSeekBar.setProgress(savedMusicVolume);
        binding.musicVolumeTextView.setText(getString(R.string.percent_format, savedMusicVolume));
        binding.soundVolumeSeekBar.setProgress(savedSoundVolume);
        binding.soundVolumeTextView.setText(getString(R.string.percent_format, savedSoundVolume));
        
        // Set language radio button
        if ("en".equals(savedLanguage)) {
            binding.languageEnglishRadio.setChecked(true);
        } else {
            binding.languageRussianRadio.setChecked(true);
        }

        // Apply initial music volume
        float volume = savedMusicVolume / 100.0f;
        musicManager.setMusicVolume(volume);

        // Music Volume SeekBar
        binding.musicVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.musicVolumeTextView.setText(getString(R.string.percent_format, progress));
                if (fromUser) {
                    // Apply music volume immediately
                    float volume = progress / 100.0f;
                    musicManager.setMusicVolume(volume);
                    // Save to preferences
                    prefs.edit().putInt(KEY_MUSIC_VOLUME, progress).apply();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Sound Volume SeekBar
        binding.soundVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.soundVolumeTextView.setText(getString(R.string.percent_format, progress));
                if (fromUser) {
                    // Save sound volume (future: apply to sound effects)
                    prefs.edit().putInt(KEY_SOUND_VOLUME, progress).apply();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Language RadioGroup
        binding.languageRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String newLanguage = "ru";
            if (checkedId == R.id.languageRussianRadio) {
                newLanguage = "ru";
                Log.d(TAG, "Language changed to Russian");
            } else if (checkedId == R.id.languageEnglishRadio) {
                newLanguage = "en";
                Log.d(TAG, "Language changed to English");
            }
            
            // Save language preference
            prefs.edit().putString(KEY_LANGUAGE, newLanguage).apply();
            
            // Apply language change
            setLocale(newLanguage);
            
            // Restart activity to apply language changes
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        });

        // Back Button
        binding.backButton.setOnClickListener(v -> finish());
    }

    /**
     * Set application locale.
     * This is a temporary implementation - future versions will have improved localization.
     */
    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        
        Log.d(TAG, "Locale set to: " + languageCode);
    }

    @Override
    public void onBackPressed() {
        // Go back to Main Menu or previous screen
        finish();
    }
}

