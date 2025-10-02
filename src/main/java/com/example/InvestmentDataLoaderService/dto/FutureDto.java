package com.example.InvestmentDataLoaderService.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record FutureDto(
    String figi,
    String ticker,
    String assetType,
    String basicAsset,
    String currency,
    String exchange,    
    Boolean shortEnabled,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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