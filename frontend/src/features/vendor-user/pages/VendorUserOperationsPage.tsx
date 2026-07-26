import React, { useState, useEffect } from "react";
import { useRealtimeChargerStream } from "../hooks/useRealtimeChargerStream";
import { RealtimeMonitorGrid } from "../components/RealtimeMonitorGrid";
import { MaintenanceToggleModal } from "../components/MaintenanceToggleModal";
import { SessionReportingTable } from "../components/SessionReportingTable";
import { OperationalCharger, ChargingSessionLog } from "../types/vendorUserTypes";
import { vendorUserApi } from "../api/vendorUserApi";
import { Activity, FileText } from "lucide-react";

export const VendorUserOperationsPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<"MONITOR" | "REPORTING">("MONITOR");
  const [sessions, setSessions] = useState<ChargingSessionLog[]>([]);

  // Realtime hook
  const {
    chargers,
    isLiveStreamActive,
    toggleLiveStream,
    eventLogs,
    updateChargerStatus,
    updateGroupMaintenance,
  } = useRealtimeChargerStream();

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [targetCharger, setTargetCharger] = useState<OperationalCharger | null>(null);
  const [targetGroupTag, setTargetGroupTag] = useState<string | null>(null);

  useEffect(() => {
    vendorUserApi.getSessionLogs().then(setSessions);
  }, []);

  const handleOpenMaintenanceModal = (charger: OperationalCharger) => {
    setTargetCharger(charger);
    setTargetGroupTag(null);
    setIsModalOpen(true);
  };

  const handleOpenGroupMaintenanceModal = (groupTag: string) => {
    setTargetGroupTag(groupTag);
    setTargetCharger(null);
    setIsModalOpen(true);
  };

  const handleConfirmMaintenance = (reason: string) => {
    if (targetCharger) {
      const isMaint = targetCharger.status !== "MAINTENANCE";
      updateChargerStatus(targetCharger.id, isMaint, reason);
    } else if (targetGroupTag) {
      updateGroupMaintenance(targetGroupTag, true, reason);
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-slate-800 pb-4">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2.5">
            <Activity className="w-6 h-6 text-cyan-400" /> Vendor Operational Console
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Real-time charger telemetry monitoring, availability maintenance control, and session logs.
          </p>
        </div>

        {/* View Switcher Tabs */}
        <div className="flex items-center gap-2 bg-slate-900 border border-slate-800 p-1.5 rounded-2xl">
          <button
            onClick={() => setActiveTab("MONITOR")}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              activeTab === "MONITOR"
                ? "bg-cyan-500 text-slate-950 shadow-lg shadow-cyan-500/20"
                : "text-slate-400 hover:text-slate-200"
            }`}
          >
            <Activity className="w-4 h-4" /> Live Status Grid
          </button>

          <button
            onClick={() => setActiveTab("REPORTING")}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              activeTab === "REPORTING"
                ? "bg-cyan-500 text-slate-950 shadow-lg shadow-cyan-500/20"
                : "text-slate-400 hover:text-slate-200"
            }`}
          >
            <FileText className="w-4 h-4" /> Session Reports
          </button>
        </div>
      </div>

      {/* Main View Area */}
      {activeTab === "MONITOR" ? (
        <RealtimeMonitorGrid
          chargers={chargers}
          isLiveStreamActive={isLiveStreamActive}
          onToggleLiveStream={toggleLiveStream}
          eventLogs={eventLogs}
          onOpenMaintenanceModal={handleOpenMaintenanceModal}
          onOpenGroupMaintenanceModal={handleOpenGroupMaintenanceModal}
        />
      ) : (
        <SessionReportingTable sessions={sessions} />
      )}

      {/* Maintenance Action Modal */}
      <MaintenanceToggleModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        targetCharger={targetCharger}
        targetGroupTag={targetGroupTag}
        onConfirm={handleConfirmMaintenance}
      />
    </div>
  );
};
