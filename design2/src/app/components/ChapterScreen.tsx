import { useState } from 'react';
import { ChevronRight, Book } from 'lucide-react';

interface ChapterScreenProps {
  chapterNumber: number;
}

const chapterData = {
  1: {
    title: 'Глава I: Наследство Мельника',
    scenes: [
      {
        speaker: 'Рассказчик',
        text: 'Старый мельник умер, оставив троим сыновьям скромное наследство: мельницу, осла и кота.',
        background: 'https://images.unsplash.com/photo-1650983296678-8654dd04ecee?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxvbGQlMjBtaWxsJTIwd2luZG1pbGwlMjBtZWRpZXZhbHxlbnwxfHx8fDE3Njc5NzkwNjh8MA&ixlib=rb-4.1.0&q=80&w=1080'
      },
      {
        speaker: 'Младший сын',
        text: 'Братья взяли мельницу и осла, а мне достался лишь этот кот. Что толку от такого наследства?',
        background: 'https://images.unsplash.com/photo-1650983296678-8654dd04ecee?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxvbGQlMjBtaWxsJTIwd2luZG1pbGwlMjBtZWRpZXZhbHxlbnwxfHx8fDE3Njc5NzkwNjh8MA&ixlib=rb-4.1.0&q=80&w=1080'
      },
      {
        speaker: 'Кот в сапогах',
        text: 'Не печалься, хозяин! Дай мне мешок и пару сапог, и я сделаю тебя богачом. Ты увидишь, что получил лучшую долю!',
        background: 'https://images.unsplash.com/photo-1650983296678-8654dd04ecee?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxvbGQlMjBtaWxsJTIwd2luZG1pbGwlMjBtZWRpZXZhbHxlbnwxfHx8fDE3Njc5NzkwNjh8MA&ixlib=rb-4.1.0&q=80&w=1080'
      },
      {
        speaker: 'Рассказчик',
        text: 'Младший сын удивился, услышав, как кот говорит, но решил довериться ему. Так началась удивительная авантюра...',
        background: 'https://images.unsplash.com/photo-1650983296678-8654dd04ecee?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxvbGQlMjBtaWxsJTIwd2luZG1pbGwlMjBtZWRpZXZhbHxlbnwxfHx8fDE3Njc5NzkwNjh8MA&ixlib=rb-4.1.0&q=80&w=1080'
      }
    ]
  }
};

export function ChapterScreen({ chapterNumber }: ChapterScreenProps) {
  const [sceneIndex, setSceneIndex] = useState(0);
  const chapter = chapterData[chapterNumber as keyof typeof chapterData];
  const currentScene = chapter.scenes[sceneIndex];
  const isLastScene = sceneIndex === chapter.scenes.length - 1;

  const handleNext = () => {
    if (!isLastScene) {
      setSceneIndex(sceneIndex + 1);
    }
  };

  return (
    <div className="relative w-full h-screen overflow-hidden">
      {/* Background Image */}
      <div 
        className="absolute inset-0 bg-cover bg-center transition-all duration-700"
        style={{
          backgroundImage: `url('${currentScene.background}')`,
        }}
      >
        <div className="absolute inset-0 bg-gradient-to-t from-[#5D4037]/95 via-[#5D4037]/60 to-[#5D4037]/40" />
      </div>

      {/* Content */}
      <div className="relative z-10 h-full flex flex-col px-12 py-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-4">
            <Book className="w-10 h-10 text-[#F39C12]" />
            <h2 className="text-3xl text-[#F39C12]" style={{ fontFamily: 'Georgia, serif', textShadow: '2px 2px 4px rgba(0,0,0,0.8)' }}>
              {chapter.title}
            </h2>
          </div>
          <div className="flex gap-2">
            {chapter.scenes.map((_, index) => (
              <div
                key={index}
                className={`w-3 h-3 rounded-full transition-all duration-300 ${
                  index === sceneIndex
                    ? 'bg-[#F39C12] w-8'
                    : index < sceneIndex
                    ? 'bg-[#D35400]'
                    : 'bg-[#5D4037]'
                }`}
              />
            ))}
          </div>
        </div>

        {/* Main Scene Area */}
        <div className="flex-1 flex items-center justify-center">
          {/* Character illustration placeholder */}
          <div className="text-center">
            <div className="inline-block mb-8">
              <div className="w-64 h-64 rounded-full bg-[#D35400]/30 border-8 border-[#F39C12]/50 flex items-center justify-center backdrop-blur-sm">
                <span className="text-9xl">
                  {currentScene.speaker === 'Кот в сапогах' ? '🐱' : 
                   currentScene.speaker === 'Младший сын' ? '👤' : 
                   '📜'}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Dialogue Box */}
        <div className="relative">
          {/* Parchment-style text box */}
          <div className="bg-[#E8DCC8] border-4 border-[#5D4037] p-8 shadow-2xl">
            {/* Speaker name */}
            <div className="mb-4">
              <div className="inline-block bg-[#D35400] border-2 border-[#F39C12] px-6 py-2">
                <h3 className="text-2xl text-[#F39C12]" style={{ fontFamily: 'Georgia, serif' }}>
                  {currentScene.speaker}
                </h3>
              </div>
            </div>

            {/* Dialogue text */}
            <p className="text-2xl text-[#5D4037] leading-relaxed mb-6" style={{ fontFamily: 'Georgia, serif' }}>
              {currentScene.text}
            </p>

            {/* Next button */}
            <div className="flex justify-end">
              <button
                onClick={handleNext}
                className={`px-8 py-4 text-xl border-4 transition-all duration-300 flex items-center gap-3 ${
                  isLastScene
                    ? 'bg-[#5D4037]/50 border-[#5D4037] text-[#5D4037] cursor-default'
                    : 'bg-[#D35400] hover:bg-[#F39C12] border-[#F39C12] text-white hover:scale-105 shadow-lg'
                }`}
                style={{ fontFamily: 'Georgia, serif' }}
                disabled={isLastScene}
              >
                {isLastScene ? 'Конец главы' : 'Далее'}
                {!isLastScene && <ChevronRight className="w-6 h-6" />}
              </button>
            </div>

            {/* Decorative corners */}
            <div className="absolute -top-2 -left-2 w-6 h-6 border-t-4 border-l-4 border-[#D35400]"></div>
            <div className="absolute -top-2 -right-2 w-6 h-6 border-t-4 border-r-4 border-[#D35400]"></div>
            <div className="absolute -bottom-2 -left-2 w-6 h-6 border-b-4 border-l-4 border-[#D35400]"></div>
            <div className="absolute -bottom-2 -right-2 w-6 h-6 border-b-4 border-r-4 border-[#D35400]"></div>
          </div>

          {/* Page indicator */}
          <div className="absolute -bottom-8 right-4 text-[#E8DCC8] text-lg" style={{ fontFamily: 'Georgia, serif' }}>
            {sceneIndex + 1} / {chapter.scenes.length}
          </div>
        </div>
      </div>
    </div>
  );
}
