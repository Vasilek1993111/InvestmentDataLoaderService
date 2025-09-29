package com.example.InvestmentDataLoaderService.dto;

import com.example.InvestmentDataLoaderService.enums.CurrencyType;
import com.example.InvestmentDataLoaderService.enums.DataSourceType;
import com.example.InvestmentDataLoaderService.enums.ExchangeType;
import com.example.InvestmentDataLoaderService.enums.InstrumentStatus;
import com.example.InvestmentDataLoaderService.exception.ValidationException;

/**
 * Параметры запроса для получения акций с валидацией
 */
public record SharesRequestParams(
    DataSourceType source,
    InstrumentStatus status,
    ExchangeType exchange,
    CurrencyType currency,
    String ticker,
    String figi
) {
    
    /**
     * Создает параметры запроса с валидацией
     * 
     * @param sourceStr строка источника данных
     * @param statusStr строка статуса инструмента
     * @param exchangeStr строка типа биржи
     * @param currency валюта
     * @param ticker тикер
     * @param figi FIGI
     * @return валидные параметры запроса
     * @throws ValidationException если параметры невалидны
     */
    public static SharesRequestParams create(
            String sourceStr,
            String statusStr,
            String exchangeStr,
            String currencyStr,
            String ticker,
            String figi
    ) throws ValidationException {
        
        // Валидация источника данных
        DataSourceType source = DataSourceType.fromString(sourceStr);
        if (source == null) {
            throw new ValidationException("Невалидный источник данных: " + sourceStr + 
                ". Допустимые значения: " + java.util.Arrays.toString(DataSourceType.values()));
        }
        
        // Валидация статуса инструмента (только для API)
        InstrumentStatus status = null;
        if (statusStr != null) {
            status = InstrumentStatus.fromString(statusStr);
            if (status == null) {
                throw new ValidationException("Невалидный статус инструмента: " + statusStr + 
                    ". Допустимые значения: " + java.util.Arrays.toString(InstrumentStatus.values()));
            }
        }
        
        // Валидация типа биржи
        ExchangeType exchange = null;
        if (exchangeStr != null) {
            exchange = ExchangeType.fromString(exchangeStr);
            if (exchange == null) {
                throw new ValidationException("Невалидный тип биржи: " + exchangeStr + 
                    ". Допустимые значения: " + java.util.Arrays.toString(ExchangeType.getAllValues()));
            }
        }
        
        // Валидация валюты
        CurrencyType currencyType = null;
        if (currencyStr != null) {
            currencyType = CurrencyType.fromString(currencyStr);
            if (currencyType == null) {
                throw new ValidationException("Невалидная валюта: " + currencyStr + 
                    ". Допустимые значения: " + java.util.Arrays.toString(CurrencyType.getAllValues()));
            }
        }
        
        return new SharesRequestParams(source, status, exchange, currencyType, ticker, figi);
    }
}
