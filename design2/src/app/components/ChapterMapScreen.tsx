import { ChevronRight, MapPin, Lock, Flag } from 'lucide-react';
import { motion } from 'motion/react';

interface ChapterMapScreenProps {
  onStartChapter: (chapter: number) => void;
  completedChapters?: number[];
  currentChapter?: number;
}

const chapters = [
  { number: 1, title: 'Наследство Мельника', x: 15, y: 75 },
  { number: 2, title: 'План хитреца', x: 28, y: 60 },
  { number: 3, title: 'Встреча с королём', x: 45, y: 50 },
  { number: 4, title: 'Замок людоеда', x: 60, y: 35 },
  { number: 5, title: 'Превращения', x: 72, y: 45 },
  { number: 6, title: 'Триумф', x: 85, y: 30 },
  { number: 7, title: 'Эпилог', x: 88, y: 15 },
];

export function ChapterMapScreen({ 
  onStartChapter, 
  completedChapters = [], 
  currentChapter = 1 
}: ChapterMapScreenProps) {
  const isCompleted = (num: number) => completedChapters.includes(num);
  const isUnlocked = (num: number) => num <= currentChapter;
  const isCurrent = (num: number) => num === currentChapter;

  return (
    <div className="relative w-full h-screen overflow-hidden">
      {/* Background */}
      <div 
        className="absolute inset-0 bg-cover bg-center"
        style={{
          backgroundImage: `url('https://images.unsplash.com/photo-1618385418700-35dc948cdeec?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxvbGQlMjB0cmVhc3VyZSUyMG1hcCUyMHBhcmNobWVudHxlbnwxfHx8fDE3Njc5NzY4MzJ8MA&ixlib=rb-4.1.0&q=80&w=1080')`,
        }}
      >
        <div className="absolute inset-0 bg-[#E8DCC8]/95" />
      </div>

      {/* Content */}
      <div className="relative z-10 h-full flex flex-col px-12 py-12">
        {/* Title */}
        <div className="text-center mb-8">
          <h2 className="text-5xl text-[#5D4037] mb-2" style={{ fontFamily: 'Georgia, serif' }}>
            Карта Саги
          </h2>
          <p className="text-xl text-[#D35400]" style={{ fontFamily: 'Georgia, serif' }}>
            Путь семи приключений
          </p>
        </div>

        {/* Map Area */}
        <div className="flex-1 relative border-8 border-[#5D4037] bg-[#F5E6D3] shadow-2xl">
          {/* Map decorations */}
          <div className="absolute inset-0 opacity-10">
            <svg className="w-full h-full">
              <pattern id="grid" width="40" height="40" patternUnits="userSpaceOnUse">
                <path d="M 40 0 L 0 0 0 40" fill="none" stroke="#5D4037" strokeWidth="0.5"/>
              </pattern>
              <rect width="100%" height="100%" fill="url(#grid)" />
            </svg>
          </div>

          {/* Decorative compass */}
          <div className="absolute top-6 right-6 w-24 h-24 opacity-60">
            <svg viewBox="0 0 100 100" className="w-full h-full">
              <circle cx="50" cy="50" r="45" fill="none" stroke="#5D4037" strokeWidth="2"/>
              <circle cx="50" cy="50" r="35" fill="none" stroke="#D35400" strokeWidth="1"/>
              <path d="M50 15 L55 45 L50 50 L45 45 Z" fill="#D35400"/>
              <path d="M50 85 L45 55 L50 50 L55 55 Z" fill="#5D4037"/>
              <text x="50" y="12" textAnchor="middle" fill="#5D4037" fontSize="12" fontFamily="Georgia">С</text>
            </svg>
          </div>

          {/* Path connecting chapters - with animation */}
          <svg className="absolute inset-0 w-full h-full pointer-events-none">
            <defs>
              <marker id="arrowhead" markerWidth="10" markerHeight="10" refX="5" refY="5" orient="auto">
                <polygon points="0 0, 10 5, 0 10" fill="#5D4037" opacity="0.5" />
              </marker>
            </defs>
            {chapters.slice(0, -1).map((chapter, i) => {
              const next = chapters[i + 1];
              const isPathCompleted = isCompleted(chapter.number);
              const isPathActive = chapter.number < currentChapter;
              
              return (
                <g key={i}>
                  {/* Base path line */}
                  <motion.line
                    x1={`${chapter.x}%`}
                    y1={`${chapter.y}%`}
                    x2={`${next.x}%`}
                    y2={`${next.y}%`}
                    stroke={isPathActive ? "#D35400" : "#5D4037"}
                    strokeWidth="3"
                    strokeDasharray="8,4"
                    opacity={isPathActive ? "0.8" : "0.5"}
                    markerEnd="url(#arrowhead)"
                    initial={{ pathLength: 0 }}
                    animate={{ pathLength: isPathActive ? 1 : 0 }}
                    transition={{ duration: 1.5, delay: i * 0.3 }}
                  />
                  
                  {/* Animated paw prints on active path */}
                  {isPathActive && (
                    <motion.g
                      initial={{ opacity: 0 }}
                      animate={{ opacity: [0, 1, 0] }}
                      transition={{ 
                        duration: 2, 
                        repeat: Infinity,
                        delay: i * 0.3 
                      }}
                    >
                      <circle
                        cx={`${chapter.x + (next.x - chapter.x) * 0.33}%`}
                        cy={`${chapter.y + (next.y - chapter.y) * 0.33}%`}
                        r="4"
                        fill="#F39C12"
                      />
                      <circle
                        cx={`${chapter.x + (next.x - chapter.x) * 0.66}%`}
                        cy={`${chapter.y + (next.y - chapter.y) * 0.66}%`}
                        r="4"
                        fill="#F39C12"
                      />
                    </motion.g>
                  )}
                </g>
              );
            })}
          </svg>

          {/* Chapter markers */}
          {chapters.map((chapter) => {
            const completed = isCompleted(chapter.number);
            const unlocked = isUnlocked(chapter.number);
            const current = isCurrent(chapter.number);
            
            return (
              <motion.div
                key={chapter.number}
                className="absolute transform -translate-x-1/2 -translate-y-1/2"
                style={{ left: `${chapter.x}%`, top: `${chapter.y}%` }}
                initial={{ scale: 0, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                transition={{ 
                  duration: 0.5, 
                  delay: chapter.number * 0.2,
                  type: "spring",
                  stiffness: 200
                }}
              >
                <button
                  onClick={() => unlocked && onStartChapter(chapter.number)}
                  disabled={!unlocked}
                  className={`flex flex-col items-center gap-2 transition-all duration-300 ${
                    unlocked 
                      ? 'hover:scale-110 cursor-pointer' 
                      : 'opacity-50 cursor-not-allowed'
                  }`}
                >
                  {/* Chapter icon */}
                  <motion.div 
                    className={`w-20 h-20 rounded-full flex items-center justify-center border-4 shadow-lg relative ${
                      completed
                        ? 'bg-[#1B5E20] border-[#F39C12] shadow-[#F39C12]/50'
                        : current
                        ? 'bg-[#D35400] border-[#F39C12] shadow-[#F39C12]/50'
                        : unlocked
                        ? 'bg-[#5D4037] border-[#D35400]'
                        : 'bg-[#5D4037] border-[#5D4037]'
                    }`}
                    animate={current ? {
                      scale: [1, 1.1, 1],
                      boxShadow: [
                        '0 0 0 0 rgba(243, 156, 18, 0)',
                        '0 0 0 10px rgba(243, 156, 18, 0.3)',
                        '0 0 0 0 rgba(243, 156, 18, 0)'
                      ]
                    } : {}}
                    transition={current ? {
                      duration: 2,
                      repeat: Infinity,
                      ease: "easeInOut"
                    } : {}}
                  >
                    {completed ? (
                      <>
                        <Flag className="w-10 h-10 text-[#F39C12]" />
                        {/* Checkmark badge */}
                        <div className="absolute -top-2 -right-2 w-8 h-8 bg-[#F39C12] border-2 border-[#1B5E20] rounded-full flex items-center justify-center">
                          <span className="text-[#1B5E20] text-lg">✓</span>
                        </div>
                      </>
                    ) : current ? (
                      <MapPin className="w-10 h-10 text-[#F39C12]" />
                    ) : unlocked ? (
                      <MapPin className="w-10 h-10 text-[#D35400]" />
                    ) : (
                      <Lock className="w-8 h-8 text-[#E8DCC8]" />
                    )}
                  </motion.div>

                  {/* Chapter number */}
                  <div 
                    className={`w-12 h-12 rounded-full flex items-center justify-center border-2 text-xl ${
                      completed
                        ? 'bg-[#1B5E20] border-[#F39C12] text-[#F39C12]'
                        : current
                        ? 'bg-[#F39C12] border-[#D35400] text-[#5D4037]'
                        : unlocked
                        ? 'bg-[#D35400] border-[#F39C12] text-[#F39C12]'
                        : 'bg-[#5D4037] border-[#E8DCC8]/50 text-[#E8DCC8]'
                    }`}
                    style={{ fontFamily: 'Georgia, serif' }}
                  >
                    {chapter.number}
                  </div>

                  {/* Chapter title */}
                  {unlocked && (
                    <div className="max-w-32 text-center">
                      <div 
                        className={`border-2 px-3 py-1 text-sm shadow-md ${
                          completed
                            ? 'bg-[#1B5E20] border-[#F39C12] text-[#F39C12]'
                            : current
                            ? 'bg-[#F39C12] border-[#D35400] text-[#5D4037]'
                            : 'bg-[#E8DCC8] border-[#5D4037] text-[#5D4037]'
                        }`}
                        style={{ fontFamily: 'Georgia, serif' }}
                      >
                        {chapter.title}
                      </div>
                    </div>
                  )}
                </button>

                {/* Animated footprint trail for current chapter */}
                {current && (
                  <motion.div
                    className="absolute -bottom-8 left-1/2 transform -translate-x-1/2"
                    animate={{ 
                      y: [0, 10, 0],
                      opacity: [0.5, 1, 0.5]
                    }}
                    transition={{
                      duration: 1.5,
                      repeat: Infinity,
                      ease: "easeInOut"
                    }}
                  >
                    <span className="text-2xl">🐾</span>
                  </motion.div>
                )}
              </motion.div>
            );
          })}

          {/* Decorative elements */}
          <div className="absolute bottom-6 left-6 text-[#5D4037] opacity-40" style={{ fontFamily: 'Georgia, serif' }}>
            <div className="text-xs">Карта составлена в год</div>
            <div className="text-xs">Великих Авантюр</div>
          </div>

          {/* Legend */}
          <div className="absolute bottom-6 right-6 bg-[#E8DCC8] border-2 border-[#5D4037] p-4 opacity-80">
            <div className="space-y-2 text-xs" style={{ fontFamily: 'Georgia, serif' }}>
              <div className="flex items-center gap-2">
                <div className="w-4 h-4 bg-[#1B5E20] border border-[#F39C12] rounded-full"></div>
                <span className="text-[#5D4037]">Завершено</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-4 h-4 bg-[#D35400] border border-[#F39C12] rounded-full"></div>
                <span className="text-[#5D4037]">Текущая</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-4 h-4 bg-[#5D4037] border border-[#5D4037] rounded-full"></div>
                <span className="text-[#5D4037]">Закрыто</span>
              </div>
            </div>
          </div>
        </div>

        {/* Start Button */}
        <div className="mt-8 max-w-2xl mx-auto w-full">
          <button
            onClick={() => onStartChapter(currentChapter)}
            className="w-full bg-[#D35400] hover:bg-[#F39C12] border-4 border-[#F39C12] text-white py-5 px-8 text-2xl transition-all duration-300 transform hover:scale-105 shadow-xl flex items-center justify-center gap-4"
            style={{ fontFamily: 'Georgia, serif' }}
          >
            {currentChapter === 1 ? 'Начать главу I' : `Продолжить главу ${currentChapter}`}
            <ChevronRight className="w-8 h-8" />
          </button>
        </div>
      </div>
    </div>
  );
}
