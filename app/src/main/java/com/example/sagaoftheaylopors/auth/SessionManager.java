package com.example.sagaoftheaylopors.auth;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Local session flags for main menu UX (Continue button) and quick checks.
 */
public class SessionManager {

    private static final String PREFS_NAME = "saga_user_session";
    private static final String KEY_HAS_STARTED_GAME = "has_started_game";
    private static final String KEY_HAS_CLOUD_SAVE = "has_cloud_save";
    private static final String KEY_JUST_REGISTERED = "just_registered";
    private static final String KEY_ACTIVE_PLAYTHROUGH_ID = "active_playthrough_id";
    private static final String KEY_ATTEMPT_NUMBER = "playthrough_attempt_number";
    private static final String KEY_TOTAL_PLAY_TIME_MS = "total_play_time_ms";
    private static final String KEY_FINAL_ACCENTUATION_LABEL = "final_accentuation_label";
    private static final String KEY_FINAL_ACCENTUATION_PROB = "final_accentuation_prob";
    private static final String KEY_FINAL_ACCENTUATION_ITEMS_JSON = "final_accentuation_items_json";

    private final SharedPreferences prefs;
    private final FirebaseAuth firebaseAuth;

    public SessionManager(Context context) {
        Context app = context.getApplicationContext();
        prefs = createEncryptedPrefs(app);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public boolean isLoggedIn() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null && user.getEmail() != null;
    }

    /**
     * Continue is enabled only after the player has started a new game while authenticated.
     */
    public boolean isContinueEnabled() {
        return isLoggedIn() && prefs.getBoolean(KEY_HAS_STARTED_GAME, false);
    }

    public void markGameStarted() {
        prefs.edit().putBoolean(KEY_HAS_STARTED_GAME, true).apply();
    }

    public void setHasCloudSave(boolean hasSave) {
        prefs.edit().putBoolean(KEY_HAS_CLOUD_SAVE, hasSave).apply();
    }

    public boolean hasCloudSave() {
        return prefs.getBoolean(KEY_HAS_CLOUD_SAVE, false);
    }

    /**
     * Called after successful registration so MainMenu can show the unlock snackbar.
     */
    public void markJustRegistered() {
        prefs.edit().putBoolean(KEY_JUST_REGISTERED, true).apply();
    }

    /**
     * Reads and clears the just-registered flag in a single atomic step.
     * Returns true only on the first call after registration.
     */
    public boolean consumeJustRegistered() {
        boolean flag = prefs.getBoolean(KEY_JUST_REGISTERED, false);
        if (flag) {
            prefs.edit().putBoolean(KEY_JUST_REGISTERED, false).apply();
        }
        return flag;
    }

    @Nullable
    public String getActivePlaythroughId() {
        String id = prefs.getString(KEY_ACTIVE_PLAYTHROUGH_ID, null);
        return (id != null && !id.isEmpty()) ? id : null;
    }

    public void setActivePlaythroughId(@NonNull String playthroughId) {
        prefs.edit().putString(KEY_ACTIVE_PLAYTHROUGH_ID, playthroughId).apply();
    }

    public int incrementAndGetAttemptNumber() {
        int next = prefs.getInt(KEY_ATTEMPT_NUMBER, 0) + 1;
        prefs.edit().putInt(KEY_ATTEMPT_NUMBER, next).apply();
        return next;
    }

    public long getTotalPlayTimeMs() {
        return prefs.getLong(KEY_TOTAL_PLAY_TIME_MS, 0L);
    }

    public void setTotalPlayTimeMs(long ms) {
        prefs.edit().putLong(KEY_TOTAL_PLAY_TIME_MS, ms).apply();
    }

    public void addPlayTimeMs(long deltaMs) {
        if (deltaMs <= 0) {
            return;
        }
        setTotalPlayTimeMs(getTotalPlayTimeMs() + deltaMs);
    }

    /**
     * Stores chapter-7 prediction for {@link com.example.sagaoftheaylopors.FinalScreenActivity}.
     * {@code itemsJson} is a JSON array of {@code {label, probability}} objects.
     */
    public void saveFinalAccentuationForDisplay(
            @NonNull String primaryLabel,
            double primaryProbability,
            @NonNull String itemsJson
    ) {
        prefs.edit()
                .putString(KEY_FINAL_ACCENTUATION_LABEL, primaryLabel)
                .putFloat(KEY_FINAL_ACCENTUATION_PROB, (float) primaryProbability)
                .putString(KEY_FINAL_ACCENTUATION_ITEMS_JSON, itemsJson)
                .apply();
    }

    @Nullable
    public String getFinalAccentuationLabel() {
        return prefs.getString(KEY_FINAL_ACCENTUATION_LABEL, null);
    }

    public float getFinalAccentuationProbability() {
        return prefs.getFloat(KEY_FINAL_ACCENTUATION_PROB, 0f);
    }

    @Nullable
    public String getFinalAccentuationItemsJson() {
        return prefs.getString(KEY_FINAL_ACCENTUATION_ITEMS_JSON, null);
    }

    public void clearSessionOnLogout() {
        prefs.edit()
                .remove(KEY_HAS_STARTED_GAME)
                .remove(KEY_HAS_CLOUD_SAVE)
                .remove(KEY_JUST_REGISTERED)
                .remove(KEY_ACTIVE_PLAYTHROUGH_ID)
                .remove(KEY_ATTEMPT_NUMBER)
                .remove(KEY_TOTAL_PLAY_TIME_MS)
                .remove(KEY_FINAL_ACCENTUATION_LABEL)
                .remove(KEY_FINAL_ACCENTUATION_PROB)
                .remove(KEY_FINAL_ACCENTUATION_ITEMS_JSON)
                .apply();
    }

    private static SharedPreferences createEncryptedPrefs(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }
}
