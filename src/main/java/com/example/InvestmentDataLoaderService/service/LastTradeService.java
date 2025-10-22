package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.LastTradeDto;
import com.google.protobuf.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(LastTradeService.class);
    private final MarketDataServiceBlockingStub marketDataService;

    public LastTradeService(MarketDataServiceBlockingStub marketDataService) {
        this.marketDataService = marketDataService;
    }

    public List<LastTradeDto> getLastTrades(String instrumentId, LocalDate date, String tradeSource) {
        return getLastTrades(instrumentId, date, tradeSource, null, null);
    }
    
    public List<LastTradeDto> getLastTrades(String instrumentId, LocalDate date, String tradeSource, Instant fromTime, Instant toTime) {
       log.info ("=== T-Invest API GetLastTrades DEBUG ===");
        log.info("Instrument ID: {}", instrumentId);
        log.info("Date: {}", date);
        log.info("Trade Source: {}", tradeSource);
        
        // Параметр tradeSource пока не используется в API

        // Создаем временной диапазон для запроса
        Instant startTime;
        Instant endTime;
        
        if (fromTime != null && toTime != null) {
            // Используем переданные временные метки
            startTime = fromTime;
            endTime = toTime;
            log.info("Using provided time range");
        } else {
            // Используем время торговой сессии (10:00 - 18:40 по московскому времени)
            startTime = date.atTime(10, 0).atZone(ZoneId.of("Europe/Moscow")).toInstant();
            endTime = date.atTime(18, 40).atZone(ZoneId.of("Europe/Moscow")).toInstant();
            log.info("Using default trading session time range");
        }
        
        log.info("Time range UTC: {} to {}", startTime, endTime);
        log.info("Time range Moscow: {} to {}", startTime.atZone(ZoneId.of("Europe/Moscow")), endTime.atZone(ZoneId.of("Europe/Moscow")));
        // Создаем запрос
        GetLastTradesRequest request = GetLastTradesRequest.newBuilder()
                .setInstrumentId(instrumentId)
                .setFrom(Timestamp.newBuilder().setSeconds(startTime.getEpochSecond()).setNanos(startTime.getNano()).build())
                .setTo(Timestamp.newBuilder().setSeconds(endTime.getEpochSecond()).setNanos(endTime.getNano()).build())
                .build();

        log.info("Request created successfully");   
        log.info("Request details:");
        log.info("  - InstrumentId: {}", request.getInstrumentId());   
        log.info("  - From: {}", request.getFrom());
        log.info("  - To: {}", request.getTo());
        
        // Выполняем запрос с задержкой для соблюдения лимитов API
        try {
            Thread.sleep(200); // Увеличена задержка до 200мс для снижения нагрузки
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            log.info("Calling T-Invest API...");
            GetLastTradesResponse response = marketDataService.getLastTrades(request);
            log.info("API Response received successfully");
            log.info("Response details:");
            log.info("  - Trades count: {}", response.getTradesList().size());
            log.info("  - Response toString: {}", response.toString());
            
            List<LastTradeDto> trades = new ArrayList<>();
            for (int i = 0; i < response.getTradesList().size(); i++) {
                var trade = response.getTradesList().get(i);
                Instant tradeTime = Instant.ofEpochSecond(trade.getTime().getSeconds());
                
                // Конвертируем цену из Quotation в BigDecimal
                BigDecimal price = BigDecimal.valueOf(trade.getPrice().getUnits())
                        .add(BigDecimal.valueOf(trade.getPrice().getNano()).movePointLeft(9));
                
                log.info("Trade #{}:", (i + 1));
                log.info("  - Direction: {}", trade.getDirection());
                log.info("  - Price: {}", price);
                log.info("  - Quantity: {}", trade.getQuantity());
                log.info("  - Time: {}", tradeTime);
                log.info("  - Time Moscow: {}", tradeTime.atZone(ZoneId.of("Europe/Moscow")));
                log.info("  - Trade toString: {}", trade.toString());
                
                trades.add(new LastTradeDto(
                    instrumentId,
                    trade.getDirection().name(),
                    price,
                    trade.getQuantity(),
                    tradeTime,
                    "TRADE_SOURCE_ALL"
                ));
            }
            
            log.info("=== End T-Invest API GetLastTrades DEBUG ===");
            return trades;
            
        } catch (Exception e) {
            log.error("ERROR calling T-Invest API GetLastTrades:");
            log.error("Exception type: {}", e.getClass().getSimpleName());
            log.error("Exception message: {}", e.getMessage());
            log.error("Stack trace:");
            e.printStackTrace();
            log.info("=== End T-Invest API GetLastTrades DEBUG (ERROR) ===");
            return new ArrayList<>();
        }
    }

    public List<LastTradeDto> getLastTradesForLastHour(String instrumentId, String tradeSource) {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);
        
        log.info("=== T-Invest API GetLastTrades (Last Hour) DEBUG ===");
        log.info("Instrument ID: {}", instrumentId);
        log.info("Trade Source: {}", tradeSource);
        log.info("Time range: Last hour from {} to {}", oneHourAgo, now);
        log.info("Time range Moscow: {} to {}", oneHourAgo.atZone(ZoneId.of("Europe/Moscow")), now.atZone(ZoneId.of("Europe/Moscow")));

        // Создаем запрос
        GetLastTradesRequest request = GetLastTradesRequest.newBuilder()
                .setInstrumentId(instrumentId)
                .setFrom(Timestamp.newBuilder().setSeconds(oneHourAgo.getEpochSecond()).setNanos(oneHourAgo.getNano()).build())
                .setTo(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build())
                .build();

        log.info("Request created successfully");
        log.info("Request details:");
        log.info("  - InstrumentId: {}", request.getInstrumentId());
        log.info("  - From: {}", request.getFrom());
        log.info("  - To: {}", request.getTo());
        log.info("  - Request toString: {}", request.toString());

        // Выполняем запрос с задержкой для соблюдения лимитов API
        try {
            Thread.sleep(200); // Увеличена задержка до 200мс для снижения нагрузки
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            log.info("Calling T-Invest API...");
            GetLastTradesResponse response = marketDataService.getLastTrades(request);
            log.info("API Response received successfully");
            log.info("Response details:");
            log.info("  - Trades count: {}", response.getTradesList().size());
            
            List<LastTradeDto> trades = new ArrayList<>();
            for (int i = 0; i < response.getTradesList().size(); i++) {
                var trade = response.getTradesList().get(i);
                Instant tradeTime = Instant.ofEpochSecond(trade.getTime().getSeconds());
                
                // Конвертируем цену из Quotation в BigDecimal
                BigDecimal price = BigDecimal.valueOf(trade.getPrice().getUnits())
                        .add(BigDecimal.valueOf(trade.getPrice().getNano()).movePointLeft(9));
                
                log.info("Trade #{}:", (i + 1));      
                log.info("  - Direction: {}", trade.getDirection());
                log.info("  - Price: {}", price);
                log.info("  - Quantity: {}", trade.getQuantity());
                log.info("  - Time: {}", tradeTime);
                log.info("  - Time Moscow: {}", tradeTime.atZone(ZoneId.of("Europe/Moscow")));
                log.info("  - Trade toString: {}", trade.toString());
                
                trades.add(new LastTradeDto(
                    instrumentId,
                    trade.getDirection().name(),
                    price,
                    trade.getQuantity(),
                    tradeTime,
                    "TRADE_SOURCE_ALL"
                ));
            }
            
            log.info("=== End T-Invest API GetLastTrades (Last Hour) DEBUG ===");
            return trades;

        } catch (Exception e) {
            log.error("ERROR calling T-Invest API GetLastTrades:");
            log.error("Exception type: {}", e.getClass().getSimpleName());
            log.error("Exception message: {}", e.getMessage());
            log.error("Stack trace: {}", e.getMessage());
            log.info("=== End T-Invest API GetLastTrades (Last Hour) DEBUG (ERROR) ===");
            return new ArrayList<>();
        }
    }
}
