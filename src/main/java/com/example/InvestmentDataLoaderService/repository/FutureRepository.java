package com.example.InvestmentDataLoaderService.repository;

import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FutureRepository extends JpaRepository<FutureEntity, String> { }
