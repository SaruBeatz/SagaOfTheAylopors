package com.example.sagaoftheaylopors;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sagaoftheaylopors.auth.SessionManager;
import com.example.sagaoftheaylopors.databinding.ActivityFinalScreenBinding;
import com.example.sagaoftheaylopors.ml.AccentuationTexts;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Final screen displayed after all 7 chapters are completed.
 * Shows thank you message, optional accentuation reveal, and developer information.
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
        musicManager.initialize(this);
        musicManager.enterPause(this);

        binding.totalPlaytimeTextView.setText(
                getString(R.string.final_screen_total_playtime, formatPlaytime(new SessionManager(this))));

        bindAccentuationReveal();

        binding.backToMainMenuButton.setOnClickListener(v -> {
            Log.d(TAG, "Back to main menu button clicked");
            Intent intent = new Intent(FinalScreenActivity.this, MainMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void bindAccentuationReveal() {
        SessionManager session = new SessionManager(this);
        String label = session.getFinalAccentuationLabel();
        if (label == null || label.isEmpty()) {
            return;
        }

        float prob = session.getFinalAccentuationProbability();
        int confidencePct = Math.round(prob * 100f);

        binding.accentuationTitleTextView.setVisibility(View.VISIBLE);
        binding.accentuationTitleTextView.setText(
                getString(R.string.final_screen_accentuation_title, label));

        binding.accentuationConfidenceTextView.setVisibility(View.VISIBLE);
        binding.accentuationConfidenceTextView.setText(
                getString(R.string.final_screen_accentuation_confidence, confidencePct));

        binding.accentuationDescriptionTextView.setVisibility(View.VISIBLE);
        binding.accentuationDescriptionTextView.setText(
                getString(AccentuationTexts.getDescriptionResId(label)));

        String alternatives = formatAlternatives(session.getFinalAccentuationItemsJson(), label);
        if (alternatives != null && !alternatives.isEmpty()) {
            binding.accentuationAlternativesTextView.setVisibility(View.VISIBLE);
            binding.accentuationAlternativesTextView.setText(
                    getString(R.string.final_screen_accentuation_alternatives, alternatives));
        }
    }

    /**
     * Builds "Тип2 (12%), Тип3 (8%)" from cached JSON; skips primary label.
     */
    private String formatAlternatives(String itemsJson, String primaryLabel) {
        if (itemsJson == null || itemsJson.isEmpty()) {
            return null;
        }
        try {
            JSONArray arr = new JSONArray(itemsJson);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String itemLabel = obj.optString("label", "");
                if (itemLabel.isEmpty() || itemLabel.equals(primaryLabel)) {
                    continue;
                }
                double p = obj.optDouble("probability", 0.0);
                int pct = Math.round((float) p * 100f);
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(getString(R.string.final_screen_accentuation_alternative_item, itemLabel, pct));
            }
            return sb.length() > 0 ? sb.toString() : null;
        } catch (Exception e) {
            Log.w(TAG, "Failed to parse accentuation items JSON", e);
            return null;
        }
    }

    private static String formatPlaytime(SessionManager session) {
        long ms = session.getTotalPlayTimeMs();
        long totalSec = ms / 1000;
        long hours = totalSec / 3600;
        long minutes = (totalSec % 3600) / 60;
        long seconds = totalSec % 60;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        musicManager.stopAll();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(FinalScreenActivity.this, MainMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
