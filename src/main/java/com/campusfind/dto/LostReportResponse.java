package com.campusfind.dto;

import com.campusfind.entity.LostStatus;

import java.time.LocalDate;

public record LostReportResponse(
        Long id,
        String description,
        String location,
        LocalDate dateLost,
        LostStatus status,
        CategoryResponse category,
        UserResponse reportedBy,
        Long matchedFoundItemId) {
}
