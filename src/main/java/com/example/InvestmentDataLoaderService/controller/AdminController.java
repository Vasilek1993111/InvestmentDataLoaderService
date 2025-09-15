package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.scheduler.ClosePriceSchedulerService;
import com.example.InvestmentDataLoaderService.scheduler.CandleSchedulerService;
import com.example.InvestmentDataLoaderService.scheduler.EveningSessionService;
import com.example.InvestmentDataLoaderService.scheduler.MorningSessionService;
import com.example.InvestmentDataLoaderService.scheduler.LastTradesService;
import com.example.InvestmentDataLoaderService.service.AggregationService;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.IndicativeRepository;
import com.example.InvestmentDataLoaderService.dto.LastTradesRequestDto;
import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.dto.AggregationResult;
import com.example.InvestmentDataLoaderService.dto.AggregationRequestDto;
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
    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final IndicativeRepository indicativeRepository;

    public AdminController(ClosePriceSchedulerService closePriceScheduler, 
                          CandleSchedulerService candleScheduler,
                          EveningSessionService eveningSessionService,
                          MorningSessionService morningSessionService,
                          LastTradesService lastTradesService,
                          AggregationService aggregationService,
                          ShareRepository shareRepository,
                          FutureRepository futureRepository,
                          IndicativeRepository indicativeRepository) {
        this.closePriceScheduler = closePriceScheduler;
        this.candleScheduler = candleScheduler;
        this.eveningSessionService = eveningSessionService;
        this.morningSessionService = morningSessionService;
        this.lastTradesService = lastTradesService;
        this.aggregationService = aggregationService;
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.indicativeRepository = indicativeRepository;
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

    @PostMapping("/load-candles/indicatives/{date}")
    public ResponseEntity<String> loadIndicativesCandlesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        candleScheduler.fetchAndStoreIndicativesCandlesForDate(date);
        return ResponseEntity.ok("Indicatives candles loading started for " + date);
    }

    @GetMapping("/instruments/count")
    public ResponseEntity<Map<String, Object>> getInstrumentsCount() {
        Map<String, Object> counts = new HashMap<>();
        
        // Подсчитываем инструменты в базе данных
        long sharesCount = shareRepository.count();
        long futuresCount = futureRepository.count();
        long indicativesCount = indicativeRepository.count();
        
        counts.put("shares", sharesCount);
        counts.put("futures", futuresCount);
        counts.put("indicatives", indicativesCount);
        counts.put("total", sharesCount + futuresCount + indicativesCount);
        
        return ResponseEntity.ok(counts);
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

    // === ЕДИНЫЙ ЭНДПОИНТ ДЛЯ АГРЕГИРОВАННЫХ ДАННЫХ ===

    /**
     * Унифицированный и оптимизированный пересчет агрегированных данных
     * Принимает в теле запроса ключевые слова: SHARES, FUTURES, ALL
     * Пример тела запроса: { "types": ["SHARES", "FUTURES"] }
     */
    @PostMapping("/recalculate-aggregation")
    public ResponseEntity<List<AggregationResult>> recalculateAggregation(@RequestBody AggregationRequestDto request) {
        try {
            List<AggregationResult> results = aggregationService.recalculateByTypes(
                request != null ? request.getTypes() : null
            );
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of(
                new AggregationResult("ERROR", "unknown")
            ));
        }
    }

}