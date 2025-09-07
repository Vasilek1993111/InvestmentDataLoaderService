package com.example.InvestmentDataLoaderService.dto;

import java.time.LocalDate;
import java.util.List;

public class CandleRequestDto {
    private List<String> instruments;
    private LocalDate date;
    private String interval;
    private List<String> assetType;

    public CandleRequestDto() {}

    public CandleRequestDto(List<String> instruments, LocalDate date, String interval) {
        this.instruments = instruments;
        this.date = date;
        this.interval = interval;
    }

    public CandleRequestDto(List<String> instruments, LocalDate date, String interval, List<String> assetType) {
        this.instruments = instruments;
        this.date = date;
        this.interval = interval;
        this.assetType = assetType;
    }

    public List<String> getInstruments() { return instruments; }
    public void setInstruments(List<String> instruments) { this.instruments = instruments; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getInterval() { return interval; }
    public void setInterval(String interval) { this.interval = interval; }

    public List<String> getAssetType() { return assetType; }
    public void setAssetType(List<String> assetType) { this.assetType = assetType; }
}
