package com.campusfind.controller;

import com.campusfind.dto.LostReportRequest;
import com.campusfind.dto.LostReportResponse;
import com.campusfind.entity.LostStatus;
import com.campusfind.service.LostReportService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lost")
public class LostReportController {

    private final LostReportService lostService;

    public LostReportController(LostReportService lostService) {
        this.lostService = lostService;
    }

    @PostMapping
    public ResponseEntity<LostReportResponse> create(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody LostReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lostService.create(userId, request));
    }

    @GetMapping
    public List<LostReportResponse> list(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) LostStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return lostService.list(userId, categoryId, status, date);
    }

    @GetMapping("/{id}")
    public LostReportResponse get(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id) {
        return lostService.get(userId, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id) {
        lostService.delete(userId, id);
        return ResponseEntity.ok(Map.of("message", "Lost report " + id + " deleted"));
    }
}
