#!/bin/bash
# Build script pour Cloudflare Pages
# Déplace le cache webpack hors du dossier de sortie

npm install --legacy-peer-deps
NEXT_TELEMETRY_DISABLED=1 npm run build

# Supprimer le cache webpack qui dépasse 25MB
rm -rf .next/cache
