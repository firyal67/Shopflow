import { OrderStatus } from "@/types";
import clsx from "clsx";

const STATUS_CONFIG: Record<OrderStatus, { label: string; className: string }> = {
  PENDING: { label: "En attente", className: "bg-yellow-100 text-yellow-800" },
  PAID: { label: "Payée", className: "bg-blue-100 text-blue-800" },
  PROCESSING: { label: "En traitement", className: "bg-purple-100 text-purple-800" },
  SHIPPED: { label: "Expédiée", className: "bg-indigo-100 text-indigo-800" },
  DELIVERED: { label: "Livrée", className: "bg-green-100 text-green-800" },
  CANCELLED: { label: "Annulée", className: "bg-red-100 text-red-800" },
  REFUNDED: { label: "Remboursée", className: "bg-gray-100 text-gray-800" },
};

export default function OrderStatusBadge({ status }: { status: OrderStatus }) {
  const config = STATUS_CONFIG[status];
  return (
    <span className={clsx("px-2.5 py-0.5 rounded-full text-xs font-medium", config.className)}>
      {config.label}
    </span>
  );
}
