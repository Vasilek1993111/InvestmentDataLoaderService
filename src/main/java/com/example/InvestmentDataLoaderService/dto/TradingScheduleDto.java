package com.example.InvestmentDataLoaderService.dto;

import java.util.List;

public record TradingScheduleDto(
    String exchange,
    List<TradingDayDto> days
) {}