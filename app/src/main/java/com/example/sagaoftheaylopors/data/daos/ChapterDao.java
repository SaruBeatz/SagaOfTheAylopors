package com.example.sagaoftheaylopors.data.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sagaoftheaylopors.data.entities.Chapter;

import java.util.List;

@Dao
public interface ChapterDao {
    @Query("SELECT * FROM chapters ORDER BY `order` ASC")
    List<Chapter> getAllChapters();
    
    @Query("SELECT * FROM chapters WHERE chapterId = :chapterId")
    Chapter getChapterById(int chapterId);
    
    @Query("SELECT * FROM chapters WHERE isUnlocked = 1 ORDER BY `order` ASC")
    List<Chapter> getUnlockedChapters();
    
    @Query("SELECT * FROM chapters WHERE isCompleted = 1")
    List<Chapter> getCompletedChapters();
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChapter(Chapter chapter);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChapters(List<Chapter> chapters);
    
    @Update
    void updateChapter(Chapter chapter);
    
    @Query("UPDATE chapters SET isCompleted = 1 WHERE chapterId = :chapterId")
    void markChapterCompleted(int chapterId);
    
    @Query("UPDATE chapters SET isUnlocked = 1 WHERE chapterId = :chapterId")
    void unlockChapter(int chapterId);
}
