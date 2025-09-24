package com.example.InvestmentDataLoaderService.scheduler;

import com.example.InvestmentDataLoaderService.dto.ClosePriceRequestDto;
import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.service.MainSessionPriceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class ClosePriceSchedulerService {

    private final MainSessionPriceService mainSessionPriceService;

    public ClosePriceSchedulerService(MainSessionPriceService mainSessionPriceService) {
        this.mainSessionPriceService = mainSessionPriceService;
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "Europe/Moscow")
    public void fetchAndStoreClosePrices() {
        try {
            LocalDate previousDay = LocalDate.now(ZoneId.of("Europe/Moscow")).minusDays(1);
            
            System.out.println("Starting scheduled close prices fetch for " + previousDay + " (shares, futures only)");
            
            // Создаем пустой запрос для загрузки только RUB инструментов (акции, фьючерсы)
            ClosePriceRequestDto request = new ClosePriceRequestDto();
            
            // Вызываем метод saveClosePrices, который автоматически отберет только RUB инструменты (исключая indicatives)
            SaveResponseDto response = mainSessionPriceService.saveClosePrices(request);
            
            if (response.isSuccess()) {
                System.out.println("Scheduled close prices fetch completed successfully:");
                System.out.println("- Date: " + previousDay);
                System.out.println("- Message: " + response.getMessage());
                System.out.println("- Total requested: " + response.getTotalRequested());
                System.out.println("- New items saved: " + response.getNewItemsSaved());
                System.out.println("- Existing items skipped: " + response.getExistingItemsSkipped());
            } else {
                System.err.println("Scheduled close prices fetch failed: " + response.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Error in scheduled close prices fetch: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ручное обновление цен закрытия за конкретную дату
     * @param date дата для обновления цен
     */
    public void fetchAndUpdateClosePricesForDate(LocalDate date) {
        try {
            System.out.println("Starting manual close prices fetch for date: " + date + " (shares, futures only)");
            
            // Создаем пустой запрос для загрузки только RUB инструментов (акции, фьючерсы)
            ClosePriceRequestDto request = new ClosePriceRequestDto();
            
            // Вызываем метод saveClosePrices
            SaveResponseDto response = mainSessionPriceService.saveClosePrices(request);
            
            if (response.isSuccess()) {
                System.out.println("Manual close prices fetch completed successfully:");
                System.out.println("- Date: " + date);
                System.out.println("- Message: " + response.getMessage());
                System.out.println("- Total requested: " + response.getTotalRequested());
                System.out.println("- New items saved: " + response.getNewItemsSaved());
                System.out.println("- Existing items skipped: " + response.getExistingItemsSkipped());
            } else {
                System.err.println("Manual close prices fetch failed: " + response.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Error in manual close prices fetch for date " + date + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
