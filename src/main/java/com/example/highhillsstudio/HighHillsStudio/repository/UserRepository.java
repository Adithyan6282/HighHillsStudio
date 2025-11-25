package com.example.highhillsstudio.HighHillsStudio.repository;
import com.example.highhillsstudio.HighHillsStudio.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String fullName, String email, Pageable pageable);

    Optional<User> findByEmail(String email);

    Optional<User> findByResetPasswordToken(String token);  // test


    boolean existsByEmail(String email);

    Optional<User> findByEmailChangeToken(String token);







}
