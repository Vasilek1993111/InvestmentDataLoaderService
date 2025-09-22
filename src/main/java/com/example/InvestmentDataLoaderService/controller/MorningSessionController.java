package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.scheduler.MorningSessionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для работы с ценами утренней сессии
 * Управляет загрузкой цен утренней торговой сессии
 */
@RestController
@RequestMapping("/api/morning-session")
public class MorningSessionController {

    private final MorningSessionService morningSessionService;

    public MorningSessionController(MorningSessionService morningSessionService) {
        this.morningSessionService = morningSessionService;
    }

    /**
     * Загрузка цен утренней сессии за сегодня
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> loadMorningSessionPricesToday() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            morningSessionService.fetchAndStoreMorningSessionPrices();
            
            response.put("success", true);
            response.put("message", "Загрузка цен утренней сессии запущена для сегодня");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка загрузки цен утренней сессии: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Загрузка цен утренней сессии за дату
     */
    @PostMapping("/{date}")
    public ResponseEntity<SaveResponseDto> loadMorningSessionPricesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        SaveResponseDto response = morningSessionService.fetchAndStoreMorningSessionPricesForDate(date);
        return ResponseEntity.ok(response);
    }
}
