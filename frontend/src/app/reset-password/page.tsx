"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import api from "@/lib/axios";
import Link from "next/link";
import { CheckCircle, Mail, KeyRound } from "lucide-react";

const requestSchema = z.object({
  email: z.string().email("Email invalide"),
});

const resetSchema = z.object({
  token: z.string().min(1, "Token requis"),
  newPassword: z.string()
    .min(8, "Au moins 8 caractères")
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, "Doit contenir majuscule, minuscule et chiffre"),
});

type RequestForm = z.infer<typeof requestSchema>;
type ResetForm = z.infer<typeof resetSchema>;

export default function ResetPasswordPage() {
  const [step, setStep] = useState<"request" | "reset" | "done">("request");
  const [token, setToken] = useState("");
  const [error, setError] = useState("");

  const requestForm = useForm<RequestForm>({ resolver: zodResolver(requestSchema) });
  const resetForm = useForm<ResetForm>({ resolver: zodResolver(resetSchema) });

  const onRequest = async (data: RequestForm) => {
    setError("");
    try {
      const res = await api.post("/api/users/password-reset/request", { email: data.email });
      // En dev, le token est retourné directement dans la réponse
      if (res.data?.token) {
        setToken(res.data.token);
        resetForm.setValue("token", res.data.token);
      }
      setStep("reset");
    } catch (err: any) {
      setError(err.response?.data?.message || "Email introuvable");
    }
  };

  const onReset = async (data: ResetForm) => {
    setError("");
    try {
      await api.post("/api/users/password-reset/confirm", {
        token: data.token,
        newPassword: data.newPassword,
      });
      setStep("done");
    } catch (err: any) {
      setError(err.response?.data?.message || "Token invalide ou expiré");
    }
  };

  return (
    <div className="max-w-md mx-auto mt-8">
      <div className="card">
        {step === "request" && (
          <>
            <div className="flex items-center gap-3 mb-6">
              <Mail size={24} className="text-primary-600" />
              <h1 className="text-2xl font-bold text-gray-900">Mot de passe oublié</h1>
            </div>
            <p className="text-sm text-gray-600 mb-4">
              Entrez votre email pour recevoir un lien de réinitialisation.
            </p>
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4 text-sm">
                {error}
              </div>
            )}
            <form onSubmit={requestForm.handleSubmit(onRequest)} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                <input
                  {...requestForm.register("email")}
                  type="email"
                  className="input-field"
                  placeholder="vous@exemple.com"
                />
                {requestForm.formState.errors.email && (
                  <p className="text-red-500 text-xs mt-1">{requestForm.formState.errors.email.message}</p>
                )}
              </div>
              <button
                type="submit"
                disabled={requestForm.formState.isSubmitting}
                className="btn-primary w-full py-2.5"
              >
                {requestForm.formState.isSubmitting ? "Envoi..." : "Envoyer le lien"}
              </button>
            </form>
          </>
        )}

        {step === "reset" && (
          <>
            <div className="flex items-center gap-3 mb-6">
              <KeyRound size={24} className="text-primary-600" />
              <h1 className="text-2xl font-bold text-gray-900">Nouveau mot de passe</h1>
            </div>
            {token && (
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 mb-4">
                <p className="text-xs text-blue-700 font-medium">Token (mode démo) :</p>
                <p className="text-xs text-blue-600 font-mono break-all">{token}</p>
              </div>
            )}
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4 text-sm">
                {error}
              </div>
            )}
            <form onSubmit={resetForm.handleSubmit(onReset)} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Token</label>
                <input
                  {...resetForm.register("token")}
                  className="input-field font-mono text-sm"
                  placeholder="Token reçu par email"
                />
                {resetForm.formState.errors.token && (
                  <p className="text-red-500 text-xs mt-1">{resetForm.formState.errors.token.message}</p>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Nouveau mot de passe</label>
                <input
                  {...resetForm.register("newPassword")}
                  type="password"
                  className="input-field"
                  placeholder="••••••••"
                />
                {resetForm.formState.errors.newPassword && (
                  <p className="text-red-500 text-xs mt-1">{resetForm.formState.errors.newPassword.message}</p>
                )}
              </div>
              <button
                type="submit"
                disabled={resetForm.formState.isSubmitting}
                className="btn-primary w-full py-2.5"
              >
                {resetForm.formState.isSubmitting ? "Réinitialisation..." : "Réinitialiser"}
              </button>
            </form>
          </>
        )}

        {step === "done" && (
          <div className="text-center py-4">
            <CheckCircle size={48} className="text-green-500 mx-auto mb-4" />
            <h2 className="text-xl font-bold text-gray-900 mb-2">Mot de passe modifié !</h2>
            <p className="text-gray-600 mb-6 text-sm">Vous pouvez maintenant vous connecter avec votre nouveau mot de passe.</p>
            <Link href="/login" className="btn-primary">Se connecter</Link>
          </div>
        )}

        {step !== "done" && (
          <p className="text-center text-sm text-gray-600 mt-4">
            <Link href="/login" className="text-primary-600 hover:underline">Retour à la connexion</Link>
          </p>
        )}
      </div>
    </div>
  );
}
