package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.scheduler.VolumeAggregationSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    
    @Autowired
    private VolumeAggregationSchedulerService volumeAggregationService;
    
    
    /**
     * Ручное обновление материализованного представления
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshAggregation() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            volumeAggregationService.refreshMaterializedViewManually();
            
            response.put("success", true);
            response.put("message", "Материализованное представление обновлено");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка обновления: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    
    /**
     * Получение статистики по материализованному представлению
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            volumeAggregationService.printAggregationStats();
            
            response.put("success", true);
            response.put("message", "Статистика выведена в консоль");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения статистики: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    
    /**
     * Проверка существования материализованного представления
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkMaterializedView() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean exists = volumeAggregationService.isMaterializedViewExists();
            
            response.put("success", true);
            response.put("exists", exists);
            response.put("message", exists ? "Материализованное представление существует" : "Материализованное представление не найдено");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка проверки: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
}
