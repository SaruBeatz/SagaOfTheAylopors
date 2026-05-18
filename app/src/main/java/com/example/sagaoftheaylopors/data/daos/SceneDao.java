package com.example.sagaoftheaylopors.data.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sagaoftheaylopors.data.entities.Scene;

import java.util.List;

@Dao
public interface SceneDao {
    @Query("SELECT * FROM scenes WHERE chapterId = :chapterId ORDER BY `order` ASC")
    List<Scene> getScenesByChapter(int chapterId);
    
    @Query("SELECT * FROM scenes WHERE sceneId = :sceneId")
    Scene getSceneById(int sceneId);
    
    @Query("SELECT * FROM scenes WHERE chapterId = :chapterId AND isCompleted = 0 ORDER BY `order` ASC LIMIT 1")
    Scene getNextIncompleteScene(int chapterId);
    
    @Query("SELECT COUNT(*) FROM scenes WHERE chapterId = :chapterId AND isCompleted = 1")
    int getCompletedSceneCount(int chapterId);
    
    @Query("SELECT COUNT(*) FROM scenes WHERE chapterId = :chapterId")
    int getTotalSceneCount(int chapterId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertScene(Scene scene);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertScenes(List<Scene> scenes);
    
    @Update
    void updateScene(Scene scene);
    
    @Query("UPDATE scenes SET isCompleted = 1 WHERE sceneId = :sceneId")
    void markSceneCompleted(int sceneId);
    
    @Query("UPDATE scenes SET isCompleted = 1 WHERE chapterId = :chapterId AND isCompleted = 0")
    void markAllIncompleteScenesCompleted(int chapterId);
}
