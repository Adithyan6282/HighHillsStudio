package com.example.highhillsstudio.HighHillsStudio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;  // offer applies to this product

    private String name;  // Offer name

    private BigDecimal discountAmount; // discount value

    private boolean percentage; // true if discount is percentage

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean active; // Offer active or inactive


}
