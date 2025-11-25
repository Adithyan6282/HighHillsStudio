package com.example.highhillsstudio.HighHillsStudio.service.admin;

import com.example.highhillsstudio.HighHillsStudio.entity.Admin;
import com.example.highhillsstudio.HighHillsStudio.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
//    private final String adminUsername = "admin";
//    private final String adminPassword = "admin123";
//
//    public boolean authenticate(String username, String password) {
//        return adminUsername.equals(username) && adminPassword.equals(password);
//    }

    private final AdminRepository adminRepository;
//    private final PasswordEncoder passwordEncoder;

    // Find admin by username
    public Optional<Admin> findByUsername(String username) {
        return adminRepository.findByUsername(username);
    }

    // create  a new admin

//    public Admin createAdmin(String username, String rawpassword) {
//        Admin admin = new Admin();
//        admin.setUsername(username);
//        admin.setPassword(passwordEncoder.encode(rawpassword));
//        return adminRepository.save(admin);
//    }

    // Change admin password
//    public void changePassword(Long adminId, String newPassword) {
//        adminRepository.findById(adminId).ifPresent(admin -> {
//            admin.setPassword(passwordEncoder.encode(newPassword));
//            adminRepository.save(admin);
//        });
//    }




}
