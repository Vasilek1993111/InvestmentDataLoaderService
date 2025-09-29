package com.example.InvestmentDataLoaderService.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DividendDto {
    private String figi;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate declaredDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate paymentDate;
    
    private BigDecimal dividendValue;
    private String currency;
    private String dividendType;
}
