package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.entity.SystemLogEntity;
import com.example.InvestmentDataLoaderService.repository.SystemLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для получения статуса асинхронных операций
 * 
 * <p>Предоставляет API для отслеживания статуса выполнения асинхронных задач
 * через уникальные идентификаторы (taskId).</p>
 * 
 * <p>Все операции логируются в таблицу system_logs с детальной информацией:</p>
 * <ul>
 *   <li>Статус выполнения (STARTED, COMPLETED, FAILED)</li>
 *   <li>Время начала и завершения</li>
 *   <li>Детальные сообщения о результатах</li>
 *   <li>Длительность выполнения</li>
 * </ul>
 * 
 * @author InvestmentDataLoaderService
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/status")
public class StatusController {

    private final SystemLogRepository systemLogRepository;

    public StatusController(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    /**
     * Получение статуса операции по taskId
     * 
     * <p>Возвращает текущий статус выполнения асинхронной операции.
     * Ищет все логи по заданному taskId и возвращает последний статус.</p>
     * 
     * <p>Примеры использования:</p>
     * <pre>
     * GET /api/status/550e8400-e29b-41d4-a716-446655440000
     * GET /api/status/PRELOAD_2024-01-15T00:45:00_SHARES
     * </pre>
     * 
     * <p>Возможные статусы:</p>
     * <ul>
     *   <li><strong>STARTED</strong> - операция запущена</li>
     *   <li><strong>COMPLETED</strong> - операция завершена успешно</li>
     *   <li><strong>FAILED</strong> - операция завершена с ошибкой</li>
     * </ul>
     * 
     * @param taskId уникальный идентификатор задачи
     * @return статус операции с детальной информацией
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTaskStatus(@PathVariable String taskId) {
        try {
            // Получаем все логи по taskId, отсортированные по времени создания (новые первыми)
            List<SystemLogEntity> logs = systemLogRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
            
            if (logs.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Задача с taskId '" + taskId + "' не найдена");
                response.put("error", "NOT_FOUND");
                response.put("taskId", taskId);
                response.put("timestamp", LocalDateTime.now().toString());
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Берем последний лог (самый свежий)
            SystemLogEntity latestLog = logs.get(0);
            
            // Формируем ответ
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("taskId", taskId);
            response.put("status", latestLog.getStatus());
            response.put("message", latestLog.getMessage());
            response.put("endpoint", latestLog.getEndpoint());
            response.put("method", latestLog.getMethod());
            response.put("startTime", latestLog.getStartTime().toString());
            response.put("endTime", latestLog.getEndTime() != null ? latestLog.getEndTime().toString() : null);
            
            // Вычисляем длительность для основного ответа
            if (latestLog.getDurationMs() != null) {
                response.put("durationMs", latestLog.getDurationMs());
            } else if ("STARTED".equals(latestLog.getStatus())) {
                // Для активных задач показываем время с момента запуска
                long currentDuration = System.currentTimeMillis() - latestLog.getStartTime().toEpochMilli();
                response.put("durationMs", currentDuration);
                response.put("isActive", true);
            } else {
                response.put("durationMs", "N/A");
            }
            
            response.put("timestamp", LocalDateTime.now().toString());
            
            // Добавляем историю операций (последние 5 записей)
            List<Map<String, Object>> history = logs.stream()
                .limit(5)
                .map(log -> {
                    Map<String, Object> logEntry = new HashMap<>();
                    logEntry.put("status", log.getStatus());
                    logEntry.put("message", log.getMessage());
                    logEntry.put("timestamp", log.getCreatedAt().toString());
                    
                    // Вычисляем длительность или показываем статус
                    if (log.getDurationMs() != null) {
                        logEntry.put("durationMs", log.getDurationMs());
                    } else if ("STARTED".equals(log.getStatus())) {
                        // Для активных задач показываем время с момента запуска
                        long currentDuration = System.currentTimeMillis() - log.getStartTime().toEpochMilli();
                        logEntry.put("durationMs", currentDuration);
                        logEntry.put("isActive", true);
                    } else {
                        logEntry.put("durationMs", "N/A");
                    }
                    
                    return logEntry;
                })
                .toList();
            response.put("history", history);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Ошибка получения статуса задачи: " + e.getMessage());
            response.put("error", "INTERNAL_ERROR");
            response.put("taskId", taskId);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Получение всех активных задач
     * 
     * <p>Возвращает список всех задач со статусом STARTED или PROCESSING.</p>
     * 
     * <p>Пример использования:</p>
     * <pre>
     * GET /api/status/active
     * </pre>
     * 
     * @return список активных задач
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveTasks() {
        try {
            List<SystemLogEntity> activeTasks = systemLogRepository.findActiveTasks();
            
            List<Map<String, Object>> tasks = activeTasks.stream()
                .map(task -> {
                    Map<String, Object> taskInfo = new HashMap<>();
                    taskInfo.put("taskId", task.getTaskId());
                    taskInfo.put("status", task.getStatus());
                    taskInfo.put("message", task.getMessage());
                    taskInfo.put("endpoint", task.getEndpoint());
                    taskInfo.put("method", task.getMethod());
                    taskInfo.put("startTime", task.getStartTime().toString());
                    
                    // Вычисляем текущую длительность для активных задач
                    if (task.getDurationMs() != null) {
                        taskInfo.put("durationMs", task.getDurationMs());
                    } else {
                        long currentDuration = System.currentTimeMillis() - task.getStartTime().toEpochMilli();
                        taskInfo.put("durationMs", currentDuration);
                        taskInfo.put("isActive", true);
                    }
                    
                    return taskInfo;
                })
                .toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("activeTasks", tasks);
            response.put("count", tasks.size());
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Ошибка получения активных задач: " + e.getMessage());
            response.put("error", "INTERNAL_ERROR");
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Получение статистики по задачам
     * 
     * <p>Возвращает общую статистику по всем задачам в системе.</p>
     * 
     * <p>Пример использования:</p>
     * <pre>
     * GET /api/status/stats
     * </pre>
     * 
     * @return статистика по задачам
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getTaskStats() {
        try {
            List<SystemLogEntity> allLogs = systemLogRepository.findAll();
            
            long totalTasks = allLogs.size();
            long startedTasks = allLogs.stream().filter(log -> "STARTED".equals(log.getStatus())).count();
            long completedTasks = allLogs.stream().filter(log -> "COMPLETED".equals(log.getStatus())).count();
            long failedTasks = allLogs.stream().filter(log -> "FAILED".equals(log.getStatus())).count();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalTasks", totalTasks);
            response.put("startedTasks", startedTasks);
            response.put("completedTasks", completedTasks);
            response.put("failedTasks", failedTasks);
            response.put("successRate", totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Ошибка получения статистики: " + e.getMessage());
            response.put("error", "INTERNAL_ERROR");
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
