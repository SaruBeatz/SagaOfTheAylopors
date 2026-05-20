package com.example.sagaoftheaylopors.data.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sagaoftheaylopors.data.entities.PlayerProgress;

@Dao
public interface PlayerProgressDao {

    @Query("SELECT * FROM player_progress WHERE id = 1")
    PlayerProgress getProgress();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProgress(PlayerProgress progress);

    @Update
    void updateProgress(PlayerProgress progress);

    @Query("UPDATE player_progress SET " +
           "currentChapterId = :chapterId, " +
           "currentSceneId = :sceneId, " +
           "currentDialogueId = :dialogueId, " +
           "lastSaveTimestamp = :timestamp " +
           "WHERE id = 1")
    void updateCurrentPosition(int chapterId, int sceneId, int dialogueId, long timestamp);

    /**
     * Update all 10 behavioral parameters in one atomic SQL statement.
     * Room stores Java float as SQLite REAL automatically.
     */
    @Query("UPDATE player_progress SET " +
           "sociality = :sociality, " +
           "activity = :activity, " +
           "emotionalSensitivity = :emotionalSensitivity, " +
           "anxiety = :anxiety, " +
           "selfControl = :selfControl, " +
           "impulsivity = :impulsivity, " +
           "egoFocus = :egoFocus, " +
           "rigidity = :rigidity, " +
           "negativeAffect = :negativeAffect, " +
           "adaptability = :adaptability " +
           "WHERE id = 1")
    void updateBehavioralParams(
            float sociality,
            float activity,
            float emotionalSensitivity,
            float anxiety,
            float selfControl,
            float impulsivity,
            float egoFocus,
            float rigidity,
            float negativeAffect,
            float adaptability
    );
}
