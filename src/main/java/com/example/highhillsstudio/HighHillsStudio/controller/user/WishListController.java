package com.example.highhillsstudio.HighHillsStudio.controller.user;

import com.example.highhillsstudio.HighHillsStudio.entity.Product;
import com.example.highhillsstudio.HighHillsStudio.entity.User;
import com.example.highhillsstudio.HighHillsStudio.entity.WishListItem;
import com.example.highhillsstudio.HighHillsStudio.repository.WishListRepository;
import com.example.highhillsstudio.HighHillsStudio.service.admin.UserService;
import com.example.highhillsstudio.HighHillsStudio.service.user.CartService;
import com.example.highhillsstudio.HighHillsStudio.service.user.UserProductService;
import com.example.highhillsstudio.HighHillsStudio.service.user.UsersService;
import com.example.highhillsstudio.HighHillsStudio.service.user.WishListService;
import jakarta.persistence.Column;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishListController {

    private final UsersService usersService;
    private final WishListService wishListService;
    private final UserProductService userProductService;
    private final CartService cartService;


    // view WishList
    @GetMapping
    public String viewWishlist(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = usersService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        model.addAttribute("wishlistItems", wishListService.getWishlistItemsByUser(user));
        return "user/wishlist";
    }

    // Remove item
    @PostMapping("/remove/{productId}")
    @ResponseBody
    public String removeFromWishlist(@PathVariable Long productId,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        User user = usersService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        wishListService.removeFromWishList(user, productId);
        return "success";
    }


    @PostMapping("/add/{productId}")
    @ResponseBody
    public Map<String, String> addToWishlist(@PathVariable Long productId,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        User user = usersService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Use UserProductService to fetch product
        Product product = userProductService.getProductById(productId);
        if(product == null) {
            throw new IllegalArgumentException("Product not found");
        }

        wishListService.addToWishList(user, product);

        return Map.of("status", "success", "message", "Product added to wishlist");
    }


    @GetMapping("/count")
    @ResponseBody
    public Map<String, Integer> getWishlistCount(@AuthenticationPrincipal UserDetails userDetails) {
        User user = usersService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        int count = wishListService.getWishlistItemsByUser(user).size();

        return Map.of("count", count);
    }


    // ✅ MOVE TO CART (from Wishlist)
    @PostMapping("/move-to-cart/{productId}")
    @ResponseBody
    public Map<String, String> moveToCart(@PathVariable Long productId,
                                          @AuthenticationPrincipal UserDetails userDetails) {

        // 1️⃣ Get user
        User user = usersService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2️⃣ Get wishlist item for this product & user
        WishListItem wishlistItem = wishListService.findByUserAndProduct(user, productId);
        if (wishlistItem == null) {
            return Map.of("status", "error", "message", "Item not found in wishlist");
        }

        // 3️⃣ Get product
        Product product = wishlistItem.getProduct();
        if (product == null) {
            return Map.of("status", "error", "message", "Product not found");
        }

        // 4️⃣ Get size from wishlist if available (else default "M")
        String size = (wishlistItem.getFit() != null)
                ? wishlistItem.getFit().getSize()
                : "M";

        // 5️⃣ Add to cart with quantity = 1
        cartService.addToCart(user, product.getId(), size, 1);

        // 6️⃣ Remove from wishlist
        wishListService.removeFromWishList(user, productId);

        return Map.of("status", "success", "message", "Moved to cart and removed from wishlist");
    }

}
