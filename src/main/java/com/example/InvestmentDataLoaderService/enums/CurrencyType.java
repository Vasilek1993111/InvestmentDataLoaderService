package com.example.InvestmentDataLoaderService.enums;

/**
 * Типы валют, поддерживаемые в системе
 */
public enum CurrencyType {
    /**
     * Российский рубль (заглавными буквами)
     */
    RUB("RUB"),
    
    /**
     * Российский рубль (строчными буквами)
     */
    RUB_LOWER("rub"),
    
    /**
     * Американский доллар (заглавными буквами)
     */
    USD("USD"),
    
    /**
     * Американский доллар (строчными буквами)
     */
    USD_LOWER("usd");
    
    private final String value;
    
    CurrencyType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Проверяет, является ли строка валидной валютой
     * 
     * @param value строка для проверки
     * @return true если валидная валюта, false иначе
     */
    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        for (CurrencyType currency : values()) {
            if (currency.value.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Возвращает тип валюты по строке или null если невалидный
     * 
     * @param value строка валюты
     * @return тип валюты или null
     */
    public static CurrencyType fromString(String value) {
        if (value == null) {
            return null;
        }
        for (CurrencyType currency : values()) {
            if (currency.value.equalsIgnoreCase(value)) {
                return currency;
            }
        }
        return null;
    }
    
    /**
     * Возвращает все допустимые значения валют
     * 
     * @return массив строк с допустимыми значениями
     */
    public static String[] getAllValues() {
        CurrencyType[] currencies = values();
        String[] result = new String[currencies.length];
        for (int i = 0; i < currencies.length; i++) {
            result[i] = currencies[i].value;
        }
        return result;
    }
}
