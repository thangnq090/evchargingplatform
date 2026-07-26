import { apiClient } from '../../../shared/api/apiClient';
import {
  Vendor,
  OnboardVendorPayload,
  GlobalMarkupConfig,
  PlatformIncomeSummary,
  UserAccount,
  FtsSearchResultItem,
} from '../types/admin.types';

export const adminApi = {
  // GET /api/v1/identity/vendors?limit=N&cursor=X
  async getVendors(limit: number = 50, cursor?: string): Promise<{ items: Vendor[]; nextCursor?: string; hasMore: boolean }> {
    const params: any = { limit };
    if (cursor) params.cursor = cursor;
    const res: any = await apiClient.get('/identity/vendors', { params });
    return {
      items: res.items || [],
      nextCursor: res.pagination?.nextCursor,
      hasMore: res.pagination?.hasMore || false,
    };
  },

  // POST /api/v1/identity/vendors
  async onboardVendor(payload: OnboardVendorPayload): Promise<Vendor> {
    const res: any = await apiClient.post('/identity/vendors', {
      vendorName: payload.name,
      adminName: payload.adminFullName,
      adminEmail: payload.adminEmail,
    });
    return {
      id: res.vendorId || `v-${Date.now()}`,
      name: payload.name,
      businessRegistrationNumber: payload.businessRegistrationNumber || '',
      contactEmail: payload.contactEmail,
      status: 'ACTIVE',
      chargepointCount: 0,
      createdAt: new Date().toISOString(),
    };
  },

  // GET /api/v1/admin/vendors/{vendorId}/markup
  async getGlobalMarkup(vendorId?: string): Promise<GlobalMarkupConfig> {
    const targetVendorId = vendorId || localStorage.getItem('vendorId') || '00000000-0000-0000-0000-000000000000';
    const res: any = await apiClient.get(`/admin/vendors/${targetVendorId}/markup`);
    return {
      percentageMarkup: (res.markupBasisPoints || 0) / 100,
      fixedCentsPerKwh: 0,
      updatedAt: new Date().toISOString(),
      updatedBy: 'admin@evcharging.com',
    };
  },

  // PUT /api/v1/admin/vendors/{vendorId}/markup
  async updateGlobalMarkup(percentage: number, cents: number, vendorId?: string): Promise<GlobalMarkupConfig> {
    const targetVendorId = vendorId || localStorage.getItem('vendorId') || '00000000-0000-0000-0000-000000000000';
    const basisPoints = Math.round(percentage * 100);
    await apiClient.put(`/admin/vendors/${targetVendorId}/markup`, {
      markupBasisPoints: basisPoints,
    });
    return {
      percentageMarkup: percentage,
      fixedCentsPerKwh: cents,
      updatedAt: new Date().toISOString(),
      updatedBy: 'admin@evcharging.com',
    };
  },

  // GET /api/v1/admin/dashboard
  async getPlatformIncome(period: 'TODAY' | '7_DAYS' | '30_DAYS' | 'CUSTOM'): Promise<PlatformIncomeSummary> {
    const res: any = await apiClient.get('/admin/dashboard');
    return {
      totalIncomeCents: res.totalIncomeCents || 0,
      totalKwhDelivered: res.totalKwhDelivered || 0,
      activeVendors: res.activeVendors || 0,
      totalSessions: res.totalSessions || 0,
      averageProfitPerSessionCents: res.averageProfitPerSessionCents || 0,
      period,
    };
  },

  // GET /api/v1/identity/users
  async getUsers(): Promise<UserAccount[]> {
    const res: any[] = await apiClient.get('/identity/users');
    return res.map((u: any) => ({
      id: u.id,
      email: u.email,
      fullName: u.name,
      role: u.role,
      vendorId: u.vendorId,
      status: u.status,
      createdAt: u.createdAt,
    }));
  },

  // POST /api/v1/identity/users/{userId}/status
  async toggleUserStatus(userId: string): Promise<UserAccount> {
    return await apiClient.post(`/identity/users/${userId}/status`);
  },

  // POST /api/v1/identity/users/{userId}/password/reset
  async triggerPasswordReset(userId: string): Promise<{ success: boolean; message: string }> {
    const res: any = await apiClient.post(`/identity/users/${userId}/password/reset`);
    return {
      success: true,
      message: res.message || `Password reset link generated for user ${userId}`,
    };
  },

  // GET /api/v1/search
  async searchFts(query: string): Promise<FtsSearchResultItem[]> {
    if (!query || query.trim().length < 2) return [];
    return await apiClient.get(`/search?q=${encodeURIComponent(query)}`);
  },
};
