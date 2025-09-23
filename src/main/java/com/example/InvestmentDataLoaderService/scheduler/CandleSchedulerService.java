package com.example.InvestmentDataLoaderService.scheduler;

import com.example.InvestmentDataLoaderService.dto.MinuteCandleRequestDto;
import com.example.InvestmentDataLoaderService.dto.DailyCandleRequestDto;
import com.example.InvestmentDataLoaderService.service.MinuteCandleService;
import com.example.InvestmentDataLoaderService.service.DailyCandleService;
import com.example.InvestmentDataLoaderService.repository.SystemLogRepository;
import com.example.InvestmentDataLoaderService.entity.SystemLogEntity;
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
            
            System.out.println("=== НАЧАЛО ЕЖЕДНЕВНОЙ ЗАГРУЗКИ СВЕЧЕЙ ===");
            System.out.println("Task ID: " + taskId);
            System.out.println("Дата: " + previousDay);
            System.out.println("Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Логируем начало загрузки
            logSchedulerStart(taskId, "Ежедневная загрузка свечей", previousDay);
            
            // Загружаем минутные свечи для всех типов активов
            fetchMinuteCandlesForAllAssets(previousDay, taskId);
            
            // Небольшая пауза между загрузкой минутных и дневных свечей
            Thread.sleep(10000);
            
            // Загружаем дневные свечи для всех типов активов
            fetchDailyCandlesForAllAssets(previousDay, taskId);
            
            System.out.println("=== ЗАВЕРШЕНИЕ ЕЖЕДНЕВНОЙ ЗАГРУЗКИ СВЕЧЕЙ ===");
            System.out.println("Время завершения: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Логируем завершение загрузки
            logSchedulerEnd(taskId, "Ежедневная загрузка свечей", startTime);
            
        } catch (Exception e) {
            System.err.println("Критическая ошибка в ежедневной загрузке свечей: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Загружает минутные свечи для всех типов активов
     */
    private void fetchMinuteCandlesForAllAssets(LocalDate date, String parentTaskId) {
        try {
            String taskId = parentTaskId + "_MINUTE";
            
            System.out.println("[" + taskId + "] Начало загрузки минутных свечей за " + date);
            
            MinuteCandleRequestDto request = new MinuteCandleRequestDto();
            request.setDate(date);
            request.setAssetType(Arrays.asList("SHARES", "FUTURES", "INDICATIVES"));
            
            // Запускаем загрузку в асинхронном режиме
            minuteCandleService.saveMinuteCandlesAsync(request, taskId);
            
            System.out.println("[" + taskId + "] Загрузка минутных свечей запущена в фоновом режиме");
            
        } catch (Exception e) {
            System.err.println("Ошибка при запуске загрузки минутных свечей: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Загружает дневные свечи для всех типов активов
     */
    private void fetchDailyCandlesForAllAssets(LocalDate date, String parentTaskId) {
        try {
            String taskId = parentTaskId + "_DAILY";
            
            System.out.println("[" + taskId + "] Начало загрузки дневных свечей за " + date);
            
            DailyCandleRequestDto request = new DailyCandleRequestDto();
            request.setDate(date);
            request.setAssetType(Arrays.asList("SHARES", "FUTURES", "INDICATIVES"));
            
            // Запускаем загрузку в асинхронном режиме
            dailyCandleService.saveDailyCandlesAsync(request, taskId);
            
            System.out.println("[" + taskId + "] Загрузка дневных свечей запущена в фоновом режиме");
            
        } catch (Exception e) {
            System.err.println("Ошибка при запуске загрузки дневных свечей: " + e.getMessage());
            e.printStackTrace();
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
            
            System.out.println("=== НАЧАЛО РУЧНОЙ ЗАГРУЗКИ СВЕЧЕЙ ===");
            System.out.println("Task ID: " + taskId);
            System.out.println("Дата: " + date);
            System.out.println("Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Логируем начало загрузки
            logSchedulerStart(taskId, "Ручная загрузка свечей", date);
            
            // Загружаем минутные свечи для всех типов активов
            fetchMinuteCandlesForAllAssets(date, taskId);
            
            // Небольшая пауза между загрузкой минутных и дневных свечей
            Thread.sleep(10000);
            
            // Загружаем дневные свечи для всех типов активов
            fetchDailyCandlesForAllAssets(date, taskId);
            
            System.out.println("=== ЗАВЕРШЕНИЕ РУЧНОЙ ЗАГРУЗКИ СВЕЧЕЙ ===");
            System.out.println("Время завершения: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Логируем завершение загрузки
            logSchedulerEnd(taskId, "Ручная загрузка свечей", startTime);
            
        } catch (Exception e) {
            System.err.println("Критическая ошибка в ручной загрузке свечей: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ручная загрузка только минутных свечей за конкретную дату
     * @param date дата для загрузки свечей
     */
    public void fetchAndStoreMinuteCandlesForDate(LocalDate date) {
        try {
            String taskId = "MANUAL_MINUTE_" + UUID.randomUUID().toString().substring(0, 8);
            
            System.out.println("=== НАЧАЛО РУЧНОЙ ЗАГРУЗКИ МИНУТНЫХ СВЕЧЕЙ ===");
            System.out.println("Task ID: " + taskId);
            System.out.println("Дата: " + date);
            
            fetchMinuteCandlesForAllAssets(date, taskId);
            
            System.out.println("=== ЗАВЕРШЕНИЕ РУЧНОЙ ЗАГРУЗКИ МИНУТНЫХ СВЕЧЕЙ ===");
            
        } catch (Exception e) {
            System.err.println("Ошибка в ручной загрузке минутных свечей: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ручная загрузка только дневных свечей за конкретную дату
     * @param date дата для загрузки свечей
     */
    public void fetchAndStoreDailyCandlesForDate(LocalDate date) {
        try {
            String taskId = "MANUAL_DAILY_" + UUID.randomUUID().toString().substring(0, 8);
            
            System.out.println("=== НАЧАЛО РУЧНОЙ ЗАГРУЗКИ ДНЕВНЫХ СВЕЧЕЙ ===");
            System.out.println("Task ID: " + taskId);
            System.out.println("Дата: " + date);
            
            fetchDailyCandlesForAllAssets(date, taskId);
            
            System.out.println("=== ЗАВЕРШЕНИЕ РУЧНОЙ ЗАГРУЗКИ ДНЕВНЫХ СВЕЧЕЙ ===");
            
        } catch (Exception e) {
            System.err.println("Ошибка в ручной загрузке дневных свечей: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Логирует начало работы шедулера
     */
    private void logSchedulerStart(String taskId, String operation, LocalDate date) {
        try {
            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId(taskId);
            log.setEndpoint("/scheduler/candles");
            log.setMethod("SCHEDULED");
            log.setStatus("STARTED");
            log.setMessage(operation + " для даты " + date + " запущена");
            log.setStartTime(Instant.now());
            log.setEndTime(Instant.now());
            log.setDurationMs(0L);
            
            systemLogRepository.save(log);
            System.out.println("Лог начала работы шедулера сохранен: " + taskId);
            
        } catch (Exception e) {
            System.err.println("Ошибка сохранения лога начала работы шедулера: " + e.getMessage());
        }
    }

    /**
     * Логирует завершение работы шедулера
     */
    private void logSchedulerEnd(String taskId, String operation, Instant startTime) {
        try {
            Instant endTime = Instant.now();
            long duration = endTime.toEpochMilli() - startTime.toEpochMilli();
            
            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId(taskId);
            log.setEndpoint("/scheduler/candles");
            log.setMethod("SCHEDULED");
            log.setStatus("COMPLETED");
            log.setMessage(operation + " завершена успешно. Время выполнения: " + duration + " мс");
            log.setStartTime(startTime);
            log.setEndTime(endTime);
            log.setDurationMs(duration);
            
            systemLogRepository.save(log);
            System.out.println("Лог завершения работы шедулера сохранен: " + taskId);
            
        } catch (Exception e) {
            System.err.println("Ошибка сохранения лога завершения работы шедулера: " + e.getMessage());
        }
    }

}
