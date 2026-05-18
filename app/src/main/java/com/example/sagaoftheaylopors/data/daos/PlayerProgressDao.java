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
    
    @Query("UPDATE player_progress SET currentChapterId = :chapterId, currentSceneId = :sceneId, currentDialogueId = :dialogueId, lastSaveTimestamp = :timestamp WHERE id = 1")
    void updateCurrentPosition(int chapterId, int sceneId, int dialogueId, long timestamp);
    
    @Query("UPDATE player_progress SET cunning = :cunning, bravery = :bravery, creativity = :creativity WHERE id = 1")
    void updateStats(int cunning, int bravery, int creativity);
}
