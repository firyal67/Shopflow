"use client";

import { useQuery } from "@tanstack/react-query";
import api from "@/lib/axios";
import OrderStatusBadge from "@/components/ui/OrderStatusBadge";
import { Order } from "@/types";
import Link from "next/link";
import { Package } from "lucide-react";

export default function OrdersPage() {
  const { data, isLoading } = useQuery({
    queryKey: ["my-orders"],
    queryFn: () => api.get("/api/orders/my").then((r) => r.data),
  });

  if (isLoading) {
    return <div className="space-y-4">{Array(3).fill(0).map((_, i) => <div key={i} className="h-24 bg-gray-200 rounded-xl animate-pulse" />)}</div>;
  }

  const orders: Order[] = data?.content ?? [];

  return (
    <div className="max-w-3xl mx-auto space-y-4">
      <h1 className="text-2xl font-bold text-gray-900">Mes commandes</h1>

      {orders.length === 0 ? (
        <div className="text-center py-16">
          <Package size={48} className="text-gray-300 mx-auto mb-4" />
          <p className="text-gray-500">Aucune commande pour le moment</p>
          <Link href="/products" className="btn-primary mt-4 inline-block">
            Commencer mes achats
          </Link>
        </div>
      ) : (
        orders.map((order) => (
          <div key={order.id} className="card hover:shadow-md transition-shadow">
            <div className="flex items-start justify-between mb-3">
              <div>
                <p className="font-mono font-bold text-gray-900">{order.numeroCommande}</p>
                <p className="text-sm text-gray-500">
                  {new Date(order.dateCommande).toLocaleDateString("fr-FR", {
                    day: "numeric", month: "long", year: "numeric"
                  })}
                </p>
              </div>
              <div className="flex items-center gap-3">
                {order.isNew && (
                  <span className="w-2 h-2 bg-primary-500 rounded-full" title="Nouveau statut" />
                )}
                <OrderStatusBadge status={order.statut} />
              </div>
            </div>

            <div className="text-sm text-gray-600 mb-3">
              {order.lignes.slice(0, 2).map((item) => (
                <span key={item.id} className="mr-2">
                  {item.productNom} ×{item.quantite}
                </span>
              ))}
              {order.lignes.length > 2 && <span>+{order.lignes.length - 2} autre(s)</span>}
            </div>

            <div className="flex items-center justify-between">
              <p className="font-bold text-gray-900">{order.totalTTC.toFixed(2)} €</p>
              <Link
                href={`/orders/${order.id}`}
                className="text-sm text-primary-600 hover:underline"
              >
                Voir le détail →
              </Link>
            </div>
          </div>
        ))
      )}
    </div>
  );
}
