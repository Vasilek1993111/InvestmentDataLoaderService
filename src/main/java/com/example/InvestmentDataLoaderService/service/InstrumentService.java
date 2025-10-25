package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.IndicativeEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.entity.SystemLogEntity;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.IndicativeRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.SystemLogRepository;
import com.example.InvestmentDataLoaderService.client.TinkoffRestClient;
import com.example.InvestmentDataLoaderService.client.TinkoffApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc.InstrumentsServiceBlockingStub;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Сервис для работы с финансовыми инструментами
 * 
 * <p>Предоставляет бизнес-логику для работы с тремя типами финансовых инструментов:</p>
 * <ul>
 *   <li><strong>Акции (Shares)</strong> - обыкновенные и привилегированные акции</li>
 *   <li><strong>Фьючерсы (Futures)</strong> - производные финансовые инструменты</li>
 *   <li><strong>Индикативы (Indicatives)</strong> - индикативные инструменты</li>
 * </ul>
 * 
 * <p>Основные возможности сервиса:</p>
 * <ul>
 *   <li>Получение инструментов из внешнего API Tinkoff с кэшированием</li>
 *   <li>Получение инструментов из локальной базы данных</li>
 *   <li>Сохранение инструментов в БД с защитой от дубликатов</li>
 *   <li>Фильтрация инструментов по различным параметрам</li>
 *   <li>Поиск инструментов по FIGI или тикеру</li>
 *   <li>Получение статистики по количеству инструментов</li>
 * </ul>
 * 
 * <p>Сервис использует:</p>
 * <ul>
 *   <li>gRPC клиент для получения акций и фьючерсов</li>
 *   <li>REST клиент для получения индикативных инструментов</li>
 *   <li>Spring Cache для кэширования данных</li>
 *   <li>JPA репозитории для работы с БД</li>
 * </ul>
 * 
 * @author InvestmentDataLoaderService
 * @version 1.0
 * @since 2024
 */
@Service
public class InstrumentService {

    private static final Logger log = LoggerFactory.getLogger(InstrumentService.class);
    private final InstrumentsServiceBlockingStub instrumentsService;
    private final ShareRepository shareRepo;
    private final FutureRepository futureRepo;
    private final IndicativeRepository indicativeRepo;
    private final SystemLogRepository systemLogRepository;
    private final TinkoffRestClient restClient;
    private final TinkoffApiClient tinkoffApiClient;

    /**
     * Конструктор сервиса инструментов
     * 
     * @param instrumentsService gRPC клиент для получения акций и фьючерсов из Tinkoff API
     * @param shareRepo репозиторий для работы с акциями в БД
     * @param futureRepo репозиторий для работы с фьючерсами в БД
     * @param indicativeRepo репозиторий для работы с индикативными инструментами в БД
     * @param systemLogRepository репозиторий для логирования операций
     * @param restClient REST клиент для получения индикативных инструментов из Tinkoff API
     * @param tinkoffApiClient API клиент для получения полной информации о фьючерсах
     */
    public InstrumentService(InstrumentsServiceBlockingStub instrumentsService,
                           ShareRepository shareRepo,
                           FutureRepository futureRepo,
                           IndicativeRepository indicativeRepo,
                           SystemLogRepository systemLogRepository,
                           TinkoffRestClient restClient,
                           TinkoffApiClient tinkoffApiClient) {
        this.instrumentsService = instrumentsService;
        this.shareRepo = shareRepo;
        this.futureRepo = futureRepo;
        this.indicativeRepo = indicativeRepo;
        this.systemLogRepository = systemLogRepository;
        this.restClient = restClient;
        this.tinkoffApiClient = tinkoffApiClient;
    }

    // === МЕТОДЫ ДЛЯ РАБОТЫ С АКЦИЯМИ ===

    /**
     * Получение списка акций из Tinkoff API с фильтрацией через REST
     * 
     * <p>Метод получает акции из внешнего API Tinkoff через REST с применением фильтров.
     * Результат кэшируется для повышения производительности.</p>
     * 
     * <p>Преимущества REST API: включает поле assetUid в ответе, что позволяет
     * получить полную информацию об акциях за один запрос.</p>
     * 
     * @param status статус инструмента (INSTRUMENT_STATUS_ACTIVE, INSTRUMENT_STATUS_BASE)
     * @param exchange биржа (например: MOEX, SPB)
     * @param currency валюта (например: RUB, USD, EUR)
     * @param ticker тикер акции (например: SBER, GAZP)
     * @param figi уникальный идентификатор инструмента
     * @return список акций, отсортированный по тикеру (включая assetUid)
     */
    @Cacheable(cacheNames = com.example.InvestmentDataLoaderService.config.CacheConfig.SHARES_CACHE,
            key = "T(java.util.Objects).toString(#status,'') + '|' + T(java.util.Objects).toString(#exchange,'') + '|' + T(java.util.Objects).toString(#currency,'') + '|' + T(java.util.Objects).toString(#ticker,'') + '|' + T(java.util.Objects).toString(#figi,'')")
    public List<ShareDto> getShares(String status, String exchange, String currency, String ticker, String figi) {
        // Получаем данные из T-Bank API через REST
        JsonNode response = restClient.getShares(status);
        
        List<ShareDto> shares = new ArrayList<>();
        
        // Проверяем структуру ответа
        JsonNode instrumentsList = response.get("instruments");
        if (instrumentsList == null) {
            // Если instrumentsList отсутствует, возможно массив находится в корне
            if (response.isArray()) {
                instrumentsList = response;
            } else {
                log.warn("Неожиданная структура ответа API: {}", response);
                return shares;
            }
        }
        
        for (JsonNode instrument : instrumentsList) {
            // Применяем фильтры
            boolean matchesExchange = (exchange == null || exchange.isEmpty() || 
                                     instrument.get("exchange").asText().equalsIgnoreCase(exchange));
            boolean matchesCurrency = (currency == null || currency.isEmpty() || 
                                     instrument.get("currency").asText().equalsIgnoreCase(currency));
            boolean matchesTicker = (ticker == null || ticker.isEmpty() || 
                                   instrument.get("ticker").asText().equalsIgnoreCase(ticker));
            boolean matchesFigi = (figi == null || figi.isEmpty() || 
                                 instrument.get("figi").asText().equalsIgnoreCase(figi));
            if (matchesExchange && matchesCurrency && matchesTicker && matchesFigi) {
                shares.add(new ShareDto(
                    instrument.get("figi").asText(),
                    instrument.get("ticker").asText(),
                    instrument.get("name").asText(),
                    instrument.get("currency").asText(),
                    instrument.get("exchange").asText(),
                    instrument.get("sector").asText(),
                    instrument.get("tradingStatus").asText(),
                    instrument.get("shortEnabledFlag").asBoolean(),
                    instrument.get("assetUid").asText(),
                    QuotationDto.minPriceIncrementToBigDecimal(instrument.get("minPriceIncrement")),
                    instrument.has("lot") ? instrument.get("lot").asInt() : 1
                ));
            }
        }
        
        // Сортируем по тикеру
        shares.sort(Comparator.comparing(ShareDto::ticker, String.CASE_INSENSITIVE_ORDER));
        return shares;
    }



    /**
     * Получение акций из базы данных с фильтрацией
     * 
     * <p>Метод получает акции из локальной БД с применением расширенных фильтров.
     * Поддерживает фильтрацию по всем полям сущности акции.</p>
     * 
     * @param filter фильтр для поиска акций в БД
     * @return список акций из БД, отсортированный по тикеру
     */
    public List<ShareDto> getSharesFromDatabase(ShareFilterDto filter) {
        List<ShareEntity> entities = shareRepo.findAll();
        List<ShareDto> result = new ArrayList<>();
        log.info("entities: {}", entities);
        for (ShareEntity entity : entities) {
            // Применяем фильтры
            boolean matchesExchange = (filter.getExchange() == null || filter.getExchange().isEmpty() || 
                                     entity.getExchange() != null && entity.getExchange().equalsIgnoreCase(filter.getExchange()));
            boolean matchesCurrency = (filter.getCurrency() == null || filter.getCurrency().isEmpty() || 
                                     entity.getCurrency() != null && entity.getCurrency().equalsIgnoreCase(filter.getCurrency()));
            boolean matchesTicker = (filter.getTicker() == null || filter.getTicker().isEmpty() || 
                                   entity.getTicker() != null && entity.getTicker().equalsIgnoreCase(filter.getTicker()));
            boolean matchesFigi = (filter.getFigi() == null || filter.getFigi().isEmpty() || 
                                 entity.getFigi() != null && entity.getFigi().equalsIgnoreCase(filter.getFigi()));
            boolean matchesSector = (filter.getSector() == null || filter.getSector().isEmpty() || 
                                   entity.getSector() != null && entity.getSector().equalsIgnoreCase(filter.getSector()));
            boolean matchesTradingStatus = (filter.getTradingStatus() == null || filter.getTradingStatus().isEmpty() || 
                                          entity.getTradingStatus() != null && entity.getTradingStatus().equalsIgnoreCase(filter.getTradingStatus()));
            
            if (matchesExchange && matchesCurrency && matchesTicker && matchesFigi && matchesSector && matchesTradingStatus) {
                result.add(new ShareDto(
                    entity.getFigi(),
                    entity.getTicker(),
                    entity.getName(),
                    entity.getCurrency(),
                    entity.getExchange(),
                    entity.getSector(),
                    entity.getTradingStatus(),
                    entity.getShortEnabled(),
                    entity.getAssetUid(),
                    entity.getMinPriceIncrement(),
                    entity.getLot()
                ));
            }
        }
        
        // Сортируем по тикеру
        result.sort(Comparator.comparing(ShareDto::ticker, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    /**
     * Получение акции по FIGI из базы данных
     * 
     * @param figi уникальный идентификатор инструмента
     * @return акция, если найдена, иначе null
     */
    public ShareDto getShareByFigi(String figi) {
        log.info("getShareByFigi: figi: {}", figi);
        return shareRepo.findById(figi)
                .map(entity -> new ShareDto(
                    entity.getFigi(),
                    entity.getTicker(),
                    entity.getName(),
                    entity.getCurrency(),
                    entity.getExchange(),
                    entity.getSector(),
                    entity.getTradingStatus(),
                    entity.getShortEnabled(),
                    entity.getAssetUid(),
                    entity.getMinPriceIncrement(),
                    entity.getLot()
                ))
                .orElse(null);
    }

    /**
     * Получение акции по тикеру из базы данных
     * 
     * @param ticker тикер акции
     * @return акция, если найдена, иначе null
     */
    public ShareDto getShareByTicker(String ticker) {
        return shareRepo.findByTickerIgnoreCase(ticker)
                .map(entity -> new ShareDto(
                    entity.getFigi(),
                    entity.getTicker(),
                    entity.getName(),
                    entity.getCurrency(),
                    entity.getExchange(),
                    entity.getSector(),
                    entity.getTradingStatus(),
                    entity.getShortEnabled(),
                    entity.getAssetUid(),
                    entity.getMinPriceIncrement(),
                    entity.getLot()
                ))
                .orElse(null);
    }


    // === МЕТОДЫ ДЛЯ РАБОТЫ С ФЬЮЧЕРСАМИ ===

    @Cacheable(cacheNames = com.example.InvestmentDataLoaderService.config.CacheConfig.FUTURES_CACHE,
            key = "T(java.util.Objects).toString(#status,'') + '|' + T(java.util.Objects).toString(#exchange,'') + '|' + T(java.util.Objects).toString(#currency,'') + '|' + T(java.util.Objects).toString(#ticker,'') + '|' + T(java.util.Objects).toString(#assetType,'')")
    public List<FutureDto> getFutures(String status, String exchange, String currency, String ticker, String assetType) {
        // Определяем статус инструмента для запроса к API
        InstrumentStatus instrumentStatus = InstrumentStatus.INSTRUMENT_STATUS_BASE;
        if (status != null && !status.isEmpty()) {
            try {
                instrumentStatus = InstrumentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Если статус некорректный, используем значение по умолчанию
                instrumentStatus = InstrumentStatus.INSTRUMENT_STATUS_BASE;
            }
        }

        // Запрашиваем фьючерсы из T-Bank API
        FuturesResponse response = instrumentsService.futures(InstrumentsRequest.newBuilder()
                .setInstrumentStatus(instrumentStatus)
                .build());
        
        // Задержка для соблюдения лимитов API
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        List<FutureDto> futures = new ArrayList<>();
        for (var instrument : response.getInstrumentsList()) {
            // Применяем фильтры
            boolean matchesExchange = (exchange == null || exchange.isEmpty() || 
                                     instrument.getExchange().equalsIgnoreCase(exchange));
            boolean matchesCurrency = (currency == null || currency.isEmpty() || 
                                     instrument.getCurrency().equalsIgnoreCase(currency));
            boolean matchesTicker = (ticker == null || ticker.isEmpty() || 
                                   instrument.getTicker().equalsIgnoreCase(ticker));
            boolean matchesAssetType = (assetType == null || assetType.isEmpty() || 
                                      instrument.getAssetType().equalsIgnoreCase(assetType));
            
            if (matchesExchange && matchesCurrency && matchesTicker && matchesAssetType) {
                // Получаем дату экспирации из того же объекта instrument
                LocalDateTime expirationDate = null;
                
                if (instrument.hasExpirationDate()) {
                    expirationDate = tinkoffApiClient.convertTimestampToLocalDateTime(instrument.getExpirationDate());
                }

                
                futures.add(new FutureDto(
                    instrument.getFigi(),
                    instrument.getTicker(),
                    instrument.getAssetType(),
                    instrument.getBasicAsset(),
                    instrument.getCurrency(),
                    instrument.getExchange(),
                    true,
                    expirationDate,
                    convertQuotationToBigDecimal(instrument.getMinPriceIncrement()),
                    instrument.getLot(),
                    convertQuotationToBigDecimal(instrument.getBasicAssetSize())
                ));
            }
        }
        
        // Сортируем по тикеру
        futures.sort(Comparator.comparing(FutureDto::ticker, String.CASE_INSENSITIVE_ORDER));
        
       
       
        
        return futures;
    }


    /**
     * Получение фьючерса по FIGI из базы данных
     */
    public FutureDto getFutureByFigi(String figi) {
        return futureRepo.findById(figi)
        .map(entity -> new FutureDto(
            entity.getFigi(),
            entity.getTicker(),
            entity.getAssetType(),
            entity.getBasicAsset(),
            entity.getCurrency(),
            entity.getExchange(),
            entity.getShortEnabled(), 
            entity.getExpirationDate(),
            entity.getMinPriceIncrement(),
            entity.getLot(),
            entity.getBasicAssetSize()
            ))
        .orElse(null);
        
               
    }

    /**
     * Получение фьючерса по тикеру из базы данных
     */
    public FutureDto getFutureByTicker(String ticker) {
        return futureRepo.findByTickerIgnoreCase(ticker)
        .map(entity -> new FutureDto(
            entity.getFigi(),
            entity.getTicker(),
            entity.getAssetType(),
            entity.getBasicAsset(),
            entity.getCurrency(),
            entity.getExchange(),
            entity.getShortEnabled(), 
            entity.getExpirationDate(),
            entity.getMinPriceIncrement(),
            entity.getLot(),
            entity.getBasicAssetSize()
            ))
        .orElse(null);
    }

    // === МЕТОДЫ ДЛЯ РАБОТЫ С ИНДИКАТИВНЫМИ ИНСТРУМЕНТАМИ ===

    @Cacheable(cacheNames = com.example.InvestmentDataLoaderService.config.CacheConfig.INDICATIVES_CACHE,
            key = "T(java.util.Objects).toString(#exchange,'') + '|' + T(java.util.Objects).toString(#currency,'') + '|' + T(java.util.Objects).toString(#ticker,'') + '|' + T(java.util.Objects).toString(#figi,'')")
    public List<IndicativeDto> getIndicatives(String exchange, String currency, String ticker, String figi) {
        try {
            // Используем REST API для получения индикативных инструментов
            JsonNode response = restClient.getIndicatives();
            
            List<IndicativeDto> indicatives = new ArrayList<>();
            
            // Парсим JSON ответ
            if (response.has("instruments")) {
                JsonNode instruments = response.get("instruments");
                if (instruments != null && instruments.isArray()) {
                    for (JsonNode instrument : instruments) {
                        // Применяем фильтры
                        boolean matchesExchange = (exchange == null || exchange.isEmpty() || 
                                                 instrument.get("exchange").asText().equalsIgnoreCase(exchange));
                        boolean matchesCurrency = (currency == null || currency.isEmpty() || 
                                                 instrument.get("currency").asText().equalsIgnoreCase(currency));
                        boolean matchesTicker = (ticker == null || ticker.isEmpty() || 
                                               instrument.get("ticker").asText().equalsIgnoreCase(ticker));
                        boolean matchesFigi = (figi == null || figi.isEmpty() || 
                                             instrument.get("figi").asText().equalsIgnoreCase(figi));
                        
                        if (matchesExchange && matchesCurrency && matchesTicker && matchesFigi) {
                            String figiValue = instrument.get("figi").asText();
                            // Пропускаем индикативы с пустым или null figi
                            if (figiValue != null && !figiValue.trim().isEmpty()) {
                                indicatives.add(new IndicativeDto(
                                    figiValue,
                                    instrument.get("ticker").asText(),
                                    instrument.get("name").asText(),
                                    instrument.get("currency").asText(),
                                    instrument.get("exchange").asText(),
                                    instrument.has("classCode") ? instrument.get("classCode").asText() : null,
                                    instrument.has("uid") ? instrument.get("uid").asText() : null,
                                    instrument.has("sellAvailableFlag") ? instrument.get("sellAvailableFlag").asBoolean() : null,
                                    instrument.has("buyAvailableFlag") ? instrument.get("buyAvailableFlag").asBoolean() : null
                                ));
                            }
                        }
                    }
                }
            } else if (response.has("instrumentsList")) {
                // Альтернативная структура ответа
                JsonNode instruments = response.get("instrumentsList");
                if (instruments != null && instruments.isArray()) {
                    for (JsonNode instrument : instruments) {
                        // Применяем фильтры
                        boolean matchesExchange = (exchange == null || exchange.isEmpty() || 
                                                 instrument.get("exchange").asText().equalsIgnoreCase(exchange));
                        boolean matchesCurrency = (currency == null || currency.isEmpty() || 
                                                 instrument.get("currency").asText().equalsIgnoreCase(currency));
                        boolean matchesTicker = (ticker == null || ticker.isEmpty() || 
                                               instrument.get("ticker").asText().equalsIgnoreCase(ticker));
                        boolean matchesFigi = (figi == null || figi.isEmpty() || 
                                             instrument.get("figi").asText().equalsIgnoreCase(figi));
                        
                        if (matchesExchange && matchesCurrency && matchesTicker && matchesFigi) {
                            String figiValue = instrument.get("figi").asText();
                            // Пропускаем индикативы с пустым или null figi
                            if (figiValue != null && !figiValue.trim().isEmpty()) {
                                indicatives.add(new IndicativeDto(
                                    figiValue,
                                    instrument.get("ticker").asText(),
                                    instrument.get("name").asText(),
                                    instrument.get("currency").asText(),
                                    instrument.get("exchange").asText(),
                                    instrument.has("classCode") ? instrument.get("classCode").asText() : null,
                                    instrument.has("uid") ? instrument.get("uid").asText() : null,
                                    instrument.has("sellAvailableFlag") ? instrument.get("sellAvailableFlag").asBoolean() : null,
                                    instrument.has("buyAvailableFlag") ? instrument.get("buyAvailableFlag").asBoolean() : null
                                ));
                            }
                        }
                    }
                }
            }
            
            // Если ни один из вариантов не сработал, проверяем, не является ли ответ массивом напрямую
            if (response.isArray()) {
                for (JsonNode instrument : response) {
                    // Применяем фильтры
                    boolean matchesExchange = (exchange == null || exchange.isEmpty() || 
                                             instrument.get("exchange").asText().equalsIgnoreCase(exchange));
                    boolean matchesCurrency = (currency == null || currency.isEmpty() || 
                                             instrument.get("currency").asText().equalsIgnoreCase(currency));
                    boolean matchesTicker = (ticker == null || ticker.isEmpty() || 
                                           instrument.get("ticker").asText().equalsIgnoreCase(ticker));
                    boolean matchesFigi = (figi == null || figi.isEmpty() || 
                                         instrument.get("figi").asText().equalsIgnoreCase(figi));
                    
                    if (matchesExchange && matchesCurrency && matchesTicker && matchesFigi) {
                        String figiValue = instrument.get("figi").asText();
                        // Пропускаем индикативы с пустым или null figi
                        if (figiValue != null && !figiValue.trim().isEmpty()) {
                            indicatives.add(new IndicativeDto(
                                figiValue,
                                instrument.get("ticker").asText(),
                                instrument.get("name").asText(),
                                instrument.get("currency").asText(),
                                instrument.get("exchange").asText(),
                                instrument.has("classCode") ? instrument.get("classCode").asText() : null,
                                instrument.has("uid") ? instrument.get("uid").asText() : null,
                                instrument.has("sellAvailableFlag") ? instrument.get("sellAvailableFlag").asBoolean() : null,
                                instrument.has("buyAvailableFlag") ? instrument.get("buyAvailableFlag").asBoolean() : null
                            ));
                        }
                    }
                }
            }
            
            // Сортируем по тикеру
            indicatives.sort(Comparator.comparing(IndicativeDto::ticker, String.CASE_INSENSITIVE_ORDER));
            return indicatives;
            
        } catch (Exception e) {
            // Если REST API не доступен, используем данные из БД
            log.warn("REST API method indicatives not available, using database: {}", e.getMessage());
            
            List<IndicativeDto> indicatives = new ArrayList<>();
            
            // Получаем все индикативные инструменты из БД
            List<IndicativeEntity> entities = indicativeRepo.findAll();
            
            for (IndicativeEntity entity : entities) {
                // Применяем фильтры
                boolean matchesExchange = (exchange == null || exchange.isEmpty() || 
                                         entity.getExchange().equalsIgnoreCase(exchange));
                boolean matchesCurrency = (currency == null || currency.isEmpty() || 
                                         entity.getCurrency().equalsIgnoreCase(currency));
                boolean matchesTicker = (ticker == null || ticker.isEmpty() || 
                                       entity.getTicker().equalsIgnoreCase(ticker));
                boolean matchesFigi = (figi == null || figi.isEmpty() || 
                                     entity.getFigi().equalsIgnoreCase(figi));
                
                if (matchesExchange && matchesCurrency && matchesTicker && matchesFigi) {
                    indicatives.add(new IndicativeDto(
                        entity.getFigi(),
                        entity.getTicker(),
                        entity.getName(),
                        entity.getCurrency(),
                        entity.getExchange(),
                        entity.getClassCode(),
                        entity.getUid(),
                        entity.getSellAvailableFlag(),
                        entity.getBuyAvailableFlag()
                    ));
                }
            }
            
            // Сортируем по тикеру
            indicatives.sort(Comparator.comparing(IndicativeDto::ticker, String.CASE_INSENSITIVE_ORDER));
            
            
            
            return indicatives;
        }
    }

    public IndicativeDto getIndicativeBy(String figi) {
        try {
            // Используем REST API для получения индикативного инструмента по FIGI
            JsonNode response = restClient.getIndicativeBy(figi);
            
            if (response.has("instrument")) {
                JsonNode instrument = response.get("instrument");
                
                return new IndicativeDto(
                    instrument.get("figi").asText(),
                    instrument.get("ticker").asText(),
                    instrument.get("name").asText(),
                    instrument.get("currency").asText(),
                    instrument.get("exchange").asText(),
                    instrument.has("classCode") ? instrument.get("classCode").asText() : null,
                    instrument.has("uid") ? instrument.get("uid").asText() : null,
                    instrument.has("sellAvailableFlag") ? instrument.get("sellAvailableFlag").asBoolean() : null,
                    instrument.has("buyAvailableFlag") ? instrument.get("buyAvailableFlag").asBoolean() : null
                );
            }
            
            return null;
            
        } catch (Exception e) {
            // Если REST API не доступен, ищем в БД
            log.warn("REST API method getIndicativeBy not available, using database: {}", e.getMessage());
            
            return indicativeRepo.findById(figi)
                .map(entity -> new IndicativeDto(
                    entity.getFigi(),
                    entity.getTicker(),
                    entity.getName(),
                    entity.getCurrency(),
                    entity.getExchange(),
                    entity.getClassCode(),
                    entity.getUid(),
                    entity.getSellAvailableFlag(),
                    entity.getBuyAvailableFlag()
                ))
                .orElse(null);
        }
    }

    public IndicativeDto getIndicativeByTicker(String ticker) {
        try {
            // Получаем все индикативные инструменты и ищем по тикеру
            List<IndicativeDto> indicatives = getIndicatives(null, null, ticker, null);
            
            // Возвращаем первый найденный инструмент с таким тикером
            return indicatives.stream()
                .filter(indicative -> indicative.ticker().equalsIgnoreCase(ticker))
                .findFirst()
                .orElse(null);
                
        } catch (com.example.InvestmentDataLoaderService.exception.InstrumentsNotFoundException e) {
            // Если инструменты не найдены, возвращаем null
            return null;
        } catch (Exception e) {
            log.error("Error getting indicative by ticker: {}", e.getMessage(), e);
            return null;
        }
    }


    /**
     * Получение количества инструментов по типам
     * 
     * <p>Подсчитывает количество инструментов в локальной БД по типам:
     * акции, фьючерсы, индикативные инструменты и общее количество.</p>
     * 
     * @return карта с количеством инструментов по типам
     */
    public Map<String, Long> getInstrumentCounts() {
        Map<String, Long> counts = new HashMap<>();
        
        // Подсчитываем инструменты в базе данных
        long sharesCount = shareRepo.count();
        long futuresCount = futureRepo.count();
        long indicativesCount = indicativeRepo.count();
        
        counts.put("shares", sharesCount);
        counts.put("futures", futuresCount);
        counts.put("indicatives", indicativesCount);
        counts.put("total", sharesCount + futuresCount + indicativesCount);
        
        return counts;
    }


    public List<FutureDto> getFuturesFromDatabase() {
        List<FutureEntity> entities = futureRepo.findAll();
        return entities.stream()
                .map(entity -> new FutureDto(
                    entity.getFigi(),
                    entity.getTicker(),
                    entity.getAssetType(),
                    entity.getBasicAsset(),
                    entity.getCurrency(),
                    entity.getExchange(),
                    entity.getShortEnabled(),
                    entity.getExpirationDate(),
                    entity.getMinPriceIncrement(),
                    entity.getLot(),
                    entity.getBasicAssetSize()
                ))
                .collect(Collectors.toList());
    }

/**
 * Получение всех индикативов из базы данных
 */
    public List<IndicativeDto> getIndicativesFromDatabase() {
        List<IndicativeEntity> entities = indicativeRepo.findAll();
        return entities.stream()
                .map(entity -> new IndicativeDto(
                    entity.getFigi(),
                    entity.getTicker(),
                    entity.getName(),
                    entity.getCurrency(),
                    entity.getExchange(),
                    entity.getClassCode(),
                    entity.getUid(),
                    entity.getSellAvailableFlag(),
                    entity.getBuyAvailableFlag()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Преобразует gRPC Quotation в BigDecimal
     */
    private BigDecimal convertQuotationToBigDecimal(ru.tinkoff.piapi.contract.v1.Quotation quotation) {
        if (quotation == null) {
            return BigDecimal.ZERO;
        }
        
        QuotationDto quotationDto = new QuotationDto(quotation.getUnits(), quotation.getNano());
        return quotationDto.toBigDecimal();
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ КЛАССЫ ====================
    
    /**
     * Результат обработки акции
     */
    private static class ShareProcessingResult {
        private final ShareDto shareDto;
        private final boolean saved;
        private final boolean existing;
        private final boolean hasError;
        private final String errorMessage;
        
        public ShareProcessingResult(ShareDto shareDto, boolean saved, boolean existing, boolean hasError, String errorMessage) {
            this.shareDto = shareDto;
            this.saved = saved;
            this.existing = existing;
            this.hasError = hasError;
            this.errorMessage = errorMessage;
        }
        
        public ShareDto getShareDto() { return shareDto; }
        public boolean isSaved() { return saved; }
        public boolean isExisting() { return existing; }
        public boolean hasError() { return hasError; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * Результат обработки фьючерса
     */
    private static class FutureProcessingResult {
        private final FutureDto futureDto;
        private final boolean saved;
        private final boolean existing;
        private final boolean hasError;
        private final String errorMessage;
        
        public FutureProcessingResult(FutureDto futureDto, boolean saved, boolean existing, boolean hasError, String errorMessage) {
            this.futureDto = futureDto;
            this.saved = saved;
            this.existing = existing;
            this.hasError = hasError;
            this.errorMessage = errorMessage;
        }
        
        public FutureDto getFutureDto() { return futureDto; }
        public boolean isSaved() { return saved; }
        public boolean isExisting() { return existing; }
        public boolean hasError() { return hasError; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * Результат обработки индикатива
     */
    private static class IndicativeProcessingResult {
        private final IndicativeDto indicativeDto;
        private final boolean saved;
        private final boolean existing;
        private final boolean hasError;
        private final String errorMessage;
        
        public IndicativeProcessingResult(IndicativeDto indicativeDto, boolean saved, boolean existing, boolean hasError, String errorMessage) {
            this.indicativeDto = indicativeDto;
            this.saved = saved;
            this.existing = existing;
            this.hasError = hasError;
            this.errorMessage = errorMessage;
        }
        
        public IndicativeDto getIndicativeDto() { return indicativeDto; }
        public boolean isSaved() { return saved; }
        public boolean isExisting() { return existing; }
        public boolean hasError() { return hasError; }
        public String getErrorMessage() { return errorMessage; }
    }

    // ==================== АСИНХРОННЫЕ МЕТОДЫ ====================

    /**
     * Асинхронная обработка одной акции
     */
    private ShareProcessingResult processShareAsync(ShareDto shareDto) {
        try {
            // Проверяем, существует ли акция в БД
            if (!shareRepo.existsById(shareDto.figi())) {
                // Создаем и сохраняем новую акцию
                ShareEntity shareEntity = new ShareEntity(
                    shareDto.figi(),
                    shareDto.ticker(),
                    shareDto.name(),
                    shareDto.currency(),
                    shareDto.exchange(),
                    shareDto.sector(),
                    shareDto.tradingStatus(),
                    shareDto.shortEnabled(),
                    shareDto.assetUid(),
                    shareDto.minPriceIncrement(),
                    shareDto.lot(),
                    null, // createdAt будет установлен автоматически
                    null  // updatedAt будет установлен автоматически
                );
                
                shareRepo.save(shareEntity);
                return new ShareProcessingResult(shareDto, true, false, false, null);
            } else {
                // Обновляем shortEnabled у существующей записи, если он изменился/пустой
                shareRepo.findById(shareDto.figi()).ifPresent(entity -> {
                    Boolean newValue = shareDto.shortEnabled();
                    Boolean oldValue = entity.getShortEnabled();
                    if ((newValue != null && !newValue.equals(oldValue)) || (oldValue == null && newValue != null)) {
                        entity.setShortEnabled(newValue);
                        shareRepo.save(entity);
                    }
                });
                return new ShareProcessingResult(shareDto, false, true, false, null);
            }
        } catch (Exception e) {
            log.error("Error processing share {}: {}", shareDto.figi(), e.getMessage(), e);
            return new ShareProcessingResult(shareDto, false, false, true, e.getMessage());
        }
    }

    /**
     * Асинхронная обработка одного фьючерса
     */
    private FutureProcessingResult processFutureAsync(FutureDto futureDto) {
        try {
            // Проверяем, существует ли фьючерс в БД
            if (!futureRepo.existsById(futureDto.figi())) {
                // Создаем и сохраняем новый фьючерс
                FutureEntity futureEntity = new FutureEntity(
                    futureDto.figi(),
                    futureDto.ticker(),
                    futureDto.assetType(),
                    futureDto.basicAsset(),
                    futureDto.currency(),
                    futureDto.exchange(),
                    Boolean.TRUE,
                    futureDto.expirationDate(),
                    futureDto.minPriceIncrement(),
                    futureDto.lot(),
                    futureDto.basicAssetSize()
                );
                
                futureRepo.save(futureEntity);
                return new FutureProcessingResult(futureDto, true, false, false, null);
            } else {
                // Обновляем shortEnabled и expirationDate у существующей записи
                futureRepo.findById(futureDto.figi()).ifPresent(entity -> {
                    boolean needsUpdate = false;
                    
                    // Обновляем shortEnabled (для фьючерсов true, если null)
                    Boolean newShortEnabled = Boolean.TRUE;
                    Boolean oldShortEnabled = entity.getShortEnabled();
                    if (oldShortEnabled == null || !newShortEnabled.equals(oldShortEnabled)) {
                        entity.setShortEnabled(newShortEnabled);
                        needsUpdate = true;
                    }
                    
                    // Обновляем expirationDate
                    LocalDateTime newExpirationDate = futureDto.expirationDate();
                    LocalDateTime oldExpirationDate = entity.getExpirationDate();
                    if (newExpirationDate != null && !newExpirationDate.equals(oldExpirationDate)) {
                        entity.setExpirationDate(newExpirationDate);
                        needsUpdate = true;
                    }
                    
                    if (needsUpdate) {
                        futureRepo.save(entity);
                    }
                });
                return new FutureProcessingResult(futureDto, false, true, false, null);
            }
        } catch (Exception e) {
            log.error("Error processing future {}: {}", futureDto.figi(), e.getMessage(), e);
            return new FutureProcessingResult(futureDto, false, false, true, e.getMessage());
        }
    }

    /**
     * Асинхронная обработка одного индикатива
     */
    private IndicativeProcessingResult processIndicativeAsync(IndicativeDto indicativeDto) {
        try {
            // Пропускаем индикативы с пустым или null figi
            if (indicativeDto.figi() == null || indicativeDto.figi().trim().isEmpty()) {
                return new IndicativeProcessingResult(indicativeDto, false, false, true, "Empty FIGI");
            }
            
            // Проверяем, существует ли индикативный инструмент в БД
            if (!indicativeRepo.existsById(indicativeDto.figi())) {
                // Создаем и сохраняем новый индикативный инструмент
                IndicativeEntity indicativeEntity = new IndicativeEntity(
                    indicativeDto.figi(),
                    indicativeDto.ticker(),
                    indicativeDto.name(),
                    indicativeDto.currency(),
                    indicativeDto.exchange(),
                    indicativeDto.classCode(),
                    indicativeDto.uid(),
                    indicativeDto.sellAvailableFlag(),
                    indicativeDto.buyAvailableFlag()
                );
                
                indicativeRepo.save(indicativeEntity);
                return new IndicativeProcessingResult(indicativeDto, true, false, false, null);
            } else {
                return new IndicativeProcessingResult(indicativeDto, false, true, false, null);
            }
        } catch (Exception e) {
            log.error("Error processing indicative {}: {}", indicativeDto.figi(), e.getMessage(), e);
            return new IndicativeProcessingResult(indicativeDto, false, false, true, e.getMessage());
        }
    }

    /**
     * Асинхронное сохранение акций в базу данных
     * 
     * <p>Выполняет сохранение акций в асинхронном режиме с логированием процесса.
     * Возвращает CompletableFuture для отслеживания выполнения операции.</p>
     * 
     * @param filter фильтр для получения акций из API
     * @param taskId уникальный идентификатор задачи для логирования
     * @return CompletableFuture с результатом операции сохранения
     */
    public CompletableFuture<SaveResponseDto> saveSharesAsync(ShareFilterDto filter, String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("=== АСИНХРОННОЕ СОХРАНЕНИЕ АКЦИЙ ===");
                log.info("Task ID: {}", taskId);
                log.info("Фильтр: {}", filter);
                
                // Получаем акции из API (блокирующий запрос остается)
                List<ShareDto> sharesFromApi = getShares(filter.getStatus(), filter.getExchange(), filter.getCurrency(), filter.getTicker(), filter.getFigi());
                
                log.info("Получено {} акций из API, начинаем параллельную обработку...", sharesFromApi.size());
                
                // Параллельная обработка акций
                List<CompletableFuture<ShareProcessingResult>> futures = sharesFromApi.parallelStream()
                    .map(shareDto -> CompletableFuture.supplyAsync(() -> processShareAsync(shareDto)))
                    .collect(Collectors.toList());
                
                // Ждем завершения всех операций
                CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
                );
                
                // Получаем результаты
                List<ShareProcessingResult> results = allFutures.thenApply(v -> 
                    futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                ).join();
                
                // Подсчитываем статистику
                List<ShareDto> savedShares = results.stream()
                    .filter(result -> result.isSaved())
                    .map(ShareProcessingResult::getShareDto)
                    .collect(Collectors.toList());
                
                int existingCount = (int) results.stream()
                    .filter(result -> result.isExisting())
                    .count();
                
                int errorCount = (int) results.stream()
                    .filter(result -> result.hasError())
                    .count();
                
                // Формируем ответ
                boolean success = !sharesFromApi.isEmpty();
                String message;
                
                if (savedShares.isEmpty()) {
                    if (sharesFromApi.isEmpty()) {
                        message = "Новых акций не обнаружено. По заданным фильтрам акции не найдены.";
                    } else {
                        message = "Новых акций не обнаружено. Все найденные акции уже существуют в базе данных.";
                    }
                } else {
                    message = String.format("Успешно загружено %d новых акций из %d найденных. Ошибок: %d", 
                        savedShares.size(), sharesFromApi.size(), errorCount);
                }
                
                SaveResponseDto result = new SaveResponseDto(
                    success,
                    message,
                    sharesFromApi.size(),
                    savedShares.size(),
                    existingCount,
                    errorCount, // invalidItemsFiltered
                    0, // missingFromApi
                    savedShares
                );
                
                log.info("Асинхронное сохранение акций завершено для taskId: {}", taskId);
                log.info("Результат: {}", result.getMessage());
                
                // Логируем успешное завершение в БД
                try {
                    SystemLogEntity successLog = new SystemLogEntity();
                    successLog.setTaskId(taskId);
                    successLog.setEndpoint("/api/instruments/shares");
                    successLog.setMethod("POST");
                    successLog.setStatus("COMPLETED");
                    successLog.setMessage(result.getMessage());
                    successLog.setStartTime(Instant.now().minusMillis(1000)); // Примерное время начала
                    successLog.setEndTime(Instant.now());
                    systemLogRepository.save(successLog);
                } catch (Exception logException) {
                    log.error("Ошибка сохранения лога успешного завершения: {}", logException.getMessage(), logException);
                }
                
                return result;
            } catch (Exception e) {
                log.error("Ошибка асинхронного сохранения акций для taskId {}: {}", taskId, e.getMessage(), e);
                
                // Логируем ошибку в БД
                try {
                    SystemLogEntity errorLog = new SystemLogEntity();
                    errorLog.setTaskId(taskId);
                    errorLog.setEndpoint("/api/instruments/shares");
                    errorLog.setMethod("POST");
                    errorLog.setStatus("FAILED");
                    errorLog.setMessage("Ошибка асинхронного сохранения акций: " + e.getMessage());
                    errorLog.setStartTime(Instant.now().minusMillis(1000)); // Примерное время начала
                    errorLog.setEndTime(Instant.now());
                    systemLogRepository.save(errorLog);
                } catch (Exception logException) {
                    log.error("Ошибка сохранения лога ошибки: {}", logException.getMessage(), logException);
                }
                
                throw new RuntimeException("Ошибка асинхронного сохранения акций: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Асинхронное сохранение фьючерсов в базу данных
     * 
     * <p>Выполняет сохранение фьючерсов в асинхронном режиме с логированием процесса.
     * Возвращает CompletableFuture для отслеживания выполнения операции.</p>
     * 
     * @param filter фильтр для получения фьючерсов из API
     * @param taskId уникальный идентификатор задачи для логирования
     * @return CompletableFuture с результатом операции сохранения
     */
    public CompletableFuture<SaveResponseDto> saveFuturesAsync(FutureFilterDto filter, String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("=== АСИНХРОННОЕ СОХРАНЕНИЕ ФЬЮЧЕРСОВ ===");
                log.info("Task ID: {}", taskId);
                log.info("Фильтр: {}", filter);
                
                // Получаем фьючерсы из API (блокирующий запрос остается)
                List<FutureDto> futuresFromApi = getFutures(filter.getStatus(), filter.getExchange(), filter.getCurrency(), filter.getTicker(), filter.getAssetType());
                
                log.info("Получено {} фьючерсов из API, начинаем параллельную обработку...", futuresFromApi.size());
                
                // Параллельная обработка фьючерсов
                List<CompletableFuture<FutureProcessingResult>> futures = futuresFromApi.parallelStream()
                    .map(futureDto -> CompletableFuture.supplyAsync(() -> processFutureAsync(futureDto)))
                    .collect(Collectors.toList());
                
                // Ждем завершения всех операций
                CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
                );
                
                // Получаем результаты
                List<FutureProcessingResult> results = allFutures.thenApply(v -> 
                    futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                ).join();
                
                // Подсчитываем статистику
                List<FutureDto> savedFutures = results.stream()
                    .filter(result -> result.isSaved())
                    .map(FutureProcessingResult::getFutureDto)
                    .collect(Collectors.toList());
                
                int existingCount = (int) results.stream()
                    .filter(result -> result.isExisting())
                    .count();
                
                int errorCount = (int) results.stream()
                    .filter(result -> result.hasError())
                    .count();
                
                // Формируем ответ
                boolean success = !futuresFromApi.isEmpty();
                String message;
                
                if (savedFutures.isEmpty()) {
                    if (futuresFromApi.isEmpty()) {
                        message = "Новых фьючерсов не обнаружено. По заданным фильтрам фьючерсы не найдены.";
                    } else {
                        message = "Новых фьючерсов не обнаружено. Все найденные фьючерсы уже существуют в базе данных.";
                    }
                } else {
                    message = String.format("Успешно загружено %d новых фьючерсов из %d найденных. Ошибок: %d", 
                        savedFutures.size(), futuresFromApi.size(), errorCount);
                }
                
                SaveResponseDto result = new SaveResponseDto(
                    success,
                    message,
                    futuresFromApi.size(),
                    savedFutures.size(),
                    existingCount,
                    errorCount, // invalidItemsFiltered
                    0, // missingFromApi
                    savedFutures
                );
                
                log.info("Асинхронное сохранение фьючерсов завершено для taskId: {}", taskId);
                log.info("Результат: {}", result.getMessage());
                
                // Логируем успешное завершение в БД
                try {
                    SystemLogEntity successLog = new SystemLogEntity();
                    successLog.setTaskId(taskId);
                    successLog.setEndpoint("/api/instruments/futures");
                    successLog.setMethod("POST");
                    successLog.setStatus("COMPLETED");
                    successLog.setMessage(result.getMessage());
                    successLog.setStartTime(Instant.now().minusMillis(1000)); // Примерное время начала
                    successLog.setEndTime(Instant.now());
                    systemLogRepository.save(successLog);
                } catch (Exception logException) {
                    log.error("Ошибка сохранения лога успешного завершения: {}", logException.getMessage(), logException);
                }
                
                return result;
            } catch (Exception e) {
                log.error("Ошибка асинхронного сохранения фьючерсов для taskId {}: {}", taskId, e.getMessage(), e);
                
                // Логируем ошибку в БД
                try {
                    SystemLogEntity errorLog = new SystemLogEntity();
                    errorLog.setTaskId(taskId);
                    errorLog.setEndpoint("/api/instruments/futures");
                    errorLog.setMethod("POST");
                    errorLog.setStatus("FAILED");
                    errorLog.setMessage("Ошибка асинхронного сохранения фьючерсов: " + e.getMessage());
                    errorLog.setStartTime(Instant.now().minusMillis(1000)); // Примерное время начала
                    errorLog.setEndTime(Instant.now());
                    systemLogRepository.save(errorLog);
                } catch (Exception logException) {
                    log.error("Ошибка сохранения лога ошибки: {}", logException.getMessage(), logException);
                }
                
                throw new RuntimeException("Ошибка асинхронного сохранения фьючерсов: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Асинхронное сохранение индикативов в базу данных
     * 
     * <p>Выполняет сохранение индикативных инструментов в асинхронном режиме с логированием процесса.
     * Возвращает CompletableFuture для отслеживания выполнения операции.</p>
     * 
     * @param filter фильтр для получения индикативов из API
     * @param taskId уникальный идентификатор задачи для логирования
     * @return CompletableFuture с результатом операции сохранения
     */
    public CompletableFuture<SaveResponseDto> saveIndicativesAsync(IndicativeFilterDto filter, String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("=== АСИНХРОННОЕ СОХРАНЕНИЕ ИНДИКАТИВОВ ===");
                log.info("Task ID: {}", taskId);
                log.info("Фильтр: {}", filter);
                
                // Получаем индикативные инструменты из API (блокирующий запрос остается)
                List<IndicativeDto> indicativesFromApi = getIndicatives(
                    filter.getExchange(), 
                    filter.getCurrency(), 
                    filter.getTicker(), 
                    filter.getFigi()
                );
                
                log.info("Получено {} индикативов из API, начинаем параллельную обработку...", indicativesFromApi.size());
                
                // Параллельная обработка индикативов
                List<CompletableFuture<IndicativeProcessingResult>> futures = indicativesFromApi.parallelStream()
                    .map(indicativeDto -> CompletableFuture.supplyAsync(() -> processIndicativeAsync(indicativeDto)))
                    .collect(Collectors.toList());
                
                // Ждем завершения всех операций
                CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
                );
                
                // Получаем результаты
                List<IndicativeProcessingResult> results = allFutures.thenApply(v -> 
                    futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                ).join();
                
                // Подсчитываем статистику
                List<IndicativeDto> savedIndicatives = results.stream()
                    .filter(result -> result.isSaved())
                    .map(IndicativeProcessingResult::getIndicativeDto)
                    .collect(Collectors.toList());
                
                int existingCount = (int) results.stream()
                    .filter(result -> result.isExisting())
                    .count();
                
                int errorCount = (int) results.stream()
                    .filter(result -> result.hasError())
                    .count();
                
                // Формируем ответ
                boolean success = !indicativesFromApi.isEmpty();
                String message;
                
                if (savedIndicatives.isEmpty()) {
                    if (indicativesFromApi.isEmpty()) {
                        message = "Новых индикативных инструментов не обнаружено. По заданным фильтрам инструменты не найдены.";
                    } else {
                        message = "Новых индикативных инструментов не обнаружено. Все найденные инструменты уже существуют в базе данных.";
                    }
                } else {
                    message = String.format("Успешно загружено %d новых индикативных инструментов из %d найденных. Ошибок: %d", 
                        savedIndicatives.size(), indicativesFromApi.size(), errorCount);
                }
                
                SaveResponseDto result = new SaveResponseDto(
                    success,
                    message,
                    indicativesFromApi.size(),
                    savedIndicatives.size(),
                    existingCount,
                    errorCount, // invalidItemsFiltered
                    0, // missingFromApi
                    savedIndicatives
                );
                
                log.info("Асинхронное сохранение индикативов завершено для taskId: {}", taskId);
                log.info("Результат: {}", result.getMessage());
                
                // Логируем успешное завершение в БД
                try {
                    SystemLogEntity successLog = new SystemLogEntity();
                    successLog.setTaskId(taskId);
                    successLog.setEndpoint("/api/instruments/indicatives");
                    successLog.setMethod("POST");
                    successLog.setStatus("COMPLETED");
                    successLog.setMessage(result.getMessage());
                    successLog.setStartTime(Instant.now().minusMillis(1000)); // Примерное время начала
                    successLog.setEndTime(Instant.now());
                    systemLogRepository.save(successLog);
                } catch (Exception logException) {
                    log.error("Ошибка сохранения лога успешного завершения: {}", logException.getMessage(), logException);
                }
                
                return result;
            } catch (Exception e) {
                log.error("Ошибка асинхронного сохранения индикативов для taskId {}: {}", taskId, e.getMessage(), e);
                
                // Логируем ошибку в БД
                try {
                    SystemLogEntity errorLog = new SystemLogEntity();
                    errorLog.setTaskId(taskId);
                    errorLog.setEndpoint("/api/instruments/indicatives");
                    errorLog.setMethod("POST");
                    errorLog.setStatus("FAILED");
                    errorLog.setMessage("Ошибка асинхронного сохранения индикативов: " + e.getMessage());
                    errorLog.setStartTime(Instant.now().minusMillis(1000)); // Примерное время начала
                    errorLog.setEndTime(Instant.now());
                    systemLogRepository.save(errorLog);
                } catch (Exception logException) {
                    log.error("Ошибка сохранения лога ошибки: {}", logException.getMessage(), logException);
                }
                
                throw new RuntimeException("Ошибка асинхронного сохранения индикативов: " + e.getMessage(), e);
            }
        });
    }
}
