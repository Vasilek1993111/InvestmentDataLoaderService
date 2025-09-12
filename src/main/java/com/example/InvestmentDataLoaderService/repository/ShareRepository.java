package com.example.InvestmentDataLoaderService.repository;

import com.example.InvestmentDataLoaderService.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ShareRepository extends JpaRepository<ShareEntity,String> {
    Optional<ShareEntity> findByTicker(String ticker);
    
    /**
     * Получает только FIGI акций (без загрузки полных объектов)
     * Оптимизированный метод для пакетной обработки
     */
    @Query("SELECT DISTINCT s.figi FROM ShareEntity s ORDER BY s.figi")
    List<String> findAllFigis();
    
    /**
     * Проверяет существование акции по FIGI
     */
    boolean existsByFigi(String figi);
}





