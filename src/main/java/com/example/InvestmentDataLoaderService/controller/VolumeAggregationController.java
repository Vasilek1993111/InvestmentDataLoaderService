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
     * Ручное обновление данных за сегодня
     */
    @PostMapping("/refresh-today")
    public ResponseEntity<Map<String, Object>> refreshTodayData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            volumeAggregationService.refreshTodayDataManually();
            
            response.put("success", true);
            response.put("message", "Данные за сегодня успешно обновлены");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка обновления данных за сегодня: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Ручное полное обновление материализованного представления
     */
    @PostMapping("/refresh-full")
    public ResponseEntity<Map<String, Object>> refreshFullAggregation() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            volumeAggregationService.refreshMaterializedViewManually();
            
            response.put("success", true);
            response.put("message", "Материализованное представление полностью обновлено");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка полного обновления: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Ручное обновление материализованного представления (совместимость)
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshAggregation() {
        return refreshTodayData();
    }
    
    /**
     * Получение статистики за сегодня
     */
    @GetMapping("/stats-today")
    public ResponseEntity<Map<String, Object>> getTodayStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            volumeAggregationService.printTodayStats();
            
            response.put("success", true);
            response.put("message", "Статистика за сегодня выведена в консоль");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения статистики за сегодня: " + e.getMessage());
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
     * Получение детальной статистики по сессиям
     */
    @GetMapping("/detailed-stats")
    public ResponseEntity<Map<String, Object>> getDetailedStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> stats = volumeAggregationService.getDetailedStats();
            
            if (stats != null) {
                response.put("success", true);
                response.put("data", stats);
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Не удалось получить детальную статистику");
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.status(500).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения детальной статистики: " + e.getMessage());
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
    
    /**
     * Получение информации о расписании обновлений
     */
    @GetMapping("/schedule-info")
    public ResponseEntity<Map<String, Object>> getScheduleInfo() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> scheduleInfo = new HashMap<>();
            scheduleInfo.put("minute_refresh", "0 * * * * * (каждую минуту)");
            scheduleInfo.put("daily_full_refresh", "0 0 2 * * * (каждый день в 2:00)");
            scheduleInfo.put("timezone", "Europe/Moscow");
            scheduleInfo.put("description", "Материализованное представление обновляется каждую минуту для актуальных данных");
            
            response.put("success", true);
            response.put("data", scheduleInfo);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения информации о расписании: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
