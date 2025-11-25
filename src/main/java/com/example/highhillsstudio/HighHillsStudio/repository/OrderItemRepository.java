package com.example.highhillsstudio.HighHillsStudio.repository;

import com.example.highhillsstudio.HighHillsStudio.entity.Fit;
import com.example.highhillsstudio.HighHillsStudio.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    boolean existsByFit(Fit fit);

    @Query(value = "SELECT p.name AS name, SUM(oi.quantity) AS total_sold " +
            "FROM order_items oi " +
            "JOIN products p ON oi.product_id = p.id " +
            "GROUP BY p.name " +
            "ORDER BY total_sold DESC " +
            "LIMIT 10", nativeQuery = true)
    List<Object[]> findTop10BestSellingProducts();

    @Query(value = "SELECT c.name AS name, SUM(oi.quantity) AS total_sold " +
            "FROM order_items oi " +
            "JOIN products p ON oi.product_id = p.id " +
            "JOIN categories c ON p.category_id = c.id " +
            "GROUP BY c.name " +
            "ORDER BY total_sold DESC " +
            "LIMIT 10", nativeQuery = true)
    List<Object[]> findTop10BestSellingCategories();


}
