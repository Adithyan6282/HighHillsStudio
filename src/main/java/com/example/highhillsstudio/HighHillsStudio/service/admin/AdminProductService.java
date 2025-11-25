package com.example.highhillsstudio.HighHillsStudio.service.admin;

import com.example.highhillsstudio.HighHillsStudio.dto.admin.ProductDTO;
import com.example.highhillsstudio.HighHillsStudio.dto.admin.ProductTypeDTO;
import com.example.highhillsstudio.HighHillsStudio.entity.*;
import com.example.highhillsstudio.HighHillsStudio.entity.Collection;
import com.example.highhillsstudio.HighHillsStudio.mapper.ProductMapper;
import com.example.highhillsstudio.HighHillsStudio.repository.*;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminProductService {



    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductTypeRepository typeRepository;
    private final ColorRepository colorRepository;
    private final FitRepository fitRepository;
    private final StockRepository stockRepository;
    private final CollectionRepository collectionRepository;
    private final ProductGenderRepository productGenderRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;

//    private final String UPLOAD_DIR = "C:/highhills/uploads/products/";

    // test

    @Value("${product.upload.dir}")
    private String UPLOAD_DIR;



    // -------------------------
    // List products with pagination & search
    // -------------------------
    public Page<Product> listProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        if (keyword != null && !keyword.isEmpty()) {
            return productRepository.findAllByNameContainingIgnoreCaseAndIsActiveTrue(keyword, pageable);
        }
        return productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) return null;

        // Initialize fits safely
        if (product.getFits() != null) {
            product.getFits().removeIf(Objects::isNull); // remove null entries
            for (Fit fit : product.getFits()) {
                if (fit.getSize() == null) fit.setSize("");
                if (fit.getStock() == null) {
                    Stock stock = new Stock();
                    stock.setQuantity(0); // default quantity
                    stock.setFit(fit);
                    fit.setStock(stock);
                }
            }
        } else {
            product.setFits(new ArrayList<>());
        }

        // Initialize images safely
        if (product.getImages() != null) {
            product.getImages().removeIf(Objects::isNull);
            for (ProductImage img : product.getImages()) {
                if (img.getImg() == null) img.setImg("");
                if (img.getIsMain() == null) img.setIsMain(false);
            }
        } else {
            product.setImages(new ArrayList<>());
        }

        // Initialize other associations safely
        if (product.getProductType() != null && product.getProductType().getName() == null) {
            product.getProductType().setName("");
        }
        if (product.getCategory() != null && product.getCategory().getName() == null) {
            product.getCategory().setName("");
        }
        if (product.getProductGender() != null && product.getProductGender().getName() == null) {
            product.getProductGender().setName("");
        }
        if (product.getCollection() != null && product.getCollection().getName() == null) {
            product.getCollection().setName("");
        }
        if (product.getColor() != null && product.getColor().getName() == null) {
            product.getColor().setName("");
        }
        return product;

    }

    public void deleteProduct(Long id) {
        Product product = getProduct(id);
        if (product != null) {
            product.setIsActive(false);
            productRepository.save(product);
        }
    }

    public Integer getStockQuantity(Long productId) {
        Product product = getProduct(productId);
        if (product != null && product.getFits() != null) {
            return product.getFits().stream()
                    .map(fit -> fit.getStock() != null ? fit.getStock().getQuantity() : 0)
                    .reduce(0, Integer::sum);
        }
        return 0;
    }

    public List<ProductType> getAllTypes() { return typeRepository.findAll(); }

    public List<Color> getAllColors() { return colorRepository.findAll(); }

    public List<Fit> getAllFits() { return fitRepository.findAll(); }

    public List<Collection> getAllCollections() { return collectionRepository.findAll(); }

    public List<ProductGender> getAllGenders() { return productGenderRepository.findAll(); }

    public List<Category> getAllCategories() { return categoryRepository.findAll(); }

    public List<ProductType> getTypesByCategory(Long categoryId) {
        return typeRepository.findByCategoryId(categoryId);
     }


    // Get global types (category-independent)
    // -------------------------
    public List<ProductType> getGlobalTypes() {
        return typeRepository.findByCategoryIsNull(); // assumes global types have null category
    }

    public List<ProductTypeDTO> getTypesDTOByCategory(Long categoryId) {
        List<ProductType> types = getTypesByCategory(categoryId);
        List<ProductType> globalTypes = getGlobalTypes();

        types.addAll(globalTypes);

        return types.stream()
                .filter(ProductType::isActive)
                .map(t -> new ProductTypeDTO(t.getId(), t.getName()))
                .toList();
    }

    // -------------------------
    // Save new product
    // -------------------------
    @Transactional
    public Product saveProduct(Product product,
                               List<String> sizes,
                               List<Integer> quantities,
                               List<MultipartFile> files) throws IOException {

        validateImages(files);

        mapSizesToFits(product, sizes, quantities);
        Product savedProduct = productRepository.save(product);
        handleImages(savedProduct, files, new ArrayList<>());
        return savedProduct;
    }

    // -------------------------
    // Update existing product
    // -------------------------


    @Transactional
    public Product saveOrUpdateProduct(Product product,
                                       List<Long> existingImageIds,
                                       List<MultipartFile> newFiles,
                                       List<String> sizes,
                                       List<Integer> quantities) throws IOException {

        // 1️⃣ Persist product data
        Product savedProduct = productRepository.save(product);

        // 2️⃣ Update images
        List<ProductImage> currentImages = savedProduct.getImages() != null
                ? savedProduct.getImages()
                : new ArrayList<>();

        List<ProductImage> toRemoveImages = currentImages.stream()
                .filter(img -> existingImageIds == null || !existingImageIds.contains(img.getId()))
                .toList();

        for (ProductImage img : toRemoveImages) {
            File f = new File(UPLOAD_DIR + Paths.get(img.getImg()).getFileName());
            if (f.exists()) f.delete();
        }

        currentImages.removeAll(toRemoveImages);
        handleImages(savedProduct, newFiles, currentImages);

        // 3️⃣ Remove old fits completely
        savedProduct.getFits().clear();

        // 4️⃣ Add NEW fits
        mapSizesToFits(savedProduct, sizes, quantities);

        // 5️⃣ Final save
        return productRepository.save(savedProduct);
    }




    // -------------------------
    // Get ProductDTO for JSON
    // -------------------------
    @Transactional(readOnly = true)
    public ProductDTO getProductDTO(Long id){
        Product product = getProduct(id);
        if (product == null) return null;

        return ProductMapper.toDTO(product);
    }

    // -------------------------
    // Helper methods
    // -------------------------
    private void validateImages(List<MultipartFile> files){
        if(files == null || files.size() < 3) throw new IllegalArgumentException("Each product must have at least 3 images.");
    }






    private void mapSizesToFits(Product product,
                                List<String> sizes,
                                List<Integer> quantities) {

        if (sizes == null || quantities == null || sizes.size() != quantities.size())
            return;

        if (product.getFits() == null)
            product.setFits(new ArrayList<>());

        // Map existing fits
        Map<String, Fit> existingFits = product.getFits().stream()
                .collect(Collectors.toMap(f -> f.getSize().toUpperCase(), f -> f));

        for (int i = 0; i < sizes.size(); i++) {

            String size = sizes.get(i);
            Integer qty = quantities.get(i);

            if (size == null || size.isBlank())
                continue;

            String key = size.toUpperCase();

            if (existingFits.containsKey(key)) {
                // ✔ update existing fit
                Fit fit = existingFits.get(key);
                if (fit.getStock() != null) {
                    fit.getStock().setQuantity(qty);
                } else {
                    Stock stock = new Stock();
                    stock.setFit(fit);
                    stock.setQuantity(qty);
                    fit.setStock(stock);
                }
            } else {
                // ✔ create new fit
                Fit newFit = new Fit();
                newFit.setProduct(product);
                newFit.setSize(size);
                newFit.setIsActive(true);

                Stock stock = new Stock();
                stock.setFit(newFit);
                stock.setQuantity(qty);

                newFit.setStock(stock);
                product.getFits().add(newFit);
            }
        }


    }




    private void handleImages(Product product, List<MultipartFile> files, List<ProductImage> existingImages) throws IOException {
        if(files == null) return;
        boolean isFirst = existingImages.isEmpty();
        Set<String> paths = new HashSet<>();
        existingImages.forEach(img -> paths.add(img.getImg()));

        List<ProductImage> newImages = new ArrayList<>();

        for(MultipartFile file : files){
            if(file.isEmpty()) continue;

            String baseName = System.currentTimeMillis() + "_" + file.getOriginalFilename().replaceAll("\\s+","_");
            String finalName = baseName.replaceAll("\\.[^.]+$","") + ".png";
            String finalPath = "/uploads/products/" + finalName;

            if(paths.contains(finalPath)) continue;

//            File dest = new File(UPLOAD_DIR + finalName);

            //  Use Paths.get() for cross-platform path    Test
            File dest = Paths.get(UPLOAD_DIR, finalName).toFile();

            if(!dest.getParentFile().exists()) dest.getParentFile().mkdirs();

            BufferedImage img = ImageIO.read(file.getInputStream());
            if(img == null) throw new IOException("Unsupported image format: " + file.getOriginalFilename());

            Thumbnails.of(img).size(800,800).keepAspectRatio(true).outputFormat("png").toFile(dest);

            ProductImage pi = ProductImage.builder()
                    .product(product)
                    .img(finalPath)
                    .isActive(true)
                    .isMain(isFirst)
                    .build();

            newImages.add(pi);
            paths.add(finalPath);
            isFirst = false;
        }

        productImageRepository.saveAll(newImages);
        if(product.getImages() == null) product.setImages(new ArrayList<>());
        product.getImages().addAll(newImages);
    }


    public boolean existsByField(String field, String value, Long id) {
        if (field.equalsIgnoreCase("name")) {
            Optional<Product> existing = productRepository.findByNameIgnoreCase(value);
            return existing.isPresent() && (id == null || !existing.get().getId().equals(id));
        }
        return false;
    }





}



