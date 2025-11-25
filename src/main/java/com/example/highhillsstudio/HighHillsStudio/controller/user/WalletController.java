package com.example.highhillsstudio.HighHillsStudio.controller.user;

import com.example.highhillsstudio.HighHillsStudio.entity.User;
import com.example.highhillsstudio.HighHillsStudio.entity.WalletTransaction;
import com.example.highhillsstudio.HighHillsStudio.service.user.UsersService;
import com.example.highhillsstudio.HighHillsStudio.service.user.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final UsersService usersService;




    @GetMapping
    public String viewWalletPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = usersService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Get wallet balance and transactions
        BigDecimal balance = walletService.getWalletBalance(user);
        List<WalletTransaction> transactions = walletService.getTransactionsByUser(user);

        model.addAttribute("walletBalance", balance);
        model.addAttribute("walletTransactions", transactions);
        model.addAttribute("user", user);

        return "user/wallet"; // Create this Thymeleaf page
    }




    @GetMapping("/balance")
    @ResponseBody
    public BigDecimal getWalletBalance(@AuthenticationPrincipal UserDetails userDetails) {
        // Fetch the real user from database using their email/username
        User user = usersService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return walletService.getWalletBalance(user);
    }
    @GetMapping("/refresh")
    @ResponseBody
    public Map<String, Object> refreshWallet(@AuthenticationPrincipal UserDetails userDetails) {
        User user = usersService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BigDecimal balance = walletService.getWalletBalance(user);
        List<WalletTransaction> transactions = walletService.getTransactionsByUser(user);

        Map<String, Object> response = new HashMap<>();
        response.put("balance", balance);
        response.put("transactions", transactions);
        return response;
    }

}
