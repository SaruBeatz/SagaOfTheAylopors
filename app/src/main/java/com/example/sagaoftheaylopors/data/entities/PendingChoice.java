package com.example.sagaoftheaylopors.data.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Buffered player choice pending Firestore flush at chapter end.
 * Uses stable JSON ids — never Room numeric ids in cloud payloads.
 */
@Entity(
        tableName = "pending_choices",
        indices = {
                @Index("playthroughId"),
                @Index(value = {"playthroughId", "chapterId", "synced"})
        }
)
public class PendingChoice {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String playthroughId;
    public int chapterId;
    public String sceneJsonId;
    public String dialogueJsonId;
    /** Same as dialogueJsonId — one choice point per dialogue with choices. */
    public String choicePointId;
    public String selectedChoiceId;
    public int selectedChoiceIndex;
    public String selectedChoiceText;
    public long selectedAt;
    public long timeFromDialogueShownMs;
    public long timeFromSceneStartMs;
    public long timeFromChapterStartMs;
    public long totalPlayTimeMsAtChoice;
    public String statsBeforeJson;
    public String statsAfterJson;
    public boolean synced;
}
