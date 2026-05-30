import api from "@/lib/axios";
import { Review, PageResponse } from "@/types";

const reviewService = {
  async createReview(productId: number, note: number, commentaire?: string): Promise<Review> {
    const { data } = await api.post("/api/reviews", { productId, note, commentaire });
    return data;
  },

  async getProductReviews(productId: number, page = 0, size = 10): Promise<PageResponse<Review>> {
    const { data } = await api.get(`/api/reviews/product/${productId}?page=${page}&size=${size}`);
    return data;
  },

  async approveReview(id: number): Promise<Review> {
    const { data } = await api.put(`/api/reviews/${id}/approve`);
    return data;
  },

  async getPendingReviews(page = 0, size = 20): Promise<PageResponse<Review>> {
    const { data } = await api.get(`/api/reviews/pending?page=${page}&size=${size}`);
    return data;
  },
};

export default reviewService;
