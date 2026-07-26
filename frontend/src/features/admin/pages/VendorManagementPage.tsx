import React, { useState, useEffect } from 'react';
import { adminApi } from '../api/adminApi';
import { Vendor, OnboardVendorPayload } from '../types/admin.types';
import { VendorOnboardingModal } from '../components/VendorOnboardingModal';
import { Building2, Plus, CheckCircle2, Search, AlertCircle } from 'lucide-react';

export const VendorManagementPage: React.FC = () => {
  const [vendors, setVendors] = useState<Vendor[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [search, setSearch] = useState('');
  const [toast, setToast] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setError(null);
    adminApi.getVendors().then((res) => setVendors(res.items)).catch((err) => {
      setError(err.message || 'Failed to fetch vendor list from backend');
    });
  }, []);

  const handleOnboard = async (payload: OnboardVendorPayload) => {
    const created = await adminApi.onboardVendor(payload);
    setVendors((prev) => [created, ...prev]);
    setToast(`Successfully onboarded vendor '${created.name}' & sent invitation to ${payload.adminEmail}`);
    setTimeout(() => setToast(null), 4000);
  };

  const filtered = vendors.filter(
    (v) =>
      v.name?.toLowerCase().includes(search.toLowerCase()) ||
      v.businessRegistrationNumber?.toLowerCase().includes(search.toLowerCase()) ||
      v.contactEmail?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6 p-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-100 flex items-center gap-2">
            <Building2 className="w-6 h-6 text-cyan-400" />
            <span>Vendor Partner Management</span>
          </h1>
          <p className="text-sm text-slate-400">
            Onboard new charging station vendors and manage original Vendor Admin invitations.
          </p>
        </div>

        <button
          onClick={() => setIsModalOpen(true)}
          className="flex items-center gap-2 px-4 py-2 text-sm font-medium bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white rounded-xl shadow-lg shadow-cyan-500/20 transition-all self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>Onboard New Vendor</span>
        </button>
      </div>

      {error && (
        <div className="p-4 bg-red-950/80 border border-red-800 rounded-xl text-red-200 text-sm flex items-start gap-3 shadow-lg animate-in fade-in duration-200">
          <AlertCircle className="w-5 h-5 text-red-400 shrink-0 mt-0.5" />
          <div>
            <h4 className="font-semibold text-red-300">Backend API Error</h4>
            <p className="text-xs text-red-400 mt-1">{error}</p>
          </div>
        </div>
      )}

      {toast && (
        <div className="flex items-center gap-2 px-4 py-3 bg-emerald-950/80 border border-emerald-800 text-emerald-300 rounded-xl text-xs font-semibold animate-in fade-in duration-200">
          <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          <span>{toast}</span>
        </div>
      )}

      {/* Filter & Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-xl space-y-4">
        <div className="relative max-w-xs">
          <Search className="absolute left-3 top-2.5 w-4 h-4 text-slate-500" />
          <input
            type="text"
            placeholder="Search vendor name, ID, email..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-9 pr-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
          />
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-300">
            <thead className="bg-slate-950 border-b border-slate-800 text-xs font-semibold text-slate-400 uppercase tracking-wider">
              <tr>
                <th className="px-4 py-3">Vendor Name</th>
                <th className="px-4 py-3">Tax / Registration ID</th>
                <th className="px-4 py-3">Contact Email</th>
                <th className="px-4 py-3">Active Chargepoints</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3">Onboarded Date</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-mono text-xs">
              {filtered.map((v) => (
                <tr key={v.id} className="hover:bg-slate-800/30 transition-colors">
                  <td className="px-4 py-3 font-semibold text-slate-100 font-sans">{v.name}</td>
                  <td className="px-4 py-3 text-slate-400">{v.businessRegistrationNumber}</td>
                  <td className="px-4 py-3 text-slate-300">{v.contactEmail}</td>
                  <td className="px-4 py-3 text-cyan-400">{v.chargepointCount} Chargepoints</td>
                  <td className="px-4 py-3">
                    <span
                      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-[10px] font-semibold ${
                        v.status === 'ACTIVE'
                          ? 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                          : 'bg-amber-950 text-amber-400 border border-amber-800'
                      }`}
                    >
                      {v.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-slate-500 font-sans">
                    {new Date(v.createdAt).toLocaleDateString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <VendorOnboardingModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleOnboard}
      />
    </div>
  );
};
