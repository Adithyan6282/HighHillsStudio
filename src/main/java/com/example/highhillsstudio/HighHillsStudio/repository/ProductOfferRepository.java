package com.example.highhillsstudio.HighHillsStudio.repository;

import com.example.highhillsstudio.HighHillsStudio.entity.Product;
import com.example.highhillsstudio.HighHillsStudio.entity.ProductOffer;
import com.example.highhillsstudio.HighHillsStudio.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductOfferRepository extends JpaRepository<ProductOffer, Long> {

    Optional<ProductOffer> findByProductAndActiveTrue(Product product);

}
