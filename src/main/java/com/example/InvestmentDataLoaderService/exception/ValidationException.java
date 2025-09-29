package com.example.InvestmentDataLoaderService.exception;

/**
 * Исключение для ошибок валидации
 */
public class ValidationException extends DataLoadException {
    
    private final String field;
    
    public ValidationException(String message) {
        super(message);
        this.field = null;
    }
    
    public ValidationException(String message, String field) {
        super(message);
        this.field = field;
    }
    
    public ValidationException(String message, String field, Throwable cause) {
        super(message, cause);
        this.field = field;
    }
    
    public String getField() {
        return field;
    }
}
