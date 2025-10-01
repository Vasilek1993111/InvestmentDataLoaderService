package com.example.InvestmentDataLoaderService.dto;

import java.time.LocalDateTime;

public record FutureDto(
    String figi,
    String ticker,
    String assetType,
    String basicAsset,
    String currency,
    String exchange,
    Boolean shortEnabled,
    LocalDateTime expirationDate
) {
    // Совместимость с кодом, ожидающим 6 аргументов (без shortEnabled и expirationDate)
    public FutureDto(String figi,
                     String ticker,
                     String assetType,
                     String basicAsset,
                     String currency,
                     String exchange) {
        this(figi, ticker, assetType, basicAsset, currency, exchange, null, null);
    }
    
    // Совместимость с кодом, ожидающим 7 аргументов (без expirationDate)
    public FutureDto(String figi,
                     String ticker,
                     String assetType,
                     String basicAsset,
                     String currency,
                     String exchange,
                     Boolean shortEnabled) {
        this(figi, ticker, assetType, basicAsset, currency, exchange, shortEnabled, null);
    }
}