package com.example.sagaoftheaylopors.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Tracks player's current story position and behavioral parameters.
 * Singleton pattern — always id = 1.
 *
 * Behavioral parameters: range [0.0 – 1.0], start value 0.5.
 * They feed the ML classifier that predicts Leonhard personality
 * accentuations at the end of the game.
 *
 * Each in-game choice applies a small float delta (e.g. +0.03, -0.02).
 * Max expected drift per parameter across the whole game: ≈ ±0.45.
 */
@Entity(tableName = "player_progress")
public class PlayerProgress {

    @PrimaryKey
    public int id = 1;

    // ─── Story position ───────────────────────────────────────────────────────

    public int currentChapterId;
    public int currentSceneId;
    public int currentDialogueId;

    // ─── Behavioral parameters ────────────────────────────────────────────────
    // All start at 0.5 (neutral), range [0.0, 1.0].

    /** Общительность — drive to interact and belong to a group */
    public float sociality            = 0.5f;

    /** Энергичность — initiative, physical and emotional drive */
    public float activity             = 0.5f;

    /** Эмпатия и чувствительность — emotional depth, empathy */
    public float emotionalSensitivity = 0.5f;

    /** Тревожность и неуверенность — fear, doubt, avoidance */
    public float anxiety              = 0.5f;

    /** Самоконтроль и дисциплина — deliberate decisions, rule-following */
    public float selfControl          = 0.5f;

    /** Импульсивность и вспыльчивость — acting on impulse */
    public float impulsivity          = 0.5f;

    /** Потребность во внимании и признании — desire to stand out */
    public float egoFocus             = 0.5f;

    /** Обидчивость и подозрительность — sensitivity to injustice */
    public float rigidity             = 0.5f;

    /** Пессимизм и подавленность — negative mood baseline */
    public float negativeAffect       = 0.5f;

    /** Гибкость и приспособляемость — openness to change */
    public float adaptability         = 0.5f;

    // ─── Session metadata ─────────────────────────────────────────────────────

    public String selectedCharacter;
    public long lastSaveTimestamp;

    // ─── Constructors ─────────────────────────────────────────────────────────

    public PlayerProgress() {
        this.id = 1;
        this.currentChapterId = 1;
        this.currentSceneId   = 1;
        this.currentDialogueId = 1;
        this.lastSaveTimestamp = System.currentTimeMillis();
    }

    public PlayerProgress(int currentChapterId, int currentSceneId, int currentDialogueId,
                          String selectedCharacter) {
        this.id = 1;
        this.currentChapterId  = currentChapterId;
        this.currentSceneId    = currentSceneId;
        this.currentDialogueId = currentDialogueId;
        this.selectedCharacter = selectedCharacter;
        this.lastSaveTimestamp = System.currentTimeMillis();
    }

    /**
     * Apply all 10 behavioral deltas from one choice.
     * Values are clamped to [0.0, 1.0] after each application.
     */
    public void applyEffects(
            float dSociality, float dActivity, float dEmotionalSensitivity,
            float dAnxiety, float dSelfControl, float dImpulsivity,
            float dEgoFocus, float dRigidity, float dNegativeAffect, float dAdaptability) {

        sociality            = clamp(sociality            + dSociality);
        activity             = clamp(activity             + dActivity);
        emotionalSensitivity = clamp(emotionalSensitivity + dEmotionalSensitivity);
        anxiety              = clamp(anxiety              + dAnxiety);
        selfControl          = clamp(selfControl          + dSelfControl);
        impulsivity          = clamp(impulsivity          + dImpulsivity);
        egoFocus             = clamp(egoFocus             + dEgoFocus);
        rigidity             = clamp(rigidity             + dRigidity);
        negativeAffect       = clamp(negativeAffect       + dNegativeAffect);
        adaptability         = clamp(adaptability         + dAdaptability);
    }

    private static float clamp(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }
}
