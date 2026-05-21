package com.example.sagaoftheaylopors.data.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.sagaoftheaylopors.data.entities.PendingChoice;

import java.util.List;

@Dao
public interface PendingChoiceDao {

    @Insert
    long insert(PendingChoice choice);

    @Query("SELECT * FROM pending_choices WHERE playthroughId = :playthroughId AND chapterId = :chapterId AND synced = 0 ORDER BY selectedAt ASC")
    List<PendingChoice> getUnsyncedForChapter(String playthroughId, int chapterId);

    @Query("SELECT COUNT(*) FROM pending_choices WHERE synced = 0")
    int countUnsynced();

    @Query("SELECT COUNT(*) FROM pending_choices WHERE playthroughId = :playthroughId AND chapterId = :chapterId AND synced = 0")
    int countUnsyncedForChapter(String playthroughId, int chapterId);

    @Query("SELECT DISTINCT chapterId FROM pending_choices WHERE playthroughId = :playthroughId AND synced = 0 ORDER BY chapterId ASC")
    List<Integer> getUnsyncedChapterIds(String playthroughId);

    @Query("SELECT * FROM pending_choices WHERE playthroughId = :playthroughId ORDER BY chapterId ASC, selectedAt ASC")
    List<PendingChoice> getAllForPlaythrough(String playthroughId);

    @Query("DELETE FROM pending_choices WHERE playthroughId = :playthroughId AND chapterId = :chapterId AND dialogueJsonId = :dialogueJsonId AND synced = 0")
    void deleteUnsyncedForDialogue(String playthroughId, int chapterId, String dialogueJsonId);

    @Query("UPDATE pending_choices SET synced = 1 WHERE playthroughId = :playthroughId AND chapterId = :chapterId AND synced = 0")
    void markChapterSynced(String playthroughId, int chapterId);
}
