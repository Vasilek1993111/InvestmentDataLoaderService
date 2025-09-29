package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.enums.DataSourceType;
import com.example.InvestmentDataLoaderService.exception.ValidationException;
import com.example.InvestmentDataLoaderService.service.InstrumentService;
import com.example.InvestmentDataLoaderService.util.QueryParamValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
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
 * <p>Все методы возвращают данные в формате JSON и поддерживают HTTP статус-коды:</p>
 * <ul>
 *   <li><strong>200 OK</strong> - успешный запрос</li>
 *   <li><strong>400 Bad Request</strong> - некорректные параметры, валидация, формат данных</li>
 *   <li><strong>404 Not Found</strong> - инструмент не найден, эндпоинт не существует</li>
 *   <li><strong>500 Internal Server Error</strong> - внутренние ошибки сервера</li>
 * </ul>
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
     * GET /api/instruments/shares?exchange=moex_mrng_evng_e_wknd_dlr&currency=RUB
     * GET /api/instruments/shares?source=database&exchange=MOEX
     * GET /api/instruments/shares?status=INSTRUMENT_STATUS_BASE&source=api
     * POST /api/instruments/shares?source=database
     * {
     *   "exchange": "moex_mrng_evng_e_wknd_dlr",
     *   "currency": "RUB",
     *   "sector": "Technology"
     * }
     * </pre>
     * 
     * <p>Валидация параметров:</p>
     * <ul>
     *   <li><strong>source</strong>: "api" или "database" (по умолчанию "api")</li>
     *   <li><strong>status</strong>: INSTRUMENT_STATUS_UNSPECIFIED, INSTRUMENT_STATUS_BASE, INSTRUMENT_STATUS_ALL</li>
     *   <li><strong>exchange</strong>: moex_mrng_evng_e_wknd_dlr, MOEX, moex, SPB, FORTS_MAIN, UNKNOWN</li>
     *   <li><strong>currency</strong>: RUB, USD, rub, usd</li>
     * </ul>
     * 
     * <p>При передаче невалидных значений возвращается HTTP 400 Bad Request с описанием ошибки.</p>
     * 
     * @param source источник данных: "api" (по умолчанию) или "database"
     * @param status статус инструмента (только для API): INSTRUMENT_STATUS_UNSPECIFIED, INSTRUMENT_STATUS_BASE, INSTRUMENT_STATUS_ALL
     * @param exchange биржа: moex_mrng_evng_e_wknd_dlr, MOEX, moex, SPB, FORTS_MAIN, UNKNOWN
     * @param currency валюта: RUB, USD, rub, usd
     * @param ticker тикер инструмента (например: SBER, GAZP)
     * @param figi уникальный идентификатор инструмента
     * @param filter расширенный фильтр для базы данных (только для source=database)
     * @return список акций, соответствующих критериям фильтрации
     * @throws ValidationException при передаче невалидных параметров
     */
    @GetMapping("/shares")
    public ResponseEntity<?> getShares(
            @RequestParam(defaultValue = "api") String source,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String ticker,
            @RequestParam(required = false) String figi,
            @RequestBody(required = false) ShareFilterDto filter
    ) throws ValidationException {
        // Валидация разрешенных параметров
        QueryParamValidator.validateSharesParams();
        
        // Валидация параметров запроса
        SharesRequestParams params = SharesRequestParams.create(source, status, exchange, currency, ticker, figi);
        
        if (params.source() == DataSourceType.DATABASE) {
            // Если фильтр не передан, создаем его из параметров
            if (filter == null) {
                filter = new ShareFilterDto();
                filter.setExchange(exchange);
                filter.setCurrency(currency);
                filter.setTicker(ticker);
                filter.setFigi(figi);
            }
            
            List<ShareDto> shares = instrumentService.getSharesFromDatabase(filter);
            
            // Если акции не найдены, возвращаем 404
            if (shares.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Акции не найдены по заданным критериям");
                response.put("timestamp", LocalDateTime.now().toString());
                response.put("error", "SharesNotFound");
                response.put("path", "/api/instruments/shares");
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            return ResponseEntity.ok(shares);
        } else {
            // По умолчанию используем API
            List<ShareDto> shares = instrumentService.getShares(
                params.status() != null ? params.status().name() : null,
                params.exchange() != null ? params.exchange().getValue() : null,
                params.currency() != null ? params.currency().getValue() : null,
                params.ticker(),
                params.figi()
            );
            
            // Если акции не найдены, возвращаем 404
            if (shares.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Акции не найдены по заданным критериям");
                response.put("timestamp", LocalDateTime.now().toString());
                response.put("error", "SharesNotFound");
                response.put("path", "/api/instruments/shares");
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            return ResponseEntity.ok(shares);
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
     * @return акция, если найдена, иначе 404 Not Found с информативным сообщением
     */
    @GetMapping("/shares/{identifier}")
    public ResponseEntity<?> getShareByIdentifier(@PathVariable String identifier) {
        // Проверяем, является ли параметр FIGI (обычно длиннее и содержит специальные символы)
        if (identifier.length() > 10 || identifier.contains("-") || identifier.contains("_")) {
            // Если это похоже на FIGI, ищем по FIGI
            ShareDto share = instrumentService.getShareByFigi(identifier);
            if (share != null) {
                return ResponseEntity.ok(share);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Акция с FIGI '" + identifier + "' не найдена в базе данных",
                    "error", "NotFound",
                    "timestamp", LocalDateTime.now().toString(),
                    "identifier", identifier,
                    "type", "FIGI"
                ));
            }
        }
        
        // Иначе ищем по тикеру
        ShareDto share = instrumentService.getShareByTicker(identifier);
        if (share != null) {
            return ResponseEntity.ok(share);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", "Акция с тикером '" + identifier + "' не найдена в базе данных",
                "error", "NotFound",
                "timestamp", LocalDateTime.now().toString(),
                "identifier", identifier,
                "type", "TICKER"
            ));
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
     *   "status": "INSTRUMENT_STATUS_BASE",
     *   "exchange": "moex_mrng_evng_e_wknd_dlr",
     *   "currency": "RUB"
     * }
     * </pre>
     * 
     * <p>Валидация полей фильтра:</p>
     * <ul>
     *   <li><strong>status</strong>: INSTRUMENT_STATUS_UNSPECIFIED, INSTRUMENT_STATUS_BASE, INSTRUMENT_STATUS_ALL</li>
     *   <li><strong>exchange</strong>: moex_mrng_evng_e_wknd_dlr, MOEX, moex, SPB, FORTS_MAIN, UNKNOWN</li>
     *   <li><strong>currency</strong>: RUB, USD, rub, usd</li>
     * </ul>
     * 
     * @param filter фильтр для получения акций из API
     * @return результат операции сохранения с детальной статистикой
     * @throws ValidationException при передаче невалидных параметров фильтра
     */
    @PostMapping("/shares")
    @Transactional
    public ResponseEntity<?> saveShares(@RequestBody ShareFilterDto filter) throws ValidationException {
        // Валидация параметров фильтра
        ShareFilterRequestParams params = ShareFilterRequestParams.create(
            filter.getStatus(),
            filter.getExchange(),
            filter.getCurrency(),
            filter.getTicker(),
            filter.getFigi(),
            filter.getSector(),
            filter.getTradingStatus()
        );
        
        // Создаем новый фильтр с валидированными значениями
        ShareFilterDto validatedFilter = new ShareFilterDto();
        validatedFilter.setStatus(params.status() != null ? params.status().name() : null);
        validatedFilter.setExchange(params.exchange() != null ? params.exchange().getValue() : null);
        validatedFilter.setCurrency(params.currency() != null ? params.currency().getValue() : null);
        validatedFilter.setTicker(params.ticker());
        validatedFilter.setFigi(params.figi());
        validatedFilter.setSector(params.sector());
        validatedFilter.setTradingStatus(params.tradingStatus());
        
        SaveResponseDto response = instrumentService.saveShares(validatedFilter);
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
     * GET /api/instruments/futures?exchange=moex_mrng_evng_e_wknd_dlr&currency=RUB
     * GET /api/instruments/futures?assetType=COMMODITY&status=INSTRUMENT_STATUS_BASE
     * </pre>
     * 
     * <p>Валидация параметров:</p>
     * <ul>
     *   <li><strong>status</strong>: INSTRUMENT_STATUS_UNSPECIFIED, INSTRUMENT_STATUS_BASE, INSTRUMENT_STATUS_ALL</li>
     *   <li><strong>exchange</strong>: moex_mrng_evng_e_wknd_dlr, MOEX, moex, SPB, FORTS_MAIN, FORTS_EVENING, forts_futures_weekend, UNKNOWN</li>
     *   <li><strong>currency</strong>: RUB, USD, rub, usd</li>
     *   <li><strong>assetType</strong>: COMMODITY, CURRENCY, EQUITY, BOND, INDEX, INTEREST_RATE, CRYPTO, UNKNOWN</li>
     * </ul>
     * 
     * <p>При передаче невалидных значений возвращается HTTP 400 Bad Request с описанием ошибки.</p>
     * 
     * @param status статус инструмента: INSTRUMENT_STATUS_UNSPECIFIED, INSTRUMENT_STATUS_BASE, INSTRUMENT_STATUS_ALL
     * @param exchange биржа: moex_mrng_evng_e_wknd_dlr, MOEX, moex, SPB, FORTS_MAIN, FORTS_EVENING, forts_futures_weekend, UNKNOWN
     * @param currency валюта: RUB, USD, rub, usd
     * @param ticker тикер фьючерса
     * @param assetType тип базового актива: COMMODITY, CURRENCY, EQUITY, BOND, INDEX, INTEREST_RATE, CRYPTO, UNKNOWN
     * @return список фьючерсов, соответствующих критериям фильтрации
     * @throws ValidationException при передаче невалидных параметров
     */
    @GetMapping("/futures")
    public ResponseEntity<?> getFutures(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String ticker,
            @RequestParam(required = false) String assetType
    ) throws ValidationException {
        // Валидация разрешенных параметров
        QueryParamValidator.validateFuturesParams();
        
        // Валидация параметров запроса
        FuturesRequestParams params = FuturesRequestParams.create(status, exchange, currency, ticker, assetType);
        
        List<FutureDto> futures = instrumentService.getFutures(
            params.status() != null ? params.status().name() : null,
            params.exchange() != null ? params.exchange().getValue() : null,
            params.currency() != null ? params.currency().getValue() : null,
            params.ticker(),
            params.assetType() != null ? params.assetType().getValue() : null
        );
        
        // Если фьючерсы не найдены, возвращаем 404
        if (futures.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Фьючерсы не найдены по заданным критериям");
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("error", "FuturesNotFound");
            response.put("path", "/api/instruments/futures");
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        return ResponseEntity.ok(futures);
    }

    /**
     * Получение фьючерса по FIGI или тикеру из базы данных
     * 
     * <p>Автоматически определяет тип идентификатора аналогично методу для акций.</p>
     * 
     * @param identifier FIGI или тикер фьючерса
     * @return фьючерс, если найден, иначе 404 Not Found с информативным сообщением
     */
    @GetMapping("/futures/{identifier}")
    public ResponseEntity<?> getFutureByIdentifier(@PathVariable String identifier) {
        // Проверяем, является ли параметр FIGI (обычно длиннее и содержит специальные символы)
        if (identifier.length() > 10 || identifier.contains("-") || identifier.contains("_")) {
            // Если это похоже на FIGI, ищем по FIGI
            FutureDto future = instrumentService.getFutureByFigi(identifier);
            if (future != null) {
                return ResponseEntity.ok(future);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Фьючерс с FIGI '" + identifier + "' не найден в базе данных",
                    "error", "NotFound",
                    "timestamp", LocalDateTime.now().toString(),
                    "identifier", identifier,
                    "type", "FIGI"
                ));
            }
        }
        
        // Иначе ищем по тикеру
        FutureDto future = instrumentService.getFutureByTicker(identifier);
        if (future != null) {
            return ResponseEntity.ok(future);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", "Фьючерс с тикером '" + identifier + "' не найден в базе данных",
                "error", "NotFound",
                "timestamp", LocalDateTime.now().toString(),
                "identifier", identifier,
                "type", "TICKER"
            ));
        }
    }

    /**
     * Сохранение фьючерсов по фильтру
     * 
     * <p>Аналогично методу сохранения акций, но для фьючерсов.
     * Получает фьючерсы из API и сохраняет в БД с защитой от дубликатов.</p>
     * 
     * <p>Пример использования:</p>
     * <pre>
     * POST /api/instruments/futures
     * {
     *   "status": "INSTRUMENT_STATUS_BASE",
     *   "exchange": "moex_mrng_evng_e_wknd_dlr",
     *   "currency": "RUB",
     *   "assetType": "COMMODITY"
     * }
     * </pre>
     * 
     * <p>Валидация полей фильтра:</p>
     * <ul>
     *   <li><strong>status</strong>: INSTRUMENT_STATUS_UNSPECIFIED, INSTRUMENT_STATUS_BASE, INSTRUMENT_STATUS_ALL</li>
     *   <li><strong>exchange</strong>: moex_mrng_evng_e_wknd_dlr, MOEX, moex, SPB, FORTS_MAIN, UNKNOWN</li>
     *   <li><strong>currency</strong>: RUB, USD, rub, usd</li>
     * </ul>
     * 
     * @param filter фильтр для получения фьючерсов из API
     * @return результат операции сохранения с детальной статистикой
     * @throws ValidationException при передаче невалидных параметров фильтра
     */
    @PostMapping("/futures")
    @Transactional
    public ResponseEntity<?> saveFutures(@RequestBody FutureFilterDto filter) throws ValidationException {
        // Валидация параметров фильтра
        FutureFilterRequestParams params = FutureFilterRequestParams.create(
            filter.getStatus(),
            filter.getExchange(),
            filter.getCurrency(),
            filter.getTicker(),
            filter.getAssetType()
        );
        
        // Создаем новый фильтр с валидированными значениями
        FutureFilterDto validatedFilter = new FutureFilterDto();
        validatedFilter.setStatus(params.status() != null ? params.status().name() : null);
        validatedFilter.setExchange(params.exchange() != null ? params.exchange().getValue() : null);
        validatedFilter.setCurrency(params.currency() != null ? params.currency().getValue() : null);
        validatedFilter.setTicker(params.ticker());
        validatedFilter.setAssetType(params.assetType());
        
        SaveResponseDto response = instrumentService.saveFutures(validatedFilter);
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
    public ResponseEntity<?> getIndicatives(
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String ticker,
            @RequestParam(required = false) String figi
    ) throws ValidationException {
        // Валидация разрешенных параметров
        QueryParamValidator.validateIndicativesParams();
        
        List<IndicativeDto> indicatives = instrumentService.getIndicatives(exchange, currency, ticker, figi);
        
        // Если индикативы не найдены, возвращаем 404
        if (indicatives.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Индикативные инструменты не найдены по заданным критериям");
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("error", "IndicativesNotFound");
            response.put("path", "/api/instruments/indicatives");
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        return ResponseEntity.ok(indicatives);
    }

    /**
     * Сохранение индикативов по фильтру
     * 
     * <p>Аналогично методам сохранения акций и фьючерсов, но для индикативных инструментов.
     * Получает индикативы из API и сохраняет в БД с защитой от дубликатов.</p>
     * 
     * <p>Пример использования:</p>
     * <pre>
     * POST /api/instruments/indicatives
     * {
     *   "exchange": "moex_mrng_evng_e_wknd_dlr",
     *   "currency": "RUB",
     *   "ticker": "IMOEX"
     * }
     * </pre>
     * 
     * <p>Валидация полей фильтра:</p>
     * <ul>
     *   <li><strong>exchange</strong>: moex_mrng_evng_e_wknd_dlr, MOEX, moex, SPB, FORTS_MAIN, UNKNOWN</li>
     *   <li><strong>currency</strong>: RUB, USD, rub, usd</li>
     * </ul>
     * 
     * @param filter фильтр для получения индикативов из API
     * @return результат операции сохранения с детальной статистикой
     * @throws ValidationException при передаче невалидных параметров фильтра
     */
    @PostMapping("/indicatives")
    @Transactional
    public ResponseEntity<?> saveIndicatives(@RequestBody IndicativeFilterDto filter) throws ValidationException {
        // Валидация параметров фильтра
        IndicativeFilterRequestParams params = IndicativeFilterRequestParams.create(
            filter.getExchange(),
            filter.getCurrency(),
            filter.getTicker(),
            filter.getFigi()
        );
        
        // Создаем новый фильтр с валидированными значениями
        IndicativeFilterDto validatedFilter = new IndicativeFilterDto();
        validatedFilter.setExchange(params.exchange() != null ? params.exchange().getValue() : null);
        validatedFilter.setCurrency(params.currency() != null ? params.currency().getValue() : null);
        validatedFilter.setTicker(params.ticker());
        validatedFilter.setFigi(params.figi());
        
        SaveResponseDto response = instrumentService.saveIndicatives(validatedFilter);
        return ResponseEntity.ok(response);
    }

    /**
     * Получение индикатива по FIGI или тикеру
     * 
     * <p>Автоматически определяет тип идентификатора аналогично методам для акций и фьючерсов.</p>
     * 
     * @param identifier FIGI или тикер индикативного инструмента
     * @return индикативный инструмент, если найден, иначе 404 Not Found с информативным сообщением
     */
    @GetMapping("/indicatives/{identifier}")
    public ResponseEntity<?> getIndicativeByIdentifier(@PathVariable String identifier) {
        // Проверяем, является ли параметр FIGI (обычно длиннее и содержит специальные символы)
        if (identifier.length() > 10 || identifier.contains("-") || identifier.contains("_")) {
            // Если это похоже на FIGI, ищем по FIGI
            IndicativeDto indicative = instrumentService.getIndicativeBy(identifier);
            if (indicative != null) {
                return ResponseEntity.ok(indicative);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Индикативный инструмент с FIGI '" + identifier + "' не найден в базе данных",
                    "error", "NotFound",
                    "timestamp", LocalDateTime.now().toString(),
                    "identifier", identifier,
                    "type", "FIGI"
                ));
            }
        }
        
        // Иначе ищем по тикеру
        IndicativeDto indicative = instrumentService.getIndicativeByTicker(identifier);
        if (indicative != null) {
            return ResponseEntity.ok(indicative);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", "Индикативный инструмент с тикером '" + identifier + "' не найден в базе данных",
                "error", "NotFound",
                "timestamp", LocalDateTime.now().toString(),
                "identifier", identifier,
                "type", "TICKER"
            ));
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
