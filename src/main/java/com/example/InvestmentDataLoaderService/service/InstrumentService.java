package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.IndicativeEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.IndicativeRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.client.TinkoffRestClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc.InstrumentsServiceBlockingStub;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final InstrumentsServiceBlockingStub instrumentsService;
    private final ShareRepository shareRepo;
    private final FutureRepository futureRepo;
    private final IndicativeRepository indicativeRepo;
    private final TinkoffRestClient restClient;

    /**
     * Конструктор сервиса инструментов
     * 
     * @param instrumentsService gRPC клиент для получения акций и фьючерсов из Tinkoff API
     * @param shareRepo репозиторий для работы с акциями в БД
     * @param futureRepo репозиторий для работы с фьючерсами в БД
     * @param indicativeRepo репозиторий для работы с индикативными инструментами в БД
     * @param restClient REST клиент для получения индикативных инструментов из Tinkoff API
     */
    public InstrumentService(InstrumentsServiceBlockingStub instrumentsService,
                           ShareRepository shareRepo,
                           FutureRepository futureRepo,
                           IndicativeRepository indicativeRepo,
                           TinkoffRestClient restClient) {
        this.instrumentsService = instrumentsService;
        this.shareRepo = shareRepo;
        this.futureRepo = futureRepo;
        this.indicativeRepo = indicativeRepo;
        this.restClient = restClient;
    }

    // === МЕТОДЫ ДЛЯ РАБОТЫ С АКЦИЯМИ ===

    /**
     * Получение списка акций из Tinkoff API с фильтрацией
     * 
     * <p>Метод получает акции из внешнего API Tinkoff с применением фильтров.
     * Результат кэшируется для повышения производительности.</p>
     * 
     * @param status статус инструмента (INSTRUMENT_STATUS_ACTIVE, INSTRUMENT_STATUS_BASE)
     * @param exchange биржа (например: MOEX, SPB)
     * @param currency валюта (например: RUB, USD, EUR)
     * @param ticker тикер акции (например: SBER, GAZP)
     * @param figi уникальный идентификатор инструмента
     * @return список акций, отсортированный по тикеру
     */
    @Cacheable(cacheNames = com.example.InvestmentDataLoaderService.config.CacheConfig.SHARES_CACHE,
            key = "T(java.util.Objects).toString(#status,'') + '|' + T(java.util.Objects).toString(#exchange,'') + '|' + T(java.util.Objects).toString(#currency,'') + '|' + T(java.util.Objects).toString(#ticker,'') + '|' + T(java.util.Objects).toString(#figi,'')")
    public List<ShareDto> getShares(String status, String exchange, String currency, String ticker, String figi) {
        // Определяем статус инструмента для запроса к API
        InstrumentStatus instrumentStatus = InstrumentStatus.INSTRUMENT_STATUS_BASE;
        if (status != null && !status.isEmpty()) {
            try {
                instrumentStatus = InstrumentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Если статус не найден, используем базовый статус
                instrumentStatus = InstrumentStatus.INSTRUMENT_STATUS_BASE;
            }
        }
        
        // Получаем данные из T-Bank API
        SharesResponse response = instrumentsService.shares(InstrumentsRequest.newBuilder()
                .setInstrumentStatus(instrumentStatus)
                .build());
        
        List<ShareDto> shares = new ArrayList<>();
        for (var instrument : response.getInstrumentsList()) {
            // Применяем фильтры
            boolean matchesExchange = (exchange == null || exchange.isEmpty() || 
                                     instrument.getExchange().equalsIgnoreCase(exchange));
            boolean matchesCurrency = (currency == null || currency.isEmpty() || 
                                     instrument.getCurrency().equalsIgnoreCase(currency));
            boolean matchesTicker = (ticker == null || ticker.isEmpty() || 
                                   instrument.getTicker().equalsIgnoreCase(ticker));
            boolean matchesFigi = (figi == null || figi.isEmpty() || 
                                 instrument.getFigi().equalsIgnoreCase(figi));
            
            if (matchesExchange && matchesCurrency && matchesTicker && matchesFigi) {
                shares.add(new ShareDto(
                    instrument.getFigi(),
                    instrument.getTicker(),
                    instrument.getName(),
                    instrument.getCurrency(),
                    instrument.getExchange(),
                    instrument.getSector(),
                    instrument.getTradingStatus().name()
                ));
            }
        }
        
        // Сортируем по тикеру
        shares.sort(Comparator.comparing(ShareDto::ticker, String.CASE_INSENSITIVE_ORDER));
        
        // Если инструменты не найдены, выбрасываем исключение
        if (shares.isEmpty()) {
            throw new com.example.InvestmentDataLoaderService.exception.InstrumentsNotFoundException(
                "Акции не найдены по заданным критериям"
            );
        }
        
        return shares;
    }

    /**
     * Сохранение акций в базу данных с защитой от дубликатов
     * 
     * <p>Метод получает акции из Tinkoff API по заданным фильтрам и сохраняет их в БД.
     * Если акция уже существует в БД, она не будет сохранена повторно.</p>
     * 
     * <p>Возвращает детальную информацию о результате операции:</p>
     * <ul>
     *   <li>Количество найденных акций в API</li>
     *   <li>Количество сохраненных новых акций</li>
     *   <li>Количество уже существующих акций</li>
     * </ul>
     * 
     * @param filter фильтр для получения акций из API
     * @return результат операции сохранения с детальной статистикой
     */
    public SaveResponseDto saveShares(ShareFilterDto filter) {
        // Получаем акции из API (используем существующий метод)
        List<ShareDto> sharesFromApi = getShares(filter.getStatus(), filter.getExchange(), filter.getCurrency(), filter.getTicker(), filter.getFigi());
        
        List<ShareDto> savedShares = new ArrayList<>();
        int existingCount = 0;
        
        for (ShareDto shareDto : sharesFromApi) {
            // Проверяем, существует ли акция в БД
            if (!shareRepo.existsById(shareDto.figi())) {
                // Создаем и сохраняем новую акцию с новыми полями
                ShareEntity shareEntity = new ShareEntity(
                    shareDto.figi(),
                    shareDto.ticker(),
                    shareDto.name(),
                    shareDto.currency(),
                    shareDto.exchange(),
                    shareDto.sector(),
                    shareDto.tradingStatus(),
                    null, // createdAt будет установлен автоматически
                    null  // updatedAt будет установлен автоматически
                );
                
                try {
                    shareRepo.save(shareEntity);
                    savedShares.add(shareDto);
                } catch (Exception e) {
                    // Логируем ошибку, но продолжаем обработку других акций
                    System.err.println("Error saving share " + shareDto.figi() + ": " + e.getMessage());
                }
            } else {
                existingCount++;
            }
        }
        
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
            message = String.format("Успешно загружено %d новых акций из %d найденных.", savedShares.size(), sharesFromApi.size());
        }
        
        return new SaveResponseDto(
            success,
            message,
            sharesFromApi.size(),
            savedShares.size(),
            existingCount,
            0, // invalidItemsFiltered
            0, // missingFromApi
            savedShares
        );
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
                    entity.getTradingStatus()
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
        return shareRepo.findById(figi)
                .map(entity -> new ShareDto(
                    entity.getFigi(),
                    entity.getTicker(),
                    entity.getName(),
                    entity.getCurrency(),
                    entity.getExchange(),
                    entity.getSector(),
                    entity.getTradingStatus()
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
                    entity.getTradingStatus()
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
                futures.add(new FutureDto(
                    instrument.getFigi(),
                    instrument.getTicker(),
                    instrument.getAssetType(),
                    instrument.getBasicAsset(),
                    instrument.getCurrency(),
                    instrument.getExchange()
                ));
            }
        }
        
        // Сортируем по тикеру
        futures.sort(Comparator.comparing(FutureDto::ticker, String.CASE_INSENSITIVE_ORDER));
        
        // Если инструменты не найдены, выбрасываем исключение
        if (futures.isEmpty()) {
            throw new com.example.InvestmentDataLoaderService.exception.InstrumentsNotFoundException(
                "Фьючерсы не найдены по заданным критериям"
            );
        }
        
        return futures;
    }

    public SaveResponseDto saveFutures(FutureFilterDto filter) {
        // Получаем фьючерсы из API (используем существующий метод)
        List<FutureDto> futuresFromApi = getFutures(filter.getStatus(), filter.getExchange(), filter.getCurrency(), filter.getTicker(), filter.getAssetType());
        
        List<FutureDto> savedFutures = new ArrayList<>();
        int existingCount = 0;
        
        for (FutureDto futureDto : futuresFromApi) {
            // Проверяем, существует ли фьючерс в БД
            if (!futureRepo.existsById(futureDto.figi())) {
                // Создаем и сохраняем новый фьючерс
                FutureEntity futureEntity = new FutureEntity(
                    futureDto.figi(),
                    futureDto.ticker(),
                    futureDto.assetType(),
                    futureDto.basicAsset(),
                    futureDto.currency(),
                    futureDto.exchange()
                );
                
                try {
                    futureRepo.save(futureEntity);
                    savedFutures.add(futureDto);
                } catch (Exception e) {
                    // Логируем ошибку, но продолжаем обработку других фьючерсов
                    System.err.println("Error saving future " + futureDto.figi() + ": " + e.getMessage());
                }
            } else {
                existingCount++;
            }
        }
        
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
            message = String.format("Успешно загружено %d новых фьючерсов из %d найденных.", savedFutures.size(), futuresFromApi.size());
        }
        
        return new SaveResponseDto(
            success,
            message,
            futuresFromApi.size(),
            savedFutures.size(),
            existingCount,
            0, // invalidItemsFiltered
            0, // missingFromApi
            savedFutures
        );
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
                    entity.getExchange()
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
                    entity.getExchange()
                ))
                .orElse(null);
    }

    // === МЕТОДЫ ДЛЯ РАБОТЫ С ИНДИКАТИВНЫМИ ИНСТРУМЕНТАМИ ===

    @Cacheable(cacheNames = com.example.InvestmentDataLoaderService.config.CacheConfig.INDICATIVES_CACHE,
            key = "T(java.util.Objects).toString(#exchange,'') + '|' + T(java.util.Objects).toString(#currency,'') + '|' + T(java.util.Objects).toString(#ticker,'') + '|' + T(java.util.Objects).toString(#figi,'')")
    public List<IndicativeDto> getIndicatives(String exchange, String currency, String ticker, String figi) {
        try {
            // Используем REST API для получения индикативных инструментов
            var response = restClient.getIndicatives();
            
            List<IndicativeDto> indicatives = new ArrayList<>();
            
            // Парсим JSON ответ
            if (response.has("instruments")) {
                var instruments = response.get("instruments");
                if (instruments.isArray()) {
                    for (var instrument : instruments) {
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
                var instruments = response.get("instrumentsList");
                if (instruments.isArray()) {
                    for (var instrument : instruments) {
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
            
            // Сортируем по тикеру
            indicatives.sort(Comparator.comparing(IndicativeDto::ticker, String.CASE_INSENSITIVE_ORDER));
            return indicatives;
            
        } catch (Exception e) {
            // Если REST API не доступен, используем данные из БД
            System.err.println("REST API method indicatives not available, using database: " + e.getMessage());
            
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
            
            // Если инструменты не найдены, выбрасываем исключение
            if (indicatives.isEmpty()) {
                throw new com.example.InvestmentDataLoaderService.exception.InstrumentsNotFoundException(
                    "Индикативные инструменты не найдены по заданным критериям"
                );
            }
            
            return indicatives;
        }
    }

    public IndicativeDto getIndicativeBy(String figi) {
        try {
            // Используем REST API для получения индикативного инструмента по FIGI
            var response = restClient.getIndicativeBy(figi);
            
            if (response.has("instrument")) {
                var instrument = response.get("instrument");
                
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
            System.err.println("REST API method getIndicativeBy not available, using database: " + e.getMessage());
            
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
            System.err.println("Error getting indicative by ticker: " + e.getMessage());
            return null;
        }
    }

    public SaveResponseDto saveIndicatives(IndicativeFilterDto filter) {
        // Получаем индикативные инструменты из API
        List<IndicativeDto> indicativesFromApi = getIndicatives(
            filter.getExchange(), 
            filter.getCurrency(), 
            filter.getTicker(), 
            filter.getFigi()
        );
        
        List<IndicativeDto> savedIndicatives = new ArrayList<>();
        int existingCount = 0;
        
        for (IndicativeDto indicativeDto : indicativesFromApi) {
            // Пропускаем индикативы с пустым или null figi
            if (indicativeDto.figi() == null || indicativeDto.figi().trim().isEmpty()) {
                System.out.println("Skipping indicative with empty FIGI: " + indicativeDto.ticker());
                continue;
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
                
                try {
                    indicativeRepo.save(indicativeEntity);
                    savedIndicatives.add(indicativeDto);
                } catch (Exception e) {
                    // Логируем ошибку, но продолжаем обработку других инструментов
                    System.err.println("Error saving indicative " + indicativeDto.figi() + ": " + e.getMessage());
                }
            } else {
                existingCount++;
            }
        }
        
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
            message = String.format("Успешно загружено %d новых индикативных инструментов из %d найденных.", savedIndicatives.size(), indicativesFromApi.size());
        }
        
        return new SaveResponseDto(
            success,
            message,
            indicativesFromApi.size(),
            savedIndicatives.size(),
            existingCount,
            0, // invalidItemsFiltered
            0, // missingFromApi
            savedIndicatives
        );
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
}
