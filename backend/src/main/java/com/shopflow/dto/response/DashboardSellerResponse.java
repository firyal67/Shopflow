package com.shopflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardSellerResponse {
    private BigDecimal revenus;
    private Long totalCommandes;
    private Long commandesEnAttente;
    private List<OrderResponse> commandesRecentes;
    private List<ProductResponse> alertesStockFaible;
}
