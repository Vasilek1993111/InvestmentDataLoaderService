package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.*;
import ru.tinkoff.piapi.contract.v1.UsersServiceGrpc.UsersServiceBlockingStub;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TInvestService {

    private final UsersServiceBlockingStub usersService;
    private final InstrumentService instrumentService;
    private final MarketDataService marketDataService;
    private final TradingService tradingService;

    public TInvestService(UsersServiceBlockingStub usersService,
                          InstrumentService instrumentService,
                          MarketDataService marketDataService,
                          TradingService tradingService) {
        this.usersService = usersService;
        this.instrumentService = instrumentService;
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

    // === ДЕЛЕГИРОВАНИЕ К INSTRUMENT SERVICE ===

    public List<ShareDto> getShares(String status, String exchange, String currency, String ticker, String figi) {
        return instrumentService.getShares(status, exchange, currency, ticker, figi);
    }

    public List<ShareDto> saveShares(String status, String exchange, String currency, String ticker) {
        ShareFilterDto filter = new ShareFilterDto();
        filter.setStatus(status);
        filter.setExchange(exchange);
        filter.setCurrency(currency);
        filter.setTicker(ticker);
        
        SaveResponseDto response = instrumentService.saveShares(filter);
        @SuppressWarnings("unchecked")
        List<ShareDto> result = (List<ShareDto>) response.getSavedItems();
        return result;
    }

    public SaveResponseDto saveShares(ShareFilterDto filter) {
        return instrumentService.saveShares(filter);
    }

    public List<ShareDto> getSharesFromDatabase(ShareFilterDto filter) {
        return instrumentService.getSharesFromDatabase(filter);
    }

    public ShareDto getShareByFigi(String figi) {
        return instrumentService.getShareByFigi(figi);
    }

    public ShareDto getShareByTicker(String ticker) {
        return instrumentService.getShareByTicker(ticker);
    }

    public SaveResponseDto updateShare(ShareDto shareDto) {
        return instrumentService.updateShare(shareDto);
    }

    public List<FutureDto> getFutures(String status, String exchange, String currency, String ticker, String assetType) {
        return instrumentService.getFutures(status, exchange, currency, ticker, assetType);
    }

    public List<FutureDto> saveFutures(String status, String exchange, String currency, String ticker, String assetType) {
        FutureFilterDto filter = new FutureFilterDto();
        filter.setStatus(status);
        filter.setExchange(exchange);
        filter.setCurrency(currency);
        filter.setTicker(ticker);
        filter.setAssetType(assetType);
        
        SaveResponseDto response = instrumentService.saveFutures(filter);
        @SuppressWarnings("unchecked")
        List<FutureDto> result = (List<FutureDto>) response.getSavedItems();
        return result;
    }

    public SaveResponseDto saveFutures(FutureFilterDto filter) {
        return instrumentService.saveFutures(filter);
    }

    public FutureDto getFutureByFigi(String figi) {
        return instrumentService.getFutureByFigi(figi);
    }

    public FutureDto getFutureByTicker(String ticker) {
        return instrumentService.getFutureByTicker(ticker);
    }

    public List<IndicativeDto> getIndicatives(String exchange, String currency, String ticker, String figi) {
        return instrumentService.getIndicatives(exchange, currency, ticker, figi);
    }

    public IndicativeDto getIndicativeBy(String figi) {
        return instrumentService.getIndicativeBy(figi);
    }

    public IndicativeDto getIndicativeByTicker(String ticker) {
        return instrumentService.getIndicativeByTicker(ticker);
    }

    public SaveResponseDto saveIndicatives(IndicativeFilterDto filter) {
        return instrumentService.saveIndicatives(filter);
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
