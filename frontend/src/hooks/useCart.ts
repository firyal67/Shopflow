import { useCartStore } from "@/store/cartStore";
import cartService from "@/services/cartService";
import { useAuthStore } from "@/store/authStore";

export function useCart() {
  const store = useCartStore();
  const { user } = useAuthStore();

  const syncCart = async () => {
    if (!user || user.role !== "CUSTOMER") return;
    try {
      const data = await cartService.getCart();
      store.setCart({
        items: data.lignes,
        sousTotal: data.sousTotal,
        fraisLivraison: data.fraisLivraison,
        remiseCoupon: data.remiseCoupon,
        totalTTC: data.totalTTC,
        couponCode: data.couponCode,
        itemCount: data.lignes?.reduce((acc: number, i: any) => acc + i.quantite, 0) ?? 0,
      });
    } catch {}
  };

  const addToCart = async (productId: number, quantite = 1, variantId?: number) => {
    const data = await cartService.addItem(productId, quantite, variantId);
    store.setCart({
      items: data.lignes,
      sousTotal: data.sousTotal,
      fraisLivraison: data.fraisLivraison,
      remiseCoupon: data.remiseCoupon,
      totalTTC: data.totalTTC,
      itemCount: data.lignes?.reduce((acc: number, i: any) => acc + i.quantite, 0) ?? 0,
    });
    return data;
  };

  const removeFromCart = async (itemId: number) => {
    const data = await cartService.removeItem(itemId);
    store.setCart({
      items: data.lignes,
      totalTTC: data.totalTTC,
      itemCount: data.lignes?.reduce((acc: number, i: any) => acc + i.quantite, 0) ?? 0,
    });
    return data;
  };

  const updateQuantity = async (itemId: number, quantite: number) => {
    const data = await cartService.updateItem(itemId, quantite);
    store.setCart({
      items: data.lignes,
      totalTTC: data.totalTTC,
      itemCount: data.lignes?.reduce((acc: number, i: any) => acc + i.quantite, 0) ?? 0,
    });
    return data;
  };

  const applyCoupon = async (code: string) => {
    const data = await cartService.applyCoupon(code);
    store.setCart({ items: data.lignes, remiseCoupon: data.remiseCoupon, totalTTC: data.totalTTC, couponCode: data.couponCode });
    return data;
  };

  const removeCoupon = async () => {
    const data = await cartService.removeCoupon();
    store.setCart({ remiseCoupon: 0, couponCode: undefined, totalTTC: data.totalTTC });
    return data;
  };

  return {
    ...store,
    syncCart,
    addToCart,
    removeFromCart,
    updateQuantity,
    applyCoupon,
    removeCoupon,
  };
}
