"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import api from "@/lib/axios";
import { Product, Category } from "@/types";
import ProductCard from "@/components/ui/ProductCard";
import { ProductCardSkeleton } from "@/components/ui/Skeleton";
import { useAuthStore } from "@/store/authStore";
import { useCart } from "@/hooks/useCart";
import { ShoppingBag, Truck, Shield, RotateCcw, Zap, ArrowRight, Sparkles, Star } from "lucide-react";
import { useToast } from "@/components/ui/Toast";

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
  const { toast } = useToast();

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
    try { await addToCart(product.id, 1); toast("Ajouté au panier ✓", "success"); }
    catch (e: any) { toast(e.response?.data?.message || "Erreur lors de l'ajout", "error"); }
  };

  return (
    <div className="space-y-16 md:space-y-20">

      {/* ── Hero Banner ─────────────────────────────────────────────── */}
      <section className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#0f0f1a] via-zinc-900 to-[#1a0f0a] text-white px-6 md:px-10 py-14 md:py-20 shadow-2xl border border-zinc-800/30">
        {/* Glow orbs */}
        <div className="absolute -top-20 -right-20 w-80 h-80 bg-amber-500/10 rounded-full blur-3xl" />
        <div className="absolute -bottom-20 -left-20 w-72 h-72 bg-amber-600/5 rounded-full blur-3xl" />

        <div className="relative z-10 max-w-2xl">
          <span className="inline-flex items-center gap-1.5 bg-amber-500/10 text-amber-400 text-xs font-semibold px-3 py-1.5 rounded-full mb-5 border border-amber-500/20">
            <Sparkles size={12} />
            Nouveautés été 2026
          </span>
          <h1 className="text-4xl md:text-5xl lg:text-6xl font-extrabold leading-[1.1] tracking-tight mb-4">
            Des produits <br />
            <span className="gradient-text">exceptionnels</span>
          </h1>
          <p className="text-zinc-400 text-lg md:text-xl max-w-lg mb-8 leading-relaxed">
            Mode, tech, maison — tout ce dont vous avez besoin, livré rapidement.
          </p>
          <div className="flex gap-3 flex-wrap">
            <Link href="/products" className="btn-primary inline-flex items-center gap-2 px-7 py-3.5 text-base">
              <ShoppingBag size={18} /> Voir le catalogue
            </Link>
            <Link href="/products?promo=true" className="btn-outline inline-flex items-center gap-2 px-7 py-3.5 text-base">
              Promotions <ArrowRight size={16} />
            </Link>
          </div>
        </div>
      </section>

      {/* ── Avantages ───────────────────────────────────────────────── */}
      <section className="grid grid-cols-2 md:grid-cols-4 gap-3 md:gap-4">
        {[
          { icon: <Truck size={20} />, title: "Livraison rapide", desc: "En 24-48h", color: "text-amber-400" },
          { icon: <Shield size={20} />, title: "Paiement sécurisé", desc: "SSL & 3D Secure", color: "text-emerald-400" },
          { icon: <RotateCcw size={20} />, title: "Retours gratuits", desc: "30 jours satisfait", color: "text-blue-400" },
          { icon: <Zap size={20} />, title: "Support 7j/7", desc: "Chat & email", color: "text-purple-400" },
        ].map((item, i) => (
          <div key={i} className="card-hover flex items-center gap-3.5 p-4">
            <div className={`p-2.5 rounded-xl bg-zinc-800/80 ${item.color}`}>
              {item.icon}
            </div>
            <div>
              <p className="font-semibold text-zinc-100 text-sm">{item.title}</p>
              <p className="text-xs text-zinc-500 mt-0.5">{item.desc}</p>
            </div>
          </div>
        ))}
      </section>

      {/* ── Catégories ──────────────────────────────────────────────── */}
      {categories.length > 0 && (
        <section>
          <div className="flex items-center justify-between mb-6">
            <div>
              <h2 className="section-title">Nos catégories</h2>
              <p className="section-subtitle">Explorez nos univers</p>
            </div>
            <Link href="/products" className="btn-outline text-xs py-2 px-3.5">
              Tout voir
            </Link>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-3">
            {categories.map(cat => (
              <Link
                key={cat.id}
                href={`/products?categoryId=${cat.id}`}
                className="group card-hover text-center p-5"
              >
                <div className="text-3xl mb-2.5 transition-transform duration-300 group-hover:scale-110">{CATEGORY_ICONS[cat.nom] || "🛍️"}</div>
                <p className="text-sm font-semibold text-zinc-300 group-hover:text-amber-400 transition-colors">{cat.nom}</p>
              </Link>
            ))}
          </div>
        </section>
      )}

      {/* ── Promotions ──────────────────────────────────────────────── */}
      <section>
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="section-title">
              <span className="inline-flex items-center gap-2">
                Promotions
                <Sparkles size={18} className="text-amber-400" />
              </span>
            </h2>
            <p className="section-subtitle">Offres limitées, profitez-en !</p>
          </div>
          <Link href="/products?promo=true" className="text-sm font-medium text-amber-400 hover:text-amber-300 flex items-center gap-1.5 transition-colors">
            Voir tout <ArrowRight size={14} />
          </Link>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
          {loadingPromo
            ? Array(4).fill(0).map((_, i) => <ProductCardSkeleton key={i} />)
            : promoProducts.length === 0
              ? <p className="text-zinc-500 col-span-4 text-center py-12">Aucune promotion en cours</p>
              : promoProducts.map(p => (
                  <ProductCard key={p.id} product={p} onAddToCart={user?.role === "CUSTOMER" ? handleAddToCart : undefined} />
                ))
          }
        </div>
      </section>

      {/* ── Meilleures ventes ───────────────────────────────────────── */}
      <section>
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="section-title">
              <span className="inline-flex items-center gap-2">
                Meilleures ventes
                <Star size={18} className="text-amber-400" />
              </span>
            </h2>
            <p className="section-subtitle">Les produits préférés de nos clients</p>
          </div>
          <Link href="/products" className="text-sm font-medium text-amber-400 hover:text-amber-300 flex items-center gap-1.5 transition-colors">
            Voir tout <ArrowRight size={14} />
          </Link>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
          {loadingTop
            ? Array(5).fill(0).map((_, i) => <ProductCardSkeleton key={i} />)
            : topProducts.length === 0
              ? <p className="text-zinc-500 col-span-5 text-center py-12">Aucun produit disponible</p>
              : topProducts.slice(0, 10).map(p => (
                  <ProductCard key={p.id} product={p} onAddToCart={user?.role === "CUSTOMER" ? handleAddToCart : undefined} />
                ))
          }
        </div>
      </section>

      {/* ── CTA final ───────────────────────────────────────────────── */}
      {!user && (
        <section className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-zinc-900 via-zinc-900/80 to-black p-8 md:p-12 text-center border border-zinc-800/40">
          <div className="absolute inset-0 bg-gradient-to-r from-amber-500/5 via-transparent to-transparent" />
          <div className="relative z-10">
            <h2 className="text-3xl md:text-4xl font-bold text-zinc-100 mb-3">Rejoignez ShopFlow</h2>
            <p className="text-zinc-500 text-lg max-w-md mx-auto mb-8">
              Créez votre compte et profitez d'offres exclusives dès aujourd'hui
            </p>
            <div className="flex gap-3 justify-center flex-wrap">
              <Link href="/register" className="btn-primary inline-flex items-center gap-2 px-7 py-3.5 text-base">
                Créer un compte <ArrowRight size={18} />
              </Link>
              <Link href="/login" className="btn-secondary inline-flex items-center gap-2 px-7 py-3.5 text-base">
                Se connecter
              </Link>
            </div>
          </div>
        </section>
      )}
    </div>
  );
}
