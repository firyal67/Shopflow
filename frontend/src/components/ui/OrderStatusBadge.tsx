import { OrderStatus } from "@/types";
import clsx from "clsx";

const STATUS_CONFIG: Record<OrderStatus, { label: string; className: string }> = {
  PENDING: { label: "En attente", className: "bg-yellow-500/10 text-yellow-400" },
  PAID: { label: "Payée", className: "bg-blue-500/10 text-blue-400" },
  PROCESSING: { label: "En traitement", className: "bg-purple-500/10 text-purple-400" },
  SHIPPED: { label: "Expédiée", className: "bg-indigo-500/10 text-indigo-400" },
  DELIVERED: { label: "Livrée", className: "bg-green-500/10 text-green-400" },
  CANCELLED: { label: "Annulée", className: "bg-red-500/10 text-red-400" },
  REFUNDED: { label: "Remboursée", className: "bg-zinc-500/10 text-zinc-400" },
};

export default function OrderStatusBadge({ status }: { status: OrderStatus }) {
  const config = STATUS_CONFIG[status];
  return (
    <span className={clsx("px-2.5 py-0.5 rounded-full text-xs font-medium", config.className)}>
      {config.label}
    </span>
  );
}
