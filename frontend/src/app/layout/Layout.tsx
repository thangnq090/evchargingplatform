import { Outlet, Link, useLocation, useNavigate } from "react-router-dom";
import { GlobalFtsSearchBar } from "../../features/admin/components/GlobalFtsSearchBar";
import { useAuth } from "../../features/auth/hooks/useAuth";
import { Zap, LayoutDashboard, Building2, Users, Shield, LogOut } from "lucide-react";

export function Layout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  const navItems = [
    { label: "Admin Overview", path: "/admin/dashboard", icon: LayoutDashboard },
    { label: "Vendor Management", path: "/admin/vendors", icon: Building2 },
    { label: "Vendor Portal", path: "/vendor/portal", icon: Zap },
    { label: "User Governance", path: "/admin/users", icon: Users },
  ];

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 font-sans flex flex-col antialiased">
      {/* Header */}
      <header className="bg-slate-900/90 backdrop-blur-md border-b border-slate-800/80 sticky top-0 z-40">
        <div className="mx-auto max-w-7xl px-4 py-3 sm:px-6 lg:px-8 flex items-center justify-between gap-4">
          {/* Logo & Navigation */}
          <div className="flex items-center gap-8">
            <Link to="/admin/dashboard" className="flex items-center gap-2.5 group">
              <div className="p-2 bg-gradient-to-tr from-cyan-500 to-blue-600 rounded-xl shadow-lg shadow-cyan-500/20 group-hover:scale-105 transition-transform">
                <Zap className="w-5 h-5 text-slate-950 fill-slate-950" />
              </div>
              <div>
                <span className="text-base font-bold tracking-tight bg-gradient-to-r from-slate-100 to-slate-300 bg-clip-text text-transparent">
                  EV Charging Platform
                </span>
                <span className="block text-[10px] font-mono text-cyan-400 font-semibold uppercase tracking-widest">
                  Admin Portal
                </span>
              </div>
            </Link>

            <nav className="hidden md:flex items-center gap-1">
              {navItems.map((item) => {
                const Icon = item.icon;
                const isActive = location.pathname === item.path;
                return (
                  <Link
                    key={item.path}
                    to={item.path}
                    className={`flex items-center gap-2 px-3 py-2 rounded-lg text-xs font-semibold transition-all ${
                      isActive
                        ? "bg-slate-800 text-cyan-400 border border-cyan-500/30"
                        : "text-slate-400 hover:text-slate-200 hover:bg-slate-800/50"
                    }`}
                  >
                    <Icon className="w-4 h-4" />
                    <span>{item.label}</span>
                  </Link>
                );
              })}
            </nav>
          </div>

          {/* Global FTS Search Bar */}
          <GlobalFtsSearchBar />

          {/* User Profile & Logout */}
          <div className="hidden sm:flex items-center gap-3">
            <div className="flex items-center gap-2 px-3 py-1.5 bg-slate-950 border border-slate-800 rounded-xl">
              <Shield className="w-4 h-4 text-cyan-400" />
              <div className="text-left">
                <span className="block text-xs font-semibold text-slate-200">
                  {user?.fullName || user?.email || "Authenticated User"}
                </span>
                <span className="block text-[10px] font-mono text-slate-400">
                  {user?.role || "ROLE_USER"}
                </span>
              </div>
            </div>

            <button
              onClick={handleLogout}
              title="Sign Out"
              className="p-2 rounded-xl bg-slate-950 border border-slate-800 text-slate-400 hover:text-red-400 hover:border-red-900/50 transition-colors"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="mx-auto max-w-7xl w-full flex-1 px-4 py-6 sm:px-6 lg:px-8">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-800/60 bg-slate-950 py-4 text-center text-xs text-slate-500">
        EV Charging Platform &copy; 2026 — Platform Governance & Admin Suite
      </footer>
    </div>
  );
}
