package com.example.InvestmentDataLoaderService.client;

import com.example.InvestmentDataLoaderService.dto.CandleDto;
import com.example.InvestmentDataLoaderService.entity.DividendEntity;
import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc.MarketDataServiceBlockingStub;
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc.InstrumentsServiceBlockingStub;
import com.example.InvestmentDataLoaderService.dto.AssetFundamentalDto;
import java.time.format.DateTimeFormatter;

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
 * Получение дивидендов из Tinkoff Invest API с простой задержкой
 */
public List<DividendEntity> getDividends(String figi, LocalDate from, LocalDate to) {
    if (figi == null || figi.trim().isEmpty()) {
        return new ArrayList<>();
    }
    
    // Простая задержка перед запросом
    try {
        Thread.sleep(200); // 200ms задержка для соблюдения лимитов
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return new ArrayList<>();
    }
    
    // Повторные попытки при ошибках
    int maxRetries = 2;
    int baseRetryDelay = 4000;
    
    for (int attempt = 1; attempt <= maxRetries; attempt++) {
        try {
            // Задержка для соблюдения лимитов API
            Thread.sleep(200);
            
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
            System.err.println("Ошибка получения дивидендов для " + figi + " (попытка " + attempt + "/" + maxRetries + "): " + e.getMessage());
            
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
    
    System.err.println("Не удалось получить дивиденды для " + figi + " после " + maxRetries + " попыток");
    return new ArrayList<>();
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

    /**
 * Получение фундаментальных показателей активов через gRPC API
 * Согласно документации: https://developer.tbank.ru/invest/services/instruments/methods#getassetfundamentalsrequest
 */
public List<AssetFundamentalDto> getAssetFundamentals(List<String> assetUids) {
    try {
    
       
        
        // Создаем gRPC запрос
        GetAssetFundamentalsRequest.Builder requestBuilder = GetAssetFundamentalsRequest.newBuilder();
        for (String assetUid : assetUids) {
            requestBuilder.addAssets(assetUid);
        }
        GetAssetFundamentalsRequest request = requestBuilder.build();
        
        // Выполняем gRPC запрос
        GetAssetFundamentalsResponse response = instrumentsService.getAssetFundamentals(request);
        
        // Конвертируем ответ в DTO
        return convertToAssetFundamentalDtos(response);
        
    } catch (Exception e) {
        System.err.println("Ошибка получения фундаментальных показателей: " + e.getMessage());
        return new ArrayList<>();
    }
}


/**
 * Конвертация gRPC ответа в DTO объекты
 */
private List<AssetFundamentalDto> convertToAssetFundamentalDtos(GetAssetFundamentalsResponse response) {
    List<AssetFundamentalDto> fundamentals = new ArrayList<>();
    
    for (GetAssetFundamentalsResponse.StatisticResponse statistic : response.getFundamentalsList()) {
        AssetFundamentalDto dto = new AssetFundamentalDto();
        
        // Основные поля
        dto.setAssetUid(statistic.getAssetUid());
        dto.setCurrency(statistic.getCurrency());
        dto.setDomicileIndicatorCode(statistic.getDomicileIndicatorCode());
        
        // Рыночные данные
        dto.setMarketCapitalization(convertDoubleToBigDecimal(statistic.getMarketCapitalization()));
        dto.setHighPriceLast52Weeks(convertDoubleToBigDecimal(statistic.getHighPriceLast52Weeks()));
        dto.setLowPriceLast52Weeks(convertDoubleToBigDecimal(statistic.getLowPriceLast52Weeks()));
        dto.setAverageDailyVolumeLast10Days(convertDoubleToBigDecimal(statistic.getAverageDailyVolumeLast10Days()));
        dto.setAverageDailyVolumeLast4Weeks(convertDoubleToBigDecimal(statistic.getAverageDailyVolumeLast4Weeks()));
        dto.setBeta(convertDoubleToBigDecimal(statistic.getBeta()));
        dto.setFreeFloat(convertDoubleToBigDecimal(statistic.getFreeFloat()));
        dto.setForwardAnnualDividendYield(convertDoubleToBigDecimal(statistic.getForwardAnnualDividendYield()));
        dto.setSharesOutstanding(convertDoubleToBigDecimal(statistic.getSharesOutstanding()));
        
        // Финансовые показатели
        dto.setRevenueTtm(convertDoubleToBigDecimal(statistic.getRevenueTtm()));
        dto.setEbitdaTtm(convertDoubleToBigDecimal(statistic.getEbitdaTtm()));
        dto.setNetIncomeTtm(convertDoubleToBigDecimal(statistic.getNetIncomeTtm()));
        dto.setEpsTtm(convertDoubleToBigDecimal(statistic.getEpsTtm()));
        dto.setDilutedEpsTtm(convertDoubleToBigDecimal(statistic.getDilutedEpsTtm()));
        dto.setFreeCashFlowTtm(convertDoubleToBigDecimal(statistic.getFreeCashFlowTtm()));
        
        // Ростовые показатели
        dto.setFiveYearAnnualRevenueGrowthRate(convertDoubleToBigDecimal(statistic.getFiveYearAnnualRevenueGrowthRate()));
        dto.setThreeYearAnnualRevenueGrowthRate(convertDoubleToBigDecimal(statistic.getThreeYearAnnualRevenueGrowthRate()));
        dto.setOneYearAnnualRevenueGrowthRate(convertDoubleToBigDecimal(statistic.getOneYearAnnualRevenueGrowthRate()));
        
        // Финансовые коэффициенты
        dto.setPeRatioTtm(convertDoubleToBigDecimal(statistic.getPeRatioTtm()));
        dto.setPriceToSalesTtm(convertDoubleToBigDecimal(statistic.getPriceToSalesTtm()));
        dto.setPriceToBookTtm(convertDoubleToBigDecimal(statistic.getPriceToBookTtm()));
        dto.setPriceToFreeCashFlowTtm(convertDoubleToBigDecimal(statistic.getPriceToFreeCashFlowTtm()));
        dto.setTotalEnterpriseValueMrq(convertDoubleToBigDecimal(statistic.getTotalEnterpriseValueMrq()));
        dto.setEvToEbitdaMrq(convertDoubleToBigDecimal(statistic.getEvToEbitdaMrq()));
        dto.setEvToSales(convertDoubleToBigDecimal(statistic.getEvToSales()));
        
        // Показатели рентабельности
        dto.setNetMarginMrq(convertDoubleToBigDecimal(statistic.getNetMarginMrq()));
        dto.setNetInterestMarginMrq(convertDoubleToBigDecimal(statistic.getNetInterestMarginMrq()));
        dto.setRoe(convertDoubleToBigDecimal(statistic.getRoe()));
        dto.setRoa(convertDoubleToBigDecimal(statistic.getRoa()));
        dto.setRoic(convertDoubleToBigDecimal(statistic.getRoic()));
        
        // Долговые показатели
        dto.setTotalDebtMrq(convertDoubleToBigDecimal(statistic.getTotalDebtMrq()));
        dto.setTotalDebtToEquityMrq(convertDoubleToBigDecimal(statistic.getTotalDebtToEquityMrq()));
        dto.setTotalDebtToEbitdaMrq(convertDoubleToBigDecimal(statistic.getTotalDebtToEbitdaMrq()));
        dto.setFreeCashFlowToPrice(convertDoubleToBigDecimal(statistic.getFreeCashFlowToPrice()));
        dto.setNetDebtToEbitda(convertDoubleToBigDecimal(statistic.getNetDebtToEbitda()));
        dto.setCurrentRatioMrq(convertDoubleToBigDecimal(statistic.getCurrentRatioMrq()));
        dto.setFixedChargeCoverageRatioFy(convertDoubleToBigDecimal(statistic.getFixedChargeCoverageRatioFy()));
        
        // Дивидендные показатели
        dto.setDividendYieldDailyTtm(convertDoubleToBigDecimal(statistic.getDividendYieldDailyTtm()));
        dto.setDividendRateTtm(convertDoubleToBigDecimal(statistic.getDividendRateTtm()));
        dto.setDividendsPerShare(convertDoubleToBigDecimal(statistic.getDividendsPerShare()));
        dto.setFiveYearsAverageDividendYield(convertDoubleToBigDecimal(statistic.getFiveYearsAverageDividendYield()));
        dto.setFiveYearAnnualDividendGrowthRate(convertDoubleToBigDecimal(statistic.getFiveYearAnnualDividendGrowthRate()));
        dto.setDividendPayoutRatioFy(convertDoubleToBigDecimal(statistic.getDividendPayoutRatioFy()));
        
        // Другие показатели
        dto.setBuyBackTtm(convertDoubleToBigDecimal(statistic.getBuyBackTtm()));
        dto.setAdrToCommonShareRatio(convertDoubleToBigDecimal(statistic.getAdrToCommonShareRatio()));
        dto.setNumberOfEmployees(convertDoubleToBigDecimal(statistic.getNumberOfEmployees()));
        
        // Даты (конвертируем из Timestamp)
        if (statistic.hasExDividendDate()) {
            dto.setExDividendDate(convertTimestampToString(statistic.getExDividendDate()));
        }
        if (statistic.hasFiscalPeriodStartDate()) {
            dto.setFiscalPeriodStartDate(convertTimestampToString(statistic.getFiscalPeriodStartDate()));
        }
        if (statistic.hasFiscalPeriodEndDate()) {
            dto.setFiscalPeriodEndDate(convertTimestampToString(statistic.getFiscalPeriodEndDate()));
        }
        
        // Показатели изменений
        dto.setRevenueChangeFiveYears(convertDoubleToBigDecimal(statistic.getRevenueChangeFiveYears()));
        dto.setEpsChangeFiveYears(convertDoubleToBigDecimal(statistic.getEpsChangeFiveYears()));
        dto.setEbitdaChangeFiveYears(convertDoubleToBigDecimal(statistic.getEbitdaChangeFiveYears()));
        dto.setTotalDebtChangeFiveYears(convertDoubleToBigDecimal(statistic.getTotalDebtChangeFiveYears()));
        
        fundamentals.add(dto);
    }
    
    return fundamentals;
}

/**
 * Конвертация double в BigDecimal
 */
private BigDecimal convertDoubleToBigDecimal(double value) {
    return BigDecimal.valueOf(value);
}

/**
 * Конвертация protobuf Timestamp в String (для exDividendDate)
 */
private String convertTimestampToString(Timestamp timestamp) {
    if (timestamp == null) {
        return null;
    }
    Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    LocalDateTime dateTime = instant.atZone(ZoneId.of("Europe/Moscow")).toLocalDateTime();
    return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
}
}
