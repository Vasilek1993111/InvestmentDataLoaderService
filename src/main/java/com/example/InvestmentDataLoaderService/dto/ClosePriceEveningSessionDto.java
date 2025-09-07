package com.example.InvestmentDataLoaderService.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ClosePriceEveningSessionDto {
    private LocalDate priceDate;
    private String figi;
    private BigDecimal closePrice;
    private String instrumentType;
    private String currency;
    private String exchange;

    public ClosePriceEveningSessionDto() {}

    public ClosePriceEveningSessionDto(LocalDate priceDate, String figi, BigDecimal closePrice, 
                                     String instrumentType, String currency, String exchange) {
        this.priceDate = priceDate;
        this.figi = figi;
        this.closePrice = closePrice;
        this.instrumentType = instrumentType;
        this.currency = currency;
        this.exchange = exchange;
    }

    public LocalDate getPriceDate() {
        return priceDate;
    }

    public void setPriceDate(LocalDate priceDate) {
        this.priceDate = priceDate;
    }

    public String getFigi() {
        return figi;
    }

    public void setFigi(String figi) {
        this.figi = figi;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(BigDecimal closePrice) {
        this.closePrice = closePrice;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }
}
