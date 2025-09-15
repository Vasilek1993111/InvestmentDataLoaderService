package com.example.InvestmentDataLoaderService.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record LastTradeDto(
    String figi,
    String direction,
    BigDecimal price,
    long quantity,
    Instant time,
    String tradeSource
) {}