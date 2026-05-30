"use client";

import { Star } from "lucide-react";

interface RatingStarsProps {
  rating: number;
  max?: number;
  size?: number;
  interactive?: boolean;
  onChange?: (rating: number) => void;
}

export default function RatingStars({
  rating,
  max = 5,
  size = 16,
  interactive = false,
  onChange,
}: RatingStarsProps) {
  return (
    <div className="flex items-center gap-0.5">
      {Array.from({ length: max }, (_, i) => i + 1).map((star) => (
        <Star
          key={star}
          size={size}
          className={`${
            star <= Math.round(rating)
              ? "fill-yellow-400 text-yellow-400"
              : "text-gray-300"
          } ${interactive ? "cursor-pointer hover:text-yellow-400 transition-colors" : ""}`}
          onClick={() => interactive && onChange?.(star)}
        />
      ))}
    </div>
  );
}
