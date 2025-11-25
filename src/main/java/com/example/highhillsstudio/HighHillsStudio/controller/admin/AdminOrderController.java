package com.example.highhillsstudio.HighHillsStudio.controller.admin;

import com.example.highhillsstudio.HighHillsStudio.entity.OrderStatus;
import com.example.highhillsstudio.HighHillsStudio.service.admin.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final AdminOrderService orderService;



    @GetMapping
    public String listOrders(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "statusFilter", required = false) String statusFilter,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortField", defaultValue = "placedAt") String sortField,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            Model model) {

        var ordersPage = orderService.getOrders(keyword, statusFilter, page, size, sortField, sortDir);

        model.addAttribute("orders", ordersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordersPage.getTotalPages());
        model.addAttribute("totalItems", ordersPage.getTotalElements());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", "asc".equalsIgnoreCase(sortDir) ? "desc" : "asc");
        model.addAttribute("keyword", keyword);
        model.addAttribute("statusFilter", statusFilter);

        return "admin/orders";
    }



    @PostMapping("/update-status")
    @ResponseBody
    public ResponseEntity<String> updateOrderStatus(
            @RequestParam("orderCode") String orderCode,
            @RequestParam("status") String status) {
        try {
            System.out.println("🟢 Received: orderCode=" + orderCode + ", status=" + status);
            OrderStatus newStatus = OrderStatus.valueOf(status.trim().toUpperCase());
            orderService.updateOrderStatus(orderCode, newStatus);
            return ResponseEntity.ok("Status updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to update status: " + e.getMessage());
        }
    }

}


