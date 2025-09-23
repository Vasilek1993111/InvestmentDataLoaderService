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
        if (instrumentId == null || instrumentId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // Определяем интервал свечей
        CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        if (interval != null && !interval.isEmpty()) {
            try {
                candleInterval = CandleInterval.valueOf(interval.toUpperCase());
            } catch (IllegalArgumentException e) {
                candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
            }
        }

        // Создаем временной диапазон для запроса (весь день)
        Instant startTime = date.atStartOfDay(ZoneId.of("Europe/Moscow")).toInstant();
        Instant endTime = date.plusDays(1).atStartOfDay(ZoneId.of("Europe/Moscow")).toInstant();

        // Создаем запрос
        GetCandlesRequest request = GetCandlesRequest.newBuilder()
                .setInstrumentId(instrumentId)
                .setFrom(Timestamp.newBuilder().setSeconds(startTime.getEpochSecond()).setNanos(startTime.getNano()).build())
                .setTo(Timestamp.newBuilder().setSeconds(endTime.getEpochSecond()).setNanos(endTime.getNano()).build())
                .setInterval(candleInterval)
                .build();

        // Задержка для соблюдения лимитов API
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Повторные попытки при ошибках
        int maxRetries = 3;
        int baseRetryDelay = 2000;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                GetCandlesResponse response = marketDataService.getCandles(request);
                
                List<CandleDto> candles = new ArrayList<>();
                for (var candle : response.getCandlesList()) {
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
                
                return candles;
            
            } catch (Exception e) {
                if (attempt < maxRetries) {
                    int retryDelay = baseRetryDelay * (int) Math.pow(2, attempt - 1);
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        return new ArrayList<>();
    }
}
