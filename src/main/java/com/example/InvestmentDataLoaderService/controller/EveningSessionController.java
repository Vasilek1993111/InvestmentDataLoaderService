package com.example.InvestmentDataLoaderService.controller;

import com.example.InvestmentDataLoaderService.entity.ClosePriceEveningSessionEntity;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.repository.ClosePriceEveningSessionRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.MinuteCandleRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Контроллер для работы с ценами вечерней сессии
 */
@RestController
@RequestMapping("/api/evening-session-prices")
public class EveningSessionController {

    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final MinuteCandleRepository minuteCandleRepository;
    private final ClosePriceEveningSessionRepository closePriceEveningSessionRepository;

    public EveningSessionController(
            ShareRepository shareRepository,
            FutureRepository futureRepository,
            MinuteCandleRepository minuteCandleRepository,
            ClosePriceEveningSessionRepository closePriceEveningSessionRepository
    ) {
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.minuteCandleRepository = minuteCandleRepository;
        this.closePriceEveningSessionRepository = closePriceEveningSessionRepository;
    }

     /**
     * Получение цен закрытия вечерней сессии за вчерашний день
     * Цена закрытия определяется как close последней минутной свечи в дне
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getEveningSessionClosePricesYesterday() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Получаем вчерашнюю дату
            LocalDate yesterday = LocalDate.now().minusDays(1);
            
            System.out.println("Получение цен закрытия вечерней сессии за " + yesterday);
            
            // Получаем все акции и фьючерсы из БД
            List<ShareEntity> shares = shareRepository.findAll();
            List<FutureEntity> futures = futureRepository.findAll();
            
            List<Map<String, Object>> eveningClosePrices = new ArrayList<>();
            int totalProcessed = 0;
            int foundPrices = 0;
            int missingData = 0;
            
            // Обрабатываем акции
            for (ShareEntity share : shares) {
                try {
                    totalProcessed++;
                    
                    // Получаем последнюю свечу за день для акции
                    var lastCandle = minuteCandleRepository.findLastCandleForDate(share.getFigi(), yesterday);
                    
                    if (lastCandle != null) {
                        BigDecimal lastClosePrice = lastCandle.getClose();
                        
                        // Проверяем, что цена не равна 0 (невалидная цена)
                        if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                            Map<String, Object> priceData = new HashMap<>();
                            priceData.put("figi", share.getFigi());
                            priceData.put("ticker", share.getTicker());
                            priceData.put("name", share.getName());
                            priceData.put("priceDate", yesterday);
                            priceData.put("closePrice", lastClosePrice);
                            priceData.put("instrumentType", "SHARE");
                            priceData.put("currency", "RUB");
                            priceData.put("exchange", "MOEX");
                            
                            eveningClosePrices.add(priceData);
                            foundPrices++;
                        } else {
                            missingData++;
                        }
                    } else {
                        missingData++;
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка обработки акции " + share.getTicker() + ": " + e.getMessage());
                    missingData++;
                }
            }
            
            // Обрабатываем фьючерсы
            for (FutureEntity future : futures) {
                try {
                    totalProcessed++;
                    
                    // Получаем последнюю свечу за день для фьючерса
                    var lastCandle = minuteCandleRepository.findLastCandleForDate(future.getFigi(), yesterday);
                    
                    if (lastCandle != null) {
                        BigDecimal lastClosePrice = lastCandle.getClose();
                        
                        // Проверяем, что цена не равна 0 (невалидная цена)
                        if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                            Map<String, Object> priceData = new HashMap<>();
                            priceData.put("figi", future.getFigi());
                            priceData.put("ticker", future.getTicker());
                            priceData.put("name", future.getTicker());
                            priceData.put("priceDate", yesterday);
                            priceData.put("closePrice", lastClosePrice);
                            priceData.put("instrumentType", "FUTURE");
                            priceData.put("currency", "RUB");
                            priceData.put("exchange", "MOEX");
                            
                            eveningClosePrices.add(priceData);
                            foundPrices++;
                        } else {
                            missingData++;
                        }
                    } else {
                        missingData++;
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка обработки фьючерса " + future.getTicker() + ": " + e.getMessage());
                    missingData++;
                }
            }
            
            response.put("success", true);
            response.put("message", "Цены закрытия вечерней сессии за " + yesterday + " получены успешно");
            response.put("data", eveningClosePrices);
            response.put("count", eveningClosePrices.size());
            response.put("date", yesterday);
            response.put("statistics", Map.of(
                "totalProcessed", totalProcessed,
                "foundPrices", foundPrices,
                "missingData", missingData
            ));
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения цен закрытия вечерней сессии за вчера: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Загрузка цен закрытия вечерней сессии за вчерашний день
     * Цена закрытия определяется как close последней минутной свечи в дне
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> loadEveningSessionPrices() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Получаем вчерашнюю дату
            LocalDate yesterday = LocalDate.now().minusDays(1);
            
            System.out.println("=== ЗАГРУЗКА ЦЕН ЗАКРЫТИЯ ВЕЧЕРНЕЙ СЕССИИ ===");
            System.out.println("Дата: " + yesterday);
            
            // Получаем все акции и фьючерсы из БД
            List<ShareEntity> shares = shareRepository.findAll();
            List<FutureEntity> futures = futureRepository.findAll();
            
            System.out.println("Найдено акций: " + shares.size());
            System.out.println("Найдено фьючерсов: " + futures.size());
            
            int totalRequested = shares.size() + futures.size();
            int newItemsSaved = 0;
            int existingItemsSkipped = 0;
            int invalidItemsFiltered = 0;
            int missingFromApi = 0;
            
            List<Map<String, Object>> savedItems = new ArrayList<>();
            
            // Обрабатываем акции
            for (ShareEntity share : shares) {
                try {
                    System.out.println("Обрабатываем акцию: " + share.getTicker() + " (" + share.getFigi() + ")");
                    
                    // Получаем последнюю свечу за день для акции
                    var lastCandle = minuteCandleRepository.findLastCandleForDate(share.getFigi(), yesterday);
                    
                    if (lastCandle != null) {
                        BigDecimal lastClosePrice = lastCandle.getClose();
                        
                        // Проверяем, есть ли уже запись для этой даты и FIGI
                        if (closePriceEveningSessionRepository.existsByPriceDateAndFigi(yesterday, share.getFigi())) {
                            existingItemsSkipped++;
                            System.out.println("Запись уже существует для " + share.getTicker() + " за " + yesterday);
                            continue;
                        }
                        
                        // Проверяем, что цена не равна 0 (невалидная цена)
                        if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                            // Создаем запись для сохранения
                            ClosePriceEveningSessionEntity entity = new ClosePriceEveningSessionEntity();
                            entity.setFigi(share.getFigi());
                            entity.setPriceDate(yesterday);
                            entity.setClosePrice(lastClosePrice);
                            entity.setInstrumentType("SHARE");
                            entity.setCurrency("RUB");
                            entity.setExchange("MOEX");
                            
                            closePriceEveningSessionRepository.save(entity);
                            
                            Map<String, Object> savedItem = new HashMap<>();
                            savedItem.put("figi", share.getFigi());
                            savedItem.put("ticker", share.getTicker());
                            savedItem.put("name", share.getName());
                            savedItem.put("priceDate", yesterday);
                            savedItem.put("closePrice", lastClosePrice);
                            savedItem.put("instrumentType", "SHARE");
                            savedItem.put("currency", "RUB");
                            savedItem.put("exchange", "MOEX");
                            
                            savedItems.add(savedItem);
                            newItemsSaved++;
                            System.out.println("Сохранена цена закрытия вечерней сессии для " + share.getTicker() + ": " + lastClosePrice);
                        } else {
                            invalidItemsFiltered++;
                            System.out.println("Невалидная цена для " + share.getTicker() + ": " + lastClosePrice);
                        }
                    } else {
                        missingFromApi++;
                        System.out.println("Свечи не найдены для " + share.getTicker() + " за " + yesterday);
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка обработки акции " + share.getTicker() + ": " + e.getMessage());
                    missingFromApi++;
                }
            }
            
            // Обрабатываем фьючерсы
            for (FutureEntity future : futures) {
                try {
                    System.out.println("Обрабатываем фьючерс: " + future.getTicker() + " (" + future.getFigi() + ")");
                    
                    // Получаем последнюю свечу за день для фьючерса
                    var lastCandle = minuteCandleRepository.findLastCandleForDate(future.getFigi(), yesterday);
                    
                    if (lastCandle != null) {
                        BigDecimal lastClosePrice = lastCandle.getClose();
                        
                        // Проверяем, есть ли уже запись для этой даты и FIGI
                        if (closePriceEveningSessionRepository.existsByPriceDateAndFigi(yesterday, future.getFigi())) {
                            existingItemsSkipped++;
                            System.out.println("Запись уже существует для " + future.getTicker() + " за " + yesterday);
                            continue;
                        }
                        
                        // Проверяем, что цена не равна 0 (невалидная цена)
                        if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                            // Создаем запись для сохранения
                            ClosePriceEveningSessionEntity entity = new ClosePriceEveningSessionEntity();
                            entity.setFigi(future.getFigi());
                            entity.setPriceDate(yesterday);
                            entity.setClosePrice(lastClosePrice);
                            entity.setInstrumentType("FUTURE");
                            entity.setCurrency("RUB");
                            entity.setExchange("MOEX");
                            
                            closePriceEveningSessionRepository.save(entity);
                            
                            Map<String, Object> savedItem = new HashMap<>();
                            savedItem.put("figi", future.getFigi());
                            savedItem.put("ticker", future.getTicker());
                            savedItem.put("name", future.getTicker());
                            savedItem.put("priceDate", yesterday);
                            savedItem.put("closePrice", lastClosePrice);
                            savedItem.put("instrumentType", "FUTURE");
                            savedItem.put("currency", "RUB");
                            savedItem.put("exchange", "MOEX");
                            
                            savedItems.add(savedItem);
                            newItemsSaved++;
                            System.out.println("Сохранена цена закрытия вечерней сессии для " + future.getTicker() + ": " + lastClosePrice);
                        } else {
                            invalidItemsFiltered++;
                            System.out.println("Невалидная цена для " + future.getTicker() + ": " + lastClosePrice);
                        }
                    } else {
                        missingFromApi++;
                        System.out.println("Свечи не найдены для " + future.getTicker() + " за " + yesterday);
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка обработки фьючерса " + future.getTicker() + ": " + e.getMessage());
                    missingFromApi++;
                }
            }
            
            System.out.println("=== ЗАГРУЗКА ЗАВЕРШЕНА ===");
            System.out.println("Всего запрошено: " + totalRequested);
            System.out.println("Сохранено новых: " + newItemsSaved);
            System.out.println("Пропущено существующих: " + existingItemsSkipped);
            System.out.println("Отфильтровано невалидных: " + invalidItemsFiltered);
            System.out.println("Не найдено в API: " + missingFromApi);
            
            response.put("success", true);
            response.put("message", "Цены закрытия вечерней сессии за " + yesterday + " загружены успешно");
            response.put("data", savedItems);
            response.put("count", savedItems.size());
            response.put("date", yesterday);
            response.put("statistics", Map.of(
                "totalRequested", totalRequested,
                "newItemsSaved", newItemsSaved,
                "existingItemsSkipped", existingItemsSkipped,
                "invalidItemsFiltered", invalidItemsFiltered,
                "missingFromApi", missingFromApi
            ));
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка загрузки цен закрытия вечерней сессии за вчера: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Получение цен вечерней сессии для всех инструментов (акции + фьючерсы) за конкретную дату.
     * Использует данные из minute_candles
     */
    @GetMapping("/{date}")
    public ResponseEntity<Map<String, Object>> getEveningSessionPricesForAllInstruments(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            
            // Получаем все акции и фьючерсы из БД
            List<ShareEntity> shares = shareRepository.findAll();
            List<FutureEntity> futures = futureRepository.findAll();
            
            List<Map<String, Object>> eveningPrices = new ArrayList<>();
            int totalProcessed = 0;
            int foundPrices = 0;
            int missingData = 0;
            
            // Обрабатываем акции
            for (ShareEntity share : shares) {
                try {
                    totalProcessed++;
                    
                    // Получаем последнюю свечу за день для акции
                    var lastCandle = minuteCandleRepository.findLastCandleForDate(share.getFigi(), date);
                    
                    if (lastCandle != null) {
                        BigDecimal lastClosePrice = lastCandle.getClose();
                        
                        // Проверяем, что цена не равна 0 (невалидная цена)
                        if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                            Map<String, Object> priceData = new HashMap<>();
                            priceData.put("figi", share.getFigi());
                            priceData.put("ticker", share.getTicker());
                            priceData.put("name", share.getName());
                            priceData.put("priceDate", date);
                            priceData.put("closePrice", lastClosePrice);
                            priceData.put("instrumentType", "SHARE");
                            priceData.put("currency", "RUB");
                            priceData.put("exchange", "MOEX");
                            
                            eveningPrices.add(priceData);
                            foundPrices++;
                        } else {
                            missingData++;
                        }
                    } else {
                        missingData++;
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка обработки акции " + share.getTicker() + ": " + e.getMessage());
                    missingData++;
                }
            }
            
            // Обрабатываем фьючерсы
            for (FutureEntity future : futures) {
                try {
                    totalProcessed++;
                    
                    // Получаем последнюю свечу за день для фьючерса
                    var lastCandle = minuteCandleRepository.findLastCandleForDate(future.getFigi(), date);
                    
                    if (lastCandle != null) {
                        BigDecimal lastClosePrice = lastCandle.getClose();
                        
                        // Проверяем, что цена не равна 0 (невалидная цена)
                        if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                            Map<String, Object> priceData = new HashMap<>();
                            priceData.put("figi", future.getFigi());
                            priceData.put("ticker", future.getTicker());
                            priceData.put("name", future.getTicker());
                            priceData.put("priceDate", date);
                            priceData.put("closePrice", lastClosePrice);
                            priceData.put("instrumentType", "FUTURE");
                            priceData.put("currency", "RUB");
                            priceData.put("exchange", "MOEX");
                            
                            eveningPrices.add(priceData);
                            foundPrices++;
                        } else {
                            missingData++;
                        }
                    } else {
                        missingData++;
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка обработки фьючерса " + future.getTicker() + ": " + e.getMessage());
                    missingData++;
                }
            }
            
            response.put("success", true);
            response.put("message", "Цены вечерней сессии для всех инструментов за " + date + " получены успешно");
            response.put("data", eveningPrices);
            response.put("count", eveningPrices.size());
            response.put("date", date);
            response.put("totalProcessed", totalProcessed);
            response.put("foundPrices", foundPrices);
            response.put("missingData", missingData);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения цен вечерней сессии для всех инструментов за " + date + ": " + e.getMessage());
            response.put("date", date);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Загрузка цен вечерней сессии за конкретную дату.
     * Использует данные из minute_candles, обрабатывает только акции и фьючерсы
     */
    @PostMapping("/{date}")
    public ResponseEntity<Map<String, Object>> loadEveningSessionPricesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            System.out.println("=== ЗАГРУЗКА ЦЕН ВЕЧЕРНЕЙ СЕССИИ ===");
            System.out.println("Дата: " + date);
            
            
            // Получаем все акции и фьючерсы из БД
            List<ShareEntity> shares = shareRepository.findAll();
            List<FutureEntity> futures = futureRepository.findAll();
            
            System.out.println("Найдено акций: " + shares.size());
            System.out.println("Найдено фьючерсов: " + futures.size());
            
            int totalRequested = shares.size() + futures.size();
            int newItemsSaved = 0;
            int existingItemsSkipped = 0;
            int invalidItemsFiltered = 0;
            int missingFromApi = 0;
            
            List<Map<String, Object>> savedItems = new ArrayList<>();
            
            // Обрабатываем акции
            for (ShareEntity share : shares) {
                try {
                    System.out.println("Обрабатываем акцию: " + share.getTicker() + " (" + share.getFigi() + ")");
                    
                    // Получаем последнюю свечу за день для акции
                    var lastCandle = minuteCandleRepository.findLastCandleForDate(share.getFigi(), date);
                    
                    if (lastCandle != null) {
                        BigDecimal lastClosePrice = lastCandle.getClose();
                        
                        // Проверяем, есть ли уже запись для этой даты и FIGI
                        if (closePriceEveningSessionRepository.existsByPriceDateAndFigi(date, share.getFigi())) {
                            existingItemsSkipped++;
                            System.out.println("Запись уже существует для " + share.getTicker() + " за " + date);
                            continue;
                        }
                        
                        // Проверяем, что цена не равна 0 (невалидная цена)
                        if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                            // Создаем запись для сохранения
                            ClosePriceEveningSessionEntity entity = new ClosePriceEveningSessionEntity();
                            entity.setFigi(share.getFigi());
                            entity.setPriceDate(date);
                            entity.setClosePrice(lastClosePrice);
                            entity.setInstrumentType("SHARE");
                            entity.setCurrency("RUB");
                            entity.setExchange("MOEX");
                            
                            closePriceEveningSessionRepository.save(entity);
                            
                            Map<String, Object> savedItem = new HashMap<>();
                            savedItem.put("figi", share.getFigi());
                            savedItem.put("ticker", share.getTicker());
                            savedItem.put("name", share.getName());
                            savedItem.put("priceDate", date);
                            savedItem.put("closePrice", lastClosePrice);
                            savedItem.put("instrumentType", "SHARE");
                            savedItem.put("currency", "RUB");
                            savedItem.put("exchange", "MOEX");
                            
                            savedItems.add(savedItem);
                            newItemsSaved++;
                            System.out.println("Сохранена цена вечерней сессии для " + share.getTicker() + ": " + lastClosePrice);
                        } else {
                            invalidItemsFiltered++;
                            System.out.println("Невалидная цена для " + share.getTicker() + ": " + lastClosePrice);
                        }
                    } else {
                        missingFromApi++;
                        System.out.println("Нет данных для акции " + share.getTicker() + " за " + date);
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка обработки акции " + share.getTicker() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Обрабатываем фьючерсы
            for (FutureEntity future : futures) {
                try {
                    System.out.println("Обрабатываем фьючерс: " + future.getTicker() + " (" + future.getFigi() + ")");
                    
                    // Получаем последнюю свечу за день для фьючерса
                    var lastCandle = minuteCandleRepository.findLastCandleForDate(future.getFigi(), date);
                    
                    if (lastCandle != null) {
                        BigDecimal lastClosePrice = lastCandle.getClose();
                        
                        // Проверяем, есть ли уже запись для этой даты и FIGI
                        if (closePriceEveningSessionRepository.existsByPriceDateAndFigi(date, future.getFigi())) {
                            existingItemsSkipped++;
                            System.out.println("Запись уже существует для " + future.getTicker() + " за " + date);
                            continue;
                        }
                        
                        // Проверяем, что цена не равна 0 (невалидная цена)
                        if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                            // Создаем запись для сохранения
                            ClosePriceEveningSessionEntity entity = new ClosePriceEveningSessionEntity();
                            entity.setFigi(future.getFigi());
                            entity.setPriceDate(date);
                            entity.setClosePrice(lastClosePrice);
                            entity.setInstrumentType("FUTURE");
                            entity.setCurrency("RUB");
                            entity.setExchange("MOEX");
                            
                            closePriceEveningSessionRepository.save(entity);
                            
                            Map<String, Object> savedItem = new HashMap<>();
                            savedItem.put("figi", future.getFigi());
                            savedItem.put("ticker", future.getTicker());
                            savedItem.put("name", future.getTicker());
                            savedItem.put("priceDate", date);
                            savedItem.put("closePrice", lastClosePrice);
                            savedItem.put("instrumentType", "FUTURE");
                            savedItem.put("currency", "RUB");
                            savedItem.put("exchange", "MOEX");
                            
                            savedItems.add(savedItem);
                            newItemsSaved++;
                            System.out.println("Сохранена цена вечерней сессии для " + future.getTicker() + ": " + lastClosePrice);
                        } else {
                            invalidItemsFiltered++;
                            System.out.println("Невалидная цена для " + future.getTicker() + ": " + lastClosePrice);
                        }
                    } else {
                        missingFromApi++;
                        System.out.println("Нет данных для фьючерса " + future.getTicker() + " за " + date);
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка обработки фьючерса " + future.getTicker() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Успешно загружено " + newItemsSaved + " новых цен вечерней сессии из " + totalRequested + " найденных.");
            response.put("date", date);
            response.put("totalRequested", totalRequested);
            response.put("newItemsSaved", newItemsSaved);
            response.put("existingItemsSkipped", existingItemsSkipped);
            response.put("invalidItemsFiltered", invalidItemsFiltered);
            response.put("missingFromApi", missingFromApi);
            response.put("savedItems", savedItems);
            response.put("timestamp", LocalDateTime.now());
            
            System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ЦЕН ВЕЧЕРНЕЙ СЕССИИ ===");
            System.out.println("Всего запрошено: " + totalRequested);
            System.out.println("Новых сохранено: " + newItemsSaved);
            System.out.println("Существующих пропущено: " + existingItemsSkipped);
            System.out.println("Невалидных отфильтровано: " + invalidItemsFiltered);
            System.out.println("Отсутствующих в API: " + missingFromApi);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Ошибка загрузки цен вечерней сессии за дату " + date + ": " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Ошибка загрузки цен вечерней сессии за дату " + date + ": " + e.getMessage());
            errorResponse.put("date", date);
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Получение цен вечерней сессии для всех акций за конкретную дату.
     * Использует данные из minute_candles, обрабатывает только акции
     */
    @GetMapping("/shares/{date}")
    public ResponseEntity<Map<String, Object>> getEveningSessionPricesForShares(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            
            // Получаем все акции из БД
            List<ShareEntity> shares = shareRepository.findAll();
            
            List<Map<String, Object>> eveningPrices = new ArrayList<>();
            int totalProcessed = 0;
            int foundPrices = 0;
            int missingData = 0;
            
            // Обрабатываем акции
            for (ShareEntity share : shares) {
                try {
                    totalProcessed++;
                    
                    // Получаем последнюю свечу за день для акции
                    var lastCandle = minuteCandleRepository.findLastCandleForDate(share.getFigi(), date);
                    
                    if (lastCandle != null) {
                        BigDecimal lastClosePrice = lastCandle.getClose();
                        
                        // Проверяем, что цена не равна 0 (невалидная цена)
                        if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                            Map<String, Object> priceData = new HashMap<>();
                            priceData.put("figi", share.getFigi());
                            priceData.put("ticker", share.getTicker());
                            priceData.put("name", share.getName());
                            priceData.put("priceDate", date);
                            priceData.put("closePrice", lastClosePrice);
                            priceData.put("instrumentType", "SHARE");
                            priceData.put("currency", "RUB");
                            priceData.put("exchange", "MOEX");
                            
                            eveningPrices.add(priceData);
                            foundPrices++;
                        } else {
                            missingData++;
                        }
                    } else {
                        missingData++;
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка обработки акции " + share.getTicker() + ": " + e.getMessage());
                    missingData++;
                }
            }
            
            response.put("success", true);
            response.put("message", "Цены вечерней сессии для акций за " + date + " получены успешно");
            response.put("data", eveningPrices);
            response.put("count", eveningPrices.size());
            response.put("date", date);
            response.put("totalProcessed", totalProcessed);
            response.put("foundPrices", foundPrices);
            response.put("missingData", missingData);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения цен вечерней сессии для акций за " + date + ": " + e.getMessage());
            response.put("date", date);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Получение цен вечерней сессии для всех фьючерсов за конкретную дату.
     * Использует данные из minute_candles, обрабатывает только фьючерсы
     */
    @GetMapping("/futures/{date}")
    public ResponseEntity<Map<String, Object>> getEveningSessionPricesForFutures(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            
            // Получаем все фьючерсы из БД
            List<FutureEntity> futures = futureRepository.findAll();
            
            List<Map<String, Object>> eveningPrices = new ArrayList<>();
            int totalProcessed = 0;
            int foundPrices = 0;
            int missingData = 0;
            
            // Обрабатываем фьючерсы
            for (FutureEntity future : futures) {
                try {
                    totalProcessed++;
                    
                    // Получаем последнюю свечу за день для фьючерса
                    var lastCandle = minuteCandleRepository.findLastCandleForDate(future.getFigi(), date);
                    
                    if (lastCandle != null) {
                        BigDecimal lastClosePrice = lastCandle.getClose();
                        
                        // Проверяем, что цена не равна 0 (невалидная цена)
                        if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                            Map<String, Object> priceData = new HashMap<>();
                            priceData.put("figi", future.getFigi());
                            priceData.put("ticker", future.getTicker());
                            priceData.put("name", future.getTicker());
                            priceData.put("priceDate", date);
                            priceData.put("closePrice", lastClosePrice);
                            priceData.put("instrumentType", "FUTURE");
                            priceData.put("currency", "RUB");
                            priceData.put("exchange", "MOEX");
                            
                            eveningPrices.add(priceData);
                            foundPrices++;
                        } else {
                            missingData++;
                        }
                    } else {
                        missingData++;
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка обработки фьючерса " + future.getTicker() + ": " + e.getMessage());
                    missingData++;
                }
            }
            
            response.put("success", true);
            response.put("message", "Цены вечерней сессии для фьючерсов за " + date + " получены успешно");
            response.put("data", eveningPrices);
            response.put("count", eveningPrices.size());
            response.put("date", date);
            response.put("totalProcessed", totalProcessed);
            response.put("foundPrices", foundPrices);
            response.put("missingData", missingData);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения цен вечерней сессии для фьючерсов за " + date + ": " + e.getMessage());
            response.put("date", date);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

   




    /**
     * Сохранение цен вечерней сессии для акций за конкретную дату.
     * Использует данные из minute_candles, обрабатывает только акции и сохраняет в БД
     */
    @PostMapping("/shares/{date}")
    public ResponseEntity<Map<String, Object>> saveEveningSessionPricesForShares(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            
            // Получаем все акции из БД
            List<ShareEntity> shares = shareRepository.findAll();
            
            int totalProcessed = 0;
            int newItemsSaved = 0;
            int existingItemsSkipped = 0;
            int invalidItemsFiltered = 0;
            int missingData = 0;
            
            List<Map<String, Object>> savedItems = new ArrayList<>();
            
            // Обрабатываем акции
            for (ShareEntity share : shares) {
                try {
                    totalProcessed++;
                    
                    // Получаем последнюю свечу за день для акции
                    var lastCandle = minuteCandleRepository.findLastCandleForDate(share.getFigi(), date);
                    
                    if (lastCandle != null) {
                        BigDecimal lastClosePrice = lastCandle.getClose();
                        
                        // Проверяем, есть ли уже запись для этой даты и FIGI
                        if (closePriceEveningSessionRepository.existsByPriceDateAndFigi(date, share.getFigi())) {
                            existingItemsSkipped++;
                            continue;
                        }
                        
                        // Проверяем, что цена не равна 0 (невалидная цена)
                        if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                            // Создаем запись для сохранения
                            ClosePriceEveningSessionEntity entity = new ClosePriceEveningSessionEntity();
                            entity.setFigi(share.getFigi());
                            entity.setPriceDate(date);
                            entity.setClosePrice(lastClosePrice);
                            entity.setInstrumentType("SHARE");
                            entity.setCurrency("RUB");
                            entity.setExchange("MOEX");
                            
                            closePriceEveningSessionRepository.save(entity);
                            
                            Map<String, Object> savedItem = new HashMap<>();
                            savedItem.put("figi", share.getFigi());
                            savedItem.put("ticker", share.getTicker());
                            savedItem.put("name", share.getName());
                            savedItem.put("priceDate", date);
                            savedItem.put("closePrice", lastClosePrice);
                            savedItem.put("instrumentType", "SHARE");
                            
                            savedItems.add(savedItem);
                            newItemsSaved++;
                        } else {
                            invalidItemsFiltered++;
                        }
                    } else {
                        missingData++;
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка обработки акции " + share.getTicker() + ": " + e.getMessage());
                    missingData++;
                }
            }
            
            response.put("success", true);
            response.put("message", "Успешно сохранено " + newItemsSaved + " новых цен вечерней сессии для акций за " + date);
            response.put("date", date);
            response.put("totalProcessed", totalProcessed);
            response.put("newItemsSaved", newItemsSaved);
            response.put("existingItemsSkipped", existingItemsSkipped);
            response.put("invalidItemsFiltered", invalidItemsFiltered);
            response.put("missingData", missingData);
            response.put("savedItems", savedItems);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка сохранения цен вечерней сессии для акций за " + date + ": " + e.getMessage());
            response.put("date", date);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Сохранение цен вечерней сессии для фьючерсов за конкретную дату.
     * Использует данные из minute_candles, обрабатывает только фьючерсы и сохраняет в БД
     */
    @PostMapping("/futures/{date}")
    public ResponseEntity<Map<String, Object>> saveEveningSessionPricesForFutures(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            
            // Получаем все фьючерсы из БД
            List<FutureEntity> futures = futureRepository.findAll();
            
            int totalProcessed = 0;
            int newItemsSaved = 0;
            int existingItemsSkipped = 0;
            int invalidItemsFiltered = 0;
            int missingData = 0;
            
            List<Map<String, Object>> savedItems = new ArrayList<>();
            
            // Обрабатываем фьючерсы
            for (FutureEntity future : futures) {
                try {
                    totalProcessed++;
                    
                    // Получаем последнюю свечу за день для фьючерса
                    var lastCandle = minuteCandleRepository.findLastCandleForDate(future.getFigi(), date);
                    
                    if (lastCandle != null) {
                        BigDecimal lastClosePrice = lastCandle.getClose();
                        
                        // Проверяем, есть ли уже запись для этой даты и FIGI
                        if (closePriceEveningSessionRepository.existsByPriceDateAndFigi(date, future.getFigi())) {
                            existingItemsSkipped++;
                            continue;
                        }
                        
                        // Проверяем, что цена не равна 0 (невалидная цена)
                        if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                            // Создаем запись для сохранения
                            ClosePriceEveningSessionEntity entity = new ClosePriceEveningSessionEntity();
                            entity.setFigi(future.getFigi());
                            entity.setPriceDate(date);
                            entity.setClosePrice(lastClosePrice);
                            entity.setInstrumentType("FUTURE");
                            entity.setCurrency("RUB");
                            entity.setExchange("MOEX");
                            
                            closePriceEveningSessionRepository.save(entity);
                            
                            Map<String, Object> savedItem = new HashMap<>();
                            savedItem.put("figi", future.getFigi());
                            savedItem.put("ticker", future.getTicker());
                            savedItem.put("name", future.getTicker());
                            savedItem.put("priceDate", date);
                            savedItem.put("closePrice", lastClosePrice);
                            savedItem.put("instrumentType", "FUTURE");
                            
                            savedItems.add(savedItem);
                            newItemsSaved++;
                        } else {
                            invalidItemsFiltered++;
                        }
                    } else {
                        missingData++;
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка обработки фьючерса " + future.getTicker() + ": " + e.getMessage());
                    missingData++;
                }
            }
            
            response.put("success", true);
            response.put("message", "Успешно сохранено " + newItemsSaved + " новых цен вечерней сессии для фьючерсов за " + date);
            response.put("date", date);
            response.put("totalProcessed", totalProcessed);
            response.put("newItemsSaved", newItemsSaved);
            response.put("existingItemsSkipped", existingItemsSkipped);
            response.put("invalidItemsFiltered", invalidItemsFiltered);
            response.put("missingData", missingData);
            response.put("savedItems", savedItems);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка сохранения цен вечерней сессии для фьючерсов за " + date + ": " + e.getMessage());
            response.put("date", date);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

     /**
     * Получение цены вечерней сессии по инструменту за конкретную дату.
     * Использует данные из minute_candles
     */
    @GetMapping("/{figi}/{date}")
    public ResponseEntity<Map<String, Object>> getEveningSessionPriceByFigiAndDate(
            @PathVariable String figi,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            
            // Получаем последнюю свечу за день для инструмента
            var lastCandle = minuteCandleRepository.findLastCandleForDate(figi, date);
            
            if (lastCandle != null) {
                BigDecimal lastClosePrice = lastCandle.getClose();
                
                // Проверяем, что цена не равна 0 (невалидная цена)
                if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                    Map<String, Object> priceData = new HashMap<>();
                    priceData.put("figi", figi);
                    priceData.put("priceDate", date);
                    priceData.put("closePrice", lastClosePrice);
                    priceData.put("instrumentType", "UNKNOWN"); // Не знаем тип без дополнительного запроса
                    priceData.put("currency", "RUB");
                    priceData.put("exchange", "MOEX");
                    
                    response.put("success", true);
                    response.put("message", "Цена вечерней сессии для инструмента " + figi + " за " + date + " получена успешно");
                    response.put("data", priceData);
                    response.put("figi", figi);
                    response.put("date", date);
                    response.put("timestamp", LocalDateTime.now());
                    
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", "Невалидная цена закрытия для инструмента " + figi + " за " + date);
                    response.put("figi", figi);
                    response.put("date", date);
                    response.put("timestamp", LocalDateTime.now());
                    return ResponseEntity.ok(response);
                }
            } else {
                response.put("success", false);
                response.put("message", "Цена вечерней сессии не найдена для инструмента " + figi + " за " + date);
                response.put("figi", figi);
                response.put("date", date);
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка получения цены вечерней сессии для инструмента " + figi + " за " + date + ": " + e.getMessage());
            response.put("figi", figi);
            response.put("date", date);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Сохранение цены вечерней сессии по инструменту за конкретную дату.
     * Использует данные из minute_candles и сохраняет в БД
     */
    @PostMapping("/{figi}/{date}")
    public ResponseEntity<Map<String, Object>> saveEveningSessionPriceByFigiAndDate(
            @PathVariable String figi,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            
            // Проверяем, есть ли уже запись для этой даты и FIGI
            if (closePriceEveningSessionRepository.existsByPriceDateAndFigi(date, figi)) {
                response.put("success", false);
                response.put("message", "Запись уже существует для инструмента " + figi + " за " + date);
                response.put("figi", figi);
                response.put("date", date);
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.ok(response);
            }
            
            // Получаем последнюю свечу за день для инструмента
            var lastCandle = minuteCandleRepository.findLastCandleForDate(figi, date);
            
            if (lastCandle != null) {
                BigDecimal lastClosePrice = lastCandle.getClose();
                
                // Проверяем, что цена не равна 0 (невалидная цена)
                if (lastClosePrice.compareTo(BigDecimal.ZERO) > 0) {
                    // Определяем тип инструмента (акция или фьючерс)
                    String instrumentType = "UNKNOWN";
                    String exchange = "MOEX";
                    
                    // Проверяем, есть ли инструмент в таблице акций
                    boolean isShare = shareRepository.findAll().stream()
                            .anyMatch(share -> share.getFigi().equals(figi));
                    boolean isFuture = futureRepository.findAll().stream()
                            .anyMatch(future -> future.getFigi().equals(figi));
                    
                    if (isShare) {
                        instrumentType = "SHARE";
                        exchange = "MOEX";
                    } else if (isFuture) {
                        instrumentType = "FUTURE";
                        exchange = "MOEX";
                    }
                    
                    // Создаем запись для сохранения
                    ClosePriceEveningSessionEntity entity = new ClosePriceEveningSessionEntity();
                    entity.setFigi(figi);
                    entity.setPriceDate(date);
                    entity.setClosePrice(lastClosePrice);
                    entity.setInstrumentType(instrumentType);
                    entity.setCurrency("RUB");
                    entity.setExchange(exchange);
                    
                    closePriceEveningSessionRepository.save(entity);
                    
                    Map<String, Object> savedItem = new HashMap<>();
                    savedItem.put("figi", figi);
                    savedItem.put("priceDate", date);
                    savedItem.put("closePrice", lastClosePrice);
                    savedItem.put("instrumentType", instrumentType);
                    savedItem.put("currency", "RUB");
                    savedItem.put("exchange", exchange);
                    
                    response.put("success", true);
                    response.put("message", "Цена вечерней сессии для инструмента " + figi + " за " + date + " сохранена успешно");
                    response.put("data", savedItem);
                    response.put("figi", figi);
                    response.put("date", date);
                    response.put("timestamp", LocalDateTime.now());
                    
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", "Невалидная цена закрытия для инструмента " + figi + " за " + date);
                    response.put("figi", figi);
                    response.put("date", date);
                    response.put("timestamp", LocalDateTime.now());
                    return ResponseEntity.ok(response);
                }
            } else {
                response.put("success", false);
                response.put("message", "Цена вечерней сессии не найдена для инструмента " + figi + " за " + date);
                response.put("figi", figi);
                response.put("date", date);
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка сохранения цены вечерней сессии для инструмента " + figi + " за " + date + ": " + e.getMessage());
            response.put("figi", figi);
            response.put("date", date);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
