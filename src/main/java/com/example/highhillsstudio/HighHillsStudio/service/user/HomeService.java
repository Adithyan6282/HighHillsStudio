package com.example.highhillsstudio.HighHillsStudio.service.user;

import com.example.highhillsstudio.HighHillsStudio.entity.Category;
import com.example.highhillsstudio.HighHillsStudio.entity.Product;
import com.example.highhillsstudio.HighHillsStudio.repository.CategoryRepository;
import com.example.highhillsstudio.HighHillsStudio.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class HomeService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // Fetch all active categories
    public List<Category> getAllCategories() {
        return categoryRepository.findByIsActiveTrue(PageRequest.of(0, 20)).getContent();
    }

    // Fetch latest N active products

    public List<Product> getLatestProducts(int count) {
        return productRepository.findAllByIsActiveTrue(PageRequest.of(0, count))
                .getContent();
    }
}
