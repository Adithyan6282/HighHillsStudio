package com.example.highhillsstudio.HighHillsStudio.service.admin;

import com.example.highhillsstudio.HighHillsStudio.dto.user.OrderDTO;
import com.example.highhillsstudio.HighHillsStudio.dto.user.OrderItemDTO;
import com.example.highhillsstudio.HighHillsStudio.entity.OrderStatus;
import com.example.highhillsstudio.HighHillsStudio.entity.UserOrder;
import com.example.highhillsstudio.HighHillsStudio.repository.UserOrderRepository;
import com.example.highhillsstudio.HighHillsStudio.service.user.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final UserOrderRepository orderRepository;
    private final WalletService walletService;

    public Page<OrderDTO> getOrders(String keyword, String statusFilter, int page, int size, String sortField, String sortDir) {
        // Default sorting
        Sort sort = Sort.by(sortField != null ? sortField : "placedAt");
        sort = "desc".equalsIgnoreCase(sortDir) ? sort.descending() : sort.ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserOrder> ordersPage;


        if ((keyword != null && !keyword.isEmpty()) && (statusFilter != null && !statusFilter.isEmpty())) {
            ordersPage = orderRepository.findByKeywordAndStatus(keyword, OrderStatus.valueOf(statusFilter), pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            ordersPage = orderRepository.searchByKeyword(keyword, pageable);
        } else if (statusFilter != null && !statusFilter.isEmpty()) {
            ordersPage = orderRepository.findByStatus(OrderStatus.valueOf(statusFilter), pageable);
        } else {
            ordersPage = orderRepository.findAll(pageable);
        }

        return ordersPage.map(order -> {
            List<OrderItemDTO> items = order.getItems().stream()
                    .map(item -> OrderItemDTO.builder()
                            .id(item.getId())
                            .productName(item.getProductName())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .cancellable(order.getStatus() == OrderStatus.PLACED)
                            .build())
                    .collect(Collectors.toList());

            return OrderDTO.builder()
                    .orderCode(order.getOrderCode())
                    .status(order.getStatus().name())
                    .totalAmount(order.getTotalAmount())
                    .userName(order.getUser().getFullName())
                    .userEmail(order.getUser().getEmail())
                    .placedAt(order.getPlacedAt())
                    .items(items)
                    .build();
        });
    }



    public void updateOrderStatus(String orderCode, OrderStatus newStatus) {
        System.out.println("🟢 updateOrderStatus called for orderCode = " + orderCode + ", newStatus = " + newStatus);

        // 1️⃣ Fetch order
        UserOrder order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        orderRepository.save(order);

        System.out.println("🔄 Order status changed from " + oldStatus + " ➜ " + newStatus);
        System.out.println("👤 User: " + order.getUser().getEmail() + " | Amount: " + order.getTotalAmount());

        // 2️⃣ Auto refund logic
        if (newStatus == OrderStatus.CANCELED) {
            System.out.println("💰 Refund triggered for CANCELED order.");
            walletService.refundForCanceledOrder(order.getUser(), order.getTotalAmount());

        } else if (newStatus == OrderStatus.RETURN_CONFIRMED) {
            System.out.println("💰 Refund triggered for RETURN_CONFIRMED order.");
            walletService.refundForReturnedOrder(order.getUser(), order.getTotalAmount());
        }

        System.out.println("✅ updateOrderStatus completed for " + orderCode);
    }



}

