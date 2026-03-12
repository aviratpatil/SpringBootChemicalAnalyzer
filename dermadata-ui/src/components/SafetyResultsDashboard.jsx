import { useState } from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer } from 'recharts';
import { Shield, AlertTriangle, XCircle, CheckCircle, Beaker, ArrowLeft, Brain, Search, Download, Flag, Ban, FlaskConical, TriangleAlert, ListChecks } from 'lucide-react';

const SCORE_COLORS = {
    SAFE: { main: '#10b981', light: '#34d399', bg: 'rgba(16, 185, 129, 0.1)', ring: 'rgba(16, 185, 129, 0.25)' },
    CAUTION: { main: '#f59e0b', light: '#fbbf24', bg: 'rgba(245, 158, 11, 0.1)', ring: 'rgba(245, 158, 11, 0.25)' },
    DANGER: { main: '#ef4444', light: '#f87171', bg: 'rgba(239, 68, 68, 0.1)', ring: 'rgba(239, 68, 68, 0.25)' },
};

const STATUS_CONFIG = {
    SAFE: { badge: 'bg-emerald-500/15 text-emerald-400 border-emerald-500/25', icon: CheckCircle, label: 'SAFE' },
    RESTRICTED: { badge: 'bg-amber-500/15 text-amber-400 border-amber-500/25', icon: AlertTriangle, label: 'RESTRICTED' },
    EXCEEDED: { badge: 'bg-red-500/15 text-red-400 border-red-500/25', icon: XCircle, label: 'EXCEEDED' },
    PROHIBITED: { badge: 'bg-red-500/25 text-red-300 border-red-500/40', icon: Ban, label: 'PROHIBITED' },
    NOT_REGULATED: { badge: 'bg-neutral-500/15 text-yellow-500 border-neutral-500/25', icon: TriangleAlert, label: 'NOT REGULATED' },
};

export default function SafetyResultsDashboard({ report, onBack }) {
    const [searchQuery, setSearchQuery] = useState('');

    if (!report) return null;

    const colors = SCORE_COLORS[report.scoreCategory] || SCORE_COLORS.CAUTION;

    const gaugeData = [
        { name: 'Score', value: report.safetyScore },
        { name: 'Remaining', value: 100 - report.safetyScore },
    ];

    const filteredIngredients = report.ingredientResults.filter(ing =>
        ing.inciName.toLowerCase().includes(searchQuery.toLowerCase())
    );

    const handleDownload = () => {
        const lines = [
            `DermaData Compliance Report`,
            `Product Type: ${report.productType}`,
            `Safety Score: ${report.safetyScore}/100 (${report.scoreCategory})`,
            `Analyzed: ${new Date(report.analyzedAt).toLocaleString()}`,
            ``,
            `Total: ${report.totalIngredients} | Flagged: ${report.flaggedIngredients} | Prohibited: ${report.prohibitedCount} | Exceeded: ${report.exceededCount} | Violations: ${report.combinationViolations}`,
            ``,
            `--- Ingredient Breakdown ---`,
            ...report.ingredientResults.map(i =>
                `${i.inciName} | Detected: ${i.detectedConcentration ?? '-'}% | EU Max: ${i.euMaxConcentration ?? '-'}% | Status: ${i.status} | Penalty: -${i.penaltyPoints}`
            ),
        ];
        if (report.combinationWarnings?.length) {
            lines.push('', '--- Combination Warnings ---');
            report.combinationWarnings.forEach(w => {
                lines.push(`${w.ingredientA} × ${w.ingredientB}: ${w.explanation} (-${w.penaltyPoints} pts)`);
            });
        }
        const blob = new Blob([lines.join('\n')], { type: 'text/plain' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `dermadata-report-${report.productType.toLowerCase()}-${report.safetyScore}.txt`;
        a.click();
        URL.revokeObjectURL(url);
    };

    return (
        <div className="space-y-6">
            {/* ── Top Bar ── */}
            <div className="flex items-center justify-between animate-fade-in-up">
                <p className="text-neutral-400 text-sm font-medium">
                    DermaData – Cosmetic Ingredient Safety Analyzer
                </p>
                <button
                    onClick={onBack}
                    className="flex items-center gap-2 text-emerald-400 hover:text-emerald-300 transition-colors cursor-pointer text-sm font-bold"
                >
                    New Analysis (+)
                </button>
            </div>

            {/* ════ PROHIBITED WARNING BANNER ════ */}
            {report.hardCapApplied && (
                <div className="bg-red-500/20 border border-red-500/40 rounded-xl p-4 flex items-center justify-center gap-3 animate-fade-in-up mt-6 mb-2 shadow-[0_0_20px_rgba(239,68,68,0.15)]">
                    <TriangleAlert className="w-6 h-6 text-red-400 animate-pulse" />
                    <p className="text-white font-black tracking-wide">
                        ⛔ AUTOMATIC DANGER — Prohibited substance detected
                    </p>
                    <span className="text-red-300 text-xs font-bold border border-red-500/30 rounded-full px-2 py-0.5 ml-2">
                        {report.overrideReason}
                    </span>
                </div>
            )}

            {/* ════ SCORE CARD ════ */}
            <div className="glass-card p-8 animate-fade-in-up" style={{ animationDelay: '50ms' }}>
                <div className="flex flex-col lg:flex-row items-center gap-8">
                    {/* ── Circular Gauge ── */}
                    <div className="relative w-48 h-48 flex-shrink-0">
                        <div
                            className="absolute inset-0 rounded-full"
                            style={{ boxShadow: `0 0 40px ${colors.ring}, inset 0 0 20px ${colors.ring}` }}
                        />
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie data={gaugeData} cx="50%" cy="50%" startAngle={90} endAngle={-270} innerRadius={64} outerRadius={84} paddingAngle={0} dataKey="value" stroke="none" cornerRadius={6}>
                                    <Cell fill={colors.main} />
                                    <Cell fill="rgba(255,255,255,0.03)" />
                                </Pie>
                            </PieChart>
                        </ResponsiveContainer>
                        <div className="absolute inset-0 flex flex-col items-center justify-center">
                            <span className="text-5xl font-black" style={{ color: colors.light }}>{report.safetyScore}</span>
                            <span className="text-neutral-600 text-[10px] font-bold tracking-[0.2em] uppercase mt-0.5">out of 100</span>
                        </div>
                    </div>

                    {/* ── Info ── */}
                    <div className="flex-1 text-center lg:text-left space-y-3">
                        <div className="flex items-center gap-3 justify-center lg:justify-start">
                            <CheckCircle className="w-5 h-5" style={{ color: colors.light }} />
                            <span
                                className="text-sm font-black uppercase tracking-[0.15em]"
                                style={{ color: colors.light }}
                            >
                                {report.scoreCategory}
                            </span>
                        </div>
                        <h2 className="text-2xl font-bold text-neutral-100">
                            Safety Score: <span style={{ color: colors.light }}>{report.safetyScore}</span>
                        </h2>
                        <p className="text-neutral-500 text-sm">
                            <span className="text-neutral-300 font-semibold">{report.productType}</span>
                            <span className="mx-2 text-neutral-700">·</span>
                            {new Date(report.analyzedAt).toLocaleString()}
                        </p>
                        <p className="text-neutral-500 text-xs">
                            Total <span className="text-neutral-300 font-bold">{report.totalIngredients}</span>
                            <span className="mx-1.5 text-neutral-700">·</span>
                            Flagged <span className="text-amber-400 font-bold">{report.flaggedIngredients}</span>
                            <span className="mx-1.5 text-neutral-700">·</span>
                            Prohibited <span className="text-red-400 font-bold">{report.prohibitedCount}</span>
                            <span className="mx-1.5 text-neutral-700">·</span>
                            Exceeded <span className="text-red-400 font-bold">{report.exceededCount}</span>
                            <span className="mx-1.5 text-neutral-700">·</span>
                            Violations <span className="text-purple-400 font-bold">{report.combinationViolations}</span>
                        </p>
                    </div>

                    {/* ── Stat Cards ── */}
                    <div className="grid grid-cols-2 gap-3 flex-shrink-0">
                        <StatCard icon={<ListChecks className="w-5 h-5" />} label="Total" value={report.totalIngredients} color="#10b981" />
                        <StatCard icon={<Flag className="w-5 h-5" />} label="Flagged" value={report.flaggedIngredients} color="#f59e0b" />
                        <StatCard icon={<Ban className="w-5 h-5" />} label="Exceeded" value={report.exceededCount} color="#ef4444" />
                        <StatCard icon={<TriangleAlert className="w-5 h-5" />} label="Violations" value={report.combinationViolations} color="#a855f7" />
                    </div>
                </div>
            </div>

            {/* ════ INGREDIENT BREAKDOWN ════ */}
            <div className="glass-card overflow-hidden animate-fade-in-up" style={{ animationDelay: '150ms' }}>
                {/* ── Header ── */}
                <div className="px-6 py-4 border-b border-white/[0.05] flex items-center justify-between flex-wrap gap-3">
                    <h3 className="text-sm font-black text-neutral-200 flex items-center gap-2">
                        <Beaker className="w-4 h-4 text-emerald-400" />
                        Ingredient Breakdown
                    </h3>
                    <div className="flex items-center gap-3">
                        <div className="relative">
                            <Search className="w-3.5 h-3.5 text-neutral-600 absolute left-3 top-1/2 -translate-y-1/2" />
                            <input
                                type="text"
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                placeholder="Search..."
                                className="bg-white/[0.03] border border-white/[0.05] rounded-lg pl-8 pr-3 py-1.5 text-xs text-neutral-300 placeholder-neutral-600 focus:outline-none focus:border-emerald-500/30 w-40"
                            />
                        </div>
                        <button
                            onClick={handleDownload}
                            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white/[0.03] border border-white/[0.05] text-neutral-400 text-xs font-semibold hover:bg-white/[0.06] transition-all cursor-pointer"
                        >
                            <Download className="w-3.5 h-3.5" /> Download Report
                        </button>
                    </div>
                </div>

                {/* ── Table Header ── */}
                <div className="grid grid-cols-[1fr_90px_90px_140px_120px_70px] gap-2 px-6 py-3 text-[11px] font-bold text-neutral-500 uppercase tracking-wider border-b border-white/[0.04]">
                    <span>Name</span>
                    <span className="text-center">Detected %</span>
                    <span className="text-center">EU Max %</span>
                    <span className="text-center">Status</span>
                    <span>Regulation</span>
                    <span className="text-center">Penalty</span>
                </div>

                {/* ── Rows ── */}
                <div className="divide-y divide-white/[0.03] max-h-[500px] overflow-y-auto">
                    {filteredIngredients.map((ing, index) => {
                        const config = STATUS_CONFIG[ing.status] || STATUS_CONFIG.NOT_REGULATED;
                        const StatusIcon = config.icon;
                        return (
                            <div
                                key={index}
                                className="grid grid-cols-[1fr_90px_90px_140px_120px_70px] gap-2 px-6 py-3 items-center hover:bg-white/[0.015] transition-colors"
                            >
                                <div className="flex items-center gap-2">
                                    <FlaskConical className="w-3.5 h-3.5 text-neutral-600 flex-shrink-0" />
                                    <span className="text-sm font-medium text-neutral-200 truncate">{ing.inciName}</span>
                                </div>
                                <span className="text-center font-mono text-xs text-neutral-400">
                                    {ing.detectedConcentration != null ? `${ing.detectedConcentration}%` : '—'}
                                </span>
                                <span className="text-center font-mono text-xs text-neutral-400">
                                    {ing.euMaxConcentration != null ? `${ing.euMaxConcentration}%` : '—'}
                                </span>
                                <div className="flex justify-center">
                                    <span className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-bold border ${config.badge}`}>
                                        <StatusIcon className="w-3 h-3" />
                                        {config.label}
                                    </span>
                                </div>
                                <span className="text-neutral-600 text-[11px] truncate">{ing.regulationRef || '—'}</span>
                                <span className="text-center">
                                    {ing.penaltyPoints > 0 ? (
                                        <span className="text-red-400 font-black text-xs">-{ing.penaltyPoints}</span>
                                    ) : (
                                        <span className="text-neutral-700 text-xs">0</span>
                                    )}
                                </span>
                            </div>
                        );
                    })}
                </div>
            </div>

            {/* ════ COMBINATION WARNINGS ════ */}
            {report.combinationWarnings?.length > 0 && (
                <div className="glass-card p-6 animate-fade-in-up" style={{ animationDelay: '250ms' }}>
                    <h3 className="text-sm font-black text-neutral-200 flex items-center gap-2 mb-5">
                        <AlertTriangle className="w-4 h-4 text-amber-400" />
                        Combination Warnings
                        <span className="text-xs font-normal bg-amber-500/10 text-amber-400 px-2 py-0.5 rounded-full border border-amber-500/20">{report.combinationWarnings.length}</span>
                    </h3>
                    <div className="space-y-3">
                        {report.combinationWarnings.map((w, i) => (
                            <div key={i} className="bg-amber-500/[0.03] border border-amber-500/10 rounded-xl p-4 flex items-start gap-3">
                                <AlertTriangle className="w-4 h-4 text-amber-500 flex-shrink-0 mt-0.5" />
                                <div className="flex-1 min-w-0">
                                    <div className="flex items-center gap-2 mb-1.5 flex-wrap">
                                        <span className="font-bold text-sm text-amber-300">{w.ingredientA}</span>
                                        <span className="text-neutral-600 text-xs">×</span>
                                        <span className="font-bold text-sm text-amber-300">{w.ingredientB}</span>
                                        <span className="ml-auto text-red-400 font-black text-xs">-{w.penaltyPoints} pts</span>
                                    </div>
                                    <p className="text-neutral-400 text-xs leading-relaxed">{w.explanation}</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* ════ AI INSIGHTS ════ */}
            {report.llmInsights?.length > 0 && (
                <div className="glass-card p-6 animate-fade-in-up" style={{ animationDelay: '350ms' }}>
                    <h3 className="text-sm font-black text-neutral-200 flex items-center gap-2 mb-5">
                        <Brain className="w-4 h-4 text-cyan-400" />
                        AI Safety Insights
                    </h3>
                    <div className="space-y-2.5">
                        {report.llmInsights.map((insight, i) => (
                            <div key={i} className="bg-cyan-500/[0.03] border border-cyan-500/10 rounded-xl px-4 py-3 text-neutral-400 text-xs leading-relaxed">
                                {insight}
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}

function StatCard({ icon, label, value, color }) {
    return (
        <div
            className="flex flex-col items-center justify-center py-4 px-5 rounded-xl min-w-[100px]"
            style={{ background: `${color}0a`, border: `1px solid ${color}18` }}
        >
            <div style={{ color }} className="mb-1.5">{icon}</div>
            <span className="text-xl font-black" style={{ color }}>{value}</span>
            <span className="text-neutral-500 text-[10px] font-bold uppercase tracking-widest mt-0.5">{label}</span>
        </div>
    );
}
