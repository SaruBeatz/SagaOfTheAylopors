package com.example.sagaoftheaylopors.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.sagaoftheaylopors.data.database.AppDatabase;
import com.example.sagaoftheaylopors.data.database.StoryDataInitializer;
import com.example.sagaoftheaylopors.data.entities.Chapter;
import com.example.sagaoftheaylopors.data.entities.Choice;
import com.example.sagaoftheaylopors.data.entities.Dialogue;
import com.example.sagaoftheaylopors.data.entities.PlayerProgress;
import com.example.sagaoftheaylopors.data.entities.Scene;

import java.util.List;

/**
 * Repository pattern for story data access.
 * Provides a clean API for accessing story content and player progress.
 */
public class StoryRepository {
    private static StoryRepository INSTANCE;
    private final AppDatabase database;
    private final Context context;
    
    private StoryRepository(Context context) {
        this.context = context.getApplicationContext();
        database = AppDatabase.getDatabase(this.context);
    }
    
    public static synchronized StoryRepository getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new StoryRepository(context);
        }
        return INSTANCE;
    }
    
    // Chapter operations
    public List<Chapter> getAllChapters() {
        return database.chapterDao().getAllChapters();
    }
    
    public Chapter getChapter(int chapterId) {
        return database.chapterDao().getChapterById(chapterId);
    }
    
    private static final String TAG = "StoryRepository";
    
    public void markChapterCompleted(int chapterId) {
        Log.d(TAG, "=== MARK CHAPTER COMPLETED ===");
        Log.d(TAG, "Completing chapter: " + chapterId);
        
        // Mark chapter as completed in database
        database.chapterDao().markChapterCompleted(chapterId);
        Chapter chapter = database.chapterDao().getChapterById(chapterId);
        
        // Verify chapter was marked as completed
        if (chapter != null && chapter.isCompleted) {
            Log.d(TAG, "✓ Chapter " + chapterId + " successfully marked as completed in database");
        } else {
            Log.e(TAG, "✗ ERROR: Chapter " + chapterId + " was NOT marked as completed!");
        }
        
        // Determine next chapter ID
        int nextChapterId = -1;
        if (chapter != null && chapter.nextChapterId > 0) {
            nextChapterId = chapter.nextChapterId;
            Log.d(TAG, "Next chapter from database nextChapterId field: " + nextChapterId);
        } else if (chapterId < 7) {
            // If nextChapterId not set, assume sequential (chapter 1 -> 2, 2 -> 3, etc.)
            // Only 7 chapters total now
            nextChapterId = chapterId + 1;
            Log.d(TAG, "Next chapter calculated sequentially: " + nextChapterId + " (from " + chapterId + " + 1)");
        } else {
            Log.d(TAG, "Chapter " + chapterId + " is the last chapter (no next chapter)");
        }
        
        // Unlock next chapter if valid
        if (nextChapterId > 0 && nextChapterId <= 7) {
            Log.d(TAG, "Attempting to unlock chapter: " + nextChapterId);
            
            // CRITICAL FIX: Check if next chapter exists in database first
            // If it doesn't exist, initialize it from JSON before unlocking
            Chapter nextChapter = database.chapterDao().getChapterById(nextChapterId);
            if (nextChapter == null) {
                Log.w(TAG, "⚠ WARNING: Chapter " + nextChapterId + " does not exist in database!");
                Log.d(TAG, "Initializing chapter " + nextChapterId + " from JSON...");
                
                // Initialize the chapter from JSON (synchronous/blocking)
                // We need to do this synchronously to ensure it exists before unlocking
                StoryDataInitializer.initializeChapterSyncBlocking(context, nextChapterId);
                
                // Verify initialization
                nextChapter = database.chapterDao().getChapterById(nextChapterId);
                if (nextChapter != null) {
                    Log.d(TAG, "✓ Chapter " + nextChapterId + " successfully initialized from JSON");
                } else {
                    Log.e(TAG, "✗ ERROR: Failed to initialize chapter " + nextChapterId + " from JSON!");
                }
            } else {
                Log.d(TAG, "Chapter " + nextChapterId + " already exists in database (unlocked: " + nextChapter.isUnlocked + ")");
            }
            
            // Now unlock the chapter (whether it was just created or already existed)
            database.chapterDao().unlockChapter(nextChapterId);
            
            // Verify unlock succeeded
            Chapter unlockedChapter = database.chapterDao().getChapterById(nextChapterId);
            if (unlockedChapter != null && unlockedChapter.isUnlocked) {
                Log.d(TAG, "✓ Chapter " + nextChapterId + " successfully unlocked");
            } else {
                Log.e(TAG, "✗ ERROR: Chapter " + nextChapterId + " was NOT unlocked!");
                Log.e(TAG, "   Chapter exists: " + (unlockedChapter != null));
                Log.e(TAG, "   Is unlocked: " + (unlockedChapter != null ? unlockedChapter.isUnlocked : "N/A"));
            }
        } else {
            Log.d(TAG, "Skipping unlock - nextChapterId (" + nextChapterId + ") is invalid");
        }
        
        Log.d(TAG, "=== END MARK CHAPTER COMPLETED ===");
    }
    
    // Scene operations
    public List<Scene> getScenesByChapter(int chapterId) {
        return database.sceneDao().getScenesByChapter(chapterId);
    }
    
    public Scene getScene(int sceneId) {
        return database.sceneDao().getSceneById(sceneId);
    }
    
    public int getCompletedSceneCount(int chapterId) {
        return database.sceneDao().getCompletedSceneCount(chapterId);
    }

    public int getTotalSceneCount(int chapterId) {
        return database.sceneDao().getTotalSceneCount(chapterId);
    }

    public boolean isChapterComplete(int chapterId) {
        int completed = database.sceneDao().getCompletedSceneCount(chapterId);
        int total = database.sceneDao().getTotalSceneCount(chapterId);
        boolean isComplete = completed == total && total > 0;
        
        Log.d(TAG, "=== CHECK CHAPTER COMPLETE ===");
        Log.d(TAG, "Chapter: " + chapterId);
        Log.d(TAG, "Completed scenes: " + completed);
        Log.d(TAG, "Total scenes: " + total);
        Log.d(TAG, "Is complete: " + isComplete);
        
        // List all scenes in chapter for debugging
        List<Scene> allScenes = database.sceneDao().getScenesByChapter(chapterId);
        Log.d(TAG, "Detailed scene breakdown for chapter " + chapterId + ":");
        for (Scene scene : allScenes) {
            Log.d(TAG, "  Scene " + scene.sceneId + " (order " + scene.order + "): completed=" + scene.isCompleted);
        }
        
        if (completed != total) {
            Log.d(TAG, "⚠ Chapter not complete: " + completed + "/" + total + " scenes completed");
            Log.d(TAG, "   Still need to complete " + (total - completed) + " more scene(s)");
        }
        
        Log.d(TAG, "=== END CHECK CHAPTER COMPLETE ===");
        
        return isComplete;
    }
    
    public void markSceneCompleted(int sceneId) {
        Log.d(TAG, "=== MARK SCENE COMPLETED ===");
        Log.d(TAG, "Completing scene: " + sceneId);
        
        // Get scene before marking complete (to verify it exists)
        Scene sceneBefore = database.sceneDao().getSceneById(sceneId);
        if (sceneBefore == null) {
            Log.e(TAG, "✗ ERROR: Scene " + sceneId + " does not exist in database!");
            Log.d(TAG, "=== END MARK SCENE COMPLETED (ERROR) ===");
            return;
        }
        
        Log.d(TAG, "Scene " + sceneId + " before completion - belongs to chapter: " + sceneBefore.chapterId + ", order: " + sceneBefore.order + ", already completed: " + sceneBefore.isCompleted);
        
        // Check if this is the final scene (no next scene, or highest order)
        List<Scene> allScenes = database.sceneDao().getScenesByChapter(sceneBefore.chapterId);
        boolean isFinalScene = false;
        int maxOrder = 0;
        for (Scene s : allScenes) {
            maxOrder = Math.max(maxOrder, s.order);
        }
        isFinalScene = (sceneBefore.nextSceneId == -1) || (sceneBefore.order == maxOrder);
        
        Log.d(TAG, "Scene " + sceneId + " is final scene: " + isFinalScene + " (order: " + sceneBefore.order + ", max order: " + maxOrder + ", nextSceneId: " + sceneBefore.nextSceneId + ")");
        
        // Mark scene as completed
        database.sceneDao().markSceneCompleted(sceneId);
        
        // Verify the scene was marked as completed
        Scene sceneAfter = database.sceneDao().getSceneById(sceneId);
        if (sceneAfter != null) {
            if (sceneAfter.isCompleted) {
                Log.d(TAG, "✓ Scene " + sceneId + " successfully marked as completed in database");
                Log.d(TAG, "Scene " + sceneId + " belongs to chapter: " + sceneAfter.chapterId);
            } else {
                Log.e(TAG, "✗ ERROR: Scene " + sceneId + " was NOT marked as completed! (isCompleted=" + sceneAfter.isCompleted + ")");
            }
            
            // If this is the final scene, mark all other incomplete scenes as completed
            // This handles branching narratives where player may not visit all paths
            if (isFinalScene) {
                Log.d(TAG, "🎯 Final scene completed! Marking all remaining incomplete scenes in chapter " + sceneAfter.chapterId + " as completed...");
                
                // Get all scenes to see which ones are incomplete
                List<Scene> chapterScenes = database.sceneDao().getScenesByChapter(sceneAfter.chapterId);
                int incompleteCount = 0;
                for (Scene s : chapterScenes) {
                    if (!s.isCompleted) {
                        incompleteCount++;
                        Log.d(TAG, "  Scene " + s.sceneId + " (order " + s.order + ") is incomplete - will be marked as completed");
                    }
                }
                
                if (incompleteCount > 0) {
                    Log.d(TAG, "Marking " + incompleteCount + " incomplete scene(s) as completed...");
                    database.sceneDao().markAllIncompleteScenesCompleted(sceneAfter.chapterId);
                    
                    // Verify all scenes are now completed
                    chapterScenes = database.sceneDao().getScenesByChapter(sceneAfter.chapterId);
                    int stillIncomplete = 0;
                    for (Scene s : chapterScenes) {
                        if (!s.isCompleted) {
                            stillIncomplete++;
                            Log.e(TAG, "  ✗ ERROR: Scene " + s.sceneId + " (order " + s.order + ") is still incomplete!");
                        }
                    }
                    
                    if (stillIncomplete == 0) {
                        Log.d(TAG, "✓ All scenes in chapter " + sceneAfter.chapterId + " are now marked as completed");
                    } else {
                        Log.e(TAG, "✗ ERROR: " + stillIncomplete + " scene(s) are still incomplete after marking!");
                    }
                } else {
                    Log.d(TAG, "All scenes were already completed - no action needed");
                }
            }
            
            // Chapter unlock + cloud sync happen only in DialogueActivity.checkChapterCompletion()
            Log.d(TAG, "Scenes done for chapter " + sceneAfter.chapterId
                    + " — chapter flag set when DialogueActivity validates completion");
        } else {
            Log.e(TAG, "✗ ERROR: Scene " + sceneId + " not found after marking as completed!");
        }
        Log.d(TAG, "=== END MARK SCENE COMPLETED ===");
    }
    
    // Dialogue operations
    public List<Dialogue> getDialoguesByScene(int sceneId) {
        return database.dialogueDao().getDialoguesByScene(sceneId);
    }
    
    public Dialogue getDialogue(int dialogueId) {
        return database.dialogueDao().getDialogueById(dialogueId);
    }
    
    // Choice operations
    public List<Choice> getChoicesByDialogue(int dialogueId) {
        return database.choiceDao().getChoicesByDialogue(dialogueId);
    }
    
    public Choice getChoice(int choiceId) {
        return database.choiceDao().getChoiceById(choiceId);
    }
    
    // Player progress operations
    public PlayerProgress getProgress() {
        PlayerProgress progress = database.playerProgressDao().getProgress();
        if (progress == null) {
            // Initialize new progress
            progress = new PlayerProgress();
            database.playerProgressDao().insertProgress(progress);
        }
        return progress;
    }
    
    public void updateProgress(int chapterId, int sceneId, int dialogueId) {
        database.playerProgressDao().updateCurrentPosition(
            chapterId, sceneId, dialogueId, System.currentTimeMillis()
        );
    }
    
    /** Persist all 10 behavioral parameters after a choice. */
    public void updateBehavioralParams(PlayerProgress progress) {
        database.playerProgressDao().updateBehavioralParams(
                progress.sociality,
                progress.activity,
                progress.emotionalSensitivity,
                progress.anxiety,
                progress.selfControl,
                progress.impulsivity,
                progress.egoFocus,
                progress.rigidity,
                progress.negativeAffect,
                progress.adaptability
        );
    }
    
    public void saveProgress(PlayerProgress progress) {
        progress.lastSaveTimestamp = System.currentTimeMillis();
        database.playerProgressDao().updateProgress(progress);
    }
}
