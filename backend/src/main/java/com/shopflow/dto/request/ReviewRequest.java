package com.shopflow.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull
    private Long productId;

    @Min(1) @Max(5)
    @NotNull(message = "Note obligatoire (1-5)")
    private Integer note;

    private String commentaire;
}
