package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.ClosePriceEntity;
import com.example.InvestmentDataLoaderService.entity.ClosePriceKey;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.entity.IndicativeEntity;
import com.example.InvestmentDataLoaderService.repository.ClosePriceRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.IndicativeRepository;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.*;
import org.springframework.dao.DataIntegrityViolationException;
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc.MarketDataServiceBlockingStub;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClosePriceService {

    private final MarketDataServiceBlockingStub marketDataService;
    private final ShareRepository shareRepo;
    private final FutureRepository futureRepo;
    private final ClosePriceRepository closePriceRepo;
    private final IndicativeRepository indicativeRepo;

    public ClosePriceService(MarketDataServiceBlockingStub marketDataService,
                            ShareRepository shareRepo,
                            FutureRepository futureRepo,
                            ClosePriceRepository closePriceRepo,
                            IndicativeRepository indicativeRepo) {
        this.marketDataService = marketDataService;
        this.shareRepo = shareRepo;
        this.futureRepo = futureRepo;
        this.closePriceRepo = closePriceRepo;
        this.indicativeRepo = indicativeRepo;
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
    private ClosePriceProcessingResult filterValidPricesWithStats(List<ClosePriceDto> prices) {
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
        
        return new ClosePriceProcessingResult(validPrices, invalidPricesCount, prices.size());
    }

    public List<ClosePriceDto> getClosePrices(List<String> instrumentIds, String status) {
        GetClosePricesRequest.Builder builder = GetClosePricesRequest.newBuilder();
        
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
        
        // Фильтруем неверные цены с датой 1970-01-01
        return filterValidPrices(list);
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
        ClosePriceProcessingResult processingResult = filterValidPricesWithStats(closePricesFromApi);
        List<ClosePriceDto> closePrices = processingResult.getValidPrices();
        int invalidPricesFiltered = processingResult.getInvalidPricesFiltered();
        
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
}
