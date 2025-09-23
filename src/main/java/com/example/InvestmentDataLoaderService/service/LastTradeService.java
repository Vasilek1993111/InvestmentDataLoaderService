package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.LastTradeDto;
import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc.MarketDataServiceBlockingStub;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class LastTradeService {

    private final MarketDataServiceBlockingStub marketDataService;

    public LastTradeService(MarketDataServiceBlockingStub marketDataService) {
        this.marketDataService = marketDataService;
    }

    public List<LastTradeDto> getLastTrades(String instrumentId, LocalDate date, String tradeSource) {
        return getLastTrades(instrumentId, date, tradeSource, null, null);
    }
    
    public List<LastTradeDto> getLastTrades(String instrumentId, LocalDate date, String tradeSource, Instant fromTime, Instant toTime) {
        System.out.println("=== T-Invest API GetLastTrades DEBUG ===");
        System.out.println("Instrument ID: " + instrumentId);
        System.out.println("Date: " + date);
        System.out.println("Trade Source: " + tradeSource);
        
        // Параметр tradeSource пока не используется в API

        // Создаем временной диапазон для запроса
        Instant startTime;
        Instant endTime;
        
        if (fromTime != null && toTime != null) {
            // Используем переданные временные метки
            startTime = fromTime;
            endTime = toTime;
            System.out.println("Using provided time range");
        } else {
            // Используем время торговой сессии (10:00 - 18:40 по московскому времени)
            startTime = date.atTime(10, 0).atZone(ZoneId.of("Europe/Moscow")).toInstant();
            endTime = date.atTime(18, 40).atZone(ZoneId.of("Europe/Moscow")).toInstant();
            System.out.println("Using default trading session time range");
        }
        
        System.out.println("Time range UTC: " + startTime + " to " + endTime);
        System.out.println("Time range Moscow: " + startTime.atZone(ZoneId.of("Europe/Moscow")) + " to " + endTime.atZone(ZoneId.of("Europe/Moscow")));

        // Создаем запрос
        GetLastTradesRequest request = GetLastTradesRequest.newBuilder()
                .setInstrumentId(instrumentId)
                .setFrom(Timestamp.newBuilder().setSeconds(startTime.getEpochSecond()).setNanos(startTime.getNano()).build())
                .setTo(Timestamp.newBuilder().setSeconds(endTime.getEpochSecond()).setNanos(endTime.getNano()).build())
                .build();

        System.out.println("Request created successfully");
        System.out.println("Request details:");
        System.out.println("  - InstrumentId: " + request.getInstrumentId());
        System.out.println("  - From: " + request.getFrom());
        System.out.println("  - To: " + request.getTo());

        // Выполняем запрос с задержкой для соблюдения лимитов API
        try {
            Thread.sleep(200); // Увеличена задержка до 200мс для снижения нагрузки
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            System.out.println("Calling T-Invest API...");
            GetLastTradesResponse response = marketDataService.getLastTrades(request);
            System.out.println("API Response received successfully");
            System.out.println("Response details:");
            System.out.println("  - Trades count: " + response.getTradesList().size());
            System.out.println("  - Response toString: " + response.toString());
            
            List<LastTradeDto> trades = new ArrayList<>();
            for (int i = 0; i < response.getTradesList().size(); i++) {
                var trade = response.getTradesList().get(i);
                Instant tradeTime = Instant.ofEpochSecond(trade.getTime().getSeconds());
                
                // Конвертируем цену из Quotation в BigDecimal
                BigDecimal price = BigDecimal.valueOf(trade.getPrice().getUnits())
                        .add(BigDecimal.valueOf(trade.getPrice().getNano()).movePointLeft(9));
                
                System.out.println("Trade #" + (i + 1) + ":");
                System.out.println("  - Direction: " + trade.getDirection());
                System.out.println("  - Price: " + price);
                System.out.println("  - Quantity: " + trade.getQuantity());
                System.out.println("  - Time: " + tradeTime);
                System.out.println("  - Time Moscow: " + tradeTime.atZone(ZoneId.of("Europe/Moscow")));
                System.out.println("  - Trade toString: " + trade.toString());
                
                trades.add(new LastTradeDto(
                    instrumentId,
                    trade.getDirection().name(),
                    price,
                    trade.getQuantity(),
                    tradeTime,
                    "TRADE_SOURCE_ALL"
                ));
            }
            
            System.out.println("=== End T-Invest API GetLastTrades DEBUG ===");
            return trades;
            
        } catch (Exception e) {
            System.err.println("ERROR calling T-Invest API GetLastTrades:");
            System.err.println("Exception type: " + e.getClass().getSimpleName());
            System.err.println("Exception message: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.out.println("=== End T-Invest API GetLastTrades DEBUG (ERROR) ===");
            return new ArrayList<>();
        }
    }

    public List<LastTradeDto> getLastTradesForLastHour(String instrumentId, String tradeSource) {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);
        
        System.out.println("=== T-Invest API GetLastTrades (Last Hour) DEBUG ===");
        System.out.println("Instrument ID: " + instrumentId);
        System.out.println("Trade Source: " + tradeSource);
        System.out.println("Time range: Last hour from " + oneHourAgo + " to " + now);
        System.out.println("Time range Moscow: " + oneHourAgo.atZone(ZoneId.of("Europe/Moscow")) + " to " + now.atZone(ZoneId.of("Europe/Moscow")));
        
        // Создаем запрос
        GetLastTradesRequest request = GetLastTradesRequest.newBuilder()
                .setInstrumentId(instrumentId)
                .setFrom(Timestamp.newBuilder().setSeconds(oneHourAgo.getEpochSecond()).setNanos(oneHourAgo.getNano()).build())
                .setTo(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build())
                .build();

        System.out.println("Request created successfully");
        System.out.println("Request details:");
        System.out.println("  - InstrumentId: " + request.getInstrumentId());
        System.out.println("  - From: " + request.getFrom());
        System.out.println("  - To: " + request.getTo());
        System.out.println("  - Request toString: " + request.toString());

        // Выполняем запрос с задержкой для соблюдения лимитов API
        try {
            Thread.sleep(200); // Увеличена задержка до 200мс для снижения нагрузки
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            System.out.println("Calling T-Invest API...");
            GetLastTradesResponse response = marketDataService.getLastTrades(request);
            System.out.println("API Response received successfully");
            System.out.println("Response details:");
            System.out.println("  - Trades count: " + response.getTradesList().size());
            
            List<LastTradeDto> trades = new ArrayList<>();
            for (int i = 0; i < response.getTradesList().size(); i++) {
                var trade = response.getTradesList().get(i);
                Instant tradeTime = Instant.ofEpochSecond(trade.getTime().getSeconds());
                
                // Конвертируем цену из Quotation в BigDecimal
                BigDecimal price = BigDecimal.valueOf(trade.getPrice().getUnits())
                        .add(BigDecimal.valueOf(trade.getPrice().getNano()).movePointLeft(9));
                
                System.out.println("Trade #" + (i + 1) + ":");
                System.out.println("  - Direction: " + trade.getDirection());
                System.out.println("  - Price: " + price);
                System.out.println("  - Quantity: " + trade.getQuantity());
                System.out.println("  - Time: " + tradeTime);
                System.out.println("  - Time Moscow: " + tradeTime.atZone(ZoneId.of("Europe/Moscow")));
                System.out.println("  - Trade toString: " + trade.toString());
                
                trades.add(new LastTradeDto(
                    instrumentId,
                    trade.getDirection().name(),
                    price,
                    trade.getQuantity(),
                    tradeTime,
                    "TRADE_SOURCE_ALL"
                ));
            }
            
            System.out.println("=== End T-Invest API GetLastTrades (Last Hour) DEBUG ===");
            return trades;
            
        } catch (Exception e) {
            System.err.println("ERROR calling T-Invest API GetLastTrades:");
            System.err.println("Exception type: " + e.getClass().getSimpleName());
            System.err.println("Exception message: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.out.println("=== End T-Invest API GetLastTrades (Last Hour) DEBUG (ERROR) ===");
            return new ArrayList<>();
        }
    }
}
