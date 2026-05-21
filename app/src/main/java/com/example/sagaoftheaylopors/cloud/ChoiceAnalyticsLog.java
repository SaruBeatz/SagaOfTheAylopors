package com.example.sagaoftheaylopors.cloud;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.sagaoftheaylopors.data.entities.PlayerProgress;

/**
 * Structured log tag for choice / stats debugging (filter Logcat by {@value #TAG}).
 */
public final class ChoiceAnalyticsLog {

    public static final String TAG = "ChoiceAnalytics";

    private ChoiceAnalyticsLog() {
    }

    public static void choiceRecorded(
            int chapterNumber,
            @NonNull String choicePointId,
            @NonNull String dialogueJsonId,
            @NonNull String selectedChoiceId,
            @NonNull PlayerProgress after
    ) {
        Log.i(TAG, String.format(
                "CHOICE chapter=%d docId=%s dialogue=%s picked=%s | stats: soc=%.3f act=%.3f emp=%.3f anx=%.3f ctrl=%.3f imp=%.3f ego=%.3f rig=%.3f neg=%.3f adp=%.3f",
                chapterNumber,
                choicePointId,
                dialogueJsonId,
                selectedChoiceId,
                after.sociality,
                after.activity,
                after.emotionalSensitivity,
                after.anxiety,
                after.selfControl,
                after.impulsivity,
                after.egoFocus,
                after.rigidity,
                after.negativeAffect,
                after.adaptability
        ));
    }

    public static void chapterSyncStart(int chapterId, int pendingCount) {
        Log.i(TAG, "SYNC_START chapter=" + chapterId + " pending=" + pendingCount);
    }

    public static void chapterSyncDone(int chapterId, boolean success) {
        Log.i(TAG, "SYNC_DONE chapter=" + chapterId + " success=" + success);
    }

    public static void chapterCompletionValidated(int chapterId, int completedScenes, int totalScenes) {
        Log.i(TAG, "CHAPTER_COMPLETE_OK chapter=" + chapterId
                + " scenes=" + completedScenes + "/" + totalScenes);
    }

    public static void chapterCompletionRejected(int chapterId, String reason) {
        Log.w(TAG, "CHAPTER_COMPLETE_REJECTED chapter=" + chapterId + " reason=" + reason);
    }

    public static void warn(@NonNull String message) {
        Log.w(TAG, message);
    }
}
