package com.example.InvestmentDataLoaderService.scheduler;

import com.example.InvestmentDataLoaderService.dto.LastTradesRequestDto;
import com.example.InvestmentDataLoaderService.service.LastTradesService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

@Service
public class LastTradesSchedulerService {

    private final LastTradesService lastTradesService;

    public LastTradesSchedulerService(LastTradesService lastTradesService) {
        this.lastTradesService = lastTradesService;
    }

    /**
     * Планировщик обезличенных сделок
     * Запускается каждые 30 минут с 2:00 до 00:00 по московскому времени
     * Сначала загружает акции, затем фьючерсы
     */
    @Scheduled(cron = "0 0,30 2-23 * * *", zone = "Europe/Moscow")
    public void scheduledLastTradesLoading() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        String taskId = "SCHEDULED_LAST_TRADES_" + now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        
        System.out.println("=== НАЧАЛО ПЛАНИРУЕМОЙ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ===");
        System.out.println("[" + taskId + "] Время запуска: " + now);
        System.out.println("[" + taskId + "] Запуск каждые 30 минут с 2:00 до 00:00");
        
        try {
            // Этап 1: Загрузка обезличенных сделок по акциям
            System.out.println("[" + taskId + "] === ЭТАП 1: ЗАГРУЗКА АКЦИЙ ===");
            loadSharesLastTrades(taskId);
            
            // Небольшая пауза между этапами
            Thread.sleep(5000); // 5 секунд
            
            // Этап 2: Загрузка обезличенных сделок по фьючерсам
            System.out.println("[" + taskId + "] === ЭТАП 2: ЗАГРУЗКА ФЬЮЧЕРСОВ ===");
            loadFuturesLastTrades(taskId);
            
            System.out.println("[" + taskId + "] === ЗАВЕРШЕНИЕ ПЛАНИРУЕМОЙ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ===");
            System.out.println("[" + taskId + "] Время завершения: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] === ОШИБКА ПЛАНИРУЕМОЙ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ===");
            System.err.println("[" + taskId + "] Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Загрузка обезличенных сделок по акциям
     */
    private void loadSharesLastTrades(String taskId) {
        try {
            System.out.println("[" + taskId + "] Запуск загрузки обезличенных сделок по акциям...");
            
            LastTradesRequestDto request = new LastTradesRequestDto();
            request.setFigis(Arrays.asList("ALL_SHARES"));
            request.setTradeSource("TRADE_SOURCE_ALL");
            
            lastTradesService.fetchAndStoreLastTradesByRequestAsync(request);
            
            System.out.println("[" + taskId + "] Загрузка обезличенных сделок по акциям запущена в фоновом режиме");
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка запуска загрузки акций: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Загрузка обезличенных сделок по фьючерсам
     */
    private void loadFuturesLastTrades(String taskId) {
        try {
            System.out.println("[" + taskId + "] Запуск загрузки обезличенных сделок по фьючерсам...");
            
            LastTradesRequestDto request = new LastTradesRequestDto();
            request.setFigis(Arrays.asList("ALL_FUTURES"));
            request.setTradeSource("TRADE_SOURCE_ALL");
            
            lastTradesService.fetchAndStoreLastTradesByRequestAsync(request);
            
            System.out.println("[" + taskId + "] Загрузка обезличенных сделок по фьючерсам запущена в фоновом режиме");
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка запуска загрузки фьючерсов: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
