package com.example.InvestmentDataLoaderService.scheduler;

import com.example.InvestmentDataLoaderService.dto.MinuteCandleRequestDto;
import com.example.InvestmentDataLoaderService.dto.DailyCandleRequestDto;
import com.example.InvestmentDataLoaderService.service.MinuteCandleService;
import com.example.InvestmentDataLoaderService.service.DailyCandleService;
import com.example.InvestmentDataLoaderService.repository.SystemLogRepository;
import com.example.InvestmentDataLoaderService.entity.SystemLogEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

@Service
public class CandleSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(CandleSchedulerService.class);
    private final MinuteCandleService minuteCandleService;
    private final DailyCandleService dailyCandleService;
    private final SystemLogRepository systemLogRepository;

    public CandleSchedulerService(MinuteCandleService minuteCandleService, 
                                 DailyCandleService dailyCandleService,
                                 SystemLogRepository systemLogRepository) {
        this.minuteCandleService = minuteCandleService;
        this.dailyCandleService = dailyCandleService;
        this.systemLogRepository = systemLogRepository;
    }

    /**
     * Ежедневная загрузка свечей за предыдущий день
     * Запускается в 1:10 по московскому времени
     * Сначала загружает минутные свечи, затем дневные свечи
     */
    @Scheduled(cron = "0 10 1 * * *", zone = "Europe/Moscow")
    public void fetchAndStoreCandles() {
        try {
            LocalDate previousDay = LocalDate.now(ZoneId.of("Europe/Moscow")).minusDays(1);
            String taskId = "SCHEDULER_" + UUID.randomUUID().toString().substring(0, 8);
            Instant startTime = Instant.now();
            
            log.info("=== НАЧАЛО ЕЖЕДНЕВНОЙ ЗАГРУЗКИ СВЕЧЕЙ ===");
            log.info("Task ID: {}", taskId);
            log.info("Дата: {}", previousDay);
            log.info("Время запуска: {}", LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Логируем начало загрузки
            logSchedulerStart(taskId, "Ежедневная загрузка свечей", previousDay);
            
            // Загружаем минутные свечи для всех типов активов
            fetchMinuteCandlesForAllAssets(previousDay, taskId);
            
            // Небольшая пауза между загрузкой минутных и дневных свечей
            Thread.sleep(10000);
            
            // Загружаем дневные свечи для всех типов активов
            fetchDailyCandlesForAllAssets(previousDay, taskId);
            
            log.info("=== ЗАВЕРШЕНИЕ ЕЖЕДНЕВНОЙ ЗАГРУЗКИ СВЕЧЕЙ ===");
            log.info("Время завершения: {}", LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Логируем завершение загрузки
            logSchedulerEnd(taskId, "Ежедневная загрузка свечей", startTime);
            
        } catch (Exception e) {
            log.error("Критическая ошибка в ежедневной загрузке свечей", e);
        }
    }

    /**
     * Загружает минутные свечи для всех типов активов
     */
    private void fetchMinuteCandlesForAllAssets(LocalDate date, String parentTaskId) {
        try {
            String taskId = parentTaskId + "_MINUTE";
            
            log.info("[{}] Начало загрузки минутных свечей за {}", taskId, date);
            
            MinuteCandleRequestDto request = new MinuteCandleRequestDto();
            request.setDate(date);
            request.setAssetType(Arrays.asList("SHARES", "FUTURES", "INDICATIVES"));
            
            // Запускаем загрузку в асинхронном режиме
            minuteCandleService.saveMinuteCandlesAsync(request, taskId);
            
            log.info("[{}] Загрузка минутных свечей запущена в фоновом режиме", taskId);
            
        } catch (Exception e) {
            log.error("Ошибка при запуске загрузки минутных свечей", e);
        }
    }

    /**
     * Загружает дневные свечи для всех типов активов
     */
    private void fetchDailyCandlesForAllAssets(LocalDate date, String parentTaskId) {
        try {
            String taskId = parentTaskId + "_DAILY";
            
            log.info("[{}] Начало загрузки дневных свечей за {}", taskId, date);
            
            DailyCandleRequestDto request = new DailyCandleRequestDto();
            request.setDate(date);
            request.setAssetType(Arrays.asList("SHARES", "FUTURES", "INDICATIVES"));
            
            // Запускаем загрузку в асинхронном режиме
            dailyCandleService.saveDailyCandlesAsync(request, taskId);
            
            log.info("[{}] Загрузка дневных свечей запущена в фоновом режиме", taskId);
            
        } catch (Exception e) {
            log.error("Ошибка при запуске загрузки дневных свечей", e);
        }
    }

    /**
     * Ручная загрузка свечей за конкретную дату
     * @param date дата для загрузки свечей
     */
    public void fetchAndStoreCandlesForDate(LocalDate date) {
        try {
            String taskId = "MANUAL_" + UUID.randomUUID().toString().substring(0, 8);
            Instant startTime = Instant.now();
            
            log.info("=== НАЧАЛО РУЧНОЙ ЗАГРУЗКИ СВЕЧЕЙ ===");
            log.info("Task ID: {}", taskId);
            log.info("Дата: {}", date);
            log.info("Время запуска: {}", LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Логируем начало загрузки
            logSchedulerStart(taskId, "Ручная загрузка свечей", date);
            
            // Загружаем минутные свечи для всех типов активов
            fetchMinuteCandlesForAllAssets(date, taskId);
            
            // Небольшая пауза между загрузкой минутных и дневных свечей
            Thread.sleep(10000);
            
            // Загружаем дневные свечи для всех типов активов
            fetchDailyCandlesForAllAssets(date, taskId);
            
            log.info("=== ЗАВЕРШЕНИЕ РУЧНОЙ ЗАГРУЗКИ СВЕЧЕЙ ===");
            log.info("Время завершения: {}", LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Логируем завершение загрузки
            logSchedulerEnd(taskId, "Ручная загрузка свечей", startTime);
            
        } catch (Exception e) {
            log.error("Критическая ошибка в ручной загрузке свечей", e);
        }
    }

    /**
     * Ручная загрузка только минутных свечей за конкретную дату
     * @param date дата для загрузки свечей
     */
    public void fetchAndStoreMinuteCandlesForDate(LocalDate date) {
        try {
            String taskId = "MANUAL_MINUTE_" + UUID.randomUUID().toString().substring(0, 8);
            
            log.info("=== НАЧАЛО РУЧНОЙ ЗАГРУЗКИ МИНУТНЫХ СВЕЧЕЙ ===");
            log.info("Task ID: {}", taskId);
            log.info("Дата: {}", date);
            
            fetchMinuteCandlesForAllAssets(date, taskId);
            
            log.info("=== ЗАВЕРШЕНИЕ РУЧНОЙ ЗАГРУЗКИ МИНУТНЫХ СВЕЧЕЙ ===");
            
        } catch (Exception e) {
            log.error("Ошибка в ручной загрузке минутных свечей", e);
        }
    }

    /**
     * Ручная загрузка только дневных свечей за конкретную дату
     * @param date дата для загрузки свечей
     */
    public void fetchAndStoreDailyCandlesForDate(LocalDate date) {
        try {
            String taskId = "MANUAL_DAILY_" + UUID.randomUUID().toString().substring(0, 8);
            
            log.info("=== НАЧАЛО РУЧНОЙ ЗАГРУЗКИ ДНЕВНЫХ СВЕЧЕЙ ===");
            log.info("Task ID: {}", taskId);
            log.info("Дата: {}", date);
            
            fetchDailyCandlesForAllAssets(date, taskId);
            
            log.info("=== ЗАВЕРШЕНИЕ РУЧНОЙ ЗАГРУЗКИ ДНЕВНЫХ СВЕЧЕЙ ===");
            
        } catch (Exception e) {
            log.error("Ошибка в ручной загрузке дневных свечей", e);
        }
    }

    /**
     * Логирует начало работы шедулера
     */
    private void logSchedulerStart(String taskId, String operation, LocalDate date) {
        try {
            SystemLogEntity systemLog = new SystemLogEntity();
            systemLog.setTaskId(taskId);
            systemLog.setEndpoint("/scheduler/candles");
            systemLog.setMethod("SCHEDULED");
            systemLog.setStatus("STARTED");
            systemLog.setMessage(operation + " для даты " + date + " запущена");
            systemLog.setStartTime(Instant.now());
            systemLog.setEndTime(Instant.now());
            systemLog.setDurationMs(0L);
            
            systemLogRepository.save(systemLog);
            log.info("Лог начала работы шедулера сохранен: {}", taskId);
            
        } catch (Exception e) {
            log.error("Ошибка сохранения лога начала работы шедулера", e);
        }
    }

    /**
     * Логирует завершение работы шедулера
     */
    private void logSchedulerEnd(String taskId, String operation, Instant startTime) {
        try {
            Instant endTime = Instant.now();
            long duration = endTime.toEpochMilli() - startTime.toEpochMilli();
            
            SystemLogEntity systemLog = new SystemLogEntity();
            systemLog.setTaskId(taskId);
            systemLog.setEndpoint("/scheduler/candles");
            systemLog.setMethod("SCHEDULED");
            systemLog.setStatus("COMPLETED");
            systemLog.setMessage(operation + " завершена успешно. Время выполнения: " + duration + " мс");
            systemLog.setStartTime(startTime);
            systemLog.setEndTime(endTime);
            systemLog.setDurationMs(duration);
            
            systemLogRepository.save(systemLog);
            log.info("Лог завершения работы шедулера сохранен: {}", taskId);
            
        } catch (Exception e) {
            log.error("Ошибка сохранения лога завершения работы шедулера", e);
        }
    }

}
