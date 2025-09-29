package com.example.InvestmentDataLoaderService.dto;

import com.example.InvestmentDataLoaderService.enums.CurrencyType;
import com.example.InvestmentDataLoaderService.enums.ExchangeType;
import com.example.InvestmentDataLoaderService.exception.ValidationException;

/**
 * Параметры фильтра для индикативных инструментов с валидацией
 */
public record IndicativeFilterRequestParams(
    ExchangeType exchange,
    CurrencyType currency,
    String ticker,
    String figi
) {
    
    /**
     * Создает параметры фильтра с валидацией
     * 
     * @param exchangeStr строка биржи
     * @param currencyStr строка валюты
     * @param ticker тикер
     * @param figi FIGI
     * @return валидные параметры фильтра
     * @throws ValidationException если параметры невалидны
     */
    public static IndicativeFilterRequestParams create(
            String exchangeStr,
            String currencyStr,
            String ticker,
            String figi
    ) throws ValidationException {
        
        // Валидация биржи
        ExchangeType exchange = null;
        if (exchangeStr != null) {
            exchange = ExchangeType.fromString(exchangeStr);
            if (exchange == null) {
                throw new ValidationException("Невалидная биржа: " + exchangeStr + 
                    ". Допустимые значения: " + java.util.Arrays.toString(ExchangeType.getAllValues()), "exchange");
            }
        }
        
        // Валидация валюты
        CurrencyType currency = null;
        if (currencyStr != null) {
            currency = CurrencyType.fromString(currencyStr);
            if (currency == null) {
                throw new ValidationException("Невалидная валюта: " + currencyStr + 
                    ". Допустимые значения: " + java.util.Arrays.toString(CurrencyType.getAllValues()), "currency");
            }
        }
        
        return new IndicativeFilterRequestParams(exchange, currency, ticker, figi);
    }
}
