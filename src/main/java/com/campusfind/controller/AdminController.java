package com.campusfind.controller;

import com.campusfind.dto.DashboardResponse;
import com.campusfind.dto.UserResponse;
import com.campusfind.service.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return adminService.dashboard(userId);
    }

    @GetMapping("/users")
    public List<UserResponse> users(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return adminService.users(userId);
    }
}
