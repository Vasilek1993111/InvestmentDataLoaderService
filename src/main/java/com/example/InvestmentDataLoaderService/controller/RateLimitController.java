package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.service.RateLimitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для мониторинга и управления rate limiting
 */
@RestController
@RequestMapping("/api/rate-limit")
public class RateLimitController {

    private final RateLimitService rateLimitService;

    public RateLimitController(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    /**
     * Получение статистики по rate limiting
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getRateLimitStats() {
        RateLimitService.RateLimitStats stats = rateLimitService.getStats();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Статистика rate limiting получена");
        response.put("availablePermits", stats.getAvailablePermits());
        response.put("maxPermits", stats.getMaxPermits());
        response.put("usedPermits", stats.getUsedPermits());
        response.put("activeOperationTypes", stats.getActiveOperationTypes());
        response.put("utilizationPercent", (double) stats.getUsedPermits() / stats.getMaxPermits() * 100);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Получение информации о текущем состоянии rate limiting
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus() {
        RateLimitService.RateLimitStats stats = rateLimitService.getStats();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("status", stats.getAvailablePermits() > 0 ? "AVAILABLE" : "LIMITED");
        response.put("message", stats.getAvailablePermits() > 0 
            ? "API запросы доступны" 
            : "Достигнут лимит одновременных запросов");
        response.put("availablePermits", stats.getAvailablePermits());
        response.put("maxPermits", stats.getMaxPermits());
        
        return ResponseEntity.ok(response);
    }
}

