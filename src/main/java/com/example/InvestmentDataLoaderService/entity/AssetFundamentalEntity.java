package com.example.InvestmentDataLoaderService.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "asset_fundamentals", schema = "invest")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetFundamentalEntity {

    @Id
    @Column(name = "asset_uid")
    private String assetUid;

    @Column(name = "domicile_indicator_code")
    private String domicileIndicatorCode;

   
    @Column(name = "dividend_yield_daily_ttm")
    private BigDecimal dividendYieldDailyTtm;

    @Column(name = "price_to_sales_ttm")
    private BigDecimal priceToSalesTtm;

    @Column(name = "adr_to_common_share_ratio")
    private BigDecimal adrToCommonShareRatio;

    @Column(name = "high_price_last_52_weeks")
    private BigDecimal highPriceLast52Weeks;

    @Column(name = "dividend_rate_ttm")
    private BigDecimal dividendRateTtm;

    @Column(name = "revenue_change_five_years")
    private BigDecimal revenueChangeFiveYears;

    @Column(name = "total_debt_mrq")
    private BigDecimal totalDebtMrq;

    @Column(name = "five_year_annual_revenue_growth_rate")
    private BigDecimal fiveYearAnnualRevenueGrowthRate;

    @Column(name = "five_year_annual_dividend_growth_rate")
    private BigDecimal fiveYearAnnualDividendGrowthRate;

    @Column(name = "free_float")
    private BigDecimal freeFloat;

    @Column(name = "free_cash_flow_to_price")
    private BigDecimal freeCashFlowToPrice;

    @Column(name = "revenue_ttm")
    private BigDecimal revenueTtm;

    @Column(name = "total_enterprise_value_mrq")
    private BigDecimal totalEnterpriseValueMrq;

    @Column(name = "eps_ttm")
    private BigDecimal epsTtm;

    @Column(name = "pe_ratio_ttm")
    private BigDecimal peRatioTtm;

    @Column(name = "fiscal_period_start_date")
    private String fiscalPeriodStartDate;

    @Column(name = "ev_to_sales")
    private BigDecimal evToSales;

    @Column(name = "fiscal_period_end_date")
    private String fiscalPeriodEndDate;

    @Column(name = "roic")
    private BigDecimal roic;

    @Column(name = "dividend_payout_ratio_fy")
    private BigDecimal dividendPayoutRatioFy;

    @Column(name = "market_capitalization")
    private BigDecimal marketCapitalization;

    @Column(name = "ebitda_change_five_years")
    private BigDecimal ebitdaChangeFiveYears;

    @Column(name = "eps_change_five_years")
    private BigDecimal epsChangeFiveYears;

    @Column(name = "price_to_free_cash_flow_ttm")
    private BigDecimal priceToFreeCashFlowTtm;

    @Column(name = "number_of_employees")
    private BigDecimal numberOfEmployees;

    @Column(name = "net_interest_margin_mrq")
    private BigDecimal netInterestMarginMrq;

    @Column(name = "one_year_annual_revenue_growth_rate")
    private BigDecimal oneYearAnnualRevenueGrowthRate;

    @Column(name = "current_ratio_mrq")
    private BigDecimal currentRatioMrq;

    @Column(name = "average_daily_volume_last_4_weeks")
    private BigDecimal averageDailyVolumeLast4Weeks;

    @Column(name = "forward_annual_dividend_yield")
    private BigDecimal forwardAnnualDividendYield;

    @Column(name = "net_margin_mrq")
    private BigDecimal netMarginMrq;

    @Column(name = "roa")
    private BigDecimal roa;

    @Column(name = "total_debt_to_ebitda_mrq")
    private BigDecimal totalDebtToEbitdaMrq;

    @Column(name = "roe")
    private BigDecimal roe;

    @Column(name = "free_cash_flow_ttm")
    private BigDecimal freeCashFlowTtm;

    @Column(name = "ev_to_ebitda_mrq")
    private BigDecimal evToEbitdaMrq;

    @Column(name = "five_years_average_dividend_yield")
    private BigDecimal fiveYearsAverageDividendYield;

    @Column(name = "price_to_book_ttm")
    private BigDecimal priceToBookTtm;

    @Column(name = "total_debt_to_equity_mrq")
    private BigDecimal totalDebtToEquityMrq;

    @Column(name = "average_daily_volume_last_10_days")
    private BigDecimal averageDailyVolumeLast10Days;

    @Column(name = "fixed_charge_coverage_ratio_fy")
    private BigDecimal fixedChargeCoverageRatioFy;

    @Column(name = "currency")
    private String currency;

    @Column(name = "net_income_ttm")
    private BigDecimal netIncomeTtm;

    @Column(name = "shares_outstanding")
    private BigDecimal sharesOutstanding;

    @Column(name = "beta")
    private BigDecimal beta;

    @Column(name = "three_year_annual_revenue_growth_rate")
    private BigDecimal threeYearAnnualRevenueGrowthRate;

    @Column(name = "total_debt_change_five_years")
    private BigDecimal totalDebtChangeFiveYears;

    @Column(name = "low_price_last_52_weeks")
    private BigDecimal lowPriceLast52Weeks;

    @Column(name = "ex_dividend_date")
    private String exDividendDate;

    @Column(name = "ebitda_ttm")
    private BigDecimal ebitdaTtm;

    @Column(name = "net_debt_to_ebitda")
    private BigDecimal netDebtToEbitda;

    @Column(name = "diluted_eps_ttm")
    private BigDecimal dilutedEpsTtm;

    @Column(name = "buy_back_ttm")
    private BigDecimal buyBackTtm;

    @Column(name = "dividends_per_share")
    private BigDecimal dividendsPerShare;
}
