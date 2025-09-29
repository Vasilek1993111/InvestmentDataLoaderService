package com.example.InvestmentDataLoaderService.repository;

import com.example.InvestmentDataLoaderService.entity.DividendEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DividendRepository extends JpaRepository<DividendEntity, Long> {
    
    /**
     * Находит все дивиденды по FIGI
     */
    List<DividendEntity> findByFigiOrderByRecordDateDesc(String figi);
    
    /**
     * Проверяет существование дивиденда по FIGI и дате фиксации
     */
    boolean existsByFigiAndRecordDate(String figi, LocalDate recordDate);
    
}
