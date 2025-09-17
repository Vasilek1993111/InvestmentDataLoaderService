package com.example.InvestmentDataLoaderService.repository;

import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FutureRepository extends JpaRepository<FutureEntity, String> {
    Optional<FutureEntity> findByTickerIgnoreCase(String ticker);
}
