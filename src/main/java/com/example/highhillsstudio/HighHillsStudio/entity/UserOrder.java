package com.example.highhillsstudio.HighHillsStudio.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "user_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private BigDecimal totalAmount;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon; // which coupon was applied (if any)

    @Column(precision = 10, scale = 2)
    private BigDecimal couponDiscount = BigDecimal.ZERO; // total discount from coupon

    @Column(precision = 10, scale = 2)
    private BigDecimal offerDiscount = BigDecimal.ZERO; // total discount from product/category offers

    @Column(precision = 10, scale = 2)
    private BigDecimal finalAmount; // totalAmount - all discounts







    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;  //   ENUM based status  "PLACED", "SHIPPED", "DELIVERED", "CANCELED"

    @CreationTimestamp
    private LocalDateTime placedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private UserAddress shippingAddress;

    @Column(nullable = false, unique = true)
    private String orderCode;

    @Column(length = 500)
    private String returnReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();




}
