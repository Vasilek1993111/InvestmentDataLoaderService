package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.ClosePriceEveningSessionEntity;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.repository.ClosePriceEveningSessionRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EveningSessionService {

    private final MainSessionPriceService mainSessionPriceService;
    private final ShareRepository shareRepo;
    private final FutureRepository futureRepo;
    private final ClosePriceEveningSessionRepository closePriceEveningSessionRepo;

    public EveningSessionService(MainSessionPriceService mainSessionPriceService,
                               ShareRepository shareRepo,
                               FutureRepository futureRepo,
                               ClosePriceEveningSessionRepository closePriceEveningSessionRepo) {
        this.mainSessionPriceService = mainSessionPriceService;
        this.shareRepo = shareRepo;
        this.futureRepo = futureRepo;
        this.closePriceEveningSessionRepo = closePriceEveningSessionRepo;
    }

    /**
     * Сохранение цен вечерней сессии
     */
    public SaveResponseDto saveClosePricesEveningSession(ClosePriceEveningSessionRequestDto request) {
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
                "Нет инструментов для загрузки цен вечерней сессии. В базе данных нет акций в рублях или фьючерсов в рублях.",
                0, 0, 0, 0, 0, new ArrayList<>()
            );
        }

        // Получаем цены закрытия из API по частям (только shares+futures)
        List<ClosePriceDto> closePricesFromApi = new ArrayList<>();
        int requestedInstrumentsCount = instrumentIds.size();
        
        try {
            // Батчим запросы по 100 инструментов, чтобы избежать лимитов API
            int batchSize = 100;
            System.out.println("Запрашиваем цены закрытия для evening session для " + instrumentIds.size() + " инструментов батчами по " + batchSize);
            for (int i = 0; i < instrumentIds.size(); i += batchSize) {
                int toIndex = Math.min(i + batchSize, instrumentIds.size());
                List<String> batch = instrumentIds.subList(i, toIndex);
                List<ClosePriceDto> prices = mainSessionPriceService.getClosePrices(batch, null);
                closePricesFromApi.addAll(prices);
                System.out.println("Получено цен для батча evening session (" + batch.size() + "): " + prices.size());
                try { Thread.sleep(200); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }

        } catch (Exception e) {
            System.err.println("Ошибка при получении цен закрытия из API для evening session: " + e.getMessage());
            System.err.println("Количество инструментов в запросе: " + instrumentIds.size());
            
            return new SaveResponseDto(
                false,
                "Ошибка при получении цен закрытия из API для evening session: " + e.getMessage() + 
                ". Количество инструментов: " + instrumentIds.size(),
                0, 0, 0, 0, 0, new ArrayList<>()
            );
        }
        
        // Фильтруем неверные цены и получаем статистику
        ClosePriceProcessingResult processingResult = filterValidPricesWithStats(closePricesFromApi);
        List<ClosePriceDto> closePrices = processingResult.getValidPrices();
        int invalidPricesFiltered = processingResult.getInvalidPricesFiltered();
        
        List<ClosePriceEveningSessionDto> savedPrices = new ArrayList<>();
        int existingCount = 0;
        int failedSavesCount = 0;
        
        System.out.println("=== НАЧАЛО ОБРАБОТКИ ЦЕН ВЕЧЕРНЕЙ СЕССИИ ===");
        System.out.println("Всего получено цен из API: " + closePricesFromApi.size());
        System.out.println("Отфильтровано неверных цен (1970-01-01): " + invalidPricesFiltered);
        System.out.println("Валидных цен для обработки: " + closePrices.size());
        System.out.println("Запрашивалось инструментов: " + requestedInstrumentsCount);
        
        for (ClosePriceDto closePriceDto : closePrices) {
            // Используем eveningSessionPrice, если она есть, иначе closePrice
            BigDecimal eveningPrice = closePriceDto.eveningSessionPrice() != null ? 
                closePriceDto.eveningSessionPrice() : closePriceDto.closePrice();
            
            if (eveningPrice == null) {
                System.out.println("Пропускаем " + closePriceDto.figi() + " - нет цены вечерней сессии");
                continue;
            }
            
            LocalDate priceDate = LocalDate.parse(closePriceDto.tradingDate());
            
            System.out.println("Обрабатываем evening session: " + closePriceDto.figi() + " на дату: " + priceDate + " цена: " + eveningPrice);

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

            // Проверяем, существует ли уже запись
            if (closePriceEveningSessionRepo.existsByPriceDateAndFigi(priceDate, closePriceDto.figi())) {
                System.out.println("Запись evening session уже существует для " + closePriceDto.figi() + " на дату " + priceDate + ", пропускаем");
                existingCount++;
                continue;
            }

            try {
                // Создаем новую запись
                ClosePriceEveningSessionEntity entity = new ClosePriceEveningSessionEntity();
                entity.setPriceDate(priceDate);
                entity.setFigi(closePriceDto.figi());
                entity.setClosePrice(eveningPrice);
                entity.setInstrumentType(instrumentType);
                entity.setCurrency(currency);
                entity.setExchange(exchange);

                closePriceEveningSessionRepo.save(entity);
                
                ClosePriceEveningSessionDto dto = new ClosePriceEveningSessionDto(
                    priceDate,
                    closePriceDto.figi(),
                    eveningPrice,
                    instrumentType,
                    currency,
                    exchange
                );
                savedPrices.add(dto);
                
                System.out.println("Сохранена цена вечерней сессии для " + closePriceDto.figi() + ": " + eveningPrice);

            } catch (DataIntegrityViolationException e) {
                System.err.println("Ошибка целостности данных при сохранении evening session для " + closePriceDto.figi() + ": " + e.getMessage());
                failedSavesCount++;
            } catch (Exception e) {
                System.err.println("Неожиданная ошибка при сохранении evening session для " + closePriceDto.figi() + ": " + e.getMessage());
                failedSavesCount++;
            }
        }

        int newItemsSaved = savedPrices.size();
        int totalRequested = requestedInstrumentsCount;
        int missingFromApi = totalRequested - closePrices.size();
        
        System.out.println("=== ИТОГИ ОБРАБОТКИ ЦЕН ВЕЧЕРНЕЙ СЕССИИ ===");
        System.out.println("Запрашивалось инструментов: " + totalRequested);
        System.out.println("Получено из API: " + closePrices.size());
        System.out.println("Отфильтровано неверных: " + invalidPricesFiltered);
        System.out.println("Новых записей сохранено: " + newItemsSaved);
        System.out.println("Уже существовало: " + existingCount);
        System.out.println("Ошибок сохранения: " + failedSavesCount);
        System.out.println("Не получено из API: " + missingFromApi);

        boolean success = failedSavesCount == 0;
        String message = success ? 
            "Цены вечерней сессии успешно сохранены" : 
            "Цены вечерней сессии сохранены с ошибками";

        return new SaveResponseDto(
            success,
            message,
            totalRequested,
            newItemsSaved,
            existingCount,
            invalidPricesFiltered,
            missingFromApi,
            savedPrices
        );
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
}
