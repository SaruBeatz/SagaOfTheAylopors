import { Volume2, Volume1, Globe, Mail, ArrowLeft } from 'lucide-react';
import { useState } from 'react';

interface SettingsScreenProps {
  onBack: () => void;
}

export function SettingsScreen({ onBack }: SettingsScreenProps) {
  const [musicVolume, setMusicVolume] = useState(70);
  const [soundVolume, setSoundVolume] = useState(80);
  const [language, setLanguage] = useState('ru');

  return (
    <div className="relative w-full h-screen overflow-hidden">
      {/* Background */}
      <div 
        className="absolute inset-0 bg-cover bg-center"
        style={{
          backgroundImage: `url('https://images.unsplash.com/photo-1678574420972-2b59628f904c?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtZWRpZXZhbCUyMGNhYmluJTIwaW50ZXJpb3IlMjB0YWJsZSUyMHNjcm9sbHN8ZW58MXx8fHwxNzY3OTc5MDY3fDA&ixlib=rb-4.1.0&q=80&w=1080')`,
        }}
      >
        <div className="absolute inset-0 bg-[#5D4037]/85" />
      </div>

      {/* Content */}
      <div className="relative z-10 h-full flex items-center justify-center px-12">
        <div className="w-full max-w-4xl">
          {/* Main panel */}
          <div className="bg-[#E8DCC8] border-8 border-[#5D4037] shadow-2xl p-12">
            {/* Decorative corners */}
            <div className="absolute top-4 left-4 w-8 h-8 border-t-4 border-l-4 border-[#D35400]"></div>
            <div className="absolute top-4 right-4 w-8 h-8 border-t-4 border-r-4 border-[#D35400]"></div>
            <div className="absolute bottom-4 left-4 w-8 h-8 border-b-4 border-l-4 border-[#D35400]"></div>
            <div className="absolute bottom-4 right-4 w-8 h-8 border-b-4 border-r-4 border-[#D35400]"></div>

            {/* Title */}
            <h2 className="text-5xl text-center mb-12 text-[#5D4037]" style={{ fontFamily: 'Georgia, serif' }}>
              Настройки пути
            </h2>

            {/* Settings options */}
            <div className="space-y-8 mb-12">
              {/* Music Volume */}
              <div className="bg-[#F5E6D3] border-4 border-[#5D4037] p-6">
                <div className="flex items-center gap-6 mb-4">
                  <Volume2 className="w-10 h-10 text-[#D35400]" />
                  <h3 className="text-2xl text-[#5D4037]" style={{ fontFamily: 'Georgia, serif' }}>
                    Громкость музыки
                  </h3>
                </div>
                <div className="flex items-center gap-4">
                  <input
                    type="range"
                    min="0"
                    max="100"
                    value={musicVolume}
                    onChange={(e) => setMusicVolume(Number(e.target.value))}
                    className="flex-1 h-3 appearance-none bg-[#5D4037] rounded-full cursor-pointer"
                    style={{
                      background: `linear-gradient(to right, #D35400 0%, #D35400 ${musicVolume}%, #5D4037 ${musicVolume}%, #5D4037 100%)`
                    }}
                  />
                  <span className="text-2xl text-[#5D4037] w-16 text-right" style={{ fontFamily: 'Georgia, serif' }}>
                    {musicVolume}%
                  </span>
                </div>
              </div>

              {/* Sound Volume */}
              <div className="bg-[#F5E6D3] border-4 border-[#5D4037] p-6">
                <div className="flex items-center gap-6 mb-4">
                  <Volume1 className="w-10 h-10 text-[#D35400]" />
                  <h3 className="text-2xl text-[#5D4037]" style={{ fontFamily: 'Georgia, serif' }}>
                    Громкость звуков
                  </h3>
                </div>
                <div className="flex items-center gap-4">
                  <input
                    type="range"
                    min="0"
                    max="100"
                    value={soundVolume}
                    onChange={(e) => setSoundVolume(Number(e.target.value))}
                    className="flex-1 h-3 appearance-none bg-[#5D4037] rounded-full cursor-pointer"
                    style={{
                      background: `linear-gradient(to right, #D35400 0%, #D35400 ${soundVolume}%, #5D4037 ${soundVolume}%, #5D4037 100%)`
                    }}
                  />
                  <span className="text-2xl text-[#5D4037] w-16 text-right" style={{ fontFamily: 'Georgia, serif' }}>
                    {soundVolume}%
                  </span>
                </div>
              </div>

              {/* Language */}
              <div className="bg-[#F5E6D3] border-4 border-[#5D4037] p-6">
                <div className="flex items-center gap-6 mb-4">
                  <Globe className="w-10 h-10 text-[#D35400]" />
                  <h3 className="text-2xl text-[#5D4037]" style={{ fontFamily: 'Georgia, serif' }}>
                    Язык
                  </h3>
                </div>
                <div className="flex gap-4">
                  {[
                    { code: 'ru', label: 'Русский' },
                    { code: 'en', label: 'English' },
                    { code: 'fr', label: 'Français' }
                  ].map((lang) => (
                    <button
                      key={lang.code}
                      onClick={() => setLanguage(lang.code)}
                      className={`flex-1 py-3 px-6 text-xl border-4 transition-all duration-300 ${
                        language === lang.code
                          ? 'bg-[#D35400] border-[#F39C12] text-white'
                          : 'bg-[#E8DCC8] border-[#5D4037] text-[#5D4037] hover:border-[#D35400]'
                      }`}
                      style={{ fontFamily: 'Georgia, serif' }}
                    >
                      {lang.label}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* Info section */}
            <div className="bg-[#5D4037]/20 border-2 border-[#5D4037] p-6 mb-8">
              <div className="grid grid-cols-2 gap-6">
                <div>
                  <p className="text-lg text-[#5D4037] mb-2" style={{ fontFamily: 'Georgia, serif' }}>
                    <strong>Версия:</strong> 1.0
                  </p>
                  <p className="text-lg text-[#5D4037]" style={{ fontFamily: 'Georgia, serif' }}>
                    <strong>Дата выпуска:</strong> 2026
                  </p>
                </div>
                <div className="flex items-center gap-4">
                  <Mail className="w-8 h-8 text-[#D35400]" />
                  <div>
                    <p className="text-lg text-[#5D4037]" style={{ fontFamily: 'Georgia, serif' }}>
                      <strong>Связь с разработчиком</strong>
                    </p>
                    <p className="text-sm text-[#5D4037]" style={{ fontFamily: 'Georgia, serif' }}>
                      support@ailoporos.saga
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Back button */}
            <button
              onClick={onBack}
              className="w-full bg-[#D35400] hover:bg-[#F39C12] border-4 border-[#F39C12] text-white py-5 px-8 text-2xl transition-all duration-300 transform hover:scale-105 shadow-xl flex items-center justify-center gap-4"
              style={{ fontFamily: 'Georgia, serif' }}
            >
              <ArrowLeft className="w-8 h-8" />
              Вернуться в игру
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
