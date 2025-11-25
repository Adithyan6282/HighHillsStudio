package com.example.highhillsstudio.HighHillsStudio.controller.user;

import com.example.highhillsstudio.HighHillsStudio.dto.user.CheckoutSummaryDTO;
import com.example.highhillsstudio.HighHillsStudio.dto.user.UserAddressDTO;
import com.example.highhillsstudio.HighHillsStudio.entity.*;
import com.example.highhillsstudio.HighHillsStudio.repository.UserAddressRepository;
import com.example.highhillsstudio.HighHillsStudio.repository.UserOrderRepository;
import com.example.highhillsstudio.HighHillsStudio.repository.UserRepository;
import com.example.highhillsstudio.HighHillsStudio.security.CustomUserDetails;
import com.example.highhillsstudio.HighHillsStudio.service.admin.UserService;
import com.example.highhillsstudio.HighHillsStudio.service.user.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {



    private final CheckoutService checkoutService;
    private final UserRepository userRepository;
    private final UsersService usersService;
    private final CartService cartService;
    private final UserProfileService userProfileService;
    private final UserOrderRepository userOrderRepository;
    private final UserAddressRepository userAddressRepository;
    private final WalletService walletService;





    @GetMapping
    public String checkoutPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        //  Get logged-in user
        User user = usersService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        //  Get user addresses
        List<UserAddressDTO> addresses = checkoutService.getUserAddresses(user);

        //  Get cart summary
        CheckoutSummaryDTO cartSummary = checkoutService.getCartSummary(user);

        //  Set shipping charge
        if (cartSummary.getItems() != null && !cartSummary.getItems().isEmpty()) {
            cartSummary.setShipping(BigDecimal.valueOf(50)); // shipping charge
        } else {
            cartSummary.setShipping(BigDecimal.ZERO);
        }

        // Fetch Wallet Balance
        BigDecimal walletBalance = walletService.getWalletBalance(user);

        //  Generate unique order code
        String orderCode = "ORDER_" + System.currentTimeMillis();



        // 7️Add attributes to the model
        model.addAttribute("addresses", addresses);
        model.addAttribute("cartSummary", cartSummary);
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("orderCode", orderCode);
        model.addAttribute("walletBalance", walletBalance);

        // 8️⃣ Return checkout Thymeleaf page
        return "user/checkout"; // Thymeleaf template: user/checkout.html



    }

    // -------------------- AJAX: Set Default Address --------------------
    @PostMapping("/addresses/default")
    @ResponseBody
    public Map<String, Object> setDefaultAddress(
            @RequestParam Long addressId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = usersService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            checkoutService.setDefaultAddress(user, addressId);

            response.put("success", true);
            response.put("message", "Default address updated");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }



    // -------------------- AJAX: Add or Update Address --------------------
    @PostMapping("/addresses/save")
    @ResponseBody
    public Map<String, Object> saveAddress(
            @RequestBody UserAddressDTO addressDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = usersService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            UserAddressDTO saved = checkoutService.saveAddress(user, addressDTO);
            response.put("success", true);
            response.put("address", saved);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }


    // -------------------- AJAX: Delete Address --------------------
    @DeleteMapping("/addresses/{id}")
    @ResponseBody
    public Map<String, Object> deleteAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = usersService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            checkoutService.deleteAddress(user, id);

            response.put("success", true);
            response.put("message", "Address deleted");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }





    @PostMapping("/place-order")
    public String placeOrderCOD(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String orderCode,
                                @RequestParam Long addressId,
                                Model model) {

        // 1️⃣ Get logged-in user
        User user = usersService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2️⃣ Get the cart summary
        CheckoutSummaryDTO cartSummary = checkoutService.getCartSummary(user);

        // 3️⃣ Restrict COD if order total > 1000
        if (cartSummary.getFinalTotal().compareTo(BigDecimal.valueOf(1000)) > 0) {
            model.addAttribute("message", "❌ Cash on Delivery is not allowed for orders above ₹1000. Please use Online Payment or Wallet.");
            return "user/payment-failure"; // or redirect back to checkout page
        }

        // 4️⃣ Get the order (create if not exists)
        UserOrder order = userOrderRepository.findByOrderCode(orderCode)
                .orElseGet(() -> {
                    UserAddress address = checkoutService.getAddressByIdForUser(user, addressId);
                    return checkoutService.createOrderFromCart(user, address, OrderStatus.PLACED);
                });

        // 5️⃣ Mark order as PLACED
        order.setStatus(OrderStatus.PLACED);
        userOrderRepository.save(order);

        // 6️⃣ Deduct stock & clear cart
        checkoutService.deductStockForOrder(order);
        cartService.clearCart(user);

        // 7️⃣ Success page
        model.addAttribute("order", order);
        model.addAttribute("paymentMethod", "COD");
        model.addAttribute("message", "✅ Cash on Delivery order has been placed successfully!");
        return "user/order-success";
    }





    @PostMapping("/pay-with-wallet")
    @Transactional
    public String payWithWallet(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String orderCode,
                                @RequestParam Long addressId,
                                Model model) {

        User user = usersService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 1️⃣ Check if order already exists
        UserOrder existingOrder = userOrderRepository.findByOrderCode(orderCode).orElse(null);

        CheckoutSummaryDTO cartSummary;
        BigDecimal orderTotal;

        if (existingOrder == null) {
            // 2️⃣ Only check cart if order not created
            cartSummary = checkoutService.getCartSummary(user);

            if (cartSummary.getItems().isEmpty()) {
                model.addAttribute("message", "❌ Cannot place order with empty cart.");
                return "user/payment-failure";
            }

            orderTotal = cartSummary.getFinalTotal();
        } else {
            // Order exists → use existing final amount
            orderTotal = existingOrder.getFinalAmount();
        }

        BigDecimal walletBalance = walletService.getWalletBalance(user);

        if (walletBalance.compareTo(orderTotal) < 0) {
            model.addAttribute("message", "❌ Insufficient Wallet Balance!");
            return "user/payment-failure";
        }

        // 3️⃣ Create order if not exists
        UserOrder order = existingOrder != null ?
                existingOrder :
                checkoutService.createOrderFromCart(user,
                        checkoutService.getAddressByIdForUser(user, addressId),
                        OrderStatus.PAID);

        // 4️⃣ Mark as paid
        order.setStatus(OrderStatus.PAID);
        userOrderRepository.save(order);

        // 5️⃣ Deduct stock, wallet, clear cart
        checkoutService.deductStockForOrder(order);
        walletService.payWithWallet(user, orderTotal);
        cartService.clearCart(user);

        model.addAttribute("order", order);
        model.addAttribute("paymentMethod", "WALLET");
        model.addAttribute("message", "✅ Wallet Payment Successful!");

        return "user/order-success";
    }




    @PostMapping("/apply-coupon")
    @ResponseBody
    public Map<String, Object> applyCoupon(
            @RequestParam String code,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 1️⃣ Get logged-in user
            User user = usersService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // 2️⃣ Save coupon to cart
            checkoutService.applyCouponToCart(user, code);

            // 3️⃣ Recalculate totals from cart
            CheckoutSummaryDTO summary = checkoutService.getCartSummary(user); // uses applied coupon in cart

            response.put("success", true);
            response.put("discountedTotal", summary.getFinalTotal());
            response.put("couponDiscount", summary.getCouponDiscount());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }




    @PostMapping("/remove-coupon")
    @ResponseBody
    public Map<String, Object> removeCoupon(
            @RequestParam BigDecimal total
    ) {
        Map<String, Object> response = new HashMap<>();
        BigDecimal resetTotal = checkoutService.removeCoupon(total);
        response.put("success", true);
        response.put("resetTotal", resetTotal);
        return response;
    }

    @GetMapping("/check-duplicate-address")
    @ResponseBody
    public boolean checkDuplicateAddress(
            @RequestParam String line1,
            @RequestParam String line2,
            @RequestParam String phone) {

        return userAddressRepository.existsByLine1AndLine2AndPhone(line1, line2, phone);
    }



}

