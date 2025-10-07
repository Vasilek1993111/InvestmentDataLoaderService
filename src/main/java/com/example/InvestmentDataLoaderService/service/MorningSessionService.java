package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.OpenPriceDto;
import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.entity.MinuteCandleEntity;
import com.example.InvestmentDataLoaderService.entity.OpenPriceEntity;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.IndicativeEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.repository.OpenPriceRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.IndicativeRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.MinuteCandleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MorningSessionService {

    private final ShareRepository shareRepository;
    private final FutureRepository futureRepository;
    private final IndicativeRepository indicativeRepository;
    private final MinuteCandleRepository minuteCandleRepository;
    private final OpenPriceRepository openPriceRepository;

    public MorningSessionService(ShareRepository shareRepository, 
                               FutureRepository futureRepository,
                               IndicativeRepository indicativeRepository,
                               OpenPriceRepository openPriceRepository,
                               MinuteCandleRepository minuteCandleRepository) {
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.indicativeRepository = indicativeRepository;
        this.openPriceRepository = openPriceRepository;
        this.minuteCandleRepository = minuteCandleRepository;
    }

    /**
     * Обрабатывает цены открытия утренней сессии для указанной даты
     */
    public SaveResponseDto processMorningSessionPrices(LocalDate date, String taskId) {
        return processMorningSessionPrices(date, taskId, true);
    }

    /**
     * Обрабатывает цены открытия утренней сессии для указанной даты
     * includeIndicatives: включать ли индикативные инструменты
     */
    public SaveResponseDto processMorningSessionPrices(LocalDate date, String taskId, boolean includeIndicatives) {
        try {
            System.out.println("[" + taskId + "] Начало обработки цен открытия утренней сессии за " + date);
            
            // Получаем все акции, фьючерсы и индикативные инструменты из БД
            List<ShareEntity> shares = shareRepository.findAll();
            List<FutureEntity> futures = futureRepository.findAll();
            List<IndicativeEntity> indicatives = includeIndicatives ? indicativeRepository.findAll() : new ArrayList<>();
            System.out.println("[" + taskId + "] Найдено " + shares.size() + " акций, " + futures.size() + " фьючерсов и " + indicatives.size() + " индикативных инструментов для обработки");
            
            List<OpenPriceDto> savedItems = new ArrayList<>();
            int totalRequested = 0;
            int savedCount = 0;
            int existingCount = 0;
            int processedInstruments = 0;
            
            // Обрабатываем акции
            for (ShareEntity share : shares) {
                try {
                    processedInstruments++;
                    System.out.println("[" + taskId + "] Обработка акции " + processedInstruments + "/" + (shares.size() + futures.size()) + ": " + share.getTicker() + " (" + share.getFigi() + ")");
                    
                    // Проверяем, есть ли уже запись для этой даты и FIGI
                    if (openPriceRepository.existsByPriceDateAndFigi(date, share.getFigi())) {
                        existingCount++;
                        System.out.println("[" + taskId + "] Запись уже существует для " + share.getTicker() + " за " + date);
                        continue;
                    }
                    
                    // Ищем первую свечу за указанную дату до 06:59:59
                    BigDecimal firstOpenPrice = findFirstOpenPriceForDate(share.getFigi(), date, taskId, "share");
                    
                    if (firstOpenPrice != null) {
                        totalRequested++;
                        
                        // Создаем DTO для сохранения
                        OpenPriceDto dto = new OpenPriceDto(
                            share.getFigi(),
                            date,
                            firstOpenPrice,
                            "share",
                            "RUB",
                            "moex_mrng_evng_e_wknd_dlr"
                        );
                        
                        // Сохраняем в БД
                        OpenPriceEntity entity = new OpenPriceEntity();
                        entity.setId(new com.example.InvestmentDataLoaderService.entity.OpenPriceKey(date, share.getFigi()));
                        entity.setInstrumentType(dto.instrumentType());
                        entity.setOpenPrice(dto.openPrice());
                        entity.setCurrency(dto.currency());
                        entity.setExchange(dto.exchange());
                        
                        openPriceRepository.save(entity);
                        savedItems.add(dto);
                        savedCount++;
                        
                        System.out.println("[" + taskId + "] Сохранена цена открытия для " + share.getTicker() + ": " + firstOpenPrice);
                    } else {
                        System.out.println("[" + taskId + "] Не найдена свеча для " + share.getTicker() + " за " + date);
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
                    
                    // Проверяем, есть ли уже запись для этой даты и FIGI
                    if (openPriceRepository.existsByPriceDateAndFigi(date, future.getFigi())) {
                        existingCount++;
                        System.out.println("[" + taskId + "] Запись уже существует для " + future.getTicker() + " за " + date);
                        continue;
                    }
                    
                    // Ищем первую свечу за указанную дату до 08:59:59
                    BigDecimal firstOpenPrice = findFirstOpenPriceForDate(future.getFigi(), date, taskId, "future");
                    
                    if (firstOpenPrice != null) {
                        totalRequested++;
                        
                        // Создаем DTO для сохранения
                        OpenPriceDto dto = new OpenPriceDto(
                            future.getFigi(),
                            date,
                            firstOpenPrice,
                            "future",
                            "RUB",
                            "FORTS_EVENING"
                        );
                        
                        // Сохраняем в БД
                        OpenPriceEntity entity = new OpenPriceEntity();
                        entity.setId(new com.example.InvestmentDataLoaderService.entity.OpenPriceKey(date, future.getFigi()));
                        entity.setInstrumentType(dto.instrumentType());
                        entity.setOpenPrice(dto.openPrice());
                        entity.setCurrency(dto.currency());
                        entity.setExchange(dto.exchange());
                        
                        openPriceRepository.save(entity);
                        savedItems.add(dto);
                        savedCount++;
                        
                        System.out.println("[" + taskId + "] Сохранена цена открытия для " + future.getTicker() + ": " + firstOpenPrice);
                    } else {
                        System.out.println("[" + taskId + "] Не найдена свеча для " + future.getTicker() + " за " + date);
                    }
                    
                } catch (Exception e) {
                    System.err.println("[" + taskId + "] Ошибка обработки фьючерса " + future.getTicker() + ": " + e.getMessage());
                }
            }
            
            // Обрабатываем индикативные инструменты (опционально)
            if (includeIndicatives) {
                for (IndicativeEntity indicative : indicatives) {
                    // Пропускаем пустые или null FIGI
                    if (indicative.getFigi() == null || indicative.getFigi().trim().isEmpty()) {
                        System.err.println("[" + taskId + "] Skipping empty or null FIGI for indicative: " + indicative.getTicker());
                        continue;
                    }
                    
                    try {
                        processedInstruments++;
                        System.out.println("[" + taskId + "] Обработка индикативного инструмента " + processedInstruments + "/" + (shares.size() + futures.size() + indicatives.size()) + ": " + indicative.getTicker() + " (" + indicative.getFigi() + ")");
                        
                        // Проверяем, есть ли уже запись для этой даты и FIGI
                        if (openPriceRepository.existsByPriceDateAndFigi(date, indicative.getFigi())) {
                            existingCount++;
                            System.out.println("[" + taskId + "] Запись уже существует для " + indicative.getTicker() + " за " + date);
                            continue;
                        }
                        
                        BigDecimal firstOpenPrice = findFirstOpenPriceForDate(indicative.getFigi(), date, taskId, "indicative");
                        
                        if (firstOpenPrice != null) {
                            totalRequested++;
                            
                            // Создаем DTO для сохранения
                            OpenPriceDto dto = new OpenPriceDto(
                                indicative.getFigi(),
                                date,
                                firstOpenPrice,
                                "indicative",
                                indicative.getCurrency() != null ? indicative.getCurrency() : "USD",
                                "MOEX"
                            );
                            
                            // Сохраняем в БД
                            OpenPriceEntity entity = new OpenPriceEntity();
                            entity.setId(new com.example.InvestmentDataLoaderService.entity.OpenPriceKey(date, indicative.getFigi()));
                            entity.setInstrumentType(dto.instrumentType());
                            entity.setOpenPrice(dto.openPrice());
                            entity.setCurrency(dto.currency());
                            entity.setExchange(dto.exchange());
                            
                            openPriceRepository.save(entity);
                            savedItems.add(dto);
                            savedCount++;
                            
                            System.out.println("[" + taskId + "] Сохранена цена открытия для " + indicative.getTicker() + ": " + firstOpenPrice);
                        } else {
                            System.out.println("[" + taskId + "] Не найдена свеча для " + indicative.getTicker() + " за " + date);
                        }
                        
                    } catch (Exception e) {
                        System.err.println("[" + taskId + "] Ошибка обработки индикативного инструмента " + indicative.getTicker() + ": " + e.getMessage());
                    }
                }
            }
            
            System.out.println("[" + taskId + "] Обработка завершена:");
            System.out.println("[" + taskId + "] - Обработано инструментов: " + processedInstruments + " (акций: " + shares.size() + ", фьючерсов: " + futures.size() + ", индикативных: " + indicatives.size() + ")");
            System.out.println("[" + taskId + "] - Запрошено цен: " + totalRequested);
            System.out.println("[" + taskId + "] - Сохранено новых: " + savedCount);
            System.out.println("[" + taskId + "] - Пропущено существующих: " + existingCount);
            
            return new SaveResponseDto(
                true,
                "Цены открытия утренней сессии загружены. Сохранено: " + savedCount + ", пропущено: " + existingCount,
                totalRequested,
                savedCount,
                existingCount,
                0, // invalidItemsFiltered
                0, // missingFromApi
                savedItems
            );
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Критическая ошибка в обработке цен открытия утренней сессии: " + e.getMessage());
            e.printStackTrace();
            return new SaveResponseDto(
                false,
                "Ошибка загрузки цен открытия утренней сессии: " + e.getMessage(),
                0,
                0,
                0,
                0,
                0, // missingFromApi
                new ArrayList<>()
            );
        }
    }

    /**
     * Находит цену открытия как цену первой минутной свечи за сутки (по Москве) для указанной даты и FIGI
     * Без ограничений по времени: берется самая ранняя свеча в дне.
     */
    private BigDecimal findFirstOpenPriceForDate(String figi, LocalDate date, String taskId, String instrumentType) {
        try {
            System.out.println("[" + taskId + "] Поиск первой свечи за сутки для " + figi + " (" + instrumentType + ") за дату: " + date);
            
            // Старт и конец суток в зоне Europe/Moscow
            var zone = ZoneId.of("Europe/Moscow");
            var startOfDay = date.atStartOfDay(zone).toInstant();
            var startOfNextDay = date.plusDays(1).atStartOfDay(zone).toInstant();

            // Ищем минутные свечи за сутки и берём первую (самую раннюю)
            List<MinuteCandleEntity> minuteCandles = minuteCandleRepository.findByFigiAndTimeBetween(figi, startOfDay, startOfNextDay);

            if (!minuteCandles.isEmpty()) {
                MinuteCandleEntity earliestMinute = minuteCandles.get(0); // ORDER BY c.time ASC
                System.out.println("[" + taskId + "] Найдена первая минутная свеча для " + figi + " с ценой открытия: " + earliestMinute.getOpen());
                return earliestMinute.getOpen();
            } else {
                System.out.println("[" + taskId + "] Свечи не найдены для " + figi + " за дату: " + date);
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.println("[" + taskId + "] Ошибка поиска первой цены открытия для " + figi + ": " + e.getMessage());
            return null;
        }
    }


    /**
     * Ручная загрузка цен открытия утренней сессии за конкретную дату
     * Ищет первую минутную свечу в любой день
     */
    public SaveResponseDto fetchAndStoreMorningSessionPricesForDate(LocalDate date) {
        String taskId = "MANUAL_MORNING_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Для контроллера: без индикативов
        return processMorningSessionPrices(date, taskId, false);
    }

    /**
     * Предпросмотр цен открытия утренней сессии за дату без сохранения в БД
     * Ищет первую минутную свечу в любой день
     */
    public SaveResponseDto previewMorningSessionPricesForDate(LocalDate date) {
        String taskId = "PREVIEW_MORNING_" + UUID.randomUUID().toString().substring(0, 8);

        try {
            System.out.println("[" + taskId + "] Предпросмотр цен открытия за " + date);
            List<ShareEntity> shares = shareRepository.findAll();
            List<FutureEntity> futures = futureRepository.findAll();
            // Индикативы не включаем для GET/предпросмотра

            List<OpenPriceDto> items = new ArrayList<>();
            int totalRequested = 0;
            

            // Акции
            for (ShareEntity share : shares) {
                BigDecimal price = findFirstOpenPriceForDate(share.getFigi(), date, taskId, "share");
                if (price != null) {
                    totalRequested++;
                    items.add(new OpenPriceDto(
                        share.getFigi(),
                        date,
                        price,
                        "share",
                        "RUB",
                        "moex_mrng_evng_e_wknd_dlr"
                    ));
                }
            }

            // Фьючерсы
            for (FutureEntity future : futures) {
                BigDecimal price = findFirstOpenPriceForDate(future.getFigi(), date, taskId, "future");
                if (price != null) {
                    totalRequested++;
                    items.add(new OpenPriceDto(
                        future.getFigi(),
                        date,
                        price,
                        "future",
                        "RUB",
                        "FORTS_EVENING"
                    ));
                }
            }

            // Индикативы намеренно игнорируются

            return new SaveResponseDto(
                true,
                "Предпросмотр цен открытия без сохранения. Найдено элементов: " + items.size(),
                totalRequested,
                items.size(),
                0,
                0,
                0,
                items
            );

        } catch (Exception e) {
            return new SaveResponseDto(
                false,
                "Ошибка предпросмотра: " + e.getMessage(),
                0,
                0,
                0,
                0,
                0,
                new ArrayList<>()
            );
        }
    }

    /**
     * Загрузка цен открытия только по акциям за дату
     */
    public SaveResponseDto fetchAndStoreSharesPricesForDate(LocalDate date) {
        String taskId = "MANUAL_SHARES_" + UUID.randomUUID().toString().substring(0, 8);
        return processSharesPrices(date, taskId);
    }

    /**
     * Загрузка цен открытия только по фьючерсам за дату
     */
    public SaveResponseDto fetchAndStoreFuturesPricesForDate(LocalDate date) {
        String taskId = "MANUAL_FUTURES_" + UUID.randomUUID().toString().substring(0, 8);
        return processFuturesPrices(date, taskId);
    }

    /**
     * Предпросмотр цен открытия только по акциям за дату
     */
    public SaveResponseDto previewSharesPricesForDate(LocalDate date) {
        String taskId = "PREVIEW_SHARES_" + UUID.randomUUID().toString().substring(0, 8);
        return previewSharesPrices(date, taskId);
    }

    /**
     * Предпросмотр цен открытия только по фьючерсам за дату
     */
    public SaveResponseDto previewFuturesPricesForDate(LocalDate date) {
        String taskId = "PREVIEW_FUTURES_" + UUID.randomUUID().toString().substring(0, 8);
        return previewFuturesPrices(date, taskId);
    }

    /**
     * Обрабатывает только акции
     */
    private SaveResponseDto processSharesPrices(LocalDate date, String taskId) {
        try {
            System.out.println("[" + taskId + "] Обработка акций за " + date);
            List<ShareEntity> shares = shareRepository.findAll();
            System.out.println("[" + taskId + "] Найдено " + shares.size() + " акций для обработки");
            
            List<OpenPriceDto> savedItems = new ArrayList<>();
            int totalRequested = shares.size();
            int savedCount = 0;
            int existingCount = 0;
            
            for (ShareEntity share : shares) {
                try {
                    System.out.println("[" + taskId + "] Обработка акции: " + share.getTicker() + " (" + share.getFigi() + ")");
                    
                    if (openPriceRepository.existsByPriceDateAndFigi(date, share.getFigi())) {
                        existingCount++;
                        System.out.println("[" + taskId + "] Запись уже существует для " + share.getTicker() + " за " + date);
                        continue;
                    }
                    
                    BigDecimal firstOpenPrice = findFirstOpenPriceForDate(share.getFigi(), date, taskId, "share");
                    
                    if (firstOpenPrice != null) {
                        OpenPriceDto openPriceDto = new OpenPriceDto(
                            share.getFigi(),
                            date,
                            firstOpenPrice,
                            "share",
                            share.getCurrency(),
                            share.getExchange()
                        );
                        
                        openPriceRepository.save(new OpenPriceEntity(
                            openPriceDto.priceDate(),
                            openPriceDto.figi(),
                            openPriceDto.instrumentType(),
                            openPriceDto.openPrice(),
                            openPriceDto.currency(),
                            openPriceDto.exchange()
                        ));
                        savedItems.add(openPriceDto);
                        savedCount++;
                        System.out.println("[" + taskId + "] Сохранена цена открытия для " + share.getTicker() + ": " + firstOpenPrice);
                    } else {
                        System.out.println("[" + taskId + "] Цена открытия не найдена для " + share.getTicker());
                    }
                    
                } catch (Exception e) {
                    System.err.println("[" + taskId + "] Ошибка обработки акции " + share.getTicker() + ": " + e.getMessage());
                }
            }
            
            return new SaveResponseDto(
                true,
                "Обработка акций завершена. Сохранено: " + savedCount + ", пропущено: " + existingCount,
                totalRequested,
                savedCount,
                existingCount,
                0,
                0,
                savedItems
            );
            
        } catch (Exception e) {
            return new SaveResponseDto(
                false,
                "Ошибка обработки акций: " + e.getMessage(),
                0,
                0,
                0,
                0,
                0,
                new ArrayList<>()
            );
        }
    }

    /**
     * Обрабатывает только фьючерсы
     */
    private SaveResponseDto processFuturesPrices(LocalDate date, String taskId) {
        try {
            System.out.println("[" + taskId + "] Обработка фьючерсов за " + date);
            List<FutureEntity> futures = futureRepository.findAll();
            System.out.println("[" + taskId + "] Найдено " + futures.size() + " фьючерсов для обработки");
            
            List<OpenPriceDto> savedItems = new ArrayList<>();
            int totalRequested = futures.size();
            int savedCount = 0;
            int existingCount = 0;
            
            for (FutureEntity future : futures) {
                try {
                    System.out.println("[" + taskId + "] Обработка фьючерса: " + future.getTicker() + " (" + future.getFigi() + ")");
                    
                    if (openPriceRepository.existsByPriceDateAndFigi(date, future.getFigi())) {
                        existingCount++;
                        System.out.println("[" + taskId + "] Запись уже существует для " + future.getTicker() + " за " + date);
                        continue;
                    }
                    
                    BigDecimal firstOpenPrice = findFirstOpenPriceForDate(future.getFigi(), date, taskId, "future");
                    
                    if (firstOpenPrice != null) {
                        OpenPriceDto openPriceDto = new OpenPriceDto(
                            future.getFigi(),
                            date,
                            firstOpenPrice,
                            "future",
                            future.getCurrency(),
                            future.getExchange()
                        );
                        
                        openPriceRepository.save(new OpenPriceEntity(
                            openPriceDto.priceDate(),
                            openPriceDto.figi(),
                            openPriceDto.instrumentType(),
                            openPriceDto.openPrice(),
                            openPriceDto.currency(),
                            openPriceDto.exchange()
                        ));
                        savedItems.add(openPriceDto);
                        savedCount++;
                        System.out.println("[" + taskId + "] Сохранена цена открытия для " + future.getTicker() + ": " + firstOpenPrice);
                    } else {
                        System.out.println("[" + taskId + "] Цена открытия не найдена для " + future.getTicker());
                    }
                    
                } catch (Exception e) {
                    System.err.println("[" + taskId + "] Ошибка обработки фьючерса " + future.getTicker() + ": " + e.getMessage());
                }
            }
            
            return new SaveResponseDto(
                true,
                "Обработка фьючерсов завершена. Сохранено: " + savedCount + ", пропущено: " + existingCount,
                totalRequested,
                savedCount,
                existingCount,
                0,
                0,
                savedItems
            );
            
        } catch (Exception e) {
            return new SaveResponseDto(
                false,
                "Ошибка обработки фьючерсов: " + e.getMessage(),
                0,
                0,
                0,
                0,
                0,
                new ArrayList<>()
            );
        }
    }

    /**
     * Предпросмотр только акций
     */
    private SaveResponseDto previewSharesPrices(LocalDate date, String taskId) {
        try {
            System.out.println("[" + taskId + "] Предпросмотр акций за " + date);
            List<ShareEntity> shares = shareRepository.findAll();
            
            List<OpenPriceDto> items = new ArrayList<>();
            int totalRequested = shares.size();
            
            for (ShareEntity share : shares) {
                BigDecimal price = findFirstOpenPriceForDate(share.getFigi(), date, taskId, "share");
                if (price != null) {
                    items.add(new OpenPriceDto(
                        share.getFigi(),
                        date,
                        price,
                        "share",
                        share.getCurrency(),
                        share.getExchange()
                    ));
                }
            }
            
            return new SaveResponseDto(
                true,
                "Предпросмотр акций завершен. Найдено: " + items.size() + " из " + totalRequested,
                totalRequested,
                items.size(),
                0,
                0,
                0,
                items
            );
            
        } catch (Exception e) {
            return new SaveResponseDto(
                false,
                "Ошибка предпросмотра акций: " + e.getMessage(),
                0,
                0,
                0,
                0,
                0,
                new ArrayList<>()
            );
        }
    }

    /**
     * Предпросмотр только фьючерсов
     */
    private SaveResponseDto previewFuturesPrices(LocalDate date, String taskId) {
        try {
            System.out.println("[" + taskId + "] Предпросмотр фьючерсов за " + date);
            List<FutureEntity> futures = futureRepository.findAll();
            
            List<OpenPriceDto> items = new ArrayList<>();
            int totalRequested = futures.size();
            
            for (FutureEntity future : futures) {
                BigDecimal price = findFirstOpenPriceForDate(future.getFigi(), date, taskId, "future");
                if (price != null) {
                    items.add(new OpenPriceDto(
                        future.getFigi(),
                        date,
                        price,
                        "future",
                        future.getCurrency(),
                        future.getExchange()
                    ));
                }
            }
            
            return new SaveResponseDto(
                true,
                "Предпросмотр фьючерсов завершен. Найдено: " + items.size() + " из " + totalRequested,
                totalRequested,
                items.size(),
                0,
                0,
                0,
                items
            );
            
        } catch (Exception e) {
            return new SaveResponseDto(
                false,
                "Ошибка предпросмотра фьючерсов: " + e.getMessage(),
                0,
                0,
                0,
                0,
                0,
                new ArrayList<>()
            );
        }
    }

    /**
     * Загрузка цены открытия по конкретному figi за дату
     */
    public SaveResponseDto fetchAndStorePriceByFigiForDate(String figi, LocalDate date) {
        String taskId = "MANUAL_FIGI_" + UUID.randomUUID().toString().substring(0, 8);
        
        try {
            System.out.println("[" + taskId + "] Загрузка цены открытия для FIGI: " + figi + " за дату: " + date);
            
            // Определяем тип инструмента
            String instrumentType = determineInstrumentType(figi);
            if (instrumentType == null) {
                return new SaveResponseDto(
                    false,
                    "Инструмент с FIGI " + figi + " не найден в базе данных",
                    0,
                    0,
                    0,
                    0,
                    0,
                    new ArrayList<>()
                );
            }
            
            // Проверяем, есть ли уже запись
            if (openPriceRepository.existsByPriceDateAndFigi(date, figi)) {
                return new SaveResponseDto(
                    true,
                    "Цена открытия для FIGI " + figi + " за дату " + date + " уже существует",
                    1,
                    0,
                    1,
                    0,
                    0,
                    new ArrayList<>()
                );
            }
            
            // Ищем первую свечу
            BigDecimal firstOpenPrice = findFirstOpenPriceForDate(figi, date, taskId, instrumentType);
            
            if (firstOpenPrice != null) {
                // Получаем информацию об инструменте
                String currency = getCurrencyByFigi(figi, instrumentType);
                String exchange = getExchangeByFigi(figi, instrumentType);
                
                OpenPriceDto openPriceDto = new OpenPriceDto(
                    figi,
                    date,
                    firstOpenPrice,
                    instrumentType,
                    currency,
                    exchange
                );
                
                openPriceRepository.save(new OpenPriceEntity(
                    openPriceDto.priceDate(),
                    openPriceDto.figi(),
                    openPriceDto.instrumentType(),
                    openPriceDto.openPrice(),
                    openPriceDto.currency(),
                    openPriceDto.exchange()
                ));
                
                List<OpenPriceDto> savedItems = new ArrayList<>();
                savedItems.add(openPriceDto);
                
                return new SaveResponseDto(
                    true,
                    "Цена открытия для FIGI " + figi + " успешно сохранена: " + firstOpenPrice,
                    1,
                    1,
                    0,
                    0,
                    0,
                    savedItems
                );
            } else {
                return new SaveResponseDto(
                    true,
                    "Цена открытия для FIGI " + figi + " за дату " + date + " не найдена",
                    1,
                    0,
                    0,
                    0,
                    0,
                    new ArrayList<>()
                );
            }
            
        } catch (Exception e) {
            return new SaveResponseDto(
                false,
                "Ошибка загрузки цены открытия для FIGI " + figi + ": " + e.getMessage(),
                0,
                0,
                0,
                0,
                0,
                new ArrayList<>()
            );
        }
    }

    /**
     * Предпросмотр цены открытия по конкретному figi за дату
     */
    public SaveResponseDto previewPriceByFigiForDate(String figi, LocalDate date) {
        String taskId = "PREVIEW_FIGI_" + UUID.randomUUID().toString().substring(0, 8);
        
        try {
            System.out.println("[" + taskId + "] Предпросмотр цены открытия для FIGI: " + figi + " за дату: " + date);
            
            // Определяем тип инструмента
            String instrumentType = determineInstrumentType(figi);
            if (instrumentType == null) {
                return new SaveResponseDto(
                    false,
                    "Инструмент с FIGI " + figi + " не найден в базе данных",
                    0,
                    0,
                    0,
                    0,
                    0,
                    new ArrayList<>()
                );
            }
            
            // Ищем первую свечу
            BigDecimal firstOpenPrice = findFirstOpenPriceForDate(figi, date, taskId, instrumentType);
            
            if (firstOpenPrice != null) {
                // Получаем информацию об инструменте
                String currency = getCurrencyByFigi(figi, instrumentType);
                String exchange = getExchangeByFigi(figi, instrumentType);
                
                OpenPriceDto openPriceDto = new OpenPriceDto(
                    figi,
                    date,
                    firstOpenPrice,
                    instrumentType,
                    currency,
                    exchange
                );
                
                List<OpenPriceDto> items = new ArrayList<>();
                items.add(openPriceDto);
                
                return new SaveResponseDto(
                    true,
                    "Цена открытия для FIGI " + figi + " найдена: " + firstOpenPrice,
                    1,
                    1,
                    0,
                    0,
                    0,
                    items
                );
            } else {
                return new SaveResponseDto(
                    true,
                    "Цена открытия для FIGI " + figi + " за дату " + date + " не найдена",
                    1,
                    0,
                    0,
                    0,
                    0,
                    new ArrayList<>()
                );
            }
            
        } catch (Exception e) {
            return new SaveResponseDto(
                false,
                "Ошибка предпросмотра цены открытия для FIGI " + figi + ": " + e.getMessage(),
                0,
                0,
                0,
                0,
                0,
                new ArrayList<>()
            );
        }
    }

    /**
     * Определяет тип инструмента по FIGI
     */
    private String determineInstrumentType(String figi) {
        try {
            // Проверяем акции
            if (shareRepository.existsById(figi)) {
                System.out.println("FIGI " + figi + " найден как акция");
                return "share";
            }
            
            // Проверяем фьючерсы
            if (futureRepository.existsById(figi)) {
                System.out.println("FIGI " + figi + " найден как фьючерс");
                return "future";
            }
            
            // Проверяем индикативы
            if (indicativeRepository.existsById(figi)) {
                System.out.println("FIGI " + figi + " найден как индикатив");
                return "indicative";
            }
            
            System.out.println("FIGI " + figi + " не найден ни в одной таблице инструментов");
            return null;
            
        } catch (Exception e) {
            System.err.println("Ошибка при определении типа инструмента для FIGI " + figi + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Публичный метод для проверки типа инструмента по FIGI
     */
    public String checkInstrumentType(String figi) {
        return determineInstrumentType(figi);
    }

    /**
     * Получает валюту по FIGI и типу инструмента
     */
    private String getCurrencyByFigi(String figi, String instrumentType) {
        switch (instrumentType) {
            case "share":
                return shareRepository.findById(figi)
                    .map(ShareEntity::getCurrency)
                    .orElse("RUB");
            case "future":
                return futureRepository.findById(figi)
                    .map(FutureEntity::getCurrency)
                    .orElse("RUB");
            case "indicative":
                return indicativeRepository.findById(figi)
                    .map(IndicativeEntity::getCurrency)
                    .orElse("USD");
            default:
                return "RUB";
        }
    }

    /**
     * Получает биржу по FIGI и типу инструмента
     */
    private String getExchangeByFigi(String figi, String instrumentType) {
        switch (instrumentType) {
            case "share":
                return shareRepository.findById(figi)
                    .map(ShareEntity::getExchange)
                    .orElse("moex_mrng_evng_e_wknd_dlr");
            case "future":
                return futureRepository.findById(figi)
                    .map(FutureEntity::getExchange)
                    .orElse("FORTS_EVENING");
            case "indicative":
                return "MOEX";
            default:
                return "MOEX";
        }
    }
}
