package com.shopflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CartResponse {
    private Long id;
    private List<CartItemResponse> lignes;
    private BigDecimal sousTotal;
    private BigDecimal fraisLivraison;
    private BigDecimal remiseCoupon;
    private BigDecimal totalTTC;
    private String couponCode;
    private LocalDateTime dateModification;
}
