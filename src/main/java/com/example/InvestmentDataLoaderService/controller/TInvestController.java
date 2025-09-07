package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.service.TInvestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

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
            @RequestParam(required = false) String ticker
    ) {
        return ResponseEntity.ok(service.getShares(status, exchange, currency, ticker));
    }

    @PostMapping("/shares")
    public ResponseEntity<List<ShareDto>> saveShares(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String ticker
    ) {
        List<ShareDto> savedShares = service.saveShares(status, exchange, currency, ticker);
        return ResponseEntity.ok(savedShares);
    }

    @GetMapping("/futures")
    public ResponseEntity<List<FutureDto>> futures(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String exchange
    ) {
        return ResponseEntity.ok(service.getFutures(status, exchange));
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

    @GetMapping("/close-prices")
    public ResponseEntity<List<ClosePriceDto>> closes(
            @RequestParam List<String> instrumentId,
            @RequestParam(required = false) String instrumentStatus
    ) {
        return ResponseEntity.ok(service.getClosePrices(instrumentId, instrumentStatus));
    }
}