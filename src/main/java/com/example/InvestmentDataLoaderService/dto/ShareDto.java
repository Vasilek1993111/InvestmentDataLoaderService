package com.example.InvestmentDataLoaderService.dto;

public record ShareDto(
    String figi,
    String ticker,
    String name,
    String currency,
    String exchange,
    String sector,
    String tradingStatus
) {
    // Конструктор для совместимости с существующим кодом (без sector и tradingStatus)
    public ShareDto(String figi, String ticker, String name, String currency, String exchange) {
        this(figi, ticker, name, currency, exchange, null, null);
    }
}