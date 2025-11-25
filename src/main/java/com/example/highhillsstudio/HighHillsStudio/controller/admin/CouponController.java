package com.example.highhillsstudio.HighHillsStudio.controller.admin;

import com.example.highhillsstudio.HighHillsStudio.dto.admin.CouponRequest;
import com.example.highhillsstudio.HighHillsStudio.service.admin.CouponService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;




    //  Show coupon list
    @GetMapping
    public String listCoupons(Model model) {
        model.addAttribute("coupons", couponService.getAllCoupons());
        return "admin/coupons/list"; // points to list.html
    }

    //  Show form to add a new coupon
    @GetMapping("/add")
    public String addCouponForm(Model model) {
        model.addAttribute("couponRequest", new CouponRequest());
        return "admin/coupons/form"; // points to form.html
    }

    //  Handle form submission
    @PostMapping("/create")
    public String createCoupon(@Valid @ModelAttribute("couponRequest") CouponRequest request,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            return "admin/coupons/form";
        }

        try {
            couponService.createCoupon(request);
        } catch (IllegalArgumentException e) {
            result.rejectValue("code", "error.couponRequest", e.getMessage());
            return "admin/coupons/form";
        }

        return "redirect:/admin/coupons";
    }

    //  Delete coupon
    @GetMapping("/delete/{id}")
    public String deleteCoupon(@PathVariable Long id) {
        try {
            couponService.deleteCoupon(id);
        } catch (IllegalArgumentException ignored) {
        }
        return "redirect:/admin/coupons";
    }
}
