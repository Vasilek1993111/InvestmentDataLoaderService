package com.example.InvestmentDataLoaderService.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Расширенный DTO для минутных свечей с дополнительной статистикой
 */
public record MinuteCandleExtendedDto(
        @JsonProperty("figi")
        String figi,
        
        @JsonProperty("ticker")
        String ticker,
        
        @JsonProperty("name")
        String name,
        
        @JsonProperty("time")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant time,
        
        @JsonProperty("open")
        BigDecimal open,
        
        @JsonProperty("close")
        BigDecimal close,
        
        @JsonProperty("high")
        BigDecimal high,
        
        @JsonProperty("low")
        BigDecimal low,
        
        @JsonProperty("volume")
        Long volume,
        
        @JsonProperty("isComplete")
        Boolean isComplete,
        
        // Расширенная статистика
        @JsonProperty("priceChange")
        BigDecimal priceChange,
        
        @JsonProperty("priceChangePercent")
        BigDecimal priceChangePercent,
        
        @JsonProperty("candleType")
        String candleType,
        
        @JsonProperty("bodySize")
        BigDecimal bodySize,
        
        @JsonProperty("upperShadow")
        BigDecimal upperShadow,
        
        @JsonProperty("lowerShadow")
        BigDecimal lowerShadow,
        
        @JsonProperty("highLowRange")
        BigDecimal highLowRange,
        
        @JsonProperty("averagePrice")
        BigDecimal averagePrice
) {
    
    /**
     * Создает DTO из базовых данных свечи с вычислением расширенной статистики
     */
    public static MinuteCandleExtendedDto fromBasicData(String figi, String ticker, String name, 
                                                      Instant time, BigDecimal open, BigDecimal close, 
                                                      BigDecimal high, BigDecimal low, Long volume, 
                                                      Boolean isComplete) {
        
        // Вычисляем расширенную статистику
        BigDecimal priceChange = close.subtract(open);
        BigDecimal priceChangePercent = open.compareTo(BigDecimal.ZERO) > 0 
            ? priceChange.divide(open, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        String candleType = close.compareTo(open) > 0 ? "BULLISH" : 
                           close.compareTo(open) < 0 ? "BEARISH" : "DOJI";
        
        BigDecimal bodySize = priceChange.abs();
        BigDecimal upperShadow = high.subtract(close.max(open));
        BigDecimal lowerShadow = open.min(close).subtract(low);
        BigDecimal highLowRange = high.subtract(low);
        BigDecimal averagePrice = high.add(low).add(open).add(close).divide(BigDecimal.valueOf(4), 2, java.math.RoundingMode.HALF_UP);
        
        return new MinuteCandleExtendedDto(
            figi, ticker, name, time, open, close, high, low, volume, isComplete,
            priceChange, priceChangePercent, candleType, bodySize, upperShadow, 
            lowerShadow, highLowRange, averagePrice
        );
    }
}
