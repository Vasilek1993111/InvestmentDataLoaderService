package com.example.InvestmentDataLoaderService.util;

import com.example.InvestmentDataLoaderService.dto.AssetFundamentalDto;
import com.example.InvestmentDataLoaderService.entity.AssetFundamentalEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для конвертации между AssetFundamentalEntity и AssetFundamentalDto
 */
@Component
public class AssetFundamentalMapper {

    /**
     * Конвертация Entity в DTO
     */
    public AssetFundamentalDto toDto(AssetFundamentalEntity entity) {
        if (entity == null) {
            return null;
        }

        AssetFundamentalDto dto = new AssetFundamentalDto();
        
        // Основные поля
        dto.setAssetUid(entity.getAssetUid());
        dto.setDomicileIndicatorCode(entity.getDomicileIndicatorCode());
        dto.setCurrency(entity.getCurrency());
        
        // Рыночные данные
        dto.setMarketCapitalization(entity.getMarketCapitalization());
        dto.setHighPriceLast52Weeks(entity.getHighPriceLast52Weeks());
        dto.setLowPriceLast52Weeks(entity.getLowPriceLast52Weeks());
        dto.setAverageDailyVolumeLast10Days(entity.getAverageDailyVolumeLast10Days());
        dto.setAverageDailyVolumeLast4Weeks(entity.getAverageDailyVolumeLast4Weeks());
        dto.setBeta(entity.getBeta());
        dto.setFreeFloat(entity.getFreeFloat());
        dto.setForwardAnnualDividendYield(entity.getForwardAnnualDividendYield());
        dto.setSharesOutstanding(entity.getSharesOutstanding());
        
        // Финансовые показатели
        dto.setRevenueTtm(entity.getRevenueTtm());
        dto.setEbitdaTtm(entity.getEbitdaTtm());
        dto.setNetIncomeTtm(entity.getNetIncomeTtm());
        dto.setEpsTtm(entity.getEpsTtm());
        dto.setDilutedEpsTtm(entity.getDilutedEpsTtm());
        dto.setFreeCashFlowTtm(entity.getFreeCashFlowTtm());
        
        // Ростовые показатели
        dto.setFiveYearAnnualRevenueGrowthRate(entity.getFiveYearAnnualRevenueGrowthRate());
        dto.setThreeYearAnnualRevenueGrowthRate(entity.getThreeYearAnnualRevenueGrowthRate());
        dto.setOneYearAnnualRevenueGrowthRate(entity.getOneYearAnnualRevenueGrowthRate());
        
        // Финансовые коэффициенты
        dto.setPeRatioTtm(entity.getPeRatioTtm());
        dto.setPriceToSalesTtm(entity.getPriceToSalesTtm());
        dto.setPriceToBookTtm(entity.getPriceToBookTtm());
        dto.setPriceToFreeCashFlowTtm(entity.getPriceToFreeCashFlowTtm());
        dto.setTotalEnterpriseValueMrq(entity.getTotalEnterpriseValueMrq());
        dto.setEvToEbitdaMrq(entity.getEvToEbitdaMrq());
        dto.setEvToSales(entity.getEvToSales());
        
        // Показатели рентабельности
        dto.setNetMarginMrq(entity.getNetMarginMrq());
        dto.setNetInterestMarginMrq(entity.getNetInterestMarginMrq());
        dto.setRoe(entity.getRoe());
        dto.setRoa(entity.getRoa());
        dto.setRoic(entity.getRoic());
        
        // Долговые показатели
        dto.setTotalDebtMrq(entity.getTotalDebtMrq());
        dto.setTotalDebtToEquityMrq(entity.getTotalDebtToEquityMrq());
        dto.setTotalDebtToEbitdaMrq(entity.getTotalDebtToEbitdaMrq());
        dto.setFreeCashFlowToPrice(entity.getFreeCashFlowToPrice());
        dto.setNetDebtToEbitda(entity.getNetDebtToEbitda());
        dto.setCurrentRatioMrq(entity.getCurrentRatioMrq());
        dto.setFixedChargeCoverageRatioFy(entity.getFixedChargeCoverageRatioFy());
        
        // Дивидендные показатели
        dto.setDividendYieldDailyTtm(entity.getDividendYieldDailyTtm());
        dto.setDividendRateTtm(entity.getDividendRateTtm());
        dto.setDividendsPerShare(entity.getDividendsPerShare());
        dto.setFiveYearsAverageDividendYield(entity.getFiveYearsAverageDividendYield());
        dto.setFiveYearAnnualDividendGrowthRate(entity.getFiveYearAnnualDividendGrowthRate());
        dto.setDividendPayoutRatioFy(entity.getDividendPayoutRatioFy());
        
        // Другие показатели
        dto.setBuyBackTtm(entity.getBuyBackTtm());
        dto.setAdrToCommonShareRatio(entity.getAdrToCommonShareRatio());
        dto.setNumberOfEmployees(entity.getNumberOfEmployees());
        
        // Даты
        dto.setExDividendDate(entity.getExDividendDate());
        dto.setFiscalPeriodStartDate(entity.getFiscalPeriodStartDate());
        dto.setFiscalPeriodEndDate(entity.getFiscalPeriodEndDate());
        
        // Показатели изменений
        dto.setRevenueChangeFiveYears(entity.getRevenueChangeFiveYears());
        dto.setEpsChangeFiveYears(entity.getEpsChangeFiveYears());
        dto.setEbitdaChangeFiveYears(entity.getEbitdaChangeFiveYears());
        dto.setTotalDebtChangeFiveYears(entity.getTotalDebtChangeFiveYears());
        
        return dto;
    }

    /**
     * Конвертация DTO в Entity
     */
    public AssetFundamentalEntity toEntity(AssetFundamentalDto dto) {
        if (dto == null) {
            return null;
        }

        AssetFundamentalEntity entity = new AssetFundamentalEntity();
        
        // Основные поля
        entity.setAssetUid(dto.getAssetUid());
        entity.setDomicileIndicatorCode(dto.getDomicileIndicatorCode());
        entity.setCurrency(dto.getCurrency());
        
        // Рыночные данные
        entity.setMarketCapitalization(dto.getMarketCapitalization());
        entity.setHighPriceLast52Weeks(dto.getHighPriceLast52Weeks());
        entity.setLowPriceLast52Weeks(dto.getLowPriceLast52Weeks());
        entity.setAverageDailyVolumeLast10Days(dto.getAverageDailyVolumeLast10Days());
        entity.setAverageDailyVolumeLast4Weeks(dto.getAverageDailyVolumeLast4Weeks());
        entity.setBeta(dto.getBeta());
        entity.setFreeFloat(dto.getFreeFloat());
        entity.setForwardAnnualDividendYield(dto.getForwardAnnualDividendYield());
        entity.setSharesOutstanding(dto.getSharesOutstanding());
        
        // Финансовые показатели
        entity.setRevenueTtm(dto.getRevenueTtm());
        entity.setEbitdaTtm(dto.getEbitdaTtm());
        entity.setNetIncomeTtm(dto.getNetIncomeTtm());
        entity.setEpsTtm(dto.getEpsTtm());
        entity.setDilutedEpsTtm(dto.getDilutedEpsTtm());
        entity.setFreeCashFlowTtm(dto.getFreeCashFlowTtm());
        
        // Ростовые показатели
        entity.setFiveYearAnnualRevenueGrowthRate(dto.getFiveYearAnnualRevenueGrowthRate());
        entity.setThreeYearAnnualRevenueGrowthRate(dto.getThreeYearAnnualRevenueGrowthRate());
        entity.setOneYearAnnualRevenueGrowthRate(dto.getOneYearAnnualRevenueGrowthRate());
        
        // Финансовые коэффициенты
        entity.setPeRatioTtm(dto.getPeRatioTtm());
        entity.setPriceToSalesTtm(dto.getPriceToSalesTtm());
        entity.setPriceToBookTtm(dto.getPriceToBookTtm());
        entity.setPriceToFreeCashFlowTtm(dto.getPriceToFreeCashFlowTtm());
        entity.setTotalEnterpriseValueMrq(dto.getTotalEnterpriseValueMrq());
        entity.setEvToEbitdaMrq(dto.getEvToEbitdaMrq());
        entity.setEvToSales(dto.getEvToSales());
        
        // Показатели рентабельности
        entity.setNetMarginMrq(dto.getNetMarginMrq());
        entity.setNetInterestMarginMrq(dto.getNetInterestMarginMrq());
        entity.setRoe(dto.getRoe());
        entity.setRoa(dto.getRoa());
        entity.setRoic(dto.getRoic());
        
        // Долговые показатели
        entity.setTotalDebtMrq(dto.getTotalDebtMrq());
        entity.setTotalDebtToEquityMrq(dto.getTotalDebtToEquityMrq());
        entity.setTotalDebtToEbitdaMrq(dto.getTotalDebtToEbitdaMrq());
        entity.setFreeCashFlowToPrice(dto.getFreeCashFlowToPrice());
        entity.setNetDebtToEbitda(dto.getNetDebtToEbitda());
        entity.setCurrentRatioMrq(dto.getCurrentRatioMrq());
        entity.setFixedChargeCoverageRatioFy(dto.getFixedChargeCoverageRatioFy());
        
        // Дивидендные показатели
        entity.setDividendYieldDailyTtm(dto.getDividendYieldDailyTtm());
        entity.setDividendRateTtm(dto.getDividendRateTtm());
        entity.setDividendsPerShare(dto.getDividendsPerShare());
        entity.setFiveYearsAverageDividendYield(dto.getFiveYearsAverageDividendYield());
        entity.setFiveYearAnnualDividendGrowthRate(dto.getFiveYearAnnualDividendGrowthRate());
        entity.setDividendPayoutRatioFy(dto.getDividendPayoutRatioFy());
        
        // Другие показатели
        entity.setBuyBackTtm(dto.getBuyBackTtm());
        entity.setAdrToCommonShareRatio(dto.getAdrToCommonShareRatio());
        entity.setNumberOfEmployees(dto.getNumberOfEmployees());
        
        // Даты
        entity.setExDividendDate(dto.getExDividendDate());
        entity.setFiscalPeriodStartDate(dto.getFiscalPeriodStartDate());
        entity.setFiscalPeriodEndDate(dto.getFiscalPeriodEndDate());
        
        // Показатели изменений
        entity.setRevenueChangeFiveYears(dto.getRevenueChangeFiveYears());
        entity.setEpsChangeFiveYears(dto.getEpsChangeFiveYears());
        entity.setEbitdaChangeFiveYears(dto.getEbitdaChangeFiveYears());
        entity.setTotalDebtChangeFiveYears(dto.getTotalDebtChangeFiveYears());
        
        return entity;
    }

    /**
     * Конвертация списка Entity в список DTO
     */
    public List<AssetFundamentalDto> toDtoList(List<AssetFundamentalEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Конвертация списка DTO в список Entity
     */
    public List<AssetFundamentalEntity> toEntityList(List<AssetFundamentalDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
