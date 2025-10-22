package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Сервис для прогрева кэша при запуске приложения
 * 
 * <p>Выполняет загрузку основных инструментов в кэш после полной инициализации Spring,
 * что обеспечивает быстрый отклик при первом обращении к API.</p>
 * 
 * <p>Загружает в кэш:</p>
 * <ul>
 *   <li>Акции с биржи MOEX</li>
 *   <li>Все фьючерсы</li>
 *   <li>Все индикативные инструменты</li>
 * </ul>
 * 
 * @author InvestmentDataLoaderService
 * @version 1.0
 * @since 2024
 */
@Service
public class CacheWarmupService {

    private static final Logger log = LoggerFactory.getLogger(CacheWarmupService.class);
    private static final String MOEX_EXCHANGE = "moex_mrng_evng_e_wknd_dlr";
    
    private final InstrumentService instrumentService;

    public CacheWarmupService(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    /**
     * Прогрев кэша при запуске приложения
     * 
     * <p>Выполняется после полной инициализации Spring контекста.
     * Загружает основные инструменты в кэш для обеспечения быстрого отклика API.</p>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmupCacheOnStartup() {
        String taskId = "STARTUP_WARMUP_" + LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        log.info("[{}] Начало прогрева кэша при запуске приложения", taskId);

        try {
            // Прогрев кэша акций (только MOEX)
            log.info("[{}] Warming up shares cache (exchange={})", taskId, MOEX_EXCHANGE);
            ShareFilterDto shareFilter = new ShareFilterDto(null, MOEX_EXCHANGE, null, null, null, null, null);
            List<ShareDto> shares = instrumentService.getShares(
                shareFilter.getStatus(), 
                shareFilter.getExchange(), 
                shareFilter.getCurrency(), 
                shareFilter.getTicker(), 
                shareFilter.getFigi()
            );
            log.info("[{}] В кэш загружено акций: {}", taskId, shares.size());

            // Прогрев кэша фьючерсов (все)
            log.info("[{}] Прогрев кэша фьючерсов (все)", taskId);
            FutureFilterDto futureFilter = new FutureFilterDto(null, null, null, null, null);
            List<FutureDto> futures = instrumentService.getFutures(
                futureFilter.getStatus(), 
                futureFilter.getExchange(), 
                futureFilter.getCurrency(), 
                futureFilter.getTicker(), 
                futureFilter.getAssetType()
            );
            log.info("[{}] В кэш загружено фьючерсов: {}", taskId, futures.size());

            // Прогрев кэша индикативных инструментов (все)
            log.info("[{}] Прогрев кэша индикативных инструментов (все)", taskId);
            IndicativeFilterDto indicativeFilter = new IndicativeFilterDto(null, null, null, null);
            List<IndicativeDto> indicatives = instrumentService.getIndicatives(
                indicativeFilter.getExchange(), 
                indicativeFilter.getCurrency(), 
                indicativeFilter.getTicker(), 
                indicativeFilter.getFigi()
            );
            log.info("[{}] В кэш загружено индикативных инструментов: {}", taskId, indicatives.size());

            log.info("[{}] Прогрев кэша при запуске завершен успешно", taskId);
            
        } catch (Exception e) {
            log.error("[{}] Ошибка при прогреве кэша при запуске: {}", taskId, e.getMessage(), e);
        }
    }

    /**
     * Ручной прогрев кэша (для тестирования или принудительного обновления)
     * 
     * <p>Может быть вызван через REST API или другие сервисы для принудительного
     * прогрева кэша без перезапуска приложения.</p>
     */
    public void manualWarmupCache() {
        String taskId = "MANUAL_WARMUP_" + LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        log.info("[{}] Начало ручного прогрева кэша", taskId);

        try {
            // Прогрев кэша акций
            log.info("[{}] Прогрев кэша акций", taskId);
            ShareFilterDto shareFilter = new ShareFilterDto(null, MOEX_EXCHANGE, null, null, null, null, null);
            List<ShareDto> shares = instrumentService.getShares(
                shareFilter.getStatus(), 
                shareFilter.getExchange(), 
                shareFilter.getCurrency(), 
                shareFilter.getTicker(), 
                shareFilter.getFigi()
            );
            log.info("[{}] В кэш загружено акций: {}", taskId, shares.size());

            // Прогрев кэша фьючерсов
            log.info("[{}] Прогрев кэша фьючерсов", taskId);
            FutureFilterDto futureFilter = new FutureFilterDto(null, null, null, null, null);
            List<FutureDto> futures = instrumentService.getFutures(
                futureFilter.getStatus(), 
                futureFilter.getExchange(), 
                futureFilter.getCurrency(), 
                futureFilter.getTicker(), 
                futureFilter.getAssetType()
            );
            log.info("[{}] В кэш загружено фьючерсов: {}", taskId, futures.size());

            // Прогрев кэша индикативных инструментов
            log.info("[{}] Прогрев кэша индикативных инструментов", taskId);
            IndicativeFilterDto indicativeFilter = new IndicativeFilterDto(null, null, null, null);
            List<IndicativeDto> indicatives = instrumentService.getIndicatives(
                indicativeFilter.getExchange(), 
                indicativeFilter.getCurrency(), 
                indicativeFilter.getTicker(), 
                indicativeFilter.getFigi()
            );
            log.info("[{}] В кэш загружено индикативных инструментов: {}", taskId, indicatives.size());

            log.info("[{}] Ручной прогрев кэша завершен успешно", taskId);
            
        } catch (Exception e) {
            log.error("[{}] Ошибка при ручном прогреве кэша: {}", taskId, e.getMessage(), e);
            throw new RuntimeException("Ошибка при ручном прогреве кэша", e);
        }
    }
}
