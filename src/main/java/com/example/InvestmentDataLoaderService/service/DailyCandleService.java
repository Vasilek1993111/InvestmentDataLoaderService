package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.DailyCandleRequestDto;
import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.entity.DailyCandleEntity;
import com.example.InvestmentDataLoaderService.entity.SystemLogEntity;
import com.example.InvestmentDataLoaderService.repository.DailyCandleRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.IndicativeRepository;
import com.example.InvestmentDataLoaderService.repository.SystemLogRepository;
import com.example.InvestmentDataLoaderService.client.TinkoffApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Сервис для загрузки дневных свечей
 */
@Service
public class DailyCandleService {

    private static final Logger log = LoggerFactory.getLogger(DailyCandleService.class);
    private final DailyCandleRepository dailyCandleRepository;
    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final IndicativeRepository indicativeRepository;
    private final TinkoffApiClient tinkoffApiClient;
    private final SystemLogRepository systemLogRepository;
    private final Executor dailyCandleExecutor;
    private final Executor dailyApiDataExecutor;
    private final Executor dailyBatchWriteExecutor;

    public DailyCandleService(
            DailyCandleRepository dailyCandleRepository,
            ShareRepository shareRepository,
            FutureRepository futureRepository,
            IndicativeRepository indicativeRepository,
            TinkoffApiClient tinkoffApiClient,
            SystemLogRepository systemLogRepository,
            @Qualifier("dailyCandleExecutor") Executor dailyCandleExecutor,
            @Qualifier("dailyApiDataExecutor") Executor dailyApiDataExecutor,
            @Qualifier("dailyBatchWriteExecutor") Executor dailyBatchWriteExecutor) {
        this.dailyCandleRepository = dailyCandleRepository;
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.indicativeRepository = indicativeRepository;
        this.tinkoffApiClient = tinkoffApiClient;
        this.systemLogRepository = systemLogRepository;
        this.dailyCandleExecutor = dailyCandleExecutor;
        this.dailyApiDataExecutor = dailyApiDataExecutor;
        this.dailyBatchWriteExecutor = dailyBatchWriteExecutor;
    }

    /**
     * Загрузка дневных свечей
     */
    public CompletableFuture<SaveResponseDto> saveDailyCandlesAsync(DailyCandleRequestDto request, String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("=== НАЧАЛО ЗАГРУЗКИ ДНЕВНЫХ СВЕЧЕЙ ===");
                log.info("Task ID: {}", taskId);
                log.info("Request: {}", request);

                List<String> instrumentIds = request.getInstruments();
                LocalDate date = request.getDate();
                List<String> assetTypes = request.getAssetType();

                // Если дата не указана, используем сегодняшний день
                if (date == null) {
                    date = LocalDate.now(ZoneId.of("Europe/Moscow"));
                }
                
                final LocalDate finalDate = date;

                // Если инструменты не указаны, получаем все инструменты из БД
                if (instrumentIds == null || instrumentIds.isEmpty()) {
                    instrumentIds = getAllInstrumentIds(assetTypes);
                }

                log.info("Загружаем дневные свечи для {} инструментов", instrumentIds.size());
                log.info("Дата: {}", finalDate);

                // Счетчики для статистики
                AtomicInteger totalRequested = new AtomicInteger(0);
                AtomicInteger newItemsSaved = new AtomicInteger(0);
                AtomicInteger existingItemsSkipped = new AtomicInteger(0);
                AtomicInteger invalidItemsFiltered = new AtomicInteger(0);
                AtomicInteger missingFromApi = new AtomicInteger(0);
                List<String> savedItems = Collections.synchronizedList(new ArrayList<>());

                // Разбиваем инструменты на батчи для обработки
                int batchSize = Math.max(1, instrumentIds.size() / 8); // 8 батчей максимум для дневных свечей
                List<List<String>> batches = partitionList(instrumentIds, batchSize);

                log.info("Обрабатываем {} батчей по {} инструментов", batches.size(), batchSize);

                // Создаем задачи для каждого батча
                List<CompletableFuture<Void>> batchTasks = batches.stream()
                    .map(batch -> processBatchAsync(batch, finalDate, taskId, totalRequested, 
                        newItemsSaved, existingItemsSkipped, invalidItemsFiltered, 
                        missingFromApi, savedItems))
                    .collect(Collectors.toList());

                // Ждем завершения всех батчей с таймаутом
                CompletableFuture<Void> allBatches = CompletableFuture.allOf(
                    batchTasks.toArray(new CompletableFuture[0]));

                try {
                    // Таймаут 2 часа для загрузки всех свечей
                    allBatches.get(2, TimeUnit.HOURS);
                } catch (TimeoutException e) {
                    log.error("Превышен таймаут ожидания завершения загрузки дневных свечей (2 часа)");
                    // Продолжаем работу, чтобы вернуть статистику по обработанным данным
                } catch (Exception e) {
                    log.error("Ошибка ожидания завершения загрузки дневных свечей: {}", e.getMessage(), e);
                }

                log.info("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ДНЕВНЫХ СВЕЧЕЙ ===");
                log.info("Всего запрошено: {}", totalRequested.get());
                log.info("Новых сохранено: {}", newItemsSaved.get());
                log.info("Пропущено существующих: {}", existingItemsSkipped.get());
                log.info("Отфильтровано неверных: {}", invalidItemsFiltered.get());
                log.info("Отсутствует в API: {}", missingFromApi.get());

                return new SaveResponseDto(
                    true,
                    "Загрузка дневных свечей завершена успешно",
                    totalRequested.get(),
                    newItemsSaved.get(),
                    existingItemsSkipped.get(),
                    invalidItemsFiltered.get(),
                    missingFromApi.get(),
                    savedItems
                );

            } catch (Exception e) {
                log.error("Критическая ошибка загрузки дневных свечей: {}", e.getMessage(), e);
                return new SaveResponseDto(
                    false,
                    "Ошибка загрузки дневных свечей: " + e.getMessage(),
                    0, 0, 0, 0, 0, new ArrayList<>()
                );
            }
        }, dailyCandleExecutor);
    }

    /**
     * Обрабатывает батч инструментов
     */
    private CompletableFuture<Void> processBatchAsync(List<String> batch, LocalDate date, String taskId,
                                                     AtomicInteger totalRequested, AtomicInteger newItemsSaved,
                                                     AtomicInteger existingItemsSkipped, AtomicInteger invalidItemsFiltered,
                                                     AtomicInteger missingFromApi, List<String> savedItems) {
        // Создаем задачи для каждого инструмента в батче напрямую через dailyApiDataExecutor
        // чтобы избежать вложенности executor'ов и блокировок
        List<CompletableFuture<Void>> instrumentTasks = batch.stream()
            .map(figi -> processInstrumentAsync(figi, date, taskId, totalRequested, 
                newItemsSaved, existingItemsSkipped, invalidItemsFiltered, 
                missingFromApi, savedItems))
            .collect(Collectors.toList());

        // Возвращаем CompletableFuture, который завершится когда все инструменты обработаны
        // Не блокируем поток executor'а
        return CompletableFuture.allOf(instrumentTasks.toArray(new CompletableFuture[0]))
            .thenRun(() -> log.info("Батч из {} инструментов обработан", batch.size()));
    }

    /**
     * Обрабатывает один инструмент асинхронно
     * Использует dailyApiDataExecutor напрямую для избежания вложенности executor'ов
     */
    private CompletableFuture<Void> processInstrumentAsync(String figi, LocalDate date, String taskId,
                                                          AtomicInteger totalRequested, AtomicInteger newItemsSaved,
                                                          AtomicInteger existingItemsSkipped, AtomicInteger invalidItemsFiltered,
                                                          AtomicInteger missingFromApi, List<String> savedItems) {
        // Используем dailyApiDataExecutor напрямую, чтобы избежать вложенности и блокировок
        return CompletableFuture.supplyAsync(() -> {
            AtomicInteger figiNewItems = new AtomicInteger(0);
            AtomicInteger figiExistingItems = new AtomicInteger(0);
            AtomicInteger figiInvalidItems = new AtomicInteger(0);
            Instant figiStartTime = Instant.now();
            
            try {
                log.info("Обрабатываем инструмент: {}", figi);
                
                // Получаем дневные свечи из API синхронно (executor уже обрабатывает параллелизм)
                List<com.example.InvestmentDataLoaderService.dto.CandleDto> candles;
                try {
                    candles = tinkoffApiClient.getCandles(figi, date, "CANDLE_INTERVAL_DAY");
                } catch (Exception e) {
                    log.error("Ошибка получения данных из API для {}: {}", figi, e.getMessage(), e);
                    candles = null;
                }
                
                if (candles == null || candles.isEmpty()) {
                    log.info("Нет данных для инструмента: {}", figi);
                    missingFromApi.incrementAndGet();
                    
                    // Логируем отсутствие данных для FIGI
                    logFigiProcessing(taskId, figi, "NO_DATA", "Нет данных в API для инструмента " + figi, 
                        figiStartTime, 0, 0, 0, 0);
                    return null;
                }

                totalRequested.addAndGet(candles.size());
                log.info("Получено {} дневных свечей для {}", candles.size(), figi);

                // Сохраняем свечи в БД пакетно
                List<DailyCandleEntity> entitiesToSave = new ArrayList<>();
                List<String> existingTimes = new ArrayList<>();
                
                for (var candle : candles) {
                    try {
                        // Фильтруем незакрытые свечи (is_complete=false)
                        if (!candle.isComplete()) {
                            log.debug("Пропускаем незакрытую свечу для {} в {}", figi, candle.time());
                            invalidItemsFiltered.incrementAndGet();
                            continue;
                        }
                        
                        DailyCandleEntity entity = convertToEntity(candle, figi);
                        
                        // Проверяем, существует ли уже такая свеча
                        if (!dailyCandleRepository.existsByFigiAndTime(figi, entity.getTime())) {
                            entitiesToSave.add(entity);
                            savedItems.add(figi + ":" + entity.getTime());
                        } else {
                            existingTimes.add(entity.getTime().toString());
                        }
                    } catch (Exception e) {
                        log.error("Ошибка конвертации свечи для {}: {}", figi, e.getMessage(), e);
                        figiInvalidItems.incrementAndGet();
                        invalidItemsFiltered.incrementAndGet();
                    }
                }

                // Пакетная запись в БД асинхронно (не блокируем текущий поток)
                if (!entitiesToSave.isEmpty()) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            saveDailyCandlesBatch(entitiesToSave);
                            figiNewItems.addAndGet(entitiesToSave.size());
                            newItemsSaved.addAndGet(entitiesToSave.size());
                            log.info("Сохранено {} новых дневных свечей для {}", entitiesToSave.size(), figi);
                        } catch (Exception e) {
                            log.error("Ошибка пакетного сохранения для {}: {}", figi, e.getMessage(), e);
                        }
                    }, dailyBatchWriteExecutor);
                    // Не ждем завершения сохранения - оно произойдет асинхронно
                }

                figiExistingItems.addAndGet(existingTimes.size());
                existingItemsSkipped.addAndGet(existingTimes.size());

                // Логируем успешную обработку FIGI
                logFigiProcessing(taskId, figi, "SUCCESS", 
                    "Успешно обработан инструмент " + figi + ". Получено " + candles.size() + " свечей", 
                    figiStartTime, candles.size(), figiNewItems.get(), figiExistingItems.get(), figiInvalidItems.get());

                // Убрана задержка - она уже есть в TinkoffApiClient (300ms перед каждым запросом)
                return null;

            } catch (Exception e) {
                log.error("Ошибка обработки инструмента {}: {}", figi, e.getMessage(), e);
                invalidItemsFiltered.incrementAndGet();
                
                // Логируем ошибку обработки FIGI
                logFigiProcessing(taskId, figi, "ERROR", 
                    "Ошибка обработки инструмента " + figi + ": " + e.getMessage(), 
                    figiStartTime, 0, 0, 0, 0);
                return null;
            }
        }, dailyApiDataExecutor).thenRun(() -> {}); // Преобразуем в CompletableFuture<Void>
    }

    /**
     * Получает все ID инструментов по типам активов
     */
    @Transactional(readOnly = true)
    private List<String> getAllInstrumentIds(List<String> assetTypes) {
        List<String> allIds = new ArrayList<>();
        
        if (assetTypes == null || assetTypes.isEmpty() || assetTypes.contains("SHARES")) {
            allIds.addAll(shareRepository.findAll().stream()
                .map(share -> share.getFigi())
                .toList());
        }
        
        if (assetTypes == null || assetTypes.isEmpty() || assetTypes.contains("FUTURES")) {
            allIds.addAll(futureRepository.findAll().stream()
                .map(future -> future.getFigi())
                .toList());
        }
        
        if (assetTypes == null || assetTypes.isEmpty() || assetTypes.contains("INDICATIVES")) {
            allIds.addAll(indicativeRepository.findAll().stream()
                .map(indicative -> indicative.getFigi())
                .toList());
        }
        
        return allIds;
    }

    /**
     * Конвертирует DTO свечи в Entity
     */
    private DailyCandleEntity convertToEntity(com.example.InvestmentDataLoaderService.dto.CandleDto candle, String figi) {
        return new DailyCandleEntity(
            figi,
            candle.volume(),
            candle.high(),
            candle.low(),
            candle.time(),
            candle.close(),
            candle.open(),
            candle.isComplete()
        );
    }


    /**
     * Разбивает список на части указанного размера
     */
    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return partitions;
    }

    /**
     * Транзакционное пакетное сохранение дневных свечей
     */
    @Transactional
    public void saveDailyCandlesBatch(List<DailyCandleEntity> entities) {
        dailyCandleRepository.saveAll(entities);
    }

    /**
     * Логирует обработку конкретного FIGI в system_logs
     */
    @Transactional
    private void logFigiProcessing(String taskId, String figi, String status, String message, 
                                 Instant startTime, int totalCandles, int newItems, int existingItems, int invalidItems) {
        try {
            SystemLogEntity figiLog = new SystemLogEntity();
            figiLog.setTaskId(taskId);
            figiLog.setEndpoint("/api/candles/daily/FIGI_PROCESSING");
            figiLog.setMethod("POST");
            figiLog.setStatus(status);
            figiLog.setMessage(message + 
                (totalCandles > 0 ? " | Всего свечей: " + totalCandles : "") +
                (newItems > 0 ? " | Новых: " + newItems : "") +
                (existingItems > 0 ? " | Существующих: " + existingItems : "") +
                (invalidItems > 0 ? " | Неверных: " + invalidItems : "") +
                " | FIGI: " + figi);
            figiLog.setStartTime(startTime);
            figiLog.setEndTime(Instant.now());
            figiLog.setDurationMs(Instant.now().toEpochMilli() - startTime.toEpochMilli());
            
            systemLogRepository.save(figiLog);
            log.info("Лог обработки FIGI сохранен: {} ({})", figi, status);
            
        } catch (Exception e) {
            log.error("Ошибка сохранения лога обработки FIGI {}: {}", figi, e.getMessage(), e);
        }
    }
}