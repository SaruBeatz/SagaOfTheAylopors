package com.example.sagaoftheaylopors.ml;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of on-device accentuation inference (top-k above threshold).
 */
public final class AccentuationPredictionResult {

    public static final String MODEL_VERSION = "v1_lr";

    @NonNull
    public final String primaryLabel;
    public final double primaryProbability;
    @NonNull
    public final List<AccentuationPredictionItem> items;
    public final int chapterId;

    public AccentuationPredictionResult(
            @NonNull String primaryLabel,
            double primaryProbability,
            @NonNull List<AccentuationPredictionItem> items,
            int chapterId
    ) {
        this.primaryLabel = primaryLabel;
        this.primaryProbability = primaryProbability;
        this.items = items;
        this.chapterId = chapterId;
    }

    @Nullable
    public AccentuationPredictionItem getItem(int index) {
        if (index < 0 || index >= items.size()) {
            return null;
        }
        return items.get(index);
    }

    /**
     * Firestore field {@code predictions} on progress/current and playthroughs/{id}.
     */
    /** JSON array for SessionManager / UI cache. */
    @NonNull
    public String itemsToJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            AccentuationPredictionItem item = items.get(i);
            sb.append("{\"label\":\"")
                    .append(item.label.replace("\"", "\\\""))
                    .append("\",\"probability\":")
                    .append(item.probability)
                    .append('}');
        }
        sb.append(']');
        return sb.toString();
    }

    @NonNull
    public Map<String, Object> toFirestoreMap() {
        Map<String, Object> root = new HashMap<>();
        root.put("modelVersion", MODEL_VERSION);
        root.put("chapterId", chapterId);
        root.put("predictedAt", FieldValue.serverTimestamp());
        root.put("primaryLabel", primaryLabel);
        root.put("primaryProbability", Math.round(primaryProbability * 10000.0) / 10000.0);

        List<Map<String, Object>> itemMaps = new ArrayList<>();
        for (AccentuationPredictionItem item : items) {
            itemMaps.add(item.toFirestoreMap());
        }
        root.put("items", itemMaps);
        return root;
    }
}
