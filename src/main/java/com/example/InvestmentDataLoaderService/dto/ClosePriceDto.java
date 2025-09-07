package com.example.InvestmentDataLoaderService.dto;

import java.math.BigDecimal;

public class ClosePriceDto {
    private String figi;
    private String tradingDate;
    private BigDecimal closePrice;
    private BigDecimal eveningSessionPrice;

    public ClosePriceDto(String figi, String tradingDate, BigDecimal closePrice) {
        this.figi = figi;
        this.tradingDate = tradingDate;
        this.closePrice = closePrice;
    }

    public ClosePriceDto(String figi, String tradingDate, BigDecimal closePrice, BigDecimal eveningSessionPrice) {
        this.figi = figi;
        this.tradingDate = tradingDate;
        this.closePrice = closePrice;
        this.eveningSessionPrice = eveningSessionPrice;
    }

    public String getFigi() { return figi; }
    public String getTradingDate() { return tradingDate; }
    public BigDecimal getClosePrice() { return closePrice; }
    public BigDecimal getEveningSessionPrice() { return eveningSessionPrice; }
}
