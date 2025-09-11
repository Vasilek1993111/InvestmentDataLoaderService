package com.example.InvestmentDataLoaderService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenPriceDto {
    private String figi;
    private LocalDate priceDate;
    private BigDecimal openPrice;
    private String instrumentType;
    private String currency;
    private String exchange;
}
