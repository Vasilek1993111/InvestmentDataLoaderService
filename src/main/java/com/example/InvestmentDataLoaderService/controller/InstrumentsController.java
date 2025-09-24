package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.service.InstrumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Контроллер для управления финансовыми инструментами
 * 
 * <p>Предоставляет REST API для работы с тремя типами финансовых инструментов:</p>
 * <ul>
 *   <li><strong>Акции (Shares)</strong> - обыкновенные и привилегированные акции</li>
 *   <li><strong>Фьючерсы (Futures)</strong> - производные финансовые инструменты</li>
 *   <li><strong>Индикативы (Indicatives)</strong> - индикативные инструменты</li>
 * </ul>
 * 
 * <p>Основные возможности:</p>
 * <ul>
 *   <li>Получение инструментов из внешнего API Tinkoff или локальной БД</li>
 *   <li>Фильтрация по различным параметрам (биржа, валюта, тикер, FIGI)</li>
 *   <li>Сохранение инструментов в БД с защитой от дубликатов</li>
 *   <li>Поиск инструментов по FIGI или тикеру</li>
 *   <li>Получение статистики по количеству инструментов</li>
 * </ul>
 * 
 * <p>Все методы возвращают данные в формате JSON и поддерживают HTTP статус-коды.</p>
 * 
 * @author InvestmentDataLoaderService
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/instruments")
public class InstrumentsController {

    private final InstrumentService instrumentService;

    public InstrumentsController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    // ==================== АКЦИИ ====================

    /**
     * Получение списка акций с фильтрацией
     * 
     * <p>Поддерживает два источника данных:</p>
     * <ul>
     *   <li><strong>API (по умолчанию)</strong> - получение данных из Tinkoff API с кэшированием</li>
     *   <li><strong>Database</strong> - получение данных из локальной БД</li>
     * </ul>
     * 
     * <p>Примеры использования:</p>
     * <pre>
     * GET /api/instruments/shares?exchange=MOEX&currency=RUB
     * GET /api/instruments/shares?source=database&exchange=MOEX
     * POST /api/instruments/shares?source=database
     * {
     *   "exchange": "MOEX",
     *   "currency": "RUB",
     *   "sector": "Technology"
     * }
     * </pre>
     * 
     * @param source источник данных: "api" (по умолчанию) или "database"
     * @param status статус инструмента (только для API): INSTRUMENT_STATUS_ACTIVE, INSTRUMENT_STATUS_BASE
     * @param exchange биржа (например: MOEX, SPB)
     * @param currency валюта (например: RUB, USD, EUR)
     * @param ticker тикер инструмента (например: SBER, GAZP)
     * @param figi уникальный идентификатор инструмента
     * @param filter расширенный фильтр для базы данных (только для source=database)
     * @return список акций, соответствующих критериям фильтрации
     */
    @GetMapping("/shares")
    public ResponseEntity<List<ShareDto>> getShares(
            @RequestParam(defaultValue = "api") String source,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String ticker,
            @RequestParam(required = false) String figi,
            @RequestBody(required = false) ShareFilterDto filter
    ) {
        if ("database".equalsIgnoreCase(source)) {
            // Если фильтр не передан, создаем его из параметров
            if (filter == null) {
                filter = new ShareFilterDto();
                filter.setExchange(exchange);
                filter.setCurrency(currency);
                filter.setTicker(ticker);
                filter.setFigi(figi);
            }
            return ResponseEntity.ok(instrumentService.getSharesFromDatabase(filter));
        } else {
            // По умолчанию используем API
            return ResponseEntity.ok(instrumentService.getShares(status, exchange, currency, ticker, figi));
        }
    }


    /**
     * Получение акции по FIGI или тикеру из базы данных
     * 
     * <p>Автоматически определяет тип идентификатора:</p>
     * <ul>
     *   <li>Если идентификатор длиннее 10 символов или содержит "-" или "_" - поиск по FIGI</li>
     *   <li>Иначе - поиск по тикеру</li>
     * </ul>
     * 
     * <p>Примеры использования:</p>
     * <pre>
     * GET /api/instruments/shares/BBG004730N88  (поиск по FIGI)
     * GET /api/instruments/shares/SBER          (поиск по тикеру)
     * </pre>
     * 
     * @param identifier FIGI или тикер акции
     * @return акция, если найдена, иначе 404 Not Found
     */
    @GetMapping("/shares/{identifier}")
    public ResponseEntity<ShareDto> getShareByIdentifier(@PathVariable String identifier) {
        // Проверяем, является ли параметр FIGI (обычно длиннее и содержит специальные символы)
        if (identifier.length() > 10 || identifier.contains("-") || identifier.contains("_")) {
            // Если это похоже на FIGI, ищем по FIGI
            ShareDto share = instrumentService.getShareByFigi(identifier);
            if (share != null) {
                return ResponseEntity.ok(share);
            }
        }
        
        // Иначе ищем по тикеру
        ShareDto share = instrumentService.getShareByTicker(identifier);
        if (share != null) {
            return ResponseEntity.ok(share);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Сохранение акций по фильтру
     * 
     * <p>Получает акции из Tinkoff API по заданным фильтрам и сохраняет их в БД.
     * Если акция уже существует в БД, она не будет сохранена повторно.</p>
     * 
     * <p>Возвращает детальную информацию о результате операции:</p>
     * <ul>
     *   <li>Количество найденных акций в API</li>
     *   <li>Количество сохраненных новых акций</li>
     *   <li>Количество уже существующих акций</li>
     * </ul>
     * 
     * <p>Пример использования:</p>
     * <pre>
     * POST /api/instruments/shares
     * {
     *   "status": "INSTRUMENT_STATUS_ACTIVE",
     *   "exchange": "MOEX",
     *   "currency": "RUB"
     * }
     * </pre>
     * 
     * @param filter фильтр для получения акций из API
     * @return результат операции сохранения с детальной статистикой
     */
    @PostMapping("/shares")
    public ResponseEntity<SaveResponseDto> saveShares(@RequestBody ShareFilterDto filter) {
        SaveResponseDto response = instrumentService.saveShares(filter);
        return ResponseEntity.ok(response);
    }


    // ==================== ФЬЮЧЕРСЫ ====================

    /**
     * Получение списка фьючерсов с фильтрацией
     * 
     * <p>Получает фьючерсы из Tinkoff API с применением фильтров.
     * Данные кэшируются для повышения производительности.</p>
     * 
     * <p>Примеры использования:</p>
     * <pre>
     * GET /api/instruments/futures?exchange=MOEX&currency=RUB
     * GET /api/instruments/futures?assetType=COMMODITY&status=INSTRUMENT_STATUS_ACTIVE
     * </pre>
     * 
     * @param status статус инструмента: INSTRUMENT_STATUS_ACTIVE, INSTRUMENT_STATUS_BASE
     * @param exchange биржа (например: MOEX, SPB)
     * @param currency валюта (например: RUB, USD, EUR)
     * @param ticker тикер фьючерса
     * @param assetType тип базового актива (например: COMMODITY, CURRENCY, EQUITY)
     * @return список фьючерсов, соответствующих критериям фильтрации
     */
    @GetMapping("/futures")
    public ResponseEntity<List<FutureDto>> getFutures(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String ticker,
            @RequestParam(required = false) String assetType
    ) {
        return ResponseEntity.ok(instrumentService.getFutures(status, exchange, currency, ticker, assetType));
    }

    /**
     * Получение фьючерса по FIGI или тикеру из базы данных
     * 
     * <p>Автоматически определяет тип идентификатора аналогично методу для акций.</p>
     * 
     * @param identifier FIGI или тикер фьючерса
     * @return фьючерс, если найден, иначе 404 Not Found
     */
    @GetMapping("/futures/{identifier}")
    public ResponseEntity<FutureDto> getFutureByIdentifier(@PathVariable String identifier) {
        // Проверяем, является ли параметр FIGI (обычно длиннее и содержит специальные символы)
        if (identifier.length() > 10 || identifier.contains("-") || identifier.contains("_")) {
            // Если это похоже на FIGI, ищем по FIGI
            FutureDto future = instrumentService.getFutureByFigi(identifier);
            if (future != null) {
                return ResponseEntity.ok(future);
            }
        }
        
        // Иначе ищем по тикеру
        FutureDto future = instrumentService.getFutureByTicker(identifier);
        if (future != null) {
            return ResponseEntity.ok(future);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Сохранение фьючерсов по фильтру
     * 
     * <p>Аналогично методу сохранения акций, но для фьючерсов.
     * Получает фьючерсы из API и сохраняет в БД с защитой от дубликатов.</p>
     * 
     * @param filter фильтр для получения фьючерсов из API
     * @return результат операции сохранения с детальной статистикой
     */
    @PostMapping("/futures")
    public ResponseEntity<SaveResponseDto> saveFutures(@RequestBody FutureFilterDto filter) {
        SaveResponseDto response = instrumentService.saveFutures(filter);
        return ResponseEntity.ok(response);
    }

    // ==================== ИНДИКАТИВЫ ====================

    /**
     * Получение списка индикативов с фильтрацией
     * 
     * <p>Получает индикативные инструменты из Tinkoff REST API или БД (fallback).
     * Данные кэшируются для повышения производительности.</p>
     * 
     * <p>Примеры использования:</p>
     * <pre>
     * GET /api/instruments/indicatives?exchange=MOEX&currency=RUB
     * GET /api/instruments/indicatives?ticker=USD000UTSTOM
     * </pre>
     * 
     * @param exchange биржа (например: MOEX, SPB)
     * @param currency валюта (например: RUB, USD, EUR)
     * @param ticker тикер индикативного инструмента
     * @param figi уникальный идентификатор инструмента
     * @return список индикативных инструментов, соответствующих критериям фильтрации
     */
    @GetMapping("/indicatives")
    public ResponseEntity<List<IndicativeDto>> getIndicatives(
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String ticker,
            @RequestParam(required = false) String figi
    ) {
        return ResponseEntity.ok(instrumentService.getIndicatives(exchange, currency, ticker, figi));
    }

    /**
     * Сохранение индикативов по фильтру
     * 
     * <p>Аналогично методам сохранения акций и фьючерсов, но для индикативных инструментов.
     * Получает индикативы из API и сохраняет в БД с защитой от дубликатов.</p>
     * 
     * @param filter фильтр для получения индикативов из API
     * @return результат операции сохранения с детальной статистикой
     */
    @PostMapping("/indicatives")
    public ResponseEntity<SaveResponseDto> saveIndicatives(@RequestBody IndicativeFilterDto filter) {
        SaveResponseDto response = instrumentService.saveIndicatives(filter);
        return ResponseEntity.ok(response);
    }

    /**
     * Получение индикатива по FIGI или тикеру
     * 
     * <p>Автоматически определяет тип идентификатора аналогично методам для акций и фьючерсов.</p>
     * 
     * @param identifier FIGI или тикер индикативного инструмента
     * @return индикативный инструмент, если найден, иначе 404 Not Found
     */
    @GetMapping("/indicatives/{identifier}")
    public ResponseEntity<IndicativeDto> getIndicativeByIdentifier(@PathVariable String identifier) {
        // Проверяем, является ли параметр FIGI (обычно длиннее и содержит специальные символы)
        if (identifier.length() > 10 || identifier.contains("-") || identifier.contains("_")) {
            // Если это похоже на FIGI, ищем по FIGI
            IndicativeDto indicative = instrumentService.getIndicativeBy(identifier);
            if (indicative != null) {
                return ResponseEntity.ok(indicative);
            }
        }
        
        // Иначе ищем по тикеру
        IndicativeDto indicative = instrumentService.getIndicativeByTicker(identifier);
        if (indicative != null) {
            return ResponseEntity.ok(indicative);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== СТАТИСТИКА И СЧЕТЧИКИ ====================

    /**
     * Получение количества инструментов по типам
     * 
     * <p>Возвращает статистику по количеству инструментов в локальной БД:</p>
     * <ul>
     *   <li>shares - количество акций</li>
     *   <li>futures - количество фьючерсов</li>
     *   <li>indicatives - количество индикативных инструментов</li>
     *   <li>total - общее количество инструментов</li>
     * </ul>
     * 
     * <p>Пример ответа:</p>
     * <pre>
     * {
     *   "shares": 150,
     *   "futures": 45,
     *   "indicatives": 12,
     *   "total": 207
     * }
     * </pre>
     * 
     * @return карта с количеством инструментов по типам
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getInstrumentCounts() {
        Map<String, Long> counts = instrumentService.getInstrumentCounts();
        return ResponseEntity.ok(counts);
    }

}
