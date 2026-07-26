export type ChargerStatus = "AVAILABLE" | "CHARGING" | "FAULTED" | "UNAVAILABLE" | "MAINTENANCE";

export type ConnectorType = "CCS2" | "CHAdeMO" | "TYPE2" | "NACS";

export interface OperationalCharger {
  id: string;
  name: string;
  stationId: string;
  stationName: string;
  groupTag: string;
  status: ChargerStatus;
  connectorType: ConnectorType;
  maxPowerKw: number;
  currentPowerKw: number;
  energyDeliveredKwh: number;
  activeSessionId?: string;
  maintenanceReason?: string;
  lastPingAt: string;
}

export type SessionStatus = "COMPLETED" | "ACTIVE" | "FAILED" | "ABORTED";

export interface ChargingSessionLog {
  id: string;
  chargerId: string;
  chargerName: string;
  stationName: string;
  userEmail: string;
  startTime: string;
  endTime?: string;
  durationMinutes: number;
  kwhDelivered: number;
  totalCostCents: number;
  status: SessionStatus;
}

export interface SSEEvent {
  id: string;
  type: "STATUS_CHANGE" | "TELEMETRY_UPDATE" | "MAINTENANCE_UPDATE";
  chargerId: string;
  chargerName: string;
  status?: ChargerStatus;
  currentPowerKw?: number;
  energyDeliveredKwh?: number;
  timestamp: string;
}

export interface MaintenanceToggleRequest {
  chargerIds: string[];
  setMaintenance: boolean;
  reason?: string;
  estimatedHours?: number;
}
