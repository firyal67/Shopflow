package com.shopflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private Long sellerId;
    private String sellerNomBoutique;
    private String nom;
    private String description;
    private BigDecimal prix;
    private BigDecimal prixPromo;
    private boolean enPromotion;
    private Double pourcentageRemise;
    private Integer stock;
    private boolean actif;
    private LocalDateTime dateCreation;
    private List<String> images;
    private Set<CategoryResponse> categories;
    private List<VariantResponse> variants;
    private Double noteMoyenne;
    private Integer totalVentes;
    private Long totalAvis;
}
