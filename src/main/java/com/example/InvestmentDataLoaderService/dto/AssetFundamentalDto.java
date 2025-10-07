package com.example.InvestmentDataLoaderService.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetFundamentalDto {
    
    private String assetUid;
    private String domicileIndicatorCode;
    private BigDecimal dividendYieldDailyTtm;
    private BigDecimal priceToSalesTtm;
    private BigDecimal adrToCommonShareRatio;
    private BigDecimal highPriceLast52Weeks;
    private BigDecimal dividendRateTtm;
    private BigDecimal revenueChangeFiveYears;
    private BigDecimal totalDebtMrq;
    private BigDecimal fiveYearAnnualRevenueGrowthRate;
    private BigDecimal fiveYearAnnualDividendGrowthRate;
    private BigDecimal freeFloat;
    private BigDecimal freeCashFlowToPrice;
    private BigDecimal revenueTtm;
    private BigDecimal totalEnterpriseValueMrq;
    private BigDecimal epsTtm;
    private BigDecimal peRatioTtm;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private String fiscalPeriodStartDate;
    
    private BigDecimal evToSales;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private String fiscalPeriodEndDate;
    
    private BigDecimal roic;
    private BigDecimal dividendPayoutRatioFy;
    private BigDecimal marketCapitalization;
    private BigDecimal ebitdaChangeFiveYears;
    private BigDecimal epsChangeFiveYears;
    private BigDecimal priceToFreeCashFlowTtm;
    private BigDecimal numberOfEmployees;
    private BigDecimal netInterestMarginMrq;
    private BigDecimal oneYearAnnualRevenueGrowthRate;
    private BigDecimal currentRatioMrq;
    private BigDecimal averageDailyVolumeLast4Weeks;
    private BigDecimal forwardAnnualDividendYield;
    private BigDecimal netMarginMrq;
    private BigDecimal roa;
    private BigDecimal totalDebtToEbitdaMrq;
    private BigDecimal roe;
    private BigDecimal freeCashFlowTtm;
    private BigDecimal evToEbitdaMrq;
    private BigDecimal fiveYearsAverageDividendYield;
    private BigDecimal priceToBookTtm;
    private BigDecimal totalDebtToEquityMrq;
    private BigDecimal averageDailyVolumeLast10Days;
    private BigDecimal fixedChargeCoverageRatioFy;
    private String currency;
    private BigDecimal netIncomeTtm;
    private BigDecimal sharesOutstanding;
    private BigDecimal beta;
    private BigDecimal threeYearAnnualRevenueGrowthRate;
    private BigDecimal totalDebtChangeFiveYears;
    private BigDecimal lowPriceLast52Weeks;
    private String exDividendDate;
    private BigDecimal ebitdaTtm;
    private BigDecimal netDebtToEbitda;
    private BigDecimal dilutedEpsTtm;
    private BigDecimal buyBackTtm;
    private BigDecimal dividendsPerShare;
}
