package com.campusfind.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ConfirmMatchRequest(
        @NotNull(message = "lostReportId is required") @Positive(message = "lostReportId must be positive") Long lostReportId,
        @NotNull(message = "foundItemId is required") @Positive(message = "foundItemId must be positive") Long foundItemId) {
}
