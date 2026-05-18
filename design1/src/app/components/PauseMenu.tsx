import { motion } from 'motion/react';
import { Play, Save, Settings, Home } from 'lucide-react';
import { ImageWithFallback } from './figma/ImageWithFallback';

interface PauseMenuProps {
  onResume: () => void;
  onSettings: () => void;
  onMainMenu: () => void;
}

export function PauseMenu({ onResume, onSettings, onMainMenu }: PauseMenuProps) {
  return (
    <div className="relative w-full h-full overflow-hidden">
      {/* Blurred Game Background */}
      <div className="absolute inset-0">
        <ImageWithFallback
          src="https://images.unsplash.com/photo-1666195139009-a64f7f4b06b2?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxhdXR1bW4lMjBmb3Jlc3QlMjBwYXRoJTIwam91cm5leXxlbnwxfHx8fDE3Njc5ODE3NDB8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral"
          alt="Game Scene"
          className="w-full h-full object-cover"
          style={{ filter: 'blur(8px)' }}
        />
        <div className="absolute inset-0 bg-black/60" />
      </div>

      {/* Pause Menu Panel */}
      <div className="relative z-10 h-full flex items-center justify-center">
        <motion.div
          className="relative px-16 py-12 rounded-xl"
          style={{
            background: 'linear-gradient(135deg, rgba(93, 64, 55, 0.98) 0%, rgba(45, 31, 26, 0.98) 100%)',
            border: '4px solid #D35400',
            boxShadow: '0 20px 60px rgba(0, 0, 0, 0.9)',
          }}
          initial={{ opacity: 0, y: -50, scale: 0.9 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          transition={{ duration: 0.4, ease: 'easeOut' }}
        >
          {/* Decorative Parchment Texture */}
          <div className="absolute inset-0 opacity-10 rounded-xl" 
            style={{
              backgroundImage: 'url("data:image/svg+xml,%3Csvg width="100" height="100" xmlns="http://www.w3.org/2000/svg"%3E%3Cfilter id="noise"%3E%3CfeTurbulence type="fractalNoise" baseFrequency="0.9" numOctaves="4" /%3E%3C/filter%3E%3Crect width="100" height="100" filter="url(%23noise)" opacity="0.3" /%3E%3C/svg%3E")',
            }}
          />

          {/* Title */}
          <motion.h2
            className="text-5xl text-center mb-10"
            style={{
              fontFamily: '"Cinzel", serif',
              fontWeight: 700,
              color: '#D35400',
              textShadow: '0 2px 15px rgba(211, 84, 0, 0.6)',
            }}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.2 }}
          >
            Пауза
          </motion.h2>

          {/* Menu Buttons */}
          <div className="space-y-4 min-w-[400px]">
            {/* Resume Button */}
            <motion.button
              onClick={onResume}
              className="w-full py-4 px-6 rounded-lg flex items-center justify-between group relative overflow-hidden"
              style={{
                background: 'linear-gradient(135deg, #1B5E20 0%, #0f4016 100%)',
                border: '2px solid #D35400',
                boxShadow: '0 4px 12px rgba(211, 84, 0, 0.3)',
              }}
              whileHover={{ scale: 1.03, x: 5 }}
              whileTap={{ scale: 0.98 }}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.3 }}
            >
              <span
                className="text-xl flex items-center gap-3"
                style={{
                  fontFamily: '"Cinzel", serif',
                  fontWeight: 600,
                  color: '#FFF8DC',
                }}
              >
                <Play className="w-6 h-6 fill-current" />
                Продолжить путь
              </span>
            </motion.button>

            {/* Save Button */}
            <motion.button
              className="w-full py-4 px-6 rounded-lg flex items-center justify-between group"
              style={{
                background: 'linear-gradient(135deg, #5D4037 0%, #3d2b26 100%)',
                border: '2px solid #8B4513',
              }}
              whileHover={{ scale: 1.03, x: 5 }}
              whileTap={{ scale: 0.98 }}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.4 }}
            >
              <span
                className="text-xl flex items-center gap-3"
                style={{
                  fontFamily: '"Cinzel", serif',
                  fontWeight: 600,
                  color: '#FFF8DC',
                }}
              >
                <Save className="w-6 h-6" />
                Сохранить игру
              </span>
            </motion.button>

            {/* Settings Button */}
            <motion.button
              onClick={onSettings}
              className="w-full py-4 px-6 rounded-lg flex items-center justify-between group"
              style={{
                background: 'linear-gradient(135deg, #5D4037 0%, #3d2b26 100%)',
                border: '2px solid #8B4513',
              }}
              whileHover={{ scale: 1.03, x: 5 }}
              whileTap={{ scale: 0.98 }}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.5 }}
            >
              <span
                className="text-xl flex items-center gap-3"
                style={{
                  fontFamily: '"Cinzel", serif',
                  fontWeight: 600,
                  color: '#FFF8DC',
                }}
              >
                <Settings className="w-6 h-6" />
                Настройки
              </span>
            </motion.button>

            {/* Main Menu Button */}
            <motion.button
              onClick={onMainMenu}
              className="w-full py-4 px-6 rounded-lg flex items-center justify-between group"
              style={{
                background: 'linear-gradient(135deg, #5D4037 0%, #3d2b26 100%)',
                border: '2px solid #8B4513',
              }}
              whileHover={{ scale: 1.03, x: 5 }}
              whileTap={{ scale: 0.98 }}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.6 }}
            >
              <span
                className="text-xl flex items-center gap-3"
                style={{
                  fontFamily: '"Cinzel", serif',
                  fontWeight: 600,
                  color: '#FFF8DC',
                }}
              >
                <Home className="w-6 h-6" />
                В главное меню
              </span>
            </motion.button>
          </div>

          {/* Decorative Elements */}
          <div className="absolute -top-4 left-1/2 -translate-x-1/2 w-24 h-8 bg-[#D35400] rounded-full" 
            style={{ boxShadow: '0 4px 15px rgba(211, 84, 0, 0.5)' }}
          />
        </motion.div>
      </div>
    </div>
  );
}
