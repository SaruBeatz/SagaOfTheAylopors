package com.example.sagaoftheaylopors;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Manages background music playback across activities.
 * Music files are added to res/raw/ when ready.
 * Until then all play/stop methods are safe no-ops.
 */
public class MusicManager {
    private static final String TAG = "MusicManager";
    private static MusicManager instance;
    private MediaPlayer currentPlayer;
    private float musicVolume = 0.7f;

    // Add resource IDs here when music files are placed in res/raw/:
    // e.g. R.raw.music_ch1_mystery, R.raw.music_main_theme, etc.
    private static final int[] MUSIC_TRACKS = {};   // empty = no music yet
    private static final int PAUSE_MUSIC_ID  = -1;  // -1 = no file yet

    private MusicManager() {}

    public static synchronized MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    public void playRandomMusic(Context context) {
        if (MUSIC_TRACKS.length == 0) return;
        stopMusic();
        int idx = (int) (Math.random() * MUSIC_TRACKS.length);
        playMusic(context, MUSIC_TRACKS[idx], true);
    }

    public void playPauseMusic(Context context) {
        if (PAUSE_MUSIC_ID == -1) return;
        stopMusic();
        playMusic(context, PAUSE_MUSIC_ID, true);
    }

    private void playMusic(Context context, int resId, boolean loop) {
        if (resId <= 0) return;
        try {
            if (currentPlayer != null) currentPlayer.release();
            currentPlayer = MediaPlayer.create(context, resId);
            if (currentPlayer != null) {
                currentPlayer.setLooping(loop);
                currentPlayer.setVolume(musicVolume, musicVolume);
                currentPlayer.start();
                Log.d(TAG, "Playing track resId=" + resId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing music", e);
        }
    }

    public void stopMusic() {
        if (currentPlayer != null) {
            try {
                if (currentPlayer.isPlaying()) currentPlayer.stop();
                currentPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping music", e);
            }
            currentPlayer = null;
        }
    }

    public void pauseMusic() {
        if (currentPlayer != null && currentPlayer.isPlaying()) {
            currentPlayer.pause();
        }
    }

    public void resumeMusic() {
        if (currentPlayer != null && !currentPlayer.isPlaying()) {
            try { currentPlayer.start(); } catch (Exception e) {
                Log.e(TAG, "Error resuming music", e);
            }
        }
    }

    public void setMusicVolume(float volume) {
        musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        if (currentPlayer != null) currentPlayer.setVolume(musicVolume, musicVolume);
    }

    public float getMusicVolume() { return musicVolume; }

    public boolean isPlaying() {
        return currentPlayer != null && currentPlayer.isPlaying();
    }
}
