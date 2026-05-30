import { create } from "zustand";

interface CartItem {
  id: number;
  productId: number;
  productNom: string;
  productImage?: string;
  prixUnitaire: number;
  variantId?: number;
  variantAttribut?: string;
  variantValeur?: string;
  quantite: number;
  sousTotal: number;
  stockDisponible: number;
}

interface CartState {
  items: CartItem[];
  sousTotal: number;
  fraisLivraison: number;
  remiseCoupon: number;
  totalTTC: number;
  couponCode?: string;
  itemCount: number;
  setCart: (cart: Partial<CartState>) => void;
  clearCart: () => void;
}

export const useCartStore = create<CartState>((set) => ({
  items: [],
  sousTotal: 0,
  fraisLivraison: 0,
  remiseCoupon: 0,
  totalTTC: 0,
  couponCode: undefined,
  itemCount: 0,
  setCart: (cart) =>
    set((state) => ({
      ...state,
      ...cart,
      itemCount: cart.items?.reduce((acc, i) => acc + i.quantite, 0) ?? state.itemCount,
    })),
  clearCart: () =>
    set({ items: [], sousTotal: 0, fraisLivraison: 0, remiseCoupon: 0, totalTTC: 0, itemCount: 0 }),
}));
