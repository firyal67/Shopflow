package com.shopflow.dto.response;

import com.shopflow.entity.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String numeroCommande;
    private OrderStatus statut;
    private String adresseLivraison;
    private BigDecimal sousTotal;
    private BigDecimal fraisLivraison;
    private BigDecimal remiseCoupon;
    private BigDecimal totalTTC;
    private LocalDateTime dateCommande;
    private List<OrderItemResponse> lignes;
    private boolean isNew;
    private String couponCode;
}
