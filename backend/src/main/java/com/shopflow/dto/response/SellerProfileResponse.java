package com.shopflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerProfileResponse {
    private Long id;
    private String nomBoutique;
    private String description;
    private String logo;
    private Double note;
}
