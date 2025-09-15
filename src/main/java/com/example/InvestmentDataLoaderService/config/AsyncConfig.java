package com.example.InvestmentDataLoaderService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

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
}
