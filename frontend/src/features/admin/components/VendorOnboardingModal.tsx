import React, { useState } from 'react';
import { X, Building2, Mail, User, Hash } from 'lucide-react';
import { OnboardVendorPayload } from '../types/admin.types';

interface VendorOnboardingModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (payload: OnboardVendorPayload) => Promise<void>;
}

export const VendorOnboardingModal: React.FC<VendorOnboardingModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
}) => {
  const [formData, setFormData] = useState<OnboardVendorPayload>({
    name: '',
    businessRegistrationNumber: '',
    contactEmail: '',
    adminEmail: '',
    adminFullName: '',
  });
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await onSubmit(formData);
      onClose();
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
      <div className="bg-slate-900 border border-slate-800 rounded-xl shadow-2xl w-full max-w-lg overflow-hidden animate-in fade-in zoom-in-95 duration-200">
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800">
          <div className="flex items-center gap-2 text-cyan-400 font-semibold text-lg">
            <Building2 className="w-5 h-5" />
            <span>Onboard New Vendor</span>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-slate-200 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div>
            <label className="block text-xs font-medium text-slate-300 uppercase tracking-wider mb-1">
              Vendor / Organization Name
            </label>
            <div className="relative">
              <Building2 className="absolute left-3 top-2.5 w-4 h-4 text-slate-500" />
              <input
                type="text"
                required
                placeholder="e.g. Acme Charge Tech"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-9 pr-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 uppercase tracking-wider mb-1">
              Business Registration / Tax ID
            </label>
            <div className="relative">
              <Hash className="absolute left-3 top-2.5 w-4 h-4 text-slate-500" />
              <input
                type="text"
                required
                placeholder="e.g. REG-991823"
                value={formData.businessRegistrationNumber}
                onChange={(e) =>
                  setFormData({ ...formData, businessRegistrationNumber: e.target.value })
                }
                className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-9 pr-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 uppercase tracking-wider mb-1">
              Organization Contact Email
            </label>
            <div className="relative">
              <Mail className="absolute left-3 top-2.5 w-4 h-4 text-slate-500" />
              <input
                type="email"
                required
                placeholder="contact@vendor.com"
                value={formData.contactEmail}
                onChange={(e) => setFormData({ ...formData, contactEmail: e.target.value })}
                className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-9 pr-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
              />
            </div>
          </div>

          <div className="pt-2 border-t border-slate-800">
            <h4 className="text-xs font-semibold text-cyan-400 uppercase tracking-wider mb-3">
              Initial Vendor Admin Details
            </h4>

            <div className="space-y-3">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">
                  Full Name
                </label>
                <div className="relative">
                  <User className="absolute left-3 top-2.5 w-4 h-4 text-slate-500" />
                  <input
                    type="text"
                    required
                    placeholder="Jane Doe"
                    value={formData.adminFullName}
                    onChange={(e) =>
                      setFormData({ ...formData, adminFullName: e.target.value })
                    }
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-9 pr-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">
                  Admin Email (Invite Target)
                </label>
                <div className="relative">
                  <Mail className="absolute left-3 top-2.5 w-4 h-4 text-slate-500" />
                  <input
                    type="email"
                    required
                    placeholder="admin@vendor.com"
                    value={formData.adminEmail}
                    onChange={(e) =>
                      setFormData({ ...formData, adminEmail: e.target.value })
                    }
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-9 pr-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
                  />
                </div>
              </div>
            </div>
          </div>

          <div className="flex justify-end gap-3 pt-4 border-t border-slate-800">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium text-slate-300 hover:text-slate-100 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 text-sm font-medium bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white rounded-lg shadow-lg shadow-cyan-500/20 transition-all disabled:opacity-50"
            >
              {loading ? 'Onboarding...' : 'Send Vendor Invitation'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
