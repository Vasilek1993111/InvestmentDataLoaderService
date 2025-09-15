package com.example.InvestmentDataLoaderService.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CandleDto(
    String figi,
    long volume,
    BigDecimal high,
    BigDecimal low,
    Instant time,
    BigDecimal close,
    BigDecimal open,
    boolean isComplete
) {}