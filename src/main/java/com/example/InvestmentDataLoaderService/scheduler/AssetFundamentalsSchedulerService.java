package com.example.InvestmentDataLoaderService.scheduler;

import com.example.InvestmentDataLoaderService.service.AssetFundamentalService;
import com.example.InvestmentDataLoaderService.repository.SystemLogRepository;
import com.example.InvestmentDataLoaderService.entity.SystemLogEntity;
import com.example.InvestmentDataLoaderService.dto.AssetFundamentalDto;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Планировщик для автоматического обновления фундаментальных показателей активов
 * Запускается ежедневно в 3:00 по московскому времени
 */
@Service
public class AssetFundamentalsSchedulerService {

    private final AssetFundamentalService assetFundamentalService;
    private final SystemLogRepository systemLogRepository;

    public AssetFundamentalsSchedulerService(AssetFundamentalService assetFundamentalService,
                                           SystemLogRepository systemLogRepository) {
        this.assetFundamentalService = assetFundamentalService;
        this.systemLogRepository = systemLogRepository;
    }

    /**
     * Ежедневное обновление фундаментальных показателей всех активов
     * Запускается в 3:00 по московскому времени
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Moscow")
    public void updateAssetFundamentals() {
        try {
            String taskId = "FUNDAMENTALS_" + UUID.randomUUID().toString().substring(0, 8);
            Instant startTime = Instant.now();
            
            System.out.println("=== НАЧАЛО ОБНОВЛЕНИЯ ФУНДАМЕНТАЛЬНЫХ ПОКАЗАТЕЛЕЙ ===");
            System.out.println("Task ID: " + taskId);
            System.out.println("Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Логируем начало обновления
            logSchedulerStart(taskId, "Обновление фундаментальных показателей");
            
            // Обновляем фундаментальные показатели для всех акций
            updateFundamentalsForShares(taskId);
            
            System.out.println("=== ЗАВЕРШЕНИЕ ОБНОВЛЕНИЯ ФУНДАМЕНТАЛЬНЫХ ПОКАЗАТЕЛЕЙ ===");
            System.out.println("Время завершения: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Логируем завершение обновления
            logSchedulerEnd(taskId, "Обновление фундаментальных показателей", startTime);
            
        } catch (Exception e) {
            System.err.println("Критическая ошибка в обновлении фундаментальных показателей: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Обновляет фундаментальные показатели для всех акций
     */
    private void updateFundamentalsForShares(String parentTaskId) {
        try {
            String taskId = parentTaskId + "_SHARES";
            
            System.out.println("[" + taskId + "] Начало обновления фундаментальных показателей для акций");
            
            // Получаем фундаментальные показатели для всех акций
            List<AssetFundamentalDto> fundamentals = assetFundamentalService.getFundamentalsForAssets(
                Arrays.asList("shares")
            );
            
            if (fundamentals != null && !fundamentals.isEmpty()) {
                System.out.println("[" + taskId + "] Получено " + fundamentals.size() + " записей фундаментальных показателей");
                
                // Сохраняем с принудительным обновлением
                assetFundamentalService.saveAssetFundamentals(fundamentals);
                
                System.out.println("[" + taskId + "] Обновление фундаментальных показателей для акций завершено");
            } else {
                System.out.println("[" + taskId + "] Не получены данные фундаментальных показателей для акций");
            }
            
        } catch (Exception e) {
            System.err.println("Ошибка при обновлении фундаментальных показателей для акций: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ручное обновление фундаментальных показателей для конкретных активов
     * @param assetUids список идентификаторов активов для обновления
     */
    public void updateFundamentalsForAssets(List<String> assetUids) {
        try {
            String taskId = "MANUAL_FUNDAMENTALS_" + UUID.randomUUID().toString().substring(0, 8);
            Instant startTime = Instant.now();
            
            System.out.println("=== НАЧАЛО РУЧНОГО ОБНОВЛЕНИЯ ФУНДАМЕНТАЛЬНЫХ ПОКАЗАТЕЛЕЙ ===");
            System.out.println("Task ID: " + taskId);
            System.out.println("Активы: " + assetUids);
            System.out.println("Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Логируем начало обновления
            logSchedulerStart(taskId, "Ручное обновление фундаментальных показателей");
            
            // Получаем фундаментальные показатели
            List<AssetFundamentalDto> fundamentals = assetFundamentalService.getFundamentalsForAssets(assetUids);
            
            if (fundamentals != null && !fundamentals.isEmpty()) {
                System.out.println("[" + taskId + "] Получено " + fundamentals.size() + " записей фундаментальных показателей");
                
                // Сохраняем с принудительным обновлением
                assetFundamentalService.saveAssetFundamentals(fundamentals);
                
                System.out.println("[" + taskId + "] Ручное обновление фундаментальных показателей завершено");
            } else {
                System.out.println("[" + taskId + "] Не получены данные фундаментальных показателей");
            }
            
            System.out.println("=== ЗАВЕРШЕНИЕ РУЧНОГО ОБНОВЛЕНИЯ ФУНДАМЕНТАЛЬНЫХ ПОКАЗАТЕЛЕЙ ===");
            System.out.println("Время завершения: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Логируем завершение обновления
            logSchedulerEnd(taskId, "Ручное обновление фундаментальных показателей", startTime);
            
        } catch (Exception e) {
            System.err.println("Критическая ошибка в ручном обновлении фундаментальных показателей: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ручное обновление фундаментальных показателей для всех акций
     */
    public void updateFundamentalsForAllShares() {
        updateFundamentalsForAssets(Arrays.asList("shares"));
    }

    /**
     * Логирует начало работы планировщика
     */
    private void logSchedulerStart(String taskId, String operation) {
        try {
            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId(taskId);
            log.setEndpoint("/scheduler/asset-fundamentals");
            log.setMethod("SCHEDULED");
            log.setStatus("STARTED");
            log.setMessage(operation + " запущена");
            log.setStartTime(Instant.now());
            log.setEndTime(Instant.now());
            log.setDurationMs(0L);
            
            systemLogRepository.save(log);
            System.out.println("Лог начала работы планировщика сохранен: " + taskId);
            
        } catch (Exception e) {
            System.err.println("Ошибка сохранения лога начала работы планировщика: " + e.getMessage());
        }
    }

    /**
     * Логирует завершение работы планировщика
     */
    private void logSchedulerEnd(String taskId, String operation, Instant startTime) {
        try {
            Instant endTime = Instant.now();
            long duration = endTime.toEpochMilli() - startTime.toEpochMilli();
            
            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId(taskId);
            log.setEndpoint("/scheduler/asset-fundamentals");
            log.setMethod("SCHEDULED");
            log.setStatus("COMPLETED");
            log.setMessage(operation + " завершена успешно. Время выполнения: " + duration + " мс");
            log.setStartTime(startTime);
            log.setEndTime(endTime);
            log.setDurationMs(duration);
            
            systemLogRepository.save(log);
            System.out.println("Лог завершения работы планировщика сохранен: " + taskId);
            
        } catch (Exception e) {
            System.err.println("Ошибка сохранения лога завершения работы планировщика: " + e.getMessage());
        }
    }
}
