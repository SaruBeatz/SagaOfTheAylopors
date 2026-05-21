package com.example.sagaoftheaylopors.data.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

/**
 * A player choice that appears after a dialogue.
 *
 * Each choice carries 10 behavioral effect deltas (float, small values like ±0.03).
 * They are accumulated into PlayerProgress and feed the ML personality classifier.
 *
 * JSON "effects" key → Java field mapping (snake_case → camelCase):
 *   sociality              → effectSociality
 *   activity               → effectActivity
 *   emotional_sensitivity  → effectEmotionalSensitivity
 *   anxiety                → effectAnxiety
 *   self_control           → effectSelfControl
 *   impulsivity            → effectImpulsivity
 *   ego_focus              → effectEgoFocus
 *   rigidity               → effectRigidity
 *   negative_affect        → effectNegativeAffect
 *   adaptability           → effectAdaptability
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

    /** ID of the dialogue this choice appears after */
    public int dialogueId;

    /** Display order within the choice set (1, 2, 3 …) */
    public int order;

    /** Choice text stored directly (loaded from JSON) */
    public String textKey;

    /** Next dialogue ID; -1 if the choice goes to a different scene */
    public int nextDialogueId;

    /** Next scene ID; -1 if staying in the same scene */
    public int nextSceneId;

    /** Whether this choice unlocks a new narrative path */
    public boolean unlocksPath;

    /**
     * Drawable resource name for the background image (optional).
     * If set, overrides dialogue and scene backgrounds for this choice.
     */
    public String background;

    // ─── Behavioral effect deltas (float, range typically ±0.01 – ±0.05) ─────
    // Positive values increase the parameter; negative values decrease it.
    // All default to 0.0 (no effect).

    public float effectSociality;
    public float effectActivity;
    public float effectEmotionalSensitivity;
    public float effectAnxiety;
    public float effectSelfControl;
    public float effectImpulsivity;
    public float effectEgoFocus;
    public float effectRigidity;
    public float effectNegativeAffect;
    public float effectAdaptability;

    /**
     * Companion identifier to save when this choice is selected (optional).
     * Set via JSON "save_companion" field on choices where the player picks a companion.
     * Stored in PlayerProgress.selectedCharacter for future chapters.
     * Example values: "alward", "lokian", "mirelin", "lavrik".
     */
    public String saveCompanion;

    public Choice() {}

    public Choice(int choiceId, int dialogueId, int order, String textKey,
                  int nextDialogueId, int nextSceneId, boolean unlocksPath) {
        this.choiceId = choiceId;
        this.dialogueId = dialogueId;
        this.order = order;
        this.textKey = textKey;
        this.nextDialogueId = nextDialogueId;
        this.nextSceneId = nextSceneId;
        this.unlocksPath = unlocksPath;
    }
}
