package com.example.InvestmentDataLoaderService.dto;


import java.math.BigDecimal;

public record ClosePriceDto(
    String figi,
    String tradingDate,
    BigDecimal closePrice,
    BigDecimal eveningSessionPrice
) {
    // Конструктор для совместимости с существующим кодом (без eveningSessionPrice)
    public ClosePriceDto(String figi, String tradingDate, BigDecimal closePrice) {
        this(figi, tradingDate, closePrice, null);
    }
}