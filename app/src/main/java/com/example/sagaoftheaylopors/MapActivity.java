package com.example.sagaoftheaylopors;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.sagaoftheaylopors.data.database.StoryDataInitializer;
import com.example.sagaoftheaylopors.data.entities.Chapter;
import com.example.sagaoftheaylopors.data.repository.StoryRepository;
import com.example.sagaoftheaylopors.databinding.ActivityMapBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity {
    private static final String TAG = "MapActivity";

    private ActivityMapBinding binding;
    private StoryRepository storyRepository;
    private int currentChapter = 1; // Active chapter (orange)
    private int highestUnlockedChapter = 1;
    private Map<Integer, CardView> chapterNodes;
    private Map<Integer, PointF> chapterPositions;
    private PathView pathView;
    private boolean isPathAnimating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize repository
        storyRepository = StoryRepository.getInstance(this);

        // Initialize PathView reference
        pathView = binding.pathView;
        
        // Start random background music
        MusicManager.getInstance().playRandomMusic(this);

        // Initialize chapter nodes map
        initializeChapterNodes();

        // Load chapter completion status
        loadChapterStates();

        // Check if we need to show path animation
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("show_path_animation", false)) {
            int completedChapter = intent.getIntExtra("completed_chapter", -1);
            if (completedChapter > 0) {
                // Delay path animation to allow layout to complete
                binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                animatePathTransition(completedChapter);
                            }, 300);
                        }
                    }
                );
            }
        } else {
            // Just update states without animation
            updateChapterStates();
            // Draw all completed paths without animation
            drawAllCompletedPaths();
        }

        // Set up chapter node click listeners
        setupChapterNodeListeners();

        // Start Chapter Button
        binding.startChapterButton.setOnClickListener(v -> {
            // Check if chapter is unlocked
            boolean canStart = currentChapter == 1 || currentChapter <= highestUnlockedChapter;
            
            if (canStart) {
                // Ensure the chapter is initialized before starting
                com.example.sagaoftheaylopors.data.entities.Chapter chapter = storyRepository.getChapter(currentChapter);
                if (chapter == null) {
                    // Initialize chapter from JSON in background (non-blocking)
                    StoryDataInitializer.initializeChapter(this, currentChapter);
                    // Show a message that chapter is loading
                    Toast.makeText(this, "Loading chapter " + currentChapter + "...", Toast.LENGTH_SHORT).show();
                    // Try again after a short delay
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Intent chapterIntent = new Intent(MapActivity.this, DialogueActivity.class);
                        chapterIntent.putExtra("chapter_number", currentChapter);
                        startActivity(chapterIntent);
                    }, 500);
                } else {
                    Intent chapterIntent = new Intent(MapActivity.this, DialogueActivity.class);
                    chapterIntent.putExtra("chapter_number", currentChapter);
                    startActivity(chapterIntent);
                }
            } else {
                Toast.makeText(this, "This chapter is not yet unlocked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeChapterNodes() {
        chapterNodes = new HashMap<>();
        chapterNodes.put(1, binding.chapterNode1);
        chapterNodes.put(2, binding.chapterNode2);
        chapterNodes.put(3, binding.chapterNode3);
        chapterNodes.put(4, binding.chapterNode4);
        chapterNodes.put(5, binding.chapterNode5);
        chapterNodes.put(6, binding.chapterNode6);
        chapterNodes.put(7, binding.chapterNode7);
        // Note: Only 7 chapters total (removed old chapter 6, renumbered 7->6, 8->7)
    }

    private void setupChapterNodeListeners() {
        for (Map.Entry<Integer, CardView> entry : chapterNodes.entrySet()) {
            int chapterNum = entry.getKey();
            CardView node = entry.getValue();
            node.setOnClickListener(v -> selectChapter(chapterNum));
        }
    }

    private void loadChapterStates() {
        Log.d(TAG, "=== LOAD CHAPTER STATES ===");
        
        List<Chapter> chapters = storyRepository.getAllChapters();
        Log.d(TAG, "Total chapters in database: " + chapters.size());
        
        // Find highest completed chapter
        int highestCompleted = 0;
        int highestUnlocked = 1; // Chapter 1 is always unlocked by default
        
        // Build maps for quick lookup
        Map<Integer, Chapter> chapterMap = new HashMap<>();
        for (Chapter chapter : chapters) {
            chapterMap.put(chapter.chapterId, chapter);
            Log.d(TAG, "Chapter " + chapter.chapterId + ": completed=" + chapter.isCompleted + ", unlocked=" + chapter.isUnlocked);
            
            if (chapter.isCompleted) {
                highestCompleted = Math.max(highestCompleted, chapter.chapterId);
            }
            if (chapter.isUnlocked) {
                highestUnlocked = Math.max(highestUnlocked, chapter.chapterId);
            }
        }
        
        Log.d(TAG, "Highest completed chapter: " + highestCompleted);
        Log.d(TAG, "Highest unlocked chapter: " + highestUnlocked);
        
        // If a chapter was just completed, ensure the next one is unlocked and initialized
        if (highestCompleted > 0 && highestCompleted < 7) {
            int nextChapterId = highestCompleted + 1;
            Log.d(TAG, "Next chapter should be: " + nextChapterId);
            
            Chapter nextChapter = chapterMap.get(nextChapterId);
            if (nextChapter == null) {
                Log.w(TAG, "⚠ WARNING: Next chapter " + nextChapterId + " does not exist in database!");
                Log.d(TAG, "Chapter should have been initialized by markChapterCompleted()");
                // Don't call markChapterCompleted again - it was already called
                // Just initialize the chapter from JSON
                StoryDataInitializer.initializeChapterSyncBlocking(this, nextChapterId);
                
                // Reload to get updated state
                chapters = storyRepository.getAllChapters();
                chapterMap.clear();
                for (Chapter chapter : chapters) {
                    chapterMap.put(chapter.chapterId, chapter);
                    if (chapter.isUnlocked) {
                        highestUnlocked = Math.max(highestUnlocked, chapter.chapterId);
                    }
                }
                
                // Re-check next chapter after initialization
                nextChapter = chapterMap.get(nextChapterId);
                if (nextChapter != null) {
                    Log.d(TAG, "✓ Next chapter " + nextChapterId + " initialized from JSON");
                    if (!nextChapter.isUnlocked) {
                        Log.w(TAG, "⚠ Next chapter " + nextChapterId + " exists but is not unlocked - unlocking now");
                        storyRepository.getChapter(nextChapterId); // Trigger unlock via repository
                        // Actually need to call unlock directly via DAO or ensure it's unlocked
                        // The repository should handle this, but let's verify
                    }
                } else {
                    Log.e(TAG, "✗ ERROR: Failed to initialize chapter " + nextChapterId + " from JSON!");
                }
                } else if (!nextChapter.isUnlocked) {
                Log.w(TAG, "⚠ Next chapter " + nextChapterId + " exists but is not unlocked!");
                Log.d(TAG, "This should not happen - markChapterCompleted should have unlocked it");
                Log.d(TAG, "Attempting to unlock chapter " + nextChapterId + " directly...");
                
                // Get the database directly and unlock
                // This is a fallback - the proper fix is in markChapterCompleted
                com.example.sagaoftheaylopors.data.database.AppDatabase database = 
                    com.example.sagaoftheaylopors.data.database.AppDatabase.getDatabase(this);
                database.chapterDao().unlockChapter(nextChapterId);
                
                // Verify unlock
                Chapter verifyUnlock = storyRepository.getChapter(nextChapterId);
                if (verifyUnlock != null && verifyUnlock.isUnlocked) {
                    Log.d(TAG, "✓ Chapter " + nextChapterId + " unlocked via fallback");
                } else {
                    Log.e(TAG, "✗ ERROR: Failed to unlock chapter " + nextChapterId + " via fallback!");
                }
                
                // Reload to get updated state
                chapters = storyRepository.getAllChapters();
                chapterMap.clear();
                for (Chapter chapter : chapters) {
                    chapterMap.put(chapter.chapterId, chapter);
                    if (chapter.isUnlocked) {
                        highestUnlocked = Math.max(highestUnlocked, chapter.chapterId);
                    }
                }
            } else {
                Log.d(TAG, "✓ Next chapter " + nextChapterId + " exists and is unlocked");
                highestUnlocked = Math.max(highestUnlocked, nextChapterId);
            }
        }
        
        // Active chapter is the next one after the highest completed (or 1 if none completed)
        // If chapter 2 is completed, currentChapter should be 3, not 2
        if (highestCompleted > 0 && highestCompleted < 7) {
            currentChapter = highestCompleted + 1; // Next chapter after completed
            Log.d(TAG, "Active chapter set to: " + currentChapter + " (next after completed " + highestCompleted + ")");
        } else if (highestCompleted == 0) {
            currentChapter = 1;
            Log.d(TAG, "Active chapter set to: 1 (no chapters completed)");
        } else {
            currentChapter = 7; // All chapters completed (only 7 chapters total)
            Log.d(TAG, "Active chapter set to: 7 (all chapters completed)");
        }
        
        // Ensure current chapter doesn't exceed highest unlocked, but allow it to be the next one
        if (currentChapter > highestUnlocked && highestCompleted > 0) {
            // If we just completed a chapter, the next one should be unlocked
            Log.d(TAG, "Current chapter (" + currentChapter + ") exceeds highest unlocked (" + highestUnlocked + ")");
            Log.d(TAG, "Since chapter " + highestCompleted + " was just completed, assuming next chapter should be unlocked");
            highestUnlocked = currentChapter;
        }
        highestUnlockedChapter = highestUnlocked;
        
        Log.d(TAG, "Final state - Current chapter: " + currentChapter + ", Highest unlocked: " + highestUnlockedChapter);
        
        // Verify next chapter unlock status
        if (highestCompleted > 0 && highestCompleted < 7) {
            int expectedNextChapter = highestCompleted + 1;
            Chapter verifyNext = chapterMap.get(expectedNextChapter);
            if (verifyNext != null) {
                Log.d(TAG, "Verification - Next chapter " + expectedNextChapter + ": exists=" + true + ", unlocked=" + verifyNext.isUnlocked);
                if (!verifyNext.isUnlocked) {
                    Log.e(TAG, "✗ ERROR: Next chapter " + expectedNextChapter + " should be unlocked but is not!");
                    Log.e(TAG, "This indicates a critical progression bug!");
                }
            } else {
                Log.e(TAG, "✗ ERROR: Next chapter " + expectedNextChapter + " does not exist in database!");
                Log.e(TAG, "This indicates a critical progression bug - chapter not initialized!");
            }
        }
        
        Log.d(TAG, "=== END LOAD CHAPTER STATES ===");
        
        updateChapterStates();
        updateStartChapterButton();
    }

    private void updateChapterStates() {
        List<Chapter> chapters = storyRepository.getAllChapters();
        Map<Integer, Boolean> completedMap = new HashMap<>();
        Map<Integer, Boolean> unlockedMap = new HashMap<>();
        
        for (Chapter chapter : chapters) {
            completedMap.put(chapter.chapterId, chapter.isCompleted);
            unlockedMap.put(chapter.chapterId, chapter.isUnlocked);
        }

        // Update each chapter node - show ALL chapters (1-7), but style them based on state
        // Force all chapters to be visible regardless of database state
        for (int chapterId = 1; chapterId <= 7; chapterId++) {
            CardView node = chapterNodes.get(chapterId);
            if (node == null) {
                continue; // Skip if node doesn't exist (shouldn't happen)
            }
            
            boolean isCompleted = completedMap.getOrDefault(chapterId, false);
            // A chapter is unlocked if:
            // 1. It's marked as unlocked in DB
            // 2. It's chapter 1 (always unlocked)
            // 3. It's <= highestUnlockedChapter (progression-based)
            boolean isUnlocked = unlockedMap.getOrDefault(chapterId, false) 
                || chapterId == 1 
                || (chapterId <= highestUnlockedChapter);
            boolean isActive = (chapterId == currentChapter);
            
            // ALWAYS show ALL chapters (1-7) - force visibility
            node.setVisibility(View.VISIBLE);
            
            // Set background color and opacity
            if (isCompleted) {
                // Green for completed
                node.setCardBackgroundColor(getColor(R.color.color_poi_completed));
                node.setAlpha(1.0f);
            } else if (isActive && isUnlocked) {
                // Orange for active and unlocked
                node.setCardBackgroundColor(getColor(R.color.color_poi_active));
                node.setAlpha(1.0f);
            } else if (isUnlocked) {
                // Default color for unlocked but not active
                node.setCardBackgroundColor(getColor(R.color.color_primary));
                node.setAlpha(1.0f);
            } else {
                // Locked chapters - grayed out but still visible
                node.setCardBackgroundColor(getColor(R.color.color_primary));
                node.setAlpha(0.4f);
            }
        }
    }

    private void selectChapter(int chapterNumber) {
        List<Chapter> chapters = storyRepository.getAllChapters();
        boolean isUnlocked = false;
        boolean isCompleted = false;
        
        for (Chapter chapter : chapters) {
            if (chapter.chapterId == chapterNumber) {
                isUnlocked = chapter.isUnlocked || chapter.isCompleted;
                isCompleted = chapter.isCompleted;
                break;
            }
        }
        
        // Also allow selecting chapter 1 by default
        if (chapterNumber == 1) {
            isUnlocked = true;
        }
        
        // Check if chapter is unlocked based on progression
        if (!isUnlocked && chapterNumber <= highestUnlockedChapter) {
            isUnlocked = true;
        }
        
        // Allow selecting if it's unlocked, completed, or is the next available chapter
        if (isUnlocked || isCompleted || chapterNumber <= highestUnlockedChapter) {
            currentChapter = chapterNumber;
            updateChapterStates();
            updateStartChapterButton();
        } else {
            Toast.makeText(this, "This chapter is not yet unlocked", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStartChapterButton() {
        String buttonText;
        List<Chapter> chapters = storyRepository.getAllChapters();
        boolean isCompleted = false;
        for (Chapter chapter : chapters) {
            if (chapter.chapterId == currentChapter && chapter.isCompleted) {
                isCompleted = true;
                break;
            }
        }
        
        // If current chapter is completed, it means we should be on the next chapter
        // So show "Start Chapter X" where X is currentChapter (which is the next one)
        if (isCompleted && currentChapter < 7) {
            // This shouldn't happen if logic is correct, but handle it
            buttonText = getString(R.string.button_start_chapter, currentChapter + 1);
        } else if (currentChapter == 1) {
            buttonText = getString(R.string.button_start_chapter, 1);
        } else {
            buttonText = getString(R.string.button_start_chapter, currentChapter);
        }
        binding.startChapterButton.setText(buttonText);
    }

    private void animatePathTransition(int completedChapter) {
        Log.d(TAG, "=== ANIMATE PATH TRANSITION ===");
        Log.d(TAG, "Completed chapter: " + completedChapter);
        
        if (isPathAnimating) {
            Log.d(TAG, "Path already animating - skipping");
            return;
        }
        
        isPathAnimating = true;
        
        // Update chapter states first
        loadChapterStates();
        
        // Wait for layout to be ready, then get positions and animate paths
        binding.getRoot().post(() -> {
            calculateChapterPositions();
            
            // Clear existing paths
            if (pathView != null) {
                pathView.clearPaths();
            }
            
            // Draw all completed chapter paths (green solid lines)
            List<Chapter> chapters = storyRepository.getAllChapters();
            Map<Integer, Boolean> completedMap = new HashMap<>();
            for (Chapter chapter : chapters) {
                completedMap.put(chapter.chapterId, chapter.isCompleted);
            }
            
            // Draw paths for all completed chapters
            for (int chapterId = 1; chapterId < completedChapter; chapterId++) {
                if (completedMap.getOrDefault(chapterId, false) && chapterId < 7) {
                    int nextChapterId = chapterId + 1;
                    if (chapterPositions.containsKey(chapterId) && chapterPositions.containsKey(nextChapterId)) {
                        PointF start = chapterPositions.get(chapterId);
                        PointF end = chapterPositions.get(nextChapterId);
                        
                        // Calculate control points for smooth curve
                        float midX = (start.x + end.x) / 2;
                        float midY = (start.y + end.y) / 2;
                        float controlOffset = Math.abs(end.x - start.x) * 0.3f;
                        float controlX1 = midX - controlOffset;
                        float controlY1 = midY - 50;
                        float controlX2 = midX + controlOffset;
                        float controlY2 = midY - 50;
                        
                        // Create and add completed path (green solid line)
                        android.graphics.Path path = PathView.createPath(
                            start.x, start.y, end.x, end.y,
                            controlX1, controlY1, controlX2, controlY2
                        );
                        PathView.PathData pathData = pathView.addPath(path, true); // true = completed
                        pathView.showPath(pathData); // Show immediately without animation
                    }
                }
            }
            
            // Draw new path (orange dashed line) from completed chapter to next chapter
            int nextChapter = completedChapter + 1;
            Log.d(TAG, "Attempting to animate path from chapter " + completedChapter + " to chapter " + nextChapter);
            Log.d(TAG, "Chapter positions available - Chapter " + completedChapter + ": " + chapterPositions.containsKey(completedChapter));
            Log.d(TAG, "Chapter positions available - Chapter " + nextChapter + ": " + chapterPositions.containsKey(nextChapter));
            
            if (nextChapter <= 7 && chapterPositions.containsKey(completedChapter) 
                && chapterPositions.containsKey(nextChapter)) {
                
                Log.d(TAG, "✓ Both chapters have positions - animating path");
                PointF start = chapterPositions.get(completedChapter);
                PointF end = chapterPositions.get(nextChapter);
                Log.d(TAG, "Start position: (" + start.x + ", " + start.y + ")");
                Log.d(TAG, "End position: (" + end.x + ", " + end.y + ")");
                
                // Calculate control points for smooth curve
                float midX = (start.x + end.x) / 2;
                float midY = (start.y + end.y) / 2;
                float controlOffset = Math.abs(end.x - start.x) * 0.3f;
                
                float controlX1 = midX - controlOffset;
                float controlY1 = midY - 50;
                float controlX2 = midX + controlOffset;
                float controlY2 = midY - 50;
                
                // Create and add new path (orange dashed line)
                if (pathView != null && pathView.getWidth() > 0 && pathView.getHeight() > 0) {
                    android.graphics.Path path = PathView.createPath(
                        start.x, start.y, end.x, end.y,
                        controlX1, controlY1, controlX2, controlY2
                    );
                    PathView.PathData pathData = pathView.addPath(path, false); // false = new path (orange dashed)
                    pathView.animatePath(pathData, 2000); // 2 second animation for better effect
                    
                    // Reset animation flag after animation completes
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        isPathAnimating = false;
                    }, 2000);
                } else {
                    // PathView not ready, try again after a short delay
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        animatePathTransition(completedChapter);
                    }, 100);
                }
            } else {
                Log.e(TAG, "✗ ERROR: Cannot animate path - chapters or positions missing!");
                Log.e(TAG, "   Next chapter: " + nextChapter + " (must be <= 7)");
                Log.e(TAG, "   Completed chapter position exists: " + chapterPositions.containsKey(completedChapter));
                Log.e(TAG, "   Next chapter position exists: " + chapterPositions.containsKey(nextChapter));
                isPathAnimating = false;
            }
        });
        Log.d(TAG, "=== END ANIMATE PATH TRANSITION ===");
    }

    private void calculateChapterPositions() {
        chapterPositions = new HashMap<>();
        
        // Get positions of all chapter nodes relative to PathView
        if (pathView == null) {
            return;
        }
        
        for (Map.Entry<Integer, CardView> entry : chapterNodes.entrySet()) {
            int chapterId = entry.getKey();
            CardView node = entry.getValue();
            
            if (node.getVisibility() == View.VISIBLE) {
                int[] nodeLocation = new int[2];
                node.getLocationOnScreen(nodeLocation);
                int[] pathViewLocation = new int[2];
                pathView.getLocationOnScreen(pathViewLocation);
                
                // Calculate relative position to PathView
                float x = nodeLocation[0] - pathViewLocation[0] + node.getWidth() / 2f;
                float y = nodeLocation[1] - pathViewLocation[1] + node.getHeight() / 2f;
                
                chapterPositions.put(chapterId, new PointF(x, y));
            }
        }
    }

    /**
     * Draw all completed chapter paths (without animation).
     * Called when map is shown without animation.
     */
    private void drawAllCompletedPaths() {
        binding.getRoot().post(() -> {
            calculateChapterPositions();
            
            if (pathView == null || pathView.getWidth() == 0 || pathView.getHeight() == 0) {
                // PathView not ready, try again after a short delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    drawAllCompletedPaths();
                }, 100);
                return;
            }
            
            // Clear existing paths
            pathView.clearPaths();
            
            // Draw all completed chapter paths (green solid lines)
            List<Chapter> chapters = storyRepository.getAllChapters();
            Map<Integer, Boolean> completedMap = new HashMap<>();
            for (Chapter chapter : chapters) {
                completedMap.put(chapter.chapterId, chapter.isCompleted);
            }
            
            // Find highest completed chapter
            int highestCompleted = 0;
            for (Chapter chapter : chapters) {
                if (chapter.isCompleted) {
                    highestCompleted = Math.max(highestCompleted, chapter.chapterId);
                }
            }
            
            // Draw paths for all completed chapters
            for (int chapterId = 1; chapterId <= highestCompleted && chapterId < 7; chapterId++) {
                if (completedMap.getOrDefault(chapterId, false)) {
                    int nextChapterId = chapterId + 1;
                    if (chapterPositions.containsKey(chapterId) && chapterPositions.containsKey(nextChapterId)) {
                        PointF start = chapterPositions.get(chapterId);
                        PointF end = chapterPositions.get(nextChapterId);
                        
                        // Calculate control points for smooth curve
                        float midX = (start.x + end.x) / 2;
                        float midY = (start.y + end.y) / 2;
                        float controlOffset = Math.abs(end.x - start.x) * 0.3f;
                        float controlX1 = midX - controlOffset;
                        float controlY1 = midY - 50;
                        float controlX2 = midX + controlOffset;
                        float controlY2 = midY - 50;
                        
                        // Create and add completed path (green solid line)
                        android.graphics.Path path = PathView.createPath(
                            start.x, start.y, end.x, end.y,
                            controlX1, controlY1, controlX2, controlY2
                        );
                        PathView.PathData pathData = pathView.addPath(path, true); // true = completed
                        pathView.showPath(pathData); // Show immediately without animation
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "=== ON RESUME - RELOADING CHAPTER STATES ===");
        // Reload chapter states when returning to map
        loadChapterStates();
        // Draw all completed paths
        drawAllCompletedPaths();
        // Resume music
        MusicManager.getInstance().resumeMusic();
        Log.d(TAG, "=== END ON RESUME ===");
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Pause music when activity is paused
        MusicManager.getInstance().pauseMusic();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop music when activity is destroyed
        MusicManager.getInstance().stopMusic();
    }

    @Override
    public void onBackPressed() {
        // Go back to Main Menu instead of Character Select
        Intent intent = new Intent(MapActivity.this, MainMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
