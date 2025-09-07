package com.example.InvestmentDataLoaderService.dto;

public class ShareDto {
    private String figi;
    private String ticker;
    private String name;
    private String currency;
    private String exchange;
    private String sector;
    private String tradingStatus;

    public ShareDto(String figi, String ticker, String name, String currency, String exchange) {
        this.figi = figi;
        this.ticker = ticker;
        this.name = name;
        this.currency = currency;
        this.exchange = exchange;
    }

    // Полный конструктор для всех полей
    public ShareDto(String figi, String ticker, String name, String currency, String exchange,
                   String sector, String tradingStatus) {
        this.figi = figi;
        this.ticker = ticker;
        this.name = name;
        this.currency = currency;
        this.exchange = exchange;
        this.sector = sector;
        this.tradingStatus = tradingStatus;
    }

    // Getters
    public String getFigi() { return figi; }
    public String getTicker() { return ticker; }
    public String getName() { return name; }
    public String getCurrency() { return currency; }
    public String getExchange() { return exchange; }
    public String getSector() { return sector; }
    public String getTradingStatus() { return tradingStatus; }
}
