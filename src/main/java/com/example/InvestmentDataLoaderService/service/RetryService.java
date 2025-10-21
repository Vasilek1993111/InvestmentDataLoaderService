package com.example.InvestmentDataLoaderService.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Сервис для выполнения операций с повторными попытками
 * Обрабатывает временные ошибки API, включая HTTP 429 (Too Many Requests)
 */
@Service
public class RetryService {

    private static final Logger log = LoggerFactory.getLogger(RetryService.class);
    
    // Максимальное количество попыток
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    // Базовая задержка между попытками (в миллисекундах)
    private static final long BASE_RETRY_DELAY_MS = 1000;
    
    // Максимальная задержка между попытками (в миллисекундах)
    private static final long MAX_RETRY_DELAY_MS = 10000;

    /**
     * Выполняет операцию с повторными попытками при ошибках
     * 
     * @param operation операция для выполнения
     * @param operationName название операции для логирования
     * @param <T> тип возвращаемого значения
     * @return результат операции
     * @throws Exception если все попытки завершились неудачей
     */
    public <T> T executeWithRetry(Supplier<T> operation, String operationName) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                log.info("[{}] Попытка {}/{}", operationName, attempt, MAX_RETRY_ATTEMPTS);
                T result = operation.get();
                
                if (attempt > 1) {
                    log.info("[{}] Успешно выполнено с попытки {}", operationName, attempt);
                }
                
                return result;
                
            } catch (Exception e) {
                lastException = e;
                
                // Проверяем, стоит ли повторять попытку
                if (!shouldRetry(e, attempt)) {
                    log.error("[{}] Критическая ошибка, повтор не требуется: {}", operationName, e.getMessage());
                    throw e;
                }
                
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    long delay = calculateRetryDelay(attempt);
                    log.error("[{}] Ошибка на попытке {}: {}", operationName, attempt, e.getMessage());
                    log.error("[{}] Повтор через {}ms", operationName, delay);
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Операция прервана", ie);
                    }
                } else {
                    log.error("[{}] Все попытки исчерпаны. Последняя ошибка: {}", operationName, e.getMessage());
                }
            }
        }
        
        throw new RuntimeException("Операция '" + operationName + "' не удалась после " + MAX_RETRY_ATTEMPTS + " попыток", lastException);
    }

    /**
     * Определяет, стоит ли повторять попытку при данной ошибке
     */
    private boolean shouldRetry(Exception e, int attempt) {
        String errorMessage = e.getMessage();
        
        // Повторяем при ошибках rate limiting (429)
        if (errorMessage != null && errorMessage.contains("429")) {
            return true;
        }
        
        // Повторяем при ошибках недоступности сервиса
        if (errorMessage != null && (
            errorMessage.contains("UNAVAILABLE") ||
            errorMessage.contains("DEADLINE_EXCEEDED") ||
            errorMessage.contains("RESOURCE_EXHAUSTED")
        )) {
            return true;
        }
        
        // Повторяем при сетевых ошибках
        if (errorMessage != null && (
            errorMessage.contains("Connection") ||
            errorMessage.contains("Timeout") ||
            errorMessage.contains("Network")
        )) {
            return true;
        }
        
        // Не повторяем при ошибках валидации или авторизации
        if (errorMessage != null && (
            errorMessage.contains("INVALID_ARGUMENT") ||
            errorMessage.contains("UNAUTHENTICATED") ||
            errorMessage.contains("PERMISSION_DENIED")
        )) {
            return false;
        }
        
        // По умолчанию повторяем только первые попытки
        return attempt < MAX_RETRY_ATTEMPTS;
    }

    /**
     * Вычисляет задержку перед следующей попыткой с экспоненциальным backoff
     */
    private long calculateRetryDelay(int attempt) {
        // Экспоненциальный backoff с jitter
        long exponentialDelay = BASE_RETRY_DELAY_MS * (1L << (attempt - 1));
        
        // Добавляем случайный jitter (±25%)
        long jitter = ThreadLocalRandom.current().nextLong(
            exponentialDelay * 75 / 100,
            exponentialDelay * 125 / 100
        );
        
        // Ограничиваем максимальной задержкой
        return Math.min(jitter, MAX_RETRY_DELAY_MS);
    }

    /**
     * Выполняет операцию с повторными попытками и rate limiting
     * 
     * @param operation операция для выполнения
     * @param operationName название операции
     * @param operationType тип операции для rate limiting
     * @param rateLimitService сервис rate limiting
     * @param <T> тип возвращаемого значения
     * @return результат операции
     * @throws Exception если операция завершилась с ошибкой
     */
    public <T> T executeWithRetryAndRateLimit(
            Supplier<T> operation, 
            String operationName, 
            String operationType,
            RateLimitService rateLimitService) throws Exception {
        
        return executeWithRetry(() -> {
            try {
                return rateLimitService.executeWithRateLimit(operationType, operation::get);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, operationName);
    }
}

