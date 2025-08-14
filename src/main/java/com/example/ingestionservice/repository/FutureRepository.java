package com.example.ingestionservice.repository;

import com.example.ingestionservice.entity.FutureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FutureRepository extends JpaRepository<FutureEntity, String> { }
