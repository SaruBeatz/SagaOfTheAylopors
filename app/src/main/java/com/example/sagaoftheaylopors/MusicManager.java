package com.example.sagaoftheaylopors;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.util.Random;

/**
 * Manages background music playback across activities.
 * Temporary implementation - future versions will have context-aware music.
 */
public class MusicManager {
    private static final String TAG = "MusicManager";
    private static MusicManager instance;
    private MediaPlayer currentPlayer;
    private float musicVolume = 0.7f; // Default 70%
    private int currentTrack = -1;
    
    // Music track resource IDs
    private static final int[] MUSIC_TRACKS = {
        R.raw.chillmusic,
        R.raw.background_nature_music,
        R.raw.pause_music
    };
    
    private static final int PAUSE_MUSIC = R.raw.pause_music;
    
    private MusicManager() {
    }
    
    public static synchronized MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }
    
    /**
     * Play a random music track (for Map/Dialogue screens).
     */
    public void playRandomMusic(Context context) {
        stopMusic();
        
        Random random = new Random();
        int trackIndex = random.nextInt(MUSIC_TRACKS.length);
        int trackResId = MUSIC_TRACKS[trackIndex];
        
        playMusic(context, trackResId, true);
    }
    
    /**
     * Play pause music (looped).
     */
    public void playPauseMusic(Context context) {
        stopMusic();
        playMusic(context, PAUSE_MUSIC, true);
    }
    
    /**
     * Play specific music track.
     * @param context Application context
     * @param trackResId Resource ID of the music track
     * @param loop Whether to loop the music
     */
    private void playMusic(Context context, int trackResId, boolean loop) {
        try {
            if (currentPlayer != null) {
                currentPlayer.release();
            }
            
            currentPlayer = MediaPlayer.create(context, trackResId);
            if (currentPlayer != null) {
                currentPlayer.setLooping(loop);
                currentPlayer.setVolume(musicVolume, musicVolume);
                currentPlayer.start();
                currentTrack = trackResId;
                Log.d(TAG, "Playing music track: " + trackResId + ", looping: " + loop);
            } else {
                Log.e(TAG, "Failed to create MediaPlayer for track: " + trackResId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing music", e);
        }
    }
    
    /**
     * Stop current music playback.
     */
    public void stopMusic() {
        if (currentPlayer != null) {
            try {
                if (currentPlayer.isPlaying()) {
                    currentPlayer.stop();
                }
                currentPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping music", e);
            }
            currentPlayer = null;
            currentTrack = -1;
        }
    }
    
    /**
     * Pause current music (can be resumed).
     */
    public void pauseMusic() {
        if (currentPlayer != null && currentPlayer.isPlaying()) {
            currentPlayer.pause();
        }
    }
    
    /**
     * Resume paused music.
     */
    public void resumeMusic() {
        if (currentPlayer != null && !currentPlayer.isPlaying()) {
            try {
                currentPlayer.start();
            } catch (Exception e) {
                Log.e(TAG, "Error resuming music", e);
            }
        }
    }
    
    /**
     * Set music volume (0.0 to 1.0).
     */
    public void setMusicVolume(float volume) {
        musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        if (currentPlayer != null) {
            currentPlayer.setVolume(musicVolume, musicVolume);
        }
    }
    
    /**
     * Get current music volume.
     */
    public float getMusicVolume() {
        return musicVolume;
    }
    
    /**
     * Check if music is currently playing.
     */
    public boolean isPlaying() {
        return currentPlayer != null && currentPlayer.isPlaying();
    }
}
