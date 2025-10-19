package com.example.InvestmentDataLoaderService.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Конфигурация для rate limiting
 */
@Configuration
@EnableConfigurationProperties(RateLimitConfig.RateLimitProperties.class)
public class RateLimitConfig {

    @Bean
    public RateLimitProperties rateLimitProperties() {
        return new RateLimitProperties();
    }

    @Bean
    public BatchProcessingProperties batchProcessingProperties() {
        return new BatchProcessingProperties();
    }

    @ConfigurationProperties(prefix = "rate-limit")
    public static class RateLimitProperties {
        private int maxConcurrentRequests = 5;
        private long minRequestIntervalMs = 100;
        private int maxRetryAttempts = 3;
        private long baseRetryDelayMs = 1000;
        private long maxRetryDelayMs = 10000;

        // Getters and setters
        public int getMaxConcurrentRequests() { return maxConcurrentRequests; }
        public void setMaxConcurrentRequests(int maxConcurrentRequests) { this.maxConcurrentRequests = maxConcurrentRequests; }

        public long getMinRequestIntervalMs() { return minRequestIntervalMs; }
        public void setMinRequestIntervalMs(long minRequestIntervalMs) { this.minRequestIntervalMs = minRequestIntervalMs; }

        public int getMaxRetryAttempts() { return maxRetryAttempts; }
        public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }

        public long getBaseRetryDelayMs() { return baseRetryDelayMs; }
        public void setBaseRetryDelayMs(long baseRetryDelayMs) { this.baseRetryDelayMs = baseRetryDelayMs; }

        public long getMaxRetryDelayMs() { return maxRetryDelayMs; }
        public void setMaxRetryDelayMs(long maxRetryDelayMs) { this.maxRetryDelayMs = maxRetryDelayMs; }
    }

    @ConfigurationProperties(prefix = "batch-processing")
    public static class BatchProcessingProperties {
        private int batchSize = 100;
        private long batchDelayMs = 500;

        // Getters and setters
        public int getBatchSize() { return batchSize; }
        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }

        public long getBatchDelayMs() { return batchDelayMs; }
        public void setBatchDelayMs(long batchDelayMs) { this.batchDelayMs = batchDelayMs; }
    }
}
