interface PriceDisplayProps {
  prix: number;
  prixPromo?: number | null;
  enPromotion?: boolean;
  pourcentageRemise?: number;
  size?: "sm" | "md" | "lg";
  className?: string;
}

function formatTND(amount: number): string {
  return new Intl.NumberFormat("fr-TN", {
    style: "currency",
    currency: "TND",
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount);
}

export default function PriceDisplay({
  prix,
  prixPromo,
  enPromotion,
  pourcentageRemise,
  size = "md",
  className = "",
}: PriceDisplayProps) {
  const sizeClasses = {
    sm: { main: "text-sm font-bold", old: "text-xs", badge: "text-[10px] px-1.5 py-0.5" },
    md: { main: "text-lg font-bold", old: "text-sm", badge: "text-xs px-2 py-0.5" },
    lg: { main: "text-3xl font-bold", old: "text-xl", badge: "text-sm px-2.5 py-1" },
  };

  const classes = sizeClasses[size];

  if (enPromotion && prixPromo) {
    return (
      <div className={`flex items-center gap-2 flex-wrap ${className}`}>
        <span className={`${classes.main} text-amber-400`}>
          {formatTND(prixPromo)}
        </span>
          <span className={`${classes.old} text-zinc-600 line-through`}>
            {formatTND(prix)}
          </span>
          {pourcentageRemise && pourcentageRemise > 0 && (
            <span className={`${classes.badge} badge-red`}>
              -{Math.round(pourcentageRemise)}%
            </span>
          )}
      </div>
    );
  }

  return (
    <span className={`${classes.main} text-zinc-100 ${className}`}>
      {formatTND(prix)}
    </span>
  );
}
