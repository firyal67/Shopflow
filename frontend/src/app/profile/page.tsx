"use client";

import { useAuthStore } from "@/store/authStore";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/axios";
import { useState } from "react";
import { Plus, Trash2, MapPin, Star } from "lucide-react";
import RatingStars from "@/components/ui/RatingStars";
import Link from "next/link";

export default function ProfilePage() {
  const { user } = useAuthStore();
  const queryClient = useQueryClient();
  const [showAddAddress, setShowAddAddress] = useState(false);
  const [newAddress, setNewAddress] = useState({ rue: "", ville: "", codePostal: "", pays: "France", principal: false });

  const { data: addresses } = useQuery({
    queryKey: ["addresses"],
    queryFn: () => api.get("/api/users/me/addresses").then((r) => r.data),
  });

  const { data: myReviews = [] } = useQuery({
    queryKey: ["my-reviews"],
    queryFn: () => api.get("/api/reviews/my").then((r) => {
      const data = r.data;
      if (Array.isArray(data)) return data;
      if (data?.content) return data.content;
      return [];
    }).catch(() => []),
  });

  const { data: myOrders } = useQuery({
    queryKey: ["my-orders-profile"],
    queryFn: () => api.get("/api/orders/my?size=3").then((r) => r.data.content),
  });

  const addMutation = useMutation({
    mutationFn: (data: typeof newAddress) => api.post("/api/users/me/addresses", data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["addresses"] });
      setShowAddAddress(false);
      setNewAddress({ rue: "", ville: "", codePostal: "", pays: "France", principal: false });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/api/users/me/addresses/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["addresses"] }),
  });

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Mon profil</h1>

      {/* Infos personnelles */}
      <div className="card space-y-3">
        <h2 className="font-semibold text-gray-900">Informations personnelles</h2>
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <p className="text-gray-500">Prénom</p>
            <p className="font-medium text-gray-900">{user?.prenom}</p>
          </div>
          <div>
            <p className="text-gray-500">Nom</p>
            <p className="font-medium text-gray-900">{user?.nom}</p>
          </div>
          <div className="col-span-2">
            <p className="text-gray-500">Email</p>
            <p className="font-medium text-gray-900">{user?.email}</p>
          </div>
          <div>
            <p className="text-gray-500">Rôle</p>
            <p className="font-medium text-gray-900">{user?.role}</p>
          </div>
        </div>
      </div>

      {/* Commandes récentes */}
      <div className="card space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="font-semibold text-gray-900">Commandes en cours</h2>
          <Link href="/orders" className="text-sm text-primary-600 hover:underline">Voir tout</Link>
        </div>
        {myOrders?.length === 0 && <p className="text-sm text-gray-500">Aucune commande</p>}
        {myOrders?.map((order: any) => (
          <div key={order.id} className="flex items-center justify-between text-sm border border-gray-100 rounded-lg p-3">
            <div>
              <p className="font-mono font-bold text-gray-900">{order.numeroCommande}</p>
              <p className="text-gray-500">{order.totalTTC.toFixed(2)} €</p>
            </div>
            <div className="flex items-center gap-3">
              <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                order.statut === "DELIVERED" ? "bg-green-100 text-green-700" :
                order.statut === "CANCELLED" ? "bg-red-100 text-red-700" :
                "bg-yellow-100 text-yellow-700"
              }`}>{order.statut}</span>
              <Link href={`/orders/${order.id}`} className="text-primary-600 hover:underline">Détail</Link>
            </div>
          </div>
        ))}
      </div>

      {/* Adresses */}
      <div className="card space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="font-semibold text-gray-900 flex items-center gap-2">
            <MapPin size={16} /> Adresses de livraison
          </h2>
          <button
            onClick={() => setShowAddAddress(!showAddAddress)}
            className="flex items-center gap-1 text-sm text-primary-600 hover:underline"
          >
            <Plus size={14} /> Ajouter
          </button>
        </div>

        {addresses?.length === 0 && <p className="text-sm text-gray-500">Aucune adresse enregistrée</p>}
        {addresses?.map((addr: any) => (
          <div key={addr.id} className="flex items-start justify-between border border-gray-100 rounded-lg p-3">
            <div>
              <p className="font-medium text-gray-900 text-sm">{addr.rue}</p>
              <p className="text-sm text-gray-500">{addr.codePostal} {addr.ville}, {addr.pays}</p>
              {addr.principal && <span className="text-xs text-primary-600 font-medium">Principale</span>}
            </div>
            <button
              onClick={() => deleteMutation.mutate(addr.id)}
              className="text-gray-400 hover:text-red-500 transition-colors"
            >
              <Trash2 size={14} />
            </button>
          </div>
        ))}

        {showAddAddress && (
          <div className="border border-gray-200 rounded-lg p-4 space-y-3">
            <input placeholder="Rue" value={newAddress.rue} onChange={(e) => setNewAddress({ ...newAddress, rue: e.target.value })} className="input-field" />
            <div className="grid grid-cols-2 gap-3">
              <input placeholder="Code postal" value={newAddress.codePostal} onChange={(e) => setNewAddress({ ...newAddress, codePostal: e.target.value })} className="input-field" />
              <input placeholder="Ville" value={newAddress.ville} onChange={(e) => setNewAddress({ ...newAddress, ville: e.target.value })} className="input-field" />
            </div>
            <input placeholder="Pays" value={newAddress.pays} onChange={(e) => setNewAddress({ ...newAddress, pays: e.target.value })} className="input-field" />
            <label className="flex items-center gap-2 text-sm text-gray-700 cursor-pointer">
              <input type="checkbox" checked={newAddress.principal} onChange={(e) => setNewAddress({ ...newAddress, principal: e.target.checked })} />
              Adresse principale
            </label>
            <div className="flex gap-2">
              <button onClick={() => addMutation.mutate(newAddress)} className="btn-primary text-sm">Enregistrer</button>
              <button onClick={() => setShowAddAddress(false)} className="btn-secondary text-sm">Annuler</button>
            </div>
          </div>
        )}
      </div>

      {/* Mes avis */}
      <div className="card space-y-3">
        <h2 className="font-semibold text-gray-900 flex items-center gap-2">
          <Star size={16} className="text-yellow-400" /> Mes derniers avis
        </h2>
        {myReviews.length === 0 ? (
          <p className="text-sm text-gray-500">Vous n'avez pas encore laissé d'avis.</p>
        ) : (
          myReviews.map((review: any) => (
            <div key={review.id} className="border border-gray-100 rounded-lg p-3 space-y-1">
              <div className="flex items-center justify-between">
                <Link href={`/products/${review.productId}`} className="font-medium text-gray-900 text-sm hover:text-primary-600">
                  Produit #{review.productId}
                </Link>
                <RatingStars rating={review.note} size={14} />
              </div>
              {review.commentaire && <p className="text-sm text-gray-600">{review.commentaire}</p>}
              <p className="text-xs text-gray-400">
                {new Date(review.dateCreation).toLocaleDateString("fr-FR")}
                {!review.approuve && <span className="ml-2 text-yellow-600">En attente de modération</span>}
              </p>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
