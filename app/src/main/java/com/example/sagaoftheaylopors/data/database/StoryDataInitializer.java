package com.example.sagaoftheaylopors.data.database;

import android.content.Context;
import android.util.Log;

import com.example.sagaoftheaylopors.data.entities.Chapter;
import com.example.sagaoftheaylopors.data.entities.Choice;
import com.example.sagaoftheaylopors.data.entities.Dialogue;
import com.example.sagaoftheaylopors.data.entities.PlayerProgress;
import com.example.sagaoftheaylopors.data.entities.Scene;
import com.example.sagaoftheaylopors.data.parser.ChapterJsonParser;
import com.example.sagaoftheaylopors.data.repository.StoryRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Initializes the database with Chapter 1 story data from JSON.
 * This should be called once when the app is first launched.
 */
public class StoryDataInitializer {
    private static final String TAG = "StoryDataInitializer";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    /**
     * Initialize Chapter 1 data asynchronously on a background thread.
     * This method returns immediately and performs the initialization in the background.
     */
    public static void initializeChapter1(Context context) {
        executor.execute(() -> initializeChapter1Sync(context));
    }
    
    /**
     * Synchronous initialization method that performs database operations.
     * This must be called on a background thread.
     */
    private static void initializeChapter1Sync(Context context) {
        try {
            StoryRepository repository = StoryRepository.getInstance(context);
            AppDatabase database = AppDatabase.getDatabase(context);
            
            // Check if Chapter 1 already exists
            Chapter existingChapter = repository.getChapter(1);
            if (existingChapter != null) {
                Log.d(TAG, "Chapter 1 already initialized");
                return; // Already initialized
            }
            
            // Parse Chapter 1 from JSON
            ChapterJsonParser.ChapterData chapterData = ChapterJsonParser.parseChapterFromAssets(context, "chapter_1.json");
            if (chapterData == null) {
                Log.e(TAG, "Failed to parse chapter_1.json");
                return;
            }
            
            // Insert chapter
            database.chapterDao().insertChapter(chapterData.chapter);
            
            // Insert scenes, dialogues, and choices
            for (ChapterJsonParser.SceneData sceneData : chapterData.scenes) {
                database.sceneDao().insertScene(sceneData.scene);
                database.dialogueDao().insertDialogues(sceneData.dialogues);
                if (!sceneData.choices.isEmpty()) {
                    database.choiceDao().insertChoices(sceneData.choices);
                }
            }
            
            // Initialize player progress if not exists
            PlayerProgress progress = repository.getProgress();
            if (progress == null) {
                // Set to first dialogue of first scene
                if (!chapterData.scenes.isEmpty() && !chapterData.scenes.get(0).dialogues.isEmpty()) {
                    int firstSceneId = chapterData.scenes.get(0).scene.sceneId;
                    int firstDialogueId = chapterData.scenes.get(0).dialogues.get(0).dialogueId;
                    progress = new PlayerProgress(1, firstSceneId, firstDialogueId, "cat");
                    database.playerProgressDao().insertProgress(progress);
                } else {
                    progress = new PlayerProgress();
                    database.playerProgressDao().insertProgress(progress);
                }
            }
            
            Log.d(TAG, "Chapter 1 initialized successfully from JSON");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Chapter 1 from JSON", e);
        }
    }
    
    /**
     * Initialize any chapter data asynchronously on a background thread.
     * This method returns immediately and performs the initialization in the background.
     */
    public static void initializeChapter(Context context, int chapterNumber) {
        executor.execute(() -> initializeChapterSync(context, chapterNumber));
    }
    
    /**
     * Synchronous initialization method for any chapter.
     * This must be called on a background thread.
     */
    private static void initializeChapterSync(Context context, int chapterNumber) {
        try {
            StoryRepository repository = StoryRepository.getInstance(context);
            AppDatabase database = AppDatabase.getDatabase(context);
            
            // Check if chapter already exists
            Chapter existingChapter = repository.getChapter(chapterNumber);
            if (existingChapter != null) {
                Log.d(TAG, "Chapter " + chapterNumber + " already initialized");
                return; // Already initialized
            }
            
            // Parse chapter from JSON
            String fileName = "chapter_" + chapterNumber + ".json";
            ChapterJsonParser.ChapterData chapterData = ChapterJsonParser.parseChapterFromAssets(context, fileName);
            if (chapterData == null) {
                Log.e(TAG, "Failed to parse " + fileName);
                return;
            }
            
            // Insert chapter
            database.chapterDao().insertChapter(chapterData.chapter);
            
            // Insert scenes, dialogues, and choices
            for (ChapterJsonParser.SceneData sceneData : chapterData.scenes) {
                database.sceneDao().insertScene(sceneData.scene);
                database.dialogueDao().insertDialogues(sceneData.dialogues);
                if (!sceneData.choices.isEmpty()) {
                    database.choiceDao().insertChoices(sceneData.choices);
                }
            }
            
            Log.d(TAG, "Chapter " + chapterNumber + " initialized successfully from JSON");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Chapter " + chapterNumber + " from JSON", e);
        }
    }
    
    /**
     * Initialize chapter synchronously (blocking). Use this when you need to wait for initialization.
     * WARNING: This blocks the current thread!
     */
    public static void initializeChapterSyncBlocking(Context context, int chapterNumber) {
        initializeChapterSync(context, chapterNumber);
    }
    
    /**
     * Shutdown the executor service. Call this when the app is terminating.
     */
    public static void shutdown() {
        executor.shutdown();
    }
}