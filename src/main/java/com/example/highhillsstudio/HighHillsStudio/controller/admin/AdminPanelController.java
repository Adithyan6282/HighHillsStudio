package com.example.highhillsstudio.HighHillsStudio.controller.admin;

import com.example.highhillsstudio.HighHillsStudio.service.admin.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/panel")
@RequiredArgsConstructor
public class AdminPanelController {


    private final DashboardService dashboardService;





    @GetMapping
    public String dashboardPage(
            @RequestParam(defaultValue = "monthly") String filter,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int productPage,
            @RequestParam(defaultValue = "5") int productSize,
            @RequestParam(defaultValue = "0") int categoryPage,
            @RequestParam(defaultValue = "5") int categorySize,
            Model model) {

        Pageable productPageable = PageRequest.of(productPage, productSize);
        Pageable categoryPageable = PageRequest.of(categoryPage, categorySize);

        var topProducts = dashboardService.getTopProducts(productPageable);
        var topCategories = dashboardService.getTopCategories(categoryPageable);
        var salesChart = dashboardService.getSalesChart(filter, startDate, endDate);

        model.addAttribute("topProducts", topProducts);
        model.addAttribute("topCategories", topCategories);
        model.addAttribute("salesChart", salesChart);
        model.addAttribute("filter", filter);

        return "admin/admin_panel";
    }


}
