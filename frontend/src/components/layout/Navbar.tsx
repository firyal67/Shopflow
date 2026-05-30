"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { ShoppingCart, User, LogOut, LayoutDashboard, Store } from "lucide-react";
import { useCartStore } from "@/store/cartStore";
import api from "@/lib/axios";

export default function Navbar() {
  const [user, setUser] = useState<any>(null);
  const [mounted, setMounted] = useState(false);
  const { itemCount, setCart } = useCartStore();

  useEffect(() => {
    setMounted(true);
    try {
      const stored = localStorage.getItem("shopflow-auth");
      if (stored) {
        const parsed = JSON.parse(stored);
        const u = parsed?.state?.user || null;
        setUser(u);
        // Charger le panier si CUSTOMER
        if (u?.role === "CUSTOMER") {
          api.get("/api/cart").then(({ data }) => {
            setCart({
              items: data.lignes,
              sousTotal: data.sousTotal,
              fraisLivraison: data.fraisLivraison,
              remiseCoupon: data.remiseCoupon,
              totalTTC: data.totalTTC,
              itemCount: data.lignes?.reduce((acc: number, i: any) => acc + i.quantite, 0) ?? 0,
            });
          }).catch(() => {});
        }
      }
    } catch {}
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("shopflow-auth");
    setUser(null);
    window.location.href = "/";
  };

  if (!mounted) {
    return (
      <nav className="bg-white shadow-sm border-b border-gray-200 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <Link href="/" className="text-2xl font-bold text-blue-600">ShopFlow</Link>
            <div className="flex items-center gap-4">
              <div className="w-16 h-8 bg-gray-100 rounded animate-pulse"></div>
              <div className="w-20 h-8 bg-gray-100 rounded animate-pulse"></div>
            </div>
          </div>
        </div>
      </nav>
    );
  }

  return (
    <nav className="bg-white shadow-sm border-b border-gray-200 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <Link href="/" className="text-2xl font-bold text-blue-600">ShopFlow</Link>

          <div className="hidden md:flex flex-1 max-w-lg mx-8">
            <form action="/products" className="w-full">
              <input
                name="q"
                type="search"
                placeholder="Rechercher des produits..."
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </form>
          </div>

          <div className="flex items-center gap-4">
            {user ? (
              <>
                {user.role === "CUSTOMER" && (
                  <Link href="/cart" className="relative p-2 text-gray-600 hover:text-blue-600">
                    <ShoppingCart size={22} />
                    {itemCount > 0 && (
                      <span className="absolute -top-0.5 -right-0.5 min-w-[18px] h-[18px] bg-blue-600 text-white text-[10px] font-bold rounded-full flex items-center justify-center px-1">
                        {itemCount > 99 ? "99+" : itemCount}
                      </span>
                    )}
                  </Link>
                )}
                {user.role === "SELLER" && (
                  <Link href="/seller" className="flex items-center gap-1 text-sm text-gray-600 hover:text-blue-600">
                    <Store size={18} />
                    <span className="hidden sm:inline">Ma boutique</span>
                  </Link>
                )}
                {user.role === "ADMIN" && (
                  <Link href="/admin" className="flex items-center gap-1 text-sm text-gray-600 hover:text-blue-600">
                    <LayoutDashboard size={18} />
                    <span className="hidden sm:inline">Admin</span>
                  </Link>
                )}
                <Link href="/profile" className="flex items-center gap-1 text-sm text-gray-600 hover:text-blue-600">
                  <User size={18} />
                  <span className="hidden sm:inline">{user.prenom}</span>
                </Link>
                <button onClick={handleLogout} className="p-2 text-gray-500 hover:text-red-500">
                  <LogOut size={18} />
                </button>
              </>
            ) : (
              <>
                <Link href="/login" className="text-sm text-gray-600 hover:text-blue-600">Connexion</Link>
                <Link href="/register" className="px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700">
                  S'inscrire
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
