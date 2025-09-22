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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис для загрузки дневных свечей
 */
@Service
public class DailyCandleService {

    private final DailyCandleRepository dailyCandleRepository;
    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final IndicativeRepository indicativeRepository;
    private final MarketDataService marketDataService;
    private final SystemLogRepository systemLogRepository;

    public DailyCandleService(DailyCandleRepository dailyCandleRepository,
                             ShareRepository shareRepository,
                             FutureRepository futureRepository,
                             IndicativeRepository indicativeRepository,
                             MarketDataService marketDataService,
                             SystemLogRepository systemLogRepository) {
        this.dailyCandleRepository = dailyCandleRepository;
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.indicativeRepository = indicativeRepository;
        this.marketDataService = marketDataService;
        this.systemLogRepository = systemLogRepository;
    }

    /**
     * Асинхронная загрузка дневных свечей
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

                // Если инструменты не указаны, получаем все инструменты из БД
                if (instrumentIds == null || instrumentIds.isEmpty()) {
                    instrumentIds = getAllInstrumentIds(assetTypes);
                }

                System.out.println("Загружаем дневные свечи для " + instrumentIds.size() + " инструментов");
                System.out.println("Дата: " + date);

                int totalRequested = 0;
                int newItemsSaved = 0;
                int existingItemsSkipped = 0;
                int invalidItemsFiltered = 0;
                int missingFromApi = 0;
                List<String> savedItems = new ArrayList<>();

                for (String figi : instrumentIds) {
                    int figiNewItems = 0;
                    int figiExistingItems = 0;
                    int figiInvalidItems = 0;
                    Instant figiStartTime = Instant.now();
                    
                    try {
                        System.out.println("Обрабатываем инструмент: " + figi);
                        
                        // Получаем дневные свечи из API
                        var candles = marketDataService.getCandles(figi, date, "CANDLE_INTERVAL_DAY");
                        
                        if (candles == null || candles.isEmpty()) {
                            System.out.println("Нет данных для инструмента: " + figi);
                            missingFromApi++;
                            
                            // Логируем отсутствие данных для FIGI
                            logFigiProcessing(taskId, figi, "NO_DATA", "Нет данных в API для инструмента " + figi, 
                                figiStartTime, 0, 0, 0, 0);
                            continue;
                        }

                        totalRequested += candles.size();
                        System.out.println("Получено " + candles.size() + " дневных свечей для " + figi);

                        // Сохраняем свечи в БД
                        for (var candle : candles) {
                            try {
                                DailyCandleEntity entity = convertToEntity(candle, figi);
                                
                                // Проверяем, существует ли уже такая свеча
                                if (!dailyCandleRepository.existsByFigiAndTime(figi, entity.getTime())) {
                                    dailyCandleRepository.save(entity);
                                    figiNewItems++;
                                    newItemsSaved++;
                                    savedItems.add(figi + ":" + entity.getTime());
                                } else {
                                    figiExistingItems++;
                                    existingItemsSkipped++;
                                }
                            } catch (Exception e) {
                                System.err.println("Ошибка сохранения свечи для " + figi + ": " + e.getMessage());
                                figiInvalidItems++;
                                invalidItemsFiltered++;
                            }
                        }

                        // Логируем успешную обработку FIGI
                        logFigiProcessing(taskId, figi, "SUCCESS", 
                            "Успешно обработан инструмент " + figi + ". Получено " + candles.size() + " свечей", 
                            figiStartTime, candles.size(), figiNewItems, figiExistingItems, figiInvalidItems);

                    } catch (Exception e) {
                        System.err.println("Ошибка обработки инструмента " + figi + ": " + e.getMessage());
                        invalidItemsFiltered++;
                        
                        // Логируем ошибку обработки FIGI
                        logFigiProcessing(taskId, figi, "ERROR", 
                            "Ошибка обработки инструмента " + figi + ": " + e.getMessage(), 
                            figiStartTime, 0, 0, 0, 0);
                    }
                }

                System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ДНЕВНЫХ СВЕЧЕЙ ===");
                System.out.println("Всего запрошено: " + totalRequested);
                System.out.println("Новых сохранено: " + newItemsSaved);
                System.out.println("Пропущено существующих: " + existingItemsSkipped);
                System.out.println("Отфильтровано неверных: " + invalidItemsFiltered);
                System.out.println("Отсутствует в API: " + missingFromApi);

                String message = String.format(
                    "Загрузка дневных свечей завершена: запрошено=%d, новых=%d, существующих=%d, невалидных=%d, отсутствующих=%d",
                    totalRequested, newItemsSaved, existingItemsSkipped, invalidItemsFiltered, missingFromApi
                );
                
                return new SaveResponseDto(
                    true,
                    message,
                    totalRequested,
                    newItemsSaved,
                    existingItemsSkipped,
                    invalidItemsFiltered,
                    missingFromApi,
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
        });
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
