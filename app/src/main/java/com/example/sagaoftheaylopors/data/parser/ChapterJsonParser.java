package com.example.sagaoftheaylopors.data.parser;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.sagaoftheaylopors.data.entities.Chapter;
import com.example.sagaoftheaylopors.data.entities.Scene;
import com.example.sagaoftheaylopors.data.entities.Dialogue;
import com.example.sagaoftheaylopors.data.entities.Choice;

/**
 * Parser for chapter JSON files.
 * Loads chapter data from JSON and converts it to Room entities.
 */
public class ChapterJsonParser {
    private static final String TAG = "ChapterJsonParser";
    
    /**
     * Parse chapter JSON file and return structured data.
     */
    public static ChapterData parseChapterFromAssets(Context context, String fileName) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)
            );
            
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line).append('\n');
            }
            reader.close();
            inputStream.close();
            
            String jsonString = jsonBuilder.toString();
            return parseChapterJson(jsonString);
        } catch (Exception e) {
            Log.e(TAG, "Error loading chapter JSON from assets", e);
            return null;
        }
    }
    
    /**
     * Parse chapter JSON string into structured data.
     */
    public static ChapterData parseChapterJson(String jsonString) {
        try {
            JSONObject chapterJson = new JSONObject(jsonString);
            
            int chapterId = chapterJson.getInt("chapter_id");
            String title = chapterJson.getString("title");
            JSONArray scenesArray = chapterJson.getJSONArray("scenes");
            
            Chapter chapter = new Chapter(
                chapterId,
                title, // Store title as text (was titleKey)
                "", // subtitleKey - optional
                chapterId, // order
                true, // isUnlocked
                false, // isCompleted
                chapterId + 1 // nextChapterId - default to next chapter
            );
            
            List<SceneData> scenesData = new ArrayList<>();
            Map<String, Integer> sceneIdMap = new HashMap<>(); // Map JSON scene IDs to numeric IDs
            Map<String, Integer> dialogueIdMap = new HashMap<>(); // Map JSON dialogue IDs to numeric IDs
            Map<String, Integer> choiceIdMap = new HashMap<>(); // Map JSON choice IDs to numeric IDs
            
            int sceneCounter = 1;
            int dialogueCounter = 1;
            int choiceCounter = 1;
            
            // First pass: map all IDs
            for (int i = 0; i < scenesArray.length(); i++) {
                JSONObject sceneJson = scenesArray.getJSONObject(i);
                String sceneIdKey = sceneJson.getString("id");
                sceneIdMap.put(sceneIdKey, sceneCounter++);
                
                JSONArray dialoguesArray = sceneJson.getJSONArray("dialogues");
                for (int j = 0; j < dialoguesArray.length(); j++) {
                    JSONObject dialogueJson = dialoguesArray.getJSONObject(j);
                    String dialogueIdKey = dialogueJson.getString("id");
                    dialogueIdMap.put(dialogueIdKey, dialogueCounter++);
                    
                    if (dialogueJson.has("choices")) {
                        JSONArray choicesArray = dialogueJson.getJSONArray("choices");
                        for (int k = 0; k < choicesArray.length(); k++) {
                            JSONObject choiceJson = choicesArray.getJSONObject(k);
                            String choiceIdKey = choiceJson.getString("id");
                            choiceIdMap.put(choiceIdKey, choiceCounter++);
                        }
                    }
                }
            }
            
            // Second pass: create entities
            for (int i = 0; i < scenesArray.length(); i++) {
                JSONObject sceneJson = scenesArray.getJSONObject(i);
                String sceneIdKey = sceneJson.getString("id");
                int sceneId = sceneIdMap.get(sceneIdKey);
                int order = sceneJson.getInt("order");
                
                Scene scene = new Scene();
                scene.sceneId = sceneId;
                scene.chapterId = chapterId;
                scene.order = order;
                scene.nameKey = "";
                scene.isCompleted = false;
                scene.nextSceneId = (i < scenesArray.length() - 1) ? sceneIdMap.get(scenesArray.getJSONObject(i + 1).getString("id")) : -1;
                
                // Parse background if present
                scene.background = sceneJson.has("background") && !sceneJson.isNull("background")
                    ? sceneJson.getString("background") : null;
                
                List<Dialogue> dialogues = new ArrayList<>();
                List<Choice> choices = new ArrayList<>();
                
                JSONArray dialoguesArray = sceneJson.getJSONArray("dialogues");
                for (int j = 0; j < dialoguesArray.length(); j++) {
                    JSONObject dialogueJson = dialoguesArray.getJSONObject(j);
                    String dialogueIdKey = dialogueJson.getString("id");
                    int dialogueId = dialogueIdMap.get(dialogueIdKey);
                    int dialogueOrder = dialogueJson.getInt("order");
                    String speaker = dialogueJson.getString("speaker");
                    String text = dialogueJson.getString("text");
                    boolean hasChoices = dialogueJson.getBoolean("has_choices");
                    
                    // Determine next dialogue ID
                    int nextDialogueId = -1;
                    if (!hasChoices && dialogueJson.has("next_dialogue") && !dialogueJson.isNull("next_dialogue")) {
                        String nextDialogueKey = dialogueJson.getString("next_dialogue");
                        if (dialogueIdMap.containsKey(nextDialogueKey)) {
                            nextDialogueId = dialogueIdMap.get(nextDialogueKey);
                        }
                    }
                    
                    Dialogue dialogue = new Dialogue();
                    dialogue.dialogueId = dialogueId;
                    dialogue.sceneId = sceneId;
                    dialogue.order = dialogueOrder;
                    dialogue.speakerType = speaker.toLowerCase().replace(" ", "_");
                    dialogue.textKey = text; // Store text directly in textKey field
                    dialogue.hasChoices = hasChoices;
                    dialogue.nextDialogueId = nextDialogueId;
                    
                    // Parse background if present
                    dialogue.background = dialogueJson.has("background") && !dialogueJson.isNull("background")
                        ? dialogueJson.getString("background") : null;
                    
                    dialogues.add(dialogue);
                    
                    // Parse choices if present
                    if (hasChoices && dialogueJson.has("choices")) {
                        JSONArray choicesArray = dialogueJson.getJSONArray("choices");
                        for (int k = 0; k < choicesArray.length(); k++) {
                            JSONObject choiceJson = choicesArray.getJSONObject(k);
                            String choiceIdKey = choiceJson.getString("id");
                            int choiceId = choiceIdMap.get(choiceIdKey);
                            String choiceText = choiceJson.getString("text");
                            
                            // Parse effects if present (optional field)
                            JSONObject effects = null;
                            if (choiceJson.has("effects") && !choiceJson.isNull("effects")) {
                                effects = choiceJson.getJSONObject("effects");
                            }
                            
                            // Determine next scene ID
                            int nextSceneId = -1;
                            if (choiceJson.has("next_scene") && !choiceJson.isNull("next_scene")) {
                                String nextSceneKey = choiceJson.getString("next_scene");
                                if (sceneIdMap.containsKey(nextSceneKey)) {
                                    nextSceneId = sceneIdMap.get(nextSceneKey);
                                }
                            }
                            
                            // Determine next dialogue ID - check for next_dialogue in choice
                            int choiceNextDialogueId = -1;
                            if (choiceJson.has("next_dialogue") && !choiceJson.isNull("next_dialogue")) {
                                String nextDialogueKey = choiceJson.getString("next_dialogue");
                                if (dialogueIdMap.containsKey(nextDialogueKey)) {
                                    choiceNextDialogueId = dialogueIdMap.get(nextDialogueKey);
                                }
                            } else if (nextSceneId > 0) {
                                // If no next_dialogue specified but next_scene is set,
                                // this will be calculated in the third pass
                                choiceNextDialogueId = -1; // Will be calculated later
                            }
                            
                            Choice choice = new Choice();
                            choice.choiceId = choiceId;
                            choice.dialogueId = dialogueId;
                            choice.order = k + 1;
                            choice.textKey = choiceText; // Store text directly
                            choice.nextDialogueId = choiceNextDialogueId;
                            choice.nextSceneId = nextSceneId;
                            
                            // Parse background if present
                            choice.background = choiceJson.has("background") && !choiceJson.isNull("background")
                                ? choiceJson.getString("background") : null;
                            
                            // Extract effects - store first non-zero effect as statModifier for compatibility
                            // Full effects will be stored in separate fields (to be added)
                            choice.statModifier = null;
                            choice.statChange = 0;
                            
                            // Find first non-zero effect (only if effects object exists)
                            if (effects != null) {
                                String[] effectKeys = {"empathy", "self_confidence", "prudence", "ambition", "creativity", "impulsiveness"};
                                for (String key : effectKeys) {
                                    if (effects.has(key) && effects.getInt(key) != 0) {
                                        // Map to old stat names for compatibility
                                        if (key.equals("creativity")) {
                                            choice.statModifier = "creativity";
                                        } else if (key.equals("self_confidence") || key.equals("ambition")) {
                                            choice.statModifier = "bravery";
                                        } else {
                                            choice.statModifier = "cunning";
                                        }
                                        choice.statChange = effects.getInt(key);
                                        break;
                                    }
                                }
                            }
                            
                            choice.unlocksPath = false;
                            choices.add(choice);
                        }
                    }
                }
                
                SceneData sceneData = new SceneData(scene, dialogues, choices);
                scenesData.add(sceneData);
            }
            
            // Third pass: Update choice nextDialogueId based on nextSceneId
            for (SceneData sceneData : scenesData) {
                for (Choice choice : sceneData.choices) {
                    if (choice.nextSceneId > 0) {
                        // Find first dialogue in next scene
                        for (SceneData targetSceneData : scenesData) {
                            if (targetSceneData.scene.sceneId == choice.nextSceneId) {
                                if (!targetSceneData.dialogues.isEmpty()) {
                                    choice.nextDialogueId = targetSceneData.dialogues.get(0).dialogueId;
                                }
                                break;
                            }
                        }
                    }
                }
            }
            
            return new ChapterData(chapter, scenesData);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing chapter JSON", e);
            return null;
        }
    }
    
    /**
     * Data structure holding parsed chapter data.
     */
    public static class ChapterData {
        public final Chapter chapter;
        public final List<SceneData> scenes;
        
        public ChapterData(Chapter chapter, List<SceneData> scenes) {
            this.chapter = chapter;
            this.scenes = scenes;
        }
    }
    
    /**
     * Data structure holding parsed scene data.
     */
    public static class SceneData {
        public final Scene scene;
        public final List<Dialogue> dialogues;
        public final List<Choice> choices;
        
        public SceneData(Scene scene, List<Dialogue> dialogues, List<Choice> choices) {
            this.scene = scene;
            this.dialogues = dialogues;
            this.choices = choices;
        }
    }
}