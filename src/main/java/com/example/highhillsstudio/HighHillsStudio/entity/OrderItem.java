package com.example.highhillsstudio.HighHillsStudio.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    private Long productId;

    private String productName;

    private BigDecimal price;

    private int quantity;

    // link to the product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // link to specific Fit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fit_id")
    private Fit fit;

    // Track indivindual item status (PLACED, CANCElED)
    private OrderStatus status = OrderStatus.PLACED;

    // Back reference to the parent order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private UserOrder order;


    // 🔹 NEW FIELD: To store which offer was applied (Product Offer / Category Offer)
    @Column(name = "offer_type")
    private String offerType;






}
