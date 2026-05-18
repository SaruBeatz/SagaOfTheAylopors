import { useState } from 'react';
import { ChevronRight, Crown, User, Cat } from 'lucide-react';

interface HeroSelectionScreenProps {
  playerName: string;
  onContinue: (hero: string) => void;
}

const heroes = [
  { 
    id: 'cat', 
    name: 'Кот в сапогах', 
    icon: '🐱',
    description: 'Хитрый и ловкий авантюрист',
    color: '#D35400'
  },
  { 
    id: 'marquis', 
    name: 'Маркиз Карабас', 
    icon: '👤',
    description: 'Младший сын мельника',
    color: '#5D4037'
  },
  { 
    id: 'king', 
    name: 'Король', 
    icon: '👑',
    description: 'Мудрый правитель',
    color: '#F39C12'
  },
  { 
    id: 'princess', 
    name: 'Принцесса', 
    icon: '👸',
    description: 'Прекрасная и умная',
    color: '#283593'
  },
  { 
    id: 'ogre', 
    name: 'Людоед', 
    icon: '👹',
    description: 'Могучий и опасный',
    color: '#1B5E20'
  },
];

export function HeroSelectionScreen({ playerName, onContinue }: HeroSelectionScreenProps) {
  const [selectedHero, setSelectedHero] = useState('cat');

  const selected = heroes.find(h => h.id === selectedHero)!;

  return (
    <div className="relative w-full h-screen overflow-hidden">
      {/* Background */}
      <div 
        className="absolute inset-0 bg-cover bg-center"
        style={{
          backgroundImage: `url('https://images.unsplash.com/photo-1690965704262-83452e27ef53?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtZWRpZXZhbCUyMGNhc3RsZSUyMGhhbGwlMjBnYWxsZXJ5fGVufDF8fHx8MTc2Nzk3OTA2OHww&ixlib=rb-4.1.0&q=80&w=1080')`,
        }}
      >
        <div className="absolute inset-0 bg-[#5D4037]/85" />
      </div>

      {/* Content */}
      <div className="relative z-10 h-full flex flex-col px-12 py-12">
        {/* Title */}
        <div className="text-center mb-8">
          <h2 className="text-5xl text-[#F39C12] mb-3" style={{ fontFamily: 'Georgia, serif', textShadow: '3px 3px 6px rgba(0,0,0,0.8)' }}>
            Выбери своего героя
          </h2>
          <p className="text-xl text-[#E8DCC8]" style={{ fontFamily: 'Georgia, serif' }}>
            {playerName}, с кем ты отправишься в путь?
          </p>
        </div>

        {/* Main Content Area */}
        <div className="flex-1 flex gap-8">
          {/* Left side - Selected Hero Display */}
          <div className="w-2/5 flex items-center justify-center">
            <div className="text-center">
              {/* Large portrait medallion */}
              <div 
                className="w-80 h-80 rounded-full mx-auto mb-6 flex items-center justify-center border-8 shadow-2xl"
                style={{ 
                  backgroundColor: selected.color,
                  borderColor: '#F39C12'
                }}
              >
                <span className="text-9xl">{selected.icon}</span>
              </div>
              
              <h3 className="text-4xl text-[#F39C12] mb-3" style={{ fontFamily: 'Georgia, serif' }}>
                {selected.name}
              </h3>
              <p className="text-xl text-[#E8DCC8]" style={{ fontFamily: 'Georgia, serif' }}>
                {selected.description}
              </p>
            </div>
          </div>

          {/* Right side - Hero Gallery */}
          <div className="w-3/5 flex flex-col justify-center">
            <div className="grid grid-cols-5 gap-6">
              {heroes.map((hero) => (
                <button
                  key={hero.id}
                  onClick={() => setSelectedHero(hero.id)}
                  className={`flex flex-col items-center gap-3 transition-all duration-300 ${
                    selectedHero === hero.id
                      ? 'transform scale-110'
                      : 'opacity-70 hover:opacity-100'
                  }`}
                >
                  {/* Portrait medallion */}
                  <div 
                    className={`w-32 h-32 rounded-full flex items-center justify-center border-4 shadow-lg transition-all duration-300 ${
                      selectedHero === hero.id
                        ? 'border-[#F39C12] shadow-[#F39C12]/50'
                        : 'border-[#5D4037]'
                    }`}
                    style={{ backgroundColor: hero.color }}
                  >
                    <span className="text-5xl">{hero.icon}</span>
                  </div>
                  
                  {/* Name label */}
                  <div 
                    className={`px-4 py-2 border-2 text-center text-sm ${
                      selectedHero === hero.id
                        ? 'bg-[#F39C12] border-[#F39C12] text-[#5D4037]'
                        : 'bg-[#5D4037] border-[#E8DCC8]/50 text-[#E8DCC8]'
                    }`}
                    style={{ fontFamily: 'Georgia, serif' }}
                  >
                    {hero.name.split(' ')[0]}
                  </div>
                </button>
              ))}
            </div>

            {/* Continue Button */}
            <div className="mt-12">
              <button
                onClick={() => onContinue(selectedHero)}
                className="w-full bg-[#D35400] hover:bg-[#F39C12] border-4 border-[#F39C12] text-white py-5 px-8 text-2xl transition-all duration-300 transform hover:scale-105 shadow-xl flex items-center justify-center gap-4"
                style={{ fontFamily: 'Georgia, serif' }}
              >
                Отправиться с ним
                <ChevronRight className="w-8 h-8" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
