export interface Product {
  id: number;
  sellerId: number;
  sellerNomBoutique?: string;
  nom: string;
  description?: string;
  prix: number;
  prixPromo?: number;
  enPromotion: boolean;
  pourcentageRemise: number;
  stock: number;
  actif: boolean;
  dateCreation: string;
  images: string[];
  categories: Category[];
  variants: ProductVariant[];
  noteMoyenne: number;
  totalVentes: number;
}

export interface ProductVariant {
  id: number;
  attribut: string;
  valeur: string;
  stockSupplementaire: number;
  prixDelta: number;
}

export interface Category {
  id: number;
  nom: string;
  description?: string;
  parentId?: number;
  children?: Category[];
}

export interface Order {
  id: number;
  numeroCommande: string;
  statut: OrderStatus;
  adresseLivraison: string;
  sousTotal: number;
  fraisLivraison: number;
  remiseCoupon: number;
  totalTTC: number;
  dateCommande: string;
  lignes: OrderItem[];
  isNew: boolean;
  couponCode?: string;
}

export interface OrderItem {
  id: number;
  productId: number;
  productNom: string;
  productImage?: string;
  variantId?: number;
  variantAttribut?: string;
  variantValeur?: string;
  quantite: number;
  prixUnitaire: number;
  sousTotal: number;
}

export type OrderStatus =
  | "PENDING"
  | "PAID"
  | "PROCESSING"
  | "SHIPPED"
  | "DELIVERED"
  | "CANCELLED"
  | "REFUNDED";

export interface Review {
  id: number;
  customerId: number;
  customerNom: string;
  productId: number;
  note: number;
  commentaire?: string;
  dateCreation: string;
  approuve: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
