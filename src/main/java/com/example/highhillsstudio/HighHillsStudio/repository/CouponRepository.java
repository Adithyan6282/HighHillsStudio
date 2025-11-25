package com.example.highhillsstudio.HighHillsStudio.repository;

import com.example.highhillsstudio.HighHillsStudio.entity.Coupon;
import com.example.highhillsstudio.HighHillsStudio.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeAndActiveTrue(String code);

    boolean existsByCode(String code);
    Optional<Coupon> findByCode(String code);

    List<Coupon> findByUser(User user);

    // coupon to show in user side
    List<Coupon> findByUserAndActiveTrue(User user);


}
