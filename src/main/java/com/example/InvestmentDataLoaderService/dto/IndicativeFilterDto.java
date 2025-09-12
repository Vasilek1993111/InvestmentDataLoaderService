package com.example.InvestmentDataLoaderService.dto;

/**
 * DTO для фильтрации индикативных инструментов
 * Согласно документации Tinkoff Invest API: https://developer.tbank.ru/invest/services/instruments/methods
 */
public class IndicativeFilterDto {
    private String exchange;
    private String currency;
    private String ticker;
    private String figi;

    /**
     * Конструктор по умолчанию
     */
    public IndicativeFilterDto() {}

    /**
     * Конструктор с параметрами
     */
    public IndicativeFilterDto(String exchange, String currency, String ticker, String figi) {
        this.exchange = exchange;
        this.currency = currency;
        this.ticker = ticker;
        this.figi = figi;
    }

    // Getters
    public String getExchange() { return exchange; }
    public String getCurrency() { return currency; }
    public String getTicker() { return ticker; }
    public String getFigi() { return figi; }

    // Setters
    public void setExchange(String exchange) { this.exchange = exchange; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public void setFigi(String figi) { this.figi = figi; }
}