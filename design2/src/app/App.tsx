import { useState } from 'react';
import { SplashScreen } from './components/SplashScreen';
import { DataInputScreen } from './components/DataInputScreen';
import { HeroSelectionScreen } from './components/HeroSelectionScreen';
import { ChapterMapScreen } from './components/ChapterMapScreen';
import { ChapterScreen } from './components/ChapterScreen';
import { SettingsScreen } from './components/SettingsScreen';
import { PauseMenuScreen } from './components/PauseMenuScreen';
import { ChapterSummaryScreen } from './components/ChapterSummaryScreen';
import { ChapterTransitionScreen } from './components/ChapterTransitionScreen';

type Screen = 'splash' | 'input' | 'hero' | 'map' | 'chapter' | 'settings' | 'pause' | 'summary' | 'transition';

export default function App() {
  const [currentScreen, setCurrentScreen] = useState<Screen>('splash');
  const [playerName, setPlayerName] = useState('');
  const [selectedHero, setSelectedHero] = useState('');
  const [currentChapter, setCurrentChapter] = useState(1);
  const [completedChapters, setCompletedChapters] = useState<number[]>([]);
  const [previousScreen, setPreviousScreen] = useState<Screen>('splash');

  const handleStart = () => {
    setCurrentScreen('input');
  };

  const handleDataInput = (name: string) => {
    setPlayerName(name);
    setCurrentScreen('hero');
  };

  const handleHeroSelection = (hero: string) => {
    setSelectedHero(hero);
    setCurrentScreen('map');
  };

  const handleStartChapter = (chapter: number) => {
    setCurrentChapter(chapter);
    setCurrentScreen('chapter');
  };

  const handleOpenSettings = () => {
    setPreviousScreen(currentScreen);
    setCurrentScreen('settings');
  };

  const handleCloseSettings = () => {
    setCurrentScreen(previousScreen);
  };

  const handlePause = () => {
    setPreviousScreen(currentScreen);
    setCurrentScreen('pause');
  };

  const handleResume = () => {
    setCurrentScreen(previousScreen);
  };

  const handleSave = () => {
    console.log('Game saved');
    // Implement save logic here
  };

  const handleMainMenu = () => {
    setCurrentScreen('splash');
  };

  const handleChapterComplete = () => {
    // Mark current chapter as completed
    setCompletedChapters([...completedChapters, currentChapter]);
    setCurrentScreen('summary');
  };

  const handleContinueFromSummary = () => {
    if (currentChapter < 7) {
      setCurrentScreen('transition');
    } else {
      setCurrentScreen('splash');
    }
  };

  const handleStartNextChapter = () => {
    setCurrentChapter(currentChapter + 1);
    setCurrentScreen('chapter');
  };

  return (
    <div className="w-full h-screen bg-[#5D4037] overflow-hidden">
      {currentScreen === 'splash' && <SplashScreen onStart={handleStart} />}
      
      {currentScreen === 'input' && <DataInputScreen onContinue={handleDataInput} />}
      
      {currentScreen === 'hero' && (
        <HeroSelectionScreen playerName={playerName} onContinue={handleHeroSelection} />
      )}
      
      {currentScreen === 'map' && (
        <ChapterMapScreen 
          onStartChapter={handleStartChapter}
          completedChapters={completedChapters}
          currentChapter={currentChapter}
        />
      )}
      
      {currentScreen === 'chapter' && <ChapterScreen chapterNumber={currentChapter} />}
      
      {currentScreen === 'settings' && (
        <SettingsScreen onBack={handleCloseSettings} />
      )}
      
      {currentScreen === 'pause' && (
        <PauseMenuScreen
          onResume={handleResume}
          onSave={handleSave}
          onSettings={handleOpenSettings}
          onMainMenu={handleMainMenu}
        />
      )}
      
      {currentScreen === 'summary' && (
        <ChapterSummaryScreen
          chapterNumber={currentChapter}
          chapterTitle={currentChapter === 1 ? 'Путь начат' : 'Продолжение саги'}
          playTime="1 ч 20 мин"
          choicesMade={12}
          achievement="Находчивый ученик"
          onContinue={handleContinueFromSummary}
          onMainMenu={handleMainMenu}
        />
      )}
      
      {currentScreen === 'transition' && (
        <ChapterTransitionScreen
          fromChapter={currentChapter}
          toChapter={currentChapter + 1}
          toChapterTitle={
            currentChapter === 1 ? 'План хитреца' :
            currentChapter === 2 ? 'Встреча с королём' :
            currentChapter === 3 ? 'Замок людоеда' :
            currentChapter === 4 ? 'Превращения' :
            currentChapter === 5 ? 'Триумф' :
            'Эпилог'
          }
          onContinue={handleStartNextChapter}
        />
      )}

      {/* Global controls demo - in real app these would be triggered by keyboard/UI */}
      <div className="fixed bottom-4 right-4 z-50 flex gap-2 opacity-30 hover:opacity-100 transition-opacity">
        {currentScreen === 'chapter' && (
          <>
            <button
              onClick={handlePause}
              className="bg-[#5D4037] text-[#F39C12] px-4 py-2 border-2 border-[#F39C12] text-sm"
              style={{ fontFamily: 'Georgia, serif' }}
            >
              Пауза (ESC)
            </button>
            <button
              onClick={handleChapterComplete}
              className="bg-[#1B5E20] text-[#F39C12] px-4 py-2 border-2 border-[#F39C12] text-sm"
              style={{ fontFamily: 'Georgia, serif' }}
            >
              Завершить главу (demo)
            </button>
          </>
        )}
        {currentScreen === 'map' && (
          <button
            onClick={handleOpenSettings}
            className="bg-[#5D4037] text-[#F39C12] px-4 py-2 border-2 border-[#F39C12] text-sm"
            style={{ fontFamily: 'Georgia, serif' }}
          >
            Настройки
          </button>
        )}
      </div>
    </div>
  );
}
