package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.AggregationResult;
import com.example.InvestmentDataLoaderService.entity.*;
import com.example.InvestmentDataLoaderService.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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
     * Пересчитывает агрегированные данные для всех акций
     */
    public AggregationResult recalculateAllSharesData() {
        String taskId = "SHARES_AGGREGATION_" + UUID.randomUUID().toString().substring(0, 8);
        
        System.out.println("=== НАЧАЛО ПЕРЕСЧЕТА АГРЕГИРОВАННЫХ ДАННЫХ ДЛЯ АКЦИЙ ===");
        System.out.println("[" + taskId + "] Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
        
        AggregationResult result = new AggregationResult(taskId, "shares");
        
        try {
            List<ShareEntity> shares = shareRepository.findAll();
            System.out.println("[" + taskId + "] Найдено " + shares.size() + " акций для обработки");
            
            for (ShareEntity share : shares) {
                try {
                    result.setProcessedInstruments(result.getProcessedInstruments() + 1);
                    System.out.println("[" + taskId + "] Обработка акции " + result.getProcessedInstruments() + "/" + shares.size() + ": " + share.getTicker() + " (" + share.getFigi() + ")");
                    
                    SharesAggregatedDataEntity aggregatedData = calculateSharesData(share.getFigi(), taskId);
                    
                    if (aggregatedData != null) {
                        saveOrUpdateSharesAggregatedData(aggregatedData);
                        result.setSuccessfulInstruments(result.getSuccessfulInstruments() + 1);
                        System.out.println("[" + taskId + "] ✓ Данные для " + share.getTicker() + " рассчитаны и сохранены");
                    } else {
                        System.out.println("[" + taskId + "] ✗ Недостаточно данных для " + share.getTicker());
                    }
                    
                } catch (Exception e) {
                    result.setErrorInstruments(result.getErrorInstruments() + 1);
                    System.err.println("[" + taskId + "] ✗ Ошибка обработки акции " + share.getTicker() + ": " + e.getMessage());
                }
            }
            
            result.setSuccess(true);
            
            System.out.println("[" + taskId + "] === ПЕРЕСЧЕТ АКЦИЙ ЗАВЕРШЕН ===");
            System.out.println("[" + taskId + "] Обработано акций: " + result.getProcessedInstruments());
            System.out.println("[" + taskId + "] Успешно: " + result.getSuccessfulInstruments());
            System.out.println("[" + taskId + "] Ошибок: " + result.getErrorInstruments());
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Критическая ошибка пересчета акций: " + e.getMessage());
            e.printStackTrace();
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Пересчитывает агрегированные данные для всех фьючерсов
     */
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
     * Рассчитывает агрегированные данные для акции
     */
    private SharesAggregatedDataEntity calculateSharesData(String figi, String taskId) {
        try {
            System.out.println("[" + taskId + "] Расчет данных для акции " + figi);
            
            // Получаем все свечи для данной акции
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
            BigDecimal avgVolumeWeekend = calculateAverageVolumeWeekend(dailyCandles, taskId);
            
            // Подсчитываем количество дней
            int tradingDays = (int) dailyCandles.stream()
                .filter(dc -> !isWeekend(dc.getDate()))
                .count();
            
            int weekendDays = (int) dailyCandles.stream()
                .filter(dc -> isWeekend(dc.getDate()))
                .count();
            
            // Создаем результат
            SharesAggregatedDataEntity result = new SharesAggregatedDataEntity(figi);
            result.setAvgVolumeMorning(avgVolumeMorning);
            result.setAvgVolumeWeekend(avgVolumeWeekend);
            result.setTotalTradingDays(tradingDays);
            result.setTotalWeekendDays(weekendDays);
            
            System.out.println("[" + taskId + "] Результат для акции " + figi + ":");
            System.out.println("[" + taskId + "] - Средний объем утром: " + avgVolumeMorning);
            System.out.println("[" + taskId + "] - Средний объем в выходные: " + avgVolumeWeekend);
            System.out.println("[" + taskId + "] - Торговых дней: " + tradingDays);
            System.out.println("[" + taskId + "] - Выходных дней: " + weekendDays);
            
            return result;
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка расчета данных для акции " + figi + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
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
     * Сохраняет или обновляет агрегированные данные для акций
     */
    private void saveOrUpdateSharesAggregatedData(SharesAggregatedDataEntity data) {
        SharesAggregatedDataEntity existing = sharesAggregatedDataRepository.findByFigi(data.getFigi());
        
        if (existing != null) {
            // Обновляем существующие данные
            existing.setAvgVolumeMorning(data.getAvgVolumeMorning());
            existing.setAvgVolumeWeekend(data.getAvgVolumeWeekend());
            existing.setTotalTradingDays(data.getTotalTradingDays());
            existing.setTotalWeekendDays(data.getTotalWeekendDays());
            existing.setLastCalculated(data.getLastCalculated());
            existing.setUpdatedAt(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            sharesAggregatedDataRepository.save(existing);
        } else {
            // Создаем новые данные
            sharesAggregatedDataRepository.save(data);
        }
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
     * Проверяет, является ли дата выходным днем
     */
    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
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
