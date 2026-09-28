package com.campusfind.dto;

import com.campusfind.entity.FoundStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "status is required (AVAILABLE, CLAIMED or RETURNED)") FoundStatus status) {
}
