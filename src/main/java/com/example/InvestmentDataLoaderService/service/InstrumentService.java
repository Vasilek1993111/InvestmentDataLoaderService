package com.example.InvestmentDataLoaderService.service;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.IndicativeEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.IndicativeRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc.InstrumentsServiceBlockingStub;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class InstrumentService {

    private final InstrumentsServiceBlockingStub instrumentsService;
    private final ShareRepository shareRepo;
    private final FutureRepository futureRepo;
    private final IndicativeRepository indicativeRepo;
    private final TinkoffRestClient restClient;

    public InstrumentService(InstrumentsServiceBlockingStub instrumentsService,
                           ShareRepository shareRepo,
                           FutureRepository futureRepo,
                           IndicativeRepository indicativeRepo,
                           TinkoffRestClient restClient) {
        this.instrumentsService = instrumentsService;
        this.shareRepo = shareRepo;
        this.futureRepo = futureRepo;
        this.indicativeRepo = indicativeRepo;
        this.restClient = restClient;
    }

    // === МЕТОДЫ ДЛЯ РАБОТЫ С АКЦИЯМИ ===

    public List<ShareDto> getShares(String status, String exchange, String currency, String ticker, String figi) {
        // Определяем статус инструмента для запроса к API
        InstrumentStatus instrumentStatus = InstrumentStatus.INSTRUMENT_STATUS_BASE;
        if (status != null && !status.isEmpty()) {
            try {
                instrumentStatus = InstrumentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Если статус не найден, используем базовый статус
                instrumentStatus = InstrumentStatus.INSTRUMENT_STATUS_BASE;
            }
        }
        
        // Получаем данные из T-Bank API
        SharesResponse response = instrumentsService.shares(InstrumentsRequest.newBuilder()
                .setInstrumentStatus(instrumentStatus)
                .build());
        
        List<ShareDto> shares = new ArrayList<>();
        for (var instrument : response.getInstrumentsList()) {
            // Применяем фильтры
            boolean matchesExchange = (exchange == null || exchange.isEmpty() || 
                                     instrument.getExchange().equalsIgnoreCase(exchange));
            boolean matchesCurrency = (currency == null || currency.isEmpty() || 
                                     instrument.getCurrency().equalsIgnoreCase(currency));
            boolean matchesTicker = (ticker == null || ticker.isEmpty() || 
                                   instrument.getTicker().equalsIgnoreCase(ticker));
            boolean matchesFigi = (figi == null || figi.isEmpty() || 
                                 instrument.getFigi().equalsIgnoreCase(figi));
            
            if (matchesExchange && matchesCurrency && matchesTicker && matchesFigi) {
                shares.add(new ShareDto(
                    instrument.getFigi(),
                    instrument.getTicker(),
                    instrument.getName(),
                    instrument.getCurrency(),
                    instrument.getExchange(),
                    instrument.getSector(),
                    instrument.getTradingStatus().name()
                ));
            }
        }
        
        // Сортируем по тикеру
        shares.sort(Comparator.comparing(ShareDto::getTicker, String.CASE_INSENSITIVE_ORDER));
        return shares;
    }

    public SaveResponseDto saveShares(ShareFilterDto filter) {
        // Получаем акции из API (используем существующий метод)
        List<ShareDto> sharesFromApi = getShares(filter.getStatus(), filter.getExchange(), filter.getCurrency(), filter.getTicker(), null);
        
        List<ShareDto> savedShares = new ArrayList<>();
        int existingCount = 0;
        
        for (ShareDto shareDto : sharesFromApi) {
            // Проверяем, существует ли акция в БД
            if (!shareRepo.existsById(shareDto.getFigi())) {
                // Создаем и сохраняем новую акцию
                ShareEntity shareEntity = new ShareEntity(
                    shareDto.getFigi(),
                    shareDto.getTicker(),
                    shareDto.getName(),
                    shareDto.getCurrency(),
                    shareDto.getExchange()
                );
                
                try {
                    shareRepo.save(shareEntity);
                    savedShares.add(shareDto);
                } catch (Exception e) {
                    // Логируем ошибку, но продолжаем обработку других акций
                    System.err.println("Error saving share " + shareDto.getFigi() + ": " + e.getMessage());
                }
            } else {
                existingCount++;
            }
        }
        
        // Формируем ответ
        boolean success = !sharesFromApi.isEmpty();
        String message;
        
        if (savedShares.isEmpty()) {
            if (sharesFromApi.isEmpty()) {
                message = "Новых акций не обнаружено. По заданным фильтрам акции не найдены.";
            } else {
                message = "Новых акций не обнаружено. Все найденные акции уже существуют в базе данных.";
            }
        } else {
            message = String.format("Успешно загружено %d новых акций из %d найденных.", savedShares.size(), sharesFromApi.size());
        }
        
        return new SaveResponseDto(
            success,
            message,
            sharesFromApi.size(),
            savedShares.size(),
            existingCount,
            savedShares
        );
    }

    // === МЕТОДЫ ДЛЯ РАБОТЫ С ФЬЮЧЕРСАМИ ===

    public List<FutureDto> getFutures(String status, String exchange, String currency, String ticker, String assetType) {
        // Определяем статус инструмента для запроса к API
        InstrumentStatus instrumentStatus = InstrumentStatus.INSTRUMENT_STATUS_BASE;
        if (status != null && !status.isEmpty()) {
            try {
                instrumentStatus = InstrumentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Если статус некорректный, используем значение по умолчанию
                instrumentStatus = InstrumentStatus.INSTRUMENT_STATUS_BASE;
            }
        }

        // Запрашиваем фьючерсы из T-Bank API
        FuturesResponse response = instrumentsService.futures(InstrumentsRequest.newBuilder()
                .setInstrumentStatus(instrumentStatus)
                .build());
        
        List<FutureDto> futures = new ArrayList<>();
        for (var instrument : response.getInstrumentsList()) {
            // Применяем фильтры
            boolean matchesExchange = (exchange == null || exchange.isEmpty() || 
                                     instrument.getExchange().equalsIgnoreCase(exchange));
            boolean matchesCurrency = (currency == null || currency.isEmpty() || 
                                     instrument.getCurrency().equalsIgnoreCase(currency));
            boolean matchesTicker = (ticker == null || ticker.isEmpty() || 
                                   instrument.getTicker().equalsIgnoreCase(ticker));
            boolean matchesAssetType = (assetType == null || assetType.isEmpty() || 
                                      instrument.getAssetType().equalsIgnoreCase(assetType));
            
            if (matchesExchange && matchesCurrency && matchesTicker && matchesAssetType) {
                futures.add(new FutureDto(
                    instrument.getFigi(),
                    instrument.getTicker(),
                    instrument.getAssetType(),
                    instrument.getBasicAsset(),
                    instrument.getCurrency(),
                    instrument.getExchange(),
                    null // stockTicker будет null, так как его нет в API фьючерсов
                ));
            }
        }
        
        // Сортируем по тикеру
        futures.sort(Comparator.comparing(FutureDto::getTicker, String.CASE_INSENSITIVE_ORDER));
        return futures;
    }

    public SaveResponseDto saveFutures(FutureFilterDto filter) {
        // Получаем фьючерсы из API (используем существующий метод)
        List<FutureDto> futuresFromApi = getFutures(filter.getStatus(), filter.getExchange(), filter.getCurrency(), filter.getTicker(), filter.getAssetType());
        
        List<FutureDto> savedFutures = new ArrayList<>();
        int existingCount = 0;
        
        for (FutureDto futureDto : futuresFromApi) {
            // Проверяем, существует ли фьючерс в БД
            if (!futureRepo.existsById(futureDto.getFigi())) {
                // Создаем и сохраняем новый фьючерс
                FutureEntity futureEntity = new FutureEntity(
                    futureDto.getFigi(),
                    futureDto.getTicker(),
                    futureDto.getAssetType(),
                    futureDto.getBasicAsset(),
                    futureDto.getCurrency(),
                    futureDto.getExchange(),
                    futureDto.getStockTicker()
                );
                
                try {
                    futureRepo.save(futureEntity);
                    savedFutures.add(futureDto);
                } catch (Exception e) {
                    // Логируем ошибку, но продолжаем обработку других фьючерсов
                    System.err.println("Error saving future " + futureDto.getFigi() + ": " + e.getMessage());
                }
            } else {
                existingCount++;
            }
        }
        
        // Формируем ответ
        boolean success = !futuresFromApi.isEmpty();
        String message;
        
        if (savedFutures.isEmpty()) {
            if (futuresFromApi.isEmpty()) {
                message = "Новых фьючерсов не обнаружено. По заданным фильтрам фьючерсы не найдены.";
            } else {
                message = "Новых фьючерсов не обнаружено. Все найденные фьючерсы уже существуют в базе данных.";
            }
        } else {
            message = String.format("Успешно загружено %d новых фьючерсов из %d найденных.", savedFutures.size(), futuresFromApi.size());
        }
        
        return new SaveResponseDto(
            success,
            message,
            futuresFromApi.size(),
            savedFutures.size(),
            existingCount,
            savedFutures
        );
    }

    // === МЕТОДЫ ДЛЯ РАБОТЫ С ИНДИКАТИВНЫМИ ИНСТРУМЕНТАМИ ===

    public List<IndicativeDto> getIndicatives(String exchange, String currency, String ticker, String figi) {
        try {
            // Используем REST API для получения индикативных инструментов
            var response = restClient.getIndicatives();
            
            List<IndicativeDto> indicatives = new ArrayList<>();
            
            // Парсим JSON ответ
            if (response.has("instruments")) {
                var instruments = response.get("instruments");
                if (instruments.isArray()) {
                    for (var instrument : instruments) {
                        // Применяем фильтры
                        boolean matchesExchange = (exchange == null || exchange.isEmpty() || 
                                                 instrument.get("exchange").asText().equalsIgnoreCase(exchange));
                        boolean matchesCurrency = (currency == null || currency.isEmpty() || 
                                                 instrument.get("currency").asText().equalsIgnoreCase(currency));
                        boolean matchesTicker = (ticker == null || ticker.isEmpty() || 
                                               instrument.get("ticker").asText().equalsIgnoreCase(ticker));
                        boolean matchesFigi = (figi == null || figi.isEmpty() || 
                                             instrument.get("figi").asText().equalsIgnoreCase(figi));
                        
                        if (matchesExchange && matchesCurrency && matchesTicker && matchesFigi) {
                            indicatives.add(new IndicativeDto(
                                instrument.get("figi").asText(),
                                instrument.get("ticker").asText(),
                                instrument.get("name").asText(),
                                instrument.get("currency").asText(),
                                instrument.get("exchange").asText(),
                                instrument.has("classCode") ? instrument.get("classCode").asText() : null,
                                instrument.has("uid") ? instrument.get("uid").asText() : null,
                                instrument.has("sellAvailableFlag") ? instrument.get("sellAvailableFlag").asBoolean() : null,
                                instrument.has("buyAvailableFlag") ? instrument.get("buyAvailableFlag").asBoolean() : null
                            ));
                        }
                    }
                }
            }
            
            // Сортируем по тикеру
            indicatives.sort(Comparator.comparing(IndicativeDto::getTicker, String.CASE_INSENSITIVE_ORDER));
            return indicatives;
            
        } catch (Exception e) {
            // Если REST API не доступен, используем данные из БД
            System.err.println("REST API method indicatives not available, using database: " + e.getMessage());
            
            List<IndicativeDto> indicatives = new ArrayList<>();
            
            // Получаем все индикативные инструменты из БД
            List<IndicativeEntity> entities = indicativeRepo.findAll();
            
            for (IndicativeEntity entity : entities) {
                // Применяем фильтры
                boolean matchesExchange = (exchange == null || exchange.isEmpty() || 
                                         entity.getExchange().equalsIgnoreCase(exchange));
                boolean matchesCurrency = (currency == null || currency.isEmpty() || 
                                         entity.getCurrency().equalsIgnoreCase(currency));
                boolean matchesTicker = (ticker == null || ticker.isEmpty() || 
                                       entity.getTicker().equalsIgnoreCase(ticker));
                boolean matchesFigi = (figi == null || figi.isEmpty() || 
                                     entity.getFigi().equalsIgnoreCase(figi));
                
                if (matchesExchange && matchesCurrency && matchesTicker && matchesFigi) {
                    indicatives.add(new IndicativeDto(
                        entity.getFigi(),
                        entity.getTicker(),
                        entity.getName(),
                        entity.getCurrency(),
                        entity.getExchange(),
                        entity.getClassCode(),
                        entity.getUid(),
                        entity.getSellAvailableFlag(),
                        entity.getBuyAvailableFlag()
                    ));
                }
            }
            
            // Сортируем по тикеру
            indicatives.sort(Comparator.comparing(IndicativeDto::getTicker, String.CASE_INSENSITIVE_ORDER));
            return indicatives;
        }
    }

    public IndicativeDto getIndicativeBy(String figi) {
        try {
            // Используем REST API для получения индикативного инструмента по FIGI
            var response = restClient.getIndicativeBy(figi);
            
            if (response.has("instrument")) {
                var instrument = response.get("instrument");
                
                return new IndicativeDto(
                    instrument.get("figi").asText(),
                    instrument.get("ticker").asText(),
                    instrument.get("name").asText(),
                    instrument.get("currency").asText(),
                    instrument.get("exchange").asText(),
                    instrument.has("classCode") ? instrument.get("classCode").asText() : null,
                    instrument.has("uid") ? instrument.get("uid").asText() : null,
                    instrument.has("sellAvailableFlag") ? instrument.get("sellAvailableFlag").asBoolean() : null,
                    instrument.has("buyAvailableFlag") ? instrument.get("buyAvailableFlag").asBoolean() : null
                );
            }
            
            return null;
            
        } catch (Exception e) {
            // Если REST API не доступен, ищем в БД
            System.err.println("REST API method getIndicativeBy not available, using database: " + e.getMessage());
            
            return indicativeRepo.findById(figi)
                .map(entity -> new IndicativeDto(
                    entity.getFigi(),
                    entity.getTicker(),
                    entity.getName(),
                    entity.getCurrency(),
                    entity.getExchange(),
                    entity.getClassCode(),
                    entity.getUid(),
                    entity.getSellAvailableFlag(),
                    entity.getBuyAvailableFlag()
                ))
                .orElse(null);
        }
    }

    public IndicativeDto getIndicativeByTicker(String ticker) {
        try {
            // Получаем все индикативные инструменты и ищем по тикеру
            List<IndicativeDto> indicatives = getIndicatives(null, null, ticker, null);
            
            // Возвращаем первый найденный инструмент с таким тикером
            return indicatives.stream()
                .filter(indicative -> indicative.getTicker().equalsIgnoreCase(ticker))
                .findFirst()
                .orElse(null);
                
        } catch (Exception e) {
            System.err.println("Error getting indicative by ticker: " + e.getMessage());
            return null;
        }
    }

    public SaveResponseDto saveIndicatives(IndicativeFilterDto filter) {
        // Получаем индикативные инструменты из API
        List<IndicativeDto> indicativesFromApi = getIndicatives(
            filter.getExchange(), 
            filter.getCurrency(), 
            filter.getTicker(), 
            filter.getFigi()
        );
        
        List<IndicativeDto> savedIndicatives = new ArrayList<>();
        int existingCount = 0;
        
        for (IndicativeDto indicativeDto : indicativesFromApi) {
            // Проверяем, существует ли индикативный инструмент в БД
            if (!indicativeRepo.existsById(indicativeDto.getFigi())) {
                // Создаем и сохраняем новый индикативный инструмент
                IndicativeEntity indicativeEntity = new IndicativeEntity(
                    indicativeDto.getFigi(),
                    indicativeDto.getTicker(),
                    indicativeDto.getName(),
                    indicativeDto.getCurrency(),
                    indicativeDto.getExchange(),
                    indicativeDto.getClassCode(),
                    indicativeDto.getUid(),
                    indicativeDto.getSellAvailableFlag(),
                    indicativeDto.getBuyAvailableFlag()
                );
                
                try {
                    indicativeRepo.save(indicativeEntity);
                    savedIndicatives.add(indicativeDto);
                } catch (Exception e) {
                    // Логируем ошибку, но продолжаем обработку других инструментов
                    System.err.println("Error saving indicative " + indicativeDto.getFigi() + ": " + e.getMessage());
                }
            } else {
                existingCount++;
            }
        }
        
        // Формируем ответ
        boolean success = !indicativesFromApi.isEmpty();
        String message;
        
        if (savedIndicatives.isEmpty()) {
            if (indicativesFromApi.isEmpty()) {
                message = "Новых индикативных инструментов не обнаружено. По заданным фильтрам инструменты не найдены.";
            } else {
                message = "Новых индикативных инструментов не обнаружено. Все найденные инструменты уже существуют в базе данных.";
            }
        } else {
            message = String.format("Успешно загружено %d новых индикативных инструментов из %d найденных.", savedIndicatives.size(), indicativesFromApi.size());
        }
        
        return new SaveResponseDto(
            success,
            message,
            indicativesFromApi.size(),
            savedIndicatives.size(),
            existingCount,
            savedIndicatives
        );
    }
}
