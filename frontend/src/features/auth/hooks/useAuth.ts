import { create } from "zustand";

interface AuthState {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: null | { id: string; username: string };
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

export const useAuth = create<AuthState>((set) => ({
  isAuthenticated: false,
  isLoading: false,
  user: null,
  login: async (username: string, _password: string) => {
    set({ isLoading: true });
    // Simulate API call
    await new Promise((resolve) => setTimeout(resolve, 1000));
    set({
      isAuthenticated: true,
      isLoading: false,
      user: { id: "1", username },
    });
  },
  logout: () => {
    set({ isAuthenticated: false, user: null });
  },
}));
