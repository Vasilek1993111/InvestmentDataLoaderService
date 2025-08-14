package com.example.ingestionservice.dto;

public class TradingDayDto {
    private String date;
    private boolean isTradingDay;
    private String startTime;
    private String endTime;

    public TradingDayDto(String date, boolean isTradingDay, String startTime, String endTime) {
        this.date = date;
        this.isTradingDay = isTradingDay;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getDate() { return date; }
    public boolean isTradingDay() { return isTradingDay; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
}
