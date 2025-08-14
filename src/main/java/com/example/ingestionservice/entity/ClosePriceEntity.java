package com.example.ingestionservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "close_prices", schema = "invest")
public class ClosePriceEntity {
    @EmbeddedId
    private ClosePriceKey id;
    @Column(nullable = false, precision = 18, scale = 9)
    private BigDecimal closePrice;
    private String instrumentType;
    private String currency;
    private String exchange;
    private ZonedDateTime createdAt = ZonedDateTime.now();

    public ClosePriceEntity() {}

    public ClosePriceEntity(LocalDate date, String figi, String instrumentType,
                            BigDecimal closePrice, String currency, String exchange) {
        this.id = new ClosePriceKey(date, figi);
        this.instrumentType = instrumentType;
        this.closePrice = closePrice;
        this.currency = currency;
        this.exchange = exchange;
        this.createdAt = ZonedDateTime.now();
    }
}

