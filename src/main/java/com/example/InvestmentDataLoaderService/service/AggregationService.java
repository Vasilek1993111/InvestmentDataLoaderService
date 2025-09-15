package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.AggregationResult;
import com.example.InvestmentDataLoaderService.entity.SharesAggregatedDataEntity;
import com.example.InvestmentDataLoaderService.entity.FuturesAggregatedDataEntity;
import com.example.InvestmentDataLoaderService.entity.CandleEntity;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.repository.CandleRepository;
import com.example.InvestmentDataLoaderService.repository.SharesAggregatedDataRepository;
import com.example.InvestmentDataLoaderService.repository.FuturesAggregatedDataRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Сервис агрегации данных для акций
 * Рассчитывает средние объемы торгов в утренние часы и выходные дни
 */
@Service
public class AggregationService {
    
    private final CandleRepository candleRepository;
    private final SharesAggregatedDataRepository sharesAggregatedDataRepository;
    private final FuturesAggregatedDataRepository futuresAggregatedDataRepository;
    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    
    public AggregationService(CandleRepository candleRepository,
                             SharesAggregatedDataRepository sharesAggregatedDataRepository,
                             FuturesAggregatedDataRepository futuresAggregatedDataRepository,
                             ShareRepository shareRepository,
                             FutureRepository futureRepository) {
        this.candleRepository = candleRepository;
        this.sharesAggregatedDataRepository = sharesAggregatedDataRepository;
        this.futuresAggregatedDataRepository = futuresAggregatedDataRepository;
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
    }
    
    
    /**
     * Асинхронный пересчет агрегированных данных для всех акций
     */
    @Async("aggregationTaskExecutor")
    public CompletableFuture<AggregationResult> recalculateAllSharesDataAsync() {
        // Создаем новый экземпляр сервиса для асинхронного контекста
        return CompletableFuture.supplyAsync(() -> {
            try {
                return recalculateAllSharesData();
            } catch (Exception e) {
                AggregationResult errorResult = new AggregationResult("ERROR", "shares");
                errorResult.setSuccess(false);
                errorResult.setErrorMessage(e.getMessage());
                return errorResult;
            }
        });
    }

    /**
     * Асинхронный пересчет агрегированных данных для всех фьючерсов
     */
    @Async("aggregationTaskExecutor")
    public CompletableFuture<AggregationResult> recalculateAllFuturesDataAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return recalculateAllFuturesData();
            } catch (Exception e) {
                AggregationResult errorResult = new AggregationResult("ERROR", "futures");
                errorResult.setSuccess(false);
                errorResult.setErrorMessage(e.getMessage());
                return errorResult;
            }
        });
    }

    /**
     * Унифицированный пересчет по типам инструментов с параллельным выполнением
     * Поддерживаемые типы: SHARES, FUTURES, INDICATIVES, ALL
     */
    public List<AggregationResult> recalculateByTypes(List<String> types) {
        if (types == null || types.isEmpty()) {
            types = Collections.singletonList("ALL");
        }

        // Нормализуем в верхний регистр
        List<String> normalized = types.stream()
            .map(t -> t == null ? "" : t.trim().toUpperCase())
            .filter(t -> !t.isEmpty())
            .collect(Collectors.toList());

        boolean all = normalized.contains("ALL");

        List<CompletableFuture<AggregationResult>> tasks = new ArrayList<>();

        if (all || normalized.contains("SHARES")) {
            tasks.add(CompletableFuture.supplyAsync(this::recalculateAllSharesData));
        }
        if (all || normalized.contains("FUTURES")) {
            tasks.add(CompletableFuture.supplyAsync(this::recalculateAllFuturesData));
        }
        // INDICATIVES не поддерживаются в агрегации

        return tasks.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
    }
    
    /**
     * Пересчет агрегированных данных для всех акций
     */
    @Transactional
    public AggregationResult recalculateAllSharesData() {
        String taskId = "SHARES_AGGREGATION_" + UUID.randomUUID().toString().substring(0, 8);
        
        System.out.println("=== НАЧАЛО ПЕРЕСЧЕТА АКЦИЙ ===");
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
            
            System.out.println("[" + taskId + "] === ПЕРЕСЧЕТ ЗАВЕРШЕН ===");
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
    
    /**
     * Пересчитывает агрегированные данные для всех фьючерсов
     */
    @Transactional
    public AggregationResult recalculateAllFuturesData() {
        String taskId = "FUTURES_AGGREGATION_" + UUID.randomUUID().toString().substring(0, 8);
        
        System.out.println("=== НАЧАЛО ПЕРЕСЧЕТА АГРЕГИРОВАННЫХ ДАННЫХ ДЛЯ ФЬЮЧЕРСОВ ===");
        System.out.println("[" + taskId + "] Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
        
        AggregationResult result = new AggregationResult(taskId, "futures");
        
        try {
            List<FutureEntity> futures = futureRepository.findAll();
            System.out.println("[" + taskId + "] Найдено " + futures.size() + " фьючерсов для обработки");
            
            for (FutureEntity future : futures) {
                try {
                    result.setProcessedInstruments(result.getProcessedInstruments() + 1);
                    System.out.println("[" + taskId + "] Обработка фьючерса " + result.getProcessedInstruments() + "/" + futures.size() + ": " + future.getTicker() + " (" + future.getFigi() + ")");
                    
                    FuturesAggregatedDataEntity aggregatedData = calculateFuturesData(future.getFigi(), taskId);
                    
                    if (aggregatedData != null) {
                        saveOrUpdateFuturesAggregatedData(aggregatedData);
                        result.setSuccessfulInstruments(result.getSuccessfulInstruments() + 1);
                        System.out.println("[" + taskId + "] ✓ Данные для " + future.getTicker() + " рассчитаны и сохранены");
                    } else {
                        System.out.println("[" + taskId + "] ✗ Недостаточно данных для " + future.getTicker());
                    }
                    
                } catch (Exception e) {
                    result.setErrorInstruments(result.getErrorInstruments() + 1);
                    System.err.println("[" + taskId + "] ✗ Ошибка обработки фьючерса " + future.getTicker() + ": " + e.getMessage());
                }
            }
            
            result.setSuccess(true);
            
            System.out.println("[" + taskId + "] === ПЕРЕСЧЕТ ФЬЮЧЕРСОВ ЗАВЕРШЕН ===");
            System.out.println("[" + taskId + "] Обработано фьючерсов: " + result.getProcessedInstruments());
            System.out.println("[" + taskId + "] Успешно: " + result.getSuccessfulInstruments());
            System.out.println("[" + taskId + "] Ошибок: " + result.getErrorInstruments());
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Критическая ошибка пересчета фьючерсов: " + e.getMessage());
            e.printStackTrace();
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Рассчитывает агрегированные данные для фьючерса
     */
    private FuturesAggregatedDataEntity calculateFuturesData(String figi, String taskId) {
        try {
            System.out.println("[" + taskId + "] Расчет данных для фьючерса " + figi);
            
            // Получаем все свечи для данного фьючерса
            List<CandleEntity> allCandles = candleRepository.findByFigi(figi);
            
            if (allCandles.isEmpty()) {
                System.out.println("[" + taskId + "] Нет свечей для " + figi);
                return null;
            }
            
            System.out.println("[" + taskId + "] Найдено " + allCandles.size() + " свечей для " + figi);
            
            // Группируем свечи по дням
            List<DailyCandles> dailyCandles = groupCandlesByDay(allCandles);
            
            // Рассчитываем средние объемы
            BigDecimal avgVolumeMorning = calculateAverageVolumeMorning(dailyCandles, taskId);
            BigDecimal avgVolumeEvening = calculateAverageVolumeEvening(dailyCandles, taskId);
            BigDecimal avgVolumeWeekend = calculateAverageVolumeWeekend(dailyCandles, taskId);
            
            // Подсчитываем количество дней
            int tradingDays = (int) dailyCandles.stream()
                .filter(dc -> !isWeekend(dc.getDate()))
                .count();
            
            int weekendDays = (int) dailyCandles.stream()
                .filter(dc -> isWeekend(dc.getDate()))
                .count();
            
            // Создаем результат
            FuturesAggregatedDataEntity result = new FuturesAggregatedDataEntity(figi);
            result.setAvgVolumeMorning(avgVolumeMorning);
            result.setAvgVolumeEvening(avgVolumeEvening);
            result.setAvgVolumeWeekend(avgVolumeWeekend);
            result.setTotalTradingDays(tradingDays);
            result.setTotalWeekendDays(weekendDays);
            
            System.out.println("[" + taskId + "] Результат для фьючерса " + figi + ":");
            System.out.println("[" + taskId + "] - Средний объем утром: " + avgVolumeMorning);
            System.out.println("[" + taskId + "] - Средний объем вечером: " + avgVolumeEvening);
            System.out.println("[" + taskId + "] - Средний объем в выходные: " + avgVolumeWeekend);
            System.out.println("[" + taskId + "] - Торговых дней: " + tradingDays);
            System.out.println("[" + taskId + "] - Выходных дней: " + weekendDays);
            
            return result;
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка расчета данных для фьючерса " + figi + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Рассчитывает средний объем с 19:00:00 по 23:59:59 (для фьючерсов)
     */
    private BigDecimal calculateAverageVolumeEvening(List<DailyCandles> dailyCandles, String taskId) {
        LocalTime eveningStart = LocalTime.of(19, 0, 0);
        LocalTime eveningEnd = LocalTime.of(23, 59, 59);
        
        List<BigDecimal> eveningVolumes = new ArrayList<>();
        
        for (DailyCandles dailyCandle : dailyCandles) {
            if (isWeekend(dailyCandle.getDate())) {
                continue; // Пропускаем выходные дни
            }
            
            BigDecimal dailyVolume = dailyCandle.getCandles().stream()
                .filter(candle -> {
                    LocalTime candleTime = candle.getTime().atZone(ZoneId.of("Europe/Moscow")).toLocalTime();
                    return !candleTime.isBefore(eveningStart) && !candleTime.isAfter(eveningEnd);
                })
                .map(candle -> BigDecimal.valueOf(candle.getVolume()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (dailyVolume.compareTo(BigDecimal.ZERO) > 0) {
                eveningVolumes.add(dailyVolume);
            }
        }
        
        if (eveningVolumes.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalVolume = eveningVolumes.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageVolume = totalVolume.divide(BigDecimal.valueOf(eveningVolumes.size()), 2, RoundingMode.HALF_UP);
        
        System.out.println("[" + taskId + "] Вечерние объемы: " + eveningVolumes.size() + " дней, средний: " + averageVolume);
        
        return averageVolume;
    }
    
    /**
     * Сохраняет или обновляет агрегированные данные для фьючерсов
     */
    private void saveOrUpdateFuturesAggregatedData(FuturesAggregatedDataEntity data) {
        FuturesAggregatedDataEntity existing = futuresAggregatedDataRepository.findByFigi(data.getFigi());
        
        if (existing != null) {
            // Обновляем существующие данные
            existing.setAvgVolumeMorning(data.getAvgVolumeMorning());
            existing.setAvgVolumeEvening(data.getAvgVolumeEvening());
            existing.setAvgVolumeWeekend(data.getAvgVolumeWeekend());
            existing.setTotalTradingDays(data.getTotalTradingDays());
            existing.setTotalWeekendDays(data.getTotalWeekendDays());
            existing.setLastCalculated(data.getLastCalculated());
            existing.setUpdatedAt(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            futuresAggregatedDataRepository.save(existing);
        } else {
            // Создаем новые данные
            futuresAggregatedDataRepository.save(data);
        }
    }
    
    /**
     * Получает все агрегированные данные для акций
     */
    public List<SharesAggregatedDataEntity> getAllSharesAggregatedData() {
        return sharesAggregatedDataRepository.findAllByOrderByUpdatedAtDesc();
    }
    
    /**
     * Получает все агрегированные данные для фьючерсов
     */
    public List<FuturesAggregatedDataEntity> getAllFuturesAggregatedData() {
        return futuresAggregatedDataRepository.findAllByOrderByUpdatedAtDesc();
    }
    
    /**
     * Группирует свечи по дням
     */
    private List<DailyCandles> groupCandlesByDay(List<CandleEntity> candles) {
        Map<LocalDate, List<CandleEntity>> groupedCandles = new HashMap<>();
        
        for (CandleEntity candle : candles) {
            LocalDate date = candle.getTime().atZone(ZoneId.of("Europe/Moscow")).toLocalDate();
            groupedCandles.computeIfAbsent(date, k -> new ArrayList<>()).add(candle);
        }
        
        return groupedCandles.entrySet().stream()
            .map(entry -> new DailyCandles(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(DailyCandles::getDate))
            .collect(Collectors.toList());
    }
    
    /**
     * Рассчитывает средний объем с 06:50:00 по 09:59:59
     */
    private BigDecimal calculateAverageVolumeMorning(List<DailyCandles> dailyCandles, String taskId) {
        LocalTime morningStart = LocalTime.of(6, 50, 0);
        LocalTime morningEnd = LocalTime.of(9, 59, 59);
        
        List<BigDecimal> morningVolumes = new ArrayList<>();
        
        for (DailyCandles dailyCandle : dailyCandles) {
            if (isWeekend(dailyCandle.getDate())) {
                continue; // Пропускаем выходные дни
            }
            
            BigDecimal dailyVolume = dailyCandle.getCandles().stream()
                .filter(candle -> {
                    LocalTime candleTime = candle.getTime().atZone(ZoneId.of("Europe/Moscow")).toLocalTime();
                    return !candleTime.isBefore(morningStart) && !candleTime.isAfter(morningEnd);
                })
                .map(candle -> BigDecimal.valueOf(candle.getVolume()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (dailyVolume.compareTo(BigDecimal.ZERO) > 0) {
                morningVolumes.add(dailyVolume);
            }
        }
        
        if (morningVolumes.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalVolume = morningVolumes.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageVolume = totalVolume.divide(BigDecimal.valueOf(morningVolumes.size()), 2, RoundingMode.HALF_UP);
        
        System.out.println("[" + taskId + "] Утренние объемы: " + morningVolumes.size() + " дней, средний: " + averageVolume);
        
        return averageVolume;
    }
    
    /**
     * Рассчитывает средний объем в выходные дни
     */
    private BigDecimal calculateAverageVolumeWeekend(List<DailyCandles> dailyCandles, String taskId) {
        List<BigDecimal> weekendVolumes = new ArrayList<>();
        
        for (DailyCandles dailyCandle : dailyCandles) {
            if (!isWeekend(dailyCandle.getDate())) {
                continue; // Пропускаем рабочие дни
            }
            
            BigDecimal dailyVolume = dailyCandle.getCandles().stream()
                .map(candle -> BigDecimal.valueOf(candle.getVolume()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (dailyVolume.compareTo(BigDecimal.ZERO) > 0) {
                weekendVolumes.add(dailyVolume);
            }
        }
        
        if (weekendVolumes.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalVolume = weekendVolumes.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageVolume = totalVolume.divide(BigDecimal.valueOf(weekendVolumes.size()), 2, RoundingMode.HALF_UP);
        
        System.out.println("[" + taskId + "] Выходные объемы: " + weekendVolumes.size() + " дней, средний: " + averageVolume);
        
        return averageVolume;
    }
    
    /**
     * Внутренний класс для группировки свечей по дням
     */
    private static class DailyCandles {
        private final LocalDate date;
        private final List<CandleEntity> candles;
        
        public DailyCandles(LocalDate date, List<CandleEntity> candles) {
            this.date = date;
            this.candles = candles;
        }
        
        public LocalDate getDate() {
            return date;
        }
        
        public List<CandleEntity> getCandles() {
            return candles;
        }
    }
    
}
