package com.example.InvestmentDataLoaderService.exception;

/**
 * Базовое исключение для ошибок загрузки данных
 */
public class DataLoadException extends RuntimeException {
    
    public DataLoadException(String message) {
        super(message);
    }
    
    public DataLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
