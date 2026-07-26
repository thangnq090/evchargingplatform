import { useState, useEffect, useCallback } from "react";
import { OperationalCharger, SSEEvent } from "../types/vendorUserTypes";
import { INITIAL_MOCK_CHARGERS } from "../mocks/vendorUserMockData";

export function useRealtimeChargerStream() {
  const [chargers, setChargers] = useState<OperationalCharger[]>(INITIAL_MOCK_CHARGERS);
  const [isLiveStreamActive, setIsLiveStreamActive] = useState<boolean>(true);
  const [eventLogs, setEventLogs] = useState<SSEEvent[]>([]);

  const toggleLiveStream = () => setIsLiveStreamActive((prev) => !prev);

  const updateChargerStatus = useCallback((chargerId: string, setMaintenance: boolean, reason?: string) => {
    setChargers((prev) =>
      prev.map((c) => {
        if (c.id === chargerId) {
          const nextStatus = setMaintenance ? "MAINTENANCE" : "AVAILABLE";
          return {
            ...c,
            status: nextStatus,
            currentPowerKw: nextStatus === "MAINTENANCE" ? 0 : c.currentPowerKw,
            maintenanceReason: setMaintenance ? reason || "Manual Maintenance Mode" : undefined,
            lastPingAt: new Date().toISOString(),
          };
        }
        return c;
      })
    );
  }, []);

  const updateGroupMaintenance = useCallback((groupTag: string, setMaintenance: boolean, reason?: string) => {
    setChargers((prev) =>
      prev.map((c) => {
        if (c.groupTag === groupTag) {
          const nextStatus = setMaintenance ? "MAINTENANCE" : "AVAILABLE";
          return {
            ...c,
            status: nextStatus,
            currentPowerKw: nextStatus === "MAINTENANCE" ? 0 : c.currentPowerKw,
            maintenanceReason: setMaintenance ? reason || `Group Maintenance (${groupTag})` : undefined,
            lastPingAt: new Date().toISOString(),
          };
        }
        return c;
      })
    );
  }, []);

  // Simulate SSE server-sent ticks every 3.5 seconds
  useEffect(() => {
    if (!isLiveStreamActive) return;

    const interval = setInterval(() => {
      setChargers((prev) => {
        // Pick a random charging charger to tick telemetry
        const updated = prev.map((c) => {
          if (c.status === "CHARGING") {
            const powerVariation = Math.round((c.maxPowerKw * (0.75 + Math.random() * 0.2)) * 10) / 10;
            const energyInc = Math.round((powerVariation / 3600 * 3.5) * 100) / 100;
            return {
              ...c,
              currentPowerKw: powerVariation,
              energyDeliveredKwh: Math.round((c.energyDeliveredKwh + energyInc) * 100) / 100,
              lastPingAt: new Date().toISOString(),
            };
          }
          return { ...c, lastPingAt: new Date().toISOString() };
        });

        // Generate mock SSE telemetry event log
        const chargingChargers = updated.filter((c) => c.status === "CHARGING");
        if (chargingChargers.length > 0) {
          const active = chargingChargers[Math.floor(Math.random() * chargingChargers.length)];
          if (active) {
            const newEvent: SSEEvent = {
              id: `SSE-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
              type: "TELEMETRY_UPDATE",
              chargerId: active.id,
              chargerName: active.name,
              currentPowerKw: active.currentPowerKw,
              energyDeliveredKwh: active.energyDeliveredKwh,
              timestamp: new Date().toLocaleTimeString(),
            };
            setEventLogs((logs) => [newEvent, ...logs.slice(0, 19)]);
          }
        }

        return updated;
      });
    }, 3500);

    return () => clearInterval(interval);
  }, [isLiveStreamActive]);

  return {
    chargers,
    isLiveStreamActive,
    toggleLiveStream,
    eventLogs,
    updateChargerStatus,
    updateGroupMaintenance,
  };
}
