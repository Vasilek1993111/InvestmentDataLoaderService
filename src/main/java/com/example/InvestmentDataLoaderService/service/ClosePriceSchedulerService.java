package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.ClosePriceDto;
import com.example.InvestmentDataLoaderService.entity.ClosePriceEntity;
import com.example.InvestmentDataLoaderService.entity.ClosePriceKey;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.repository.ClosePriceRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClosePriceSchedulerService {

    private final TInvestService tInvestService;
    private final ShareRepository shareRepo;
    private final FutureRepository futureRepo;
    private final ClosePriceRepository closeRepo;

    @Autowired
    public ClosePriceSchedulerService(TInvestService tInvestService,
                                      ShareRepository shareRepo,
                                      FutureRepository futureRepo,
                                      ClosePriceRepository closeRepo) {
        this.tInvestService = tInvestService;
        this.shareRepo = shareRepo;
        this.futureRepo = futureRepo;
        this.closeRepo = closeRepo;
    }

    @Scheduled(cron = "0 0 20 * * *", zone = "Europe/Moscow")
    public void fetchAndStoreClosePrices() {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Moscow"));
        List<String> ids = new ArrayList<>();
        shareRepo.findAll().forEach(s -> ids.add(s.getFigi()));
        futureRepo.findAll().forEach(f -> ids.add(f.getFigi()));

        List<ClosePriceDto> prices = tInvestService.getClosePrices(ids, null);
        int newCount = 0;
        for (ClosePriceDto dto : prices) {
            ClosePriceKey key = new ClosePriceKey(today, dto.getFigi());
            if (closeRepo.existsById(key)) {
                continue;
            }
            String type = shareRepo.existsById(dto.getFigi()) ? "SHARE" : "FUTURE";
            ClosePriceEntity e = new ClosePriceEntity(today, dto.getFigi(), type, dto.getClosePrice(), "RUB", "MOEX");
            closeRepo.save(e);
            newCount++;
        }
        System.out.println("Saved " + newCount + " new close prices for " + today);
    }

    public void fetchAndUpdateClosePricesForDate(LocalDate date) {
        closeRepo.deleteByIdPriceDate(date);
        List<String> ids = new ArrayList<>();
        shareRepo.findAll().forEach(s -> ids.add(s.getFigi()));
        futureRepo.findAll().forEach(f -> ids.add(f.getFigi()));

        List<ClosePriceDto> prices = tInvestService.getClosePrices(ids, null);
        for (ClosePriceDto dto : prices) {
            String type = shareRepo.existsById(dto.getFigi()) ? "SHARE" : "FUTURE";
            ClosePriceEntity e = new ClosePriceEntity(date, dto.getFigi(), type, dto.getClosePrice(), "RUB", "MOEX");
            closeRepo.save(e);
        }
        System.out.println("Replaced close prices for date " + date);
    }
}
