package com.example.highhillsstudio.HighHillsStudio.repository;

import com.example.highhillsstudio.HighHillsStudio.entity.Otp;
import com.example.highhillsstudio.HighHillsStudio.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {


    // Get the latest unused OTP for a user by code
    Optional<Otp> findTopByUserAndCodeAndUsedFalseOrderByExpiryTimeDesc(User user, String code);

    // Optional: get the latest OTP regardless of code
    Optional<Otp> findTopByUserOrderByExpiryTimeDesc(User user);


}
