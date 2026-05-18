package com.example.sagaoftheaylopors.data.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Represents a scene within a chapter.
 * Scenes contain dialogues and choices that progress the story.
 */
@Entity(
    tableName = "scenes",
    foreignKeys = @ForeignKey(
        entity = Chapter.class,
        parentColumns = "chapterId",
        childColumns = "chapterId",
        onDelete = CASCADE
    ),
    indices = {@Index("chapterId")}
)
public class Scene {
    @PrimaryKey
    public int sceneId;
    
    /**
     * ID of the chapter this scene belongs to
     */
    public int chapterId;
    
    /**
     * Order of scene within the chapter (1, 2, 3, etc.)
     */
    public int order;
    
    /**
     * Localization key for scene name/description (optional)
     */
    public String nameKey;
    
    /**
     * Whether this scene has been completed
     */
    public boolean isCompleted;
    
    /**
     * Next scene ID in the chapter
     * -1 if this is the last scene in the chapter
     */
    public int nextSceneId;
    
    /**
     * Drawable resource name for the background image (optional)
     * Examples: "bg_ch1_mill_morning", "bg_ch1_mill_letter_focus"
     * Can be null if no background is specified
     */
    public String background;
    
    public Scene() {}
    
    public Scene(int sceneId, int chapterId, int order, String nameKey, 
                 boolean isCompleted, int nextSceneId) {
        this.sceneId = sceneId;
        this.chapterId = chapterId;
        this.order = order;
        this.nameKey = nameKey;
        this.isCompleted = isCompleted;
        this.nextSceneId = nextSceneId;
    }
}
