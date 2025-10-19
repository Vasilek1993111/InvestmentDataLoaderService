package com.example.InvestmentDataLoaderService.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ClosePriceEveningSessionDto(
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate priceDate,
    String figi,
    BigDecimal closePrice,
    String instrumentType,
    String currency,
    String exchange
) {}