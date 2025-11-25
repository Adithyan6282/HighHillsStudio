package com.example.highhillsstudio.HighHillsStudio.service.user;

import com.example.highhillsstudio.HighHillsStudio.dto.user.OtpDTO;
import com.example.highhillsstudio.HighHillsStudio.dto.user.UserLoginDto;
import com.example.highhillsstudio.HighHillsStudio.dto.user.UserSignupDto;
import com.example.highhillsstudio.HighHillsStudio.entity.*;
import com.example.highhillsstudio.HighHillsStudio.repository.CouponRepository;
import com.example.highhillsstudio.HighHillsStudio.repository.ReferralRepository;
import com.example.highhillsstudio.HighHillsStudio.repository.UserGenderRepository;
import com.example.highhillsstudio.HighHillsStudio.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder; // for hashing passwords
    private final UserGenderRepository userGenderRepository;
    private final ReferralRepository referralRepository;
    private final CouponRepository couponRepository;




    public User signup(UserSignupDto dto, String refToken) {
        if(userRepository.existsByEmail(dto.getEmail())){
            throw new IllegalArgumentException("Email already registered");
        }

        UserGender gender = null;
        if(dto.getGender() != null) {
            gender = userGenderRepository.findByName(dto.getGender())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid gender"));
        }

        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .phone(dto.getPhone())
                .userGender(gender)
                .enabled(false)
                .build();

        User savedUser = userRepository.save(user);

        // Handle referral token
        if(refToken != null && !refToken.isEmpty()){
            redeemReferral(refToken, savedUser);
        }

        // Generate OTP
        var otp  = otpService.createOtpForUser(savedUser);
        otpService.sendOtpEmail(savedUser, otp.getCode());

        return savedUser;
    }





    // -------------------------
    // Verify OTP and enable user
    // -------------------------
    @Transactional
    public boolean verifyOtp(OtpDTO otpDto) {
        User user = userRepository.findByEmail(otpDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean valid = otpService.validateOtp(user, otpDto.getOtpCode());
        if(valid){
            user.setEnabled(true);
            userRepository.save(user);
        }

        return valid;
    }

    // -------------------------
    // Login: validate credentials
    // -------------------------
    public User login(UserLoginDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

//        if(!user.getEnabled()){
//            throw new IllegalStateException("User not verified. Please verify OTP first.");
//        }

        // Check if user is enabled (OTP verified) and not blocked
        if (!user.isEnabled()) {
            throw new IllegalStateException("User not verified. Please verify by Sign up and OTP first.");
        }

        if (user.isBlocked()) {
            throw new IllegalStateException("User is blocked. Contact support.");
        }

        if(!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("Invalid email or password");
        }

        return user;
    }

    // -------------------------
    // Find user by email
    // -------------------------
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Resend Otp
    public void resendOtp(User user) {

        Otp otp = otpService.createOtpForUser(user);
        otpService.sendOtpEmail(user, otp.getCode());
    }




    // Generate referral token for existing user
    public String generateReferralToken(User inviter) {
        String token = UUID.randomUUID().toString();
        Referral referral = Referral.builder()
                .token(token)
                .inviter(inviter)
                .used(false)
                .build();
        referralRepository.save(referral);
        return token;
    }

    // Redeem referral token when a new user registers
    public void redeemReferral(String token, User newUser) {
        referralRepository.findByToken(token).ifPresent(referral -> {
            if (!referral.isUsed()) {
                referral.setInvitee(newUser);
                referral.setUsed(true);
                referralRepository.save(referral);

                // Generate coupon for inviter
                Coupon coupon = Coupon.builder()
                        .code("REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                        .discountAmount(BigDecimal.valueOf(100)) // ₹100 off
                        .active(true)
                        .user(referral.getInviter())
                        .build();
                couponRepository.save(coupon);
            }
        });
    }




    public List<Coupon> getActiveCouponsForUser(User user) {
        return couponRepository.findByUserAndActiveTrue(user);
    }






}
