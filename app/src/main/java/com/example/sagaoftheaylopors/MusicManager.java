package com.example.sagaoftheaylopors;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Centralized BGM: dual gameplay layers (nature + chill), pause track, menu chill.
 * User volume is a multiplier on MediaPlayer only — never system STREAM_MUSIC.
 */
public class MusicManager {
    private static final String TAG = "MusicManager";
    private static final String PREFS = "SagaMusic";
    private static final String KEY_USER_VOLUME = "user_volume";
    private static final String KEY_NATURE_POS = "nature_position_ms";
    private static final String KEY_CHILL_POS = "chill_position_ms";
    private static final String KEY_LAST_CHAPTER = "last_chapter_id";

    /** Short chapter transition — must not block gameplay UI. */
    private static final long CHAPTER_FADE_MS = 200L;
    private static final long FADE_STEP_MS = 20L;
    private static final float NATURE_BASE = 0.42f;
    private static final float CHILL_BASE = 0.55f;
    private static final float PAUSE_BASE = 0.75f;
    private static final float MENU_BASE = 0.55f;
    private static final float INTER_CHAPTER_DUCK = 0.45f;

    public enum Mode { NONE, MENU, GAMEPLAY, PAUSE }

    private static MusicManager instance;

    private Context appContext;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private MediaPlayer naturePlayer;
    private MediaPlayer chillPlayer;
    private MediaPlayer pausePlayer;

    private Mode currentMode = Mode.NONE;
    private float userVolumeMultiplier = 0.7f;
    private float fadeMultiplier = 1f;
    private float duckMultiplier = 1f;
    private boolean interChapterDuckActive;

    private Runnable fadeRunnable;
    private int lastChapterId = -1;

    private MusicManager() {}

    public static synchronized MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        if (appContext == null) {
            appContext = context.getApplicationContext();
            loadPreferences();
        }
    }

    private SharedPreferences prefs() {
        return appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void loadPreferences() {
        if (appContext == null) return;
        userVolumeMultiplier = prefs().getInt(KEY_USER_VOLUME, 70) / 100f;
        lastChapterId = prefs().getInt(KEY_LAST_CHAPTER, -1);
    }

    public void saveUserVolumePercent(int percent) {
        userVolumeMultiplier = Math.max(0f, Math.min(1f, percent / 100f));
        if (appContext != null) {
            prefs().edit().putInt(KEY_USER_VOLUME, percent).apply();
        }
        applyVolumes();
    }

    public float getUserVolumeMultiplier() {
        return userVolumeMultiplier;
    }

    public int getUserVolumePercent() {
        return Math.round(userVolumeMultiplier * 100f);
    }

    /** @deprecated use setUserVolumePercent */
    public void setMusicVolume(float volume) {
        saveUserVolumePercent(Math.round(volume * 100f));
    }

    public float getMusicVolume() {
        return userVolumeMultiplier;
    }

    public int getLastChapterId() {
        return lastChapterId;
    }

    private void savePlaybackPositions() {
        if (appContext == null) return;
        SharedPreferences.Editor e = prefs().edit();
        if (naturePlayer != null) {
            try {
                e.putInt(KEY_NATURE_POS, naturePlayer.getCurrentPosition());
            } catch (Exception ignored) { }
        }
        if (chillPlayer != null) {
            try {
                e.putInt(KEY_CHILL_POS, chillPlayer.getCurrentPosition());
            } catch (Exception ignored) { }
        }
        e.apply();
    }

    private void restorePlaybackPositions() {
        if (appContext == null) return;
        int naturePos = prefs().getInt(KEY_NATURE_POS, 0);
        int chillPos = prefs().getInt(KEY_CHILL_POS, 0);
        if (naturePlayer != null) {
            try {
                naturePlayer.seekTo(naturePos);
            } catch (Exception e) {
                Log.w(TAG, "seek nature", e);
            }
        }
        if (chillPlayer != null) {
            try {
                chillPlayer.seekTo(chillPos);
            } catch (Exception e) {
                Log.w(TAG, "seek chill", e);
            }
        }
    }

    private float effectiveGameplayVolume(float layerBase) {
        return layerBase * userVolumeMultiplier * fadeMultiplier * duckMultiplier;
    }

    private float effectiveSingleVolume(float layerBase) {
        return layerBase * userVolumeMultiplier * fadeMultiplier;
    }

    private void applyVolumes() {
        if (naturePlayer != null) {
            float v = effectiveGameplayVolume(NATURE_BASE);
            naturePlayer.setVolume(v, v);
        }
        if (chillPlayer != null) {
            float v = effectiveGameplayVolume(CHILL_BASE);
            chillPlayer.setVolume(v, v);
        }
        if (pausePlayer != null) {
            float v = effectiveSingleVolume(PAUSE_BASE);
            pausePlayer.setVolume(v, v);
        }
    }

    private void cancelFade() {
        if (fadeRunnable != null) {
            handler.removeCallbacks(fadeRunnable);
            fadeRunnable = null;
        }
    }

    private void animateFade(float from, float to, long durationMs, Runnable onEnd) {
        cancelFade();
        final long start = System.currentTimeMillis();
        fadeRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - start;
                float t = durationMs <= 0 ? 1f : Math.min(1f, elapsed / (float) durationMs);
                fadeMultiplier = from + (to - from) * t;
                applyVolumes();
                if (t < 1f) {
                    handler.postDelayed(this, FADE_STEP_MS);
                } else {
                    fadeRunnable = null;
                    if (onEnd != null) onEnd.run();
                }
            }
        };
        handler.post(fadeRunnable);
    }

    private MediaPlayer createLoopPlayer(int resId) {
        if (appContext == null || resId <= 0) return null;
        try {
            MediaPlayer mp = MediaPlayer.create(appContext, resId);
            if (mp != null) {
                mp.setLooping(true);
            }
            return mp;
        } catch (Exception e) {
            Log.e(TAG, "create player res=" + resId, e);
            return null;
        }
    }

    private void releasePausePlayer() {
        if (pausePlayer != null) {
            try {
                if (pausePlayer.isPlaying()) pausePlayer.stop();
                pausePlayer.release();
            } catch (Exception e) {
                Log.w(TAG, "release pause", e);
            }
            pausePlayer = null;
        }
    }

    private void releaseGameplayPlayers() {
        savePlaybackPositions();
        if (naturePlayer != null) {
            try {
                if (naturePlayer.isPlaying()) naturePlayer.stop();
                naturePlayer.release();
            } catch (Exception e) {
                Log.w(TAG, "release nature", e);
            }
            naturePlayer = null;
        }
        if (chillPlayer != null) {
            try {
                if (chillPlayer.isPlaying()) chillPlayer.stop();
                chillPlayer.release();
            } catch (Exception e) {
                Log.w(TAG, "release chill", e);
            }
            chillPlayer = null;
        }
    }

    private void ensureGameplayPlayers() {
        if (naturePlayer == null) {
            naturePlayer = createLoopPlayer(R.raw.background_nature_music);
        }
        if (chillPlayer == null) {
            chillPlayer = createLoopPlayer(R.raw.chillmusic);
        }
        restorePlaybackPositions();
        applyVolumes();
    }

    private void startGameplayPlayersIfNeeded() {
        ensureGameplayPlayers();
        try {
            if (naturePlayer != null && !naturePlayer.isPlaying()) {
                naturePlayer.start();
            }
            if (chillPlayer != null && !chillPlayer.isPlaying()) {
                chillPlayer.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "start gameplay", e);
        }
    }

    public void enterMainMenu(Context context) {
        initialize(context);
        cancelFade();
        interChapterDuckActive = false;
        duckMultiplier = 1f;
        fadeMultiplier = 1f;
        releasePausePlayer();
        releaseGameplayPlayers();

        pausePlayer = createLoopPlayer(R.raw.chillmusic);
        if (pausePlayer != null) {
            float v = effectiveSingleVolume(MENU_BASE);
            pausePlayer.setVolume(v, v);
            pausePlayer.start();
        }
        currentMode = Mode.MENU;
        Log.d(TAG, "enterMainMenu");
    }

    public void enterGameplay(Context context) {
        initialize(context);
        cancelFade();
        releasePausePlayer();
        currentMode = Mode.GAMEPLAY;
        if (interChapterDuckActive) {
            duckMultiplier = INTER_CHAPTER_DUCK;
            fadeMultiplier = INTER_CHAPTER_DUCK;
        } else {
            duckMultiplier = 1f;
            fadeMultiplier = 1f;
        }
        startGameplayPlayersIfNeeded();
        Log.d(TAG, "enterGameplay duck=" + interChapterDuckActive);
    }

    /** Map screen after completing a chapter — softer until next chapter starts. */
    public void enterInterChapterDuck(Context context) {
        initialize(context);
        interChapterDuckActive = true;
        enterGameplay(context);
        duckMultiplier = INTER_CHAPTER_DUCK;
        fadeMultiplier = INTER_CHAPTER_DUCK;
        applyVolumes();
        Log.d(TAG, "enterInterChapterDuck");
    }

    public void enterPause(Context context) {
        initialize(context);
        cancelFade();
        savePlaybackPositions();
        interChapterDuckActive = false;
        duckMultiplier = 1f;
        fadeMultiplier = 1f;

        if (naturePlayer != null) {
            try {
                if (naturePlayer.isPlaying()) naturePlayer.pause();
            } catch (Exception ignored) { }
        }
        if (chillPlayer != null) {
            try {
                if (chillPlayer.isPlaying()) chillPlayer.pause();
            } catch (Exception ignored) { }
        }

        releasePausePlayer();
        pausePlayer = createLoopPlayer(R.raw.pause_music);
        if (pausePlayer != null) {
            applyVolumes();
            pausePlayer.start();
        }
        currentMode = Mode.PAUSE;
        Log.d(TAG, "enterPause");
    }

    public void leavePauseResumeGameplay(Context context) {
        initialize(context);
        releasePausePlayer();
        currentMode = Mode.GAMEPLAY;
        fadeMultiplier = interChapterDuckActive ? INTER_CHAPTER_DUCK : 1f;
        duckMultiplier = fadeMultiplier;
        startGameplayPlayersIfNeeded();
        Log.d(TAG, "leavePauseResumeGameplay");
    }

    /** Quick fade-in when entering a chapter (non-blocking). */
    public void fadeInChapterStart(int chapterId) {
        if (appContext == null) return;
        lastChapterId = chapterId;
        prefs().edit().putInt(KEY_LAST_CHAPTER, chapterId).apply();
        interChapterDuckActive = false;
        float from = fadeMultiplier;
        if (from > INTER_CHAPTER_DUCK + 0.05f) {
            from = INTER_CHAPTER_DUCK;
        }
        duckMultiplier = 1f;
        fadeMultiplier = from;
        applyVolumes();
        animateFade(from, 1f, CHAPTER_FADE_MS, null);
        Log.d(TAG, "fadeInChapterStart ch=" + chapterId);
    }

    /**
     * Duck BGM after chapter complete — runs in background, never blocks navigation.
     */
    public void onChapterCompletedBackground() {
        interChapterDuckActive = true;
        savePlaybackPositions();
        float target = INTER_CHAPTER_DUCK;
        animateFade(fadeMultiplier, target, CHAPTER_FADE_MS, null);
        Log.d(TAG, "onChapterCompletedBackground");
    }

    /** @deprecated Navigation must not wait on this; use {@link #onChapterCompletedBackground()}. */
    public void fadeOutChapterEnd(Runnable onFaded) {
        onChapterCompletedBackground();
        if (onFaded != null) {
            handler.post(onFaded);
        }
    }

    public void pauseForAppBackground() {
        savePlaybackPositions();
        if (currentMode == Mode.GAMEPLAY) {
            try {
                if (naturePlayer != null && naturePlayer.isPlaying()) naturePlayer.pause();
                if (chillPlayer != null && chillPlayer.isPlaying()) chillPlayer.pause();
            } catch (Exception e) {
                Log.w(TAG, "pause gameplay", e);
            }
        } else if (currentMode == Mode.PAUSE || currentMode == Mode.MENU) {
            try {
                if (pausePlayer != null && pausePlayer.isPlaying()) pausePlayer.pause();
            } catch (Exception e) {
                Log.w(TAG, "pause single", e);
            }
        }
    }

    public void resumeFromAppBackground() {
        if (currentMode == Mode.GAMEPLAY) {
            startGameplayPlayersIfNeeded();
        } else if ((currentMode == Mode.PAUSE || currentMode == Mode.MENU) && pausePlayer != null) {
            try {
                if (!pausePlayer.isPlaying()) pausePlayer.start();
            } catch (Exception e) {
                Log.w(TAG, "resume single", e);
            }
        }
        applyVolumes();
    }

    /** Legacy API — routes to mode-aware pause. */
    public void pauseMusic() {
        pauseForAppBackground();
    }

    /** Legacy API — routes to mode-aware resume. */
    public void resumeMusic() {
        resumeFromAppBackground();
    }

    /** Full stop — main menu transition or app exit. */
    public void stopAll() {
        cancelFade();
        interChapterDuckActive = false;
        fadeMultiplier = 1f;
        duckMultiplier = 1f;
        releasePausePlayer();
        releaseGameplayPlayers();
        currentMode = Mode.NONE;
        Log.d(TAG, "stopAll");
    }

    /** @deprecated use enterGameplay / enterMainMenu */
    public void playRandomMusic(Context context) {
        enterGameplay(context);
    }

    /** @deprecated use enterPause */
    public void playPauseMusic(Context context) {
        enterPause(context);
    }

    /** @deprecated use stopAll */
    public void stopMusic() {
        stopAll();
    }

    public boolean isPlaying() {
        if (currentMode == Mode.GAMEPLAY) {
            return (naturePlayer != null && naturePlayer.isPlaying())
                    || (chillPlayer != null && chillPlayer.isPlaying());
        }
        return pausePlayer != null && pausePlayer.isPlaying();
    }

    public Mode getCurrentMode() {
        return currentMode;
    }
}
