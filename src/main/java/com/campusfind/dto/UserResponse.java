package com.campusfind.dto;

import com.campusfind.entity.Role;

public record UserResponse(Long id, String name, String email, Role role) {
}
