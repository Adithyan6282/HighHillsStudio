package com.example.highhillsstudio.HighHillsStudio.repository;

import com.example.highhillsstudio.HighHillsStudio.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

}
