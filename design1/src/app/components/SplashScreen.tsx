import { motion } from 'motion/react';
import { Settings, BookOpen } from 'lucide-react';
import { ImageWithFallback } from './figma/ImageWithFallback';

export function SplashScreen() {
  return (
    <div className="relative w-full h-full overflow-hidden bg-[#0a0e27]">
      {/* Background Image with Overlay */}
      <div className="absolute inset-0">
        <ImageWithFallback
          src="https://images.unsplash.com/photo-1704226443195-c4e2151e924c?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxteXN0aWNhbCUyMGZvcmVzdCUyMHBhdGglMjB0d2lsaWdodHxlbnwxfHx8fDE3Njc5MTYxOTB8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral"
          alt="Mystical Forest"
          className="w-full h-full object-cover"
        />
        {/* Dark Gradient Overlays for Mystery */}
        <div className="absolute inset-0 bg-gradient-to-b from-[#1a1d3a]/60 via-[#0f1829]/40 to-[#0a0e27]/80" />
        <div className="absolute inset-0 bg-gradient-to-r from-[#2d1b4e]/30 via-transparent to-[#1b3a4e]/30" />
      </div>

      {/* Magical Floating Lights */}
      {[...Array(12)].map((_, i) => (
        <motion.div
          key={i}
          className="absolute w-1 h-1 rounded-full bg-gradient-to-br from-[#F4E4C1] to-[#D4AF37]"
          style={{
            left: `${15 + Math.random() * 70}%`,
            top: `${20 + Math.random() * 60}%`,
            filter: 'blur(1px)',
            boxShadow: '0 0 8px 2px rgba(244, 228, 193, 0.6)',
          }}
          animate={{
            y: [0, -20, 0],
            opacity: [0.3, 0.8, 0.3],
            scale: [1, 1.5, 1],
          }}
          transition={{
            duration: 3 + Math.random() * 2,
            repeat: Infinity,
            delay: Math.random() * 2,
          }}
        />
      ))}

      {/* Cat Silhouette */}
      <motion.div
        className="absolute bottom-[25%] left-[15%] opacity-40"
        initial={{ opacity: 0 }}
        animate={{ opacity: 0.4 }}
        transition={{ delay: 1.5, duration: 2 }}
      >
        <svg width="60" height="50" viewBox="0 0 60 50" fill="none">
          <path
            d="M10 45 Q8 35 12 32 L14 28 Q15 20 18 18 L20 10 L22 14 Q25 8 30 8 Q35 8 38 14 L40 10 L42 18 Q45 20 46 28 L48 32 Q52 35 50 45 Q48 48 45 48 L15 48 Q12 48 10 45 Z"
            fill="#1a1d3a"
            opacity="0.8"
          />
        </svg>
      </motion.div>

      {/* Main Content Container */}
      <div className="relative z-10 flex flex-col items-center justify-between h-full px-12 py-16">
        
        {/* Title Section */}
        <motion.div
          className="flex flex-col items-center mt-12"
          initial={{ opacity: 0, y: -30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 1.2, ease: 'easeOut' }}
        >
          <h1 
            className="text-7xl text-center mb-4 tracking-wider"
            style={{
              fontFamily: '"Cinzel", serif',
              fontWeight: 900,
              background: 'linear-gradient(135deg, #F4E4C1 0%, #D4AF37 50%, #C9A961 100%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              backgroundClip: 'text',
              textShadow: '0 0 40px rgba(244, 228, 193, 0.4)',
              filter: 'drop-shadow(0 2px 8px rgba(212, 175, 55, 0.5))',
            }}
          >
            САГА АЙЛОПОРОСА
          </h1>
          
          {/* Decorative Line */}
          <motion.div
            className="w-64 h-0.5 bg-gradient-to-r from-transparent via-[#D4AF37] to-transparent mb-6"
            initial={{ scaleX: 0 }}
            animate={{ scaleX: 1 }}
            transition={{ delay: 0.8, duration: 1 }}
          />
          
          <motion.p
            className="text-[#A8DADC] text-xl tracking-wide italic opacity-90"
            style={{ fontFamily: '"Philosopher", sans-serif' }}
            initial={{ opacity: 0 }}
            animate={{ opacity: 0.9 }}
            transition={{ delay: 1, duration: 1.5 }}
          >
            Путь начинается там, где кончается карта
          </motion.p>
        </motion.div>

        {/* Center Magic Glow Effect */}
        <motion.div
          className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 rounded-full"
          style={{
            background: 'radial-gradient(circle, rgba(168, 218, 220, 0.1) 0%, transparent 70%)',
          }}
          animate={{
            scale: [1, 1.2, 1],
            opacity: [0.3, 0.5, 0.3],
          }}
          transition={{
            duration: 4,
            repeat: Infinity,
          }}
        />

        {/* Bottom Section with Button and Icons */}
        <motion.div
          className="flex flex-col items-center gap-8 mb-8"
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 1.2, duration: 1 }}
        >
          {/* Main Start Button */}
          <motion.button
            className="relative px-16 py-5 rounded-lg overflow-hidden group"
            style={{
              background: 'linear-gradient(135deg, #5c2e2e 0%, #3d1f1f 100%)',
              border: '2px solid #D4AF37',
              boxShadow: '0 0 20px rgba(212, 175, 55, 0.4), inset 0 1px 0 rgba(244, 228, 193, 0.2)',
            }}
            whileHover={{ scale: 1.05, boxShadow: '0 0 30px rgba(212, 175, 55, 0.6)' }}
            whileTap={{ scale: 0.98 }}
          >
            {/* Button Glow Effect */}
            <div className="absolute inset-0 bg-gradient-to-r from-transparent via-[#D4AF37]/20 to-transparent -translate-x-full group-hover:translate-x-full transition-transform duration-1000" />
            
            <span
              className="relative text-2xl tracking-widest"
              style={{
                fontFamily: '"Cinzel", serif',
                fontWeight: 700,
                color: '#F4E4C1',
                textShadow: '0 0 10px rgba(244, 228, 193, 0.5)',
              }}
            >
              НАЧАТЬ САГУ
            </span>
          </motion.button>

          {/* Secondary Icons */}
          <div className="flex gap-8 items-center">
            <motion.button
              className="flex items-center gap-2 px-4 py-2 rounded-md text-[#A8DADC] hover:text-[#D4AF37] transition-colors group"
              whileHover={{ scale: 1.1 }}
            >
              <Settings className="w-5 h-5" />
              <span className="text-sm" style={{ fontFamily: '"Philosopher", sans-serif' }}>
                Настройки
              </span>
            </motion.button>

            <div className="w-px h-6 bg-[#A8DADC]/30" />

            <motion.button
              className="flex items-center gap-2 px-4 py-2 rounded-md text-[#A8DADC] hover:text-[#D4AF37] transition-colors group"
              whileHover={{ scale: 1.1 }}
            >
              <BookOpen className="w-5 h-5" />
              <span className="text-sm" style={{ fontFamily: '"Philosopher", sans-serif' }}>
                О игре
              </span>
            </motion.button>
          </div>
        </motion.div>
      </div>

      {/* Vignette Effect */}
      <div className="absolute inset-0 pointer-events-none shadow-[inset_0_0_100px_rgba(0,0,0,0.7)]" />
    </div>
  );
}
