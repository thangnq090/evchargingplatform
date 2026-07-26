export interface Chargepoint {
  id: string;
  stationCode: string;
  name: string;
  vendorId: string;
  vendorName: string;
  status: "AVAILABLE" | "CHARGING" | "MAINTENANCE" | "OFFLINE";
  connectorsCount: number;
  groupLabel?: string;
  basePriceTenthsOfCentsPerKwh: number; // e.g. 250 = $0.25 / kWh
  locationAddress: string;
  createdAt: string;
  updatedAt: string;
}

export interface ChargepointGroup {
  id: string;
  name: string;
  chargepointCount: number;
  defaultPriceTenthsOfCents: number;
  description?: string;
}

export interface VendorRevenueSummary {
  grossRevenueCents: number;
  platformMarkupCents: number;
  netPayoutCents: number;
  totalSessions: number;
  totalKwhDelivered: number;
  period: "DAILY" | "WEEKLY" | "MONTHLY";
}

export interface RevenueDataPoint {
  timestamp: string;
  label: string;
  grossCents: number;
  markupCents: number;
  netCents: number;
  sessions: number;
}

export interface StaffMember {
  id: string;
  email: string;
  fullName: string;
  role: "VENDOR_ADMIN" | "VENDOR_OPERATOR" | "VENDOR_STAFF";
  status: "ACTIVE" | "PENDING" | "SUSPENDED";
  invitedAt: string;
  lastActiveAt?: string;
}

export interface StaffAuditLog {
  id: string;
  staffName: string;
  action: string;
  target: string;
  timestamp: string;
}
