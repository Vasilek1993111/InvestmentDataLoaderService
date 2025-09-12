package com.example.InvestmentDataLoaderService.repository;

import com.example.InvestmentDataLoaderService.entity.CandleEntity;
import com.example.InvestmentDataLoaderService.entity.CandleKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CandleRepository extends JpaRepository<CandleEntity, CandleKey> {
    
    /**
     * Проверяет существование свечи по FIGI и времени
     */
    boolean existsByFigiAndTime(String figi, Instant time);
    
    /**
     * Находит все свечи для конкретного инструмента в заданном временном диапазоне
     */
    @Query("SELECT c FROM CandleEntity c WHERE c.figi = :figi AND c.time >= :startTime AND c.time <= :endTime ORDER BY c.time")
    List<CandleEntity> findByFigiAndTimeBetween(@Param("figi") String figi, 
                                               @Param("startTime") Instant startTime, 
                                               @Param("endTime") Instant endTime);
    
    /**
     * Находит все свечи для конкретного инструмента за день
     */
    @Query("SELECT c FROM CandleEntity c WHERE c.figi = :figi AND DATE(c.time) = DATE(:date) ORDER BY c.time")
    List<CandleEntity> findByFigiAndDate(@Param("figi") String figi, @Param("date") Instant date);
    
    /**
     * Удаляет все свечи для конкретного инструмента в заданном временном диапазоне
     */
    @Query("DELETE FROM CandleEntity c WHERE c.figi = :figi AND c.time >= :startTime AND c.time <= :endTime")
    void deleteByFigiAndTimeBetween(@Param("figi") String figi, 
                                   @Param("startTime") Instant startTime, 
                                   @Param("endTime") Instant endTime);
    
    /**
     * Находит последнюю свечу для конкретного инструмента за указанную дату
     * Использует диапазон дат для избежания проблем с часовыми поясами
     */
    @Query("SELECT c FROM CandleEntity c WHERE c.figi = :figi AND DATE(c.time) >= :date AND DATE(c.time) < :nextDate ORDER BY c.time DESC")
    List<CandleEntity> findByFigiAndDateOrderByTimeDesc(@Param("figi") String figi, 
                                                       @Param("date") LocalDate date,
                                                       @Param("nextDate") LocalDate nextDate);
    
    /**
     * Находит все свечи для конкретного инструмента
     */
    @Query("SELECT c FROM CandleEntity c WHERE c.figi = :figi ORDER BY c.time")
    List<CandleEntity> findByFigi(@Param("figi") String figi);
    
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
        FROM invest.candles c 
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
        FROM invest.candles c 
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