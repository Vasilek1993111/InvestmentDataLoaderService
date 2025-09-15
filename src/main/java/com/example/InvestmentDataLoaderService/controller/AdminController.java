package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.scheduler.ClosePriceSchedulerService;
import com.example.InvestmentDataLoaderService.scheduler.CandleSchedulerService;
import com.example.InvestmentDataLoaderService.scheduler.EveningSessionService;
import com.example.InvestmentDataLoaderService.scheduler.MorningSessionService;
import com.example.InvestmentDataLoaderService.scheduler.LastTradesService;
import com.example.InvestmentDataLoaderService.service.AggregationService;
import com.example.InvestmentDataLoaderService.service.OptimizedAggregationService;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.SharesAggregatedDataRepository;
import com.example.InvestmentDataLoaderService.dto.LastTradesRequestDto;
import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.dto.AggregationResult;
import com.example.InvestmentDataLoaderService.entity.SharesAggregatedDataEntity;
import com.example.InvestmentDataLoaderService.entity.FuturesAggregatedDataEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ClosePriceSchedulerService closePriceScheduler;
    private final CandleSchedulerService candleScheduler;
    private final EveningSessionService eveningSessionService;
    private final MorningSessionService morningSessionService;
    private final LastTradesService lastTradesService;
    private final AggregationService aggregationService;
    private final OptimizedAggregationService optimizedAggregationService;
    private final ShareRepository shareRepository;
    private final SharesAggregatedDataRepository sharesAggregatedDataRepository;

    public AdminController(ClosePriceSchedulerService closePriceScheduler, 
                          CandleSchedulerService candleScheduler,
                          EveningSessionService eveningSessionService,
                          MorningSessionService morningSessionService,
                          LastTradesService lastTradesService,
                          AggregationService aggregationService,
                          OptimizedAggregationService optimizedAggregationService,
                          ShareRepository shareRepository,
                          SharesAggregatedDataRepository sharesAggregatedDataRepository) {
        this.closePriceScheduler = closePriceScheduler;
        this.candleScheduler = candleScheduler;
        this.eveningSessionService = eveningSessionService;
        this.morningSessionService = morningSessionService;
        this.lastTradesService = lastTradesService;
        this.aggregationService = aggregationService;
        this.optimizedAggregationService = optimizedAggregationService;
        this.shareRepository = shareRepository;
        this.sharesAggregatedDataRepository = sharesAggregatedDataRepository;
    }

    @PostMapping("/load-close-prices")
    public ResponseEntity<String> loadToday() {
        closePriceScheduler.fetchAndStoreClosePrices();
        return ResponseEntity.ok("Close prices loaded for today");
    }

    @PostMapping("/load-candles")
    public ResponseEntity<String> loadCandlesToday() {
        candleScheduler.fetchAndStoreCandles();
        return ResponseEntity.ok("Candles loading started for today");
    }

    @PostMapping("/load-candles/{date}")
    public ResponseEntity<String> loadCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        candleScheduler.fetchAndStoreCandlesForDate(date);
        return ResponseEntity.ok("Candles loading started for " + date);
    }

    @PostMapping("/load-candles/shares/{date}")
    public ResponseEntity<String> loadSharesCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        candleScheduler.fetchAndStoreSharesCandlesForDate(date);
        return ResponseEntity.ok("Shares candles loading started for " + date);
    }

    @PostMapping("/load-candles/futures/{date}")
    public ResponseEntity<String> loadFuturesCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        candleScheduler.fetchAndStoreFuturesCandlesForDate(date);
        return ResponseEntity.ok("Futures candles loading started for " + date);
    }

    @PostMapping("/load-evening-session-prices")
    public ResponseEntity<String> loadEveningSessionPricesToday() {
        eveningSessionService.fetchAndStoreEveningSessionPrices();
        return ResponseEntity.ok("Evening session prices loading started for today");
    }

    @PostMapping("/load-evening-session-prices/{date}")
    public ResponseEntity<SaveResponseDto> loadEveningSessionPricesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        SaveResponseDto response = eveningSessionService.fetchAndStoreEveningSessionPricesForDate(date);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/load-morning-session-prices")
    public ResponseEntity<String> loadMorningSessionPricesToday() {
        morningSessionService.fetchAndStoreMorningSessionPrices();
        return ResponseEntity.ok("Morning session prices loading started for today");
    }

    @PostMapping("/load-morning-session-prices/{date}")
    public ResponseEntity<SaveResponseDto> loadMorningSessionPricesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        SaveResponseDto response = morningSessionService.fetchAndStoreMorningSessionPricesForDate(date);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/load-last-trades")
    public ResponseEntity<String> loadLastTrades(@RequestBody LastTradesRequestDto request) {
        lastTradesService.fetchAndStoreLastTradesByRequestAsync(request);
        return ResponseEntity.ok("Загрузка обезличенных сделок запущена в фоновом режиме");
    }

    // === ЭНДПОИНТЫ ДЛЯ АГРЕГИРОВАННЫХ ДАННЫХ ===


    // Пересчет агрегированных данных для всех фьючерсов
    @PostMapping("/recalculate-futures-aggregation")
    public ResponseEntity<AggregationResult> recalculateFuturesAggregation() {
        try {
            AggregationResult result = aggregationService.recalculateAllFuturesData();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new AggregationResult());
        }
    }

    // Получение агрегированных данных для акций
    @GetMapping("/shares-aggregation")
    public ResponseEntity<List<SharesAggregatedDataEntity>> getSharesAggregation() {
        try {
            List<SharesAggregatedDataEntity> data = aggregationService.getAllSharesAggregatedData();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // Получение агрегированных данных для фьючерсов
    @GetMapping("/futures-aggregation")
    public ResponseEntity<List<FuturesAggregatedDataEntity>> getFuturesAggregation() {
        try {
            List<FuturesAggregatedDataEntity> data = aggregationService.getAllFuturesAggregatedData();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // === ОПТИМИЗИРОВАННЫЕ ЭНДПОИНТЫ ДЛЯ АГРЕГАЦИИ ===

    /**
     * Синхронный пересчет агрегированных данных для акций (старая версия)
     */
    @PostMapping("/recalculate-shares-aggregation")
    public ResponseEntity<AggregationResult> recalculateSharesAggregation() {
        try {
            AggregationResult result = optimizedAggregationService.recalculateAllSharesData();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            AggregationResult errorResult = new AggregationResult("ERROR", "shares");
            errorResult.setSuccess(false);
            errorResult.setErrorMessage(e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }

    /**
     * Оптимизированный синхронный пересчет агрегированных данных для акций
     */
    @PostMapping("/recalculate-shares-aggregation-optimized")
    public ResponseEntity<AggregationResult> recalculateSharesAggregationOptimized() {
        try {
            AggregationResult result = optimizedAggregationService.recalculateAllSharesDataOptimized();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            AggregationResult errorResult = new AggregationResult("ERROR", "shares");
            errorResult.setSuccess(false);
            errorResult.setErrorMessage(e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }

    /**
     * Асинхронный пересчет агрегированных данных для акций
     * Запускает процесс в фоновом режиме
     */
    @PostMapping("/recalculate-shares-aggregation-async")
    public ResponseEntity<String> recalculateSharesAggregationAsync() {
        try {
            // Запускаем асинхронную задачу
            optimizedAggregationService.recalculateAllSharesDataAsync();
            
            // Возвращаем информацию о запущенной задаче
            return ResponseEntity.ok("Агрегация запущена в фоновом режиме. " +
                                   "Проверьте логи приложения для отслеживания прогресса.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка запуска агрегации: " + e.getMessage());
        }
    }

    /**
     * Проверка статуса агрегированных данных
     * Показывает общую статистику по агрегированным данным
     */
    @GetMapping("/shares-aggregation-status")
    public ResponseEntity<Map<String, Object>> getSharesAggregationStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            
            // Общее количество акций
            long totalShares = shareRepository.count();
            status.put("totalShares", totalShares);
            
            // Количество агрегированных записей
            long aggregatedRecords = sharesAggregatedDataRepository.count();
            status.put("aggregatedRecords", aggregatedRecords);
            
            // Процент покрытия
            double coverage = totalShares > 0 ? (double) aggregatedRecords / totalShares * 100 : 0;
            status.put("coveragePercentage", String.format("%.2f%%", coverage));
            
            // Статус
            boolean isComplete = aggregatedRecords > 0 && coverage > 90; // Минимум 90% покрытие
            status.put("isComplete", isComplete);
            status.put("statusMessage", isComplete ? "Агрегация завершена" : "Требуется пересчет");
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Ошибка получения статуса: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

}