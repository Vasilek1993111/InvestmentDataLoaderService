package com.example.InvestmentDataLoaderService.repository;

import com.example.InvestmentDataLoaderService.entity.ClosePriceEveningSessionEntity;
import com.example.InvestmentDataLoaderService.entity.ClosePriceEveningSessionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClosePriceEveningSessionRepository extends JpaRepository<ClosePriceEveningSessionEntity, ClosePriceEveningSessionKey> {

    /**
     * Проверяет существование записи для указанной даты и FIGI
     */
    boolean existsByPriceDateAndFigi(LocalDate priceDate, String figi);

    /**
     * Находит все записи для указанной даты
     */
    List<ClosePriceEveningSessionEntity> findByPriceDate(LocalDate priceDate);

    /**
     * Находит все записи для указанного FIGI
     */
    List<ClosePriceEveningSessionEntity> findByFigi(String figi);

    /**
     * Находит записи в диапазоне дат для указанного FIGI
     */
    @Query("SELECT c FROM ClosePriceEveningSessionEntity c WHERE c.figi = :figi AND c.priceDate BETWEEN :startDate AND :endDate ORDER BY c.priceDate DESC")
    List<ClosePriceEveningSessionEntity> findByFigiAndPriceDateBetween(
            @Param("figi") String figi, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate
    );
}
