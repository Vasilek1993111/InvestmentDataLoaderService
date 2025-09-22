package com.example.InvestmentDataLoaderService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для работы со статусом загрузки
 * Управляет получением статуса задач загрузки
 */
@RestController
@RequestMapping("/api/status")
public class StatusController {

    /**
     * Получение статуса загрузки по ID задачи
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<Map<String, Object>> getLoadingStatus(@PathVariable String taskId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Здесь можно добавить логику проверки статуса задачи
            // Пока возвращаем базовую информацию
            response.put("success", true);
            response.put("taskId", taskId);
            response.put("status", "processing"); // processing, completed, failed
            response.put("message", "Задача выполняется");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения статуса: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
