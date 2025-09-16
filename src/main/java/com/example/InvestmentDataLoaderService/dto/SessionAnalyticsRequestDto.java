package com.example.InvestmentDataLoaderService.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

/**
 * DTO для запроса аналитики по торговым сессиям
 */
public record SessionAnalyticsRequestDto(
    @JsonProperty("figi") String figi,
    @JsonProperty("start_date") LocalDate startDate,
    @JsonProperty("end_date") LocalDate endDate
) {
}
