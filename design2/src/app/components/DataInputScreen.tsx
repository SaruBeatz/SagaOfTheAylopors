import { useState } from 'react';
import { ChevronRight, Feather } from 'lucide-react';

interface DataInputScreenProps {
  onContinue: (name: string) => void;
}

export function DataInputScreen({ onContinue }: DataInputScreenProps) {
  const [name, setName] = useState('');
  const [agreed, setAgreed] = useState(false);

  const handleSubmit = () => {
    if (name.trim() && agreed) {
      onContinue(name);
    }
  };

  return (
    <div className="relative w-full h-screen overflow-hidden">
      {/* Background */}
      <div 
        className="absolute inset-0 bg-cover bg-center"
        style={{
          backgroundImage: `url('https://images.unsplash.com/photo-1678574420972-2b59628f904c?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtZWRpZXZhbCUyMGNhYmluJTIwaW50ZXJpb3IlMjB0YWJsZSUyMHNjcm9sbHN8ZW58MXx8fHwxNzY3OTc5MDY3fDA&ixlib=rb-4.1.0&q=80&w=1080')`,
        }}
      >
        <div className="absolute inset-0 bg-[#5D4037]/80" />
      </div>

      {/* Content */}
      <div className="relative z-10 h-full flex items-center justify-center px-12">
        <div className="w-full max-w-3xl">
          {/* Parchment paper effect */}
          <div className="relative bg-[#E8DCC8] border-8 border-[#5D4037] shadow-2xl p-12">
            {/* Decorative corners */}
            <div className="absolute top-4 left-4 w-8 h-8 border-t-4 border-l-4 border-[#D35400]"></div>
            <div className="absolute top-4 right-4 w-8 h-8 border-t-4 border-r-4 border-[#D35400]"></div>
            <div className="absolute bottom-4 left-4 w-8 h-8 border-b-4 border-l-4 border-[#D35400]"></div>
            <div className="absolute bottom-4 right-4 w-8 h-8 border-b-4 border-r-4 border-[#D35400]"></div>

            {/* Feather decoration */}
            <div className="absolute -top-6 -right-6">
              <Feather className="w-16 h-16 text-[#F39C12] transform rotate-45" strokeWidth={1.5} />
            </div>

            {/* Title */}
            <h2 className="text-5xl text-center mb-12 text-[#5D4037]" style={{ fontFamily: 'Georgia, serif' }}>
              Давай познакомимся
            </h2>

            {/* Scroll decoration with text */}
            <div className="mb-8">
              <label className="block text-2xl mb-4 text-[#5D4037]" style={{ fontFamily: 'Georgia, serif' }}>
                Как тебя зовут, путник?
              </label>
              <div className="relative">
                {/* Scroll style input */}
                <div className="bg-[#F5E6D3] border-4 border-[#5D4037] p-2">
                  <input
                    type="text"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    className="w-full bg-transparent text-3xl text-[#5D4037] px-4 py-3 outline-none"
                    style={{ fontFamily: 'Georgia, serif' }}
                    placeholder="Введи своё имя..."
                  />
                </div>
                {/* Ink decorations */}
                <div className="absolute -bottom-2 -right-2 w-4 h-4 bg-[#283593] rounded-full opacity-60"></div>
                <div className="absolute -bottom-1 -right-6 w-3 h-3 bg-[#283593] rounded-full opacity-40"></div>
              </div>
            </div>

            {/* Agreement checkbox - wax seal style */}
            <div className="mb-12">
              <div 
                onClick={() => setAgreed(!agreed)}
                className="flex items-start gap-6 cursor-pointer group"
              >
                {/* Wax seal checkbox */}
                <div className={`flex-shrink-0 w-16 h-16 rounded-full border-4 flex items-center justify-center transition-all duration-300 ${
                  agreed 
                    ? 'bg-[#D35400] border-[#F39C12] shadow-lg' 
                    : 'bg-[#5D4037] border-[#5D4037] group-hover:border-[#D35400]'
                }`}>
                  {agreed && (
                    <svg className="w-10 h-10 text-[#F39C12]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                    </svg>
                  )}
                </div>
                <p className="text-xl text-[#5D4037] leading-relaxed pt-2" style={{ fontFamily: 'Georgia, serif' }}>
                  Я согласен на обработку моих данных в рамках этой сказочной саги
                </p>
              </div>
            </div>

            {/* Continue Button */}
            <button
              onClick={handleSubmit}
              disabled={!name.trim() || !agreed}
              className={`w-full py-6 px-8 text-2xl border-4 transition-all duration-300 flex items-center justify-center gap-4 ${
                name.trim() && agreed
                  ? 'bg-[#D35400] hover:bg-[#F39C12] border-[#F39C12] text-white hover:scale-105 shadow-xl'
                  : 'bg-[#5D4037]/50 border-[#5D4037] text-[#5D4037]/50 cursor-not-allowed'
              }`}
              style={{ fontFamily: 'Georgia, serif' }}
            >
              Продолжить
              <ChevronRight className="w-8 h-8" />
            </button>

            {/* Ink stains decoration */}
            <div className="absolute bottom-8 left-8 w-6 h-6 bg-[#283593] rounded-full opacity-20 blur-sm"></div>
            <div className="absolute top-1/3 right-12 w-4 h-4 bg-[#283593] rounded-full opacity-15 blur-sm"></div>
          </div>
        </div>
      </div>
    </div>
  );
}
