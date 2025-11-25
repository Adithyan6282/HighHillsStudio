package com.example.highhillsstudio.HighHillsStudio.controller.user;



import com.example.highhillsstudio.HighHillsStudio.service.user.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping({"/", "/users/home"})
    public String homePage(Model model) {
        model.addAttribute("categories", homeService.getAllCategories());
        model.addAttribute("latestProducts", homeService.getLatestProducts(20)); // show 8 products
        return "user/home";

    }




}


