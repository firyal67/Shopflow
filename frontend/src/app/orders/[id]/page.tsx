"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useParams } from "next/navigation";
import api from "@/lib/axios";
import OrderStatusBadge from "@/components/ui/OrderStatusBadge";
import { Order } from "@/types";
import Image from "next/image";
import { useState } from "react";

export default function OrderDetailPage() {
  const { id } = useParams();
  const queryClient = useQueryClient();
  const [reviewProductId, setReviewProductId] = useState<number | null>(null);
  const [reviewNote, setReviewNote] = useState(5);
  const [reviewComment, setReviewComment] = useState("");

  const { data: order, isLoading } = useQuery({
    queryKey: ["order", id],
    queryFn: () => api.get<Order>(`/api/orders/${id}`).then((r) => r.data),
  });

  const cancelMutation = useMutation({
    mutationFn: () => api.put(`/api/orders/${id}/cancel`).then((r) => r.data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["order", id] }),
  });

  const reviewMutation = useMutation({
    mutationFn: () =>
      api.post("/api/reviews", { productId: reviewProductId, note: reviewNote, commentaire: reviewComment }),
    onSuccess: () => {
      setReviewProductId(null);
      setReviewComment("");
    },
  });

  if (isLoading) return <div className="animate-pulse h-64 bg-gray-200 rounded-xl" />;
  if (!order) return <p className="text-center py-16 text-gray-500">Commande introuvable</p>;

  const canCancel = order.statut === "PENDING" || order.statut === "PAID";
  const canReview = order.statut === "DELIVERED";

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900 font-mono">{order.numeroCommande}</h1>
          <p className="text-sm text-gray-500">
            {new Date(order.dateCommande).toLocaleDateString("fr-FR", { day: "numeric", month: "long", year: "numeric" })}
          </p>
        </div>
        <OrderStatusBadge status={order.statut} />
      </div>

      {/* Articles */}
      <div className="card space-y-3">
        <h2 className="font-semibold text-gray-900">Articles</h2>
        {order.lignes.map((item) => (
          <div key={item.id} className="flex gap-3 items-center">
            <div className="relative w-14 h-14 rounded-lg overflow-hidden bg-gray-100 shrink-0">
              <Image src={item.productImage || "/placeholder.png"} alt={item.productNom} fill className="object-cover" />
            </div>
            <div className="flex-1">
              <p className="font-medium text-gray-900 text-sm">{item.productNom}</p>
              {item.variantAttribut && (
                <p className="text-xs text-gray-500">{item.variantAttribut}: {item.variantValeur}</p>
              )}
              <p className="text-xs text-gray-500">×{item.quantite} — {item.prixUnitaire.toFixed(2)} €/u</p>
            </div>
            <p className="font-semibold text-gray-900">{item.sousTotal.toFixed(2)} €</p>

            {canReview && (
              <button
                onClick={() => setReviewProductId(item.productId)}
                className="text-xs text-primary-600 hover:underline"
              >
                Laisser un avis
              </button>
            )}
          </div>
        ))}
      </div>

      {/* Formulaire avis */}
      {reviewProductId && (
        <div className="card space-y-3">
          <h3 className="font-semibold text-gray-900">Votre avis</h3>
          <div className="flex gap-2">
            {[1, 2, 3, 4, 5].map((n) => (
              <button
                key={n}
                onClick={() => setReviewNote(n)}
                className={`w-9 h-9 rounded-full border-2 font-bold text-sm transition-colors ${reviewNote >= n ? "border-yellow-400 bg-yellow-50 text-yellow-600" : "border-gray-200 text-gray-400"}`}
              >
                {n}
              </button>
            ))}
          </div>
          <textarea
            value={reviewComment}
            onChange={(e) => setReviewComment(e.target.value)}
            placeholder="Votre commentaire (optionnel)"
            className="input-field resize-none"
            rows={3}
          />
          <div className="flex gap-2">
            <button onClick={() => reviewMutation.mutate()} className="btn-primary text-sm">
              Publier
            </button>
            <button onClick={() => setReviewProductId(null)} className="btn-secondary text-sm">
              Annuler
            </button>
          </div>
        </div>
      )}

      {/* Adresse + totaux */}
      <div className="card space-y-3">
        <h2 className="font-semibold text-gray-900">Livraison</h2>
        <p className="text-sm text-gray-600">{order.adresseLivraison}</p>
      </div>

      <div className="card space-y-2">
        <div className="flex justify-between text-sm text-gray-600">
          <span>Sous-total</span><span>{order.sousTotal.toFixed(2)} €</span>
        </div>
        {order.remiseCoupon > 0 && (
          <div className="flex justify-between text-sm text-green-600">
            <span>Remise ({order.couponCode})</span><span>-{order.remiseCoupon.toFixed(2)} €</span>
          </div>
        )}
        <div className="flex justify-between text-sm text-gray-600">
          <span>Livraison</span>
          <span>{order.fraisLivraison === 0 ? "Gratuite" : `${order.fraisLivraison.toFixed(2)} €`}</span>
        </div>
        <div className="border-t pt-2 flex justify-between font-bold text-gray-900">
          <span>Total TTC</span><span>{order.totalTTC.toFixed(2)} €</span>
        </div>
      </div>

      {canCancel && (
        <button
          onClick={() => cancelMutation.mutate()}
          disabled={cancelMutation.isPending}
          className="w-full py-2 border-2 border-red-300 text-red-600 rounded-lg hover:bg-red-50 transition-colors text-sm font-medium"
        >
          {cancelMutation.isPending ? "Annulation..." : "Annuler la commande"}
        </button>
      )}
    </div>
  );
}
