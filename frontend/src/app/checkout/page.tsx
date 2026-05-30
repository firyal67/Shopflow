"use client";

import { useQuery, useMutation } from "@tanstack/react-query";
import api from "@/lib/axios";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { CheckCircle, Plus } from "lucide-react";
import { useCartStore } from "@/store/cartStore";

export default function CheckoutPage() {
  const router = useRouter();
  const { clearCart } = useCartStore();
  const [selectedAddress, setSelectedAddress] = useState<number | null>(null);
  const [showAddAddress, setShowAddAddress] = useState(false);
  const [newAddress, setNewAddress] = useState({ rue: "", ville: "", codePostal: "", pays: "France" });
  const [orderSuccess, setOrderSuccess] = useState<string | null>(null);

  const { data: addresses, refetch: refetchAddresses } = useQuery({
    queryKey: ["addresses"],
    queryFn: () => api.get("/api/users/me/addresses").then((r) => r.data),
  });

  const { data: cart } = useQuery({
    queryKey: ["cart"],
    queryFn: () => api.get("/api/cart").then((r) => r.data),
  });

  const addAddressMutation = useMutation({
    mutationFn: (data: typeof newAddress) => api.post("/api/users/me/addresses", data),
    onSuccess: () => {
      refetchAddresses();
      setShowAddAddress(false);
    },
  });

  const orderMutation = useMutation({
    mutationFn: (addressId: number) =>
      api.post("/api/orders", { addressId }).then((r) => r.data),
    onSuccess: (data) => {
      clearCart();
      setOrderSuccess(data.numeroCommande);
    },
  });

  if (orderSuccess) {
    return (
      <div className="max-w-md mx-auto text-center py-16">
        <CheckCircle size={64} className="text-green-500 mx-auto mb-4" />
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Commande confirmée !</h1>
        <p className="text-gray-600 mb-2">Numéro de commande :</p>
        <p className="text-xl font-mono font-bold text-primary-600 mb-6">{orderSuccess}</p>
        <button onClick={() => router.push("/orders")} className="btn-primary">
          Voir mes commandes
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Finaliser la commande</h1>

      {/* Adresses */}
      <div className="card space-y-4">
        <h2 className="font-semibold text-gray-900">Adresse de livraison</h2>

        {addresses?.map((addr: any) => (
          <label key={addr.id} className="flex items-start gap-3 cursor-pointer">
            <input
              type="radio"
              name="address"
              value={addr.id}
              checked={selectedAddress === addr.id}
              onChange={() => setSelectedAddress(addr.id)}
              className="mt-1"
            />
            <div className={`flex-1 border rounded-lg p-3 transition-colors ${selectedAddress === addr.id ? "border-primary-500 bg-primary-50" : "border-gray-200"}`}>
              <p className="font-medium text-gray-900">{addr.rue}</p>
              <p className="text-sm text-gray-600">{addr.codePostal} {addr.ville}, {addr.pays}</p>
              {addr.principal && <span className="text-xs text-primary-600 font-medium">Adresse principale</span>}
            </div>
          </label>
        ))}

        {showAddAddress ? (
          <div className="border border-gray-200 rounded-lg p-4 space-y-3">
            <input
              placeholder="Rue"
              value={newAddress.rue}
              onChange={(e) => setNewAddress({ ...newAddress, rue: e.target.value })}
              className="input-field"
            />
            <div className="grid grid-cols-2 gap-3">
              <input
                placeholder="Code postal"
                value={newAddress.codePostal}
                onChange={(e) => setNewAddress({ ...newAddress, codePostal: e.target.value })}
                className="input-field"
              />
              <input
                placeholder="Ville"
                value={newAddress.ville}
                onChange={(e) => setNewAddress({ ...newAddress, ville: e.target.value })}
                className="input-field"
              />
            </div>
            <input
              placeholder="Pays"
              value={newAddress.pays}
              onChange={(e) => setNewAddress({ ...newAddress, pays: e.target.value })}
              className="input-field"
            />
            <div className="flex gap-2">
              <button onClick={() => addAddressMutation.mutate(newAddress)} className="btn-primary text-sm">
                Enregistrer
              </button>
              <button onClick={() => setShowAddAddress(false)} className="btn-secondary text-sm">
                Annuler
              </button>
            </div>
          </div>
        ) : (
          <button
            onClick={() => setShowAddAddress(true)}
            className="flex items-center gap-2 text-sm text-primary-600 hover:underline"
          >
            <Plus size={14} /> Ajouter une adresse
          </button>
        )}
      </div>

      {/* Récapitulatif */}
      {cart && (
        <div className="card space-y-3">
          <h2 className="font-semibold text-gray-900">Récapitulatif</h2>
          {cart.lignes?.map((item: any) => (
            <div key={item.id} className="flex justify-between text-sm text-gray-600">
              <span>{item.productNom} × {item.quantite}</span>
              <span>{item.sousTotal.toFixed(2)} €</span>
            </div>
          ))}
          <div className="border-t pt-2 flex justify-between font-bold text-gray-900">
            <span>Total TTC</span>
            <span>{cart.totalTTC?.toFixed(2)} €</span>
          </div>
        </div>
      )}

      <button
        onClick={() => selectedAddress && orderMutation.mutate(selectedAddress)}
        disabled={!selectedAddress || orderMutation.isPending}
        className="btn-primary w-full py-3 text-base"
      >
        {orderMutation.isPending ? "Traitement..." : "Confirmer la commande"}
      </button>
    </div>
  );
}
