"use client";

import Image from "next/image";
import Link from "next/link";
import { Product } from "@/types";
import PriceDisplay from "./PriceDisplay";
import { ShoppingCart, Star } from "lucide-react";

interface Props {
  product: Product;
  onAddToCart?: (product: Product) => void;
}

export default function ProductCard({ product, onAddToCart }: Props) {
  const p = product;

  return (
    <div className="group relative bg-zinc-900/80 backdrop-blur-sm rounded-2xl border border-zinc-800/50 overflow-hidden transition-all duration-300 hover:border-zinc-700/60 hover:shadow-xl hover:shadow-amber-900/5 hover:-translate-y-1">
      {/* Image + quick add */}
      <div className="relative aspect-square bg-zinc-800/50">
        <Link href={`/products/${p.id}`} className="block w-full h-full">
          {p.images && p.images[0] ? (
            <Image
              src={p.images[0]}
              alt={p.nom}
              fill
              className="object-cover transition-transform duration-500 group-hover:scale-110"
              sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 20vw"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-zinc-600 text-sm">
              Pas d'image
            </div>
          )}
        </Link>
        {/* Badges */}
        <div className="absolute top-2 left-2 flex flex-col gap-1.5 pointer-events-none">
          {p.enPromotion && (
            <span className="badge-amber text-[11px] px-2 py-0.5">
              -{p.pourcentageRemise ? Math.round(p.pourcentageRemise) : 0}%
            </span>
          )}
          {p.prixPromo && (
            <span className="badge-red text-[11px] px-2 py-0.5">Promo</span>
          )}
        </div>
        {/* Quick add */}
        {onAddToCart && (
          <button
            type="button"
            onClick={() => onAddToCart(p)}
            className="absolute bottom-2 right-2 w-9 h-9 rounded-xl bg-amber-500 hover:bg-amber-600 text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all duration-200 shadow-lg shadow-amber-900/40 scale-90 group-hover:scale-100"
          >
            <ShoppingCart size={16} />
          </button>
        )}
      </div>

      {/* Infos */}
      <div className="p-3.5 space-y-2">
        <div>
          <Link href={`/products/${p.id}`}>
            <h3 className="font-semibold text-sm text-zinc-100 line-clamp-1 hover:text-amber-400 transition-colors">
              {p.nom}
            </h3>
          </Link>
          <p className="text-xs text-zinc-500 line-clamp-1 mt-0.5">
            {p.description}
          </p>
        </div>

        <div className="flex items-center justify-between">
          <PriceDisplay
            prix={p.prix}
            prixPromo={p.prixPromo}
            enPromotion={p.enPromotion}
            pourcentageRemise={p.pourcentageRemise}
            size="sm"
          />
          {p.noteMoyenne > 0 && (
            <span className="flex items-center gap-1 text-xs text-zinc-500 shrink-0">
              <Star size={12} className="text-amber-400 fill-amber-400" />
              {p.noteMoyenne.toFixed(1)}
            </span>
          )}
        </div>

        <div className="flex items-center justify-between text-[11px]">
          <span className="text-zinc-600">{p.variants?.length || 0} variante{(p.variants?.length || 0) > 1 ? "s" : ""}</span>
          <span className={`font-medium ${p.stock > 10 ? "text-emerald-400" : p.stock > 0 ? "text-amber-400" : "text-red-400"}`}>
            {p.stock > 10 ? "En stock" : p.stock > 0 ? "Stock limité" : "Épuisé"}
          </span>
        </div>
      </div>
    </div>
  );
}
