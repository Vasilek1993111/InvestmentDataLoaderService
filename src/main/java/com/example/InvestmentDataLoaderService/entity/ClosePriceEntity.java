package com.example.InvestmentDataLoaderService.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "close_prices", schema = "invest")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClosePriceEntity {
    @EmbeddedId
    private ClosePriceKey id;
    
    @Column(name = "instrument_type", nullable = false)
    private String instrumentType;
    
    @Column(name = "close_price", nullable = false, precision = 18, scale = 9)
    private BigDecimal closePrice;
    
    @Column(nullable = false)
    private String currency;
    
    @Column(nullable = false)
    private String exchange;
    
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();
    
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt = ZonedDateTime.now();

    public ClosePriceEntity(LocalDate date, String figi, String instrumentType,
                            BigDecimal closePrice, String currency, String exchange) {
        this.id = new ClosePriceKey(date, figi);
        this.instrumentType = instrumentType;
        this.closePrice = closePrice;
        this.currency = currency;
        this.exchange = exchange;
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }
}

