package com.example.InvestmentDataLoaderService.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClosePriceEveningSessionDto(
    LocalDate priceDate,
    String figi,
    BigDecimal closePrice,
    String instrumentType,
    String currency,
    String exchange
) {}