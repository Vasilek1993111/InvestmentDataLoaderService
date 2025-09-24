package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.*;
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
        System.out.println("[" + taskId + "] Начало прогрева кэша при запуске приложения");

        try {
            // Прогрев кэша акций (только MOEX)
            System.out.println("[" + taskId + "] Прогрев кэша акций (exchange=" + MOEX_EXCHANGE + ")");
            ShareFilterDto shareFilter = new ShareFilterDto(null, MOEX_EXCHANGE, null, null, null, null, null);
            List<ShareDto> shares = instrumentService.getShares(
                shareFilter.getStatus(), 
                shareFilter.getExchange(), 
                shareFilter.getCurrency(), 
                shareFilter.getTicker(), 
                shareFilter.getFigi()
            );
            System.out.println("[" + taskId + "] В кэш загружено акций: " + shares.size());

            // Прогрев кэша фьючерсов (все)
            System.out.println("[" + taskId + "] Прогрев кэша фьючерсов (все)");
            FutureFilterDto futureFilter = new FutureFilterDto(null, null, null, null, null);
            List<FutureDto> futures = instrumentService.getFutures(
                futureFilter.getStatus(), 
                futureFilter.getExchange(), 
                futureFilter.getCurrency(), 
                futureFilter.getTicker(), 
                futureFilter.getAssetType()
            );
            System.out.println("[" + taskId + "] В кэш загружено фьючерсов: " + futures.size());

            // Прогрев кэша индикативных инструментов (все)
            System.out.println("[" + taskId + "] Прогрев кэша индикативных инструментов (все)");
            IndicativeFilterDto indicativeFilter = new IndicativeFilterDto(null, null, null, null);
            List<IndicativeDto> indicatives = instrumentService.getIndicatives(
                indicativeFilter.getExchange(), 
                indicativeFilter.getCurrency(), 
                indicativeFilter.getTicker(), 
                indicativeFilter.getFigi()
            );
            System.out.println("[" + taskId + "] В кэш загружено индикативных инструментов: " + indicatives.size());

            System.out.println("[" + taskId + "] Прогрев кэша при запуске завершен успешно");
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка при прогреве кэша при запуске: " + e.getMessage());
            e.printStackTrace();
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
        System.out.println("[" + taskId + "] Начало ручного прогрева кэша");

        try {
            // Прогрев кэша акций
            System.out.println("[" + taskId + "] Прогрев кэша акций");
            ShareFilterDto shareFilter = new ShareFilterDto(null, MOEX_EXCHANGE, null, null, null, null, null);
            List<ShareDto> shares = instrumentService.getShares(
                shareFilter.getStatus(), 
                shareFilter.getExchange(), 
                shareFilter.getCurrency(), 
                shareFilter.getTicker(), 
                shareFilter.getFigi()
            );
            System.out.println("[" + taskId + "] В кэш загружено акций: " + shares.size());

            // Прогрев кэша фьючерсов
            System.out.println("[" + taskId + "] Прогрев кэша фьючерсов");
            FutureFilterDto futureFilter = new FutureFilterDto(null, null, null, null, null);
            List<FutureDto> futures = instrumentService.getFutures(
                futureFilter.getStatus(), 
                futureFilter.getExchange(), 
                futureFilter.getCurrency(), 
                futureFilter.getTicker(), 
                futureFilter.getAssetType()
            );
            System.out.println("[" + taskId + "] В кэш загружено фьючерсов: " + futures.size());

            // Прогрев кэша индикативных инструментов
            System.out.println("[" + taskId + "] Прогрев кэша индикативных инструментов");
            IndicativeFilterDto indicativeFilter = new IndicativeFilterDto(null, null, null, null);
            List<IndicativeDto> indicatives = instrumentService.getIndicatives(
                indicativeFilter.getExchange(), 
                indicativeFilter.getCurrency(), 
                indicativeFilter.getTicker(), 
                indicativeFilter.getFigi()
            );
            System.out.println("[" + taskId + "] В кэш загружено индикативных инструментов: " + indicatives.size());

            System.out.println("[" + taskId + "] Ручной прогрев кэша завершен успешно");
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка при ручном прогреве кэша: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Ошибка при ручном прогреве кэша", e);
        }
    }
}
