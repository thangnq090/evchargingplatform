import React, { useState } from 'react';
import { DollarSign, Percent, Save, CheckCircle2 } from 'lucide-react';
import { GlobalMarkupConfig } from '../types/admin.types';

interface GlobalMarkupCardProps {
  config: GlobalMarkupConfig;
  onUpdate: (percentage: number, cents: number) => Promise<void>;
}

export const GlobalMarkupCard: React.FC<GlobalMarkupCardProps> = ({ config, onUpdate }) => {
  const [percentage, setPercentage] = useState(config.percentageMarkup);
  const [cents, setCents] = useState(config.fixedCentsPerKwh);
  const [saving, setSaving] = useState(false);
  const [successMsg, setSuccessMsg] = useState(false);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await onUpdate(percentage, cents);
      setSuccessMsg(true);
      setTimeout(() => setSuccessMsg(false), 3000);
    } catch (err) {
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-xl">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h3 className="text-base font-semibold text-slate-100 flex items-center gap-2">
            <DollarSign className="w-5 h-5 text-cyan-400" />
            <span>Platform Global Markup Rate</span>
          </h3>
          <p className="text-xs text-slate-400 mt-1">
            Markup rates added automatically to vendor base tariffs on customer sessions.
          </p>
        </div>
        {successMsg && (
          <span className="flex items-center gap-1 text-xs text-emerald-400 bg-emerald-950/60 border border-emerald-800 px-3 py-1 rounded-full animate-in fade-in duration-200">
            <CheckCircle2 className="w-3.5 h-3.5" /> Markup Saved
          </span>
        )}
      </div>

      <form onSubmit={handleSave} className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <label className="block text-xs font-medium text-slate-300 uppercase tracking-wider mb-1">
            Percentage Markup (%)
          </label>
          <div className="relative">
            <Percent className="absolute left-3 top-2.5 w-4 h-4 text-slate-500" />
            <input
              type="number"
              step="0.1"
              min="0"
              max="100"
              value={percentage}
              onChange={(e) => setPercentage(parseFloat(e.target.value) || 0)}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-9 pr-3 py-2 text-sm text-slate-100 focus:outline-none focus:border-cyan-500"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-medium text-slate-300 uppercase tracking-wider mb-1">
            Fixed Markup (Cents / kWh)
          </label>
          <div className="relative">
            <DollarSign className="absolute left-3 top-2.5 w-4 h-4 text-slate-500" />
            <input
              type="number"
              min="0"
              value={cents}
              onChange={(e) => setCents(parseInt(e.target.value, 10) || 0)}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-9 pr-3 py-2 text-sm text-slate-100 focus:outline-none focus:border-cyan-500"
            />
          </div>
        </div>

        <div className="sm:col-span-2 flex items-center justify-between pt-3 border-t border-slate-800/80">
          <span className="text-xs text-slate-500">
            Last updated: {new Date(config.updatedAt).toLocaleString()} by {config.updatedBy}
          </span>
          <button
            type="submit"
            disabled={saving}
            className="flex items-center gap-2 px-4 py-2 text-sm font-medium bg-slate-800 hover:bg-slate-700 text-cyan-400 border border-cyan-500/30 rounded-lg transition-all disabled:opacity-50"
          >
            <Save className="w-4 h-4" />
            {saving ? 'Saving...' : 'Update Markup Rules'}
          </button>
        </div>
      </form>
    </div>
  );
};
