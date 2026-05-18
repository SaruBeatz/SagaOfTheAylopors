import { ChevronRight } from 'lucide-react';

interface SplashScreenProps {
  onStart: () => void;
}

export function SplashScreen({ onStart }: SplashScreenProps) {
  return (
    <div className="relative w-full h-screen overflow-hidden bg-[#1B5E20]">
      {/* Background Image */}
      <div 
        className="absolute inset-0 bg-cover bg-center"
        style={{
          backgroundImage: `url('https://images.unsplash.com/photo-1694100381966-5cf52917d452?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxhdXR1bW4lMjBmb3Jlc3QlMjBwYXRofGVufDF8fHx8MTc2Nzk3OTA3Mnww&ixlib=rb-4.1.0&q=80&w=1080')`,
        }}
      >
        <div className="absolute inset-0 bg-gradient-to-t from-[#5D4037]/90 via-[#5D4037]/50 to-transparent" />
      </div>

      {/* Content */}
      <div className="relative z-10 h-full flex flex-col items-center justify-between px-12 py-16">
        {/* Top Section - Logo/Title */}
        <div className="flex-1 flex items-center justify-center">
          <div className="text-center">
            {/* Cat silhouette */}
            <div className="mb-8 flex justify-center">
              <svg className="w-32 h-32" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M50 80 C30 80 20 65 20 50 C20 35 25 25 35 20 L38 15 L42 20 C50 18 55 15 60 15 L63 20 L65 20 C75 25 80 35 80 50 C80 65 70 80 50 80 Z" fill="#D35400" stroke="#F39C12" strokeWidth="2"/>
                <circle cx="40" cy="45" r="3" fill="#283593"/>
                <circle cx="60" cy="45" r="3" fill="#283593"/>
                <path d="M45 55 Q50 58 55 55" stroke="#5D4037" strokeWidth="2" fill="none"/>
                <path d="M35 40 L25 35" stroke="#5D4037" strokeWidth="2"/>
                <path d="M65 40 L75 35" stroke="#5D4037" strokeWidth="2"/>
                {/* Hat */}
                <ellipse cx="50" cy="25" rx="20" ry="8" fill="#5D4037" stroke="#F39C12" strokeWidth="1.5"/>
                <path d="M35 25 L35 18 L65 18 L65 25" fill="#5D4037"/>
                <path d="M60 18 L62 8 Q63 5 65 8 L63 18" fill="#F39C12"/>
              </svg>
            </div>

            {/* Title on wooden sign */}
            <div className="relative inline-block">
              <div className="bg-[#5D4037] border-4 border-[#F39C12] px-16 py-8 transform -rotate-1 shadow-2xl">
                <h1 className="text-6xl tracking-wider mb-2" style={{ 
                  color: '#F39C12',
                  fontFamily: 'Georgia, serif',
                  textShadow: '3px 3px 6px rgba(0,0,0,0.8)'
                }}>
                  САГА
                </h1>
                <h2 className="text-4xl tracking-widest" style={{ 
                  color: '#D35400',
                  fontFamily: 'Georgia, serif',
                  textShadow: '2px 2px 4px rgba(0,0,0,0.8)'
                }}>
                  АЙЛОПОРОСА
                </h2>
              </div>
              {/* Rope decoration */}
              <div className="absolute -top-8 left-1/2 transform -translate-x-1/2 w-1 h-8 bg-[#5D4037]"></div>
            </div>
          </div>
        </div>

        {/* Bottom Section - Start Button */}
        <div className="w-full max-w-md">
          {/* Road signs decoration */}
          <div className="flex justify-center gap-8 mb-8 opacity-60">
            <div className="text-center">
              <div className="text-[#F39C12] text-sm mb-1">← Тёмный лес</div>
              <div className="w-2 h-16 bg-[#5D4037] mx-auto"></div>
            </div>
            <div className="text-center">
              <div className="text-[#F39C12] text-sm mb-1">Замок →</div>
              <div className="w-2 h-16 bg-[#5D4037] mx-auto"></div>
            </div>
          </div>

          {/* Start Button */}
          <button
            onClick={onStart}
            className="w-full bg-[#D35400] hover:bg-[#F39C12] border-4 border-[#F39C12] text-white py-6 px-8 text-2xl tracking-wide transition-all duration-300 transform hover:scale-105 shadow-xl flex items-center justify-center gap-4"
            style={{ fontFamily: 'Georgia, serif' }}
          >
            Начать авантюру
            <ChevronRight className="w-8 h-8" />
          </button>
        </div>
      </div>

      {/* Decorative elements */}
      <div className="absolute bottom-0 left-0 w-full h-32 bg-gradient-to-t from-[#5D4037] to-transparent pointer-events-none"></div>
    </div>
  );
}
