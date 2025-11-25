// src/main/java/com/example/highhillsstudio/HighHillsStudio/service/admin/SalesReportService.java
package com.example.highhillsstudio.HighHillsStudio.service.admin;

import com.example.highhillsstudio.HighHillsStudio.dto.admin.SalesReportDto;
import com.example.highhillsstudio.HighHillsStudio.entity.UserOrder;
import com.example.highhillsstudio.HighHillsStudio.repository.UserOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesReportService {

    private final UserOrderRepository orderRepo;

    public SalesReportDto generateReport(LocalDateTime start, LocalDateTime end) {
        List<UserOrder> orders = orderRepo.findOrdersBetween(start, end);
        long totalOrders = orderRepo.countOrdersBetween(start, end);
        BigDecimal totalOrderAmount = orderRepo.sumTotalAmountBetween(start, end);
        BigDecimal totalOfferDiscount = orderRepo.sumOfferDiscountBetween(start, end);
        BigDecimal totalCouponDiscount = orderRepo.sumCouponDiscountBetween(start, end);
        BigDecimal totalFinalRevenue = orderRepo.sumFinalAmountBetween(start, end);

        if (totalOrderAmount == null) totalOrderAmount = BigDecimal.ZERO;
        if (totalOfferDiscount == null) totalOfferDiscount = BigDecimal.ZERO;
        if (totalCouponDiscount == null) totalCouponDiscount = BigDecimal.ZERO;
        if (totalFinalRevenue == null) totalFinalRevenue = BigDecimal.ZERO;

        return SalesReportDto.builder()
                .totalOrders(totalOrders)
                .totalOrderAmount(totalOrderAmount)
                .totalOfferDiscount(totalOfferDiscount)
                .totalCouponDiscount(totalCouponDiscount)
                .totalFinalRevenue(totalFinalRevenue)
                .start(start)
                .end(end)
                .orders(orders)
                .build();
    }





    // New method
    public SalesReportDto generateReportPaginated(LocalDateTime start, LocalDateTime end, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserOrder> ordersPage = orderRepo.findOrdersBetween(start, end, pageable);

        long totalOrders = orderRepo.countOrdersBetween(start, end);
        BigDecimal totalOrderAmount = orderRepo.sumTotalAmountBetween(start, end);
        BigDecimal totalOfferDiscount = orderRepo.sumOfferDiscountBetween(start, end);
        BigDecimal totalCouponDiscount = orderRepo.sumCouponDiscountBetween(start, end);
        BigDecimal totalFinalRevenue = orderRepo.sumFinalAmountBetween(start, end);

        if (totalOrderAmount == null) totalOrderAmount = BigDecimal.ZERO;
        if (totalOfferDiscount == null) totalOfferDiscount = BigDecimal.ZERO;
        if (totalCouponDiscount == null) totalCouponDiscount = BigDecimal.ZERO;
        if (totalFinalRevenue == null) totalFinalRevenue = BigDecimal.ZERO;

        return SalesReportDto.builder()
                .totalOrders(totalOrders)
                .totalOrderAmount(totalOrderAmount)
                .totalOfferDiscount(totalOfferDiscount)
                .totalCouponDiscount(totalCouponDiscount)
                .totalFinalRevenue(totalFinalRevenue)
                .start(start)
                .end(end)
                .orders(ordersPage.getContent()) // only current page content
                .currentPage(ordersPage.getNumber())
                .totalPages(ordersPage.getTotalPages())
                .pageSize(ordersPage.getSize())
                .build();
    }



    public LocalDateTime[] computeRange(String filter, LocalDate customStart, LocalDate customEnd) {
        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();
        if (filter == null) filter = "daily";

        switch (filter.toLowerCase()) {
            case "daily":
                start = LocalDate.now().atStartOfDay();
                end = LocalDate.now().atTime(LocalTime.MAX);
                break;
            case "weekly":
                start = LocalDate.now().minusDays(6).atStartOfDay(); // last 7 days inclusive
                end = LocalDate.now().atTime(LocalTime.MAX);
                break;
            case "monthly":
                start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
                end = LocalDate.now().atTime(LocalTime.MAX);
                break;
            case "yearly":
                start = LocalDate.now().withDayOfYear(1).atStartOfDay();
                end = LocalDate.now().atTime(LocalTime.MAX);
                break;
            case "custom":
                if (customStart == null) customStart = LocalDate.now();
                if (customEnd == null) customEnd = LocalDate.now();
                start = customStart.atStartOfDay();
                end = customEnd.atTime(LocalTime.MAX);
                break;
            default:
                start = LocalDate.now().atStartOfDay();
                end = LocalDate.now().atTime(LocalTime.MAX);
        }
        return new LocalDateTime[]{start, end};
    }
}
