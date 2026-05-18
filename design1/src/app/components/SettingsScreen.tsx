import { motion } from 'motion/react';
import { Volume2, Volume, Globe, ArrowLeft, Mail } from 'lucide-react';
import { ImageWithFallback } from './figma/ImageWithFallback';
import { useState } from 'react';

interface SettingsScreenProps {
  onBack: () => void;
}

export function SettingsScreen({ onBack }: SettingsScreenProps) {
  const [musicVolume, setMusicVolume] = useState(70);
  const [soundVolume, setSoundVolume] = useState(80);

  return (
    <div className="relative w-full h-full overflow-hidden bg-[#2d1f1a]">
      {/* Background */}
      <div className="absolute inset-0">
        <ImageWithFallback
          src="https://images.unsplash.com/photo-1766430954484-69bcf4a9720e?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx2aW50YWdlJTIwc3R1ZHklMjB3b29kZW4lMjBkZXNrfGVufDF8fHx8MTc2Nzk4MTczOHww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral"
          alt="Study"
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-b from-[#1B5E20]/40 via-[#5D4037]/60 to-[#2d1f1a]/90" />
      </div>

      {/* Content */}
      <div className="relative z-10 h-full flex items-center justify-center px-16 py-12">
        <motion.div
          className="w-full max-w-4xl"
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.5 }}
        >
          {/* Main Panel */}
          <div
            className="relative p-12 rounded-lg"
            style={{
              background: 'linear-gradient(135deg, rgba(93, 64, 55, 0.95) 0%, rgba(61, 43, 37, 0.95) 100%)',
              border: '3px solid #D35400',
              boxShadow: '0 10px 40px rgba(0, 0, 0, 0.7), inset 0 1px 0 rgba(211, 84, 0, 0.3)',
            }}
          >
            {/* Decorative Corners */}
            <div className="absolute top-0 left-0 w-16 h-16 border-l-4 border-t-4 border-[#D35400] rounded-tl-lg" />
            <div className="absolute top-0 right-0 w-16 h-16 border-r-4 border-t-4 border-[#D35400] rounded-tr-lg" />
            <div className="absolute bottom-0 left-0 w-16 h-16 border-l-4 border-b-4 border-[#D35400] rounded-bl-lg" />
            <div className="absolute bottom-0 right-0 w-16 h-16 border-r-4 border-b-4 border-[#D35400] rounded-br-lg" />

            {/* Title */}
            <h2
              className="text-5xl text-center mb-12"
              style={{
                fontFamily: '"Cinzel", serif',
                fontWeight: 700,
                color: '#D35400',
                textShadow: '0 2px 10px rgba(211, 84, 0, 0.5)',
              }}
            >
              Настройки пути
            </h2>

            {/* Settings Options */}
            <div className="space-y-8 mb-10">
              {/* Music Volume */}
              <div className="flex items-center gap-6">
                <Volume2 className="w-8 h-8 text-[#D35400]" />
                <div className="flex-1">
                  <label
                    className="text-lg text-[#FFF8DC] mb-2 block"
                    style={{ fontFamily: '"Philosopher", sans-serif' }}
                  >
                    Громкость музыки
                  </label>
                  <input
                    type="range"
                    min="0"
                    max="100"
                    value={musicVolume}
                    onChange={(e) => setMusicVolume(Number(e.target.value))}
                    className="w-full h-2 rounded-lg appearance-none cursor-pointer"
                    style={{
                      background: `linear-gradient(to right, #D35400 0%, #D35400 ${musicVolume}%, #5D4037 ${musicVolume}%, #5D4037 100%)`,
                    }}
                  />
                  <span className="text-sm text-[#D35400] mt-1 block">{musicVolume}%</span>
                </div>
              </div>

              {/* Sound Volume */}
              <div className="flex items-center gap-6">
                <Volume className="w-8 h-8 text-[#D35400]" />
                <div className="flex-1">
                  <label
                    className="text-lg text-[#FFF8DC] mb-2 block"
                    style={{ fontFamily: '"Philosopher", sans-serif' }}
                  >
                    Громкость звуков
                  </label>
                  <input
                    type="range"
                    min="0"
                    max="100"
                    value={soundVolume}
                    onChange={(e) => setSoundVolume(Number(e.target.value))}
                    className="w-full h-2 rounded-lg appearance-none cursor-pointer"
                    style={{
                      background: `linear-gradient(to right, #D35400 0%, #D35400 ${soundVolume}%, #5D4037 ${soundVolume}%, #5D4037 100%)`,
                    }}
                  />
                  <span className="text-sm text-[#D35400] mt-1 block">{soundVolume}%</span>
                </div>
              </div>

              {/* Language */}
              <div className="flex items-center gap-6">
                <Globe className="w-8 h-8 text-[#D35400]" />
                <div className="flex-1">
                  <label
                    className="text-lg text-[#FFF8DC] mb-2 block"
                    style={{ fontFamily: '"Philosopher", sans-serif' }}
                  >
                    Язык
                  </label>
                  <select
                    className="w-full px-4 py-2 rounded bg-[#5D4037] text-[#FFF8DC] border-2 border-[#D35400] focus:outline-none focus:border-[#D35400]"
                    style={{ fontFamily: '"Philosopher", sans-serif' }}
                  >
                    <option>Русский</option>
                    <option>English</option>
                    <option>Français</option>
                  </select>
                </div>
              </div>
            </div>

            {/* Info Block */}
            <div className="border-t-2 border-[#D35400]/30 pt-6 mb-8">
              <div className="flex justify-between items-center text-[#FFF8DC]/80 text-sm">
                <div style={{ fontFamily: '"Philosopher", sans-serif' }}>
                  <p>Версия: 1.0</p>
                </div>
                <div className="flex items-center gap-2 cursor-pointer hover:text-[#D35400] transition-colors">
                  <Mail className="w-4 h-4" />
                  <span style={{ fontFamily: '"Philosopher", sans-serif' }}>Связь с разработчиком</span>
                </div>
              </div>
            </div>

            {/* Back Button */}
            <motion.button
              onClick={onBack}
              className="w-full py-4 rounded-lg flex items-center justify-center gap-3 group"
              style={{
                background: 'linear-gradient(135deg, #1B5E20 0%, #0d3010 100%)',
                border: '2px solid #D35400',
                boxShadow: '0 4px 15px rgba(211, 84, 0, 0.3)',
              }}
              whileHover={{ scale: 1.02, boxShadow: '0 6px 20px rgba(211, 84, 0, 0.5)' }}
              whileTap={{ scale: 0.98 }}
            >
              <ArrowLeft className="w-6 h-6 text-[#D35400]" />
              <span
                className="text-xl"
                style={{
                  fontFamily: '"Cinzel", serif',
                  fontWeight: 600,
                  color: '#FFF8DC',
                }}
              >
                Вернуться в игру
              </span>
            </motion.button>
          </div>
        </motion.div>
      </div>
    </div>
  );
}
