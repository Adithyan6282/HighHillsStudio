package com.example.highhillsstudio.HighHillsStudio.repository;

import com.example.highhillsstudio.HighHillsStudio.entity.UserGender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserGenderRepository extends JpaRepository<UserGender, Long> {

    // Find gender by name
    Optional<UserGender> findByName(String name);
}
