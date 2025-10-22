package com.example.InvestmentDataLoaderService.scheduler;

import com.example.InvestmentDataLoaderService.dto.LastTradesRequestDto;
import com.example.InvestmentDataLoaderService.service.LastTradesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

@Service
public class LastTradesSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(LastTradesSchedulerService.class);
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
        
        log.info("=== НАЧАЛО ПЛАНИРУЕМОЙ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ===");
        log.info("[{}] Время запуска: {}", taskId, now);
        log.info("[{}] Запуск каждые 30 минут с 2:00 до 00:00", taskId);
        
        try {
            // Этап 1: Загрузка обезличенных сделок по акциям
            log.info("[{}] === ЭТАП 1: ЗАГРУЗКА АКЦИЙ ===", taskId);
            loadSharesLastTrades(taskId);
            
            // Небольшая пауза между этапами
            Thread.sleep(5000); // 5 секунд
            
            // Этап 2: Загрузка обезличенных сделок по фьючерсам
            log.info("[{}] === ЭТАП 2: ЗАГРУЗКА ФЬЮЧЕРСОВ ===", taskId);
            loadFuturesLastTrades(taskId);
            
            log.info("[{}] === ЗАВЕРШЕНИЕ ПЛАНИРУЕМОЙ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ===", taskId);
            log.info("[{}] Время завершения: {}", taskId, LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
        } catch (Exception e) {
            log.error("[{}] === ОШИБКА ПЛАНИРУЕМОЙ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ===", taskId, e);
        }
    }

    /**
     * Загрузка обезличенных сделок по акциям
     */
    private void loadSharesLastTrades(String taskId) {
        try {
            log.info("[{}] Запуск загрузки обезличенных сделок по акциям...", taskId);
            
            LastTradesRequestDto request = new LastTradesRequestDto();
            request.setFigis(Arrays.asList("ALL_SHARES"));
            request.setTradeSource("TRADE_SOURCE_ALL");
            
            lastTradesService.fetchAndStoreLastTradesByRequestAsync(request);
            
            log.info("[{}] Загрузка обезличенных сделок по акциям запущена в фоновом режиме", taskId);
            
        } catch (Exception e) {
            log.error("[{}] Ошибка запуска загрузки акций", taskId, e);
        }
    }

    /**
     * Загрузка обезличенных сделок по фьючерсам
     */
    private void loadFuturesLastTrades(String taskId) {
        try {
            log.info("[{}] Запуск загрузки обезличенных сделок по фьючерсам...", taskId);
            
            LastTradesRequestDto request = new LastTradesRequestDto();
            request.setFigis(Arrays.asList("ALL_FUTURES"));
            request.setTradeSource("TRADE_SOURCE_ALL");
            
            lastTradesService.fetchAndStoreLastTradesByRequestAsync(request);
            
            log.info("[{}] Загрузка обезличенных сделок по фьючерсам запущена в фоновом режиме", taskId);
            
        } catch (Exception e) {
            log.error("[{}] Ошибка запуска загрузки фьючерсов", taskId, e);
        }
    }
}
