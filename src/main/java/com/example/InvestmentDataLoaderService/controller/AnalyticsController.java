package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.AggregationResult;
import com.example.InvestmentDataLoaderService.dto.AggregationRequestDto;
import com.example.InvestmentDataLoaderService.dto.SessionAnalyticsDto;
import com.example.InvestmentDataLoaderService.dto.SessionAnalyticsRequestDto;
import com.example.InvestmentDataLoaderService.scheduler.VolumeAggregationSchedulerService;
import com.example.InvestmentDataLoaderService.service.AggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Контроллер для аналитики и агрегации данных
 * Управляет материализованными представлениями и агрегированными данными
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    
    @Autowired
    private VolumeAggregationSchedulerService volumeAggregationService;
    
    @Autowired
    private AggregationService aggregationService;

    // ==================== АГРЕГАЦИЯ ОБЪЕМОВ ====================

    
    /**
     * Ручное полное обновление агрегации объемов
     */
    @PostMapping("/volume-aggregation/refresh-full")
    public ResponseEntity<Map<String, Object>> refreshFullVolumeAggregation() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            volumeAggregationService.refreshMaterializedViewManually();
            
            response.put("success", true);
            response.put("message", "Полная агрегация объемов успешно обновлена");
            response.put("type", "full");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка полного обновления агрегации: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Ручное обновление агрегации (совместимость)
     */
    @PostMapping("/volume-aggregation/refresh")
    public ResponseEntity<Map<String, Object>> refreshVolumeAggregation() {
        return refreshFullVolumeAggregation();
    }

    // ==================== СТАТИСТИКА ====================

    
    /**
     * Получение общей статистики агрегации
     */
    @GetMapping("/volume-aggregation/stats")
    public ResponseEntity<Map<String, Object>> getVolumeAggregationStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            volumeAggregationService.printAggregationStats();
            
            response.put("success", true);
            response.put("message", "Общая статистика агрегации выведена в консоль");
            response.put("type", "general");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения общей статистики: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Получение детальной статистики по сессиям
     */
    @GetMapping("/volume-aggregation/detailed-stats")
    public ResponseEntity<Map<String, Object>> getDetailedStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> stats = volumeAggregationService.getDetailedStats();
            
            if (stats != null) {
                response.put("success", true);
                response.put("data", stats);
                response.put("type", "detailed");
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

    // ==================== ПЕРЕСЧЕТ АГРЕГАЦИИ ====================

    /**
     * Пересчет агрегированных данных по типам инструментов
     */
    @PostMapping("/recalculate-aggregation")
    public ResponseEntity<Map<String, Object>> recalculateAggregation(@RequestBody AggregationRequestDto request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<AggregationResult> results = aggregationService.recalculateByTypes(
                request != null ? request.getTypes() : null
            );
            
            response.put("success", true);
            response.put("message", "Пересчет агрегации выполнен успешно");
            response.put("results", results);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка пересчета агрегации: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== ПРОВЕРКИ И МОНИТОРИНГ ====================

    /**
     * Проверка существования материализованных представлений
     */
    @GetMapping("/volume-aggregation/check")
    public ResponseEntity<Map<String, Object>> checkMaterializedViews() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean exists = volumeAggregationService.isMaterializedViewExists();
            
            response.put("success", true);
            response.put("exists", exists);
            response.put("message", exists ? "Материализованные представления существуют" : "Материализованные представления не найдены");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка проверки материализованных представлений: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Получение информации о расписании обновлений
     */
    @GetMapping("/volume-aggregation/schedule-info")
    public ResponseEntity<Map<String, Object>> getScheduleInfo() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> scheduleInfo = new HashMap<>();
            scheduleInfo.put("full_refresh", "0 20 2 * * * (каждый день в 2:20)");
            scheduleInfo.put("timezone", "Europe/Moscow");
            scheduleInfo.put("description", "Материализованное представление обновляется раз в день в 2:20");
            
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

    // ==================== СЕССИОННАЯ АНАЛИТИКА ====================

    /**
     * Получение аналитики по торговым сессиям за период
     */
    @PostMapping("/session-analytics")
    public ResponseEntity<Map<String, Object>> getSessionAnalytics(@RequestBody SessionAnalyticsRequestDto request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<SessionAnalyticsDto> analytics = aggregationService.getSessionAnalytics(
                request.figi(), 
                request.startDate(), 
                request.endDate()
            );
            
            response.put("success", true);
            response.put("data", analytics);
            response.put("count", analytics.size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения сессионной аналитики: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Получение общей аналитики по торговым сессиям (за все время)
     */
    @GetMapping("/session-analytics/overall")
    public ResponseEntity<Map<String, Object>> getOverallSessionAnalytics(
            @RequestParam(required = false) String figi) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<SessionAnalyticsDto> analytics = aggregationService.getOverallSessionAnalytics(figi);
            
            response.put("success", true);
            response.put("data", analytics);
            response.put("count", analytics.size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения общей сессионной аналитики: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    
    /**
     * Получение сводной статистики по сессиям за период
     */
    @PostMapping("/session-analytics/summary")
    public ResponseEntity<Map<String, Object>> getSessionAnalyticsSummary(@RequestBody SessionAnalyticsRequestDto request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> summary = aggregationService.getSessionAnalyticsSummary(
                request.figi(), 
                request.startDate(), 
                request.endDate()
            );
            
            response.put("success", true);
            response.put("data", summary);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения сводной сессионной аналитики: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== АНАЛИТИЧЕСКИЕ ЗАПРОСЫ ====================

    /**
     * Получение сводной аналитики по всем агрегированным данным
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getAnalyticsSummary() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> summary = new HashMap<>();
            
            // Получаем детальную статистику
            Map<String, Object> detailedStats = volumeAggregationService.getDetailedStats();
            if (detailedStats != null) {
                summary.put("volume_aggregation", detailedStats);
            }
            
            // Проверяем состояние материализованных представлений
            boolean viewsExist = volumeAggregationService.isMaterializedViewExists();
            summary.put("materialized_views_exist", viewsExist);
            
            response.put("success", true);
            response.put("data", summary);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения сводной аналитики: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
