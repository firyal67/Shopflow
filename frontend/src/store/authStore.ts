import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";

interface User {
  id: number;
  email: string;
  prenom: string;
  nom: string;
  role: "ADMIN" | "SELLER" | "CUSTOMER";
  actif: boolean;
  sellerProfile?: { id: number; nomBoutique: string; logo?: string };
}

interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  setAuth: (user: User, accessToken: string, refreshToken: string) => void;
  setTokens: (accessToken: string, refreshToken: string) => void;
  logout: () => void;
  isAuthenticated: () => boolean;
}

// Storage personnalisé qui écrit dans localStorage ET dans un cookie lisible par le middleware
const cookieStorage = {
  getItem: (name: string): string | null => {
    if (typeof window === "undefined") return null;
    // Lire depuis localStorage
    const item = localStorage.getItem(name);
    return item;
  },
  setItem: (name: string, value: string): void => {
    if (typeof window === "undefined") return;
    localStorage.setItem(name, value);
    // Écrire aussi dans un cookie pour le middleware Next.js
    const maxAge = 60 * 60 * 24 * 7; // 7 jours
    document.cookie = `${name}=${encodeURIComponent(value)}; path=/; max-age=${maxAge}; SameSite=Lax`;
  },
  removeItem: (name: string): void => {
    if (typeof window === "undefined") return;
    localStorage.removeItem(name);
    // Supprimer le cookie
    document.cookie = `${name}=; path=/; max-age=0`;
  },
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      setAuth: (user, accessToken, refreshToken) =>
        set({ user, accessToken, refreshToken }),
      setTokens: (accessToken, refreshToken) =>
        set({ accessToken, refreshToken }),
      logout: () => set({ user: null, accessToken: null, refreshToken: null }),
      isAuthenticated: () => !!get().accessToken && !!get().user,
    }),
    {
      name: "shopflow-auth",
      storage: createJSONStorage(() => cookieStorage),
    }
  )
);
