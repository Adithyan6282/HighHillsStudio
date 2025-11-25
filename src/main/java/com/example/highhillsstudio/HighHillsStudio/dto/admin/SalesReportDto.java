// src/main/java/com/example/highhillsstudio/HighHillsStudio/dto/admin/SalesReportDto.java
package com.example.highhillsstudio.HighHillsStudio.dto.admin;

import com.example.highhillsstudio.HighHillsStudio.entity.UserOrder;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReportDto {

    private long totalOrders;
    private BigDecimal totalOrderAmount;   // totalAmount sum
    private BigDecimal totalOfferDiscount;
    private BigDecimal totalCouponDiscount;
    private BigDecimal totalFinalRevenue;  // sum(finalAmount)
    private LocalDateTime start;
    private LocalDateTime end;



    private List<UserOrder> orders;


    // Pagination fields
    private int currentPage;   // zero-based
    private int totalPages;
    private int pageSize;

}
