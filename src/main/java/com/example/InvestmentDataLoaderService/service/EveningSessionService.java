package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.entity.ClosePriceEveningSessionEntity;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.entity.SystemLogEntity;
import com.example.InvestmentDataLoaderService.repository.ClosePriceEveningSessionRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.MinuteCandleRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.SystemLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Сервис для работы с ценами вечерней сессии
 * Обрабатывает данные из minute_candles и сохраняет в close_price_evening_session
 */
@Service
public class EveningSessionService {

    private static final Logger logger = LoggerFactory.getLogger(EveningSessionService.class);

    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final MinuteCandleRepository minuteCandleRepository;
    private final ClosePriceEveningSessionRepository closePriceEveningSessionRepository;
    private final SystemLogRepository systemLogRepository;
    private final ExecutorService executorService;

    public EveningSessionService(
            ShareRepository shareRepository,
            FutureRepository futureRepository,
            MinuteCandleRepository minuteCandleRepository,
            ClosePriceEveningSessionRepository closePriceEveningSessionRepository,
            SystemLogRepository systemLogRepository) {
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.minuteCandleRepository = minuteCandleRepository;
        this.closePriceEveningSessionRepository = closePriceEveningSessionRepository;
        this.systemLogRepository = systemLogRepository;
        this.executorService = Executors.newFixedThreadPool(20);
    }

    /**
     * Асинхронная загрузка цен закрытия вечерней сессии за вчерашний день
     */
    public CompletableFuture<SaveResponseDto> loadEveningSessionPricesAsync(String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LocalDate yesterday = LocalDate.now().minusDays(1);
                logger.info("[" + taskId + "] Начало асинхронной загрузки цен вечерней сессии за " + yesterday);
                
                return processEveningSessionPricesForDate(yesterday, taskId);
                
            } catch (Exception e) {
                logger.error("[" + taskId + "] Ошибка асинхронной загрузки цен вечерней сессии: " + e.getMessage());
                e.printStackTrace();
                
                // Логируем ошибку в БД (синхронно, как в InstrumentService)
                try {
                    SystemLogEntity errorLog = new SystemLogEntity();
                    errorLog.setTaskId(taskId);
                    errorLog.setEndpoint("/api/evening-session-prices");
                    errorLog.setMethod("POST");
                    errorLog.setStatus("FAILED");
                    errorLog.setMessage("Ошибка асинхронной загрузки цен вечерней сессии: " + e.getMessage());
                    errorLog.setStartTime(Instant.now().minusMillis(1000)); // Примерное время начала
                    errorLog.setEndTime(Instant.now());
                    systemLogRepository.save(errorLog);
                    logger.info("[" + taskId + "] Лог ошибки сохранен в БД");
                } catch (Exception logException) {
                    logger.error("[" + taskId + "] Ошибка сохранения лога ошибки: " + logException.getMessage());
                }
                
                return new SaveResponseDto(
                    false,
                    "Ошибка загрузки цен вечерней сессии: " + e.getMessage(),
                    0, 0, 0, 0, 0, new ArrayList<>()
                );
            }
        });
    }

    /**
     * Асинхронная загрузка цен вечерней сессии за конкретную дату
     */
    public CompletableFuture<SaveResponseDto> loadEveningSessionPricesForDateAsync(LocalDate date, String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("[{}] === СЕРВИС: Начало асинхронной загрузки цен вечерней сессии за {} ===", taskId, date);
                
                return processEveningSessionPricesForDate(date, taskId);
                
            } catch (Exception e) {
                logger.error("[{}] Ошибка асинхронной загрузки цен вечерней сессии за {}: {}", taskId, date, e.getMessage());
                e.printStackTrace();
                
                // Логируем ошибку в БД (синхронно, как в InstrumentService)
                try {
                    SystemLogEntity errorLog = new SystemLogEntity();
                    errorLog.setTaskId(taskId);
                    errorLog.setEndpoint("/api/evening-session-prices/by-date/" + date);
                    errorLog.setMethod("POST");
                    errorLog.setStatus("FAILED");
                    errorLog.setMessage("Ошибка асинхронной загрузки цен вечерней сессии за " + date + ": " + e.getMessage());
                    errorLog.setStartTime(Instant.now().minusMillis(1000)); // Примерное время начала
                    errorLog.setEndTime(Instant.now());
                    systemLogRepository.save(errorLog);
                    logger.info("[" + taskId + "] Лог ошибки сохранен в БД");
                } catch (Exception logException) {
                    logger.error("[" + taskId + "] Ошибка сохранения лога ошибки: " + logException.getMessage());
                }
                
                return new SaveResponseDto(
                    false,
                    "Ошибка загрузки цен вечерней сессии за " + date + ": " + e.getMessage(),
                    0, 0, 0, 0, 0, new ArrayList<>()
                );
            }
        });
    }

    /**
     * Асинхронная загрузка цен вечерней сессии для акций за дату
     */
    public CompletableFuture<SaveResponseDto> loadSharesEveningSessionPricesAsync(LocalDate date, String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("[" + taskId + "] Начало асинхронной загрузки цен вечерней сессии для акций за " + date);
                
                return processSharesEveningSessionPricesForDate(date, taskId);
                
            } catch (Exception e) {
                logger.error("[" + taskId + "] Ошибка асинхронной загрузки цен вечерней сессии для акций за " + date + ": " + e.getMessage());
                e.printStackTrace();
                
                // Логируем ошибку в БД (синхронно, как в InstrumentService)
                try {
                    SystemLogEntity errorLog = new SystemLogEntity();
                    errorLog.setTaskId(taskId);
                    errorLog.setEndpoint("/api/evening-session-prices/shares/" + date);
                    errorLog.setMethod("POST");
                    errorLog.setStatus("FAILED");
                    errorLog.setMessage("Ошибка асинхронной загрузки цен вечерней сессии для акций за " + date + ": " + e.getMessage());
                    errorLog.setStartTime(Instant.now().minusMillis(1000)); // Примерное время начала
                    errorLog.setEndTime(Instant.now());
                    systemLogRepository.save(errorLog);
                    logger.info("[" + taskId + "] Лог ошибки сохранен в БД");
                } catch (Exception logException) {
                    logger.error("[" + taskId + "] Ошибка сохранения лога ошибки: " + logException.getMessage());
                }
                
                return new SaveResponseDto(
                    false,
                    "Ошибка загрузки цен вечерней сессии для акций за " + date + ": " + e.getMessage(),
                    0, 0, 0, 0, 0, new ArrayList<>()
                );
            }
        });
    }

    /**
     * Асинхронная загрузка цен вечерней сессии для фьючерсов за дату
     */
    public CompletableFuture<SaveResponseDto> loadFuturesEveningSessionPricesAsync(LocalDate date, String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("[" + taskId + "] Начало асинхронной загрузки цен вечерней сессии для фьючерсов за " + date);
                
                return processFuturesEveningSessionPricesForDate(date, taskId);
                
            } catch (Exception e) {
                logger.error("[" + taskId + "] Ошибка асинхронной загрузки цен вечерней сессии для фьючерсов за " + date + ": " + e.getMessage());
                e.printStackTrace();
                
                // Логируем ошибку в БД (синхронно, как в InstrumentService)
                try {
                    SystemLogEntity errorLog = new SystemLogEntity();
                    errorLog.setTaskId(taskId);
                    errorLog.setEndpoint("/api/evening-session-prices/futures/" + date);
                    errorLog.setMethod("POST");
                    errorLog.setStatus("FAILED");
                    errorLog.setMessage("Ошибка асинхронной загрузки цен вечерней сессии для фьючерсов за " + date + ": " + e.getMessage());
                    errorLog.setStartTime(Instant.now().minusMillis(1000)); // Примерное время начала
                    errorLog.setEndTime(Instant.now());
                    systemLogRepository.save(errorLog);
                    logger.info("[" + taskId + "] Лог ошибки сохранен в БД");
                } catch (Exception logException) {
                    logger.error("[" + taskId + "] Ошибка сохранения лога ошибки: " + logException.getMessage());
                }
                
                return new SaveResponseDto(
                    false,
                    "Ошибка загрузки цен вечерней сессии для фьючерсов за " + date + ": " + e.getMessage(),
                    0, 0, 0, 0, 0, new ArrayList<>()
                );
            }
        });
    }

    /**
     * Асинхронная загрузка цены вечерней сессии по инструменту за дату
     */
    public CompletableFuture<SaveResponseDto> loadEveningSessionPriceByFigiAsync(String figi, LocalDate date, String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("[" + taskId + "] Начало асинхронной загрузки цены вечерней сессии для " + figi + " за " + date);
                
                return processEveningSessionPriceByFigi(figi, date, taskId);
                
            } catch (Exception e) {
                logger.error("[" + taskId + "] Ошибка асинхронной загрузки цены вечерней сессии для " + figi + " за " + date + ": " + e.getMessage());
                e.printStackTrace();
                
                // Логируем ошибку в БД (синхронно, как в InstrumentService)
                try {
                    SystemLogEntity errorLog = new SystemLogEntity();
                    errorLog.setTaskId(taskId);
                    errorLog.setEndpoint("/api/evening-session-prices/by-figi-date/" + figi + "/" + date);
                    errorLog.setMethod("POST");
                    errorLog.setStatus("FAILED");
                    errorLog.setMessage("Ошибка асинхронной загрузки цены вечерней сессии для " + figi + " за " + date + ": " + e.getMessage());
                    errorLog.setStartTime(Instant.now().minusMillis(1000)); // Примерное время начала
                    errorLog.setEndTime(Instant.now());
                    systemLogRepository.save(errorLog);
                    logger.info("[" + taskId + "] Лог ошибки сохранен в БД");
                } catch (Exception logException) {
                    logger.error("[" + taskId + "] Ошибка сохранения лога ошибки: " + logException.getMessage());
                }
                
                return new SaveResponseDto(
                    false,
                    "Ошибка загрузки цены вечерней сессии для " + figi + " за " + date + ": " + e.getMessage(),
                    0, 0, 0, 0, 0, new ArrayList<>()
                );
            }
        });
    }

    /**
     * Обработка цен вечерней сессии для всех инструментов за дату
     */
    private SaveResponseDto processEveningSessionPricesForDate(LocalDate date, String taskId) {
        try {
            logger.info("[" + taskId + "] Начинаем загрузку данных из БД...");
            
            // Получаем все акции и фьючерсы из БД (блокирующий запрос)
            long startTime = System.currentTimeMillis();
            List<ShareEntity> shares = shareRepository.findAll();
            long sharesTime = System.currentTimeMillis();
            logger.info("[" + taskId + "] Загрузка акций заняла: " + (sharesTime - startTime) + "мс");
            
            List<FutureEntity> futures = futureRepository.findAll();
            long futuresTime = System.currentTimeMillis();
            logger.info("[" + taskId + "] Загрузка фьючерсов заняла: " + (futuresTime - sharesTime) + "мс");
            
            logger.info("[" + taskId + "] Найдено акций: " + shares.size() + ", фьючерсов: " + futures.size());
            
            int totalRequested = shares.size() + futures.size();
            AtomicInteger newItemsSaved = new AtomicInteger(0);
            AtomicInteger existingItemsSkipped = new AtomicInteger(0);
            AtomicInteger invalidItemsFiltered = new AtomicInteger(0);
            AtomicInteger missingFromApi = new AtomicInteger(0);
            List<Map<String, Object>> savedItems = Collections.synchronizedList(new ArrayList<>());
            
            logger.info("[" + taskId + "] Начинаем параллельную обработку " + shares.size() + " акций и " + futures.size() + " фьючерсов...");
            
            // Параллельная обработка акций с обработкой ошибок
            List<CompletableFuture<Void>> shareFutures = shares.stream()
                .map(share -> CompletableFuture.runAsync(() -> {
                    try {
                        processShareEveningSessionPrice(share, date, newItemsSaved, existingItemsSkipped, 
                            invalidItemsFiltered, missingFromApi, savedItems, taskId);
                    } catch (Exception e) {
                        logger.error("[" + taskId + "] Ошибка обработки акции " + share.getTicker() + ": " + e.getMessage());
                        missingFromApi.incrementAndGet();
                    }
                }, executorService))
                .collect(Collectors.toList());
            
            // Параллельная обработка фьючерсов с обработкой ошибок
            List<CompletableFuture<Void>> futureFutures = futures.stream()
                .map(future -> CompletableFuture.runAsync(() -> {
                    try {
                        processFutureEveningSessionPrice(future, date, newItemsSaved, existingItemsSkipped, 
                            invalidItemsFiltered, missingFromApi, savedItems, taskId);
                    } catch (Exception e) {
                        logger.error("[" + taskId + "] Ошибка обработки фьючерса " + future.getTicker() + ": " + e.getMessage());
                        missingFromApi.incrementAndGet();
                    }
                }, executorService))
                .collect(Collectors.toList());
            
            // Ждем завершения всех операций
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                shareFutures.toArray(new CompletableFuture[0])
            ).thenCompose(v -> CompletableFuture.allOf(
                futureFutures.toArray(new CompletableFuture[0])
            ));
            
            logger.info("[" + taskId + "] Ждем завершения всех операций...");
            long processingStartTime = System.currentTimeMillis();
            allFutures.join();
            long processingEndTime = System.currentTimeMillis();
            logger.info("[" + taskId + "] Обработка завершена за: " + (processingEndTime - processingStartTime) + "мс");
            
            // Логируем успешное завершение в БД (синхронно, как в InstrumentService)
            try {
                SystemLogEntity successLog = new SystemLogEntity();
                successLog.setTaskId(taskId);
                successLog.setEndpoint("/api/evening-session-prices/by-date/" + date);
                successLog.setMethod("POST");
                successLog.setStatus("COMPLETED");
                successLog.setMessage("Успешно загружено " + newItemsSaved.get() + " новых цен вечерней сессии из " + totalRequested + " найденных.");
                successLog.setStartTime(Instant.now().minusMillis(1000)); // Примерное время начала
                successLog.setEndTime(Instant.now());
                systemLogRepository.save(successLog);
                logger.info("[" + taskId + "] Лог успешного завершения сохранен в БД");
            } catch (Exception logException) {
                logger.error("[" + taskId + "] Ошибка сохранения лога успешного завершения: " + logException.getMessage());
            }
            
            return new SaveResponseDto(
                true,
                "Успешно загружено " + newItemsSaved.get() + " новых цен вечерней сессии из " + totalRequested + " найденных.",
                totalRequested,
                newItemsSaved.get(),
                existingItemsSkipped.get(),
                invalidItemsFiltered.get(),
                missingFromApi.get(),
                savedItems
            );
            
        } catch (Exception e) {
            logger.error("[" + taskId + "] Ошибка обработки цен вечерней сессии за " + date + ": " + e.getMessage());
            e.printStackTrace();
            
            // Логируем ошибку в БД (синхронно, как в InstrumentService)
            try {
                SystemLogEntity errorLog = new SystemLogEntity();
                errorLog.setTaskId(taskId);
                errorLog.setEndpoint("/api/evening-session-prices/by-date/" + date);
                errorLog.setMethod("POST");
                errorLog.setStatus("FAILED");
                errorLog.setMessage("Ошибка обработки цен вечерней сессии за " + date + ": " + e.getMessage());
                errorLog.setStartTime(Instant.now().minusMillis(1000)); // Примерное время начала
                errorLog.setEndTime(Instant.now());
                systemLogRepository.save(errorLog);
                logger.info("[" + taskId + "] Лог ошибки сохранен в БД");
            } catch (Exception logException) {
                logger.error("[" + taskId + "] Ошибка сохранения лога ошибки: " + logException.getMessage());
            }
            
            throw new RuntimeException("Ошибка обработки цен вечерней сессии за " + date + ": " + e.getMessage(), e);
        }
    }

    /**
     * Обработка цен вечерней сессии для акций за дату
     */
    private SaveResponseDto processSharesEveningSessionPricesForDate(LocalDate date, String taskId) {
        try {
            // Получаем все акции из БД (блокирующий запрос)
            List<ShareEntity> shares = shareRepository.findAll();
            
            logger.info("[" + taskId + "] Найдено акций: " + shares.size());
            
            int totalRequested = shares.size();
            AtomicInteger newItemsSaved = new AtomicInteger(0);
            AtomicInteger existingItemsSkipped = new AtomicInteger(0);
            AtomicInteger invalidItemsFiltered = new AtomicInteger(0);
            AtomicInteger missingFromApi = new AtomicInteger(0);
            List<Map<String, Object>> savedItems = Collections.synchronizedList(new ArrayList<>());
            
            // Параллельная обработка акций
            List<CompletableFuture<Void>> shareFutures = shares.stream()
                .map(share -> CompletableFuture.runAsync(() -> 
                    processShareEveningSessionPrice(share, date, newItemsSaved, existingItemsSkipped, 
                        invalidItemsFiltered, missingFromApi, savedItems, taskId), executorService))
                .collect(Collectors.toList());
            
            // Ждем завершения всех операций
            CompletableFuture.allOf(shareFutures.toArray(new CompletableFuture[0])).join();
            
            // Логируем успешное завершение в БД (синхронно, как в InstrumentService)
            try {
                SystemLogEntity successLog = new SystemLogEntity();
                successLog.setTaskId(taskId);
                successLog.setEndpoint("/api/evening-session-prices/shares/" + date);
                successLog.setMethod("POST");
                successLog.setStatus("COMPLETED");
                successLog.setMessage("Успешно загружено " + newItemsSaved.get() + " новых цен вечерней сессии для акций из " + totalRequested + " найденных.");
                successLog.setStartTime(Instant.now().minusMillis(1000)); // Примерное время начала
                successLog.setEndTime(Instant.now());
                systemLogRepository.save(successLog);
                logger.info("[" + taskId + "] Лог успешного завершения для акций сохранен в БД");
            } catch (Exception logException) {
                logger.error("[" + taskId + "] Ошибка сохранения лога успешного завершения для акций: " + logException.getMessage());
            }
            
            return new SaveResponseDto(
                true,
                "Успешно загружено " + newItemsSaved.get() + " новых цен вечерней сессии для акций из " + totalRequested + " найденных.",
                totalRequested,
                newItemsSaved.get(),
                existingItemsSkipped.get(),
                invalidItemsFiltered.get(),
                missingFromApi.get(),
                savedItems
            );

        } catch (Exception e) {
            logger.error("[" + taskId + "] Ошибка обработки цен вечерней сессии для акций за " + date + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Ошибка обработки цен вечерней сессии для акций за " + date + ": " + e.getMessage(), e);
        }
    }

    /**
     * Обработка цен вечерней сессии для фьючерсов за дату
     */
    private SaveResponseDto processFuturesEveningSessionPricesForDate(LocalDate date, String taskId) {
        try {
            // Получаем все фьючерсы из БД (блокирующий запрос)
            List<FutureEntity> futures = futureRepository.findAll();
            
            logger.info("[" + taskId + "] Найдено фьючерсов: " + futures.size());
            
            int totalRequested = futures.size();
            AtomicInteger newItemsSaved = new AtomicInteger(0);
            AtomicInteger existingItemsSkipped = new AtomicInteger(0);
            AtomicInteger invalidItemsFiltered = new AtomicInteger(0);
            AtomicInteger missingFromApi = new AtomicInteger(0);
            List<Map<String, Object>> savedItems = Collections.synchronizedList(new ArrayList<>());
            
            // Параллельная обработка фьючерсов
            List<CompletableFuture<Void>> futureFutures = futures.stream()
                .map(future -> CompletableFuture.runAsync(() -> 
                    processFutureEveningSessionPrice(future, date, newItemsSaved, existingItemsSkipped, 
                        invalidItemsFiltered, missingFromApi, savedItems, taskId), executorService))
                .collect(Collectors.toList());
            
            // Ждем завершения всех операций
            CompletableFuture.allOf(futureFutures.toArray(new CompletableFuture[0])).join();
            
            // Логируем успешное завершение в БД (синхронно, как в InstrumentService)
            try {
                SystemLogEntity successLog = new SystemLogEntity();
                successLog.setTaskId(taskId);
                successLog.setEndpoint("/api/evening-session-prices/futures/" + date);
                successLog.setMethod("POST");
                successLog.setStatus("COMPLETED");
                successLog.setMessage("Успешно загружено " + newItemsSaved.get() + " новых цен вечерней сессии для фьючерсов из " + totalRequested + " найденных.");
                successLog.setStartTime(Instant.now().minusMillis(1000)); // Примерное время начала
                successLog.setEndTime(Instant.now());
                systemLogRepository.save(successLog);
                logger.info("[" + taskId + "] Лог успешного завершения для фьючерсов сохранен в БД");
            } catch (Exception logException) {
                logger.error("[" + taskId + "] Ошибка сохранения лога успешного завершения для фьючерсов: " + logException.getMessage());
            }
            
            return new SaveResponseDto(
                true,
                "Успешно загружено " + newItemsSaved.get() + " новых цен вечерней сессии для фьючерсов из " + totalRequested + " найденных.",
                totalRequested,
                newItemsSaved.get(),
                existingItemsSkipped.get(),
                invalidItemsFiltered.get(),
                missingFromApi.get(),
                savedItems
            );
            
        } catch (Exception e) {
            logger.error("[" + taskId + "] Ошибка обработки цен вечерней сессии для фьючерсов за " + date + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Ошибка обработки цен вечерней сессии для фьючерсов за " + date + ": " + e.getMessage(), e);
        }
    }

    /**
     * Обработка цены вечерней сессии по инструменту за дату
     */
    private SaveResponseDto processEveningSessionPriceByFigi(String figi, LocalDate date, String taskId) {
        try {
            // Получаем последнюю свечу за день для инструмента (блокирующий запрос)
            var lastCandle = minuteCandleRepository.findLastCandleForDate(figi, date);
            
            if (lastCandle == null) {
                return new SaveResponseDto(
                    false,
                    "Цена вечерней сессии не найдена для инструмента " + figi + " за " + date,
                    1, 0, 0, 0, 1, new ArrayList<>()
                );
            }
            
            BigDecimal lastClosePrice = lastCandle.getClose();
            
            // Проверяем, что цена не равна 0 (невалидная цена)
            if (lastClosePrice.compareTo(BigDecimal.ZERO) <= 0) {
                return new SaveResponseDto(
                    false,
                    "Невалидная цена закрытия для инструмента " + figi + " за " + date,
                    1, 0, 0, 1, 0, new ArrayList<>()
                );
            }
            
            // Проверяем, есть ли уже запись для этой даты и FIGI
            if (closePriceEveningSessionRepository.existsByPriceDateAndFigi(date, figi)) {
                return new SaveResponseDto(
                    false,
                    "Запись уже существует для инструмента " + figi + " за " + date,
                    1, 0, 1, 0, 0, new ArrayList<>()
                );
            }
            
            // Определяем тип инструмента
            String instrumentType = "UNKNOWN";
            boolean isShare = shareRepository.findAll().stream()
                    .anyMatch(share -> share.getFigi().equals(figi));
            boolean isFuture = futureRepository.findAll().stream()
                    .anyMatch(future -> future.getFigi().equals(figi));
            
            if (isShare) {
                instrumentType = "SHARE";
            } else if (isFuture) {
                    instrumentType = "FUTURE";
            }
            
            // Создаем запись для сохранения
            ClosePriceEveningSessionEntity entity = new ClosePriceEveningSessionEntity();
            entity.setFigi(figi);
            entity.setPriceDate(date);
            entity.setClosePrice(lastClosePrice);
            entity.setInstrumentType(instrumentType);
            entity.setCurrency("RUB");
            entity.setExchange("MOEX");
            
            closePriceEveningSessionRepository.save(entity);
            
            Map<String, Object> savedItem = new HashMap<>();
            savedItem.put("figi", figi);
            savedItem.put("priceDate", date.toString());
            savedItem.put("closePrice", lastClosePrice);
            savedItem.put("instrumentType", instrumentType);
            savedItem.put("currency", "RUB");
            savedItem.put("exchange", "MOEX");
            
            // Логируем успешное завершение в БД (синхронно, как в InstrumentService)
            try {
                SystemLogEntity successLog = new SystemLogEntity();
                successLog.setTaskId(taskId);
                successLog.setEndpoint("/api/evening-session-prices/by-figi-date/" + figi + "/" + date);
                successLog.setMethod("POST");
                successLog.setStatus("COMPLETED");
                successLog.setMessage("Цена вечерней сессии для инструмента " + figi + " за " + date + " сохранена успешно");
                successLog.setStartTime(Instant.now().minusMillis(1000)); // Примерное время начала
                successLog.setEndTime(Instant.now());
                systemLogRepository.save(successLog);
                logger.info("[" + taskId + "] Лог успешного завершения для инструмента сохранен в БД");
            } catch (Exception logException) {
                logger.error("[" + taskId + "] Ошибка сохранения лога успешного завершения для инструмента: " + logException.getMessage());
            }
            
            return new SaveResponseDto(
                true,
                "Цена вечерней сессии для инструмента " + figi + " за " + date + " сохранена успешно",
                1, 1, 0, 0, 0, List.of(savedItem)
            );
            
        } catch (Exception e) {
            logger.error("[" + taskId + "] Ошибка обработки цены вечерней сессии для " + figi + " за " + date + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Ошибка обработки цены вечерней сессии для " + figi + " за " + date + ": " + e.getMessage(), e);
        }
    }

    /**
     * Обработка цены вечерней сессии для акции
     */
    private void processShareEveningSessionPrice(ShareEntity share, LocalDate date, 
            AtomicInteger newItemsSaved, AtomicInteger existingItemsSkipped, 
            AtomicInteger invalidItemsFiltered, AtomicInteger missingFromApi, 
            List<Map<String, Object>> savedItems, String taskId) {
        try {
            // Получаем последнюю свечу за день для акции (блокирующий запрос)
            var lastCandle = minuteCandleRepository.findLastCandleForDate(share.getFigi(), date);
            
            if (lastCandle == null) {
                missingFromApi.incrementAndGet();
                return;
            }
            
            BigDecimal lastClosePrice = lastCandle.getClose();
            
            // Проверяем, есть ли уже запись для этой даты и FIGI
            if (closePriceEveningSessionRepository.existsByPriceDateAndFigi(date, share.getFigi())) {
                existingItemsSkipped.incrementAndGet();
                return;
            }
            
            // Проверяем, что цена не равна 0 (невалидная цена)
            if (lastClosePrice.compareTo(BigDecimal.ZERO) <= 0) {
                invalidItemsFiltered.incrementAndGet();
                return;
            }
            
            // Создаем запись для сохранения
                ClosePriceEveningSessionEntity entity = new ClosePriceEveningSessionEntity();
            entity.setFigi(share.getFigi());
            entity.setPriceDate(date);
            entity.setClosePrice(lastClosePrice);
            entity.setInstrumentType("SHARE");
            entity.setCurrency("RUB");
            entity.setExchange("MOEX");
            
            closePriceEveningSessionRepository.save(entity);
            
            Map<String, Object> savedItem = new HashMap<>();
            savedItem.put("figi", share.getFigi());
            savedItem.put("ticker", share.getTicker());
            savedItem.put("name", share.getName());
            savedItem.put("priceDate", date.toString());
            savedItem.put("closePrice", lastClosePrice);
            savedItem.put("instrumentType", "SHARE");
            savedItem.put("currency", "RUB");
            savedItem.put("exchange", "MOEX");
            
            savedItems.add(savedItem);
            newItemsSaved.incrementAndGet();
            
            } catch (Exception e) {
            logger.error("[" + taskId + "] Ошибка обработки акции " + share.getTicker() + ": " + e.getMessage());
            missingFromApi.incrementAndGet();
        }
    }

    /**
     * Обработка цены вечерней сессии для фьючерса
     */
    private void processFutureEveningSessionPrice(FutureEntity future, LocalDate date, 
            AtomicInteger newItemsSaved, AtomicInteger existingItemsSkipped, 
            AtomicInteger invalidItemsFiltered, AtomicInteger missingFromApi, 
            List<Map<String, Object>> savedItems, String taskId) {
        try {
            // Получаем последнюю свечу за день для фьючерса (блокирующий запрос)
            var lastCandle = minuteCandleRepository.findLastCandleForDate(future.getFigi(), date);
            
            if (lastCandle == null) {
                missingFromApi.incrementAndGet();
                return;
            }
            
            BigDecimal lastClosePrice = lastCandle.getClose();
            
            // Проверяем, есть ли уже запись для этой даты и FIGI
            if (closePriceEveningSessionRepository.existsByPriceDateAndFigi(date, future.getFigi())) {
                existingItemsSkipped.incrementAndGet();
                return;
            }
            
            // Проверяем, что цена не равна 0 (невалидная цена)
            if (lastClosePrice.compareTo(BigDecimal.ZERO) <= 0) {
                invalidItemsFiltered.incrementAndGet();
                return;
            }
            
            // Создаем запись для сохранения
            ClosePriceEveningSessionEntity entity = new ClosePriceEveningSessionEntity();
            entity.setFigi(future.getFigi());
            entity.setPriceDate(date);
            entity.setClosePrice(lastClosePrice);
            entity.setInstrumentType("FUTURE");
            entity.setCurrency("RUB");
            entity.setExchange("MOEX");
            
            closePriceEveningSessionRepository.save(entity);
            
            Map<String, Object> savedItem = new HashMap<>();
            savedItem.put("figi", future.getFigi());
            savedItem.put("ticker", future.getTicker());
            savedItem.put("name", future.getTicker());
            savedItem.put("priceDate", date.toString());
            savedItem.put("closePrice", lastClosePrice);
            savedItem.put("instrumentType", "FUTURE");
            savedItem.put("currency", "RUB");
            savedItem.put("exchange", "MOEX");
            
            savedItems.add(savedItem);
            newItemsSaved.incrementAndGet();
            
        } catch (Exception e) {
            logger.error("[" + taskId + "] Ошибка обработки фьючерса " + future.getTicker() + ": " + e.getMessage());
            missingFromApi.incrementAndGet();
        }
    }

}