package com.example.InvestmentDataLoaderService.dto;

public record ShareDto(
    String figi,
    String ticker,
    String name,
    String currency,
    String exchange,
    String sector,
    String tradingStatus,
    Boolean shortEnabled
) {
    // Конструктор для совместимости с существующим кодом (без новых полей)
    public ShareDto(String figi, String ticker, String name, String currency, String exchange) {
        this(figi, ticker, name, currency, exchange, null, null, null);
    }

    // Совместимость с кодом, ожидающим 7 аргументов (без shortEnabled)
    public ShareDto(String figi,
                    String ticker,
                    String name,
                    String currency,
                    String exchange,
                    String sector,
                    String tradingStatus) {
        this(figi, ticker, name, currency, exchange, sector, tradingStatus, null);
    }
}