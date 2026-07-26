import { apiClient } from "../../../shared/api/apiClient";

export interface BackendStationResponse {
  id: string;
  vendorId: string;
  stationCode: string;
  name: string;
  location: {
    address: string;
    city?: string;
    state?: string;
    country?: string;
    latitude: number;
    longitude: number;
  };
  connectors: Array<{
    connectorId: number;
    type: string;
    maxPowerKw: number;
    status: string;
  }>;
  status: string;
  pricingPolicy: {
    basePriceTenthsOfCentsPerKwh: number;
  };
  groupLabel?: string;
  createdAt: string;
  updatedAt: string;
}

export interface BackendPaginatedList<T> {
  items: T[];
  nextCursor?: string;
  hasMore: boolean;
}

export interface CreateStationBackendPayload {
  name: string;
  location: {
    latitude: number;
    longitude: number;
  };
  connectors: Array<{
    type: string;
    maxPowerKw: number;
  }>;
  groupLabel?: string;
  unitPriceTenthCents: number;
}

export interface UpdateStationBackendPayload {
  name?: string;
  groupLabel?: string;
  unitPriceTenthCents?: number;
  location?: {
    latitude: number;
    longitude: number;
  };
}

export interface AddVendorUserBackendPayload {
  name: string;
  email: string;
  phone?: string;
  password?: string;
}

export interface BackendUserResponse {
  id: string;
  name: string;
  email: string;
  phone?: string;
  role: string;
  vendorId?: string;
  accountNumber?: string;
  status: string;
  createdAt: string;
}

export const vendorAdminApi = {
  // --- Station Management ---
  listStations: async (status?: string, limit = 50): Promise<BackendPaginatedList<BackendStationResponse>> => {
    const params: Record<string, any> = { limit };
    if (status && status !== "ALL") {
      params.status = status;
    }
    return apiClient.get("/stations", { params });
  },

  createStation: async (payload: CreateStationBackendPayload): Promise<BackendStationResponse> => {
    return apiClient.post("/stations", payload);
  },

  updateStation: async (stationId: string, payload: UpdateStationBackendPayload): Promise<BackendStationResponse> => {
    return apiClient.patch(`/stations/${stationId}`, payload);
  },

  changeStationStatus: async (stationId: string, status: string): Promise<BackendStationResponse> => {
    return apiClient.put(`/stations/${stationId}/status`, { status });
  },

  deleteStation: async (stationId: string): Promise<void> => {
    return apiClient.delete(`/stations/${stationId}`);
  },

  // --- Staff Management ---
  addVendorUser: async (vendorId: string, payload: AddVendorUserBackendPayload): Promise<BackendUserResponse> => {
    return apiClient.post(`/identity/vendors/${vendorId}/users`, payload);
  },
};
