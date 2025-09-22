package com.example.InvestmentDataLoaderService.util;

import com.example.InvestmentDataLoaderService.dto.CandleDto;
import com.example.InvestmentDataLoaderService.dto.MinuteCandleExtendedDto;
import com.example.InvestmentDataLoaderService.entity.MinuteCandleEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Маппер для конвертации между DTO и Entity для минутных свечей
 */
public class MinuteCandleMapper {

    /**
     * Конвертирует CandleDto в MinuteCandleEntity
     */
    public static MinuteCandleEntity toEntity(CandleDto candleDto) {
        if (candleDto == null) {
            return null;
        }

        return new MinuteCandleEntity(
            candleDto.figi(),
            candleDto.volume(),
            candleDto.high(),
            candleDto.low(),
            candleDto.time(),
            candleDto.close(),
            candleDto.open(),
            candleDto.isComplete()
        );
    }

    /**
     * Конвертирует MinuteCandleEntity в MinuteCandleExtendedDto
     */
    public static MinuteCandleExtendedDto toExtendedDto(MinuteCandleEntity entity) {
        if (entity == null) {
            return null;
        }

        return new MinuteCandleExtendedDto(
            entity.getFigi(),
            null, // ticker - будет определен по FIGI
            null, // name - будет определен по FIGI
            entity.getTime(),
            entity.getOpen(),
            entity.getClose(),
            entity.getHigh(),
            entity.getLow(),
            entity.getVolume(),
            entity.isComplete(),
            entity.getPriceChange(),
            entity.getPriceChangePercent(),
            entity.getCandleType(),
            entity.getBodySize(),
            entity.getUpperShadow(),
            entity.getLowerShadow(),
            entity.getHighLowRange(),
            entity.getAveragePrice()
        );
    }

    /**
     * Конвертирует MinuteCandleEntity в MinuteCandleExtendedDto с дополнительной информацией
     */
    public static MinuteCandleExtendedDto toExtendedDto(MinuteCandleEntity entity, String ticker, String name) {
        if (entity == null) {
            return null;
        }

        return new MinuteCandleExtendedDto(
            entity.getFigi(),
            ticker,
            name,
            entity.getTime(),
            entity.getOpen(),
            entity.getClose(),
            entity.getHigh(),
            entity.getLow(),
            entity.getVolume(),
            entity.isComplete(),
            entity.getPriceChange(),
            entity.getPriceChangePercent(),
            entity.getCandleType(),
            entity.getBodySize(),
            entity.getUpperShadow(),
            entity.getLowerShadow(),
            entity.getHighLowRange(),
            entity.getAveragePrice()
        );
    }

    /**
     * Конвертирует CandleDto в MinuteCandleExtendedDto с вычислением расширенной статистики
     */
    public static MinuteCandleExtendedDto toExtendedDto(CandleDto candleDto) {
        if (candleDto == null) {
            return null;
        }

        return MinuteCandleExtendedDto.fromBasicData(
            candleDto.figi(),
            null, // ticker - будет определен по FIGI
            null, // name - будет определен по FIGI
            candleDto.time(),
            candleDto.open(),
            candleDto.close(),
            candleDto.high(),
            candleDto.low(),
            candleDto.volume(),
            candleDto.isComplete()
        );
    }

    /**
     * Конвертирует CandleDto в MinuteCandleExtendedDto с дополнительной информацией
     */
    public static MinuteCandleExtendedDto toExtendedDto(CandleDto candleDto, String ticker, String name) {
        if (candleDto == null) {
            return null;
        }

        return MinuteCandleExtendedDto.fromBasicData(
            candleDto.figi(),
            ticker,
            name,
            candleDto.time(),
            candleDto.open(),
            candleDto.close(),
            candleDto.high(),
            candleDto.low(),
            candleDto.volume(),
            candleDto.isComplete()
        );
    }

    /**
     * Обновляет существующую Entity данными из CandleDto
     */
    public static void updateEntity(MinuteCandleEntity entity, CandleDto candleDto) {
        if (entity == null || candleDto == null) {
            return;
        }

        entity.setVolume(candleDto.volume());
        entity.setHigh(candleDto.high());
        entity.setLow(candleDto.low());
        entity.setTime(candleDto.time());
        entity.setClose(candleDto.close());
        entity.setOpen(candleDto.open());
        entity.setComplete(candleDto.isComplete());

        // Пересчитываем расширенную статистику
        entity.calculateExtendedStatistics();
    }

    /**
     * Вычисляет расширенную статистику для свечи
     */
    public static void calculateExtendedStatistics(MinuteCandleEntity entity) {
        if (entity == null || entity.getOpen() == null || entity.getClose() == null || 
            entity.getHigh() == null || entity.getLow() == null) {
            return;
        }

        BigDecimal open = entity.getOpen();
        BigDecimal close = entity.getClose();
        BigDecimal high = entity.getHigh();
        BigDecimal low = entity.getLow();

        BigDecimal priceChange = close.subtract(open);
        BigDecimal priceChangePercent = open.compareTo(BigDecimal.ZERO) > 0 
            ? priceChange.divide(open, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;

        String candleType = close.compareTo(open) > 0 ? "BULLISH" : 
                           close.compareTo(open) < 0 ? "BEARISH" : "DOJI";

        BigDecimal bodySize = priceChange.abs();
        BigDecimal upperShadow = high.subtract(close.max(open));
        BigDecimal lowerShadow = open.min(close).subtract(low);
        BigDecimal highLowRange = high.subtract(low);
        BigDecimal averagePrice = high.add(low).add(open).add(close).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP);

        entity.setPriceChange(priceChange);
        entity.setPriceChangePercent(priceChangePercent);
        entity.setCandleType(candleType);
        entity.setBodySize(bodySize);
        entity.setUpperShadow(upperShadow);
        entity.setLowerShadow(lowerShadow);
        entity.setHighLowRange(highLowRange);
        entity.setAveragePrice(averagePrice);
    }
}
