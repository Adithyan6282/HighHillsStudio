package com.example.highhillsstudio.HighHillsStudio.dto.admin;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String code;

    @NotNull(message = "Discount amount is required")
    @DecimalMin(value = "0.01", message = "Discount must be greater than 0")
    private BigDecimal discountAmount;

    private LocalDateTime expiryDate;



}
