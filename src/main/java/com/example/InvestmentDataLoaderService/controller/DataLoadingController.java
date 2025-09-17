package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.service.TInvestService;
import com.example.InvestmentDataLoaderService.scheduler.CandleSchedulerService;
import com.example.InvestmentDataLoaderService.scheduler.EveningSessionService;
import com.example.InvestmentDataLoaderService.scheduler.MorningSessionService;
import com.example.InvestmentDataLoaderService.scheduler.LastTradesService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * Контроллер для загрузки и управления данными
 * Объединяет функциональность загрузки свечей, цен, сделок и других торговых данных
 */
@RestController
@RequestMapping("/api/data-loading")
public class DataLoadingController {

    private final TInvestService service;
    private final CandleSchedulerService candleScheduler;
    private final EveningSessionService eveningSessionService;
    private final MorningSessionService morningSessionService;
    private final LastTradesService lastTradesService;

    public DataLoadingController(TInvestService service,
                               CandleSchedulerService candleScheduler,
                               EveningSessionService eveningSessionService,
                               MorningSessionService morningSessionService,
                               LastTradesService lastTradesService) {
        this.service = service;
        this.candleScheduler = candleScheduler;
        this.eveningSessionService = eveningSessionService;
        this.morningSessionService = morningSessionService;
        this.lastTradesService = lastTradesService;
    }

    // ==================== СВЕЧИ ====================

    /**
     * Загрузка свечей за сегодня
     */
    @PostMapping("/candles")
    public ResponseEntity<CandleLoadResponseDto> loadCandlesToday(@RequestBody(required = false) CandleRequestDto request) {
        // Если request null, создаем пустой объект
        if (request == null) {
            request = new CandleRequestDto();
        }
        
        // Генерируем уникальный ID задачи
        String taskId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        
        // Запускаем загрузку в фоновом режиме
        service.saveCandlesAsync(request, taskId);
        
        // Немедленно возвращаем ответ о запуске
        CandleLoadResponseDto response = new CandleLoadResponseDto(
            true,
            "Загрузка свечей запущена в фоновом режиме",
            startTime,
            taskId
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Загрузка свечей за конкретную дату
     */
    @PostMapping("/candles/{date}")
    public ResponseEntity<Map<String, Object>> loadCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            candleScheduler.fetchAndStoreCandlesForDate(date);
            
            response.put("success", true);
            response.put("message", "Загрузка свечей запущена для " + date);
            response.put("date", date);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка загрузки свечей для " + date + ": " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Загрузка свечей акций за дату
     */
    @PostMapping("/candles/shares/{date}")
    public ResponseEntity<Map<String, Object>> loadSharesCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            candleScheduler.fetchAndStoreSharesCandlesForDate(date);
            
            response.put("success", true);
            response.put("message", "Загрузка свечей акций запущена для " + date);
            response.put("date", date);
            response.put("type", "shares");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка загрузки свечей акций для " + date + ": " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Загрузка свечей фьючерсов за дату
     */
    @PostMapping("/candles/futures/{date}")
    public ResponseEntity<Map<String, Object>> loadFuturesCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            candleScheduler.fetchAndStoreFuturesCandlesForDate(date);
            
            response.put("success", true);
            response.put("message", "Загрузка свечей фьючерсов запущена для " + date);
            response.put("date", date);
            response.put("type", "futures");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка загрузки свечей фьючерсов для " + date + ": " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Загрузка свечей индикативов за дату
     */
    @PostMapping("/candles/indicatives/{date}")
    public ResponseEntity<Map<String, Object>> loadIndicativesCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            candleScheduler.fetchAndStoreIndicativesCandlesForDate(date);
            
            response.put("success", true);
            response.put("message", "Загрузка свечей индикативов запущена для " + date);
            response.put("date", date);
            response.put("type", "indicatives");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка загрузки свечей индикативов для " + date + ": " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== ЦЕНЫ ЗАКРЫТИЯ ====================

    /**
     * Синхронная загрузка цен закрытия за сегодня по всем инструментам
     */
    @PostMapping("/close-prices")
    public ResponseEntity<SaveResponseDto> loadClosePricesToday() {
        ClosePriceRequestDto request = new ClosePriceRequestDto();
        SaveResponseDto response = service.saveClosePrices(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Синхронная точечная загрузка цен закрытия по указанным инструментам
     */
    @PostMapping("/close-prices/save")
    public ResponseEntity<SaveResponseDto> saveClosePrices(@RequestBody(required = false) ClosePriceRequestDto request) {
        // Если request null, создаем пустой объект
        if (request == null) {
            request = new ClosePriceRequestDto();
        }
        SaveResponseDto response = service.saveClosePrices(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Получение цены закрытия по инструменту из T-INVEST API
     */
    @GetMapping("/close-prices/{figi}")
    public ResponseEntity<Map<String, Object>> getClosePriceByFigi(@PathVariable String figi) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ClosePriceDto> closePrices = service.getClosePrices(List.of(figi), null);
            
            if (closePrices.isEmpty()) {
                response.put("success", false);
                response.put("message", "Цена закрытия не найдена для инструмента: " + figi);
                response.put("figi", figi);
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.ok(response);
            }
            
            ClosePriceDto closePrice = closePrices.get(0);
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

    /**
     * Получение цен закрытия по нескольким инструментам из T-INVEST API
     */
    @PostMapping("/close-prices/get")
    public ResponseEntity<Map<String, Object>> getClosePricesByFigis(@RequestBody ClosePriceRequestDto request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (request.getInstruments() == null || request.getInstruments().isEmpty()) {
                response.put("success", false);
                response.put("message", "Список инструментов не может быть пустым");
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.badRequest().body(response);
            }
            
            List<ClosePriceDto> closePrices = service.getClosePrices(request.getInstruments(), null);
            
            response.put("success", true);
            response.put("message", "Получено цен закрытия: " + closePrices.size() + " из " + request.getInstruments().size() + " запрошенных");
            response.put("data", closePrices);
            response.put("requestedCount", request.getInstruments().size());
            response.put("receivedCount", closePrices.size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения цен закрытия: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== СЕССИОННЫЕ ЦЕНЫ ====================

    /**
     * Загрузка цен вечерней сессии за сегодня
     */
    @PostMapping("/evening-session-prices")
    public ResponseEntity<Map<String, Object>> loadEveningSessionPricesToday() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            eveningSessionService.fetchAndStoreEveningSessionPrices();
            
            response.put("success", true);
            response.put("message", "Загрузка цен вечерней сессии запущена для сегодня");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка загрузки цен вечерней сессии: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
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
