package com.shopflow.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemRequest {
    @NotNull(message = "Product ID obligatoire")
    private Long productId;
    private Long variantId;
    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantite = 1;
}
