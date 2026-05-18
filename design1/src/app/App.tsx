import { GameNavigation } from './components/GameNavigation';

export default function App() {
  return (
    <div className="size-full flex items-center justify-center bg-[#0a0e27]">
      <div className="w-full h-full max-w-[1920px] max-h-[1080px] aspect-video">
        <GameNavigation />
      </div>
    </div>
  );
}