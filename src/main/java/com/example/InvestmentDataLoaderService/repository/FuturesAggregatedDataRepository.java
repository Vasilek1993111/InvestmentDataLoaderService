package com.example.InvestmentDataLoaderService.repository;

import com.example.InvestmentDataLoaderService.entity.FuturesAggregatedDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FuturesAggregatedDataRepository extends JpaRepository<FuturesAggregatedDataEntity, String> {
    
    /**
     * Проверяет существование записи по FIGI
     */
    boolean existsByFigi(String figi);
    
    /**
     * Находит запись по FIGI
     */
    FuturesAggregatedDataEntity findByFigi(String figi);
    
    /**
     * Получает все записи, отсортированные по последнему обновлению
     */
    List<FuturesAggregatedDataEntity> findAllByOrderByUpdatedAtDesc();
    
    /**
     * Получает записи, которые не обновлялись с указанной даты
     */
    @Query("SELECT f FROM FuturesAggregatedDataEntity f WHERE f.lastCalculated < :date ORDER BY f.lastCalculated ASC")
    List<FuturesAggregatedDataEntity> findStaleRecords(@Param("date") LocalDate date);
    
    /**
     * Получает топ N записей по утреннему объему
     */
    @Query("SELECT f FROM FuturesAggregatedDataEntity f WHERE f.avgVolumeMorning IS NOT NULL ORDER BY f.avgVolumeMorning DESC")
    List<FuturesAggregatedDataEntity> findTopByMorningVolume(@Param("limit") int limit);
}
