package com.example.InvestmentDataLoaderService.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class LastTradeDto {
    private String figi;
    private String direction;
    private BigDecimal price;
    private long quantity;
    private Instant time;
    private String tradeSource;

    public LastTradeDto() {}

    public LastTradeDto(String figi, String direction, BigDecimal price, long quantity, Instant time, String tradeSource) {
        this.figi = figi;
        this.direction = direction;
        this.price = price;
        this.quantity = quantity;
        this.time = time;
        this.tradeSource = tradeSource;
    }

    public String getFigi() {
        return figi;
    }

    public void setFigi(String figi) {
        this.figi = figi;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public String getTradeSource() {
        return tradeSource;
    }

    public void setTradeSource(String tradeSource) {
        this.tradeSource = tradeSource;
    }
}
