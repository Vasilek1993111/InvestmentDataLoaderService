package com.example.InvestmentDataLoaderService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandleLoadResponseDto {
    private boolean started;
    private String message;
    private LocalDateTime startTime;
    private String taskId;
}