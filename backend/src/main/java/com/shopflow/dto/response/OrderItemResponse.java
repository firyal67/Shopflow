package com.shopflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productNom;
    private String productImage;
    private Long variantId;
    private String variantAttribut;
    private String variantValeur;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal sousTotal;
}
