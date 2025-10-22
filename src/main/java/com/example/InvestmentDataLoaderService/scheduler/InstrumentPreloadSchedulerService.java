package com.example.InvestmentDataLoaderService.scheduler;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.service.InstrumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class InstrumentPreloadSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(InstrumentPreloadSchedulerService.class);
    private static final String MOEX_EXCHANGE = "moex_mrng_evng_e_wknd_dlr";

    private final InstrumentService instrumentService;

    public InstrumentPreloadSchedulerService(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    /**
     * Ежедневный прогрев кеша и асинхронное сохранение инструментов:
     * - Акции: только биржа moex_mrng_evng_e_wknd_dlr
     * - Фьючерсы: все
     * - Индексы (индикативные инструменты): все
     * 
     * <p>Использует асинхронные методы для сохранения инструментов в БД.
     * Прогрев кеша выполняется синхронно, а сохранение - асинхронно с параллельной обработкой.</p>
     */
    @Scheduled(cron = "0 45 0 * * *", zone = "Europe/Moscow")
    public void preloadAndPersistInstruments() {
        String taskId = "PRELOAD_" + LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        log.info("[{}] Начало ежедневного прогрева кеша и асинхронного сохранения инструментов (00:45 MSK)", taskId);

        try {
            // Акции: только MOEX
            log.info("[{}] Загрузка акций (exchange={})", taskId, MOEX_EXCHANGE);
            ShareFilterDto shareFilter = new ShareFilterDto(null, MOEX_EXCHANGE, null, null, null, null, null);
            // Прогрев кеша
            List<ShareDto> shares = instrumentService.getShares(shareFilter.getStatus(), shareFilter.getExchange(), shareFilter.getCurrency(), shareFilter.getTicker(), shareFilter.getFigi());
            log.info("[{}] В кеш загружено акций: {}", taskId, shares.size());
            
            // Асинхронное сохранение в БД с параллельной обработкой
            String shareTaskId = taskId + "_SHARES";
            log.info("[{}] Запуск асинхронного сохранения акций (taskId: {})", taskId, shareTaskId);
            instrumentService.saveSharesAsync(shareFilter, shareTaskId)
                .thenAccept(result -> {
                    log.info("[{}] Асинхронное сохранение акций завершено: {}", shareTaskId, result.getMessage());
                })
                .exceptionally(throwable -> {
                    log.error("[{}] Ошибка асинхронного сохранения акций", shareTaskId, throwable);
                    return null;
                });

            // Фьючерсы: все
            log.info("[{}] Загрузка фьючерсов (все)", taskId);
            FutureFilterDto futureFilter = new FutureFilterDto(null, null, null, null, null);
            List<FutureDto> futures = instrumentService.getFutures(
                    futureFilter.getStatus(), futureFilter.getExchange(), futureFilter.getCurrency(), futureFilter.getTicker(), futureFilter.getAssetType());
            log.info("[{}] В кеш загружено фьючерсов: {}", taskId, futures.size());
            
            // Асинхронное сохранение фьючерсов
            String futureTaskId = taskId + "_FUTURES";
            log.info("[{}] Запуск асинхронного сохранения фьючерсов (taskId: {})", taskId, futureTaskId);
            instrumentService.saveFuturesAsync(futureFilter, futureTaskId)
                .thenAccept(result -> {
                    log.info("[{}] Асинхронное сохранение фьючерсов завершено: {}", futureTaskId, result.getMessage());
                })
                .exceptionally(throwable -> {
                    log.error("[{}] Ошибка асинхронного сохранения фьючерсов", futureTaskId, throwable);
                    return null;
                });

            // Индексы (индикативные): все
            log.info("[{}] Загрузка индексов/индикативных инструментов (все)", taskId);
            IndicativeFilterDto indicativeFilter = new IndicativeFilterDto(null, null, null, null);
            List<IndicativeDto> indicatives = instrumentService.getIndicatives(
                    indicativeFilter.getExchange(), indicativeFilter.getCurrency(), indicativeFilter.getTicker(), indicativeFilter.getFigi());
            log.info("[{}] В кеш загружено индикативных инструментов: {}", taskId, indicatives.size());
            
            // Асинхронное сохранение индикативов
            String indicativeTaskId = taskId + "_INDICATIVES";
            log.info("[{}] Запуск асинхронного сохранения индикативов (taskId: {})", taskId, indicativeTaskId);
            instrumentService.saveIndicativesAsync(indicativeFilter, indicativeTaskId)
                .thenAccept(result -> {
                    log.info("[{}] Асинхронное сохранение индикативов завершено: {}", indicativeTaskId, result.getMessage());
                })
                .exceptionally(throwable -> {
                    log.error("[{}] Ошибка асинхронного сохранения индикативов", indicativeTaskId, throwable);
                    return null;
                });

            log.info("[{}] Прогрев кеша завершен, асинхронное сохранение инструментов запущено", taskId);
            log.info("[{}] Отслеживайте статус операций по taskId: {}, {}, {}", taskId, shareTaskId, futureTaskId, indicativeTaskId);
            
        } catch (Exception e) {
            log.error("[{}] Ошибка ежедневного прогрева кеша", taskId, e);
        }
    }
}


