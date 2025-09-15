package com.example.InvestmentDataLoaderService.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OpenPriceDto(
    String figi,
    LocalDate priceDate,
    BigDecimal openPrice,
    String instrumentType,
    String currency,
    String exchange
) {}