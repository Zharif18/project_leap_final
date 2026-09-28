package com.campusfind.dto;

import com.campusfind.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "name is required") @Size(max = 100, message = "name must be at most 100 characters") String name,
        @NotBlank(message = "email is required") @Email(message = "email must be a valid email address") @Size(max = 150) String email,
        @NotBlank(message = "password is required") @Size(min = 6, max = 72, message = "password must be 6 to 72 characters") String password,
        @NotNull(message = "role is required (STUDENT, STAFF or ADMIN)") Role role,
        String adminCode) {
}
