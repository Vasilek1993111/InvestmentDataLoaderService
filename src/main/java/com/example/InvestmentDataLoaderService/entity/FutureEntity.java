package com.example.InvestmentDataLoaderService.entity;

import com.example.InvestmentDataLoaderService.util.TimeZoneUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "futures", schema = "invest")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FutureEntity {
    @Id
    private String figi;
    
    @Column(name = "ticker", nullable = false)
    private String ticker;
    
    @Column(name = "asset_type", nullable = false)
    private String assetType;
    
    @Column(name = "basic_asset", nullable = false)
    private String basicAsset;
    
    @Column(name = "currency", nullable = false)
    private String currency;
    
    @Column(name = "exchange", nullable = false)
    private String exchange;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(TimeZoneUtils.getMoscowZone());
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now(TimeZoneUtils.getMoscowZone());

    public FutureEntity(String figi, String ticker, String assetType, String basicAsset, 
                       String currency, String exchange) {
        this.figi = figi;
        this.ticker = ticker;
        this.assetType = assetType;
        this.basicAsset = basicAsset;
        this.currency = currency;
        this.exchange = exchange;
        this.createdAt = LocalDateTime.now(TimeZoneUtils.getMoscowZone());
        this.updatedAt = LocalDateTime.now(TimeZoneUtils.getMoscowZone());
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now(TimeZoneUtils.getMoscowZone());
    }
}
