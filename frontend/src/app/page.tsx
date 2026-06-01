"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import api from "@/lib/axios";
import { Product, Category } from "@/types";
import ProductCard from "@/components/ui/ProductCard";
import { ProductCardSkeleton } from "@/components/ui/Skeleton";
import { useAuthStore } from "@/store/authStore";
import { useCart } from "@/hooks/useCart";
import { ShoppingBag, Truck, Shield, RotateCcw, Tag, Zap } from "lucide-react";

const CATEGORY_ICONS: Record<string, string> = {
  "Vêtements": "👗",
  "Électronique": "📱",
  "Beauté & Soins": "✨",
  "Sport": "⚽",
  "Maison": "🏠",
  "T-Shirts": "👕",
  "Pantalons": "👖",
  "Robes": "👗",
  "Vestes": "🧥",
  "Audio": "🎧",
  "Accessoires Tech": "🔌",
  "Smartphones": "📲",
  "Soins Peau": "🧴",
  "Vêtements Sport": "🏃",
};

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
      .then(r => setCategories(r.data.filter((c: Category) => !c.parentId)))
      .catch(() => {});
  }, []);

  const handleAddToCart = async (product: Product) => {
    if (!user || user.role !== "CUSTOMER") return;
    try { await addToCart(product.id, 1); }
    catch (e: any) { alert(e.response?.data?.message || "Erreur lors de l'ajout"); }
  };

  return (
    <div className="space-y-14">

      {/* ── Hero Banner ──────────────────────────────────────────────────── */}
      <section className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-indigo-600 via-blue-600 to-blue-500 text-white px-8 py-14">
        <div className="relative z-10 max-w-xl">
          <span className="inline-block bg-white/20 text-white text-xs font-semibold px-3 py-1 rounded-full mb-4 tracking-wide uppercase">
            🔥 Nouveautés printemps 2026
          </span>
          <h1 className="text-4xl font-extrabold leading-tight mb-3">
            Découvrez des produits <span className="text-yellow-300">exceptionnels</span>
          </h1>
          <p className="text-blue-100 text-lg mb-6">
            Mode, tech, beauté — tout ce dont vous avez besoin, livré rapidement.
          </p>
          <div className="flex gap-3 flex-wrap">
            <Link href="/products" className="bg-white text-blue-600 font-bold px-6 py-3 rounded-xl hover:bg-blue-50 transition-colors inline-flex items-center gap-2">
              <ShoppingBag size={18} /> Voir le catalogue
            </Link>
            <Link href="/products?promo=true" className="bg-white/20 text-white font-semibold px-6 py-3 rounded-xl hover:bg-white/30 transition-colors inline-flex items-center gap-2">
              <Tag size={18} /> Promotions
            </Link>
          </div>
        </div>
        {/* Décorations */}
        <div className="absolute right-8 top-1/2 -translate-y-1/2 w-64 h-64 bg-white/10 rounded-full blur-3xl" />
        <div className="absolute right-32 top-8 w-32 h-32 bg-yellow-300/20 rounded-full blur-2xl" />
      </section>

      {/* ── Avantages ────────────────────────────────────────────────────── */}
      <section className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {[
          { icon: <Truck size={22} className="text-blue-600" />, title: "Livraison rapide", desc: "En 24-48h" },
          { icon: <Shield size={22} className="text-green-600" />, title: "Paiement sécurisé", desc: "SSL & 3D Secure" },
          { icon: <RotateCcw size={22} className="text-orange-500" />, title: "Retours gratuits", desc: "30 jours" },
          { icon: <Zap size={22} className="text-purple-600" />, title: "Support 7j/7", desc: "Chat & email" },
        ].map((item, i) => (
          <div key={i} className="bg-white border border-gray-100 rounded-xl p-4 flex items-center gap-3 shadow-sm">
            <div className="p-2 bg-gray-50 rounded-lg shrink-0">{item.icon}</div>
            <div>
              <p className="font-semibold text-gray-900 text-sm">{item.title}</p>
              <p className="text-xs text-gray-500">{item.desc}</p>
            </div>
          </div>
        ))}
      </section>

      {/* ── Catégories ───────────────────────────────────────────────────── */}
      {categories.length > 0 && (
        <section>
          <h2 className="text-2xl font-bold text-gray-900 mb-5">Nos catégories</h2>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-3">
            {categories.map(cat => (
              <Link
                key={cat.id}
                href={`/products?categoryId=${cat.id}`}
                className="group bg-white border border-gray-100 rounded-xl p-4 text-center hover:border-blue-400 hover:shadow-md transition-all"
              >
                <div className="text-3xl mb-2">{CATEGORY_ICONS[cat.nom] || "🛍️"}</div>
                <p className="text-sm font-semibold text-gray-800 group-hover:text-blue-600 transition-colors">{cat.nom}</p>
              </Link>
            ))}
          </div>
        </section>
      )}

      {/* ── Promotions ───────────────────────────────────────────────────── */}
      <section>
        <div className="flex items-center justify-between mb-5">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">🔥 Promotions</h2>
            <p className="text-sm text-gray-500 mt-0.5">Offres limitées, profitez-en !</p>
          </div>
          <Link href="/products?promo=true" className="text-sm font-medium text-blue-600 hover:underline flex items-center gap-1">
            Voir tout →
          </Link>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
          {loadingPromo
            ? Array(4).fill(0).map((_, i) => <ProductCardSkeleton key={i} />)
            : promoProducts.length === 0
              ? <p className="text-gray-400 col-span-4 text-center py-8">Aucune promotion en cours</p>
              : promoProducts.map(p => (
                  <ProductCard key={p.id} product={p} onAddToCart={user?.role === "CUSTOMER" ? handleAddToCart : undefined} />
                ))
          }
        </div>
      </section>

      {/* ── Meilleures ventes ────────────────────────────────────────────── */}
      <section>
        <div className="flex items-center justify-between mb-5">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">⭐ Meilleures ventes</h2>
            <p className="text-sm text-gray-500 mt-0.5">Les produits préférés de nos clients</p>
          </div>
          <Link href="/products" className="text-sm font-medium text-blue-600 hover:underline flex items-center gap-1">
            Voir tout →
          </Link>
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

      {/* ── CTA final ────────────────────────────────────────────────────── */}
      {!user && (
        <section className="bg-gradient-to-r from-gray-900 to-gray-800 rounded-2xl p-8 text-white text-center">
          <h2 className="text-2xl font-bold mb-2">Rejoignez ShopFlow</h2>
          <p className="text-gray-400 mb-6">Créez votre compte et profitez d'offres exclusives dès aujourd'hui</p>
          <div className="flex gap-3 justify-center">
            <Link href="/register" className="bg-blue-600 text-white font-semibold px-6 py-3 rounded-xl hover:bg-blue-700 transition-colors">
              Créer un compte
            </Link>
            <Link href="/login" className="bg-white/10 text-white font-semibold px-6 py-3 rounded-xl hover:bg-white/20 transition-colors">
              Se connecter
            </Link>
          </div>
        </section>
      )}
    </div>
  );
}
