package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.SystemLogEntity;
import com.example.InvestmentDataLoaderService.exception.DataLoadException;
import com.example.InvestmentDataLoaderService.service.MainSessionPriceService;
import com.example.InvestmentDataLoaderService.repository.SystemLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Контроллер для работы с ценами основной сессии
 * Управляет загрузкой цен основной торговой сессии и цен закрытия
 */
@RestController
@RequestMapping("/api/main-session-prices")
public class MainSessionPricesController {

    private static final Logger log = LoggerFactory.getLogger(MainSessionPricesController.class);
    private final MainSessionPriceService mainSessionPriceService;
    private final SystemLogRepository systemLogRepository;

    public MainSessionPricesController(MainSessionPriceService mainSessionPriceService,
                                      SystemLogRepository systemLogRepository) {
        this.mainSessionPriceService = mainSessionPriceService;
        this.systemLogRepository = systemLogRepository;
    }

    // ==================== ЦЕНЫ ЗАКРЫТИЯ ОСНОВНОЙ СЕССИИ ====================

    /**
     * Асинхронная загрузка цен закрытия основной сессии для акций и фьючерсов из T-INVEST API в БД
     * Работает с акциями и фьючерсами одновременно, использует кэш и T-INVEST API
     */
    @PostMapping("/")
    public ResponseEntity<Map<String, Object>> loadClosePricesToday() {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/main-session-prices/";
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки цен закрытия основной сессии");
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            log.info("Лог начала работы сохранен для taskId: {}", taskId);
        } catch (Exception logException) {
            log.error("Ошибка сохранения лога начала работы для taskId: {}: {}", taskId, logException.getMessage(), logException);
        }

        try {
            log.info("=== АСИНХРОННАЯ ЗАГРУЗКА ЦЕН ЗАКРЫТИЯ ДЛЯ АКЦИЙ И ФЬЮЧЕРСОВ ===");
            log.info("Task ID: {}", taskId);

            // Запускаем асинхронное сохранение
            mainSessionPriceService.saveClosePricesAsync(taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Асинхронная загрузка цен закрытия основной сессии запущена");
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            log.error("Ошибка запуска асинхронной загрузки цен закрытия основной сессии: {}", e.getMessage(), e);

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("POST");
            errorLog.setStatus("FAILED");
            errorLog.setMessage("Ошибка запуска асинхронной загрузки цен закрытия основной сессии: " + e.getMessage());
            errorLog.setStartTime(startTime);
            errorLog.setEndTime(Instant.now());

            try {
                systemLogRepository.save(errorLog);
            } catch (Exception logException) {
                log.error("Ошибка сохранения лога ошибки для taskId: {}: {}", taskId, logException.getMessage(), logException);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Ошибка запуска асинхронной загрузки цен закрытия основной сессии: " + e.getMessage(),
                "taskId", taskId,
                "error", "InternalServerError"
            ));
        }
    }


    /**
     * Получение цен закрытия для всех акций из T-INVEST API
     */
    @GetMapping("/shares")
    public ResponseEntity<Map<String, Object>> getClosePricesForShares() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ClosePriceDto> allClosePrices = mainSessionPriceService.getClosePricesForAllShares();
            
            // Фильтруем неверные цены (с датой 1970-01-01)
            List<ClosePriceDto> validClosePrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("message", "Цены закрытия для акций получены успешно");
            response.put("data", validClosePrices);
            response.put("count", validClosePrices.size());
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Исключение будет обработано GlobalExceptionHandler
            throw new DataLoadException("Ошибка получения цен закрытия для акций: " + e.getMessage(), e);
        }
    }

    /**
     * Асинхронная загрузка цен закрытия для всех акций из T-INVEST API в БД
     */
    @PostMapping("/shares")
    public ResponseEntity<Map<String, Object>> loadClosePricesForShares() {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/main-session-prices/shares";
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки цен закрытия для акций");
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            log.info("Лог начала работы сохранен для taskId: {}", taskId);
        } catch (Exception logException) {
            log.error("Ошибка сохранения лога начала работы для taskId: {}: {}", taskId, logException.getMessage(), logException);
        }

        try {
            log.info("=== АСИНХРОННАЯ ЗАГРУЗКА ЦЕН ЗАКРЫТИЯ ДЛЯ АКЦИЙ ===");
            log.info("Task ID: {}", taskId);

            // Запускаем асинхронное сохранение
            mainSessionPriceService.saveSharesClosePricesAsync(taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Асинхронная загрузка цен закрытия для акций запущена");
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            log.error("Ошибка запуска асинхронной загрузки цен закрытия для акций: {}", e.getMessage(), e);
            
            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("POST");
            errorLog.setStatus("FAILED");
            errorLog.setMessage("Ошибка запуска асинхронной загрузки цен закрытия для акций: " + e.getMessage());
            errorLog.setStartTime(startTime);
            errorLog.setEndTime(Instant.now());

            try {
                systemLogRepository.save(errorLog);
            } catch (Exception logException) {
                log.error("Ошибка сохранения лога ошибки для taskId: {}: {}", taskId, logException.getMessage(), logException);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Ошибка запуска асинхронной загрузки цен закрытия для акций: " + e.getMessage(),
                "taskId", taskId,
                "error", "InternalServerError"
            ));
        }
    }


    /**
     * Получение цен закрытия для всех фьючерсов из T-INVEST API
     */
    @GetMapping("/futures")
    public ResponseEntity<Map<String, Object>> getClosePricesForFutures() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ClosePriceDto> allClosePrices = mainSessionPriceService.getClosePricesForAllFutures();
            
            // Фильтруем неверные цены (с датой 1970-01-01)
            List<ClosePriceDto> validClosePrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("message", "Цены закрытия для фьючерсов получены успешно");
            response.put("data", validClosePrices);
            response.put("count", validClosePrices.size());
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Исключение будет обработано GlobalExceptionHandler
            throw new DataLoadException("Ошибка получения цен закрытия для фьючерсов: " + e.getMessage(), e);
        }
    }

    /**
     * Асинхронная загрузка цен закрытия для всех фьючерсов из T-INVEST API в БД
     */
    @PostMapping("/futures")
    public ResponseEntity<Map<String, Object>> loadClosePricesForFutures() {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/main-session-prices/futures";
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки цен закрытия для фьючерсов");
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            log.info("Лог начала работы сохранен для taskId: {}", taskId);
        } catch (Exception logException) {
            log.error("Ошибка сохранения лога начала работы для taskId: {}: {}", taskId, logException.getMessage(), logException);
        }

        try {
            log.info("=== АСИНХРОННАЯ ЗАГРУЗКА ЦЕН ЗАКРЫТИЯ ДЛЯ ФЬЮЧЕРСОВ ===");
            log.info("Task ID: {}", taskId);

            // Запускаем асинхронное сохранение
            mainSessionPriceService.saveFuturesClosePricesAsync(taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Асинхронная загрузка цен закрытия для фьючерсов запущена");
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            log.error("Ошибка запуска асинхронной загрузки цен закрытия для фьючерсов: {}", e.getMessage(), e);
            
            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("POST");
            errorLog.setStatus("FAILED");
            errorLog.setMessage("Ошибка запуска асинхронной загрузки цен закрытия для фьючерсов: " + e.getMessage());
            errorLog.setStartTime(startTime);
            errorLog.setEndTime(Instant.now());

            try {
                systemLogRepository.save(errorLog);
            } catch (Exception logException) {
                log.error("Ошибка сохранения лога ошибки для taskId: {}: {}", taskId, logException.getMessage(), logException);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Ошибка запуска асинхронной загрузки цен закрытия для фьючерсов: " + e.getMessage(),
                "taskId", taskId,
                "error", "InternalServerError"
            ));
        }
    }

    /**
     * Получение цены закрытия по инструменту из T-INVEST API
     */
    @GetMapping("/by-figi/{figi}")
    public ResponseEntity<Map<String, Object>> getClosePriceByFigi(@PathVariable String figi) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ClosePriceDto> allClosePrices = mainSessionPriceService.getClosePrices(List.of(figi), null);
            
            // Фильтруем неверные цены (с датой 1970-01-01)
            List<ClosePriceDto> validClosePrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .collect(Collectors.toList());
            
            if (validClosePrices.isEmpty()) {
                response.put("success", false);
                response.put("message", "Цена закрытия не найдена для инструмента: " + figi);
                response.put("figi", figi);
                response.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.ok(response);
            }
            
            ClosePriceDto closePrice = validClosePrices.get(0);
            response.put("success", true);
            response.put("message", "Цена закрытия получена успешно");
            response.put("data", closePrice);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения цены закрытия: " + e.getMessage());
            response.put("figi", figi);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Асинхронная загрузка цены закрытия по конкретному инструменту из T-INVEST API в БД
     */
    @PostMapping("/instrument/{figi}")
    public ResponseEntity<Map<String, Object>> loadClosePriceByFigi(@PathVariable String figi) {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/main-session-prices/instrument/" + figi;
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки цены закрытия для инструмента: " + figi);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            log.info("Лог начала работы сохранен для taskId: {}", taskId);
        } catch (Exception logException) {
            log.error("Ошибка сохранения лога начала работы для taskId: {}: {}", taskId, logException.getMessage(), logException);
        }

        try {
            log.info("=== АСИНХРОННАЯ ЗАГРУЗКА ЦЕНЫ ЗАКРЫТИЯ ДЛЯ ИНСТРУМЕНТА ===");
            log.info("FIGI: {}", figi);
            log.info("Task ID: {}", taskId);

            // Запускаем асинхронное сохранение
            mainSessionPriceService.saveInstrumentClosePriceAsync(figi, taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Асинхронная загрузка цены закрытия для инструмента запущена");
            response.put("figi", figi);
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            log.error("Ошибка запуска асинхронной загрузки цены закрытия для инструмента {}: {}", figi, e.getMessage(), e);
            
            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("POST");
            errorLog.setStatus("FAILED");
            errorLog.setMessage("Ошибка запуска асинхронной загрузки цены закрытия для инструмента " + figi + ": " + e.getMessage());
            errorLog.setStartTime(startTime);
            errorLog.setEndTime(Instant.now());

            try {
                systemLogRepository.save(errorLog);
            } catch (Exception logException) {
                log.error("Ошибка сохранения лога ошибки для taskId: {}: {}", taskId, logException.getMessage(), logException);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Ошибка запуска асинхронной загрузки цены закрытия для инструмента " + figi + ": " + e.getMessage(),
                "figi", figi,
                "taskId", taskId,
                "error", "InternalServerError"
            ));
        }
    }

    // ==================== ЦЕНЫ ОСНОВНОЙ СЕССИИ ====================

    /**
     * Асинхронная загрузка цен основной сессии за дату
     * Работает только с рабочими днями, использует данные из minute_candles
     * Обрабатывает только акции и фьючерсы
     */
    @PostMapping("/by-date/{date}")
    public ResponseEntity<Map<String, Object>> loadMainSessionPricesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/main-session-prices/by-date/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки цен основной сессии за дату: " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            log.info("Лог начала работы сохранен для taskId: {}", taskId);
        } catch (Exception logException) {
            log.error("Ошибка сохранения лога начала работы для taskId: {}: {}", taskId, logException.getMessage(), logException);
        }

        try {
            log.info("=== АСИНХРОННАЯ ЗАГРУЗКА ЦЕН ОСНОВНОЙ СЕССИИ ===");
            log.info("Дата: {}", date);
            log.info("Task ID: {}", taskId);

            // Запускаем асинхронное сохранение
            mainSessionPriceService.saveMainSessionPricesForDateAsync(date, taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Асинхронная загрузка цен основной сессии за дату запущена");
            response.put("date", date.toString());
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            log.error("Ошибка запуска асинхронной загрузки цен основной сессии за дату {}: {}", date, e.getMessage(), e);
            
            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("POST");
            errorLog.setStatus("FAILED");
            errorLog.setMessage("Ошибка запуска асинхронной загрузки цен основной сессии за дату " + date + ": " + e.getMessage());
            errorLog.setStartTime(startTime);
            errorLog.setEndTime(Instant.now());

            try {
                systemLogRepository.save(errorLog);
            } catch (Exception logException) {
                log.error("Ошибка сохранения лога ошибки для taskId: {}: {}", taskId, logException.getMessage(), logException);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Ошибка запуска асинхронной загрузки цен основной сессии за дату " + date + ": " + e.getMessage(),
                "date", date.toString(),
                "taskId", taskId,
                "error", "InternalServerError"
            ));
        }
    }

}