import React, { useState } from "react";
import { ChargepointManagementView } from "../components/ChargepointManagementView";
import { VendorRevenueAnalyticsView } from "../components/VendorRevenueAnalyticsView";
import { VendorStaffManagementView } from "../components/VendorStaffManagementView";
import { Zap, TrendingUp, Users } from "lucide-react";

export const VendorAdminPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<"CHARGEPOINTS" | "ANALYTICS" | "STAFF">("CHARGEPOINTS");

  return (
    <div className="space-y-6">
      {/* Tab Header */}
      <div className="flex items-center gap-2 border-b border-slate-800 pb-3">
        <button
          onClick={() => setActiveTab("CHARGEPOINTS")}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
            activeTab === "CHARGEPOINTS"
              ? "bg-cyan-500 text-slate-950 shadow-lg shadow-cyan-500/20"
              : "bg-slate-900 text-slate-400 hover:text-slate-200 border border-slate-800"
          }`}
        >
          <Zap className="w-4 h-4" /> Chargepoints & Pricing
        </button>

        <button
          onClick={() => setActiveTab("ANALYTICS")}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
            activeTab === "ANALYTICS"
              ? "bg-cyan-500 text-slate-950 shadow-lg shadow-cyan-500/20"
              : "bg-slate-900 text-slate-400 hover:text-slate-200 border border-slate-800"
          }`}
        >
          <TrendingUp className="w-4 h-4" /> Financial Analytics
        </button>

        <button
          onClick={() => setActiveTab("STAFF")}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
            activeTab === "STAFF"
              ? "bg-cyan-500 text-slate-950 shadow-lg shadow-cyan-500/20"
              : "bg-slate-900 text-slate-400 hover:text-slate-200 border border-slate-800"
          }`}
        >
          <Users className="w-4 h-4" /> Staff & RBAC
        </button>
      </div>

      {/* Tab Content */}
      {activeTab === "CHARGEPOINTS" && <ChargepointManagementView />}
      {activeTab === "ANALYTICS" && <VendorRevenueAnalyticsView />}
      {activeTab === "STAFF" && <VendorStaffManagementView />}
    </div>
  );
};
