import { useState, useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import { Upload, FileText, FlaskConical, Trash2, Plus, Loader2, Sparkles, Camera, ShieldCheck } from 'lucide-react';
import { extractIngredients, analyzeIngredients } from '../services/api';

const PRODUCT_TYPES = [
    { label: 'Shampoo', icon: '🧴' },
    { label: 'Soap', icon: '🧼' },
    { label: 'Conditioner', icon: '🧴' },
];

export default function IngredientAnalyzer({ onAnalysisComplete }) {
    const [ingredients, setIngredients] = useState([]);
    const [rawText, setRawText] = useState('');
    const [productType, setProductType] = useState('Shampoo');
    const [loading, setLoading] = useState(false);
    const [extracting, setExtracting] = useState(false);
    const [error, setError] = useState(null);

    const onDrop = useCallback(async (acceptedFiles) => {
        if (acceptedFiles.length === 0) return;
        setExtracting(true);
        setError(null);
        try {
            const base64 = await fileToBase64(acceptedFiles[0]);
            const extracted = await extractIngredients({ imageBase64: base64 });
            setIngredients(extracted.map(i => ({ ...i, concentration: i.concentration || '' })));
        } catch {
            setError('Failed to extract ingredients from image.');
        } finally {
            setExtracting(false);
        }
    }, []);

    const { getRootProps, getInputProps, isDragActive } = useDropzone({
        onDrop,
        accept: { 'image/*': ['.png', '.jpg', '.jpeg', '.webp', '.heif'] },
        maxFiles: 1,
    });

    const handleExtractFromText = async () => {
        if (!rawText.trim()) return;
        setExtracting(true);
        setError(null);
        try {
            const extracted = await extractIngredients({ rawText });
            setIngredients(extracted.map(i => ({ ...i, concentration: i.concentration || '' })));
        } catch {
            setError('Failed to extract ingredients from text.');
        } finally {
            setExtracting(false);
        }
    };

    const handleAnalyze = async () => {
        if (ingredients.length === 0) { setError('Please extract or add ingredients first.'); return; }
        setLoading(true);
        setError(null);
        try {
            const request = {
                ingredients: ingredients.map(i => ({ ...i, concentration: i.concentration !== '' ? parseFloat(i.concentration) : null })),
                productType,
            };
            const report = await analyzeIngredients(request);
            onAnalysisComplete(report);
        } catch {
            setError('Analysis failed. Make sure the backend is running.');
        } finally {
            setLoading(false);
        }
    };

    const updateField = (index, field, value) => {
        const u = [...ingredients];
        u[index] = { ...u[index], [field]: value };
        setIngredients(u);
    };

    const removeIngredient = (i) => setIngredients(ingredients.filter((_, idx) => idx !== i));
    const addIngredient = () => setIngredients([...ingredients, { rawName: '', inciName: '', concentration: '', position: ingredients.length + 1, confidenceScore: 1.0 }]);

    const getBarColor = (s) => s >= 0.9 ? 'from-emerald-400 to-cyan-400' : s >= 0.7 ? 'from-yellow-400 to-amber-400' : 'from-red-400 to-orange-400';

    return (
        <div className="space-y-7">

            {/* ═══════════════ HEADER ═══════════════ */}
            <div className="text-center space-y-4 animate-fade-in-up">
                <div className="inline-flex items-center gap-4">
                    <div className="p-3 rounded-2xl bg-gradient-to-br from-emerald-500/20 to-cyan-500/10 border border-emerald-500/20 animate-float">
                        <FlaskConical className="w-9 h-9 text-emerald-400" />
                    </div>
                    <div className="text-left">
                        <h1 className="text-4xl md:text-5xl font-black tracking-tight bg-gradient-to-r from-emerald-400 via-teal-300 to-cyan-400 bg-clip-text text-transparent">
                            DermaData
                        </h1>
                        <p className="text-neutral-500 text-[10px] font-bold tracking-[0.25em] uppercase">Cosmetic Ingredient Safety Analyzer</p>
                    </div>
                </div>
                <p className="text-neutral-500 text-sm max-w-xl mx-auto leading-relaxed">
                    Upload a product label image or paste the ingredient list. We'll analyze each ingredient against{' '}
                    <span className="text-neutral-200 font-semibold bg-emerald-500/10 px-1.5 py-0.5 rounded">(EC) No 1223/2009</span>{' '}
                    and generate a safety compliance score.
                </p>
            </div>

            {/* ═══════════════ PRODUCT TYPE ═══════════════ */}
            <div className="glass-card p-5 animate-fade-in-up" style={{ animationDelay: '80ms' }}>
                <p className="text-[11px] font-bold text-neutral-500 uppercase tracking-[0.2em] mb-3 text-center">Product Type Selection</p>
                <div className="grid grid-cols-3 gap-3">
                    {PRODUCT_TYPES.map(({ label, icon }) => (
                        <button
                            key={label}
                            onClick={() => setProductType(label)}
                            className={`flex items-center justify-center gap-2.5 py-3 rounded-xl text-sm font-bold tracking-wide transition-all duration-300 cursor-pointer
                ${productType === label
                                    ? 'bg-gradient-to-r from-emerald-500 to-teal-500 text-black shadow-lg shadow-emerald-500/25'
                                    : 'bg-white/[0.03] text-neutral-500 border border-white/[0.05] hover:bg-white/[0.06] hover:text-neutral-300'}`}
                        >
                            <span className="text-base">{icon}</span> {label}
                        </button>
                    ))}
                </div>
            </div>

            {/* ═══════════════ INGREDIENT INPUT ═══════════════ */}
            <div className="glass-card p-5 animate-fade-in-up" style={{ animationDelay: '160ms' }}>
                <p className="text-[11px] font-bold text-neutral-500 uppercase tracking-[0.2em] mb-4 text-center">Ingredient Input</p>
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">

                    {/* ── Scan Label ── */}
                    <div className="bg-white/[0.02] border border-white/[0.04] rounded-2xl p-5 flex flex-col">
                        <h3 className="text-sm font-bold mb-4 flex items-center gap-2 text-neutral-200">
                            <Camera className="w-4 h-4 text-emerald-400" /> Scan Label Image
                        </h3>

                        <div
                            {...getRootProps()}
                            className={`flex-1 border-2 border-dashed rounded-xl flex items-center justify-center cursor-pointer transition-all min-h-[140px] mb-4
                ${isDragActive ? 'border-emerald-500 bg-emerald-500/[0.05]' : 'border-white/[0.06] hover:border-emerald-500/30'}`}
                        >
                            <input {...getInputProps()} />
                            {extracting ? (
                                <Loader2 className="w-8 h-8 text-emerald-400 animate-spin" />
                            ) : (
                                <div className="text-center p-4">
                                    <Upload className="w-6 h-6 text-neutral-600 mx-auto mb-2" />
                                    <p className="text-neutral-400 text-xs"><span className="text-emerald-400 font-semibold">Click to upload</span> or drag & drop</p>
                                    <p className="text-neutral-600 text-[10px] mt-1">PNG, JPG, WEBP up to 10MB</p>
                                </div>
                            )}
                        </div>

                        <button
                            {...getRootProps()}
                            className="w-full py-2.5 rounded-xl bg-white/[0.04] border border-white/[0.05] text-neutral-400 font-bold text-xs tracking-widest hover:bg-white/[0.07] transition-all cursor-pointer flex items-center justify-center gap-2 mt-auto"
                        >
                            <Camera className="w-3.5 h-3.5" /> SELECT IMAGE
                        </button>
                    </div>

                    {/* ── Paste List ── */}
                    <div className="bg-white/[0.02] border border-white/[0.04] rounded-2xl p-5 flex flex-col">
                        <h3 className="text-sm font-bold mb-4 flex items-center gap-2 text-neutral-200">
                            <FileText className="w-4 h-4 text-cyan-400" /> Paste Ingredient List
                        </h3>

                        <textarea
                            value={rawText}
                            onChange={(e) => setRawText(e.target.value)}
                            placeholder={"Example: Aqua, Sodium Lauryl Sulfate, Cocamidopropyl Betaine, Glycerin, Methylparaben..."}
                            className="flex-1 min-h-[140px] bg-black/30 border border-white/[0.04] rounded-xl p-4 text-sm text-neutral-200 placeholder-neutral-600 resize-none focus:outline-none focus:border-emerald-500/30 transition-all leading-relaxed mb-4"
                        />

                        <button
                            onClick={handleExtractFromText}
                            disabled={!rawText.trim() || extracting}
                            className="w-full py-2.5 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 text-white font-bold text-xs tracking-[0.15em] uppercase hover:from-emerald-500 hover:to-teal-500 disabled:opacity-25 disabled:cursor-not-allowed transition-all cursor-pointer flex items-center justify-center gap-2 shadow-lg shadow-emerald-500/10 mt-auto"
                        >
                            {extracting ? <Loader2 className="w-4 h-4 animate-spin" /> : <Sparkles className="w-4 h-4" />}
                            EXTRACT & NORMALIZE
                        </button>
                        <p className="text-neutral-600 text-[10px] text-center mt-2">Provide comma-separated chemical names.</p>
                    </div>
                </div>
            </div>

            {/* ═══════════════ ERROR ═══════════════ */}
            {error && (
                <div className="bg-red-500/[0.06] border border-red-500/15 rounded-xl px-5 py-3 text-red-400 text-sm flex items-center gap-3 animate-fade-in-up">
                    <div className="w-2 h-2 rounded-full bg-red-500 flex-shrink-0" /> {error}
                </div>
            )}

            {/* ═══════════════ EXTRACTED CHEMICAL PROFILE ═══════════════ */}
            {ingredients.length > 0 && (
                <div className="glass-card overflow-hidden animate-fade-in-up" style={{ animationDelay: '100ms' }}>
                    {/* Header */}
                    <div className="flex items-center justify-between px-5 py-4 border-b border-white/[0.04]">
                        <p className="text-sm font-black text-neutral-200 flex items-center gap-2">
                            <FlaskConical className="w-4 h-4 text-emerald-400" />
                            Extracted Chemical Profile
                            <span className="text-xs font-normal bg-emerald-500/15 text-emerald-400 px-2.5 py-0.5 rounded-full border border-emerald-500/20 ml-1">
                                {ingredients.length}
                            </span>
                        </p>
                        <button onClick={addIngredient} className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg bg-emerald-500/10 text-emerald-400 text-xs font-bold hover:bg-emerald-500/20 transition-all cursor-pointer border border-emerald-500/15 tracking-wider">
                            + ADD
                        </button>
                    </div>

                    {/* Table Header */}
                    <div className="grid grid-cols-[45px_1fr_120px_160px_36px] gap-3 px-5 py-2.5 border-b border-white/[0.04] text-[10px] font-bold text-neutral-500 uppercase tracking-widest">
                        <span>#</span>
                        <span>INCI Name</span>
                        <span>Conc. (%)</span>
                        <span>Confidence</span>
                        <span></span>
                    </div>

                    {/* Rows */}
                    <div className="divide-y divide-white/[0.025] max-h-[400px] overflow-y-auto">
                        {ingredients.map((ing, index) => (
                            <div key={index} className="grid grid-cols-[45px_1fr_120px_160px_36px] gap-3 px-5 py-2 items-center hover:bg-white/[0.012] transition-colors group">
                                <span className="text-neutral-600 font-mono text-[11px] font-bold">{String(index + 1).padStart(2, '0')}</span>
                                <input
                                    type="text"
                                    value={ing.inciName}
                                    onChange={(e) => updateField(index, 'inciName', e.target.value)}
                                    className="bg-transparent text-sm text-neutral-200 font-medium focus:outline-none w-full truncate"
                                />
                                <input
                                    type="number"
                                    value={ing.concentration}
                                    onChange={(e) => updateField(index, 'concentration', e.target.value)}
                                    placeholder="e.g. 0.5"
                                    step="0.01" min="0" max="100"
                                    className="bg-white/[0.03] border border-white/[0.04] rounded-lg px-2.5 py-1.5 text-[11px] text-neutral-300 focus:outline-none focus:border-emerald-500/30 w-full"
                                />
                                <div className="flex items-center gap-2">
                                    <div className="flex-1 h-2 bg-white/[0.04] rounded-full overflow-hidden">
                                        <div className={`h-full rounded-full bg-gradient-to-r ${getBarColor(ing.confidenceScore)} transition-all duration-700`} style={{ width: `${(ing.confidenceScore || 0) * 100}%` }} />
                                    </div>
                                    <span className="text-neutral-400 text-[11px] font-bold w-8 text-right">{((ing.confidenceScore || 0) * 100).toFixed(0)}%</span>
                                </div>
                                <button onClick={() => removeIngredient(index)} className="p-1 rounded text-neutral-700 hover:text-red-400 hover:bg-red-500/10 transition-all cursor-pointer opacity-0 group-hover:opacity-100">
                                    <Trash2 className="w-3.5 h-3.5" />
                                </button>
                            </div>
                        ))}
                    </div>

                    {/* CTA */}
                    <div className="px-5 py-4 border-t border-white/[0.04] flex justify-center">
                        <button
                            onClick={handleAnalyze}
                            disabled={loading || ingredients.length === 0}
                            className="px-10 py-3.5 rounded-2xl bg-gradient-to-r from-emerald-500 to-teal-500 text-black font-black text-sm tracking-[0.1em] uppercase hover:shadow-xl hover:shadow-emerald-500/20 disabled:opacity-25 disabled:cursor-not-allowed transition-all duration-300 flex items-center gap-3 cursor-pointer"
                        >
                            {loading ? <><Loader2 className="w-5 h-5 animate-spin" /> ANALYZING...</> : <><ShieldCheck className="w-5 h-5" /> GENERATE COMPLIANCE REPORT</>}
                        </button>
                    </div>
                </div>
            )}

            {/* ── Disabled CTA when no ingredients ── */}
            {ingredients.length === 0 && (
                <div className="flex justify-center animate-fade-in-up" style={{ animationDelay: '240ms' }}>
                    <div className="px-10 py-3.5 rounded-2xl bg-white/[0.03] border border-white/[0.05] text-neutral-600 font-black text-sm tracking-[0.1em] uppercase flex items-center gap-3">
                        <ShieldCheck className="w-5 h-5" /> CALCULATE YOUR SAFETY SCORE
                    </div>
                </div>
            )}
        </div>
    );
}

function fileToBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result.split(',')[1]);
        reader.onerror = reject;
        reader.readAsDataURL(file);
    });
}
