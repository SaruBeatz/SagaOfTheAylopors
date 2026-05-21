package com.example.sagaoftheaylopors.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.sagaoftheaylopors.data.daos.ChapterDao;
import com.example.sagaoftheaylopors.data.daos.ChoiceDao;
import com.example.sagaoftheaylopors.data.daos.DialogueDao;
import com.example.sagaoftheaylopors.data.daos.PlayerProgressDao;
import com.example.sagaoftheaylopors.data.daos.SceneDao;
import com.example.sagaoftheaylopors.data.entities.Chapter;
import com.example.sagaoftheaylopors.data.entities.Choice;
import com.example.sagaoftheaylopors.data.entities.Dialogue;
import com.example.sagaoftheaylopors.data.entities.PlayerProgress;
import com.example.sagaoftheaylopors.data.entities.Scene;

@Database(
    entities = {
        Chapter.class,
        Scene.class,
        Dialogue.class,
        Choice.class,
        PlayerProgress.class
    },
    version = 7,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    
    public abstract ChapterDao chapterDao();
    public abstract SceneDao sceneDao();
    public abstract DialogueDao dialogueDao();
    public abstract ChoiceDao choiceDao();
    public abstract PlayerProgressDao playerProgressDao();
    
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "saga_database"
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // TODO: Refactor to use background threads
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
