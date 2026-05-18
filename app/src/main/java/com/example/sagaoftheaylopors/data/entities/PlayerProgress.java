package com.example.sagaoftheaylopors.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Tracks player's current progress in the story.
 * Only one record should exist (singleton pattern).
 */
@Entity(tableName = "player_progress")
public class PlayerProgress {
    @PrimaryKey
    public int id = 1; // Always 1, singleton
    
    /**
     * Current chapter ID the player is in
     */
    public int currentChapterId;
    
    /**
     * Current scene ID within the current chapter
     */
    public int currentSceneId;
    
    /**
     * Current dialogue ID within the current scene
     */
    public int currentDialogueId;
    
    /**
     * Player's cunning stat (affected by choices)
     */
    public int cunning;
    
    /**
     * Player's bravery stat (affected by choices)
     */
    public int bravery;
    
    /**
     * Player's creativity stat (affected by choices)
     */
    public int creativity;
    
    /**
     * Selected character (e.g., "cat", "marquis")
     */
    public String selectedCharacter;
    
    /**
     * Timestamp of last save
     */
    public long lastSaveTimestamp;
    
    public PlayerProgress() {
        this.id = 1;
        this.currentChapterId = 1;
        this.currentSceneId = 1;
        this.currentDialogueId = 1;
        this.cunning = 0;
        this.bravery = 0;
        this.creativity = 0;
        this.lastSaveTimestamp = System.currentTimeMillis();
    }
    
    public PlayerProgress(int currentChapterId, int currentSceneId, int currentDialogueId,
                          int cunning, int bravery, int creativity, String selectedCharacter) {
        this.id = 1;
        this.currentChapterId = currentChapterId;
        this.currentSceneId = currentSceneId;
        this.currentDialogueId = currentDialogueId;
        this.cunning = cunning;
        this.bravery = bravery;
        this.creativity = creativity;
        this.selectedCharacter = selectedCharacter;
        this.lastSaveTimestamp = System.currentTimeMillis();
    }
}
