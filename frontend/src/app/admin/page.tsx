"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/axios";
import { useState } from "react";
import OrderStatusBadge from "@/components/ui/OrderStatusBadge";
import { Users, Package, ShoppingBag, TrendingUp, CheckCircle, XCircle } from "lucide-react";

export default function AdminDashboardPage() {
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState<"dashboard" | "users" | "orders" | "reviews">("dashboard");

  const { data: dashboard } = useQuery({
    queryKey: ["admin-dashboard"],
    queryFn: () => api.get("/api/dashboard/admin").then((r) => r.data),
  });

  const { data: users } = useQuery({
    queryKey: ["admin-users"],
    queryFn: () => api.get("/api/users").then((r) => r.data.content),
    enabled: activeTab === "users",
  });

  const { data: orders } = useQuery({
    queryKey: ["admin-orders"],
    queryFn: () => api.get("/api/orders").then((r) => r.data.content),
    enabled: activeTab === "orders",
  });

  const { data: pendingReviews } = useQuery({
    queryKey: ["pending-reviews"],
    queryFn: () => api.get("/api/reviews/pending").then((r) => r.data.content),
    enabled: activeTab === "reviews",
  });

  const toggleUserMutation = useMutation({
    mutationFn: (id: number) => api.put(`/api/users/${id}/toggle-status`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin-users"] }),
  });

  const approveReviewMutation = useMutation({
    mutationFn: (id: number) => api.put(`/api/reviews/${id}/approve`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["pending-reviews"] }),
  });

  const tabs = [
    { key: "dashboard", label: "Dashboard" },
    { key: "users", label: "Utilisateurs" },
    { key: "orders", label: "Commandes" },
    { key: "reviews", label: "Avis" },
  ] as const;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Administration</h1>

      <div className="flex gap-1 bg-gray-100 p-1 rounded-lg w-fit flex-wrap">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${activeTab === tab.key ? "bg-white text-gray-900 shadow-sm" : "text-gray-600 hover:text-gray-900"}`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Dashboard */}
      {activeTab === "dashboard" && dashboard && (
        <div className="space-y-6">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="card text-center">
              <TrendingUp size={24} className="text-primary-600 mx-auto mb-2" />
              <p className="text-2xl font-bold text-gray-900">{dashboard.chiffreAffairesGlobal?.toFixed(2)} €</p>
              <p className="text-xs text-gray-500">Chiffre d'affaires</p>
            </div>
            <div className="card text-center">
              <ShoppingBag size={24} className="text-green-500 mx-auto mb-2" />
              <p className="text-2xl font-bold text-gray-900">{dashboard.totalCommandes}</p>
              <p className="text-xs text-gray-500">Commandes</p>
            </div>
            <div className="card text-center">
              <Users size={24} className="text-blue-500 mx-auto mb-2" />
              <p className="text-2xl font-bold text-gray-900">{dashboard.totalUtilisateurs}</p>
              <p className="text-xs text-gray-500">Utilisateurs</p>
            </div>
            <div className="card text-center">
              <Package size={24} className="text-purple-500 mx-auto mb-2" />
              <p className="text-2xl font-bold text-gray-900">{dashboard.totalProduits}</p>
              <p className="text-xs text-gray-500">Produits</p>
            </div>
          </div>

          <div>
            <h3 className="font-semibold text-gray-900 mb-3">Commandes récentes</h3>
            <div className="space-y-2">
              {dashboard.commandesRecentes?.map((order: any) => (
                <div key={order.id} className="card flex items-center justify-between py-2">
                  <span className="font-mono text-sm text-gray-900">{order.numeroCommande}</span>
                  <div className="flex items-center gap-3">
                    <span className="text-sm font-medium">{order.totalTTC.toFixed(2)} €</span>
                    <OrderStatusBadge status={order.statut} />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Utilisateurs */}
      {activeTab === "users" && (
        <div className="space-y-3">
          {users?.map((user: any) => (
            <div key={user.id} className="card flex items-center justify-between">
              <div>
                <p className="font-medium text-gray-900">{user.prenom} {user.nom}</p>
                <p className="text-sm text-gray-500">{user.email} — {user.role}</p>
              </div>
              <div className="flex items-center gap-3">
                <span className={`text-xs font-medium px-2 py-1 rounded-full ${user.actif ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700"}`}>
                  {user.actif ? "Actif" : "Inactif"}
                </span>
                <button
                  onClick={() => toggleUserMutation.mutate(user.id)}
                  className="text-gray-400 hover:text-primary-600 transition-colors"
                  title={user.actif ? "Désactiver" : "Activer"}
                >
                  {user.actif ? <XCircle size={18} /> : <CheckCircle size={18} />}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Commandes */}
      {activeTab === "orders" && (
        <div className="space-y-3">
          {orders?.map((order: any) => (
            <div key={order.id} className="card flex items-center justify-between">
              <div>
                <p className="font-mono font-bold text-gray-900 text-sm">{order.numeroCommande}</p>
                <p className="text-sm text-gray-500">{order.totalTTC.toFixed(2)} €</p>
              </div>
              <OrderStatusBadge status={order.statut} />
            </div>
          ))}
        </div>
      )}

      {/* Avis à modérer */}
      {activeTab === "reviews" && (
        <div className="space-y-3">
          <h2 className="font-semibold text-gray-900">Avis en attente de modération</h2>
          {pendingReviews?.length === 0 && <p className="text-gray-500 text-sm">Aucun avis en attente</p>}
          {pendingReviews?.map((review: any) => (
            <div key={review.id} className="card space-y-2">
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-medium text-gray-900 text-sm">{review.customerNom}</p>
                  <p className="text-xs text-gray-500">Note: {review.note}/5</p>
                </div>
                <button
                  onClick={() => approveReviewMutation.mutate(review.id)}
                  className="flex items-center gap-1 text-sm text-green-600 hover:underline"
                >
                  <CheckCircle size={14} /> Approuver
                </button>
              </div>
              {review.commentaire && <p className="text-sm text-gray-600">{review.commentaire}</p>}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
