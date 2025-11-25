package com.example.highhillsstudio.HighHillsStudio.service.user;

import com.example.highhillsstudio.HighHillsStudio.entity.Category;
import com.example.highhillsstudio.HighHillsStudio.entity.Product;
import com.example.highhillsstudio.HighHillsStudio.entity.ProductImage;
import com.example.highhillsstudio.HighHillsStudio.repository.CategoryRepository;
import com.example.highhillsstudio.HighHillsStudio.repository.ProductImageRepository;
import com.example.highhillsstudio.HighHillsStudio.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final CategoryRepository categoryRepository;

    // Home page: latest products with images
    public List<Product> getLatestProducts(int count) {
        List<Product> products = productRepository.findAllActiveWithImages();
        return products.stream().limit(count).toList();
    }

    // List all products (active only)
    public List<Product> getAllProducts() {
        return productRepository.findByIsActiveTrue();
    }

    // Paginated product listing
    public Page<Product> getProductsPage(int page, int size, String sort) {

        Pageable pageable = buildPageable(page, size, sort);
        return productRepository.findAllByIsActiveTrue(pageable);

    }

    // Fetch all active categories
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }


    public Page<Product> searchProductsByKeyword(String keyword, int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        return productRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(keyword, pageable);
    }

    public Page<Product> searchProductsByKeywordAndCategory(String keyword, Long categoryId, int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        return productRepository.findByNameContainingIgnoreCaseAndCategoryIdAndIsActiveTrue(keyword, categoryId, pageable);
    }

    // Get single product by ID (with images)
    public Product getProductById(Long id) {
        return productRepository.findByIdWithImages(id);
    }

    // Get product images
    public List<ProductImage> getProductImage(Product product) {
        return imageRepository.findByProductAndIsActiveTrue(product);
    }


    // Get main image
    public ProductImage getMainImage(Product product) {
        return imageRepository.findByProductAndIsMainTrue(product)
                .orElse(null);
    }

    public Page<Product> getProductsByCategory(Long categoryId, int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        return productRepository.findAllByCategoryIdAndIsActiveTrue(categoryId, pageable);
    }

    // Fetch all products (active + inactive) for listing
    public Page<Product> getProductsPageAll(int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        return productRepository.findAll(pageable); // no IsActive filter
    }
    public List<Product> getRelatedProducts(Long categoryId, Long excludeProductId, int limit) {
        Pageable pageable = PageRequest.of(0, limit); // first page, limit items
        return productRepository.findByCategoryIdAndIdNotAndIsActiveTrue(categoryId, excludeProductId, pageable);
    }


    public Page<Product> getProductsByCategoryAll(Long categoryId, int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        return productRepository.findAllByCategoryId(categoryId, pageable);
    }

    //  Helper method for dynamic sorting
    private Pageable buildPageable(int page, int size, String sort) {
        Sort sortOption;

        if ("priceLowHigh".equalsIgnoreCase(sort)) {
            sortOption = Sort.by("finalPrice").ascending();
        } else if ("priceHighLow".equalsIgnoreCase(sort)) {
            sortOption = Sort.by("finalPrice").descending();
        } else if ("nameAZ".equalsIgnoreCase(sort)) {
            sortOption = Sort.by("name").ascending();
        } else if ("nameZA".equalsIgnoreCase(sort)) {
            sortOption = Sort.by("name").descending();
        } else {
            sortOption = Sort.unsorted(); // default
        }

        return PageRequest.of(page, size, sortOption);
    }





    // Search + Filter + Pagination + Sorting (includes inactive products)
    public Page<Product> searchAndFilterProductsIncludeInactive(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort);

        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        boolean hasCategory = categoryId != null;
        boolean hasPriceRange = minPrice != null && maxPrice != null;

        if (hasKeyword && hasCategory && hasPriceRange) {
            return productRepository.findByNameContainingIgnoreCaseAndCategoryIdAndFinalPriceBetween(
                    keyword, categoryId, minPrice, maxPrice, pageable
            );
        } else if (hasKeyword && hasCategory) {
            return productRepository.findByNameContainingIgnoreCaseAndCategoryId(
                    keyword, categoryId, pageable
            );
        } else if (hasKeyword && hasPriceRange) {
            return productRepository.findByNameContainingIgnoreCaseAndFinalPriceBetween(
                    keyword, minPrice, maxPrice, pageable
            );
        } else if (hasCategory && hasPriceRange) {
            return productRepository.findAllByCategoryIdAndFinalPriceBetween(
                    categoryId, minPrice, maxPrice, pageable
            );
        } else if (hasKeyword) {
            return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else if (hasCategory) {
            return productRepository.findAllByCategoryId(categoryId, pageable);
        } else if (hasPriceRange) {
            return productRepository.findAllByFinalPriceBetween(minPrice, maxPrice, pageable);
        } else {
            return productRepository.findAll(pageable);
        }
    }


}
