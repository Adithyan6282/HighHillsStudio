package com.example.highhillsstudio.HighHillsStudio.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDTO {

    private Long cartId;   // Cart Id
    private List<CartItemDTO> items;   //  List of cart items
    private BigDecimal totalAmount;  // total amt of the cart
}
