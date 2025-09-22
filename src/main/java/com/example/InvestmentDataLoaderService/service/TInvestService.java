package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.*;
import ru.tinkoff.piapi.contract.v1.UsersServiceGrpc.UsersServiceBlockingStub;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TInvestService {

    private final UsersServiceBlockingStub usersService;
    private final MarketDataService marketDataService;
    private final TradingService tradingService;

    public TInvestService(UsersServiceBlockingStub usersService,
                          MarketDataService marketDataService,
                          TradingService tradingService) {
        this.usersService = usersService;
        this.marketDataService = marketDataService;
        this.tradingService = tradingService;
    }

    // === МЕТОДЫ ДЛЯ РАБОТЫ С АККАУНТАМИ ===

    public List<AccountDto> getAccounts() {
        var res = usersService.getAccounts(ru.tinkoff.piapi.contract.v1.GetAccountsRequest.newBuilder().build());
        List<AccountDto> list = new ArrayList<>();
        for (var a : res.getAccountsList()) {
            list.add(new AccountDto(a.getId(), a.getName(), a.getType().name()));
        }
        return list;
    }


    // === ДЕЛЕГИРОВАНИЕ К TRADING SERVICE ===

    public List<TradingScheduleDto> getTradingSchedules(String exchange, java.time.Instant from, java.time.Instant to) {
        return tradingService.getTradingSchedules(exchange, from, to);
    }

    public List<TradingStatusDto> getTradingStatuses(List<String> instrumentIds) {
        return tradingService.getTradingStatuses(instrumentIds);
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
