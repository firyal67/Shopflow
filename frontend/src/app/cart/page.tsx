"use client";

import { useEffect, useState } from "react";
import api from "@/lib/axios";
import { useCartStore } from "@/store/cartStore";
import { Trash2, Plus, Minus, Tag, ShoppingCart } from "lucide-react";
import Link from "next/link";
import Image from "next/image";
import { useRouter } from "next/navigation";

export default function CartPage() {
  const { setCart, clearCart } = useCartStore();
  const router = useRouter();
  const [cart, setLocalCart] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [couponCode, setCouponCode] = useState("");
  const [couponError, setCouponError] = useState("");
  const [updating, setUpdating] = useState(false);

  const fetchCart = async () => {
    try {
      const { data } = await api.get("/api/cart");
      setLocalCart(data);
      setCart({
        items: data.lignes,
        sousTotal: data.sousTotal,
        fraisLivraison: data.fraisLivraison,
        remiseCoupon: data.remiseCoupon,
        totalTTC: data.totalTTC,
        itemCount: data.lignes?.reduce((acc: number, i: any) => acc + i.quantite, 0) ?? 0,
      });
    } catch (e) {
      setLocalCart({ lignes: [] });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCart();
  }, []);

  const updateItem = async (itemId: number, quantite: number) => {
    setUpdating(true);
    try {
      const { data } = await api.put(`/api/cart/items/${itemId}`, { quantite });
      setLocalCart(data);
      setCart({ items: data.lignes, totalTTC: data.totalTTC, itemCount: data.lignes?.reduce((acc: number, i: any) => acc + i.quantite, 0) ?? 0 });
    } catch {} finally {
      setUpdating(false);
    }
  };

  const removeItem = async (itemId: number) => {
    setUpdating(true);
    try {
      const { data } = await api.delete(`/api/cart/items/${itemId}`);
      setLocalCart(data);
      setCart({ items: data.lignes, totalTTC: data.totalTTC, itemCount: data.lignes?.reduce((acc: number, i: any) => acc + i.quantite, 0) ?? 0 });
    } catch {} finally {
      setUpdating(false);
    }
  };

  const applyCoupon = async () => {
    setCouponError("");
    try {
      const { data } = await api.post("/api/cart/coupon", { code: couponCode });
      setLocalCart(data);
      setCouponCode("");
    } catch (e: any) {
      setCouponError(e.response?.data?.message || "Code promo invalide");
    }
  };

  const removeCoupon = async () => {
    try {
      const { data } = await api.delete("/api/cart/coupon");
      setLocalCart(data);
    } catch {}
  };

  if (loading) {
    return (
      <div className="space-y-4">
        {Array(3).fill(0).map((_, i) => (
          <div key={i} className="h-24 bg-zinc-800 rounded-xl animate-pulse" />
        ))}
      </div>
    );
  }

  const lignes = cart?.lignes ?? [];

  if (lignes.length === 0) {
    return (
      <div className="text-center py-16">
        <ShoppingCart size={64} className="text-zinc-700 mx-auto mb-4" />
        <p className="text-xl text-zinc-400 mb-4">Votre panier est vide</p>
        <Link href="/products" className="btn-primary inline-block">
          Continuer mes achats
        </Link>
      </div>
    );
  }

  return (
    <div className="grid lg:grid-cols-3 gap-8">
      {/* Articles */}
      <div className="lg:col-span-2 space-y-4">
        <h1 className="section-title">
          Mon panier ({lignes.length} article{lignes.length > 1 ? "s" : ""})
        </h1>

        {lignes.map((item: any) => (
          <div key={item.id} className="bg-zinc-900 rounded-xl border border-zinc-800 p-4 flex gap-4 hover:border-zinc-700 transition-colors">
            <div className="relative w-20 h-20 rounded-lg overflow-hidden bg-zinc-800 shrink-0">
              {item.productImage ? (
                <Image src={item.productImage} alt={item.productNom} fill className="object-cover" />
              ) : (
                <div className="w-full h-full bg-zinc-800 flex items-center justify-center text-zinc-600 text-xs">
                  No image
                </div>
              )}
            </div>

            <div className="flex-1 min-w-0">
              <h3 className="font-medium text-zinc-100 truncate">{item.productNom}</h3>
              {item.variantAttribut && (
                <p className="text-sm text-zinc-500">{item.variantAttribut}: {item.variantValeur}</p>
              )}
              <p className="text-amber-400 font-semibold">{Number(item.prixUnitaire).toFixed(2)} €</p>
            </div>

            <div className="flex flex-col items-end gap-2">
              <button
                onClick={() => removeItem(item.id)}
                disabled={updating}
                className="text-zinc-500 hover:text-red-400 transition-colors"
              >
                <Trash2 size={16} />
              </button>

              <div className="flex items-center gap-2">
                <button
                  onClick={() => updateItem(item.id, item.quantite - 1)}
                  disabled={updating}
                  className="w-7 h-7 rounded-full border border-zinc-700 flex items-center justify-center text-zinc-300 hover:border-amber-500/50"
                >
                  <Minus size={12} />
                </button>
                <span className="w-8 text-center font-medium text-zinc-100">{item.quantite}</span>
                <button
                  onClick={() => updateItem(item.id, item.quantite + 1)}
                  disabled={updating || item.quantite >= item.stockDisponible}
                  className="w-7 h-7 rounded-full border border-zinc-700 flex items-center justify-center text-zinc-300 hover:border-amber-500/50 disabled:opacity-40"
                >
                  <Plus size={12} />
                </button>
              </div>

              <p className="font-bold text-zinc-100">{Number(item.sousTotal).toFixed(2)} €</p>
            </div>
          </div>
        ))}
      </div>

      {/* Recap */}
      <div>
        <div className="bg-zinc-900 rounded-xl border border-zinc-800 p-4 space-y-4">
          <h2 className="font-bold text-zinc-100 text-lg">Recapitulatif</h2>

          {/* Coupon */}
          {cart?.couponCode ? (
            <div className="flex items-center justify-between bg-green-500/10 border border-green-500/20 rounded-lg px-3 py-2">
              <div className="flex items-center gap-2 text-green-400 text-sm">
                <Tag size={14} />
                <span className="font-medium">{cart.couponCode}</span>
              </div>
              <button onClick={removeCoupon} className="text-green-500 hover:text-red-400 text-xs">
                Retirer
              </button>
            </div>
          ) : (
            <div>
              <div className="flex gap-2">
                <input
                  value={couponCode}
                  onChange={(e) => setCouponCode(e.target.value)}
                  placeholder="Code promo"
                  className="bg-zinc-800 border border-zinc-700 rounded-lg px-3 py-2 text-sm text-zinc-100 placeholder-zinc-500 flex-1 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <button
                  onClick={applyCoupon}
                  className="px-3 py-2 bg-zinc-800 hover:bg-zinc-700 text-zinc-200 text-sm rounded-lg border border-zinc-700"
                >
                  Appliquer
                </button>
              </div>
              {couponError && <p className="text-red-400 text-xs mt-1">{couponError}</p>}
            </div>
          )}

          <div className="space-y-2 text-sm">
            <div className="flex justify-between text-zinc-400">
              <span>Sous-total</span>
              <span>{Number(cart?.sousTotal ?? 0).toFixed(2)} €</span>
            </div>
            {cart?.remiseCoupon > 0 && (
              <div className="flex justify-between text-green-400">
                <span>Remise</span>
                <span>-{Number(cart.remiseCoupon).toFixed(2)} €</span>
              </div>
            )}
            <div className="flex justify-between text-zinc-400">
              <span>Livraison</span>
              <span>{cart?.fraisLivraison === 0 ? "Gratuite" : `${Number(cart?.fraisLivraison ?? 0).toFixed(2)} €`}</span>
            </div>
            <div className="border-t border-zinc-700 pt-2 flex justify-between font-bold text-zinc-100 text-base">
              <span>Total TTC</span>
              <span>{Number(cart?.totalTTC ?? 0).toFixed(2)} €</span>
            </div>
          </div>

          <button
            onClick={() => router.push("/checkout")}
            className="w-full py-3 bg-amber-500 hover:bg-amber-600 text-white font-medium rounded-lg transition-colors"
          >
            Commander
          </button>

          <Link href="/products" className="block text-center text-sm text-zinc-500 hover:text-amber-400">
            Continuer mes achats
          </Link>
        </div>
      </div>
    </div>
  );
}
