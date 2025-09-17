package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.ClosePriceEntity;
import com.example.InvestmentDataLoaderService.entity.ClosePriceKey;
import com.example.InvestmentDataLoaderService.entity.CandleEntity;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.entity.IndicativeEntity;
import com.example.InvestmentDataLoaderService.repository.ClosePriceRepository;
import com.example.InvestmentDataLoaderService.repository.CandleRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.IndicativeRepository;
import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.*;
import org.springframework.dao.DataIntegrityViolationException;
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc.MarketDataServiceBlockingStub;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class MarketDataService {

    private final MarketDataServiceBlockingStub marketDataService;
    private final ShareRepository shareRepo;
    private final FutureRepository futureRepo;
    private final ClosePriceRepository closePriceRepo;
    private final CandleRepository candleRepository;
    private final IndicativeRepository indicativeRepo;


    public MarketDataService(MarketDataServiceBlockingStub marketDataService,
                           ShareRepository shareRepo,
                           FutureRepository futureRepo,
                           ClosePriceRepository closePriceRepo,
                           CandleRepository candleRepository,
                           IndicativeRepository indicativeRepo) {
        this.marketDataService = marketDataService;
        this.shareRepo = shareRepo;
        this.futureRepo = futureRepo;
        this.closePriceRepo = closePriceRepo;
        this.candleRepository = candleRepository;
        this.indicativeRepo = indicativeRepo;
    }

    // === МЕТОДЫ ДЛЯ РАБОТЫ С ЦЕНАМИ ЗАКРЫТИЯ ===

    /**
     * Вспомогательный метод: определяет список FIGI для загрузки цен закрытия по запросу
     */
    public List<String> resolveInstrumentIdsForClosePrices(ClosePriceRequestDto request) {
        List<String> instrumentIds = request != null ? request.getInstruments() : null;
        if (instrumentIds != null) {
            // Фильтруем пустые/пробельные FIGI
            List<String> filtered = new ArrayList<>();
            for (String id : instrumentIds) {
                if (id != null && !id.trim().isEmpty()) {
                    filtered.add(id);
                }
            }
            return filtered;
        }

        List<String> allInstrumentIds = new ArrayList<>();
        // Только акции в рублях
        List<ShareEntity> shares = shareRepo.findAll();
        for (ShareEntity share : shares) {
            if ("RUB".equalsIgnoreCase(share.getCurrency())) {
                allInstrumentIds.add(share.getFigi());
            }
        }
        // Только фьючерсы в рублях
        List<FutureEntity> futures = futureRepo.findAll();
        for (FutureEntity future : futures) {
            if ("RUB".equalsIgnoreCase(future.getCurrency())) {
                allInstrumentIds.add(future.getFigi());
            }
        }
        // Все индикативы с непустым FIGI
        List<IndicativeEntity> indicatives = indicativeRepo.findAll();
        for (IndicativeEntity indicative : indicatives) {
            if (indicative.getFigi() != null && !indicative.getFigi().trim().isEmpty()) {
                allInstrumentIds.add(indicative.getFigi());
            }
        }
        return allInstrumentIds;
    }


    public List<ClosePriceDto> getClosePrices(List<String> instrumentIds, String status) {
        GetClosePricesRequest.Builder builder = GetClosePricesRequest.newBuilder();
        
        System.out.println("=== ЗАПРОС ЦЕН ЗАКРЫТИЯ ===");
        System.out.println("Количество инструментов: " + (instrumentIds != null ? instrumentIds.size() : 0));
        
        // Если instrumentIds не переданы, возвращаем пустой список
        // (логика загрузки всех инструментов перенесена в saveClosePrices)
        if (instrumentIds == null || instrumentIds.isEmpty()) {
            System.out.println("instrumentIds не переданы, возвращаем пустой список");
            return new ArrayList<>();
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
                    .atZone(ZoneId.of("Europe/Moscow"))
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
        
        // Если инструменты не указаны, получаем RUB инструменты (shares, futures) и все indicatives из БД
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
            
            // Получаем все индикативные инструменты из таблицы indicatives (без фильтра по валюте)
            List<IndicativeEntity> indicatives = indicativeRepo.findAll();
            for (IndicativeEntity indicative : indicatives) {
                // Исключаем пустые FIGI
                if (indicative.getFigi() != null && !indicative.getFigi().trim().isEmpty()) {
                    allInstrumentIds.add(indicative.getFigi());
                }
            }
            
            instrumentIds = allInstrumentIds;
        }
        
        // Если после получения из БД список все еще пуст, возвращаем пустой результат
        if (instrumentIds.isEmpty()) {
            return new SaveResponseDto(
                false,
                "Нет инструментов для загрузки цен закрытия. В базе данных нет акций в рублях, фьючерсов в рублях или индикативных инструментов.",
                0, 0, 0, new ArrayList<>()
            );
        }

        // Получаем цены закрытия из API по частям (shares+futures, затем indicatives)
        List<ClosePriceDto> closePricesFromApi = new ArrayList<>();
        int requestedInstrumentsCount = instrumentIds.size();
        
        try {
            // Если в запросе передан явный список инструментов — загружаем только по ним
            int batchSize = 100;
            if (request.getInstruments() != null && !request.getInstruments().isEmpty()) {
                System.out.println("Запрашиваем цены закрытия точечно для " + instrumentIds.size() + " инструментов батчами по " + batchSize);
                for (int i = 0; i < instrumentIds.size(); i += batchSize) {
                    int toIndex = Math.min(i + batchSize, instrumentIds.size());
                    List<String> batch = instrumentIds.subList(i, toIndex);
                    List<ClosePriceDto> prices = getClosePrices(batch, null);
                    closePricesFromApi.addAll(prices);
                    System.out.println("Получено цен для батча точечных инструментов (" + batch.size() + "): " + prices.size());
                    try { Thread.sleep(200); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }

                // Fallback для акций: если по части FIGI не пришли цены закрытия,
                // пробуем получить последнюю свечу предыдущего торгового дня (до 5 дней назад)
                try {
                    Set<String> requested = new HashSet<>();
                    for (String id : instrumentIds) {
                        if (id != null && !id.trim().isEmpty()) {
                            requested.add(id);
                        }
                    }
                    Set<String> havePrices = new HashSet<>();
                    for (ClosePriceDto p : closePricesFromApi) {
                        havePrices.add(p.figi());
                    }

                    int fallbackTried = 0;
                    int fallbackSaved = 0;
                    for (String figi : requested) {
                        if (havePrices.contains(figi)) {
                            continue;
                        }
                        // Обрабатываем только акции
                        if (!shareRepo.existsById(figi)) {
                            continue;
                        }
                        
                        // Обычный fallback для акций
                        // Ищем последнюю доступную свечу, до 5 дней назад
                        BigDecimal lastClose = null;
                        LocalDate lastDate = null;
                        for (int d = 1; d <= 5; d++) {
                            LocalDate day = LocalDate.now(ZoneId.of("Europe/Moscow")).minusDays(d);
                            List<CandleDto> candles = getCandles(figi, day, "CANDLE_INTERVAL_1_MIN");
                            fallbackTried++;
                            if (!candles.isEmpty()) {
                                CandleDto lastCandle = candles.get(candles.size() - 1);
                                lastClose = lastCandle.close();
                                lastDate = day;
                                break;
                            }
                            try { Thread.sleep(100); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        }
                        // Если свечей нет, пытаемся взять цену из последних сделок за предыдущие дни (до 5 дней)
                        if (lastClose == null) {
                            for (int d = 1; d <= 5; d++) {
                                LocalDate day = LocalDate.now(ZoneId.of("Europe/Moscow")).minusDays(d);
                                List<LastTradeDto> trades = getLastTrades(figi, day, "TRADE_SOURCE_ALL");
                                if (!trades.isEmpty()) {
                                    LastTradeDto lastTrade = trades.get(trades.size() - 1);
                                    lastClose = lastTrade.price();
                                    lastDate = day;
                                    break;
                                }
                                try { Thread.sleep(100); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                            }
                        }
                        if (lastClose != null && lastDate != null) {
                            closePricesFromApi.add(new ClosePriceDto(figi, lastDate.toString(), lastClose, null));
                            fallbackSaved++;
                        }
                    }
                    System.out.println("Fallback по свечам для акций (точечный запрос): попыток=" + fallbackTried + ", получено цен=" + fallbackSaved);
                } catch (Exception fe) {
                    System.err.println("Ошибка в fallback по свечам для акций (точечный запрос): " + fe.getMessage());
                }
            } else {
            // Сначала получаем цены для shares и futures
            List<String> sharesAndFuturesIds = new ArrayList<>();
            List<ShareEntity> shares = shareRepo.findAll();
            for (ShareEntity share : shares) {
                if ("RUB".equalsIgnoreCase(share.getCurrency())) {
                    sharesAndFuturesIds.add(share.getFigi());
                }
            }
            List<FutureEntity> futures = futureRepo.findAll();
            for (FutureEntity future : futures) {
                if ("RUB".equalsIgnoreCase(future.getCurrency())) {
                    sharesAndFuturesIds.add(future.getFigi());
                }
            }

            // Батчим запросы по 100 инструментов, чтобы избежать лимитов API
            if (!sharesAndFuturesIds.isEmpty()) {
                System.out.println("Запрашиваем цены закрытия для " + sharesAndFuturesIds.size() + " shares и futures батчами по " + batchSize);
                for (int i = 0; i < sharesAndFuturesIds.size(); i += batchSize) {
                    int toIndex = Math.min(i + batchSize, sharesAndFuturesIds.size());
                    List<String> batch = sharesAndFuturesIds.subList(i, toIndex);
                    List<ClosePriceDto> sharesFuturesPrices = getClosePrices(batch, null);
                    closePricesFromApi.addAll(sharesFuturesPrices);
                    System.out.println("Получено цен для батча shares/futures (" + batch.size() + "): " + sharesFuturesPrices.size());
                    try { Thread.sleep(200); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }

            // Затем пытаемся получить цены для indicatives (если API поддерживает)
            List<String> indicativesIds = new ArrayList<>();
            List<String> indicativesFigis = new ArrayList<>();
            List<IndicativeEntity> indicatives = indicativeRepo.findAll();
            for (IndicativeEntity indicative : indicatives) {
                // Предпочитаем UID для запросов к API, если он есть; иначе используем FIGI
                String uid = indicative.getUid();
                String figi = indicative.getFigi();
                if (uid != null && !uid.trim().isEmpty()) {
                    indicativesIds.add(uid);
                } else if (figi != null && !figi.trim().isEmpty()) {
                    indicativesIds.add(figi);
                }
                if (figi != null && !figi.trim().isEmpty()) {
                    indicativesFigis.add(figi);
                }
            }

            if (!indicativesIds.isEmpty()) {
                try {
                    System.out.println("Запрашиваем цены закрытия для " + indicativesIds.size() + " indicatives батчами по " + batchSize);
                    for (int i = 0; i < indicativesIds.size(); i += batchSize) {
                        int toIndex = Math.min(i + batchSize, indicativesIds.size());
                        List<String> batch = indicativesIds.subList(i, toIndex);
                        List<ClosePriceDto> indicativesPrices = getClosePrices(batch, null);
                        closePricesFromApi.addAll(indicativesPrices);
                        System.out.println("Получено цен для батча indicatives (" + batch.size() + "): " + indicativesPrices.size());
                        try { Thread.sleep(200); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка при получении цен закрытия для indicatives: " + e.getMessage());
                    System.err.println("Продолжаем без indicatives...");
                }

                // Fallback: для индикативов без цен закрытия пробуем взять закрытие из свечей за предыдущий день
                try {
                    LocalDate previousDay = LocalDate.now(ZoneId.of("Europe/Moscow")).minusDays(1);
                    Set<String> havePrices = new HashSet<>();
                    for (ClosePriceDto p : closePricesFromApi) {
                        havePrices.add(p.figi());
                    }

                    int fallbackTried = 0;
                    int fallbackSaved = 0;
                    for (int idx = 0; idx < indicativesFigis.size(); idx++) {
                        String figi = indicativesFigis.get(idx);
                        if (figi == null || figi.trim().isEmpty()) {
                            continue;
                        }
                        if (havePrices.contains(figi)) {
                            continue;
                        }
                        // Получаем свечи за день и берём последнюю как цену закрытия.
                        // Для индикативов пробуем сначала uid, если доступен; иначе figi
                        String instrumentIdForCandles = figi;
                        try {
                            IndicativeEntity entity = indicativeRepo.findById(figi).orElse(null);
                            if (entity != null && entity.getUid() != null && !entity.getUid().trim().isEmpty()) {
                                instrumentIdForCandles = entity.getUid();
                            }
                        } catch (Exception ignored) {}
                        List<CandleDto> candles = getCandles(instrumentIdForCandles, previousDay, "CANDLE_INTERVAL_1_MIN");
                        fallbackTried++;
                        if (!candles.isEmpty()) {
                            CandleDto lastCandle = candles.get(candles.size() - 1);
                            ClosePriceDto fallbackPrice = new ClosePriceDto(
                                    figi,
                                    previousDay.toString(),
                                    lastCandle.close(),
                                    null
                            );
                            closePricesFromApi.add(fallbackPrice);
                            fallbackSaved++;
                        }
                        try { Thread.sleep(100); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    }
                    System.out.println("Fallback по свечам для indicatives: попыток=" + fallbackTried + ", получено цен=" + fallbackSaved);
                } catch (Exception fe) {
                    System.err.println("Ошибка в fallback по свечам для indicatives: " + fe.getMessage());
                }
            }

            // Закрываем ветку else (полная загрузка)
        }

        } catch (Exception e) {
            System.err.println("Ошибка при получении цен закрытия из API: " + e.getMessage());
            System.err.println("Количество инструментов в запросе: " + instrumentIds.size());
            
            // Возвращаем ошибку с подробной информацией
            return new SaveResponseDto(
                false,
                "Ошибка при получении цен закрытия из API: " + e.getMessage() + 
                ". Количество инструментов: " + instrumentIds.size(),
                0, 0, 0, new ArrayList<>()
            );
        }
        
        List<ClosePriceDto> savedPrices = new ArrayList<>();
        int existingCount = 0;
        
        int failedSavesCount = 0;
        for (ClosePriceDto closePriceDto : closePricesFromApi) {
            LocalDate priceDate = LocalDate.parse(closePriceDto.tradingDate());
            ClosePriceKey key = new ClosePriceKey(priceDate, closePriceDto.figi());

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
                } else {
                    // Проверяем в таблице indicatives
                    IndicativeEntity indicative = indicativeRepo.findById(closePriceDto.figi()).orElse(null);
                    if (indicative != null) {
                        instrumentType = "INDICATIVE";
                        currency = indicative.getCurrency();
                        exchange = indicative.getExchange();
                    }
                }
            }

            // Если запись уже существует — считаем как существующую и не пытаемся сохранять
            try {
                if (closePriceRepo.existsById(key)) {
                    existingCount++;
                    continue;
                }
            } catch (Exception e) {
                // В спорных случаях всё равно попробуем сохранить, а ошибку залогируем
                System.err.println("existsById check failed for key (" + priceDate + ", " + closePriceDto.figi() + ") : " + e.getMessage());
            }

            // Создаем и пытаемся сохранить новую запись
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
            } catch (DataIntegrityViolationException dive) {
                failedSavesCount++;
                System.err.println("DataIntegrityViolation saving close price for " + closePriceDto.figi() +
                        " on " + priceDate + ": " + dive.getMessage());
            } catch (Exception e) {
                // Если не удалось сохранить по другой причине, проверим, существует ли запись
                boolean exists = false;
                try { exists = closePriceRepo.existsById(key); } catch (Exception ignored) {}
                if (exists) {
                    existingCount++;
                } else {
                    System.err.println("Error saving close price for " + closePriceDto.figi() +
                            " on " + priceDate + ": " + e.getMessage());
                    failedSavesCount++;
                }
            }
        }
        
        // Подсчитываем полученные цены и сохраненные
        int receivedPricesCount = closePricesFromApi.size();
        // Формируем ответ
        boolean success = receivedPricesCount > 0;
        String message;
        
        if (savedPrices.isEmpty()) {
            if (closePricesFromApi.isEmpty()) {
                message = "Новых цен закрытия не обнаружено. По заданным инструментам цены не найдены.";
            } else {
                message = failedSavesCount > 0
                    ? String.format("Новых цен закрытия не обнаружено. Ошибок сохранения: %d. Проверьте наличие инструментов и ограничения БД.", failedSavesCount)
                    : "Новых цен закрытия не обнаружено. Все найденные цены уже существуют в базе данных.";
            }
        } else {
            message = String.format(
                "Запрошено по %d инструментам, получено цен: %d. Сохранено новых записей: %d, пропущено (уже были): %d.",
                requestedInstrumentsCount,
                receivedPricesCount,
                savedPrices.size(),
                existingCount
            );
        }
        
        return new SaveResponseDto(
            success,
            message,
            requestedInstrumentsCount,
            savedPrices.size(),
            existingCount,
            savedPrices
        );
    }

    // === МЕТОДЫ ДЛЯ РАБОТЫ СО СВЕЧАМИ ===

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
                                if (share.getFigi() != null && !share.getFigi().trim().isEmpty()) {
                                    instrumentIds.add(share.getFigi());
                                }
                            }
                        } else if ("FUTURES".equalsIgnoreCase(assetType)) {
                            // Получаем все FIGI из таблицы futures
                            List<FutureEntity> futures = futureRepo.findAll();
                            for (FutureEntity future : futures) {
                                if (future.getFigi() != null && !future.getFigi().trim().isEmpty()) {
                                    instrumentIds.add(future.getFigi());
                                }
                            }
                        } else if ("INDICATIVES".equalsIgnoreCase(assetType)) {
                            // Получаем все FIGI из таблицы indicatives
                            List<IndicativeEntity> indicatives = indicativeRepo.findAll();
                            for (IndicativeEntity indicative : indicatives) {
                                // Исключаем пустые FIGI
                                if (indicative.getFigi() != null && !indicative.getFigi().trim().isEmpty()) {
                                    instrumentIds.add(indicative.getFigi());
                                }
                            }
                        }
                    }
                } else {
                    // Если типы активов не указаны, получаем все инструменты
                    // Получаем все FIGI из таблицы shares
                    List<ShareEntity> shares = shareRepo.findAll();
                    for (ShareEntity share : shares) {
                        if (share.getFigi() != null && !share.getFigi().trim().isEmpty()) {
                            instrumentIds.add(share.getFigi());
                        }
                    }
                    
                    // Получаем все FIGI из таблицы futures
                    List<FutureEntity> futures = futureRepo.findAll();
                    for (FutureEntity future : futures) {
                        if (future.getFigi() != null && !future.getFigi().trim().isEmpty()) {
                            instrumentIds.add(future.getFigi());
                        }
                    }
                    
                    // Получаем все FIGI из таблицы indicatives
                    List<IndicativeEntity> indicatives = indicativeRepo.findAll();
                    for (IndicativeEntity indicative : indicatives) {
                        // Исключаем пустые FIGI
                        if (indicative.getFigi() != null && !indicative.getFigi().trim().isEmpty()) {
                            instrumentIds.add(indicative.getFigi());
                        }
                    }
                }
            }
            
            if (instrumentIds.isEmpty()) {
                return new SaveResponseDto(
                    false,
                    "Нет инструментов для загрузки свечей. В базе данных нет акций, фьючерсов или индикативных инструментов.",
                    0, 0, 0, new ArrayList<>()
                );
            }
            
            List<CandleDto> collectedCandles = new ArrayList<>();
            int totalRequested = 0;
            
            // Обрабатываем все инструменты пакетами для предотвращения зависания
            int batchSize = 50; // Размер пакета
            int totalInstruments = instrumentIds.size();
            System.out.println("Обрабатываем " + totalInstruments + " инструментов пакетами по " + batchSize);
            
            for (int batchStart = 0; batchStart < totalInstruments; batchStart += batchSize) {
                int batchEnd = Math.min(batchStart + batchSize, totalInstruments);
                List<String> batch = instrumentIds.subList(batchStart, batchEnd);
                
                System.out.println("Обработка пакета " + (batchStart / batchSize + 1) + "/" + ((totalInstruments + batchSize - 1) / batchSize) + 
                                 " (инструменты " + (batchStart + 1) + "-" + batchEnd + " из " + totalInstruments + ")");
                
                for (int i = 0; i < batch.size(); i++) {
                    String instrumentId = batch.get(i);
                    
                    // Пропускаем пустые или null FIGI
                    if (instrumentId == null || instrumentId.trim().isEmpty()) {
                        System.err.println("Skipping empty or null FIGI in batch processing");
                        continue;
                    }
                    
                    try {
                        // Показываем прогресс каждые 10 инструментов
                        if (i % 10 == 0) {
                            System.out.println("Прогресс в пакете: " + (i + 1) + "/" + batch.size() + " инструментов обработано");
                        }

                        // Получаем свечи для инструмента
                        List<CandleDto> candles = getCandles(instrumentId, date, interval);
                        totalRequested += candles.size();
                        collectedCandles.addAll(candles);
                        
                        // Увеличенная задержка между запросами для снижения нагрузки
                        Thread.sleep(500);
                        
                    } catch (Exception e) {
                        System.err.println("Error processing instrument " + instrumentId + ": " + e.getMessage());
                    }
                }
                
                // Увеличенная пауза между пакетами для снижения нагрузки
                if (batchEnd < totalInstruments) {
                    System.out.println("Пауза между пакетами...");
                    try {
                        Thread.sleep(5000); // 5 секунд между пакетами
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("Interrupted during batch pause");
                        break;
                    }
                }
            }
            
            // Индивидуальное сохранение каждой свечи
            int savedCount = 0;
            int existingCount = 0;
            
            for (CandleDto candleDto : collectedCandles) {
                try {
                    CandleEntity entity = new CandleEntity(
                        candleDto.figi(),
                        candleDto.volume(),
                        candleDto.high(),
                        candleDto.low(),
                        candleDto.time(),
                        candleDto.close(),
                        candleDto.open(),
                        candleDto.isComplete()
                    );
                    candleRepository.save(entity);
                    savedCount++;
                } catch (Exception e) {
                    // Assume duplicate if save fails
                    existingCount++;
                }
            }
            
            boolean success = savedCount > 0;
            String message = success
                ? String.format("Успешно загружено %d новых свечей из %d найденных.", savedCount, totalRequested)
                : (totalRequested == existingCount
                    ? "Новых свечей не обнаружено. Все найденные свечи уже существуют в базе данных."
                    : "Свечи не сохранены.");
            
            return new SaveResponseDto(success, message, totalRequested, savedCount, existingCount, collectedCandles);
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
                                int addedCount = 0;
                                for (ShareEntity share : shares) {
                                    if (share.getFigi() != null && !share.getFigi().trim().isEmpty()) {
                                        instrumentIds.add(share.getFigi());
                                        addedCount++;
                                    }
                                }
                                System.out.println("[" + taskId + "] Добавлено " + addedCount + " акций (из " + shares.size() + " в БД)");
                            } else if ("FUTURES".equalsIgnoreCase(assetType)) {
                                // Получаем все FIGI из таблицы futures
                                List<FutureEntity> futures = futureRepo.findAll();
                                int addedCount = 0;
                                for (FutureEntity future : futures) {
                                    if (future.getFigi() != null && !future.getFigi().trim().isEmpty()) {
                                        instrumentIds.add(future.getFigi());
                                        addedCount++;
                                    }
                                }
                                System.out.println("[" + taskId + "] Добавлено " + addedCount + " фьючерсов (из " + futures.size() + " в БД)");
                            } else if ("INDICATIVES".equalsIgnoreCase(assetType)) {
                                // Получаем все FIGI из таблицы indicatives
                                List<IndicativeEntity> indicatives = indicativeRepo.findAll();
                                int addedCount = 0;
                                for (IndicativeEntity indicative : indicatives) {
                                    // Исключаем пустые FIGI
                                    if (indicative.getFigi() != null && !indicative.getFigi().trim().isEmpty()) {
                                        instrumentIds.add(indicative.getFigi());
                                        addedCount++;
                                    }
                                }
                                System.out.println("[" + taskId + "] Добавлено " + addedCount + " индикативных инструментов");
                            }
                        }
                    } else {
                        // Если типы активов не указаны, получаем все инструменты
                        // Получаем все FIGI из таблицы shares
                        List<ShareEntity> shares = shareRepo.findAll();
                        int addedShares = 0;
                        for (ShareEntity share : shares) {
                            if (share.getFigi() != null && !share.getFigi().trim().isEmpty()) {
                                instrumentIds.add(share.getFigi());
                                addedShares++;
                            }
                        }
                        System.out.println("[" + taskId + "] Добавлено " + addedShares + " акций (из " + shares.size() + " в БД)");
                        
                        // Получаем все FIGI из таблицы futures
                        List<FutureEntity> futures = futureRepo.findAll();
                        int addedFutures = 0;
                        for (FutureEntity future : futures) {
                            if (future.getFigi() != null && !future.getFigi().trim().isEmpty()) {
                                instrumentIds.add(future.getFigi());
                                addedFutures++;
                            }
                        }
                        System.out.println("[" + taskId + "] Добавлено " + addedFutures + " фьючерсов (из " + futures.size() + " в БД)");
                        
                        // Получаем все FIGI из таблицы indicatives
                        List<IndicativeEntity> indicatives = indicativeRepo.findAll();
                        int addedIndicatives = 0;
                        for (IndicativeEntity indicative : indicatives) {
                            // Исключаем пустые FIGI
                            if (indicative.getFigi() != null && !indicative.getFigi().trim().isEmpty()) {
                                instrumentIds.add(indicative.getFigi());
                                addedIndicatives++;
                            }
                        }
                        
                        System.out.println("[" + taskId + "] Добавлено " + shares.size() + " акций, " + futures.size() + " фьючерсов и " + addedIndicatives + " индикативных инструментов");
                    }
                }
                
                if (instrumentIds.isEmpty()) {
                    System.out.println("[" + taskId + "] Нет инструментов для загрузки свечей");
                    return;
                }
                
                // Обрабатываем все инструменты пакетами для предотвращения зависания
                int batchSize = 100; // Размер пакета в асинхронном режиме
                int totalInstruments = instrumentIds.size();
                System.out.println("[" + taskId + "] Обрабатываем " + totalInstruments + " инструментов пакетами по " + batchSize);
                
                int totalRequested = 0;
                int savedCount = 0;
                int existingCount = 0;
                int processedInstruments = 0;
                
                for (int batchStart = 0; batchStart < totalInstruments; batchStart += batchSize) {
                    int batchEnd = Math.min(batchStart + batchSize, totalInstruments);
                    List<String> batch = instrumentIds.subList(batchStart, batchEnd);
                    
                    System.out.println("[" + taskId + "] Обработка пакета " + (batchStart / batchSize + 1) + "/" + ((totalInstruments + batchSize - 1) / batchSize) + 
                                     " (инструменты " + (batchStart + 1) + "-" + batchEnd + " из " + totalInstruments + ")");
                    
                    for (String instrumentId : batch) {
                        // Пропускаем пустые или null FIGI
                        if (instrumentId == null || instrumentId.trim().isEmpty()) {
                            System.err.println("[" + taskId + "] Skipping empty or null FIGI in batch processing");
                            continue;
                        }
                        
                        try {
                            processedInstruments++;
                            System.out.println("[" + taskId + "] Обработка инструмента " + processedInstruments + "/" + totalInstruments + ": " + instrumentId);
                            
                            // Получаем свечи для инструмента и сохраняем по одной
                            List<CandleDto> candles = getCandles(instrumentId, date, interval);
                            totalRequested += candles.size();
                            
                            for (CandleDto candleDto : candles) {
                                try {
                                    CandleEntity entity = new CandleEntity(
                                        candleDto.figi(),
                                        candleDto.volume(),
                                        candleDto.high(),
                                        candleDto.low(),
                                        candleDto.time(),
                                        candleDto.close(),
                                        candleDto.open(),
                                        candleDto.isComplete()
                                    );
                                    candleRepository.save(entity);
                                    savedCount++;
                                } catch (Exception e) {
                                    // Assume duplicate if save fails
                                    existingCount++;
                                }
                            }
                            
                            // Увеличенная задержка между запросами для снижения нагрузки
                            Thread.sleep(500);
                            
                        } catch (Exception e) {
                            System.err.println("[" + taskId + "] Ошибка обработки инструмента " + instrumentId + ": " + e.getMessage());
                        }
                    }
                    
                    // Увеличенная пауза между пакетами для снижения нагрузки
                    if (batchEnd < totalInstruments) {
                        System.out.println("[" + taskId + "] Пауза между пакетами...");
                        try {
                            Thread.sleep(10000); // 10 секунд между пакетами в асинхронном режиме
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.err.println("[" + taskId + "] Interrupted during batch pause");
                            break;
                        }
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
