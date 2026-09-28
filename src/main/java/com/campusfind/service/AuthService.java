package com.campusfind.service;

import com.campusfind.dto.LoginRequest;
import com.campusfind.dto.RegisterRequest;
import com.campusfind.dto.ResponseMapper;
import com.campusfind.dto.UserResponse;
import com.campusfind.entity.Role;
import com.campusfind.entity.User;
import com.campusfind.exception.ApiException;
import com.campusfind.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final String adminCode;

    public AuthService(UserRepository userRepo,
                       PasswordEncoder encoder,
                       @Value("${campusfind.admin-code}") String adminCode) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.adminCode = adminCode;
    }

    public UserResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepo.existsByEmail(email)) {
            throw ApiException.conflict("An account with this email already exists");
        }
        if (req.role() == Role.ADMIN && !adminCode.equals(req.adminCode())) {
            throw ApiException.forbidden("A valid adminCode is required to register an ADMIN account");
        }
        User user = new User(req.name().trim(), email, encoder.encode(req.password()), req.role());
        return ResponseMapper.toUser(userRepo.save(user));
    }

    public UserResponse login(LoginRequest req) {
        User user = userRepo.findByEmail(req.email().trim().toLowerCase())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));
        if (!encoder.matches(req.password(), user.getPassword())) {
            throw ApiException.unauthorized("Invalid email or password");
        }
        return ResponseMapper.toUser(user);
    }

    /** Every protected endpoint receives the logged-in user's id in the X-User-Id header. */
    public User requireUser(Long userId) {
        if (userId == null) {
            throw ApiException.unauthorized("Please login first: send your user id in the X-User-Id header");
        }
        return userRepo.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Unknown user id " + userId + ". Please login again"));
    }

    public void requireRole(User user, String message, Role... allowed) {
        for (Role r : allowed) {
            if (user.getRole() == r) {
                return;
            }
        }
        throw ApiException.forbidden(message);
    }
}
