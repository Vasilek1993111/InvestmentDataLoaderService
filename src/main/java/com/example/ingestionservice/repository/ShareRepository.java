package com.example.ingestionservice.repository;

import com.example.ingestionservice.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShareRepository extends JpaRepository<ShareEntity,String> {
    Optional<ShareEntity> findByTicker(String ticker);
}





