package com.example.ingestionservice.dto;

import java.util.List;

public class TradingScheduleDto {
    private String exchange;
    private List<TradingDayDto> days;

    public TradingScheduleDto(String exchange, List<TradingDayDto> days) {
        this.exchange = exchange;
        this.days = days;
    }

    public String getExchange() { return exchange; }
    public List<TradingDayDto> getDays() { return days; }
}
