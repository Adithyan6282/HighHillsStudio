package com.example.highhillsstudio.HighHillsStudio.dto.user;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutSummaryDTO {

    private List<CheckoutItemDTO> items;
    private BigDecimal subtotal;
    private BigDecimal totalDiscount; // test
    private BigDecimal shipping;
    private BigDecimal couponDiscount;
    private BigDecimal finalTotal;

}
