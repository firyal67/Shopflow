"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/axios";
import { useState } from "react";
import Image from "next/image";
import OrderStatusBadge from "@/components/ui/OrderStatusBadge";
import ImageManager from "@/components/ui/ImageManager";
import { Product } from "@/types";
import { Plus, Edit, Trash2, AlertTriangle, Check, X, ImageIcon, Package, TrendingUp, ShoppingBag } from "lucide-react";
import Link from "next/link";
import { useAuthStore } from "@/store/authStore";

interface EditForm {
  nom: string;
  description: string;
  prix: string;
  prixPromo: string;
  stock: string;
  images: string[];
}

export default function SellerDashboardPage() {
  const queryClient = useQueryClient();
  const { user } = useAuthStore();
  const [activeTab, setActiveTab] = useState<"dashboard" | "products" | "orders">("products");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editForm, setEditForm] = useState<EditForm>({ nom: "", description: "", prix: "", prixPromo: "", stock: "", images: [] });
  const [saveError, setSaveError] = useState("");
  const [imgErrors, setImgErrors] = useState<Record<number, boolean>>({});

  const { data: dashboard } = useQuery({
    queryKey: ["seller-dashboard"],
    queryFn: () => api.get("/api/dashboard/seller").then((r) => r.data),
  });

  const { data: products } = useQuery({
    queryKey: ["seller-products"],
    queryFn: () => api.get(`/api/products?size=100`).then((r) => r.data.content as Product[]),
    enabled: !!user?.id,
  });

  const { data: orders } = useQuery({
    queryKey: ["seller-orders"],
    queryFn: () => api.get("/api/orders/seller?size=20").then((r) => r.data.content),
    enabled: activeTab === "orders",
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, statut }: { id: number; statut: string }) =>
      api.put(`/api/orders/${id}/status`, { statut }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["seller-orders"] }),
  });

  const deactivateMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/api/products/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["seller-products"] }),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: any }) => api.put(`/api/products/${id}`, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["seller-products"] });
      setEditingId(null);
      setSaveError("");
    },
    onError: (e: any) => setSaveError(e.response?.data?.message || "Erreur lors de la mise à jour"),
  });

  const startEdit = (p: Product) => {
    setEditingId(p.id);
    setSaveError("");
    setEditForm({
      nom: p.nom,
      description: p.description || "",
      prix: String(p.prix),
      prixPromo: p.prixPromo ? String(p.prixPromo) : "",
      stock: String(p.stock),
      images: p.images || [],
    });
  };

  const saveEdit = (p: Product) => {
    updateMutation.mutate({
      id: p.id,
      data: {
        nom: editForm.nom,
        description: editForm.description,
        prix: parseFloat(editForm.prix),
        prixPromo: editForm.prixPromo ? parseFloat(editForm.prixPromo) : null,
        stock: parseInt(editForm.stock),
        images: editForm.images,
        categoryIds: p.categories?.map((c: any) => c.id) || [],
      },
    });
  };

  const tabs = [
    { key: "products", label: "📦 Catalogue", count: products?.length },
    { key: "dashboard", label: "📊 Dashboard" },
    { key: "orders", label: "🛒 Commandes" },
  ] as const;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Espace Vendeur</h1>
        <Link href="/seller/products/new" className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-semibold rounded-xl hover:bg-blue-700 transition-colors">
          <Plus size={16} /> Nouveau produit
        </Link>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 bg-gray-100 p-1 rounded-xl w-fit">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-1.5 ${
              activeTab === tab.key ? "bg-white text-gray-900 shadow-sm" : "text-gray-600 hover:text-gray-900"
            }`}
          >
            {tab.label}
            {"count" in tab && tab.count !== undefined && (
              <span className="bg-blue-100 text-blue-700 text-xs font-bold px-1.5 py-0.5 rounded-full">{tab.count}</span>
            )}
          </button>
        ))}
      </div>

      {/* ── CATALOGUE ─────────────────────────────────────────────────────── */}
      {activeTab === "products" && (
        <div className="space-y-4">
          {!products || products.length === 0 ? (
            <div className="text-center py-16 text-gray-400">
              <Package size={48} className="mx-auto mb-3 opacity-30" />
              <p className="font-medium">Aucun produit</p>
              <Link href="/seller/products/new" className="mt-3 inline-block text-sm text-blue-600 hover:underline">
                Créer votre premier produit →
              </Link>
            </div>
          ) : (
            <div className="grid grid-cols-1 gap-4">
              {products.map((p) => (
                <div key={p.id} className="bg-white border border-gray-100 rounded-2xl shadow-sm overflow-hidden">
                  {editingId === p.id ? (
                    /* ── MODE ÉDITION ─────────────────────────────── */
                    <div className="p-5 space-y-4">
                      <div className="flex items-center justify-between mb-2">
                        <h3 className="font-bold text-gray-900">Modifier le produit</h3>
                        <button onClick={() => { setEditingId(null); setSaveError(""); }} className="text-gray-400 hover:text-gray-600">
                          <X size={18} />
                        </button>
                      </div>

                      {saveError && <p className="text-red-500 text-xs bg-red-50 p-2 rounded-lg">{saveError}</p>}

                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                        <div className="sm:col-span-2">
                          <label className="text-xs font-medium text-gray-500 mb-1 block">Nom du produit</label>
                          <input
                            value={editForm.nom}
                            onChange={e => setEditForm({ ...editForm, nom: e.target.value })}
                            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                          />
                        </div>
                        <div className="sm:col-span-2">
                          <label className="text-xs font-medium text-gray-500 mb-1 block">Description</label>
                          <textarea
                            value={editForm.description}
                            onChange={e => setEditForm({ ...editForm, description: e.target.value })}
                            rows={3}
                            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                          />
                        </div>
                        <div>
                          <label className="text-xs font-medium text-gray-500 mb-1 block">Prix (€)</label>
                          <input
                            type="number" step="0.01" min="0"
                            value={editForm.prix}
                            onChange={e => setEditForm({ ...editForm, prix: e.target.value })}
                            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                          />
                        </div>
                        <div>
                          <label className="text-xs font-medium text-gray-500 mb-1 block">Prix promo (€)</label>
                          <input
                            type="number" step="0.01" min="0"
                            value={editForm.prixPromo}
                            onChange={e => setEditForm({ ...editForm, prixPromo: e.target.value })}
                            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="Laisser vide si pas de promo"
                          />
                        </div>
                        <div>
                          <label className="text-xs font-medium text-gray-500 mb-1 block">Stock</label>
                          <input
                            type="number" min="0"
                            value={editForm.stock}
                            onChange={e => setEditForm({ ...editForm, stock: e.target.value })}
                            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                          />
                        </div>
                      </div>

                      {/* Gestionnaire d'images */}
                      <div>
                        <label className="text-xs font-medium text-gray-500 mb-2 block">Photos du produit</label>
                        <ImageManager
                          images={editForm.images}
                          onChange={(imgs) => setEditForm({ ...editForm, images: imgs })}
                          maxImages={5}
                        />
                      </div>

                      <div className="flex gap-2 justify-end pt-2 border-t">
                        <button
                          onClick={() => { setEditingId(null); setSaveError(""); }}
                          className="px-4 py-2 bg-gray-100 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-200"
                        >
                          Annuler
                        </button>
                        <button
                          onClick={() => saveEdit(p)}
                          disabled={updateMutation.isPending}
                          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-semibold rounded-lg hover:bg-blue-700 disabled:opacity-50"
                        >
                          <Check size={14} />
                          {updateMutation.isPending ? "Sauvegarde..." : "Enregistrer"}
                        </button>
                      </div>
                    </div>
                  ) : (
                    /* ── MODE AFFICHAGE ───────────────────────────── */
                    <div className="flex gap-4 p-4">
                      {/* Image principale */}
                      <div className="relative w-20 h-20 rounded-xl overflow-hidden bg-gray-100 shrink-0">
                        {p.images?.[0] && !imgErrors[p.id] ? (
                          <Image
                            src={p.images[0]}
                            alt={p.nom}
                            fill
                            className="object-cover"
                            onError={() => setImgErrors(prev => ({ ...prev, [p.id]: true }))}
                            unoptimized
                          />
                        ) : (
                          <div className="w-full h-full flex items-center justify-center text-gray-300">
                            <ImageIcon size={24} />
                          </div>
                        )}
                        {/* Badge nb images */}
                        {p.images?.length > 1 && (
                          <span className="absolute bottom-1 right-1 bg-black/60 text-white text-xs px-1 rounded">
                            +{p.images.length - 1}
                          </span>
                        )}
                      </div>

                      {/* Infos */}
                      <div className="flex-1 min-w-0">
                        <div className="flex items-start justify-between gap-2">
                          <div className="min-w-0">
                            <p className="font-semibold text-gray-900 truncate">{p.nom}</p>
                            <p className="text-xs text-gray-500 line-clamp-1 mt-0.5">{p.description}</p>
                          </div>
                          {!p.actif && (
                            <span className="shrink-0 text-xs bg-red-100 text-red-600 px-2 py-0.5 rounded-full font-medium">Inactif</span>
                          )}
                        </div>

                        <div className="flex items-center gap-3 mt-2 flex-wrap">
                          <span className="font-bold text-gray-900 text-sm">{Number(p.prix).toFixed(2)} €</span>
                          {p.prixPromo && (
                            <span className="text-xs bg-red-50 text-red-600 px-2 py-0.5 rounded-full font-medium">
                              Promo : {Number(p.prixPromo).toFixed(2)} €
                            </span>
                          )}
                          <span className="text-xs text-gray-500">Stock : <strong className={p.stock < 5 ? "text-red-500" : "text-gray-900"}>{p.stock}</strong></span>
                          <span className="text-xs text-gray-400">{p.categories?.map((c: any) => c.nom).join(", ")}</span>
                        </div>
                      </div>

                      {/* Actions */}
                      <div className="flex items-center gap-1 shrink-0">
                        <button
                          onClick={() => startEdit(p)}
                          className="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                          title="Modifier"
                        >
                          <Edit size={16} />
                        </button>
                        <button
                          onClick={() => {
                            if (confirm(`Supprimer "${p.nom}" définitivement ?`)) {
                              deactivateMutation.mutate(p.id);
                            }
                          }}
                          className="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                          title="Supprimer"
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* ── DASHBOARD ─────────────────────────────────────────────────────── */}
      {activeTab === "dashboard" && dashboard && (
        <div className="space-y-6">
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
            <div className="card text-center">
              <TrendingUp size={24} className="text-blue-600 mx-auto mb-1" />
              <p className="text-2xl font-bold text-blue-600">{Number(dashboard.revenus ?? 0).toFixed(2)} €</p>
              <p className="text-sm text-gray-500">Revenus</p>
            </div>
            <div className="card text-center">
              <ShoppingBag size={24} className="text-gray-600 mx-auto mb-1" />
              <p className="text-2xl font-bold text-gray-900">{dashboard.totalCommandes}</p>
              <p className="text-sm text-gray-500">Commandes</p>
            </div>
            <div className="card text-center col-span-2 md:col-span-1">
              <Package size={24} className="text-yellow-500 mx-auto mb-1" />
              <p className="text-2xl font-bold text-yellow-600">{dashboard.commandesEnAttente}</p>
              <p className="text-sm text-gray-500">En attente</p>
            </div>
          </div>

          {dashboard.alertesStockFaible?.length > 0 && (
            <div className="card border-l-4 border-yellow-400">
              <div className="flex items-center gap-2 mb-3">
                <AlertTriangle size={18} className="text-yellow-500" />
                <h3 className="font-semibold text-gray-900">Alertes stock faible</h3>
              </div>
              <div className="space-y-2">
                {dashboard.alertesStockFaible.map((p: Product) => (
                  <div key={p.id} className="flex justify-between text-sm">
                    <span className="text-gray-700">{p.nom}</span>
                    <span className="text-red-600 font-medium">{p.stock} restant(s)</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* ── COMMANDES ─────────────────────────────────────────────────────── */}
      {activeTab === "orders" && (
        <div className="space-y-3">
          <h2 className="font-semibold text-gray-900">Commandes reçues</h2>
          {!orders || orders.length === 0 ? (
            <p className="text-gray-500 text-sm py-8 text-center">Aucune commande reçue pour l'instant.</p>
          ) : (
            orders.map((order: any) => (
              <div key={order.id} className="card space-y-3">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-mono font-bold text-gray-900 text-sm">{order.numeroCommande}</p>
                    <p className="text-xs text-gray-400">{new Date(order.dateCommande).toLocaleDateString("fr-FR", { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" })}</p>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="font-bold text-gray-900">{Number(order.totalTTC).toFixed(2)} €</span>
                    <OrderStatusBadge status={order.statut} />
                  </div>
                </div>
                {order.lignes?.length > 0 && (
                  <div className="border-t pt-2 space-y-1">
                    {order.lignes.map((item: any) => (
                      <div key={item.id} className="flex justify-between text-sm text-gray-600">
                        <span>{item.productNom} {item.variantValeur ? `(${item.variantValeur})` : ""} × {item.quantite}</span>
                        <span>{Number(item.sousTotal).toFixed(2)} €</span>
                      </div>
                    ))}
                  </div>
                )}
                <div className="flex items-center justify-between pt-1 border-t">
                  <p className="text-xs text-gray-500 truncate max-w-xs">📍 {order.adresseLivraison}</p>
                  <div className="flex gap-2">
                    {order.statut === "PENDING" && (
                      <>
                        <button onClick={() => updateStatusMutation.mutate({ id: order.id, statut: "PROCESSING" })} disabled={updateStatusMutation.isPending} className="flex items-center gap-1 px-3 py-1.5 bg-green-600 text-white text-xs font-medium rounded-lg hover:bg-green-700 disabled:opacity-50">
                          <Check size={13} /> Accepter
                        </button>
                        <button onClick={() => updateStatusMutation.mutate({ id: order.id, statut: "CANCELLED" })} disabled={updateStatusMutation.isPending} className="flex items-center gap-1 px-3 py-1.5 bg-red-500 text-white text-xs font-medium rounded-lg hover:bg-red-600 disabled:opacity-50">
                          <X size={13} /> Refuser
                        </button>
                      </>
                    )}
                    {order.statut === "PROCESSING" && (
                      <button onClick={() => updateStatusMutation.mutate({ id: order.id, statut: "SHIPPED" })} disabled={updateStatusMutation.isPending} className="px-3 py-1.5 bg-blue-600 text-white text-xs font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50">
                        Marquer expédié
                      </button>
                    )}
                    {order.statut === "SHIPPED" && (
                      <button onClick={() => updateStatusMutation.mutate({ id: order.id, statut: "DELIVERED" })} disabled={updateStatusMutation.isPending} className="px-3 py-1.5 bg-gray-700 text-white text-xs font-medium rounded-lg hover:bg-gray-800 disabled:opacity-50">
                        Marquer livré
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}
