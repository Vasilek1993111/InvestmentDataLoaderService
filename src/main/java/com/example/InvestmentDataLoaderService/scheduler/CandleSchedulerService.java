package com.example.InvestmentDataLoaderService.scheduler;

import com.example.InvestmentDataLoaderService.dto.CandleRequestDto;
import com.example.InvestmentDataLoaderService.service.TInvestService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.UUID;

@Service
public class CandleSchedulerService {

    private final TInvestService tInvestService;

    public CandleSchedulerService(TInvestService tInvestService) {
        this.tInvestService = tInvestService;
    }

    /**
     * Ежедневная загрузка минутных свечей за предыдущий день
     * Запускается в 1:10 по московскому времени
     * Сначала загружает акции, затем фьючерсы
     */
    @Scheduled(cron = "0 10 1 * * *", zone = "Europe/Moscow")
    public void fetchAndStoreCandles() {
        try {
            LocalDate previousDay = LocalDate.now(ZoneId.of("Europe/Moscow")).minusDays(1);
            
            System.out.println("=== НАЧАЛО ЕЖЕДНЕВНОЙ ЗАГРУЗКИ СВЕЧЕЙ ===");
            System.out.println("Дата: " + previousDay);
            System.out.println("Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Загружаем свечи для акций
            fetchCandlesForShares(previousDay);
            
            // Небольшая пауза между загрузкой акций и фьючерсов
            Thread.sleep(5000);
            
            // Загружаем свечи для фьючерсов
            fetchCandlesForFutures(previousDay);
            
            System.out.println("=== ЗАВЕРШЕНИЕ ЕЖЕДНЕВНОЙ ЗАГРУЗКИ СВЕЧЕЙ ===");
            System.out.println("Время завершения: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
        } catch (Exception e) {
            System.err.println("Критическая ошибка в ежедневной загрузке свечей: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Загружает минутные свечи для акций за указанную дату
     */
    private void fetchCandlesForShares(LocalDate date) {
        try {
            String taskId = "SHARES_" + UUID.randomUUID().toString().substring(0, 8);
            
            System.out.println("[" + taskId + "] Начало загрузки свечей для акций за " + date);
            
            CandleRequestDto request = new CandleRequestDto();
            request.setDate(date);
            request.setInterval("CANDLE_INTERVAL_1_MIN");
            request.setAssetType(Arrays.asList("SHARES"));
            
            // Запускаем загрузку в асинхронном режиме
            tInvestService.saveCandlesAsync(request, taskId);
            
            System.out.println("[" + taskId + "] Загрузка свечей для акций запущена в фоновом режиме");
            
        } catch (Exception e) {
            System.err.println("Ошибка при запуске загрузки свечей для акций: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Загружает минутные свечи для фьючерсов за указанную дату
     */
    private void fetchCandlesForFutures(LocalDate date) {
        try {
            String taskId = "FUTURES_" + UUID.randomUUID().toString().substring(0, 8);
            
            System.out.println("[" + taskId + "] Начало загрузки свечей для фьючерсов за " + date);
            
            CandleRequestDto request = new CandleRequestDto();
            request.setDate(date);
            request.setInterval("CANDLE_INTERVAL_1_MIN");
            request.setAssetType(Arrays.asList("FUTURES"));
            
            // Запускаем загрузку в асинхронном режиме
            tInvestService.saveCandlesAsync(request, taskId);
            
            System.out.println("[" + taskId + "] Загрузка свечей для фьючерсов запущена в фоновом режиме");
            
        } catch (Exception e) {
            System.err.println("Ошибка при запуске загрузки свечей для фьючерсов: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ручная загрузка свечей за конкретную дату
     * @param date дата для загрузки свечей
     */
    public void fetchAndStoreCandlesForDate(LocalDate date) {
        try {
            System.out.println("=== НАЧАЛО РУЧНОЙ ЗАГРУЗКИ СВЕЧЕЙ ===");
            System.out.println("Дата: " + date);
            System.out.println("Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Загружаем свечи для акций
            fetchCandlesForShares(date);
            
            // Небольшая пауза между загрузкой акций и фьючерсов
            Thread.sleep(5000);
            
            // Загружаем свечи для фьючерсов
            fetchCandlesForFutures(date);
            
            System.out.println("=== ЗАВЕРШЕНИЕ РУЧНОЙ ЗАГРУЗКИ СВЕЧЕЙ ===");
            System.out.println("Время завершения: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
        } catch (Exception e) {
            System.err.println("Критическая ошибка в ручной загрузке свечей: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ручная загрузка свечей только для акций за конкретную дату
     * @param date дата для загрузки свечей
     */
    public void fetchAndStoreSharesCandlesForDate(LocalDate date) {
        fetchCandlesForShares(date);
    }

    /**
     * Ручная загрузка свечей только для фьючерсов за конкретную дату
     * @param date дата для загрузки свечей
     */
    public void fetchAndStoreFuturesCandlesForDate(LocalDate date) {
        fetchCandlesForFutures(date);
    }
}
