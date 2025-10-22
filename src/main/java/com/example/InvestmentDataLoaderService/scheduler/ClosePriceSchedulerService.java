package com.example.InvestmentDataLoaderService.scheduler;

import com.example.InvestmentDataLoaderService.dto.ClosePriceRequestDto;
import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.service.MainSessionPriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class ClosePriceSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(ClosePriceSchedulerService.class);
    private final MainSessionPriceService mainSessionPriceService;

    public ClosePriceSchedulerService(MainSessionPriceService mainSessionPriceService) {
        this.mainSessionPriceService = mainSessionPriceService;
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "Europe/Moscow")
    public void fetchAndStoreClosePrices() {
        try {
            LocalDate previousDay = LocalDate.now(ZoneId.of("Europe/Moscow")).minusDays(1);
            
            log.info("Starting scheduled close prices fetch for {} (shares, futures only)", previousDay);
            
            // Создаем пустой запрос для загрузки только RUB инструментов (акции, фьючерсы)
            ClosePriceRequestDto request = new ClosePriceRequestDto();
            
            // Вызываем метод saveClosePrices, который автоматически отберет только RUB инструменты (исключая indicatives)
            SaveResponseDto response = mainSessionPriceService.saveClosePrices(request);
            
            if (response.isSuccess()) {
                log.info("Scheduled close prices fetch completed successfully:");
                log.info("- Date: {}", previousDay);
                log.info("- Message: {}", response.getMessage());
                log.info("- Total requested: {}", response.getTotalRequested());
                log.info("- New items saved: {}", response.getNewItemsSaved());
                log.info("- Existing items skipped: {}", response.getExistingItemsSkipped());
            } else {
                log.error("Scheduled close prices fetch failed: {}", response.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Error in scheduled close prices fetch", e);
        }
    }

    /**
     * Ручное обновление цен закрытия за конкретную дату
     * @param date дата для обновления цен
     */
    public void fetchAndUpdateClosePricesForDate(LocalDate date) {
        try {
            log.info("Starting manual close prices fetch for date: {} (shares, futures only)", date);
            
            // Создаем пустой запрос для загрузки только RUB инструментов (акции, фьючерсы)
            ClosePriceRequestDto request = new ClosePriceRequestDto();
            
            // Вызываем метод saveClosePrices
            SaveResponseDto response = mainSessionPriceService.saveClosePrices(request);
            
            if (response.isSuccess()) {
                log.info("Manual close prices fetch completed successfully:");
                log.info("- Date: {}", date);
                log.info("- Message: {}", response.getMessage());
                log.info("- Total requested: {}", response.getTotalRequested());
                log.info("- New items saved: {}", response.getNewItemsSaved());
                log.info("- Existing items skipped: {}", response.getExistingItemsSkipped());
            } else {
                log.error("Manual close prices fetch failed: {}", response.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Error in manual close prices fetch for date {}", date, e);
        }
    }
}
