package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.dto.ShareDto;
import com.example.InvestmentDataLoaderService.dto.FutureDto;
import com.example.InvestmentDataLoaderService.dto.IndicativeDto;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.MinuteCandleEntity;
import com.example.InvestmentDataLoaderService.entity.ClosePriceEntity;
import com.example.InvestmentDataLoaderService.entity.ClosePriceKey;
import com.example.InvestmentDataLoaderService.exception.DataLoadException;
import com.example.InvestmentDataLoaderService.service.MainSessionPriceService;
import com.example.InvestmentDataLoaderService.service.InstrumentService;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.MinuteCandleRepository;
import com.example.InvestmentDataLoaderService.repository.ClosePriceRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Контроллер для работы с ценами основной сессии
 * Управляет загрузкой цен основной торговой сессии и цен закрытия
 */
@RestController
@RequestMapping("/api/main-session-prices")
public class MainSessionPricesController {

    private final MainSessionPriceService mainSessionPriceService;
    private final InstrumentService instrumentService;
    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final MinuteCandleRepository minuteCandleRepository;
    private final ClosePriceRepository closePriceRepository;

    public MainSessionPricesController(MainSessionPriceService mainSessionPriceService,
                                      InstrumentService instrumentService,
                                      ShareRepository shareRepository,
                                      FutureRepository futureRepository,
                                      MinuteCandleRepository minuteCandleRepository,
                                      ClosePriceRepository closePriceRepository) {
        this.mainSessionPriceService = mainSessionPriceService;
        this.instrumentService = instrumentService;
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.minuteCandleRepository = minuteCandleRepository;
        this.closePriceRepository = closePriceRepository;
    }

    // ==================== ЦЕНЫ ЗАКРЫТИЯ ОСНОВНОЙ СЕССИИ ====================

    /**
     * Загрузка цен закрытия основной сессии для акций и фьючерсов из T-INVEST API в БД
     * Работает с акциями и фьючерсами одновременно, использует кэш и T-INVEST API
     */
    @PostMapping("/")
    @Transactional
    public ResponseEntity<Map<String, Object>> loadClosePricesToday() {
        try {
            System.out.println("=== ЗАГРУЗКА ЦЕН ЗАКРЫТИЯ ДЛЯ АКЦИЙ И ФЬЮЧЕРСОВ ===");
            
            // Получаем все акции из кэша
            List<ShareDto> shares = instrumentService.getShares(null, null, "RUB", null, null);
            System.out.println("Найдено акций в кэше: " + shares.size());
            
            // Получаем все фьючерсы из кэша
            List<FutureDto> futures = instrumentService.getFutures(null, null, "RUB", null, null);
            System.out.println("Найдено фьючерсов в кэше: " + futures.size());
            
            if (shares.isEmpty() && futures.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Не найдено акций и фьючерсов в кэше");
                response.put("totalRequested", 0);
                response.put("newItemsSaved", 0);
                response.put("existingItemsSkipped", 0);
                response.put("invalidItemsFiltered", 0);
                response.put("missingFromApi", 0);
                response.put("savedItems", new ArrayList<>());
                response.put("timestamp", LocalDateTime.now().toString());
                
                return ResponseEntity.ok(response);
            }
            
            // Собираем FIGI всех инструментов в рублях
            List<String> allFigis = new ArrayList<>();
            for (ShareDto share : shares) {
                if ("RUB".equalsIgnoreCase(share.currency())) {
                    allFigis.add(share.figi());
                }
            }
            for (FutureDto future : futures) {
                if ("RUB".equalsIgnoreCase(future.currency())) {
                    allFigis.add(future.figi());
                }
            }
            
            if (allFigis.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Не найдено инструментов в рублях в кэше");
                response.put("totalRequested", 0);
                response.put("newItemsSaved", 0);
                response.put("existingItemsSkipped", 0);
                response.put("invalidItemsFiltered", 0);
                response.put("missingFromApi", 0);
                response.put("savedItems", new ArrayList<>());
                response.put("timestamp", LocalDateTime.now().toString());
                
                return ResponseEntity.ok(response);
            }
            
            // Получаем цены закрытия из T-INVEST API
            List<ClosePriceDto> allClosePrices = mainSessionPriceService.getClosePrices(allFigis, null);
            
            // Фильтруем неверные цены (с датой 1970-01-01)
            List<ClosePriceDto> validClosePrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .collect(Collectors.toList());
            
            System.out.println("Получено цен из API: " + allClosePrices.size());
            System.out.println("Валидных цен после фильтрации: " + validClosePrices.size());
            
            List<Map<String, Object>> savedItems = new ArrayList<>();
            int totalRequested = allFigis.size();
            int newItemsSaved = 0;
            int existingItemsSkipped = 0;
            int invalidItemsFiltered = allClosePrices.size() - validClosePrices.size();
            int missingFromApi = totalRequested - allClosePrices.size();
            
            // Сохраняем валидные цены в БД
            for (ClosePriceDto closePriceDto : validClosePrices) {
                try {
                    LocalDate priceDate = LocalDate.parse(closePriceDto.tradingDate());
                    ClosePriceKey key = new ClosePriceKey(priceDate, closePriceDto.figi());
                    
                    // Проверяем, есть ли уже запись для этой даты и FIGI
                    if (closePriceRepository.existsById(key)) {
                        existingItemsSkipped++;
                        System.out.println("Запись уже существует для " + closePriceDto.figi() + " за " + priceDate);
                        continue;
                    }
                    
                    // Определяем тип инструмента и получаем дополнительную информацию из кэша
                    String instrumentType = "UNKNOWN";
                    String currency = "UNKNOWN";
                    String exchange = "UNKNOWN";
                    String ticker = closePriceDto.figi();
                    String name = closePriceDto.figi();
                    
                    // Проверяем в кэше акций
                    ShareDto share = instrumentService.getShareByFigi(closePriceDto.figi());
                    if (share != null) {
                        instrumentType = "SHARE";
                        currency = share.currency();
                        exchange = share.exchange();
                        ticker = share.ticker();
                        name = share.name();
                    } else {
                        // Проверяем в кэше фьючерсов
                        FutureDto future = instrumentService.getFutureByFigi(closePriceDto.figi());
                        if (future != null) {
                            instrumentType = "FUTURE";
                            currency = future.currency();
                            exchange = future.exchange();
                            ticker = future.ticker();
                            name = future.ticker();
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
                    
                    closePriceRepository.save(entity);
                    
                    Map<String, Object> savedItem = new HashMap<>();
                    savedItem.put("figi", closePriceDto.figi());
                    savedItem.put("ticker", ticker);
                    savedItem.put("name", name);
                    savedItem.put("priceDate", priceDate);
                    savedItem.put("closePrice", closePriceDto.closePrice());
                    savedItem.put("instrumentType", instrumentType);
                    savedItem.put("currency", currency);
                    savedItem.put("exchange", exchange);
                    savedItems.add(savedItem);
                    
                    newItemsSaved++;
                    System.out.println("Сохранена цена закрытия для " + ticker + " (" + closePriceDto.figi() + "): " + closePriceDto.closePrice());
                    
                } catch (Exception e) {
                    System.err.println("Ошибка обработки цены для " + closePriceDto.figi() + ": " + e.getMessage());
                    invalidItemsFiltered++;
                }
            }
            
            // Формируем ответ
            String message = String.format("Успешно загружено %d новых цен закрытия для акций и фьючерсов из %d найденных.", newItemsSaved, validClosePrices.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message);
            response.put("totalRequested", totalRequested);
            response.put("newItemsSaved", newItemsSaved);
            response.put("existingItemsSkipped", existingItemsSkipped);
            response.put("invalidItemsFiltered", invalidItemsFiltered);
            response.put("missingFromApi", missingFromApi);
            response.put("savedItems", savedItems);
            response.put("timestamp", LocalDateTime.now().toString());
            
            System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ЦЕН ЗАКРЫТИЯ ДЛЯ АКЦИЙ И ФЬЮЧЕРСОВ ===");
            System.out.println("Всего запрошено: " + totalRequested);
            System.out.println("Новых записей: " + newItemsSaved);
            System.out.println("Пропущено существующих: " + existingItemsSkipped);
            System.out.println("Отфильтровано: " + invalidItemsFiltered);
            System.out.println("Не найдено данных: " + missingFromApi);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке цен закрытия для акций и фьючерсов: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Ошибка при загрузке цен закрытия для акций и фьючерсов: " + e.getMessage());
            errorResponse.put("totalRequested", 0);
            errorResponse.put("newItemsSaved", 0);
            errorResponse.put("existingItemsSkipped", 0);
            errorResponse.put("invalidItemsFiltered", 0);
            errorResponse.put("missingFromApi", 0);
            errorResponse.put("savedItems", new ArrayList<>());
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    /**
     * Получение цен закрытия для всех акций из T-INVEST API
     */
    @GetMapping("/shares")
    public ResponseEntity<Map<String, Object>> getClosePricesForShares() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ClosePriceDto> allClosePrices = mainSessionPriceService.getClosePricesForAllShares();
            
            // Фильтруем неверные цены (с датой 1970-01-01)
            List<ClosePriceDto> validClosePrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("message", "Цены закрытия для акций получены успешно");
            response.put("data", validClosePrices);
            response.put("count", validClosePrices.size());
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Исключение будет обработано GlobalExceptionHandler
            throw new DataLoadException("Ошибка получения цен закрытия для акций: " + e.getMessage(), e);
        }
    }

    /**
     * Загрузка цен закрытия для всех акций из T-INVEST API в БД
     */
    @PostMapping("/shares")
    @Transactional
    public ResponseEntity<Map<String, Object>> loadClosePricesForShares() {
        try {
            System.out.println("=== ЗАГРУЗКА ЦЕН ЗАКРЫТИЯ ДЛЯ АКЦИЙ ===");
            
            // Получаем все акции из кэша
            List<ShareDto> shares = instrumentService.getShares(null, null, "RUB", null, null);
            System.out.println("Найдено акций в кэше: " + shares.size());
            
            if (shares.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Не найдено акций в кэше");
                response.put("totalRequested", 0);
                response.put("newItemsSaved", 0);
                response.put("existingItemsSkipped", 0);
                response.put("invalidItemsFiltered", 0);
                response.put("missingFromApi", 0);
                response.put("savedItems", new ArrayList<>());
                response.put("timestamp", LocalDateTime.now().toString());
                
                return ResponseEntity.ok(response);
            }
            
            // Получаем FIGI всех акций в рублях
            List<String> shareFigis = new ArrayList<>();
            for (ShareDto share : shares) {
                if ("RUB".equalsIgnoreCase(share.currency())) {
                    shareFigis.add(share.figi());
                }
            }
            
            if (shareFigis.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Не найдено акций в рублях в базе данных");
                response.put("totalRequested", 0);
                response.put("newItemsSaved", 0);
                response.put("existingItemsSkipped", 0);
                response.put("invalidItemsFiltered", 0);
                response.put("missingFromApi", 0);
                response.put("savedItems", new ArrayList<>());
                response.put("timestamp", LocalDateTime.now().toString());
                
                return ResponseEntity.ok(response);
            }
            
            // Получаем цены закрытия из T-INVEST API
            List<ClosePriceDto> allClosePrices = mainSessionPriceService.getClosePrices(shareFigis, null);
            
            // Фильтруем неверные цены (с датой 1970-01-01)
            List<ClosePriceDto> validClosePrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .collect(Collectors.toList());
            
            System.out.println("Получено цен из API: " + allClosePrices.size());
            System.out.println("Валидных цен после фильтрации: " + validClosePrices.size());
            
            List<Map<String, Object>> savedItems = new ArrayList<>();
            int totalRequested = shareFigis.size();
            int newItemsSaved = 0;
            int existingItemsSkipped = 0;
            int invalidItemsFiltered = allClosePrices.size() - validClosePrices.size();
            int missingFromApi = totalRequested - allClosePrices.size();
            
            // Сохраняем валидные цены в БД
            for (ClosePriceDto closePriceDto : validClosePrices) {
                try {
                    LocalDate priceDate = LocalDate.parse(closePriceDto.tradingDate());
                    ClosePriceKey key = new ClosePriceKey(priceDate, closePriceDto.figi());
                    
                    // Проверяем, есть ли уже запись для этой даты и FIGI
                    if (closePriceRepository.existsById(key)) {
                        existingItemsSkipped++;
                        System.out.println("Запись уже существует для " + closePriceDto.figi() + " за " + priceDate);
                        continue;
                    }
                    
                    // Получаем акцию из кэша для получения дополнительной информации
                    ShareDto share = instrumentService.getShareByFigi(closePriceDto.figi());
                    
                    if (share == null) {
                        System.err.println("Не найдена акция для FIGI: " + closePriceDto.figi());
                        continue;
                    }
                    
                    // Создаем запись для сохранения
                    ClosePriceEntity entity = new ClosePriceEntity(
                        priceDate,
                        closePriceDto.figi(),
                        "SHARE",
                        closePriceDto.closePrice(),
                        share.currency(),
                        share.exchange()
                    );
                    
                    closePriceRepository.save(entity);
                    
                    Map<String, Object> savedItem = new HashMap<>();
                    savedItem.put("figi", closePriceDto.figi());
                    savedItem.put("ticker", share.ticker());
                    savedItem.put("name", share.name());
                    savedItem.put("priceDate", priceDate);
                    savedItem.put("closePrice", closePriceDto.closePrice());
                    savedItem.put("instrumentType", "SHARE");
                    savedItem.put("currency", share.currency());
                    savedItem.put("exchange", share.exchange());
                    savedItems.add(savedItem);
                    
                    newItemsSaved++;
                    System.out.println("Сохранена цена закрытия для " + share.ticker() + ": " + closePriceDto.closePrice());
                    
                } catch (Exception e) {
                    System.err.println("Ошибка обработки цены для " + closePriceDto.figi() + ": " + e.getMessage());
                    invalidItemsFiltered++;
                }
            }
            
            // Формируем ответ
            String message = String.format("Успешно загружено %d новых цен закрытия для акций из %d найденных.", newItemsSaved, validClosePrices.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message);
            response.put("totalRequested", totalRequested);
            response.put("newItemsSaved", newItemsSaved);
            response.put("existingItemsSkipped", existingItemsSkipped);
            response.put("invalidItemsFiltered", invalidItemsFiltered);
            response.put("missingFromApi", missingFromApi);
            response.put("savedItems", savedItems);
            response.put("timestamp", LocalDateTime.now().toString());
            
            System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ЦЕН ЗАКРЫТИЯ ДЛЯ АКЦИЙ ===");
            System.out.println("Всего запрошено: " + totalRequested);
            System.out.println("Новых записей: " + newItemsSaved);
            System.out.println("Пропущено существующих: " + existingItemsSkipped);
            System.out.println("Отфильтровано: " + invalidItemsFiltered);
            System.out.println("Не найдено данных: " + missingFromApi);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке цен закрытия для акций: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Ошибка при загрузке цен закрытия для акций: " + e.getMessage());
            errorResponse.put("totalRequested", 0);
            errorResponse.put("newItemsSaved", 0);
            errorResponse.put("existingItemsSkipped", 0);
            errorResponse.put("invalidItemsFiltered", 0);
            errorResponse.put("missingFromApi", 0);
            errorResponse.put("savedItems", new ArrayList<>());
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Получение цен закрытия для всех фьючерсов из T-INVEST API
     */
    @GetMapping("/futures")
    public ResponseEntity<Map<String, Object>> getClosePricesForFutures() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ClosePriceDto> allClosePrices = mainSessionPriceService.getClosePricesForAllFutures();
            
            // Фильтруем неверные цены (с датой 1970-01-01)
            List<ClosePriceDto> validClosePrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("message", "Цены закрытия для фьючерсов получены успешно");
            response.put("data", validClosePrices);
            response.put("count", validClosePrices.size());
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Исключение будет обработано GlobalExceptionHandler
            throw new DataLoadException("Ошибка получения цен закрытия для фьючерсов: " + e.getMessage(), e);
        }
    }

    /**
     * Загрузка цен закрытия для всех фьючерсов из T-INVEST API в БД
     */
    @PostMapping("/futures")
    @Transactional
    public ResponseEntity<Map<String, Object>> loadClosePricesForFutures() {
        try {
            System.out.println("=== ЗАГРУЗКА ЦЕН ЗАКРЫТИЯ ДЛЯ ФЬЮЧЕРСОВ ===");
            
            // Получаем все фьючерсы из кэша
            List<FutureDto> futures = instrumentService.getFutures(null, null, "RUB", null, null);
            System.out.println("Найдено фьючерсов в кэше: " + futures.size());
            
            if (futures.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Не найдено фьючерсов в кэше");
                response.put("totalRequested", 0);
                response.put("newItemsSaved", 0);
                response.put("existingItemsSkipped", 0);
                response.put("invalidItemsFiltered", 0);
                response.put("missingFromApi", 0);
                response.put("savedItems", new ArrayList<>());
                response.put("timestamp", LocalDateTime.now().toString());
                
                return ResponseEntity.ok(response);
            }
            
            // Получаем FIGI всех фьючерсов в рублях
            List<String> futureFigis = new ArrayList<>();
            for (FutureDto future : futures) {
                if ("RUB".equalsIgnoreCase(future.currency())) {
                    futureFigis.add(future.figi());
                }
            }
            
            if (futureFigis.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Не найдено фьючерсов в рублях в базе данных");
                response.put("totalRequested", 0);
                response.put("newItemsSaved", 0);
                response.put("existingItemsSkipped", 0);
                response.put("invalidItemsFiltered", 0);
                response.put("missingFromApi", 0);
                response.put("savedItems", new ArrayList<>());
                response.put("timestamp", LocalDateTime.now().toString());
                
                return ResponseEntity.ok(response);
            }
            
            // Получаем цены закрытия из T-INVEST API
            List<ClosePriceDto> allClosePrices = mainSessionPriceService.getClosePrices(futureFigis, null);
            
            // Фильтруем неверные цены (с датой 1970-01-01)
            List<ClosePriceDto> validClosePrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .collect(Collectors.toList());
            
            System.out.println("Получено цен из API: " + allClosePrices.size());
            System.out.println("Валидных цен после фильтрации: " + validClosePrices.size());
            
            List<Map<String, Object>> savedItems = new ArrayList<>();
            int totalRequested = futureFigis.size();
            int newItemsSaved = 0;
            int existingItemsSkipped = 0;
            int invalidItemsFiltered = allClosePrices.size() - validClosePrices.size();
            int missingFromApi = totalRequested - allClosePrices.size();
            
            // Сохраняем валидные цены в БД
            for (ClosePriceDto closePriceDto : validClosePrices) {
                try {
                    LocalDate priceDate = LocalDate.parse(closePriceDto.tradingDate());
                    ClosePriceKey key = new ClosePriceKey(priceDate, closePriceDto.figi());
                    
                    // Проверяем, есть ли уже запись для этой даты и FIGI
                    if (closePriceRepository.existsById(key)) {
                        existingItemsSkipped++;
                        System.out.println("Запись уже существует для " + closePriceDto.figi() + " за " + priceDate);
                        continue;
                    }
                    
                    // Получаем фьючерс из кэша для получения дополнительной информации
                    FutureDto future = instrumentService.getFutureByFigi(closePriceDto.figi());
                    
                    if (future == null) {
                        System.err.println("Не найден фьючерс для FIGI: " + closePriceDto.figi());
                        continue;
                    }
                    
                    // Создаем запись для сохранения
                    ClosePriceEntity entity = new ClosePriceEntity(
                        priceDate,
                        closePriceDto.figi(),
                        "FUTURE",
                        closePriceDto.closePrice(),
                        future.currency(),
                        future.exchange()
                    );
                    
                    closePriceRepository.save(entity);
                    
                    Map<String, Object> savedItem = new HashMap<>();
                    savedItem.put("figi", closePriceDto.figi());
                    savedItem.put("ticker", future.ticker());
                    savedItem.put("name", future.ticker());
                    savedItem.put("priceDate", priceDate);
                    savedItem.put("closePrice", closePriceDto.closePrice());
                    savedItem.put("instrumentType", "FUTURE");
                    savedItem.put("currency", future.currency());
                    savedItem.put("exchange", future.exchange());
                    savedItems.add(savedItem);
                    
                    newItemsSaved++;
                    System.out.println("Сохранена цена закрытия для " + future.ticker() + ": " + closePriceDto.closePrice());
                    
                } catch (Exception e) {
                    System.err.println("Ошибка обработки цены для " + closePriceDto.figi() + ": " + e.getMessage());
                    invalidItemsFiltered++;
                }
            }
            
            // Формируем ответ
            String message = String.format("Успешно загружено %d новых цен закрытия для фьючерсов из %d найденных.", newItemsSaved, validClosePrices.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message);
            response.put("totalRequested", totalRequested);
            response.put("newItemsSaved", newItemsSaved);
            response.put("existingItemsSkipped", existingItemsSkipped);
            response.put("invalidItemsFiltered", invalidItemsFiltered);
            response.put("missingFromApi", missingFromApi);
            response.put("savedItems", savedItems);
            response.put("timestamp", LocalDateTime.now().toString());
            
            System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ЦЕН ЗАКРЫТИЯ ДЛЯ ФЬЮЧЕРСОВ ===");
            System.out.println("Всего запрошено: " + totalRequested);
            System.out.println("Новых записей: " + newItemsSaved);
            System.out.println("Пропущено существующих: " + existingItemsSkipped);
            System.out.println("Отфильтровано: " + invalidItemsFiltered);
            System.out.println("Не найдено данных: " + missingFromApi);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке цен закрытия для фьючерсов: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Ошибка при загрузке цен закрытия для фьючерсов: " + e.getMessage());
            errorResponse.put("totalRequested", 0);
            errorResponse.put("newItemsSaved", 0);
            errorResponse.put("existingItemsSkipped", 0);
            errorResponse.put("invalidItemsFiltered", 0);
            errorResponse.put("missingFromApi", 0);
            errorResponse.put("savedItems", new ArrayList<>());
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Получение цены закрытия по инструменту из T-INVEST API
     */
    @GetMapping("/by-figi/{figi}")
    public ResponseEntity<Map<String, Object>> getClosePriceByFigi(@PathVariable String figi) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ClosePriceDto> allClosePrices = mainSessionPriceService.getClosePrices(List.of(figi), null);
            
            // Фильтруем неверные цены (с датой 1970-01-01)
            List<ClosePriceDto> validClosePrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .collect(Collectors.toList());
            
            if (validClosePrices.isEmpty()) {
                response.put("success", false);
                response.put("message", "Цена закрытия не найдена для инструмента: " + figi);
                response.put("figi", figi);
                response.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.ok(response);
            }
            
            ClosePriceDto closePrice = validClosePrices.get(0);
            response.put("success", true);
            response.put("message", "Цена закрытия получена успешно");
            response.put("data", closePrice);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения цены закрытия: " + e.getMessage());
            response.put("figi", figi);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Загрузка цены закрытия по конкретному инструменту из T-INVEST API в БД
     */
    @PostMapping("/instrument/{figi}")
    @Transactional
    public ResponseEntity<Map<String, Object>> loadClosePriceByFigi(@PathVariable String figi) {
        try {
            System.out.println("=== ЗАГРУЗКА ЦЕНЫ ЗАКРЫТИЯ ДЛЯ ИНСТРУМЕНТА ===");
            System.out.println("FIGI: " + figi);
            
            // Получаем цену закрытия из T-INVEST API
            List<ClosePriceDto> allClosePrices = mainSessionPriceService.getClosePrices(List.of(figi), null);
            
            // Фильтруем неверные цены (с датой 1970-01-01)
            List<ClosePriceDto> validClosePrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .collect(Collectors.toList());
            
            if (validClosePrices.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Цена закрытия не найдена для инструмента: " + figi);
                response.put("figi", figi);
                response.put("totalRequested", 1);
                response.put("newItemsSaved", 0);
                response.put("existingItemsSkipped", 0);
                response.put("invalidItemsFiltered", allClosePrices.size());
                response.put("missingFromApi", 1 - allClosePrices.size());
                response.put("savedItems", new ArrayList<>());
                response.put("timestamp", LocalDateTime.now().toString());
                
                return ResponseEntity.ok(response);
            }
            
            ClosePriceDto closePriceDto = validClosePrices.get(0);
            LocalDate priceDate = LocalDate.parse(closePriceDto.tradingDate());
            ClosePriceKey key = new ClosePriceKey(priceDate, closePriceDto.figi());
            
            // Проверяем, есть ли уже запись для этой даты и FIGI
            if (closePriceRepository.existsById(key)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Цена закрытия уже существует для инструмента " + figi + " за " + priceDate);
                response.put("figi", figi);
                response.put("priceDate", priceDate);
                response.put("closePrice", closePriceDto.closePrice());
                response.put("totalRequested", 1);
                response.put("newItemsSaved", 0);
                response.put("existingItemsSkipped", 1);
                response.put("invalidItemsFiltered", allClosePrices.size() - validClosePrices.size());
                response.put("missingFromApi", 0);
                response.put("savedItems", new ArrayList<>());
                response.put("timestamp", LocalDateTime.now().toString());
                
                return ResponseEntity.ok(response);
            }
            
            // Определяем тип инструмента и получаем дополнительную информацию из кэша
            String instrumentType = "UNKNOWN";
            String currency = "UNKNOWN";
            String exchange = "UNKNOWN";
            String ticker = figi;
            String name = figi;
            
            // Проверяем в кэше акций
            ShareDto share = instrumentService.getShareByFigi(figi);
            if (share != null) {
                instrumentType = "SHARE";
                currency = share.currency();
                exchange = share.exchange();
                ticker = share.ticker();
                name = share.name();
            } else {
                // Проверяем в кэше фьючерсов
                FutureDto future = instrumentService.getFutureByFigi(figi);
                if (future != null) {
                    instrumentType = "FUTURE";
                    currency = future.currency();
                    exchange = future.exchange();
                    ticker = future.ticker();
                    name = future.ticker();
                } else {
                    // Проверяем в кэше индикативов
                    IndicativeDto indicative = instrumentService.getIndicativeBy(figi);
                    if (indicative != null) {
                        instrumentType = "INDICATIVE";
                        currency = indicative.currency();
                        exchange = indicative.exchange();
                        ticker = indicative.ticker();
                        name = indicative.ticker();
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
            
            closePriceRepository.save(entity);
            
            Map<String, Object> savedItem = new HashMap<>();
            savedItem.put("figi", closePriceDto.figi());
            savedItem.put("ticker", ticker);
            savedItem.put("name", name);
            savedItem.put("priceDate", priceDate);
            savedItem.put("closePrice", closePriceDto.closePrice());
            savedItem.put("instrumentType", instrumentType);
            savedItem.put("currency", currency);
            savedItem.put("exchange", exchange);
            
            List<Map<String, Object>> savedItems = new ArrayList<>();
            savedItems.add(savedItem);
            
            System.out.println("Сохранена цена закрытия для " + ticker + " (" + figi + "): " + closePriceDto.closePrice());
            
            // Формируем ответ
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Цена закрытия успешно загружена для инструмента: " + figi);
            response.put("figi", figi);
            response.put("totalRequested", 1);
            response.put("newItemsSaved", 1);
            response.put("existingItemsSkipped", 0);
            response.put("invalidItemsFiltered", allClosePrices.size() - validClosePrices.size());
            response.put("missingFromApi", 0);
            response.put("savedItems", savedItems);
            response.put("timestamp", LocalDateTime.now().toString());
            
            System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ЦЕНЫ ЗАКРЫТИЯ ДЛЯ ИНСТРУМЕНТА ===");
            System.out.println("FIGI: " + figi);
            System.out.println("Тип инструмента: " + instrumentType);
            System.out.println("Цена закрытия: " + closePriceDto.closePrice());
            System.out.println("Дата: " + priceDate);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке цены закрытия для инструмента " + figi + ": " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Ошибка при загрузке цены закрытия для инструмента " + figi + ": " + e.getMessage());
            errorResponse.put("figi", figi);
            errorResponse.put("totalRequested", 1);
            errorResponse.put("newItemsSaved", 0);
            errorResponse.put("existingItemsSkipped", 0);
            errorResponse.put("invalidItemsFiltered", 0);
            errorResponse.put("missingFromApi", 0);
            errorResponse.put("savedItems", new ArrayList<>());
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== ЦЕНЫ ОСНОВНОЙ СЕССИИ ====================

    /**
     * Загрузка цен основной сессии за дату
     * Работает только с рабочими днями, использует данные из minute_candles
     * Обрабатывает только акции и фьючерсы
     */
    @PostMapping("/by-date/{date}")
    @Transactional
    public ResponseEntity<Map<String, Object>> loadMainSessionPricesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            System.out.println("=== ЗАГРУЗКА ЦЕН ОСНОВНОЙ СЕССИИ ===");
            System.out.println("Дата: " + date);
            
            // Проверяем, является ли дата выходным днем
            if (isWeekend(date)) {
                String message = "В выходные дни (суббота и воскресенье) основная сессия не проводится. Дата: " + date;
                System.out.println(message);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", message);
                response.put("date", date.toString());
                response.put("totalRequested", 0);
                response.put("newItemsSaved", 0);
                response.put("existingItemsSkipped", 0);
                response.put("invalidItemsFiltered", 0);
                response.put("missingFromApi", 0);
                response.put("savedItems", new ArrayList<>());
                
                return ResponseEntity.ok(response);
            }
            
            // Получаем все акции и фьючерсы из БД
            List<ShareEntity> shares = shareRepository.findAll();
            List<FutureEntity> futures = futureRepository.findAll();
            System.out.println("Найдено акций: " + shares.size() + ", фьючерсов: " + futures.size());
            
            if (shares.isEmpty() && futures.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Не найдено акций и фьючерсов в базе данных");
                response.put("date", date.toString());
                response.put("totalRequested", 0);
                response.put("newItemsSaved", 0);
                response.put("existingItemsSkipped", 0);
                response.put("invalidItemsFiltered", 0);
                response.put("missingFromApi", 0);
                response.put("savedItems", new ArrayList<>());
                
                return ResponseEntity.ok(response);
            }
            
            List<Map<String, Object>> savedItems = new ArrayList<>();
            int totalRequested = 0;
            int newItemsSaved = 0;
            int existingItemsSkipped = 0;
            int invalidItemsFiltered = 0;
            int missingFromApi = 0;
            
            // Обрабатываем акции
            for (ShareEntity share : shares) {
                try {
                    System.out.println("Обрабатываем акцию: " + share.getTicker() + " (" + share.getFigi() + ")");
                    
                    // Проверяем, есть ли уже запись для этой даты и FIGI
                    ClosePriceKey key = new ClosePriceKey(date, share.getFigi());
                    if (closePriceRepository.existsById(key)) {
                        existingItemsSkipped++;
                        System.out.println("Запись уже существует для " + share.getTicker() + " за " + date);
                        continue;
                    }
                    
                    // Увеличиваем счетчик запрошенных для каждого инструмента
                    totalRequested++;
                    
                    // Ищем последнюю свечу за указанную дату в minute_candles
                    BigDecimal lastClosePrice = findLastClosePriceFromMinuteCandles(share.getFigi(), date);
                    
                    if (lastClosePrice != null) {
                        
                        // Создаем запись для сохранения
                        ClosePriceEntity entity = new ClosePriceEntity(
                            date,
                            share.getFigi(),
                            "SHARE",
                            lastClosePrice,
                            "RUB",
                            "moex_mrng_evng_e_wknd_dlr"
                        );
                        
                        closePriceRepository.save(entity);
                        
                        Map<String, Object> savedItem = new HashMap<>();
                        savedItem.put("figi", share.getFigi());
                        savedItem.put("ticker", share.getTicker());
                        savedItem.put("name", share.getName());
                        savedItem.put("priceDate", date);
                        savedItem.put("closePrice", lastClosePrice);
                        savedItem.put("instrumentType", "SHARE");
                        savedItem.put("currency", "RUB");
                        savedItem.put("exchange", "moex_mrng_evng_e_wknd_dlr");
                        savedItems.add(savedItem);
                        
                        newItemsSaved++;
                        System.out.println("Сохранена цена закрытия для " + share.getTicker() + ": " + lastClosePrice);
                    } else {
                        missingFromApi++;
                        System.out.println("Не найдена свеча для " + share.getTicker() + " за " + date);
                    }
                    
                } catch (Exception e) {
                    System.err.println("Ошибка обработки акции " + share.getTicker() + ": " + e.getMessage());
                    invalidItemsFiltered++;
                }
            }
            
            // Обрабатываем фьючерсы
            for (FutureEntity future : futures) {
                try {
                    System.out.println("Обрабатываем фьючерс: " + future.getTicker() + " (" + future.getFigi() + ")");
                    
                    // Проверяем, есть ли уже запись для этой даты и FIGI
                    ClosePriceKey key = new ClosePriceKey(date, future.getFigi());
                    if (closePriceRepository.existsById(key)) {
                        existingItemsSkipped++;
                        System.out.println("Запись уже существует для " + future.getTicker() + " за " + date);
                        continue;
                    }
                    
                    // Увеличиваем счетчик запрошенных для каждого инструмента
                    totalRequested++;
                    
                    // Ищем последнюю свечу за указанную дату в minute_candles
                    BigDecimal lastClosePrice = findLastClosePriceFromMinuteCandles(future.getFigi(), date);
                    
                    if (lastClosePrice != null) {
                        
                        // Создаем запись для сохранения
                        ClosePriceEntity entity = new ClosePriceEntity(
                            date,
                            future.getFigi(),
                            "FUTURE",
                            lastClosePrice,
                            "RUB",
                            "FORTS_MAIN"
                        );
                        
                        closePriceRepository.save(entity);
                        
                        Map<String, Object> savedItem = new HashMap<>();
                        savedItem.put("figi", future.getFigi());
                        savedItem.put("ticker", future.getTicker());
                        savedItem.put("name", future.getTicker());
                        savedItem.put("priceDate", date);
                        savedItem.put("closePrice", lastClosePrice);
                        savedItem.put("instrumentType", "FUTURE");
                        savedItem.put("currency", "RUB");
                        savedItem.put("exchange", "FORTS_MAIN");
                        savedItems.add(savedItem);
                        
                        newItemsSaved++;
                        System.out.println("Сохранена цена закрытия для " + future.getTicker() + ": " + lastClosePrice);
                    } else {
                        missingFromApi++;
                        System.out.println("Не найдена свеча для " + future.getTicker() + " за " + date);
                    }
                    
                } catch (Exception e) {
                    System.err.println("Ошибка обработки фьючерса " + future.getTicker() + ": " + e.getMessage());
                    invalidItemsFiltered++;
                }
            }
            
            // Формируем ответ
            String message = String.format("Успешно загружено %d новых цен основной сессии из %d найденных.", newItemsSaved, totalRequested);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message);
            response.put("date", date.toString());
            response.put("totalRequested", totalRequested);
            response.put("newItemsSaved", newItemsSaved);
            response.put("existingItemsSkipped", existingItemsSkipped);
            response.put("invalidItemsFiltered", invalidItemsFiltered);
            response.put("missingFromApi", missingFromApi);
            response.put("savedItems", savedItems);
            
            System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ЦЕН ОСНОВНОЙ СЕССИИ ===");
            System.out.println("Всего запрошено: " + totalRequested);
            System.out.println("Новых записей: " + newItemsSaved);
            System.out.println("Пропущено существующих: " + existingItemsSkipped);
            System.out.println("Отфильтровано: " + invalidItemsFiltered);
            System.out.println("Не найдено данных: " + missingFromApi);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке цен основной сессии: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Ошибка при загрузке цен основной сессии: " + e.getMessage());
            errorResponse.put("date", date.toString());
            errorResponse.put("totalRequested", 0);
            errorResponse.put("newItemsSaved", 0);
            errorResponse.put("existingItemsSkipped", 0);
            errorResponse.put("invalidItemsFiltered", 0);
            errorResponse.put("missingFromApi", 0);
            errorResponse.put("savedItems", new ArrayList<>());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
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
            List<MinuteCandleEntity> candles = minuteCandleRepository.findByFigiAndTimeBetween(figi, startInstant, endInstant);

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
     * Проверяет, является ли дата выходным днем
     */
    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}