package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.TradingScheduleDto;
import com.example.InvestmentDataLoaderService.dto.TradingStatusDto;
import com.example.InvestmentDataLoaderService.dto.TradingDayDto;
import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc.InstrumentsServiceBlockingStub;
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc.MarketDataServiceBlockingStub;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class TradingService {

    private final InstrumentsServiceBlockingStub instrumentsService;
    private final MarketDataServiceBlockingStub marketDataService;

    public TradingService(InstrumentsServiceBlockingStub instrumentsService,
                         MarketDataServiceBlockingStub marketDataService) {
        this.instrumentsService = instrumentsService;
        this.marketDataService = marketDataService;
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
}
