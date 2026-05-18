import { Clock, TrendingUp, Award, Users, Mail, ChevronRight, Home } from 'lucide-react';

interface ChapterSummaryScreenProps {
  chapterNumber: number;
  chapterTitle: string;
  playTime: string;
  choicesMade: number;
  achievement?: string;
  onContinue: () => void;
  onMainMenu: () => void;
}

export function ChapterSummaryScreen({
  chapterNumber,
  chapterTitle,
  playTime,
  choicesMade,
  achievement,
  onContinue,
  onMainMenu
}: ChapterSummaryScreenProps) {
  return (
    <div className="relative w-full h-screen overflow-hidden">
      {/* Background - cozy scene */}
      <div 
        className="absolute inset-0 bg-cover bg-center"
        style={{
          backgroundImage: `url('https://images.unsplash.com/photo-1678574420972-2b59628f904c?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtZWRpZXZhbCUyMGNhYmluJTIwaW50ZXJpb3IlMjB0YWJsZSUyMHNjcm9sbHN8ZW58MXx8fHwxNzY3OTc5MDY3fDA&ixlib=rb-4.1.0&q=80&w=1080')`,
        }}
      >
        <div className="absolute inset-0 bg-gradient-to-b from-[#5D4037]/80 via-[#5D4037]/85 to-[#5D4037]/90" />
      </div>

      {/* Candlelight effect */}
      <div className="absolute top-10 right-20 w-32 h-32 bg-[#F39C12] rounded-full blur-3xl opacity-30 animate-pulse"></div>
      <div className="absolute top-20 left-20 w-24 h-24 bg-[#F39C12] rounded-full blur-2xl opacity-20 animate-pulse"></div>

      {/* Content */}
      <div className="relative z-10 h-full flex flex-col px-12 py-10">
        {/* Title */}
        <div className="text-center mb-8">
          <h2 className="text-5xl text-[#F39C12] mb-3" style={{ 
            fontFamily: 'Georgia, serif',
            textShadow: '3px 3px 6px rgba(0,0,0,0.8)'
          }}>
            Глава {chapterNumber}: {chapterTitle}
          </h2>
          <div className="w-64 h-1 bg-[#D35400] mx-auto"></div>
        </div>

        {/* Main content area */}
        <div className="flex-1 flex gap-8">
          {/* Left side - Statistics manuscript */}
          <div className="w-2/3">
            <div className="bg-[#E8DCC8] border-8 border-[#5D4037] shadow-2xl p-10 h-full">
              {/* Decorative header */}
              <div className="text-center mb-8">
                <div className="inline-block bg-[#5D4037] border-2 border-[#F39C12] px-8 py-3">
                  <h3 className="text-3xl text-[#F39C12]" style={{ fontFamily: 'Georgia, serif' }}>
                    Путь завершён
                  </h3>
                </div>
              </div>

              {/* Statistics */}
              <div className="space-y-6 mb-10">
                {/* Time played */}
                <div className="bg-[#F5E6D3] border-4 border-[#5D4037] p-6 flex items-center gap-6">
                  <div className="w-16 h-16 bg-[#D35400] border-4 border-[#F39C12] rounded-full flex items-center justify-center flex-shrink-0">
                    <Clock className="w-8 h-8 text-[#F39C12]" />
                  </div>
                  <div className="flex-1">
                    <p className="text-xl text-[#5D4037] mb-1" style={{ fontFamily: 'Georgia, serif' }}>
                      В пути:
                    </p>
                    <p className="text-3xl text-[#D35400]" style={{ fontFamily: 'Georgia, serif' }}>
                      {playTime}
                    </p>
                  </div>
                </div>

                {/* Choices made */}
                <div className="bg-[#F5E6D3] border-4 border-[#5D4037] p-6 flex items-center gap-6">
                  <div className="w-16 h-16 bg-[#D35400] border-4 border-[#F39C12] rounded-full flex items-center justify-center flex-shrink-0">
                    <TrendingUp className="w-8 h-8 text-[#F39C12]" />
                  </div>
                  <div className="flex-1">
                    <p className="text-xl text-[#5D4037] mb-1" style={{ fontFamily: 'Georgia, serif' }}>
                      Сделано выборов:
                    </p>
                    <p className="text-3xl text-[#D35400]" style={{ fontFamily: 'Georgia, serif' }}>
                      {choicesMade}
                    </p>
                  </div>
                </div>

                {/* Achievement */}
                {achievement && (
                  <div className="bg-[#F5E6D3] border-4 border-[#F39C12] p-6 flex items-center gap-6 shadow-lg">
                    <div className="w-16 h-16 bg-[#F39C12] border-4 border-[#D35400] rounded-full flex items-center justify-center flex-shrink-0">
                      <Award className="w-8 h-8 text-[#5D4037]" />
                    </div>
                    <div className="flex-1">
                      <p className="text-xl text-[#5D4037] mb-1" style={{ fontFamily: 'Georgia, serif' }}>
                        Достижение:
                      </p>
                      <p className="text-3xl text-[#D35400]" style={{ fontFamily: 'Georgia, serif' }}>
                        "{achievement}"
                      </p>
                    </div>
                  </div>
                )}
              </div>

              {/* Decorative separator */}
              <div className="flex items-center gap-4 mb-6">
                <div className="flex-1 h-1 bg-[#5D4037]"></div>
                <span className="text-4xl text-[#D35400]">✦</span>
                <div className="flex-1 h-1 bg-[#5D4037]"></div>
              </div>

              {/* Quote or flavor text */}
              <div className="text-center italic text-xl text-[#5D4037] px-8" style={{ fontFamily: 'Georgia, serif' }}>
                "Каждое приключение — это шаг к величию..."
              </div>

              {/* Decorative corners */}
              <div className="absolute top-4 left-4 w-8 h-8 border-t-4 border-l-4 border-[#D35400]"></div>
              <div className="absolute top-4 right-4 w-8 h-8 border-t-4 border-r-4 border-[#D35400]"></div>
            </div>
          </div>

          {/* Right side - Credits and actions */}
          <div className="w-1/3 flex flex-col gap-6">
            {/* Creators block */}
            <div className="bg-[#E8DCC8] border-6 border-[#5D4037] shadow-xl p-8">
              <div className="text-center mb-6">
                <Users className="w-12 h-12 text-[#D35400] mx-auto mb-3" />
                <h3 className="text-2xl text-[#5D4037] mb-4" style={{ fontFamily: 'Georgia, serif' }}>
                  Со��датели саги
                </h3>
              </div>

              <div className="space-y-4">
                {/* Studio logo/name */}
                <div className="text-center bg-[#5D4037] border-2 border-[#F39C12] py-3 px-4">
                  <p className="text-xl text-[#F39C12]" style={{ fontFamily: 'Georgia, serif' }}>
                    Студия «Айлопорос»
                  </p>
                </div>

                {/* Feedback button */}
                <button className="w-full bg-[#F5E6D3] border-3 border-[#5D4037] hover:border-[#D35400] py-3 px-4 flex items-center justify-center gap-3 transition-all duration-300">
                  <Mail className="w-6 h-6 text-[#D35400]" />
                  <span className="text-lg text-[#5D4037]" style={{ fontFamily: 'Georgia, serif' }}>
                    Написать отзыв
                  </span>
                </button>

                {/* Social icons */}
                <div className="flex justify-center gap-3 pt-2">
                  {['📱', '🌐', '📧'].map((icon, i) => (
                    <button
                      key={i}
                      className="w-12 h-12 bg-[#5D4037] hover:bg-[#D35400] border-2 border-[#F39C12] rounded-full flex items-center justify-center transition-all duration-300 text-xl"
                    >
                      {icon}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* Action buttons */}
            <div className="flex-1 flex flex-col justify-end gap-4">
              {/* Continue button - primary */}
              <button
                onClick={onContinue}
                className="w-full bg-[#D35400] hover:bg-[#F39C12] border-4 border-[#F39C12] text-white py-6 px-6 text-2xl transition-all duration-300 transform hover:scale-105 shadow-xl flex items-center justify-center gap-3"
                style={{ fontFamily: 'Georgia, serif' }}
              >
                Дальше в путь
                <ChevronRight className="w-8 h-8" />
              </button>

              {/* Main menu button - secondary */}
              <button
                onClick={onMainMenu}
                className="w-full bg-[#5D4037] hover:bg-[#D35400] border-4 border-[#5D4037] hover:border-[#F39C12] text-[#E8DCC8] py-4 px-6 text-xl transition-all duration-300 flex items-center justify-center gap-3"
                style={{ fontFamily: 'Georgia, serif' }}
              >
                <Home className="w-6 h-6" />
                В главное меню
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
