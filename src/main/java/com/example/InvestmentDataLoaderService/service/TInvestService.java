package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TInvestService {

    private final MarketDataService marketDataService;

    public TInvestService(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }


    // === ДЕЛЕГИРОВАНИЕ К MARKET DATA SERVICE ===

    public List<ClosePriceDto> getClosePrices(List<String> instrumentIds, String status) {
        return marketDataService.getClosePrices(instrumentIds, status);
    }

    public SaveResponseDto saveClosePrices(ClosePriceRequestDto request) {
        return marketDataService.saveClosePrices(request);
    }

    public SaveResponseDto saveClosePricesEveningSession(ClosePriceEveningSessionRequestDto request) {
        return marketDataService.saveClosePricesEveningSession(request);
    }

    public List<ClosePriceDto> getClosePricesForAllShares() {
        return marketDataService.getClosePricesForAllShares();
    }

    public List<ClosePriceDto> getClosePricesForAllFutures() {
        return marketDataService.getClosePricesForAllFutures();
    }

    public List<CandleDto> getCandles(String instrumentId, java.time.LocalDate date, String interval) {
        return marketDataService.getCandles(instrumentId, date, interval);
    }

    public java.util.concurrent.CompletableFuture<SaveResponseDto> saveCandles(CandleRequestDto request) {
        return marketDataService.saveCandles(request);
    }

    public void saveCandlesAsync(CandleRequestDto request, String taskId) {
        marketDataService.saveCandlesAsync(request, taskId);
    }

    public List<LastTradeDto> getLastTrades(String instrumentId, java.time.LocalDate date, String tradeSource) {
        return marketDataService.getLastTrades(instrumentId, date, tradeSource);
    }
    
    public List<LastTradeDto> getLastTrades(String instrumentId, java.time.LocalDate date, String tradeSource, java.time.Instant fromTime, java.time.Instant toTime) {
        return marketDataService.getLastTrades(instrumentId, date, tradeSource, fromTime, toTime);
    }

    public List<LastTradeDto> getLastTradesForLastHour(String instrumentId, String tradeSource) {
        return marketDataService.getLastTradesForLastHour(instrumentId, tradeSource);
    }

}
