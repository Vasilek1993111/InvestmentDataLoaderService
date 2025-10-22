package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.MinuteCandleRequestDto;
import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.entity.MinuteCandleEntity;
import com.example.InvestmentDataLoaderService.entity.SystemLogEntity;
import com.example.InvestmentDataLoaderService.repository.MinuteCandleRepository;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Сервис для загрузки минутных свечей
 */
@Service
public class MinuteCandleService {

    private static final Logger log = LoggerFactory.getLogger(MinuteCandleService.class);
    private final MinuteCandleRepository minuteCandleRepository;
    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final IndicativeRepository indicativeRepository;
    private final TinkoffApiClient tinkoffApiClient;
    private final SystemLogRepository systemLogRepository;
    private final Executor minuteCandleExecutor;
    private final Executor apiDataExecutor;
    private final Executor batchWriteExecutor;

    public MinuteCandleService(
            MinuteCandleRepository minuteCandleRepository,
            ShareRepository shareRepository,
            FutureRepository futureRepository,
            IndicativeRepository indicativeRepository,
            TinkoffApiClient tinkoffApiClient,
            SystemLogRepository systemLogRepository,
            @Qualifier("minuteCandleExecutor") Executor minuteCandleExecutor,
            @Qualifier("apiDataExecutor") Executor apiDataExecutor,
            @Qualifier("batchWriteExecutor") Executor batchWriteExecutor) {
        this.minuteCandleRepository = minuteCandleRepository;
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.indicativeRepository = indicativeRepository;
        this.tinkoffApiClient = tinkoffApiClient;
        this.systemLogRepository = systemLogRepository;
        this.minuteCandleExecutor = minuteCandleExecutor;
        this.apiDataExecutor = apiDataExecutor;
        this.batchWriteExecutor = batchWriteExecutor;
    }

    /**
     * Загрузка минутных свечей
     */
    public CompletableFuture<SaveResponseDto> saveMinuteCandlesAsync(MinuteCandleRequestDto request, String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("=== НАЧАЛО ЗАГРУЗКИ МИНУТНЫХ СВЕЧЕЙ ===");
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

                log.info("Загружаем минутные свечи для {} инструментов", instrumentIds.size());
                log.info("Дата: {}", finalDate);

                // Счетчики для статистики
                AtomicInteger totalRequested = new AtomicInteger(0);
                AtomicInteger newItemsSaved = new AtomicInteger(0);
                AtomicInteger existingItemsSkipped = new AtomicInteger(0);
                AtomicInteger invalidItemsFiltered = new AtomicInteger(0);
                AtomicInteger missingFromApi = new AtomicInteger(0);
                List<String> savedItems = Collections.synchronizedList(new ArrayList<>());

                // Разбиваем инструменты на батчи для обработки
                int batchSize = Math.max(1, instrumentIds.size() / 10); // 10 батчей максимум
                List<List<String>> batches = partitionList(instrumentIds, batchSize);

                log.info("Обрабатываем {} батчей по {} инструментов", batches.size(), batchSize);

                // Создаем задачи для каждого батча
                List<CompletableFuture<Void>> batchTasks = batches.stream()
                    .map(batch -> processBatchAsync(batch, finalDate, taskId, totalRequested, 
                        newItemsSaved, existingItemsSkipped, invalidItemsFiltered, 
                        missingFromApi, savedItems))
                    .collect(Collectors.toList());

                // Ждем завершения всех батчей
                CompletableFuture<Void> allBatches = CompletableFuture.allOf(
                    batchTasks.toArray(new CompletableFuture[0]));

                allBatches.join(); // Ждем завершения всех задач

                log.info("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ МИНУТНЫХ СВЕЧЕЙ ===");
                log.info("Всего запрошено: {}", totalRequested.get());
                log.info("Новых сохранено: {}", newItemsSaved.get());
                log.info("Пропущено существующих: {}", existingItemsSkipped.get());
                log.info("Отфильтровано неверных: {}", invalidItemsFiltered.get());
                log.info("Отсутствует в API: {}", missingFromApi.get());

                return new SaveResponseDto(
                    true,
                    "Загрузка минутных свечей завершена успешно",
                    totalRequested.get(),
                    newItemsSaved.get(),
                    existingItemsSkipped.get(),
                    invalidItemsFiltered.get(),
                    missingFromApi.get(),
                    savedItems
                );

            } catch (Exception e) {
                log.error("Критическая ошибка загрузки минутных свечей: {}", e.getMessage(), e);
                return new SaveResponseDto(
                    false,
                    "Ошибка загрузки минутных свечей: " + e.getMessage(),
                    0, 0, 0, 0, 0, new ArrayList<>()
                );
            }
        }, minuteCandleExecutor);
    }

    /**
     * Обрабатывает батч инструментов
     */
    private CompletableFuture<Void> processBatchAsync(List<String> batch, LocalDate date, String taskId,
                                                     AtomicInteger totalRequested, AtomicInteger newItemsSaved,
                                                     AtomicInteger existingItemsSkipped, AtomicInteger invalidItemsFiltered,
                                                     AtomicInteger missingFromApi, List<String> savedItems) {
        return CompletableFuture.runAsync(() -> {
            log.info("Обрабатываем батч из {} инструментов", batch.size());
            
            // Создаем задачи для каждого инструмента в батче
            List<CompletableFuture<Void>> instrumentTasks = batch.stream()
                .map(figi -> processInstrumentAsync(figi, date, taskId, totalRequested, 
                    newItemsSaved, existingItemsSkipped, invalidItemsFiltered, 
                    missingFromApi, savedItems))
                .collect(Collectors.toList());

            // Ждем завершения всех инструментов в батче
            CompletableFuture.allOf(instrumentTasks.toArray(new CompletableFuture[0])).join();
            
            log.info("Батч из {} инструментов обработан", batch.size());
        }, minuteCandleExecutor);
    }

    /**
     * Обрабатывает один инструмент асинхронно
     */
    private CompletableFuture<Void> processInstrumentAsync(String figi, LocalDate date, String taskId,
                                                          AtomicInteger totalRequested, AtomicInteger newItemsSaved,
                                                          AtomicInteger existingItemsSkipped, AtomicInteger invalidItemsFiltered,
                                                          AtomicInteger missingFromApi, List<String> savedItems) {
        return CompletableFuture.runAsync(() -> {
            AtomicInteger figiNewItems = new AtomicInteger(0);
            AtomicInteger figiExistingItems = new AtomicInteger(0);
            AtomicInteger figiInvalidItems = new AtomicInteger(0);
            Instant figiStartTime = Instant.now();
            
            try {
                log.info("Обрабатываем инструмент: {}", figi);
                
                // Получаем минутные свечи из API асинхронно
                CompletableFuture<List<com.example.InvestmentDataLoaderService.dto.CandleDto>> apiTask = 
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return tinkoffApiClient.getCandles(figi, date, "CANDLE_INTERVAL_1_MIN");
                        } catch (Exception e) {
                            log.error("Ошибка получения данных из API для {}: {}", figi, e.getMessage(), e);
                            return null;
                        }
                    }, apiDataExecutor);

                List<com.example.InvestmentDataLoaderService.dto.CandleDto> candles = apiTask.get();
                
                if (candles == null || candles.isEmpty()) {
                    log.info("Нет данных для инструмента: {}", figi);
                    missingFromApi.incrementAndGet();
                    
                    // Логируем отсутствие данных для FIGI
                    logFigiProcessing(taskId, figi, "NO_DATA", "Нет данных в API для инструмента " + figi, 
                        figiStartTime, 0, 0, 0, 0);
                    return;
                }

                totalRequested.addAndGet(candles.size());
                log.info("Получено {} минутных свечей для {}", candles.size(), figi);

                // Сохраняем свечи в БД пакетно
                List<MinuteCandleEntity> entitiesToSave = new ArrayList<>();
                List<String> existingTimes = new ArrayList<>();
                
                for (var candle : candles) {
                    try {
                        // Фильтруем незакрытые свечи (is_complete=false)
                        if (!candle.isComplete()) {
                            log.info("Пропускаем незакрытую свечу для {} в {}", figi, candle.time());
                            invalidItemsFiltered.incrementAndGet();
                            continue;
                        }
                        
                        MinuteCandleEntity entity = convertToEntity(candle, figi);
                        
                        // Проверяем, существует ли уже такая свеча
                        if (!minuteCandleRepository.existsByFigiAndTime(figi, entity.getTime())) {
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

                // Пакетная запись в БД
                if (!entitiesToSave.isEmpty()) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            saveMinuteCandlesBatch(entitiesToSave);
                            figiNewItems.addAndGet(entitiesToSave.size());
                            newItemsSaved.addAndGet(entitiesToSave.size());
                            log.info("Сохранено {} новых свечей для {}", entitiesToSave.size(), figi);
                        } catch (Exception e) {
                            log.error("Ошибка пакетного сохранения для {}: {}", figi, e.getMessage(), e);
                        }
                    }, batchWriteExecutor).join();
                }

                figiExistingItems.addAndGet(existingTimes.size());
                existingItemsSkipped.addAndGet(existingTimes.size());

                // Логируем успешную обработку FIGI
                logFigiProcessing(taskId, figi, "SUCCESS", 
                    "Успешно обработан инструмент " + figi + ". Получено " + candles.size() + " свечей", 
                    figiStartTime, candles.size(), figiNewItems.get(), figiExistingItems.get(), figiInvalidItems.get());

            } catch (Exception e) {
                log.error("Ошибка обработки инструмента {}: {}", figi, e.getMessage(), e);
                invalidItemsFiltered.incrementAndGet();
                
                // Логируем ошибку обработки FIGI
                logFigiProcessing(taskId, figi, "ERROR", 
                    "Ошибка обработки инструмента " + figi + ": " + e.getMessage(), 
                    figiStartTime, 0, 0, 0, 0);
            }
        }, minuteCandleExecutor);
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
    private MinuteCandleEntity convertToEntity(com.example.InvestmentDataLoaderService.dto.CandleDto candle, String figi) {
        return new MinuteCandleEntity(
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
     * Транзакционное пакетное сохранение минутных свечей
     */
    @Transactional
    public void saveMinuteCandlesBatch(List<MinuteCandleEntity> entities) {
        minuteCandleRepository.saveAll(entities);
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
            figiLog.setEndpoint("/api/candles/minute/FIGI_PROCESSING");
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