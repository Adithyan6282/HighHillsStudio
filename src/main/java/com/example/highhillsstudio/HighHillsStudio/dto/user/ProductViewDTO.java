package com.example.highhillsstudio.HighHillsStudio.dto.user;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductViewDTO {
    private Long id;
    private String name;
    private BigDecimal finalPrice;
    private String mainImage;
    private boolean active; // new field
//    private boolean inStock;


}
