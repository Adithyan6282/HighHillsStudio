package com.example.highhillsstudio.HighHillsStudio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;  // Offer applies to this category

    private String name;

    private BigDecimal discountAmount;

    private boolean percentage;

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean active;

}
