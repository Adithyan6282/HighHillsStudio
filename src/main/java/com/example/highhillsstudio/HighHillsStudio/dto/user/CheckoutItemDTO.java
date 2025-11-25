package com.example.highhillsstudio.HighHillsStudio.dto.user;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutItemDTO {


    private Long productId;
    private String productName;
    private String imageUrl;
    private int quantity;
    private BigDecimal basePrice;
    private int discountPercentage;

    // ✅ Add these two fields for frontend comparison display
//    private BigDecimal productOffer;   // offer applied directly on product
//    private BigDecimal categoryOffer;  // offer applied via product's category

    // ✅ Which offer was applied (Product Offer / Category Offer)
    private String appliedOfferType;

    // Best discount chosen between product & category
    private BigDecimal bestOfferDiscount;

    // Total discount for 1 unit = flatDiscount + bestOfferDiscount
    private BigDecimal totalDiscount;

    // Final price per unit after all discounts
    private BigDecimal finalPrice;

    // Final total price for (finalPrice * quantity)
    private BigDecimal totalPrice;










}

