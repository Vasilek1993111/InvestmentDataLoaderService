package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.service.TInvestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Контроллер для работы с торговыми данными
 * Управляет счетами, ценами, расписаниями и статусами торгов
 */
@RestController
@RequestMapping("/api/trading")
public class TradingController {

    private final TInvestService service;

    public TradingController(TInvestService service) {
        this.service = service;
    }

    // ==================== СЧЕТА ====================

    /**
     * Получение списка счетов
     */
    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDto>> getAccounts() {
        return ResponseEntity.ok(service.getAccounts());
    }

    // ==================== ЦЕНЫ ЗАКРЫТИЯ ====================

    /**
     * Получение цен закрытия с фильтрацией
     */
    @GetMapping("/close-prices")
    public ResponseEntity<List<ClosePriceDto>> getClosePrices(
            @RequestParam(required = false) List<String> instrumentId,
            @RequestParam(required = false) String instrumentStatus
    ) {
        return ResponseEntity.ok(service.getClosePrices(instrumentId, instrumentStatus));
    }

    // ==================== ТОРГОВЫЕ РАСПИСАНИЯ ====================

    /**
     * Получение торговых расписаний
     */
    @GetMapping("/schedules")
    public ResponseEntity<List<TradingScheduleDto>> getTradingSchedules(
            @RequestParam(required = false) String exchange,
            @RequestParam String from,
            @RequestParam String to
    ) {
        Instant fromInstant = Instant.parse(from);
        Instant toInstant = Instant.parse(to);
        return ResponseEntity.ok(service.getTradingSchedules(exchange, fromInstant, toInstant));
    }

    /**
     * Получение торговых расписаний за период (упрощенный API)
     */
    @GetMapping("/schedules/period")
    public ResponseEntity<Map<String, Object>> getTradingSchedulesForPeriod(
            @RequestParam(required = false) String exchange,
            @RequestParam String from,
            @RequestParam String to
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Instant fromInstant = Instant.parse(from);
            Instant toInstant = Instant.parse(to);
            List<TradingScheduleDto> schedules = service.getTradingSchedules(exchange, fromInstant, toInstant);
            
            response.put("success", true);
            response.put("data", schedules);
            response.put("count", schedules.size());
            response.put("from", from);
            response.put("to", to);
            response.put("exchange", exchange);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения торговых расписаний: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== ТОРГОВЫЕ СТАТУСЫ ====================

    /**
     * Получение торговых статусов инструментов
     */
    @GetMapping("/statuses")
    public ResponseEntity<List<TradingStatusDto>> getTradingStatuses(
            @RequestParam List<String> instrumentId
    ) {
        return ResponseEntity.ok(service.getTradingStatuses(instrumentId));
    }

    /**
     * Получение торговых статусов с дополнительной информацией
     */
    @GetMapping("/statuses/detailed")
    public ResponseEntity<Map<String, Object>> getDetailedTradingStatuses(
            @RequestParam List<String> instrumentId
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<TradingStatusDto> statuses = service.getTradingStatuses(instrumentId);
            
            response.put("success", true);
            response.put("data", statuses);
            response.put("count", statuses.size());
            response.put("requested_instruments", instrumentId.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения торговых статусов: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== ТОРГОВЫЕ ДНИ ====================

    /**
     * Получение информации о торговых днях
     */
    @GetMapping("/trading-days")
    public ResponseEntity<Map<String, Object>> getTradingDays(
            @RequestParam(required = false) String exchange,
            @RequestParam String from,
            @RequestParam String to
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Instant fromInstant = Instant.parse(from);
            Instant toInstant = Instant.parse(to);
            List<TradingScheduleDto> schedules = service.getTradingSchedules(exchange, fromInstant, toInstant);
            
            // Группируем по дням и определяем торговые дни
            Map<String, Object> tradingDays = new HashMap<>();
            int tradingDaysCount = 0;
            int nonTradingDaysCount = 0;
            
            for (TradingScheduleDto schedule : schedules) {
                for (TradingDayDto day : schedule.days()) {
                    if (day.isTradingDay()) {
                        tradingDaysCount++;
                        tradingDays.put(day.date(), "trading");
                    } else {
                        nonTradingDaysCount++;
                        tradingDays.put(day.date(), "non-trading");
                    }
                }
            }
            
            response.put("success", true);
            response.put("trading_days", tradingDays);
            response.put("trading_days_count", tradingDaysCount);
            response.put("non_trading_days_count", nonTradingDaysCount);
            response.put("total_days", tradingDaysCount + nonTradingDaysCount);
            response.put("from", from);
            response.put("to", to);
            response.put("exchange", exchange);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения информации о торговых днях: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== СТАТИСТИКА ТОРГОВ ====================

    /**
     * Получение статистики торгов за период
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getTradingStats(
            @RequestParam(required = false) String exchange,
            @RequestParam String from,
            @RequestParam String to
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Instant fromInstant = Instant.parse(from);
            Instant toInstant = Instant.parse(to);
            List<TradingScheduleDto> schedules = service.getTradingSchedules(exchange, fromInstant, toInstant);
            
            // Анализируем расписания
            long tradingDays = 0;
            long nonTradingDays = 0;
            
            for (TradingScheduleDto schedule : schedules) {
                for (TradingDayDto day : schedule.days()) {
                    if (day.isTradingDay()) {
                        tradingDays++;
                    } else {
                        nonTradingDays++;
                    }
                }
            }
            
            long totalDays = tradingDays + nonTradingDays;
            
            response.put("success", true);
            response.put("period", Map.of(
                "from", from,
                "to", to,
                "exchange", exchange != null ? exchange : "all"
            ));
            response.put("trading_days", tradingDays);
            response.put("non_trading_days", nonTradingDays);
            response.put("total_days", totalDays);
            response.put("trading_percentage", totalDays > 0 ? (double) tradingDays / totalDays * 100 : 0);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения статистики торгов: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== ПОИСК ПО ТОРГОВЫМ ДАННЫМ ====================

    /**
     * Поиск инструментов по торговым данным
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchTradingData(
            @RequestParam String query,
            @RequestParam(required = false) String type
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Здесь можно добавить поиск по торговым данным
            // Пока возвращаем базовую структуру
            response.put("success", true);
            response.put("query", query);
            response.put("type", type);
            response.put("message", "Поиск по торговым данным выполнен");
            response.put("results", List.of()); // Пустой список результатов
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка поиска по торговым данным: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
