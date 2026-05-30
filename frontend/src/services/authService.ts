import api from "@/lib/axios";
import { useAuthStore } from "@/store/authStore";

export interface LoginPayload {
  email: string;
  motDePasse: string;
}

export interface RegisterPayload {
  email: string;
  motDePasse: string;
  prenom: string;
  nom: string;
  role?: "CUSTOMER" | "SELLER";
  nomBoutique?: string;
  descriptionBoutique?: string;
}

const authService = {
  async login(payload: LoginPayload) {
    const { data } = await api.post("/api/auth/login", payload);
    useAuthStore.getState().setAuth(data.user, data.accessToken, data.refreshToken);
    return data;
  },

  async register(payload: RegisterPayload) {
    const { data } = await api.post("/api/auth/register", payload);
    useAuthStore.getState().setAuth(data.user, data.accessToken, data.refreshToken);
    return data;
  },

  async logout() {
    const refreshToken = useAuthStore.getState().refreshToken;
    if (refreshToken) {
      await api.post("/api/auth/logout", { refreshToken }).catch(() => {});
    }
    useAuthStore.getState().logout();
  },

  async refresh(refreshToken: string) {
    const { data } = await api.post("/api/auth/refresh", { refreshToken });
    useAuthStore.getState().setTokens(data.accessToken, data.refreshToken);
    return data;
  },

  async requestPasswordReset(email: string) {
    const { data } = await api.post("/api/users/password-reset/request", { email });
    return data;
  },

  async resetPassword(token: string, newPassword: string) {
    const { data } = await api.post("/api/users/password-reset/confirm", { token, newPassword });
    return data;
  },
};

export default authService;
