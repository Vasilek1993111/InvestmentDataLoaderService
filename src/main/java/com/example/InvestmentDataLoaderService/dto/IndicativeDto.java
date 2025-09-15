package com.example.InvestmentDataLoaderService.dto;

/**
 * DTO для индикативных инструментов (индексы, товары и другие)
 * Согласно документации Tinkoff Invest API: https://developer.tbank.ru/invest/services/instruments/methods
 */
public record IndicativeDto(
    String figi,
    String ticker,
    String name,
    String currency,
    String exchange,
    String classCode,
    String uid,
    Boolean sellAvailableFlag,
    Boolean buyAvailableFlag
) {
    /**
     * Конструктор для основных полей (для совместимости с существующим кодом)
     */
    public IndicativeDto(String figi, String ticker, String name, String currency, String exchange) {
        this(figi, ticker, name, currency, exchange, null, null, null, null);
    }
}