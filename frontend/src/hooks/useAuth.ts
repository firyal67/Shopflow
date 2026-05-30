import { useAuthStore } from "@/store/authStore";
import api from "@/lib/axios";
import { useRouter } from "next/navigation";

export function useAuth() {
  const { user, setAuth, logout, isAuthenticated } = useAuthStore();
  const router = useRouter();

  const login = async (email: string, motDePasse: string) => {
    const { data } = await api.post("/api/auth/login", { email, motDePasse });
    setAuth(data.user, data.accessToken, data.refreshToken);
    return data;
  };

  const register = async (payload: {
    email: string;
    motDePasse: string;
    prenom: string;
    nom: string;
    role?: string;
    nomBoutique?: string;
  }) => {
    const { data } = await api.post("/api/auth/register", payload);
    setAuth(data.user, data.accessToken, data.refreshToken);
    return data;
  };

  const signOut = async () => {
    const refreshToken = useAuthStore.getState().refreshToken;
    if (refreshToken) {
      await api.post("/api/auth/logout", { refreshToken }).catch(() => {});
    }
    logout();
    router.push("/");
  };

  return { user, login, register, signOut, isAuthenticated };
}
