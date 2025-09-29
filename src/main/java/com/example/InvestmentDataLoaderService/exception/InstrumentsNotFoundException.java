package com.example.InvestmentDataLoaderService.exception;

/**
 * Исключение, выбрасываемое когда инструменты не найдены
 */
public class InstrumentsNotFoundException extends DataLoadException {
    
    public InstrumentsNotFoundException(String message) {
        super(message);
    }
    
    public InstrumentsNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
