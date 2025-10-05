package com.example.InvestmentDataLoaderService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetFundamentalsRequestDto {
    private List<String> assets;
}
