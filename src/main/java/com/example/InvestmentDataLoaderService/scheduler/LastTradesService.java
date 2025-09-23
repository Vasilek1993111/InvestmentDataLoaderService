package com.example.InvestmentDataLoaderService.scheduler;

import com.example.InvestmentDataLoaderService.dto.LastTradeDto;
import com.example.InvestmentDataLoaderService.dto.LastTradesRequestDto;
import com.example.InvestmentDataLoaderService.dto.LastTradesResponseDto;
import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.entity.LastPriceEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.repository.LastPriceRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.service.LastTradeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class LastTradesService {

    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final LastPriceRepository lastPriceRepository;
    private final LastTradeService lastTradeService;

    public LastTradesService(ShareRepository shareRepository, 
                           FutureRepository futureRepository,
                           LastPriceRepository lastPriceRepository,
                           LastTradeService lastTradeService) {
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.lastPriceRepository = lastPriceRepository;
        this.lastTradeService = lastTradeService;
    }

    /**
     * Ежедневная загрузка обезличенных сделок
     * Запускается в 3:00 по московскому времени
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Moscow")
    public void fetchAndStoreLastTrades() {
        try {
            LocalDate previousDay = LocalDate.now(ZoneId.of("Europe/Moscow")).minusDays(1);
            String taskId = "LAST_TRADES_" + UUID.randomUUID().toString().substring(0, 8);
            
            System.out.println("=== НАЧАЛО ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ===");
            System.out.println("[" + taskId + "] Дата: " + previousDay);
            System.out.println("[" + taskId + "] Время запуска: " + LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            
            // Торги проводятся в выходные дни, поэтому проверку убираем
            
            SaveResponseDto response = processLastTrades(previousDay, taskId);
            
            System.out.println("[" + taskId + "] Загрузка завершена:");
            System.out.println("[" + taskId + "] - Успех: " + response.isSuccess());
            System.out.println("[" + taskId + "] - Сообщение: " + response.getMessage());
            System.out.println("[" + taskId + "] - Всего запрошено: " + response.getTotalRequested());
            System.out.println("[" + taskId + "] - Сохранено новых: " + response.getNewItemsSaved());
            System.out.println("[" + taskId + "] - Пропущено существующих: " + response.getExistingItemsSkipped());
            System.out.println("=== ЗАВЕРШЕНИЕ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ===");
            
        } catch (Exception e) {
            System.err.println("Критическая ошибка в загрузке обезличенных сделок: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Обрабатывает обезличенные сделки для указанной даты
     */
    public SaveResponseDto processLastTrades(LocalDate date, String taskId) {
        try {
            System.out.println("[" + taskId + "] Начало обработки обезличенных сделок за " + date);
            
            // Получаем все акции и фьючерсы из БД
            List<ShareEntity> shares = shareRepository.findAll();
            List<FutureEntity> futures = futureRepository.findAll();
            System.out.println("[" + taskId + "] Найдено " + shares.size() + " акций и " + futures.size() + " фьючерсов для обработки");
            
            List<LastTradesResponseDto> savedItems = new ArrayList<>();
            int totalRequested = 0;
            int savedCount = 0;
            int existingCount = 0;
            int processedInstruments = 0;
            
            // Обрабатываем акции
            for (ShareEntity share : shares) {
                try {
                    processedInstruments++;
                    System.out.println("[" + taskId + "] Обработка акции " + processedInstruments + "/" + (shares.size() + futures.size()) + ": " + share.getTicker() + " (" + share.getFigi() + ")");
                    
                    List<LastTradesResponseDto> trades = fetchLastTradesForInstrument(share.getFigi(), date, "moex_mrng_evng_e_wknd_dlr", taskId);
                    
                    for (LastTradesResponseDto trade : trades) {
                        totalRequested++;
                        
                        // Проверяем, есть ли уже запись для этого FIGI и времени
                        if (lastPriceRepository.existsById(new com.example.InvestmentDataLoaderService.entity.LastPriceKey(trade.getFigi(), trade.getTime()))) {
                            existingCount++;
                            continue;
                        }
                        
                        // Сохраняем в БД
                        LastPriceEntity entity = new LastPriceEntity(
                            trade.getFigi(),
                            trade.getTime(),
                            trade.getPrice(),
                            trade.getCurrency(),
                            trade.getExchange()
                        );
                        
                        lastPriceRepository.save(entity);
                        savedItems.add(trade);
                        savedCount++;
                    }
                    
                } catch (Exception e) {
                    System.err.println("[" + taskId + "] Ошибка обработки акции " + share.getTicker() + ": " + e.getMessage());
                }
            }
            
            // Обрабатываем фьючерсы
            for (FutureEntity future : futures) {
                try {
                    processedInstruments++;
                    System.out.println("[" + taskId + "] Обработка фьючерса " + processedInstruments + "/" + (shares.size() + futures.size()) + ": " + future.getTicker() + " (" + future.getFigi() + ")");
                    
                    List<LastTradesResponseDto> trades = fetchLastTradesForInstrument(future.getFigi(), date, "FORTS_EVENING", taskId);
                    
                    for (LastTradesResponseDto trade : trades) {
                        totalRequested++;
                        
                        // Проверяем, есть ли уже запись для этого FIGI и времени
                        if (lastPriceRepository.existsById(new com.example.InvestmentDataLoaderService.entity.LastPriceKey(trade.getFigi(), trade.getTime()))) {
                            existingCount++;
                            continue;
                        }
                        
                        // Сохраняем в БД
                        LastPriceEntity entity = new LastPriceEntity(
                            trade.getFigi(),
                            trade.getTime(),
                            trade.getPrice(),
                            trade.getCurrency(),
                            trade.getExchange()
                        );
                        
                        lastPriceRepository.save(entity);
                        savedItems.add(trade);
                        savedCount++;
                    }
                    
                } catch (Exception e) {
                    System.err.println("[" + taskId + "] Ошибка обработки фьючерса " + future.getTicker() + ": " + e.getMessage());
                }
            }
            
            System.out.println("[" + taskId + "] Обработка завершена:");
            System.out.println("[" + taskId + "] - Обработано инструментов: " + processedInstruments + " (акций: " + shares.size() + ", фьючерсов: " + futures.size() + ")");
            System.out.println("[" + taskId + "] - Запрошено сделок: " + totalRequested);
            System.out.println("[" + taskId + "] - Сохранено новых: " + savedCount);
            System.out.println("[" + taskId + "] - Пропущено существующих: " + existingCount);
            
            return new SaveResponseDto(
                true,
                "Обезличенные сделки загружены. Сохранено: " + savedCount + ", пропущено: " + existingCount,
                totalRequested,
                savedCount,
                existingCount,
                0, // invalidItemsFiltered
                0, // missingFromApi
                savedItems
            );
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Критическая ошибка в обработке обезличенных сделок: " + e.getMessage());
            e.printStackTrace();
            return new SaveResponseDto(
                false,
                "Ошибка загрузки обезличенных сделок: " + e.getMessage(),
                0,
                0,
                0,
                0, // invalidItemsFiltered
                0, // missingFromApi
                new ArrayList<>()
            );
        }
    }

    /**
     * Загружает обезличенные сделки для конкретного инструмента за указанную дату
     */
    private List<LastTradesResponseDto> fetchLastTradesForInstrument(String figi, LocalDate date, String exchange, String taskId) {
        try {
            System.out.println("[" + taskId + "] Загрузка обезличенных сделок для " + figi + " за дату: " + date);
            
            // Вызываем T-Invest API для получения обезличенных сделок
            List<LastTradeDto> tradesFromApi = lastTradeService.getLastTrades(figi, date, "TRADE_SOURCE_ALL");
            
            // Получаем валюту инструмента из базы данных
            String currency = "RUB"; // По умолчанию
            ShareEntity share = shareRepository.findById(figi).orElse(null);
            if (share != null) {
                currency = share.getCurrency();
            } else {
                FutureEntity future = futureRepository.findById(figi).orElse(null);
                if (future != null) {
                    currency = future.getCurrency();
                }
            }
            
            // Конвертируем в LastTradesResponseDto
            List<LastTradesResponseDto> trades = new ArrayList<>();
            for (LastTradeDto trade : tradesFromApi) {
                trades.add(new LastTradesResponseDto(
                    trade.figi(),
                    trade.time().atZone(ZoneId.of("Europe/Moscow")).toLocalDateTime(),
                    trade.price(),
                    currency,
                    exchange
                ));
            }
            
            System.out.println("[" + taskId + "] Загружено " + trades.size() + " сделок для " + figi);
            return trades;
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка загрузки обезличенных сделок для " + figi + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<LastTradesResponseDto> fetchLastTradesForLastHour(String figi, String exchange, String taskId) {
        try {
            System.out.println("[" + taskId + "] Загрузка обезличенных сделок за последний час для " + figi);
            
            // Вызываем T-Invest API для получения обезличенных сделок за последний час
            List<LastTradeDto> tradesFromApi = lastTradeService.getLastTradesForLastHour(figi, "TRADE_SOURCE_ALL");
            
            // Получаем валюту инструмента из базы данных
            String currency = "RUB"; // По умолчанию
            ShareEntity share = shareRepository.findById(figi).orElse(null);
            if (share != null) {
                currency = share.getCurrency();
            } else {
                FutureEntity future = futureRepository.findById(figi).orElse(null);
                if (future != null) {
                    currency = future.getCurrency();
                }
            }
            
            // Конвертируем в LastTradesResponseDto
            List<LastTradesResponseDto> trades = new ArrayList<>();
            for (LastTradeDto trade : tradesFromApi) {
                trades.add(new LastTradesResponseDto(
                    trade.figi(),
                    trade.time().atZone(ZoneId.of("Europe/Moscow")).toLocalDateTime(),
                    trade.price(),
                    currency,
                    exchange
                ));
            }
            
            System.out.println("[" + taskId + "] Загружено " + trades.size() + " сделок за последний час для " + figi);
            return trades;
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка загрузки обезличенных сделок за последний час для " + figi + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }


    /**
     * Ручная загрузка обезличенных сделок за конкретную дату
     */
    public SaveResponseDto fetchAndStoreLastTradesForDate(LocalDate date) {
        String taskId = "MANUAL_LAST_TRADES_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Торги проводятся в выходные дни, поэтому проверку убираем
        
        return processLastTrades(date, taskId);
    }

    /**
     * Ручная загрузка обезличенных сделок для конкретного инструмента за дату
     */
    public SaveResponseDto fetchAndStoreLastTradesForInstrument(String figi, LocalDate date) {
        String taskId = "MANUAL_LAST_TRADES_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Торги проводятся в выходные дни, поэтому проверку убираем
        
        try {
            System.out.println("[" + taskId + "] Начало обработки обезличенных сделок для " + figi + " за " + date);
            
            List<LastTradesResponseDto> savedItems = new ArrayList<>();
            int totalRequested = 0;
            int savedCount = 0;
            int existingCount = 0;
            
            // Определяем биржу по типу инструмента
            String exchange = "moex_mrng_evng_e_wknd_dlr"; // По умолчанию для акций
            
            List<LastTradesResponseDto> trades = fetchLastTradesForInstrument(figi, date, exchange, taskId);
            
            for (LastTradesResponseDto trade : trades) {
                totalRequested++;
                
                // Проверяем, есть ли уже запись для этого FIGI и времени
                if (lastPriceRepository.existsById(new com.example.InvestmentDataLoaderService.entity.LastPriceKey(trade.getFigi(), trade.getTime()))) {
                    existingCount++;
                    continue;
                }
                
                // Сохраняем в БД
                LastPriceEntity entity = new LastPriceEntity(
                    trade.getFigi(),
                    trade.getTime(),
                    trade.getPrice(),
                    trade.getCurrency(),
                    trade.getExchange()
                );
                
                lastPriceRepository.save(entity);
                savedItems.add(trade);
                savedCount++;
            }
            
            System.out.println("[" + taskId + "] Обработка завершена:");
            System.out.println("[" + taskId + "] - Запрошено сделок: " + totalRequested);
            System.out.println("[" + taskId + "] - Сохранено новых: " + savedCount);
            System.out.println("[" + taskId + "] - Пропущено существующих: " + existingCount);
            
            return new SaveResponseDto(
                true,
                "Обезличенные сделки для " + figi + " загружены. Сохранено: " + savedCount + ", пропущено: " + existingCount,
                totalRequested,
                savedCount,
                existingCount,
                0, // invalidItemsFiltered
                0, // missingFromApi
                savedItems
            );
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Критическая ошибка в обработке обезличенных сделок для " + figi + ": " + e.getMessage());
            e.printStackTrace();
            return new SaveResponseDto(
                false,
                "Ошибка загрузки обезличенных сделок для " + figi + ": " + e.getMessage(),
                0,
                0,
                0,
                0, // invalidItemsFiltered
                0, // missingFromApi
                new ArrayList<>()
            );
        }
    }

    /**
     * Загрузка обезличенных сделок за последний час по запросу с телом
     */
    public SaveResponseDto fetchAndStoreLastTradesByRequest(LastTradesRequestDto request) {
        String taskId = "REQUEST_LAST_TRADES_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Проверяем обязательные параметры
        if (request.getFigis() == null || request.getFigis().isEmpty()) {
            String message = "Параметр 'figis' является обязательным и не может быть пустым";
            System.out.println("[" + taskId + "] " + message);
            return new SaveResponseDto(
                false,
                message,
                0,
                0,
                0,
                0, // invalidItemsFiltered
                0, // missingFromApi
                new ArrayList<>()
            );
        }
        
        try {
            System.out.println("[" + taskId + "] Начало обработки обезличенных сделок за последний час для " + request.getFigis().size() + " инструментов");
            
            List<LastTradesResponseDto> savedItems = new ArrayList<>();
            int totalRequested = 0;
            int savedCount = 0;
            int existingCount = 0;
            int processedInstruments = 0;
            
            // Определяем список инструментов для обработки
            List<String> instrumentsToProcess;
            if (request.isLoadAll()) {
                System.out.println("[" + taskId + "] Загружаем обезличенные сделки для всех инструментов");
                instrumentsToProcess = new ArrayList<>();
                
                // Добавляем все акции
                List<ShareEntity> shares = shareRepository.findAll();
                for (ShareEntity share : shares) {
                    instrumentsToProcess.add(share.getFigi());
                }
                
                // Добавляем все фьючерсы
                List<FutureEntity> futures = futureRepository.findAll();
                for (FutureEntity future : futures) {
                    instrumentsToProcess.add(future.getFigi());
                }
                
                System.out.println("[" + taskId + "] Найдено " + instrumentsToProcess.size() + " инструментов для обработки");
            } else if (request.isLoadAllShares()) {
                System.out.println("[" + taskId + "] Загружаем обезличенные сделки для всех акций");
                instrumentsToProcess = new ArrayList<>();
                
                // Добавляем только акции
                List<ShareEntity> shares = shareRepository.findAll();
                for (ShareEntity share : shares) {
                    instrumentsToProcess.add(share.getFigi());
                }
                
                System.out.println("[" + taskId + "] Найдено " + instrumentsToProcess.size() + " акций для обработки");
            } else if (request.isLoadAllFutures()) {
                System.out.println("[" + taskId + "] Загружаем обезличенные сделки для всех фьючерсов");
                instrumentsToProcess = new ArrayList<>();
                
                // Добавляем только фьючерсы
                List<FutureEntity> futures = futureRepository.findAll();
                for (FutureEntity future : futures) {
                    instrumentsToProcess.add(future.getFigi());
                }
                
                System.out.println("[" + taskId + "] Найдено " + instrumentsToProcess.size() + " фьючерсов для обработки");
            } else {
                instrumentsToProcess = request.getFigis();
            }
            
            // Обрабатываем инструменты
            for (String figi : instrumentsToProcess) {
                try {
                    processedInstruments++;
                    System.out.println("[" + taskId + "] Обработка инструмента " + processedInstruments + "/" + instrumentsToProcess.size() + ": " + figi);
                    
                    // Определяем тип инструмента и биржу
                    String exchange = "moex_mrng_evng_e_wknd_dlr"; // По умолчанию для акций
                    
                    // Проверяем, является ли это фьючерсом
                    if (futureRepository.existsById(figi)) {
                        exchange = "FORTS_EVENING";
                    }
                    
                    // Получаем сделки за последний час
                    List<LastTradesResponseDto> trades = fetchLastTradesForLastHour(figi, exchange, taskId);
                        
                        for (LastTradesResponseDto trade : trades) {
                            totalRequested++;
                            
                            // Проверяем, есть ли уже запись для этого FIGI и времени
                            if (lastPriceRepository.existsById(new com.example.InvestmentDataLoaderService.entity.LastPriceKey(trade.getFigi(), trade.getTime()))) {
                                existingCount++;
                                continue;
                            }
                            
                            // Сохраняем в БД
                            LastPriceEntity entity = new LastPriceEntity(
                                trade.getFigi(),
                                trade.getTime(),
                                trade.getPrice(),
                                trade.getCurrency(),
                                trade.getExchange()
                            );
                            
                            lastPriceRepository.save(entity);
                            savedItems.add(trade);
                            savedCount++;
                        }
                        
                } catch (Exception e) {
                    System.err.println("[" + taskId + "] Ошибка обработки инструмента " + figi + ": " + e.getMessage());
                }
            }
            
            System.out.println("[" + taskId + "] Обработка завершена:");
            System.out.println("[" + taskId + "] - Обработано инструментов: " + processedInstruments);
            System.out.println("[" + taskId + "] - Запрошено сделок: " + totalRequested);
            System.out.println("[" + taskId + "] - Сохранено новых: " + savedCount);
            System.out.println("[" + taskId + "] - Пропущено существующих: " + existingCount);
            
            return new SaveResponseDto(
                true,
                "Обезличенные сделки загружены по запросу. Сохранено: " + savedCount + ", пропущено: " + existingCount,
                totalRequested,
                savedCount,
                existingCount,
                0, // invalidItemsFiltered
                0, // missingFromApi
                savedItems
            );
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Критическая ошибка в обработке обезличенных сделок по запросу: " + e.getMessage());
            e.printStackTrace();
            return new SaveResponseDto(
                false,
                "Ошибка загрузки обезличенных сделок по запросу: " + e.getMessage(),
                0,
                0,
                0,
                0, // invalidItemsFiltered
                0, // missingFromApi
                new ArrayList<>()
            );
        }
    }

    /**
     * Асинхронная загрузка обезличенных сделок за последний час
     */
    public void fetchAndStoreLastTradesByRequestAsync(LastTradesRequestDto request) {
        CompletableFuture.runAsync(() -> {
            try {
                System.out.println("=== НАЧАЛО АСИНХРОННОЙ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ===");
                SaveResponseDto result = fetchAndStoreLastTradesByRequest(request);
                System.out.println("=== ЗАВЕРШЕНИЕ АСИНХРОННОЙ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ===");
                System.out.println("Результат: " + result.getMessage());
            } catch (Exception e) {
                System.err.println("=== ОШИБКА АСИНХРОННОЙ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ===");
                System.err.println("Ошибка: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Асинхронная загрузка обезличенных сделок только по акциям за последний час
     */
    public void fetchAndStoreLastTradesSharesAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                System.out.println("=== НАЧАЛО АСИНХРОННОЙ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ПО АКЦИЯМ ===");
                
                // Создаем запрос только для акций
                LastTradesRequestDto request = new LastTradesRequestDto();
                request.setFigis(java.util.Arrays.asList("ALL_SHARES"));
                request.setTradeSource("TRADE_SOURCE_ALL");
                
                SaveResponseDto result = fetchAndStoreLastTradesByRequest(request);
                System.out.println("=== ЗАВЕРШЕНИЕ АСИНХРОННОЙ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ПО АКЦИЯМ ===");
                System.out.println("Результат: " + result.getMessage());
                System.out.println("Обработано инструментов: " + result.getTotalRequested());
                System.out.println("Сохранено новых: " + result.getNewItemsSaved());
                System.out.println("Пропущено существующих: " + result.getExistingItemsSkipped());
            } catch (Exception e) {
                System.err.println("=== ОШИБКА АСИНХРОННОЙ ЗАГРУЗКИ ОБЕЗЛИЧЕННЫХ СДЕЛОК ПО АКЦИЯМ ===");
                System.err.println("Ошибка: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
