import { motion, AnimatePresence } from 'motion/react';
import { ArrowRight, Flag, Sparkles } from 'lucide-react';
import { ImageWithFallback } from './figma/ImageWithFallback';
import { useState, useEffect } from 'react';

interface ChapterTransitionProps {
  onStartChapter: () => void;
}

export function ChapterTransition({ onStartChapter }: ChapterTransitionProps) {
  const [animationState, setAnimationState] = useState<'initial' | 'completed'>('initial');

  useEffect(() => {
    // Auto-transition to completed state after 2 seconds
    const timer = setTimeout(() => {
      setAnimationState('completed');
    }, 2000);
    return () => clearTimeout(timer);
  }, []);

  return (
    <div className="relative w-full h-full overflow-hidden bg-[#1a1410]">
      {/* Background */}
      <div className="absolute inset-0">
        <ImageWithFallback
          src="https://images.unsplash.com/photo-1690536599932-1b4098f5100b?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtZWRpZXZhbCUyMGNhc3RsZSUyMGRpc3RhbmNlfGVufDF8fHx8MTc2Nzk4MTczOXww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral"
          alt="Journey Path"
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-b from-[#1B5E20]/40 via-[#5D4037]/50 to-[#1a1410]/90" />
      </div>

      {/* Content */}
      <div className="relative z-10 h-full flex flex-col items-center justify-between px-16 py-12">
        
        {/* Title */}
        <motion.div
          className="text-center mt-12"
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
        >
          <h2
            className="text-5xl"
            style={{
              fontFamily: '"Cinzel", serif',
              fontWeight: 700,
              color: '#D35400',
              textShadow: '0 0 20px rgba(211, 84, 0, 0.5)',
            }}
          >
            Путешествие продолжается
          </h2>
        </motion.div>

        {/* Journey Map */}
        <div className="flex-1 flex items-center justify-center w-full max-w-5xl">
          <motion.div
            className="relative w-full"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.3 }}
          >
            {/* Map Container */}
            <svg width="100%" height="300" viewBox="0 0 800 300" className="overflow-visible">
              {/* Path Line (Dotted/Dashed) */}
              <motion.path
                d="M 150 150 Q 300 100, 450 150 Q 600 200, 650 150"
                stroke="#8B4513"
                strokeWidth="3"
                fill="none"
                strokeDasharray="10,5"
                opacity="0.5"
              />

              {/* Animated Connecting Path - only visible in completed state */}
              <AnimatePresence>
                {animationState === 'completed' && (
                  <motion.path
                    d="M 150 150 Q 300 100, 450 150 Q 600 200, 650 150"
                    stroke="#D35400"
                    strokeWidth="4"
                    fill="none"
                    initial={{ pathLength: 0, opacity: 0 }}
                    animate={{ pathLength: 1, opacity: 1 }}
                    exit={{ opacity: 0 }}
                    transition={{ duration: 2, ease: "easeInOut" }}
                    style={{
                      filter: 'drop-shadow(0 0 8px rgba(211, 84, 0, 0.8))',
                    }}
                  />
                )}
              </AnimatePresence>

              {/* Paw Print Trail - appears with animation */}
              <AnimatePresence>
                {animationState === 'completed' && (
                  <>
                    {[0, 0.2, 0.4, 0.6, 0.8].map((offset, idx) => {
                      const x = 150 + (500 * offset);
                      const y = 150 + Math.sin(offset * Math.PI * 2) * (offset < 0.5 ? -50 : 50);
                      
                      return (
                        <motion.g
                          key={idx}
                          initial={{ opacity: 0, scale: 0 }}
                          animate={{ opacity: 0.6, scale: 1 }}
                          transition={{ delay: 0.3 + idx * 0.3, duration: 0.5 }}
                        >
                          {/* Main pad */}
                          <ellipse cx={x} cy={y} rx="8" ry="10" fill="#D35400" opacity="0.7" />
                          {/* Toe pads */}
                          <circle cx={x - 6} cy={y - 8} r="3" fill="#D35400" opacity="0.7" />
                          <circle cx={x} cy={y - 10} r="3" fill="#D35400" opacity="0.7" />
                          <circle cx={x + 6} cy={y - 8} r="3" fill="#D35400" opacity="0.7" />
                        </motion.g>
                      );
                    })}
                  </>
                )}
              </AnimatePresence>

              {/* Chapter I Marker */}
              <motion.g
                initial={{ opacity: 0, scale: 0 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: 0.5, type: "spring", stiffness: 200 }}
              >
                {/* Base circle */}
                <circle 
                  cx="150" 
                  cy="150" 
                  r="35" 
                  fill="#5D4037"
                  stroke={animationState === 'initial' ? '#D35400' : '#8B4513'}
                  strokeWidth="4"
                />
                
                {/* Chapter Number */}
                <text
                  x="150"
                  y="160"
                  textAnchor="middle"
                  style={{
                    fontFamily: '"Cinzel", serif',
                    fontSize: '28px',
                    fontWeight: 700,
                    fill: '#FFF8DC',
                  }}
                >
                  I
                </text>

                {/* Completion Flag - only in completed state */}
                <AnimatePresence>
                  {animationState === 'completed' && (
                    <motion.g
                      initial={{ opacity: 0, y: -10, scale: 0 }}
                      animate={{ opacity: 1, y: 0, scale: 1 }}
                      transition={{ delay: 0.5, type: "spring" }}
                    >
                      <Flag
                        x="130"
                        y="105"
                        width="20"
                        height="20"
                        stroke="#1B5E20"
                        fill="#1B5E20"
                        strokeWidth="2"
                      />
                    </motion.g>
                  )}
                </AnimatePresence>
              </motion.g>

              {/* Chapter II Marker */}
              <motion.g
                initial={{ opacity: 0, scale: 0 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: 0.7, type: "spring", stiffness: 200 }}
              >
                {/* Base circle */}
                <circle 
                  cx="650" 
                  cy="150" 
                  r="35" 
                  fill="#5D4037"
                  stroke="#D35400"
                  strokeWidth="4"
                />
                
                {/* Pulsing glow effect - only in completed state */}
                <AnimatePresence>
                  {animationState === 'completed' && (
                    <motion.circle
                      cx="650"
                      cy="150"
                      r="35"
                      fill="none"
                      stroke="#D35400"
                      strokeWidth="3"
                      initial={{ scale: 1, opacity: 0.8 }}
                      animate={{ 
                        scale: [1, 1.5, 1],
                        opacity: [0.8, 0, 0.8],
                      }}
                      transition={{
                        duration: 2,
                        repeat: Infinity,
                        ease: "easeInOut"
                      }}
                    />
                  )}
                </AnimatePresence>

                {/* Chapter Number */}
                <text
                  x="650"
                  y="160"
                  textAnchor="middle"
                  style={{
                    fontFamily: '"Cinzel", serif',
                    fontSize: '28px',
                    fontWeight: 700,
                    fill: animationState === 'completed' ? '#D35400' : '#FFF8DC',
                  }}
                >
                  <motion.tspan
                    animate={animationState === 'completed' ? {
                      filter: ['drop-shadow(0 0 0px rgba(211, 84, 0, 0))', 'drop-shadow(0 0 10px rgba(211, 84, 0, 1))', 'drop-shadow(0 0 0px rgba(211, 84, 0, 0))']
                    } : {}}
                    transition={{ duration: 2, repeat: Infinity }}
                  >
                    II
                  </motion.tspan>
                </text>

                {/* Sparkle effect - only in completed state */}
                <AnimatePresence>
                  {animationState === 'completed' && (
                    <>
                      {[0, 120, 240].map((angle, idx) => {
                        const rad = (angle * Math.PI) / 180;
                        const distance = 50;
                        return (
                          <motion.circle
                            key={idx}
                            cx={650 + Math.cos(rad) * distance}
                            cy={150 + Math.sin(rad) * distance}
                            r="3"
                            fill="#D35400"
                            initial={{ opacity: 0, scale: 0 }}
                            animate={{ 
                              opacity: [0, 1, 0],
                              scale: [0, 1, 0],
                            }}
                            transition={{
                              duration: 1.5,
                              repeat: Infinity,
                              delay: idx * 0.2,
                            }}
                          />
                        );
                      })}
                    </>
                  )}
                </AnimatePresence>
              </motion.g>

              {/* Chapter Labels */}
              <text
                x="150"
                y="210"
                textAnchor="middle"
                style={{
                  fontFamily: '"Philosopher", sans-serif',
                  fontSize: '16px',
                  fill: '#FFF8DC',
                  opacity: 0.8,
                }}
              >
                Мельница
              </text>
              
              <text
                x="650"
                y="210"
                textAnchor="middle"
                style={{
                  fontFamily: '"Philosopher", sans-serif',
                  fontSize: '16px',
                  fill: '#D35400',
                  fontWeight: 600,
                }}
              >
                Лес Людоеда
              </text>
            </svg>
          </motion.div>
        </div>

        {/* Motivational Quote */}
        <motion.div
          className="text-center mb-8"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 1.5 }}
        >
          <div
            className="inline-block px-8 py-4 rounded-lg"
            style={{
              background: 'rgba(93, 64, 55, 0.6)',
              border: '2px solid #D35400',
            }}
          >
            <p
              className="text-xl italic text-[#FFF8DC]"
              style={{ fontFamily: '"Philosopher", sans-serif' }}
            >
              "За каждым поворотом — новая история"
            </p>
          </div>
        </motion.div>

        {/* Start Button */}
        <motion.button
          onClick={onStartChapter}
          className="px-16 py-5 rounded-lg flex items-center gap-4 group mb-8"
          style={{
            background: 'linear-gradient(135deg, #1B5E20 0%, #0f4016 100%)',
            border: '3px solid #D35400',
            boxShadow: '0 6px 25px rgba(211, 84, 0, 0.5)',
          }}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 2 }}
          whileHover={{ scale: 1.05, boxShadow: '0 8px 30px rgba(211, 84, 0, 0.7)' }}
          whileTap={{ scale: 0.98 }}
        >
          <Sparkles className="w-7 h-7 text-[#D35400]" />
          <span
            className="text-3xl"
            style={{
              fontFamily: '"Cinzel", serif',
              fontWeight: 700,
              color: '#FFF8DC',
            }}
          >
            Вступить в Главу II
          </span>
          <ArrowRight className="w-7 h-7 text-[#D35400] group-hover:translate-x-2 transition-transform" />
        </motion.button>
      </div>
    </div>
  );
}
