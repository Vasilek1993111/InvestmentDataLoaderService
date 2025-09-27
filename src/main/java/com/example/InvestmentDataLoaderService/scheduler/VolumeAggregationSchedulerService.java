package com.example.InvestmentDataLoaderService.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Сервис для обновления материализованного представления агрегации объемов
 * Обновляется каждую минуту для актуальных данных
 */
@Service
public class VolumeAggregationSchedulerService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    
    /**
     * Обновление общего материализованного представления раз в день в 2:00
     * Обновляет все исторические данные
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "Europe/Moscow")
    public void fullRefreshVolumeAggregation() {
        String taskId = "FULL_VOLUME_AGG_" + UUID.randomUUID().toString().substring(0, 8);
        
        try {
            System.out.println("=== ОБНОВЛЕНИЕ ОБЩЕГО ПРЕДСТАВЛЕНИЯ ===");
            System.out.println("[" + taskId + "] Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Попытка обновления с CONCURRENTLY (если возможно)
            try {
                String refreshSql = "REFRESH MATERIALIZED VIEW CONCURRENTLY invest.daily_volume_aggregation";
                System.out.println("[" + taskId + "] Выполняем: " + refreshSql);
                jdbcTemplate.execute(refreshSql);
                System.out.println("[" + taskId + "] Обновление с CONCURRENTLY выполнено успешно");
            } catch (Exception e) {
                // Если CONCURRENTLY не поддерживается, используем обычное обновление
                System.out.println("[" + taskId + "] CONCURRENTLY не поддерживается, используем обычное обновление");
                String refreshSql = "REFRESH MATERIALIZED VIEW invest.daily_volume_aggregation";
                jdbcTemplate.execute(refreshSql);
                System.out.println("[" + taskId + "] Обычное обновление выполнено успешно");
            }
            
            System.out.println("[" + taskId + "] Общее представление обновлено");
            System.out.println("=== ЗАВЕРШЕНИЕ ОБНОВЛЕНИЯ ===");
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка обновления общего представления: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}
