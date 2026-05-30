import clsx from "clsx";

export function Skeleton({ className }: { className?: string }) {
  return (
    <div className={clsx("animate-pulse bg-gray-200 rounded", className)} />
  );
}

export function ProductCardSkeleton() {
  return (
    <div className="card space-y-3">
      <Skeleton className="aspect-square w-full rounded-lg" />
      <Skeleton className="h-3 w-1/3" />
      <Skeleton className="h-4 w-full" />
      <Skeleton className="h-4 w-2/3" />
      <div className="flex justify-between">
        <Skeleton className="h-5 w-16" />
        <Skeleton className="h-8 w-8 rounded-lg" />
      </div>
    </div>
  );
}
