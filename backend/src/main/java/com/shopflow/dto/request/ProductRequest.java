package com.shopflow.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class ProductRequest {

    @NotBlank(message = "Nom obligatoire")
    private String nom;

    private String description;

    @NotNull(message = "Prix obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être positif")
    private BigDecimal prix;

    private BigDecimal prixPromo;

    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer stock = 0;

    private Set<Long> categoryIds;

    private List<String> images = new ArrayList<>();

    private List<VariantRequest> variants = new ArrayList<>();
}
