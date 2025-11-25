package com.example.highhillsstudio.HighHillsStudio.controller.user;

import com.example.highhillsstudio.HighHillsStudio.config.SecurityConfig;
import com.example.highhillsstudio.HighHillsStudio.entity.User;
import com.example.highhillsstudio.HighHillsStudio.repository.UserRepository;
import com.example.highhillsstudio.HighHillsStudio.security.CustomUserDetailsService;
import com.example.highhillsstudio.HighHillsStudio.service.user.EmailChangeService;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.jaas.SecurityContextLoginModule;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class EmailChangeController {


    private final EmailChangeService emailChangeService;

    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;



    @GetMapping("/user/profile/verify-email")
    public String verifyEmail(@RequestParam("token") String token,
                              RedirectAttributes redirectAttributes) {

        try {
            // 1️⃣ Update email in DB and get updated user
            User updatedUser = emailChangeService.confirmEmailChange(token);

            // 2️⃣ Update current Spring Security session
            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

            if (currentAuth != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(updatedUser.getEmail());

                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        currentAuth.getCredentials(),
                        userDetails.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }

            redirectAttributes.addFlashAttribute("success", "Email updated successfully!");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/user/profile/edit"; // stay on profile page
    }


}
