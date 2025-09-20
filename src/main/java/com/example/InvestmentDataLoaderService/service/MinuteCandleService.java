package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.MinuteCandleRequestDto;
import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.entity.MinuteCandleEntity;
import com.example.InvestmentDataLoaderService.repository.MinuteCandleRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.IndicativeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис для загрузки минутных свечей
 */
@Service
public class MinuteCandleService {

    private final MinuteCandleRepository minuteCandleRepository;
    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final IndicativeRepository indicativeRepository;
    private final MarketDataService marketDataService;

    public MinuteCandleService(MinuteCandleRepository minuteCandleRepository,
                              ShareRepository shareRepository,
                              FutureRepository futureRepository,
                              IndicativeRepository indicativeRepository,
                              MarketDataService marketDataService) {
        this.minuteCandleRepository = minuteCandleRepository;
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.indicativeRepository = indicativeRepository;
        this.marketDataService = marketDataService;
    }

    /**
     * Асинхронная загрузка минутных свечей
     */
    public CompletableFuture<SaveResponseDto> saveMinuteCandlesAsync(MinuteCandleRequestDto request, String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("=== НАЧАЛО ЗАГРУЗКИ МИНУТНЫХ СВЕЧЕЙ ===");
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

                System.out.println("Загружаем минутные свечи для " + instrumentIds.size() + " инструментов");
                System.out.println("Дата: " + date);

                int totalRequested = 0;
                int newItemsSaved = 0;
                int existingItemsSkipped = 0;
                int invalidItemsFiltered = 0;
                int missingFromApi = 0;
                List<String> savedItems = new ArrayList<>();

                for (String figi : instrumentIds) {
                    try {
                        System.out.println("Обрабатываем инструмент: " + figi);
                        
                        // Получаем минутные свечи из API
                        var candles = marketDataService.getCandles(figi, date, "CANDLE_INTERVAL_1_MIN");
                        
                        if (candles == null || candles.isEmpty()) {
                            System.out.println("Нет данных для инструмента: " + figi);
                            missingFromApi++;
                            continue;
                        }

                        totalRequested += candles.size();
                        System.out.println("Получено " + candles.size() + " минутных свечей для " + figi);

                        // Сохраняем свечи в БД
                        for (var candle : candles) {
                            try {
                                MinuteCandleEntity entity = convertToEntity(candle, figi);
                                
                                // Проверяем, существует ли уже такая свеча
                                if (!minuteCandleRepository.existsByFigiAndTime(figi, entity.getTime())) {
                                    minuteCandleRepository.save(entity);
                                    newItemsSaved++;
                                    savedItems.add(figi + ":" + entity.getTime());
                                } else {
                                    existingItemsSkipped++;
                                }
                            } catch (Exception e) {
                                System.err.println("Ошибка сохранения свечи для " + figi + ": " + e.getMessage());
                                invalidItemsFiltered++;
                            }
                        }

                    } catch (Exception e) {
                        System.err.println("Ошибка обработки инструмента " + figi + ": " + e.getMessage());
                        invalidItemsFiltered++;
                    }
                }

                System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ МИНУТНЫХ СВЕЧЕЙ ===");
                System.out.println("Всего запрошено: " + totalRequested);
                System.out.println("Новых сохранено: " + newItemsSaved);
                System.out.println("Пропущено существующих: " + existingItemsSkipped);
                System.out.println("Отфильтровано неверных: " + invalidItemsFiltered);
                System.out.println("Отсутствует в API: " + missingFromApi);

                return new SaveResponseDto(
                    true,
                    "Загрузка минутных свечей завершена успешно",
                    totalRequested,
                    newItemsSaved,
                    existingItemsSkipped,
                    invalidItemsFiltered,
                    missingFromApi,
                    savedItems
                );

            } catch (Exception e) {
                System.err.println("Критическая ошибка загрузки минутных свечей: " + e.getMessage());
                e.printStackTrace();
                return new SaveResponseDto(
                    false,
                    "Ошибка загрузки минутных свечей: " + e.getMessage(),
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
}
