package com.example.highhillsstudio.HighHillsStudio.controller.admin;
import com.example.highhillsstudio.HighHillsStudio.dto.admin.ProductDTO;
import com.example.highhillsstudio.HighHillsStudio.dto.admin.ProductTypeDTO;
import com.example.highhillsstudio.HighHillsStudio.entity.Category;
import com.example.highhillsstudio.HighHillsStudio.entity.Product;
import com.example.highhillsstudio.HighHillsStudio.entity.ProductType;
import com.example.highhillsstudio.HighHillsStudio.mapper.ProductMapper;
import com.example.highhillsstudio.HighHillsStudio.service.admin.CategoryService;
import com.example.highhillsstudio.HighHillsStudio.service.admin.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Controller
    @RequestMapping("/admin/products")
    @RequiredArgsConstructor
    public class AdminProductController {


        private final AdminProductService adminProductService;
        private final CategoryService categoryService;



        // -------------------------
        // List products with search & pagination
        // -------------------------
        @GetMapping
        public String listProducts(Model model,
                                   @RequestParam(defaultValue = "") String keyword,
                                   @RequestParam(defaultValue = "0") int page) {

            Page<Product> products = adminProductService.listProducts(keyword, page, 5);

            Map<Long, Integer> stockMap = new HashMap<>();
            for (Product p : products.getContent()) {
                stockMap.put(p.getId(), adminProductService.getStockQuantity(p.getId()));
            }

            model.addAttribute("productsPage", products);
            model.addAttribute("keyword", keyword);
            model.addAttribute("stockMap", stockMap);

            return "admin/products/list";
        }

        // -------------------------
        // AJAX / JSON response for editing
        // -------------------------
        @GetMapping("/{id}/json")
        @ResponseBody
        public Map<String,Object> getProductJson(@PathVariable Long id) {
            Map<String, Object> resp = new HashMap<>();
            try {
                ProductDTO dto = adminProductService.getProductDTO(id);

                if (dto == null) {
                    resp.put("status", "error");
                    resp.put("message", "Product not found");
                    resp.put("data", null);
                } else {
                    resp.put("status", "success");
                    resp.put("message", "Product fetched successfully");
                    resp.put("data", dto);
                }

            } catch (Exception e) {
                e.printStackTrace(); // full stacktrace for debugging
                resp.put("status", "error");
                resp.put("message", "Something went wrong: " + (e.getMessage() != null ? e.getMessage() : "Check server logs"));
                resp.put("data", null);
            }
            return resp;
        }

        // -------------------------
        // Show add product form
        // -------------------------
        @GetMapping("/new")
        public String newProduct(Model model) {

            Product product = new Product();
            model.addAttribute("product", product);

            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories != null ? categories : List.of());

            // Load category-specific + global product types safely
            List<ProductTypeDTO> types = List.of();
            if (!categories.isEmpty()) {
                types = adminProductService.getTypesDTOByCategory(categories.get(0).getId());
            } else {
                // fallback to global types only
                types = adminProductService.getGlobalTypes().stream()
                        .filter(ProductType::isActive)
                        .map(t -> new ProductTypeDTO(t.getId(), t.getName()))
                        .toList();
            }

            model.addAttribute("types", types);

            model.addAttribute("colors", adminProductService.getAllColors());
            model.addAttribute("fits", adminProductService.getAllFits());
            model.addAttribute("collections", adminProductService.getAllCollections());
            model.addAttribute("genders", adminProductService.getAllGenders());
            model.addAttribute("stockQuantity", 0);
//            model.addAttribute("finalPrice", product.getFinalPrice());
            model.addAttribute("finalPrice",
                    product.getFinalPrice() != null ? product.getFinalPrice() : BigDecimal.ZERO);




            return "admin/products/form";
        }

        // -------------------------
        // Show edit product form
        // -------------------------
        @GetMapping("/{id}/edit")
        public String editProduct(@PathVariable Long id, Model model) {
            // 1️⃣ Fetch product safely
            Product product = adminProductService.getProduct(id);
            if (product == null) {
                model.addAttribute("errorMessage", "Product not found");
                return "admin/error"; // simple error.html page
            }
            model.addAttribute("product", product);

            // 2️⃣ Categories (never null)
            List<Category> categories = Optional.ofNullable(categoryService.getAllCategories()).orElse(List.of());
            model.addAttribute("categories", categories);

            // 3️⃣ Product types (category-specific + global)
            List<ProductTypeDTO> types = new ArrayList<>(); // ← mutable list

            // Category-specific types
            if (product.getCategory() != null) {
                List<ProductTypeDTO> categoryTypes = Optional.ofNullable(
                        adminProductService.getTypesDTOByCategory(product.getCategory().getId())
                ).orElse(List.of());
                types.addAll(categoryTypes); // safe now
            }

            // Global types
            List<ProductTypeDTO> globalTypes = Optional.ofNullable(adminProductService.getGlobalTypes())
                    .orElse(List.of()).stream()
                    .filter(t -> t != null && t.isActive())
                    .map(t -> new ProductTypeDTO(t.getId(), t.getName() != null ? t.getName() : ""))
                    .toList();

            types.addAll(globalTypes); // now safe

            model.addAttribute("types", types);

            // 4️⃣ Colors, fits, collections, genders (never null)
            model.addAttribute("colors", Optional.ofNullable(adminProductService.getAllColors()).orElse(List.of()));
            model.addAttribute("fits", Optional.ofNullable(adminProductService.getAllFits()).orElse(List.of()));
            model.addAttribute("collections", Optional.ofNullable(adminProductService.getAllCollections()).orElse(List.of()));
            model.addAttribute("genders", Optional.ofNullable(adminProductService.getAllGenders()).orElse(List.of()));

            // 5️⃣ Stock quantity (safe)
            Integer stockQty = Optional.ofNullable(adminProductService.getStockQuantity(product.getId())).orElse(0);
            model.addAttribute("stockQuantity", stockQty);

            // 6️⃣ Final price (safe)
            model.addAttribute("finalPrice", product.getFinalPrice() != null ? product.getFinalPrice() : BigDecimal.ZERO);

            return "admin/products/form";



        }

        // -------------------------
        // AJAX: get product types by category (category + global)
        // -------------------------
        @GetMapping("/types")
        @ResponseBody
        public List<ProductTypeDTO> getTypesByCategory(@RequestParam Long categoryId) {

            List<ProductType> types = adminProductService.getTypesByCategory(categoryId);
            List<ProductType> globalTypes = adminProductService.getGlobalTypes();

            types.addAll(globalTypes);

            return types.stream()
                    .filter(ProductType::isActive)
                    .map(t -> new ProductTypeDTO(t.getId(), t.getName()))
                    .toList();
        }

        // -------------------------
        // Save new or update existing product
        // -------------------------
        @PostMapping("/save")
        public String saveProduct(@ModelAttribute Product product,
                                  @RequestParam(value = "sizes") List<String> sizes,
                                  @RequestParam(value = "quantities") List<Integer> quantities,
                                  @RequestParam(value = "existingImageIds", required = false) List<Long> existingImageIds,
                                  @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles) throws IOException {

            int existingCount = existingImageIds != null ? existingImageIds.size() : 0;
            int newCount = imageFiles != null ? imageFiles.size() : 0;

            if (existingCount + newCount < 3) {
                throw new IllegalArgumentException("Each product must have at least 3 images.");
            }

            if (product.getId() == null) {
                adminProductService.saveProduct(product, sizes, quantities, imageFiles);
            } else {
                adminProductService.saveOrUpdateProduct(product, existingImageIds, imageFiles, sizes, quantities);
            }

            return "redirect:/admin/products";
        }

        // -------------------------
        // Soft delete product
        // -------------------------
        @PostMapping("/{id}/delete")
        public String deleteProduct(@PathVariable Long id) {
            adminProductService.deleteProduct(id);
            return "redirect:/admin/products";
        }

        // test //

    @GetMapping("/check-unique")
    @ResponseBody
    public Map<String, Object> checkUnique(
            @RequestParam String field,
            @RequestParam String value,
            @RequestParam(required = false) Long id) {

        boolean exists = adminProductService.existsByField(field, value, id);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("message", exists ? field + " already exists!" : field + " is available");
        return response;
    }



}


