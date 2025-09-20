package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.IndicativeEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.exception.DataLoadException;
import com.example.InvestmentDataLoaderService.service.TInvestService;
import com.example.InvestmentDataLoaderService.service.MinuteCandleService;
import com.example.InvestmentDataLoaderService.service.DailyCandleService;
import com.example.InvestmentDataLoaderService.service.MarketDataService;
import com.example.InvestmentDataLoaderService.repository.IndicativeRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.scheduler.EveningSessionService;
import com.example.InvestmentDataLoaderService.scheduler.MorningSessionService;
import com.example.InvestmentDataLoaderService.scheduler.LastTradesService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Контроллер для загрузки и управления данными
 * Объединяет функциональность загрузки свечей, цен, сделок и других торговых данных
 */
@RestController
@RequestMapping("/api/data-loading")
public class DataLoadingController {

    private final TInvestService service;
    private final MinuteCandleService minuteCandleService;
    private final DailyCandleService dailyCandleService;
    private final MarketDataService marketDataService;
    private final IndicativeRepository indicativeRepository;
    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final EveningSessionService eveningSessionService;
    private final MorningSessionService morningSessionService;
    private final LastTradesService lastTradesService;

    public DataLoadingController(TInvestService service,
                               MinuteCandleService minuteCandleService,
                               DailyCandleService dailyCandleService,
                               MarketDataService marketDataService,
                               IndicativeRepository indicativeRepository,
                               ShareRepository shareRepository,
                               FutureRepository futureRepository,
                               EveningSessionService eveningSessionService,
                               MorningSessionService morningSessionService,
                               LastTradesService lastTradesService) {
        this.service = service;
        this.minuteCandleService = minuteCandleService;
        this.dailyCandleService = dailyCandleService;
        this.marketDataService = marketDataService;
        this.indicativeRepository = indicativeRepository;
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.eveningSessionService = eveningSessionService;
        this.morningSessionService = morningSessionService;
        this.lastTradesService = lastTradesService;
    }

    // ==================== СВЕЧИ ====================

    /**
     * Загрузка минутных свечей за сегодня (или указанную дату)
     */
    @PostMapping("/candles/minute")
    public ResponseEntity<CandleLoadResponseDto> loadMinuteCandlesToday(@RequestBody(required = false) MinuteCandleRequestDto request) {
        // Если request null, создаем пустой объект
        if (request == null) {
            request = new MinuteCandleRequestDto();
        }
        
        // Генерируем уникальный ID задачи
        String taskId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        
        // Запускаем загрузку в фоновом режиме
        minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        
        // Немедленно возвращаем ответ о запуске
        CandleLoadResponseDto response = new CandleLoadResponseDto(
            true,
            "Загрузка минутных свечей запущена в фоновом режиме",
            startTime,
            taskId
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Загрузка дневных свечей за сегодня (или указанную дату)
     */
    @PostMapping("/candles/daily")
    public ResponseEntity<CandleLoadResponseDto> loadDailyCandlesToday(@RequestBody(required = false) DailyCandleRequestDto request) {
        // Если request null, создаем пустой объект
        if (request == null) {
            request = new DailyCandleRequestDto();
        }
        
        // Генерируем уникальный ID задачи
        String taskId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        
        // Запускаем загрузку в фоновом режиме
        dailyCandleService.saveDailyCandlesAsync(request, taskId);
        
        // Немедленно возвращаем ответ о запуске
        CandleLoadResponseDto response = new CandleLoadResponseDto(
            true,
            "Загрузка дневных свечей запущена в фоновом режиме",
            startTime,
            taskId
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Загрузка минутных свечей за конкретную дату
     */
    @PostMapping("/candles/minute/{date}")
    public ResponseEntity<CandleLoadResponseDto> loadMinuteCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // Создаем запрос для минутных свечей
        MinuteCandleRequestDto request = new MinuteCandleRequestDto();
        request.setDate(date);
        
        // Генерируем уникальный ID задачи
        String taskId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        
        // Запускаем загрузку в фоновом режиме
        minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        
        // Немедленно возвращаем ответ о запуске
        CandleLoadResponseDto response = new CandleLoadResponseDto(
            true,
            "Загрузка минутных свечей запущена для " + date,
            startTime,
            taskId
        );
            
            return ResponseEntity.ok(response);
    }

    /**
     * Загрузка дневных свечей за конкретную дату
     */
    @PostMapping("/candles/daily/{date}")
    public ResponseEntity<CandleLoadResponseDto> loadDailyCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // Создаем запрос для дневных свечей
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setDate(date);
        
        // Генерируем уникальный ID задачи
        String taskId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        
        // Запускаем загрузку в фоновом режиме
        dailyCandleService.saveDailyCandlesAsync(request, taskId);
        
        // Немедленно возвращаем ответ о запуске
        CandleLoadResponseDto response = new CandleLoadResponseDto(
            true,
            "Загрузка дневных свечей запущена для " + date,
            startTime,
            taskId
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Загрузка минутных свечей акций за дату
     */
    @PostMapping("/candles/shares/minute/{date}")
    public ResponseEntity<CandleLoadResponseDto> loadSharesMinuteCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // Создаем запрос для минутных свечей акций
        MinuteCandleRequestDto request = new MinuteCandleRequestDto();
        request.setDate(date);
        request.setAssetType(List.of("SHARES"));
        
        // Генерируем уникальный ID задачи
        String taskId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        
        // Запускаем загрузку в фоновом режиме
        minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        
        // Немедленно возвращаем ответ о запуске
        CandleLoadResponseDto response = new CandleLoadResponseDto(
            true,
            "Загрузка минутных свечей акций запущена для " + date,
            startTime,
            taskId
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Получение минутных свечей акций за дату без сохранения
     */
    @GetMapping("/candles/shares/minute/{date}")
    public ResponseEntity<Map<String, Object>> getSharesMinuteCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            System.out.println("=== ПОЛУЧЕНИЕ МИНУТНЫХ СВЕЧЕЙ АКЦИЙ ===");
            System.out.println("Дата: " + date);
            
            // Получаем все акции из БД
            List<ShareEntity> shares = shareRepository.findAll();
            System.out.println("Найдено акций: " + shares.size());
            
            if (shares.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Акции не найдены в базе данных");
                response.put("date", date);
                response.put("candles", new ArrayList<>());
                return ResponseEntity.ok(response);
            }
            
            // Собираем все свечи от API
            List<Map<String, Object>> allCandles = new ArrayList<>();
            List<Map<String, Object>> failedInstruments = new ArrayList<>();
            List<Map<String, Object>> noDataInstruments = new ArrayList<>();
            List<Map<String, Object>> errorInstruments = new ArrayList<>();
            
            int totalInstruments = shares.size();
            int processedInstruments = 0;
            int successfulInstruments = 0;
            int noDataCount = 0;
            int errorCount = 0;
            int totalCandlesCount = 0;
            
            long startProcessingTime = System.currentTimeMillis();
            
            for (ShareEntity share : shares) {
                long instrumentStartTime = System.currentTimeMillis();
                
                try {
                    System.out.println("Обрабатываем акцию: " + share.getFigi() + " - " + share.getName());
                    
                    // Получаем минутные свечи из API
                    List<CandleDto> candles = marketDataService.getCandles(share.getFigi(), date, "CANDLE_INTERVAL_1_MIN");
                    
                    if (candles != null && !candles.isEmpty()) {
                        System.out.println("Получено " + candles.size() + " свечей для " + share.getFigi());
                        
                        // Преобразуем свечи в формат для ответа
                        for (CandleDto candle : candles) {
                            Map<String, Object> candleData = new HashMap<>();
                            candleData.put("figi", candle.figi());
                            candleData.put("instrumentName", share.getName());
                            candleData.put("volume", candle.volume());
                            candleData.put("high", candle.high());
                            candleData.put("low", candle.low());
                            candleData.put("open", candle.open());
                            candleData.put("close", candle.close());
                            candleData.put("time", candle.time());
                            candleData.put("isComplete", candle.isComplete());
                            
                            allCandles.add(candleData);
                        }
                        
                        totalCandlesCount += candles.size();
                        successfulInstruments++;
                        
                        System.out.println("Успешно обработан " + share.getFigi() + " за " + (System.currentTimeMillis() - instrumentStartTime) + "мс");
                    } else {
                        System.out.println("Нет данных для акции: " + share.getFigi());
                        noDataCount++;
                        
                        // Записываем информацию об инструменте без данных
                        Map<String, Object> noDataInfo = new HashMap<>();
                        noDataInfo.put("figi", share.getFigi());
                        noDataInfo.put("name", share.getName());
                        noDataInfo.put("reason", "Нет торговых данных за указанную дату");
                        noDataInfo.put("processingTimeMs", System.currentTimeMillis() - instrumentStartTime);
                        noDataInstruments.add(noDataInfo);
                    }
                    
                    processedInstruments++;
                    
                    // Небольшая задержка между запросами к API
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    System.err.println("Ошибка при получении данных для " + share.getFigi() + ": " + e.getMessage());
                    errorCount++;
                    processedInstruments++;
                    
                    // Определяем тип ошибки
                    String errorType = "UNKNOWN_ERROR";
                    String errorReason = e.getMessage();
                    
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("DEADLINE_EXCEEDED")) {
                            errorType = "TIMEOUT";
                            errorReason = "Превышено время ожидания ответа от API";
                        } else if (e.getMessage().contains("UNAVAILABLE")) {
                            errorType = "SERVICE_UNAVAILABLE";
                            errorReason = "Сервис API временно недоступен";
                        } else if (e.getMessage().contains("PERMISSION_DENIED")) {
                            errorType = "PERMISSION_DENIED";
                            errorReason = "Нет доступа к инструменту";
                        } else if (e.getMessage().contains("INVALID_ARGUMENT")) {
                            errorType = "INVALID_ARGUMENT";
                            errorReason = "Неверный аргумент запроса";
                        }
                    }
                    
                    // Записываем информацию об ошибке
                    Map<String, Object> errorInfo = new HashMap<>();
                    errorInfo.put("figi", share.getFigi());
                    errorInfo.put("name", share.getName());
                    errorInfo.put("errorType", errorType);
                    errorInfo.put("reason", errorReason);
                    errorInfo.put("processingTimeMs", System.currentTimeMillis() - instrumentStartTime);
                    errorInstruments.add(errorInfo);
                }
            }
            
            long totalProcessingTime = System.currentTimeMillis() - startProcessingTime;
            
            // Формируем детальную статистику
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalInstruments", totalInstruments);
            statistics.put("processedInstruments", processedInstruments);
            statistics.put("successfulInstruments", successfulInstruments);
            statistics.put("noDataInstruments", noDataCount);
            statistics.put("errorInstruments", errorCount);
            statistics.put("successRate", totalInstruments > 0 ? (double) successfulInstruments / totalInstruments * 100 : 0);
            statistics.put("totalProcessingTimeMs", totalProcessingTime);
            statistics.put("averageProcessingTimePerInstrumentMs", processedInstruments > 0 ? totalProcessingTime / processedInstruments : 0);
            
            // Формируем ответ
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Данные получены успешно");
            response.put("date", date);
            response.put("statistics", statistics);
            response.put("totalCandles", totalCandlesCount);
            response.put("candles", allCandles);
            response.put("failedInstruments", failedInstruments);
            response.put("noDataInstruments", noDataInstruments);
            response.put("errorInstruments", errorInstruments);
            
            System.out.println("=== ЗАВЕРШЕНИЕ ПОЛУЧЕНИЯ МИНУТНЫХ СВЕЧЕЙ АКЦИЙ ===");
            System.out.println("Всего инструментов: " + totalInstruments);
            System.out.println("Обработано: " + processedInstruments);
            System.out.println("Успешно: " + successfulInstruments);
            System.out.println("Без данных: " + noDataCount);
            System.out.println("С ошибками: " + errorCount);
            System.out.println("Процент успеха: " + String.format("%.2f", statistics.get("successRate")) + "%");
            System.out.println("Всего свечей: " + totalCandlesCount);
            System.out.println("Время обработки: " + totalProcessingTime + "мс");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Ошибка при получении минутных свечей акций: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Ошибка при получении данных: " + e.getMessage());
            errorResponse.put("date", date);
            errorResponse.put("candles", new ArrayList<>());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Загрузка дневных свечей акций за дату
     */
    @PostMapping("/candles/shares/daily/{date}")
    public ResponseEntity<CandleLoadResponseDto> loadSharesDailyCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // Создаем запрос для дневных свечей акций
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setDate(date);
        request.setAssetType(List.of("SHARES"));
        
        // Генерируем уникальный ID задачи
        String taskId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        
        // Запускаем загрузку в фоновом режиме
        dailyCandleService.saveDailyCandlesAsync(request, taskId);
        
        // Немедленно возвращаем ответ о запуске
        CandleLoadResponseDto response = new CandleLoadResponseDto(
            true,
            "Загрузка дневных свечей акций запущена для " + date,
            startTime,
            taskId
        );
            
            return ResponseEntity.ok(response);
    }

    /**
     * Получение дневных свечей акций за дату без сохранения
     */
    @GetMapping("/candles/shares/daily/{date}")
    public ResponseEntity<Map<String, Object>> getSharesDailyCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            System.out.println("=== ПОЛУЧЕНИЕ ДНЕВНЫХ СВЕЧЕЙ АКЦИЙ ===");
            System.out.println("Дата: " + date);
            
            // Получаем все акции из БД
            List<ShareEntity> shares = shareRepository.findAll();
            System.out.println("Найдено акций: " + shares.size());
            
            if (shares.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Акции не найдены в базе данных");
                response.put("date", date);
                response.put("candles", new ArrayList<>());
                return ResponseEntity.ok(response);
            }
            
            // Собираем все свечи от API
            List<Map<String, Object>> allCandles = new ArrayList<>();
            List<Map<String, Object>> failedInstruments = new ArrayList<>();
            List<Map<String, Object>> noDataInstruments = new ArrayList<>();
            List<Map<String, Object>> errorInstruments = new ArrayList<>();
            
            int totalInstruments = shares.size();
            int processedInstruments = 0;
            int successfulInstruments = 0;
            int noDataCount = 0;
            int errorCount = 0;
            int totalCandlesCount = 0;
            
            long startProcessingTime = System.currentTimeMillis();
            
            for (ShareEntity share : shares) {
                long instrumentStartTime = System.currentTimeMillis();
                
                try {
                    System.out.println("Обрабатываем акцию: " + share.getFigi() + " - " + share.getName());
                    
                    // Получаем дневные свечи из API
                    List<CandleDto> candles = marketDataService.getCandles(share.getFigi(), date, "CANDLE_INTERVAL_DAY");
                    
                    if (candles != null && !candles.isEmpty()) {
                        System.out.println("Получено " + candles.size() + " свечей для " + share.getFigi());
                        
                        // Преобразуем свечи в формат для ответа
                        for (CandleDto candle : candles) {
                            Map<String, Object> candleData = new HashMap<>();
                            candleData.put("figi", candle.figi());
                            candleData.put("instrumentName", share.getName());
                            candleData.put("volume", candle.volume());
                            candleData.put("high", candle.high());
                            candleData.put("low", candle.low());
                            candleData.put("open", candle.open());
                            candleData.put("close", candle.close());
                            candleData.put("time", candle.time());
                            candleData.put("isComplete", candle.isComplete());
                            
                            allCandles.add(candleData);
                        }
                        
                        totalCandlesCount += candles.size();
                        successfulInstruments++;
                        
                        System.out.println("Успешно обработан " + share.getFigi() + " за " + (System.currentTimeMillis() - instrumentStartTime) + "мс");
                    } else {
                        System.out.println("Нет данных для акции: " + share.getFigi());
                        noDataCount++;
                        
                        // Записываем информацию об инструменте без данных
                        Map<String, Object> noDataInfo = new HashMap<>();
                        noDataInfo.put("figi", share.getFigi());
                        noDataInfo.put("name", share.getName());
                        noDataInfo.put("reason", "Нет торговых данных за указанную дату");
                        noDataInfo.put("processingTimeMs", System.currentTimeMillis() - instrumentStartTime);
                        noDataInstruments.add(noDataInfo);
                    }
                    
                    processedInstruments++;
                    
                    // Небольшая задержка между запросами к API
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    System.err.println("Ошибка при получении данных для " + share.getFigi() + ": " + e.getMessage());
                    errorCount++;
                    processedInstruments++;
                    
                    // Определяем тип ошибки
                    String errorType = "UNKNOWN_ERROR";
                    String errorReason = e.getMessage();
                    
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("DEADLINE_EXCEEDED")) {
                            errorType = "TIMEOUT";
                            errorReason = "Превышено время ожидания ответа от API";
                        } else if (e.getMessage().contains("UNAVAILABLE")) {
                            errorType = "SERVICE_UNAVAILABLE";
                            errorReason = "Сервис API временно недоступен";
                        } else if (e.getMessage().contains("PERMISSION_DENIED")) {
                            errorType = "PERMISSION_DENIED";
                            errorReason = "Нет доступа к инструменту";
                        } else if (e.getMessage().contains("INVALID_ARGUMENT")) {
                            errorType = "INVALID_ARGUMENT";
                            errorReason = "Неверный аргумент запроса";
                        }
                    }
                    
                    // Записываем информацию об ошибке
                    Map<String, Object> errorInfo = new HashMap<>();
                    errorInfo.put("figi", share.getFigi());
                    errorInfo.put("name", share.getName());
                    errorInfo.put("errorType", errorType);
                    errorInfo.put("reason", errorReason);
                    errorInfo.put("processingTimeMs", System.currentTimeMillis() - instrumentStartTime);
                    errorInstruments.add(errorInfo);
                }
            }
            
            long totalProcessingTime = System.currentTimeMillis() - startProcessingTime;
            
            // Формируем детальную статистику
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalInstruments", totalInstruments);
            statistics.put("processedInstruments", processedInstruments);
            statistics.put("successfulInstruments", successfulInstruments);
            statistics.put("noDataInstruments", noDataCount);
            statistics.put("errorInstruments", errorCount);
            statistics.put("successRate", totalInstruments > 0 ? (double) successfulInstruments / totalInstruments * 100 : 0);
            statistics.put("totalProcessingTimeMs", totalProcessingTime);
            statistics.put("averageProcessingTimePerInstrumentMs", processedInstruments > 0 ? totalProcessingTime / processedInstruments : 0);
            
            // Формируем ответ
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Данные получены успешно");
            response.put("date", date);
            response.put("statistics", statistics);
            response.put("totalCandles", totalCandlesCount);
            response.put("candles", allCandles);
            response.put("failedInstruments", failedInstruments);
            response.put("noDataInstruments", noDataInstruments);
            response.put("errorInstruments", errorInstruments);
            
            System.out.println("=== ЗАВЕРШЕНИЕ ПОЛУЧЕНИЯ ДНЕВНЫХ СВЕЧЕЙ АКЦИЙ ===");
            System.out.println("Всего инструментов: " + totalInstruments);
            System.out.println("Обработано: " + processedInstruments);
            System.out.println("Успешно: " + successfulInstruments);
            System.out.println("Без данных: " + noDataCount);
            System.out.println("С ошибками: " + errorCount);
            System.out.println("Процент успеха: " + String.format("%.2f", statistics.get("successRate")) + "%");
            System.out.println("Всего свечей: " + totalCandlesCount);
            System.out.println("Время обработки: " + totalProcessingTime + "мс");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Ошибка при получении дневных свечей акций: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Ошибка при получении данных: " + e.getMessage());
            errorResponse.put("date", date);
            errorResponse.put("candles", new ArrayList<>());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Загрузка минутных свечей фьючерсов за дату
     */
    @PostMapping("/candles/futures/minute/{date}")
    public ResponseEntity<CandleLoadResponseDto> loadFuturesMinuteCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // Создаем запрос для минутных свечей фьючерсов
        MinuteCandleRequestDto request = new MinuteCandleRequestDto();
        request.setDate(date);
        request.setAssetType(List.of("FUTURES"));
        
        // Генерируем уникальный ID задачи
        String taskId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        
        // Запускаем загрузку в фоновом режиме
        minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        
        // Немедленно возвращаем ответ о запуске
        CandleLoadResponseDto response = new CandleLoadResponseDto(
            true,
            "Загрузка минутных свечей фьючерсов запущена для " + date,
            startTime,
            taskId
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Получение минутных свечей фьючерсов за дату без сохранения
     */
    @GetMapping("/candles/futures/minute/{date}")
    public ResponseEntity<Map<String, Object>> getFuturesMinuteCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            System.out.println("=== ПОЛУЧЕНИЕ МИНУТНЫХ СВЕЧЕЙ ФЬЮЧЕРСОВ ===");
            System.out.println("Дата: " + date);
            
            // Получаем все фьючерсы из БД
            List<FutureEntity> futures = futureRepository.findAll();
            System.out.println("Найдено фьючерсов: " + futures.size());
            
            if (futures.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Фьючерсы не найдены в базе данных");
                response.put("date", date);
                response.put("candles", new ArrayList<>());
                return ResponseEntity.ok(response);
            }
            
            // Собираем все свечи от API
            List<Map<String, Object>> allCandles = new ArrayList<>();
            List<Map<String, Object>> failedInstruments = new ArrayList<>();
            List<Map<String, Object>> noDataInstruments = new ArrayList<>();
            List<Map<String, Object>> errorInstruments = new ArrayList<>();
            
            int totalInstruments = futures.size();
            int processedInstruments = 0;
            int successfulInstruments = 0;
            int noDataCount = 0;
            int errorCount = 0;
            int totalCandlesCount = 0;
            
            long startProcessingTime = System.currentTimeMillis();
            
            for (FutureEntity future : futures) {
                long instrumentStartTime = System.currentTimeMillis();
                
                try {
                    System.out.println("Обрабатываем фьючерс: " + future.getFigi() + " - " + future.getTicker());
                    
                    // Получаем минутные свечи из API
                    List<CandleDto> candles = marketDataService.getCandles(future.getFigi(), date, "CANDLE_INTERVAL_1_MIN");
                    
                    if (candles != null && !candles.isEmpty()) {
                        System.out.println("Получено " + candles.size() + " свечей для " + future.getFigi());
                        
                        // Преобразуем свечи в формат для ответа
                        for (CandleDto candle : candles) {
                            Map<String, Object> candleData = new HashMap<>();
                            candleData.put("figi", candle.figi());
                            candleData.put("instrumentName", future.getTicker());
                            candleData.put("volume", candle.volume());
                            candleData.put("high", candle.high());
                            candleData.put("low", candle.low());
                            candleData.put("open", candle.open());
                            candleData.put("close", candle.close());
                            candleData.put("time", candle.time());
                            candleData.put("isComplete", candle.isComplete());
                            
                            allCandles.add(candleData);
                        }
                        
                        totalCandlesCount += candles.size();
                        successfulInstruments++;
                        
                        System.out.println("Успешно обработан " + future.getFigi() + " за " + (System.currentTimeMillis() - instrumentStartTime) + "мс");
                    } else {
                        System.out.println("Нет данных для фьючерса: " + future.getFigi());
                        noDataCount++;
                        
                        // Записываем информацию об инструменте без данных
                        Map<String, Object> noDataInfo = new HashMap<>();
                        noDataInfo.put("figi", future.getFigi());
                        noDataInfo.put("name", future.getTicker());
                        noDataInfo.put("reason", "Нет торговых данных за указанную дату");
                        noDataInfo.put("processingTimeMs", System.currentTimeMillis() - instrumentStartTime);
                        noDataInstruments.add(noDataInfo);
                    }
                    
                    processedInstruments++;
                    
                    // Небольшая задержка между запросами к API
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    System.err.println("Ошибка при получении данных для " + future.getFigi() + ": " + e.getMessage());
                    errorCount++;
                    processedInstruments++;
                    
                    // Определяем тип ошибки
                    String errorType = "UNKNOWN_ERROR";
                    String errorReason = e.getMessage();
                    
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("DEADLINE_EXCEEDED")) {
                            errorType = "TIMEOUT";
                            errorReason = "Превышено время ожидания ответа от API";
                        } else if (e.getMessage().contains("UNAVAILABLE")) {
                            errorType = "SERVICE_UNAVAILABLE";
                            errorReason = "Сервис API временно недоступен";
                        } else if (e.getMessage().contains("PERMISSION_DENIED")) {
                            errorType = "PERMISSION_DENIED";
                            errorReason = "Нет доступа к инструменту";
                        } else if (e.getMessage().contains("INVALID_ARGUMENT")) {
                            errorType = "INVALID_ARGUMENT";
                            errorReason = "Неверный аргумент запроса";
                        }
                    }
                    
                    // Записываем информацию об ошибке
                    Map<String, Object> errorInfo = new HashMap<>();
                    errorInfo.put("figi", future.getFigi());
                    errorInfo.put("name", future.getTicker());
                    errorInfo.put("errorType", errorType);
                    errorInfo.put("reason", errorReason);
                    errorInfo.put("processingTimeMs", System.currentTimeMillis() - instrumentStartTime);
                    errorInstruments.add(errorInfo);
                }
            }
            
            long totalProcessingTime = System.currentTimeMillis() - startProcessingTime;
            
            // Формируем детальную статистику
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalInstruments", totalInstruments);
            statistics.put("processedInstruments", processedInstruments);
            statistics.put("successfulInstruments", successfulInstruments);
            statistics.put("noDataInstruments", noDataCount);
            statistics.put("errorInstruments", errorCount);
            statistics.put("successRate", totalInstruments > 0 ? (double) successfulInstruments / totalInstruments * 100 : 0);
            statistics.put("totalProcessingTimeMs", totalProcessingTime);
            statistics.put("averageProcessingTimePerInstrumentMs", processedInstruments > 0 ? totalProcessingTime / processedInstruments : 0);
            
            // Формируем ответ
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Данные получены успешно");
            response.put("date", date);
            response.put("statistics", statistics);
            response.put("totalCandles", totalCandlesCount);
            response.put("candles", allCandles);
            response.put("failedInstruments", failedInstruments);
            response.put("noDataInstruments", noDataInstruments);
            response.put("errorInstruments", errorInstruments);
            
            System.out.println("=== ЗАВЕРШЕНИЕ ПОЛУЧЕНИЯ МИНУТНЫХ СВЕЧЕЙ ФЬЮЧЕРСОВ ===");
            System.out.println("Всего инструментов: " + totalInstruments);
            System.out.println("Обработано: " + processedInstruments);
            System.out.println("Успешно: " + successfulInstruments);
            System.out.println("Без данных: " + noDataCount);
            System.out.println("С ошибками: " + errorCount);
            System.out.println("Процент успеха: " + String.format("%.2f", statistics.get("successRate")) + "%");
            System.out.println("Всего свечей: " + totalCandlesCount);
            System.out.println("Время обработки: " + totalProcessingTime + "мс");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Ошибка при получении минутных свечей фьючерсов: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Ошибка при получении данных: " + e.getMessage());
            errorResponse.put("date", date);
            errorResponse.put("candles", new ArrayList<>());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Загрузка дневных свечей фьючерсов за дату
     */
    @PostMapping("/candles/futures/daily/{date}")
    public ResponseEntity<CandleLoadResponseDto> loadFuturesDailyCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // Создаем запрос для дневных свечей фьючерсов
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setDate(date);
        request.setAssetType(List.of("FUTURES"));
        
        // Генерируем уникальный ID задачи
        String taskId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        
        // Запускаем загрузку в фоновом режиме
        dailyCandleService.saveDailyCandlesAsync(request, taskId);
        
        // Немедленно возвращаем ответ о запуске
        CandleLoadResponseDto response = new CandleLoadResponseDto(
            true,
            "Загрузка дневных свечей фьючерсов запущена для " + date,
            startTime,
            taskId
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Получение дневных свечей фьючерсов за дату без сохранения
     */
    @GetMapping("/candles/futures/daily/{date}")
    public ResponseEntity<Map<String, Object>> getFuturesDailyCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            System.out.println("=== ПОЛУЧЕНИЕ ДНЕВНЫХ СВЕЧЕЙ ФЬЮЧЕРСОВ ===");
            System.out.println("Дата: " + date);
            
            // Получаем все фьючерсы из БД
            List<FutureEntity> futures = futureRepository.findAll();
            System.out.println("Найдено фьючерсов: " + futures.size());
            
            if (futures.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Фьючерсы не найдены в базе данных");
                response.put("date", date);
                response.put("candles", new ArrayList<>());
                return ResponseEntity.ok(response);
            }
            
            // Собираем все свечи от API
            List<Map<String, Object>> allCandles = new ArrayList<>();
            List<Map<String, Object>> failedInstruments = new ArrayList<>();
            List<Map<String, Object>> noDataInstruments = new ArrayList<>();
            List<Map<String, Object>> errorInstruments = new ArrayList<>();
            
            int totalInstruments = futures.size();
            int processedInstruments = 0;
            int successfulInstruments = 0;
            int noDataCount = 0;
            int errorCount = 0;
            int totalCandlesCount = 0;
            
            long startProcessingTime = System.currentTimeMillis();
            
            for (FutureEntity future : futures) {
                long instrumentStartTime = System.currentTimeMillis();
                
                try {
                    System.out.println("Обрабатываем фьючерс: " + future.getFigi() + " - " + future.getTicker());
                    
                    // Получаем дневные свечи из API
                    List<CandleDto> candles = marketDataService.getCandles(future.getFigi(), date, "CANDLE_INTERVAL_DAY");
                    
                    if (candles != null && !candles.isEmpty()) {
                        System.out.println("Получено " + candles.size() + " свечей для " + future.getFigi());
                        
                        // Преобразуем свечи в формат для ответа
                        for (CandleDto candle : candles) {
                            Map<String, Object> candleData = new HashMap<>();
                            candleData.put("figi", candle.figi());
                            candleData.put("instrumentName", future.getTicker());
                            candleData.put("volume", candle.volume());
                            candleData.put("high", candle.high());
                            candleData.put("low", candle.low());
                            candleData.put("open", candle.open());
                            candleData.put("close", candle.close());
                            candleData.put("time", candle.time());
                            candleData.put("isComplete", candle.isComplete());
                            
                            allCandles.add(candleData);
                        }
                        
                        totalCandlesCount += candles.size();
                        successfulInstruments++;
                        
                        System.out.println("Успешно обработан " + future.getFigi() + " за " + (System.currentTimeMillis() - instrumentStartTime) + "мс");
                    } else {
                        System.out.println("Нет данных для фьючерса: " + future.getFigi());
                        noDataCount++;
                        
                        // Записываем информацию об инструменте без данных
                        Map<String, Object> noDataInfo = new HashMap<>();
                        noDataInfo.put("figi", future.getFigi());
                        noDataInfo.put("name", future.getTicker());
                        noDataInfo.put("reason", "Нет торговых данных за указанную дату");
                        noDataInfo.put("processingTimeMs", System.currentTimeMillis() - instrumentStartTime);
                        noDataInstruments.add(noDataInfo);
                    }
                    
                    processedInstruments++;
                    
                    // Небольшая задержка между запросами к API
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    System.err.println("Ошибка при получении данных для " + future.getFigi() + ": " + e.getMessage());
                    errorCount++;
                    processedInstruments++;
                    
                    // Определяем тип ошибки
                    String errorType = "UNKNOWN_ERROR";
                    String errorReason = e.getMessage();
                    
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("DEADLINE_EXCEEDED")) {
                            errorType = "TIMEOUT";
                            errorReason = "Превышено время ожидания ответа от API";
                        } else if (e.getMessage().contains("UNAVAILABLE")) {
                            errorType = "SERVICE_UNAVAILABLE";
                            errorReason = "Сервис API временно недоступен";
                        } else if (e.getMessage().contains("PERMISSION_DENIED")) {
                            errorType = "PERMISSION_DENIED";
                            errorReason = "Нет доступа к инструменту";
                        } else if (e.getMessage().contains("INVALID_ARGUMENT")) {
                            errorType = "INVALID_ARGUMENT";
                            errorReason = "Неверный аргумент запроса";
                        }
                    }
                    
                    // Записываем информацию об ошибке
                    Map<String, Object> errorInfo = new HashMap<>();
                    errorInfo.put("figi", future.getFigi());
                    errorInfo.put("name", future.getTicker());
                    errorInfo.put("errorType", errorType);
                    errorInfo.put("reason", errorReason);
                    errorInfo.put("processingTimeMs", System.currentTimeMillis() - instrumentStartTime);
                    errorInstruments.add(errorInfo);
                }
            }
            
            long totalProcessingTime = System.currentTimeMillis() - startProcessingTime;
            
            // Формируем детальную статистику
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalInstruments", totalInstruments);
            statistics.put("processedInstruments", processedInstruments);
            statistics.put("successfulInstruments", successfulInstruments);
            statistics.put("noDataInstruments", noDataCount);
            statistics.put("errorInstruments", errorCount);
            statistics.put("successRate", totalInstruments > 0 ? (double) successfulInstruments / totalInstruments * 100 : 0);
            statistics.put("totalProcessingTimeMs", totalProcessingTime);
            statistics.put("averageProcessingTimePerInstrumentMs", processedInstruments > 0 ? totalProcessingTime / processedInstruments : 0);
            
            // Формируем ответ
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Данные получены успешно");
            response.put("date", date);
            response.put("statistics", statistics);
            response.put("totalCandles", totalCandlesCount);
            response.put("candles", allCandles);
            response.put("failedInstruments", failedInstruments);
            response.put("noDataInstruments", noDataInstruments);
            response.put("errorInstruments", errorInstruments);
            
            System.out.println("=== ЗАВЕРШЕНИЕ ПОЛУЧЕНИЯ ДНЕВНЫХ СВЕЧЕЙ ФЬЮЧЕРСОВ ===");
            System.out.println("Всего инструментов: " + totalInstruments);
            System.out.println("Обработано: " + processedInstruments);
            System.out.println("Успешно: " + successfulInstruments);
            System.out.println("Без данных: " + noDataCount);
            System.out.println("С ошибками: " + errorCount);
            System.out.println("Процент успеха: " + String.format("%.2f", statistics.get("successRate")) + "%");
            System.out.println("Всего свечей: " + totalCandlesCount);
            System.out.println("Время обработки: " + totalProcessingTime + "мс");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Ошибка при получении дневных свечей фьючерсов: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Ошибка при получении данных: " + e.getMessage());
            errorResponse.put("date", date);
            errorResponse.put("candles", new ArrayList<>());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Загрузка минутных свечей индикативов за дату
     */
    @PostMapping("/candles/indicatives/minute/{date}")
    public ResponseEntity<CandleLoadResponseDto> loadIndicativesMinuteCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // Создаем запрос для минутных свечей индикативов
        MinuteCandleRequestDto request = new MinuteCandleRequestDto();
        request.setDate(date);
        request.setAssetType(List.of("INDICATIVES"));
        
        // Генерируем уникальный ID задачи
        String taskId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        
        // Запускаем загрузку в фоновом режиме
        minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        
        // Немедленно возвращаем ответ о запуске
        CandleLoadResponseDto response = new CandleLoadResponseDto(
            true,
            "Загрузка минутных свечей индикативов запущена для " + date,
            startTime,
            taskId
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Получение минутных свечей индикативов за дату без сохранения
     */
    @GetMapping("/candles/indicatives/minute/{date}")
    public ResponseEntity<Map<String, Object>> getIndicativesMinuteCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            System.out.println("=== ПОЛУЧЕНИЕ МИНУТНЫХ СВЕЧЕЙ ИНДИКАТИВОВ ===");
            System.out.println("Дата: " + date);
            
            // Получаем все индикативы из БД
            List<IndicativeEntity> indicatives = indicativeRepository.findAll();
            System.out.println("Найдено индикативов: " + indicatives.size());
            
            if (indicatives.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Индикативы не найдены в базе данных");
                response.put("date", date);
                response.put("candles", new ArrayList<>());
                return ResponseEntity.ok(response);
            }
            
            // Собираем все свечи от API
            List<Map<String, Object>> allCandles = new ArrayList<>();
            List<Map<String, Object>> failedInstruments = new ArrayList<>();
            List<Map<String, Object>> noDataInstruments = new ArrayList<>();
            List<Map<String, Object>> errorInstruments = new ArrayList<>();
            
            int totalInstruments = indicatives.size();
            int processedInstruments = 0;
            int successfulInstruments = 0;
            int noDataCount = 0;
            int errorCount = 0;
            int totalCandlesCount = 0;
            
            long startProcessingTime = System.currentTimeMillis();
            
            for (IndicativeEntity indicative : indicatives) {
                long instrumentStartTime = System.currentTimeMillis();
                
                try {
                    System.out.println("Обрабатываем индикатив: " + indicative.getFigi() + " - " + indicative.getName());
                    
                    // Получаем минутные свечи из API
                    List<CandleDto> candles = marketDataService.getCandles(indicative.getFigi(), date, "CANDLE_INTERVAL_1_MIN");
                    
                    if (candles != null && !candles.isEmpty()) {
                        System.out.println("Получено " + candles.size() + " свечей для " + indicative.getFigi());
                        
                        // Преобразуем свечи в формат для ответа
                        for (CandleDto candle : candles) {
                            Map<String, Object> candleData = new HashMap<>();
                            candleData.put("figi", candle.figi());
                            candleData.put("instrumentName", indicative.getName());
                            candleData.put("volume", candle.volume());
                            candleData.put("high", candle.high());
                            candleData.put("low", candle.low());
                            candleData.put("open", candle.open());
                            candleData.put("close", candle.close());
                            candleData.put("time", candle.time());
                            candleData.put("isComplete", candle.isComplete());
                            
                            allCandles.add(candleData);
                        }
                        
                        totalCandlesCount += candles.size();
                        successfulInstruments++;
                        
                        System.out.println("Успешно обработан " + indicative.getFigi() + " за " + (System.currentTimeMillis() - instrumentStartTime) + "мс");
                    } else {
                        System.out.println("Нет данных для индикатива: " + indicative.getFigi());
                        noDataCount++;
                        
                        // Записываем информацию об инструменте без данных
                        Map<String, Object> noDataInfo = new HashMap<>();
                        noDataInfo.put("figi", indicative.getFigi());
                        noDataInfo.put("name", indicative.getName());
                        noDataInfo.put("reason", "Нет торговых данных за указанную дату");
                        noDataInfo.put("processingTimeMs", System.currentTimeMillis() - instrumentStartTime);
                        noDataInstruments.add(noDataInfo);
                    }
                    
                    processedInstruments++;
                    
                    // Небольшая задержка между запросами к API
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    System.err.println("Ошибка при получении данных для " + indicative.getFigi() + ": " + e.getMessage());
                    errorCount++;
                    processedInstruments++;
                    
                    // Определяем тип ошибки
                    String errorType = "UNKNOWN_ERROR";
                    String errorReason = e.getMessage();
                    
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("DEADLINE_EXCEEDED")) {
                            errorType = "TIMEOUT";
                            errorReason = "Превышено время ожидания ответа от API";
                        } else if (e.getMessage().contains("UNAVAILABLE")) {
                            errorType = "SERVICE_UNAVAILABLE";
                            errorReason = "Сервис API временно недоступен";
                        } else if (e.getMessage().contains("PERMISSION_DENIED")) {
                            errorType = "PERMISSION_DENIED";
                            errorReason = "Нет доступа к инструменту";
                        } else if (e.getMessage().contains("INVALID_ARGUMENT")) {
                            errorType = "INVALID_ARGUMENT";
                            errorReason = "Неверный аргумент запроса";
                        }
                    }
                    
                    // Записываем информацию об ошибке
                    Map<String, Object> errorInfo = new HashMap<>();
                    errorInfo.put("figi", indicative.getFigi());
                    errorInfo.put("name", indicative.getName());
                    errorInfo.put("errorType", errorType);
                    errorInfo.put("reason", errorReason);
                    errorInfo.put("processingTimeMs", System.currentTimeMillis() - instrumentStartTime);
                    errorInstruments.add(errorInfo);
                }
            }
            
            long totalProcessingTime = System.currentTimeMillis() - startProcessingTime;
            
            // Формируем детальную статистику
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalInstruments", totalInstruments);
            statistics.put("processedInstruments", processedInstruments);
            statistics.put("successfulInstruments", successfulInstruments);
            statistics.put("noDataInstruments", noDataCount);
            statistics.put("errorInstruments", errorCount);
            statistics.put("successRate", totalInstruments > 0 ? (double) successfulInstruments / totalInstruments * 100 : 0);
            statistics.put("totalProcessingTimeMs", totalProcessingTime);
            statistics.put("averageProcessingTimePerInstrumentMs", processedInstruments > 0 ? totalProcessingTime / processedInstruments : 0);
            
            // Формируем ответ
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Данные получены успешно");
            response.put("date", date);
            response.put("statistics", statistics);
            response.put("totalCandles", totalCandlesCount);
            response.put("candles", allCandles);
            response.put("failedInstruments", failedInstruments);
            response.put("noDataInstruments", noDataInstruments);
            response.put("errorInstruments", errorInstruments);
            
            System.out.println("=== ЗАВЕРШЕНИЕ ПОЛУЧЕНИЯ МИНУТНЫХ СВЕЧЕЙ ИНДИКАТИВОВ ===");
            System.out.println("Всего инструментов: " + totalInstruments);
            System.out.println("Обработано: " + processedInstruments);
            System.out.println("Успешно: " + successfulInstruments);
            System.out.println("Без данных: " + noDataCount);
            System.out.println("С ошибками: " + errorCount);
            System.out.println("Процент успеха: " + String.format("%.2f", statistics.get("successRate")) + "%");
            System.out.println("Всего свечей: " + totalCandlesCount);
            System.out.println("Время обработки: " + totalProcessingTime + "мс");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Ошибка при получении минутных свечей индикативов: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Ошибка при получении данных: " + e.getMessage());
            errorResponse.put("date", date);
            errorResponse.put("candles", new ArrayList<>());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Загрузка дневных свечей индикативов за дату
     */
    @PostMapping("/candles/indicatives/daily/{date}")
    public ResponseEntity<CandleLoadResponseDto> loadIndicativesDailyCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // Создаем запрос для дневных свечей индикативов
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setDate(date);
        request.setAssetType(List.of("INDICATIVES"));
        
        // Генерируем уникальный ID задачи
        String taskId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        
        // Запускаем загрузку в фоновом режиме
        dailyCandleService.saveDailyCandlesAsync(request, taskId);
        
        // Немедленно возвращаем ответ о запуске
        CandleLoadResponseDto response = new CandleLoadResponseDto(
            true,
            "Загрузка дневных свечей индикативов запущена для " + date,
            startTime,
            taskId
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Получение дневных свечей индикативов за дату без сохранения
     */
    @GetMapping("/candles/indicatives/daily/{date}")
    public ResponseEntity<Map<String, Object>> getIndicativesDailyCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            System.out.println("=== ПОЛУЧЕНИЕ ДНЕВНЫХ СВЕЧЕЙ ИНДИКАТИВОВ ===");
            System.out.println("Дата: " + date);
            
            // Получаем все индикативы из БД
            List<IndicativeEntity> indicatives = indicativeRepository.findAll();
            System.out.println("Найдено индикативов: " + indicatives.size());
            
            if (indicatives.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Индикативы не найдены в базе данных");
                response.put("date", date);
                response.put("candles", new ArrayList<>());
                return ResponseEntity.ok(response);
            }
            
            // Собираем все свечи от API
            List<Map<String, Object>> allCandles = new ArrayList<>();
            List<Map<String, Object>> failedInstruments = new ArrayList<>();
            List<Map<String, Object>> noDataInstruments = new ArrayList<>();
            List<Map<String, Object>> errorInstruments = new ArrayList<>();
            
            int totalInstruments = indicatives.size();
            int processedInstruments = 0;
            int successfulInstruments = 0;
            int noDataCount = 0;
            int errorCount = 0;
            int totalCandlesCount = 0;
            
            long startProcessingTime = System.currentTimeMillis();
            
            for (IndicativeEntity indicative : indicatives) {
                long instrumentStartTime = System.currentTimeMillis();
                
                try {
                    System.out.println("Обрабатываем индикатив: " + indicative.getFigi() + " - " + indicative.getName());
                    
                    // Получаем дневные свечи из API
                    List<CandleDto> candles = marketDataService.getCandles(indicative.getFigi(), date, "CANDLE_INTERVAL_DAY");
                    
                    if (candles != null && !candles.isEmpty()) {
                        System.out.println("Получено " + candles.size() + " свечей для " + indicative.getFigi());
                        
                        // Преобразуем свечи в формат для ответа
                        for (CandleDto candle : candles) {
                            Map<String, Object> candleData = new HashMap<>();
                            candleData.put("figi", candle.figi());
                            candleData.put("instrumentName", indicative.getName());
                            candleData.put("volume", candle.volume());
                            candleData.put("high", candle.high());
                            candleData.put("low", candle.low());
                            candleData.put("open", candle.open());
                            candleData.put("close", candle.close());
                            candleData.put("time", candle.time());
                            candleData.put("isComplete", candle.isComplete());
                            
                            allCandles.add(candleData);
                        }
                        
                        totalCandlesCount += candles.size();
                        successfulInstruments++;
                        
                        System.out.println("Успешно обработан " + indicative.getFigi() + " за " + (System.currentTimeMillis() - instrumentStartTime) + "мс");
                    } else {
                        System.out.println("Нет данных для индикатива: " + indicative.getFigi());
                        noDataCount++;
                        
                        // Записываем информацию об инструменте без данных
                        Map<String, Object> noDataInfo = new HashMap<>();
                        noDataInfo.put("figi", indicative.getFigi());
                        noDataInfo.put("name", indicative.getName());
                        noDataInfo.put("reason", "Нет торговых данных за указанную дату");
                        noDataInfo.put("processingTimeMs", System.currentTimeMillis() - instrumentStartTime);
                        noDataInstruments.add(noDataInfo);
                    }
                    
                    processedInstruments++;
                    
                    // Небольшая задержка между запросами к API
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    System.err.println("Ошибка при получении данных для " + indicative.getFigi() + ": " + e.getMessage());
                    errorCount++;
                    processedInstruments++;
                    
                    // Определяем тип ошибки
                    String errorType = "UNKNOWN_ERROR";
                    String errorReason = e.getMessage();
                    
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("DEADLINE_EXCEEDED")) {
                            errorType = "TIMEOUT";
                            errorReason = "Превышено время ожидания ответа от API";
                        } else if (e.getMessage().contains("UNAVAILABLE")) {
                            errorType = "SERVICE_UNAVAILABLE";
                            errorReason = "Сервис API временно недоступен";
                        } else if (e.getMessage().contains("PERMISSION_DENIED")) {
                            errorType = "PERMISSION_DENIED";
                            errorReason = "Нет доступа к инструменту";
                        } else if (e.getMessage().contains("INVALID_ARGUMENT")) {
                            errorType = "INVALID_ARGUMENT";
                            errorReason = "Неверный аргумент запроса";
                        }
                    }
                    
                    // Записываем информацию об ошибке
                    Map<String, Object> errorInfo = new HashMap<>();
                    errorInfo.put("figi", indicative.getFigi());
                    errorInfo.put("name", indicative.getName());
                    errorInfo.put("errorType", errorType);
                    errorInfo.put("reason", errorReason);
                    errorInfo.put("processingTimeMs", System.currentTimeMillis() - instrumentStartTime);
                    errorInstruments.add(errorInfo);
                }
            }
            
            long totalProcessingTime = System.currentTimeMillis() - startProcessingTime;
            
            // Формируем детальную статистику
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalInstruments", totalInstruments);
            statistics.put("processedInstruments", processedInstruments);
            statistics.put("successfulInstruments", successfulInstruments);
            statistics.put("noDataInstruments", noDataCount);
            statistics.put("errorInstruments", errorCount);
            statistics.put("successRate", totalInstruments > 0 ? (double) successfulInstruments / totalInstruments * 100 : 0);
            statistics.put("totalProcessingTimeMs", totalProcessingTime);
            statistics.put("averageProcessingTimePerInstrumentMs", processedInstruments > 0 ? totalProcessingTime / processedInstruments : 0);
            
            // Формируем ответ
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Данные получены успешно");
            response.put("date", date);
            response.put("statistics", statistics);
            response.put("totalCandles", totalCandlesCount);
            response.put("candles", allCandles);
            response.put("failedInstruments", failedInstruments);
            response.put("noDataInstruments", noDataInstruments);
            response.put("errorInstruments", errorInstruments);
            
            System.out.println("=== ЗАВЕРШЕНИЕ ПОЛУЧЕНИЯ ДНЕВНЫХ СВЕЧЕЙ ИНДИКАТИВОВ ===");
            System.out.println("Всего инструментов: " + totalInstruments);
            System.out.println("Обработано: " + processedInstruments);
            System.out.println("Успешно: " + successfulInstruments);
            System.out.println("Без данных: " + noDataCount);
            System.out.println("С ошибками: " + errorCount);
            System.out.println("Процент успеха: " + String.format("%.2f", statistics.get("successRate")) + "%");
            System.out.println("Всего свечей: " + totalCandlesCount);
            System.out.println("Время обработки: " + totalProcessingTime + "мс");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Ошибка при получении дневных свечей индикативов: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Ошибка при получении данных: " + e.getMessage());
            errorResponse.put("date", date);
            errorResponse.put("candles", new ArrayList<>());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== ЦЕНЫ ЗАКРЫТИЯ ====================

    /**
     * Синхронная загрузка цен закрытия за сегодня по всем инструментам
     */
    @PostMapping("/close-prices")
    public ResponseEntity<Map<String, Object>> loadClosePricesToday() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            ClosePriceRequestDto request = new ClosePriceRequestDto();
            SaveResponseDto saveResponse = service.saveClosePrices(request);
            
            response.put("success", saveResponse.isSuccess());
            response.put("message", saveResponse.getMessage());
            response.put("totalRequested", saveResponse.getTotalRequested());
            response.put("newItemsSaved", saveResponse.getNewItemsSaved());
            response.put("existingItemsSkipped", saveResponse.getExistingItemsSkipped());
            response.put("invalidItemsFiltered", saveResponse.getInvalidItemsFiltered());
            response.put("missingFromApi", saveResponse.getMissingFromApi());
            response.put("savedItems", saveResponse.getSavedItems());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Исключение будет обработано GlobalExceptionHandler
            throw new DataLoadException("Ошибка загрузки цен закрытия за сегодня: " + e.getMessage(), e);
        }
    }

    /**
     * Синхронная точечная загрузка цен закрытия по указанным инструментам
     */
    @PostMapping("/close-prices/save")
    public ResponseEntity<SaveResponseDto> saveClosePrices(@RequestBody(required = false) ClosePriceRequestDto request) {
        try {
            // Если request null, создаем пустой объект
            if (request == null) {
                request = new ClosePriceRequestDto();
            }
            SaveResponseDto response = service.saveClosePrices(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Исключение будет обработано GlobalExceptionHandler
            throw new DataLoadException("Ошибка сохранения цен закрытия: " + e.getMessage(), e);
        }
    }

    /**
     * Получение цен закрытия для всех акций из T-INVEST API
     */
    @GetMapping("/close-prices/shares")
    public ResponseEntity<Map<String, Object>> getClosePricesForShares() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ClosePriceDto> allClosePrices = service.getClosePricesForAllShares();
            
            // Фильтруем неверные цены (с датой 1970-01-01)
            List<ClosePriceDto> validClosePrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("message", "Цены закрытия для акций получены успешно");
            response.put("data", validClosePrices);
            response.put("count", validClosePrices.size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Исключение будет обработано GlobalExceptionHandler
            throw new DataLoadException("Ошибка получения цен закрытия для акций: " + e.getMessage(), e);
        }
    }

    /**
     * Получение цен закрытия для всех фьючерсов из T-INVEST API
     */
    @GetMapping("/close-prices/futures")
    public ResponseEntity<Map<String, Object>> getClosePricesForFutures() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ClosePriceDto> allClosePrices = service.getClosePricesForAllFutures();
            
            // Фильтруем неверные цены (с датой 1970-01-01)
            List<ClosePriceDto> validClosePrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("message", "Цены закрытия для фьючерсов получены успешно");
            response.put("data", validClosePrices);
            response.put("count", validClosePrices.size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Исключение будет обработано GlobalExceptionHandler
            throw new DataLoadException("Ошибка получения цен закрытия для фьючерсов: " + e.getMessage(), e);
        }
    }

    /**
     * Получение цены закрытия по инструменту из T-INVEST API
     */
    @GetMapping("/close-prices/{figi}")
    public ResponseEntity<Map<String, Object>> getClosePriceByFigi(@PathVariable String figi) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ClosePriceDto> allClosePrices = service.getClosePrices(List.of(figi), null);
            
            // Фильтруем неверные цены (с датой 1970-01-01)
            List<ClosePriceDto> validClosePrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .collect(Collectors.toList());
            
            if (validClosePrices.isEmpty()) {
                response.put("success", false);
                response.put("message", "Цена закрытия не найдена для инструмента: " + figi);
                response.put("figi", figi);
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.ok(response);
            }
            
            ClosePriceDto closePrice = validClosePrices.get(0);
            response.put("success", true);
            response.put("message", "Цена закрытия получена успешно");
            response.put("data", closePrice);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения цены закрытия: " + e.getMessage());
            response.put("figi", figi);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== ЦЕНЫ ВЕЧЕРНЕЙ СЕССИИ ====================

    /**
     * Получение цен вечерней сессии для всех акций из T-INVEST API
     */
    @GetMapping("/evening-session-prices/shares")
    public ResponseEntity<Map<String, Object>> getEveningSessionPricesForShares() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ClosePriceDto> allClosePrices = service.getClosePricesForAllShares();
            
            // Фильтруем неверные цены (с датой 1970-01-01) и оставляем только с eveningSessionPrice
            List<ClosePriceDto> validEveningPrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .filter(price -> price.eveningSessionPrice() != null)
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("message", "Цены вечерней сессии для акций получены успешно");
            response.put("data", validEveningPrices);
            response.put("count", validEveningPrices.size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Исключение будет обработано GlobalExceptionHandler
            throw new DataLoadException("Ошибка получения цен вечерней сессии для акций: " + e.getMessage(), e);
        }
    }

    /**
     * Получение цен вечерней сессии для всех фьючерсов из T-INVEST API
     */
    @GetMapping("/evening-session-prices/futures")
    public ResponseEntity<Map<String, Object>> getEveningSessionPricesForFutures() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ClosePriceDto> allClosePrices = service.getClosePricesForAllFutures();
            
            // Фильтруем неверные цены (с датой 1970-01-01) и оставляем только с eveningSessionPrice
            List<ClosePriceDto> validEveningPrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .filter(price -> price.eveningSessionPrice() != null)
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("message", "Цены вечерней сессии для фьючерсов получены успешно");
            response.put("data", validEveningPrices);
            response.put("count", validEveningPrices.size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Исключение будет обработано GlobalExceptionHandler
            throw new DataLoadException("Ошибка получения цен вечерней сессии для фьючерсов: " + e.getMessage(), e);
        }
    }

    /**
     * Получение цены вечерней сессии по инструменту из T-INVEST API
     */
    @GetMapping("/evening-session-prices/{figi}")
    public ResponseEntity<Map<String, Object>> getEveningSessionPriceByFigi(@PathVariable String figi) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ClosePriceDto> allClosePrices = service.getClosePrices(List.of(figi), null);
            
            // Фильтруем неверные цены (с датой 1970-01-01) и оставляем только с eveningSessionPrice
            List<ClosePriceDto> validEveningPrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .filter(price -> price.eveningSessionPrice() != null)
                    .collect(Collectors.toList());
            
            if (validEveningPrices.isEmpty()) {
                response.put("success", false);
                response.put("message", "Цена вечерней сессии не найдена для инструмента: " + figi);
                response.put("figi", figi);
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.ok(response);
            }
            
            ClosePriceDto eveningPrice = validEveningPrices.get(0);
            response.put("success", true);
            response.put("message", "Цена вечерней сессии получена успешно");
            response.put("data", eveningPrice);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения цены вечерней сессии: " + e.getMessage());
            response.put("figi", figi);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }


    // ==================== СЕССИОННЫЕ ЦЕНЫ ====================

    /**
     * Синхронная загрузка цен вечерней сессии за сегодня по всем инструментам
     */
    @PostMapping("/evening-session-prices")
    public ResponseEntity<Map<String, Object>> loadEveningSessionPricesToday() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
            SaveResponseDto saveResponse = service.saveClosePricesEveningSession(request);
            
            response.put("success", saveResponse.isSuccess());
            response.put("message", saveResponse.getMessage());
            response.put("totalRequested", saveResponse.getTotalRequested());
            response.put("newItemsSaved", saveResponse.getNewItemsSaved());
            response.put("existingItemsSkipped", saveResponse.getExistingItemsSkipped());
            response.put("invalidItemsFiltered", saveResponse.getInvalidItemsFiltered());
            response.put("missingFromApi", saveResponse.getMissingFromApi());
            response.put("savedItems", saveResponse.getSavedItems());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Исключение будет обработано GlobalExceptionHandler
            throw new DataLoadException("Ошибка загрузки цен вечерней сессии за сегодня: " + e.getMessage(), e);
        }
    }

    /**
     * Синхронная точечная загрузка цен вечерней сессии по указанным инструментам
     */
    @PostMapping("/evening-session-prices/save")
    public ResponseEntity<SaveResponseDto> saveEveningSessionPrices(@RequestBody(required = false) ClosePriceEveningSessionRequestDto request) {
        try {
            // Если request null, создаем пустой объект
            if (request == null) {
                request = new ClosePriceEveningSessionRequestDto();
            }
            SaveResponseDto response = service.saveClosePricesEveningSession(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Исключение будет обработано GlobalExceptionHandler
            throw new DataLoadException("Ошибка сохранения цен вечерней сессии: " + e.getMessage(), e);
        }
    }

    /**
     * Загрузка цен вечерней сессии за дату
     */
    @PostMapping("/evening-session-prices/{date}")
    public ResponseEntity<SaveResponseDto> loadEveningSessionPricesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        SaveResponseDto response = eveningSessionService.fetchAndStoreEveningSessionPricesForDate(date);
        return ResponseEntity.ok(response);
    }

    /**
     * Загрузка цен утренней сессии за сегодня
     */
    @PostMapping("/morning-session-prices")
    public ResponseEntity<Map<String, Object>> loadMorningSessionPricesToday() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            morningSessionService.fetchAndStoreMorningSessionPrices();
            
            response.put("success", true);
            response.put("message", "Загрузка цен утренней сессии запущена для сегодня");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка загрузки цен утренней сессии: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Загрузка цен утренней сессии за дату
     */
    @PostMapping("/morning-session-prices/{date}")
    public ResponseEntity<SaveResponseDto> loadMorningSessionPricesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        SaveResponseDto response = morningSessionService.fetchAndStoreMorningSessionPricesForDate(date);
        return ResponseEntity.ok(response);
    }

    // ==================== СДЕЛКИ ====================

    /**
     * Загрузка последних сделок
     */
    @PostMapping("/last-trades")
    public ResponseEntity<Map<String, Object>> loadLastTrades(@RequestBody LastTradesRequestDto request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            lastTradesService.fetchAndStoreLastTradesByRequestAsync(request);
            
            response.put("success", true);
            response.put("message", "Загрузка обезличенных сделок запущена в фоновом режиме");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка загрузки сделок: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== СТАТУС ЗАГРУЗКИ ====================

    /**
     * Получение статуса загрузки по ID задачи
     */
    @GetMapping("/status/{taskId}")
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
