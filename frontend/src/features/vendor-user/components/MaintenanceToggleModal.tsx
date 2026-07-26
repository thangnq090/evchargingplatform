import React, { useState } from "react";
import { Wrench, AlertTriangle, X, CheckCircle2 } from "lucide-react";
import { OperationalCharger } from "../types/vendorUserTypes";

interface MaintenanceToggleModalProps {
  isOpen: boolean;
  onClose: () => void;
  targetCharger?: OperationalCharger | null;
  targetGroupTag?: string | null;
  onConfirm: (reason: string, estHours: number) => void;
}

export const MaintenanceToggleModal: React.FC<MaintenanceToggleModalProps> = ({
  isOpen,
  onClose,
  targetCharger,
  targetGroupTag,
  onConfirm,
}) => {
  const [reason, setReason] = useState("");
  const [estHours, setEstHours] = useState(2);

  if (!isOpen) return null;

  const isGroup = Boolean(targetGroupTag);
  const title = isGroup
    ? `Group Maintenance Control (${targetGroupTag})`
    : `Maintenance Mode: ${targetCharger?.name || targetCharger?.id}`;

  const isCurrentlyInMaintenance = isGroup
    ? false
    : targetCharger?.status === "MAINTENANCE";

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onConfirm(reason, estHours);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/80 backdrop-blur-sm p-4">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 max-w-md w-full shadow-2xl relative">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-slate-400 hover:text-slate-200 transition-colors"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center gap-3 mb-4">
          <div className={`p-2.5 rounded-xl ${isCurrentlyInMaintenance ? "bg-emerald-500/10 text-emerald-400 border border-emerald-500/20" : "bg-amber-500/10 text-amber-400 border border-amber-500/20"}`}>
            {isCurrentlyInMaintenance ? <CheckCircle2 className="w-6 h-6" /> : <Wrench className="w-6 h-6" />}
          </div>
          <div>
            <h3 className="text-base font-bold text-slate-100">{title}</h3>
            <p className="text-xs text-slate-400">
              {isCurrentlyInMaintenance
                ? "Re-activate charger to AVAILABLE status"
                : "Set operational status to MAINTENANCE"}
            </p>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {!isCurrentlyInMaintenance && (
            <>
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Reason for Maintenance / Diagnostic Notes
                </label>
                <textarea
                  required
                  rows={3}
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  placeholder="e.g. Scheduled firmware update, connector thermal check..."
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Estimated Downtime Duration (Hours)
                </label>
                <input
                  type="number"
                  min="1"
                  max="168"
                  value={estHours}
                  onChange={(e) => setEstHours(Number(e.target.value))}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-100 focus:outline-none focus:border-cyan-500"
                />
              </div>

              <div className="p-3 bg-amber-500/10 border border-amber-500/20 rounded-xl flex items-start gap-2.5 text-xs text-amber-300">
                <AlertTriangle className="w-4 h-4 shrink-0 mt-0.5" />
                <span>
                  Chargers in Maintenance mode will reject incoming session start commands and report unavailable in public mobile apps.
                </span>
              </div>
            </>
          )}

          <div className="flex items-center justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl text-xs font-semibold bg-slate-800 text-slate-300 hover:bg-slate-700 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
                isCurrentlyInMaintenance
                  ? "bg-emerald-500 text-slate-950 hover:bg-emerald-400 shadow-lg shadow-emerald-500/20"
                  : "bg-amber-500 text-slate-950 hover:bg-amber-400 shadow-lg shadow-amber-500/20"
              }`}
            >
              {isCurrentlyInMaintenance ? "Bring Online (Available)" : "Confirm Maintenance Mode"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
