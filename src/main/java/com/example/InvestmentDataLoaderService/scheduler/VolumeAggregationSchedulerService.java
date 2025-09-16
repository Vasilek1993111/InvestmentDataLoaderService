package com.example.InvestmentDataLoaderService.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.Map;

/**
 * Сервис для обновления материализованного представления агрегации объемов
 * Обновляется каждую минуту для актуальных данных
 */
@Service
public class VolumeAggregationSchedulerService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * Обновление дневного материализованного представления каждую минуту
     * Обновляет только данные за текущий день для быстрого доступа
     * Запускается в 0 секунд каждой минуты
     */
    @Scheduled(cron = "0 * * * * *", zone = "Europe/Moscow")
    public void refreshTodayVolumeAggregation() {
        String taskId = "TODAY_VOLUME_AGG_" + UUID.randomUUID().toString().substring(0, 8);
        
        try {
            System.out.println("=== ОБНОВЛЕНИЕ ДНЕВНОГО ПРЕДСТАВЛЕНИЯ ===");
            System.out.println("[" + taskId + "] Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Обновляем дневное материализованное представление
            refreshTodayData(taskId);
            
            System.out.println("[" + taskId + "] Дневное представление успешно обновлено");
            System.out.println("=== ЗАВЕРШЕНИЕ ОБНОВЛЕНИЯ ===");
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка обновления дневного представления: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Обновление общего материализованного представления раз в день в 2:20
     * Обновляет все исторические данные
     */
    @Scheduled(cron = "0 20 2 * * *", zone = "Europe/Moscow")
    public void fullRefreshVolumeAggregation() {
        String taskId = "FULL_VOLUME_AGG_" + UUID.randomUUID().toString().substring(0, 8);
        
        try {
            System.out.println("=== ОБНОВЛЕНИЕ ОБЩЕГО ПРЕДСТАВЛЕНИЯ ===");
            System.out.println("[" + taskId + "] Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Обновляем общее материализованное представление
            refreshGeneralData(taskId);
            
            // Получаем статистику после обновления
            printAggregationStats(taskId);
            
            System.out.println("[" + taskId + "] Общее представление обновлено");
            System.out.println("=== ЗАВЕРШЕНИЕ ОБНОВЛЕНИЯ ===");
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка обновления общего представления: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Ручное обновление данных за сегодня
     */
    @Transactional
    public void refreshTodayDataManually() {
        String taskId = "MANUAL_TODAY_AGG_" + UUID.randomUUID().toString().substring(0, 8);
        
        try {
            System.out.println("=== РУЧНОЕ ОБНОВЛЕНИЕ ДАННЫХ ЗА СЕГОДНЯ ===");
            System.out.println("[" + taskId + "] Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            refreshTodayData(taskId);
            
            System.out.println("[" + taskId + "] Данные за сегодня успешно обновлены вручную");
            System.out.println("=== ЗАВЕРШЕНИЕ РУЧНОГО ОБНОВЛЕНИЯ ===");
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка ручного обновления: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Ручное полное обновление материализованного представления
     */
    @Transactional
    public void refreshMaterializedViewManually() {
        String taskId = "MANUAL_FULL_AGG_" + UUID.randomUUID().toString().substring(0, 8);
        
        try {
            System.out.println("=== РУЧНОЕ ПОЛНОЕ ОБНОВЛЕНИЕ МАТЕРИАЛИЗОВАННОГО ПРЕДСТАВЛЕНИЯ ===");
            System.out.println("[" + taskId + "] Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            refreshMaterializedView(taskId);
            
            System.out.println("[" + taskId + "] Материализованное представление успешно обновлено вручную");
            System.out.println("=== ЗАВЕРШЕНИЕ РУЧНОГО ОБНОВЛЕНИЯ ===");
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка ручного обновления: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Обновление дневного материализованного представления
     */
    @Transactional
    public void refreshTodayData(String taskId) {
        try {
            System.out.println("[" + taskId + "] Начинаем обновление дневного представления...");
            
            // Вызываем функцию для обновления дневного представления
            String updateTodaySql = "SELECT invest.update_today_volume_aggregation()";
            jdbcTemplate.execute(updateTodaySql);
            
            System.out.println("[" + taskId + "] Дневное представление обновлено");
            
            // Получаем статистику по сегодняшним данным
            printTodayStats(taskId);
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка при обновлении дневного представления: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Обновление общего материализованного представления
     */
    @Transactional
    public void refreshGeneralData(String taskId) {
        try {
            System.out.println("[" + taskId + "] Начинаем обновление общего представления...");
            
            // Вызываем функцию для обновления общего представления
            String updateGeneralSql = "SELECT invest.update_daily_volume_aggregation()";
            jdbcTemplate.execute(updateGeneralSql);
            
            System.out.println("[" + taskId + "] Общее представление обновлено");
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка при обновлении общего представления: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Обновление общего материализованного представления с полным пересчетом
     */
    @Transactional
    public void refreshMaterializedView(String taskId) {
        try {
            System.out.println("[" + taskId + "] Начинаем полное обновление общего представления...");
            
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
            
            // Получаем статистику обновления
            printAggregationStats(taskId);
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка при обновлении общего представления: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Получение статистики по материализованному представлению
     */
    public void printAggregationStats() {
        printAggregationStats("STATS");
    }
    
    /**
     * Получение статистики по сегодняшним данным
     */
    public void printTodayStats() {
        printTodayStats("TODAY_STATS");
    }
    
    /**
     * Получение статистики по сегодняшним данным с taskId
     */
    private void printTodayStats(String taskId) {
        try {
            String todayStatsSql = """
                SELECT 
                    COUNT(*) as today_records,
                    COUNT(DISTINCT figi) as today_instruments,
                    SUM(morning_session_volume) as today_morning_volume,
                    SUM(main_session_volume) as today_main_volume,
                    SUM(evening_session_volume) as today_evening_volume,
                    SUM(weekend_session_volume) as today_weekend_volume,
                    SUM(total_volume) as today_total_volume,
                    AVG(morning_session_volume) as avg_today_morning_volume,
                    AVG(main_session_volume) as avg_today_main_volume,
                    AVG(evening_session_volume) as avg_today_evening_volume,
                    AVG(weekend_session_volume) as avg_today_weekend_volume,
                    AVG(total_volume) as avg_today_daily_volume,
                    MAX(last_updated) as last_updated
                FROM invest.today_volume_aggregation
                """;
            
            var stats = jdbcTemplate.queryForMap(todayStatsSql);
            
            System.out.println("[" + taskId + "] === СТАТИСТИКА ЗА СЕГОДНЯ (ДНЕВНОЕ ПРЕДСТАВЛЕНИЕ) ===");
            System.out.println("[" + taskId + "] Записей за сегодня: " + stats.get("today_records"));
            System.out.println("[" + taskId + "] Инструментов за сегодня: " + stats.get("today_instruments"));
            System.out.println("[" + taskId + "] Утренний объем: " + stats.get("today_morning_volume"));
            System.out.println("[" + taskId + "] Основной объем: " + stats.get("today_main_volume"));
            System.out.println("[" + taskId + "] Вечерний объем: " + stats.get("today_evening_volume"));
            System.out.println("[" + taskId + "] Выходной объем: " + stats.get("today_weekend_volume"));
            System.out.println("[" + taskId + "] Общий объем за сегодня: " + stats.get("today_total_volume"));
            System.out.println("[" + taskId + "] Последнее обновление: " + stats.get("last_updated"));
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка получения статистики за сегодня: " + e.getMessage());
        }
    }
    
    /**
     * Получение статистики по материализованному представлению с taskId
     */
    private void printAggregationStats(String taskId) {
        try {
            String statsSql = """
                SELECT 
                    COUNT(*) as total_records,
                    COUNT(DISTINCT figi) as unique_instruments,
                    MIN(trade_date) as earliest_date,
                    MAX(trade_date) as latest_date,
                    SUM(total_daily_volume) as total_volume_all_time,
                    AVG(total_daily_volume) as avg_daily_volume,
                    MAX(last_updated) as last_updated
                FROM invest.daily_volume_aggregation
                """;
            
            var stats = jdbcTemplate.queryForMap(statsSql);
            
            System.out.println("[" + taskId + "] === СТАТИСТИКА МАТЕРИАЛИЗОВАННОГО ПРЕДСТАВЛЕНИЯ ===");
            System.out.println("[" + taskId + "] Всего записей: " + stats.get("total_records"));
            System.out.println("[" + taskId + "] Уникальных инструментов: " + stats.get("unique_instruments"));
            System.out.println("[" + taskId + "] Период данных: " + stats.get("earliest_date") + " - " + stats.get("latest_date"));
            System.out.println("[" + taskId + "] Общий объем за все время: " + stats.get("total_volume_all_time"));
            System.out.println("[" + taskId + "] Средний дневной объем: " + stats.get("avg_daily_volume"));
            System.out.println("[" + taskId + "] Последнее обновление: " + stats.get("last_updated"));
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка получения статистики: " + e.getMessage());
        }
    }
    
    /**
     * Получение детальной статистики по сессиям
     */
    public Map<String, Object> getDetailedStats() {
        try {
            String statsSql = """
                SELECT 
                    COUNT(*) as total_records,
                    COUNT(DISTINCT figi) as unique_instruments,
                    MIN(trade_date) as earliest_date,
                    MAX(trade_date) as latest_date,
                    SUM(morning_session_volume) as total_morning_volume,
                    SUM(main_session_volume) as total_main_volume,
                    SUM(evening_session_volume) as total_evening_volume,
                    SUM(weekend_session_volume) as total_weekend_volume,
                    SUM(total_daily_volume) as total_volume_all_time,
                    AVG(morning_session_volume) as avg_morning_volume,
                    AVG(main_session_volume) as avg_main_volume,
                    AVG(evening_session_volume) as avg_evening_volume,
                    AVG(weekend_session_volume) as avg_weekend_volume,
                    AVG(total_daily_volume) as avg_daily_volume,
                    MAX(last_updated) as last_updated
                FROM invest.daily_volume_aggregation
                """;
            
            return jdbcTemplate.queryForMap(statsSql);
            
        } catch (Exception e) {
            System.err.println("Ошибка получения детальной статистики: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Проверка существования материализованного представления
     */
    public boolean isMaterializedViewExists() {
        try {
            String checkSql = """
                SELECT EXISTS (
                    SELECT 1 
                    FROM pg_matviews 
                    WHERE schemaname = 'invest' 
                    AND matviewname = 'daily_volume_aggregation'
                )
                """;
            
            Boolean exists = jdbcTemplate.queryForObject(checkSql, Boolean.class);
            return exists != null && exists;
            
        } catch (Exception e) {
            System.err.println("Ошибка проверки существования материализованного представления: " + e.getMessage());
            return false;
        }
    }
}
