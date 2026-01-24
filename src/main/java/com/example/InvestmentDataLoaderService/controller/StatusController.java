package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.entity.SystemLogEntity;
import com.example.InvestmentDataLoaderService.repository.SystemLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(StatusController.class);
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
        log.info("=== ПОЛУЧЕНИЕ СТАТУСА ЗАДАЧИ: {} ===", taskId);
        try {
            log.info("Ищем логи для taskId: {}", taskId);
            // Получаем все логи по taskId, отсортированные по времени создания (новые первыми)
            List<SystemLogEntity> allLogs = systemLogRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
            
            if (allLogs.isEmpty()) {
                log.warn("Задача с taskId '{}' не найдена", taskId);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Задача с taskId '" + taskId + "' не найдена");
                response.put("error", "NOT_FOUND");
                response.put("taskId", taskId);
                response.put("timestamp", LocalDateTime.now().toString());
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Фильтруем только логи со статусами STARTED, FAILED, COMPLETED
            List<SystemLogEntity> filteredLogs = allLogs.stream()
                .filter(logEntry -> "STARTED".equals(logEntry.getStatus()) || 
                              "FAILED".equals(logEntry.getStatus()) || 
                              "COMPLETED".equals(logEntry.getStatus()))
                .toList();
            
            if (filteredLogs.isEmpty()) {
                log.warn("Для задачи с taskId '{}' не найдено логов со статусами STARTED, FAILED, COMPLETED", taskId);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Для задачи с taskId '" + taskId + "' не найдено логов со статусами STARTED, FAILED, COMPLETED");
                response.put("error", "NOT_FOUND");
                response.put("taskId", taskId);
                response.put("timestamp", LocalDateTime.now().toString());
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Определяем приоритетный статус: COMPLETED > FAILED > STARTED
            SystemLogEntity resultLog = null;
            
            // Ищем COMPLETED (высший приоритет - задача успешно завершена)
            for (SystemLogEntity logEntry : filteredLogs) {
                if ("COMPLETED".equals(logEntry.getStatus())) {
                    resultLog = logEntry;
                    log.info("Найден статус COMPLETED для taskId {}: задача завершена успешно", taskId);
                    break;
                }
            }
            
            // Если COMPLETED не найден, ищем FAILED (задача завершена с ошибкой)
            if (resultLog == null) {
                for (SystemLogEntity logEntry : filteredLogs) {
                    if ("FAILED".equals(logEntry.getStatus())) {
                        resultLog = logEntry;
                        log.info("Найден статус FAILED для taskId {}: задача завершена с ошибкой", taskId);
                        break;
                    }
                }
            }
            
            // Если ни COMPLETED, ни FAILED не найдены, берем STARTED (задача выполняется)
            if (resultLog == null) {
                for (SystemLogEntity logEntry : filteredLogs) {
                    if ("STARTED".equals(logEntry.getStatus())) {
                        resultLog = logEntry;
                        log.info("Найден статус STARTED для taskId {}: задача выполняется", taskId);
                        break;
                    }
                }
            }
            
            if (resultLog == null) {
                log.error("Не удалось определить статус для taskId {}", taskId);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Не удалось определить статус для задачи с taskId '" + taskId + "'");
                response.put("error", "INTERNAL_ERROR");
                response.put("taskId", taskId);
                response.put("timestamp", LocalDateTime.now().toString());
                
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
            log.info("Определен статус для taskId {}: {}, сообщение: {}", 
                taskId, resultLog.getStatus(), resultLog.getMessage());
            
            // Формируем ответ
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("taskId", taskId);
            response.put("status", resultLog.getStatus());
            response.put("message", resultLog.getMessage());
            response.put("endpoint", resultLog.getEndpoint());
            response.put("method", resultLog.getMethod());
            response.put("startTime", resultLog.getStartTime().toString());
            response.put("endTime", resultLog.getEndTime() != null ? resultLog.getEndTime().toString() : null);
            
            // Вычисляем длительность для основного ответа
            if (resultLog.getDurationMs() != null) {
                response.put("durationMs", resultLog.getDurationMs());
            } else if ("STARTED".equals(resultLog.getStatus())) {
                // Для активных задач показываем время с момента запуска
                long currentDuration = System.currentTimeMillis() - resultLog.getStartTime().toEpochMilli();
                response.put("durationMs", currentDuration);
                response.put("isActive", true);
            } else {
                response.put("durationMs", "N/A");
            }
            
            response.put("timestamp", LocalDateTime.now().toString());
            
            // Добавляем историю операций только с нужными статусами (последние 5 записей)
            List<Map<String, Object>> history = filteredLogs.stream()
                .limit(5)
                .map(logEntry -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("status", logEntry.getStatus());
                    entry.put("message", logEntry.getMessage());
                    entry.put("timestamp", logEntry.getCreatedAt().toString());
                    
                    // Вычисляем длительность или показываем статус
                    if (logEntry.getDurationMs() != null) {
                        entry.put("durationMs", logEntry.getDurationMs());
                    } else if ("STARTED".equals(logEntry.getStatus())) {
                        // Для активных задач показываем время с момента запуска
                        long currentDuration = System.currentTimeMillis() - logEntry.getStartTime().toEpochMilli();
                        entry.put("durationMs", currentDuration);
                        entry.put("isActive", true);
                    } else {
                        entry.put("durationMs", "N/A");
                    }
                    
                    return entry;
                })
                .toList();
            response.put("history", history);
            
            log.info("Статус задачи {} успешно получен. Найдено {} записей в истории", taskId, filteredLogs.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Ошибка при получении статуса задачи {}: {}", taskId, e.getMessage(), e);
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
        log.info("=== ПОЛУЧЕНИЕ АКТИВНЫХ ЗАДАЧ ===");
        try {
            log.info("Ищем активные задачи...");
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
            
            log.info("Найдено {} активных задач", tasks.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Ошибка при получении активных задач: {}", e.getMessage(), e);
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
        log.info("=== ПОЛУЧЕНИЕ СТАТИСТИКИ ЗАДАЧ ===");
        try {
            log.info("Собираем статистику по всем задачам...");
            List<SystemLogEntity> allLogs = systemLogRepository.findAll();
            
            long totalTasks = allLogs.size();
            long startedTasks = allLogs.stream().filter(logEntry -> "STARTED".equals(logEntry.getStatus())).count();
            long completedTasks = allLogs.stream().filter(logEntry -> "COMPLETED".equals(logEntry.getStatus())).count();
            long failedTasks = allLogs.stream().filter(logEntry -> "FAILED".equals(logEntry.getStatus())).count();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalTasks", totalTasks);
            response.put("startedTasks", startedTasks);
            response.put("completedTasks", completedTasks);
            response.put("failedTasks", failedTasks);
            response.put("successRate", totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0);
            response.put("timestamp", LocalDateTime.now().toString());
            
            log.info("Статистика задач: всего {}, запущено {}, завершено {}, ошибок {}, успешность {}%", 
                totalTasks, startedTasks, completedTasks, failedTasks, 
                totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Ошибка при получении статистики задач: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Ошибка получения статистики: " + e.getMessage());
            response.put("error", "INTERNAL_ERROR");
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
