package com.example.sagaoftheaylopors.cloud;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagaoftheaylopors.data.entities.PlayerProgress;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Serializes the 10 behavioral parameters for Firestore and local pending-choice buffers.
 */
public final class StatsSnapshot {

    private StatsSnapshot() {
    }

    @NonNull
    public static Map<String, Object> fromProgress(@NonNull PlayerProgress progress) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("sociality", roundStat(progress.sociality));
        stats.put("activity", roundStat(progress.activity));
        stats.put("emotionalSensitivity", roundStat(progress.emotionalSensitivity));
        stats.put("anxiety", roundStat(progress.anxiety));
        stats.put("selfControl", roundStat(progress.selfControl));
        stats.put("impulsivity", roundStat(progress.impulsivity));
        stats.put("egoFocus", roundStat(progress.egoFocus));
        stats.put("rigidity", roundStat(progress.rigidity));
        stats.put("negativeAffect", roundStat(progress.negativeAffect));
        stats.put("adaptability", roundStat(progress.adaptability));
        return stats;
    }

    /** Firestore-friendly double in [0,1], avoids integer-looking 1 in console. */
    private static double roundStat(float value) {
        double clamped = Math.max(0.0, Math.min(1.0, value));
        return Math.round(clamped * 1000.0) / 1000.0;
    }

    @NonNull
    public static Map<String, Object> neutralDefaults() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("sociality", 0.5);
        stats.put("activity", 0.5);
        stats.put("emotionalSensitivity", 0.5);
        stats.put("anxiety", 0.5);
        stats.put("selfControl", 0.5);
        stats.put("impulsivity", 0.5);
        stats.put("egoFocus", 0.5);
        stats.put("rigidity", 0.5);
        stats.put("negativeAffect", 0.5);
        stats.put("adaptability", 0.5);
        return stats;
    }

    @NonNull
    public static String toJson(@NonNull PlayerProgress progress) {
        return new JSONObject(fromProgress(progress)).toString();
    }

    @NonNull
    public static String mapToJson(@NonNull Map<String, Object> stats) {
        return new JSONObject(stats).toString();
    }

    @Nullable
    public static Map<String, Object> fromJson(@Nullable String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            JSONObject object = new JSONObject(json);
            Map<String, Object> stats = new HashMap<>();
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                stats.put(key, object.getDouble(key));
            }
            return stats;
        } catch (Exception e) {
            return null;
        }
    }
}
