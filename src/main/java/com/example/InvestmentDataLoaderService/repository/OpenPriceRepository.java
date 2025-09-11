package com.example.InvestmentDataLoaderService.repository;

import com.example.InvestmentDataLoaderService.entity.OpenPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface OpenPriceRepository extends JpaRepository<OpenPriceEntity, com.example.InvestmentDataLoaderService.entity.OpenPriceKey> {
    
    /**
     * Проверяет существование записи по дате и FIGI
     */
    @Query("SELECT COUNT(o) > 0 FROM OpenPriceEntity o WHERE o.id.priceDate = :priceDate AND o.id.figi = :figi")
    boolean existsByPriceDateAndFigi(@Param("priceDate") LocalDate priceDate, @Param("figi") String figi);
    
    /**
     * Находит запись по дате и FIGI
     */
    @Query("SELECT o FROM OpenPriceEntity o WHERE o.id.priceDate = :priceDate AND o.id.figi = :figi")
    OpenPriceEntity findByPriceDateAndFigi(@Param("priceDate") LocalDate priceDate, @Param("figi") String figi);
}
