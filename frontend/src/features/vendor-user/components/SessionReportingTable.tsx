import React, { useState } from "react";
import { ChargingSessionLog, SessionStatus } from "../types/vendorUserTypes";
import { vendorUserApi } from "../api/vendorUserApi";
import { FileText, Download, Calendar, Filter, Search, Zap, Clock, DollarSign } from "lucide-react";

interface SessionReportingTableProps {
  sessions: ChargingSessionLog[];
}

export const SessionReportingTable: React.FC<SessionReportingTableProps> = ({ sessions }) => {
  const [dateRange, setDateRange] = useState<"TODAY" | "7DAYS" | "30DAYS" | "ALL">("ALL");
  const [statusFilter, setStatusFilter] = useState<string>("ALL");
  const [searchQuery, setSearchQuery] = useState<string>("");

  const filteredSessions = sessions.filter((s) => {
    const matchesSearch =
      s.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.userEmail.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.chargerName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.stationName.toLowerCase().includes(searchQuery.toLowerCase());

    const matchesStatus = statusFilter === "ALL" || s.status === statusFilter;

    // Date range filter
    const now = Date.now();
    const sessionTime = new Date(s.startTime).getTime();
    let matchesDate = true;
    if (dateRange === "TODAY") {
      matchesDate = now - sessionTime <= 24 * 60 * 60 * 1000;
    } else if (dateRange === "7DAYS") {
      matchesDate = now - sessionTime <= 7 * 24 * 60 * 60 * 1000;
    } else if (dateRange === "30DAYS") {
      matchesDate = now - sessionTime <= 30 * 24 * 60 * 60 * 1000;
    }

    return matchesSearch && matchesStatus && matchesDate;
  });

  const totalEnergy = filteredSessions.reduce((acc, s) => acc + s.kwhDelivered, 0).toFixed(1);
  const totalRevenue = (filteredSessions.reduce((acc, s) => acc + s.totalCostCents, 0) / 100).toFixed(2);

  const getStatusBadge = (status: SessionStatus) => {
    switch (status) {
      case "ACTIVE":
        return <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">ACTIVE</span>;
      case "COMPLETED":
        return <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">COMPLETED</span>;
      case "FAILED":
        return <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-rose-500/10 text-rose-400 border border-rose-500/20">FAILED</span>;
      default:
        return <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-slate-500/10 text-slate-400 border border-slate-500/20">ABORTED</span>;
    }
  };

  return (
    <div className="space-y-6">
      {/* Metrics Summary Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-slate-900 border border-slate-800 p-4 rounded-2xl">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold mb-1">
            <span>Reported Sessions</span>
            <Clock className="w-4 h-4 text-cyan-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100">{filteredSessions.length}</div>
          <div className="text-[11px] text-slate-400 mt-1">Filtered result count</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-2xl">
          <div className="flex items-center justify-between text-cyan-400 text-xs font-semibold mb-1">
            <span>Total Energy Dispensed</span>
            <Zap className="w-4 h-4" />
          </div>
          <div className="text-2xl font-bold text-cyan-400">{totalEnergy} <span className="text-sm font-normal">kWh</span></div>
          <div className="text-[11px] text-cyan-400/80 mt-1">Cumulative session volume</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-2xl">
          <div className="flex items-center justify-between text-emerald-400 text-xs font-semibold mb-1">
            <span>Total Session Revenue</span>
            <DollarSign className="w-4 h-4" />
          </div>
          <div className="text-2xl font-bold text-emerald-400">${totalRevenue}</div>
          <div className="text-[11px] text-emerald-400/80 mt-1">Gross session charging billing</div>
        </div>
      </div>

      {/* Filter and Export Toolbar */}
      <div className="flex flex-col lg:flex-row items-center justify-between gap-4 bg-slate-900/90 border border-slate-800 p-4 rounded-2xl">
        <div className="flex flex-wrap items-center gap-3 w-full lg:w-auto">
          {/* Search Box */}
          <div className="relative flex-1 sm:w-64">
            <Search className="w-4 h-4 text-slate-500 absolute left-3 top-2.5" />
            <input
              type="text"
              placeholder="Search session ID, email, charger..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-9 pr-3 py-2 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
            />
          </div>

          {/* Date Range Selector */}
          <div className="flex items-center gap-1 bg-slate-950 border border-slate-800 rounded-xl p-1 text-xs">
            <Calendar className="w-3.5 h-3.5 text-slate-400 ml-2" />
            {[
              { key: "ALL", label: "All Time" },
              { key: "TODAY", label: "Today" },
              { key: "7DAYS", label: "7 Days" },
              { key: "30DAYS", label: "30 Days" },
            ].map((d) => (
              <button
                key={d.key}
                onClick={() => setDateRange(d.key as any)}
                className={`px-2.5 py-1 rounded-lg text-[11px] font-semibold transition-all ${
                  dateRange === d.key
                    ? "bg-slate-800 text-cyan-400"
                    : "text-slate-400 hover:text-slate-200"
                }`}
              >
                {d.label}
              </button>
            ))}
          </div>

          {/* Status Selector */}
          <div className="flex items-center gap-1 bg-slate-950 border border-slate-800 rounded-xl p-1 text-xs">
            <Filter className="w-3.5 h-3.5 text-slate-400 ml-2" />
            {["ALL", "ACTIVE", "COMPLETED", "FAILED"].map((st) => (
              <button
                key={st}
                onClick={() => setStatusFilter(st)}
                className={`px-2.5 py-1 rounded-lg text-[11px] font-semibold transition-all ${
                  statusFilter === st
                    ? "bg-slate-800 text-cyan-400"
                    : "text-slate-400 hover:text-slate-200"
                }`}
              >
                {st}
              </button>
            ))}
          </div>
        </div>

        {/* Export Buttons */}
        <div className="flex items-center gap-2 w-full lg:w-auto justify-end">
          <button
            onClick={() => vendorUserApi.exportSessionsCSV(filteredSessions)}
            className="flex items-center gap-2 px-3 py-2 bg-slate-950 hover:bg-slate-800 text-slate-200 border border-slate-800 rounded-xl text-xs font-semibold transition-colors"
          >
            <Download className="w-4 h-4 text-emerald-400" /> Export CSV
          </button>

          <button
            onClick={() => vendorUserApi.exportSessionsPDF(filteredSessions)}
            className="flex items-center gap-2 px-3 py-2 bg-cyan-500 hover:bg-cyan-400 text-slate-950 rounded-xl text-xs font-bold shadow-lg shadow-cyan-500/20 transition-all"
          >
            <FileText className="w-4 h-4" /> PDF Report
          </button>
        </div>
      </div>

      {/* Data Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-slate-300">
            <thead className="bg-slate-950 border-b border-slate-800 text-slate-400 font-semibold uppercase tracking-wider text-[10px]">
              <tr>
                <th className="p-4">Session ID</th>
                <th className="p-4">Charger & Station</th>
                <th className="p-4">User</th>
                <th className="p-4">Start Time</th>
                <th className="p-4">Duration</th>
                <th className="p-4">Energy (kWh)</th>
                <th className="p-4">Total Revenue</th>
                <th className="p-4 text-right">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {filteredSessions.length === 0 ? (
                <tr>
                  <td colSpan={8} className="p-8 text-center text-slate-500 text-xs">
                    No charging session records found for the selected criteria.
                  </td>
                </tr>
              ) : (
                filteredSessions.map((s) => (
                  <tr key={s.id} className="hover:bg-slate-800/40 transition-colors">
                    <td className="p-4 font-mono font-bold text-cyan-400">{s.id}</td>
                    <td className="p-4">
                      <div className="font-semibold text-slate-200">{s.chargerName}</div>
                      <div className="text-[10px] text-slate-400">{s.stationName} ({s.chargerId})</div>
                    </td>
                    <td className="p-4 text-slate-300">{s.userEmail}</td>
                    <td className="p-4 font-mono text-[11px] text-slate-400">
                      {new Date(s.startTime).toLocaleString()}
                    </td>
                    <td className="p-4 font-mono text-slate-300">{s.durationMinutes} min</td>
                    <td className="p-4 font-mono font-bold text-slate-100">{s.kwhDelivered.toFixed(2)} kWh</td>
                    <td className="p-4 font-mono font-bold text-emerald-400">${(s.totalCostCents / 100).toFixed(2)}</td>
                    <td className="p-4 text-right">{getStatusBadge(s.status)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
