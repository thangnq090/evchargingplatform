import { Chargepoint, ChargepointGroup, RevenueDataPoint, StaffMember, StaffAuditLog } from "../types/vendorAdmin.types";

export const INITIAL_CHARGEPOINTS: Chargepoint[] = [
  {
    id: "cp-101",
    stationCode: "CP-US-WEST-01",
    name: "Downtown EV Hub - Bay 1",
    vendorId: "v-001",
    vendorName: "EcoCharge Networks",
    status: "AVAILABLE",
    connectorsCount: 4,
    groupLabel: "Downtown High-Speed",
    basePriceTenthsOfCentsPerKwh: 350, // $0.35/kWh
    locationAddress: "100 Main St, San Francisco, CA",
    createdAt: "2026-01-15T08:00:00Z",
    updatedAt: "2026-07-26T10:00:00Z",
  },
  {
    id: "cp-102",
    stationCode: "CP-US-WEST-02",
    name: "Downtown EV Hub - Bay 2",
    vendorId: "v-001",
    vendorName: "EcoCharge Networks",
    status: "CHARGING",
    connectorsCount: 2,
    groupLabel: "Downtown High-Speed",
    basePriceTenthsOfCentsPerKwh: 350,
    locationAddress: "100 Main St, San Francisco, CA",
    createdAt: "2026-01-15T08:00:00Z",
    updatedAt: "2026-07-26T14:20:00Z",
  },
  {
    id: "cp-103",
    stationCode: "CP-US-NORTH-05",
    name: "Airport Supercharger North",
    vendorId: "v-001",
    vendorName: "EcoCharge Networks",
    status: "AVAILABLE",
    connectorsCount: 8,
    groupLabel: "Transit Corridors",
    basePriceTenthsOfCentsPerKwh: 420, // $0.42/kWh
    locationAddress: "Terminal 2, SFO Airport, CA",
    createdAt: "2026-03-10T09:30:00Z",
    updatedAt: "2026-07-25T11:00:00Z",
  },
  {
    id: "cp-104",
    stationCode: "CP-US-SOUTH-12",
    name: "Silicon Valley Depot",
    vendorId: "v-001",
    vendorName: "EcoCharge Networks",
    status: "MAINTENANCE",
    connectorsCount: 2,
    groupLabel: "Fleet Depots",
    basePriceTenthsOfCentsPerKwh: 280, // $0.28/kWh
    locationAddress: "500 Innovation Way, San Jose, CA",
    createdAt: "2026-04-01T12:00:00Z",
    updatedAt: "2026-07-24T16:45:00Z",
  },
];

export const INITIAL_GROUPS: ChargepointGroup[] = [
  { id: "grp-1", name: "Downtown High-Speed", chargepointCount: 2, defaultPriceTenthsOfCents: 350, description: "DC Fast Chargers in central urban hub" },
  { id: "grp-2", name: "Transit Corridors", chargepointCount: 1, defaultPriceTenthsOfCents: 420, description: "High power charging stations near airport & highway" },
  { id: "grp-3", name: "Fleet Depots", chargepointCount: 1, defaultPriceTenthsOfCents: 280, description: "Overnight & depot chargers for commercial fleets" },
];

export const REVENUE_DAILY_DATA: RevenueDataPoint[] = [
  { timestamp: "2026-07-20", label: "Mon", grossCents: 145000, markupCents: 14500, netCents: 130500, sessions: 42 },
  { timestamp: "2026-07-21", label: "Tue", grossCents: 162000, markupCents: 16200, netCents: 145800, sessions: 48 },
  { timestamp: "2026-07-22", label: "Wed", grossCents: 189000, markupCents: 18900, netCents: 170100, sessions: 56 },
  { timestamp: "2026-07-23", label: "Thu", grossCents: 175000, markupCents: 17500, netCents: 157500, sessions: 51 },
  { timestamp: "2026-07-24", label: "Fri", grossCents: 230000, markupCents: 23000, netCents: 207000, sessions: 72 },
  { timestamp: "2026-07-25", label: "Sat", grossCents: 285000, markupCents: 28500, netCents: 256500, sessions: 89 },
  { timestamp: "2026-07-26", label: "Sun", grossCents: 210000, markupCents: 21000, netCents: 189000, sessions: 65 },
];

export const INITIAL_STAFF: StaffMember[] = [
  { id: "stf-01", email: "vendor.admin@ecocharge.io", fullName: "Sarah Jenkins", role: "VENDOR_ADMIN", status: "ACTIVE", invitedAt: "2026-01-01T00:00:00Z", lastActiveAt: "2026-07-26T18:30:00Z" },
  { id: "stf-02", email: "op.mike@ecocharge.io", fullName: "Mike Vance", role: "VENDOR_OPERATOR", status: "ACTIVE", invitedAt: "2026-02-10T10:00:00Z", lastActiveAt: "2026-07-26T14:10:00Z" },
  { id: "stf-03", email: "tech.lisa@ecocharge.io", fullName: "Lisa Wong", role: "VENDOR_STAFF", status: "PENDING", invitedAt: "2026-07-24T09:15:00Z" },
];

export const INITIAL_AUDIT_LOGS: StaffAuditLog[] = [
  { id: "log-1", staffName: "Sarah Jenkins", action: "UPDATE_PRICING", target: "CP-US-NORTH-05 (420 tenths/kWh)", timestamp: "2026-07-25T11:00:00Z" },
  { id: "log-2", staffName: "Mike Vance", action: "TOGGLE_MAINTENANCE", target: "CP-US-SOUTH-12", timestamp: "2026-07-24T16:45:00Z" },
  { id: "log-3", staffName: "Sarah Jenkins", action: "INVITE_STAFF", target: "tech.lisa@ecocharge.io", timestamp: "2026-07-24T09:15:00Z" },
];
