import { apiClient } from '../../../shared/api/apiClient';

export interface LoginPayload {
  email: string;
  password: string;
}

export interface LoginBackendResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: {
    id: string;
    email: string;
    fullName: string;
    role: 'ROLE_ADMIN' | 'ROLE_VENDOR_ADMIN' | 'ROLE_VENDOR_USER' | 'ROLE_CUSTOMER';
    vendorId?: string;
  };
}

export const authApi = {
  async login(payload: LoginPayload): Promise<LoginBackendResponse> {
    return await apiClient.post('/identity/auth/login', payload);
  },

  async logout(): Promise<void> {
    try {
      await apiClient.post('/identity/auth/logout');
    } catch {
      // Ignore 401 or network errors during logout call
    } finally {
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
    }
  },
};
