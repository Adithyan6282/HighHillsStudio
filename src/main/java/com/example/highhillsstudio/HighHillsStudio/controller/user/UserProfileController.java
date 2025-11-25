package com.example.highhillsstudio.HighHillsStudio.controller.user;

import com.example.highhillsstudio.HighHillsStudio.dto.user.OrderDTO;
import com.example.highhillsstudio.HighHillsStudio.entity.*;
import com.example.highhillsstudio.HighHillsStudio.service.user.EmailChangeService;
import com.example.highhillsstudio.HighHillsStudio.service.user.UserProfileService;
import com.example.highhillsstudio.HighHillsStudio.service.user.UsersService;
import com.example.highhillsstudio.HighHillsStudio.service.user.WalletService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;
    private final EmailChangeService emailChangeService;
    private final WalletService walletService;
    private final UsersService usersService;

    @Value("${app.base-url}")
    private String baseUrl;

    // show user profile page
    @GetMapping
    public String profilePage(Model model, Principal principal) {
        User user = profileService.getUserByEmail(principal.getName());
        List<UserAddress> addresses = profileService.getUserAddresses(user.getId());
        List<OrderDTO> orders = profileService.getUserOrders(user.getId());

        // ✅ Fetch wallet balance and recent transactions
        BigDecimal walletBalance = walletService.getWalletBalance(user);
        List<WalletTransaction> walletTransactions = walletService.getRecentTransactions(user);

        // Referral link
        String referralToken = usersService.generateReferralToken(user);

        String referralLink = baseUrl + "/users/signup?ref=" + referralToken;

        // ✅ Get active coupons
        List<Coupon> coupons = usersService.getActiveCouponsForUser(user);



        model.addAttribute("referralLink", referralLink);
        model.addAttribute("user", user);
        model.addAttribute("addresses", addresses);
        model.addAttribute("orders", orders);
        model.addAttribute("walletBalance", walletBalance);
        model.addAttribute("walletTransactions", walletTransactions);
        model.addAttribute("coupons", coupons); // pass coupons to template
        return "user/profile";  // Thymeleaf template, read-only profile page
    }

    // Show edit profile page
    @GetMapping("/edit")
    public String editProfilePage(Model model, Principal principal) {
        User user = profileService.getUserByEmail(principal.getName());
        model.addAttribute("user", user);
        return "user/edit-profile"; // seperate edit page
    }

    // Edit user profile (form submit, page reload) including profile image
    @PostMapping("/edit")
    public String editProfile(@ModelAttribute User user,
                              @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImage,
                              Principal principal) {

        User currentUser  = profileService.getUserByEmail(principal.getName());

        // // update profile image, name, phone
        profileService.updateUserProfile(user, profileImage);

        // if a new email was entered , send verification token
        if(user.getPendingEmail() != null && !user.getPendingEmail().isBlank()
                && !user.getPendingEmail().equals(currentUser.getEmail())) {
            emailChangeService.createEmailChangeToken(currentUser, user.getPendingEmail());
        }

        return "redirect:/user/profile";
    }

    // Save or update address via Fetch API
    @PostMapping("/address/save")
    @ResponseBody
    public ResponseEntity<UserAddress> saveAddressAjax(@RequestBody UserAddress address, Principal principal) {
        User user = profileService.getUserByEmail(principal.getName());
        address.setUser(user);
        UserAddress saved = profileService.saveAddress(address);
        return ResponseEntity.ok(saved);
    }

    // Delete address via Fetch API
    @DeleteMapping("/address/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteAddressAjax(@PathVariable Long id) {
         profileService.deleteAddress(id);
         return ResponseEntity.ok("SUCCESS");

    }


    // Cancel order via Fetch API
    @PostMapping("/order/cancel")
    @ResponseBody
    public ResponseEntity<String> cancelOrderAjax(@RequestParam String orderCode, Principal principal) {
        User user = profileService.getUserByEmail(principal.getName());
        boolean success = profileService.cancelOrder(orderCode, user.getId());
        return ResponseEntity.ok(success ? "SUCCESS" : "FAILURE");
    }

    @PostMapping("/email/change")
    public String changeEmail(@RequestParam("newEmail") String newEmail,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {

        User user = profileService.getUserByEmail(principal.getName());
        emailChangeService.createEmailChangeToken(user, newEmail);

        redirectAttributes.addFlashAttribute("success", "Verification link sent to new email!");
        return "redirect:/user/profile/edit";
    }


    // Show details of a single order
    @GetMapping("/order/{orderCode}")
    public String orderDetailsPage(@PathVariable String orderCode, Model model, Principal principal) {
        User user = profileService.getUserByEmail(principal.getName());
        OrderDTO order = profileService.getUserOrderByCode(orderCode, user.getId());
        model.addAttribute("order", order);
        return "user/order-details";  // new Thymeleaf template for or  der details
    }

    // Return order via Fetch API
    @PostMapping("/order/return")
    @ResponseBody
    public ResponseEntity<String> returnOrderAjax(
            @RequestParam String orderCode,
            @RequestParam String reason,
            Principal principal) {

        if(reason == null || reason.isBlank()) {
            return ResponseEntity.badRequest().body("Reason is required");
        }

        User user = profileService.getUserByEmail(principal.getName());
        boolean success = profileService.returnOrder(orderCode, user.getId(), reason);
        return ResponseEntity.ok(success ? "SUCCESS" : "FAILURE");
    }


    @GetMapping("/order/invoice/{orderCode}")
    public void downloadInvoice(@PathVariable String orderCode, HttpServletResponse response, Principal principal) {
        try {
            User user = profileService.getUserByEmail(principal.getName());
            ByteArrayOutputStream invoicePdf = profileService.generateInvoicePdf(orderCode, user.getId());

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=invoice_" + orderCode + ".pdf");
            response.getOutputStream().write(invoicePdf.toByteArray());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping({"/orders", "/orders/search"})
    public String ordersPage(@RequestParam(value = "q", required = false) String query,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "5") int size,
                             Model model, Principal principal) {

        User user = profileService.getUserByEmail(principal.getName());
        Page<OrderDTO> ordersPage;

        if (query != null && !query.isBlank()) {
            List<OrderDTO> filteredOrders = profileService.searchUserOrders(user.getId(), query);
            int start = Math.min(page * size, filteredOrders.size());
            int end = Math.min(start + size, filteredOrders.size());
            ordersPage = new PageImpl<>(filteredOrders.subList(start, end), PageRequest.of(page, size), filteredOrders.size());
            model.addAttribute("searchQuery", query);
        } else {
            ordersPage = profileService.getUserOrdersPaginated(user.getId(), page, size);
        }

        model.addAttribute("ordersPage", ordersPage);
        return "user/orders";
    }


}



