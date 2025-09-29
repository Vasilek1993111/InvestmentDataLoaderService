package com.example.InvestmentDataLoaderService.dto;

import com.example.InvestmentDataLoaderService.enums.CurrencyType;
import com.example.InvestmentDataLoaderService.enums.ExchangeType;
import com.example.InvestmentDataLoaderService.enums.InstrumentStatus;
import com.example.InvestmentDataLoaderService.exception.ValidationException;

/**
 * Параметры фильтра для акций с валидацией
 */
public record ShareFilterRequestParams(
    InstrumentStatus status,
    ExchangeType exchange,
    CurrencyType currency,
    String ticker,
    String figi,
    String sector,
    String tradingStatus
) {
    
    /**
     * Создает параметры фильтра с валидацией
     * 
     * @param statusStr строка статуса инструмента
     * @param exchangeStr строка биржи
     * @param currencyStr строка валюты
     * @param ticker тикер
     * @param figi FIGI
     * @param sector сектор
     * @param tradingStatusStr строка статуса торговли
     * @return валидные параметры фильтра
     * @throws ValidationException если параметры невалидны
     */
    public static ShareFilterRequestParams create(
            String statusStr,
            String exchangeStr,
            String currencyStr,
            String ticker,
            String figi,
            String sector,
            String tradingStatusStr
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
        
        return new ShareFilterRequestParams(status, exchange, currency, ticker, figi, sector, tradingStatusStr);
    }
}
