import api from "@/lib/axios";

const cartService = {
  async getCart() {
    const { data } = await api.get("/api/cart");
    return data;
  },

  async addItem(productId: number, quantite = 1, variantId?: number) {
    const { data } = await api.post("/api/cart/items", { productId, quantite, variantId });
    return data;
  },

  async updateItem(itemId: number, quantite: number) {
    const { data } = await api.put(`/api/cart/items/${itemId}`, { quantite });
    return data;
  },

  async removeItem(itemId: number) {
    const { data } = await api.delete(`/api/cart/items/${itemId}`);
    return data;
  },

  async applyCoupon(code: string) {
    const { data } = await api.post("/api/cart/coupon", { code });
    return data;
  },

  async removeCoupon() {
    const { data } = await api.delete("/api/cart/coupon");
    return data;
  },
};

export default cartService;
