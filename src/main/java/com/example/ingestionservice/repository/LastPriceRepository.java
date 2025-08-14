package com.example.ingestionservice.repository;

import com.example.ingestionservice.entity.LastPriceEntity;
import com.example.ingestionservice.entity.LastPriceKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface LastPriceRepository extends JpaRepository<LastPriceEntity, LastPriceKey> {
    boolean existsById(LastPriceKey id);
    void deleteByIdTime(LocalDateTime time);
}
