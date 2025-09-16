package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.service.TInvestService;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.IndicativeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Контроллер для управления финансовыми инструментами
 * Объединяет функциональность работы с акциями, фьючерсами и индикативами
 */
@RestController
@RequestMapping("/api/instruments")
public class InstrumentsController {

    private final TInvestService service;
    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final IndicativeRepository indicativeRepository;

    public InstrumentsController(TInvestService service,
                               ShareRepository shareRepository,
                               FutureRepository futureRepository,
                               IndicativeRepository indicativeRepository) {
        this.service = service;
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.indicativeRepository = indicativeRepository;
    }

    // ==================== АКЦИИ ====================

    /**
     * Получение списка акций с фильтрацией
     */
    @GetMapping("/shares")
    public ResponseEntity<List<ShareDto>> getShares(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String ticker,
            @RequestParam(required = false) String figi
    ) {
        return ResponseEntity.ok(service.getShares(status, exchange, currency, ticker, figi));
    }

    /**
     * Сохранение акций по фильтру
     */
    @PostMapping("/shares")
    public ResponseEntity<SaveResponseDto> saveShares(@RequestBody ShareFilterDto filter) {
        SaveResponseDto response = service.saveShares(filter);
        return ResponseEntity.ok(response);
    }

    // ==================== ФЬЮЧЕРСЫ ====================

    /**
     * Получение списка фьючерсов с фильтрацией
     */
    @GetMapping("/futures")
    public ResponseEntity<List<FutureDto>> getFutures(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String ticker,
            @RequestParam(required = false) String assetType
    ) {
        return ResponseEntity.ok(service.getFutures(status, exchange, currency, ticker, assetType));
    }

    /**
     * Сохранение фьючерсов по фильтру
     */
    @PostMapping("/futures")
    public ResponseEntity<SaveResponseDto> saveFutures(@RequestBody FutureFilterDto filter) {
        SaveResponseDto response = service.saveFutures(filter);
        return ResponseEntity.ok(response);
    }

    // ==================== ИНДИКАТИВЫ ====================

    /**
     * Получение списка индикативов с фильтрацией
     */
    @GetMapping("/indicatives")
    public ResponseEntity<List<IndicativeDto>> getIndicatives(
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String ticker,
            @RequestParam(required = false) String figi
    ) {
        return ResponseEntity.ok(service.getIndicatives(exchange, currency, ticker, figi));
    }

    /**
     * Сохранение индикативов по фильтру
     */
    @PostMapping("/indicatives")
    public ResponseEntity<SaveResponseDto> saveIndicatives(@RequestBody IndicativeFilterDto filter) {
        SaveResponseDto response = service.saveIndicatives(filter);
        return ResponseEntity.ok(response);
    }

    /**
     * Получение индикатива по FIGI
     */
    @GetMapping("/indicatives/{figi}")
    public ResponseEntity<IndicativeDto> getIndicativeByFigi(@PathVariable String figi) {
        IndicativeDto indicative = service.getIndicativeBy(figi);
        if (indicative != null) {
            return ResponseEntity.ok(indicative);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Получение индикатива по тикеру
     */
    @GetMapping("/indicatives/ticker/{ticker}")
    public ResponseEntity<IndicativeDto> getIndicativeByTicker(@PathVariable String ticker) {
        IndicativeDto indicative = service.getIndicativeByTicker(ticker);
        if (indicative != null) {
            return ResponseEntity.ok(indicative);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== СТАТИСТИКА И СЧЕТЧИКИ ====================

    /**
     * Получение количества инструментов по типам
     */
    @GetMapping("/count")
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

    /**
     * Получение статистики по инструментам
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getInstrumentsStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long sharesCount = shareRepository.count();
            long futuresCount = futureRepository.count();
            long indicativesCount = indicativeRepository.count();
            
            stats.put("success", true);
            stats.put("shares", sharesCount);
            stats.put("futures", futuresCount);
            stats.put("indicatives", indicativesCount);
            stats.put("total", sharesCount + futuresCount + indicativesCount);
            stats.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            stats.put("success", false);
            stats.put("message", "Ошибка получения статистики инструментов: " + e.getMessage());
            stats.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(stats);
        }
    }

    // ==================== ПОИСК И ФИЛЬТРАЦИЯ ====================

    /**
     * Поиск инструментов по тикеру или FIGI
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchInstruments(
            @RequestParam String query,
            @RequestParam(required = false) String type
    ) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Поиск по акциям
            if (type == null || "shares".equals(type)) {
                List<ShareDto> shares = service.getShares(null, null, null, query, query);
                result.put("shares", shares);
            }
            
            // Поиск по фьючерсам
            if (type == null || "futures".equals(type)) {
                List<FutureDto> futures = service.getFutures(null, null, null, query, null);
                result.put("futures", futures);
            }
            
            // Поиск по индикативам
            if (type == null || "indicatives".equals(type)) {
                List<IndicativeDto> indicatives = service.getIndicatives(null, null, query, query);
                result.put("indicatives", indicatives);
            }
            
            result.put("success", true);
            result.put("query", query);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Ошибка поиска инструментов: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * Получение инструментов по бирже
     */
    @GetMapping("/by-exchange/{exchange}")
    public ResponseEntity<Map<String, Object>> getInstrumentsByExchange(@PathVariable String exchange) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<ShareDto> shares = service.getShares(null, exchange, null, null, null);
            List<FutureDto> futures = service.getFutures(null, exchange, null, null, null);
            List<IndicativeDto> indicatives = service.getIndicatives(exchange, null, null, null);
            
            result.put("success", true);
            result.put("exchange", exchange);
            result.put("shares", shares);
            result.put("futures", futures);
            result.put("indicatives", indicatives);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Ошибка получения инструментов по бирже: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(result);
        }
    }
}
