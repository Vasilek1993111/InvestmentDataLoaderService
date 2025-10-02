package com.example.InvestmentDataLoaderService.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record FutureDto(
    String figi,
    String ticker,
    String assetType,
    String basicAsset,
    String currency,
    String exchange,    
    Boolean shortEnabled,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime expirationDate
) {
    
}
