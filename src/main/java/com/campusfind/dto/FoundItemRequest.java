package com.campusfind.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record FoundItemRequest(
        @NotNull(message = "categoryId is required") @Positive(message = "categoryId must be positive") Long categoryId,
        @NotBlank(message = "description is required") @Size(max = 500, message = "description must be at most 500 characters") String description,
        @NotBlank(message = "location is required") @Size(max = 255, message = "location must be at most 255 characters") String location,
        @NotNull(message = "dateFound is required (yyyy-MM-dd)") @PastOrPresent(message = "dateFound cannot be in the future") LocalDate dateFound) {
}
