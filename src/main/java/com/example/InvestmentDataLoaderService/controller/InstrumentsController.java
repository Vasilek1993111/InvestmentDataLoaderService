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
     * Получение списка акций с фильтрацией (из API)
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
     * Получение акции по FIGI или тикеру из базы данных
     */
    @GetMapping("/shares/{identifier}")
    public ResponseEntity<ShareDto> getShareByIdentifier(@PathVariable String identifier) {
        // Проверяем, является ли параметр FIGI (обычно длиннее и содержит специальные символы)
        if (identifier.length() > 10 || identifier.contains("-") || identifier.contains("_")) {
            // Если это похоже на FIGI, ищем по FIGI
            ShareDto share = service.getShareByFigi(identifier);
            if (share != null) {
                return ResponseEntity.ok(share);
            }
        }
        
        // Иначе ищем по тикеру
        ShareDto share = service.getShareByTicker(identifier);
        if (share != null) {
            return ResponseEntity.ok(share);
        } else {
            return ResponseEntity.notFound().build();
        }
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
     * Получение фьючерса по FIGI или тикеру из базы данных
     */
    @GetMapping("/futures/{identifier}")
    public ResponseEntity<FutureDto> getFutureByIdentifier(@PathVariable String identifier) {
        // Проверяем, является ли параметр FIGI (обычно длиннее и содержит специальные символы)
        if (identifier.length() > 10 || identifier.contains("-") || identifier.contains("_")) {
            // Если это похоже на FIGI, ищем по FIGI
            FutureDto future = service.getFutureByFigi(identifier);
            if (future != null) {
                return ResponseEntity.ok(future);
            }
        }
        
        // Иначе ищем по тикеру
        FutureDto future = service.getFutureByTicker(identifier);
        if (future != null) {
            return ResponseEntity.ok(future);
        } else {
            return ResponseEntity.notFound().build();
        }
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
     * Получение индикатива по FIGI или тикеру
     */
    @GetMapping("/indicatives/{identifier}")
    public ResponseEntity<IndicativeDto> getIndicativeByIdentifier(@PathVariable String identifier) {
        // Проверяем, является ли параметр FIGI (обычно длиннее и содержит специальные символы)
        if (identifier.length() > 10 || identifier.contains("-") || identifier.contains("_")) {
            // Если это похоже на FIGI, ищем по FIGI
            IndicativeDto indicative = service.getIndicativeBy(identifier);
            if (indicative != null) {
                return ResponseEntity.ok(indicative);
            }
        }
        
        // Иначе ищем по тикеру
        IndicativeDto indicative = service.getIndicativeByTicker(identifier);
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
}
