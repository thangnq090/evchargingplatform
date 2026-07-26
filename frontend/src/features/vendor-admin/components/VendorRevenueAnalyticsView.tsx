import React, { useState } from "react";
import { REVENUE_DAILY_DATA } from "../mocks/vendorAdminData";
import { DollarSign, TrendingUp, Zap, Calendar, Download, PieChart, Award } from "lucide-react";

export const VendorRevenueAnalyticsView: React.FC = () => {
  const [period, setPeriod] = useState<"DAILY" | "WEEKLY" | "MONTHLY">("DAILY");

  // Sum metrics
  const totalGrossCents = REVENUE_DAILY_DATA.reduce((acc, d) => acc + d.grossCents, 0);
  const totalMarkupCents = REVENUE_DAILY_DATA.reduce((acc, d) => acc + d.markupCents, 0);
  const totalNetCents = REVENUE_DAILY_DATA.reduce((acc, d) => acc + d.netCents, 0);
  const totalSessions = REVENUE_DAILY_DATA.reduce((acc, d) => acc + d.sessions, 0);

  const formatCurrency = (cents: number) => {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
    }).format(cents / 100);
  };

  const handleExportCSV = () => {
    const csvContent =
      "data:text/csv;charset=utf-8," +
      ["Date,Gross Revenue,Platform Markup,Net Payout,Sessions"]
        .concat(
          REVENUE_DAILY_DATA.map(
            (d) =>
              `${d.timestamp},${(d.grossCents / 100).toFixed(2)},${(d.markupCents / 100).toFixed(
                2
              )},${(d.netCents / 100).toFixed(2)},${d.sessions}`
          )
        )
        .join("\n");
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `vendor_revenue_report_${period.toLowerCase()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <TrendingUp className="w-5 h-5 text-cyan-400" /> Vendor Financial Analytics
          </h2>
          <p className="text-xs text-slate-400">
            Monitor revenue generated across chargepoints, platform fee deductions, and session volume.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="flex items-center bg-slate-900 border border-slate-800 rounded-xl p-1">
            {(["DAILY", "WEEKLY", "MONTHLY"] as const).map((p) => (
              <button
                key={p}
                onClick={() => setPeriod(p)}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
                  period === p
                    ? "bg-cyan-500 text-slate-950 shadow-md shadow-cyan-500/20"
                    : "text-slate-400 hover:text-slate-200"
                }`}
              >
                {p.charAt(0) + p.slice(1).toLowerCase()}
              </button>
            ))}
          </div>

          <button
            onClick={handleExportCSV}
            className="flex items-center gap-1.5 px-3 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-xl text-xs font-semibold border border-slate-700 transition-colors"
          >
            <Download className="w-4 h-4 text-cyan-400" /> Export CSV
          </button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-4 shadow-xl relative overflow-hidden">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-semibold text-slate-400">Total Gross Income</span>
            <div className="p-2 bg-cyan-500/10 rounded-xl">
              <DollarSign className="w-4 h-4 text-cyan-400" />
            </div>
          </div>
          <div className="text-2xl font-bold font-mono text-slate-100">{formatCurrency(totalGrossCents)}</div>
          <p className="text-[10px] text-emerald-400 mt-1 font-semibold">↑ +14.2% vs previous period</p>
        </div>

        <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-4 shadow-xl">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-semibold text-slate-400">Platform Markup (10%)</span>
            <div className="p-2 bg-amber-500/10 rounded-xl">
              <PieChart className="w-4 h-4 text-amber-400" />
            </div>
          </div>
          <div className="text-2xl font-bold font-mono text-amber-400">{formatCurrency(totalMarkupCents)}</div>
          <p className="text-[10px] text-slate-500 mt-1">Deducted platform fee</p>
        </div>

        <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-4 shadow-xl">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-semibold text-slate-400">Net Vendor Payout</span>
            <div className="p-2 bg-emerald-500/10 rounded-xl">
              <TrendingUp className="w-4 h-4 text-emerald-400" />
            </div>
          </div>
          <div className="text-2xl font-bold font-mono text-emerald-400">{formatCurrency(totalNetCents)}</div>
          <p className="text-[10px] text-emerald-400 mt-1 font-semibold">Ready for settlement</p>
        </div>

        <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-4 shadow-xl">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-semibold text-slate-400">Total Charging Sessions</span>
            <div className="p-2 bg-blue-500/10 rounded-xl">
              <Zap className="w-4 h-4 text-blue-400" />
            </div>
          </div>
          <div className="text-2xl font-bold font-mono text-slate-100">{totalSessions}</div>
          <p className="text-[10px] text-slate-400 mt-1">~54 sessions / day avg</p>
        </div>
      </div>

      {/* Visual Revenue Breakdown Chart Component */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-5 shadow-xl space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-bold text-slate-200 flex items-center gap-2">
            <Calendar className="w-4 h-4 text-cyan-400" /> Revenue & Session Breakdown ({period})
          </h3>
          <div className="flex items-center gap-4 text-xs font-mono">
            <span className="flex items-center gap-1 text-cyan-400">
              <span className="w-2.5 h-2.5 rounded-full bg-cyan-400"></span> Gross Revenue
            </span>
            <span className="flex items-center gap-1 text-emerald-400">
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-400"></span> Net Payout
            </span>
          </div>
        </div>

        {/* Custom Bar Visualization */}
        <div className="h-48 flex items-end justify-between gap-3 pt-6 pb-2 px-2 border-b border-slate-800">
          {REVENUE_DAILY_DATA.map((d) => {
            const maxGross = 300000;
            const grossHeightPct = Math.min(100, Math.round((d.grossCents / maxGross) * 100));
            const netHeightPct = Math.min(100, Math.round((d.netCents / maxGross) * 100));

            return (
              <div key={d.timestamp} className="flex-1 flex flex-col items-center gap-2 h-full justify-end group relative">
                {/* Tooltip */}
                <div className="absolute -top-12 opacity-0 group-hover:opacity-100 transition-opacity bg-slate-950 border border-slate-700 text-slate-200 text-[10px] p-2 rounded-lg pointer-events-none z-10 shadow-xl whitespace-nowrap">
                  <div>{d.label} ({d.timestamp})</div>
                  <div className="text-cyan-400 font-mono">Gross: {formatCurrency(d.grossCents)}</div>
                  <div className="text-emerald-400 font-mono">Net: {formatCurrency(d.netCents)}</div>
                </div>

                <div className="w-full flex items-end justify-center gap-1 h-full">
                  <div
                    style={{ height: `${grossHeightPct}%` }}
                    className="w-1/2 bg-cyan-500/80 group-hover:bg-cyan-400 rounded-t-md transition-all"
                  ></div>
                  <div
                    style={{ height: `${netHeightPct}%` }}
                    className="w-1/2 bg-emerald-500/80 group-hover:bg-emerald-400 rounded-t-md transition-all"
                  ></div>
                </div>
                <span className="text-[11px] font-mono text-slate-400">{d.label}</span>
              </div>
            );
          })}
        </div>
      </div>

      {/* Chargepoint Performance Leaderboard */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-5 shadow-xl space-y-4">
        <h3 className="text-sm font-bold text-slate-200 flex items-center gap-2">
          <Award className="w-4 h-4 text-amber-400" /> Chargepoint Revenue Leaderboard
        </h3>

        <div className="space-y-3">
          {[
            { code: "CP-US-WEST-01", name: "Downtown EV Hub - Bay 1", revenue: 845000, sessions: 210, share: "45%" },
            { code: "CP-US-NORTH-05", name: "Airport Supercharger North", revenue: 612000, sessions: 145, share: "32%" },
            { code: "CP-US-WEST-02", name: "Downtown EV Hub - Bay 2", revenue: 439000, sessions: 112, share: "23%" },
          ].map((item, idx) => (
            <div
              key={item.code}
              className="flex items-center justify-between p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 hover:border-slate-700 transition-colors"
            >
              <div className="flex items-center gap-3">
                <div
                  className={`w-7 h-7 rounded-xl flex items-center justify-center font-bold text-xs font-mono ${
                    idx === 0
                      ? "bg-amber-500/20 text-amber-400 border border-amber-500/30"
                      : idx === 1
                      ? "bg-slate-400/20 text-slate-300 border border-slate-400/30"
                      : "bg-amber-800/20 text-amber-600 border border-amber-800/30"
                  }`}
                >
                  #{idx + 1}
                </div>
                <div>
                  <div className="text-xs font-bold text-slate-100">{item.name}</div>
                  <div className="font-mono text-[10px] text-cyan-400">{item.code}</div>
                </div>
              </div>

              <div className="text-right font-mono">
                <div className="text-xs font-bold text-slate-100">{formatCurrency(item.revenue)}</div>
                <div className="text-[10px] text-slate-400">{item.sessions} sessions ({item.share})</div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
