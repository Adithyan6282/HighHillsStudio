package com.example.highhillsstudio.HighHillsStudio.controller.admin;

import com.example.highhillsstudio.HighHillsStudio.entity.User;
import com.example.highhillsstudio.HighHillsStudio.service.admin.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
// List users with pagination & search
    @GetMapping
    public String listUsers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model
    ) {
        Page<User> usersPage = userService.getUsers(keyword, page, 5); // 5 users per page
        model.addAttribute("usersPage", usersPage);
        model.addAttribute("keyword", keyword);
        return "admin/users/list";
    }

    // Block user
    @PostMapping("/{id}/block")
    public String blockUser(@PathVariable Long id,
                            @RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "page", defaultValue = "0") int page) {
        userService.blockUser(id);
        return "redirect:/admin/users?page=" + page + "&keyword=" + (keyword == null ? "" :keyword);
    }

    //Unblock user
    @PostMapping("/{id}/unblock")
    public String unblockUser(@PathVariable Long id,
                              @RequestParam(value = "keyword", required = false) String keyword,
                              @RequestParam(value = "page", defaultValue = "0") int page) {
        userService.unblockUser(id);
        return "redirect:/admin/users?page=" + page + "&keyword=" + (keyword == null ? "" : keyword);
    }


}
