import api from "@/lib/axios";
import { Product, PageResponse } from "@/types";

export interface ProductFilters {
  categoryId?: number;
  prixMin?: number;
  prixMax?: number;
  sellerId?: number;
  promo?: boolean;
  q?: string;
  page?: number;
  size?: number;
  sort?: string;
}

const productService = {
  async getProducts(filters: ProductFilters = {}): Promise<PageResponse<Product>> {
    const params = new URLSearchParams();
    if (filters.categoryId) params.set("categoryId", String(filters.categoryId));
    if (filters.prixMin) params.set("prixMin", String(filters.prixMin));
    if (filters.prixMax) params.set("prixMax", String(filters.prixMax));
    if (filters.sellerId) params.set("sellerId", String(filters.sellerId));
    if (filters.promo) params.set("promo", "true");
    if (filters.page !== undefined) params.set("page", String(filters.page));
    if (filters.size) params.set("size", String(filters.size));

    const endpoint = filters.q
      ? `/api/products/search?q=${encodeURIComponent(filters.q)}&${params}`
      : `/api/products?${params}`;

    const { data } = await api.get(endpoint);
    return data;
  },

  async getProductById(id: number): Promise<Product> {
    const { data } = await api.get(`/api/products/${id}`);
    return data;
  },

  async getTopSelling(): Promise<Product[]> {
    const { data } = await api.get("/api/products/top-selling");
    return data;
  },

  async createProduct(payload: Partial<Product>) {
    const { data } = await api.post("/api/products", payload);
    return data;
  },

  async updateProduct(id: number, payload: Partial<Product>) {
    const { data } = await api.put(`/api/products/${id}`, payload);
    return data;
  },

  async deleteProduct(id: number) {
    await api.delete(`/api/products/${id}`);
  },

  async searchProducts(q: string, page = 0, size = 12): Promise<PageResponse<Product>> {
    const { data } = await api.get(`/api/products/search?q=${encodeURIComponent(q)}&page=${page}&size=${size}`);
    return data;
  },
};

export default productService;
