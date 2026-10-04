package com.example.sagaoftheaylopors.ml;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * One accentuation candidate with model probability.
 */
public final class AccentuationPredictionItem {

    @NonNull
    public final String label;
    public final double probability;

    public AccentuationPredictionItem(@NonNull String label, double probability) {
        this.label = label;
        this.probability = probability;
    }

    @NonNull
    public Map<String, Object> toFirestoreMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("label", label);
        map.put("probability", Math.round(probability * 10000.0) / 10000.0);
        return map;
    }
}
