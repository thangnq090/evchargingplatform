import React, { useState, useEffect } from 'react';
import { adminApi } from '../api/adminApi';
import { UserAccount } from '../types/admin.types';
import { Users, Lock, Unlock, KeyRound, ShieldAlert, CheckCircle2, AlertCircle } from 'lucide-react';

export const UserGovernancePage: React.FC = () => {
  const [users, setUsers] = useState<UserAccount[]>([]);
  const [filterRole, setFilterRole] = useState<string>('ALL');
  const [notification, setNotification] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setError(null);
    adminApi.getUsers().then(setUsers).catch((err) => {
      setError(err.message || 'Failed to fetch system users from backend');
    });
  }, []);

  const handleToggleStatus = async (user: UserAccount) => {
    try {
      const updated = await adminApi.toggleUserStatus(user.id);
      setUsers((prev) => prev.map((u) => (u.id === updated.id ? updated : u)));
      setNotification(`Account ${updated.email} is now ${updated.status}`);
      setTimeout(() => setNotification(null), 3000);
    } catch (err) {
      console.error(err);
    }
  };

  const handleResetPassword = async (user: UserAccount) => {
    try {
      const res = await adminApi.triggerPasswordReset(user.id);
      setNotification(res.message);
      setTimeout(() => setNotification(null), 4000);
    } catch (err) {
      console.error(err);
    }
  };

  const filteredUsers =
    filterRole === 'ALL' ? users : users.filter((u) => u.role === filterRole);

  const getRoleBadge = (role: string) => {
    switch (role) {
      case 'ADMIN':
        return (
          <span className="px-2 py-0.5 text-[10px] font-semibold bg-red-950 text-red-400 border border-red-800 rounded-full">
            Platform Admin
          </span>
        );
      case 'VENDOR_ADMIN':
        return (
          <span className="px-2 py-0.5 text-[10px] font-semibold bg-purple-950 text-purple-400 border border-purple-800 rounded-full">
            Vendor Admin
          </span>
        );
      case 'VENDOR_USER':
        return (
          <span className="px-2 py-0.5 text-[10px] font-semibold bg-cyan-950 text-cyan-400 border border-cyan-800 rounded-full">
            Vendor Operator
          </span>
        );
      case 'CUSTOMER':
        return (
          <span className="px-2 py-0.5 text-[10px] font-semibold bg-blue-950 text-blue-400 border border-blue-800 rounded-full">
            Customer
          </span>
        );
    }
  };

  return (
    <div className="space-y-6 p-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-100 flex items-center gap-2">
            <Users className="w-6 h-6 text-cyan-400" />
            <span>User & Account Governance</span>
          </h1>
          <p className="text-sm text-slate-400">
            System-wide account status controls, RBAC inspection, and password resets.
          </p>
        </div>

        {notification && (
          <div className="flex items-center gap-2 px-4 py-2 bg-cyan-950 border border-cyan-800 text-cyan-300 rounded-xl text-xs font-semibold animate-in fade-in duration-200">
            <CheckCircle2 className="w-4 h-4 text-cyan-400" />
            <span>{notification}</span>
          </div>
        )}
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
      <div className="flex items-center gap-2 bg-slate-900 border border-slate-800 p-1.5 rounded-xl w-fit">
        {['ALL', 'ADMIN', 'VENDOR_ADMIN', 'VENDOR_USER', 'CUSTOMER'].map((r) => (
          <button
            key={r}
            onClick={() => setFilterRole(r)}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-all ${
              filterRole === r
                ? 'bg-cyan-500 text-slate-950 shadow-md'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            {r === 'ALL' ? 'All Roles' : r.replace('_', ' ')}
          </button>
        ))}
      </div>

      {/* Users Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-300">
            <thead className="bg-slate-950 border-b border-slate-800 text-xs font-semibold text-slate-400 uppercase tracking-wider">
              <tr>
                <th className="px-4 py-3">Full Name & Email</th>
                <th className="px-4 py-3">Role</th>
                <th className="px-4 py-3">Tenant Scope</th>
                <th className="px-4 py-3">Account Status</th>
                <th className="px-4 py-3 text-right">Governance Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-mono text-xs">
              {filteredUsers.map((u) => (
                <tr key={u.id} className="hover:bg-slate-800/30 transition-colors">
                  <td className="px-4 py-3">
                    <div className="font-semibold text-slate-100 font-sans">{u.fullName}</div>
                    <div className="text-slate-400 font-mono text-[11px]">{u.email}</div>
                  </td>
                  <td className="px-4 py-3">{getRoleBadge(u.role)}</td>
                  <td className="px-4 py-3 text-slate-300 font-sans">
                    {u.vendorName ? u.vendorName : <span className="text-slate-500">Global System</span>}
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold ${
                        u.status === 'ACTIVE'
                          ? 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                          : 'bg-red-950 text-red-400 border border-red-800'
                      }`}
                    >
                      {u.status === 'ACTIVE' ? <CheckCircle2 className="w-3 h-3" /> : <ShieldAlert className="w-3 h-3" />}
                      {u.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <div className="flex items-center justify-end gap-2">
                      <button
                        onClick={() => handleToggleStatus(u)}
                        title={u.status === 'ACTIVE' ? 'Lock Account' : 'Unlock Account'}
                        className={`p-1.5 rounded-lg border transition-colors ${
                          u.status === 'ACTIVE'
                            ? 'bg-slate-950 border-slate-800 text-amber-400 hover:bg-amber-950/40'
                            : 'bg-slate-950 border-slate-800 text-emerald-400 hover:bg-emerald-950/40'
                        }`}
                      >
                        {u.status === 'ACTIVE' ? <Lock className="w-4 h-4" /> : <Unlock className="w-4 h-4" />}
                      </button>

                      <button
                        onClick={() => handleResetPassword(u)}
                        title="Trigger Password Reset Email"
                        className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-950 border border-slate-800 text-cyan-400 hover:bg-cyan-950/40 transition-colors font-sans text-xs font-medium"
                      >
                        <KeyRound className="w-3.5 h-3.5" />
                        <span>Reset Password</span>
                      </button>
                    </div>
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
