package com.example.sagaoftheaylopors.cloud;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagaoftheaylopors.auth.AuthRepository;
import com.example.sagaoftheaylopors.auth.SessionManager;
import com.example.sagaoftheaylopors.data.database.AppDatabase;
import com.example.sagaoftheaylopors.data.entities.Chapter;
import com.example.sagaoftheaylopors.data.entities.Dialogue;
import com.example.sagaoftheaylopors.data.entities.PendingChoice;
import com.example.sagaoftheaylopors.data.entities.PlayerProgress;
import com.example.sagaoftheaylopors.data.entities.Scene;
import com.example.sagaoftheaylopors.data.repository.StoryRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Firestore sync for playthroughs, chapter choice batches, and progress/current.
 *
 * TODO: Mark speedrunner playthroughs (excluded from ML training corpus).
 * TODO: Autoclicker detection — optional separate analytics flag on choices.
 */
public class PlaythroughRepository {

    private static final String TAG = "PlaythroughRepository";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_PROGRESS = "progress";
    private static final String DOC_PROGRESS_CURRENT = "current";
    private static final String COLLECTION_PLAYTHROUGHS = "playthroughs";
    private static final String COLLECTION_CHOICES = "choices";

    private static PlaythroughRepository instance;

    /** Prevents duplicate concurrent syncs for the same playthrough+chapter. */
    private final Set<String> syncInFlight = Collections.synchronizedSet(new HashSet<>());

    private final FirebaseFirestore firestore;
    private final AppDatabase database;
    private final AuthRepository authRepository;

    private PlaythroughRepository(Context context) {
        firestore = FirebaseFirestore.getInstance();
        database = AppDatabase.getDatabase(context);
        authRepository = new AuthRepository();
    }

    public static synchronized PlaythroughRepository getInstance(Context context) {
        if (instance == null) {
            instance = new PlaythroughRepository(context.getApplicationContext());
        }
        return instance;
    }

    @Nullable
    private String currentUid() {
        FirebaseUser user = authRepository.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    private DocumentReference userRef(@NonNull String uid) {
        return firestore.collection(COLLECTION_USERS).document(uid);
    }

    private DocumentReference progressCurrentRef(@NonNull String uid) {
        return userRef(uid).collection(COLLECTION_PROGRESS).document(DOC_PROGRESS_CURRENT);
    }

    private DocumentReference playthroughRef(@NonNull String uid, @NonNull String playthroughId) {
        return userRef(uid).collection(COLLECTION_PLAYTHROUGHS).document(playthroughId);
    }

    /**
     * Starts a new cloud playthrough after character selection (new game).
     */
    @NonNull
    public Task<String> createPlaythrough(@NonNull Context context, @Nullable String selectedCharacter) {
        String uid = currentUid();
        if (uid == null) {
            return Tasks.forException(new IllegalStateException("Not logged in"));
        }

        SessionManager sessionManager = new SessionManager(context);
        String playthroughId = UUID.randomUUID().toString();
        int attemptNumber = sessionManager.incrementAndGetAttemptNumber();
        boolean isFirstPlaythrough = attemptNumber == 1;
        long now = System.currentTimeMillis();

        sessionManager.setActivePlaythroughId(playthroughId);
        sessionManager.setTotalPlayTimeMs(0);

        List<Scene> chapterOneScenes = database.sceneDao().getScenesByChapter(1);
        String startSceneJsonId = "";
        String startDialogueJsonId = "";
        if (!chapterOneScenes.isEmpty()) {
            Scene firstScene = chapterOneScenes.get(0);
            startSceneJsonId = firstScene.jsonId != null ? firstScene.jsonId : "";
            List<Dialogue> dialogues = database.dialogueDao().getDialoguesByScene(firstScene.sceneId);
            if (!dialogues.isEmpty() && dialogues.get(0).jsonId != null) {
                startDialogueJsonId = dialogues.get(0).jsonId;
            }
        }

        Map<String, Object> playthroughDoc = new HashMap<>();
        playthroughDoc.put("playthroughId", playthroughId);
        playthroughDoc.put("attemptNumber", attemptNumber);
        playthroughDoc.put("isFirstPlaythrough", isFirstPlaythrough);
        playthroughDoc.put("status", "active");
        playthroughDoc.put("startedAt", FieldValue.serverTimestamp());
        playthroughDoc.put("lastUpdatedAt", FieldValue.serverTimestamp());
        playthroughDoc.put("totalPlayTimeMs", 0L);

        Map<String, Object> progressDoc = new HashMap<>();
        progressDoc.put("activePlaythroughId", playthroughId);
        progressDoc.put("currentChapterId", 1);
        progressDoc.put("currentSceneId", startSceneJsonId);
        progressDoc.put("currentDialogueId", startDialogueJsonId);
        progressDoc.put("selectedCharacter", selectedCharacter != null ? selectedCharacter : "");
        progressDoc.put("totalPlayTimeMs", 0L);
        progressDoc.put("lastPlayedAt", FieldValue.serverTimestamp());
        progressDoc.put("stats", StatsSnapshot.neutralDefaults());

        WriteBatch batch = firestore.batch();
        batch.set(playthroughRef(uid, playthroughId), playthroughDoc);
        batch.set(progressCurrentRef(uid), progressDoc);

        return batch.commit().continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException() != null
                        ? task.getException()
                        : new IllegalStateException("createPlaythrough failed");
            }
            Log.d(TAG, "Created playthrough " + playthroughId + " attempt=" + attemptNumber);
            return playthroughId;
        });
    }

    /**
     * Buffers a choice locally (Room). Cloud flush happens at chapter end.
     */
    public void savePendingChoice(
            @NonNull Context context,
            @NonNull PendingChoice pending
    ) {
        SessionManager sessionManager = new SessionManager(context);
        String playthroughId = sessionManager.getActivePlaythroughId();
        if (playthroughId == null || playthroughId.isEmpty()) {
            Log.w(TAG, "savePendingChoice skipped — no active playthrough");
            return;
        }
        pending.playthroughId = playthroughId;
        pending.synced = false;

        database.pendingChoiceDao().deleteUnsyncedForDialogue(
                playthroughId, pending.chapterId, pending.dialogueJsonId);

        int seq = database.pendingChoiceDao().countUnsyncedForChapter(playthroughId, pending.chapterId) + 1;
        pending.choicePointId = pending.chapterId + "_" + seq;

        database.pendingChoiceDao().insert(pending);
        Log.d(TAG, "Buffered choice " + pending.choicePointId + " dialogue=" + pending.dialogueJsonId);
    }

    /**
     * Flushes all unsynced choices for a chapter, then updates playthrough + progress/current.
     */
    @NonNull
    public Task<Void> syncChapterChoices(
            @NonNull Context context,
            int completedChapterId,
            @NonNull PlayerProgress progress
    ) {
        String uid = currentUid();
        if (uid == null) {
            return Tasks.forException(new IllegalStateException("Not logged in"));
        }

        SessionManager sessionManager = new SessionManager(context);
        String playthroughId = sessionManager.getActivePlaythroughId();
        if (playthroughId == null || playthroughId.isEmpty()) {
            return Tasks.forException(new IllegalStateException("No active playthrough"));
        }

        List<PendingChoice> pendingList = database.pendingChoiceDao()
                .getUnsyncedForChapter(playthroughId, completedChapterId);
        long totalPlayTimeMs = sessionManager.getTotalPlayTimeMs();

        StoryRepository storyRepository = StoryRepository.getInstance(context);
        Scene currentScene = storyRepository.getScene(progress.currentSceneId);
        Dialogue currentDialogue = storyRepository.getDialogue(progress.currentDialogueId);

        String sceneJsonId = currentScene != null && currentScene.jsonId != null
                ? currentScene.jsonId : "";
        String dialogueJsonId = currentDialogue != null && currentDialogue.jsonId != null
                ? currentDialogue.jsonId : "";

        int nextChapterId = completedChapterId + 1;
        String nextSceneJsonId = sceneJsonId;
        String nextDialogueJsonId = dialogueJsonId;
        if (nextChapterId <= 7) {
            List<Scene> nextScenes = database.sceneDao().getScenesByChapter(nextChapterId);
            if (!nextScenes.isEmpty()) {
                Scene first = nextScenes.get(0);
                nextSceneJsonId = first.jsonId != null ? first.jsonId : "";
                List<Dialogue> dialogues = database.dialogueDao().getDialoguesByScene(first.sceneId);
                if (!dialogues.isEmpty() && dialogues.get(0).jsonId != null) {
                    nextDialogueJsonId = dialogues.get(0).jsonId;
                }
            }
        }

        WriteBatch batch = firestore.batch();
        DocumentReference playthroughDoc = playthroughRef(uid, playthroughId);

        ChoiceAnalyticsLog.chapterSyncStart(completedChapterId, pendingList.size());

        for (PendingChoice pending : pendingList) {
            Map<String, Object> choiceDoc = buildChoiceDocument(pending, totalPlayTimeMs);
            String docId = pending.choicePointId;
            if (docId == null || docId.isEmpty()) {
                docId = pending.chapterId + "_unknown";
            }
            DocumentReference choiceRef = playthroughDoc
                    .collection(COLLECTION_CHOICES)
                    .document(docId);
            batch.set(choiceRef, choiceDoc, SetOptions.merge());
        }

        Map<String, Object> playthroughUpdate = new HashMap<>();
        playthroughUpdate.put("playthroughId", playthroughId);
        playthroughUpdate.put("lastUpdatedAt", FieldValue.serverTimestamp());
        playthroughUpdate.put("lastCompletedChapterId", completedChapterId);
        playthroughUpdate.put("totalPlayTimeMs", totalPlayTimeMs);
        playthroughUpdate.put("stats", StatsSnapshot.fromProgress(progress));
        playthroughUpdate.put("status", completedChapterId >= 7 ? "completed" : "active");
        if (completedChapterId >= 7) {
            playthroughUpdate.put("finishedAt", FieldValue.serverTimestamp());
        }
        batch.set(playthroughDoc, playthroughUpdate, SetOptions.merge());

        Map<String, Object> progressUpdate = new HashMap<>();
        progressUpdate.put("activePlaythroughId", playthroughId);
        progressUpdate.put("currentChapterId", nextChapterId <= 7 ? nextChapterId : completedChapterId);
        progressUpdate.put("currentSceneId", nextSceneJsonId);
        progressUpdate.put("currentDialogueId", nextDialogueJsonId);
        progressUpdate.put("selectedCharacter", progress.selectedCharacter != null
                ? progress.selectedCharacter : "");
        progressUpdate.put("totalPlayTimeMs", totalPlayTimeMs);
        progressUpdate.put("lastPlayedAt", FieldValue.serverTimestamp());
        progressUpdate.put("stats", StatsSnapshot.fromProgress(progress));
        batch.set(progressCurrentRef(uid), progressUpdate, SetOptions.merge());

        final String finalPlaythroughId = playthroughId;
        final int chapterId = completedChapterId;
        final int choiceCount = pendingList.size();

        return batch.commit().continueWith(task -> {
            if (!task.isSuccessful()) {
                Exception error = task.getException();
                Log.e(TAG, "syncChapterChoices failed for chapter " + chapterId, error);
                throw error != null ? error : new IllegalStateException("syncChapterChoices failed");
            }
            database.pendingChoiceDao().markChapterSynced(finalPlaythroughId, chapterId);
            sessionManager.setHasCloudSave(true);
            ChoiceAnalyticsLog.chapterSyncDone(chapterId, true);
            Log.d(TAG, "Synced chapter " + chapterId + ": " + choiceCount + " choice(s), progress+playthrough updated");
            return null;
        });
    }

    /**
     * Sync one chapter if it has unsynced choices or is marked complete in Room.
     * Safe to call on chapter end, onStop, or map return (deduped).
     */
    public void syncCompletedChapterInBackground(@NonNull Context context, int completedChapterId) {
        Context app = context.getApplicationContext();
        if (currentUid() == null) {
            return;
        }
        SessionManager sessionManager = new SessionManager(app);
        String playthroughId = sessionManager.getActivePlaythroughId();
        if (playthroughId == null || playthroughId.isEmpty()) {
            return;
        }

        int unsynced = database.pendingChoiceDao()
                .countUnsyncedForChapter(playthroughId, completedChapterId);
        Chapter chapter = database.chapterDao().getChapterById(completedChapterId);
        boolean chapterMarkedComplete = chapter != null && chapter.isCompleted;

        if (unsynced == 0 && !chapterMarkedComplete) {
            Log.d(TAG, "Skip sync chapter " + completedChapterId + " — no pending choices, not complete");
            return;
        }

        String flightKey = playthroughId + ":" + completedChapterId;
        if (!syncInFlight.add(flightKey)) {
            Log.d(TAG, "Skip sync chapter " + completedChapterId + " — already in flight");
            return;
        }

        PlayerProgress progress = StoryRepository.getInstance(app).getProgress();
        Log.d(TAG, "Background sync chapter " + completedChapterId
                + " pending=" + unsynced + " complete=" + chapterMarkedComplete);
        syncChapterChoices(app, completedChapterId, progress)
                .addOnCompleteListener(task -> {
                    syncInFlight.remove(flightKey);
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Chapter " + completedChapterId + " background sync failed",
                                task.getException());
                    }
                });
    }

    /**
     * Retry any chapters still unsynced (e.g. after failed network).
     */
    public void syncAllUnsyncedChaptersInBackground(@NonNull Context context, @NonNull PlayerProgress progress) {
        String uid = currentUid();
        if (uid == null) {
            return;
        }
        SessionManager sessionManager = new SessionManager(context);
        String playthroughId = sessionManager.getActivePlaythroughId();
        if (playthroughId == null || playthroughId.isEmpty()) {
            return;
        }

        List<Integer> chapterIds = database.pendingChoiceDao().getUnsyncedChapterIds(playthroughId);
        if (chapterIds.isEmpty()) {
            Log.d(TAG, "No unsynced chapters");
            return;
        }

        Log.d(TAG, "Background sync for chapters: " + chapterIds);
        syncChapterChain(context, progress, chapterIds, 0);
    }

    private void syncChapterChain(
            @NonNull Context context,
            @NonNull PlayerProgress progress,
            @NonNull List<Integer> chapterIds,
            int index
    ) {
        if (index >= chapterIds.size()) {
            ChoiceDebugExporter.writeSnapshot(context, "after_all_chapter_sync");
            return;
        }
        int chapterId = chapterIds.get(index);
        syncChapterChoices(context, chapterId, progress)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        ChoiceAnalyticsLog.chapterSyncDone(chapterId, false);
                        Log.e(TAG, "Background sync failed for chapter " + chapterId, task.getException());
                    }
                    syncChapterChain(context, progress, chapterIds, index + 1);
                });
    }

    /**
     * Lightweight position + time sync (pause / leave dialogue).
     */
    @NonNull
    public Task<Void> updateProgressCurrent(
            @NonNull Context context,
            @NonNull PlayerProgress progress
    ) {
        String uid = currentUid();
        if (uid == null) {
            return Tasks.forResult(null);
        }

        SessionManager sessionManager = new SessionManager(context);
        String playthroughId = sessionManager.getActivePlaythroughId();
        if (playthroughId == null || playthroughId.isEmpty()) {
            return Tasks.forResult(null);
        }

        StoryRepository storyRepository = StoryRepository.getInstance(context);
        Scene scene = storyRepository.getScene(progress.currentSceneId);
        Dialogue dialogue = storyRepository.getDialogue(progress.currentDialogueId);

        Map<String, Object> data = new HashMap<>();
        data.put("activePlaythroughId", playthroughId);
        data.put("currentChapterId", progress.currentChapterId);
        data.put("currentSceneId", scene != null && scene.jsonId != null ? scene.jsonId : "");
        data.put("currentDialogueId", dialogue != null && dialogue.jsonId != null ? dialogue.jsonId : "");
        data.put("selectedCharacter", progress.selectedCharacter != null ? progress.selectedCharacter : "");
        data.put("totalPlayTimeMs", sessionManager.getTotalPlayTimeMs());
        data.put("lastPlayedAt", FieldValue.serverTimestamp());
        data.put("stats", StatsSnapshot.fromProgress(progress));

        return progressCurrentRef(uid).set(data, SetOptions.merge());
    }

    @NonNull
    private Map<String, Object> buildChoiceDocument(@NonNull PendingChoice pending, long totalPlayTimeMs) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("choicePointId", pending.choicePointId);
        doc.put("choiceSequenceId", pending.choicePointId);
        doc.put("chapterId", pending.chapterId);
        doc.put("sceneId", pending.sceneJsonId);
        doc.put("dialogueId", pending.dialogueJsonId);
        doc.put("selectedChoiceId", pending.selectedChoiceId);
        doc.put("selectedChoiceIndex", pending.selectedChoiceIndex);
        doc.put("selectedChoiceTextKey", pending.selectedChoiceText);
        doc.put("selectedAt", FieldValue.serverTimestamp());
        doc.put("totalPlayTimeMs", pending.totalPlayTimeMsAtChoice > 0
                ? pending.totalPlayTimeMsAtChoice : totalPlayTimeMs);
        doc.put("timeFromDialogueShownMs", pending.timeFromDialogueShownMs);
        doc.put("timeFromSceneStartMs", pending.timeFromSceneStartMs);
        doc.put("timeFromChapterStartMs", pending.timeFromChapterStartMs);

        Map<String, Object> statsBefore = StatsSnapshot.fromJson(pending.statsBeforeJson);
        Map<String, Object> statsAfter = StatsSnapshot.fromJson(pending.statsAfterJson);
        if (statsBefore != null) {
            doc.put("statsBefore", statsBefore);
        }
        if (statsAfter != null) {
            doc.put("statsAfter", statsAfter);
        }
        doc.put("dialogueJsonId", pending.dialogueJsonId);
        return doc;
    }
}
