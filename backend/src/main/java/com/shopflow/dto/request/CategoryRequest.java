package com.shopflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Nom obligatoire")
    private String nom;
    private String description;
    private Long parentId;
}
