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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Сервис для загрузки дневных свечей
 */
@Service
public class DailyCandleService {

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
                System.out.println("=== НАЧАЛО ЗАГРУЗКИ ДНЕВНЫХ СВЕЧЕЙ ===");
                System.out.println("Task ID: " + taskId);
                System.out.println("Request: " + request);

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

                System.out.println("Загружаем дневные свечи для " + instrumentIds.size() + " инструментов");
                System.out.println("Дата: " + finalDate);

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

                System.out.println("Обрабатываем " + batches.size() + " батчей по " + batchSize + " инструментов");

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

                System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ДНЕВНЫХ СВЕЧЕЙ ===");
                System.out.println("Всего запрошено: " + totalRequested.get());
                System.out.println("Новых сохранено: " + newItemsSaved.get());
                System.out.println("Пропущено существующих: " + existingItemsSkipped.get());
                System.out.println("Отфильтровано неверных: " + invalidItemsFiltered.get());
                System.out.println("Отсутствует в API: " + missingFromApi.get());

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
                System.err.println("Критическая ошибка загрузки дневных свечей: " + e.getMessage());
                e.printStackTrace();
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
        return CompletableFuture.runAsync(() -> {
            System.out.println("Обрабатываем батч из " + batch.size() + " инструментов");
            
            // Создаем задачи для каждого инструмента в батче
            List<CompletableFuture<Void>> instrumentTasks = batch.stream()
                .map(figi -> processInstrumentAsync(figi, date, taskId, totalRequested, 
                    newItemsSaved, existingItemsSkipped, invalidItemsFiltered, 
                    missingFromApi, savedItems))
                .collect(Collectors.toList());

            // Ждем завершения всех инструментов в батче
            CompletableFuture.allOf(instrumentTasks.toArray(new CompletableFuture[0])).join();
            
            System.out.println("Батч из " + batch.size() + " инструментов обработан");
        }, dailyCandleExecutor);
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
                System.out.println("Обрабатываем инструмент: " + figi);
                
                // Получаем дневные свечи из API асинхронно
                CompletableFuture<List<com.example.InvestmentDataLoaderService.dto.CandleDto>> apiTask = 
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return tinkoffApiClient.getCandles(figi, date, "CANDLE_INTERVAL_DAY");
                        } catch (Exception e) {
                            System.err.println("Ошибка получения данных из API для " + figi + ": " + e.getMessage());
                            return null;
                        }
                    }, dailyApiDataExecutor);

                List<com.example.InvestmentDataLoaderService.dto.CandleDto> candles = apiTask.get();
                
                if (candles == null || candles.isEmpty()) {
                    System.out.println("Нет данных для инструмента: " + figi);
                    missingFromApi.incrementAndGet();
                    
                    // Логируем отсутствие данных для FIGI
                    logFigiProcessing(taskId, figi, "NO_DATA", "Нет данных в API для инструмента " + figi, 
                        figiStartTime, 0, 0, 0, 0);
                    return;
                }

                totalRequested.addAndGet(candles.size());
                System.out.println("Получено " + candles.size() + " дневных свечей для " + figi);

                // Сохраняем свечи в БД пакетно
                List<DailyCandleEntity> entitiesToSave = new ArrayList<>();
                List<String> existingTimes = new ArrayList<>();
                
                for (var candle : candles) {
                    try {
                        // Фильтруем незакрытые свечи (is_complete=false)
                        if (!candle.isComplete()) {
                            System.out.println("Пропускаем незакрытую свечу для " + figi + " в " + candle.time());
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
                        System.err.println("Ошибка конвертации свечи для " + figi + ": " + e.getMessage());
                        figiInvalidItems.incrementAndGet();
                        invalidItemsFiltered.incrementAndGet();
                    }
                }

                // Пакетная запись в БД
                if (!entitiesToSave.isEmpty()) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            dailyCandleRepository.saveAll(entitiesToSave);
                            figiNewItems.addAndGet(entitiesToSave.size());
                            newItemsSaved.addAndGet(entitiesToSave.size());
                            System.out.println("Сохранено " + entitiesToSave.size() + " новых дневных свечей для " + figi);
                        } catch (Exception e) {
                            System.err.println("Ошибка пакетного сохранения для " + figi + ": " + e.getMessage());
                        }
                    }, dailyBatchWriteExecutor).join();
                }

                figiExistingItems.addAndGet(existingTimes.size());
                existingItemsSkipped.addAndGet(existingTimes.size());

                // Логируем успешную обработку FIGI
                logFigiProcessing(taskId, figi, "SUCCESS", 
                    "Успешно обработан инструмент " + figi + ". Получено " + candles.size() + " свечей", 
                    figiStartTime, candles.size(), figiNewItems.get(), figiExistingItems.get(), figiInvalidItems.get());

            } catch (Exception e) {
                System.err.println("Ошибка обработки инструмента " + figi + ": " + e.getMessage());
                invalidItemsFiltered.incrementAndGet();
                
                // Логируем ошибку обработки FIGI
                logFigiProcessing(taskId, figi, "ERROR", 
                    "Ошибка обработки инструмента " + figi + ": " + e.getMessage(), 
                    figiStartTime, 0, 0, 0, 0);
            }
        }, dailyCandleExecutor);
    }

    /**
     * Получает все ID инструментов по типам активов
     */
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
     * Логирует обработку конкретного FIGI в system_logs
     */
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
            System.out.println("Лог обработки FIGI сохранен: " + figi + " (" + status + ")");
            
        } catch (Exception e) {
            System.err.println("Ошибка сохранения лога обработки FIGI " + figi + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}