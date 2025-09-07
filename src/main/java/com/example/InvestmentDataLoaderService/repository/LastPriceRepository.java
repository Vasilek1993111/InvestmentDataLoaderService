package com.example.InvestmentDataLoaderService.repository;

import com.example.InvestmentDataLoaderService.entity.LastPriceEntity;
import com.example.InvestmentDataLoaderService.entity.LastPriceKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface LastPriceRepository extends JpaRepository<LastPriceEntity, LastPriceKey> {
    boolean existsById(LastPriceKey id);
    void deleteByIdTime(LocalDateTime time);
}
