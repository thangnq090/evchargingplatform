import React, { useState, useEffect } from 'react';
import { adminApi } from '../api/adminApi';
import { PlatformIncomeSummary, GlobalMarkupConfig, Vendor } from '../types/admin.types';
import { GlobalMarkupCard } from '../components/GlobalMarkupCard';
import { Building2, DollarSign, Zap, TrendingUp, ArrowUpRight, AlertCircle } from 'lucide-react';

export const AdminDashboardPage: React.FC = () => {
  const [income, setIncome] = useState<PlatformIncomeSummary | null>(null);
  const [markup, setMarkup] = useState<GlobalMarkupConfig | null>(null);
  const [vendors, setVendors] = useState<Vendor[]>([]);
  const [period, setPeriod] = useState<'TODAY' | '7_DAYS' | '30_DAYS' | 'CUSTOM'>('30_DAYS');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadDashboardData = async () => {
      setError(null);
      try {
        const [inc, mk, vds] = await Promise.all([
          adminApi.getPlatformIncome(period),
          adminApi.getGlobalMarkup(),
          adminApi.getVendors(),
        ]);
        setIncome(inc);
        setMarkup(mk);
        setVendors(vds.items);
      } catch (err: any) {
        setError(err.message || 'Failed to load platform dashboard data from backend server.');
      }
    };
    loadDashboardData();
  }, [period]);

  const handleUpdateMarkup = async (percentage: number, cents: number) => {
    const updated = await adminApi.updateGlobalMarkup(percentage, cents);
    setMarkup(updated);
  };

  return (
    <div className="space-y-6 p-6">
      {/* Error Alert */}
      {error && (
        <div className="p-4 bg-red-950/80 border border-red-800 rounded-xl text-red-200 text-sm flex items-start gap-3 shadow-lg animate-in fade-in duration-200">
          <AlertCircle className="w-5 h-5 text-red-400 shrink-0 mt-0.5" />
          <div>
            <h4 className="font-semibold text-red-300">Backend Connection Error</h4>
            <p className="text-xs text-red-400 mt-1">{error}</p>
          </div>
        </div>
      )}
      {/* Header & Filter */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-100">Platform Admin Overview</h1>
          <p className="text-sm text-slate-400">
            Global income summary, platform markup controls, and vendor activity.
          </p>
        </div>

        <div className="flex items-center gap-1 bg-slate-900 border border-slate-800 p-1 rounded-xl self-start sm:self-auto">
          {(['TODAY', '7_DAYS', '30_DAYS', 'CUSTOM'] as const).map((p) => (
            <button
              key={p}
              onClick={() => setPeriod(p)}
              className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-all ${
                period === p
                  ? 'bg-cyan-500 text-slate-950 shadow-md shadow-cyan-500/20'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              {p.replace('_', ' ')}
            </button>
          ))}
        </div>
      </div>

      {/* KPI Cards */}
      {income && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-xl relative overflow-hidden group">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                Total Revenue
              </span>
              <div className="p-2 bg-emerald-950/60 border border-emerald-800/60 text-emerald-400 rounded-lg">
                <DollarSign className="w-4 h-4" />
              </div>
            </div>
            <div className="mt-3">
              <div className="text-2xl font-bold text-slate-100">
                ${(income.totalIncomeCents / 100).toLocaleString(undefined, { minimumFractionDigits: 2 })}
              </div>
              <div className="flex items-center gap-1 text-xs text-emerald-400 mt-1 font-medium">
                <ArrowUpRight className="w-3.5 h-3.5" />
                <span>+12.4% vs previous {period.toLowerCase().replace('_', ' ')}</span>
              </div>
            </div>
          </div>

          <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-xl relative overflow-hidden group">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                Energy Delivered
              </span>
              <div className="p-2 bg-cyan-950/60 border border-cyan-800/60 text-cyan-400 rounded-lg">
                <Zap className="w-4 h-4" />
              </div>
            </div>
            <div className="mt-3">
              <div className="text-2xl font-bold text-slate-100">
                {income.totalKwhDelivered.toLocaleString()} <span className="text-sm font-normal text-slate-400">kWh</span>
              </div>
              <div className="text-xs text-slate-400 mt-1">Across {income.totalSessions} sessions</div>
            </div>
          </div>

          <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-xl relative overflow-hidden group">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                Active Vendors
              </span>
              <div className="p-2 bg-purple-950/60 border border-purple-800/60 text-purple-400 rounded-lg">
                <Building2 className="w-4 h-4" />
              </div>
            </div>
            <div className="mt-3">
              <div className="text-2xl font-bold text-slate-100">{income.activeVendors}</div>
              <div className="text-xs text-slate-400 mt-1">Operating active chargepoints</div>
            </div>
          </div>

          <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-xl relative overflow-hidden group">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                Avg Profit / Session
              </span>
              <div className="p-2 bg-amber-950/60 border border-amber-800/60 text-amber-400 rounded-lg">
                <TrendingUp className="w-4 h-4" />
              </div>
            </div>
            <div className="mt-3">
              <div className="text-2xl font-bold text-slate-100">
                ${(income.averageProfitPerSessionCents / 100).toFixed(2)}
              </div>
              <div className="text-xs text-slate-400 mt-1">Net platform margin per session</div>
            </div>
          </div>
        </div>
      )}

      {/* Markup Config Card */}
      {markup && <GlobalMarkupCard config={markup} onUpdate={handleUpdateMarkup} />}

      {/* Vendor Table Snapshot */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-xl">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-base font-semibold text-slate-100 flex items-center gap-2">
            <Building2 className="w-5 h-5 text-cyan-400" />
            <span>Onboarded Vendors</span>
          </h3>
          <span className="text-xs text-slate-400 font-mono">{vendors.length} Total Vendors</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-300">
            <thead className="bg-slate-950 border-b border-slate-800 text-xs font-semibold text-slate-400 uppercase tracking-wider">
              <tr>
                <th className="px-4 py-3">Vendor Name</th>
                <th className="px-4 py-3">Business Reg / Tax ID</th>
                <th className="px-4 py-3">Contact Email</th>
                <th className="px-4 py-3">Chargepoints</th>
                <th className="px-4 py-3">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-mono text-xs">
              {vendors.map((v) => (
                <tr key={v.id} className="hover:bg-slate-800/30 transition-colors">
                  <td className="px-4 py-3 font-semibold text-slate-100 font-sans">{v.name}</td>
                  <td className="px-4 py-3 text-slate-400">{v.businessRegistrationNumber}</td>
                  <td className="px-4 py-3 text-slate-300">{v.contactEmail}</td>
                  <td className="px-4 py-3 text-cyan-400">{v.chargepointCount} CP</td>
                  <td className="px-4 py-3">
                    <span
                      className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                        v.status === 'ACTIVE'
                          ? 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                          : 'bg-amber-950 text-amber-400 border border-amber-800'
                      }`}
                    >
                      {v.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
