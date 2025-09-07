package com.example.InvestmentDataLoaderService.repository;

import com.example.InvestmentDataLoaderService.entity.ClosePriceEntity;
import com.example.InvestmentDataLoaderService.entity.ClosePriceKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ClosePriceRepository extends JpaRepository<ClosePriceEntity, ClosePriceKey> {
    boolean existsById(ClosePriceKey id);
    void deleteByIdPriceDate(LocalDate priceDate);
}
