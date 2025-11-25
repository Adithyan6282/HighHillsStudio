package com.example.highhillsstudio.HighHillsStudio.service.admin;

import com.example.highhillsstudio.HighHillsStudio.entity.CategoryOffer;
import com.example.highhillsstudio.HighHillsStudio.entity.ProductOffer;
import com.example.highhillsstudio.HighHillsStudio.repository.CategoryOfferRepository;
import com.example.highhillsstudio.HighHillsStudio.repository.CategoryRepository;
import com.example.highhillsstudio.HighHillsStudio.repository.ProductOfferRepository;
import com.example.highhillsstudio.HighHillsStudio.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final ProductOfferRepository productOfferRepo;
    private final CategoryOfferRepository categoryOfferRepo;

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;

    // Product Offer
    public List<ProductOffer> getAllProductOffers() {
        return productOfferRepo.findAll();
    }

    public ProductOffer saveProductOffer(ProductOffer offer) {
        offer.setActive(true);
        return productOfferRepo.save(offer);
    }

    public void deleteProductOffer(Long id) {
        productOfferRepo.deleteById(id);
    }

    // Category Offer
    public List<CategoryOffer> getAllCategoryOffers() {
        return categoryOfferRepo.findAll();
    }

    public CategoryOffer saveCategoryOffer(CategoryOffer offer) {
        offer.setActive(true);
        return categoryOfferRepo.save(offer);
    }

    public void deleteCategoryOffer(Long id) {
        categoryOfferRepo.deleteById(id);
    }
}
