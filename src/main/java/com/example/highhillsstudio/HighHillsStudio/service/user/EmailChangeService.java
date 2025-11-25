package com.example.highhillsstudio.HighHillsStudio.service.user;

import com.example.highhillsstudio.HighHillsStudio.entity.User;
import com.example.highhillsstudio.HighHillsStudio.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailChangeService {

    private final UserRepository userRepository;
    private final JavaMailSender javaMailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @PersistenceContext
    private EntityManager entityManager;


    public void createEmailChangeToken(User user, String newEmail) {
        String token = UUID.randomUUID().toString();
        user.setEmailChangeToken(token);
        user.setEmailChangeExpiry(LocalDateTime.now().plusHours(1));
        user.setPendingEmail(newEmail); // temporarily store new email
        userRepository.saveAndFlush(user);

//        String verificationLink = "http://localhost:8080/user/profile/verify-email?token=" + token;

        String verificationLink = baseUrl + "/user/profile/verify-email?token=" + token;


        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newEmail); // send Link to new email
        mailMessage.setSubject("Email Change Verification");
        mailMessage.setText("Click the link to verify your new email: " + verificationLink +
                "\nThis link expires in 1 hour.");
//        javaMailSender.send(mailMessage);
        // <-- Add try-catch around the mail sending
        try {
            javaMailSender.send(mailMessage);
            System.out.println("Verification email sent to: " + newEmail);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }

    }




    @Transactional
    public User confirmEmailChange(String token) {
        User user = userRepository.findByEmailChangeToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        if (user.getEmailChangeExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token has expired");
        }

        user.setEmail(user.getPendingEmail());
        user.setPendingEmail(null);
        user.setEmailChangeToken(null);
        user.setEmailChangeExpiry(null);

        userRepository.saveAndFlush(user);

        System.out.println("✅ Email updated to: " + user.getEmail());

        return user; // <-- return updated user
    }

}


