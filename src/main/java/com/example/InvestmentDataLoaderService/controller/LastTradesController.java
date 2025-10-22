package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.LastTradesRequestDto;
import com.example.InvestmentDataLoaderService.service.LastTradesService;
import com.example.InvestmentDataLoaderService.service.CachedInstrumentService;
import com.example.InvestmentDataLoaderService.entity.SystemLogEntity;
import com.example.InvestmentDataLoaderService.repository.SystemLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.UUID;

/**
 * Контроллер для работы с последними сделками
 * Управляет загрузкой обезличенных сделок
 */
@RestController
@RequestMapping("/api/last-trades")
public class LastTradesController {

    private static final Logger log = LoggerFactory.getLogger(LastTradesController.class);
    private final LastTradesService lastTradesService;
    private final CachedInstrumentService cachedInstrumentService;
    private final SystemLogRepository systemLogRepository;
    private final Executor lastTradesApiExecutor;
    private final Executor lastTradesBatchExecutor;
    private final Executor lastTradesProcessingExecutor;

    public LastTradesController(LastTradesService lastTradesService, 
                              CachedInstrumentService cachedInstrumentService,
                              SystemLogRepository systemLogRepository,
                              @Qualifier("lastTradesApiExecutor") Executor lastTradesApiExecutor,
                              @Qualifier("lastTradesBatchExecutor") Executor lastTradesBatchExecutor,
                              @Qualifier("lastTradesProcessingExecutor") Executor lastTradesProcessingExecutor) {
        this.lastTradesService = lastTradesService;
        this.cachedInstrumentService = cachedInstrumentService;
        this.systemLogRepository = systemLogRepository;
        this.lastTradesApiExecutor = lastTradesApiExecutor;
        this.lastTradesBatchExecutor = lastTradesBatchExecutor;
        this.lastTradesProcessingExecutor = lastTradesProcessingExecutor;
    }

    /**
     * Загрузка последних сделок (оптимизировано)
     * Поддерживает как GET с параметрами, так и POST с JSON телом
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> load(
            @RequestParam(required = false) String figis,
            @RequestParam(required = false) String tradeSource,
            @RequestBody(required = false) LastTradesRequestDto requestBody) {
        
        String taskId = "LAST_TRADES_" + UUID.randomUUID().toString().substring(0, 8);
        Instant startTime = Instant.now();
        String httpMethod = requestBody != null ? "POST" : "GET";
        
        // Логируем начало обработки
        logSystemEvent(taskId, "/api/last-trades", httpMethod, "STARTED", 
            "Начало загрузки последних сделок", startTime);
        
        try {
            LastTradesRequestDto request;
            
            // Если есть JSON тело, используем его
            if (requestBody != null) {
                request = requestBody;
            } else {
                // Иначе создаем из параметров запроса
                request = new LastTradesRequestDto();
                request.setFigis(java.util.Arrays.asList(
                    (figis != null ? figis : "ALL").split(",")
                ));
                request.setTradeSource(tradeSource != null ? tradeSource : "TRADE_SOURCE_ALL");
            }
            
            // Логируем детали запроса
            logSystemEvent(taskId, "/api/last-trades", httpMethod, "PROCESSING", 
                "Обработка запроса: " + request.getFigis() + " | Источник: " + request.getTradeSource(), startTime);
            
            ResponseEntity<Map<String, Object>> response = loadOptimized(request, taskId);
            
            // Логируем успешное завершение
            logSystemEvent(taskId, "/api/last-trades", httpMethod, "COMPLETED", 
                "Загрузка последних сделок успешно завершена", startTime);
            
            return response;
            
        } catch (Exception e) {
            // Логируем ошибку
            logSystemEvent(taskId, "/api/last-trades", httpMethod, "FAILED", 
                "Ошибка загрузки последних сделок: " + e.getMessage(), startTime);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Ошибка загрузки сделок: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            errorResponse.put("taskId", taskId);
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Оптимизированная загрузка последних сделок с учетом лимитов Т-Инвест
     * Использует специализированные executor'ы для максимальной производительности
     */
    private ResponseEntity<Map<String, Object>> loadOptimized(LastTradesRequestDto request, String taskId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Быстрая валидация запроса
            if (request.getFigis() == null || request.getFigis().isEmpty()) {
                response.put("success", false);
                response.put("message", "Параметр 'figis' является обязательным");
                response.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.badRequest().body(response);
            }
            
            // Получаем информацию о кэше асинхронно для быстрого ответа
            CompletableFuture<String> cacheInfoFuture = CompletableFuture.supplyAsync(() -> 
                cachedInstrumentService.getCacheInfo(), lastTradesProcessingExecutor);
            
            // Запускаем загрузку сделок с оптимизированными executor'ами
            CompletableFuture.runAsync(() -> {
                try {
                    log.info("=== ОПТИМИЗИРОВАННАЯ ЗАГРУЗКА LAST TRADES ===");
                    log.info("Используем специализированные executor'ы для максимальной производительности");
                    
                    // Логируем начало обработки каждого FIGI
                    for (String figi : request.getFigis()) {
                        logFigiProcessing(taskId, figi, "STARTED", "Начало обработки FIGI", Instant.now());
                    }
                    
                    // Используем оптимизированный сервис с правильными executor'ами
                    lastTradesService.fetchAndStoreLastTradesByRequestAsync(request);
                    
                    // Логируем завершение обработки каждого FIGI
                    for (String figi : request.getFigis()) {
                        logFigiProcessing(taskId, figi, "COMPLETED", "Обработка FIGI завершена", Instant.now());
                    }
                    
                    log.info("=== ЗАВЕРШЕНИЕ ОПТИМИЗИРОВАННОЙ ЗАГРУЗКИ ===");
                } catch (Exception e) {
                    log.error("Ошибка оптимизированной загрузки: {}", e.getMessage(), e);
                    e.printStackTrace();
                    
                    // Логируем ошибку для каждого FIGI
                    for (String figi : request.getFigis()) {
                        logFigiProcessing(taskId, figi, "FAILED", "Ошибка обработки FIGI: " + e.getMessage(), Instant.now());
                    }
                }
            }, lastTradesApiExecutor);
            
            // Ждем информацию о кэше для быстрого ответа
            String cacheInfo = cacheInfoFuture.get();
            
            response.put("success", true);
            response.put("message", "Оптимизированная загрузка обезличенных сделок запущена");
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("taskId", taskId);
            response.put("cacheInfo", cacheInfo);
            response.put("dataSource", "optimized_cache_with_db_fallback");
            response.put("executorInfo", "Используются специализированные executor'ы для LastTrades");
            response.put("performanceMode", "HIGH_SPEED");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка оптимизированной загрузки сделок: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("errorType", "OPTIMIZATION_ERROR");
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Информация о кэше инструментов
     */
    @GetMapping("/cache")
    public ResponseEntity<Map<String, Object>> getCache() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("success", true);
            response.put("cacheInfo", cachedInstrumentService.getCacheInfo());
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("dataSource", "cache_with_db_fallback");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения информации о кэше: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Загрузка только акций (оптимизировано)
     */
    @GetMapping("/shares")
    public ResponseEntity<Map<String, Object>> loadShares() {
        String taskId = "SHARES_" + UUID.randomUUID().toString().substring(0, 8);
        Instant startTime = Instant.now();
        
        logSystemEvent(taskId, "/api/last-trades/shares", "GET", "STARTED", 
            "Начало загрузки только акций", startTime);
        
        try {
            ResponseEntity<Map<String, Object>> response = loadOptimized(createRequest("ALL_SHARES", "TRADE_SOURCE_ALL"), taskId);
            
            // Добавляем taskId в ответ
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            if (responseBody != null) {
                responseBody.put("taskId", taskId);
            }
            
            logSystemEvent(taskId, "/api/last-trades/shares", "GET", "COMPLETED", 
                "Загрузка акций успешно завершена", startTime);
            return response;
        } catch (Exception e) {
            logSystemEvent(taskId, "/api/last-trades/shares", "GET", "FAILED", 
                "Ошибка загрузки акций: " + e.getMessage(), startTime);
            throw e;
        }
    }

    /**
     * Загрузка только фьючерсов (оптимизировано)
     */
    @GetMapping("/futures")
    public ResponseEntity<Map<String, Object>> loadFutures() {
        String taskId = "FUTURES_" + UUID.randomUUID().toString().substring(0, 8);
        Instant startTime = Instant.now();
        
        logSystemEvent(taskId, "/api/last-trades/futures", "GET", "STARTED", 
            "Начало загрузки только фьючерсов", startTime);
        
        try {
            ResponseEntity<Map<String, Object>> response = loadOptimized(createRequest("ALL_FUTURES", "TRADE_SOURCE_ALL"), taskId);
            
            // Добавляем taskId в ответ
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            if (responseBody != null) {
                responseBody.put("taskId", taskId);
            }
            
            logSystemEvent(taskId, "/api/last-trades/futures", "GET", "COMPLETED", 
                "Загрузка фьючерсов успешно завершена", startTime);
            return response;
        } catch (Exception e) {
            logSystemEvent(taskId, "/api/last-trades/futures", "GET", "FAILED", 
                "Ошибка загрузки фьючерсов: " + e.getMessage(), startTime);
            throw e;
        }
    }

    /**
     * Создает запрос для загрузки
     */
    private LastTradesRequestDto createRequest(String figis, String tradeSource) {
        LastTradesRequestDto request = new LastTradesRequestDto();
        request.setFigis(java.util.Arrays.asList(figis));
        request.setTradeSource(tradeSource);
        return request;
    }

    /**
     * Информация о производительности executor'ов
     */
    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformance() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> executorInfo = new HashMap<>();
            
            // Информация о lastTradesApiExecutor
            if (lastTradesApiExecutor instanceof org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor) {
                org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor apiExecutor = 
                    (org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor) lastTradesApiExecutor;
                Map<String, Object> apiInfo = new HashMap<>();
                apiInfo.put("corePoolSize", apiExecutor.getCorePoolSize());
                apiInfo.put("maxPoolSize", apiExecutor.getMaxPoolSize());
                apiInfo.put("queueCapacity", apiExecutor.getQueueCapacity());
                apiInfo.put("activeCount", apiExecutor.getActiveCount());
                apiInfo.put("threadNamePrefix", "LastTradesApi-");
                executorInfo.put("lastTradesApiExecutor", apiInfo);
            }
            
            // Информация о lastTradesBatchExecutor
            if (lastTradesBatchExecutor instanceof org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor) {
                org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor batchExecutor = 
                    (org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor) lastTradesBatchExecutor;
                Map<String, Object> batchInfo = new HashMap<>();
                batchInfo.put("corePoolSize", batchExecutor.getCorePoolSize());
                batchInfo.put("maxPoolSize", batchExecutor.getMaxPoolSize());
                batchInfo.put("queueCapacity", batchExecutor.getQueueCapacity());
                batchInfo.put("activeCount", batchExecutor.getActiveCount());
                batchInfo.put("threadNamePrefix", "LastTradesBatch-");
                executorInfo.put("lastTradesBatchExecutor", batchInfo);
            }
            
            // Информация о lastTradesProcessingExecutor
            if (lastTradesProcessingExecutor instanceof org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor) {
                org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor processingExecutor = 
                    (org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor) lastTradesProcessingExecutor;
                Map<String, Object> processingInfo = new HashMap<>();
                processingInfo.put("corePoolSize", processingExecutor.getCorePoolSize());
                processingInfo.put("maxPoolSize", processingExecutor.getMaxPoolSize());
                processingInfo.put("queueCapacity", processingExecutor.getQueueCapacity());
                processingInfo.put("activeCount", processingExecutor.getActiveCount());
                processingInfo.put("threadNamePrefix", "LastTradesProc-");
                executorInfo.put("lastTradesProcessingExecutor", processingInfo);
            }
            
            response.put("success", true);
            response.put("executorInfo", executorInfo);
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("optimizationLevel", "HIGH_PERFORMANCE");
            response.put("tInvestLimits", "Respected with specialized executors");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения информации о производительности: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Логирует системное событие в system_logs
     */
    private void logSystemEvent(String taskId, String endpoint, String method, String status, String message, Instant startTime) {
        try {
            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId(taskId);
            log.setEndpoint(endpoint);
            log.setMethod(method);
            log.setStatus(status);
            log.setMessage(message);
            log.setStartTime(startTime);
            log.setEndTime(Instant.now());
            log.setDurationMs(Instant.now().toEpochMilli() - startTime.toEpochMilli());
            
            systemLogRepository.save(log);
            
            
        } catch (Exception e) {
            log.error("Ошибка сохранения системного лога: {}", e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * Логирует обработку конкретного FIGI в system_logs
     */
    private void logFigiProcessing(String taskId, String figi, String status, String message, Instant startTime) {
        try {
            SystemLogEntity figiLog = new SystemLogEntity();
            figiLog.setTaskId(taskId);
            figiLog.setEndpoint("/api/last-trades/FIGI_PROCESSING");
            figiLog.setMethod("POST");
            figiLog.setStatus(status);
            figiLog.setMessage(message + " | FIGI: " + figi);
            figiLog.setStartTime(startTime);
            figiLog.setEndTime(Instant.now());
            figiLog.setDurationMs(Instant.now().toEpochMilli() - startTime.toEpochMilli());
            
            systemLogRepository.save(figiLog);
            log.info("Лог обработки FIGI сохранен: {} ({})", figi, status);
            
        } catch (Exception e) {
            log.error("Ошибка сохранения лога обработки FIGI {}: {}", figi, e.getMessage(), e);
            e.printStackTrace();
        }
    }
}
