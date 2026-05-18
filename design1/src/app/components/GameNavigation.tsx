import { useState } from 'react';
import { AnimatePresence, motion } from 'motion/react';
import { SplashScreen } from './SplashScreen';
import { SettingsScreen } from './SettingsScreen';
import { PauseMenu } from './PauseMenu';
import { ChapterSummary } from './ChapterSummary';
import { ChapterTransition } from './ChapterTransition';

type Screen = 'splash' | 'settings' | 'pause' | 'summary' | 'transition' | 'game';

export function GameNavigation() {
  const [currentScreen, setCurrentScreen] = useState<Screen>('splash');

  // Navigation controls at the top for demo purposes
  const navigationButtons: Array<{ label: string; screen: Screen }> = [
    { label: 'Главное меню', screen: 'splash' },
    { label: 'Настройки', screen: 'settings' },
    { label: 'Пауза', screen: 'pause' },
    { label: 'Итоги главы', screen: 'summary' },
    { label: 'Переход главы', screen: 'transition' },
  ];

  const renderScreen = () => {
    switch (currentScreen) {
      case 'splash':
        return <SplashScreen />;
      
      case 'settings':
        return (
          <SettingsScreen 
            onBack={() => setCurrentScreen('splash')}
          />
        );
      
      case 'pause':
        return (
          <PauseMenu
            onResume={() => setCurrentScreen('game')}
            onSettings={() => setCurrentScreen('settings')}
            onMainMenu={() => setCurrentScreen('splash')}
          />
        );
      
      case 'summary':
        return (
          <ChapterSummary
            onContinue={() => setCurrentScreen('transition')}
            onMainMenu={() => setCurrentScreen('splash')}
          />
        );
      
      case 'transition':
        return (
          <ChapterTransition
            onStartChapter={() => setCurrentScreen('game')}
          />
        );
      
      case 'game':
        return (
          <div className="relative w-full h-full bg-[#1a1410] flex items-center justify-center">
            <div className="text-center">
              <h2 
                className="text-5xl text-[#D35400] mb-8"
                style={{ fontFamily: '"Cinzel", serif', fontWeight: 700 }}
              >
                Игровой процесс
              </h2>
              <p 
                className="text-xl text-[#FFF8DC] mb-8"
                style={{ fontFamily: '"Philosopher", sans-serif' }}
              >
                Здесь будет происходить визуальная новелла...
              </p>
              <button
                onClick={() => setCurrentScreen('pause')}
                className="px-8 py-4 rounded-lg"
                style={{
                  background: 'linear-gradient(135deg, #5D4037 0%, #3d2b26 100%)',
                  border: '2px solid #D35400',
                  fontFamily: '"Cinzel", serif',
                  fontWeight: 600,
                  color: '#FFF8DC',
                }}
              >
                Открыть меню паузы
              </button>
            </div>
          </div>
        );
      
      default:
        return <SplashScreen />;
    }
  };

  return (
    <div className="w-full h-full flex flex-col">
      {/* Demo Navigation Bar */}
      <div className="bg-[#2d1f1a] border-b-2 border-[#D35400] px-4 py-2 flex gap-2 overflow-x-auto z-50">
        {navigationButtons.map((button) => (
          <button
            key={button.screen}
            onClick={() => setCurrentScreen(button.screen)}
            className={`px-4 py-2 rounded text-sm whitespace-nowrap transition-colors ${
              currentScreen === button.screen
                ? 'bg-[#D35400] text-[#FFF8DC]'
                : 'bg-[#5D4037] text-[#FFF8DC] hover:bg-[#D35400]/70'
            }`}
            style={{ fontFamily: '"Philosopher", sans-serif' }}
          >
            {button.label}
          </button>
        ))}
      </div>

      {/* Screen Content with Transitions */}
      <div className="flex-1 relative overflow-hidden">
        <AnimatePresence mode="wait">
          <motion.div
            key={currentScreen}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.3 }}
            className="absolute inset-0"
          >
            {renderScreen()}
          </motion.div>
        </AnimatePresence>
      </div>
    </div>
  );
}
