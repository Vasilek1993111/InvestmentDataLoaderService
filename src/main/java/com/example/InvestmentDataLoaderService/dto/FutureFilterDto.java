package com.example.InvestmentDataLoaderService.dto;

public class FutureFilterDto {
    private String status;
    private String exchange;
    private String currency;
    private String ticker;
    private String assetType;

    public FutureFilterDto() {}

    public FutureFilterDto(String status, String exchange, String currency, String ticker, String assetType) {
        this.status = status;
        this.exchange = exchange;
        this.currency = currency;
        this.ticker = ticker;
        this.assetType = assetType;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getAssetType() { return assetType; }
    public void setAssetType(String assetType) { this.assetType = assetType; }
}
