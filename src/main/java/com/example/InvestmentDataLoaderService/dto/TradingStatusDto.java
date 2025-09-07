package com.example.InvestmentDataLoaderService.dto;

public class TradingStatusDto {
    private String figi;
    private String tradingStatus;

    public TradingStatusDto(String figi, String tradingStatus) {
        this.figi = figi;
        this.tradingStatus = tradingStatus;
    }

    public String getFigi() { return figi; }
    public String getTradingStatus() { return tradingStatus; }
}
