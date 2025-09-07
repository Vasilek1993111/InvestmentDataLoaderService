package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.service.TInvestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class TInvestController {

    private final TInvestService service;

    public TInvestController(TInvestService service) {
        this.service = service;
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDto>> accounts() {
        return ResponseEntity.ok(service.getAccounts());
    }

    @GetMapping("/shares")
    public ResponseEntity<List<ShareDto>> shares(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String ticker,
            @RequestParam(required = false) String figi
    ) {
        return ResponseEntity.ok(service.getShares(status, exchange, currency, ticker, figi));
    }

    @PostMapping("/shares")
    public ResponseEntity<SaveResponseDto> saveShares(@RequestBody ShareFilterDto filter) {
        SaveResponseDto response = service.saveShares(filter);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/futures")
    public ResponseEntity<List<FutureDto>> futures(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String ticker,
            @RequestParam(required = false) String assetType
    ) {
        return ResponseEntity.ok(service.getFutures(status, exchange, currency, ticker, assetType));
    }

    @PostMapping("/futures")
    public ResponseEntity<SaveResponseDto> saveFutures(@RequestBody FutureFilterDto filter) {
        SaveResponseDto response = service.saveFutures(filter);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/close-prices")
    public ResponseEntity<List<ClosePriceDto>> closes(
            @RequestParam(required = false) List<String> instrumentId,
            @RequestParam(required = false) String instrumentStatus
    ) {
        return ResponseEntity.ok(service.getClosePrices(instrumentId, instrumentStatus));
    }

    @PostMapping("/close-prices")
    public ResponseEntity<SaveResponseDto> saveClosePrices(@RequestBody(required = false) ClosePriceRequestDto request) {
        // Если request null, создаем пустой объект
        if (request == null) {
            request = new ClosePriceRequestDto();
        }
        SaveResponseDto response = service.saveClosePrices(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trading-schedules")
    public ResponseEntity<List<TradingScheduleDto>> schedules(
            @RequestParam(required = false) String exchange,
            @RequestParam String from,
            @RequestParam String to
    ) {
        Instant f = Instant.parse(from), t = Instant.parse(to);
        return ResponseEntity.ok(service.getTradingSchedules(exchange, f, t));
    }

    @GetMapping("/trading-statuses")
    public ResponseEntity<List<TradingStatusDto>> statuses(
            @RequestParam List<String> instrumentId
    ) {
        return ResponseEntity.ok(service.getTradingStatuses(instrumentId));
    }

    @PostMapping("/candles")
    public ResponseEntity<CandleLoadResponseDto> saveCandles(@RequestBody(required = false) CandleRequestDto request) {
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

}