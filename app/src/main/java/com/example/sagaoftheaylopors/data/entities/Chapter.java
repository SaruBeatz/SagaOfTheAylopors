package com.example.sagaoftheaylopors.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a story chapter.
 * Each chapter contains multiple scenes that must be completed in order.
 */
@Entity(tableName = "chapters")
public class Chapter {
    @PrimaryKey
    public int chapterId;
    
    /**
     * Localization key for chapter title (e.g., "chapter_1_title")
     */
    public String titleKey;
    
    /**
     * Localization key for chapter description/subtitle
     */
    public String descriptionKey;
    
    /**
     * Order of chapter in the story (1, 2, 3, etc.)
     */
    public int order;
    
    /**
     * Whether this chapter is unlocked (can be accessed)
     */
    public boolean isUnlocked;
    
    /**
     * Whether this chapter has been completed
     */
    public boolean isCompleted;
    
    /**
     * Next chapter ID (for automatic progression)
     * -1 if this is the last chapter
     */
    public int nextChapterId;
    
    public Chapter() {}
    
    public Chapter(int chapterId, String titleKey, String descriptionKey, int order, 
                   boolean isUnlocked, boolean isCompleted, int nextChapterId) {
        this.chapterId = chapterId;
        this.titleKey = titleKey;
        this.descriptionKey = descriptionKey;
        this.order = order;
        this.isUnlocked = isUnlocked;
        this.isCompleted = isCompleted;
        this.nextChapterId = nextChapterId;
    }
}
