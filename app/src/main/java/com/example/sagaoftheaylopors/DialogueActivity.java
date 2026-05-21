package com.example.sagaoftheaylopors;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sagaoftheaylopors.databinding.ActivityDialogueBinding;
import com.example.sagaoftheaylopors.data.database.StoryDataInitializer;
import com.example.sagaoftheaylopors.data.entities.Choice;
import com.example.sagaoftheaylopors.data.entities.Dialogue;
import com.example.sagaoftheaylopors.data.entities.PlayerProgress;
import com.example.sagaoftheaylopors.data.repository.StoryRepository;

import java.util.List;

public class DialogueActivity extends AppCompatActivity {

    // Text rendering state machine
    private enum TextState {
        TEXT_RENDERING,     // Text is currently animating/revealing
        TEXT_COMPLETE,      // All text has been displayed
        CHOICE_PENDING,     // Text complete, choices available but waiting for player acknowledgment
        CHOICE_ACTIVE       // Choices are displayed and player can select
    }
    
    private TextState currentTextState = TextState.TEXT_RENDERING;

    private ActivityDialogueBinding binding;
    private StoryRepository storyRepository;
    private PlayerProgress playerProgress;
    private Dialogue currentDialogue;
    private List<Choice> currentChoices;
    private int chapterNumber = 1;
    
    // Text animation state
    private String fullText;
    private int currentChunkStart = 0;
    private int currentCharIndex = 0;
    private Handler textHandler;
    private Runnable textAnimationRunnable;
    private boolean isAnimating = false;
    private static final int CHUNK_SIZE = 150; // Characters per chunk (100-200 range)

    // ── Text pagination — long dialogues are split into pages ────────────
    private final java.util.List<String> textPages = new java.util.ArrayList<>();
    private int currentPageIndex = 0;
    // ~130 chars per page keeps text within ~5 lines on typical landscape phones
    private static final int PAGE_LIMIT = 130;

    // ── Peek mode: hold 2 s outside dialogue box to inspect background ──
    private final Handler peekHandler = new Handler(Looper.getMainLooper());
    private boolean peekActive = false;
    private final Runnable activatePeek = () -> {
        peekActive = true;
        animateDialogueUi(0f, 250);
    };
    private static final long LETTER_DELAY = 30; // Milliseconds between letters
    private static final long FAST_LETTER_DELAY = 5; // Fast animation speed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDialogueBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Very light soft-focus on portraits only (radius 2 = almost imperceptible).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            RenderEffect portraitBlur = RenderEffect.createBlurEffect(2f, 2f, Shader.TileMode.CLAMP);
            binding.characterPortraitImageView.setRenderEffect(portraitBlur);
            binding.characterPortraitRight.setRenderEffect(portraitBlur);
        }

        textHandler = new Handler(Looper.getMainLooper());

        // Initialize database and repository
        storyRepository = StoryRepository.getInstance(this);
        
        // Get chapter number from intent
        chapterNumber = getIntent().getIntExtra("chapter_number", 1);
        
        // Initialize the requested chapter if it doesn't exist (blocking call to ensure data is loaded)
        com.example.sagaoftheaylopors.data.entities.Chapter chapter = storyRepository.getChapter(chapterNumber);
        if (chapter == null) {
            // Chapter doesn't exist, initialize it from JSON
            StoryDataInitializer.initializeChapterSyncBlocking(this, chapterNumber);
            // Reload to get the initialized chapter
            chapter = storyRepository.getChapter(chapterNumber);
        }
        
        // Also initialize Chapter 1 if needed (for first-time users)
        StoryDataInitializer.initializeChapter1(this);
        
        // Load player progress
        playerProgress = storyRepository.getProgress();
        
        // If starting a new chapter, set progress to first dialogue of that chapter
        if (chapterNumber != playerProgress.currentChapterId) {
            // Find first dialogue of the chapter
            List<com.example.sagaoftheaylopors.data.entities.Scene> scenes = 
                storyRepository.getScenesByChapter(chapterNumber);
            if (!scenes.isEmpty()) {
                List<Dialogue> dialogues = storyRepository.getDialoguesByScene(scenes.get(0).sceneId);
                if (!dialogues.isEmpty()) {
                    playerProgress.currentChapterId = chapterNumber;
                    playerProgress.currentSceneId = scenes.get(0).sceneId;
                    playerProgress.currentDialogueId = dialogues.get(0).dialogueId;
                    storyRepository.updateProgress(
                        playerProgress.currentChapterId,
                        playerProgress.currentSceneId,
                        playerProgress.currentDialogueId
                    );
                }
            }
        }

        updateChapterTitle();
        loadCurrentDialogue();

        // Pause Button
        binding.pauseButton.setOnClickListener(v -> {
            Intent intent = new Intent(DialogueActivity.this, PauseActivity.class);
            startActivity(intent);
        });
        
        // Start random background music
        MusicManager.getInstance().playRandomMusic(this);

        // Text area click handler - handles text progression and choice display
        binding.textArea.setOnClickListener(v -> {
            if (currentTextState == TextState.TEXT_RENDERING && isAnimating) {
                // Speed up animation - complete current chunk immediately
                if (textAnimationRunnable != null) {
                    textHandler.removeCallbacks(textAnimationRunnable);
                }
                completeCurrentChunk();
            } else if (currentTextState == TextState.TEXT_RENDERING && !isAnimating) {
                // Text was rendering but animation stopped (chunk complete)
                // Check if more chunks to show or if text is complete
                if (currentChunkStart < fullText.length() && currentChunkStart + CHUNK_SIZE < fullText.length()) {
                    // More chunks to show - show next chunk
                    showNextChunk();
                } else {
                    // All text shown - ensure full text is displayed and check for choices
                    binding.dialogueTextView.setText(fullText);
                    onTextComplete();
                }
            } else if (currentTextState == TextState.TEXT_COMPLETE) {
                if (currentPageIndex < textPages.size() - 1) {
                    // Advance to the next page of the current dialogue.
                    currentPageIndex++;
                    fullText = textPages.get(currentPageIndex);
                    hideChoiceIndicator();
                    currentTextState = TextState.TEXT_RENDERING;
                    startTextAnimation();
                } else {
                    // Last page — move to next dialogue.
                    progressToNextDialogue();
                }
            } else if (currentTextState == TextState.CHOICE_PENDING) {
                // Player acknowledged text - show choices
                showChoices();
            }
            // If CHOICE_ACTIVE, clicking text area does nothing (user must click choice buttons)
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload progress in case it was updated elsewhere
        playerProgress = storyRepository.getProgress();
        loadCurrentDialogue();
        // Resume music when activity resumes
        MusicManager.getInstance().resumeMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop text animation when paused
        stopTextAnimation();
        // Pause music when activity is paused
        MusicManager.getInstance().pauseMusic();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        peekHandler.removeCallbacks(activatePeek);
        MusicManager.getInstance().stopMusic();
    }

    /**
     * Intercept touch events to detect a 2-second hold outside the dialogue box.
     * When triggered, fades out the UI so the player can inspect the background.
     * Releasing the finger restores the UI immediately.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!isTouchInsideDialogueBox(event)) {
                    peekHandler.postDelayed(activatePeek, 1000);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                peekHandler.removeCallbacks(activatePeek);
                if (peekActive) {
                    peekActive = false;
                    animateDialogueUi(1f, 200);
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private boolean isTouchInsideDialogueBox(MotionEvent event) {
        int[] loc = new int[2];
        binding.textArea.getLocationOnScreen(loc);
        float x = event.getRawX();
        float y = event.getRawY();
        return x >= loc[0] && x <= loc[0] + binding.textArea.getWidth()
            && y >= loc[1] && y <= loc[1] + binding.textArea.getHeight();
    }

    private void animateDialogueUi(float alpha, int durationMs) {
        binding.textArea.animate().alpha(alpha).setDuration(durationMs).start();
        binding.titleContainer.animate().alpha(alpha).setDuration(durationMs).start();
        binding.characterPortraitImageView.animate().alpha(alpha).setDuration(durationMs).start();
        binding.characterPortraitRight.animate().alpha(alpha).setDuration(durationMs).start();
    }

    // ── Camera effects ──────────────────────────────────────────────────

    /**
     * Plays a background camera effect on the backgroundImageView.
     * Previous animations are cancelled and transform is reset first.
     */
    private void playCameraEffect(String effect) {
        binding.backgroundImageView.animate().cancel();
        binding.backgroundImageView.setScaleX(1f);
        binding.backgroundImageView.setScaleY(1f);
        binding.backgroundImageView.setTranslationX(0f);
        binding.backgroundImageView.setTranslationY(0f);

        switch (effect) {
            case "pan_up":
                binding.backgroundImageView.animate()
                    .translationY(-dpToPx(50)).setDuration(5000).start();
                break;
            case "slow_zoom_in":
                binding.backgroundImageView.animate()
                    .scaleX(1.18f).scaleY(1.18f).setDuration(4000).start();
                break;
            case "slow_zoom_out":
                binding.backgroundImageView.setScaleX(1.18f);
                binding.backgroundImageView.setScaleY(1.18f);
                binding.backgroundImageView.animate()
                    .scaleX(1.0f).scaleY(1.0f).setDuration(4000).start();
                break;
            case "panorama":
                binding.backgroundImageView.animate()
                    .translationX(dpToPx(55)).setDuration(4500).start();
                break;
            default:
                break;
        }
    }

    private float dpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    // ── Full-screen transitions ─────────────────────────────────────────

    /**
     * Fades the global overlay to black (or dark), changes scene, then fades back.
     * Fires startTextAnimation() at the right moment.
     *
     * @param type "fade_in" | "fade_out_in" | "darkening"
     */
    private void playTransitionThenShow(String type) {
        switch (type) {
            case "fade_out_in": {
                // Fade to black → 400 ms hold → fade back → show text
                binding.globalOverlay.animate().alpha(1f).setDuration(600)
                    .withEndAction(() -> textHandler.postDelayed(() -> {
                        binding.globalOverlay.animate().alpha(0.33f).setDuration(700)
                            .withEndAction(() -> {
                                Animation fi = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                                binding.textArea.startAnimation(fi);
                                startTextAnimation();
                            }).start();
                    }, 400)).start();
                break;
            }
            case "darkening": {
                // Quick darkening, then reveal
                binding.globalOverlay.animate().alpha(0.85f).setDuration(500)
                    .withEndAction(() ->
                        binding.globalOverlay.animate().alpha(0.33f).setDuration(600)
                            .withEndAction(() -> {
                                Animation fi = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                                binding.textArea.startAnimation(fi);
                                startTextAnimation();
                            }).start()
                    ).start();
                break;
            }
            default: { // "fade_in" and anything else
                Animation fi = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                binding.textArea.startAnimation(fi);
                startTextAnimation();
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Override back button to show pause menu instead of going back to map
        Intent intent = new Intent(DialogueActivity.this, PauseStatsActivity.class);
        startActivity(intent);
    }

    private void updateChapterTitle() {
        // Try to get title from database (from JSON file)
        com.example.sagaoftheaylopors.data.entities.Chapter chapter = storyRepository.getChapter(chapterNumber);
        String chapterTitle;
        
        if (chapter != null && chapter.titleKey != null && !chapter.titleKey.isEmpty()) {
            // Use title from JSON (stored in titleKey field)
            chapterTitle = chapter.titleKey;
        } else {
            // Fallback to string resources if chapter not loaded yet
            int chapterTitleRes = R.string.chapter_1_title; // Default to chapter 1
            switch (chapterNumber) {
                case 1:
                    chapterTitleRes = R.string.chapter_1_title;
                    break;
                case 2:
                    chapterTitleRes = R.string.chapter_2_title;
                    break;
                case 3:
                    chapterTitleRes = R.string.chapter_3_title;
                    break;
                case 4:
                    chapterTitleRes = R.string.chapter_4_title;
                    break;
                case 5:
                    chapterTitleRes = R.string.chapter_5_title;
                    break;
                case 6:
                    chapterTitleRes = R.string.chapter_6_title;
                    break;
                case 7:
                    chapterTitleRes = R.string.chapter_7_title;
                    break;
            }
            chapterTitle = getString(chapterTitleRes);
        }
        
        binding.chapterTitleTextView.setText(getString(R.string.dialogue_chapter_title, chapterNumber, chapterTitle));
    }

    private void loadCurrentDialogue() {
        if (playerProgress == null) {
            return;
        }

        // Load current dialogue
        currentDialogue = storyRepository.getDialogue(playerProgress.currentDialogueId);

        // Auto-skip companion-filtered dialogues that do not match the player's chosen companion
        currentDialogue = skipFilteredDialogues(currentDialogue);

        if (currentDialogue == null) {
            // Dialogue not found, try to find first dialogue in current scene
            List<Dialogue> dialogues = storyRepository.getDialoguesByScene(playerProgress.currentSceneId);
            if (!dialogues.isEmpty()) {
                currentDialogue = dialogues.get(0);
                playerProgress.currentDialogueId = currentDialogue.dialogueId;
                storyRepository.updateProgress(
                    playerProgress.currentChapterId,
                    playerProgress.currentSceneId,
                    playerProgress.currentDialogueId
                );
            } else {
                // No dialogues found, end chapter
                finish();
                return;
            }
        }

        // Display dialogue
        displayDialogue(currentDialogue);

        // DO NOT show choices immediately - they will be shown after text is complete and acknowledged
        // Reset state machine
        currentTextState = TextState.TEXT_RENDERING;
        hideChoices();
        hideChoiceIndicator();
    }

    /**
     * Splits text into pages at natural sentence boundaries so that each page
     * fits within the dialogue box without truncation.
     * Breaks at . ! ? » or \n; falls back to the last space if needed.
     */
    private void buildTextPages(String text) {
        textPages.clear();
        currentPageIndex = 0;
        if (text == null || text.isEmpty()) {
            textPages.add("");
            return;
        }
        if (text.length() <= PAGE_LIMIT) {
            textPages.add(text);
            return;
        }
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + PAGE_LIMIT, text.length());
            if (end < text.length()) {
                // Prefer a sentence-ending character in the second half of the page
                int splitAt = -1;
                for (int i = end; i > start + PAGE_LIMIT / 2; i--) {
                    char c = text.charAt(i - 1);
                    if (c == '.' || c == '!' || c == '?' || c == '»' || c == '\n') {
                        splitAt = i;
                        break;
                    }
                }
                if (splitAt > start) {
                    end = splitAt;
                } else {
                    // Fall back to last whitespace
                    for (int i = end; i > start; i--) {
                        if (Character.isWhitespace(text.charAt(i - 1))) {
                            end = i;
                            break;
                        }
                    }
                }
            }
            String page = text.substring(start, end).trim();
            if (!page.isEmpty()) textPages.add(page);
            start = end;
        }
        if (textPages.isEmpty()) textPages.add(text);
    }

    private void displayDialogue(Dialogue dialogue) {
        // Stop any existing animation
        stopTextAnimation();
        
        // Reset state machine
        currentTextState = TextState.TEXT_RENDERING;
        
        // Clear choices and reset UI
        currentChoices = null;
        hideChoices();
        hideChoiceIndicator();
        
        // Make sure text area is visible
        binding.textArea.setVisibility(View.VISIBLE);
        
        // Split long text into pages; short text becomes a single page.
        buildTextPages(dialogue.textKey);
        fullText = textPages.get(0);
        currentChunkStart = 0;
        currentCharIndex = 0;

        // Get speaker name
        String speakerName = getSpeakerName(dialogue.speakerType);
        binding.speakerNameTextView.setText(speakerName);

        // Update character portrait based on portrait_mode field or legacy speaker type
        updateCharacterPortrait(dialogue);
        
        // Update text area visual dominance based on speaker
        boolean hasSpeaker = dialogue.speakerType != null && 
            ("cat".equalsIgnoreCase(dialogue.speakerType) || "puss".equalsIgnoreCase(dialogue.speakerType));
        
        if (hasSpeaker) {
            // Normal opacity for dialogue with speaker
            binding.textArea.setAlpha(1.0f);
        } else {
            // Slightly reduced visual dominance for narration (subtle but readable)
            binding.textArea.setAlpha(0.92f);
        }
        
        // Update background (dialogue background > scene background)
        updateBackground();
        
        // ── Camera effect — starts playing right as the scene loads ────────
        if (dialogue.cameraEffect != null && !dialogue.cameraEffect.isEmpty()) {
            playCameraEffect(dialogue.cameraEffect);
        }

        // ── Transition / pause effect ────────────────────────────────────
        if (dialogue.transitionBefore != null && !dialogue.transitionBefore.isEmpty()) {
            playTransitionThenShow(dialogue.transitionBefore);
        } else if (dialogue.pauseBefore > 0f) {
            // Hide dialogue box, let the player see the background, then reveal.
            binding.textArea.setAlpha(0f);
            textHandler.postDelayed(() -> {
                binding.textArea.setAlpha(1f);
                Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                binding.textArea.startAnimation(fadeIn);
                startTextAnimation();
            }, (long)(dialogue.pauseBefore * 1000L));
        } else {
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            binding.textArea.startAnimation(fadeIn);
            startTextAnimation();
        }
    }

    private void startTextAnimation() {
        if (fullText == null || fullText.isEmpty()) {
            return;
        }
        
        // Reset to start of first chunk
        currentChunkStart = 0;
        currentCharIndex = 0;
        isAnimating = true;
        
        // Clear text view
        binding.dialogueTextView.setText("");
        
        // Start animating first chunk
        animateTextChunk();
    }

    private void animateTextChunk() {
        if (currentChunkStart >= fullText.length()) {
            isAnimating = false;
            // Text animation complete - check if all text is shown
            if (currentChunkStart >= fullText.length()) {
                onTextComplete();
            }
            return;
        }
        
        int chunkEnd = Math.min(currentChunkStart + CHUNK_SIZE, fullText.length());
        String chunk = fullText.substring(currentChunkStart, chunkEnd);
        
        currentCharIndex = 0;
        final String previousText = currentChunkStart > 0 ? fullText.substring(0, currentChunkStart) : "";
        textAnimationRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentCharIndex < chunk.length()) {
                    // Display previous chunks + current chunk progress
                    String displayedText = previousText + chunk.substring(0, currentCharIndex + 1);
                    binding.dialogueTextView.setText(displayedText);
                    currentCharIndex++;
                    textHandler.postDelayed(this, LETTER_DELAY);
                } else {
                    // Chunk complete
                    isAnimating = false;
                    textAnimationRunnable = null;
                    // Display all text up to end of this chunk
                    String displayedText = previousText + chunk;
                    binding.dialogueTextView.setText(displayedText);
                    // Check if all text is shown
                    if (currentChunkStart + CHUNK_SIZE >= fullText.length()) {
                        onTextComplete();
                    }
                }
            }
        };
        textHandler.post(textAnimationRunnable);
    }
    
    /**
     * Called when the current page's text animation is complete.
     * If more pages remain, shows the "▼" indicator and waits for a tap.
     * On the last page, proceeds with the normal choice / progress logic.
     */
    private void onTextComplete() {
        currentTextState = TextState.TEXT_COMPLETE;

        // More pages to show — prompt the player to tap to continue reading.
        if (currentPageIndex < textPages.size() - 1) {
            showChoiceIndicator(); // reuses the blinking "..." indicator
            return;
        }

        // Last page — normal logic: check for choices.
        if (currentDialogue != null && currentDialogue.hasChoices) {
            currentChoices = storyRepository.getChoicesByDialogue(currentDialogue.dialogueId);
            if (currentChoices != null && !currentChoices.isEmpty()) {
                showChoiceIndicator();
                currentTextState = TextState.CHOICE_PENDING;
            }
        }
    }
    
    /**
     * Shows the choice indicator (e.g., "...") when choices are pending.
     */
    private void showChoiceIndicator() {
        if (binding.choiceIndicatorTextView != null) {
            binding.choiceIndicatorTextView.setVisibility(View.VISIBLE);
            // Optional: Add blinking animation
            Animation blinkAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            blinkAnimation.setDuration(600);
            blinkAnimation.setRepeatMode(Animation.REVERSE);
            blinkAnimation.setRepeatCount(Animation.INFINITE);
            binding.choiceIndicatorTextView.startAnimation(blinkAnimation);
        }
    }
    
    /**
     * Hides the choice indicator.
     */
    private void hideChoiceIndicator() {
        if (binding.choiceIndicatorTextView != null) {
            binding.choiceIndicatorTextView.clearAnimation();
            binding.choiceIndicatorTextView.setVisibility(View.GONE);
        }
    }

    private void completeCurrentChunk() {
        if (textAnimationRunnable != null) {
            textHandler.removeCallbacks(textAnimationRunnable);
            textAnimationRunnable = null;
        }
        
        int chunkEnd = Math.min(currentChunkStart + CHUNK_SIZE, fullText.length());
        // Display all text from beginning up to end of current chunk
        String displayedText = fullText.substring(0, chunkEnd);
        binding.dialogueTextView.setText(displayedText);
        isAnimating = false;
        
        // Check if all text is shown after completing this chunk
        if (chunkEnd >= fullText.length()) {
            onTextComplete();
        }
    }

    private void showNextChunk() {
        currentChunkStart += CHUNK_SIZE;
        currentTextState = TextState.TEXT_RENDERING;
        if (currentChunkStart < fullText.length()) {
            // Show next chunk immediately (fast)
            int chunkEnd = Math.min(currentChunkStart + CHUNK_SIZE, fullText.length());
            String chunk = fullText.substring(currentChunkStart, chunkEnd);
            
            // Animate fast
            currentCharIndex = 0;
            isAnimating = true;
            final String previousText = currentChunkStart > 0 ? fullText.substring(0, currentChunkStart) : "";
            textAnimationRunnable = new Runnable() {
                @Override
                public void run() {
                    if (currentCharIndex < chunk.length()) {
                        // Display previous chunks + current chunk progress
                        String displayedText = previousText + chunk.substring(0, currentCharIndex + 1);
                        binding.dialogueTextView.setText(displayedText);
                        currentCharIndex++;
                        textHandler.postDelayed(this, FAST_LETTER_DELAY);
                    } else {
                        isAnimating = false;
                        textAnimationRunnable = null;
                        // Display all text up to end of this chunk
                        String displayedText = previousText + chunk;
                        binding.dialogueTextView.setText(displayedText);
                        // Check if all text is shown
                        if (currentChunkStart + CHUNK_SIZE >= fullText.length()) {
                            onTextComplete();
                        }
                    }
                }
            };
            textHandler.post(textAnimationRunnable);
        } else {
            // All chunks shown
            onTextComplete();
        }
    }

    private void stopTextAnimation() {
        if (textAnimationRunnable != null) {
            textHandler.removeCallbacks(textAnimationRunnable);
            textAnimationRunnable = null;
        }
        isAnimating = false;
    }

    private String getSpeakerName(String speakerType) {
        if (speakerType == null) {
            return getString(R.string.speaker_narrator);
        }
        
        String lowerSpeaker = speakerType.toLowerCase();
        
        // Always handle these special cases
        switch (lowerSpeaker) {
            case "narrator":
                return getString(R.string.speaker_narrator);
            case "cat":
            case "puss":
                return getString(R.string.speaker_puss);
            case "young_son":
            case "youngerson":
                return getString(R.string.speaker_young_son);
            case "guard":
                return "Guard"; // Display as-is for now
        }
        
        // Check if we're before chapter 4 (helpers haven't introduced themselves)
        // Before chapter 4: use JSON names (capitalize appropriately)
        // After chapter 4: use actual names from strings.xml
        boolean beforeChapter4 = chapterNumber < 4;
        
        // Map helper characters
        if (lowerSpeaker.equals("mentor")) {
            if (beforeChapter4) {
                return "Mentor";
            } else {
                return getString(R.string.speaker_mentor);
            }
        } else if (lowerSpeaker.equals("trickyhelper")) {
            if (beforeChapter4) {
                return "Tricky Helper";
            } else {
                return getString(R.string.speaker_tricky_helper);
            }
        } else if (lowerSpeaker.equals("weakbutknowing")) {
            if (beforeChapter4) {
                return "Weak But Knowing";
            } else {
                return getString(R.string.speaker_weak_but_knowing);
            }
        } else if (lowerSpeaker.equals("strongbutdependent")) {
            if (beforeChapter4) {
                return "Strong But Dependent";
            } else {
                return getString(R.string.speaker_strong_but_dependent);
            }
        }
        
        // Try to get speaker name from strings (fallback for other speakers)
        String speakerKey = "speaker_" + lowerSpeaker.replace(" ", "_");
        try {
            Resources res = getResources();
            int resId = res.getIdentifier(speakerKey, "string", getPackageName());
            if (resId != 0) {
                return getString(resId);
            }
        } catch (Exception e) {
            // Ignore
        }
        
        // Last resort: capitalize first letter and return
        if (lowerSpeaker.length() > 0) {
            return lowerSpeaker.substring(0, 1).toUpperCase() + 
                   (lowerSpeaker.length() > 1 ? lowerSpeaker.substring(1) : "");
        }
        
        return getString(R.string.speaker_narrator);
    }

    /**
     * Show character portraits for the given speaker.
     *
     * When ANY named character speaks, BOTH portraits are displayed so the
     * player always sees who is present in the scene:
     *   LEFT  (characterPortraitImageView) — miller's son / master (Ch1) or selected companion (Ch4+)
     *   RIGHT (characterPortraitRight)      — cat / Puss-in-Boots
     *
     * "narrator" alone → both hidden (pure description with no characters nearby).
     *
     * For dialogues with a "portrait_mode" field the explicit mode overrides legacy logic:
     *   "cat_only"       — only the Cat portrait (right)
     *   "companion_only" — only the companion portrait (left)
     *   "both"           — Cat (right) and companion (left) both visible
     *   "none"           — both hidden
     */
    private void updateCharacterPortrait(Dialogue dialogue) {
        if (dialogue == null) return;

        if (dialogue.portraitMode != null && !dialogue.portraitMode.isEmpty()) {
            applyPortraitMode(dialogue.portraitMode);
        } else {
            updateCharacterPortraitLegacy(dialogue.speakerType);
        }
    }

    /** Explicit portrait mode driven by the "portrait_mode" JSON field. */
    private void applyPortraitMode(String mode) {
        boolean showCat = false;
        boolean showCompanion = false;

        switch (mode.toLowerCase()) {
            case "cat_only":       showCat = true; break;
            case "companion_only": showCompanion = true; break;
            case "both":           showCat = true; showCompanion = true; break;
            case "none": default:  break;
        }

        // LEFT portrait — companion (Ch4+), identified by playerProgress.selectedCharacter
        if (showCompanion) {
            String companionId = (playerProgress != null) ? playerProgress.selectedCharacter : null;
            int resId = getCompanionPortraitResId(companionId);
            if (resId != 0) {
                binding.characterPortraitImageView.setImageResource(resId);
                binding.characterPortraitImageView.setVisibility(View.VISIBLE);
                applyTopCrop(binding.characterPortraitImageView);
            } else {
                binding.characterPortraitImageView.setVisibility(View.GONE);
            }
        } else {
            binding.characterPortraitImageView.setVisibility(View.GONE);
        }

        // RIGHT portrait — cat
        if (showCat) {
            binding.characterPortraitRight.setImageResource(R.drawable.cat_full_height);
            binding.characterPortraitRight.setVisibility(View.VISIBLE);
            applyTopCrop(binding.characterPortraitRight);
        } else {
            binding.characterPortraitRight.setVisibility(View.GONE);
        }
    }

    /**
     * Returns the drawable resource id for a companion portrait, or 0 if not found.
     * Drawable naming convention: companion_{id} (e.g. companion_alward).
     */
    private int getCompanionPortraitResId(String companionId) {
        if (companionId == null || companionId.isEmpty()) return 0;
        return getResources().getIdentifier(
            "companion_" + companionId.toLowerCase(),
            "drawable",
            getPackageName()
        );
    }

    /** Legacy portrait logic — inferred from speaker type (used when portrait_mode is null). */
    private void updateCharacterPortraitLegacy(String speakerType) {
        boolean isNamedCharacter = false;

        if (speakerType != null) {
            switch (speakerType.toLowerCase()) {
                case "cat":
                case "puss":
                case "puss_in_boots":
                case "master":
                case "miller":
                case "young_miller":
                case "youth":
                    isNamedCharacter = true;
                    break;
                default:
                    break;
            }
        }

        // LEFT portrait — miller's son / master (appears only in Chapter 1).
        boolean showMaster = isNamedCharacter && chapterNumber == 1;
        if (showMaster) {
            binding.characterPortraitImageView.setImageResource(R.drawable.master_full_height);
            binding.characterPortraitImageView.setVisibility(View.VISIBLE);
            applyTopCrop(binding.characterPortraitImageView);
        } else {
            binding.characterPortraitImageView.setVisibility(View.GONE);
        }

        // RIGHT portrait — cat (always shown when any named character speaks).
        if (isNamedCharacter) {
            binding.characterPortraitRight.setImageResource(R.drawable.cat_full_height);
            binding.characterPortraitRight.setVisibility(View.VISIBLE);
            applyTopCrop(binding.characterPortraitRight);
        } else {
            binding.characterPortraitRight.setVisibility(View.GONE);
        }
    }

    /**
     * Returns true if the given dialogue should be skipped because it is
     * restricted to a specific companion and the player chose a different one.
     */
    private boolean shouldSkipDialogue(Dialogue d) {
        if (d == null) return false;
        if (d.onlyForCompanion == null || d.onlyForCompanion.isEmpty()) return false;
        String selected = (playerProgress != null) ? playerProgress.selectedCharacter : null;
        if (selected == null || selected.isEmpty()) return true; // no companion chosen yet → skip companion lines
        return !d.onlyForCompanion.equalsIgnoreCase(selected);
    }

    /**
     * Walks forward through the dialogue chain, skipping any entries that are
     * filtered out by onlyForCompanion, until a displayable dialogue is found
     * or the chain ends.  Updates playerProgress.currentDialogueId accordingly.
     * A safety counter prevents infinite loops.
     */
    private Dialogue skipFilteredDialogues(Dialogue start) {
        Dialogue d = start;
        int safetyLimit = 60;
        while (d != null && shouldSkipDialogue(d) && safetyLimit-- > 0) {
            if (d.nextDialogueId > 0) {
                d = storyRepository.getDialogue(d.nextDialogueId);
            } else {
                d = null;
                break;
            }
        }
        if (d != null) {
            playerProgress.currentDialogueId = d.dialogueId;
        }
        return d;
    }

    /**
     * Scales the portrait so that exactly the TOP 50 % of the original image
     * fills the view height — head and torso visible, legs clipped.
     * Image is centered horizontally within the view.
     * Requires scaleType="matrix" on the ImageView.
     */
    private void applyTopCrop(android.widget.ImageView view) {
        view.post(() -> {
            if (view.getDrawable() == null) return;
            int vW = view.getWidth();
            int vH = view.getHeight();
            int iW = view.getDrawable().getIntrinsicWidth();
            int iH = view.getDrawable().getIntrinsicHeight();
            if (vW <= 0 || vH <= 0 || iW <= 0 || iH <= 0) return;

            // Scale so the top 50 % of the image exactly fills the view height.
            // If width-fill needs a larger scale, use that instead (stays readable).
            float scaleByWidth  = (float) vW / iW;
            float scaleFor50pct = (float) vH / (iH * 0.50f);
            float scale = Math.max(scaleByWidth, scaleFor50pct);

            // Center the (possibly wider) image horizontally.
            float xOffset = (vW - iW * scale) / 2f;

            Matrix m = new Matrix();
            m.setScale(scale, scale);
            m.postTranslate(xOffset, 0f);  // top Y = 0 → image starts from head
            view.setImageMatrix(m);
        });
    }

    /**
     * Update background image based on current dialogue or scene.
     * Priority: Dialogue background > Scene background > Default
     */
    private void updateBackground() {
        String backgroundName = null;
        String source = "none";
        
        // Try dialogue background first
        if (currentDialogue != null && currentDialogue.background != null && !currentDialogue.background.isEmpty()) {
            backgroundName = currentDialogue.background;
            source = "dialogue";
        } else {
            // Fall back to scene background
            com.example.sagaoftheaylopors.data.entities.Scene currentScene = 
                storyRepository.getScene(playerProgress.currentSceneId);
            if (currentScene != null && currentScene.background != null && !currentScene.background.isEmpty()) {
                backgroundName = currentScene.background;
                source = "scene";
            }
        }
        
        // Set background if found
        if (backgroundName != null) {
            try {
                int resId = getResources().getIdentifier(backgroundName, "drawable", getPackageName());
                if (resId != 0) {
                    binding.backgroundImageView.setImageResource(resId);
                    Log.d("DialogueActivity", "Background loaded successfully - Name: " + backgroundName 
                        + ", Source: " + source + ", Resource ID: " + resId);
                } else {
                    Log.w("DialogueActivity", "Background resource not found - Name: " + backgroundName 
                        + ", Source: " + source + " (Resource ID is 0)");
                }
            } catch (Exception e) {
                Log.e("DialogueActivity", "Error loading background - Name: " + backgroundName 
                    + ", Source: " + source + ", Error: " + e.getMessage());
            }
        } else {
            Log.d("DialogueActivity", "No background specified (dialogue or scene)");
        }
    }

    /**
     * Shows choices after text is complete and player has acknowledged.
     * Called explicitly when transitioning from CHOICE_PENDING to CHOICE_ACTIVE.
     */
    private void showChoices() {
        if (currentChoices == null || currentChoices.isEmpty()) {
            hideChoices();
            return;
        }

        // Hide choice indicator
        hideChoiceIndicator();
        
        // Hide text area to make room for choices
        binding.textArea.setVisibility(View.GONE);
        
        // Update state
        currentTextState = TextState.CHOICE_ACTIVE;
        
        // Show choices
        binding.choicesContainer.setVisibility(View.VISIBLE);
        
        // Animate choices container appearing
        Animation slideUpFadeIn = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in);
        binding.choicesContainer.startAnimation(slideUpFadeIn);
        
        // Clear existing choice buttons
        binding.choicesContainer.removeAllViews();

        // Create choice buttons
        for (Choice choice : currentChoices) {
            Button choiceButton = new Button(this);
            choiceButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            // Use textKey directly as it now contains the actual text from JSON
            choiceButton.setText(choice.textKey);
            choiceButton.setTextSize(16);
            choiceButton.setPadding(16, 16, 16, 16);
            choiceButton.setBackgroundResource(R.drawable.button_secondary);
            choiceButton.setTextColor(getColor(R.color.color_text_light));
            choiceButton.setMinHeight(56);
            
            // Set margin
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) choiceButton.getLayoutParams();
            params.setMargins(0, 0, 0, 16);
            choiceButton.setLayoutParams(params);

            // Set click listener
            choiceButton.setOnClickListener(v -> onChoiceSelected(choice));

            binding.choicesContainer.addView(choiceButton);
        }
    }
    
    /**
     * @deprecated Use showChoices() instead. Kept for backwards compatibility.
     */
    private void loadChoices() {
        // This method is no longer used for immediate choice display
        // Choices are now shown via showChoices() after text acknowledgment
    }

    private void hideChoices() {
        binding.choicesContainer.setVisibility(View.GONE);
        // Don't clear currentChoices here - they may be pending display
        // Only clear when starting a new dialogue
    }

    private void onChoiceSelected(Choice choice) {
        // Stop text animation
        stopTextAnimation();
        
        // Apply all 10 behavioral effect deltas accumulated from this choice.
        {
            PlayerProgress progress = storyRepository.getProgress();

            float prevSociality            = progress.sociality;
            float prevActivity             = progress.activity;
            float prevEmotionalSensitivity = progress.emotionalSensitivity;
            float prevAnxiety              = progress.anxiety;
            float prevSelfControl          = progress.selfControl;
            float prevImpulsivity          = progress.impulsivity;
            float prevEgoFocus             = progress.egoFocus;
            float prevRigidity             = progress.rigidity;
            float prevNegativeAffect       = progress.negativeAffect;
            float prevAdaptability         = progress.adaptability;

            progress.applyEffects(
                    choice.effectSociality,
                    choice.effectActivity,
                    choice.effectEmotionalSensitivity,
                    choice.effectAnxiety,
                    choice.effectSelfControl,
                    choice.effectImpulsivity,
                    choice.effectEgoFocus,
                    choice.effectRigidity,
                    choice.effectNegativeAffect,
                    choice.effectAdaptability
            );

            storyRepository.updateBehavioralParams(progress);

            // Persist the chosen companion so future chapters can reference it
            if (choice.saveCompanion != null && !choice.saveCompanion.isEmpty()) {
                playerProgress.selectedCharacter = choice.saveCompanion;
                storyRepository.saveProgress(playerProgress);
                Log.d("DialogueActivity", "Companion saved: " + choice.saveCompanion);
            }

            Log.d("DialogueActivity", String.format(
                "Behavioral params after choice [choiceId=%d]: " +
                "soc %.3f→%.3f | act %.3f→%.3f | emp %.3f→%.3f | " +
                "anx %.3f→%.3f | ctrl %.3f→%.3f | imp %.3f→%.3f | " +
                "ego %.3f→%.3f | rig %.3f→%.3f | neg %.3f→%.3f | adp %.3f→%.3f",
                choice.choiceId,
                prevSociality,            progress.sociality,
                prevActivity,             progress.activity,
                prevEmotionalSensitivity, progress.emotionalSensitivity,
                prevAnxiety,              progress.anxiety,
                prevSelfControl,          progress.selfControl,
                prevImpulsivity,          progress.impulsivity,
                prevEgoFocus,             progress.egoFocus,
                prevRigidity,             progress.rigidity,
                prevNegativeAffect,       progress.negativeAffect,
                prevAdaptability,         progress.adaptability
            ));
        }

        // Update background if choice has one (choice background has highest priority)
        if (choice.background != null && !choice.background.isEmpty()) {
            try {
                int resId = getResources().getIdentifier(choice.background, "drawable", getPackageName());
                if (resId != 0) {
                    binding.backgroundImageView.setImageResource(resId);
                    Log.d("DialogueActivity", "Choice background loaded successfully - Name: " + choice.background 
                        + ", Resource ID: " + resId);
                } else {
                    Log.w("DialogueActivity", "Choice background resource not found - Name: " + choice.background 
                        + " (Resource ID is 0)");
                }
            } catch (Exception e) {
                Log.e("DialogueActivity", "Error loading choice background - Name: " + choice.background 
                    + ", Error: " + e.getMessage());
            }
        }

        // Hide choices
        hideChoices();
        
        // Reset state machine
        currentTextState = TextState.TEXT_RENDERING;

        // Determine next dialogue/scene
        if (choice.nextSceneId > 0) {
            // Jump to different scene
            int currentSceneId = playerProgress.currentSceneId;
            int nextSceneId = choice.nextSceneId;
            
            // CRITICAL FIX: Mark current scene as completed before jumping to next scene
            // This ensures all scenes are marked complete as player progresses
            if (currentSceneId != nextSceneId) {
                Log.d("DialogueActivity", "Choice jumps from scene " + currentSceneId + " to scene " + nextSceneId + " - marking current scene as completed");
                storyRepository.markSceneCompleted(currentSceneId);
            }
            
            playerProgress.currentSceneId = nextSceneId;
            playerProgress.currentDialogueId = choice.nextDialogueId > 0 ? choice.nextDialogueId : 1;
        } else if (choice.nextDialogueId > 0) {
            // Jump to different dialogue in same scene
            playerProgress.currentDialogueId = choice.nextDialogueId;
        } else {
            // Default: progress to next dialogue
            progressToNextDialogue();
            return;
        }

        // Update progress
        storyRepository.updateProgress(
            playerProgress.currentChapterId,
            playerProgress.currentSceneId,
            playerProgress.currentDialogueId
        );

        // Reload dialogue
        loadCurrentDialogue();
    }

    private void progressToNextDialogue() {
        if (currentDialogue == null) {
            return;
        }

        // Check if there's a next dialogue
        if (currentDialogue.nextDialogueId > 0) {
            playerProgress.currentDialogueId = currentDialogue.nextDialogueId;
            storyRepository.updateProgress(
                playerProgress.currentChapterId,
                playerProgress.currentSceneId,
                playerProgress.currentDialogueId
            );
            loadCurrentDialogue();
        } else {
            // No next dialogue, check for next scene
            com.example.sagaoftheaylopors.data.entities.Scene currentScene = 
                storyRepository.getScene(playerProgress.currentSceneId);
            
            // CRITICAL FIX: Mark current scene as completed before moving to next scene
            // This ensures all scenes are marked complete as player progresses
            int currentSceneId = playerProgress.currentSceneId;
            Log.d("DialogueActivity", "No next dialogue in scene " + currentSceneId + " - marking scene as completed");
            storyRepository.markSceneCompleted(currentSceneId);
            
            if (currentScene != null && currentScene.nextSceneId > 0) {
                // Move to next scene
                int nextSceneId = currentScene.nextSceneId;
                Log.d("DialogueActivity", "Moving to next scene: " + nextSceneId);
                
                playerProgress.currentSceneId = nextSceneId;
                List<Dialogue> nextSceneDialogues = storyRepository.getDialoguesByScene(nextSceneId);
                if (!nextSceneDialogues.isEmpty()) {
                    playerProgress.currentDialogueId = nextSceneDialogues.get(0).dialogueId;
                    storyRepository.updateProgress(
                        playerProgress.currentChapterId,
                        playerProgress.currentSceneId,
                        playerProgress.currentDialogueId
                    );
                    Log.d("DialogueActivity", "Loaded first dialogue of next scene: " + playerProgress.currentDialogueId);
                    loadCurrentDialogue();
                } else {
                    // No dialogues in next scene, mark next scene as complete too
                    Log.w("DialogueActivity", "Next scene " + nextSceneId + " has no dialogues - marking as complete");
                    storyRepository.markSceneCompleted(nextSceneId);
                    checkChapterCompletion();
                }
            } else {
                // End of scene and no next scene - this is the last scene
                Log.d("DialogueActivity", "End of scene " + currentSceneId + " - no next scene, checking chapter completion");
                checkChapterCompletion();
            }
        }
    }

    private void checkChapterCompletion() {
        Log.d("DialogueActivity", "=== CHECK CHAPTER COMPLETION ===");
        Log.d("DialogueActivity", "Current chapter ID: " + playerProgress.currentChapterId);
        Log.d("DialogueActivity", "Current scene ID: " + playerProgress.currentSceneId);
        Log.d("DialogueActivity", "Current dialogue ID: " + playerProgress.currentDialogueId);
        
        // Check if all scenes in chapter are complete
        boolean isComplete = storyRepository.isChapterComplete(playerProgress.currentChapterId);
        Log.d("DialogueActivity", "Chapter " + playerProgress.currentChapterId + " complete status: " + isComplete);
        
        if (isComplete) {
            Log.d("DialogueActivity", "✓ Chapter " + playerProgress.currentChapterId + " is complete!");
            Log.d("DialogueActivity", "Marking chapter " + playerProgress.currentChapterId + " as completed...");
            
            // Mark chapter as complete (this also unlocks next chapter)
            storyRepository.markChapterCompleted(playerProgress.currentChapterId);
            
            // Verify chapter was marked complete
            com.example.sagaoftheaylopors.data.entities.Chapter completedChapter = 
                storyRepository.getChapter(playerProgress.currentChapterId);
            if (completedChapter != null && completedChapter.isCompleted) {
                Log.d("DialogueActivity", "✓ Chapter " + playerProgress.currentChapterId + " verified as completed in database");
            } else {
                Log.e("DialogueActivity", "✗ ERROR: Chapter " + playerProgress.currentChapterId + " NOT verified as completed!");
            }
            
            // Determine next chapter
            int nextChapterId = playerProgress.currentChapterId + 1;
            if (nextChapterId > 7) {
                Log.d("DialogueActivity", "Chapter " + playerProgress.currentChapterId + " is the last chapter");
            } else {
                Log.d("DialogueActivity", "Next chapter should be: " + nextChapterId);
                
                // Verify next chapter is unlocked
                com.example.sagaoftheaylopors.data.entities.Chapter nextChapter = 
                    storyRepository.getChapter(nextChapterId);
                if (nextChapter != null) {
                    Log.d("DialogueActivity", "Next chapter " + nextChapterId + " exists in database");
                    Log.d("DialogueActivity", "Next chapter unlocked: " + nextChapter.isUnlocked);
                } else {
                    Log.e("DialogueActivity", "✗ ERROR: Next chapter " + nextChapterId + " does NOT exist in database!");
                }
            }
            
            Log.d("DialogueActivity", "Returning to MapActivity with chapter completion info");
            
            // Check if all chapters are completed (7 chapters total)
            if (playerProgress.currentChapterId >= 7) {
                // All chapters completed - show final screen
                Log.d("DialogueActivity", "All chapters completed - showing final screen");
                Intent intent = new Intent(DialogueActivity.this, FinalScreenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                // Go back to map with chapter completion info
                Intent intent = new Intent(DialogueActivity.this, MapActivity.class);
                intent.putExtra("completed_chapter", playerProgress.currentChapterId);
                intent.putExtra("show_path_animation", true);
                startActivity(intent);
                finish();
            }
        } else {
            Log.d("DialogueActivity", "Chapter " + playerProgress.currentChapterId + " not yet complete - more scenes remain");
            Log.d("DialogueActivity", "Returning to MapActivity (chapter not complete)");
            
            // Chapter not complete, but scene is - go back to map
            finish();
        }
        
        Log.d("DialogueActivity", "=== END CHECK CHAPTER COMPLETION ===");
    }
}
