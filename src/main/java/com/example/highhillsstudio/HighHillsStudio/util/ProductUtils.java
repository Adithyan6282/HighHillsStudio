package com.example.highhillsstudio.HighHillsStudio.util;

import com.example.highhillsstudio.HighHillsStudio.entity.Product;
import com.example.highhillsstudio.HighHillsStudio.entity.ProductImage;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("productUtils") // Expose to Thymeleaf as #productUtils
public class ProductUtils {


    public String getMainImage(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return "/images/default.png"; // fallback
        }

        Optional<ProductImage> mainImage = product.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.isActive()) && Boolean.TRUE.equals(img.getIsMain()))
                .findFirst();

        return mainImage.map(ProductImage::getImg)
                .orElse(product.getImages().get(0).getImg());
    }




}
