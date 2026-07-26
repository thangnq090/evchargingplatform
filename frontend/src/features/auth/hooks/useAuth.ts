import { create } from "zustand";
import { authApi, LoginBackendResponse } from "../api/authApi";

interface UserProfile {
  id: string;
  email: string;
  fullName: string;
  role: string;
  vendorId?: string;
}

interface AuthState {
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  user: UserProfile | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const savedToken = localStorage.getItem("token");
const savedUser = localStorage.getItem("user");

export const useAuth = create<AuthState>((set) => ({
  isAuthenticated: !!savedToken,
  isLoading: false,
  error: null,
  user: savedUser ? JSON.parse(savedUser) : null,
  token: savedToken,

  login: async (email: string, password: string) => {
    set({ isLoading: true, error: null });
    try {
      const res: LoginBackendResponse = await authApi.login({ email, password });
      localStorage.setItem("token", res.accessToken);
      localStorage.setItem("refreshToken", res.refreshToken);
      localStorage.setItem("user", JSON.stringify(res.user));

      set({
        isAuthenticated: true,
        isLoading: false,
        user: res.user,
        token: res.accessToken,
        error: null,
      });
    } catch (err: any) {
      set({
        isLoading: false,
        error: err.message || "Failed to authenticate with server.",
      });
      throw err;
    }
  },

  logout: async () => {
    set({ isLoading: true });
    await authApi.logout();
    set({
      isAuthenticated: false,
      isLoading: false,
      user: null,
      token: null,
      error: null,
    });
  },
}));
