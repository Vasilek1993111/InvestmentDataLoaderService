package com.example.InvestmentDataLoaderService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DividendRequestDto {
    private List<String> instruments;
    private LocalDate from;
    private LocalDate to;
}
