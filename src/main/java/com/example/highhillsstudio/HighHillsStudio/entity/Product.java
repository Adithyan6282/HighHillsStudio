package com.example.highhillsstudio.HighHillsStudio.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.action.internal.OrphanRemovalAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    @NotBlank(message = "Product name is required")
    @Size(max = 100)
    private String name;  // Product name

    @Column(columnDefinition = "TEXT")
    private String description;  // Product description

    @Column(nullable = false)
    private BigDecimal basePrice;  // Product base price

    @Column(nullable = false)
    private Integer discountPercentage;  // Discount on the product (e.g., 10 for 10%)

    @Column(nullable = false)  // test //
    private BigDecimal finalPrice;

    @ManyToOne
    @JoinColumn(name = "product_type_id")
    @JsonIgnore  // Child side of ProductType -> Product
    private ProductType productType;  // Type of the product (e.g., T-shirt, Shoes)

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category; // Optional if you want to store both

    @ManyToOne
    @JoinColumn(name = "product_gender_id")
    private ProductGender productGender;  // Gender for which the product is intended (e.g., Men, Women, Unisex)

    @ManyToOne
    @JoinColumn(name = "collection_id")
    private Collection collection;  // Collection the product belongs to (e.g., Summer Collection)

    @ManyToOne
    @JoinColumn(name = "color_id")
    private Color color;  // Color of the product


    @Column(nullable = false)
    private Boolean isActive = true;  // Indicates if the product is active or not soft delete / active


    @Column(columnDefinition = "TEXT")
    private String highlights; // Product highlights/specs    test


    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();  // List of images associated with the product



    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Fit> fits = new ArrayList<>();


//    @OneToMany(mappedBy = "product",
//            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
//    private List<Fit> fits = new ArrayList<>();







    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();


    @PrePersist
    @PreUpdate
    public void calculateFinalPrice() {
        if (basePrice != null && discountPercentage != null) {
            this.finalPrice = basePrice.subtract(
                    basePrice.multiply(BigDecimal.valueOf(discountPercentage))
                            .divide(BigDecimal.valueOf(100))
            );
        } else {
            this.finalPrice = basePrice;
        }
    }

    // Dynamic availability check based on stock quantity of all fits

    @Transient
    public boolean getAvailability() {
        if(!Boolean.TRUE.equals(this.isActive)) return false;
        return fits.stream()
                .anyMatch(fit -> fit.getStock() != null && fit.getStock().getQuantity() > 0);
    }

    @Transient
    public int getStockBySize(String size) {
        return fits.stream()
                .filter(f -> f.getSize().equalsIgnoreCase(size) && f.getStock() != null)
                .mapToInt(f -> f.getStock().getQuantity())
                .findFirst()
                .orElse(0);
    }



}

