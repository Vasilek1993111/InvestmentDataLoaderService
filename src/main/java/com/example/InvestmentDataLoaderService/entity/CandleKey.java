package com.example.InvestmentDataLoaderService.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class CandleKey implements Serializable {
    private String figi;
    private Instant time;

    public CandleKey() {}

    public CandleKey(String figi, Instant time) {
        this.figi = figi;
        this.time = time;
    }

    public String getFigi() { return figi; }
    public void setFigi(String figi) { this.figi = figi; }

    public Instant getTime() { return time; }
    public void setTime(Instant time) { this.time = time; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CandleKey candleKey = (CandleKey) o;
        return Objects.equals(figi, candleKey.figi) && Objects.equals(time, candleKey.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(figi, time);
    }
}
