import React, { useState } from "react";
import { OperationalCharger, ChargerStatus, SSEEvent } from "../types/vendorUserTypes";
import { Zap, Activity, AlertTriangle, Wrench, Search, Radio, Filter, Power, Layers } from "lucide-react";

interface RealtimeMonitorGridProps {
  chargers: OperationalCharger[];
  isLiveStreamActive: boolean;
  onToggleLiveStream: () => void;
  eventLogs: SSEEvent[];
  onOpenMaintenanceModal: (charger: OperationalCharger) => void;
  onOpenGroupMaintenanceModal: (groupTag: string) => void;
}

export const RealtimeMonitorGrid: React.FC<RealtimeMonitorGridProps> = ({
  chargers,
  isLiveStreamActive,
  onToggleLiveStream,
  eventLogs,
  onOpenMaintenanceModal,
  onOpenGroupMaintenanceModal,
}) => {
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedStatus, setSelectedStatus] = useState<string>("ALL");
  const [selectedGroupTag, setSelectedGroupTag] = useState<string>("ALL");

  // Filter options
  const groupTags = Array.from(new Set(chargers.map((c) => c.groupTag)));

  const filteredChargers = chargers.filter((c) => {
    const matchesSearch =
      c.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      c.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
      c.stationName.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = selectedStatus === "ALL" || c.status === selectedStatus;
    const matchesGroup = selectedGroupTag === "ALL" || c.groupTag === selectedGroupTag;
    return matchesSearch && matchesStatus && matchesGroup;
  });

  // KPI calculations
  const totalCount = chargers.length;
  const chargingCount = chargers.filter((c) => c.status === "CHARGING").length;
  const availableCount = chargers.filter((c) => c.status === "AVAILABLE").length;
  const maintenanceCount = chargers.filter((c) => c.status === "MAINTENANCE").length;
  const faultedCount = chargers.filter((c) => c.status === "FAULTED").length;

  const totalActivePowerKw = chargers
    .reduce((acc, c) => acc + (c.currentPowerKw || 0), 0)
    .toFixed(1);

  const getStatusBadge = (status: ChargerStatus) => {
    switch (status) {
      case "AVAILABLE":
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">AVAILABLE</span>;
      case "CHARGING":
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 flex items-center gap-1"><Zap className="w-3 h-3 animate-pulse" /> CHARGING</span>;
      case "MAINTENANCE":
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/10 text-amber-400 border border-amber-500/20">MAINTENANCE</span>;
      case "FAULTED":
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-rose-500/10 text-rose-400 border border-rose-500/20">FAULTED</span>;
      default:
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-slate-500/10 text-slate-400 border border-slate-500/20">UNAVAILABLE</span>;
    }
  };

  return (
    <div className="space-y-6">
      {/* KPI Cards & Live SSE Status Banner */}
      <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
        <div className="bg-slate-900 border border-slate-800 p-4 rounded-2xl">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold mb-1">
            <span>Total Chargers</span>
            <Power className="w-4 h-4 text-slate-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100">{totalCount}</div>
          <div className="text-[11px] text-slate-400 mt-1">Across all station locations</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-2xl">
          <div className="flex items-center justify-between text-cyan-400 text-xs font-semibold mb-1">
            <span>Active Charging</span>
            <Zap className="w-4 h-4" />
          </div>
          <div className="text-2xl font-bold text-cyan-400">{chargingCount}</div>
          <div className="text-[11px] text-cyan-400/80 mt-1">{totalActivePowerKw} kW active load</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-2xl">
          <div className="flex items-center justify-between text-emerald-400 text-xs font-semibold mb-1">
            <span>Available Online</span>
            <Activity className="w-4 h-4" />
          </div>
          <div className="text-2xl font-bold text-emerald-400">{availableCount}</div>
          <div className="text-[11px] text-emerald-400/80 mt-1">Ready for EV sessions</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-2xl">
          <div className="flex items-center justify-between text-amber-400 text-xs font-semibold mb-1">
            <span>Under Maintenance</span>
            <Wrench className="w-4 h-4" />
          </div>
          <div className="text-2xl font-bold text-amber-400">{maintenanceCount}</div>
          <div className="text-[11px] text-amber-400/80 mt-1">Service & inspection</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-2xl">
          <div className="flex items-center justify-between text-rose-400 text-xs font-semibold mb-1">
            <span>Faulted Chargers</span>
            <AlertTriangle className="w-4 h-4" />
          </div>
          <div className="text-2xl font-bold text-rose-400">{faultedCount}</div>
          <div className="text-[11px] text-rose-400/80 mt-1">Attention required</div>
        </div>
      </div>

      {/* SSE Live Stream Controls & Ticker Bar */}
      <div className="bg-slate-900/90 border border-slate-800 p-4 rounded-2xl flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <button
            onClick={onToggleLiveStream}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
              isLiveStreamActive
                ? "bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 shadow-lg shadow-emerald-500/10"
                : "bg-slate-800 text-slate-400 border border-slate-700"
            }`}
          >
            <Radio className={`w-4 h-4 ${isLiveStreamActive ? "animate-pulse text-emerald-400" : "text-slate-500"}`} />
            {isLiveStreamActive ? "Live SSE Stream: ACTIVE" : "Live SSE Stream: PAUSED"}
          </button>

          <span className="text-xs text-slate-400 hidden sm:inline">
            Receiving telemetry events every 3.5s
          </span>
        </div>

        {/* Latest SSE Event ticker */}
        {eventLogs.length > 0 && eventLogs[0] && (
          <div className="flex items-center gap-2 text-xs font-mono bg-slate-950 px-3 py-1.5 rounded-xl border border-slate-800 text-slate-300 max-w-md overflow-hidden">
            <span className="text-cyan-400 font-bold shrink-0">[{eventLogs[0].timestamp}]</span>
            <span className="truncate">{eventLogs[0].chargerName}: {eventLogs[0].currentPowerKw} kW ({eventLogs[0].energyDeliveredKwh} kWh)</span>
          </div>
        )}
      </div>

      {/* Controls & Filter Toolbar */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="flex flex-wrap items-center gap-3 w-full sm:w-auto">
          {/* Search bar */}
          <div className="relative flex-1 sm:w-64">
            <Search className="w-4 h-4 text-slate-500 absolute left-3 top-2.5" />
            <input
              type="text"
              placeholder="Search charger, ID, station..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full bg-slate-900 border border-slate-800 rounded-xl pl-9 pr-3 py-2 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
            />
          </div>

          {/* Status Filter */}
          <div className="flex items-center gap-1 bg-slate-900 border border-slate-800 rounded-xl p-1 text-xs">
            <Filter className="w-3.5 h-3.5 text-slate-400 ml-2" />
            {["ALL", "AVAILABLE", "CHARGING", "MAINTENANCE", "FAULTED"].map((st) => (
              <button
                key={st}
                onClick={() => setSelectedStatus(st)}
                className={`px-2.5 py-1 rounded-lg text-[11px] font-semibold transition-all ${
                  selectedStatus === st
                    ? "bg-slate-800 text-cyan-400"
                    : "text-slate-400 hover:text-slate-200"
                }`}
              >
                {st}
              </button>
            ))}
          </div>

          {/* Group Filter */}
          <select
            value={selectedGroupTag}
            onChange={(e) => setSelectedGroupTag(e.target.value)}
            className="bg-slate-900 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
          >
            <option value="ALL">All Groups</option>
            {groupTags.map((gt) => (
              <option key={gt} value={gt}>{gt}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Real-time Charger Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredChargers.map((c) => (
          <div
            key={c.id}
            className="bg-slate-900 border border-slate-800 rounded-2xl p-5 hover:border-slate-700 transition-all flex flex-col justify-between"
          >
            <div>
              <div className="flex items-start justify-between gap-2 mb-2">
                <div>
                  <h4 className="text-sm font-bold text-slate-100 flex items-center gap-2">
                    {c.name}
                  </h4>
                  <p className="text-xs text-slate-400">{c.stationName}</p>
                </div>
                {getStatusBadge(c.status)}
              </div>

              <div className="flex items-center gap-2 mb-4">
                <span className="px-2 py-0.5 rounded bg-slate-950 text-slate-400 font-mono text-[10px] border border-slate-800">
                  {c.id}
                </span>
                <span className="px-2 py-0.5 rounded bg-slate-950 text-cyan-400 font-mono text-[10px] border border-slate-800 flex items-center gap-1">
                  <Layers className="w-3 h-3" /> {c.groupTag}
                </span>
                <span className="px-2 py-0.5 rounded bg-slate-950 text-slate-300 font-mono text-[10px] border border-slate-800">
                  {c.connectorType} ({c.maxPowerKw}kW)
                </span>
              </div>

              {/* Telemetry Section */}
              <div className="bg-slate-950 border border-slate-800/80 rounded-xl p-3 mb-4 space-y-2">
                <div className="flex items-center justify-between text-xs">
                  <span className="text-slate-400">Current Power Draw:</span>
                  <span className={`font-mono font-bold ${c.currentPowerKw > 0 ? "text-cyan-400" : "text-slate-400"}`}>
                    {c.currentPowerKw} kW
                  </span>
                </div>
                <div className="flex items-center justify-between text-xs">
                  <span className="text-slate-400">Energy Delivered:</span>
                  <span className="font-mono font-bold text-slate-200">
                    {c.energyDeliveredKwh} kWh
                  </span>
                </div>
                {c.maintenanceReason && (
                  <div className="text-[11px] text-amber-400 bg-amber-500/10 p-2 rounded-lg border border-amber-500/20 mt-2">
                    <strong>Note:</strong> {c.maintenanceReason}
                  </div>
                )}
              </div>
            </div>

            {/* Action Bar */}
            <div className="flex items-center justify-between pt-2 border-t border-slate-800/80">
              <span className="text-[10px] text-slate-500 font-mono">
                Ping: {new Date(c.lastPingAt).toLocaleTimeString()}
              </span>

              <div className="flex items-center gap-2">
                <button
                  onClick={() => onOpenGroupMaintenanceModal(c.groupTag)}
                  title={`Toggle Maintenance for Group ${c.groupTag}`}
                  className="px-2 py-1 bg-slate-950 hover:bg-slate-800 text-slate-400 hover:text-amber-400 border border-slate-800 rounded-lg text-[10px] font-semibold transition-colors flex items-center gap-1"
                >
                  <Layers className="w-3 h-3" /> Group Maintenance
                </button>

                <button
                  onClick={() => onOpenMaintenanceModal(c)}
                  className={`px-2.5 py-1 rounded-lg text-xs font-bold transition-all flex items-center gap-1 ${
                    c.status === "MAINTENANCE"
                      ? "bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 hover:bg-emerald-500/30"
                      : "bg-amber-500/20 text-amber-400 border border-amber-500/30 hover:bg-amber-500/30"
                  }`}
                >
                  <Wrench className="w-3 h-3" />
                  {c.status === "MAINTENANCE" ? "Exit Maint." : "Maintenance"}
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
