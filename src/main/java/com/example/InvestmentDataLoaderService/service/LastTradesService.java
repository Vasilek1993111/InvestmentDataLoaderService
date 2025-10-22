package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.LastTradeDto;
import com.example.InvestmentDataLoaderService.dto.LastTradesRequestDto;
import com.example.InvestmentDataLoaderService.dto.LastTradesResponseDto;
import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.entity.LastPriceEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.repository.LastPriceRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class LastTradesService {

    private static final Logger log = LoggerFactory.getLogger(LastTradesService.class);
    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final LastPriceRepository lastPriceRepository;
    private final LastTradeService lastTradeService;
    private final CachedInstrumentService cachedInstrumentService;

    public LastTradesService(ShareRepository shareRepository, 
                           FutureRepository futureRepository,
                           LastPriceRepository lastPriceRepository,
                           LastTradeService lastTradeService,
                           CachedInstrumentService cachedInstrumentService) {
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.lastPriceRepository = lastPriceRepository;
        this.lastTradeService = lastTradeService;
        this.cachedInstrumentService = cachedInstrumentService;
    }

    /**
     * Ежедневная загрузка обезличенных сделок
     * Запускается в 3:00 по московскому времени
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Moscow")
    public void fetchAndStoreLastTrades() {
        try {
            LocalDate previousDay = LocalDate.now(ZoneId.of("Europe/Moscow")).minusDays(1);
            String taskId = "LAST_TRADES_" + UUID.randomUUID().toString().substring(0, 8);
            
            log.info("=== НАЧАЛО ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ===");
            log.info("[{}] Дата: {}", taskId, previousDay);
            log.info("[{}] Время запуска: {}", taskId, LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Торги проводятся в выходные дни, поэтому проверку убираем
            
            SaveResponseDto response = processLastTrades(previousDay, taskId);
            
            log.info("[{}] Загрузка завершена:", taskId);
            log.info("[{}] - Успех: {}", taskId, response.isSuccess());
            log.info("[{}] - Сообщение: {}", taskId, response.getMessage());
            log.info("[{}] - Всего запрошено: {}", taskId, response.getTotalRequested());
            log.info("[{}] - Сохранено новых: {}", taskId, response.getNewItemsSaved());
            log.info("[{}] - Пропущено существующих: {}", taskId, response.getExistingItemsSkipped());
            log.info("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ===");
            
        } catch (Exception e) {
            log.error("Критическая ошибка в загрузке обезличенных сделок: {}", e.getMessage(), e);
        }
    }

    /**
     * Обрабатывает обезличенные сделки для указанной даты
     */
    @Transactional(readOnly = true)
    public SaveResponseDto processLastTrades(LocalDate date, String taskId) {
        try {
            log.info("[{}] Начало обработки обезличенных сделок за {}", taskId, date);
            
            // Получаем все акции и фьючерсы из кэша (с fallback на БД)
            List<ShareEntity> shares = cachedInstrumentService.getAllShares();
            List<FutureEntity> futures = cachedInstrumentService.getAllFutures();
            log.info("[{}] {}", taskId, cachedInstrumentService.getCacheInfo());
            log.info("[{}] Найдено {} акций и {} фьючерсов для обработки", taskId, shares.size(), futures.size());
            
            List<LastTradesResponseDto> savedItems = new ArrayList<>();
            int totalRequested = 0;
            int savedCount = 0;
            int existingCount = 0;
            int processedInstruments = 0;
            
            // Обрабатываем акции
            for (ShareEntity share : shares) {
                try {
                    processedInstruments++;
                    log.info("[{}] Обработка акции {}/{}: {} ({})", taskId, processedInstruments, (shares.size() + futures.size()), share.getTicker(), share.getFigi());
                    
                    List<LastTradesResponseDto> trades = fetchLastTradesForInstrument(share.getFigi(), date, "moex_mrng_evng_e_wknd_dlr", taskId);
                    
                    for (LastTradesResponseDto trade : trades) {
                        totalRequested++;
                        
                        // Проверяем, есть ли уже запись для этого FIGI и времени
                        if (lastPriceRepository.existsById(new com.example.InvestmentDataLoaderService.entity.LastPriceKey(trade.getFigi(), trade.getTime()))) {
                            existingCount++;
                            continue;
                        }
                        
                        // Сохраняем в БД
                        LastPriceEntity entity = new LastPriceEntity(
                            trade.getFigi(),
                            trade.getTime(),
                            trade.getPrice(),
                            trade.getCurrency(),
                            trade.getExchange()
                        );
                        
                        saveLastPriceEntity(entity);
                        savedItems.add(trade);
                        savedCount++;
                    }
                    
                } catch (Exception e) {
                    log.error("[{}] Ошибка обработки акции {}: {}", taskId, share.getTicker(), e.getMessage(), e);
                }
            }
            
            // Обрабатываем фьючерсы
            for (FutureEntity future : futures) {
                try {
                    processedInstruments++;
                    log.info("[{}] Обработка фьючерса {}/{}: {} ({})", taskId, processedInstruments, (shares.size() + futures.size()), future.getTicker(), future.getFigi());
                    
                    List<LastTradesResponseDto> trades = fetchLastTradesForInstrument(future.getFigi(), date, "FORTS_EVENING", taskId);
                    
                    for (LastTradesResponseDto trade : trades) {
                        totalRequested++;
                        
                        // Проверяем, есть ли уже запись для этого FIGI и времени
                        if (lastPriceRepository.existsById(new com.example.InvestmentDataLoaderService.entity.LastPriceKey(trade.getFigi(), trade.getTime()))) {
                            existingCount++;
                            continue;
                        }
                        
                        // Сохраняем в БД
                        LastPriceEntity entity = new LastPriceEntity(
                            trade.getFigi(),
                            trade.getTime(),
                            trade.getPrice(),
                            trade.getCurrency(),
                            trade.getExchange()
                        );
                        
                        saveLastPriceEntity(entity);
                        savedItems.add(trade);
                        savedCount++;
                    }
                    
                } catch (Exception e) {
                    log.error("[{}] Ошибка обработки фьючерса {}: {}", taskId, future.getTicker(), e.getMessage(), e);
                }
            }
            
            log.info("[{}] Обработка завершена:", taskId);
            log.info("[{}] - Обработано инструментов: {} (акций: {}, фьючерсов: {})", taskId, processedInstruments, shares.size(), futures.size());
            log.info("[{}] - Запрошено сделок: {}", taskId, totalRequested);
            log.info("[{}] - Сохранено новых: {}", taskId, savedCount);
            log.info("[{}] - Пропущено существующих: {}", taskId, existingCount);
            
            return new SaveResponseDto(
                true,
                "Обезличенные сделки загружены. Сохранено: " + savedCount + ", пропущено: " + existingCount,
                totalRequested,
                savedCount,
                existingCount,
                0, // invalidItemsFiltered
                0, // missingFromApi
                savedItems
            );
            
        } catch (Exception e) {
            log.error("[{}] Критическая ошибка в обработке обезличенных сделок: {}", taskId, e.getMessage(), e);
            return new SaveResponseDto(
                false,
                "Ошибка загрузки обезличенных сделок: " + e.getMessage(),
                0,
                0,
                0,
                0,
                0, // missingFromApi
                new ArrayList<>()
            );
        }
    }

    /**
     * Загружает обезличенные сделки для конкретного инструмента за указанную дату
     */
    private List<LastTradesResponseDto> fetchLastTradesForInstrument(String figi, LocalDate date, String exchange, String taskId) {
        try {
            log.info("[{}] Загрузка обезличенных сделок для {} за дату: {}", taskId, figi, date);
            
            // Вызываем T-Invest API для получения обезличенных сделок
            List<LastTradeDto> tradesFromApi = lastTradeService.getLastTrades(figi, date, "TRADE_SOURCE_ALL");
            
            // Получаем валюту инструмента из кэша (с fallback на БД)
            String currency = "RUB"; // По умолчанию
            try {
                // Сначала пытаемся найти в кэше
                if (cachedInstrumentService.isInstrumentInCache(figi)) {
                    List<ShareEntity> shares = cachedInstrumentService.getAllShares();
                    ShareEntity share = shares.stream()
                            .filter(s -> s.getFigi().equals(figi))
                            .findFirst()
                            .orElse(null);
                    
                    if (share != null) {
                        currency = share.getCurrency();
                    } else {
                        List<FutureEntity> futures = cachedInstrumentService.getAllFutures();
                        FutureEntity future = futures.stream()
                                .filter(f -> f.getFigi().equals(figi))
                                .findFirst()
                                .orElse(null);
                        if (future != null) {
                            currency = future.getCurrency();
                        }
                    }
                } else {
                    // Fallback на БД
                    ShareEntity share = shareRepository.findById(figi).orElse(null);
                    if (share != null) {
                        currency = share.getCurrency();
                    } else {
                        FutureEntity future = futureRepository.findById(figi).orElse(null);
                        if (future != null) {
                            currency = future.getCurrency();
                        }
                    }
                }
            } catch (Exception e) {
                log.error("[{}] Ошибка получения валюты из кэша, используем БД: {}", taskId, e.getMessage(), e);
                // Fallback на БД
                ShareEntity share = shareRepository.findById(figi).orElse(null);
                if (share != null) {
                    currency = share.getCurrency();
                } else {
                    FutureEntity future = futureRepository.findById(figi).orElse(null);
                    if (future != null) {
                        currency = future.getCurrency();
                    }
                }
            }
            
            // Конвертируем в LastTradesResponseDto
            List<LastTradesResponseDto> trades = new ArrayList<>();
            for (LastTradeDto trade : tradesFromApi) {
                trades.add(new LastTradesResponseDto(
                    trade.figi(),
                    trade.time().atZone(ZoneId.of("Europe/Moscow")).toLocalDateTime(),
                    trade.price(),
                    currency,
                    exchange
                ));
            }
            
            log.info("[{}] Загружено {} сделок для {}", taskId, trades.size(), figi);
            return trades;
            
        } catch (Exception e) {
            log.error("[{}] Ошибка загрузки обезличенных сделок для {}: {}", taskId, figi, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private List<LastTradesResponseDto> fetchLastTradesForLastHour(String figi, String exchange, String taskId) {
        try {
            log.info("[{}] Загрузка обезличенных сделок за последний час для {}", taskId, figi);
            
            // Вызываем T-Invest API для получения обезличенных сделок за последний час
            List<LastTradeDto> tradesFromApi = lastTradeService.getLastTradesForLastHour(figi, "TRADE_SOURCE_ALL");
            
            // Получаем валюту инструмента из кэша (с fallback на БД)
            String currency = "RUB"; // По умолчанию
            try {
                // Сначала пытаемся найти в кэше
                if (cachedInstrumentService.isInstrumentInCache(figi)) {
                    List<ShareEntity> shares = cachedInstrumentService.getAllShares();
                    ShareEntity share = shares.stream()
                            .filter(s -> s.getFigi().equals(figi))
                            .findFirst()
                            .orElse(null);
                    
                    if (share != null) {
                        currency = share.getCurrency();
                    } else {
                        List<FutureEntity> futures = cachedInstrumentService.getAllFutures();
                        FutureEntity future = futures.stream()
                                .filter(f -> f.getFigi().equals(figi))
                                .findFirst()
                                .orElse(null);
                        if (future != null) {
                            currency = future.getCurrency();
                        }
                    }
                } else {
                    // Fallback на БД
                    ShareEntity share = shareRepository.findById(figi).orElse(null);
                    if (share != null) {
                        currency = share.getCurrency();
                    } else {
                        FutureEntity future = futureRepository.findById(figi).orElse(null);
                        if (future != null) {
                            currency = future.getCurrency();
                        }
                    }
                }
            } catch (Exception e) {
                log.error("[{}] Ошибка получения валюты из кэша, используем БД: {}", taskId, e.getMessage(), e);
                // Fallback на БД
                ShareEntity share = shareRepository.findById(figi).orElse(null);
                if (share != null) {
                    currency = share.getCurrency();
                } else {
                    FutureEntity future = futureRepository.findById(figi).orElse(null);
                    if (future != null) {
                        currency = future.getCurrency();
                    }
                }
            }
            
            // Конвертируем в LastTradesResponseDto
            List<LastTradesResponseDto> trades = new ArrayList<>();
            for (LastTradeDto trade : tradesFromApi) {
                trades.add(new LastTradesResponseDto(
                    trade.figi(),
                    trade.time().atZone(ZoneId.of("Europe/Moscow")).toLocalDateTime(),
                    trade.price(),
                    currency,
                    exchange
                ));
            }
            
            log.info("[{}] Загружено {} сделок за последний час для {}", taskId, trades.size(), figi);
            return trades;
            
        } catch (Exception e) {
            log.error("[{}] Ошибка загрузки обезличенных сделок за последний час для {}: {}", taskId, figi, e.getMessage(), e);
            return new ArrayList<>();
        }
    }


    /**
     * Ручная загрузка обезличенных сделок за конкретную дату
     */
    public SaveResponseDto fetchAndStoreLastTradesForDate(LocalDate date) {
        String taskId = "MANUAL_LAST_TRADES_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Торги проводятся в выходные дни, поэтому проверку убираем
        
        return processLastTrades(date, taskId);
    }

    /**
     * Ручная загрузка обезличенных сделок для конкретного инструмента за дату
     */
    public SaveResponseDto fetchAndStoreLastTradesForInstrument(String figi, LocalDate date) {
        String taskId = "MANUAL_LAST_TRADES_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Торги проводятся в выходные дни, поэтому проверку убираем
        
        try {
            log.info("[{}] Начало обработки обезличенных сделок для {} за {}", taskId, figi, date);
            
            List<LastTradesResponseDto> savedItems = new ArrayList<>();
            int totalRequested = 0;
            int savedCount = 0;
            int existingCount = 0;
            
            // Определяем биржу по типу инструмента
            String exchange = "moex_mrng_evng_e_wknd_dlr"; // По умолчанию для акций
            
            List<LastTradesResponseDto> trades = fetchLastTradesForInstrument(figi, date, exchange, taskId);
            
            for (LastTradesResponseDto trade : trades) {
                totalRequested++;
                
                // Проверяем, есть ли уже запись для этого FIGI и времени
                if (lastPriceRepository.existsById(new com.example.InvestmentDataLoaderService.entity.LastPriceKey(trade.getFigi(), trade.getTime()))) {
                    existingCount++;
                    continue;
                }
                
                // Сохраняем в БД
                LastPriceEntity entity = new LastPriceEntity(
                    trade.getFigi(),
                    trade.getTime(),
                    trade.getPrice(),
                    trade.getCurrency(),
                    trade.getExchange()
                );
                
                            saveLastPriceEntity(entity);
                savedItems.add(trade);
                savedCount++;
            }
            
            log.info("[{}] Обработка завершена:", taskId);
            log.info("[{}] - Запрошено сделок: {}", taskId, totalRequested);
            log.info("[{}] - Сохранено новых: {}", taskId, savedCount);
            log.info("[{}] - Пропущено существующих: {}", taskId, existingCount);
            
            return new SaveResponseDto(
                true,
                "Обезличенные сделки для " + figi + " загружены. Сохранено: " + savedCount + ", пропущено: " + existingCount,
                totalRequested,
                savedCount,
                existingCount,
                0, // invalidItemsFiltered
                0, // missingFromApi
                savedItems
            );
            
        } catch (Exception e) {
            log.error("[{}] Критическая ошибка в обработке обезличенных сделок для {}: {}", taskId, figi, e.getMessage(), e);
            return new SaveResponseDto(
                false,
                "Ошибка загрузки обезличенных сделок для " + figi + ": " + e.getMessage(),
                0,
                0,
                0,
                0,
                0, // missingFromApi
                new ArrayList<>()
            );
        }
    }

    /**
     * Загрузка обезличенных сделок за последний час по запросу с телом
     */
    public SaveResponseDto fetchAndStoreLastTradesByRequest(LastTradesRequestDto request) {
        String taskId = "REQUEST_LAST_TRADES_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Проверяем обязательные параметры
        if (request.getFigis() == null || request.getFigis().isEmpty()) {
            String message = "Параметр 'figis' является обязательным и не может быть пустым";
            log.info("[{}] {}", taskId, message);
            return new SaveResponseDto(
                false,
                message,
                0,
                0,
                0,
                0,
                0, // missingFromApi
                new ArrayList<>()
            );
        }
        
        try {
            log.info("[{}] Начало обработки обезличенных сделок за последний час для {} инструментов", taskId, request.getFigis().size());
            
            List<LastTradesResponseDto> savedItems = new ArrayList<>();
            int totalRequested = 0;
            int savedCount = 0;
            int existingCount = 0;
            int processedInstruments = 0;
            
            // Определяем список инструментов для обработки
            List<String> instrumentsToProcess;
            if (request.isLoadAll()) {
                log.info("[{}] Загружаем обезличенные сделки для всех инструментов", taskId);
                instrumentsToProcess = new ArrayList<>();
                
                // Добавляем все акции из кэша
                List<ShareEntity> shares = cachedInstrumentService.getAllShares();
                for (ShareEntity share : shares) {
                    instrumentsToProcess.add(share.getFigi());
                }
                
                // Добавляем все фьючерсы из кэша
                List<FutureEntity> futures = cachedInstrumentService.getAllFutures();
                for (FutureEntity future : futures) {
                    instrumentsToProcess.add(future.getFigi());
                }
                
                log.info("[{}] {}", taskId, cachedInstrumentService.getCacheInfo());
                log.info("[{}] Найдено {} инструментов для обработки", taskId, instrumentsToProcess.size());
            } else if (request.isLoadAllShares()) {
                log.info("[{}] Загружаем обезличенные сделки для всех акций", taskId);
                instrumentsToProcess = new ArrayList<>();
                
                // Добавляем только акции из кэша
                List<ShareEntity> shares = cachedInstrumentService.getAllShares();
                for (ShareEntity share : shares) {
                    instrumentsToProcess.add(share.getFigi());
                }
                
                log.info("[{}] {}", taskId, cachedInstrumentService.getCacheInfo());
                log.info("[{}] Найдено {} акций для обработки", taskId, instrumentsToProcess.size());
            } else if (request.isLoadAllFutures()) {
                log.info("[{}] Загружаем обезличенные сделки для всех фьючерсов", taskId);
                instrumentsToProcess = new ArrayList<>();
                
                // Добавляем только фьючерсы из кэша
                List<FutureEntity> futures = cachedInstrumentService.getAllFutures();
                for (FutureEntity future : futures) {
                    instrumentsToProcess.add(future.getFigi());
                }
                
                log.info("[{}] {}", taskId, cachedInstrumentService.getCacheInfo());
                log.info("[{}] Найдено {} фьючерсов для обработки", taskId, instrumentsToProcess.size());
            } else {
                instrumentsToProcess = request.getFigis();
            }
            
            // Обрабатываем инструменты
            for (String figi : instrumentsToProcess) {
                try {
                    processedInstruments++;
                    log.info("[{}] Обработка инструмента {}/{}: {}", taskId, processedInstruments, instrumentsToProcess.size(), figi);
                    
                    // Определяем тип инструмента и биржу
                    String exchange = "moex_mrng_evng_e_wknd_dlr"; // По умолчанию для акций
                    
                    // Проверяем, является ли это фьючерсом
                    if (futureRepository.existsById(figi)) {
                        exchange = "FORTS_EVENING";
                    }
                    
                    // Получаем сделки за последний час
                    List<LastTradesResponseDto> trades = fetchLastTradesForLastHour(figi, exchange, taskId);
                        
                        for (LastTradesResponseDto trade : trades) {
                            totalRequested++;
                            
                            // Проверяем, есть ли уже запись для этого FIGI и времени
                            if (lastPriceRepository.existsById(new com.example.InvestmentDataLoaderService.entity.LastPriceKey(trade.getFigi(), trade.getTime()))) {
                                existingCount++;
                                continue;
                            }
                            
                            // Сохраняем в БД
                            LastPriceEntity entity = new LastPriceEntity(
                                trade.getFigi(),
                                trade.getTime(),
                                trade.getPrice(),
                                trade.getCurrency(),
                                trade.getExchange()
                            );
                            
                            saveLastPriceEntity(entity);
                            savedItems.add(trade);
                            savedCount++;
                        }
                        
                } catch (Exception e) {
                    log.error("[{}] Ошибка обработки инструмента {}: {}", taskId, figi, e.getMessage(), e);
                }
            }
            
            log.info("[{}] Обработка завершена:", taskId);
            log.info("[{}] - Обработано инструментов: {}", taskId, processedInstruments);
            log.info("[{}] - Запрошено сделок: {}", taskId, totalRequested);
            log.info("[{}] - Сохранено новых: {}", taskId, savedCount);
            log.info("[{}] - Пропущено существующих: {}", taskId, existingCount);
            
            return new SaveResponseDto(
                true,
                "Обезличенные сделки загружены по запросу. Сохранено: " + savedCount + ", пропущено: " + existingCount,
                totalRequested,
                savedCount,
                existingCount,
                0, // invalidItemsFiltered
                0, // missingFromApi
                savedItems
            );
            
        } catch (Exception e) {
            log.error("[{}] Критическая ошибка в обработке обезличенных сделок по запросу: {}", taskId, e.getMessage(), e);
            return new SaveResponseDto(
                false,
                "Ошибка загрузки обезличенных сделок по запросу: " + e.getMessage(),
                0,
                0,
                0,
                0,
                0, // missingFromApi
                new ArrayList<>()
            );
        }
    }

    /**
     * Асинхронная загрузка обезличенных сделок за последний час
     */
    public void fetchAndStoreLastTradesByRequestAsync(LastTradesRequestDto request) {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("=== НАЧАЛО АСИНХРОННОЙ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ===");
                SaveResponseDto result = fetchAndStoreLastTradesByRequest(request);
                log.info("=== ЗАВЕРШЕНИЕ АСИНХРОННОЙ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ===");
                log.info("Результат: {}", result.getMessage());
            } catch (Exception e) {
                log.error("=== ОШИБКА АСИНХРОННОЙ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ===");
                log.error("Ошибка: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * Транзакционное сохранение LastPriceEntity
     */
    @Transactional
    public void saveLastPriceEntity(LastPriceEntity entity) {
        lastPriceRepository.save(entity);
    }

    /**
     * Асинхронная загрузка обезличенных сделок только по акциям за последний час
     */
    public void fetchAndStoreLastTradesSharesAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("=== НАЧАЛО АСИНХРОННОЙ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ПО АКЦИЯМ ===");
                
                // Создаем запрос только для акций
                LastTradesRequestDto request = new LastTradesRequestDto();
                request.setFigis(java.util.Arrays.asList("ALL_SHARES"));
                request.setTradeSource("TRADE_SOURCE_ALL");
                
                SaveResponseDto result = fetchAndStoreLastTradesByRequest(request);
                log.info("=== ЗАВЕРШЕНИЕ АСИНХРОННОЙ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ПО АКЦИЯМ ===");
                log.info("Результат: {}", result.getMessage());
                log.info("Обработано инструментов: {}", result.getTotalRequested());
                log.info("Сохранено новых: {}", result.getNewItemsSaved());
                log.info("Пропущено существующих: {}", result.getExistingItemsSkipped());
            } catch (Exception e) {
                log.error("=== ОШИБКА АСИНХРОННОЙ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ПО АКЦИЯМ ===");
                log.error("Ошибка: {}", e.getMessage(), e);
            }
        });
    }
}
