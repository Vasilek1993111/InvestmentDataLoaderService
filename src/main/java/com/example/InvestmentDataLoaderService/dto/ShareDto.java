package com.example.InvestmentDataLoaderService.dto;

import java.math.BigDecimal;

public record ShareDto(
    String figi,
    String ticker,
    String name,
    String currency,
    String exchange,
    String sector,
    String tradingStatus,
    Boolean shortEnabled,
    String assetUid,
    BigDecimal minPriceIncrement,
    Integer lot
) {
   
}
