package com.example.InvestmentDataLoaderService.scheduler;

import com.example.InvestmentDataLoaderService.service.AssetFundamentalService;
import com.example.InvestmentDataLoaderService.repository.SystemLogRepository;
import com.example.InvestmentDataLoaderService.entity.SystemLogEntity;
import com.example.InvestmentDataLoaderService.dto.AssetFundamentalDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AssetFundamentalsSchedulerService.class);
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
            
            log.info("=== НАЧАЛО ОБНОВЛЕНИЯ ФУНДАМЕНТАЛЬНЫХ ПОКАЗАТЕЛЕЙ ===");
            log.info("Task ID: {}", taskId);
            log.info("Время запуска: {}", LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Логируем начало обновления
            logSchedulerStart(taskId, "Обновление фундаментальных показателей");
            
            // Обновляем фундаментальные показатели для всех акций
            updateFundamentalsForShares(taskId);
            
            log.info("=== ЗАВЕРШЕНИЕ ОБНОВЛЕНИЯ ФУНДАМЕНТАЛЬНЫХ ПОКАЗАТЕЛЕЙ ===");
            log.info("Время завершения: {}", LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Логируем завершение обновления
            logSchedulerEnd(taskId, "Обновление фундаментальных показателей", startTime);
            
        } catch (Exception e) {
            log.error("Критическая ошибка в обновлении фундаментальных показателей", e);
        }
    }

    /**
     * Обновляет фундаментальные показатели для всех акций
     */
    private void updateFundamentalsForShares(String parentTaskId) {
        try {
            String taskId = parentTaskId + "_SHARES";
            
            log.info("[{}] Начало обновления фундаментальных показателей для акций", taskId);
            
            // Получаем фундаментальные показатели для всех акций
            List<AssetFundamentalDto> fundamentals = assetFundamentalService.getFundamentalsForAssets(
                Arrays.asList("shares")
            );
            
            if (fundamentals != null && !fundamentals.isEmpty()) {
                log.info("[{}] Получено {} записей фундаментальных показателей", taskId, fundamentals.size());
                
                // Сохраняем с принудительным обновлением
                assetFundamentalService.saveAssetFundamentals(fundamentals);
                
                log.info("[{}] Обновление фундаментальных показателей для акций завершено", taskId);
            } else {
                log.info("[{}] Не получены данные фундаментальных показателей для акций", taskId);
            }
            
        } catch (Exception e) {
            log.error("Ошибка при обновлении фундаментальных показателей для акций", e);
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
            
            log.info("=== НАЧАЛО РУЧНОГО ОБНОВЛЕНИЯ ФУНДАМЕНТАЛЬНЫХ ПОКАЗАТЕЛЕЙ ===");
            log.info("Task ID: {}", taskId);
            log.info("Активы: {}", assetUids);
            log.info("Время запуска: {}", LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Логируем начало обновления
            logSchedulerStart(taskId, "Ручное обновление фундаментальных показателей");
            
            // Получаем фундаментальные показатели
            List<AssetFundamentalDto> fundamentals = assetFundamentalService.getFundamentalsForAssets(assetUids);
            
            if (fundamentals != null && !fundamentals.isEmpty()) {
                log.info("[{}] Получено {} записей фундаментальных показателей", taskId, fundamentals.size());
                
                // Сохраняем с принудительным обновлением
                assetFundamentalService.saveAssetFundamentals(fundamentals);
                
                log.info("[{}] Ручное обновление фундаментальных показателей завершено", taskId);
            } else {
                log.info("[{}] Не получены данные фундаментальных показателей", taskId);
            }
            
            log.info("=== ЗАВЕРШЕНИЕ РУЧНОГО ОБНОВЛЕНИЯ ФУНДАМЕНТАЛЬНЫХ ПОКАЗАТЕЛЕЙ ===");
            log.info("Время завершения: {}", LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Логируем завершение обновления
            logSchedulerEnd(taskId, "Ручное обновление фундаментальных показателей", startTime);
            
        } catch (Exception e) {
            log.error("Критическая ошибка в ручном обновлении фундаментальных показателей", e);
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
            SystemLogEntity systemLog = new SystemLogEntity();
            systemLog.setTaskId(taskId);
            systemLog.setEndpoint("/scheduler/asset-fundamentals");
            systemLog.setMethod("SCHEDULED");
            systemLog.setStatus("STARTED");
            systemLog.setMessage(operation + " запущена");
            systemLog.setStartTime(Instant.now());
            systemLog.setEndTime(Instant.now());
            systemLog.setDurationMs(0L);
            
            systemLogRepository.save(systemLog);
            log.info("Лог начала работы планировщика сохранен: {}", taskId);
            
        } catch (Exception e) {
            log.error("Ошибка сохранения лога начала работы планировщика", e);
        }
    }

    /**
     * Логирует завершение работы планировщика
     */
    private void logSchedulerEnd(String taskId, String operation, Instant startTime) {
        try {
            Instant endTime = Instant.now();
            long duration = endTime.toEpochMilli() - startTime.toEpochMilli();
            
            SystemLogEntity systemLog = new SystemLogEntity();
            systemLog.setTaskId(taskId);
            systemLog.setEndpoint("/scheduler/asset-fundamentals");
            systemLog.setMethod("SCHEDULED");
            systemLog.setStatus("COMPLETED");
            systemLog.setMessage(operation + " завершена успешно. Время выполнения: " + duration + " мс");
            systemLog.setStartTime(startTime);
            systemLog.setEndTime(endTime);
            systemLog.setDurationMs(duration);
            
            systemLogRepository.save(systemLog);
            log.info("Лог завершения работы планировщика сохранен: {}", taskId);
            
        } catch (Exception e) {
            log.error("Ошибка сохранения лога завершения работы планировщика", e);
        }
    }
}
