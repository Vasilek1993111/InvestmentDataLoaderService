package com.example.InvestmentDataLoaderService.repository;

import com.example.InvestmentDataLoaderService.entity.SharesAggregatedDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SharesAggregatedDataRepository extends JpaRepository<SharesAggregatedDataEntity, String> {
    
    /**
     * Проверяет существование записи по FIGI
     */
    boolean existsByFigi(String figi);
    
    /**
     * Находит запись по FIGI
     */
    SharesAggregatedDataEntity findByFigi(String figi);
    
    /**
     * Получает все записи, отсортированные по последнему обновлению
     */
    List<SharesAggregatedDataEntity> findAllByOrderByUpdatedAtDesc();
    
    /**
     * Получает записи, которые не обновлялись с указанной даты
     */
    @Query("SELECT s FROM SharesAggregatedDataEntity s WHERE s.lastCalculated < :date ORDER BY s.lastCalculated ASC")
    List<SharesAggregatedDataEntity> findStaleRecords(@Param("date") LocalDate date);
    
    /**
     * Получает топ N записей по утреннему объему
     */
    @Query("SELECT s FROM SharesAggregatedDataEntity s WHERE s.avgVolumeMorning IS NOT NULL ORDER BY s.avgVolumeMorning DESC")
    List<SharesAggregatedDataEntity> findTopByMorningVolume(@Param("limit") int limit);
}
