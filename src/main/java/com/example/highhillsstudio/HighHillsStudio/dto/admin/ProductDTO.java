package com.example.highhillsstudio.HighHillsStudio.dto.admin;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private Integer discountPercentage;
    private BigDecimal finalPrice;

    private Long productTypeId;
    private String productTypeName;

    private Long categoryId;
    private String categoryName;

    private Long productGenderId;
    private String productGenderName;

    private Long collectionId;
    private String collectionName;

    private Long colorId;
    private String colorName;

    private Boolean isActive;

    private List<FitDTO> fits;
    private List<ProductImageDTO> images;
}
