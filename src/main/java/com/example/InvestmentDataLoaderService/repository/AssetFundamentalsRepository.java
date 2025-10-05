package com.example.InvestmentDataLoaderService.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.example.InvestmentDataLoaderService.entity.AssetFundamentalEntity;

import java.util.Optional;

@Repository
public interface AssetFundamentalsRepository extends JpaRepository<AssetFundamentalEntity, String> {
    
    /**
     * Найти фундаментальные показатели по идентификатору актива
     */
    Optional<AssetFundamentalEntity> findByAssetUid(String assetUid);
    
   
}
