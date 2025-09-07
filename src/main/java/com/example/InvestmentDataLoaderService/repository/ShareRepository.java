package com.example.InvestmentDataLoaderService.repository;

import com.example.InvestmentDataLoaderService.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShareRepository extends JpaRepository<ShareEntity,String> {
    Optional<ShareEntity> findByTicker(String ticker);
}





