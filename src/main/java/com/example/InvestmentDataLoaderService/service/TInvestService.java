package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.google.protobuf.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc.InstrumentsServiceBlockingStub;
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc.MarketDataServiceBlockingStub;
import ru.tinkoff.piapi.contract.v1.UsersServiceGrpc.UsersServiceBlockingStub;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TInvestService {

    private final UsersServiceBlockingStub usersService;
    private final InstrumentsServiceBlockingStub instrumentsService;
    private final MarketDataServiceBlockingStub marketDataService;
    private final ShareRepository shareRepo;
    private final FutureRepository futureRepo;

    @Autowired
    public TInvestService(UsersServiceBlockingStub usersService,
                          InstrumentsServiceBlockingStub instrumentsService,
                          MarketDataServiceBlockingStub marketDataService,
                          ShareRepository shareRepo,
                          FutureRepository futureRepo) {
        this.usersService = usersService;
        this.instrumentsService = instrumentsService;
        this.marketDataService = marketDataService;
        this.shareRepo = shareRepo;
        this.futureRepo = futureRepo;
    }

    public List<AccountDto> getAccounts() {
        GetAccountsResponse res = usersService.getAccounts(GetAccountsRequest.newBuilder().build());
        List<AccountDto> list = new ArrayList<>();
        for (var a : res.getAccountsList()) {
            list.add(new AccountDto(a.getId(), a.getName(), a.getType().name()));
        }
        return list;
    }

    public List<ShareDto> getShares(String status, String exchange, String currency, String ticker) {
        // Определяем статус инструмента для запроса к API
        InstrumentStatus instrumentStatus = InstrumentStatus.INSTRUMENT_STATUS_BASE;
        if (status != null && !status.isEmpty()) {
            try {
                instrumentStatus = InstrumentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Если статус не найден, используем базовый статус
                instrumentStatus = InstrumentStatus.INSTRUMENT_STATUS_BASE;
            }
        }
        
        // Получаем данные из T-Bank API
        SharesResponse response = instrumentsService.shares(InstrumentsRequest.newBuilder()
                .setInstrumentStatus(instrumentStatus)
                .build());
        
        List<ShareDto> shares = new ArrayList<>();
        for (var instrument : response.getInstrumentsList()) {
            // Применяем фильтры
            boolean matchesExchange = (exchange == null || exchange.isEmpty() || 
                                     instrument.getExchange().equalsIgnoreCase(exchange));
            boolean matchesCurrency = (currency == null || currency.isEmpty() || 
                                     instrument.getCurrency().equalsIgnoreCase(currency));
            boolean matchesTicker = (ticker == null || ticker.isEmpty() || 
                                   instrument.getTicker().equalsIgnoreCase(ticker));
            
            if (matchesExchange && matchesCurrency && matchesTicker) {
                shares.add(new ShareDto(
                    instrument.getFigi(),
                    instrument.getTicker(),
                    instrument.getName(),
                    instrument.getCurrency(),
                    instrument.getExchange(),
                    instrument.getSector(),
                    instrument.getTradingStatus().name()
                ));
            }
        }
        
        // Сортируем по тикеру
        shares.sort(Comparator.comparing(ShareDto::getTicker, String.CASE_INSENSITIVE_ORDER));
        return shares;
    }

    public List<ShareDto> saveShares(String status, String exchange, String currency, String ticker) {
        // Получаем акции из API (используем существующий метод)
        List<ShareDto> sharesFromApi = getShares(status, exchange, currency, ticker);
        
        List<ShareDto> savedShares = new ArrayList<>();
        
        for (ShareDto shareDto : sharesFromApi) {
            // Проверяем, существует ли акция в БД
            if (!shareRepo.existsById(shareDto.getFigi())) {
                // Создаем и сохраняем новую акцию
                ShareEntity shareEntity = new ShareEntity(
                    shareDto.getFigi(),
                    shareDto.getTicker(),
                    shareDto.getName(),
                    shareDto.getCurrency(),
                    shareDto.getExchange()
                );
                
                try {
                    shareRepo.save(shareEntity);
                    savedShares.add(shareDto);
                } catch (Exception e) {
                    // Логируем ошибку, но продолжаем обработку других акций
                    System.err.println("Error saving share " + shareDto.getFigi() + ": " + e.getMessage());
                }
            }
        }
        
        return savedShares;
    }

    public List<FutureDto> getFutures(String status, String exchange) {
        FuturesResponse res = instrumentsService.futures(InstrumentsRequest.newBuilder()
                .setInstrumentStatus("".equals(status) ? InstrumentStatus.INSTRUMENT_STATUS_BASE : InstrumentStatus.valueOf(status))
                .build());
        List<FutureDto> list = new ArrayList<>();
        for (var f : res.getInstrumentsList()) {
            if (exchange == null || exchange.isEmpty() || f.getExchange().equalsIgnoreCase(exchange)) {
                list.add(new FutureDto(f.getFigi(), f.getTicker(), f.getAssetType(), f.getBasicAsset(), f.getCurrency(), f.getExchange()));
            }
        }
        // сохраняем новые
        for (var dto : list) {
            if (!futureRepo.existsById(dto.getFigi())) {
                futureRepo.save(new FutureEntity(dto.getFigi(), dto.getTicker(), dto.getAssetType(), dto.getBasicAsset(), dto.getCurrency(), dto.getExchange(), null));
            }
        }
        list.sort(Comparator.comparing(FutureDto::getTicker, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public List<TradingScheduleDto> getTradingSchedules(String exchange, Instant from, Instant to) {
        // Проверка периода (минимум 1 день, максимум 14 дней)
        long daysBetween = ChronoUnit.DAYS.between(from, to);
        if (daysBetween < 1) {
            throw new IllegalArgumentException("Period between 'from' and 'to' must be at least 1 day");
        }
        if (daysBetween > 14) {
            throw new IllegalArgumentException("Period between 'from' and 'to' cannot exceed 14 days");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must be before 'to'");
        }

        TradingSchedulesResponse res = instrumentsService.tradingSchedules(
                TradingSchedulesRequest.newBuilder()
                        .setExchange(exchange == null ? "" : exchange)
                        .setFrom(Timestamp.newBuilder().setSeconds(from.getEpochSecond()).setNanos(from.getNano()).build())
                        .setTo(Timestamp.newBuilder().setSeconds(to.getEpochSecond()).setNanos(to.getNano()).build())
                        .build());
        List<TradingScheduleDto> list = new ArrayList<>();
        for (var ex : res.getExchangesList()) {
            List<TradingDayDto> days = new ArrayList<>();
            for (var d : ex.getDaysList()) {
                days.add(new TradingDayDto(
                        Instant.ofEpochSecond(d.getDate().getSeconds()).atZone(ZoneId.of("UTC")).toLocalDate().toString(),
                        d.getIsTradingDay(),
                        Instant.ofEpochSecond(d.getStartTime().getSeconds()).atZone(ZoneId.of("UTC")).toString(),
                        Instant.ofEpochSecond(d.getEndTime().getSeconds()).atZone(ZoneId.of("UTC")).toString()
                ));
            }
            list.add(new TradingScheduleDto(ex.getExchange(), days));
        }
        return list;
    }

    public List<TradingStatusDto> getTradingStatuses(List<String> instrumentIds) {
        GetTradingStatusesResponse res = marketDataService.getTradingStatuses(
                GetTradingStatusesRequest.newBuilder().addAllInstrumentId(instrumentIds).build());
        List<TradingStatusDto> list = new ArrayList<>();
        for (var s : res.getTradingStatusesList()) {
            list.add(new TradingStatusDto(s.getFigi(), s.getTradingStatus().name()));
        }
        return list;
    }

    public List<ClosePriceDto> getClosePrices(List<String> instrumentIds, String status) {
        GetClosePricesRequest.Builder builder = GetClosePricesRequest.newBuilder();

        // Исправленный код: lambda для создания и добавления каждого InstrumentClosePriceRequest
        instrumentIds.forEach(id -> {
            builder.addInstruments(
                    InstrumentClosePriceRequest.newBuilder()
                            .setInstrumentId(id)
                            .build()
            );
        });

        GetClosePricesResponse res = marketDataService.getClosePrices(builder.build());
        List<ClosePriceDto> list = new ArrayList<>();
        for (var p : res.getClosePricesList()) {
            String date = Instant.ofEpochSecond(p.getTime().getSeconds())
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate()
                    .toString();
            Quotation qp = p.getPrice();
            BigDecimal price = BigDecimal.valueOf(qp.getUnits())
                    .add(BigDecimal.valueOf(qp.getNano()).movePointLeft(9));
            list.add(new ClosePriceDto(p.getFigi(), date, price));
        }
        return list;
    }
}
