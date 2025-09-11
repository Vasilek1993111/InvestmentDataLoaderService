package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.service.ClosePriceSchedulerService;
import com.example.InvestmentDataLoaderService.service.CandleSchedulerService;
import com.example.InvestmentDataLoaderService.service.EveningSessionService;
import com.example.InvestmentDataLoaderService.service.MorningSessionService;
import com.example.InvestmentDataLoaderService.service.LastTradesService;
import com.example.InvestmentDataLoaderService.service.AggregationService;
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

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ClosePriceSchedulerService closePriceScheduler;
    private final CandleSchedulerService candleScheduler;
    private final EveningSessionService eveningSessionService;
    private final MorningSessionService morningSessionService;
    private final LastTradesService lastTradesService;
    private final AggregationService aggregationService;

    public AdminController(ClosePriceSchedulerService closePriceScheduler, 
                          CandleSchedulerService candleScheduler,
                          EveningSessionService eveningSessionService,
                          MorningSessionService morningSessionService,
                          LastTradesService lastTradesService,
                          AggregationService aggregationService) {
        this.closePriceScheduler = closePriceScheduler;
        this.candleScheduler = candleScheduler;
        this.eveningSessionService = eveningSessionService;
        this.morningSessionService = morningSessionService;
        this.lastTradesService = lastTradesService;
        this.aggregationService = aggregationService;
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

    // Пересчет агрегированных данных для всех акций
    @PostMapping("/recalculate-shares-aggregation")
    public ResponseEntity<AggregationResult> recalculateSharesAggregation() {
        try {
            AggregationResult result = aggregationService.recalculateAllSharesData();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new AggregationResult());
        }
    }

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
}