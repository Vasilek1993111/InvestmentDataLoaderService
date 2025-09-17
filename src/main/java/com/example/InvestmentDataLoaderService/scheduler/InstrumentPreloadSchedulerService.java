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
     * Ежедневный прогрев кеша и сохранение инструментов:
     * - Акции: только биржа moex_mrng_evng_e_wknd_dlr
     * - Фьючерсы: все
     * - Индексы (индикативные инструменты): все
     */
    @Scheduled(cron = "0 45 0 * * *", zone = "Europe/Moscow")
    public void preloadAndPersistInstruments() {
        String taskId = "PRELOAD_" + LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        System.out.println("[" + taskId + "] Начало ежедневного прогрева кеша и сохранения инструментов (00:45 MSK)");

        try {
            // Акции: только MOEX
            System.out.println("[" + taskId + "] Загрузка акций (exchange=" + MOEX_EXCHANGE + ")");
            ShareFilterDto shareFilter = new ShareFilterDto(null, MOEX_EXCHANGE, null, null, null, null, null);
            // Прогрев кеша
            List<ShareDto> shares = instrumentService.getShares(shareFilter.getStatus(), shareFilter.getExchange(), shareFilter.getCurrency(), shareFilter.getTicker(), shareFilter.getFigi());
            System.out.println("[" + taskId + "] В кеш загружено акций: " + shares.size());
            // Сохранение в БД без дублей
            SaveResponseDto shareSave = instrumentService.saveShares(shareFilter);
            System.out.println("[" + taskId + "] Сохранение акций: " + shareSave.getMessage());

            // Фьючерсы: все
            System.out.println("[" + taskId + "] Загрузка фьючерсов (все)");
            FutureFilterDto futureFilter = new FutureFilterDto(null, null, null, null, null);
            List<FutureDto> futures = instrumentService.getFutures(
                    futureFilter.getStatus(), futureFilter.getExchange(), futureFilter.getCurrency(), futureFilter.getTicker(), futureFilter.getAssetType());
            System.out.println("[" + taskId + "] В кеш загружено фьючерсов: " + futures.size());
            SaveResponseDto futureSave = instrumentService.saveFutures(futureFilter);
            System.out.println("[" + taskId + "] Сохранение фьючерсов: " + futureSave.getMessage());

            // Индексы (индикативные): все
            System.out.println("[" + taskId + "] Загрузка индексов/индикативных инструментов (все)");
            IndicativeFilterDto indicativeFilter = new IndicativeFilterDto(null, null, null, null);
            List<IndicativeDto> indicatives = instrumentService.getIndicatives(
                    indicativeFilter.getExchange(), indicativeFilter.getCurrency(), indicativeFilter.getTicker(), indicativeFilter.getFigi());
            System.out.println("[" + taskId + "] В кеш загружено индикативных инструментов: " + indicatives.size());
            SaveResponseDto indicativeSave = instrumentService.saveIndicatives(indicativeFilter);
            System.out.println("[" + taskId + "] Сохранение индикативных: " + indicativeSave.getMessage());

            System.out.println("[" + taskId + "] Прогрев кеша и сохранение инструментов завершены");
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка ежедневного прогрева/сохранения инструментов: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


