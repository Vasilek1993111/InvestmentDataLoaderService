package com.example.InvestmentDataLoaderService.dto;

/**
 * DTO для индикативных инструментов (индексы, товары и другие)
 * Согласно документации Tinkoff Invest API: https://developer.tbank.ru/invest/services/instruments/methods
 */
public class IndicativeDto {
    private String figi;
    private String ticker;
    private String name;
    private String currency;
    private String exchange;
    private String classCode;
    private String uid;
    private Boolean sellAvailableFlag;
    private Boolean buyAvailableFlag;

    /**
     * Конструктор для основных полей
     */
    public IndicativeDto(String figi, String ticker, String name, String currency, String exchange) {
        this.figi = figi;
        this.ticker = ticker;
        this.name = name;
        this.currency = currency;
        this.exchange = exchange;
    }

    /**
     * Полный конструктор для всех полей
     */
    public IndicativeDto(String figi, String ticker, String name, String currency, String exchange,
                        String classCode, String uid, Boolean sellAvailableFlag, Boolean buyAvailableFlag) {
        this.figi = figi;
        this.ticker = ticker;
        this.name = name;
        this.currency = currency;
        this.exchange = exchange;
        this.classCode = classCode;
        this.uid = uid;
        this.sellAvailableFlag = sellAvailableFlag;
        this.buyAvailableFlag = buyAvailableFlag;
    }

    // Getters
    public String getFigi() { return figi; }
    public String getTicker() { return ticker; }
    public String getName() { return name; }
    public String getCurrency() { return currency; }
    public String getExchange() { return exchange; }
    public String getClassCode() { return classCode; }
    public String getUid() { return uid; }
    public Boolean getSellAvailableFlag() { return sellAvailableFlag; }
    public Boolean getBuyAvailableFlag() { return buyAvailableFlag; }

    // Setters
    public void setFigi(String figi) { this.figi = figi; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public void setName(String name) { this.name = name; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    public void setClassCode(String classCode) { this.classCode = classCode; }
    public void setUid(String uid) { this.uid = uid; }
    public void setSellAvailableFlag(Boolean sellAvailableFlag) { this.sellAvailableFlag = sellAvailableFlag; }
    public void setBuyAvailableFlag(Boolean buyAvailableFlag) { this.buyAvailableFlag = buyAvailableFlag; }
}