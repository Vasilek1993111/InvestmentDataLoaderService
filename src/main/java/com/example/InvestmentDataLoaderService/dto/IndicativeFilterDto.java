package com.example.InvestmentDataLoaderService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO для фильтрации индикативных инструментов
 * Согласно документации Tinkoff Invest API: https://developer.tbank.ru/invest/services/instruments/methods
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicativeFilterDto {
    private String exchange;
    private String currency;
    private String ticker;
    private String figi;
}