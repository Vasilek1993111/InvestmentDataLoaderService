package com.example.ingestionservice.dto;

import java.math.BigDecimal;

public class ClosePriceDto {
    private String figi;
    private String tradingDate;
    private BigDecimal closePrice;

    public ClosePriceDto(String figi, String tradingDate, BigDecimal closePrice) {
        this.figi = figi;
        this.tradingDate = tradingDate;
        this.closePrice = closePrice;
    }

    public String getFigi() { return figi; }
    public String getTradingDate() { return tradingDate; }
    public BigDecimal getClosePrice() { return closePrice; }
}
