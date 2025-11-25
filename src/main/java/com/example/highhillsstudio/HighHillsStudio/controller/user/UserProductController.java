package com.example.highhillsstudio.HighHillsStudio.controller.user;

import com.example.highhillsstudio.HighHillsStudio.dto.user.ProductViewDTO;
import com.example.highhillsstudio.HighHillsStudio.entity.*;
import com.example.highhillsstudio.HighHillsStudio.service.user.ReviewService;
import com.example.highhillsstudio.HighHillsStudio.service.user.UserProductService;
import com.example.highhillsstudio.HighHillsStudio.util.ProductUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class UserProductController {

    private final UserProductService productService;
    private final ProductUtils productUtils;
    private final ReviewService reviewService;


    // Main page load (All products OR filter by category)  // test
    @GetMapping({"", "/list"})
    public String listProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String sort,
            Model model
    ) {
        Page<Product> productPage = (categoryId != null)
                ? productService.getProductsByCategoryAll(categoryId, page, size, sort)
                : productService.getProductsPageAll(page, size, sort);

        List<ProductViewDTO> products = productPage.getContent().stream()
                .map(p -> new ProductViewDTO(
                        p.getId(),
                        p.getName(),
                        p.getFinalPrice(),
                        productUtils.getMainImage(p) != null ? productUtils.getMainImage(p) : "/images/no-image.png",
                        Boolean.TRUE.equals(p.getIsActive())  // Active badge
                ))
                .collect(Collectors.toList());

        // Only main categories for home page links
        List<Category> mainCategories = productService.getAllCategories().stream()
                .filter(c -> List.of("Men", "Women", "Kids", "Accessories").contains(c.getName()))
                .toList();

        model.addAttribute("categories", mainCategories);
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("selectedCategoryId", categoryId);

        return "user/product-list";
    }




    // API endpoint for JS fetch
    @GetMapping("/fetch")
    @ResponseBody
    public ProductResponse fetchProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sort
    ) {
        Page<Product> productPage;

        if ((keyword != null && !keyword.isEmpty())
                || categoryId != null
                || (minPrice != null && maxPrice != null)) {
            productPage = productService.searchAndFilterProductsIncludeInactive(keyword, categoryId, minPrice, maxPrice, page, size, sort);
        } else {
            productPage = productService.getProductsPageAll(page, size, sort);
        }

        List<ProductViewDTO> products = productPage.getContent().stream()
                .map(p -> new ProductViewDTO(
                        p.getId(),
                        p.getName(),
                        p.getFinalPrice(),
                        productUtils.getMainImage(p) != null ? productUtils.getMainImage(p) : "/images/no-image.png",
                        Boolean.TRUE.equals(p.getIsActive())
                ))
                .collect(Collectors.toList());

        return new ProductResponse(products, productPage.getNumber(), productPage.getTotalPages());
    }



    // Product details
    @GetMapping("/{id}")
    public String productDetails(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);

        // check if product exists
        if (product == null) {
            return "redirect:/products"; // redirect to product list if ID is Invalid
        }

        // check if product or category is inactive

        boolean isInactive = !Boolean.TRUE.equals(product.getIsActive())
                || (product.getCategory() != null && !Boolean.TRUE.equals(product.getCategory().isActive()));

//        if(!Boolean.TRUE.equals(product.getIsActive())) {
//            return "redirect:/products"; // redirect to product list if product is blocked/unavailable
//        }


        if (isInactive) {
            // Optionally redirect or show disabled button
            model.addAttribute("isInactive", true);
        } else {
            model.addAttribute("isInactive", false);
        }

        // Normal case - load product details
        List<ProductImage> images = productService.getProductImage(product);
        List<Review> reviews = reviewService.getReviewsByProduct(product);

        model.addAttribute("product", product);
        model.addAttribute("images", images);
        model.addAttribute("reviews", reviews);

        // for average rating (ignore null ratings)
        double avgRating = reviews.stream()
                .filter(r -> r.getRating() != null)
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        model.addAttribute("avgRating", avgRating);

        // empty Review object for the form
        model.addAttribute("review", new Review());

        // -------------------------------
        // Product Highlights / Specs
        // -------------------------------
        List<String> highlightsList = new ArrayList<>();
        if (product.getHighlights() != null && !product.getHighlights().isEmpty()) {
            highlightsList = Arrays.asList(product.getHighlights().split("\\r?\\n"));
        }
        model.addAttribute("highlightsList", highlightsList);

        // -------------------------------
        // Product status (availability)
        // -------------------------------
//        boolean isInactive = !Boolean.TRUE.equals(product.getIsActive());
//        boolean isOutOfStock = !product.getAvailability();

//        model.addAttribute("isInactive", isInactive);
//        model.addAttribute("isOutOfStock", isOutOfStock);
        // Product status (availability)
        model.addAttribute("isInactive", false); // Already checked above
        model.addAttribute("isOutOfStock", !product.getAvailability());

        // Fits (sizes)
        List<Fit> fits = product.getFits();
        model.addAttribute("fits", fits);

        // Related Products (recommendations)
        int relatedLimit = 4; // number of related products to show
        List<Product> relatedProducts = productService.getRelatedProducts(
                product.getCategory().getId(), // same category
                product.getId(),    // exclude product
                relatedLimit
        );
        model.addAttribute("relatedProducts", relatedProducts);





        return "user/product-details";
    }


    @PostMapping("/{id}/review")
    public String addReview(@PathVariable Long id,
                            @ModelAttribute("review") Review review,
                            BindingResult result,
                            Model model)  {

        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/error";
        }


        // Always create a NEW Review object
        Review newReview = Review.builder()
                .username(review.getUsername())
                .rating(review.getRating())
                .comment(review.getComment())
                .product(product)
                .build();

        if (result.hasErrors()) {
            // reuse the same code as GET to reload product details
            List<ProductImage> images = productService.getProductImage(product);
            List<Review> reviews = reviewService.getReviewsByProduct(product);

            model.addAttribute("product", product);
            model.addAttribute("images", images);
            model.addAttribute("reviews", reviews);

            double avgRating = reviews.stream()
                    .filter(r -> r.getRating() != null)
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            model.addAttribute("avgRating", avgRating);

            return "user/product-details"; // redisplay form with errors
        }

        reviewService.saveReview(newReview);  // redisplay form with errors
        return "redirect:/products/" + id;
    }

    // API response wrapper
    public record ProductResponse(List<ProductViewDTO> products, int currentPage, int totalPages) {}
}

