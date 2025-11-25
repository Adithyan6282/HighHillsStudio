package com.example.highhillsstudio.HighHillsStudio.service.admin;

import com.example.highhillsstudio.HighHillsStudio.repository.OrderItemRepository;
import com.example.highhillsstudio.HighHillsStudio.repository.UserOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {



    private final OrderItemRepository orderItemRepo;

    private final UserOrderRepository userOrderRepo;

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE; // "yyyy-MM-dd"

    // Top 10 Products
//    public List<Map<String, Object>> getTopProducts() {
//        return orderItemRepo.findTop10BestSellingProducts().stream()
//                .map(obj -> Map.of("name", obj[0], "totalSold", obj[1]))
//                .toList();
//    }





    // Top Products with pagination
    public Page<Map<String, Object>> getTopProducts(Pageable pageable) {
        List<Map<String, Object>> topProducts = orderItemRepo.findTop10BestSellingProducts().stream()
                .map(obj -> Map.of("name", obj[0], "totalSold", obj[1]))
                .toList();

        int start = Math.min((int) pageable.getOffset(), topProducts.size());
        int end = Math.min((start + pageable.getPageSize()), topProducts.size());

        return new PageImpl<>(topProducts.subList(start, end), pageable, topProducts.size());
    }




    // Top Categories with pagination
    public Page<Map<String, Object>> getTopCategories(Pageable pageable) {
        List<Map<String, Object>> topCategories = orderItemRepo.findTop10BestSellingCategories().stream()
                .map(obj -> Map.of("name", obj[0], "totalSold", obj[1]))
                .toList();

        int start = Math.min((int) pageable.getOffset(), topCategories.size());
        int end = Math.min((start + pageable.getPageSize()), topCategories.size());

        return new PageImpl<>(topCategories.subList(start, end), pageable, topCategories.size());
    }

    // Top 10 Categories
//    public List<Map<String, Object>> getTopCategories() {
//        return orderItemRepo.findTop10BestSellingCategories().stream()
//                .map(obj -> Map.of("name", obj[0], "totalSold", obj[1]))
//                .toList();
//    }

    // Sales Chart
    public List<Map<String, Object>> getSalesChart(String filter, String startDateStr, String endDateStr) {

        List<Object[]> result;

        if ("monthly".equalsIgnoreCase(filter)) {

            // Handle optional dates
            LocalDate startDate = startDateStr != null ? LocalDate.parse(startDateStr, formatter) : LocalDate.now().minusMonths(1);
            LocalDate endDate = endDateStr != null ? LocalDate.parse(endDateStr, formatter) : LocalDate.now();

            result = userOrderRepo.findMonthlySales(startDate, endDate);

        } else {
            // Yearly sales (no date filter)
            result = userOrderRepo.findYearlySales();
        }

        return result.stream()
                .map(obj -> Map.of("period", obj[0], "totalSales", obj[1]))
                .toList();
    }

}
