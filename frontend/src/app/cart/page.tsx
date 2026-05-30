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
          <div key={i} className="h-24 bg-gray-200 rounded-xl animate-pulse" />
        ))}
      </div>
    );
  }

  const lignes = cart?.lignes ?? [];

  if (lignes.length === 0) {
    return (
      <div className="text-center py-16">
        <ShoppingCart size={64} className="text-gray-300 mx-auto mb-4" />
        <p className="text-xl text-gray-500 mb-4">Votre panier est vide</p>
        <Link href="/products" className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 inline-block">
          Continuer mes achats
        </Link>
      </div>
    );
  }

  return (
    <div className="grid lg:grid-cols-3 gap-8">
      {/* Articles */}
      <div className="lg:col-span-2 space-y-4">
        <h1 className="text-2xl font-bold text-gray-900">
          Mon panier ({lignes.length} article{lignes.length > 1 ? "s" : ""})
        </h1>

        {lignes.map((item: any) => (
          <div key={item.id} className="bg-white rounded-xl border border-gray-100 p-4 flex gap-4 shadow-sm">
            <div className="relative w-20 h-20 rounded-lg overflow-hidden bg-gray-100 shrink-0">
              {item.productImage ? (
                <Image src={item.productImage} alt={item.productNom} fill className="object-cover" />
              ) : (
                <div className="w-full h-full bg-gray-200 flex items-center justify-center text-gray-400 text-xs">
                  No image
                </div>
              )}
            </div>

            <div className="flex-1 min-w-0">
              <h3 className="font-medium text-gray-900 truncate">{item.productNom}</h3>
              {item.variantAttribut && (
                <p className="text-sm text-gray-500">{item.variantAttribut}: {item.variantValeur}</p>
              )}
              <p className="text-blue-600 font-semibold">{Number(item.prixUnitaire).toFixed(2)} €</p>
            </div>

            <div className="flex flex-col items-end gap-2">
              <button
                onClick={() => removeItem(item.id)}
                disabled={updating}
                className="text-gray-400 hover:text-red-500 transition-colors"
              >
                <Trash2 size={16} />
              </button>

              <div className="flex items-center gap-2">
                <button
                  onClick={() => updateItem(item.id, item.quantite - 1)}
                  disabled={updating}
                  className="w-7 h-7 rounded-full border border-gray-200 flex items-center justify-center hover:border-blue-500"
                >
                  <Minus size={12} />
                </button>
                <span className="w-8 text-center font-medium">{item.quantite}</span>
                <button
                  onClick={() => updateItem(item.id, item.quantite + 1)}
                  disabled={updating || item.quantite >= item.stockDisponible}
                  className="w-7 h-7 rounded-full border border-gray-200 flex items-center justify-center hover:border-blue-500 disabled:opacity-40"
                >
                  <Plus size={12} />
                </button>
              </div>

              <p className="font-bold text-gray-900">{Number(item.sousTotal).toFixed(2)} €</p>
            </div>
          </div>
        ))}
      </div>

      {/* Recap */}
      <div>
        <div className="bg-white rounded-xl border border-gray-100 p-4 shadow-sm space-y-4">
          <h2 className="font-bold text-gray-900 text-lg">Recapitulatif</h2>

          {/* Coupon */}
          {cart?.couponCode ? (
            <div className="flex items-center justify-between bg-green-50 border border-green-200 rounded-lg px-3 py-2">
              <div className="flex items-center gap-2 text-green-700 text-sm">
                <Tag size={14} />
                <span className="font-medium">{cart.couponCode}</span>
              </div>
              <button onClick={removeCoupon} className="text-green-600 hover:text-red-500 text-xs">
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
                  className="border border-gray-300 rounded-lg px-3 py-2 text-sm flex-1 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <button
                  onClick={applyCoupon}
                  className="px-3 py-2 bg-gray-100 hover:bg-gray-200 text-gray-800 text-sm rounded-lg"
                >
                  Appliquer
                </button>
              </div>
              {couponError && <p className="text-red-500 text-xs mt-1">{couponError}</p>}
            </div>
          )}

          <div className="space-y-2 text-sm">
            <div className="flex justify-between text-gray-600">
              <span>Sous-total</span>
              <span>{Number(cart?.sousTotal ?? 0).toFixed(2)} €</span>
            </div>
            {cart?.remiseCoupon > 0 && (
              <div className="flex justify-between text-green-600">
                <span>Remise</span>
                <span>-{Number(cart.remiseCoupon).toFixed(2)} €</span>
              </div>
            )}
            <div className="flex justify-between text-gray-600">
              <span>Livraison</span>
              <span>{cart?.fraisLivraison === 0 ? "Gratuite" : `${Number(cart?.fraisLivraison ?? 0).toFixed(2)} €`}</span>
            </div>
            <div className="border-t pt-2 flex justify-between font-bold text-gray-900 text-base">
              <span>Total TTC</span>
              <span>{Number(cart?.totalTTC ?? 0).toFixed(2)} €</span>
            </div>
          </div>

          <button
            onClick={() => router.push("/checkout")}
            className="w-full py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors"
          >
            Commander
          </button>

          <Link href="/products" className="block text-center text-sm text-gray-500 hover:text-blue-600">
            Continuer mes achats
          </Link>
        </div>
      </div>
    </div>
  );
}
