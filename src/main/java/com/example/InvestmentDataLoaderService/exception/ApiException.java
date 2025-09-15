package com.example.InvestmentDataLoaderService.exception;

/**
 * Исключение для ошибок API
 */
public class ApiException extends DataLoadException {
    
    private final int statusCode;
    
    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    public ApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}
