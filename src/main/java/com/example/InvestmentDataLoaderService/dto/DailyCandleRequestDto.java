package com.example.InvestmentDataLoaderService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO для запроса загрузки дневных свечей
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyCandleRequestDto {
    private List<String> instruments;
    private LocalDate date;
    private List<String> assetType;
}
