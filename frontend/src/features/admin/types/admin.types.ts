export interface Vendor {
  id: string;
  name: string;
  businessRegistrationNumber: string;
  contactEmail: string;
  status: 'ACTIVE' | 'PENDING' | 'SUSPENDED';
  chargepointCount: number;
  createdAt: string;
}

export interface OnboardVendorPayload {
  name: string;
  businessRegistrationNumber: string;
  contactEmail: string;
  adminEmail: string;
  adminFullName: string;
}

export interface GlobalMarkupConfig {
  percentageMarkup: number;
  fixedCentsPerKwh: number;
  updatedAt: string;
  updatedBy: string;
}

export interface PlatformIncomeSummary {
  totalIncomeCents: number;
  totalKwhDelivered: number;
  activeVendors: number;
  totalSessions: number;
  averageProfitPerSessionCents: number;
  period: 'TODAY' | '7_DAYS' | '30_DAYS' | 'CUSTOM';
}

export interface UserAccount {
  id: string;
  email: string;
  fullName: string;
  role: 'ROLE_ADMIN' | 'ROLE_VENDOR_ADMIN' | 'ROLE_VENDOR_USER' | 'ROLE_CUSTOMER';
  vendorId?: string;
  vendorName?: string;
  status: 'ACTIVE' | 'LOCKED' | 'SUSPENDED';
  createdAt: string;
}

export interface FtsSearchResultItem {
  id: string;
  type: 'SESSION' | 'CUSTOMER' | 'VEHICLE' | 'ERROR_CODE';
  title: string;
  subtitle: string;
  snippet: string;
  targetUrl: string;
}
