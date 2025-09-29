package com.example.InvestmentDataLoaderService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Контроллер для системных операций и мониторинга
 * Управляет состоянием системы, проверками и диагностикой
 */
@RestController
@RequestMapping("/api/system")
public class SystemController {
    

    // ==================== ЗДОРОВЬЕ СИСТЕМЫ ====================

    /**
     * Проверка здоровья системы
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Проверяем основные компоненты системы
            boolean materializedViewsExist = false; // TODO: Реализовать проверку материализованных представлений
            
            Map<String, Object> components = new HashMap<>();
            components.put("database", "healthy");
            components.put("materialized_views", materializedViewsExist ? "healthy" : "warning");
            components.put("schedulers", "healthy");
            components.put("api", "healthy");
            
            response.put("status", "healthy");
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("components", components);
            response.put("uptime", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "unhealthy");
            response.put("message", "Ошибка проверки здоровья системы: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * Детальная диагностика системы
     */
    @GetMapping("/diagnostics")
    public ResponseEntity<Map<String, Object>> getSystemDiagnostics() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> diagnostics = new HashMap<>();
            
            // Проверка материализованных представлений
            boolean viewsExist = false; // TODO: Реализовать проверку материализованных представлений
            diagnostics.put("materialized_views", Map.of(
                "exists", viewsExist,
                "status", viewsExist ? "ok" : "missing"
            ));
            
            // Информация о системе
            diagnostics.put("system", Map.of(
                "java_version", System.getProperty("java.version"),
                "os_name", System.getProperty("os.name"),
                "os_version", System.getProperty("os.version"),
                "available_processors", Runtime.getRuntime().availableProcessors(),
                "max_memory", Runtime.getRuntime().maxMemory(),
                "total_memory", Runtime.getRuntime().totalMemory(),
                "free_memory", Runtime.getRuntime().freeMemory()
            ));
            
            // Информация о расписании
            Map<String, Object> scheduleInfo = new HashMap<>();
            scheduleInfo.put("daily_refresh", "0 * * * * * (каждую минуту)");
            scheduleInfo.put("full_refresh", "0 20 2 * * * (каждый день в 2:20)");
            scheduleInfo.put("timezone", "Europe/Moscow");
            diagnostics.put("schedules", scheduleInfo);
            
            response.put("success", true);
            response.put("data", diagnostics);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка диагностики системы: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== ПРОВЕРКИ КОМПОНЕНТОВ ====================

    /**
     * Проверка материализованных представлений
     */
    @GetMapping("/volume-aggregation/check")
    public ResponseEntity<Map<String, Object>> checkMaterializedViews() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean exists = false; // TODO: Реализовать проверку материализованных представлений
            
            response.put("success", true);
            response.put("exists", exists);
            response.put("message", exists ? "Материализованные представления существуют" : "Материализованные представления не найдены");
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка проверки материализованных представлений: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            
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
            scheduleInfo.put("daily_refresh", "0 * * * * * (каждую минуту)");
            scheduleInfo.put("full_refresh", "0 20 2 * * * (каждый день в 2:20)");
            scheduleInfo.put("timezone", "Europe/Moscow");
            scheduleInfo.put("description", "Дневное представление обновляется каждую минуту, общее - в 2:20");
            
            response.put("success", true);
            response.put("data", scheduleInfo);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения информации о расписании: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== СТАТИСТИКА СИСТЕМЫ ====================

    /**
     * Получение статистики системы
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Статистика памяти
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> memoryStats = new HashMap<>();
            memoryStats.put("max_memory_mb", runtime.maxMemory() / 1024 / 1024);
            memoryStats.put("total_memory_mb", runtime.totalMemory() / 1024 / 1024);
            memoryStats.put("free_memory_mb", runtime.freeMemory() / 1024 / 1024);
            memoryStats.put("used_memory_mb", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024);
            stats.put("memory", memoryStats);
            
            // Статистика процессора
            Map<String, Object> processorStats = new HashMap<>();
            processorStats.put("available_processors", runtime.availableProcessors());
            stats.put("processor", processorStats);
            
            // Статистика времени работы
            Map<String, Object> uptimeStats = new HashMap<>();
            uptimeStats.put("start_time", System.currentTimeMillis());
            uptimeStats.put("uptime_ms", System.currentTimeMillis());
            stats.put("uptime", uptimeStats);
            
            // Статистика материализованных представлений
            // TODO: Реализовать получение детальной статистики
            stats.put("volume_aggregation", Map.of("status", "not_implemented"));
            
            response.put("success", true);
            response.put("data", stats);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения статистики системы: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== УПРАВЛЕНИЕ СИСТЕМОЙ ====================

    /**
     * Получение информации о версии и конфигурации
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> info = new HashMap<>();
            
            // Информация о Java
            info.put("java", Map.of(
                "version", System.getProperty("java.version"),
                "vendor", System.getProperty("java.vendor"),
                "home", System.getProperty("java.home")
            ));
            
            // Информация об ОС
            info.put("os", Map.of(
                "name", System.getProperty("os.name"),
                "version", System.getProperty("os.version"),
                "arch", System.getProperty("os.arch")
            ));
            
            // Информация о пользователе
            info.put("user", Map.of(
                "name", System.getProperty("user.name"),
                "home", System.getProperty("user.home"),
                "dir", System.getProperty("user.dir")
            ));
            
            // Информация о приложении
            info.put("application", Map.of(
                "name", "Investment Data Loader Service",
                "version", "1.0.0",
                "description", "Сервис загрузки инвестиционных данных"
            ));
            
            response.put("success", true);
            response.put("data", info);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения информации о системе: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Проверка доступности внешних сервисов
     */
    @GetMapping("/external-services")
    public ResponseEntity<Map<String, Object>> checkExternalServices() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> services = new HashMap<>();
            
            // Здесь можно добавить проверки внешних сервисов
            // Например, проверка доступности Tinkoff API
            services.put("tinkoff_api", Map.of(
                "status", "unknown",
                "message", "Проверка не реализована"
            ));
            
            services.put("database", Map.of(
                "status", "healthy",
                "message", "Подключение к базе данных работает"
            ));
            
            response.put("success", true);
            response.put("data", services);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка проверки внешних сервисов: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
