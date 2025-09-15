package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.CandleEntity;
import com.example.InvestmentDataLoaderService.entity.CandleKey;
import com.example.InvestmentDataLoaderService.entity.ClosePriceEntity;
import com.example.InvestmentDataLoaderService.entity.ClosePriceKey;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.repository.CandleRepository;
import com.example.InvestmentDataLoaderService.repository.ClosePriceRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc.MarketDataServiceBlockingStub;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class MarketDataService {

    private final MarketDataServiceBlockingStub marketDataService;
    private final ShareRepository shareRepo;
    private final FutureRepository futureRepo;
    private final ClosePriceRepository closePriceRepo;
    private final CandleRepository candleRepo;

    public MarketDataService(MarketDataServiceBlockingStub marketDataService,
                           ShareRepository shareRepo,
                           FutureRepository futureRepo,
                           ClosePriceRepository closePriceRepo,
                           CandleRepository candleRepo) {
        this.marketDataService = marketDataService;
        this.shareRepo = shareRepo;
        this.futureRepo = futureRepo;
        this.closePriceRepo = closePriceRepo;
        this.candleRepo = candleRepo;
    }

    // === МЕТОДЫ ДЛЯ РАБОТЫ С ЦЕНАМИ ЗАКРЫТИЯ ===

    public List<ClosePriceDto> getClosePrices(List<String> instrumentIds, String status) {
        GetClosePricesRequest.Builder builder = GetClosePricesRequest.newBuilder();
        
        // Если instrumentIds не переданы, получаем все инструменты из БД
        if (instrumentIds == null || instrumentIds.isEmpty()) {
            List<String> allInstrumentIds = new ArrayList<>();
            
            // Получаем все FIGI из таблицы shares
            List<ShareEntity> shares = shareRepo.findAll();
            for (ShareEntity share : shares) {
                allInstrumentIds.add(share.getFigi());
            }
            
            // Получаем все FIGI из таблицы futures
            List<FutureEntity> futures = futureRepo.findAll();
            for (FutureEntity future : futures) {
                allInstrumentIds.add(future.getFigi());
            }
            
            instrumentIds = allInstrumentIds;
        }
        
        // Если после получения из БД список все еще пуст, возвращаем пустой результат
        if (instrumentIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Добавляем каждый инструмент в запрос
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
            
            // Обрабатываем eveningSessionPrice если оно есть
            BigDecimal eveningSessionPrice = null;
            if (p.hasEveningSessionPrice()) {
                Quotation esp = p.getEveningSessionPrice();
                eveningSessionPrice = BigDecimal.valueOf(esp.getUnits())
                        .add(BigDecimal.valueOf(esp.getNano()).movePointLeft(9));
            }
            
            list.add(new ClosePriceDto(p.getFigi(), date, price, eveningSessionPrice));
        }
        return list;
    }

    public SaveResponseDto saveClosePrices(ClosePriceRequestDto request) {
        List<String> instrumentIds = request.getInstruments();
        
        // Если инструменты не указаны, получаем только RUB инструменты из БД
        if (instrumentIds == null || instrumentIds.isEmpty()) {
            List<String> allInstrumentIds = new ArrayList<>();
            
            // Получаем только акции в рублях из таблицы shares
            List<ShareEntity> shares = shareRepo.findAll();
            for (ShareEntity share : shares) {
                if ("RUB".equalsIgnoreCase(share.getCurrency())) {
                    allInstrumentIds.add(share.getFigi());
                }
            }
            
            // Получаем только фьючерсы в рублях из таблицы futures
            List<FutureEntity> futures = futureRepo.findAll();
            for (FutureEntity future : futures) {
                if ("RUB".equalsIgnoreCase(future.getCurrency())) {
                    allInstrumentIds.add(future.getFigi());
                }
            }
            
            instrumentIds = allInstrumentIds;
        }
        
        // Если после получения из БД список все еще пуст, возвращаем пустой результат
        if (instrumentIds.isEmpty()) {
            return new SaveResponseDto(
                false,
                "Нет инструментов в рублях для загрузки цен закрытия. В базе данных нет акций или фьючерсов с валютой RUB.",
                0, 0, 0, new ArrayList<>()
            );
        }

        // Получаем цены закрытия из API
        List<ClosePriceDto> closePricesFromApi = getClosePrices(instrumentIds, null);
        
        List<ClosePriceDto> savedPrices = new ArrayList<>();
        int existingCount = 0;
        
        for (ClosePriceDto closePriceDto : closePricesFromApi) {
            LocalDate priceDate = LocalDate.parse(closePriceDto.tradingDate());
            ClosePriceKey key = new ClosePriceKey(priceDate, closePriceDto.figi());
            
            // Проверяем, существует ли запись с такой датой и FIGI
            if (!closePriceRepo.existsById(key)) {
                // Определяем тип инструмента и получаем дополнительную информацию
                String instrumentType = "UNKNOWN";
                String currency = "UNKNOWN";
                String exchange = "UNKNOWN";
                
                // Проверяем в таблице shares
                ShareEntity share = shareRepo.findById(closePriceDto.figi()).orElse(null);
                if (share != null) {
                    instrumentType = "SHARE";
                    currency = share.getCurrency();
                    exchange = share.getExchange();
                } else {
                    // Проверяем в таблице futures
                    FutureEntity future = futureRepo.findById(closePriceDto.figi()).orElse(null);
                    if (future != null) {
                        instrumentType = "FUTURE";
                        currency = future.getCurrency();
                        exchange = future.getExchange();
                    }
                }
                
                // Создаем и сохраняем новую запись
                ClosePriceEntity closePriceEntity = new ClosePriceEntity(
                    priceDate,
                    closePriceDto.figi(),
                    instrumentType,
                    closePriceDto.closePrice(),
                    currency,
                    exchange
                );
                
                try {
                    closePriceRepo.save(closePriceEntity);
                    savedPrices.add(closePriceDto);
                } catch (Exception e) {
                    // Логируем ошибку, но продолжаем обработку других цен
                    System.err.println("Error saving close price for " + closePriceDto.figi() + 
                                     " on " + priceDate + ": " + e.getMessage());
                }
            } else {
                existingCount++;
            }
        }
        
        // Формируем ответ
        boolean success = !closePricesFromApi.isEmpty();
        String message;
        
        if (savedPrices.isEmpty()) {
            if (closePricesFromApi.isEmpty()) {
                message = "Новых цен закрытия не обнаружено. По заданным инструментам цены не найдены.";
            } else {
                message = "Новых цен закрытия не обнаружено. Все найденные цены уже существуют в базе данных.";
            }
        } else {
            message = String.format("Успешно загружено %d новых цен закрытия из %d найденных.", 
                                  savedPrices.size(), closePricesFromApi.size());
        }
        
        return new SaveResponseDto(
            success,
            message,
            closePricesFromApi.size(),
            savedPrices.size(),
            existingCount,
            savedPrices
        );
    }

    // === МЕТОДЫ ДЛЯ РАБОТЫ СО СВЕЧАМИ ===

    public List<CandleDto> getCandles(String instrumentId, LocalDate date, String interval) {
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
        Instant startTime = date.atStartOfDay(ZoneId.of("UTC")).toInstant();
        Instant endTime = date.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant();
        
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
            Thread.sleep(100); // Задержка 100мс между запросами
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            System.out.println("Calling T-Invest API GetCandles...");
            GetCandlesResponse response = marketDataService.getCandles(request);
            System.out.println("API Response received successfully");
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
            System.err.println("ERROR calling T-Invest API GetCandles:");
            System.err.println("Exception type: " + e.getClass().getSimpleName());
            System.err.println("Exception message: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.out.println("=== End T-Invest API GetCandles DEBUG (ERROR) ===");
            return new ArrayList<>();
        }
    }

    public CompletableFuture<SaveResponseDto> saveCandles(CandleRequestDto request) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> instrumentIds = request.getInstruments();
            LocalDate date = request.getDate();
            String interval = request.getInterval();
            List<String> assetTypes = request.getAssetType();
            
            // Если дата не указана, используем вчерашний день
            if (date == null) {
                date = LocalDate.now(ZoneId.of("Europe/Moscow")).minusDays(1);
            }
            
            // Если интервал не указан, используем 1 минуту
            if (interval == null || interval.isEmpty()) {
                interval = "CANDLE_INTERVAL_1_MIN";
            }
            
            // Если инструменты не указаны, получаем все инструменты из БД
            if (instrumentIds == null || instrumentIds.isEmpty()) {
                instrumentIds = new ArrayList<>();
                
                // Если указаны типы активов, фильтруем по ним
                if (assetTypes != null && !assetTypes.isEmpty()) {
                    for (String assetType : assetTypes) {
                        if ("SHARES".equalsIgnoreCase(assetType)) {
                            // Получаем все FIGI из таблицы shares
                            List<ShareEntity> shares = shareRepo.findAll();
                            for (ShareEntity share : shares) {
                                instrumentIds.add(share.getFigi());
                            }
                        } else if ("FUTURES".equalsIgnoreCase(assetType)) {
                            // Получаем все FIGI из таблицы futures
                            List<FutureEntity> futures = futureRepo.findAll();
                            for (FutureEntity future : futures) {
                                instrumentIds.add(future.getFigi());
                            }
                        }
                    }
                } else {
                    // Если типы активов не указаны, получаем все инструменты
                    // Получаем все FIGI из таблицы shares
                    List<ShareEntity> shares = shareRepo.findAll();
                    for (ShareEntity share : shares) {
                        instrumentIds.add(share.getFigi());
                    }
                    
                    // Получаем все FIGI из таблицы futures
                    List<FutureEntity> futures = futureRepo.findAll();
                    for (FutureEntity future : futures) {
                        instrumentIds.add(future.getFigi());
                    }
                }
            }
            
            if (instrumentIds.isEmpty()) {
                return new SaveResponseDto(
                    false,
                    "Нет инструментов для загрузки свечей. В базе данных нет акций или фьючерсов.",
                    0, 0, 0, new ArrayList<>()
                );
            }
            
            List<CandleDto> allCandles = new ArrayList<>();
            int totalRequested = 0;
            int savedCount = 0;
            int existingCount = 0;
            
            for (String instrumentId : instrumentIds) {
                try {
                    // Получаем свечи для инструмента
                    List<CandleDto> candles = getCandles(instrumentId, date, interval);
                    totalRequested += candles.size();
                    
                    // Сохраняем свечи в БД
                    for (CandleDto candleDto : candles) {
                        CandleKey key = new CandleKey(candleDto.figi(), candleDto.time());
                        
                        if (!candleRepo.existsById(key)) {
                            CandleEntity candleEntity = new CandleEntity(
                                candleDto.figi(),
                                candleDto.volume(),
                                candleDto.high(),
                                candleDto.low(),
                                candleDto.time(),
                                candleDto.close(),
                                candleDto.open(),
                                candleDto.isComplete()
                            );
                            
                            try {
                                candleRepo.save(candleEntity);
                                allCandles.add(candleDto);
                                savedCount++;
                            } catch (Exception e) {
                                System.err.println("Error saving candle for " + candleDto.figi() + 
                                                 " at " + candleDto.time() + ": " + e.getMessage());
                            }
                        } else {
                            existingCount++;
                        }
                    }
                    
                    // Задержка между запросами для разных инструментов
                    Thread.sleep(200);
                    
                } catch (Exception e) {
                    System.err.println("Error processing instrument " + instrumentId + ": " + e.getMessage());
                }
            }
            
            // Формируем ответ
            boolean success = totalRequested > 0;
            String message;
            
            if (savedCount == 0) {
                if (totalRequested == 0) {
                    message = "Свечи не найдены. По заданным параметрам данные не найдены.";
                } else {
                    message = "Новых свечей не обнаружено. Все найденные свечи уже существуют в базе данных.";
                }
            } else {
                message = String.format("Успешно загружено %d новых свечей из %d найденных.", 
                                      savedCount, totalRequested);
            }
            
            return new SaveResponseDto(
                success,
                message,
                totalRequested,
                savedCount,
                existingCount,
                allCandles
            );
        });
    }

    public void saveCandlesAsync(CandleRequestDto request, String taskId) {
        CompletableFuture.runAsync(() -> {
            try {
                System.out.println("[" + taskId + "] Начало загрузки свечей в " + LocalDateTime.now());
                
                List<String> instrumentIds = request.getInstruments();
                LocalDate date = request.getDate();
                String interval = request.getInterval();
                List<String> assetTypes = request.getAssetType();
                
                // Если дата не указана, используем вчерашний день
                if (date == null) {
                    date = LocalDate.now(ZoneId.of("Europe/Moscow")).minusDays(1);
                }
                
                // Если интервал не указан, используем 1 минуту
                if (interval == null || interval.isEmpty()) {
                    interval = "CANDLE_INTERVAL_1_MIN";
                }
                
                // Если инструменты не указаны, получаем все инструменты из БД
                if (instrumentIds == null || instrumentIds.isEmpty()) {
                    instrumentIds = new ArrayList<>();
                    
                    // Если указаны типы активов, фильтруем по ним
                    if (assetTypes != null && !assetTypes.isEmpty()) {
                        for (String assetType : assetTypes) {
                            if ("SHARES".equalsIgnoreCase(assetType)) {
                                // Получаем все FIGI из таблицы shares
                                List<ShareEntity> shares = shareRepo.findAll();
                                for (ShareEntity share : shares) {
                                    instrumentIds.add(share.getFigi());
                                }
                                System.out.println("[" + taskId + "] Добавлено " + shares.size() + " акций");
                            } else if ("FUTURES".equalsIgnoreCase(assetType)) {
                                // Получаем все FIGI из таблицы futures
                                List<FutureEntity> futures = futureRepo.findAll();
                                for (FutureEntity future : futures) {
                                    instrumentIds.add(future.getFigi());
                                }
                                System.out.println("[" + taskId + "] Добавлено " + futures.size() + " фьючерсов");
                            }
                        }
                    } else {
                        // Если типы активов не указаны, получаем все инструменты
                        // Получаем все FIGI из таблицы shares
                        List<ShareEntity> shares = shareRepo.findAll();
                        for (ShareEntity share : shares) {
                            instrumentIds.add(share.getFigi());
                        }
                        
                        // Получаем все FIGI из таблицы futures
                        List<FutureEntity> futures = futureRepo.findAll();
                        for (FutureEntity future : futures) {
                            instrumentIds.add(future.getFigi());
                        }
                        
                        System.out.println("[" + taskId + "] Добавлено " + shares.size() + " акций и " + futures.size() + " фьючерсов");
                    }
                }
                
                if (instrumentIds.isEmpty()) {
                    System.out.println("[" + taskId + "] Нет инструментов для загрузки свечей");
                    return;
                }
                
                System.out.println("[" + taskId + "] Найдено " + instrumentIds.size() + " инструментов для загрузки");
                
                int totalRequested = 0;
                int savedCount = 0;
                int existingCount = 0;
                int processedInstruments = 0;
                
                for (String instrumentId : instrumentIds) {
                    try {
                        processedInstruments++;
                        System.out.println("[" + taskId + "] Обработка инструмента " + processedInstruments + "/" + instrumentIds.size() + ": " + instrumentId);
                        
                        // Получаем свечи для инструмента
                        List<CandleDto> candles = getCandles(instrumentId, date, interval);
                        totalRequested += candles.size();
                        
                        // Сохраняем свечи в БД
                        for (CandleDto candleDto : candles) {
                            CandleKey key = new CandleKey(candleDto.figi(), candleDto.time());
                            
                            if (!candleRepo.existsById(key)) {
                                CandleEntity candleEntity = new CandleEntity(
                                    candleDto.figi(),
                                    candleDto.volume(),
                                    candleDto.high(),
                                    candleDto.low(),
                                    candleDto.time(),
                                    candleDto.close(),
                                    candleDto.open(),
                                    candleDto.isComplete()
                                );
                                
                                try {
                                    candleRepo.save(candleEntity);
                                    savedCount++;
                                } catch (Exception e) {
                                    System.err.println("[" + taskId + "] Ошибка сохранения свечи для " + candleDto.figi() + 
                                                     " в " + candleDto.time() + ": " + e.getMessage());
                                }
                            } else {
                                existingCount++;
                            }
                        }
                        
                        // Задержка между запросами для разных инструментов
                        Thread.sleep(200);
                        
                    } catch (Exception e) {
                        System.err.println("[" + taskId + "] Ошибка обработки инструмента " + instrumentId + ": " + e.getMessage());
                    }
                }
                
                System.out.println("[" + taskId + "] Загрузка завершена в " + LocalDateTime.now());
                System.out.println("[" + taskId + "] Итого: запрошено " + totalRequested + ", сохранено " + savedCount + ", пропущено " + existingCount);
                
            } catch (Exception e) {
                System.err.println("[" + taskId + "] Критическая ошибка при загрузке свечей: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // === МЕТОДЫ ДЛЯ РАБОТЫ С ОБЕЗЛИЧЕННЫМИ СДЕЛКАМИ ===

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
            Thread.sleep(100); // Задержка 100мс между запросами
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
            Thread.sleep(100); // Задержка 100мс между запросами
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
