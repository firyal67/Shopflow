"use client";

import Link from "next/link";
import Image from "next/image";
import { Product } from "@/types";
import RatingStars from "./RatingStars";
import PriceDisplay from "./PriceDisplay";
import { ShoppingCart, Eye } from "lucide-react";
import { useState } from "react";

interface ProductCardProps {
  product: Product;
  onAddToCart?: (product: Product) => void;
}

export default function ProductCard({ product, onAddToCart }: ProductCardProps) {
  const [adding, setAdding] = useState(false);
  const [imgError, setImgError] = useState(false);
  const image = !imgError && product.images?.[0] ? product.images[0] : null;

  const handleAdd = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!onAddToCart || adding) return;
    setAdding(true);
    try { await onAddToCart(product); }
    finally { setAdding(false); }
  };

  return (
    <div className="group bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-lg hover:-translate-y-0.5 transition-all duration-200">

      {/* Image */}
      <Link href={`/products/${product.id}`}>
        <div className="relative aspect-square overflow-hidden bg-gray-50">
          {image ? (
            <Image
              src={image}
              alt={product.nom}
              fill
              className="object-cover group-hover:scale-105 transition-transform duration-500"
              onError={() => setImgError(true)}
              sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 25vw"
            />
          ) : (
            <div className="w-full h-full flex flex-col items-center justify-center text-gray-300 gap-2">
              <ShoppingCart size={32} />
              <span className="text-xs">Image indisponible</span>
            </div>
          )}

          {/* Badges */}
          <div className="absolute top-2 left-2 flex flex-col gap-1">
            {product.enPromotion && (
              <span className="bg-red-500 text-white text-xs font-bold px-2 py-0.5 rounded-full shadow">
                -{Math.round(product.pourcentageRemise)}%
              </span>
            )}
            {product.stock === 0 && (
              <span className="bg-gray-800 text-white text-xs font-medium px-2 py-0.5 rounded-full">
                Épuisé
              </span>
            )}
          </div>

          {/* Overlay actions */}
          <div className="absolute inset-0 bg-black/0 group-hover:bg-black/5 transition-colors flex items-end justify-end p-2 opacity-0 group-hover:opacity-100">
            <Link
              href={`/products/${product.id}`}
              className="p-2 bg-white rounded-lg shadow text-gray-700 hover:text-blue-600 transition-colors"
              title="Voir le produit"
              onClick={(e) => e.stopPropagation()}
            >
              <Eye size={16} />
            </Link>
          </div>
        </div>
      </Link>

      {/* Infos */}
      <div className="p-3 space-y-2">
        {/* Vendeur */}
        <p className="text-xs text-gray-400 font-medium truncate">{product.sellerNomBoutique}</p>

        {/* Nom */}
        <Link href={`/products/${product.id}`}>
          <h3 className="font-semibold text-gray-900 line-clamp-2 text-sm leading-snug hover:text-blue-600 transition-colors min-h-[2.5rem]">
            {product.nom}
          </h3>
        </Link>

        {/* Notes */}
        <div className="flex items-center gap-1.5">
          <RatingStars rating={product.noteMoyenne ?? 0} size={12} />
          <span className="text-xs text-gray-400">{(product.noteMoyenne ?? 0).toFixed(1)}</span>
          <span className="text-xs text-gray-300">·</span>
          <span className="text-xs text-gray-400">{product.totalVentes ?? 0} ventes</span>
        </div>

        {/* Prix + Panier */}
        <div className="flex items-center justify-between pt-1">
          <PriceDisplay
            prix={product.prix}
            prixPromo={product.prixPromo}
            enPromotion={product.enPromotion}
            pourcentageRemise={product.pourcentageRemise}
            size="sm"
          />

          {onAddToCart && product.stock > 0 ? (
            <button
              onClick={handleAdd}
              disabled={adding}
              className="flex items-center gap-1.5 bg-blue-600 text-white text-xs font-semibold px-3 py-1.5 rounded-lg hover:bg-blue-700 active:scale-95 transition-all disabled:opacity-60"
              title="Ajouter au panier"
            >
              {adding ? (
                <span className="w-3 h-3 border-2 border-white/40 border-t-white rounded-full animate-spin" />
              ) : (
                <ShoppingCart size={13} />
              )}
              {adding ? "" : "Ajouter"}
            </button>
          ) : product.stock === 0 ? null : (
            <Link
              href={`/products/${product.id}`}
              className="text-xs text-blue-600 font-medium hover:underline"
            >
              Voir →
            </Link>
          )}
        </div>
      </div>
    </div>
  );
}
