package com.example.InvestmentDataLoaderService.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "candles", schema = "invest")
@IdClass(CandleKey.class)
public class CandleEntity {
    
    @Id
    @Column(name = "figi", nullable = false)
    private String figi;
    
    @Column(name = "volume", nullable = false)
    private long volume;
    
    @Column(name = "high", nullable = false, precision = 18, scale = 9)
    private BigDecimal high;
    
    @Column(name = "low", nullable = false, precision = 18, scale = 9)
    private BigDecimal low;
    
    @Id
    @Column(name = "time", nullable = false)
    private Instant time;
    
    @Column(name = "close", nullable = false, precision = 18, scale = 9)
    private BigDecimal close;
    
    @Column(name = "open", nullable = false, precision = 18, scale = 9)
    private BigDecimal open;
    
    @Column(name = "is_complete", nullable = false)
    private boolean isComplete;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CandleEntity() {
        this.createdAt = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).toInstant();
        this.updatedAt = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).toInstant();
    }

    public CandleEntity(String figi, long volume, BigDecimal high, BigDecimal low, 
                       Instant time, BigDecimal close, BigDecimal open, boolean isComplete) {
        this();
        this.figi = figi;
        this.volume = volume;
        this.high = high;
        this.low = low;
        // Конвертируем время в UTC+3 (московское время)
        this.time = convertToMoscowTime(time);
        this.close = close;
        this.open = open;
        this.isComplete = isComplete;
    }
    
    /**
     * Конвертирует время в московское время (UTC+3)
     * Время от T-Bank API приходит в UTC, мы сохраняем его в московской временной зоне
     */
    private Instant convertToMoscowTime(Instant time) {
        if (time == null) {
            return null;
        }
        // Время от API в UTC, конвертируем в московское время
        ZonedDateTime utcTime = time.atZone(ZoneId.of("UTC"));
        ZonedDateTime moscowTime = utcTime.withZoneSameInstant(ZoneId.of("Europe/Moscow"));
        return moscowTime.toInstant();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).toInstant();
    }

    // Getters and Setters
    public String getFigi() { return figi; }
    public void setFigi(String figi) { this.figi = figi; }

    public long getVolume() { return volume; }
    public void setVolume(long volume) { this.volume = volume; }

    public BigDecimal getHigh() { return high; }
    public void setHigh(BigDecimal high) { this.high = high; }

    public BigDecimal getLow() { return low; }
    public void setLow(BigDecimal low) { this.low = low; }

    public Instant getTime() { return time; }
    public void setTime(Instant time) { this.time = convertToMoscowTime(time); }

    public BigDecimal getClose() { return close; }
    public void setClose(BigDecimal close) { this.close = close; }

    public BigDecimal getOpen() { return open; }
    public void setOpen(BigDecimal open) { this.open = open; }

    public boolean isComplete() { return isComplete; }
    public void setComplete(boolean complete) { isComplete = complete; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
