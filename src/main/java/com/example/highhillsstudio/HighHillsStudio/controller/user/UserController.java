package com.example.highhillsstudio.HighHillsStudio.controller.user;

import com.example.highhillsstudio.HighHillsStudio.dto.response.ApiResponse;
import com.example.highhillsstudio.HighHillsStudio.dto.user.OtpDTO;
import com.example.highhillsstudio.HighHillsStudio.dto.user.UserLoginDto;
import com.example.highhillsstudio.HighHillsStudio.dto.user.UserResponseDto;
import com.example.highhillsstudio.HighHillsStudio.dto.user.UserSignupDto;
import com.example.highhillsstudio.HighHillsStudio.entity.User;
import com.example.highhillsstudio.HighHillsStudio.service.user.OtpService;
import com.example.highhillsstudio.HighHillsStudio.service.user.UsersService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UsersService usersService;
    private final OtpService otpService;


    // ---------------- LOGIN PAGE ----------------
    @GetMapping("/login")
    public String showLoginPage() {
        return "user/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute @Validated UserLoginDto dto,
                        Model model,
                        HttpSession session) {

        try {
            User user = usersService.login(dto);

            // Save email in session to know user is logged in
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("message", "Login successful! Welcome " + user.getFullName());

            return "redirect:/users/home";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "user/login";
        }
    }





    @GetMapping("/signup")
    public String showSignupPage(@RequestParam(required = false) String ref, Model model) {
        model.addAttribute("ref", ref);  // save token to HTML page
        return "user/signup";
    }





    @PostMapping("/signup")
    public String signup(@ModelAttribute @Validated UserSignupDto dto,
                         @RequestParam(required = false) String ref, // <-- referral token
                         Model model) {
        try {
            User user = usersService.signup(dto, ref); // pass referral token
            return "redirect:/users/verify-otp?email=" + user.getEmail();
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "user/signup";
        }
    }







    @GetMapping("/verify-otp")      // test
    public String showVerifyOtpPage(@RequestParam String email, Model model) {
        // pre-fill dto with email
        model.addAttribute("otpDTO", new OtpDTO(email, null));
        return "user/verify-otp";
    }






    @PostMapping("/verify-otp")
    public String verifyOtp(@ModelAttribute OtpDTO dto, Model model) {
        if (dto.getOtpCode() == null || dto.getOtpCode().isBlank()) {
            model.addAttribute("error", "OTP code is required");
            model.addAttribute("otpDTO", dto);
            return "user/verify-otp";
        }

        boolean verified = usersService.verifyOtp(dto);
        if (verified) {
            model.addAttribute("message", "Account verified! Please login.");
            return "redirect:/users/login";
        }

        model.addAttribute("error", "Invalid or expired OTP");
        model.addAttribute("otpDTO", dto);
        return "user/verify-otp";
    }





    @PostMapping("/resend-otp")
    public String resendOtp(@RequestParam String email, Model model) {

        usersService.findByEmail(email).ifPresent(user -> {
            otpService.resendOtp(user); // FIXED: call OtpService
        });

        model.addAttribute("message", "A new OTP has been sent to " + email);
        model.addAttribute("otpDTO", new OtpDTO(email, null));

        return "user/verify-otp";
    }



}

