package com.example.sagaoftheaylopors.auth;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Stores privacy consent locally (not in Firebase until user registers).
 * Bump {@link #CURRENT_CONSENT_VERSION} when policy text changes materially.
 */
public class ConsentManager {

    public static final int CURRENT_CONSENT_VERSION = 1;
    private static final String PREFS_NAME = "saga_privacy_consent";
    private static final String KEY_ACCEPTED = "consent_accepted";
    private static final String KEY_VERSION = "consent_version";
    private static final String KEY_TIMESTAMP_MS = "consent_timestamp_ms";

    private final SharedPreferences prefs;

    public ConsentManager(Context context) {
        prefs = createEncryptedPrefs(context.getApplicationContext());
    }

    public boolean hasValidConsent() {
        return prefs.getBoolean(KEY_ACCEPTED, false)
                && prefs.getInt(KEY_VERSION, 0) >= CURRENT_CONSENT_VERSION;
    }

    public void recordConsent() {
        prefs.edit()
                .putBoolean(KEY_ACCEPTED, true)
                .putInt(KEY_VERSION, CURRENT_CONSENT_VERSION)
                .putLong(KEY_TIMESTAMP_MS, System.currentTimeMillis())
                .apply();
    }

    public long getConsentTimestampMs() {
        return prefs.getLong(KEY_TIMESTAMP_MS, 0L);
    }

    public int getAcceptedVersion() {
        return prefs.getInt(KEY_VERSION, 0);
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
