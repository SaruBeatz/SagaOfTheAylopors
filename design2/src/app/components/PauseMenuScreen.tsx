import { Play, Save, Settings, Home } from 'lucide-react';

interface PauseMenuScreenProps {
  onResume: () => void;
  onSave: () => void;
  onSettings: () => void;
  onMainMenu: () => void;
  backgroundImage?: string;
}

export function PauseMenuScreen({ 
  onResume, 
  onSave, 
  onSettings, 
  onMainMenu,
  backgroundImage = 'https://images.unsplash.com/photo-1694100381966-5cf52917d452?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxhdXR1bW4lMjBmb3Jlc3QlMjBwYXRofGVufDF8fHx8MTc2Nzk3OTA3Mnww&ixlib=rb-4.1.0&q=80&w=1080'
}: PauseMenuScreenProps) {
  return (
    <div className="relative w-full h-screen overflow-hidden">
      {/* Background - blurred game scene */}
      <div 
        className="absolute inset-0 bg-cover bg-center"
        style={{
          backgroundImage: `url('${backgroundImage}')`,
          filter: 'blur(4px) brightness(0.6)'
        }}
      />

      {/* Dark overlay */}
      <div className="absolute inset-0 bg-[#5D4037]/70" />

      {/* Content */}
      <div className="relative z-10 h-full flex items-center justify-center px-12">
        <div className="w-full max-w-2xl">
          {/* Parchment panel */}
          <div className="relative bg-[#E8DCC8] border-8 border-[#5D4037] shadow-2xl p-12">
            {/* Decorative corners */}
            <div className="absolute top-4 left-4 w-8 h-8 border-t-4 border-l-4 border-[#D35400]"></div>
            <div className="absolute top-4 right-4 w-8 h-8 border-t-4 border-r-4 border-[#D35400]"></div>
            <div className="absolute bottom-4 left-4 w-8 h-8 border-b-4 border-l-4 border-[#D35400]"></div>
            <div className="absolute bottom-4 right-4 w-8 h-8 border-b-4 border-r-4 border-[#D35400]"></div>

            {/* Title */}
            <div className="text-center mb-12">
              <div className="inline-block bg-[#5D4037] border-4 border-[#F39C12] px-12 py-4 shadow-lg">
                <h2 className="text-5xl text-[#F39C12]" style={{ fontFamily: 'Georgia, serif' }}>
                  Путь приостановлен
                </h2>
              </div>
            </div>

            {/* Menu buttons */}
            <div className="space-y-4">
              {/* Resume button - primary */}
              <button
                onClick={onResume}
                className="w-full bg-[#D35400] hover:bg-[#F39C12] border-4 border-[#F39C12] text-white py-6 px-8 text-2xl transition-all duration-300 transform hover:scale-105 shadow-xl flex items-center justify-center gap-4"
                style={{ fontFamily: 'Georgia, serif' }}
              >
                <Play className="w-8 h-8" fill="currentColor" />
                Продолжить путь
              </button>

              {/* Save button */}
              <button
                onClick={onSave}
                className="w-full bg-[#5D4037] hover:bg-[#D35400] border-4 border-[#5D4037] hover:border-[#F39C12] text-[#E8DCC8] py-5 px-8 text-xl transition-all duration-300 flex items-center justify-center gap-4"
                style={{ fontFamily: 'Georgia, serif' }}
              >
                <Save className="w-7 h-7" />
                Сохранить игру
              </button>

              {/* Settings button */}
              <button
                onClick={onSettings}
                className="w-full bg-[#5D4037] hover:bg-[#D35400] border-4 border-[#5D4037] hover:border-[#F39C12] text-[#E8DCC8] py-5 px-8 text-xl transition-all duration-300 flex items-center justify-center gap-4"
                style={{ fontFamily: 'Georgia, serif' }}
              >
                <Settings className="w-7 h-7" />
                Настройки
              </button>

              {/* Main menu button */}
              <button
                onClick={onMainMenu}
                className="w-full bg-[#5D4037] hover:bg-[#D35400] border-4 border-[#5D4037] hover:border-[#F39C12] text-[#E8DCC8] py-5 px-8 text-xl transition-all duration-300 flex items-center justify-center gap-4"
                style={{ fontFamily: 'Georgia, serif' }}
              >
                <Home className="w-7 h-7" />
                В главное меню
              </button>
            </div>

            {/* Decorative ink stain */}
            <div className="absolute bottom-8 right-8 w-8 h-8 bg-[#283593] rounded-full opacity-20 blur-sm"></div>
          </div>

          {/* Hint text */}
          <p className="text-center mt-6 text-[#E8DCC8] text-lg" style={{ fontFamily: 'Georgia, serif' }}>
            Нажмите ESC чтобы вернуться к игре
          </p>
        </div>
      </div>
    </div>
  );
}
