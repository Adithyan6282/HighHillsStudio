package com.example.highhillsstudio.HighHillsStudio.service.admin;

import com.example.highhillsstudio.HighHillsStudio.dto.admin.CouponRequest;
import com.example.highhillsstudio.HighHillsStudio.entity.Coupon;
import com.example.highhillsstudio.HighHillsStudio.repository.CouponRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;


    @Transactional
    public Coupon createCoupon(CouponRequest request) {
        if (request.getExpiryDate() != null && request.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Expiry date cannot be in the past");
        }

        if (couponRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Coupon code already exists");
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode())
                .discountAmount(request.getDiscountAmount())
                .expiryDate(request.getExpiryDate())
                .active(true)
                .build();

        return couponRepository.save(coupon);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new IllegalArgumentException("Coupon not found");
        }
        couponRepository.deleteById(id);
    }

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }



}
