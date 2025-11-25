package com.example.highhillsstudio.HighHillsStudio.mapper;
import com.example.highhillsstudio.HighHillsStudio.dto.admin.FitDTO;
import com.example.highhillsstudio.HighHillsStudio.dto.admin.ProductImageDTO;


import com.example.highhillsstudio.HighHillsStudio.dto.admin.ProductDTO;
import com.example.highhillsstudio.HighHillsStudio.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductMapper {

    public static ProductDTO toDTO(Product product) {
        if (product == null) return null;

        // Map fits
        List<FitDTO> fitDTOs = Optional.ofNullable(product.getFits())
                .orElse(List.of())
                .stream()
                .filter(fit -> fit != null)
                .map(fit -> new FitDTO(
                        fit.getId(),
                        fit.getSize() != null ? fit.getSize() : "",
                        fit.getStock() != null ? fit.getStock().getQuantity() : 0
                ))
                .collect(Collectors.toList());

        // Map images
        List<ProductImageDTO> imageDTOs = Optional.ofNullable(product.getImages())
                .orElse(List.of())
                .stream()
                .filter(img -> img != null)
                .map(img -> new ProductImageDTO(
                        img.getId(),
                        img.getImg() != null ? img.getImg() : "",
                        img.getIsMain() != null ? img.getIsMain() : false
                ))
                .collect(Collectors.toList());

        // Build ProductDTO
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName() != null ? product.getName() : "")
                .description(product.getDescription() != null ? product.getDescription() : "")
                .basePrice(product.getBasePrice() != null ? product.getBasePrice() : BigDecimal.ZERO)
                .finalPrice(product.getFinalPrice() != null ? product.getFinalPrice() : BigDecimal.ZERO)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null && product.getCategory().getName() != null
                        ? product.getCategory().getName() : "")
                .productTypeId(product.getProductType() != null ? product.getProductType().getId() : null)
                .productTypeName(product.getProductType() != null && product.getProductType().getName() != null
                        ? product.getProductType().getName() : "")
                .colorId(product.getColor() != null ? product.getColor().getId() : null)
                .colorName(product.getColor() != null && product.getColor().getName() != null
                        ? product.getColor().getName() : "")
                .collectionId(product.getCollection() != null ? product.getCollection().getId() : null)
                .collectionName(product.getCollection() != null && product.getCollection().getName() != null
                        ? product.getCollection().getName() : "")
                .productGenderId(product.getProductGender() != null ? product.getProductGender().getId() : null)
                .productGenderName(product.getProductGender() != null && product.getProductGender().getName() != null
                        ? product.getProductGender().getName() : "")
                .isActive(product.getIsActive() != null ? product.getIsActive() : false)
                .fits(fitDTOs)
                .images(imageDTOs)
                .build();
    }
}




