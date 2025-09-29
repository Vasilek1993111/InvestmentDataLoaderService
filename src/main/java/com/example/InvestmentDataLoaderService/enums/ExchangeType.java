package com.example.InvestmentDataLoaderService.enums;

/**
 * Типы бирж, используемые в системе
 */
public enum ExchangeType {
    /**
     * Московская биржа - утренняя и вечерняя сессии, включая выходные и дилерские торги
     */
    MOEX_MRNG_EVNG_E_WKND_DLR("moex_mrng_evng_e_wknd_dlr"),
    
    /**
     * Московская биржа (сокращенное название)
     */
    MOEX("MOEX"),
    
    /**
     * Московская биржа (строчными буквами)
     */
    MOEX_LOWER("moex"),
    
    /**
     * Санкт-Петербургская биржа
     */
    SPB("SPB"),
    
    /**
     * FORTS - срочный рынок Московской биржи
     */
    FORTS_MAIN("FORTS_MAIN"),
    
    /**
     * FORTS - вечерняя сессия
     */
    FORTS_EVENING("FORTS_EVENING"),
    
    /**
     * FORTS - фьючерсы выходного дня
     */
    FORTS_FUTURES_WEEKEND("forts_futures_weekend"),
    
    /**
     * Неизвестная биржа
     */
    UNKNOWN("UNKNOWN");
    
    private final String value;
    
    ExchangeType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Проверяет, является ли строка валидной биржей
     * 
     * @param value строка для проверки
     * @return true если валидная биржа, false иначе
     */
    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        for (ExchangeType exchange : values()) {
            if (exchange.value.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Возвращает тип биржи по строке или null если невалидный
     * 
     * @param value строка биржи
     * @return тип биржи или null
     */
    public static ExchangeType fromString(String value) {
        if (value == null) {
            return null;
        }
        for (ExchangeType exchange : values()) {
            if (exchange.value.equalsIgnoreCase(value)) {
                return exchange;
            }
        }
        return null;
    }
    
    /**
     * Возвращает все допустимые значения бирж
     * 
     * @return массив строк с допустимыми значениями
     */
    public static String[] getAllValues() {
        ExchangeType[] exchanges = values();
        String[] result = new String[exchanges.length];
        for (int i = 0; i < exchanges.length; i++) {
            result[i] = exchanges[i].value;
        }
        return result;
    }
}
