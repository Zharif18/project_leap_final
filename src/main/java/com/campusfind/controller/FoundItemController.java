package com.campusfind.controller;

import com.campusfind.dto.FoundItemRequest;
import com.campusfind.dto.FoundItemResponse;
import com.campusfind.dto.StatusUpdateRequest;
import com.campusfind.entity.FoundStatus;
import com.campusfind.service.FoundItemService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/found")
public class FoundItemController {

    private final FoundItemService foundService;

    public FoundItemController(FoundItemService foundService) {
        this.foundService = foundService;
    }

    @PostMapping
    public ResponseEntity<FoundItemResponse> create(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody FoundItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(foundService.create(userId, request));
    }

    @GetMapping
    public List<FoundItemResponse> list(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) FoundStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return foundService.list(userId, categoryId, status, date);
    }

    @GetMapping("/{id}")
    public FoundItemResponse get(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id) {
        return foundService.get(userId, id);
    }

    @PutMapping("/{id}/status")
    public FoundItemResponse updateStatus(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        return foundService.updateStatus(userId, id, request.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id) {
        foundService.delete(userId, id);
        return ResponseEntity.ok(Map.of("message", "Found item " + id + " deleted"));
    }
}
