package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.AggregationResult;
import com.example.InvestmentDataLoaderService.entity.SharesAggregatedDataEntity;
import com.example.InvestmentDataLoaderService.repository.CandleRepository;
import com.example.InvestmentDataLoaderService.repository.SharesAggregatedDataRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис агрегации данных для акций
 * Рассчитывает средние объемы торгов в утренние часы и выходные дни
 */
@Service
public class OptimizedAggregationService {
    
    private final CandleRepository candleRepository;
    private final SharesAggregatedDataRepository sharesAggregatedDataRepository;
    private final ShareRepository shareRepository;
    
    public OptimizedAggregationService(CandleRepository candleRepository,
                                     SharesAggregatedDataRepository sharesAggregatedDataRepository,
                                     ShareRepository shareRepository) {
        this.candleRepository = candleRepository;
        this.sharesAggregatedDataRepository = sharesAggregatedDataRepository;
        this.shareRepository = shareRepository;
    }
    
    
    /**
     * Асинхронный пересчет агрегированных данных для всех акций
     */
    @Async("aggregationTaskExecutor")
    public CompletableFuture<AggregationResult> recalculateAllSharesDataAsync() {
        // Создаем новый экземпляр сервиса для асинхронного контекста
        return CompletableFuture.supplyAsync(() -> {
            try {
                return recalculateAllSharesDataOptimized();
            } catch (Exception e) {
                AggregationResult errorResult = new AggregationResult("ERROR", "shares");
                errorResult.setSuccess(false);
                errorResult.setErrorMessage(e.getMessage());
                return errorResult;
            }
        });
    }
    
    /**
     * Пересчет агрегированных данных для всех акций (оптимизированная версия)
     */
    @Transactional
    public AggregationResult recalculateAllSharesDataOptimized() {
        String taskId = "SHARES_AGGREGATION_OPT_" + UUID.randomUUID().toString().substring(0, 8);
        
        System.out.println("=== НАЧАЛО ОПТИМИЗИРОВАННОГО ПЕРЕСЧЕТА АКЦИЙ ===");
        System.out.println("[" + taskId + "] Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
        
        AggregationResult result = new AggregationResult(taskId, "shares");
        
        try {
            // Получаем все FIGI акций
            List<String> allFigis = shareRepository.findAllFigis();
            System.out.println("[" + taskId + "] Найдено " + allFigis.size() + " акций для обработки");
            
            // Обрабатываем пакетами по 50 акций
            int batchSize = 50;
            for (int i = 0; i < allFigis.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, allFigis.size());
                List<String> batchFigis = allFigis.subList(i, endIndex);
                
                System.out.println("[" + taskId + "] Обработка пакета " + (i/batchSize + 1) + 
                                 "/" + ((allFigis.size() + batchSize - 1) / batchSize) + 
                                 " (акций: " + batchFigis.size() + ")");
                
                // Получаем агрегированные данные для пакета одним запросом
                List<Object[]> batchData = candleRepository.getAggregatedDataByFigis(batchFigis);
                
                // Группируем данные по FIGI
                Map<String, List<Object[]>> groupedData = new HashMap<>();
                for (Object[] row : batchData) {
                    String figi = (String) row[0];
                    groupedData.computeIfAbsent(figi, k -> new ArrayList<>()).add(row);
                }
                
                // Обрабатываем каждую акцию в пакете
                for (String figi : batchFigis) {
                    try {
                        result.setProcessedInstruments(result.getProcessedInstruments() + 1);
                        
                        List<Object[]> figiData = groupedData.get(figi);
                        if (figiData != null && !figiData.isEmpty()) {
                            // Создаем агрегированную сущность
                            SharesAggregatedDataEntity aggregated = new SharesAggregatedDataEntity(figi);
                            
                            // Рассчитываем средние значения
                            BigDecimal totalMorningVolume = BigDecimal.ZERO;
                            BigDecimal totalWeekendVolume = BigDecimal.ZERO;
                            int morningDays = 0;
                            int weekendDays = 0;
                            
                            for (Object[] row : figiData) {
                                LocalDate tradeDate = ((java.sql.Date) row[1]).toLocalDate();
                                BigDecimal morningVolume = (BigDecimal) row[2];
                                BigDecimal weekendVolume = (BigDecimal) row[3];
                                
                                if (isWeekend(tradeDate)) {
                                    if (weekendVolume.compareTo(BigDecimal.ZERO) > 0) {
                                        totalWeekendVolume = totalWeekendVolume.add(weekendVolume);
                                        weekendDays++;
                                    }
                                } else {
                                    if (morningVolume.compareTo(BigDecimal.ZERO) > 0) {
                                        totalMorningVolume = totalMorningVolume.add(morningVolume);
                                        morningDays++;
                                    }
                                }
                            }
                            
                            if (morningDays > 0) {
                                aggregated.setAvgVolumeMorning(totalMorningVolume.divide(BigDecimal.valueOf(morningDays), 2, RoundingMode.HALF_UP));
                            }
                            if (weekendDays > 0) {
                                aggregated.setAvgVolumeWeekend(totalWeekendVolume.divide(BigDecimal.valueOf(weekendDays), 2, RoundingMode.HALF_UP));
                            }
                            
                            aggregated.setTotalTradingDays(morningDays);
                            aggregated.setTotalWeekendDays(weekendDays);
                            
                            System.out.println("[" + taskId + "] Сохранение данных для " + figi + "...");
                            saveOrUpdateSharesAggregatedData(aggregated);
                            result.setSuccessfulInstruments(result.getSuccessfulInstruments() + 1);
                            
                            System.out.println("[" + taskId + "] ✓ Данные для " + figi + " рассчитаны и сохранены");
                        } else {
                            System.out.println("[" + taskId + "] ✗ Недостаточно данных для " + figi);
                        }
                        
                    } catch (Exception e) {
                        result.setErrorInstruments(result.getErrorInstruments() + 1);
                        System.err.println("[" + taskId + "] ✗ Ошибка обработки " + figi + ": " + e.getMessage());
                    }
                }
            }
            
            result.setSuccess(true);
            
            System.out.println("[" + taskId + "] === ОПТИМИЗИРОВАННЫЙ ПЕРЕСЧЕТ ЗАВЕРШЕН ===");
            System.out.println("[" + taskId + "] Обработано акций: " + result.getProcessedInstruments());
            System.out.println("[" + taskId + "] Успешно: " + result.getSuccessfulInstruments());
            System.out.println("[" + taskId + "] Ошибок: " + result.getErrorInstruments());
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return result;
    }

    /**
     * Пересчет агрегированных данных для всех акций (старая версия)
     */
    @Transactional
    public AggregationResult recalculateAllSharesData() {
        String taskId = "SHARES_AGGREGATION_SYNC_" + UUID.randomUUID().toString().substring(0, 8);
        
        System.out.println("=== НАЧАЛО СИНХРОННОГО ПЕРЕСЧЕТА АКЦИЙ ===");
        System.out.println("[" + taskId + "] Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
        
        AggregationResult result = new AggregationResult(taskId, "shares");
        
        try {
            // Получаем все FIGI акций
            List<String> allFigis = shareRepository.findAllFigis();
            System.out.println("[" + taskId + "] Найдено " + allFigis.size() + " акций для обработки");
            
            // Обрабатываем все акции
            for (String figi : allFigis) {
                try {
                    result.setProcessedInstruments(result.getProcessedInstruments() + 1);
                    
                    // Получаем данные для одной акции за последние 6 месяцев
                    List<Object[]> aggregatedData = candleRepository.getAggregatedDataByFigiAndDateRange(
                        figi, 
                        LocalDate.now().minusMonths(6).atStartOfDay(ZoneId.of("Europe/Moscow")).toInstant(),
                        LocalDate.now().atStartOfDay(ZoneId.of("Europe/Moscow")).toInstant()
                    );
                    
                    if (!aggregatedData.isEmpty()) {
                        // Создаем агрегированную сущность
                        SharesAggregatedDataEntity aggregated = new SharesAggregatedDataEntity(figi);
                        
                        // Рассчитываем средние значения
                        BigDecimal totalMorningVolume = BigDecimal.ZERO;
                        BigDecimal totalWeekendVolume = BigDecimal.ZERO;
                        int morningDays = 0;
                        int weekendDays = 0;
                        
                        for (Object[] row : aggregatedData) {
                            LocalDate tradeDate = ((java.sql.Date) row[0]).toLocalDate();
                            BigDecimal morningVolume = (BigDecimal) row[1];
                            BigDecimal weekendVolume = (BigDecimal) row[2];
                            
                            if (isWeekend(tradeDate)) {
                                if (weekendVolume.compareTo(BigDecimal.ZERO) > 0) {
                                    totalWeekendVolume = totalWeekendVolume.add(weekendVolume);
                                    weekendDays++;
                                }
                            } else {
                                if (morningVolume.compareTo(BigDecimal.ZERO) > 0) {
                                    totalMorningVolume = totalMorningVolume.add(morningVolume);
                                    morningDays++;
                                }
                            }
                        }
                        
                        if (morningDays > 0) {
                            aggregated.setAvgVolumeMorning(totalMorningVolume.divide(BigDecimal.valueOf(morningDays), 2, RoundingMode.HALF_UP));
                        }
                        if (weekendDays > 0) {
                            aggregated.setAvgVolumeWeekend(totalWeekendVolume.divide(BigDecimal.valueOf(weekendDays), 2, RoundingMode.HALF_UP));
                        }
                        
                        aggregated.setTotalTradingDays(morningDays);
                        aggregated.setTotalWeekendDays(weekendDays);
                        
                        System.out.println("[" + taskId + "] Сохранение данных для " + figi + "...");
                        saveOrUpdateSharesAggregatedData(aggregated);
                        result.setSuccessfulInstruments(result.getSuccessfulInstruments() + 1);
                        
                        System.out.println("[" + taskId + "] ✓ Данные для " + figi + " рассчитаны и сохранены");
                    } else {
                        System.out.println("[" + taskId + "] ✗ Недостаточно данных для " + figi);
                    }
                    
                } catch (Exception e) {
                    result.setErrorInstruments(result.getErrorInstruments() + 1);
                    System.err.println("[" + taskId + "] ✗ Ошибка обработки " + figi + ": " + e.getMessage());
                    System.err.println("[" + taskId + "] Stack trace: " + e.getClass().getSimpleName());
                    e.printStackTrace();
                }
            }
            
            result.setSuccess(true);
            
            System.out.println("[" + taskId + "] === СИНХРОННЫЙ ПЕРЕСЧЕТ ЗАВЕРШЕН ===");
            System.out.println("[" + taskId + "] Обработано акций: " + result.getProcessedInstruments());
            System.out.println("[" + taskId + "] Успешно: " + result.getSuccessfulInstruments());
            System.out.println("[" + taskId + "] Ошибок: " + result.getErrorInstruments());
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return result;
    }
    
    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
    
    
    /**
     * Сохраняет или обновляет агрегированные данные
     */
    @Transactional
    public void saveOrUpdateSharesAggregatedData(SharesAggregatedDataEntity data) {
        SharesAggregatedDataEntity existing = sharesAggregatedDataRepository.findByFigi(data.getFigi());
        
        if (existing != null) {
            existing.setAvgVolumeMorning(data.getAvgVolumeMorning());
            existing.setAvgVolumeWeekend(data.getAvgVolumeWeekend());
            existing.setTotalTradingDays(data.getTotalTradingDays());
            existing.setTotalWeekendDays(data.getTotalWeekendDays());
            existing.setLastCalculated(data.getLastCalculated());
            existing.setUpdatedAt(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            sharesAggregatedDataRepository.save(existing);
        } else {
            sharesAggregatedDataRepository.save(data);
        }
    }
    
}
