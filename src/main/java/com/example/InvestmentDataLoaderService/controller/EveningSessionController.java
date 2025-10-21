package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.entity.SystemLogEntity;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.MinuteCandleRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.SystemLogRepository;
import com.example.InvestmentDataLoaderService.service.EveningSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Контроллер для работы с ценами вечерней сессии
 */
@RestController
@RequestMapping("/api/evening-session-prices")
public class EveningSessionController {

    private static final Logger logger = LoggerFactory.getLogger(EveningSessionController.class);

    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final MinuteCandleRepository minuteCandleRepository;
    private final EveningSessionService eveningSessionService;
    private final SystemLogRepository systemLogRepository;

    public EveningSessionController(
            ShareRepository shareRepository,
            FutureRepository futureRepository,
            MinuteCandleRepository minuteCandleRepository,
            EveningSessionService eveningSessionService,
            SystemLogRepository systemLogRepository
    ) {
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.minuteCandleRepository = minuteCandleRepository;
        this.eveningSessionService = eveningSessionService;
        this.systemLogRepository = systemLogRepository;
    }

    /**
     * Асинхронная загрузка цен закрытия вечерней сессии за вчерашний день
     * Цена закрытия определяется как close последней минутной свечи в дне
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> loadEveningSessionPrices() {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/evening-session-prices";
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки цен закрытия вечерней сессии за вчерашний день");
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            logger.info("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            logger.error("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            logger.info("=== АСИНХРОННАЯ ЗАГРУЗКА ЦЕН ЗАКРЫТИЯ ВЕЧЕРНЕЙ СЕССИИ ===");
            logger.info("Task ID: " + taskId);

            // Запускаем асинхронное сохранение
            eveningSessionService.loadEveningSessionPricesAsync(taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Асинхронная загрузка цен закрытия вечерней сессии запущена");
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            logger.error("Ошибка запуска асинхронной загрузки цен закрытия вечерней сессии: " + e.getMessage());

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("POST");
            errorLog.setStatus("FAILED");
            errorLog.setMessage("Ошибка запуска асинхронной загрузки цен закрытия вечерней сессии: " + e.getMessage());
            errorLog.setStartTime(startTime);
            errorLog.setEndTime(Instant.now());

            try {
                systemLogRepository.save(errorLog);
            } catch (Exception logException) {
                logger.error("Ошибка сохранения лога ошибки: " + logException.getMessage());
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Ошибка запуска асинхронной загрузки цен закрытия вечерней сессии: " + e.getMessage(),
                "taskId", taskId,
                "error", "InternalServerError"
            ));
        }
    }

    /**
     * Асинхронная загрузка цен вечерней сессии за конкретную дату.
     * Использует данные из minute_candles, обрабатывает только акции и фьючерсы
     */
    @PostMapping("/by-date/{date}")
    public ResponseEntity<Map<String, Object>> loadEveningSessionPricesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request
    ) {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/evening-session-prices/by-date/" + date;
        Instant startTime = Instant.now();
        
        logger.info("=== ПОЛУЧЕН ЗАПРОС НА ДАТУ: {} ===", date);
        logger.info("Request URI: {}, Query String: {}", request.getRequestURI(), request.getQueryString());

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки цен вечерней сессии за дату: " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            logger.info("Лог начала работы сохранен для taskId: {}", taskId);
        } catch (Exception logException) {
            logger.error("Ошибка сохранения лога начала работы: {}", logException.getMessage());
        }

        try {
            logger.info("=== АСИНХРОННАЯ ЗАГРУЗКА ЦЕН ВЕЧЕРНЕЙ СЕССИИ ЗА ДАТУ ===");
            logger.info("Дата: {}, Task ID: {}", date, taskId);

            // Запускаем асинхронное сохранение
            logger.info("[{}] Запускаем асинхронную загрузку...", taskId);
            eveningSessionService.loadEveningSessionPricesForDateAsync(date, taskId);
            logger.info("[{}] Асинхронная загрузка запущена, возвращаем ответ...", taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Асинхронная загрузка цен вечерней сессии за дату запущена");
            response.put("date", date.toString());
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            logger.info("=== ОТПРАВЛЯЕМ ОТВЕТ С ДАТОЙ: {} ===", date);
            logger.info("=== ENDPOINT: {} ===", endpoint);
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            logger.error("Ошибка запуска асинхронной загрузки цен вечерней сессии за дату {}: {}", date, e.getMessage());
            logger.error("Stack trace:", e);
            
            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("POST");
            errorLog.setStatus("FAILED");
            errorLog.setMessage("Ошибка запуска асинхронной загрузки цен вечерней сессии за дату " + date + ": " + e.getMessage());
            errorLog.setStartTime(startTime);
            errorLog.setEndTime(Instant.now());

            try {
                systemLogRepository.save(errorLog);
            } catch (Exception logException) {
                logger.error("Ошибка сохранения лога ошибки: " + logException.getMessage());
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Ошибка запуска асинхронной загрузки цен вечерней сессии за дату " + date + ": " + e.getMessage(),
                "date", date.toString(),
                "taskId", taskId,
                "error", "InternalServerError"
            ));
        }
    }

    /**
     * Асинхронная загрузка цен вечерней сессии для акций за конкретную дату.
     * Использует данные из minute_candles, обрабатывает только акции и сохраняет в БД
     */
    @PostMapping("/shares/{date}")
    public ResponseEntity<Map<String, Object>> saveEveningSessionPricesForShares(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/evening-session-prices/shares/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки цен вечерней сессии для акций за дату: " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            logger.info("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            logger.error("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            logger.info("=== АСИНХРОННАЯ ЗАГРУЗКА ЦЕН ВЕЧЕРНЕЙ СЕССИИ ДЛЯ АКЦИЙ ЗА ДАТУ ===");
            logger.info("Дата: " + date);
            logger.info("Task ID: " + taskId);

            // Запускаем асинхронное сохранение
            eveningSessionService.loadSharesEveningSessionPricesAsync(date, taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Асинхронная загрузка цен вечерней сессии для акций за дату запущена");
            response.put("date", date.toString());
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            logger.error("Ошибка запуска асинхронной загрузки цен вечерней сессии для акций за дату " + date + ": " + e.getMessage());

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("POST");
            errorLog.setStatus("FAILED");
            errorLog.setMessage("Ошибка запуска асинхронной загрузки цен вечерней сессии для акций за дату " + date + ": " + e.getMessage());
            errorLog.setStartTime(startTime);
            errorLog.setEndTime(Instant.now());

            try {
                systemLogRepository.save(errorLog);
            } catch (Exception logException) {
                logger.error("Ошибка сохранения лога ошибки: " + logException.getMessage());
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Ошибка запуска асинхронной загрузки цен вечерней сессии для акций за дату " + date + ": " + e.getMessage(),
                "date", date.toString(),
                "taskId", taskId,
                "error", "InternalServerError"
            ));
        }
    }

    /**
     * Асинхронная загрузка цен вечерней сессии для фьючерсов за конкретную дату.
     * Использует данные из minute_candles, обрабатывает только фьючерсы и сохраняет в БД
     */
    @PostMapping("/futures/{date}")
    public ResponseEntity<Map<String, Object>> saveEveningSessionPricesForFutures(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/evening-session-prices/futures/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки цен вечерней сессии для фьючерсов за дату: " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            logger.info("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            logger.error("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            logger.info("=== АСИНХРОННАЯ ЗАГРУЗКА ЦЕН ВЕЧЕРНЕЙ СЕССИИ ДЛЯ ФЬЮЧЕРСОВ ЗА ДАТУ ===");
            logger.info("Дата: " + date);
            logger.info("Task ID: " + taskId);

            // Запускаем асинхронное сохранение
            eveningSessionService.loadFuturesEveningSessionPricesAsync(date, taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Асинхронная загрузка цен вечерней сессии для фьючерсов за дату запущена");
            response.put("date", date.toString());
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            logger.error("Ошибка запуска асинхронной загрузки цен вечерней сессии для фьючерсов за дату " + date + ": " + e.getMessage());

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("POST");
            errorLog.setStatus("FAILED");
            errorLog.setMessage("Ошибка запуска асинхронной загрузки цен вечерней сессии для фьючерсов за дату " + date + ": " + e.getMessage());
            errorLog.setStartTime(startTime);
            errorLog.setEndTime(Instant.now());

            try {
                systemLogRepository.save(errorLog);
            } catch (Exception logException) {
                logger.error("Ошибка сохранения лога ошибки: " + logException.getMessage());
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Ошибка запуска асинхронной загрузки цен вечерней сессии для фьючерсов за дату " + date + ": " + e.getMessage(),
                "date", date.toString(),
                "taskId", taskId,
                "error", "InternalServerError"
            ));
        }
    }

    /**
     * Асинхронная загрузка цены вечерней сессии по инструменту за конкретную дату.
     * Использует данные из minute_candles и сохраняет в БД
     */
    @PostMapping("/by-figi-date/{figi}/{date}")
    public ResponseEntity<Map<String, Object>> saveEveningSessionPriceByFigiAndDate(
            @PathVariable String figi,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String taskId = UUID.randomUUID().toString();
        String endpoint = "/api/evening-session-prices/by-figi-date/" + figi + "/" + date;
        Instant startTime = Instant.now();

        // Логируем начало работы
        SystemLogEntity startLog = new SystemLogEntity();
        startLog.setTaskId(taskId);
        startLog.setEndpoint(endpoint);
        startLog.setMethod("POST");
        startLog.setStatus("STARTED");
        startLog.setMessage("Начало асинхронной загрузки цены вечерней сессии для инструмента " + figi + " за дату: " + date);
        startLog.setStartTime(startTime);

        try {
            systemLogRepository.save(startLog);
            logger.info("Лог начала работы сохранен для taskId: " + taskId);
        } catch (Exception logException) {
            logger.error("Ошибка сохранения лога начала работы: " + logException.getMessage());
        }

        try {
            logger.info("=== АСИНХРОННАЯ ЗАГРУЗКА ЦЕНЫ ВЕЧЕРНЕЙ СЕССИИ ПО ИНСТРУМЕНТУ ===");
            logger.info("FIGI: " + figi);
            logger.info("Дата: " + date);
            logger.info("Task ID: " + taskId);

            // Запускаем асинхронное сохранение
            eveningSessionService.loadEveningSessionPriceByFigiAsync(figi, date, taskId);

            // НЕ ждем завершения - возвращаем taskId сразу
            Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
            response.put("message", "Асинхронная загрузка цены вечерней сессии для инструмента запущена");
            response.put("figi", figi);
            response.put("date", date.toString());
            response.put("taskId", taskId);
            response.put("endpoint", endpoint);
            response.put("status", "STARTED");
            response.put("startTime", startTime.toString());

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            logger.error("Ошибка запуска асинхронной загрузки цены вечерней сессии для инструмента " + figi + " за дату " + date + ": " + e.getMessage());

            // Логируем ошибку
            SystemLogEntity errorLog = new SystemLogEntity();
            errorLog.setTaskId(taskId);
            errorLog.setEndpoint(endpoint);
            errorLog.setMethod("POST");
            errorLog.setStatus("FAILED");
            errorLog.setMessage("Ошибка запуска асинхронной загрузки цены вечерней сессии для инструмента " + figi + " за дату " + date + ": " + e.getMessage());
            errorLog.setStartTime(startTime);
            errorLog.setEndTime(Instant.now());

            try {
                systemLogRepository.save(errorLog);
            } catch (Exception logException) {
                logger.error("Ошибка сохранения лога ошибки: " + logException.getMessage());
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Ошибка запуска асинхронной загрузки цены вечерней сессии для инструмента " + figi + " за дату " + date + ": " + e.getMessage(),
                "figi", figi,
                "date", date.toString(),
                "taskId", taskId,
                "error", "InternalServerError"
            ));
        }
    }

    // ==================== GET МЕТОДЫ ====================

     /**
     * Получение цен закрытия вечерней сессии за вчерашний день
     * Цена закрытия определяется как close последней минутной свечи в дне
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getEveningSessionClosePricesYesterday() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Получаем вчерашнюю дату
            LocalDate yesterday = LocalDate.now().minusDays(1);
            
            logger.info("Получение цен закрытия вечерней сессии за {}", yesterday);
            
            // Получаем все акции и фьючерсы из БД
            List<ShareEntity> shares = shareRepository.findAll();
            List<FutureEntity> futures = futureRepository.findAll();
            
            List<Map<String, Object>> eveningClosePrices = new ArrayList<>();
            int totalProcessed = 0;
            int foundPrices = 0;
            int missingData = 0;
            
            // Обрабатываем акции
            for (ShareEntity share : shares) {
                try {
                    totalProcessed++;
                    
                    // Получаем последнюю свечу за день для акции
                    var lastCandle = minuteCandleRepository.findLastCandleForDate(share.getFigi(), yesterday);
                    
                    if (lastCandle != null) {
                        BigDecimal lastClosePrice = lastCandle.getClose();
                        
                        // Проверяем, что цена не равна 0 (невалидная цена)
                        if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                            Map<String, Object> priceData = new HashMap<>();
                            priceData.put("figi", share.getFigi());
                            priceData.put("ticker", share.getTicker());
                            priceData.put("name", share.getName());
                            priceData.put("priceDate", yesterday.toString());
                            priceData.put("closePrice", lastClosePrice);
                            priceData.put("instrumentType", "SHARE");
                            priceData.put("currency", "RUB");
                            priceData.put("exchange", "MOEX");
                            
                            eveningClosePrices.add(priceData);
                            foundPrices++;
                        } else {
                            missingData++;
                        }
                    } else {
                        missingData++;
                    }
                } catch (Exception e) {
                    logger.error("Ошибка обработки акции " + share.getTicker() + ": " + e.getMessage());
                    missingData++;
                }
            }
            
            // Обрабатываем фьючерсы
            for (FutureEntity future : futures) {
                try {
                    totalProcessed++;
                    
                    // Получаем последнюю свечу за день для фьючерса
                    var lastCandle = minuteCandleRepository.findLastCandleForDate(future.getFigi(), yesterday);
                    
                    if (lastCandle != null) {
                        BigDecimal lastClosePrice = lastCandle.getClose();
                        
                        // Проверяем, что цена не равна 0 (невалидная цена)
                        if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                            Map<String, Object> priceData = new HashMap<>();
                            priceData.put("figi", future.getFigi());
                            priceData.put("ticker", future.getTicker());
                            priceData.put("name", future.getTicker());
                            priceData.put("priceDate", yesterday.toString());
                            priceData.put("closePrice", lastClosePrice);
                            priceData.put("instrumentType", "FUTURE");
                            priceData.put("currency", "RUB");
                            priceData.put("exchange", "MOEX");
                            
                            eveningClosePrices.add(priceData);
                            foundPrices++;
                        } else {
                            missingData++;
                        }
                    } else {
                        missingData++;
                    }
                } catch (Exception e) {
                    logger.error("Ошибка обработки фьючерса " + future.getTicker() + ": " + e.getMessage());
                    missingData++;
                }
            }
            
            response.put("success", true);
            response.put("message", "Цены закрытия вечерней сессии за " + yesterday + " получены успешно");
            response.put("data", eveningClosePrices);
            response.put("count", eveningClosePrices.size());
            response.put("date", yesterday.toString());
            response.put("statistics", Map.of(
                "totalProcessed", totalProcessed,
                "foundPrices", foundPrices,
                "missingData", missingData
            ));
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения цен закрытия вечерней сессии за вчера: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Получение цен вечерней сессии для всех инструментов (акции + фьючерсы) за конкретную дату.
     * Использует данные из minute_candles
     */
    @GetMapping("/by-date/{date}")
    public ResponseEntity<Map<String, Object>> getEveningSessionPricesForAllInstruments(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        logger.info("=== GET МЕТОД ВЫЗВАН С ДАТОЙ: {} ===", date);
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Получаем все акции и фьючерсы из БД
            List<ShareEntity> shares = shareRepository.findAll();
            List<FutureEntity> futures = futureRepository.findAll();
            
            List<Map<String, Object>> eveningPrices = new ArrayList<>();
            int totalProcessed = 0;
            int foundPrices = 0;
            int missingData = 0;
            
            // Обрабатываем акции
            for (ShareEntity share : shares) {
                try {
                    totalProcessed++;
                    
                    // Получаем последнюю свечу за день для акции
                    var lastCandle = minuteCandleRepository.findLastCandleForDate(share.getFigi(), date);
                    
                    if (lastCandle != null) {
                        BigDecimal lastClosePrice = lastCandle.getClose();
                        
                        // Проверяем, что цена не равна 0 (невалидная цена)
                        if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                            Map<String, Object> priceData = new HashMap<>();
                            priceData.put("figi", share.getFigi());
                            priceData.put("ticker", share.getTicker());
                            priceData.put("name", share.getName());
                            priceData.put("priceDate", date.toString());
                            priceData.put("closePrice", lastClosePrice);
                            priceData.put("instrumentType", "SHARE");
                            priceData.put("currency", "RUB");
                            priceData.put("exchange", "MOEX");
                            
                            eveningPrices.add(priceData);
                            foundPrices++;
                        } else {
                            missingData++;
                        }
                    } else {
                        missingData++;
                    }
                } catch (Exception e) {
                    logger.error("Ошибка обработки акции " + share.getTicker() + ": " + e.getMessage());
                    missingData++;
                }
            }
            
            // Обрабатываем фьючерсы
            for (FutureEntity future : futures) {
                try {
                    totalProcessed++;
                    
                    // Получаем последнюю свечу за день для фьючерса
                    var lastCandle = minuteCandleRepository.findLastCandleForDate(future.getFigi(), date);
                    
                    if (lastCandle != null) {
                        BigDecimal lastClosePrice = lastCandle.getClose();
                        
                        // Проверяем, что цена не равна 0 (невалидная цена)
                        if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                            Map<String, Object> priceData = new HashMap<>();
                            priceData.put("figi", future.getFigi());
                            priceData.put("ticker", future.getTicker());
                            priceData.put("name", future.getTicker());
                            priceData.put("priceDate", date.toString());
                            priceData.put("closePrice", lastClosePrice);
                            priceData.put("instrumentType", "FUTURE");
                            priceData.put("currency", "RUB");
                            priceData.put("exchange", "MOEX");
                            
                            eveningPrices.add(priceData);
                            foundPrices++;
                        } else {
                            missingData++;
                        }
                    } else {
                        missingData++;
                    }
                } catch (Exception e) {
                    logger.error("Ошибка обработки фьючерса " + future.getTicker() + ": " + e.getMessage());
                    missingData++;
                }
            }
            
            response.put("success", true);
            response.put("message", "Цены вечерней сессии для всех инструментов за " + date + " получены успешно");
            response.put("data", eveningPrices);
            response.put("count", eveningPrices.size());
            response.put("date", date.toString());
            response.put("totalProcessed", totalProcessed);
            response.put("foundPrices", foundPrices);
            response.put("missingData", missingData);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения цен вечерней сессии для всех инструментов за " + date + ": " + e.getMessage());
            response.put("date", date.toString());
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Получение цен вечерней сессии для всех акций за конкретную дату.
     * Использует данные из minute_candles, обрабатывает только акции
     */
    @GetMapping("/shares/{date}")
    public ResponseEntity<Map<String, Object>> getEveningSessionPricesForShares(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Получаем все акции из БД
            List<ShareEntity> shares = shareRepository.findAll();
            
            List<Map<String, Object>> eveningPrices = new ArrayList<>();
            int totalProcessed = 0;
            int foundPrices = 0;
            int missingData = 0;
            
            // Обрабатываем акции
            for (ShareEntity share : shares) {
                try {
                    totalProcessed++;
                    
                    // Получаем последнюю свечу за день для акции
                    var lastCandle = minuteCandleRepository.findLastCandleForDate(share.getFigi(), date);
                    
                    if (lastCandle != null) {
                        BigDecimal lastClosePrice = lastCandle.getClose();
                        
                        // Проверяем, что цена не равна 0 (невалидная цена)
                        if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                            Map<String, Object> priceData = new HashMap<>();
                            priceData.put("figi", share.getFigi());
                            priceData.put("ticker", share.getTicker());
                            priceData.put("name", share.getName());
                            priceData.put("priceDate", date.toString());
                            priceData.put("closePrice", lastClosePrice);
                            priceData.put("instrumentType", "SHARE");
                            priceData.put("currency", "RUB");
                            priceData.put("exchange", "MOEX");
                            
                            eveningPrices.add(priceData);
                            foundPrices++;
                        } else {
                            missingData++;
                        }
                    } else {
                        missingData++;
                    }
                } catch (Exception e) {
                    logger.error("Ошибка обработки акции " + share.getTicker() + ": " + e.getMessage());
                    missingData++;
                }
            }
            
            response.put("success", true);
            response.put("message", "Цены вечерней сессии для акций за " + date + " получены успешно");
            response.put("data", eveningPrices);
            response.put("count", eveningPrices.size());
            response.put("date", date.toString());
            response.put("totalProcessed", totalProcessed);
            response.put("foundPrices", foundPrices);
            response.put("missingData", missingData);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения цен вечерней сессии для акций за " + date + ": " + e.getMessage());
            response.put("date", date.toString());
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Получение цен вечерней сессии для всех фьючерсов за конкретную дату.
     * Использует данные из minute_candles, обрабатывает только фьючерсы
     */
    @GetMapping("/futures/{date}")
    public ResponseEntity<Map<String, Object>> getEveningSessionPricesForFutures(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Получаем все фьючерсы из БД
            List<FutureEntity> futures = futureRepository.findAll();
            
            List<Map<String, Object>> eveningPrices = new ArrayList<>();
            int totalProcessed = 0;
            int foundPrices = 0;
            int missingData = 0;
            
            // Обрабатываем фьючерсы
            for (FutureEntity future : futures) {
                try {
                    totalProcessed++;
                    
                    // Получаем последнюю свечу за день для фьючерса
                    var lastCandle = minuteCandleRepository.findLastCandleForDate(future.getFigi(), date);
                    
                    if (lastCandle != null) {
                        BigDecimal lastClosePrice = lastCandle.getClose();
                        
                        // Проверяем, что цена не равна 0 (невалидная цена)
                        if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                            Map<String, Object> priceData = new HashMap<>();
                            priceData.put("figi", future.getFigi());
                            priceData.put("ticker", future.getTicker());
                            priceData.put("name", future.getTicker());
                            priceData.put("priceDate", date.toString());
                            priceData.put("closePrice", lastClosePrice);
                            priceData.put("instrumentType", "FUTURE");
                            priceData.put("currency", "RUB");
                            priceData.put("exchange", "MOEX");
                            
                            eveningPrices.add(priceData);
                            foundPrices++;
                        } else {
                            missingData++;
                        }
                    } else {
                        missingData++;
                    }
                } catch (Exception e) {
                    logger.error("Ошибка обработки фьючерса " + future.getTicker() + ": " + e.getMessage());
                    missingData++;
                }
            }
            
            response.put("success", true);
            response.put("message", "Цены вечерней сессии для фьючерсов за " + date + " получены успешно");
            response.put("data", eveningPrices);
            response.put("count", eveningPrices.size());
            response.put("date", date.toString());
            response.put("totalProcessed", totalProcessed);
            response.put("foundPrices", foundPrices);
            response.put("missingData", missingData);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения цен вечерней сессии для фьючерсов за " + date + ": " + e.getMessage());
            response.put("date", date.toString());
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

     /**
     * Получение цены вечерней сессии по инструменту за конкретную дату.
     * Использует данные из minute_candles
     */
    @GetMapping("/by-figi-date/{figi}/{date}")
    public ResponseEntity<Map<String, Object>> getEveningSessionPriceByFigiAndDate(
            @PathVariable String figi,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Получаем последнюю свечу за день для инструмента
            var lastCandle = minuteCandleRepository.findLastCandleForDate(figi, date);
            
            if (lastCandle != null) {
                BigDecimal lastClosePrice = lastCandle.getClose();
                
                // Проверяем, что цена не равна 0 (невалидная цена)
                if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                    Map<String, Object> priceData = new HashMap<>();
                    priceData.put("figi", figi);
                    priceData.put("priceDate", date.toString());
                    priceData.put("closePrice", lastClosePrice);
                    priceData.put("instrumentType", "UNKNOWN"); // Не знаем тип без дополнительного запроса
                    priceData.put("currency", "RUB");
                    priceData.put("exchange", "MOEX");
                    
                    response.put("success", true);
                    response.put("message", "Цена вечерней сессии для инструмента " + figi + " за " + date + " получена успешно");
                    response.put("data", priceData);
                    response.put("figi", figi);
                    response.put("date", date.toString());
                    response.put("timestamp", LocalDateTime.now().toString());
                    
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", "Невалидная цена закрытия для инструмента " + figi + " за " + date);
                    response.put("figi", figi);
                    response.put("date", date.toString());
                    response.put("timestamp", LocalDateTime.now().toString());
                    return ResponseEntity.ok(response);
                }
            } else {
                response.put("success", false);
                response.put("message", "Цена вечерней сессии не найдена для инструмента " + figi + " за " + date);
                response.put("figi", figi);
                response.put("date", date.toString());
                response.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения цены вечерней сессии для инструмента " + figi + " за " + date + ": " + e.getMessage());
            response.put("figi", figi);
            response.put("date", date.toString());
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}