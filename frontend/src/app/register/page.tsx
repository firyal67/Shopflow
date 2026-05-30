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
  motDePasse: z.string()
    .min(8, "Au moins 8 caractères")
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, "Doit contenir majuscule, minuscule et chiffre"),
  prenom: z.string().min(1, "Prénom requis"),
  nom: z.string().min(1, "Nom requis"),
  role: z.enum(["CUSTOMER", "SELLER"]),
  nomBoutique: z.string().optional(),
  descriptionBoutique: z.string().optional(),
}).refine((data) => {
  if (data.role === "SELLER") {
    return data.nomBoutique && data.nomBoutique.trim().length >= 2;
  }
  return true;
}, {
  message: "Le nom de la boutique est obligatoire (min 2 caractères)",
  path: ["nomBoutique"],
});

type FormData = z.infer<typeof schema>;

export default function RegisterPage() {
  const { register: registerUser } = useAuth();
  const router = useRouter();
  const [error, setError] = useState("");

  const { register, handleSubmit, watch, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { role: "CUSTOMER" },
  });

  const role = watch("role");

  const onSubmit = async (data: FormData) => {
    setError("");
    try {
      await registerUser(data);
      router.push(data.role === "SELLER" ? "/seller" : "/");
    } catch (err: any) {
      setError(err.response?.data?.message || "Erreur lors de l'inscription");
    }
  };

  return (
    <div className="max-w-md mx-auto mt-8">
      <div className="card">
        <h1 className="text-2xl font-bold text-gray-900 mb-6">Créer un compte</h1>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4 text-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {/* Type de compte */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Type de compte</label>
            <div className="flex gap-3">
              {(["CUSTOMER", "SELLER"] as const).map((r) => (
                <label key={r} className="flex-1 cursor-pointer">
                  <input {...register("role")} type="radio" value={r} className="sr-only" />
                  <div className={`border-2 rounded-lg p-3 text-center text-sm font-medium transition-colors ${role === r ? "border-primary-500 bg-primary-50 text-primary-700" : "border-gray-200 text-gray-600"}`}>
                    {r === "CUSTOMER" ? "Client" : "Vendeur"}
                  </div>
                </label>
              ))}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Prénom</label>
              <input {...register("prenom")} className="input-field" />
              {errors.prenom && <p className="text-red-500 text-xs mt-1">{errors.prenom.message}</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Nom</label>
              <input {...register("nom")} className="input-field" />
              {errors.nom && <p className="text-red-500 text-xs mt-1">{errors.nom.message}</p>}
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input {...register("email")} type="email" className="input-field" />
            {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email.message}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Mot de passe</label>
            <input {...register("motDePasse")} type="password" className="input-field" />
            {errors.motDePasse && <p className="text-red-500 text-xs mt-1">{errors.motDePasse.message}</p>}
          </div>

          {role === "SELLER" && (
            <>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Nom de la boutique <span className="text-red-500">*</span>
                </label>
                <input {...register("nomBoutique")} className="input-field" placeholder="Ma Super Boutique" />
                {errors.nomBoutique && <p className="text-red-500 text-xs mt-1">{errors.nomBoutique.message}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Description de la boutique</label>
                <textarea {...register("descriptionBoutique")} className="input-field resize-none" rows={2} placeholder="Décrivez votre boutique..." />
              </div>
            </>
          )}

          <button type="submit" disabled={isSubmitting} className="btn-primary w-full py-2.5">
            {isSubmitting ? "Inscription..." : "Créer mon compte"}
          </button>
        </form>

        <p className="text-center text-sm text-gray-600 mt-4">
          Déjà un compte ?{" "}
          <Link href="/login" className="text-primary-600 hover:underline font-medium">
            Se connecter
          </Link>
        </p>
      </div>
    </div>
  );
}
