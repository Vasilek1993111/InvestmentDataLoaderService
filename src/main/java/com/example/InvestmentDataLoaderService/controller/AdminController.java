package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.service.ClosePriceSchedulerService;
import com.example.InvestmentDataLoaderService.service.CandleSchedulerService;
import com.example.InvestmentDataLoaderService.service.EveningSessionService;
import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ClosePriceSchedulerService closePriceScheduler;
    private final CandleSchedulerService candleScheduler;
    private final EveningSessionService eveningSessionService;

    public AdminController(ClosePriceSchedulerService closePriceScheduler, 
                          CandleSchedulerService candleScheduler,
                          EveningSessionService eveningSessionService) {
        this.closePriceScheduler = closePriceScheduler;
        this.candleScheduler = candleScheduler;
        this.eveningSessionService = eveningSessionService;
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
}