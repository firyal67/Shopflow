"use client";

export const dynamic = "force-dynamic";

import { Suspense } from "react";
import { useQuery } from "@tanstack/react-query";
import { useSearchParams, useRouter } from "next/navigation";
import api from "@/lib/axios";
import ProductCard from "@/components/ui/ProductCard";
import { ProductCardSkeleton } from "@/components/ui/Skeleton";
import { Product, Category, PageResponse } from "@/types";
import { useAuthStore } from "@/store/authStore";
import { useCartStore } from "@/store/cartStore";
import { useState, useEffect } from "react";
import { Search, SlidersHorizontal, X } from "lucide-react";

const SORT_OPTIONS = [
  { label: "Nouveautés", value: "dateCreation,desc" },
  { label: "Prix croissant", value: "prix,asc" },
  { label: "Prix décroissant", value: "prix,desc" },
  { label: "Popularité", value: "totalVentes,desc" },
  { label: "Meilleures notes", value: "noteMoyenne,desc" },
];

function ProductsContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const { user } = useAuthStore();
  const { setCart } = useCartStore();
  const [mounted, setMounted] = useState(false);

  useEffect(() => { setMounted(true); }, []);

  const [page, setPage] = useState(0);
  const [prixMin, setPrixMin] = useState(searchParams.get("prixMin") || "");
  const [prixMax, setPrixMax] = useState(searchParams.get("prixMax") || "");
  const [sort, setSort] = useState("dateCreation,desc");

  const q = searchParams.get("q") || "";
  const categoryId = searchParams.get("categoryId") || "";
  const promo = searchParams.get("promo") || "";

  const queryKey = ["products", q, categoryId, promo, prixMin, prixMax, sort, page];

  const { data, isLoading } = useQuery({
    queryKey,
    queryFn: () => {
      const [sortField, sortDir] = sort.split(",");
      const params = new URLSearchParams({
        page: String(page),
        size: "12",
        sort: `${sortField},${sortDir}`,
      });
      if (categoryId) params.set("categoryId", categoryId);
      if (promo) params.set("promo", promo);
      if (prixMin) params.set("prixMin", prixMin);
      if (prixMax) params.set("prixMax", prixMax);

      const endpoint = q
        ? `/api/products/search?q=${encodeURIComponent(q)}&${params}`
        : `/api/products?${params}`;
      return api.get<PageResponse<Product>>(endpoint).then((r) => r.data);
    },
  });

  const { data: categories } = useQuery({
    queryKey: ["categories"],
    queryFn: () => api.get<Category[]>("/api/categories").then((r) => r.data),
  });

  const handleAddToCart = async (product: Product) => {
    if (!user || user.role !== "CUSTOMER") return;
    try {
      const { data } = await api.post("/api/cart/items", { productId: product.id, quantite: 1 });
      setCart({ items: data.lignes, totalTTC: data.totalTTC, itemCount: data.lignes?.reduce((acc: number, i: any) => acc + i.quantite, 0) ?? 0 });
    } catch (e: any) {
      alert(e.response?.data?.message || "Erreur lors de l'ajout au panier");
    }
  };

  const handleSearch = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const search = (fd.get("q") as string).trim();
    setPage(0);
    const params = new URLSearchParams();
    if (search) params.set("q", search);
    if (categoryId) params.set("categoryId", categoryId);
    router.push(`/products?${params}`);
  };

  const handleCategoryClick = (id?: number) => {
    setPage(0);
    const params = new URLSearchParams();
    if (id) params.set("categoryId", String(id));
    if (q) params.set("q", q);
    router.push(`/products?${params}`);
  };

  const handlePromoToggle = (checked: boolean) => {
    setPage(0);
    const params = new URLSearchParams();
    if (q) params.set("q", q);
    if (categoryId) params.set("categoryId", categoryId);
    if (checked) params.set("promo", "true");
    router.push(`/products?${params}`);
  };

  const clearFilters = () => {
    setPrixMin("");
    setPrixMax("");
    setPage(0);
    router.push("/products");
  };

  const hasActiveFilters = !!(q || categoryId || promo || prixMin || prixMax);

  return (
    <div className="flex gap-6">
      {/* Filtres sidebar */}
      <aside className="hidden lg:block w-56 shrink-0 space-y-6">
        <div className="flex items-center justify-between">
          <h3 className="font-semibold text-gray-900 flex items-center gap-2">
            <SlidersHorizontal size={16} /> Filtres
          </h3>
          {hasActiveFilters && (
            <button onClick={clearFilters} className="text-xs text-primary-600 hover:underline flex items-center gap-1">
              <X size={12} /> Effacer
            </button>
          )}
        </div>

        {/* Catégories */}
        <div>
          <h4 className="text-sm font-medium text-gray-700 mb-2">Catégories</h4>
          <div className="space-y-1">
            <button
              onClick={() => handleCategoryClick()}
              className={`w-full text-left text-sm px-2 py-1.5 rounded-lg transition-colors ${
                !categoryId ? "bg-primary-50 text-primary-600 font-medium" : "text-gray-600 hover:bg-gray-50"
              }`}
            >
              Toutes les catégories
            </button>
            {categories?.map((cat) => (
              <button
                key={cat.id}
                onClick={() => handleCategoryClick(cat.id)}
                className={`w-full text-left text-sm px-2 py-1.5 rounded-lg transition-colors ${
                  categoryId === String(cat.id)
                    ? "bg-primary-50 text-primary-600 font-medium"
                    : "text-gray-600 hover:bg-gray-50"
                }`}
              >
                {cat.nom}
              </button>
            ))}
          </div>
        </div>

        {/* Prix */}
        <div>
          <h4 className="text-sm font-medium text-gray-700 mb-2">Prix (€)</h4>
          <div className="flex gap-2">
            <input
              type="number"
              placeholder="Min"
              value={prixMin}
              onChange={(e) => { setPrixMin(e.target.value); setPage(0); }}
              className="w-full border border-gray-200 rounded-lg px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
            <input
              type="number"
              placeholder="Max"
              value={prixMax}
              onChange={(e) => { setPrixMax(e.target.value); setPage(0); }}
              className="w-full border border-gray-200 rounded-lg px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
        </div>

        {/* Promo */}
        <label className="flex items-center gap-2 cursor-pointer">
          <input
            type="checkbox"
            checked={promo === "true"}
            onChange={(e) => handlePromoToggle(e.target.checked)}
            className="rounded text-primary-600 border-gray-300"
          />
          <span className="text-sm text-gray-700">🔥 En promotion</span>
        </label>
      </aside>

      {/* Contenu principal */}
      <div className="flex-1 min-w-0">
        {/* Barre de recherche */}
        <form onSubmit={handleSearch} className="mb-5 flex gap-2">
          <div className="relative flex-1">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              name="q"
              defaultValue={q}
              placeholder="Rechercher un produit..."
              className="w-full border border-gray-200 rounded-lg pl-9 pr-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
          <button type="submit" className="btn-primary px-5">Rechercher</button>
        </form>

        {/* Barre résultats + tri */}
        <div className="flex items-center justify-between mb-4 gap-4">
          <p className="text-sm text-gray-500 shrink-0">
            {isLoading ? "Chargement..." : `${data?.totalElements ?? 0} produit(s)`}
          </p>
          <div className="flex items-center gap-2">
            <span className="text-sm text-gray-500 hidden sm:inline">Trier par :</span>
            <select
              value={sort}
              onChange={(e) => { setSort(e.target.value); setPage(0); }}
              className="border border-gray-200 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 bg-white"
            >
              {SORT_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>{opt.label}</option>
              ))}
            </select>
          </div>
        </div>

        {/* Filtres actifs (badges) */}
        {hasActiveFilters && (
          <div className="flex flex-wrap gap-2 mb-4">
            {q && (
              <span className="inline-flex items-center gap-1 px-3 py-1 bg-primary-50 text-primary-700 text-xs rounded-full">
                Recherche : "{q}"
                <button onClick={() => router.push("/products")}><X size={12} /></button>
              </span>
            )}
            {categoryId && categories && (
              <span className="inline-flex items-center gap-1 px-3 py-1 bg-primary-50 text-primary-700 text-xs rounded-full">
                {categories.find((c) => String(c.id) === categoryId)?.nom}
                <button onClick={() => handleCategoryClick()}><X size={12} /></button>
              </span>
            )}
            {promo === "true" && (
              <span className="inline-flex items-center gap-1 px-3 py-1 bg-red-50 text-red-600 text-xs rounded-full">
                🔥 Promotions
                <button onClick={() => handlePromoToggle(false)}><X size={12} /></button>
              </span>
            )}
            {(prixMin || prixMax) && (
              <span className="inline-flex items-center gap-1 px-3 py-1 bg-primary-50 text-primary-700 text-xs rounded-full">
                Prix : {prixMin || "0"} – {prixMax || "∞"} €
                <button onClick={() => { setPrixMin(""); setPrixMax(""); }}><X size={12} /></button>
              </span>
            )}
          </div>
        )}

        {/* Grille produits */}
        {isLoading ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 xl:grid-cols-4 gap-4">
            {Array(12).fill(0).map((_, i) => <ProductCardSkeleton key={i} />)}
          </div>
        ) : data?.content.length === 0 ? (
          <div className="text-center py-16 text-gray-500">
            <p className="text-lg font-medium mb-2">Aucun produit trouvé</p>
            <p className="text-sm">Essayez de modifier vos filtres</p>
            {hasActiveFilters && (
              <button onClick={clearFilters} className="btn-primary mt-4">
                Effacer les filtres
              </button>
            )}
          </div>
        ) : (
          <>
            <div className="grid grid-cols-2 sm:grid-cols-3 xl:grid-cols-4 gap-4">
              {data?.content.map((p) => (
                <ProductCard
                  key={p.id}
                  product={p}
                  onAddToCart={mounted && user?.role === "CUSTOMER" ? handleAddToCart : undefined}
                />
              ))}
            </div>

            {/* Pagination */}
            {data && data.totalPages > 1 && (
              <div className="flex justify-center gap-2 mt-8">
                <button
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={page === 0}
                  className="px-3 py-2 rounded-lg text-sm border border-gray-200 disabled:opacity-40 hover:border-primary-500 transition-colors"
                >
                  ←
                </button>
                {Array.from({ length: data.totalPages }, (_, i) => (
                  <button
                    key={i}
                    onClick={() => setPage(i)}
                    className={`w-9 h-9 rounded-lg text-sm font-medium transition-colors ${
                      page === i
                        ? "bg-primary-600 text-white"
                        : "bg-white border border-gray-200 text-gray-700 hover:border-primary-500"
                    }`}
                  >
                    {i + 1}
                  </button>
                ))}
                <button
                  onClick={() => setPage((p) => Math.min(data.totalPages - 1, p + 1))}
                  disabled={page === data.totalPages - 1}
                  className="px-3 py-2 rounded-lg text-sm border border-gray-200 disabled:opacity-40 hover:border-primary-500 transition-colors"
                >
                  →
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}

export default function ProductsPage() {
  return (
    <Suspense fallback={
      <div className="grid grid-cols-2 sm:grid-cols-3 xl:grid-cols-4 gap-4">
        {Array(12).fill(0).map((_, i) => <ProductCardSkeleton key={i} />)}
      </div>
    }>
      <ProductsContent />
    </Suspense>
  );
}
