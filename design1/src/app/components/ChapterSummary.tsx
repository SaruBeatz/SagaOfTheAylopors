import { motion } from 'motion/react';
import { ArrowRight, Home, Award, Clock, MousePointer, Mail, Twitter, Facebook } from 'lucide-react';
import { ImageWithFallback } from './figma/ImageWithFallback';

interface ChapterSummaryProps {
  onContinue: () => void;
  onMainMenu: () => void;
}

export function ChapterSummary({ onContinue, onMainMenu }: ChapterSummaryProps) {
  return (
    <div className="relative w-full h-full overflow-hidden bg-[#1a1410]">
      {/* Background */}
      <div className="absolute inset-0">
        <ImageWithFallback
          src="https://images.unsplash.com/photo-1560528522-918483dd63e2?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjYW1wZmlyZSUyMG5pZ2h0JTIwc3RhcnN8ZW58MXx8fHwxNzY3OTY2Nzc1fDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral"
          alt="Campfire"
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-b from-[#1B5E20]/30 via-[#1a1410]/70 to-[#1a1410]/95" />
      </div>

      {/* Floating embers */}
      {[...Array(8)].map((_, i) => (
        <motion.div
          key={i}
          className="absolute w-1 h-1 rounded-full bg-[#D35400]"
          style={{
            left: `${30 + Math.random() * 40}%`,
            bottom: `${10 + Math.random() * 30}%`,
            boxShadow: '0 0 6px 2px rgba(211, 84, 0, 0.8)',
          }}
          animate={{
            y: [-10, -100],
            opacity: [0.8, 0],
            scale: [1, 0.5],
          }}
          transition={{
            duration: 4 + Math.random() * 3,
            repeat: Infinity,
            delay: Math.random() * 3,
          }}
        />
      ))}

      {/* Content */}
      <div className="relative z-10 h-full flex flex-col items-center justify-between px-16 py-12">
        
        {/* Header */}
        <motion.div
          className="text-center mt-8"
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
        >
          <h1
            className="text-6xl mb-4"
            style={{
              fontFamily: '"Cinzel", serif',
              fontWeight: 700,
              color: '#D35400',
              textShadow: '0 0 30px rgba(211, 84, 0, 0.6)',
            }}
          >
            Глава I: Путь начат
          </h1>
          <div className="w-48 h-1 bg-gradient-to-r from-transparent via-[#D35400] to-transparent mx-auto" />
        </motion.div>

        {/* Stats Panel */}
        <motion.div
          className="w-full max-w-3xl"
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ delay: 0.3, duration: 0.6 }}
        >
          <div
            className="relative p-10 rounded-lg"
            style={{
              background: 'linear-gradient(135deg, rgba(93, 64, 55, 0.92) 0%, rgba(45, 31, 26, 0.92) 100%)',
              border: '3px solid #D35400',
              boxShadow: '0 10px 40px rgba(0, 0, 0, 0.8)',
            }}
          >
            {/* Parchment texture */}
            <div className="absolute inset-0 opacity-5 rounded-lg"
              style={{
                backgroundImage: `repeating-linear-gradient(0deg, #D35400 0px, transparent 2px, transparent 4px)`,
              }}
            />

            {/* Stats Grid */}
            <div className="grid grid-cols-3 gap-8 mb-8">
              <motion.div
                className="text-center"
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.5 }}
              >
                <Clock className="w-12 h-12 text-[#D35400] mx-auto mb-3" />
                <p className="text-sm text-[#FFF8DC]/70 mb-1" style={{ fontFamily: '"Philosopher", sans-serif' }}>
                  В пути
                </p>
                <p className="text-2xl text-[#FFF8DC]" style={{ fontFamily: '"Cinzel", serif', fontWeight: 600 }}>
                  1 ч 20 мин
                </p>
              </motion.div>

              <motion.div
                className="text-center"
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.6 }}
              >
                <MousePointer className="w-12 h-12 text-[#D35400] mx-auto mb-3" />
                <p className="text-sm text-[#FFF8DC]/70 mb-1" style={{ fontFamily: '"Philosopher", sans-serif' }}>
                  Сделано выборов
                </p>
                <p className="text-2xl text-[#FFF8DC]" style={{ fontFamily: '"Cinzel", serif', fontWeight: 600 }}>
                  12
                </p>
              </motion.div>

              <motion.div
                className="text-center"
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.7 }}
              >
                <Award className="w-12 h-12 text-[#D35400] mx-auto mb-3" />
                <p className="text-sm text-[#FFF8DC]/70 mb-1" style={{ fontFamily: '"Philosopher", sans-serif' }}>
                  Достижение
                </p>
                <p className="text-xl text-[#FFF8DC]" style={{ fontFamily: '"Cinzel", serif', fontWeight: 600 }}>
                  Находчивый ученик
                </p>
              </motion.div>
            </div>

            {/* Credits Section */}
            <motion.div
              className="border-t-2 border-[#D35400]/30 pt-6"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.9 }}
            >
              <h3 className="text-lg text-center text-[#D35400] mb-4" style={{ fontFamily: '"Cinzel", serif' }}>
                Создатели саги
              </h3>
              <div className="flex justify-center items-center gap-6 mb-4">
                <p className="text-[#FFF8DC]" style={{ fontFamily: '"Philosopher", sans-serif' }}>
                  Студия "Сказочный Путь"
                </p>
              </div>
              <div className="flex justify-center items-center gap-6">
                <button className="flex items-center gap-2 text-[#FFF8DC]/80 hover:text-[#D35400] transition-colors">
                  <Mail className="w-5 h-5" />
                  <span className="text-sm" style={{ fontFamily: '"Philosopher", sans-serif' }}>Написать отзыв</span>
                </button>
                <div className="flex gap-3">
                  <Twitter className="w-5 h-5 text-[#FFF8DC]/80 hover:text-[#D35400] transition-colors cursor-pointer" />
                  <Facebook className="w-5 h-5 text-[#FFF8DC]/80 hover:text-[#D35400] transition-colors cursor-pointer" />
                </div>
              </div>
            </motion.div>
          </div>
        </motion.div>

        {/* Action Buttons */}
        <motion.div
          className="flex gap-6"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 1.1 }}
        >
          <motion.button
            onClick={onContinue}
            className="px-12 py-4 rounded-lg flex items-center gap-3 group"
            style={{
              background: 'linear-gradient(135deg, #1B5E20 0%, #0f4016 100%)',
              border: '3px solid #D35400',
              boxShadow: '0 4px 20px rgba(211, 84, 0, 0.4)',
            }}
            whileHover={{ scale: 1.05, boxShadow: '0 6px 25px rgba(211, 84, 0, 0.6)' }}
            whileTap={{ scale: 0.98 }}
          >
            <span
              className="text-2xl"
              style={{
                fontFamily: '"Cinzel", serif',
                fontWeight: 700,
                color: '#FFF8DC',
              }}
            >
              Дальше в путь
            </span>
            <ArrowRight className="w-6 h-6 text-[#D35400] group-hover:translate-x-1 transition-transform" />
          </motion.button>

          <motion.button
            onClick={onMainMenu}
            className="px-8 py-4 rounded-lg"
            style={{
              background: 'linear-gradient(135deg, #5D4037 0%, #3d2b26 100%)',
              border: '2px solid #8B4513',
            }}
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.98 }}
          >
            <span
              className="text-xl flex items-center gap-2"
              style={{
                fontFamily: '"Cinzel", serif',
                fontWeight: 600,
                color: '#FFF8DC',
              }}
            >
              <Home className="w-5 h-5" />
              В главное меню
            </span>
          </motion.button>
        </motion.div>
      </div>
    </div>
  );
}
