package com.example.ingestionservice.repository;

import com.example.ingestionservice.entity.ClosePriceEntity;
import com.example.ingestionservice.entity.ClosePriceKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ClosePriceRepository extends JpaRepository<ClosePriceEntity, ClosePriceKey> {
    boolean existsById(ClosePriceKey id);
    void deleteByIdPriceDate(LocalDate priceDate);
}
