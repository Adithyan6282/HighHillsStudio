package com.example.highhillsstudio.HighHillsStudio.controller.user;

import com.example.highhillsstudio.HighHillsStudio.service.user.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;



    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("pageTitle", "Change Password");
            model.addAttribute("email", principal.getName());  // autofill logged in user's email
        } else {
            model.addAttribute("pageTitle", "Forgot Password");
        }
        return "user/forgot-password";
    }




    // Handle forgot password submission
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try{
            forgotPasswordService.createPasswordResetToken(email);
            redirectAttributes.addFlashAttribute("message", "Password reset link sent to your email");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users/forgot-password";

    }

    // Show reset password form
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("token", token);
        return "user/reset-password";
    }



    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam String token,
            @RequestParam String password,
            RedirectAttributes redirectAttributes,
            Principal principal) {
        try {
            forgotPasswordService.resetPassword(token, password);
            redirectAttributes.addFlashAttribute("message", "Password successfully reset.");
            if (principal != null) {
                return "redirect:/users/profile";
            } else {
                return "redirect:/users/login";
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/reset-password?token=" + token;
        }
    }



}
