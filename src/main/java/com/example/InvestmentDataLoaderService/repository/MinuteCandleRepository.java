package com.example.InvestmentDataLoaderService.repository;

import com.example.InvestmentDataLoaderService.entity.MinuteCandleEntity;
import com.example.InvestmentDataLoaderService.entity.MinuteCandleKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MinuteCandleRepository extends JpaRepository<MinuteCandleEntity, MinuteCandleKey> {
    
    /**
     * Проверяет существование минутной свечи по FIGI и времени
     */
    boolean existsByFigiAndTime(String figi, Instant time);
    
    /**
     * Находит минутную свечу по FIGI и времени
     */
    MinuteCandleEntity findByFigiAndTime(String figi, Instant time);
    
    /**
     * Находит все минутные свечи для конкретного инструмента в заданном временном диапазоне
     */
    @Query("SELECT c FROM MinuteCandleEntity c WHERE c.figi = :figi AND c.time >= :startTime AND c.time <= :endTime ORDER BY c.time")
    List<MinuteCandleEntity> findByFigiAndTimeBetween(@Param("figi") String figi, 
                                                     @Param("startTime") Instant startTime, 
                                                     @Param("endTime") Instant endTime);
    
    /**
     * Находит все минутные свечи для конкретного инструмента за день
     */
    @Query("SELECT c FROM MinuteCandleEntity c WHERE c.figi = :figi AND DATE(c.time) = DATE(:date) ORDER BY c.time")
    List<MinuteCandleEntity> findByFigiAndDate(@Param("figi") String figi, @Param("date") Instant date);
    
    /**
     * Удаляет все минутные свечи для конкретного инструмента в заданном временном диапазоне
     */
    @Query("DELETE FROM MinuteCandleEntity c WHERE c.figi = :figi AND c.time >= :startTime AND c.time <= :endTime")
    void deleteByFigiAndTimeBetween(@Param("figi") String figi, 
                                   @Param("startTime") Instant startTime, 
                                   @Param("endTime") Instant endTime);
    
    /**
     * Находит все минутные свечи за определенную дату
     */
    @Query("SELECT c FROM MinuteCandleEntity c WHERE DATE(c.time) = :date ORDER BY c.figi, c.time")
    List<MinuteCandleEntity> findByDate(@Param("date") LocalDate date);
    
    /**
     * Получает агрегированные данные для списка FIGI одним запросом
     * Оптимизированный метод для пакетной обработки
     */
    @Query(value = """
        SELECT c.figi, 
               DATE(c.time) as trade_date,
               SUM(CASE WHEN EXTRACT(HOUR FROM c.time) BETWEEN 6 AND 9 
                        AND EXTRACT(MINUTE FROM c.time) >= 50 
                        THEN c.volume ELSE 0 END) as morning_volume,
               SUM(CASE WHEN EXTRACT(DOW FROM c.time) IN (0, 6) 
                        THEN c.volume ELSE 0 END) as weekend_volume
        FROM invest.minute_candles c 
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
               SUM(CASE WHEN EXTRACT(HOUR FROM c.time) BETWEEN 6 AND 9 
                        AND EXTRACT(MINUTE FROM c.time) >= 50 
                        THEN c.volume ELSE 0 END) as morning_volume,
               SUM(CASE WHEN EXTRACT(DOW FROM c.time) IN (0, 6) 
                        THEN c.volume ELSE 0 END) as weekend_volume
        FROM invest.minute_candles c 
        WHERE c.figi = :figi 
        AND c.time >= :startDate 
        AND c.time < :endDate
        GROUP BY DATE(c.time)
        ORDER BY DATE(c.time)
        """, nativeQuery = true)
    List<Object[]> getAggregatedDataByFigiAndDateRange(@Param("figi") String figi,
                                                      @Param("startDate") Instant startDate,
                                                      @Param("endDate") Instant endDate);
    
    /**
     * Находит последнюю минутную свечу для конкретного инструмента за день
     */
    @Query("SELECT c FROM MinuteCandleEntity c WHERE c.figi = :figi AND DATE(c.time) = :date ORDER BY c.time DESC LIMIT 1")
    MinuteCandleEntity findLastCandleForDate(@Param("figi") String figi, @Param("date") LocalDate date);
}
