"use client";

import Link from "next/link";
import { useAuthStore } from "@/store/authStore";
import { useCart } from "@/hooks/useCart";
import { useState } from "react";
import { ShoppingCart, User, ChevronDown, LogOut, Package, LayoutDashboard, Store, Menu, X } from "lucide-react";
import { usePathname } from "next/navigation";

export default function Navbar() {
  const { user, logout } = useAuthStore();
  const { itemCount } = useCart();
  const pathname = usePathname();
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [showMobileMenu, setShowMobileMenu] = useState(false);

  const isActive = (path: string) => pathname === path || pathname.startsWith(path + "/");
  const linkClass = (path: string) =>
    `text-sm font-medium transition-colors duration-200 ${
      isActive(path) ? "text-amber-400" : "text-zinc-400 hover:text-zinc-100"
    }`;

  return (
    <nav className="sticky top-0 z-50 glass border-b border-zinc-800/50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="h-16 flex items-center justify-between gap-4">
          {/* Logo */}
          <Link href="/" className="flex items-center gap-2 shrink-0 group">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-amber-400 to-amber-600 flex items-center justify-center text-white font-extrabold text-base shadow-lg shadow-amber-900/40 group-hover:shadow-amber-900/60 group-hover:scale-105 transition-all duration-300">
              S
            </div>
            <span className="font-bold text-xl tracking-tight text-zinc-100 group-hover:text-amber-400 transition-colors duration-300">ShopFlow</span>
          </Link>

          {/* Desktop links */}
          <div className="hidden md:flex items-center gap-6">
            <Link href="/products" className={linkClass("/products")}>
              Catalogue
            </Link>
            {user?.role === "SELLER" && (
              <Link href="/seller" className={linkClass("/seller")}>
                <Store size={14} className="inline -mt-0.5 mr-1" />
                Vente
              </Link>
            )}
            {user?.role === "ADMIN" && (
              <Link href="/admin" className={linkClass("/admin")}>
                <LayoutDashboard size={14} className="inline -mt-0.5 mr-1" />
                Admin
              </Link>
            )}
          </div>

          {/* Right section */}
          <div className="flex items-center gap-2">
            {/* Cart */}
            <Link
              href="/cart"
              className={`relative p-2 rounded-xl transition-all duration-200 ${
                isActive("/cart") ? "text-amber-400 bg-amber-500/10" : "text-zinc-400 hover:text-zinc-100 hover:bg-zinc-800/50"
              }`}
            >
              <ShoppingCart size={20} />
              {itemCount > 0 && (
                <span className="absolute -top-0.5 -right-0.5 w-5 h-5 rounded-full bg-amber-500 text-white text-[10px] font-bold flex items-center justify-center shadow-lg shadow-amber-900/40">
                  {itemCount > 9 ? "9+" : itemCount}
                </span>
              )}
            </Link>

            {/* User menu */}
            {user ? (
              <div className="relative">
                <button
                  onClick={() => setShowUserMenu(!showUserMenu)}
                  className={`flex items-center gap-2 p-2 rounded-xl transition-all duration-200 ${
                    showUserMenu ? "text-amber-400 bg-amber-500/10" : "text-zinc-400 hover:text-zinc-100 hover:bg-zinc-800/50"
                  }`}
                >
                  <User size={20} />
                  <span className="hidden sm:inline text-sm font-medium max-w-[100px] truncate">
                    {user.prenom}
                  </span>
                  <ChevronDown size={14} className={`hidden sm:block transition-transform duration-200 ${showUserMenu ? "rotate-180" : ""}`} />
                </button>

                {showUserMenu && (
                  <>
                    <div className="fixed inset-0 z-10" onClick={() => setShowUserMenu(false)} />
                    <div className="absolute right-0 mt-2 w-56 glass rounded-2xl shadow-xl shadow-black/40 border border-zinc-700/50 py-1 z-20 animate-scale-in">
                      <div className="px-4 py-2.5 border-b border-zinc-800/50">
                        <p className="text-sm font-semibold text-zinc-100">{user.prenom} {user.nom}</p>
                        <p className="text-xs text-zinc-500 truncate">{user.email}</p>
                      </div>
                      <Link
                        href="/profile"
                        onClick={() => setShowUserMenu(false)}
                        className="flex items-center gap-3 px-4 py-2.5 text-sm text-zinc-300 hover:bg-zinc-800/50 transition-colors"
                      >
                        <User size={16} className="text-zinc-500" />
                        Mon profil
                      </Link>
                      <Link
                        href="/orders"
                        onClick={() => setShowUserMenu(false)}
                        className="flex items-center gap-3 px-4 py-2.5 text-sm text-zinc-300 hover:bg-zinc-800/50 transition-colors"
                      >
                        <Package size={16} className="text-zinc-500" />
                        Mes commandes
                      </Link>
                      {user.role === "SELLER" && (
                        <Link
                          href="/seller"
                          onClick={() => setShowUserMenu(false)}
                          className="flex items-center gap-3 px-4 py-2.5 text-sm text-zinc-300 hover:bg-zinc-800/50 transition-colors"
                        >
                          <Store size={16} className="text-zinc-500" />
                          Espace vendeur
                        </Link>
                      )}
                      <hr className="border-zinc-800/50 my-1" />
                      <button
                        onClick={() => { logout(); setShowUserMenu(false); }}
                        className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-red-400 hover:bg-red-500/10 transition-colors"
                      >
                        <LogOut size={16} />
                        Déconnexion
                      </button>
                    </div>
                  </>
                )}
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link
                  href="/register"
                  className="btn-outline text-sm py-2 px-4"
                >
                  Inscription
                </Link>
                <Link
                  href="/login"
                  className="btn-primary text-sm py-2 px-4"
                >
                  Connexion
                </Link>
              </div>
            )}

            {/* Mobile menu button */}
            <button
              onClick={() => setShowMobileMenu(!showMobileMenu)}
              className="md:hidden p-2 rounded-xl text-zinc-400 hover:text-zinc-100 hover:bg-zinc-800/50 transition-all"
            >
              {showMobileMenu ? <X size={20} /> : <Menu size={20} />}
            </button>
          </div>
        </div>

        {/* Mobile menu */}
        {showMobileMenu && (
          <div className="md:hidden pb-4 border-t border-zinc-800/50 pt-3 space-y-1 animate-slide-up">
            <Link href="/products" onClick={() => setShowMobileMenu(false)} className={`block px-3 py-2 rounded-xl text-sm transition-colors ${isActive("/products") ? "text-amber-400 bg-amber-500/10" : "text-zinc-400 hover:text-zinc-100 hover:bg-zinc-800/50"}`}>
              Catalogue
            </Link>
            {user?.role === "SELLER" && (
              <Link href="/seller" onClick={() => setShowMobileMenu(false)} className={`block px-3 py-2 rounded-xl text-sm transition-colors ${isActive("/seller") ? "text-amber-400 bg-amber-500/10" : "text-zinc-400 hover:text-zinc-100 hover:bg-zinc-800/50"}`}>
                Espace vendeur
              </Link>
            )}
            {user?.role === "ADMIN" && (
              <Link href="/admin" onClick={() => setShowMobileMenu(false)} className={`block px-3 py-2 rounded-xl text-sm transition-colors ${isActive("/admin") ? "text-amber-400 bg-amber-500/10" : "text-zinc-400 hover:text-zinc-100 hover:bg-zinc-800/50"}`}>
                Admin
              </Link>
            )}
            {user && (
              <>
                <Link href="/orders" onClick={() => setShowMobileMenu(false)} className={`block px-3 py-2 rounded-xl text-sm transition-colors ${isActive("/orders") ? "text-amber-400 bg-amber-500/10" : "text-zinc-400 hover:text-zinc-100 hover:bg-zinc-800/50"}`}>
                  Mes commandes
                </Link>
                <button onClick={() => { logout(); setShowMobileMenu(false); }} className="w-full text-left px-3 py-2 rounded-xl text-sm text-red-400 hover:bg-red-500/10 transition-colors">
                  Déconnexion
                </button>
              </>
            )}
          </div>
        )}
      </div>
    </nav>
  );
}
