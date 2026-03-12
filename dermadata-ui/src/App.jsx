import { useState } from 'react';
import IngredientAnalyzer from './components/IngredientAnalyzer';
import SafetyResultsDashboard from './components/SafetyResultsDashboard';

function App() {
  const [report, setReport] = useState(null);

  return (
    <div className="min-h-screen flex flex-col relative overflow-x-hidden bg-black">
      {/* ── Decorative Background ── */}
      <div className="fixed inset-0 pointer-events-none -z-10">
        <div className="absolute top-0 left-0 w-full h-full bg-black" />
        <div className="absolute -top-[400px] -right-[300px] w-[800px] h-[800px] bg-emerald-500/[0.04] rounded-full blur-[150px]" />
        <div className="absolute -bottom-[400px] -left-[300px] w-[800px] h-[800px] bg-cyan-500/[0.03] rounded-full blur-[150px]" />
        <div className="absolute top-[50%] left-[50%] -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-emerald-600/[0.02] rounded-full blur-[120px]" />
      </div>

      {/* ── Main Content ── */}
      <main className="flex-1 w-full max-w-[1400px] mx-auto px-6 sm:px-10 lg:px-16 py-8 sm:py-10">
        {report ? (
          <SafetyResultsDashboard report={report} onBack={() => setReport(null)} />
        ) : (
          <IngredientAnalyzer onAnalysisComplete={setReport} />
        )}
      </main>

      {/* ── Footer ── */}
      <footer className="w-full text-center py-6 px-6 border-t border-white/[0.04]">
        <p className="text-neutral-600 text-xs tracking-wide mb-1">
          DermaData – EU Cosmetics Regulation (EC) No 1223/2009 Compliance Analyzer
        </p>
        <p className="text-neutral-700 text-[11px] mb-3">
          For educational and informational purposes. Not a substitute for professional regulatory advice.
        </p>
        <div className="flex items-center justify-center gap-4 text-neutral-600 text-[11px]">
          <span className="hover:text-neutral-400 cursor-pointer transition-colors">Terms of Service</span>
          <span className="text-neutral-800">|</span>
          <span className="hover:text-neutral-400 cursor-pointer transition-colors">Privacy Policy</span>
          <span className="text-neutral-800">|</span>
          <span className="hover:text-neutral-400 cursor-pointer transition-colors">Help</span>
        </div>
      </footer>
    </div>
  );
}

export default App;
