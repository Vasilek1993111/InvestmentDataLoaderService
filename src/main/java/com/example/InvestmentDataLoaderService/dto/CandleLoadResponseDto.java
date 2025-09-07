package com.example.InvestmentDataLoaderService.dto;

import java.time.LocalDateTime;

public class CandleLoadResponseDto {
    private boolean started;
    private String message;
    private LocalDateTime startTime;
    private String taskId;

    public CandleLoadResponseDto() {}

    public CandleLoadResponseDto(boolean started, String message, LocalDateTime startTime, String taskId) {
        this.started = started;
        this.message = message;
        this.startTime = startTime;
        this.taskId = taskId;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
