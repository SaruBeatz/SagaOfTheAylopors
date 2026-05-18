package com.example.sagaoftheaylopors.data.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.sagaoftheaylopors.data.entities.Dialogue;

import java.util.List;

@Dao
public interface DialogueDao {
    @Query("SELECT * FROM dialogues WHERE sceneId = :sceneId ORDER BY `order` ASC")
    List<Dialogue> getDialoguesByScene(int sceneId);
    
    @Query("SELECT * FROM dialogues WHERE dialogueId = :dialogueId")
    Dialogue getDialogueById(int dialogueId);
    
    @Query("SELECT * FROM dialogues WHERE sceneId = :sceneId AND `order` = :order")
    Dialogue getDialogueBySceneAndOrder(int sceneId, int order);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDialogue(Dialogue dialogue);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDialogues(List<Dialogue> dialogues);
}
