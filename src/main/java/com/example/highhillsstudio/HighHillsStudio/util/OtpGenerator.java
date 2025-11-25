package com.example.highhillsstudio.HighHillsStudio.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;

public class OtpGenerator {

    private static final SecureRandom random = new SecureRandom();

    // Generate a 6 digit Otp
    public static String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Expiry 5 min from now
    public static LocalDateTime getExpiryTime() {
        return LocalDateTime.now().plusMinutes(5);
    }
}
