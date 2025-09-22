package com.example.InvestmentDataLoaderService.repository;

import com.example.InvestmentDataLoaderService.entity.SystemLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLogEntity, Long> {
    
    /**
     * Находит все логи по task_id
     */
    List<SystemLogEntity> findByTaskIdOrderByCreatedAtDesc(String taskId);
    
    /**
     * Находит все логи по endpoint
     */
    List<SystemLogEntity> findByEndpointOrderByCreatedAtDesc(String endpoint);
    
    /**
     * Находит все логи по статусу
     */
    List<SystemLogEntity> findByStatusOrderByCreatedAtDesc(String status);
    
    /**
     * Находит активные задачи (STARTED, PROCESSING)
     */
    @Query("SELECT s FROM SystemLogEntity s WHERE s.status IN ('STARTED', 'PROCESSING') ORDER BY s.startTime DESC")
    List<SystemLogEntity> findActiveTasks();
    
    /**
     * Находит логи за период времени
     */
    @Query("SELECT s FROM SystemLogEntity s WHERE s.startTime >= :startTime AND s.startTime <= :endTime ORDER BY s.startTime DESC")
    List<SystemLogEntity> findByTimeRange(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);
}