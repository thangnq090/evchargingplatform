import { apiClient } from '../../../shared/api/apiClient';

export interface Vehicle {
  id: string;
  customerId: string;
  make: string;
  model: string;
  year?: number;
  licensePlate: string;
  rfidTagNumber?: string;
  status: 'ACTIVE' | 'DELISTED';
  createdAt: string;
}

export interface RegisterVehiclePayload {
  make: string;
  model: string;
  year?: number;
  licensePlate: string;
  rfidTagNumber?: string;
}

export interface ActiveSession {
  id: string;
  stationId: string;
  connectorId: number;
  customerId: string;
  startTime: string;
  status: 'CHARGING' | 'COMPLETED' | 'STOPPED';
  energyDeliveredKwh: number;
  unitRateAmount: number;
  totalAmount: number;
  currency: string;
}

export interface SessionHistoryItem {
  id: string;
  stationId: string;
  connectorId: number;
  startTime: string;
  endTime: string;
  status: string;
  totalEnergyKwh: number;
  totalAmount: {
    amount: number;
    currency: string;
  };
}

export interface InvoiceLineItem {
  description: string;
  unitPrice: number;
  quantity: number;
  totalAmount: number;
  currency: string;
}

export interface InvoiceDetails {
  id: string;
  sessionId: string;
  customerId: string;
  vendorId: string;
  totalAmount: number;
  currency: string;
  status: 'PENDING' | 'PAID' | 'VOIDED';
  createdAt: string;
  lineItems: InvoiceLineItem[];
}

export const customerApi = {
  // Vehicle & RFID Card Registry
  async getVehicles(customerId: string): Promise<Vehicle[]> {
    const res = await apiClient.get<any>(`/vehicles/customer/${customerId}`);
    return res.data || res;
  },

  async registerVehicle(customerId: string, payload: RegisterVehiclePayload): Promise<Vehicle> {
    const res = await apiClient.post<any>(`/vehicles`, { customerId, ...payload });
    return res.data || res;
  },

  async delistVehicle(vehicleId: string): Promise<void> {
    await apiClient.delete(`/vehicles/${vehicleId}`);
  },

  // Charging Sessions
  async getActiveSession(customerId: string): Promise<ActiveSession | null> {
    try {
      const res = await apiClient.get<any>(`/sessions/customer/${customerId}/active`);
      return res.data || res;
    } catch {
      return null;
    }
  },

  async stopSession(sessionId: string): Promise<void> {
    await apiClient.post(`/sessions/${sessionId}/stop`, {});
  },

  async getSessionHistory(customerId: string): Promise<SessionHistoryItem[]> {
    const res = await apiClient.get<any>(`/sessions/customer/${customerId}`);
    return res.data || res || [];
  },

  // Invoices
  async getInvoiceBySession(sessionId: string): Promise<InvoiceDetails> {
    const res = await apiClient.get<any>(`/billing/invoices/session/${sessionId}`);
    return res.data || res;
  },
};
