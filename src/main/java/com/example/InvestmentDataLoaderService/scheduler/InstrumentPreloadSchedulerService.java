package com.example.InvestmentDataLoaderService.scheduler;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.service.InstrumentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class InstrumentPreloadSchedulerService {

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
        System.out.println("[" + taskId + "] Начало ежедневного прогрева кеша и асинхронного сохранения инструментов (00:45 MSK)");

        try {
            // Акции: только MOEX
            System.out.println("[" + taskId + "] Загрузка акций (exchange=" + MOEX_EXCHANGE + ")");
            ShareFilterDto shareFilter = new ShareFilterDto(null, MOEX_EXCHANGE, null, null, null, null, null);
            // Прогрев кеша
            List<ShareDto> shares = instrumentService.getShares(shareFilter.getStatus(), shareFilter.getExchange(), shareFilter.getCurrency(), shareFilter.getTicker(), shareFilter.getFigi());
            System.out.println("[" + taskId + "] В кеш загружено акций: " + shares.size());
            
            // Асинхронное сохранение в БД с параллельной обработкой
            String shareTaskId = taskId + "_SHARES";
            System.out.println("[" + taskId + "] Запуск асинхронного сохранения акций (taskId: " + shareTaskId + ")");
            instrumentService.saveSharesAsync(shareFilter, shareTaskId)
                .thenAccept(result -> {
                    System.out.println("[" + shareTaskId + "] Асинхронное сохранение акций завершено: " + result.getMessage());
                })
                .exceptionally(throwable -> {
                    System.err.println("[" + shareTaskId + "] Ошибка асинхронного сохранения акций: " + throwable.getMessage());
                    return null;
                });

            // Фьючерсы: все
            System.out.println("[" + taskId + "] Загрузка фьючерсов (все)");
            FutureFilterDto futureFilter = new FutureFilterDto(null, null, null, null, null);
            List<FutureDto> futures = instrumentService.getFutures(
                    futureFilter.getStatus(), futureFilter.getExchange(), futureFilter.getCurrency(), futureFilter.getTicker(), futureFilter.getAssetType());
            System.out.println("[" + taskId + "] В кеш загружено фьючерсов: " + futures.size());
            
            // Асинхронное сохранение фьючерсов
            String futureTaskId = taskId + "_FUTURES";
            System.out.println("[" + taskId + "] Запуск асинхронного сохранения фьючерсов (taskId: " + futureTaskId + ")");
            instrumentService.saveFuturesAsync(futureFilter, futureTaskId)
                .thenAccept(result -> {
                    System.out.println("[" + futureTaskId + "] Асинхронное сохранение фьючерсов завершено: " + result.getMessage());
                })
                .exceptionally(throwable -> {
                    System.err.println("[" + futureTaskId + "] Ошибка асинхронного сохранения фьючерсов: " + throwable.getMessage());
                    return null;
                });

            // Индексы (индикативные): все
            System.out.println("[" + taskId + "] Загрузка индексов/индикативных инструментов (все)");
            IndicativeFilterDto indicativeFilter = new IndicativeFilterDto(null, null, null, null);
            List<IndicativeDto> indicatives = instrumentService.getIndicatives(
                    indicativeFilter.getExchange(), indicativeFilter.getCurrency(), indicativeFilter.getTicker(), indicativeFilter.getFigi());
            System.out.println("[" + taskId + "] В кеш загружено индикативных инструментов: " + indicatives.size());
            
            // Асинхронное сохранение индикативов
            String indicativeTaskId = taskId + "_INDICATIVES";
            System.out.println("[" + taskId + "] Запуск асинхронного сохранения индикативов (taskId: " + indicativeTaskId + ")");
            instrumentService.saveIndicativesAsync(indicativeFilter, indicativeTaskId)
                .thenAccept(result -> {
                    System.out.println("[" + indicativeTaskId + "] Асинхронное сохранение индикативов завершено: " + result.getMessage());
                })
                .exceptionally(throwable -> {
                    System.err.println("[" + indicativeTaskId + "] Ошибка асинхронного сохранения индикативов: " + throwable.getMessage());
                    return null;
                });

            System.out.println("[" + taskId + "] Прогрев кеша завершен, асинхронное сохранение инструментов запущено");
            System.out.println("[" + taskId + "] Отслеживайте статус операций по taskId: " + shareTaskId + ", " + futureTaskId + ", " + indicativeTaskId);
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка ежедневного прогрева кеша: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


