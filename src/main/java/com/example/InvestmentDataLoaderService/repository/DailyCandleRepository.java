package com.example.InvestmentDataLoaderService.repository;

import com.example.InvestmentDataLoaderService.entity.DailyCandleEntity;
import com.example.InvestmentDataLoaderService.entity.DailyCandleKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyCandleRepository extends JpaRepository<DailyCandleEntity, DailyCandleKey> {
    
    /**
     * Проверяет существование дневной свечи по FIGI и времени
     */
    boolean existsByFigiAndTime(String figi, Instant time);
    
    /**
     * Находит дневную свечу по FIGI и времени
     */
    DailyCandleEntity findByFigiAndTime(String figi, Instant time);
    
    /**
     * Находит все дневные свечи для конкретного инструмента в заданном временном диапазоне
     */
    @Query("SELECT c FROM DailyCandleEntity c WHERE c.figi = :figi AND c.time >= :startTime AND c.time <= :endTime ORDER BY c.time")
    List<DailyCandleEntity> findByFigiAndTimeBetween(@Param("figi") String figi, 
                                                    @Param("startTime") Instant startTime, 
                                                    @Param("endTime") Instant endTime);
    
    /**
     * Находит все дневные свечи для конкретного инструмента за день
     */
    @Query("SELECT c FROM DailyCandleEntity c WHERE c.figi = :figi AND DATE(c.time) = DATE(:date) ORDER BY c.time")
    List<DailyCandleEntity> findByFigiAndDate(@Param("figi") String figi, @Param("date") Instant date);
    
    /**
     * Удаляет все дневные свечи для конкретного инструмента в заданном временном диапазоне
     */
    @Query("DELETE FROM DailyCandleEntity c WHERE c.figi = :figi AND c.time >= :startTime AND c.time <= :endTime")
    void deleteByFigiAndTimeBetween(@Param("figi") String figi, 
                                   @Param("startTime") Instant startTime, 
                                   @Param("endTime") Instant endTime);
    
    /**
     * Находит все дневные свечи за определенную дату
     */
    @Query("SELECT c FROM DailyCandleEntity c WHERE DATE(c.time) = :date ORDER BY c.figi, c.time")
    List<DailyCandleEntity> findByDate(@Param("date") LocalDate date);
    
    /**
     * Получает агрегированные данные для списка FIGI одним запросом
     * Оптимизированный метод для пакетной обработки
     */
    @Query(value = """
        SELECT c.figi, 
               DATE(c.time) as trade_date,
               SUM(c.volume) as total_volume,
               COUNT(c.figi) as total_candles,
               AVG(c.volume) as avg_volume_per_candle
        FROM invest.daily_candles c 
        WHERE c.figi IN :figis 
        GROUP BY c.figi, DATE(c.time)
        ORDER BY c.figi, DATE(c.time)
        """, nativeQuery = true)
    List<Object[]> getAggregatedDataByFigis(@Param("figis") List<String> figis);
    
    /**
     * Получает агрегированные данные для одного FIGI с ограничением по времени
     */
    @Query(value = """
        SELECT DATE(c.time) as trade_date,
               SUM(c.volume) as total_volume,
               COUNT(c.figi) as total_candles,
               AVG(c.volume) as avg_volume_per_candle
        FROM invest.daily_candles c 
        WHERE c.figi = :figi 
        AND c.time >= :startDate 
        AND c.time < :endDate
        GROUP BY DATE(c.time)
        ORDER BY DATE(c.time)
        """, nativeQuery = true)
    List<Object[]> getAggregatedDataByFigiAndDateRange(@Param("figi") String figi,
                                                      @Param("startDate") Instant startDate,
                                                      @Param("endDate") Instant endDate);
}
