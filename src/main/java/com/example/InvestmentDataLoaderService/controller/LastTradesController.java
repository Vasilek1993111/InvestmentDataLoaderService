package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.LastTradesRequestDto;
import com.example.InvestmentDataLoaderService.service.LastTradesService;
import com.example.InvestmentDataLoaderService.service.CachedInstrumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для работы с последними сделками
 * Управляет загрузкой обезличенных сделок
 */
@RestController
@RequestMapping("/api/last-trades")
public class LastTradesController {

    private final LastTradesService lastTradesService;
    private final CachedInstrumentService cachedInstrumentService;

    public LastTradesController(LastTradesService lastTradesService, 
                              CachedInstrumentService cachedInstrumentService) {
        this.lastTradesService = lastTradesService;
        this.cachedInstrumentService = cachedInstrumentService;
    }

    /**
     * Загрузка последних сделок
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> loadLastTrades(@RequestBody LastTradesRequestDto request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            lastTradesService.fetchAndStoreLastTradesByRequestAsync(request);
            
            response.put("success", true);
            response.put("message", "Загрузка обезличенных сделок запущена в фоновом режиме");
            response.put("timestamp", LocalDateTime.now());
            response.put("cacheInfo", cachedInstrumentService.getCacheInfo());
            response.put("dataSource", "cache_with_db_fallback");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка загрузки сделок: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Получение информации о кэше инструментов
     */
    @GetMapping("/cache-info")
    public ResponseEntity<Map<String, Object>> getCacheInfo() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("success", true);
            response.put("cacheInfo", cachedInstrumentService.getCacheInfo());
            response.put("timestamp", LocalDateTime.now());
            response.put("dataSource", "cache_with_db_fallback");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения информации о кэше: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
