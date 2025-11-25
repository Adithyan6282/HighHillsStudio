package com.example.highhillsstudio.HighHillsStudio.repository;

import com.example.highhillsstudio.HighHillsStudio.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductTypeRepository extends JpaRepository <ProductType, Long> {




    // Category-specific types
    List<ProductType> findByCategoryId(Long categoryId);

    // Global types (category is null)
    List<ProductType> findByCategoryIsNull();


}

//    List<ProductType> findByCategoryId(Long categoryId);

//    @Query("SELECT pt FROM ProductType pt WHERE pt.category.id = :categoryId AND pt.isActive = true")
//    List<ProductType> findByCategoryId(@Param("categoryId") Long categoryId);

