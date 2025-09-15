package com.example.InvestmentDataLoaderService.dto;

public record TradingDayDto(
    String date,
    boolean isTradingDay,
    String startTime,
    String endTime
) {}