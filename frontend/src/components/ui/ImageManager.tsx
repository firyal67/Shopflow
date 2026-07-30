"use client";

import { useState } from "react";
import Image from "next/image";
import { Plus, Trash2, ImageIcon, Link, ArrowLeft, ArrowRight } from "lucide-react";

interface ImageManagerProps {
  images: string[];
  onChange: (images: string[]) => void;
  maxImages?: number;
}

export default function ImageManager({ images, onChange, maxImages = 5 }: ImageManagerProps) {
  const [urlInput, setUrlInput] = useState("");
  const [error, setError] = useState("");
  const [previewError, setPreviewError] = useState<Record<number, boolean>>({});

  const addImage = () => {
    setError("");
    const url = urlInput.trim();
    if (!url) return;

    try {
      new URL(url);
    } catch {
      setError("URL invalide. Exemple : https://images.unsplash.com/photo-xxx");
      return;
    }

    if (images.includes(url)) {
      setError("Cette image est déjà ajoutée.");
      return;
    }

    if (images.length >= maxImages) {
      setError(`Maximum ${maxImages} images autorisées.`);
      return;
    }

    onChange([...images, url]);
    setUrlInput("");
  };

  const removeImage = (index: number) => {
    onChange(images.filter((_, i) => i !== index));
    setPreviewError(prev => {
      const next = { ...prev };
      delete next[index];
      return next;
    });
  };

  const moveImage = (from: number, to: number) => {
    const newImages = [...images];
    const [moved] = newImages.splice(from, 1);
    newImages.splice(to, 0, moved);
    onChange(newImages);
  };

  return (
    <div className="space-y-4">
      {/* Grille d'aperçu */}
      {images.length > 0 && (
        <div className="grid grid-cols-3 sm:grid-cols-4 gap-3">
          {images.map((url, i) => (
            <div key={i} className="relative group aspect-square rounded-xl overflow-hidden border border-zinc-700/50 bg-zinc-800/50">
              {!previewError[i] ? (
                <Image
                  src={url}
                  alt={`Image ${i + 1}`}
                  fill
                  className="object-cover"
                  onError={() => setPreviewError(prev => ({ ...prev, [i]: true }))}
                  unoptimized
                />
              ) : (
                <div className="w-full h-full flex flex-col items-center justify-center text-zinc-500 gap-1">
                  <ImageIcon size={24} />
                  <span className="text-xs">Erreur</span>
                </div>
              )}

              {/* Badge principale */}
              {i === 0 && (
                <span className="absolute top-1.5 left-1.5 bg-amber-500 text-white text-[10px] px-1.5 py-0.5 rounded-md font-semibold shadow-lg">
                  Principale
                </span>
              )}

              {/* Actions au hover */}
              <div className="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 transition-opacity rounded-xl flex items-center justify-center gap-1.5">
                {i > 0 && (
                  <button
                    type="button"
                    onClick={() => moveImage(i, i - 1)}
                    className="p-1.5 bg-white/20 backdrop-blur rounded-lg text-white hover:bg-white/30 transition-colors"
                    title="Déplacer à gauche"
                  >
                    <ArrowLeft size={14} />
                  </button>
                )}
                <button
                  type="button"
                  onClick={() => removeImage(i)}
                  className="p-1.5 bg-red-500/80 backdrop-blur rounded-lg text-white hover:bg-red-500 transition-colors"
                  title="Supprimer"
                >
                  <Trash2 size={14} />
                </button>
                {i < images.length - 1 && (
                  <button
                    type="button"
                    onClick={() => moveImage(i, i + 1)}
                    className="p-1.5 bg-white/20 backdrop-blur rounded-lg text-white hover:bg-white/30 transition-colors"
                    title="Déplacer à droite"
                  >
                    <ArrowRight size={14} />
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Ajouter une image par URL */}
      {images.length < maxImages && (
        <div className="space-y-2">
          <div className="flex gap-2">
            <div className="relative flex-1">
              <Link size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-500" />
              <input
                type="url"
                value={urlInput}
                onChange={e => { setUrlInput(e.target.value); setError(""); }}
                onKeyDown={e => e.key === "Enter" && (e.preventDefault(), addImage())}
                placeholder="https://images.unsplash.com/photo-xxx"
                className="w-full bg-zinc-800/50 border border-zinc-700/50 rounded-xl pl-9 pr-4 py-2.5 text-sm text-zinc-100 placeholder-zinc-500 focus:outline-none focus:ring-2 focus:ring-amber-500/50 focus:border-amber-500/50 transition-all"
              />
            </div>
            <button
              type="button"
              onClick={addImage}
              className="flex items-center gap-1.5 px-4 py-2.5 bg-amber-500 text-white text-sm font-semibold rounded-xl hover:bg-amber-600 transition-all active:scale-[0.98]"
            >
              <Plus size={14} /> Ajouter
            </button>
          </div>
          {error && <p className="text-red-400 text-xs">{error}</p>}
          <p className="text-xs text-zinc-500">
            Collez une URL d'image (clic droit → "Copier l'adresse de l'image").
            {images.length > 0 && ` ${images.length}/${maxImages}`}
          </p>
        </div>
      )}

      {images.length === 0 && (
        <div className="border-2 border-dashed border-zinc-700/50 rounded-xl p-8 text-center">
          <ImageIcon size={36} className="text-zinc-600 mx-auto mb-3" />
          <p className="text-sm text-zinc-500">Aucune image ajoutée</p>
          <p className="text-xs text-zinc-600 mt-1">Collez une URL d'image ci-dessus</p>
        </div>
      )}
    </div>
  );
}
