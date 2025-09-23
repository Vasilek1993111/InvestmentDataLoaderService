package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.MinuteCandleEntity;
import com.example.InvestmentDataLoaderService.entity.ClosePriceEntity;
import com.example.InvestmentDataLoaderService.entity.ClosePriceKey;
import com.example.InvestmentDataLoaderService.exception.DataLoadException;
import com.example.InvestmentDataLoaderService.service.ClosePriceService;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.MinuteCandleRepository;
import com.example.InvestmentDataLoaderService.repository.ClosePriceRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private final ClosePriceService closePriceService;
    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final MinuteCandleRepository minuteCandleRepository;
    private final ClosePriceRepository closePriceRepository;

    public MainSessionPricesController(ClosePriceService closePriceService,
                                      ShareRepository shareRepository,
                                      FutureRepository futureRepository,
                                      MinuteCandleRepository minuteCandleRepository,
                                      ClosePriceRepository closePriceRepository) {
        this.closePriceService = closePriceService;
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.minuteCandleRepository = minuteCandleRepository;
        this.closePriceRepository = closePriceRepository;
    }

    // ==================== ЦЕНЫ ЗАКРЫТИЯ ОСНОВНОЙ СЕССИИ ====================

    /**
     * Загрузка цен закрытия основной сессии за сегодня
     * Работает только с акциями и фьючерсами, использует данные из minute_candles
     */
    @PostMapping("/close-prices")
    public ResponseEntity<Map<String, Object>> loadClosePricesToday() {
        try {
            LocalDate today = LocalDate.now();
            System.out.println("=== ЗАГРУЗКА ЦЕН ЗАКРЫТИЯ ОСНОВНОЙ СЕССИИ ЗА СЕГОДНЯ ===");
            System.out.println("Дата: " + today);
            
            // Проверяем, является ли дата выходным днем
            if (isWeekend(today)) {
                String message = "В выходные дни (суббота и воскресенье) торговля не проводится. Дата: " + today;
                System.out.println(message);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", message);
                response.put("date", today);
                response.put("totalRequested", 0);
                response.put("newItemsSaved", 0);
                response.put("existingItemsSkipped", 0);
                response.put("invalidItemsFiltered", 0);
                response.put("missingFromApi", 0);
                response.put("savedItems", new ArrayList<>());
                response.put("timestamp", LocalDateTime.now());
                
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
                response.put("date", today);
                response.put("totalRequested", 0);
                response.put("newItemsSaved", 0);
                response.put("existingItemsSkipped", 0);
                response.put("invalidItemsFiltered", 0);
                response.put("missingFromApi", 0);
                response.put("savedItems", new ArrayList<>());
                response.put("timestamp", LocalDateTime.now());
                
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
                    ClosePriceKey key = new ClosePriceKey(today, share.getFigi());
                    if (closePriceRepository.existsById(key)) {
                        existingItemsSkipped++;
                        System.out.println("Запись уже существует для " + share.getTicker() + " за " + today);
                        continue;
                    }
                    
                    // Ищем последнюю свечу за указанную дату в minute_candles
                    BigDecimal lastClosePrice = findLastClosePriceFromMinuteCandles(share.getFigi(), today);
                    
                    if (lastClosePrice != null) {
                        totalRequested++;
                        
                        // Создаем запись для сохранения
                        ClosePriceEntity entity = new ClosePriceEntity(
                            today,
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
                        savedItem.put("priceDate", today);
                        savedItem.put("closePrice", lastClosePrice);
                        savedItem.put("instrumentType", "SHARE");
                        savedItem.put("currency", "RUB");
                        savedItem.put("exchange", "moex_mrng_evng_e_wknd_dlr");
                        savedItems.add(savedItem);
                        
                        newItemsSaved++;
                        System.out.println("Сохранена цена закрытия для " + share.getTicker() + ": " + lastClosePrice);
                    } else {
                        missingFromApi++;
                        System.out.println("Не найдена свеча для " + share.getTicker() + " за " + today);
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
                    ClosePriceKey key = new ClosePriceKey(today, future.getFigi());
                    if (closePriceRepository.existsById(key)) {
                        existingItemsSkipped++;
                        System.out.println("Запись уже существует для " + future.getTicker() + " за " + today);
                        continue;
                    }
                    
                    // Ищем последнюю свечу за указанную дату в minute_candles
                    BigDecimal lastClosePrice = findLastClosePriceFromMinuteCandles(future.getFigi(), today);
                    
                    if (lastClosePrice != null) {
                        totalRequested++;
                        
                        // Создаем запись для сохранения
                        ClosePriceEntity entity = new ClosePriceEntity(
                            today,
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
                        savedItem.put("priceDate", today);
                        savedItem.put("closePrice", lastClosePrice);
                        savedItem.put("instrumentType", "FUTURE");
                        savedItem.put("currency", "RUB");
                        savedItem.put("exchange", "FORTS_MAIN");
                        savedItems.add(savedItem);
                        
                        newItemsSaved++;
                        System.out.println("Сохранена цена закрытия для " + future.getTicker() + ": " + lastClosePrice);
                    } else {
                        missingFromApi++;
                        System.out.println("Не найдена свеча для " + future.getTicker() + " за " + today);
                    }
                    
                } catch (Exception e) {
                    System.err.println("Ошибка обработки фьючерса " + future.getTicker() + ": " + e.getMessage());
                    invalidItemsFiltered++;
                }
            }
            
            // Формируем ответ
            String message = String.format("Успешно загружено %d новых цен закрытия основной сессии из %d найденных.", newItemsSaved, totalRequested);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message);
            response.put("date", today);
            response.put("totalRequested", totalRequested);
            response.put("newItemsSaved", newItemsSaved);
            response.put("existingItemsSkipped", existingItemsSkipped);
            response.put("invalidItemsFiltered", invalidItemsFiltered);
            response.put("missingFromApi", missingFromApi);
            response.put("savedItems", savedItems);
            response.put("timestamp", LocalDateTime.now());
            
            System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ЦЕН ЗАКРЫТИЯ ОСНОВНОЙ СЕССИИ ===");
            System.out.println("Всего запрошено: " + totalRequested);
            System.out.println("Новых записей: " + newItemsSaved);
            System.out.println("Пропущено существующих: " + existingItemsSkipped);
            System.out.println("Отфильтровано: " + invalidItemsFiltered);
            System.out.println("Не найдено данных: " + missingFromApi);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке цен закрытия основной сессии за сегодня: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Ошибка при загрузке цен закрытия основной сессии за сегодня: " + e.getMessage());
            errorResponse.put("date", LocalDate.now());
            errorResponse.put("totalRequested", 0);
            errorResponse.put("newItemsSaved", 0);
            errorResponse.put("existingItemsSkipped", 0);
            errorResponse.put("invalidItemsFiltered", 0);
            errorResponse.put("missingFromApi", 0);
            errorResponse.put("savedItems", new ArrayList<>());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Синхронная точечная загрузка цен закрытия по указанным инструментам
     */
    @PostMapping("/close-prices/save")
    public ResponseEntity<SaveResponseDto> saveClosePrices(@RequestBody(required = false) ClosePriceRequestDto request) {
        try {
            // Если request null, создаем пустой объект
            if (request == null) {
                request = new ClosePriceRequestDto();
            }
            SaveResponseDto response = closePriceService.saveClosePrices(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Исключение будет обработано GlobalExceptionHandler
            throw new DataLoadException("Ошибка сохранения цен закрытия: " + e.getMessage(), e);
        }
    }

    /**
     * Получение цен закрытия для всех акций из T-INVEST API
     */
    @GetMapping("/close-prices/shares")
    public ResponseEntity<Map<String, Object>> getClosePricesForShares() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ClosePriceDto> allClosePrices = closePriceService.getClosePricesForAllShares();
            
            // Фильтруем неверные цены (с датой 1970-01-01)
            List<ClosePriceDto> validClosePrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("message", "Цены закрытия для акций получены успешно");
            response.put("data", validClosePrices);
            response.put("count", validClosePrices.size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Исключение будет обработано GlobalExceptionHandler
            throw new DataLoadException("Ошибка получения цен закрытия для акций: " + e.getMessage(), e);
        }
    }

    /**
     * Получение цен закрытия для всех фьючерсов из T-INVEST API
     */
    @GetMapping("/close-prices/futures")
    public ResponseEntity<Map<String, Object>> getClosePricesForFutures() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ClosePriceDto> allClosePrices = closePriceService.getClosePricesForAllFutures();
            
            // Фильтруем неверные цены (с датой 1970-01-01)
            List<ClosePriceDto> validClosePrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("message", "Цены закрытия для фьючерсов получены успешно");
            response.put("data", validClosePrices);
            response.put("count", validClosePrices.size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Исключение будет обработано GlobalExceptionHandler
            throw new DataLoadException("Ошибка получения цен закрытия для фьючерсов: " + e.getMessage(), e);
        }
    }

    /**
     * Получение цены закрытия по инструменту из T-INVEST API
     */
    @GetMapping("/close-prices/{figi}")
    public ResponseEntity<Map<String, Object>> getClosePriceByFigi(@PathVariable String figi) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ClosePriceDto> allClosePrices = closePriceService.getClosePrices(List.of(figi), null);
            
            // Фильтруем неверные цены (с датой 1970-01-01)
            List<ClosePriceDto> validClosePrices = allClosePrices.stream()
                    .filter(price -> !"1970-01-01".equals(price.tradingDate()))
                    .collect(Collectors.toList());
            
            if (validClosePrices.isEmpty()) {
                response.put("success", false);
                response.put("message", "Цена закрытия не найдена для инструмента: " + figi);
                response.put("figi", figi);
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.ok(response);
            }
            
            ClosePriceDto closePrice = validClosePrices.get(0);
            response.put("success", true);
            response.put("message", "Цена закрытия получена успешно");
            response.put("data", closePrice);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения цены закрытия: " + e.getMessage());
            response.put("figi", figi);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== ЦЕНЫ ОСНОВНОЙ СЕССИИ ====================

    /**
     * Загрузка цен основной сессии за дату
     * Работает только с рабочими днями, использует данные из minute_candles
     * Обрабатывает только акции и фьючерсы
     */
    @PostMapping("/{date}")
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
                response.put("date", date);
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
                response.put("date", date);
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
                    
                    // Ищем последнюю свечу за указанную дату в minute_candles
                    BigDecimal lastClosePrice = findLastClosePriceFromMinuteCandles(share.getFigi(), date);
                    
                    if (lastClosePrice != null) {
                        totalRequested++;
                        
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
                    
                    // Ищем последнюю свечу за указанную дату в minute_candles
                    BigDecimal lastClosePrice = findLastClosePriceFromMinuteCandles(future.getFigi(), date);
                    
                    if (lastClosePrice != null) {
                        totalRequested++;
                        
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
            response.put("date", date);
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
            errorResponse.put("date", date);
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