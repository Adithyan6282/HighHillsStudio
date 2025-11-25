package com.example.highhillsstudio.HighHillsStudio.repository;

import com.example.highhillsstudio.HighHillsStudio.entity.User;
import com.example.highhillsstudio.HighHillsStudio.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {


    // Fetch all addresses for a user
    List<UserAddress> findByUser(User user);

    List<UserAddress> findByUserId(Long userId);








    boolean existsByLine1AndLine2AndCityAndStateAndCountryAndPincodeAndPhone(
            String line1,
            String line2,
            String city,
            String state,
            String country,
            String pincode,
            String phone
    );

    boolean existsByLine1AndLine2AndPhone(String line1, String line2, String phone);

    boolean existsByUserIdAndLine1AndLine2AndPhone(
            Long userId,
            String line1,
            String line2,
            String phone
    );




}




