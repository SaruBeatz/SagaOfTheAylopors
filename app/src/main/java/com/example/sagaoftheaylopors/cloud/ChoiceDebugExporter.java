package com.example.sagaoftheaylopors.cloud;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagaoftheaylopors.auth.SessionManager;
import com.example.sagaoftheaylopors.data.database.AppDatabase;
import com.example.sagaoftheaylopors.data.entities.PendingChoice;
import com.example.sagaoftheaylopors.data.entities.PlayerProgress;
import com.example.sagaoftheaylopors.data.repository.StoryRepository;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Writes NDJSON debug events under Android/data/.../files/choice_analytics/
 * Pull with: adb pull /sdcard/Android/data/com.example.sagaoftheaylopors/files/choice_analytics .
 * Parse with: scripts/parse_choice_debug.py debug_log.ndjson
 */
public final class ChoiceDebugExporter {

    private static final String TAG = "ChoiceDebugExporter";
    private static final String DIR_NAME = "choice_analytics";
    private static final String LOG_FILE = "debug_log.ndjson";
    private static final String SNAPSHOT_FILE = "snapshot_latest.json";

    private ChoiceDebugExporter() {
    }

    public static void logEvent(@NonNull Context context, @NonNull String eventType, @NonNull JSONObject payload) {
        try {
            File dir = analyticsDir(context);
            if (!dir.exists() && !dir.mkdirs()) {
                Log.e(TAG, "Cannot create analytics dir");
                return;
            }
            JSONObject line = new JSONObject();
            line.put("ts", isoNow());
            line.put("event", eventType);
            line.put("data", payload);

            File logFile = new File(dir, LOG_FILE);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write(line.toString());
                writer.newLine();
            }
        } catch (Exception e) {
            Log.e(TAG, "logEvent failed", e);
        }
    }

    public static void writeSnapshot(@NonNull Context context, @Nullable String note) {
        try {
            File dir = analyticsDir(context);
            if (!dir.exists() && !dir.mkdirs()) {
                return;
            }
            SessionManager session = new SessionManager(context);
            AppDatabase db = AppDatabase.getDatabase(context);
            StoryRepository story = StoryRepository.getInstance(context);
            PlayerProgress progress = story.getProgress();
            String playthroughId = session.getActivePlaythroughId();

            JSONObject root = new JSONObject();
            root.put("exportedAt", isoNow());
            if (note != null) {
                root.put("note", note);
            }
            root.put("playthroughId", playthroughId != null ? playthroughId : JSONObject.NULL);
            root.put("totalPlayTimeMs", session.getTotalPlayTimeMs());

            if (progress != null) {
                root.put("progress", new JSONObject(StatsSnapshot.fromProgress(progress)));
            }

            JSONArray pending = new JSONArray();
            if (playthroughId != null) {
                List<PendingChoice> all = db.pendingChoiceDao().getAllForPlaythrough(playthroughId);
                for (PendingChoice c : all) {
                    pending.put(pendingChoiceJson(c));
                }
            }
            root.put("pendingChoices", pending);

            File out = new File(dir, SNAPSHOT_FILE);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(out, false))) {
                writer.write(root.toString(2));
            }
            Log.i(TAG, "Snapshot written: " + out.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "writeSnapshot failed", e);
        }
    }

    @NonNull
    public static File analyticsDir(@NonNull Context context) {
        File base = context.getExternalFilesDir(null);
        if (base == null) {
            base = context.getFilesDir();
        }
        return new File(base, DIR_NAME);
    }

    @NonNull
    private static JSONObject pendingChoiceJson(@NonNull PendingChoice c) throws Exception {
        JSONObject o = new JSONObject();
        o.put("id", c.id);
        o.put("chapterId", c.chapterId);
        o.put("choicePointId", c.choicePointId);
        o.put("sceneId", c.sceneJsonId);
        o.put("dialogueId", c.dialogueJsonId);
        o.put("selectedChoiceId", c.selectedChoiceId);
        o.put("selectedChoiceIndex", c.selectedChoiceIndex);
        o.put("synced", c.synced);
        o.put("selectedAt", c.selectedAt);
        if (c.statsBeforeJson != null) {
            o.put("statsBefore", new JSONObject(c.statsBeforeJson));
        }
        if (c.statsAfterJson != null) {
            o.put("statsAfter", new JSONObject(c.statsAfterJson));
        }
        return o;
    }

    @NonNull
    private static String isoNow() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(new Date());
    }

    @NonNull
    public static JSONObject eventPayload(
            int chapterNumber,
            @NonNull String choicePointId,
            @NonNull String dialogueJsonId,
            @NonNull String sceneJsonId,
            @NonNull String selectedChoiceId,
            int selectedChoiceIndex,
            @NonNull Map<String, Object> statsBefore,
            @NonNull Map<String, Object> statsAfter
    ) throws Exception {
        JSONObject o = new JSONObject();
        o.put("chapterNumber", chapterNumber);
        o.put("choicePointId", choicePointId);
        o.put("dialogueJsonId", dialogueJsonId);
        o.put("sceneJsonId", sceneJsonId);
        o.put("selectedChoiceId", selectedChoiceId);
        o.put("selectedChoiceIndex", selectedChoiceIndex);
        o.put("statsBefore", new JSONObject(statsBefore));
        o.put("statsAfter", new JSONObject(statsAfter));
        return o;
    }
}
