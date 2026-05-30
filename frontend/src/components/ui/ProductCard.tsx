"use client";

import Link from "next/link";
import Image from "next/image";
import { Product } from "@/types";
import RatingStars from "./RatingStars";
import PriceDisplay from "./PriceDisplay";
import { ShoppingCart } from "lucide-react";
import { useState } from "react";

interface ProductCardProps {
  product: Product;
  onAddToCart?: (product: Product) => void;
}

export default function ProductCard({ product, onAddToCart }: ProductCardProps) {
  const [adding, setAdding] = useState(false);
  const image = product.images?.[0] || null;

  const handleAdd = async (e: React.MouseEvent) => {
    e.preventDefault();
    if (!onAddToCart || adding) return;
    setAdding(true);
    try {
      await onAddToCart(product);
    } finally {
      setAdding(false);
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-4 hover:shadow-md transition-shadow group">
      <Link href={`/products/${product.id}`}>
        <div className="relative aspect-square mb-3 overflow-hidden rounded-lg bg-gray-100">
          {image ? (
            <Image
              src={image}
              alt={product.nom}
              fill
              className="object-cover group-hover:scale-105 transition-transform duration-300"
              onError={(e) => { (e.target as HTMLImageElement).style.display = "none"; }}
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-gray-300 text-sm">
              Pas d'image
            </div>
          )}
          {product.enPromotion && (
            <span className="absolute top-2 left-2 bg-red-500 text-white text-xs font-bold px-2 py-1 rounded-full">
              -{Math.round(product.pourcentageRemise)}%
            </span>
          )}
        </div>
      </Link>

      <div className="space-y-1">
        <p className="text-xs text-gray-500 truncate">{product.sellerNomBoutique}</p>

        <Link href={`/products/${product.id}`}>
          <h3 className="font-medium text-gray-900 line-clamp-2 hover:text-blue-600 transition-colors text-sm">
            {product.nom}
          </h3>
        </Link>

        <div className="flex items-center gap-1">
          <RatingStars rating={product.noteMoyenne ?? 0} size={13} />
          <span className="text-xs text-gray-400">({product.totalVentes ?? 0})</span>
        </div>

        <div className="flex items-center justify-between mt-2">
          <PriceDisplay
            prix={product.prix}
            prixPromo={product.prixPromo}
            enPromotion={product.enPromotion}
            pourcentageRemise={product.pourcentageRemise}
            size="sm"
          />

          {onAddToCart && product.stock > 0 && (
            <button
              onClick={handleAdd}
              disabled={adding}
              className="p-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
              title="Ajouter au panier"
            >
              <ShoppingCart size={15} />
            </button>
          )}
        </div>

        {product.stock === 0 && (
          <p className="text-xs text-red-500 font-medium">Rupture de stock</p>
        )}
      </div>
    </div>
  );
}
