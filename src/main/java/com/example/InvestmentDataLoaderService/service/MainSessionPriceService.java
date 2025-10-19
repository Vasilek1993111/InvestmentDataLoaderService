package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.ClosePriceEntity;
import com.example.InvestmentDataLoaderService.entity.ClosePriceKey;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.entity.IndicativeEntity;
import com.example.InvestmentDataLoaderService.entity.MinuteCandleEntity;
import com.example.InvestmentDataLoaderService.repository.ClosePriceRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.IndicativeRepository;
import com.example.InvestmentDataLoaderService.repository.MinuteCandleRepository;
import com.example.InvestmentDataLoaderService.repository.SystemLogRepository;
import com.example.InvestmentDataLoaderService.entity.SystemLogEntity;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.*;
import org.springframework.dao.DataIntegrityViolationException;
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc.MarketDataServiceBlockingStub;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import com.example.InvestmentDataLoaderService.config.RateLimitConfig.BatchProcessingProperties;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MainSessionPriceService {

    private final MarketDataServiceBlockingStub marketDataService;
    private final ShareRepository shareRepo;
    private final FutureRepository futureRepo;
    private final ClosePriceRepository closePriceRepo;
    private final IndicativeRepository indicativeRepo;
    private final MinuteCandleRepository minuteCandleRepo;
    private final SystemLogRepository systemLogRepository;
    private final RateLimitService rateLimitService;
    private final RetryService retryService;
    private final ExecutorService executorService;
    private final BatchProcessingProperties batchProcessingProperties;

    public MainSessionPriceService(MarketDataServiceBlockingStub marketDataService,
                            ShareRepository shareRepo,
                            FutureRepository futureRepo,
                            ClosePriceRepository closePriceRepo,
                            IndicativeRepository indicativeRepo,
                            MinuteCandleRepository minuteCandleRepo,
                            SystemLogRepository systemLogRepository,
                            RateLimitService rateLimitService,
                            RetryService retryService,
                            BatchProcessingProperties batchProcessingProperties) {
        this.marketDataService = marketDataService;
        this.shareRepo = shareRepo;
        this.futureRepo = futureRepo;
        this.closePriceRepo = closePriceRepo;
        this.indicativeRepo = indicativeRepo;
        this.minuteCandleRepo = minuteCandleRepo;
        this.systemLogRepository = systemLogRepository;
        this.rateLimitService = rateLimitService;
        this.retryService = retryService;
        this.batchProcessingProperties = batchProcessingProperties;
        this.executorService = Executors.newFixedThreadPool(10); // Ограничиваем количество потоков
    }

    /**
     * Получение цен закрытия для всех акций из БД через T-INVEST API
     */
    public List<ClosePriceDto> getClosePricesForAllShares() {
        List<String> shareFigis = new ArrayList<>();
        
        // Получаем только акции в рублях из таблицы shares
        List<ShareEntity> shares = shareRepo.findAll();
        for (ShareEntity share : shares) {
            if ("RUB".equalsIgnoreCase(share.getCurrency())) {
                shareFigis.add(share.getFigi());
            }
        }
        
        if (shareFigis.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ClosePriceDto> allPrices = getClosePrices(shareFigis, null);
        return filterValidPrices(allPrices);
    }

    /**
     * Получение цен закрытия для всех фьючерсов из БД через T-INVEST API
     */
    public List<ClosePriceDto> getClosePricesForAllFutures() {
        List<String> futureFigis = new ArrayList<>();
        
        // Получаем только фьючерсы в рублях из таблицы futures
        List<FutureEntity> futures = futureRepo.findAll();
        for (FutureEntity future : futures) {
            if ("RUB".equalsIgnoreCase(future.getCurrency())) {
                futureFigis.add(future.getFigi());
            }
        }
        
        if (futureFigis.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ClosePriceDto> allPrices = getClosePrices(futureFigis, null);
        return filterValidPrices(allPrices);
    }

    /**
     * Фильтрация валидных цен закрытия (исключает цены с датой 1970-01-01)
     */
    private List<ClosePriceDto> filterValidPrices(List<ClosePriceDto> prices) {
        List<ClosePriceDto> validPrices = new ArrayList<>();
        int invalidPricesCount = 0;
        
        for (ClosePriceDto price : prices) {
            if ("1970-01-01".equals(price.tradingDate())) {
                invalidPricesCount++;
                System.out.println("Фильтруем неверную цену с датой 1970-01-01 для FIGI: " + price.figi());
            } else {
                validPrices.add(price);
            }
        }
        
        if (invalidPricesCount > 0) {
            System.out.println("Отфильтровано " + invalidPricesCount + " неверных цен с датой 1970-01-01");
        }
        
        return validPrices;
    }

    /**
     * Фильтрация валидных цен закрытия с детальной статистикой
     */
    private List<ClosePriceDto> filterValidPricesWithStats(List<ClosePriceDto> prices) {
        List<ClosePriceDto> validPrices = new ArrayList<>();
        int invalidPricesCount = 0;
        
        for (ClosePriceDto price : prices) {
            if ("1970-01-01".equals(price.tradingDate())) {
                invalidPricesCount++;
                System.out.println("Фильтруем неверную цену с датой 1970-01-01 для FIGI: " + price.figi());
            } else {
                validPrices.add(price);
            }
        }
        
        if (invalidPricesCount > 0) {
            System.out.println("Отфильтровано " + invalidPricesCount + " неверных цен с датой 1970-01-01");
        }
        
        return validPrices;
    }

    public List<ClosePriceDto> getClosePrices(List<String> instrumentIds, String status) {
        System.out.println("=== ЗАПРОС ЦЕН ЗАКРЫТИЯ ===");
        System.out.println("Количество инструментов: " + (instrumentIds != null ? instrumentIds.size() : 0));
        
        // Если instrumentIds не переданы, возвращаем пустой список
        if (instrumentIds == null || instrumentIds.isEmpty()) {
            System.out.println("instrumentIds не переданы, возвращаем пустой список");
            return new ArrayList<>();
        }
        
        // Если после получения из БД список все еще пуст, возвращаем пустой результат
        if (instrumentIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            // Используем rate limiting и retry для запроса к API
            List<ClosePriceDto> closePricesFromApi = retryService.executeWithRetryAndRateLimit(
                () -> getClosePricesDirectly(instrumentIds, status),
                "getClosePrices",
                "close_prices",
                rateLimitService
            );
            
            // Фильтруем неверные цены с датой 1970-01-01
            return filterValidPrices(closePricesFromApi);
            
        } catch (Exception e) {
            System.err.println("Ошибка при получении цен закрытия: " + e.getMessage());
            e.printStackTrace();
            
            // Возвращаем пустой список вместо исключения
            return new ArrayList<>();
        }
    }

    /**
     * Прямой вызов API без дополнительного rate limiting (используется внутри getClosePrices)
     */
    private List<ClosePriceDto> getClosePricesDirectly(List<String> instrumentIds, String status) {
        GetClosePricesRequest.Builder builder = GetClosePricesRequest.newBuilder();

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
        
        System.out.println("Получено " + list.size() + " цен закрытия из API");
        return list;
    }

    public SaveResponseDto saveClosePrices(ClosePriceRequestDto request) {
        List<String> instrumentIds = request.getInstruments();
        
        // Если инструменты не указаны, получаем только RUB инструменты (shares, futures) из БД
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
                "Нет инструментов для загрузки цен закрытия. В базе данных нет акций в рублях или фьючерсов в рублях.",
                0, 0, 0, 0, 0, new ArrayList<>()
            );
        }

        // Получаем цены закрытия из API по частям (только shares+futures)
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
            }

        } catch (Exception e) {
            System.err.println("Ошибка при получении цен закрытия из API: " + e.getMessage());
            System.err.println("Количество инструментов в запросе: " + instrumentIds.size());
            
            return new SaveResponseDto(
                false,
                "Ошибка при получении цен закрытия из API: " + e.getMessage() + 
                ". Количество инструментов: " + instrumentIds.size(),
                0, 0, 0, 0, 0, new ArrayList<>()
            );
        }
        
        // Фильтруем неверные цены и получаем статистику
        List<ClosePriceDto> closePrices = filterValidPricesWithStats(closePricesFromApi);
        int invalidPricesFiltered = closePricesFromApi.size() - closePrices.size();
        
        List<ClosePriceDto> savedPrices = new ArrayList<>();
        int existingCount = 0;
        int failedSavesCount = 0;
        
        System.out.println("=== НАЧАЛО ОБРАБОТКИ ЦЕН ЗАКРЫТИЯ ===");
        System.out.println("Всего получено цен из API: " + closePricesFromApi.size());
        System.out.println("Отфильтровано неверных цен (1970-01-01): " + invalidPricesFiltered);
        System.out.println("Валидных цен для обработки: " + closePrices.size());
        System.out.println("Запрашивалось инструментов: " + requestedInstrumentsCount);
        
        for (ClosePriceDto closePriceDto : closePrices) {
            LocalDate priceDate = LocalDate.parse(closePriceDto.tradingDate());
            ClosePriceKey key = new ClosePriceKey(priceDate, closePriceDto.figi());
            
            System.out.println("Обрабатываем: " + closePriceDto.figi() + " на дату: " + priceDate + " цена: " + closePriceDto.closePrice());

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

            // Если запись уже существует в основной таблице — считаем как существующую и не пытаемся сохранять
            try {
                boolean existsInMain = closePriceRepo.existsById(key);
                
                if (existsInMain) {
                    existingCount++;
                    continue;
                }
            } catch (Exception e) {
                System.err.println("existsById check failed for key (" + priceDate + ", " + closePriceDto.figi() + ") : " + e.getMessage());
            }

            // Создаем и сохраняем новую запись в invest.close_prices
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
                System.out.println("Successfully saved to close_prices: " + closePriceDto.figi() + " on " + priceDate);
            } catch (DataIntegrityViolationException dive) {
                System.err.println("DataIntegrityViolation saving to close_prices for " + closePriceDto.figi() +
                        " on " + priceDate + ": " + dive.getMessage());
                failedSavesCount++;
            } catch (Exception e) {
                System.err.println("Error saving to close_prices for " + closePriceDto.figi() +
                        " on " + priceDate + ": " + e.getMessage());
                failedSavesCount++;
            }
        }
        
        System.out.println("=== ЗАВЕРШЕНИЕ ОБРАБОТКИ ЦЕН ЗАКРЫТИЯ ===");
        System.out.println("Запрошено инструментов: " + requestedInstrumentsCount);
        System.out.println("Получено цен из API: " + closePricesFromApi.size());
        System.out.println("Отфильтровано неверных цен: " + invalidPricesFiltered);
        System.out.println("Валидных цен для обработки: " + closePrices.size());
        System.out.println("Сохранено успешно: " + savedPrices.size());
        System.out.println("Уже существовало: " + existingCount);
        System.out.println("Ошибок сохранения: " + failedSavesCount);
        
        // Подсчитываем "потерянные" цены
        int missingPrices = requestedInstrumentsCount - closePricesFromApi.size();
        int processedPrices = closePrices.size();
        int unprocessedPrices = closePricesFromApi.size() - processedPrices;
        
        System.out.println("=== ДЕТАЛЬНАЯ СТАТИСТИКА ===");
        System.out.println("Цены не получены из API: " + missingPrices);
        System.out.println("Цены отфильтрованы как неверные: " + invalidPricesFiltered);
        System.out.println("Цены обработаны: " + processedPrices);
        System.out.println("Цены не обработаны: " + unprocessedPrices);
        System.out.println("Проверка: " + requestedInstrumentsCount + " = " + closePricesFromApi.size() + " + " + missingPrices);
        
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
            invalidPricesFiltered,
            missingPrices,
            savedPrices
        );
    }

    // ==================== АСИНХРОННЫЕ МЕТОДЫ ====================

    /**
     * Асинхронное сохранение цен закрытия для акций и фьючерсов
     * 
     * <p>Выполняет параллельную обработку акций и фьючерсов с логированием результатов.</p>
     * 
     * @param taskId уникальный идентификатор задачи
     * @return CompletableFuture с результатом операции
     */
    public CompletableFuture<SaveResponseDto> saveClosePricesAsync(String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("[" + taskId + "] Начало асинхронного сохранения цен закрытия с пакетной обработкой");
                
                // Получаем все акции и фьючерсы (блокирующий запрос остается)
                List<ShareEntity> shares = shareRepo.findAll();
                List<FutureEntity> futures = futureRepo.findAll();
                
                System.out.println("[" + taskId + "] Найдено акций: " + shares.size() + ", фьючерсов: " + futures.size());
                
                // Фильтруем только RUB инструменты
                List<ShareEntity> rubShares = shares.stream()
                    .filter(share -> "RUB".equalsIgnoreCase(share.getCurrency()))
                    .collect(Collectors.toList());
                
                List<FutureEntity> rubFutures = futures.stream()
                    .filter(future -> "RUB".equalsIgnoreCase(future.getCurrency()))
                    .collect(Collectors.toList());
                
                System.out.println("[" + taskId + "] RUB акций: " + rubShares.size() + ", RUB фьючерсов: " + rubFutures.size());
                
                // Разбиваем на пакеты
                int batchSize = batchProcessingProperties.getBatchSize();
                List<List<ShareEntity>> shareBatches = createBatches(rubShares, batchSize);
                List<List<FutureEntity>> futureBatches = createBatches(rubFutures, batchSize);
                
                System.out.println("[" + taskId + "] Создано пакетов акций: " + shareBatches.size() + ", пакетов фьючерсов: " + futureBatches.size());
                
                // Обрабатываем пакеты акций
                List<CompletableFuture<List<ClosePriceProcessingResult>>> shareBatchFutures = new ArrayList<>();
                for (int i = 0; i < shareBatches.size(); i++) {
                    final int batchIndex = i;
                    CompletableFuture<List<ClosePriceProcessingResult>> batchFuture = processShareBatchAsync(shareBatches.get(i), taskId + "_shares_batch_" + batchIndex);
                    shareBatchFutures.add(batchFuture);
                    
                    // Добавляем задержку между пакетами
                    if (i < shareBatches.size() - 1) {
                        try {
                            Thread.sleep(batchProcessingProperties.getBatchDelayMs());
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                
                // Обрабатываем пакеты фьючерсов
                List<CompletableFuture<List<ClosePriceProcessingResult>>> futureBatchFutures = new ArrayList<>();
                for (int i = 0; i < futureBatches.size(); i++) {
                    final int batchIndex = i;
                    CompletableFuture<List<ClosePriceProcessingResult>> batchFuture = processFutureBatchAsync(futureBatches.get(i), taskId + "_futures_batch_" + batchIndex);
                    futureBatchFutures.add(batchFuture);
                    
                    // Добавляем задержку между пакетами
                    if (i < futureBatches.size() - 1) {
                        try {
                            Thread.sleep(batchProcessingProperties.getBatchDelayMs());
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                
                // Объединяем все futures
                List<CompletableFuture<List<ClosePriceProcessingResult>>> allBatchFutures = new ArrayList<>();
                allBatchFutures.addAll(shareBatchFutures);
                allBatchFutures.addAll(futureBatchFutures);
                
                // Ждем завершения всех пакетов
                CompletableFuture<Void> allCompleted = CompletableFuture.allOf(
                    allBatchFutures.toArray(new CompletableFuture[0])
                );
                
                // Получаем результаты
                allCompleted.get();
                
                // Собираем все результаты
                List<ClosePriceProcessingResult> results = new ArrayList<>();
                for (CompletableFuture<List<ClosePriceProcessingResult>> batchFuture : allBatchFutures) {
                    results.addAll(batchFuture.join());
                }
                
                // Агрегируем статистику
                int totalRequested = results.size();
                int newItemsSaved = (int) results.stream().filter(ClosePriceProcessingResult::isSaved).count();
                int existingItemsSkipped = (int) results.stream().filter(ClosePriceProcessingResult::isExisting).count();
                int errorCount = (int) results.stream().filter(ClosePriceProcessingResult::hasError).count();
                
                List<Map<String, Object>> savedItems = results.stream()
                    .filter(ClosePriceProcessingResult::isSaved)
                    .map(ClosePriceProcessingResult::getSavedItem)
                    .collect(Collectors.toList());
                
                String message = String.format("Пакетная обработка завершена. Загружено %d новых цен закрытия из %d найденных. Ошибок: %d", 
                    newItemsSaved, totalRequested, errorCount);
                
                SaveResponseDto result = new SaveResponseDto(
                    true,
                    message,
                    totalRequested,
                    newItemsSaved,
                    existingItemsSkipped,
                    errorCount,
                    0, // missingFromApi
                    savedItems
                );
                
                System.out.println("[" + taskId + "] Пакетная обработка завершена");
                System.out.println("[" + taskId + "] Результат: " + result.getMessage());
                
                // Логируем успешное завершение в БД
                try {
                    SystemLogEntity successLog = new SystemLogEntity();
                    successLog.setTaskId(taskId);
                    successLog.setEndpoint("/api/main-session-prices/");
                    successLog.setMethod("POST");
                    successLog.setStatus("COMPLETED");
                    successLog.setMessage(result.getMessage());
                    successLog.setStartTime(Instant.now().minusMillis(1000)); // Примерное время начала
                    successLog.setEndTime(Instant.now());
                    systemLogRepository.save(successLog);
                } catch (Exception logException) {
                    System.err.println("Ошибка сохранения лога успешного завершения: " + logException.getMessage());
                }
                
                return result;
                
            } catch (Exception e) {
                System.err.println("[" + taskId + "] Ошибка асинхронного сохранения цен закрытия: " + e.getMessage());
                e.printStackTrace();
                
                // Логируем ошибку в БД
                try {
                    SystemLogEntity errorLog = new SystemLogEntity();
                    errorLog.setTaskId(taskId);
                    errorLog.setEndpoint("/api/main-session-prices/");
                    errorLog.setMethod("POST");
                    errorLog.setStatus("FAILED");
                    errorLog.setMessage("Ошибка асинхронного сохранения цен закрытия: " + e.getMessage());
                    errorLog.setStartTime(Instant.now().minusMillis(1000)); // Примерное время начала
                    errorLog.setEndTime(Instant.now());
                    systemLogRepository.save(errorLog);
                } catch (Exception logException) {
                    System.err.println("Ошибка сохранения лога ошибки: " + logException.getMessage());
                }
                
                throw new RuntimeException("Ошибка асинхронного сохранения цен закрытия: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Асинхронное сохранение цен закрытия для акций
     */
    public CompletableFuture<SaveResponseDto> saveSharesClosePricesAsync(String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("[" + taskId + "] Начало асинхронного сохранения цен закрытия для акций с пакетной обработкой");
                
                // Получаем все акции (блокирующий запрос остается)
                List<ShareEntity> shares = shareRepo.findAll();
                
                System.out.println("[" + taskId + "] Найдено акций: " + shares.size());
                
                // Фильтруем только RUB инструменты
                List<ShareEntity> rubShares = shares.stream()
                    .filter(share -> "RUB".equalsIgnoreCase(share.getCurrency()))
                    .collect(Collectors.toList());
                
                System.out.println("[" + taskId + "] RUB акций: " + rubShares.size());
                
                // Разбиваем на пакеты
                int batchSize = batchProcessingProperties.getBatchSize();
                List<List<ShareEntity>> shareBatches = createBatches(rubShares, batchSize);
                
                System.out.println("[" + taskId + "] Создано пакетов акций: " + shareBatches.size());
                
                // Обрабатываем пакеты акций
                List<CompletableFuture<List<ClosePriceProcessingResult>>> shareBatchFutures = new ArrayList<>();
                for (int i = 0; i < shareBatches.size(); i++) {
                    final int batchIndex = i;
                    CompletableFuture<List<ClosePriceProcessingResult>> batchFuture = processShareBatchAsync(shareBatches.get(i), taskId + "_shares_batch_" + batchIndex);
                    shareBatchFutures.add(batchFuture);
                    
                    // Добавляем задержку между пакетами
                    if (i < shareBatches.size() - 1) {
                        try {
                            Thread.sleep(batchProcessingProperties.getBatchDelayMs());
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                
                // Ждем завершения всех пакетов
                CompletableFuture<Void> allCompleted = CompletableFuture.allOf(
                    shareBatchFutures.toArray(new CompletableFuture[0])
                );
                
                // Получаем результаты
                allCompleted.get();
                
                // Собираем все результаты
                List<ClosePriceProcessingResult> results = new ArrayList<>();
                for (CompletableFuture<List<ClosePriceProcessingResult>> batchFuture : shareBatchFutures) {
                    results.addAll(batchFuture.join());
                }
                
                // Агрегируем статистику
                int totalRequested = results.size();
                int newItemsSaved = (int) results.stream().filter(ClosePriceProcessingResult::isSaved).count();
                int existingItemsSkipped = (int) results.stream().filter(ClosePriceProcessingResult::isExisting).count();
                int errorCount = (int) results.stream().filter(ClosePriceProcessingResult::hasError).count();
                
                List<Map<String, Object>> savedItems = results.stream()
                    .filter(ClosePriceProcessingResult::isSaved)
                    .map(ClosePriceProcessingResult::getSavedItem)
                    .collect(Collectors.toList());
                
                String message = String.format("Пакетная обработка акций завершена. Загружено %d новых цен закрытия из %d найденных. Ошибок: %d", 
                    newItemsSaved, totalRequested, errorCount);
                
                SaveResponseDto result = new SaveResponseDto(
                    true,
                    message,
                    totalRequested,
                    newItemsSaved,
                    existingItemsSkipped,
                    errorCount,
                    0, // missingFromApi
                    savedItems
                );
                
                System.out.println("[" + taskId + "] Асинхронное сохранение цен закрытия для акций завершено");
                System.out.println("[" + taskId + "] Результат: " + result.getMessage());
                
                return result;
                
            } catch (Exception e) {
                System.err.println("[" + taskId + "] Ошибка асинхронного сохранения цен закрытия для акций: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Ошибка асинхронного сохранения цен закрытия для акций: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Асинхронное сохранение цен закрытия для фьючерсов
     */
    public CompletableFuture<SaveResponseDto> saveFuturesClosePricesAsync(String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("[" + taskId + "] Начало асинхронного сохранения цен закрытия для фьючерсов с пакетной обработкой");
                
                // Получаем все фьючерсы (блокирующий запрос остается)
                List<FutureEntity> futures = futureRepo.findAll();
                
                System.out.println("[" + taskId + "] Найдено фьючерсов: " + futures.size());
                
                // Фильтруем только RUB инструменты
                List<FutureEntity> rubFutures = futures.stream()
                    .filter(future -> "RUB".equalsIgnoreCase(future.getCurrency()))
                    .collect(Collectors.toList());
                
                System.out.println("[" + taskId + "] RUB фьючерсов: " + rubFutures.size());
                
                // Разбиваем на пакеты
                int batchSize = batchProcessingProperties.getBatchSize();
                List<List<FutureEntity>> futureBatches = createBatches(rubFutures, batchSize);
                
                System.out.println("[" + taskId + "] Создано пакетов фьючерсов: " + futureBatches.size());
                
                // Обрабатываем пакеты фьючерсов
                List<CompletableFuture<List<ClosePriceProcessingResult>>> futureBatchFutures = new ArrayList<>();
                for (int i = 0; i < futureBatches.size(); i++) {
                    final int batchIndex = i;
                    CompletableFuture<List<ClosePriceProcessingResult>> batchFuture = processFutureBatchAsync(futureBatches.get(i), taskId + "_futures_batch_" + batchIndex);
                    futureBatchFutures.add(batchFuture);
                    
                    // Добавляем задержку между пакетами
                    if (i < futureBatches.size() - 1) {
                        try {
                            Thread.sleep(batchProcessingProperties.getBatchDelayMs());
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                
                // Ждем завершения всех пакетов
                CompletableFuture<Void> allCompleted = CompletableFuture.allOf(
                    futureBatchFutures.toArray(new CompletableFuture[0])
                );
                
                // Получаем результаты
                allCompleted.get();
                
                // Собираем все результаты
                List<ClosePriceProcessingResult> results = new ArrayList<>();
                for (CompletableFuture<List<ClosePriceProcessingResult>> batchFuture : futureBatchFutures) {
                    results.addAll(batchFuture.join());
                }
                
                // Агрегируем статистику
                int totalRequested = results.size();
                int newItemsSaved = (int) results.stream().filter(ClosePriceProcessingResult::isSaved).count();
                int existingItemsSkipped = (int) results.stream().filter(ClosePriceProcessingResult::isExisting).count();
                int errorCount = (int) results.stream().filter(ClosePriceProcessingResult::hasError).count();
                
                List<Map<String, Object>> savedItems = results.stream()
                    .filter(ClosePriceProcessingResult::isSaved)
                    .map(ClosePriceProcessingResult::getSavedItem)
                    .collect(Collectors.toList());
                
                String message = String.format("Пакетная обработка фьючерсов завершена. Загружено %d новых цен закрытия из %d найденных. Ошибок: %d", 
                    newItemsSaved, totalRequested, errorCount);
                
                SaveResponseDto result = new SaveResponseDto(
                    true,
                    message,
                    totalRequested,
                    newItemsSaved,
                    existingItemsSkipped,
                    errorCount,
                    0, // missingFromApi
                    savedItems
                );
                
                System.out.println("[" + taskId + "] Асинхронное сохранение цен закрытия для фьючерсов завершено");
                System.out.println("[" + taskId + "] Результат: " + result.getMessage());
                
                return result;
                
            } catch (Exception e) {
                System.err.println("[" + taskId + "] Ошибка асинхронного сохранения цен закрытия для фьючерсов: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Ошибка асинхронного сохранения цен закрытия для фьючерсов: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Асинхронное сохранение цены закрытия для конкретного инструмента
     */
    public CompletableFuture<SaveResponseDto> saveInstrumentClosePriceAsync(String figi, String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("[" + taskId + "] Начало асинхронного сохранения цены закрытия для инструмента: " + figi);
                
                // Получаем цену закрытия из API (блокирующий запрос остается)
                List<ClosePriceDto> allClosePrices = getClosePrices(List.of(figi), null);
                
                // Фильтруем неверные цены
                List<ClosePriceDto> validClosePrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .collect(Collectors.toList());
                
                if (validClosePrices.isEmpty()) {
                    String message = "Цена закрытия не найдена для инструмента: " + figi;
                    return new SaveResponseDto(
                        false,
                        message,
                        1,
                        0,
                        0,
                        allClosePrices.size(),
                        1 - allClosePrices.size(),
                        new ArrayList<>()
                    );
                }
                
                ClosePriceDto closePriceDto = validClosePrices.get(0);
                ClosePriceProcessingResult result = processInstrumentClosePriceAsync(figi, closePriceDto);
                
                List<Map<String, Object>> savedItems = new ArrayList<>();
                if (result.isSaved()) {
                    savedItems.add(result.getSavedItem());
                }
                
                String message = result.isSaved() 
                    ? "Цена закрытия успешно загружена для инструмента: " + figi
                    : result.isExisting() 
                        ? "Цена закрытия уже существует для инструмента: " + figi
                        : "Ошибка загрузки цены закрытия для инструмента: " + figi;
                
                SaveResponseDto response = new SaveResponseDto(
                    result.isSaved() || result.isExisting(),
                    message,
                    1,
                    result.isSaved() ? 1 : 0,
                    result.isExisting() ? 1 : 0,
                    result.hasError() ? 1 : 0,
                    0,
                    savedItems
                );
                
                System.out.println("[" + taskId + "] Асинхронное сохранение цены закрытия для инструмента завершено");
                System.out.println("[" + taskId + "] Результат: " + response.getMessage());
                
                return response;
                
            } catch (Exception e) {
                System.err.println("[" + taskId + "] Ошибка асинхронного сохранения цены закрытия для инструмента " + figi + ": " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Ошибка асинхронного сохранения цены закрытия для инструмента: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Асинхронное сохранение цен основной сессии за дату
     */
    public CompletableFuture<SaveResponseDto> saveMainSessionPricesForDateAsync(LocalDate date, String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("[" + taskId + "] Начало асинхронного сохранения цен основной сессии за дату: " + date);
                
                // Проверяем выходной день
                if (isWeekend(date)) {
                    String message = "В выходные дни основная сессия не проводится. Дата: " + date;
                    return new SaveResponseDto(
                        false,
                        message,
                        0,
                        0,
                        0,
                        0,
                        0,
                        new ArrayList<>()
                    );
                }
                
                // Получаем все акции и фьючерсы (блокирующий запрос остается)
        List<ShareEntity> shares = shareRepo.findAll();
                List<FutureEntity> futures = futureRepo.findAll();
                
                System.out.println("[" + taskId + "] Найдено акций: " + shares.size() + ", фьючерсов: " + futures.size());
                
                // Параллельная обработка акций
                List<CompletableFuture<ClosePriceProcessingResult>> shareFutures = shares.stream()
                    .map(share -> CompletableFuture.supplyAsync(() -> processShareMainSessionPriceAsync(share, date), executorService))
                    .collect(Collectors.toList());
                
                // Параллельная обработка фьючерсов
                List<CompletableFuture<ClosePriceProcessingResult>> futureFutures = futures.stream()
                    .map(future -> CompletableFuture.supplyAsync(() -> processFutureMainSessionPriceAsync(future, date), executorService))
                    .collect(Collectors.toList());
                
                // Объединяем все futures
                List<CompletableFuture<ClosePriceProcessingResult>> allFutures = new ArrayList<>();
                allFutures.addAll(shareFutures);
                allFutures.addAll(futureFutures);
                
                // Ждем завершения всех операций
                CompletableFuture<Void> allCompleted = CompletableFuture.allOf(
                    allFutures.toArray(new CompletableFuture[0])
                );
                
                // Получаем результаты
                List<ClosePriceProcessingResult> results = allCompleted.thenApply(v ->
                    allFutures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                ).join();
                
                // Агрегируем статистику
                int totalRequested = results.size();
                int newItemsSaved = (int) results.stream().filter(ClosePriceProcessingResult::isSaved).count();
                int existingItemsSkipped = (int) results.stream().filter(ClosePriceProcessingResult::isExisting).count();
                int errorCount = (int) results.stream().filter(ClosePriceProcessingResult::hasError).count();
                int missingFromApi = (int) results.stream().filter(r -> !r.isSaved() && !r.isExisting() && !r.hasError()).count();
                
                List<Map<String, Object>> savedItems = results.stream()
                    .filter(ClosePriceProcessingResult::isSaved)
                    .map(ClosePriceProcessingResult::getSavedItem)
                    .collect(Collectors.toList());
                
                String message = String.format("Успешно загружено %d новых цен основной сессии из %d найденных. Ошибок: %d", 
                    newItemsSaved, totalRequested, errorCount);
                
                SaveResponseDto result = new SaveResponseDto(
                    true,
                    message,
                    totalRequested,
                    newItemsSaved,
                    existingItemsSkipped,
                    errorCount,
                    missingFromApi,
                    savedItems
                );
                
                System.out.println("[" + taskId + "] Асинхронное сохранение цен основной сессии завершено");
                System.out.println("[" + taskId + "] Результат: " + result.getMessage());
                
                return result;
                
            } catch (Exception e) {
                System.err.println("[" + taskId + "] Ошибка асинхронного сохранения цен основной сессии: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Ошибка асинхронного сохранения цен основной сессии: " + e.getMessage(), e);
            }
        });
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    /**
     * Разбивает список инструментов на пакеты заданного размера
     */
    private <T> List<List<T>> createBatches(List<T> items, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < items.size(); i += batchSize) {
            int end = Math.min(i + batchSize, items.size());
            batches.add(items.subList(i, end));
        }
        return batches;
    }

    /**
     * Обрабатывает пакет акций асинхронно
     */
    private CompletableFuture<List<ClosePriceProcessingResult>> processShareBatchAsync(List<ShareEntity> shareBatch, String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("[" + taskId + "] Обработка пакета акций: " + shareBatch.size() + " инструментов");
            
            List<CompletableFuture<ClosePriceProcessingResult>> futures = shareBatch.stream()
                .map(share -> CompletableFuture.supplyAsync(() -> processShareClosePriceAsync(share), executorService))
                .collect(Collectors.toList());
            
            // Ждем завершения всех операций в пакете
            CompletableFuture<Void> allCompleted = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            try {
                allCompleted.get(); // Ждем завершения
                
                // Собираем результаты
                List<ClosePriceProcessingResult> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
                
                System.out.println("[" + taskId + "] Пакет акций обработан: " + results.size() + " результатов");
                return results;
                
            } catch (Exception e) {
                System.err.println("[" + taskId + "] Ошибка обработки пакета акций: " + e.getMessage());
                return new ArrayList<>();
            }
        }, executorService);
    }

    /**
     * Обрабатывает пакет фьючерсов асинхронно
     */
    private CompletableFuture<List<ClosePriceProcessingResult>> processFutureBatchAsync(List<FutureEntity> futureBatch, String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("[" + taskId + "] Обработка пакета фьючерсов: " + futureBatch.size() + " инструментов");
            
            List<CompletableFuture<ClosePriceProcessingResult>> futures = futureBatch.stream()
                .map(future -> CompletableFuture.supplyAsync(() -> processFutureClosePriceAsync(future), executorService))
                .collect(Collectors.toList());
            
            // Ждем завершения всех операций в пакете
            CompletableFuture<Void> allCompleted = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            try {
                allCompleted.get(); // Ждем завершения
                
                // Собираем результаты
                List<ClosePriceProcessingResult> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
                
                System.out.println("[" + taskId + "] Пакет фьючерсов обработан: " + results.size() + " результатов");
                return results;
                
            } catch (Exception e) {
                System.err.println("[" + taskId + "] Ошибка обработки пакета фьючерсов: " + e.getMessage());
                return new ArrayList<>();
            }
        }, executorService);
    }

    /**
     * Обработка цены закрытия для акции
     */
    private ClosePriceProcessingResult processShareClosePriceAsync(ShareEntity share) {
        try {
            // Получаем цену закрытия из API с rate limiting
            List<ClosePriceDto> allClosePrices = retryService.executeWithRetryAndRateLimit(
                () -> getClosePricesDirectly(List.of(share.getFigi()), null),
                "processShareClosePrice_" + share.getFigi(),
                "close_prices",
                rateLimitService
            );
            
            // Фильтруем неверные цены
            List<ClosePriceDto> validClosePrices = allClosePrices.stream()
                .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                .collect(Collectors.toList());
            
            if (validClosePrices.isEmpty()) {
                return new ClosePriceProcessingResult(share.getFigi(), false, false, true, "Цена закрытия не найдена", null);
            }
            
            ClosePriceDto closePriceDto = validClosePrices.get(0);
            LocalDate priceDate = LocalDate.parse(closePriceDto.tradingDate());
            ClosePriceKey key = new ClosePriceKey(priceDate, closePriceDto.figi());
            
            // Проверяем, есть ли уже запись
            if (closePriceRepo.existsById(key)) {
                return new ClosePriceProcessingResult(share.getFigi(), false, true, false, "Цена закрытия уже существует", null);
            }
            
            // Создаем запись для сохранения
            ClosePriceEntity entity = new ClosePriceEntity(
                priceDate,
                closePriceDto.figi(),
                "SHARE",
                closePriceDto.closePrice(),
                share.getCurrency(),
                share.getExchange()
            );
            
            closePriceRepo.save(entity);
            
            Map<String, Object> savedItem = new HashMap<>();
            savedItem.put("figi", closePriceDto.figi());
            savedItem.put("ticker", share.getTicker());
            savedItem.put("name", share.getName());
            savedItem.put("priceDate", priceDate.toString());
            savedItem.put("closePrice", closePriceDto.closePrice());
            savedItem.put("instrumentType", "SHARE");
            savedItem.put("currency", share.getCurrency());
            savedItem.put("exchange", share.getExchange());
            
            return new ClosePriceProcessingResult(share.getFigi(), true, false, false, null, savedItem);
            
        } catch (Exception e) {
            System.err.println("Ошибка обработки цены закрытия для акции " + share.getFigi() + ": " + e.getMessage());
            return new ClosePriceProcessingResult(share.getFigi(), false, false, true, e.getMessage(), null);
        }
    }

    /**
     * Обработка цены закрытия для фьючерса
     */
    private ClosePriceProcessingResult processFutureClosePriceAsync(FutureEntity future) {
        try {
            // Получаем цену закрытия из API с rate limiting
            List<ClosePriceDto> allClosePrices = retryService.executeWithRetryAndRateLimit(
                () -> getClosePricesDirectly(List.of(future.getFigi()), null),
                "processFutureClosePrice_" + future.getFigi(),
                "close_prices",
                rateLimitService
            );
            
            // Фильтруем неверные цены
            List<ClosePriceDto> validClosePrices = allClosePrices.stream()
                .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                .collect(Collectors.toList());
            
            if (validClosePrices.isEmpty()) {
                return new ClosePriceProcessingResult(future.getFigi(), false, false, true, "Цена закрытия не найдена", null);
            }
            
            ClosePriceDto closePriceDto = validClosePrices.get(0);
            LocalDate priceDate = LocalDate.parse(closePriceDto.tradingDate());
            ClosePriceKey key = new ClosePriceKey(priceDate, closePriceDto.figi());
            
            // Проверяем, есть ли уже запись
            if (closePriceRepo.existsById(key)) {
                return new ClosePriceProcessingResult(future.getFigi(), false, true, false, "Цена закрытия уже существует", null);
            }
            
            // Создаем запись для сохранения
            ClosePriceEntity entity = new ClosePriceEntity(
                priceDate,
                closePriceDto.figi(),
                "FUTURE",
                closePriceDto.closePrice(),
                future.getCurrency(),
                future.getExchange()
            );
            
            closePriceRepo.save(entity);
            
            Map<String, Object> savedItem = new HashMap<>();
            savedItem.put("figi", closePriceDto.figi());
            savedItem.put("ticker", future.getTicker());
            savedItem.put("name", future.getTicker());
            savedItem.put("priceDate", priceDate.toString());
            savedItem.put("closePrice", closePriceDto.closePrice());
            savedItem.put("instrumentType", "FUTURE");
            savedItem.put("currency", future.getCurrency());
            savedItem.put("exchange", future.getExchange());
            
            return new ClosePriceProcessingResult(future.getFigi(), true, false, false, null, savedItem);
            
        } catch (Exception e) {
            System.err.println("Ошибка обработки цены закрытия для фьючерса " + future.getFigi() + ": " + e.getMessage());
            return new ClosePriceProcessingResult(future.getFigi(), false, false, true, e.getMessage(), null);
        }
    }

    /**
     * Обработка цены закрытия для конкретного инструмента
     */
    private ClosePriceProcessingResult processInstrumentClosePriceAsync(String figi, ClosePriceDto closePriceDto) {
        try {
            LocalDate priceDate = LocalDate.parse(closePriceDto.tradingDate());
            ClosePriceKey key = new ClosePriceKey(priceDate, closePriceDto.figi());
            
            // Проверяем, есть ли уже запись
            if (closePriceRepo.existsById(key)) {
                return new ClosePriceProcessingResult(figi, false, true, false, "Цена закрытия уже существует", null);
            }
            
            // Определяем тип инструмента
            String instrumentType = "UNKNOWN";
            String currency = "UNKNOWN";
            String exchange = "UNKNOWN";
            String ticker = figi;
            String name = figi;
            
            // Проверяем в кэше акций
            ShareEntity share = shareRepo.findById(figi).orElse(null);
            if (share != null) {
                instrumentType = "SHARE";
                currency = share.getCurrency();
                exchange = share.getExchange();
                ticker = share.getTicker();
                name = share.getName();
            } else {
                // Проверяем в кэше фьючерсов
                FutureEntity future = futureRepo.findById(figi).orElse(null);
                if (future != null) {
                    instrumentType = "FUTURE";
                    currency = future.getCurrency();
                    exchange = future.getExchange();
                    ticker = future.getTicker();
                    name = future.getTicker();
                } else {
                    // Проверяем в кэше индикативов
                    IndicativeEntity indicative = indicativeRepo.findById(figi).orElse(null);
                    if (indicative != null) {
                        instrumentType = "INDICATIVE";
                        currency = indicative.getCurrency();
                        exchange = indicative.getExchange();
                        ticker = indicative.getTicker();
                        name = indicative.getTicker();
                    }
                }
            }
            
            // Создаем запись для сохранения
            ClosePriceEntity entity = new ClosePriceEntity(
                priceDate,
                closePriceDto.figi(),
                instrumentType,
                closePriceDto.closePrice(),
                currency,
                exchange
            );
            
            closePriceRepo.save(entity);
            
            Map<String, Object> savedItem = new HashMap<>();
            savedItem.put("figi", closePriceDto.figi());
            savedItem.put("ticker", ticker);
            savedItem.put("name", name);
            savedItem.put("priceDate", priceDate.toString());
            savedItem.put("closePrice", closePriceDto.closePrice());
            savedItem.put("instrumentType", instrumentType);
            savedItem.put("currency", currency);
            savedItem.put("exchange", exchange);
            
            return new ClosePriceProcessingResult(figi, true, false, false, null, savedItem);
            
        } catch (Exception e) {
            System.err.println("Ошибка обработки цены закрытия для инструмента " + figi + ": " + e.getMessage());
            return new ClosePriceProcessingResult(figi, false, false, true, e.getMessage(), null);
        }
    }

    /**
     * Обработка цены основной сессии для акции
     */
    private ClosePriceProcessingResult processShareMainSessionPriceAsync(ShareEntity share, LocalDate date) {
        try {
            ClosePriceKey key = new ClosePriceKey(date, share.getFigi());
            
            // Проверяем, есть ли уже запись
            if (closePriceRepo.existsById(key)) {
                return new ClosePriceProcessingResult(share.getFigi(), false, true, false, "Цена основной сессии уже существует", null);
            }
            
            // Ищем последнюю свечу за указанную дату
            BigDecimal lastClosePrice = findLastClosePriceFromMinuteCandles(share.getFigi(), date);
            
            if (lastClosePrice == null) {
                return new ClosePriceProcessingResult(share.getFigi(), false, false, false, "Свеча не найдена", null);
            }
            
            // Создаем запись для сохранения
            ClosePriceEntity entity = new ClosePriceEntity(
                date,
                share.getFigi(),
                "SHARE",
                lastClosePrice,
                "RUB",
                "moex_mrng_evng_e_wknd_dlr"
            );
            
            closePriceRepo.save(entity);
            
            Map<String, Object> savedItem = new HashMap<>();
            savedItem.put("figi", share.getFigi());
            savedItem.put("ticker", share.getTicker());
            savedItem.put("name", share.getName());
            savedItem.put("priceDate", date.toString());
            savedItem.put("closePrice", lastClosePrice);
            savedItem.put("instrumentType", "SHARE");
            savedItem.put("currency", "RUB");
            savedItem.put("exchange", "moex_mrng_evng_e_wknd_dlr");
            
            return new ClosePriceProcessingResult(share.getFigi(), true, false, false, null, savedItem);
            
        } catch (Exception e) {
            System.err.println("Ошибка обработки цены основной сессии для акции " + share.getFigi() + ": " + e.getMessage());
            return new ClosePriceProcessingResult(share.getFigi(), false, false, true, e.getMessage(), null);
        }
    }

    /**
     * Обработка цены основной сессии для фьючерса
     */
    private ClosePriceProcessingResult processFutureMainSessionPriceAsync(FutureEntity future, LocalDate date) {
        try {
            ClosePriceKey key = new ClosePriceKey(date, future.getFigi());
            
            // Проверяем, есть ли уже запись
            if (closePriceRepo.existsById(key)) {
                return new ClosePriceProcessingResult(future.getFigi(), false, true, false, "Цена основной сессии уже существует", null);
            }
            
            // Ищем последнюю свечу за указанную дату
            BigDecimal lastClosePrice = findLastClosePriceFromMinuteCandles(future.getFigi(), date);
            
            if (lastClosePrice == null) {
                return new ClosePriceProcessingResult(future.getFigi(), false, false, false, "Свеча не найдена", null);
            }
            
            // Создаем запись для сохранения
            ClosePriceEntity entity = new ClosePriceEntity(
                date,
                future.getFigi(),
                "FUTURE",
                lastClosePrice,
                "RUB",
                "FORTS_MAIN"
            );
            
            closePriceRepo.save(entity);
            
            Map<String, Object> savedItem = new HashMap<>();
            savedItem.put("figi", future.getFigi());
            savedItem.put("ticker", future.getTicker());
            savedItem.put("name", future.getTicker());
            savedItem.put("priceDate", date.toString());
            savedItem.put("closePrice", lastClosePrice);
            savedItem.put("instrumentType", "FUTURE");
            savedItem.put("currency", "RUB");
            savedItem.put("exchange", "FORTS_MAIN");
            
            return new ClosePriceProcessingResult(future.getFigi(), true, false, false, null, savedItem);
            
        } catch (Exception e) {
            System.err.println("Ошибка обработки цены основной сессии для фьючерса " + future.getFigi() + ": " + e.getMessage());
            return new ClosePriceProcessingResult(future.getFigi(), false, false, true, e.getMessage(), null);
        }
    }

    /**
     * Проверяет, является ли дата выходным днем
     */
    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek().getValue() >= 6; // Суббота = 6, Воскресенье = 7
    }

    /**
     * Находит последнюю цену закрытия для указанной даты и FIGI из minute_candles
     */
    private BigDecimal findLastClosePriceFromMinuteCandles(String figi, LocalDate date) {
        try {
            System.out.println("Поиск свечи для " + figi + " за дату (MSK): " + date);

            // Формируем точные границы суток в часовом поясе Europe/Moscow
            ZoneId mskZone = ZoneId.of("Europe/Moscow");
            var startInstant = date.atStartOfDay(mskZone).toInstant();
            var endInstant = date.plusDays(1).atStartOfDay(mskZone).toInstant();

            // Ищем свечи по точному временному диапазону и берем последнюю по времени
            List<MinuteCandleEntity> candles = minuteCandleRepo.findByFigiAndTimeBetween(figi, startInstant, endInstant);

            if (!candles.isEmpty()) {
                MinuteCandleEntity lastCandle = candles.get(candles.size() - 1);
                System.out.println("Найдена свеча (" + lastCandle.getTime() + ") для " + figi + " с ценой закрытия: " + lastCandle.getClose());
                return lastCandle.getClose();
            } else {
                System.out.println("Свечи не найдены для " + figi + " за дату: " + date);
            }

            return null;

        } catch (Exception e) {
            System.err.println("Ошибка поиска последней цены закрытия для " + figi + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Результат обработки цены закрытия
     */
    private static class ClosePriceProcessingResult {
        private final String figi;
        private final boolean saved;
        private final boolean existing;
        private final boolean hasError;
        private final String errorMessage;
        private final Map<String, Object> savedItem;

        public ClosePriceProcessingResult(String figi, boolean saved, boolean existing, boolean hasError, String errorMessage, Map<String, Object> savedItem) {
            this.figi = figi;
            this.saved = saved;
            this.existing = existing;
            this.hasError = hasError;
            this.errorMessage = errorMessage;
            this.savedItem = savedItem;
        }

        public String getFigi() { return figi; }
        public boolean isSaved() { return saved; }
        public boolean isExisting() { return existing; }
        public boolean hasError() { return hasError; }
        public String getErrorMessage() { return errorMessage; }
        public Map<String, Object> getSavedItem() { return savedItem; }
    }

}
