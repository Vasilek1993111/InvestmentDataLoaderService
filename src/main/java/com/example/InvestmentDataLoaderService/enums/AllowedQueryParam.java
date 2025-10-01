package com.example.InvestmentDataLoaderService.enums;

/**
 * Разрешенные GET параметры для API
 */
public enum AllowedQueryParam {
    /**
     * Источник данных
     */
    SOURCE("source"),
    
    /**
     * Статус инструмента
     */
    STATUS("status"),
    
    /**
     * Биржа
     */
    EXCHANGE("exchange"),
    
    /**
     * Валюта
     */
    CURRENCY("currency"),
    
    /**
     * Тикер
     */
    TICKER("ticker"),
    
    /**
     * FIGI
     */
    FIGI("figi"),
    
    /**
     * Сектор (только для акций)
     */
    SECTOR("sector"),
    
    /**
     * Торговый статус (только для акций)
     */
    TRADING_STATUS("tradingStatus"),
    
    /**
     * Тип актива (только для фьючерсов)
     */
    ASSET_TYPE("assetType"),

 /**
     * Начальная дата (для торговых расписаний)
     */
    FROM("from"),
    
    /**
     * Конечная дата (для торговых расписаний)
     */
    TO("to");
    
    
    private final String value;
    
    AllowedQueryParam(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }


    public static String[] getTradingSchedulesParams() {
        return new String[]{
            EXCHANGE.value, FROM.value, TO.value
        };
    }

    /**
     * Проверяет, является ли строка разрешенным параметром
     * 
     * @param paramName имя параметра для проверки
     * @return true если параметр разрешен, false иначе
     */
    public static boolean isAllowed(String paramName) {
        if (paramName == null) {
            return false;
        }
        for (AllowedQueryParam param : values()) {
            if (param.value.equalsIgnoreCase(paramName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Возвращает все разрешенные параметры
     * 
     * @return массив строк с разрешенными параметрами
     */
    public static String[] getAllAllowedParams() {
        AllowedQueryParam[] params = values();
        String[] result = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            result[i] = params[i].value;
        }
        return result;
    }
    
    /**
     * Возвращает разрешенные параметры для акций
     * 
     * @return массив строк с параметрами для акций
     */
    public static String[] getSharesParams() {
        return new String[]{
            SOURCE.value, STATUS.value, EXCHANGE.value, CURRENCY.value, 
            TICKER.value, FIGI.value, SECTOR.value, TRADING_STATUS.value
        };
    }
    
    /**
     * Возвращает разрешенные параметры для фьючерсов
     * 
     * @return массив строк с параметрами для фьючерсов
     */
    public static String[] getFuturesParams() {
        return new String[]{
            STATUS.value, EXCHANGE.value, CURRENCY.value, 
            TICKER.value, ASSET_TYPE.value
        };
    }
    
    /**
     * Возвращает разрешенные параметры для индикативов
     * 
     * @return массив строк с параметрами для индикативов
     */
    public static String[] getIndicativesParams() {
        return new String[]{
            EXCHANGE.value, CURRENCY.value, TICKER.value, FIGI.value
        };
    }
}
