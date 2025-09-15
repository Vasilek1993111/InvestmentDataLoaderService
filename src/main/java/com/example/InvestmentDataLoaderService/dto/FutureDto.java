package com.example.InvestmentDataLoaderService.dto;

public record FutureDto(
    String figi,
    String ticker,
    String assetType,
    String basicAsset,
    String currency,
    String exchange,
    String stockTicker
) {}