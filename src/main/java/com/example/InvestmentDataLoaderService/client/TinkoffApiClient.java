package com.example.InvestmentDataLoaderService.client;

import com.example.InvestmentDataLoaderService.dto.CandleDto;
import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc.MarketDataServiceBlockingStub;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Низкоуровневый клиент для работы с Tinkoff Invest API
 * Предоставляет базовые методы для получения данных из API
 */
@Component
public class TinkoffApiClient {

    private final MarketDataServiceBlockingStub marketDataService;

    public TinkoffApiClient(MarketDataServiceBlockingStub marketDataService) {
        this.marketDataService = marketDataService;
    }

    /**
     * Получение свечей из Tinkoff Invest API
     */
    public List<CandleDto> getCandles(String instrumentId, LocalDate date, String interval) {
        // Проверяем на пустой или null FIGI
        if (instrumentId == null || instrumentId.trim().isEmpty()) {
            System.err.println("ERROR: Empty or null FIGI provided to getCandles");
            return new ArrayList<>();
        }
        
        System.out.println("=== T-Invest API GetCandles DEBUG ===");
        System.out.println("Instrument ID: " + instrumentId);
        System.out.println("Date: " + date);
        System.out.println("Interval: " + interval);
        
        // Определяем интервал свечей
        CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN; // По умолчанию 1 минута
        if (interval != null && !interval.isEmpty()) {
            try {
                candleInterval = CandleInterval.valueOf(interval.toUpperCase());
            } catch (IllegalArgumentException e) {
                candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
            }
        }
        System.out.println("Selected interval: " + candleInterval);

        // Создаем временной диапазон для запроса (весь день)
        Instant startTime = date.atStartOfDay(ZoneId.of("Europe/Moscow")).toInstant();
        Instant endTime = date.plusDays(1).atStartOfDay(ZoneId.of("Europe/Moscow")).toInstant();
        
        System.out.println("Time range UTC: " + startTime + " to " + endTime);
        System.out.println("Time range Moscow: " + startTime.atZone(ZoneId.of("Europe/Moscow")) + " to " + endTime.atZone(ZoneId.of("Europe/Moscow")));

        // Создаем запрос
        GetCandlesRequest request = GetCandlesRequest.newBuilder()
                .setInstrumentId(instrumentId)
                .setFrom(Timestamp.newBuilder().setSeconds(startTime.getEpochSecond()).setNanos(startTime.getNano()).build())
                .setTo(Timestamp.newBuilder().setSeconds(endTime.getEpochSecond()).setNanos(endTime.getNano()).build())
                .setInterval(candleInterval)
                .build();

        System.out.println("Request created successfully");
        System.out.println("Request details:");
        System.out.println("  - InstrumentId: " + request.getInstrumentId());
        System.out.println("  - From: " + request.getFrom());
        System.out.println("  - To: " + request.getTo());
        System.out.println("  - Interval: " + request.getInterval());
        System.out.println("  - Request toString: " + request.toString());

        // Выполняем запрос с задержкой для соблюдения лимитов API
        try {
            Thread.sleep(200); // Увеличена задержка до 200мс для снижения нагрузки
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Повторные попытки при таймаутах
        int maxRetries = 3;
        int baseRetryDelay = 2000; // Базовая задержка 2 секунды
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("Calling T-Invest API GetCandles... (попытка " + attempt + "/" + maxRetries + ")");
                long apiStartTime = System.currentTimeMillis();
                GetCandlesResponse response = marketDataService.getCandles(request);
                long apiEndTime = System.currentTimeMillis();
                System.out.println("API Response received successfully in " + (apiEndTime - apiStartTime) + "ms");
                System.out.println("Response details:");
                System.out.println("  - Candles count: " + response.getCandlesList().size());
                System.out.println("  - Response toString: " + response.toString());
                
                List<CandleDto> candles = new ArrayList<>();
                for (int i = 0; i < response.getCandlesList().size(); i++) {
                    var candle = response.getCandlesList().get(i);
                    Instant candleTime = Instant.ofEpochSecond(candle.getTime().getSeconds());
                    
                    // Конвертируем цены из Quotation в BigDecimal
                    BigDecimal open = BigDecimal.valueOf(candle.getOpen().getUnits())
                            .add(BigDecimal.valueOf(candle.getOpen().getNano()).movePointLeft(9));
                    BigDecimal close = BigDecimal.valueOf(candle.getClose().getUnits())
                            .add(BigDecimal.valueOf(candle.getClose().getNano()).movePointLeft(9));
                    BigDecimal high = BigDecimal.valueOf(candle.getHigh().getUnits())
                            .add(BigDecimal.valueOf(candle.getHigh().getNano()).movePointLeft(9));
                    BigDecimal low = BigDecimal.valueOf(candle.getLow().getUnits())
                            .add(BigDecimal.valueOf(candle.getLow().getNano()).movePointLeft(9));
                    
                    System.out.println("Candle #" + (i + 1) + ":");
                    System.out.println("  - Time: " + candleTime);
                    System.out.println("  - Time Moscow: " + candleTime.atZone(ZoneId.of("Europe/Moscow")));
                    System.out.println("  - Open: " + open);
                    System.out.println("  - Close: " + close);
                    System.out.println("  - High: " + high);
                    System.out.println("  - Low: " + low);
                    System.out.println("  - Volume: " + candle.getVolume());
                    System.out.println("  - IsComplete: " + candle.getIsComplete());
                    System.out.println("  - Candle toString: " + candle.toString());
                    
                    candles.add(new CandleDto(
                        instrumentId,
                        candle.getVolume(),
                        high,
                        low,
                        candleTime,
                        close,
                        open,
                        candle.getIsComplete()
                    ));
                }
                
                System.out.println("=== End T-Invest API GetCandles DEBUG ===");
                return candles;
            
            } catch (Exception e) {
                System.err.println("ERROR calling T-Invest API GetCandles (попытка " + attempt + "/" + maxRetries + "):");
                System.err.println("Exception type: " + e.getClass().getSimpleName());
                System.err.println("Exception message: " + e.getMessage());
                
                // Проверяем на таймаут
                if (e.getMessage() != null && e.getMessage().contains("DEADLINE_EXCEEDED")) {
                    System.err.println("API request timed out for instrument: " + instrumentId);
                } else if (e.getMessage() != null && e.getMessage().contains("UNAVAILABLE")) {
                    System.err.println("API service unavailable for instrument: " + instrumentId);
                } else {
                    System.err.println("Stack trace:");
                    e.printStackTrace();
                }
                
                // Если это не последняя попытка, ждем и пробуем снова
                if (attempt < maxRetries) {
                    // Экспоненциальная задержка: 2с, 4с, 8с
                    int retryDelay = baseRetryDelay * (int) Math.pow(2, attempt - 1);
                    System.err.println("Повторная попытка через " + retryDelay + "мс...");
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    System.err.println("Все попытки исчерпаны для инструмента: " + instrumentId);
                }
            }
        }
        
        System.out.println("=== End T-Invest API GetCandles DEBUG (ERROR) ===");
        return new ArrayList<>();
    }
}
