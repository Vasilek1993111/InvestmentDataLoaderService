package com.example.InvestmentDataLoaderService.scheduler;

import com.example.InvestmentDataLoaderService.service.DividendService;
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
import java.util.Map;

@Service
public class DividendSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(DividendSchedulerService.class);
    private final DividendService dividendService;
    private final SystemLogRepository systemLogRepository;

    public DividendSchedulerService(DividendService dividendService,
                                   SystemLogRepository systemLogRepository) {
        this.dividendService = dividendService;
        this.systemLogRepository = systemLogRepository;
    }

    /**
     * Ежедневная загрузка дивидендов по всем акциям
     * Запускается в 00:50 по московскому времени
     * Загружает дивиденды за период с 2024-01-01 по 2026-12-31
     */
    @Scheduled(cron = "0 50 0 * * *", zone = "Europe/Moscow")
    public void fetchAndStoreDividends() {
        try {
            String taskId = "DIVIDEND_" + UUID.randomUUID().toString().substring(0, 8);
            Instant startTime = Instant.now();
            
            System.out.println("=== НАЧАЛО ЕЖЕДНЕВНОЙ ЗАГРУЗКИ ДИВИДЕНДОВ ===");
            System.out.println("Task ID: " + taskId);
            System.out.println("Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Загружаем дивиденды для всех акций
            Map<String, Object> result = dividendService.loadDividendsForInstruments(
                Arrays.asList("SHARES"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31)
            );
            
            Instant endTime = Instant.now();
            long duration = java.time.Duration.between(startTime, endTime).toMillis();
            
            // Логируем результат
            System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ДИВИДЕНДОВ ===");
            System.out.println("Task ID: " + taskId);
            System.out.println("Время завершения: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            System.out.println("Длительность: " + duration + " мс");
            System.out.println("Результат: " + result);
            
            // Сохраняем в системный лог
            SystemLogEntity logEntry = new SystemLogEntity();
            logEntry.setTaskId(taskId);
            logEntry.setEndpoint("DIVIDEND_SCHEDULER");
            logEntry.setMethod("SCHEDULED");
            logEntry.setStatus("SUCCESS");
            logEntry.setMessage("Загрузка дивидендов завершена успешно. Обработано инструментов: " + result.get("processedInstruments") + 
                              ", Загружено: " + result.get("totalLoaded") + 
                              ", Уже существует: " + result.get("alreadyExists") + 
                              ", От API: " + result.get("totalFromApi"));
            logEntry.setStartTime(startTime);
            logEntry.setEndTime(endTime);
            logEntry.setDurationMs(duration);
            
            systemLogRepository.save(logEntry);
            
        } catch (Exception e) {
            String taskId = "DIVIDEND_" + UUID.randomUUID().toString().substring(0, 8);
            Instant endTime = Instant.now();
            long duration = java.time.Duration.between(Instant.now().minusSeconds(1), endTime).toMillis();
            
            System.err.println("=== КРИТИЧЕСКАЯ ОШИБКА В ЗАГРУЗКЕ ДИВИДЕНДОВ ===");
            System.err.println("Task ID: " + taskId);
            System.err.println("Время ошибки: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
            
            // Сохраняем ошибку в системный лог
            try {
                SystemLogEntity logEntry = new SystemLogEntity();
                logEntry.setTaskId(taskId);
                logEntry.setEndpoint("DIVIDEND_SCHEDULER");
                logEntry.setMethod("SCHEDULED");
                logEntry.setStatus("ERROR");
                logEntry.setMessage("Критическая ошибка в загрузке дивидендов: " + e.getMessage());
                logEntry.setStartTime(Instant.now().minusSeconds(1));
                logEntry.setEndTime(endTime);
                logEntry.setDurationMs(duration);
                
                systemLogRepository.save(logEntry);
            } catch (Exception logException) {
                System.err.println("Ошибка при сохранении лога: " + logException.getMessage());
            }
        }
    }
}
