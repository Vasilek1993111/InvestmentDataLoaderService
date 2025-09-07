package com.example.InvestmentDataLoaderService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "futures")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FutureEntity {
    @Id
    private String figi;
    private String ticker;
    private String assetType;
    private String basicAsset;
    private String currency;
    private String exchange;
    private String stockTicker;
}
