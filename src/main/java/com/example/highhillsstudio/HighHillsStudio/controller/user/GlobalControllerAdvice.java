package com.example.highhillsstudio.HighHillsStudio.controller.user;

import com.example.highhillsstudio.HighHillsStudio.entity.Cart;
import com.example.highhillsstudio.HighHillsStudio.entity.User;
import com.example.highhillsstudio.HighHillsStudio.service.user.CartService;
import com.example.highhillsstudio.HighHillsStudio.service.user.UsersService;
import com.example.highhillsstudio.HighHillsStudio.service.user.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(assignableTypes = {UserProductController.class, CartController.class, HomeController.class, UserProfileController.class, ForgotPasswordController.class, WishListController.class, CheckoutController.class, PaymentController.class})
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UsersService usersService;
    private final CartService cartService;
    private final WishListService wishListService;



    @ModelAttribute("cart")
    public Cart cart(@AuthenticationPrincipal UserDetails userDetails) {


        if (userDetails == null) return null;

        return usersService.findByEmail(userDetails.getUsername())
                .filter(u -> "USER".equals(u.getRole())) // Only regular users
                .map(cartService::getUserCart)
                .orElse(null);

    }

    @ModelAttribute("wishlistCount")
    public int wishlistCount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return 0;

        return usersService.findByEmail(userDetails.getUsername())
                .map(user -> wishListService.getWishlistItemsByUser(user).size())
                .orElse(0);
    }







}
