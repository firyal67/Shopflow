"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import Image from "next/image";
import api from "@/lib/axios";
import { Product, Category } from "@/types";
import ProductCard from "@/components/ui/ProductCard";
import { ProductCardSkeleton } from "@/components/ui/Skeleton";
import { useAuthStore } from "@/store/authStore";
import { useCart } from "@/hooks/useCart";

export default function HomePage() {
  const { user } = useAuthStore();
  const { addToCart } = useCart();

  const [topProducts, setTopProducts] = useState<Product[]>([]);
  const [promoProducts, setPromoProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loadingTop, setLoadingTop] = useState(true);
  const [loadingPromo, setLoadingPromo] = useState(true);

  useEffect(() => {
    api.get<Product[]>("/api/products/top-selling")
      .then(r => setTopProducts(r.data))
      .catch(() => {})
      .finally(() => setLoadingTop(false));

    api.get("/api/products?promo=true&size=4")
      .then(r => setPromoProducts(r.data.content))
      .catch(() => {})
      .finally(() => setLoadingPromo(false));

    api.get<Category[]>("/api/categories")
      .then(r => setCategories(r.data))
      .catch(() => {});
  }, []);

  const handleAddToCart = async (product: Product) => {
    if (!user || user.role !== "CUSTOMER") return;
    try {
      await addToCart(product.id, 1);
    } catch (e: any) {
      alert(e.response?.data?.message || "Erreur lors de l'ajout");
    }
  };

  return (
    <div className="space-y-12">
      {/* Banniere */}
      <section className="bg-gradient-to-r from-blue-600 to-blue-700 rounded-2xl p-8 text-white">
        <h1 className="text-3xl font-bold mb-2">Bienvenue sur ShopFlow</h1>
        <p className="text-blue-100 mb-4">Decouvrez des milliers de produits au meilleur prix</p>
        <Link href="/products" className="bg-white text-blue-600 font-semibold px-6 py-2 rounded-lg hover:bg-blue-50 transition-colors inline-block">
          Voir le catalogue
        </Link>
      </section>

      {/* Categories */}
      {categories.length > 0 && (
        <section>
          <h2 className="text-xl font-bold text-gray-900 mb-4">Categories</h2>
          <div className="flex flex-wrap gap-3">
            {categories.map(cat => (
              <Link
                key={cat.id}
                href={`/products?categoryId=${cat.id}`}
                className="px-4 py-2 bg-white border border-gray-200 rounded-full text-sm font-medium text-gray-700 hover:border-blue-500 hover:text-blue-600 transition-colors"
              >
                {cat.nom}
              </Link>
            ))}
          </div>
        </section>
      )}

      {/* Promotions */}
      <section>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-bold text-gray-900">Promotions</h2>
          <Link href="/products?promo=true" className="text-sm text-blue-600 hover:underline">Voir tout</Link>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
          {loadingPromo
            ? Array(4).fill(0).map((_, i) => <ProductCardSkeleton key={i} />)
            : promoProducts.length === 0
              ? <p className="text-gray-400 col-span-4 text-center py-8">Aucun produit en promotion</p>
              : promoProducts.map(p => (
                  <ProductCard key={p.id} product={p} onAddToCart={user?.role === "CUSTOMER" ? handleAddToCart : undefined} />
                ))
          }
        </div>
      </section>

      {/* Meilleures ventes */}
      <section>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-bold text-gray-900">Meilleures ventes</h2>
          <Link href="/products" className="text-sm text-blue-600 hover:underline">Voir tout</Link>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
          {loadingTop
            ? Array(5).fill(0).map((_, i) => <ProductCardSkeleton key={i} />)
            : topProducts.length === 0
              ? <p className="text-gray-400 col-span-5 text-center py-8">Aucun produit disponible</p>
              : topProducts.slice(0, 10).map(p => (
                  <ProductCard key={p.id} product={p} onAddToCart={user?.role === "CUSTOMER" ? handleAddToCart : undefined} />
                ))
          }
        </div>
      </section>
    </div>
  );
}
