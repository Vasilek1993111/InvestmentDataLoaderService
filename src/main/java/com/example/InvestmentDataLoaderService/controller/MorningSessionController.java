package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.scheduler.MorningSessionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
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
            LocalDate today = LocalDate.now(ZoneId.of("Europe/Moscow"));
            LocalDate effectiveDate = today;
            // если сегодня выходной — берем последний рабочий день
            while (effectiveDate.getDayOfWeek().getValue() >= 6) { // 6=Сб, 7=Вс
                effectiveDate = effectiveDate.minusDays(1);
            }

            SaveResponseDto result = morningSessionService.fetchAndStoreMorningSessionPricesForDate(effectiveDate);

            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("dateUsed", effectiveDate);
            response.put("totalRequested", result.getTotalRequested());
            response.put("saved", result.getNewItemsSaved());
            response.put("skippedExisting", result.getExistingItemsSkipped());
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
     * Предпросмотр цен открытия утренней сессии за сегодня (без сохранения в БД)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> previewMorningSessionPricesToday() {
        Map<String, Object> response = new HashMap<>();

        try {
            LocalDate today = LocalDate.now(ZoneId.of("Europe/Moscow"));
            LocalDate effectiveDate = today;
            while (effectiveDate.getDayOfWeek().getValue() >= 6) {
                effectiveDate = effectiveDate.minusDays(1);
            }

            SaveResponseDto result = morningSessionService.previewMorningSessionPricesForDate(effectiveDate);

            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("dateUsed", effectiveDate);
            response.put("totalRequested", result.getTotalRequested());
            response.put("items", result.getSavedItems());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка предпросмотра цен утренней сессии: " + e.getMessage());
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
