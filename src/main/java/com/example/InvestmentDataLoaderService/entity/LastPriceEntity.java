package com.example.InvestmentDataLoaderService.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "last_prices", schema = "invest")
public class LastPriceEntity {
    @EmbeddedId
    private LastPriceKey id;
    @Column(nullable = false, precision = 18, scale = 9)
    private BigDecimal price;
    private String currency;
    private String exchange;

    public LastPriceEntity() {}

    public LastPriceEntity(String figi, LocalDateTime time, BigDecimal price,
                           String currency, String exchange) {
        // Конвертируем время в московское время
        LocalDateTime moscowTime = convertToMoscowTime(time);
        this.id = new LastPriceKey(figi, moscowTime);
        this.price = price;
        this.currency = currency;
        this.exchange = exchange;
    }
    
    /**
     * Конвертирует время в московское время (UTC+3)
     */
    private LocalDateTime convertToMoscowTime(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        // Предполагаем, что входящее время в UTC, конвертируем в московское
        ZonedDateTime utcTime = time.atZone(ZoneId.of("UTC"));
        ZonedDateTime moscowTime = utcTime.withZoneSameInstant(ZoneId.of("Europe/Moscow"));
        return moscowTime.toLocalDateTime();
    }
    // getters/setters
}

