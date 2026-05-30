package com.shopflow.dto.request;

import com.shopflow.entity.CouponType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponRequest {
    @NotBlank
    private String code;

    @NotNull
    private CouponType type;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal valeur;

    private LocalDateTime dateExpiration;
    private Integer usagesMax;
}
