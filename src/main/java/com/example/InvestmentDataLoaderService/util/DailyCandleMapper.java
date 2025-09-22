package com.example.InvestmentDataLoaderService.util;

import com.example.InvestmentDataLoaderService.dto.DailyCandleExtendedDto;
import com.example.InvestmentDataLoaderService.entity.DailyCandleEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.IndicativeEntity;


/**
 * Утилитный класс для маппинга дневных свечей в расширенные DTO
 */
public class DailyCandleMapper {
    
    /**
     * Конвертирует DailyCandleEntity в DailyCandleExtendedDto с информацией об инструменте
     */
    public static DailyCandleExtendedDto toExtendedDto(DailyCandleEntity entity, ShareEntity share) {
        if (entity == null) {
            return null;
        }
        
        return new DailyCandleExtendedDto(
            entity.getFigi(),
            share != null ? share.getTicker() : null,
            share != null ? share.getName() : null,
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
     * Конвертирует DailyCandleEntity в DailyCandleExtendedDto с информацией о фьючерсе
     */
    public static DailyCandleExtendedDto toExtendedDto(DailyCandleEntity entity, FutureEntity future) {
        if (entity == null) {
            return null;
        }
        
        return new DailyCandleExtendedDto(
            entity.getFigi(),
            future != null ? future.getTicker() : null,
            future != null ? future.getTicker() : null, // У фьючерсов нет отдельного поля name
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
     * Конвертирует DailyCandleEntity в DailyCandleExtendedDto с информацией об индикативе
     */
    public static DailyCandleExtendedDto toExtendedDto(DailyCandleEntity entity, IndicativeEntity indicative) {
        if (entity == null) {
            return null;
        }
        
        return new DailyCandleExtendedDto(
            entity.getFigi(),
            indicative != null ? indicative.getTicker() : null,
            indicative != null ? indicative.getName() : null,
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
     * Конвертирует DailyCandleEntity в DailyCandleExtendedDto без информации об инструменте
     */
    public static DailyCandleExtendedDto toExtendedDto(DailyCandleEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return new DailyCandleExtendedDto(
            entity.getFigi(),
            null, // ticker
            null, // name
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
}
