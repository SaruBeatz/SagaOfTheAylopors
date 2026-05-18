# Story Architecture Documentation

## Overview

This document describes the architecture for handling large, branching story content with localization support in the Saga Ailoporosa Android application.

## Architecture Design

### Core Principles

1. **Separation of Content and Code**: All story text is stored in localization files (`strings.xml`), never hardcoded
2. **Database-Driven Structure**: Story structure (chapters, scenes, dialogues, choices) is stored in Room database
3. **Scalable Design**: Architecture supports hundreds of scenes and multiple story branches
4. **Localization Keys**: All text uses keys following pattern: `chapter_{id}_scene_{id}_dialogue_{id}`

## Package Structure

```
com.example.sagaoftheaylopors/
├── data/
│   ├── entities/          # Room database entities
│   │   ├── Chapter.java
│   │   ├── Scene.java
│   │   ├── Dialogue.java
│   │   ├── Choice.java
│   │   └── PlayerProgress.java
│   ├── daos/              # Data Access Objects
│   │   ├── ChapterDao.java
│   │   ├── SceneDao.java
│   │   ├── DialogueDao.java
│   │   ├── ChoiceDao.java
│   │   └── PlayerProgressDao.java
│   ├── database/          # Room database
│   │   └── AppDatabase.java
│   └── repository/        # Repository pattern
│       └── StoryRepository.java
├── viewmodel/             # ViewModels (to be created)
│   └── StoryViewModel.java
└── ui/                    # Activities and UI
    └── DialogueActivity.java
```

## Database Schema

### Chapter Entity
- `chapterId` (Primary Key): Unique chapter identifier
- `titleKey`: Localization key for chapter title
- `descriptionKey`: Localization key for chapter description
- `order`: Display order (1, 2, 3...)
- `isUnlocked`: Whether chapter can be accessed
- `isCompleted`: Whether chapter is finished
- `nextChapterId`: ID of next chapter (-1 if last)

### Scene Entity
- `sceneId` (Primary Key): Unique scene identifier
- `chapterId` (Foreign Key): Parent chapter
- `order`: Order within chapter
- `nameKey`: Optional scene name localization key
- `isCompleted`: Whether scene is finished
- `nextSceneId`: Next scene in chapter (-1 if last)

### Dialogue Entity
- `dialogueId` (Primary Key): Unique dialogue identifier
- `sceneId` (Foreign Key): Parent scene
- `order`: Display order within scene
- `speakerType`: "narrator", "cat", "marquis", etc.
- `textKey`: Localization key for dialogue text
- `hasChoices`: Whether choices follow this dialogue
- `nextDialogueId`: Next dialogue (-1 if choices follow)

### Choice Entity
- `choiceId` (Primary Key): Unique choice identifier
- `dialogueId` (Foreign Key): Parent dialogue
- `order`: Display order (1, 2, 3...)
- `textKey`: Localization key for choice text
- `nextDialogueId`: Dialogue to jump to when selected
- `nextSceneId`: Scene to jump to (if different scene)
- `statModifier`: Stat affected ("cunning", "bravery", "creativity")
- `statChange`: Stat change value (+/-)
- `unlocksPath`: Whether this unlocks a new story path

### PlayerProgress Entity
- `id` (Primary Key, always 1): Singleton pattern
- `currentChapterId`: Current chapter
- `currentSceneId`: Current scene
- `currentDialogueId`: Current dialogue
- `cunning`: Player's cunning stat
- `bravery`: Player's bravery stat
- `creativity`: Player's creativity stat
- `selectedCharacter`: Selected character name
- `lastSaveTimestamp`: Last save time

## Localization Key Pattern

### Format
```
chapter_{chapterId}_scene_{sceneId}_dialogue_{order}
chapter_{chapterId}_scene_{sceneId}_dialogue_{order}_choice_{choiceOrder}
```

### Examples
- `chapter_1_scene_1_dialogue_1` - First dialogue in first scene of chapter 1
- `chapter_1_scene_1_dialogue_3_choice_1` - First choice after dialogue 3

### Benefits
- Easy to locate text in code
- Scalable for hundreds of dialogues
- Clear hierarchy
- Supports multiple languages

## Story Progression Logic

### Automatic Chapter Progression

1. When a scene is completed, check if all scenes in chapter are complete
2. If all scenes complete, mark chapter as completed
3. If chapter has `nextChapterId`, unlock that chapter
4. Automatically transition to next chapter

### Choice Consequences

1. When player selects a choice:
   - Update player stats (if `statModifier` and `statChange` are set)
   - Jump to `nextDialogueId` or `nextSceneId`
   - If `unlocksPath` is true, unlock new scenes/chapters

### Branching Logic

- Choices can lead to different dialogues within same scene
- Choices can lead to different scenes
- Choices can affect stats that unlock different paths
- Multiple choices can lead to same outcome (convergence)
- Single choice can lead to different outcomes (divergence)

## Data Flow

```
User Action (Select Choice)
    ↓
DialogueActivity
    ↓
StoryViewModel
    ↓
StoryRepository
    ↓
AppDatabase (Room)
    ↓
Update PlayerProgress
    ↓
Load Next Dialogue/Scene
    ↓
Update UI
```

## Scalability Considerations

### Performance
- Room database is optimized for large datasets
- Queries use indices on foreign keys
- Lazy loading: only load current chapter/scene data
- Caching: Repository can cache frequently accessed data

### Content Management
- All text in `strings.xml` files (easy to edit)
- Database structure separate from content
- Can add new chapters without code changes (if using data initialization)
- Supports multiple story branches

### Localization
- Each language has its own `values-{locale}/strings.xml`
- Same key structure across all languages
- Easy to add new languages
- No external translation libraries needed

## Usage Example

### Loading Current Dialogue

```java
StoryRepository repository = StoryRepository.getInstance(context);
PlayerProgress progress = repository.getProgress();
Dialogue dialogue = repository.getDialogue(progress.currentDialogueId);
String text = getString(getResources().getIdentifier(
    dialogue.textKey, "string", getPackageName()));
```

### Processing Choice Selection

```java
Choice choice = repository.getChoice(choiceId);
// Update stats
if (choice.statModifier != null) {
    PlayerProgress progress = repository.getProgress();
    switch (choice.statModifier) {
        case "cunning":
            progress.cunning += choice.statChange;
            break;
        // ... other stats
    }
    repository.updateStats(progress.cunning, progress.bravery, progress.creativity);
}
// Jump to next dialogue/scene
if (choice.nextSceneId > 0) {
    repository.updateProgress(progress.currentChapterId, choice.nextSceneId, 1);
} else {
    repository.updateProgress(progress.currentChapterId, progress.currentSceneId, choice.nextDialogueId);
}
```

## Benefits of This Architecture

1. **Separation of Concerns**: Content (strings.xml) separate from structure (database)
2. **Scalability**: Handles hundreds of scenes efficiently
3. **Maintainability**: Clear structure, easy to add content
4. **Localization**: Built-in support for multiple languages
5. **Flexibility**: Supports complex branching narratives
6. **Performance**: Room database optimized for queries
7. **Testability**: Repository pattern allows easy testing
