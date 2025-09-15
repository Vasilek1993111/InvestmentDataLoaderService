package com.example.InvestmentDataLoaderService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FutureFilterDto {
    private String status;
    private String exchange;
    private String currency;
    private String ticker;
    private String assetType;
}