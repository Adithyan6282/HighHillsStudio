package com.example.highhillsstudio.HighHillsStudio.dto.user;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {
    private Long id;
    private String productName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private boolean cancellable;
    private String imageUrl;

}
