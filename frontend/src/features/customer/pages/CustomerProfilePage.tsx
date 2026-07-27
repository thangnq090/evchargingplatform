import { useState, useEffect } from "react";
import { useAuth } from "../../auth/hooks/useAuth";
import { customerApi, Vehicle } from "../api/customerApi";
import { User, Shield, CreditCard, Car, RefreshCw, Mail } from "lucide-react";

export function CustomerProfilePage() {
  const { user } = useAuth();
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (user?.id) {
      loadVehicles();
    }
  }, [user]);

  const loadVehicles = async () => {
    setIsLoading(true);
    try {
      if (user?.id) {
        const data = await customerApi.getVehicles(user.id);
        setVehicles(data);
      }
    } catch {
      // Mock empty list on error/no backend
    } finally {
      setIsLoading(false);
    }
  };

  const activeVehicles = vehicles.filter((v) => v.status === "ACTIVE");

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-slate-900 via-slate-900 to-cyan-950/40 border border-slate-800 rounded-2xl p-6 sm:p-8 flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
        <div className="flex items-center gap-4">
          <div className="p-4 bg-cyan-500/10 border border-cyan-500/30 rounded-2xl">
            <User className="w-8 h-8 text-cyan-400" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-2xl font-bold text-slate-100">{user?.fullName || "EV Driver"}</h1>
              <span className="px-2.5 py-0.5 text-[10px] font-mono font-bold bg-cyan-500/10 border border-cyan-500/30 text-cyan-400 rounded-full">
                ROLE_CUSTOMER
              </span>
            </div>
            <p className="text-xs text-slate-400 mt-1">EV Charging Driver Self-Service Account</p>
          </div>
        </div>

        <button
          onClick={loadVehicles}
          className="flex items-center gap-2 px-4 py-2 bg-slate-900 border border-slate-800 hover:border-slate-700 text-slate-300 rounded-xl text-xs font-semibold transition-colors"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? "animate-spin text-cyan-400" : ""}`} />
          <span>Refresh Profile</span>
        </button>
      </div>

      {/* Profile Overview Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* Account Details Card */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-4">
          <div className="flex items-center gap-2 text-slate-300 font-semibold text-sm">
            <CreditCard className="w-4 h-4 text-cyan-400" />
            <span>Account Details</span>
          </div>
          <div className="space-y-3 text-xs">
            <div>
              <span className="text-slate-500 block uppercase text-[10px] font-semibold">Account Number</span>
              <span className="font-mono text-cyan-400 font-bold text-sm">
                ACC-CUST-{(user?.id || "999").substring(0, 8).toUpperCase()}
              </span>
            </div>
            <div>
              <span className="text-slate-500 block uppercase text-[10px] font-semibold">Customer ID</span>
              <span className="font-mono text-slate-300 truncate block">{user?.id || "N/A"}</span>
            </div>
            <div>
              <span className="text-slate-500 block uppercase text-[10px] font-semibold">Email Address</span>
              <div className="flex items-center gap-1.5 text-slate-200 mt-0.5">
                <Mail className="w-3.5 h-3.5 text-slate-500" />
                <span>{user?.email || "driver@evcharging.test"}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Registered Vehicles Summary */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-4">
          <div className="flex items-center gap-2 text-slate-300 font-semibold text-sm">
            <Car className="w-4 h-4 text-cyan-400" />
            <span>Vehicles & Cards</span>
          </div>
          <div className="space-y-3 text-xs">
            <div className="flex justify-between items-center py-1 border-b border-slate-800/60">
              <span className="text-slate-400">Total Registered Vehicles</span>
              <span className="font-bold text-slate-100">{vehicles.length}</span>
            </div>
            <div className="flex justify-between items-center py-1 border-b border-slate-800/60">
              <span className="text-slate-400">Active Fleet Vehicles</span>
              <span className="font-bold text-emerald-400">{activeVehicles.length}</span>
            </div>
            <div className="flex justify-between items-center py-1">
              <span className="text-slate-400">Assigned RFID Cards</span>
              <span className="font-bold text-cyan-400">
                {vehicles.filter((v) => v.rfidTagNumber).length}
              </span>
            </div>
          </div>
        </div>

        {/* Security & Access */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-4">
          <div className="flex items-center gap-2 text-slate-300 font-semibold text-sm">
            <Shield className="w-4 h-4 text-cyan-400" />
            <span>Security & Auth</span>
          </div>
          <div className="space-y-3 text-xs">
            <div>
              <span className="text-slate-500 block uppercase text-[10px] font-semibold">Authentication Standard</span>
              <span className="text-slate-300">RS256 JWT Signed Auth Token</span>
            </div>
            <div>
              <span className="text-slate-500 block uppercase text-[10px] font-semibold">Account Status</span>
              <span className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 mt-1">
                Active & Verified
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
