package com.example.InvestmentDataLoaderService.dto;

import com.example.InvestmentDataLoaderService.enums.AssetType;
import com.example.InvestmentDataLoaderService.enums.CurrencyType;
import com.example.InvestmentDataLoaderService.enums.ExchangeType;
import com.example.InvestmentDataLoaderService.enums.InstrumentStatus;
import com.example.InvestmentDataLoaderService.exception.ValidationException;

/**
 * Параметры запроса для получения фьючерсов с валидацией
 */
public record FuturesRequestParams(
    InstrumentStatus status,
    ExchangeType exchange,
    CurrencyType currency,
    String ticker,
    AssetType assetType
) {
    
    /**
     * Создает параметры запроса с валидацией
     * 
     * @param statusStr строка статуса инструмента
     * @param exchangeStr строка биржи
     * @param currencyStr строка валюты
     * @param ticker тикер
     * @param assetTypeStr строка типа актива
     * @return валидные параметры запроса
     * @throws ValidationException если параметры невалидны
     */
    public static FuturesRequestParams create(
            String statusStr,
            String exchangeStr,
            String currencyStr,
            String ticker,
            String assetTypeStr
    ) throws ValidationException {
        
        // Валидация статуса инструмента
        InstrumentStatus status = null;
        if (statusStr != null) {
            status = InstrumentStatus.fromString(statusStr);
            if (status == null) {
                throw new ValidationException("Невалидный статус инструмента: " + statusStr + 
                    ". Допустимые значения: " + java.util.Arrays.toString(InstrumentStatus.values()), "status");
            }
        }
        
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
        
        // Валидация типа актива
        AssetType assetType = null;
        if (assetTypeStr != null) {
            assetType = AssetType.fromString(assetTypeStr);
            if (assetType == null) {
                throw new ValidationException("Невалидный тип актива: " + assetTypeStr + 
                    ". Допустимые значения: " + java.util.Arrays.toString(AssetType.getAllValues()), "assetType");
            }
        }
        
        return new FuturesRequestParams(status, exchange, currency, ticker, assetType);
    }
}
