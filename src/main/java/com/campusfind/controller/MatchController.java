package com.campusfind.controller;

import com.campusfind.dto.ConfirmMatchRequest;
import com.campusfind.dto.LostReportResponse;
import com.campusfind.dto.MatchResponse;
import com.campusfind.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    /** GET /api/matches            -> all possible matches
     *  GET /api/matches?lostId=5   -> possible matches for one lost report */
    @GetMapping
    public List<MatchResponse> possibleMatches(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(required = false) Long lostId) {
        return matchService.possibleMatches(userId, lostId);
    }

    @PostMapping("/confirm")
    public LostReportResponse confirm(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody ConfirmMatchRequest request) {
        return matchService.confirm(userId, request);
    }
}
