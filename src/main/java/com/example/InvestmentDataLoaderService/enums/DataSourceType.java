package com.example.InvestmentDataLoaderService.enums;

/**
 * Тип источника данных для получения инструментов
 */
public enum DataSourceType {
    /**
     * Получение данных из Tinkoff API
     */
    API,
    
    /**
     * Получение данных из локальной базы данных
     */
    DATABASE;
    
    /**
     * Проверяет, является ли строка валидным типом источника данных
     * 
     * @param value строка для проверки
     * @return true если валидный тип, false иначе
     */
    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        try {
            valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Возвращает тип источника данных по строке или null если невалидный
     * 
     * @param value строка типа источника
     * @return тип источника или null
     */
    public static DataSourceType fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
