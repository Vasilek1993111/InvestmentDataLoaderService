package com.example.ingestionservice.dto;

public class FutureDto {
    private String figi;
    private String ticker;
    private String assetType;
    private String basicAsset;
    private String currency;
    private String exchange;

    public FutureDto(String figi, String ticker, String assetType, String basicAsset, String currency, String exchange) {
        this.figi = figi;
        this.ticker = ticker;
        this.assetType = assetType;
        this.basicAsset = basicAsset;
        this.currency = currency;
        this.exchange = exchange;
    }

    public String getFigi() { return figi; }
    public String getTicker() { return ticker; }
    public String getAssetType() { return assetType; }
    public String getBasicAsset() { return basicAsset; }
    public String getCurrency() { return currency; }
    public String getExchange() { return exchange; }
}
