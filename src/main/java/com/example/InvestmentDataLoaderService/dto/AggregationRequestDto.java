package com.example.InvestmentDataLoaderService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggregationRequestDto {
    private List<String> types; // SHARES, FUTURES, ALL
}


