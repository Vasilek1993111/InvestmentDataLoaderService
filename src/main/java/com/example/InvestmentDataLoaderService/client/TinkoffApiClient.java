package com.example.InvestmentDataLoaderService.client;

import com.example.InvestmentDataLoaderService.dto.CandleDto;
import com.example.InvestmentDataLoaderService.entity.DividendEntity;
import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc.MarketDataServiceBlockingStub;
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc.InstrumentsServiceBlockingStub;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final InstrumentsServiceBlockingStub instrumentsService;

    public TinkoffApiClient(MarketDataServiceBlockingStub marketDataService, 
                           InstrumentsServiceBlockingStub instrumentsService) {
        this.marketDataService = marketDataService;
        this.instrumentsService = instrumentsService;
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
    
    /**
     * Получение дивидендов из Tinkoff Invest API
     */
    public List<DividendEntity> getDividends(String figi, LocalDate from, LocalDate to) {
        if (figi == null || figi.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            // Создаем запрос для получения дивидендов
            GetDividendsRequest request = GetDividendsRequest.newBuilder()
                .setInstrumentId(figi)
                .setFrom(Timestamp.newBuilder()
                    .setSeconds(from.atStartOfDay(ZoneId.of("Europe/Moscow")).toEpochSecond())
                    .setNanos(0)
                    .build())
                .setTo(Timestamp.newBuilder()
                    .setSeconds(to.plusDays(1).atStartOfDay(ZoneId.of("Europe/Moscow")).toEpochSecond())
                    .setNanos(0)
                    .build())
                .build();
            
            // Выполняем запрос
            GetDividendsResponse response = instrumentsService.getDividends(request);
            
            List<DividendEntity> dividends = new ArrayList<>();
            
            for (Dividend dividend : response.getDividendsList()) {
                DividendEntity entity = new DividendEntity();
                entity.setFigi(figi);
                
                // Конвертируем даты из protobuf Timestamp в LocalDate
                if (dividend.hasDeclaredDate()) {
                    entity.setDeclaredDate(convertTimestampToLocalDate(dividend.getDeclaredDate()));
                }
                
                if (dividend.hasRecordDate()) {
                    entity.setRecordDate(convertTimestampToLocalDate(dividend.getRecordDate()));
                }
                
                if (dividend.hasPaymentDate()) {
                    entity.setPaymentDate(convertTimestampToLocalDate(dividend.getPaymentDate()));
                }
                
                // Конвертируем значение дивиденда из MoneyValue в BigDecimal
                if (dividend.hasDividendNet()) {
                    entity.setDividendValue(convertMoneyValueToBigDecimal(dividend.getDividendNet()));
                }
                
                // Устанавливаем валюту
                entity.setCurrency(dividend.getDividendNet().getCurrency());
                
                // Устанавливаем тип дивиденда
                entity.setDividendType(dividend.getDividendType().toString());
                
                dividends.add(entity);
            }
            
            return dividends;
            
        } catch (Exception e) {
            System.err.println("Ошибка получения дивидендов для " + figi + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Конвертация protobuf Timestamp в LocalDate
     */
    private LocalDate convertTimestampToLocalDate(Timestamp timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
        return instant.atZone(ZoneId.of("Europe/Moscow")).toLocalDate();
    }
    
    /**
     * Конвертация protobuf MoneyValue в BigDecimal
     */
    private BigDecimal convertMoneyValueToBigDecimal(MoneyValue moneyValue) {
        return BigDecimal.valueOf(moneyValue.getUnits())
            .add(BigDecimal.valueOf(moneyValue.getNano(), 9));
    }
    
    /**
     * Получение полной информации о фьючерсе по FIGI
     * Включает дату экспирации
     * 
     * ⚠️ ВНИМАНИЕ: Этот метод делает запрос ко всем фьючерсам, используйте с осторожностью!
     */
    public Future getFutureBy(String figi) {
        try {
            // Задержка для соблюдения лимитов API
            Thread.sleep(500);
            
            // Получаем все фьючерсы и ищем нужный по FIGI
            FuturesResponse response = instrumentsService.futures(
                InstrumentsRequest.newBuilder()
                    .setInstrumentStatus(InstrumentStatus.INSTRUMENT_STATUS_BASE)
                    .build()
            );
            
            // Ищем фьючерс с нужным FIGI
            for (Future future : response.getInstrumentsList()) {
                if (future.getFigi().equals(figi)) {
                    return future;
                }
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.println("Ошибка получения фьючерса " + figi + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Конвертация protobuf Timestamp в LocalDateTime
     */
    public LocalDateTime convertTimestampToLocalDateTime(Timestamp timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
        return instant.atZone(ZoneId.of("Europe/Moscow")).toLocalDateTime();
    }
}
