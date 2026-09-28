package com.campusfind.service;

import com.campusfind.dto.DashboardResponse;
import com.campusfind.dto.ResponseMapper;
import com.campusfind.dto.UserResponse;
import com.campusfind.entity.FoundStatus;
import com.campusfind.entity.LostStatus;
import com.campusfind.entity.Role;
import com.campusfind.entity.User;
import com.campusfind.repository.FoundItemRepository;
import com.campusfind.repository.LostReportRepository;
import com.campusfind.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepo;
    private final LostReportRepository lostRepo;
    private final FoundItemRepository foundRepo;
    private final AuthService auth;

    public AdminService(UserRepository userRepo, LostReportRepository lostRepo,
                        FoundItemRepository foundRepo, AuthService auth) {
        this.userRepo = userRepo;
        this.lostRepo = lostRepo;
        this.foundRepo = foundRepo;
        this.auth = auth;
    }

    public DashboardResponse dashboard(Long userId) {
        User user = auth.requireUser(userId);
        auth.requireRole(user, "Only an admin can view the dashboard", Role.ADMIN);
        return new DashboardResponse(
                userRepo.count(),
                lostRepo.count(),
                foundRepo.count(),
                lostRepo.countByStatus(LostStatus.OPEN),
                lostRepo.countByStatus(LostStatus.MATCHED),
                foundRepo.countByStatus(FoundStatus.RETURNED),
                foundRepo.countByStatus(FoundStatus.AVAILABLE),
                foundRepo.countByStatus(FoundStatus.CLAIMED));
    }

    public List<UserResponse> users(Long userId) {
        User user = auth.requireUser(userId);
        auth.requireRole(user, "Only an admin can list users", Role.ADMIN);
        return userRepo.findAll().stream().map(ResponseMapper::toUser).toList();
    }
}
