package com.example.sagaoftheaylopors.data.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Represents a dialogue entry (narrator text or character dialogue).
 * Each dialogue belongs to a scene and is displayed in order.
 */
@Entity(
    tableName = "dialogues",
    foreignKeys = @ForeignKey(
        entity = Scene.class,
        parentColumns = "sceneId",
        childColumns = "sceneId",
        onDelete = CASCADE
    ),
    indices = {@Index("sceneId")}
)
public class Dialogue {
    @PrimaryKey
    public int dialogueId;
    
    /**
     * ID of the scene this dialogue belongs to
     */
    public int sceneId;
    
    /**
     * Order of dialogue within the scene (1, 2, 3, etc.)
     */
    public int order;
    
    /**
     * Speaker type: "narrator", "cat", "marquis", "king", "princess", "ogre", etc.
     * Use "narrator" for narrator text.
     */
    public String speakerType;
    
    /**
     * Localization key for the dialogue text
     * Format: "chapter_{chapterId}_scene_{sceneId}_dialogue_{order}"
     */
    public String textKey;
    
    /**
     * Whether this dialogue has choices after it
     */
    public boolean hasChoices;
    
    /**
     * Next dialogue ID in the scene
     * -1 if this is the last dialogue (or if choices follow)
     */
    public int nextDialogueId;
    
    /**
     * Drawable resource name for the background image (optional).
     * Overrides the scene background when set.
     */
    public String background;

    /**
     * Seconds to pause (show background only, hide dialogue box) before
     * this dialogue appears.  0 = no pause.
     */
    public float pauseBefore = 0f;

    /**
     * Camera / background animation to play while this dialogue is shown.
     * Values: "pan_up", "slow_zoom_in", "slow_zoom_out", "panorama"
     * null = no effect.
     */
    public String cameraEffect;

    /**
     * Full-screen transition effect that plays before this dialogue.
     * Values: "fade_in", "fade_out_in", "darkening"
     * null = no transition.
     */
    public String transitionBefore;

    /**
     * Companion filter: if set, this dialogue is only shown when the player's selected
     * companion matches this value. Null = shown for all companions (and when no companion
     * has been chosen yet).
     * Values: "alward", "lokian", "mirelin", "lavrik"
     */
    public String onlyForCompanion;

    /**
     * Portrait display mode for this dialogue.
     * "cat_only"       — only the Cat portrait (right side)
     * "companion_only" — only the companion portrait (left side)
     * "both"           — Cat (right) and companion (left) both visible
     * "none"           — both portraits hidden (pure atmosphere / transitions)
     * null             — legacy behaviour: inferred from speakerType
     */
    public String portraitMode;

    public Dialogue() {}
    
    public Dialogue(int dialogueId, int sceneId, int order, String speakerType, 
                    String textKey, boolean hasChoices, int nextDialogueId) {
        this.dialogueId = dialogueId;
        this.sceneId = sceneId;
        this.order = order;
        this.speakerType = speakerType;
        this.textKey = textKey;
        this.hasChoices = hasChoices;
        this.nextDialogueId = nextDialogueId;
    }
}
