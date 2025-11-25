package com.example.highhillsstudio.HighHillsStudio.service.user;

import com.example.highhillsstudio.HighHillsStudio.dto.user.CartDTO;
import com.example.highhillsstudio.HighHillsStudio.dto.user.CartItemDTO;
import com.example.highhillsstudio.HighHillsStudio.entity.*;
import com.example.highhillsstudio.HighHillsStudio.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final WishListRepository wishListRepository;
    private final FitRepository fitRepository;

    // Maximum units a person can add per product
    private static final int MAX_QUANTITY_PER_PRODUCT = 5;


    @Transactional
    public void addToCart(User user, Long productId, String size,  int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Check if product is active

        if(!Boolean.TRUE.equals(product.getIsActive())) {
            throw new IllegalArgumentException("This product is currently unavailable.");
        }

        // check if product's category is active

        if(product.getCategory() != null && !Boolean.TRUE.equals(product.getCategory().isActive())) {
            throw new IllegalArgumentException("product form this category cannot be added to the cart.");
        }



        String cleanedSize = size != null ? size.trim().toUpperCase() : "";
        System.out.println("Requested size: '" + cleanedSize + "'");

        Fit fit = fitRepository.findByProduct_Id(productId).stream()
                .filter(f -> f.getSize() != null &&
                        f.getSize().trim().toUpperCase().equals(cleanedSize))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Selected size not available"));

        // Check stock
        int availableStock = fit.getStock().getQuantity();
        if (quantity > availableStock) {
            throw new IllegalArgumentException("Not enough stock for selected size");
        }



        // check stock avialability
        if(!product.getAvailability()) {
            throw new IllegalArgumentException("This product is out of stock");
        }


        // Fetch or create the user's cart
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });

        // Check if same product & fit already exists in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId) &&
                        item.getFit() != null &&
                        item.getFit().getSize().trim().equalsIgnoreCase(size.trim()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;

            if (newQuantity > MAX_QUANTITY_PER_PRODUCT) {
                throw new IllegalArgumentException("you can add a maximum of " + MAX_QUANTITY_PER_PRODUCT + " units of this product.");
            }

            if(newQuantity > availableStock) {
                throw new IllegalArgumentException("Cannot add more. Stock limit reached.");
            }

            existingItem.setQuantity(newQuantity);
        } else {

            if(quantity > MAX_QUANTITY_PER_PRODUCT) {
                throw new IllegalArgumentException("You can add a maximum of " + MAX_QUANTITY_PER_PRODUCT + " units of this product");
            }
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .fit(fit) // assign fit here
                    .quantity(quantity)
                    .price(product.getFinalPrice())
                    .build();
            cart.getItems().add(newItem);
        }




        // save thr cart
        cartRepository.save(cart);

        // Remove product from wishlist if it exists

        wishListRepository.deleteByUserAndProduct(user, product);




    }

    @Transactional
    public Cart getUserCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    @Transactional
    public void removeFromCart(User user, Long productId) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        cart.getItems().clear();
        cartRepository.save(cart);
    }


    @Transactional(readOnly = true)
    public CartDTO getUserCartDTO(User user) {
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        // Map CartItems to CartItemDTO
        List<CartItemDTO> itemsDTO = cart.getItems().stream()
                .map(this::mapToCartItemDTO)
                .collect(Collectors.toList());

        return CartDTO.builder()
                .cartId(cart.getId())
                .items(itemsDTO)
                .totalAmount(cart.getTotalAmount())
                .build();
    }


    private CartItemDTO mapToCartItemDTO(CartItem item) {
        Product product = item.getProduct(); // Fetch lazily if needed

        String fitSize = item.getFit() != null ?  item.getFit().getSize() : "";

        int stockQty = 0;
        if(item.getFit() != null && item.getFit().getStock() != null) {
            stockQty = item.getFit().getStock().getQuantity();
        }

        return CartItemDTO.builder()
                .productId(product.getId())
                .productName(product.getName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .total(item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                .fitSize(fitSize)
                .availableStock(stockQty) // send stock to frontend
                .build();
    }


    @Transactional
    public void incrementCartItem(User user, Long productId, String size) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getProduct().getId().equals(productId)
                        && ci.getFit() != null
                        && ci.getFit().getSize().trim().equalsIgnoreCase(size.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        int availableStock = item.getFit().getStock().getQuantity();

        if(item.getQuantity() + 1 > MAX_QUANTITY_PER_PRODUCT) {
            throw new IllegalArgumentException("You can add a maximum of " + MAX_QUANTITY_PER_PRODUCT + " units of this product.");
        }

        if (item.getQuantity() + 1 > availableStock) {
            throw new IllegalArgumentException("Cannot add more. Stock limit reached.");
        }

        item.setQuantity(item.getQuantity() + 1);
        cartRepository.save(cart);
    }




    @Transactional
    public void decrementCartItem(User user, Long productId, String size) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getProduct().getId().equals(productId)
                        && ci.getFit() != null
                        && ci.getFit().getSize().trim().equalsIgnoreCase(size.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (item.getQuantity() <= 1) {
            // Remove item if quantity reaches 0
            cart.getItems().remove(item);
        } else {
            item.setQuantity(item.getQuantity() - 1);
        }

        cartRepository.save(cart);
    }





}
