"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useAuth } from "@/hooks/useAuth";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useState } from "react";

const schema = z.object({
  email: z.string().email("Email invalide"),
  motDePasse: z.string().min(1, "Mot de passe requis"),
});

type FormData = z.infer<typeof schema>;

export default function LoginPage() {
  const { login } = useAuth();
  const router = useRouter();
  const [error, setError] = useState("");

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data: FormData) => {
    setError("");
    try {
      const result = await login(data.email, data.motDePasse);
      const role = result.user.role;
      if (role === "ADMIN") router.push("/admin");
      else if (role === "SELLER") router.push("/seller");
      else router.push("/");
    } catch (err: any) {
      setError(err.response?.data?.message || "Email ou mot de passe incorrect");
    }
  };

  return (
    <div className="max-w-md mx-auto mt-8">
      <div className="card">
        <h1 className="text-2xl font-bold text-gray-900 mb-6">Connexion</h1>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4 text-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input {...register("email")} type="email" className="input-field" placeholder="vous@exemple.com" />
            {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email.message}</p>}
          </div>

          <div>
            <div className="flex items-center justify-between mb-1">
              <label className="block text-sm font-medium text-gray-700">Mot de passe</label>
              <Link href="/reset-password" className="text-xs text-primary-600 hover:underline">
                Mot de passe oublié ?
              </Link>
            </div>
            <input {...register("motDePasse")} type="password" className="input-field" placeholder="••••••••" />
            {errors.motDePasse && <p className="text-red-500 text-xs mt-1">{errors.motDePasse.message}</p>}
          </div>

          <button type="submit" disabled={isSubmitting} className="btn-primary w-full py-2.5">
            {isSubmitting ? "Connexion..." : "Se connecter"}
          </button>
        </form>

        <p className="text-center text-sm text-gray-600 mt-4">
          Pas encore de compte ?{" "}
          <Link href="/register" className="text-primary-600 hover:underline font-medium">
            S'inscrire
          </Link>
        </p>
      </div>
    </div>
  );
}
