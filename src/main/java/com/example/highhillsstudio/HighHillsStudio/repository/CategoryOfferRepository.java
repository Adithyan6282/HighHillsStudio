package com.example.highhillsstudio.HighHillsStudio.repository;

import com.example.highhillsstudio.HighHillsStudio.entity.Category;
import com.example.highhillsstudio.HighHillsStudio.entity.CategoryOffer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryOfferRepository extends JpaRepository<CategoryOffer, Long> {

    Optional<CategoryOffer> findByCategoryAndActiveTrue(Category category);

}
