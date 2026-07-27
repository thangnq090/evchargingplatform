import React, { useState } from "react";
import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";
import { Zap, Mail, Lock, AlertCircle, ArrowRight } from "lucide-react";

export function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const { login, isLoading, error } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await login(email, password);
      const currentUser = useAuth.getState().user;
      if (currentUser?.role === "ROLE_CUSTOMER") {
        navigate("/customer/profile");
      } else if (currentUser?.role === "ROLE_VENDOR_ADMIN") {
        navigate("/vendor/portal");
      } else if (currentUser?.role === "ROLE_VENDOR_USER") {
        navigate("/vendor/operations");
      } else {
        navigate("/admin/dashboard");
      }
    } catch {
      // Error handled by store state
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center p-4 antialiased">
      <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl p-8 shadow-2xl space-y-6">
        {/* Brand Header */}
        <div className="text-center space-y-2">
          <div className="inline-flex p-3 bg-gradient-to-tr from-cyan-500 to-blue-600 rounded-2xl shadow-xl shadow-cyan-500/20 mb-2">
            <Zap className="w-8 h-8 text-slate-950 fill-slate-950" />
          </div>
          <h1 className="text-2xl font-bold text-slate-100 tracking-tight">
            EV Charging Platform
          </h1>
          <p className="text-xs text-slate-400">
            Sign in to access your role-based platform dashboard
          </p>
        </div>

        {/* Error Alert */}
        {error && (
          <div className="p-3 bg-red-950/80 border border-red-800 rounded-xl text-red-300 text-xs flex items-start gap-2.5 animate-in fade-in duration-200">
            <AlertCircle className="w-4 h-4 text-red-400 shrink-0 mt-0.5" />
            <div>
              <span className="font-semibold block">Authentication Error</span>
              <span>{error}</span>
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
              Email Address
            </label>
            <div className="relative">
              <Mail className="absolute left-3.5 top-3 w-4 h-4 text-slate-500" />
              <input
                type="email"
                required
                placeholder="admin@evcharging.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-10 pr-4 py-2.5 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500 transition-colors"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
              Password
            </label>
            <div className="relative">
              <Lock className="absolute left-3.5 top-3 w-4 h-4 text-slate-500" />
              <input
                type="password"
                required
                placeholder="••••••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-10 pr-4 py-2.5 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500 transition-colors"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full flex items-center justify-center gap-2 py-3 text-sm font-semibold bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white rounded-xl shadow-lg shadow-cyan-500/25 transition-all disabled:opacity-50 mt-2"
          >
            <span>{isLoading ? "Authenticating..." : "Sign In to Platform"}</span>
            {!isLoading && <ArrowRight className="w-4 h-4" />}
          </button>
        </form>

        <div className="text-center pt-2 border-t border-slate-800/80 space-y-1">
          <div className="text-xs text-slate-400">
            EV Driver?{" "}
            <a href="/register" className="text-cyan-400 font-semibold hover:underline">
              Create Customer Account
            </a>
          </div>
          <span className="block text-[11px] text-slate-500">
            Secured by OAuth 2.0 / JWT Modular Identity Service
          </span>
        </div>
      </div>
    </div>
  );
}
