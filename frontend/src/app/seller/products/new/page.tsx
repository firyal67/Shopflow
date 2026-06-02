"use client";

import { useQuery, useMutation } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import api from "@/lib/axios";
import { useForm, useFieldArray, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Category } from "@/types";
import { Plus, Trash2, ArrowLeft } from "lucide-react";
import Link from "next/link";
import ImageManager from "@/components/ui/ImageManager";

const variantSchema = z.object({
  attribut: z.string().min(1, "Attribut requis"),
  valeur: z.string().min(1, "Valeur requise"),
  stockSupplementaire: z.coerce.number().min(0),
  prixDelta: z.coerce.number().min(0),
});

const productSchema = z.object({
  nom: z.string().min(2, "Nom requis (min 2 caractères)"),
  description: z.string().optional(),
  prix: z.coerce.number().positive("Prix doit être positif"),
  prixPromo: z.coerce.number().optional().nullable(),
  stock: z.coerce.number().min(0, "Stock ne peut pas être négatif"),
  images: z.array(z.string()).optional(),
  categoryIds: z.array(z.coerce.number()).min(1, "Sélectionnez au moins une catégorie"),
  variants: z.array(variantSchema).optional(),
});

type ProductForm = z.infer<typeof productSchema>;

export default function NewProductPage() {
  const router = useRouter();

  const { data: categories } = useQuery({
    queryKey: ["categories"],
    queryFn: () => api.get<Category[]>("/api/categories").then((r) => r.data),
  });

  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
  } = useForm<ProductForm>({
    resolver: zodResolver(productSchema),
    defaultValues: { stock: 0, categoryIds: [], variants: [], images: [] },
  });

  const { fields, append, remove } = useFieldArray({ control, name: "variants" });

  const mutation = useMutation({
    mutationFn: (data: ProductForm) => {
      const payload = {
        nom: data.nom,
        description: data.description || "",
        prix: Number(data.prix),
        prixPromo: data.prixPromo ? Number(data.prixPromo) : null,
        stock: Number(data.stock) || 0,
        categoryIds: Array.isArray(data.categoryIds)
          ? data.categoryIds.map((id) => Number(id)).filter((n) => !isNaN(n) && n > 0)
          : [],
        images: data.images || [],
        variants: (data.variants || []).map((v) => ({
          attribut: String(v.attribut),
          valeur: String(v.valeur),
          stockSupplementaire: Number(v.stockSupplementaire) || 0,
          prixDelta: Number(v.prixDelta) || 0,
        })),
      };
      return api.post("/api/products", payload);
    },
    onSuccess: () => router.push("/seller"),
    onError: (e: any) => {
      const msg = e.response?.data?.message || e.message || "Erreur inconnue";
      alert("Erreur: " + msg);
    },
  });

  const onSubmit = (data: ProductForm) => mutation.mutate(data);
  const allCategories = flattenCategories(categories ?? []);

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <Link href="/seller" className="text-gray-400 hover:text-gray-600">
          <ArrowLeft size={20} />
        </Link>
        <h1 className="text-2xl font-bold text-gray-900">Nouveau produit</h1>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        {/* Infos de base */}
        <div className="card space-y-4">
          <h2 className="font-semibold text-gray-900">Informations générales</h2>

          <div>
            <label className="label">Nom du produit *</label>
            <input {...register("nom")} className="input" placeholder="Ex: T-shirt coton bio" />
            {errors.nom && <p className="text-red-500 text-xs mt-1">{errors.nom.message}</p>}
          </div>

          <div>
            <label className="label">Description</label>
            <textarea {...register("description")} className="input min-h-[100px]" placeholder="Description détaillée..." />
          </div>

          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="label">Prix (€) *</label>
              <input {...register("prix")} type="number" step="0.01" className="input" placeholder="0.00" />
              {errors.prix && <p className="text-red-500 text-xs mt-1">{errors.prix.message}</p>}
            </div>
            <div>
              <label className="label">Prix promo (€)</label>
              <input {...register("prixPromo")} type="number" step="0.01" className="input" placeholder="0.00" />
            </div>
            <div>
              <label className="label">Stock *</label>
              <input {...register("stock")} type="number" className="input" placeholder="0" />
              {errors.stock && <p className="text-red-500 text-xs mt-1">{errors.stock.message}</p>}
            </div>
          </div>
        </div>

        {/* Gestionnaire d'images */}
        <div className="card space-y-3">
          <h2 className="font-semibold text-gray-900">Photos du produit</h2>
          <Controller
            control={control}
            name="images"
            render={({ field }) => (
              <ImageManager
                images={field.value || []}
                onChange={field.onChange}
                maxImages={5}
              />
            )}
          />
        </div>

        {/* Catégories */}
        <div className="card space-y-3">
          <h2 className="font-semibold text-gray-900">Catégories *</h2>
          {errors.categoryIds && <p className="text-red-500 text-xs">{errors.categoryIds.message}</p>}
          <div className="grid grid-cols-2 gap-2">
            {allCategories.map((cat) => (
              <label key={cat.id} className="flex items-center gap-2 text-sm cursor-pointer">
                <input
                  type="checkbox"
                  value={cat.id}
                  {...register("categoryIds")}
                  className="rounded border-gray-300 text-primary-600"
                />
                <span className="text-gray-700">{cat.nom}</span>
              </label>
            ))}
          </div>
        </div>

        {/* Variantes */}
        <div className="card space-y-3">
          <div className="flex items-center justify-between">
            <h2 className="font-semibold text-gray-900">Variantes</h2>
            <button
              type="button"
              onClick={() => append({ attribut: "", valeur: "", stockSupplementaire: 0, prixDelta: 0 })}
              className="flex items-center gap-1 text-sm text-primary-600 hover:underline"
            >
              <Plus size={14} /> Ajouter
            </button>
          </div>

          {fields.map((field, index) => (
            <div key={field.id} className="grid grid-cols-4 gap-2 items-end">
              <div>
                <label className="label text-xs">Attribut</label>
                <input {...register(`variants.${index}.attribut`)} className="input text-sm" placeholder="Taille" />
              </div>
              <div>
                <label className="label text-xs">Valeur</label>
                <input {...register(`variants.${index}.valeur`)} className="input text-sm" placeholder="M" />
              </div>
              <div>
                <label className="label text-xs">Stock +</label>
                <input {...register(`variants.${index}.stockSupplementaire`)} type="number" className="input text-sm" />
              </div>
              <div className="flex gap-2 items-end">
                <div className="flex-1">
                  <label className="label text-xs">Prix +€</label>
                  <input {...register(`variants.${index}.prixDelta`)} type="number" step="0.01" className="input text-sm" />
                </div>
                <button type="button" onClick={() => remove(index)} className="p-2 text-red-400 hover:text-red-600 mb-0.5">
                  <Trash2 size={16} />
                </button>
              </div>
            </div>
          ))}
        </div>

        {mutation.isError && (
          <p className="text-red-500 text-sm bg-red-50 p-3 rounded-lg">
            {(mutation.error as any)?.response?.data?.message || "Erreur lors de la création du produit."}
          </p>
        )}

        <div className="flex gap-3">
          <Link href="/seller" className="btn-secondary flex-1 text-center">
            Annuler
          </Link>
          <button type="submit" disabled={mutation.isPending} className="btn-primary flex-1">
            {mutation.isPending ? "Création..." : "Créer le produit"}
          </button>
        </div>
      </form>
    </div>
  );
}

function flattenCategories(cats: Category[]): Category[] {
  return cats.flatMap((c) => [c, ...(c.children ? flattenCategories(c.children) : [])]);
}
