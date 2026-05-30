package com.shopflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private Long customerId;
    private String customerNom;
    private Long productId;
    private Integer note;
    private String commentaire;
    private LocalDateTime dateCreation;
    private boolean approuve;
}
