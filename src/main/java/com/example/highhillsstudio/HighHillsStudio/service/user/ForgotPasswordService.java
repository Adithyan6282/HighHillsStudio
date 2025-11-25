package com.example.highhillsstudio.HighHillsStudio.service.user;

import com.example.highhillsstudio.HighHillsStudio.entity.User;
import com.example.highhillsstudio.HighHillsStudio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base-url}")
    private String baseUrl;



    //Generate reset token and send email

    public void createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordExpiry(LocalDateTime.now().plusHours(1)); // token valid for 1 hour
        userRepository.save(user);

//        String resetLink = "http://localhost:8080/users/reset-password?token=" + token;
        String resetLink = baseUrl + "/users/reset-password?token=" + token;

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("Password Reset Request");
        mailMessage.setText("Click the link to reset your password: " + resetLink +
                "\nThis link expires in 1 hour.");
        javaMailSender.send(mailMessage);

    }

    // Reset password using token

    public void resetPassword(String token, String newPassword) {
        User user  = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

//        if(user.getResetPasswordExpiry().isBefore(LocalDateTime.now())) {
//            throw new IllegalArgumentException("Token has expired");
//        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpiry(null);
        userRepository.save(user);
    }



}
