package com.example.InvestmentDataLoaderService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "shares", schema = "invest")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareEntity {
    @Id
    private String figi;
    private String ticker;
    private String name;
    private String currency;
    private String exchange;
    private String sector;
    private String tradingStatus;
    private Boolean shortEnabled;
    private String assetUid;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    //Совместимость со старой сигнатурой (без shortEnabled и assetUid)
    public ShareEntity(String figi,
                       String ticker,
                       String name,
                       String currency,
                       String exchange,
                       String sector,
                       String tradingStatus,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this.figi = figi;
        this.ticker = ticker;
        this.name = name;
        this.currency = currency;
        this.exchange = exchange;
        this.sector = sector;
        this.tradingStatus = tradingStatus;
        this.shortEnabled = null;
        this.assetUid = null;
    
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    
    }
}
