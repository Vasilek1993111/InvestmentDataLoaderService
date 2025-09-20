package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.exception.DataLoadException;
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
import java.util.stream.Collectors;

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
