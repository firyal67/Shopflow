"use client";

import { useState } from "react";
import Image from "next/image";
import { Plus, Trash2, ImageIcon, Link } from "lucide-react";

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

    // Valider que c'est une URL
    try {
      new URL(url);
    } catch {
      setError("URL invalide. Exemple : https://exemple.com/image.jpg");
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
    <div className="space-y-3">
      {/* Grille d'aperçu */}
      {images.length > 0 && (
        <div className="grid grid-cols-3 sm:grid-cols-4 gap-2">
          {images.map((url, i) => (
            <div key={i} className="relative group aspect-square rounded-lg overflow-hidden border-2 border-gray-200 bg-gray-50">
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
                <div className="w-full h-full flex flex-col items-center justify-center text-gray-300 gap-1">
                  <ImageIcon size={20} />
                  <span className="text-xs">Erreur</span>
                </div>
              )}

              {/* Badge principale */}
              {i === 0 && (
                <span className="absolute top-1 left-1 bg-blue-600 text-white text-xs px-1.5 py-0.5 rounded font-medium">
                  Principale
                </span>
              )}

              {/* Actions au hover */}
              <div className="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2">
                {i > 0 && (
                  <button
                    type="button"
                    onClick={() => moveImage(i, i - 1)}
                    className="p-1.5 bg-white/90 rounded-lg text-gray-700 hover:bg-white text-xs font-bold"
                    title="Déplacer à gauche"
                  >
                    ←
                  </button>
                )}
                <button
                  type="button"
                  onClick={() => removeImage(i)}
                  className="p-1.5 bg-red-500 rounded-lg text-white hover:bg-red-600"
                  title="Supprimer"
                >
                  <Trash2 size={13} />
                </button>
                {i < images.length - 1 && (
                  <button
                    type="button"
                    onClick={() => moveImage(i, i + 1)}
                    className="p-1.5 bg-white/90 rounded-lg text-gray-700 hover:bg-white text-xs font-bold"
                    title="Déplacer à droite"
                  >
                    →
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
              <Link size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
              <input
                type="url"
                value={urlInput}
                onChange={e => { setUrlInput(e.target.value); setError(""); }}
                onKeyDown={e => e.key === "Enter" && (e.preventDefault(), addImage())}
                placeholder="https://exemple.com/image.jpg"
                className="w-full border border-gray-300 rounded-lg pl-9 pr-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <button
              type="button"
              onClick={addImage}
              className="flex items-center gap-1.5 px-3 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700"
            >
              <Plus size={14} /> Ajouter
            </button>
          </div>
          {error && <p className="text-red-500 text-xs">{error}</p>}
          <p className="text-xs text-gray-400">
            Collez l'URL d'une image depuis internet (ex: depuis Google Images → clic droit → "Copier l'adresse de l'image").
            {images.length > 0 && ` ${images.length}/${maxImages} image(s) ajoutée(s).`}
          </p>
        </div>
      )}

      {images.length === 0 && (
        <div className="border-2 border-dashed border-gray-200 rounded-xl p-6 text-center">
          <ImageIcon size={32} className="text-gray-300 mx-auto mb-2" />
          <p className="text-sm text-gray-500">Aucune image ajoutée</p>
          <p className="text-xs text-gray-400 mt-1">Collez une URL d'image ci-dessus</p>
        </div>
      )}
    </div>
  );
}
