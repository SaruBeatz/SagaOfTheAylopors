package com.example.sagaoftheaylopors.ml;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagaoftheaylopors.data.entities.PlayerProgress;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * On-device logistic regression inference from {@code assets/ml/accentuation_export.json}.
 * No sklearn / native ML libraries required at runtime.
 */
public final class AccentuationPredictor {

    private static final String TAG = "AccentuationPredictor";
    private static final String ASSET_PATH = "ml/accentuation_export.json";
    private static final float MIN_PROBABILITY = 0.1f;
    private static final int MAX_ITEMS = 3;

    private static volatile AccentuationPredictor instance;

    private final String[] featureKeys;
    private final String[] classes;
    private final double[] scalerMean;
    private final double[] scalerScale;
    private final double[][] coefficients;
    private final double[] intercept;

    private AccentuationPredictor(
            String[] featureKeys,
            String[] classes,
            double[] scalerMean,
            double[] scalerScale,
            double[][] coefficients,
            double[] intercept
    ) {
        this.featureKeys = featureKeys;
        this.classes = classes;
        this.scalerMean = scalerMean;
        this.scalerScale = scalerScale;
        this.coefficients = coefficients;
        this.intercept = intercept;
    }

    @NonNull
    public static AccentuationPredictor getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (AccentuationPredictor.class) {
                if (instance == null) {
                    instance = load(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    /**
     * Hidden chapter-end prediction: top 2–3 classes with probability &gt;= 0.1.
     */
    @NonNull
    public AccentuationPredictionResult predict(@NonNull PlayerProgress progress, int chapterId) {
        double[] raw = vectorFromProgress(progress);
        double[] scaled = new double[raw.length];
        for (int i = 0; i < raw.length; i++) {
            scaled[i] = (raw[i] - scalerMean[i]) / scalerScale[i];
        }

        double[] scores = new double[classes.length];
        for (int k = 0; k < classes.length; k++) {
            double s = intercept[k];
            for (int j = 0; j < scaled.length; j++) {
                s += coefficients[k][j] * scaled[j];
            }
            scores[k] = s;
        }

        double[] probs = softmax(scores);
        Integer[] order = new Integer[probs.length];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        Arrays.sort(order, Comparator.comparingDouble((Integer idx) -> -probs[idx]));

        List<AccentuationPredictionItem> items = new ArrayList<>();
        for (int idx : order) {
            if (probs[idx] < MIN_PROBABILITY) {
                break;
            }
            items.add(new AccentuationPredictionItem(classes[idx], probs[idx]));
            if (items.size() >= MAX_ITEMS) {
                break;
            }
        }
        if (items.isEmpty()) {
            int best = order[0];
            items.add(new AccentuationPredictionItem(classes[best], probs[best]));
        }

        AccentuationPredictionItem top = items.get(0);
        return new AccentuationPredictionResult(
                top.label,
                top.probability,
                items,
                chapterId
        );
    }

    @NonNull
    private double[] vectorFromProgress(@NonNull PlayerProgress progress) {
        double[] values = new double[featureKeys.length];
        for (int i = 0; i < featureKeys.length; i++) {
            values[i] = progressValue(progress, featureKeys[i]);
        }
        return values;
    }

    private static double progressValue(@NonNull PlayerProgress progress, @NonNull String key) {
        switch (key) {
            case "Soc":
                return clamp(progress.sociality);
            case "Act":
                return clamp(progress.activity);
            case "Emp":
                return clamp(progress.emotionalSensitivity);
            case "Anx":
                return clamp(progress.anxiety);
            case "Ctrl":
                return clamp(progress.selfControl);
            case "Imp":
                return clamp(progress.impulsivity);
            case "Ego":
                return clamp(progress.egoFocus);
            case "Rig":
                return clamp(progress.rigidity);
            case "Neg":
                return clamp(progress.negativeAffect);
            case "Adp":
                return clamp(progress.adaptability);
            default:
                return 0.5;
        }
    }

    private static double clamp(float v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    @NonNull
    private static double[] softmax(@NonNull double[] scores) {
        double max = scores[0];
        for (double s : scores) {
            max = Math.max(max, s);
        }
        double sum = 0.0;
        double[] exp = new double[scores.length];
        for (int i = 0; i < scores.length; i++) {
            exp[i] = Math.exp(scores[i] - max);
            sum += exp[i];
        }
        double[] probs = new double[scores.length];
        for (int i = 0; i < scores.length; i++) {
            probs[i] = exp[i] / sum;
        }
        return probs;
    }

    @NonNull
    private static AccentuationPredictor load(@NonNull Context context) {
        try {
            InputStream in = context.getAssets().open(ASSET_PATH);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            JSONObject root = new JSONObject(sb.toString());

            JSONArray featuresJson = root.getJSONArray("features");
            String[] features = jsonStringArray(featuresJson);

            JSONArray classesJson = root.getJSONArray("classes");
            String[] classNames = jsonStringArray(classesJson);

            double[] mean = jsonDoubleArray(root.getJSONArray("scaler_mean"));
            double[] scale = jsonDoubleArray(root.getJSONArray("scaler_scale"));

            JSONArray coefOuter = root.getJSONArray("coefficients");
            double[][] coef = new double[coefOuter.length()][];
            for (int i = 0; i < coefOuter.length(); i++) {
                coef[i] = jsonDoubleArray(coefOuter.getJSONArray(i));
            }
            double[] interceptArr = jsonDoubleArray(root.getJSONArray("intercept"));

            Log.i(TAG, "Loaded model " + root.optString("model_name")
                    + " features=" + features.length + " classes=" + classNames.length);
            return new AccentuationPredictor(
                    features, classNames, mean, scale, coef, interceptArr);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load " + ASSET_PATH, e);
            throw new IllegalStateException("Accentuation model not loaded", e);
        }
    }

    @NonNull
    private static String[] jsonStringArray(@NonNull JSONArray arr) throws Exception {
        String[] out = new String[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            out[i] = arr.getString(i);
        }
        return out;
    }

    @NonNull
    private static double[] jsonDoubleArray(@NonNull JSONArray arr) throws Exception {
        double[] out = new double[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            out[i] = arr.getDouble(i);
        }
        return out;
    }

    /** For tests / backfill: predict from Firestore stats map keys (sociality, activity, …). */
    @NonNull
    public AccentuationPredictionResult predictFromStatsMap(
            @NonNull java.util.Map<String, Object> stats,
            int chapterId
    ) {
        PlayerProgress p = new PlayerProgress();
        p.sociality = floatFrom(stats.get("sociality"), 0.5f);
        p.activity = floatFrom(stats.get("activity"), 0.5f);
        p.emotionalSensitivity = floatFrom(stats.get("emotionalSensitivity"), 0.5f);
        p.anxiety = floatFrom(stats.get("anxiety"), 0.5f);
        p.selfControl = floatFrom(stats.get("selfControl"), 0.5f);
        p.impulsivity = floatFrom(stats.get("impulsivity"), 0.5f);
        p.egoFocus = floatFrom(stats.get("egoFocus"), 0.5f);
        p.rigidity = floatFrom(stats.get("rigidity"), 0.5f);
        p.negativeAffect = floatFrom(stats.get("negativeAffect"), 0.5f);
        p.adaptability = floatFrom(stats.get("adaptability"), 0.5f);
        return predict(p, chapterId);
    }

    private static float floatFrom(@Nullable Object value, float defaultVal) {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return defaultVal;
    }
}
