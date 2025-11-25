package com.example.highhillsstudio.HighHillsStudio.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/admin/login")
    public String loginPage() {
        return "admin/login";    // src/main/resources/templates/admin/login.html
    }

    @GetMapping("/admin/dashboard")
    public String dashboardPage() {
        return "admin/dashboard";    // src/main/resources/templates/admin/dashboard.html
    }



}
