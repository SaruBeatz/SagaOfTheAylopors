import { ChevronRight, MapPin } from 'lucide-react';

interface ChapterTransitionScreenProps {
  fromChapter: number;
  toChapter: number;
  toChapterTitle: string;
  onContinue: () => void;
}

export function ChapterTransitionScreen({
  fromChapter,
  toChapter,
  toChapterTitle,
  onContinue
}: ChapterTransitionScreenProps) {
  return (
    <div className="relative w-full h-screen overflow-hidden">
      {/* Background - path/road scene */}
      <div 
        className="absolute inset-0 bg-cover bg-center"
        style={{
          backgroundImage: `url('https://images.unsplash.com/photo-1694100381966-5cf52917d452?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxhdXR1bW4lMjBmb3Jlc3QlMjBwYXRofGVufDF8fHx8MTc2Nzk3OTA3Mnww&ixlib=rb-4.1.0&q=80&w=1080')`,
        }}
      >
        <div className="absolute inset-0 bg-gradient-to-r from-[#5D4037]/90 via-[#5D4037]/70 to-[#5D4037]/90" />
      </div>

      {/* Content */}
      <div className="relative z-10 h-full flex flex-col items-center justify-center px-12 py-12">
        {/* Characters on the path */}
        <div className="mb-12 flex items-center gap-8">
          {/* Cat character */}
          <div className="text-center transform transition-all duration-700 animate-[slideInLeft_1s_ease-out]">
            <div className="w-32 h-32 bg-[#D35400] border-6 border-[#F39C12] rounded-full flex items-center justify-center shadow-2xl">
              <span className="text-7xl">🐱</span>
            </div>
            <p className="text-lg text-[#F39C12] mt-3" style={{ fontFamily: 'Georgia, serif' }}>
              Кот в сапогах
            </p>
          </div>

          {/* Arrow/footprints between characters */}
          <div className="flex items-center gap-2">
            {[1, 2, 3, 4, 5].map((i) => (
              <div 
                key={i}
                className="text-[#F39C12] opacity-60 animate-pulse"
                style={{ 
                  animationDelay: `${i * 0.2}s`,
                  fontSize: '24px'
                }}
              >
                🐾
              </div>
            ))}
          </div>

          {/* Marquis character */}
          <div className="text-center transform transition-all duration-700 animate-[slideInRight_1s_ease-out]">
            <div className="w-32 h-32 bg-[#5D4037] border-6 border-[#F39C12] rounded-full flex items-center justify-center shadow-2xl">
              <span className="text-7xl">👤</span>
            </div>
            <p className="text-lg text-[#F39C12] mt-3" style={{ fontFamily: 'Georgia, serif' }}>
              Маркиз
            </p>
          </div>
        </div>

        {/* Progress indicator */}
        <div className="mb-12 w-full max-w-3xl">
          <div className="bg-[#E8DCC8] border-6 border-[#5D4037] p-8 shadow-2xl">
            <div className="flex items-center justify-center gap-8">
              {/* From chapter */}
              <div className="flex flex-col items-center">
                <div className="w-20 h-20 bg-[#1B5E20] border-4 border-[#F39C12] rounded-full flex items-center justify-center mb-3 shadow-lg">
                  <span className="text-3xl">✓</span>
                </div>
                <div className="text-center">
                  <p className="text-sm text-[#5D4037] mb-1" style={{ fontFamily: 'Georgia, serif' }}>
                    Завершена
                  </p>
                  <p className="text-2xl text-[#5D4037]" style={{ fontFamily: 'Georgia, serif' }}>
                    Глава {fromChapter}
                  </p>
                </div>
              </div>

              {/* Path between chapters */}
              <div className="flex-1 relative h-2 bg-[#5D4037] mx-4">
                <div className="absolute inset-0 bg-[#D35400] animate-[fillPath_2s_ease-out]"></div>
                {/* Animated paw prints */}
                <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2">
                  <span className="text-2xl animate-bounce">🐾</span>
                </div>
              </div>

              {/* To chapter */}
              <div className="flex flex-col items-center">
                <div className="w-20 h-20 bg-[#D35400] border-4 border-[#F39C12] rounded-full flex items-center justify-center mb-3 shadow-lg animate-pulse">
                  <MapPin className="w-10 h-10 text-[#F39C12]" />
                </div>
                <div className="text-center">
                  <p className="text-sm text-[#5D4037] mb-1" style={{ fontFamily: 'Georgia, serif' }}>
                    Впереди
                  </p>
                  <p className="text-2xl text-[#D35400]" style={{ fontFamily: 'Georgia, serif' }}>
                    Глава {toChapter}
                  </p>
                </div>
              </div>
            </div>

            {/* Decorative corners */}
            <div className="absolute top-2 left-2 w-6 h-6 border-t-4 border-l-4 border-[#D35400]"></div>
            <div className="absolute top-2 right-2 w-6 h-6 border-t-4 border-r-4 border-[#D35400]"></div>
            <div className="absolute bottom-2 left-2 w-6 h-6 border-b-4 border-l-4 border-[#D35400]"></div>
            <div className="absolute bottom-2 right-2 w-6 h-6 border-b-4 border-r-4 border-[#D35400]"></div>
          </div>
        </div>

        {/* Motivational text on stone/sign */}
        <div className="mb-12">
          <div className="relative inline-block">
            {/* Wooden sign background */}
            <div className="bg-[#5D4037] border-4 border-[#F39C12] px-16 py-6 shadow-2xl transform rotate-1">
              <p className="text-3xl text-[#F39C12] text-center italic" style={{ 
                fontFamily: 'Georgia, serif',
                textShadow: '2px 2px 4px rgba(0,0,0,0.6)'
              }}>
                За каждым поворотом — новая история
              </p>
            </div>
            {/* Post */}
            <div className="absolute -bottom-16 left-1/2 transform -translate-x-1/2 w-4 h-16 bg-[#5D4037]"></div>
          </div>
        </div>

        {/* Continue button - styled as gate/portal */}
        <button
          onClick={onContinue}
          className="relative group"
        >
          <div className="bg-[#D35400] hover:bg-[#F39C12] border-8 border-[#F39C12] px-20 py-8 transition-all duration-300 transform group-hover:scale-105 shadow-2xl">
            <div className="flex items-center gap-6">
              <span className="text-4xl" style={{ fontFamily: 'Georgia, serif', color: 'white' }}>
                Вступить в Главу {toChapter}
              </span>
              <ChevronRight className="w-10 h-10 text-white group-hover:translate-x-2 transition-transform" />
            </div>
            <p className="text-xl text-[#E8DCC8] text-center mt-2" style={{ fontFamily: 'Georgia, serif' }}>
              {toChapterTitle}
            </p>
          </div>
          {/* Gate pillars */}
          <div className="absolute -left-8 top-0 bottom-0 w-6 bg-[#5D4037] border-2 border-[#F39C12]"></div>
          <div className="absolute -right-8 top-0 bottom-0 w-6 bg-[#5D4037] border-2 border-[#F39C12]"></div>
        </button>
      </div>

      {/* Fog effect at bottom */}
      <div className="absolute bottom-0 left-0 right-0 h-32 bg-gradient-to-t from-[#5D4037] to-transparent pointer-events-none"></div>
    </div>
  );
}
