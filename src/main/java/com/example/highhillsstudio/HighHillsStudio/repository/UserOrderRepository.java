package com.example.highhillsstudio.HighHillsStudio.repository;
import com.example.highhillsstudio.HighHillsStudio.entity.OrderStatus;
import org.springframework.data.domain.Page;

import com.example.highhillsstudio.HighHillsStudio.entity.UserOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserOrderRepository extends JpaRepository<UserOrder, Long> {

    List<UserOrder> findByUserIdOrderByPlacedAtDesc(Long userId);

//    UserOrder findByOrderCode(String ordercode);

    Optional<UserOrder> findByOrderCode(String orderCode);

    Optional<UserOrder> findByOrderCodeAndUserId(String orderCode, Long userId);

    List<UserOrder> findAllByOrderByPlacedAtDesc(); // Orders by date desc

    // Search by order code, username, email
    Page<UserOrder> findByOrderCodeContainingIgnoreCaseOrUserFullNameContainingIgnoreCaseOrUserEmailContainingIgnoreCase(
            String orderCode, String fullName, String email, Pageable pageable
    );


    // Filter by status
    Page<UserOrder> findByStatus(OrderStatus status, Pageable pageable);

    // Search + filter combined
    @Query("SELECT o FROM UserOrder o WHERE " +
            "(LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(o.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(o.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR o.status = :status)")
    Page<UserOrder> findByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") OrderStatus status, Pageable pageable);


    // Search only
    @Query("SELECT o FROM UserOrder o " +
            "WHERE LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(o.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(o.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<UserOrder> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);





    // sales report

    @Query("SELECT u FROM UserOrder u WHERE u.placedAt BETWEEN :start AND :end AND u.status <> 'CANCELED' ORDER BY u.placedAt DESC")
    List<UserOrder> findOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(u) FROM UserOrder u WHERE u.placedAt BETWEEN :start AND :end AND u.status <> 'CANCELED'")
    long countOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(u.totalAmount),0) FROM UserOrder u WHERE u.placedAt BETWEEN :start AND :end AND u.status <> 'CANCELED'")
    BigDecimal sumTotalAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(u.offerDiscount),0) FROM UserOrder u WHERE u.placedAt BETWEEN :start AND :end AND u.status <> 'CANCELED'")
    BigDecimal sumOfferDiscountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(u.couponDiscount),0) FROM UserOrder u WHERE u.placedAt BETWEEN :start AND :end AND u.status <> 'CANCELED'")
    BigDecimal sumCouponDiscountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(u.finalAmount),0) FROM UserOrder u WHERE u.placedAt BETWEEN :start AND :end AND u.status <> 'CANCELED'")
    BigDecimal sumFinalAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);



    // Monthly Sales (PostgreSQL + LocalDate)
    @Query(value = "SELECT TO_CHAR(u.placed_at, 'YYYY-MM') AS period, SUM(u.total_amount) AS total_sales " +
            "FROM user_orders u " +
            "WHERE u.placed_at BETWEEN :startDate AND :endDate " +
            "GROUP BY period " +
            "ORDER BY period", nativeQuery = true)
    List<Object[]> findMonthlySales(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    // Yearly Sales
    @Query(value = "SELECT EXTRACT(YEAR FROM u.placed_at) AS year, SUM(u.total_amount) AS total_sales " +
            "FROM user_orders u " +
            "GROUP BY year " +
            "ORDER BY year", nativeQuery = true)
    List<Object[]> findYearlySales();



    @Query("SELECT o FROM UserOrder o WHERE o.placedAt BETWEEN :start AND :end ORDER BY o.placedAt DESC")
    Page<UserOrder> findOrdersBetween(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end,
                                      Pageable pageable);


}

