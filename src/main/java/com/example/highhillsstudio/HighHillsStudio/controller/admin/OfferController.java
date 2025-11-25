package com.example.highhillsstudio.HighHillsStudio.controller.admin;

import com.example.highhillsstudio.HighHillsStudio.entity.Category;
import com.example.highhillsstudio.HighHillsStudio.entity.CategoryOffer;
import com.example.highhillsstudio.HighHillsStudio.entity.Product;
import com.example.highhillsstudio.HighHillsStudio.entity.ProductOffer;
import com.example.highhillsstudio.HighHillsStudio.repository.CategoryRepository;
import com.example.highhillsstudio.HighHillsStudio.repository.ProductRepository;
import com.example.highhillsstudio.HighHillsStudio.service.admin.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/offer")
@RequiredArgsConstructor
public class OfferController {


    private final OfferService offerService;
    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;

    // ---------------- Product Offers ----------------
    @GetMapping("/product")
    public String productOffers(Model model) {
        model.addAttribute("offers", offerService.getAllProductOffers());
        return "admin/product-offer/list";
    }




    @GetMapping("/product/add")
    public String addOrEditProductOfferForm(@ModelAttribute("offer") ProductOffer offer, Model model, Long id) {
        if (id != null) {
            // Fetch existing offer for editing
            offer = offerService.getAllProductOffers().stream()
                    .filter(o -> o.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Product Offer not found"));
        }
        model.addAttribute("offer", offer);
        model.addAttribute("products", productRepo.findAll());
        return "admin/product-offer/form";
    }


    @GetMapping("/product/delete/{id}")
    public String deleteProductOffer(@PathVariable Long id) {
        offerService.deleteProductOffer(id);
        return "redirect:/admin/offer/product";
    }

    @PostMapping("/product/save")
    public String saveProductOffer(@ModelAttribute ProductOffer offer, Long productId) {

        // Fetch Product entity
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        offer.setProduct(product); // assign the entity
        offerService.saveProductOffer(offer);
        return "redirect:/admin/offer/product";
    }

    // ---------------- Category Offers ----------------
    @GetMapping("/category")
    public String categoryOffers(Model model) {
        model.addAttribute("offers", offerService.getAllCategoryOffers());
        return "admin/category-offer/list";
    }




    @GetMapping("/category/add")
    public String addOrEditCategoryOfferForm(@ModelAttribute("offer") CategoryOffer offer, Model model, Long id) {
        if (id != null) {
            // Fetch existing offer for editing
            offer = offerService.getAllCategoryOffers().stream()
                    .filter(o -> o.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Category Offer not found"));
        }
        model.addAttribute("offer", offer);
        model.addAttribute("categories", categoryRepo.findAll());
        return "admin/category-offer/form";
    }


    @GetMapping("/category/delete/{id}")
    public String deleteCategoryOffer(@PathVariable Long id) {
        offerService.deleteCategoryOffer(id);
        return "redirect:/admin/offer/category";
    }

    @PostMapping("/category/save")
    public String saveCategoryOffer(@ModelAttribute CategoryOffer offer, Long categoryId) {
        // Fetch the Category entity
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        offer.setCategory(category);  // Set the entity
        offerService.saveCategoryOffer(offer);
        return "redirect:/admin/offer/category";
    }


}
