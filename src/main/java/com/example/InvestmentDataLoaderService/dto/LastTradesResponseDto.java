package com.example.InvestmentDataLoaderService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LastTradesResponseDto {
    private String figi;
    private LocalDateTime time;
    private BigDecimal price;
    private String currency;
    private String exchange;
}