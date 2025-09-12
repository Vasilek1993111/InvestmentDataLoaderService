package com.example.InvestmentDataLoaderService.repository;

import com.example.InvestmentDataLoaderService.entity.IndicativeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndicativeRepository extends JpaRepository<IndicativeEntity, String> {
    boolean existsByFigi(String figi);
}
