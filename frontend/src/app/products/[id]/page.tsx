"use client";

import { useQuery } from "@tanstack/react-query";
import { useParams } from "next/navigation";
import api from "@/lib/axios";
import { Product, Review, PageResponse } from "@/types";
import RatingStars from "@/components/ui/RatingStars";
import { useAuthStore } from "@/store/authStore";
import { useCartStore } from "@/store/cartStore";
import { useState, useEffect } from "react";
import { ShoppingCart, Package } from "lucide-react";
import Image from "next/image";

export default function ProductDetailPage() {
  const { id } = useParams();
  const { user } = useAuthStore();
  const { setCart } = useCartStore();
  const [selectedVariant, setSelectedVariant] = useState<number | null>(null);
  const [selectedImage, setSelectedImage] = useState(0);
  const [addingToCart, setAddingToCart] = useState(false);
  const [mounted, setMounted] = useState(false);

  useEffect(() => { setMounted(true); }, []);

  const { data: product, isLoading } = useQuery({
    queryKey: ["product", id],
    queryFn: () => api.get<Product>(`/api/products/${id}`).then((r) => r.data),
  });

  const { data: reviews } = useQuery({
    queryKey: ["reviews", id],
    queryFn: () =>
      api.get<PageResponse<Review>>(`/api/reviews/product/${id}`).then((r) => r.data),
  });

  const handleAddToCart = async () => {
    if (!user || user.role !== "CUSTOMER" || !product) return;
    setAddingToCart(true);
    try {
      const { data } = await api.post("/api/cart/items", {
        productId: product.id,
        variantId: selectedVariant,
        quantite: 1,
      });
      setCart({ items: data.lignes, totalTTC: data.totalTTC, itemCount: data.lignes?.reduce((acc: number, i: any) => acc + i.quantite, 0) ?? 0 });
    } catch (e: any) {
      alert(e.response?.data?.message || "Erreur lors de l'ajout au panier");
    } finally {
      setAddingToCart(false);
    }
  };

  if (isLoading) {
    return (
      <div className="grid md:grid-cols-2 gap-8 animate-pulse">
        <div className="aspect-square bg-zinc-800 rounded-xl" />
        <div className="space-y-4">
          <div className="h-8 bg-zinc-800 rounded w-3/4" />
          <div className="h-6 bg-zinc-800 rounded w-1/4" />
          <div className="h-24 bg-zinc-800 rounded" />
        </div>
      </div>
    );
  }

  if (!product) return <p className="text-center py-16 text-zinc-500">Produit introuvable</p>;

  const currentPrice = product.enPromotion ? product.prixPromo! : product.prix;
  const variantGroups = product.variants.reduce((acc, v) => {
    if (!acc[v.attribut]) acc[v.attribut] = [];
    acc[v.attribut].push(v);
    return acc;
  }, {} as Record<string, typeof product.variants>);

  return (
    <div className="space-y-12">
      <div className="grid md:grid-cols-2 gap-8">
        <div className="space-y-3">
          <div className="relative aspect-square rounded-xl overflow-hidden bg-zinc-800">
            <Image
              src={product.images[selectedImage] || "/placeholder.svg"}
              alt={product.nom}
              fill
              className="object-cover"
            />
            {product.enPromotion && (
              <span className="absolute top-3 left-3 bg-red-500 text-white text-sm font-bold px-3 py-1 rounded-full shadow-lg shadow-red-500/20">
                -{Math.round(product.pourcentageRemise)}%
              </span>
            )}
          </div>
          {product.images.length > 1 && (
            <div className="flex gap-2">
              {product.images.map((img, i) => (
                <button
                  key={i}
                  onClick={() => setSelectedImage(i)}
                  className={`relative w-16 h-16 rounded-lg overflow-hidden border-2 ${selectedImage === i ? "border-amber-500/50" : "border-zinc-700"}`}
                >
                  <Image src={img} alt="" fill className="object-cover" />
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="space-y-4">
          <div>
            <p className="text-sm text-zinc-500">{product.sellerNomBoutique}</p>
            <h1 className="text-2xl font-bold text-zinc-100">{product.nom}</h1>
          </div>
          <div className="flex items-center gap-3">
            <RatingStars rating={product.noteMoyenne} size={18} />
            <span className="text-sm text-zinc-500">
              {product.noteMoyenne.toFixed(1)} ({reviews?.totalElements ?? 0} avis)
            </span>
          </div>
          <div className="flex items-baseline gap-3">
            <span className="text-3xl font-bold text-zinc-100">{currentPrice.toFixed(2)} €</span>
            {product.enPromotion && (
              <span className="text-xl text-zinc-500 line-through">{product.prix.toFixed(2)} €</span>
            )}
          </div>
          {product.description && (
            <p className="text-zinc-400 leading-relaxed">{product.description}</p>
          )}
          {Object.entries(variantGroups).map(([attribut, variants]) => (
            <div key={attribut}>
              <p className="text-sm font-medium text-zinc-300 mb-2">{attribut}</p>
              <div className="flex flex-wrap gap-2">
                {variants.map((v) => (
                  <button
                    key={v.id}
                    onClick={() => setSelectedVariant(v.id === selectedVariant ? null : v.id)}
                    className={`px-3 py-1.5 border rounded-lg text-sm font-medium transition-colors ${
                      selectedVariant === v.id
                        ? "border-amber-500/50 bg-amber-500/10 text-amber-400"
                        : "border-zinc-700 text-zinc-300 hover:border-amber-500/50"
                    }`}
                  >
                    {v.valeur}
                    {v.prixDelta > 0 && ` (+${v.prixDelta.toFixed(2)}€)`}
                  </button>
                ))}
              </div>
            </div>
          ))}
          <div className="flex items-center gap-2 text-sm">
            <Package size={16} className={product.stock > 0 ? "text-green-400" : "text-red-400"} />
            <span className={product.stock > 0 ? "text-green-400" : "text-red-400"}>
              {product.stock > 0 ? `${product.stock} en stock` : "Rupture de stock"}
            </span>
          </div>
          {product.categories.length > 0 && (
            <div className="flex flex-wrap gap-2">
              {product.categories.map((c) => (
                <span key={c.id} className="px-2 py-1 bg-zinc-800 text-zinc-400 text-xs rounded-full">
                  {c.nom}
                </span>
              ))}
            </div>
          )}
          {mounted && user && (
            <button
              onClick={handleAddToCart}
              disabled={product.stock === 0 || addingToCart}
              className="btn-primary w-full flex items-center justify-center gap-2 py-3"
            >
              <ShoppingCart size={18} />
              {addingToCart ? "Ajout en cours..." : "Ajouter au panier"}
            </button>
          )}
        </div>
      </div>

      <section>
        <h2 className="section-title mb-4">
          Avis clients ({reviews?.totalElements ?? 0})
        </h2>
        {reviews?.content.length === 0 ? (
          <p className="text-zinc-500">Aucun avis pour ce produit.</p>
        ) : (
          <div className="space-y-4">
            {reviews?.content.map((review) => (
              <div key={review.id} className="card">
                <div className="flex items-center justify-between mb-2">
                  <span className="font-medium text-zinc-100">{review.customerNom}</span>
                  <RatingStars rating={review.note} size={14} />
                </div>
                {review.commentaire && <p className="text-zinc-400 text-sm">{review.commentaire}</p>}
                <p className="text-xs text-zinc-600 mt-2">
                  {new Date(review.dateCreation).toLocaleDateString("fr-FR")}
                </p>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
