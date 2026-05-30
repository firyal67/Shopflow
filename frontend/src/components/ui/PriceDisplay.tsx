interface PriceDisplayProps {
  prix: number;
  prixPromo?: number | null;
  enPromotion?: boolean;
  pourcentageRemise?: number;
  size?: "sm" | "md" | "lg";
  className?: string;
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
    sm: { main: "text-sm font-semibold", old: "text-xs", badge: "text-xs px-1.5 py-0.5" },
    md: { main: "text-lg font-bold", old: "text-sm", badge: "text-xs px-2 py-0.5" },
    lg: { main: "text-3xl font-bold", old: "text-xl", badge: "text-sm px-2 py-1" },
  };

  const classes = sizeClasses[size];

  if (enPromotion && prixPromo) {
    return (
      <div className={`flex items-center gap-2 flex-wrap ${className}`}>
        <span className={`${classes.main} text-red-600`}>
          {Number(prixPromo).toFixed(2)} €
        </span>
        <span className={`${classes.old} text-gray-400 line-through`}>
          {Number(prix).toFixed(2)} €
        </span>
        {pourcentageRemise && pourcentageRemise > 0 && (
          <span className={`${classes.badge} bg-red-100 text-red-700 rounded-full font-medium`}>
            -{Math.round(pourcentageRemise)}%
          </span>
        )}
      </div>
    );
  }

  return (
    <span className={`${classes.main} text-gray-900 ${className}`}>
      {Number(prix).toFixed(2)} €
    </span>
  );
}
