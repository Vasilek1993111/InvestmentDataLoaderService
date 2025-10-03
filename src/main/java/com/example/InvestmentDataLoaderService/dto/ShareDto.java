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
   
}
