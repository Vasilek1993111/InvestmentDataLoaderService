package com.example.ingestionservice.dto;

public class ShareDto {
    private String figi;
    private String ticker;
    private String name;
    private String currency;
    private String exchange;

    public ShareDto(String figi, String ticker, String name, String currency, String exchange) {
        this.figi = figi;
        this.ticker = ticker;
        this.name = name;
        this.currency = currency;
        this.exchange = exchange;
    }

    public String getFigi() { return figi; }
    public String getTicker() { return ticker; }
    public String getName() { return name; }
    public String getCurrency() { return currency; }
    public String getExchange() { return exchange; }
}
