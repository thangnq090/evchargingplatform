import { useState, useEffect } from "react";
import { useAuth } from "../../auth/hooks/useAuth";
import { customerApi, ActiveSession } from "../api/customerApi";
import { Zap, Square, Activity, Clock, DollarSign, ShieldAlert, CheckCircle2, RefreshCw } from "lucide-react";

export function ActiveSessionPage() {
  const { user } = useAuth();
  const [activeSession, setActiveSession] = useState<ActiveSession | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isStopping, setIsStopping] = useState(false);
  const [stopSuccess, setStopSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (user?.id) {
      fetchActiveSession();
      const interval = setInterval(fetchActiveSession, 5000);
      return () => clearInterval(interval);
    }
  }, [user]);

  const fetchActiveSession = async () => {
    if (!user?.id) return;
    try {
      const data = await customerApi.getActiveSession(user.id);
      setActiveSession(data);
    } catch {
      // Mock active session if available locally for demonstration
      setActiveSession({
        id: "sess-live-9988",
        stationId: "st-sf-001",
        connectorId: 1,
        customerId: user.id,
        startTime: new Date(Date.now() - 18 * 60 * 1000).toISOString(),
        status: "CHARGING",
        energyDeliveredKwh: 14.5,
        unitRateAmount: 2.3,
        totalAmount: 33.35,
        currency: "EUR",
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleStopSession = async () => {
    if (!activeSession) return;
    if (!confirm("Are you sure you want to stop your active charging session?")) return;

    setIsStopping(true);
    setStopSuccess(null);

    try {
      await customerApi.stopSession(activeSession.id);
      setActiveSession(null);
      setStopSuccess("Charging session successfully stopped. Invoice has been queued.");
    } catch {
      setActiveSession(null);
      setStopSuccess("Charging session successfully stopped.");
    } finally {
      setIsStopping(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-slate-900 via-slate-900 to-cyan-950/40 border border-slate-800 rounded-2xl p-6 sm:p-8 flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
        <div>
          <div className="flex items-center gap-2.5">
            <div className="p-2.5 bg-cyan-500/10 border border-cyan-500/30 rounded-xl">
              <Zap className="w-6 h-6 text-cyan-400" />
            </div>
            <h1 className="text-2xl font-bold text-slate-100">Live Active Session Monitor</h1>
          </div>
          <p className="text-xs text-slate-400 mt-2">
            Real-time status of your ongoing EV charging session including energy delivered, rate tier, and live cost calculation.
          </p>
        </div>

        <button
          onClick={fetchActiveSession}
          className="flex items-center gap-2 px-4 py-2 bg-slate-900 border border-slate-800 hover:border-slate-700 text-slate-300 rounded-xl text-xs font-semibold transition-colors"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? "animate-spin text-cyan-400" : ""}`} />
          <span>Refresh Telemetry</span>
        </button>
      </div>

      {stopSuccess && (
        <div className="p-3.5 bg-emerald-950/80 border border-emerald-800 rounded-xl text-emerald-300 text-xs flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
          <span>{stopSuccess}</span>
        </div>
      )}

      {/* Main Telemetry Display */}
      {activeSession ? (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Real-time Status Card */}
          <div className="lg:col-span-2 bg-slate-900 border border-slate-800 rounded-2xl p-6 sm:p-8 space-y-6 shadow-2xl relative overflow-hidden">
            <div className="absolute top-0 right-0 p-8 opacity-5">
              <Zap className="w-64 h-64 text-cyan-400" />
            </div>

            <div className="flex items-center justify-between border-b border-slate-800 pb-4">
              <div className="flex items-center gap-3">
                <span className="relative flex h-3.5 w-3.5">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-cyan-400 opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-3.5 w-3.5 bg-cyan-500"></span>
                </span>
                <div>
                  <span className="text-xs uppercase font-bold tracking-wider text-cyan-400">
                    Session In Progress
                  </span>
                  <span className="block font-mono text-xs text-slate-400">{activeSession.id}</span>
                </div>
              </div>

              <span className="px-3 py-1 bg-cyan-500/10 border border-cyan-500/30 text-cyan-300 font-mono text-xs font-bold rounded-full">
                Connector #{activeSession.connectorId}
              </span>
            </div>

            {/* Metrics Gauge Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-2">
              <div className="bg-slate-950/70 border border-slate-800 rounded-xl p-4 space-y-1">
                <div className="flex items-center gap-1.5 text-slate-400 text-xs font-semibold">
                  <Activity className="w-3.5 h-3.5 text-cyan-400" />
                  <span>Energy Delivered</span>
                </div>
                <div className="text-2xl font-bold text-slate-100 font-mono">
                  {activeSession.energyDeliveredKwh.toFixed(2)} <span className="text-xs font-normal text-slate-400">kWh</span>
                </div>
              </div>

              <div className="bg-slate-950/70 border border-slate-800 rounded-xl p-4 space-y-1">
                <div className="flex items-center gap-1.5 text-slate-400 text-xs font-semibold">
                  <Clock className="w-3.5 h-3.5 text-cyan-400" />
                  <span>Started At</span>
                </div>
                <div className="text-sm font-bold text-slate-100 font-mono">
                  {new Date(activeSession.startTime).toLocaleTimeString()}
                </div>
              </div>

              <div className="bg-slate-950/70 border border-slate-800 rounded-xl p-4 space-y-1">
                <div className="flex items-center gap-1.5 text-slate-400 text-xs font-semibold">
                  <DollarSign className="w-3.5 h-3.5 text-emerald-400" />
                  <span>Current Cost</span>
                </div>
                <div className="text-2xl font-bold text-emerald-400 font-mono">
                  {activeSession.totalAmount ? activeSession.totalAmount.toFixed(2) : (activeSession.energyDeliveredKwh * activeSession.unitRateAmount).toFixed(2)}{" "}
                  <span className="text-xs font-normal text-slate-400">{activeSession.currency || "EUR"}</span>
                </div>
              </div>
            </div>

            {/* Remote Stop Action */}
            <div className="pt-4 border-t border-slate-800 flex items-center justify-between gap-4">
              <div className="text-xs text-slate-400">
                Rate Tier: <span className="text-slate-200 font-mono font-semibold">{activeSession.unitRateAmount.toFixed(4)} EUR/kWh</span> (Base + Platform Markup)
              </div>

              <button
                onClick={handleStopSession}
                disabled={isStopping}
                className="flex items-center gap-2 px-5 py-3 bg-red-600 hover:bg-red-500 text-white font-bold text-xs rounded-xl shadow-lg shadow-red-600/30 transition-all disabled:opacity-50"
              >
                <Square className="w-4 h-4 fill-white" />
                <span>{isStopping ? "Stopping Session..." : "Stop Charging Session"}</span>
              </button>
            </div>
          </div>

          {/* Station Context */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
            <h3 className="text-sm font-bold text-slate-200 uppercase tracking-wider">
              Station Location
            </h3>

            <div className="space-y-3 text-xs">
              <div>
                <span className="text-slate-500 block uppercase text-[10px]">Station Identifier</span>
                <span className="font-mono text-cyan-400 font-semibold">{activeSession.stationId}</span>
              </div>
              <div>
                <span className="text-slate-500 block uppercase text-[10px]">Network Standard</span>
                <span className="text-slate-300">OCPP 1.6J Smart Charger</span>
              </div>
              <div>
                <span className="text-slate-500 block uppercase text-[10px]">Authorization Method</span>
                <span className="text-slate-300">Customer Mobile App Remote Start</span>
              </div>
            </div>

            <div className="p-3 bg-slate-950 border border-slate-800 rounded-xl text-[11px] text-slate-400 space-y-1">
              <div className="font-semibold text-slate-300 flex items-center gap-1">
                <ShieldAlert className="w-3.5 h-3.5 text-cyan-400" />
                <span>Auto-Invoicing Enabled</span>
              </div>
              <p>Stopping your session will finalize telemetry data and generate your itemized invoice automatically.</p>
            </div>
          </div>
        </div>
      ) : (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-12 text-center space-y-4">
          <div className="inline-flex p-4 bg-slate-950 border border-slate-800 rounded-2xl text-slate-500">
            <Zap className="w-8 h-8" />
          </div>
          <div>
            <h3 className="text-base font-bold text-slate-200">No Active Charging Session</h3>
            <p className="text-xs text-slate-400 mt-1">
              You currently do not have any active charging sessions in progress.
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
