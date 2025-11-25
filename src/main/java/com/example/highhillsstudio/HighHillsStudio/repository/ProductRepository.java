    package com.example.highhillsstudio.HighHillsStudio.repository;

    import com.example.highhillsstudio.HighHillsStudio.entity.*;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.jpa.repository.EntityGraph;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;
    import org.springframework.stereotype.Repository;

    import java.math.BigDecimal;
    import java.util.List;
    import java.util.Optional;

    @Repository
    public interface ProductRepository extends JpaRepository<Product, Long> {
        // Fetch all products by keyword (active only) with pagination
        @EntityGraph(attributePaths = "images")
        Page<Product> findAllByNameContainingIgnoreCaseAndIsActiveTrue(String keyword, Pageable pageable);

        //Filter by price range only
//        Page<Product> findAllByBasePriceBetweenAndIsActiveTrue(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
        Page<Product> findAllByFinalPriceBetweenAndIsActiveTrue(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

        Page<Product> findByNameContainingIgnoreCaseAndFinalPriceBetweenAndIsActiveTrue(String name, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

        Page<Product> findByNameContainingIgnoreCaseAndCategoryIdAndFinalPriceBetweenAndIsActiveTrue(
                String name, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

        Page<Product> findAllByCategoryIdAndFinalPriceBetweenAndIsActiveTrue(
                Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable
        );

        @EntityGraph(attributePaths = {"category", "fits", "fits.stock", "images"})
        Optional<Product> findById(Long id);


        // Fetch all productd that are not delted ie List all active products
        //for pagination with images
        @EntityGraph(attributePaths = "images")
        Page<Product> findAllByIsActiveTrue(Pageable pageable);

        // For fetching single product by ID with images
        @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id = :id AND p.isActive = true")
        Product findByIdWithImages(Long id);

        //   Home page / product details: fetch products with images
        @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.isActive = true")
        List<Product> findAllActiveWithImages();

        // fetch all active products wihtout pagination
        List<Product> findByIsActiveTrue();


        // Filter by category only
        Page<Product> findAllByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

        // Filter by product type
        Page<Product> findAllByProductTypeIdAndIsActiveTrue(Long productTypeId, Pageable pageable);

        // Filter by gender
        Page<Product> findAllByProductGenderIdAndIsActiveTrue(Long genderId, Pageable pageable);

       // search by keyword (name) only
        Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

        Page<Product> findByNameContainingIgnoreCaseAndCategoryId(String name, Long categoryId, Pageable pageable);


//        @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId") // test
//        Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

        Page<Product> findByNameContainingIgnoreCaseAndCategoryIdAndIsActiveTrue(String name, Long categoryId, Pageable pageable);
        Page<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);

        // Filter by category AND price range
        Page<Product> findAllByCategoryIdAndBasePriceBetweenAndIsActiveTrue(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

        Page<Product> findByNameContainingIgnoreCaseAndBasePriceBetweenAndIsActiveTrue(
                String name, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);


        Page<Product> findByNameContainingIgnoreCaseAndCategoryIdAndBasePriceBetweenAndIsActiveTrue(
                String name,
                Long categoryId,
                BigDecimal minPrice,
                BigDecimal maxPrice,
                Pageable pageable
        );


        // Test

        // Fetch all products with pagination (active + inactive)
        @Query("SELECT p FROM Product p")
        Page<Product> findAllProducts(Pageable pageable);

        // Fetch products by category (active + inactive)
        @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
        Page<Product> findAllByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

        // Fetch by price range (active + inactive)
        Page<Product> findAllByFinalPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);




        // ✅ New: Include INACTIVE also (no “IsActiveTrue” filters)
        Page<Product> findByNameContainingIgnoreCaseAndCategoryIdAndFinalPriceBetween(
                String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);



        Page<Product> findByNameContainingIgnoreCaseAndFinalPriceBetween(
                String keyword, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

        Page<Product> findAllByCategoryIdAndFinalPriceBetween(
                Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);



        List<Product> findByCategoryIdAndIdNotAndIsActiveTrue(Long categoryId, Long excludeProductId, Pageable pageable);


        Optional<Product> findByNameIgnoreCase(String name);













    }





