package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.*;
import com.google.protobuf.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc.InstrumentsServiceBlockingStub;
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc.MarketDataServiceBlockingStub;
import ru.tinkoff.piapi.contract.v1.UsersServiceGrpc.UsersServiceBlockingStub;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class TradingService {

    private static final Logger log = LoggerFactory.getLogger(TradingService.class);
    private final InstrumentsServiceBlockingStub instrumentsService;
    private final MarketDataServiceBlockingStub marketDataService;
    private final UsersServiceBlockingStub usersService;

    public TradingService(InstrumentsServiceBlockingStub instrumentsService,
                         MarketDataServiceBlockingStub marketDataService,
                         UsersServiceBlockingStub usersService) {
        this.instrumentsService = instrumentsService;
        this.marketDataService = marketDataService;
        this.usersService = usersService;
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

    // === МЕТОДЫ ДЛЯ РАБОТЫ С ТОРГОВЫМИ РАСПИСАНИЯМИ ===

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
                        Instant.ofEpochSecond(d.getDate().getSeconds()).atZone(ZoneId.of("Europe/Moscow")).toLocalDate().toString(),
                        d.getIsTradingDay(),
                        Instant.ofEpochSecond(d.getStartTime().getSeconds()).atZone(ZoneId.of("Europe/Moscow")).toString(),
                        Instant.ofEpochSecond(d.getEndTime().getSeconds()).atZone(ZoneId.of("Europe/Moscow")).toString()
                ));
            }
            list.add(new TradingScheduleDto(ex.getExchange(), days));
        }
        return list;
    }

    // === МЕТОДЫ ДЛЯ РАБОТЫ С ТОРГОВЫМИ СТАТУСАМИ ===

    public List<TradingStatusDto> getTradingStatuses(List<String> instrumentIds) {
        GetTradingStatusesResponse res = marketDataService.getTradingStatuses(
                GetTradingStatusesRequest.newBuilder().addAllInstrumentId(instrumentIds).build());
        List<TradingStatusDto> list = new ArrayList<>();
        for (var s : res.getTradingStatusesList()) {
            list.add(new TradingStatusDto(s.getFigi(), s.getTradingStatus().name()));
        }
        return list;
    }

    public Map<String, Object> getTradingStatusesDetailed(List<String> instrumentIds) {
        List<TradingStatusDto> statuses = getTradingStatuses(instrumentIds);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", statuses);
        response.put("count", statuses.size());
        response.put("requested_instruments", instrumentIds.size());
        response.put("instruments", instrumentIds);
        
        return response;
    }

    // === МЕТОДЫ ДЛЯ АНАЛИЗА ТОРГОВЫХ ДНЕЙ ===

    public Map<String, Object> getTradingDays(String exchange, Instant from, Instant to) {
        List<TradingScheduleDto> schedules = getTradingSchedules(exchange, from, to);
        
        // Группируем по дням и определяем торговые дни
        Map<String, Object> tradingDays = new HashMap<>();
        int tradingDaysCount = 0;
        int nonTradingDaysCount = 0;
        
        for (TradingScheduleDto schedule : schedules) {
            for (TradingDayDto day : schedule.days()) {
                if (day.isTradingDay()) {
                    tradingDaysCount++;
                    tradingDays.put(day.date(), "trading");
                } else {
                    nonTradingDaysCount++;
                    tradingDays.put(day.date(), "non-trading");
                }
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("trading_days", tradingDays);
        response.put("trading_days_count", tradingDaysCount);
        response.put("non_trading_days_count", nonTradingDaysCount);
        response.put("total_days", tradingDaysCount + nonTradingDaysCount);
        response.put("from", from.toString());
        response.put("to", to.toString());
        response.put("exchange", exchange);
        
        return response;
    }

    // === МЕТОДЫ ДЛЯ СТАТИСТИКИ ТОРГОВ ===

    public Map<String, Object> getTradingStats(String exchange, Instant from, Instant to) {
        List<TradingScheduleDto> schedules = getTradingSchedules(exchange, from, to);
        
        // Анализируем расписания
        long tradingDays = 0;
        long nonTradingDays = 0;
        
        for (TradingScheduleDto schedule : schedules) {
            for (TradingDayDto day : schedule.days()) {
                if (day.isTradingDay()) {
                    tradingDays++;
                } else {
                    nonTradingDays++;
                }
            }
        }
        
        long totalDays = tradingDays + nonTradingDays;
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("period", Map.of(
            "from", from.toString(),
            "to", to.toString(),
            "exchange", exchange != null ? exchange : "all"
        ));
        response.put("trading_days", tradingDays);
        response.put("non_trading_days", nonTradingDays);
        response.put("total_days", totalDays);
        response.put("trading_percentage", totalDays > 0 ? (double) tradingDays / totalDays * 100 : 0);
        
        return response;
    }
}
