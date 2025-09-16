package com.example.InvestmentDataLoaderService.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO для аналитики по торговым сессиям
 */
public record SessionAnalyticsDto(
    @JsonProperty("figi") String figi,
    @JsonProperty("trade_date") LocalDate tradeDate,
    
    // Общие метрики
    @JsonProperty("total_volume") Long totalVolume,
    @JsonProperty("total_candles") Long totalCandles,
    @JsonProperty("avg_volume_per_candle") BigDecimal avgVolumePerCandle,
    
    // Утренняя сессия (09:00-10:00)
    @JsonProperty("morning_session_volume") Long morningSessionVolume,
    @JsonProperty("morning_session_candles") Long morningSessionCandles,
    @JsonProperty("morning_avg_volume_per_candle") BigDecimal morningAvgVolumePerCandle,
    
    // Основная сессия (10:00-18:45)
    @JsonProperty("main_session_volume") Long mainSessionVolume,
    @JsonProperty("main_session_candles") Long mainSessionCandles,
    @JsonProperty("main_avg_volume_per_candle") BigDecimal mainAvgVolumePerCandle,
    
    // Вечерняя сессия (19:05-23:50)
    @JsonProperty("evening_session_volume") Long eveningSessionVolume,
    @JsonProperty("evening_session_candles") Long eveningSessionCandles,
    @JsonProperty("evening_avg_volume_per_candle") BigDecimal eveningAvgVolumePerCandle,
    
    // Сессия выходного дня
    @JsonProperty("weekend_session_volume") Long weekendSessionVolume,
    @JsonProperty("weekend_session_candles") Long weekendSessionCandles,
    @JsonProperty("weekend_avg_volume_per_candle") BigDecimal weekendAvgVolumePerCandle
) {
}
