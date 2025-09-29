package com.example.InvestmentDataLoaderService.enums;

/**
 * Статус инструмента согласно Tinkoff API
 * 
 * @see <a href="https://developer.tbank.ru/invest/api/instruments-service-shares">Tinkoff API Documentation</a>
 */
public enum InstrumentStatus {
    /**
     * Значение не определено
     */
    INSTRUMENT_STATUS_UNSPECIFIED,
    
    /**
     * Базовый список инструментов, которыми можно торговать через T-Invest API
     */
    INSTRUMENT_STATUS_BASE,
    
    /**
     * Список всех инструментов
     */
    INSTRUMENT_STATUS_ALL;
    
    /**
     * Проверяет, является ли строка валидным статусом инструмента
     * 
     * @param value строка для проверки
     * @return true если валидный статус, false иначе
     */
    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        try {
            valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Возвращает статус по строке или null если невалидный
     * 
     * @param value строка статуса
     * @return статус или null
     */
    public static InstrumentStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
