package com.example.InvestmentDataLoaderService.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Сервис для управления ограничением скорости запросов к API
 * Предотвращает превышение лимитов API (HTTP 429)
 */
@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);
    
    // Семафор для ограничения количества одновременных запросов
    private final Semaphore apiSemaphore;
    
    // Карта для отслеживания времени последнего запроса для каждого типа операции
    private final ConcurrentHashMap<String, Instant> lastRequestTimes = new ConcurrentHashMap<>();
    
    // Минимальный интервал между запросами (в миллисекундах)
    private static final long MIN_REQUEST_INTERVAL_MS = 100; // 100ms между запросами
    
    // Максимальное количество одновременных запросов
    private static final int MAX_CONCURRENT_REQUESTS = 5;

    public RateLimitService() {
        this.apiSemaphore = new Semaphore(MAX_CONCURRENT_REQUESTS);
    }

    /**
     * Ожидает разрешения на выполнение запроса к API
     * 
     * @param operationType тип операции (например, "close_prices", "instruments")
     * @throws InterruptedException если поток был прерван во время ожидания
     */
    public void acquirePermission(String operationType) throws InterruptedException {
        // Получаем разрешение на выполнение запроса
        apiSemaphore.acquire();
        
        try {
            // Проверяем минимальный интервал между запросами
            waitForMinimumInterval(operationType);
            
            // Обновляем время последнего запроса
            lastRequestTimes.put(operationType, Instant.now());
            
        } catch (InterruptedException e) {
            // Если произошла ошибка, освобождаем семафор
            apiSemaphore.release();
            throw e;
        }
    }

    /**
     * Освобождает разрешение после завершения запроса
     */
    public void releasePermission() {
        apiSemaphore.release();
    }

    /**
     * Выполняет операцию с автоматическим управлением rate limiting
     * 
     * @param operationType тип операции
     * @param operation операция для выполнения
     * @param <T> тип возвращаемого значения
     * @return результат операции
     * @throws Exception если операция завершилась с ошибкой
     */
    public <T> T executeWithRateLimit(String operationType, RateLimitedOperation<T> operation) throws Exception {
        acquirePermission(operationType);
        try {
            return operation.execute();
        } finally {
            releasePermission();
        }
    }

    /**
     * Ожидает минимальный интервал между запросами
     */
    private void waitForMinimumInterval(String operationType) throws InterruptedException {
        Instant lastRequest = lastRequestTimes.get(operationType);
        if (lastRequest != null) {
            Instant now = Instant.now();
            Duration elapsed = Duration.between(lastRequest, now);
            
            if (elapsed.toMillis() < MIN_REQUEST_INTERVAL_MS) {
                long waitTime = MIN_REQUEST_INTERVAL_MS - elapsed.toMillis();
                System.out.println("Rate limiting: ожидание " + waitTime + "ms перед следующим запросом для " + operationType);
                Thread.sleep(waitTime);
            }
        }
    }

    /**
     * Получает статистику по rate limiting
     */
    public RateLimitStats getStats() {
        return new RateLimitStats(
            apiSemaphore.availablePermits(),
            MAX_CONCURRENT_REQUESTS,
            lastRequestTimes.size()
        );
    }

    /**
     * Функциональный интерфейс для операций с rate limiting
     */
    @FunctionalInterface
    public interface RateLimitedOperation<T> {
        T execute() throws Exception;
    }

    /**
     * Статистика по rate limiting
     */
    public static class RateLimitStats {
        private final int availablePermits;
        private final int maxPermits;
        private final int activeOperationTypes;

        public RateLimitStats(int availablePermits, int maxPermits, int activeOperationTypes) {
            this.availablePermits = availablePermits;
            this.maxPermits = maxPermits;
            this.activeOperationTypes = activeOperationTypes;
        }

        public int getAvailablePermits() { return availablePermits; }
        public int getMaxPermits() { return maxPermits; }
        public int getActiveOperationTypes() { return activeOperationTypes; }
        public int getUsedPermits() { return maxPermits - availablePermits; }
    }
}

