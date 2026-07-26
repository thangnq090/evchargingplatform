import { OperationalCharger, ChargingSessionLog, MaintenanceToggleRequest } from "../types/vendorUserTypes";
import { INITIAL_MOCK_CHARGERS, INITIAL_MOCK_SESSIONS } from "../mocks/vendorUserMockData";

let chargersState: OperationalCharger[] = [...INITIAL_MOCK_CHARGERS];
let sessionsState: ChargingSessionLog[] = [...INITIAL_MOCK_SESSIONS];

export const vendorUserApi = {
  getChargers: async (): Promise<OperationalCharger[]> => {
    return Promise.resolve([...chargersState]);
  },

  toggleMaintenance: async (req: MaintenanceToggleRequest): Promise<OperationalCharger[]> => {
    chargersState = chargersState.map((c) => {
      if (req.chargerIds.includes(c.id)) {
        const nextStatus = req.setMaintenance ? "MAINTENANCE" : "AVAILABLE";
        return {
          ...c,
          status: nextStatus,
          currentPowerKw: nextStatus === "MAINTENANCE" ? 0 : c.currentPowerKw,
          maintenanceReason: req.setMaintenance ? req.reason || "Manual Maintenance Mode" : undefined,
          lastPingAt: new Date().toISOString(),
        };
      }
      return c;
    });
    return Promise.resolve([...chargersState]);
  },

  getSessionLogs: async (): Promise<ChargingSessionLog[]> => {
    return Promise.resolve([...sessionsState]);
  },

  exportSessionsCSV: (sessions: ChargingSessionLog[]) => {
    const headers = ["Session ID", "Charger ID", "Charger Name", "Station Name", "User Email", "Start Time", "End Time", "Duration (min)", "Energy (kWh)", "Cost ($)", "Status"];
    const rows = sessions.map((s) => [
      s.id,
      s.chargerId,
      s.chargerName,
      `"${s.stationName}"`,
      s.userEmail,
      s.startTime,
      s.endTime || "N/A",
      s.durationMinutes.toString(),
      s.kwhDelivered.toFixed(2),
      (s.totalCostCents / 100).toFixed(2),
      s.status,
    ]);

    const csvContent = [headers.join(","), ...rows.map((r) => r.join(","))].join("\n");
    const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.setAttribute("href", url);
    link.setAttribute("download", `vendor_sessions_report_${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  },

  exportSessionsPDF: (sessions: ChargingSessionLog[]) => {
    const reportTitle = "EV Charging Platform - Vendor Operational Sessions Report";
    const generatedAt = new Date().toLocaleString();
    const totalSessions = sessions.length;
    const totalEnergy = sessions.reduce((acc, s) => acc + s.kwhDelivered, 0).toFixed(2);
    const totalRevenue = (sessions.reduce((acc, s) => acc + s.totalCostCents, 0) / 100).toFixed(2);

    const rowsHtml = sessions
      .map(
        (s) => `
      <tr>
        <td style="padding: 8px; border-bottom: 1px solid #e2e8f0; font-family: monospace;">${s.id}</td>
        <td style="padding: 8px; border-bottom: 1px solid #e2e8f0;">${s.chargerName}</td>
        <td style="padding: 8px; border-bottom: 1px solid #e2e8f0;">${s.userEmail}</td>
        <td style="padding: 8px; border-bottom: 1px solid #e2e8f0;">${s.durationMinutes}m</td>
        <td style="padding: 8px; border-bottom: 1px solid #e2e8f0;">${s.kwhDelivered.toFixed(2)} kWh</td>
        <td style="padding: 8px; border-bottom: 1px solid #e2e8f0;">$${(s.totalCostCents / 100).toFixed(2)}</td>
        <td style="padding: 8px; border-bottom: 1px solid #e2e8f0;"><strong>${s.status}</strong></td>
      </tr>
    `
      )
      .join("");

    const printWindow = window.open("", "_blank");
    if (printWindow) {
      printWindow.document.write(`
        <!DOCTYPE html>
        <html>
          <head>
            <title>${reportTitle}</title>
            <style>
              body { font-family: system-ui, -apple-system, sans-serif; padding: 30px; color: #0f172a; }
              h1 { font-size: 20px; color: #0284c7; margin-bottom: 4px; }
              .meta { font-size: 12px; color: #64748b; margin-bottom: 20px; }
              .summary { display: flex; gap: 20px; margin-bottom: 24px; padding: 12px; background: #f8fafc; border-radius: 8px; border: 1px solid #e2e8f0; }
              .stat { font-size: 13px; }
              .stat strong { display: block; font-size: 16px; color: #0f172a; }
              table { width: 100%; border-collapse: collapse; font-size: 12px; }
              th { background: #f1f5f9; text-align: left; padding: 8px; border-bottom: 2px solid #cbd5e1; }
            </style>
          </head>
          <body>
            <h1>${reportTitle}</h1>
            <div class="meta">Generated: ${generatedAt}</div>
            <div class="summary">
              <div class="stat">Total Sessions <strong>${totalSessions}</strong></div>
              <div class="stat">Total Energy <strong>${totalEnergy} kWh</strong></div>
              <div class="stat">Total Revenue <strong>$${totalRevenue}</strong></div>
            </div>
            <table>
              <thead>
                <tr>
                  <th>Session ID</th>
                  <th>Charger</th>
                  <th>User</th>
                  <th>Duration</th>
                  <th>Energy</th>
                  <th>Revenue</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>${rowsHtml}</tbody>
            </table>
          </body>
        </html>
      `);
      printWindow.document.close();
      printWindow.focus();
      setTimeout(() => printWindow.print(), 500);
    }
  },
};
