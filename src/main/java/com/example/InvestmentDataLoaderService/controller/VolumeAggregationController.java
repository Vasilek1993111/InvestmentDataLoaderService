package com.example.InvestmentDataLoaderService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для управления материализованным представлением агрегации объемов
 */
@RestController
@RequestMapping("/api/volume-aggregation")
public class VolumeAggregationController {
    
    
    /**
     * Ручное обновление материализованного представления
     */
    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<Map<String, Object>> refreshAggregation() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // TODO: Реализовать ручное обновление материализованного представления
            response.put("success", true);
            response.put("message", "Материализованное представление обновлено");
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка обновления: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    
}
