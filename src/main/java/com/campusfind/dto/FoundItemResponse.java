package com.campusfind.dto;

import com.campusfind.entity.FoundStatus;

import java.time.LocalDate;

public record FoundItemResponse(
        Long id,
        String description,
        String location,
        LocalDate dateFound,
        FoundStatus status,
        CategoryResponse category,
        UserResponse reportedBy) {
}
