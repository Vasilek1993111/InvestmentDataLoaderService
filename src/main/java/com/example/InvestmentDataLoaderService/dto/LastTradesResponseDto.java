package com.example.InvestmentDataLoaderService.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LastTradesResponseDto {
    private String figi;
    private LocalDateTime time;
    private BigDecimal price;
    private String currency;
    private String exchange;

    public LastTradesResponseDto() {}

    public LastTradesResponseDto(String figi, LocalDateTime time, BigDecimal price, String currency, String exchange) {
        this.figi = figi;
        this.time = time;
        this.price = price;
        this.currency = currency;
        this.exchange = exchange;
    }

    public String getFigi() {
        return figi;
    }

    public void setFigi(String figi) {
        this.figi = figi;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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
