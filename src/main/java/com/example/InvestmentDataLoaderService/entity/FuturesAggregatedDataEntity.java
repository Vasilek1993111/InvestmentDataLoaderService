package com.example.InvestmentDataLoaderService.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "futures_aggregated_data", schema = "invest")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuturesAggregatedDataEntity {
    
    @Id
    @Column(name = "figi", nullable = false)
    private String figi;
    
    @Column(name = "avg_volume_morning", precision = 18, scale = 2)
    private BigDecimal avgVolumeMorning; // Средний объем с 06:50:00 по 09:59:59
    
    @Column(name = "avg_volume_evening", precision = 18, scale = 2)
    private BigDecimal avgVolumeEvening; // Средний объем с 19:00:00 по 23:59:59
    
    @Column(name = "avg_volume_weekend", precision = 18, scale = 2)
    private BigDecimal avgVolumeWeekend; // Средний объем в выходные дни
    
    @Column(name = "total_trading_days", nullable = false)
    private Integer totalTradingDays; // Общее количество торговых дней
    
    @Column(name = "total_weekend_days", nullable = false)
    private Integer totalWeekendDays; // Общее количество выходных дней
    
    @Column(name = "last_calculated", nullable = false)
    private LocalDateTime lastCalculated = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
    
    public FuturesAggregatedDataEntity(String figi) {
        this.figi = figi;
        this.totalTradingDays = 0;
        this.totalWeekendDays = 0;
        this.lastCalculated = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        this.createdAt = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        this.updatedAt = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
    }
}
