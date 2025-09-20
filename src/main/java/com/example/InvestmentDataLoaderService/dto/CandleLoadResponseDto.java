package com.example.InvestmentDataLoaderService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO для ответа о запуске загрузки свечей
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandleLoadResponseDto {
    private boolean success;
    private String message;
    private LocalDateTime startTime;
    private String taskId;
}