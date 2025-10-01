package com.example.InvestmentDataLoaderService.enums;

/**
 * Типы активов для фьючерсов
 */
public enum AssetType {
    /**
     * Товарные активы (нефть, золото, серебро и т.д.)
     */
    TYPE_COMMODITY("TYPE_COMMODITY"),
    TYPE_CURRENCY("TYPE_CURRENCY"),
    TYPE_INDEX("TYPE_INDEX"),
    TYPE_SECURITY("TYPE_SECURITY"),
    UNKNOWN("UNKNOWN");
    
    private final String value;
    
    AssetType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Проверяет, является ли строка валидным типом актива
     * 
     * @param value строка для проверки
     * @return true если валидный тип актива, false иначе
     */
    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        for (AssetType assetType : values()) {
            if (assetType.value.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Возвращает тип актива по строке или null если невалидный
     * 
     * @param value строка типа актива
     * @return тип актива или null
     */
    public static AssetType fromString(String value) {
        if (value == null) {
            return null;
        }
        for (AssetType assetType : values()) {
            if (assetType.value.equalsIgnoreCase(value)) {
                return assetType;
            }
        }
        return null;
    }
    
    /**
     * Возвращает все допустимые значения типов активов
     * 
     * @return массив строк с допустимыми значениями
     */
    public static String[] getAllValues() {
        AssetType[] assetTypes = values();
        String[] result = new String[assetTypes.length];
        for (int i = 0; i < assetTypes.length; i++) {
            result[i] = assetTypes[i].value;
        }
        return result;
    }
}
