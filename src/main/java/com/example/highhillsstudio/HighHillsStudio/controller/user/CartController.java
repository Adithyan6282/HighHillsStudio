package com.example.highhillsstudio.HighHillsStudio.controller.user;

import com.example.highhillsstudio.HighHillsStudio.dto.user.CartDTO;
import com.example.highhillsstudio.HighHillsStudio.entity.Cart;
import com.example.highhillsstudio.HighHillsStudio.entity.Fit;
import com.example.highhillsstudio.HighHillsStudio.entity.Product;
import com.example.highhillsstudio.HighHillsStudio.entity.User;
import com.example.highhillsstudio.HighHillsStudio.repository.UserRepository;
import com.example.highhillsstudio.HighHillsStudio.service.admin.UserService;
import com.example.highhillsstudio.HighHillsStudio.service.user.CartService;
import com.example.highhillsstudio.HighHillsStudio.service.user.UserProductService;
import com.example.highhillsstudio.HighHillsStudio.service.user.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final UserProductService productService;
    private final CartService cartService;
    private final UsersService usersService;
    private final UserRepository userRepository;


    // Get cart items (for dynamic fetch)
    @GetMapping("/items")
    @ResponseBody
    public CartDTO getCartItems(@AuthenticationPrincipal UserDetails userDetails) {
        User user = usersService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return cartService.getUserCartDTO(user);
    }

    // Add to Cart (already correct)
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestBody Map<String, String > request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try{
            User user = usersService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            Long productId = Long.parseLong(request.get("productId"));
            String size = request.get("size");
            int quantity = Integer.parseInt(request.getOrDefault("quantity", "1"));

            cartService.addToCart(user, productId, size, quantity);

            response.put("success", true);
            response.put("message", "Product added to cart successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Falied to add to cart.");
        }
        return ResponseEntity.ok(response);

    }

    // View Cart (list product)
    @GetMapping
    public String viewCart(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = usersService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Cart cart = cartService.getUserCart(user);
        model.addAttribute("cart", cart);
        return "user/cart";
    }

    // Remove product from cart
    @PostMapping("/remove/{productId}")
    @ResponseBody
    public Map<String, Object> removeFromCart(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();
        try {
            User user = usersService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            cartService.removeFromCart(user, productId);
            response.put("success", true);
            response.put("message", "Item removed!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // clear all items
    @PostMapping("/clear")
    @ResponseBody
    public Map<String, Object> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = usersService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            cartService.clearCart(user);
            response.put("success", true);
            response.put("message", "Cart cleared!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    public int getCartItemCount(@AuthenticationPrincipal UserDetails userDetails) {
//        if(userDetails == null) {
//            return 0;
//        }
//        User user = usersService.findByEmail(userDetails.getUsername())
//                .orElseThrow(() -> new IllegalArgumentException("User not found"));
//        Cart cart = cartService.getUserCart(user);
//        return cart.getItems().size();


        if (userDetails == null) return 0;

        // Only try to fetch cart if it's a regular user
        return usersService.findByEmail(userDetails.getUsername())
                .map(user -> {
                    Cart cart = cartService.getUserCart(user);
                    return cart.getItems().size();
                })
                .orElse(0); // fallback to 0 if admin or user not found
    }



    @PostMapping("/increment")
    @ResponseBody
    public Map<String, Object> incrementCartItem(
            @RequestParam Long productId,
            @RequestParam String size,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();
        try {
            User user = usersService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            cartService.incrementCartItem(user, productId, size);
            response.put("success", true);
            response.put("message", "Quantity increased");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }


    @PostMapping("/decrement")
    @ResponseBody
    public Map<String, Object> decrementCartItem(
            @RequestParam Long productId,
            @RequestParam String size,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();
        try {
            User user = usersService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            cartService.decrementCartItem(user, productId, size);
            response.put("success", true);
            response.put("message", "Quantity decreased");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }






}
