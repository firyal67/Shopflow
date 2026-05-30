import api from "@/lib/axios";
import { Order, PageResponse } from "@/types";

const orderService = {
  async placeOrder(addressId: number): Promise<Order> {
    const { data } = await api.post("/api/orders", { addressId });
    return data;
  },

  async getMyOrders(page = 0, size = 10): Promise<PageResponse<Order>> {
    const { data } = await api.get(`/api/orders/my?page=${page}&size=${size}`);
    return data;
  },

  async getOrderById(id: number): Promise<Order> {
    const { data } = await api.get(`/api/orders/${id}`);
    return data;
  },

  async cancelOrder(id: number): Promise<Order> {
    const { data } = await api.put(`/api/orders/${id}/cancel`);
    return data;
  },

  async updateStatus(id: number, statut: string): Promise<Order> {
    const { data } = await api.put(`/api/orders/${id}/status`, { statut });
    return data;
  },

  async getAllOrders(page = 0, size = 20): Promise<PageResponse<Order>> {
    const { data } = await api.get(`/api/orders?page=${page}&size=${size}`);
    return data;
  },

  async getSellerOrders(page = 0, size = 20): Promise<PageResponse<Order>> {
    const { data } = await api.get(`/api/orders/seller?page=${page}&size=${size}`);
    return data;
  },
};

export default orderService;
