package com.campusfind.dto;

import java.util.List;

public record MatchResponse(
        LostReportResponse lostReport,
        FoundItemResponse foundItem,
        List<String> sharedKeywords,
        List<String> sharedLocationWords,
        int score) {
}
