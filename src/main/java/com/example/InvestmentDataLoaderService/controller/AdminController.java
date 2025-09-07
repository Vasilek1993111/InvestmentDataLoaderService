package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.service.ClosePriceSchedulerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ClosePriceSchedulerService scheduler;

    public AdminController(ClosePriceSchedulerService scheduler) {
        this.scheduler = scheduler;
    }

    @PostMapping("/load-close-prices")
    public ResponseEntity<String> loadToday() {
        scheduler.fetchAndStoreClosePrices();
        return ResponseEntity.ok("Close prices loaded for today");
    }

    @PostMapping("/load-close-prices/{date}")
    public ResponseEntity<String> reloadDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        scheduler.fetchAndUpdateClosePricesForDate(date);
        return ResponseEntity.ok("Close prices reloaded for " + date);
    }
}