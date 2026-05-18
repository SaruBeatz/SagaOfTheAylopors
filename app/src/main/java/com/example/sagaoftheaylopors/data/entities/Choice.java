package com.example.sagaoftheaylopors.data.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Represents a player choice that appears after a dialogue.
 * Choices can affect story progression, character stats, or unlock different paths.
 */
@Entity(
    tableName = "choices",
    foreignKeys = @ForeignKey(
        entity = Dialogue.class,
        parentColumns = "dialogueId",
        childColumns = "dialogueId",
        onDelete = CASCADE
    ),
    indices = {@Index("dialogueId")}
)
public class Choice {
    @PrimaryKey
    public int choiceId;
    
    /**
     * ID of the dialogue this choice appears after
     */
    public int dialogueId;
    
    /**
     * Order of choice (1, 2, 3, etc.) - determines display order
     */
    public int order;
    
    /**
     * Localization key for the choice text
     * Format: "chapter_{chapterId}_scene_{sceneId}_dialogue_{dialogueId}_choice_{order}"
     */
    public String textKey;
    
    /**
     * Next dialogue ID to jump to when this choice is selected
     * -1 if this choice leads to a different scene/chapter
     */
    public int nextDialogueId;
    
    /**
     * Next scene ID if choice leads to different scene
     * -1 if staying in same scene
     */
    public int nextSceneId;
    
    /**
     * Stat modifier: "cunning", "bravery", "creativity", etc.
     * Can be null if choice doesn't affect stats
     */
    public String statModifier;
    
    /**
     * Stat change value (positive or negative)
     */
    public int statChange;
    
    /**
     * Whether this choice unlocks a new path or scene
     */
    public boolean unlocksPath;
    
    /**
     * Drawable resource name for the background image (optional)
     * Examples: "bg_ch1_mill_door_open", "bg_ch1_mill_thinking"
     * Can be null if no background is specified
     * If set, overrides dialogue and scene backgrounds
     */
    public String background;
    
    public Choice() {}
    
    public Choice(int choiceId, int dialogueId, int order, String textKey, 
                  int nextDialogueId, int nextSceneId, String statModifier, 
                  int statChange, boolean unlocksPath) {
        this.choiceId = choiceId;
        this.dialogueId = dialogueId;
        this.order = order;
        this.textKey = textKey;
        this.nextDialogueId = nextDialogueId;
        this.nextSceneId = nextSceneId;
        this.statModifier = statModifier;
        this.statChange = statChange;
        this.unlocksPath = unlocksPath;
    }
}
