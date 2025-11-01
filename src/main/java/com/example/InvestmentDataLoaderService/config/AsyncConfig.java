package com.example.InvestmentDataLoaderService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Конфигурация для асинхронной обработки
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    
    /**
     * Executor для задач агрегации данных
     */
    @Bean("aggregationTaskExecutor")
    public Executor aggregationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("Aggregation-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * Executor для высокопараллельной записи свечей по одной
     */
    @Bean("candleTaskExecutor")
    public Executor candleTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Math.max(4, Runtime.getRuntime().availableProcessors()));
        executor.setMaxPoolSize(Math.max(8, Runtime.getRuntime().availableProcessors() * 2));
        executor.setQueueCapacity(20000);
        executor.setThreadNamePrefix("CandleWrite-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();
        return executor;
    }

    /**
     * Executor для параллельной загрузки минутных свечей по инструментам
     * Оптимизирован для обработки множества инструментов параллельно
     */
    @Bean("minuteCandleExecutor")
    public Executor minuteCandleExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        
        // Настройки для параллельной обработки инструментов
        executor.setCorePoolSize(Math.max(6, processors * 2));
        executor.setMaxPoolSize(Math.max(12, processors * 4));
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("MinuteCandle-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(300);
        
        // Политика отказа для переполнения очереди
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Настройки для оптимизации производительности
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        
        executor.initialize();
        return executor;
    }

    /**
     * Executor для параллельной загрузки данных из API
     * Оптимизирован для множественных запросов к внешнему API
     */
    @Bean("apiDataExecutor")
    public Executor apiDataExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        
        // Настройки для API запросов (учитываем ограничения API)
        executor.setCorePoolSize(Math.max(4, processors));
        executor.setMaxPoolSize(Math.max(8, processors * 2));
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("ApiData-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(180);
        
        // Политика отказа для API запросов
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Настройки для оптимизации API запросов
        executor.setKeepAliveSeconds(30);
        executor.setAllowCoreThreadTimeOut(true);
        
        executor.initialize();
        return executor;
    }

    /**
     * Executor для пакетной записи в БД
     * Оптимизирован для batch операций с базой данных
     */
    @Bean("batchWriteExecutor")
    public Executor batchWriteExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        
        // Настройки для batch операций с БД
        executor.setCorePoolSize(Math.max(2, processors / 2));
        executor.setMaxPoolSize(Math.max(4, processors));
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("BatchWrite-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(240);
        
        // Политика отказа для batch операций
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        return executor;
    }

    /**
     * Executor для параллельной загрузки дневных свечей по инструментам
     * Оптимизирован для обработки множества инструментов параллельно
     */
    @Bean("dailyCandleExecutor")
    public Executor dailyCandleExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        
        // Настройки для параллельной обработки инструментов
        executor.setCorePoolSize(Math.max(4, processors));
        executor.setMaxPoolSize(Math.max(8, processors * 2));
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("DailyCandle-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(300);
        
        // Политика отказа для переполнения очереди
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Настройки для оптимизации производительности
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        
        executor.initialize();
        return executor;
    }

    /**
     * Executor для параллельной загрузки данных из API для дневных свечей
     * Оптимизирован для множественных запросов к внешнему API
     */
    @Bean("dailyApiDataExecutor")
    public Executor dailyApiDataExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Настройки для API запросов (уменьшено количество потоков для соблюдения лимитов Tinkoff API)
        executor.setCorePoolSize(2); // Уменьшено до 2 потоков
        executor.setMaxPoolSize(4); // Уменьшено до 4 потоков максимум
        executor.setQueueCapacity(500); // Увеличена очередь для накопления задач
        executor.setThreadNamePrefix("DailyApiData-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(180);
        
        // Политика отказа для API запросов
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Настройки для оптимизации API запросов
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        
        executor.initialize();
        return executor;
    }

    /**
     * Executor для пакетной записи дневных свечей в БД
     * Оптимизирован для batch операций с базой данных
     */
    @Bean("dailyBatchWriteExecutor")
    public Executor dailyBatchWriteExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        
        // Настройки для batch операций с БД
        executor.setCorePoolSize(Math.max(2, processors / 2));
        executor.setMaxPoolSize(Math.max(4, processors));
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("DailyBatchWrite-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(240);
        
        // Политика отказа для batch операций
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        return executor;
    }

    /**
     * Executor для высокоскоростной загрузки LastTrades от Т-Инвест
     * Оптимизирован для максимальной пропускной способности API запросов
     */
    @Bean("lastTradesApiExecutor")
    public Executor lastTradesApiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        
        // Агрессивные настройки для максимальной скорости загрузки LastTrades
        executor.setCorePoolSize(Math.max(8, processors * 2));
        executor.setMaxPoolSize(Math.max(16, processors * 4));
        executor.setQueueCapacity(2000);
        executor.setThreadNamePrefix("LastTradesApi-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        
        // Политика отказа для максимальной пропускной способности
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Оптимизация для быстрой обработки
        executor.setKeepAliveSeconds(30);
        executor.setAllowCoreThreadTimeOut(false);
        
        executor.initialize();
        return executor;
    }

    /**
     * Executor для высокоскоростной записи LastTrades в БД
     * Оптимизирован для batch операций с максимальной производительностью
     */
    @Bean("lastTradesBatchExecutor")
    public Executor lastTradesBatchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        
        // Настройки для максимальной скорости записи LastTrades
        executor.setCorePoolSize(Math.max(6, processors));
        executor.setMaxPoolSize(Math.max(12, processors * 2));
        executor.setQueueCapacity(5000);
        executor.setThreadNamePrefix("LastTradesBatch-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(180);
        
        // Политика отказа для batch операций
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Оптимизация для быстрой записи
        executor.setKeepAliveSeconds(45);
        executor.setAllowCoreThreadTimeOut(false);
        
        executor.initialize();
        return executor;
    }

    /**
     * Executor для параллельной обработки LastTrades по инструментам
     * Оптимизирован для одновременной обработки множества инструментов
     */
    @Bean("lastTradesProcessingExecutor")
    public Executor lastTradesProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        
        // Настройки для параллельной обработки LastTrades
        executor.setCorePoolSize(Math.max(10, processors * 3));
        executor.setMaxPoolSize(Math.max(20, processors * 6));
        executor.setQueueCapacity(10000);
        executor.setThreadNamePrefix("LastTradesProc-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(300);
        
        // Политика отказа для обработки
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Оптимизация для параллельной обработки
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        
        executor.initialize();
        return executor;
    }
}
