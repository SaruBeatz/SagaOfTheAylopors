package com.example.sagaoftheaylopors.data.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.sagaoftheaylopors.data.entities.Choice;

import java.util.List;

@Dao
public interface ChoiceDao {
    @Query("SELECT * FROM choices WHERE dialogueId = :dialogueId ORDER BY `order` ASC")
    List<Choice> getChoicesByDialogue(int dialogueId);
    
    @Query("SELECT * FROM choices WHERE choiceId = :choiceId")
    Choice getChoiceById(int choiceId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChoice(Choice choice);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChoices(List<Choice> choices);
}
