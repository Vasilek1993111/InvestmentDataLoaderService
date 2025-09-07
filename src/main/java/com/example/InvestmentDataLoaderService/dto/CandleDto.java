package com.example.InvestmentDataLoaderService.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class CandleDto {
    private String figi;
    private long volume;
    private BigDecimal high;
    private BigDecimal low;
    private Instant time;
    private BigDecimal close;
    private BigDecimal open;
    private boolean isComplete;

    public CandleDto(String figi, long volume, BigDecimal high, BigDecimal low, 
                    Instant time, BigDecimal close, BigDecimal open, boolean isComplete) {
        this.figi = figi;
        this.volume = volume;
        this.high = high;
        this.low = low;
        this.time = time;
        this.close = close;
        this.open = open;
        this.isComplete = isComplete;
    }

    public String getFigi() { return figi; }
    public long getVolume() { return volume; }
    public BigDecimal getHigh() { return high; }
    public BigDecimal getLow() { return low; }
    public Instant getTime() { return time; }
    public BigDecimal getClose() { return close; }
    public BigDecimal getOpen() { return open; }
    public boolean isComplete() { return isComplete; }
}
