package com.shopflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VariantRequest {
    @NotBlank
    private String attribut;
    @NotBlank
    private String valeur;
    private Integer stockSupplementaire = 0;
    private BigDecimal prixDelta = BigDecimal.ZERO;
}
