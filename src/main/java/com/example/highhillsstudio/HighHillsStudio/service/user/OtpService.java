package com.example.highhillsstudio.HighHillsStudio.service.user;

import com.example.highhillsstudio.HighHillsStudio.entity.Otp;
import com.example.highhillsstudio.HighHillsStudio.entity.User;

import com.example.highhillsstudio.HighHillsStudio.repository.OtpRepository;
import com.example.highhillsstudio.HighHillsStudio.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final OtpEmailService otpEmailService;




    public Otp createOtpForUser(User user) {
        String otpCode = OtpGenerator.generateOtp();

        Otp otp = Otp.builder()
                .code(otpCode)
                .expiryTime(OtpGenerator.getExpiryTime())
                .used(false)
                .user(user)
                .build();

        otpRepository.save(otp);

        // Send OTP email
        otpEmailService.sendOtpEmail(user.getEmail(), otpCode);

        return otp;
    }





    // validate OTP
    public boolean validateOtp(User user, String otpCode) {
        Otp otp = otpRepository
                .findTopByUserAndCodeAndUsedFalseOrderByExpiryTimeDesc(user, otpCode)
                .orElse(null);

        if (otp == null) return false; // OTP not found
        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) return false; // expired

        // Mark as used
        otp.setUsed(true);
        otpRepository.save(otp);

        return true;
    }

    public void sendOtpEmail(User user, String otpCode) {
        // implement actual email sending logic here

        System.out.println("Sending OTP " + otpCode + " to email " + user.getEmail());
    }



    // Resend OTP
    public void resendOtp(User user) {

        // Optional: Disable old OTPs
        otpRepository.findTopByUserOrderByExpiryTimeDesc(user)
                .ifPresent(old -> {
                    old.setUsed(true);
                    otpRepository.save(old);
                });

        // Create & send new OTP
        createOtpForUser(user);
    }

}
