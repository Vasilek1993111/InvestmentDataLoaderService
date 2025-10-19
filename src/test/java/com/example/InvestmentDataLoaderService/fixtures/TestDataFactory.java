package com.example.InvestmentDataLoaderService.fixtures;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;
import ru.tinkoff.piapi.contract.v1.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Фабрика тестовых данных для unit-тестов
 * 
 * <p>Этот класс предоставляет статические методы для создания тестовых данных
 * различных типов: DTO, Entity, gRPC объекты, JSON ответы и утилиты валидации.</p>
 */
public class TestDataFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper();

   
    public static ShareDto createShareDto() {
        return new ShareDto(
            "BBG004730N88",
            "SBER",
            "Сбербанк",
            "RUB",
            "moex_mrng_evng_e_wknd_dlr",
            "Financials",
            "SECURITY_TRADING_STATUS_NORMAL_TRADING",
            true,
            "test-asset-uid-sber",
            new BigDecimal("0.01"),
            1
        );
    }

    public static ShareDto createShareDto(String figi, String ticker, String name) {
        return new ShareDto(
            figi,
            ticker,
            name,
            "RUB",
            "moex_mrng_evng_e_wknd_dlr",
            "Financials",
            "SECURITY_TRADING_STATUS_NORMAL_TRADING",
            true,
            "test-asset-uid-" + ticker.toLowerCase(),
            new BigDecimal("0.01"),
            1
        );
    }

    public static FutureDto createFutureDto() {
        return new FutureDto(
            "FUTSBER0324",
            "SBER-3.24",
            "FUTURES",
            "SBER",
            "RUB",          
            "moex_mrng_evng_e_wknd_dlr",
            true,
            LocalDateTime.of(2024, 3, 15, 18, 45),
            new BigDecimal("0.01"),
            1
        );
    }

    public static FutureDto createFutureDto(String figi, String ticker, String assetType) {
        return new FutureDto(
            figi,
            ticker,
            assetType,
            "SBER",
            "RUB",
            "moex_mrng_evng_e_wknd_dlr",
            true,
            LocalDateTime.of(2024, 3, 15, 18, 45),
            new BigDecimal("0.01"),
            1
        );
    }

    public static IndicativeDto createIndicativeDto() {
        return new IndicativeDto(
            "BBG004730ZJ9",
            "RTSI",
            "Индекс РТС",
            "RUB",
            "moex_mrng_evng_e_wknd_dlr",
            "SPBXM",
            "test-uid",
            true,
            true
        );
    }

    public static IndicativeDto createIndicativeDto(String figi, String ticker, String name) {
        return new IndicativeDto(
            figi,
            ticker,
            name,
            "RUB",
            "moex_mrng_evng_e_wknd_dlr",
            "SPBXM",
            "test-uid",
            true,
            true
        );
    }

    public static ShareFilterDto createShareFilterDto() {
        ShareFilterDto filter = new ShareFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_BASE");
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("SBER");
        filter.setFigi("BBG004730N88");
        filter.setSector("Financials");
        filter.setTradingStatus("SECURITY_TRADING_STATUS_NORMAL_TRADING");
        return filter;
    }

    public static FutureFilterDto createFutureFilterDto() {
        FutureFilterDto filter = new FutureFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_BASE");
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("SBER-3.24");
        filter.setAssetType("FUTURES");
        return filter;
    }

    public static IndicativeFilterDto createIndicativeFilterDto() {
        IndicativeFilterDto filter = new IndicativeFilterDto();
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("RTSI");
        filter.setFigi("BBG004730ZJ9");
        return filter;
    }

    public static SaveResponseDto createSaveResponseDto(boolean success, String message, int totalRequested, int newItemsSaved, int existingItemsSkipped, List<?> savedItems) {
        return new SaveResponseDto(success, message, totalRequested, newItemsSaved, existingItemsSkipped, 0, 0, savedItems);
    }

    public static SaveResponseDto createSuccessfulSaveResponse(List<?> savedItems) {
        return new SaveResponseDto(
            true,
            "Успешно загружено " + savedItems.size() + " новых инструментов",
            savedItems.size(),
            savedItems.size(),
            0,
            0, // invalidItemsFiltered
            0, // missingFromApi
            savedItems
        );
    }

    public static SaveResponseDto createEmptySaveResponse() {
        return new SaveResponseDto(
            false,
            "Новых инструментов не обнаружено",
            0,
            0,
            0,
            0,
            0, // missingFromApi
            Arrays.asList()
        );
    }

    public static SaveResponseDto createTestSaveResponse() {
        return new SaveResponseDto(
            true,
            "Успешно сохранено 5 новых инструментов из 10 найденных",
            10,
            5,
            5,
            0,
            0,
            Arrays.asList("item1", "item2", "item3", "item4", "item5")
        );
    }

    public static SaveResponseDto createTestPreviewResponse() {
        return new SaveResponseDto(
            true,
            "Успешно сохранено 5 новых инструментов из 10 найденных",
            10,
            5,
            5,
            0,
            0,
            Arrays.asList("item1", "item2", "item3", "item4", "item5")
        );
    }

    public static Map<String, Long> createTestInstrumentCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("shares", 150L);
        counts.put("futures", 45L);
        counts.put("indicatives", 12L);
        counts.put("total", 207L);
        return counts;
    }

    // ==================== СПЕЦИФИЧНЫЕ МЕТОДЫ ДЛЯ INSTRUMENTSCONTROLLERTEST ====================

    public static ShareDto createSberShare() {
        return new ShareDto(
            "BBG004730N88", 
            "SBER", 
            "ПАО Сбербанк", 
            "RUB", 
            "moex_mrng_evng_e_wknd_dlr", 
            "Financial", 
            "SECURITY_TRADING_STATUS_NORMAL_TRADING",
            true,
            "test-asset-uid-sber",
            new BigDecimal("0.01"),
            1
        );
    }

    public static ShareDto createGazpromShare() {
        return new ShareDto(
            "BBG004730ZJ9", 
            "GAZP", 
            "ПАО Газпром", 
            "RUB", 
            "moex_mrng_evng_e_wknd_dlr", 
            "Energy", 
            "SECURITY_TRADING_STATUS_NORMAL_TRADING",
            true,
            "test-asset-uid-gazp",
            new BigDecimal("0.01"),
            1
        );
    }   

    public static ShareDto createLukoilShare() {
        return new ShareDto(
            "BBG004730N88", 
            "LKOH", 
            "ПАО Лукойл", 
            "RUB", 
            "moex_mrng_evng_e_wknd_dlr", 
            "Energy", 
            "SECURITY_TRADING_STATUS_NORMAL_TRADING",
            true,
            "test-asset-uid-lkoh",
            new BigDecimal("0.01"),
            1
        );
    }

    public static List<ShareDto> createMoexSharesList() {
        return Arrays.asList(
            createSberShare(),
            createGazpromShare(),
            createLukoilShare()
        );
    }

    public static FutureDto createSilverFuture() {
        return new FutureDto(
            "FUTSI0624000", 
            "SI0624", 
            "COMMODITY", 
            "Silver", 
            "USD", 
            "moex_mrng_evng_e_wknd_dlr",
            true,
            LocalDateTime.of(2024, 6, 24, 18, 45),
            new BigDecimal("0.01"),
            1
        );
    }

    public static FutureDto createGoldFuture() {
        return new FutureDto(
            "FUTGZ0624000", 
            "GZ0624", 
            "COMMODITY", 
            "Gold", 
            "USD", 
            "moex_mrng_evng_e_wknd_dlr",
            true,
            LocalDateTime.of(2024, 6, 24, 18, 45),
            new BigDecimal("0.01"),
            1
        );
    }

    public static List<FutureDto> createCommodityFuturesList() {
        return Arrays.asList(
            createSilverFuture(),
            createGoldFuture()
        );
    }

    public static IndicativeDto createUsdRubIndicative() {
        return new IndicativeDto(
            "BBG0013HGFT4", 
            "USD000UTSTOM", 
            "Доллар США / Российский рубль", 
            "RUB", 
            "moex_mrng_evng_e_wknd_dlr", 
            "CURRENCY", 
            "USD000UTSTOM", 
            true, 
            true
        );
    }

    public static IndicativeDto createEurRubIndicative() {
        return new IndicativeDto(
            "BBG0013HGFT5", 
            "EUR000UTSTOM", 
            "Евро / Российский рубль", 
            "RUB", 
            "moex_mrng_evng_e_wknd_dlr", 
            "CURRENCY", 
            "EUR000UTSTOM", 
            true, 
            true
        );
    }

    public static List<IndicativeDto> createCurrencyIndicativesList() {
        return Arrays.asList(
            createUsdRubIndicative(),
            createEurRubIndicative()
        );
    }

    public static ShareFilterDto createMoexShareFilter() {
        ShareFilterDto filter = new ShareFilterDto();
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setStatus("INSTRUMENT_STATUS_BASE");
        return filter;
    }

    public static FutureFilterDto createCommodityFutureFilter() {
        FutureFilterDto filter = new FutureFilterDto();
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("USD");
        filter.setAssetType("COMMODITY");
        return filter;
    }

    public static IndicativeFilterDto createCurrencyIndicativeFilter() {
        IndicativeFilterDto filter = new IndicativeFilterDto();
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("USD000UTSTOM");
        return filter;
    }

    // ==================== ENTITY ОБЪЕКТЫ ====================

    public static ShareEntity createShareEntity() {
        ShareEntity entity = new ShareEntity();
        entity.setFigi("BBG004730N88");
        entity.setTicker("SBER");
        entity.setName("Сбербанк");
        entity.setCurrency("RUB");
        entity.setExchange("moex_mrng_evng_e_wknd_dlr");
        entity.setSector("Financials");
        entity.setTradingStatus("SECURITY_TRADING_STATUS_NORMAL_TRADING");
        entity.setShortEnabled(true);
        entity.setMinPriceIncrement(new BigDecimal("0.01"));
        entity.setLot(1);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    public static ShareEntity createShareEntity(String figi, String ticker, String name, String exchange) {
        ShareEntity entity = new ShareEntity();
        entity.setFigi(figi);
        entity.setTicker(ticker);
        entity.setName(name);
        entity.setCurrency("RUB");
        entity.setExchange(exchange);
        entity.setSector("Financials");
        entity.setTradingStatus("SECURITY_TRADING_STATUS_NORMAL_TRADING");
        entity.setShortEnabled(true);
        entity.setMinPriceIncrement(new BigDecimal("0.01"));
        entity.setLot(1);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    public static FutureEntity createFutureEntity() {
        return new FutureEntity(
            "FUTSBER0324",
            "SBER-3.24",
            "FUTURES",
            "SBER",
            "RUB",
            "moex_mrng_evng_e_wknd_dlr",
            true,
            LocalDateTime.of(2024, 3, 15, 18, 45),
            new BigDecimal("0.01"),
            1
        );
    }

    public static FutureEntity createFutureEntity(String figi, String ticker, String assetType) {
        return new FutureEntity(
            figi,
            ticker,
            assetType,
            "SBER",
            "RUB",
            "moex_mrng_evng_e_wknd_dlr",
            true,
            LocalDateTime.of(2024, 3, 15, 18, 45),
            new BigDecimal("0.01"),
            1
        );
    }

    public static IndicativeEntity createIndicativeEntity() {
        IndicativeEntity entity = new IndicativeEntity();
        entity.setFigi("BBG004730ZJ9");
        entity.setTicker("RTSI");
        entity.setName("Индекс РТС");
        entity.setCurrency("RUB");
        entity.setExchange("moex_mrng_evng_e_wknd_dlr");
        entity.setClassCode("SPBXM");
        entity.setUid("test-uid");
        entity.setSellAvailableFlag(true);
        entity.setBuyAvailableFlag(true);
        return entity;
    }

    public static IndicativeEntity createIndicativeEntity(String figi, String ticker, String name) {
        IndicativeEntity entity = new IndicativeEntity();
        entity.setFigi(figi);
        entity.setTicker(ticker);
        entity.setName(name);
        entity.setCurrency("RUB");
        entity.setExchange("moex_mrng_evng_e_wknd_dlr");
        entity.setClassCode("SPBXM");
        entity.setUid("test-uid");
        entity.setSellAvailableFlag(true);
        entity.setBuyAvailableFlag(true);
        return entity;
    }

    // ==================== GRPC ОБЪЕКТЫ ====================

    public static Share createGrpcShare() {
        return Share.newBuilder()
            .setFigi("BBG004730N88")
            .setTicker("SBER")
            .setName("Сбербанк")
            .setCurrency("RUB")
            .setExchange("moex_mrng_evng_e_wknd_dlr")
            .setSector("Financials")
            .setTradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING)
            .build();
    }

    public static Share createGrpcShare(String figi, String ticker, String name) {
        return Share.newBuilder()
            .setFigi(figi)
            .setTicker(ticker)
            .setName(name)
            .setCurrency("RUB")
            .setExchange("moex_mrng_evng_e_wknd_dlr")
            .setSector("Financials")
            .setTradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING)
            .build();
    }

    public static Future createGrpcFuture() {
        return Future.newBuilder()
            .setFigi("FUTSBER0324")
            .setTicker("SBER-3.24")
            .setAssetType("FUTURES")
            .setBasicAsset("SBER")
            .setCurrency("RUB")
            .setExchange("moex_mrng_evng_e_wknd_dlr")
            .build();
    }

    public static Future createGrpcFuture(String figi, String ticker, String assetType) {
        return Future.newBuilder()
            .setFigi(figi)
            .setTicker(ticker)
            .setAssetType(assetType)
            .setBasicAsset("SBER")
            .setCurrency("RUB")
            .setExchange("moex_mrng_evng_e_wknd_dlr")
            .build();
    }

    public static SharesResponse createSharesResponse(Share... shares) {
        SharesResponse.Builder builder = SharesResponse.newBuilder();
        for (Share share : shares) {
            builder.addInstruments(share);
        }
        return builder.build();
    }

    public static FuturesResponse createFuturesResponse(Future... futures) {
        FuturesResponse.Builder builder = FuturesResponse.newBuilder();
        for (Future future : futures) {
            builder.addInstruments(future);
        }
        return builder.build();
    }

    // ==================== JSON ОБЪЕКТЫ ДЛЯ REST API ====================

    public static JsonNode createSharesJsonNode() {
        try {
            String json = """
                {
                    "instruments": [
                        {
                            "figi": "BBG004730N88",
                            "ticker": "SBER",
                            "name": "Сбербанк",
                            "currency": "RUB",
                            "exchange": "moex_mrng_evng_e_wknd_dlr",
                            "sector": "Financials",
                            "tradingStatus": "SECURITY_TRADING_STATUS_NORMAL_TRADING",
                            "shortEnabledFlag": true,
                            "assetUid": "test-asset-uid-sber"
                        },
                        {
                            "figi": "BBG004730ZJ9",
                            "ticker": "GAZP",
                            "name": "Газпром",
                            "currency": "RUB",
                            "exchange": "moex_mrng_evng_e_wknd_dlr",
                            "sector": "Energy",
                            "tradingStatus": "SECURITY_TRADING_STATUS_NORMAL_TRADING",
                            "shortEnabledFlag": true,
                            "assetUid": "test-asset-uid-gazp"
                        }
                    ]
                }
                """;
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create shares JsonNode", e);
        }
    }

    public static JsonNode createFuturesJsonNode() {
        try {
            String json = """
                {
                    "instruments": [
                        {
                            "figi": "FUTSBER0324",
                            "ticker": "SBER-3.24",
                            "assetType": "COMMODITY",
                            "basicAsset": "SBER",
                            "currency": "RUB",
                            "exchange": "moex_mrng_evng_e_wknd_dlr",
                            "shortEnabled": true,
                            "expirationDate": "2024-03-15T18:45:00"
                        },
                        {
                            "figi": "FUTGAZP0324",
                            "ticker": "GAZP-3.24",
                            "assetType": "COMMODITY",
                            "basicAsset": "GAZP",
                            "currency": "RUB",
                            "exchange": "moex_mrng_evng_e_wknd_dlr",
                            "shortEnabled": true,
                            "expirationDate": "2024-03-15T18:45:00"
                        }
                    ]
                }
                """;
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create futures JsonNode", e);
        }
    }

    public static JsonNode createIndicativesJsonNode() {
        try {
            String json = """
                {
                    "instruments": [
                        {
                            "figi": "BBG00M5X5S47",
                            "ticker": "RTSI",
                            "name": "Индекс РТС",
                            "currency": "RUB",
                            "exchange": "moex_mrng_evng_e_wknd_dlr",
                            "classCode": "SPBXM",
                            "uid": "test-uid",
                            "sellAvailableFlag": true,
                            "buyAvailableFlag": true
                        },
                        {
                            "figi": "BBG00M5X5S48",
                            "ticker": "MOEX",
                            "name": "Индекс МосБиржи",
                            "currency": "RUB",
                            "exchange": "moex_mrng_evng_e_wknd_dlr",
                            "classCode": "SPBXM",
                            "uid": "test-uid",
                            "sellAvailableFlag": true,
                            "buyAvailableFlag": true
                        }
                    ]
                }
                """;
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create indicatives JsonNode", e);
        }
    }

    public static JsonNode createEmptyJsonNode() {
        try {
            String json = """
                {
                    "instruments": []
                }
                """;
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create empty JsonNode", e);
        }
    }

    public static String createIndicativesJsonResponse() {
        return """
            {
                "instruments": [
                    {
                        "figi": "BBG004730ZJ9",
                        "ticker": "RTSI",
                        "name": "Индекс РТС",
                        "currency": "RUB",
                        "exchange": "moex_mrng_evng_e_wknd_dlr",
                        "classCode": "SPBXM",
                        "uid": "test-uid",
                        "sellAvailableFlag": true,
                        "buyAvailableFlag": true
                    }
                ]
            }
            """;
    }

    public static String createIndicativeJsonResponse() {
        return """
            {
                "instrument": {
                    "figi": "BBG004730ZJ9",
                    "ticker": "RTSI",
                    "name": "Индекс РТС",
                    "currency": "RUB",
                    "exchange": "moex_mrng_evng_e_wknd_dlr",
                    "classCode": "SPBXM",
                    "uid": "test-uid",
                    "sellAvailableFlag": true,
                    "buyAvailableFlag": true
                }
            }
            """;
    }

    // ==================== СПИСКИ ТЕСТОВЫХ ДАННЫХ ====================

    public static List<ShareDto> createShareDtoList() {
        return Arrays.asList(
            createShareDto("BBG004730N88", "SBER", "Сбербанк"),
            createShareDto("BBG004730ZJ9", "GAZP", "Газпром"),
            createShareDto("BBG004730N88", "LKOH", "Лукойл")
        );
    }

    public static List<FutureDto> createFutureDtoList() {
        return Arrays.asList(
            createFutureDto("FUTSBER0324", "SBER-3.24", "FUTURES"),
            createFutureDto("FUTGAZP0324", "GAZP-3.24", "FUTURES"),
            createFutureDto("FUTLKOH0324", "LKOH-3.24", "FUTURES")
        );
    }

    public static List<IndicativeDto> createIndicativeDtoList() {
        return Arrays.asList(
            createIndicativeDto("BBG004730ZJ9", "RTSI", "Индекс РТС"),
            createIndicativeDto("BBG004730ZJ9", "MOEX", "Индекс МосБиржи"),
            createIndicativeDto("BBG004730ZJ9", "USD000UTSTOM", "Доллар США")
        );
    }

    public static List<ShareEntity> createShareEntityList() {
        return Arrays.asList(
            createShareEntity("BBG004730N88", "SBER", "Сбербанк", "TESTMOEX"),
            createShareEntity("BBG004730ZJ9", "GAZP", "Газпром", "TESTMOEX"),
            createShareEntity("BBG004730N89", "LKOH", "Лукойл", "TESTMOEX")
        );
    }

    public static List<FutureEntity> createFutureEntityList() {
        return Arrays.asList(
            createFutureEntity("FUTSBER0324", "SBER-3.24", "FUTURES"),
            createFutureEntity("FUTGAZP0324", "GAZP-3.24", "FUTURES"),
            createFutureEntity("FUTLKOH0324", "LKOH-3.24", "FUTURES")
        );
    }

    public static List<IndicativeEntity> createIndicativeEntityList() {
        return Arrays.asList(
            createIndicativeEntity("BBG004730ZJ9", "RTSI", "Индекс РТС"),
            createIndicativeEntity("BBG004730ZJ9", "MOEX", "Индекс МосБиржи"),
            createIndicativeEntity("BBG004730ZJ9", "USD000UTSTOM", "Доллар США")
        );
    }

    // ==================== CANDLE DTO ОБЪЕКТЫ ====================

    public static CandleDto createCandleDto() {
        return new CandleDto(
            "BBG004730N88",
            1000L,
            BigDecimal.valueOf(105.0),
            BigDecimal.valueOf(95.0),
            Instant.now(),
            BigDecimal.valueOf(102.0),
            BigDecimal.valueOf(100.0),
            true
        );
    }

    public static CandleDto createCandleDto(String figi, long volume, BigDecimal high, BigDecimal low, 
                                          Instant time, BigDecimal open, BigDecimal close, boolean isComplete) {
        return new CandleDto(figi, volume, high, low, time, open, close, isComplete);
    }

    public static MinuteCandleRequestDto createMinuteCandleRequestDto() {
        MinuteCandleRequestDto request = new MinuteCandleRequestDto();
        request.setInstruments(Arrays.asList("BBG004730N88"));
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        return request;
    }

    public static MinuteCandleRequestDto createMinuteCandleRequestDto(List<String> instruments, 
                                                                    List<String> assetType, 
                                                                    LocalDate date) {
        MinuteCandleRequestDto request = new MinuteCandleRequestDto();
        request.setInstruments(instruments);
        request.setAssetType(assetType);
        request.setDate(date);
        return request;
    }

    public static MinuteCandleRequestDto createMultipleInstrumentsRequest() {
        MinuteCandleRequestDto request = new MinuteCandleRequestDto();
        request.setInstruments(Arrays.asList("BBG004730N88", "BBG004730ZJ29"));
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        return request;
    }

    public static MinuteCandleRequestDto createEmptyInstrumentsRequest() {
        MinuteCandleRequestDto request = new MinuteCandleRequestDto();
        request.setInstruments(Arrays.asList());
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        return request;
    }

    public static MinuteCandleRequestDto createMixedAssetTypesRequest() {
        MinuteCandleRequestDto request = new MinuteCandleRequestDto();
        request.setInstruments(Arrays.asList());
        request.setAssetType(Arrays.asList("SHARES", "FUTURES", "INDICATIVES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        return request;
    }

    public static MinuteCandleRequestDto createNullValuesRequest() {
        MinuteCandleRequestDto request = new MinuteCandleRequestDto();
        request.setInstruments(null);
        request.setAssetType(null);
        request.setDate(null);
        return request;
    }

    public static MinuteCandleRequestDto createFutureDateRequest() {
        MinuteCandleRequestDto request = new MinuteCandleRequestDto();
        request.setInstruments(Arrays.asList("BBG004730N88"));
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.now().plusDays(1));
        return request;
    }

    public static MinuteCandleRequestDto createLargeInstrumentsRequest() {
        List<String> manyInstruments = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            manyInstruments.add("BBG004730N8" + String.format("%02d", i));
        }
        
        MinuteCandleRequestDto request = new MinuteCandleRequestDto();
        request.setInstruments(manyInstruments);
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        return request;
    }

    // ==================== СПИСКИ CANDLE DTO ====================

    public static List<CandleDto> createCandleDtoList() {
        return Arrays.asList(
            createCandleDto("BBG004730N88", 1000L, BigDecimal.valueOf(105.0), BigDecimal.valueOf(95.0), 
                         Instant.now(), BigDecimal.valueOf(102.0), BigDecimal.valueOf(100.0), true),
            createCandleDto("BBG004730N88", 1200L, BigDecimal.valueOf(108.0), BigDecimal.valueOf(98.0), 
                         Instant.now().minusSeconds(3600), BigDecimal.valueOf(106.0), BigDecimal.valueOf(103.0), true)
        );
    }

    public static List<CandleDto> createIncompleteCandleDtoList() {
        return Arrays.asList(
            createCandleDto("BBG004730N88", 1000L, BigDecimal.valueOf(105.0), BigDecimal.valueOf(95.0), 
                         Instant.now(), BigDecimal.valueOf(102.0), BigDecimal.valueOf(100.0), true),
            createCandleDto("BBG004730N88", 1200L, BigDecimal.valueOf(108.0), BigDecimal.valueOf(98.0), 
                         Instant.now().minusSeconds(3600), BigDecimal.valueOf(106.0), BigDecimal.valueOf(103.0), false), // Незакрытая свеча
            createCandleDto("BBG004730N88", 1500L, BigDecimal.valueOf(110.0), BigDecimal.valueOf(100.0), 
                         Instant.now().minusSeconds(7200), BigDecimal.valueOf(108.0), BigDecimal.valueOf(105.0), true)
        );
    }

    public static List<CandleDto> createInvalidCandleDtoList() {
        return Arrays.asList(
            createCandleDto("BBG004730N88", 1000L, BigDecimal.valueOf(105.0), BigDecimal.valueOf(95.0), 
                         Instant.now(), BigDecimal.valueOf(102.0), BigDecimal.valueOf(100.0), true),
            createCandleDto("BBG004730N88", 0L, BigDecimal.ZERO, BigDecimal.ZERO, 
                         Instant.EPOCH, BigDecimal.ZERO, BigDecimal.ZERO, true) // Неверные данные
        );
    }

    public static List<CandleDto> createEmptyCandleDtoList() {
        return Arrays.asList();
    }

    // ==================== DAILY CANDLE DTO ОБЪЕКТЫ ====================

    public static DailyCandleRequestDto createDailyCandleRequestDto() {
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(Arrays.asList("BBG004730N88"));
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        return request;
    }

    public static DailyCandleRequestDto createDailyCandleRequestDto(List<String> instruments, 
                                                                  List<String> assetType, 
                                                                  LocalDate date) {
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(instruments);
        request.setAssetType(assetType);
        request.setDate(date);
        return request;
    }

    public static DailyCandleRequestDto createMultipleInstrumentsDailyRequest() {
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(Arrays.asList("BBG004730N88", "BBG004730ZJ29"));
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        return request;
    }

    public static DailyCandleRequestDto createEmptyInstrumentsDailyRequest() {
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(Arrays.asList());
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        return request;
    }

    public static DailyCandleRequestDto createMixedAssetTypesDailyRequest() {
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(Arrays.asList());
        request.setAssetType(Arrays.asList("SHARES", "FUTURES", "INDICATIVES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        return request;
    }

    public static DailyCandleRequestDto createNullValuesDailyRequest() {
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(null);
        request.setAssetType(null);
        request.setDate(null);
        return request;
    }

    public static DailyCandleRequestDto createFutureDateDailyRequest() {
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(Arrays.asList("BBG004730N88"));
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.now().plusDays(1));
        return request;
    }

    public static DailyCandleRequestDto createLargeInstrumentsDailyRequest() {
        List<String> manyInstruments = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            manyInstruments.add("BBG004730N8" + String.format("%02d", i));
        }
        
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(manyInstruments);
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        return request;
    }

    // ==================== СПИСКИ DAILY CANDLE DTO ====================

    public static List<CandleDto> createDailyCandleDtoList() {
        return Arrays.asList(
            createCandleDto("BBG004730N88", 1000L, BigDecimal.valueOf(105.0), BigDecimal.valueOf(95.0), 
                         Instant.now(), BigDecimal.valueOf(102.0), BigDecimal.valueOf(100.0), true),
            createCandleDto("BBG004730N88", 1200L, BigDecimal.valueOf(108.0), BigDecimal.valueOf(98.0), 
                         Instant.now().minusSeconds(86400), BigDecimal.valueOf(106.0), BigDecimal.valueOf(103.0), true)
        );
    }

    public static List<CandleDto> createIncompleteDailyCandleDtoList() {
        return Arrays.asList(
            createCandleDto("BBG004730N88", 1000L, BigDecimal.valueOf(105.0), BigDecimal.valueOf(95.0), 
                         Instant.now(), BigDecimal.valueOf(102.0), BigDecimal.valueOf(100.0), true),
            createCandleDto("BBG004730N88", 1200L, BigDecimal.valueOf(108.0), BigDecimal.valueOf(98.0), 
                         Instant.now().minusSeconds(86400), BigDecimal.valueOf(106.0), BigDecimal.valueOf(103.0), false), // Незакрытая свеча
            createCandleDto("BBG004730N88", 1500L, BigDecimal.valueOf(110.0), BigDecimal.valueOf(100.0), 
                         Instant.now().minusSeconds(172800), BigDecimal.valueOf(108.0), BigDecimal.valueOf(105.0), true)
        );
    }

    public static List<CandleDto> createInvalidDailyCandleDtoList() {
        return Arrays.asList(
            createCandleDto("BBG004730N88", 1000L, BigDecimal.valueOf(105.0), BigDecimal.valueOf(95.0), 
                         Instant.now(), BigDecimal.valueOf(102.0), BigDecimal.valueOf(100.0), true),
            createCandleDto("BBG004730N88", 0L, BigDecimal.ZERO, BigDecimal.ZERO, 
                         Instant.EPOCH, BigDecimal.ZERO, BigDecimal.ZERO, true) // Неверные данные
        );
    }

    // ==================== EVENING SESSION DTO ОБЪЕКТЫ ====================

    public static ClosePriceEveningSessionRequestDto createEveningSessionRequestDto() {
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(Arrays.asList("TEST_SHARE_001", "TEST_FUTURE_001"));
        return request;
    }

    public static ClosePriceEveningSessionRequestDto createEveningSessionRequestDto(List<String> instruments) {
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(instruments);
        return request;
    }

    public static ClosePriceEveningSessionRequestDto createEmptyInstrumentsEveningSessionRequest() {
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(Arrays.asList());
        return request;
    }

    public static ClosePriceEveningSessionRequestDto createNullValuesEveningSessionRequest() {
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(null);
        return request;
    }

    public static ClosePriceEveningSessionRequestDto createLargeInstrumentsEveningSessionRequest() {
        List<String> manyInstruments = new ArrayList<>();
        for (int i = 0; i < 250; i++) {
            manyInstruments.add("TEST_SHARE_" + String.format("%03d", i));
        }
        
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(manyInstruments);
        return request;
    }

    public static ClosePriceEveningSessionRequestDto createVeryLargeInstrumentsEveningSessionRequest() {
        List<String> manyInstruments = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            manyInstruments.add("TEST_SHARE_" + String.format("%04d", i));
        }
        
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(manyInstruments);
        return request;
    }

    // ==================== СПИСКИ EVENING SESSION DTO ====================

    public static List<ClosePriceDto> createEveningSessionClosePriceDtoList() {
        return Arrays.asList(
            createClosePriceDto("TEST_SHARE_001", "2024-01-15", BigDecimal.valueOf(100.0), BigDecimal.valueOf(105.0)),
            createClosePriceDto("TEST_FUTURE_001", "2024-01-15", BigDecimal.valueOf(200.0), BigDecimal.valueOf(210.0))
        );
    }

    public static List<ClosePriceDto> createInvalidDateEveningSessionClosePriceDtoList() {
        return Arrays.asList(
            createClosePriceDto("TEST_SHARE_001", "2024-01-15", BigDecimal.valueOf(100.0), BigDecimal.valueOf(105.0)),
            createClosePriceDto("TEST_FUTURE_001", "1970-01-01", BigDecimal.valueOf(200.0), BigDecimal.valueOf(210.0)) // Неверная дата
        );
    }

    public static List<ClosePriceDto> createEveningPriceClosePriceDtoList() {
        return Arrays.asList(
            createClosePriceDto("TEST_SHARE_001", "2024-01-15", BigDecimal.valueOf(100.0), BigDecimal.valueOf(105.0))
        );
    }

    public static List<ClosePriceDto> createNoPricesEveningSessionClosePriceDtoList() {
        return Arrays.asList(
            createClosePriceDto("TEST_SHARE_001", "2024-01-15", null, null) // Нет цен
        );
    }

    public static List<ClosePriceDto> createEmptyEveningSessionClosePriceDtoList() {
        return Arrays.asList();
    }

    // ==================== CLOSE PRICE DTO ОБЪЕКТЫ ====================

    public static ClosePriceDto createClosePriceDto(String figi, String date, BigDecimal closePrice, BigDecimal eveningSessionPrice) {
        return new ClosePriceDto(figi, date, closePrice, eveningSessionPrice);
    }

    public static ClosePriceEntity createClosePriceEntity() {
        return new ClosePriceEntity(
            LocalDate.of(2024, 1, 15),
            "BBG004730N88",
            "SHARE",
            BigDecimal.valueOf(102.50),
            "RUB",
            "MOEX"
        );
    }

    public static ClosePriceEveningSessionEntity createClosePriceEveningSessionEntity() {
        return new ClosePriceEveningSessionEntity(
            LocalDate.of(2024, 1, 15),
            "BBG004730N88",
            BigDecimal.valueOf(102.50),
            "SHARE",
            "RUB",
            "MOEX"
        );
    }

    // ==================== MORNING SESSION DTO ОБЪЕКТЫ ====================

    public static OpenPriceDto createOpenPriceDto() {
        return createOpenPriceDto(
            "BBG004730N88",
            LocalDate.of(2024, 1, 15),
            BigDecimal.valueOf(100.50),
            "SHARE",
            "RUB",
            "MOEX"
        );
    }

    public static OpenPriceDto createOpenPriceDto(String figi, LocalDate priceDate, BigDecimal openPrice, 
                                                  String instrumentType, String currency, String exchange) {
        return new OpenPriceDto(figi, priceDate, openPrice, instrumentType, currency, exchange);
    }

    public static List<OpenPriceDto> createOpenPriceDtoList(int count) {
        List<OpenPriceDto> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(createOpenPriceDto(
                "BBG004730N8" + i,
                LocalDate.of(2024, 1, 15),
                BigDecimal.valueOf(100.50 + i),
                "SHARE",
                "RUB",
                "MOEX"
            ));
        }
        return list;
    }

    public static List<OpenPriceDto> createEmptyOpenPriceDtoList() {
        return new ArrayList<>();
    }

    // ==================== MORNING SESSION ENTITY ОБЪЕКТЫ ====================

    public static OpenPriceEntity createOpenPriceEntity() {
        return createOpenPriceEntity(
            "BBG004730N88",
            LocalDate.of(2024, 1, 15),
            BigDecimal.valueOf(100.50),
            "SHARE",
            "RUB",
            "MOEX"
        );
    }

    public static OpenPriceEntity createOpenPriceEntity(String figi, LocalDate date, BigDecimal price,
                                                        String instrumentType, String currency, String exchange) {
        return new OpenPriceEntity(date, figi, instrumentType, price, currency, exchange);
    }

    public static MinuteCandleEntity createMinuteCandleEntity(String figi, LocalDate date, BigDecimal openPrice) {
        MinuteCandleEntity candle = new MinuteCandleEntity();
        candle.setFigi(figi);
        candle.setTime(Instant.now());
        candle.setOpen(openPrice);
        candle.setHigh(openPrice.add(BigDecimal.valueOf(10)));
        candle.setLow(openPrice.subtract(BigDecimal.valueOf(5)));
        candle.setClose(openPrice.add(BigDecimal.valueOf(2)));
        candle.setVolume(1000L);
        return candle;
    }

    public static List<MinuteCandleEntity> createEmptyMinuteCandleEntityList() {
        return new ArrayList<>();
    }

    public static List<ShareEntity> createShareEntityList(int count) {
        List<ShareEntity> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(createShareEntity(
                "TEST_SHARE_" + String.format("%03d", i),
                "SBER" + i,
                "RUB",
                "MOEX"
            ));
        }
        return list;
    }

    public static List<ShareEntity> createEmptyShareEntityList() {
        return new ArrayList<>();
    }

    // ==================== SYSTEM LOG ENTITY ОБЪЕКТЫ ====================

    public static SystemLogEntity createSystemLogEntity() {
        return createSystemLogEntity(
            "test-task-123",
            "/api/minute-candles",
            "POST",
            "SUCCESS",
            "Загрузка минутных свечей завершена успешно"
        );
    }

    public static SystemLogEntity createSystemLogEntity(String taskId, String endpoint, String method, 
                                                        String status, String message) {
        SystemLogEntity log = new SystemLogEntity();
        log.setTaskId(taskId);
        log.setEndpoint(endpoint);
        log.setMethod(method);
        log.setStatus(status);
        log.setMessage(message);
        log.setStartTime(Instant.now().minusSeconds(10));
        log.setEndTime(Instant.now());
        log.setDurationMs(10000L);
        return log;
    }

    public static SystemLogEntity createSystemLogEntityWithError() {
        return createSystemLogEntity(
            "test-task-error-123",
            "/api/minute-candles",
            "POST",
            "ERROR",
            "Ошибка при загрузке минутных свечей"
        );
    }

    public static List<FutureEntity> createEmptyFutureEntityList() {
        return new ArrayList<>();
    }

    public static List<IndicativeEntity> createEmptyIndicativeEntityList() {
        return new ArrayList<>();
    }

    public static List<OpenPriceEntity> createOpenPriceEntityList(int count) {
        List<OpenPriceEntity> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(createOpenPriceEntity(
                "BBG004730N8" + i,
                LocalDate.of(2024, 1, 15),
                BigDecimal.valueOf(100.50 + i),
                "SHARE",
                "RUB",
                "MOEX"
            ));
        }
        return list;
    }

    public static List<OpenPriceEntity> createEmptyOpenPriceEntityList() {
        return new ArrayList<>();
    }

    // ==================== УТИЛИТЫ ДЛЯ ТЕСТИРОВАНИЯ ====================

    public static boolean isShareDtoValid(ShareDto share) {
        return share != null &&
               share.figi() != null && !share.figi().trim().isEmpty() &&
               share.ticker() != null && !share.ticker().trim().isEmpty() &&
               share.name() != null && !share.name().trim().isEmpty() &&
               share.currency() != null && !share.currency().trim().isEmpty() &&
               share.exchange() != null && !share.exchange().trim().isEmpty();
    }

    public static boolean isFutureDtoValid(FutureDto future) {
        return future != null &&
               future.figi() != null && !future.figi().trim().isEmpty() &&
               future.ticker() != null && !future.ticker().trim().isEmpty() &&
               future.assetType() != null && !future.assetType().trim().isEmpty() &&
               future.basicAsset() != null && !future.basicAsset().trim().isEmpty() &&
               future.currency() != null && !future.currency().trim().isEmpty() &&
               future.exchange() != null && !future.exchange().trim().isEmpty();
    }

    public static boolean isIndicativeDtoValid(IndicativeDto indicative) {
        return indicative != null &&
               indicative.figi() != null && !indicative.figi().trim().isEmpty() &&
               indicative.ticker() != null && !indicative.ticker().trim().isEmpty() &&
               indicative.name() != null && !indicative.name().trim().isEmpty() &&
               indicative.currency() != null && !indicative.currency().trim().isEmpty() &&
               indicative.exchange() != null && !indicative.exchange().trim().isEmpty();
    }

    public static boolean isSaveResponseDtoValid(SaveResponseDto response) {
        return response != null &&
               response.getMessage() != null && !response.getMessage().trim().isEmpty() &&
               response.getTotalRequested() >= 0 &&
               response.getNewItemsSaved() >= 0 &&
               response.getExistingItemsSkipped() >= 0 &&
               response.getSavedItems() != null;
    }

    public static boolean isOpenPriceDtoValid(OpenPriceDto dto) {
        return dto != null &&
               dto.figi() != null && !dto.figi().trim().isEmpty() &&
               dto.priceDate() != null &&
               dto.openPrice() != null && dto.openPrice().compareTo(BigDecimal.ZERO) > 0 &&
               dto.instrumentType() != null && !dto.instrumentType().trim().isEmpty() &&
               dto.currency() != null && !dto.currency().trim().isEmpty() &&
               dto.exchange() != null && !dto.exchange().trim().isEmpty();
    }

    // ==================== ASSET FUNDAMENTAL DTO ОБЪЕКТЫ ====================

    public static AssetFundamentalDto createAssetFundamentalDto() {
        AssetFundamentalDto dto = new AssetFundamentalDto();
        dto.setAssetUid("test-asset-uid-sber");
        dto.setCurrency("RUB");
        dto.setDomicileIndicatorCode("RU");
        dto.setMarketCapitalization(BigDecimal.valueOf(1000000000L));
        dto.setHighPriceLast52Weeks(BigDecimal.valueOf(300.0));
        dto.setLowPriceLast52Weeks(BigDecimal.valueOf(200.0));
        dto.setAverageDailyVolumeLast10Days(BigDecimal.valueOf(1000000L));
        dto.setAverageDailyVolumeLast4Weeks(BigDecimal.valueOf(1200000L));
        dto.setBeta(BigDecimal.valueOf(1.2));
        dto.setFreeFloat(BigDecimal.valueOf(0.8));
        dto.setForwardAnnualDividendYield(BigDecimal.valueOf(0.05));
        dto.setSharesOutstanding(BigDecimal.valueOf(1000000000L));
        dto.setRevenueTtm(BigDecimal.valueOf(5000000000L));
        dto.setEbitdaTtm(BigDecimal.valueOf(1000000000L));
        dto.setNetIncomeTtm(BigDecimal.valueOf(200000000L));
        dto.setEpsTtm(BigDecimal.valueOf(2.0));
        dto.setDilutedEpsTtm(BigDecimal.valueOf(1.95));
        dto.setFreeCashFlowTtm(BigDecimal.valueOf(300000000L));
        dto.setFiveYearAnnualRevenueGrowthRate(BigDecimal.valueOf(0.1));
        dto.setThreeYearAnnualRevenueGrowthRate(BigDecimal.valueOf(0.08));
        dto.setOneYearAnnualRevenueGrowthRate(BigDecimal.valueOf(0.05));
        dto.setPeRatioTtm(BigDecimal.valueOf(15.0));
        dto.setPriceToSalesTtm(BigDecimal.valueOf(2.0));
        dto.setPriceToBookTtm(BigDecimal.valueOf(1.5));
        dto.setPriceToFreeCashFlowTtm(BigDecimal.valueOf(20.0));
        dto.setTotalEnterpriseValueMrq(BigDecimal.valueOf(1200000000L));
        dto.setEvToEbitdaMrq(BigDecimal.valueOf(12.0));
        dto.setEvToSales(BigDecimal.valueOf(2.4));
        dto.setNetMarginMrq(BigDecimal.valueOf(0.04));
        dto.setNetInterestMarginMrq(BigDecimal.valueOf(0.03));
        dto.setRoe(BigDecimal.valueOf(0.15));
        dto.setRoa(BigDecimal.valueOf(0.08));
        dto.setRoic(BigDecimal.valueOf(0.12));
        dto.setTotalDebtMrq(BigDecimal.valueOf(200000000L));
        dto.setTotalDebtToEquityMrq(BigDecimal.valueOf(0.3));
        dto.setTotalDebtToEbitdaMrq(BigDecimal.valueOf(0.2));
        dto.setFreeCashFlowToPrice(BigDecimal.valueOf(0.05));
        dto.setNetDebtToEbitda(BigDecimal.valueOf(0.15));
        dto.setCurrentRatioMrq(BigDecimal.valueOf(1.5));
        dto.setFixedChargeCoverageRatioFy(BigDecimal.valueOf(5.0));
        dto.setDividendYieldDailyTtm(BigDecimal.valueOf(0.04));
        dto.setDividendRateTtm(BigDecimal.valueOf(0.8));
        dto.setDividendsPerShare(BigDecimal.valueOf(0.8));
        dto.setFiveYearsAverageDividendYield(BigDecimal.valueOf(0.06));
        dto.setFiveYearAnnualDividendGrowthRate(BigDecimal.valueOf(0.1));
        dto.setDividendPayoutRatioFy(BigDecimal.valueOf(0.4));
        dto.setBuyBackTtm(BigDecimal.valueOf(10000000L));
        dto.setAdrToCommonShareRatio(BigDecimal.valueOf(1.0));
        dto.setNumberOfEmployees(BigDecimal.valueOf(300000L));
        dto.setExDividendDate("2024-03-15T00:00:00.000Z");
        dto.setFiscalPeriodStartDate("2024-01-01T00:00:00.000Z");
        dto.setFiscalPeriodEndDate("2024-12-31T00:00:00.000Z");
        dto.setRevenueChangeFiveYears(BigDecimal.valueOf(0.5));
        dto.setEpsChangeFiveYears(BigDecimal.valueOf(0.3));
        dto.setEbitdaChangeFiveYears(BigDecimal.valueOf(0.4));
        dto.setTotalDebtChangeFiveYears(BigDecimal.valueOf(0.2));
        return dto;
    }

    public static AssetFundamentalDto createAssetFundamentalDto(String assetUid) {
        AssetFundamentalDto dto = createAssetFundamentalDto();
        dto.setAssetUid(assetUid);
        return dto;
    }

    public static List<AssetFundamentalDto> createAssetFundamentalDtoList() {
        return Arrays.asList(
            createAssetFundamentalDto("test-asset-uid-sber"),
            createAssetFundamentalDto("test-asset-uid-gazp"),
            createAssetFundamentalDto("test-asset-uid-lkoh")
        );
    }

    public static List<AssetFundamentalDto> createEmptyAssetFundamentalDtoList() {
        return new ArrayList<>();
    }

    public static AssetFundamentalsRequestDto createAssetFundamentalsRequestDto() {
        AssetFundamentalsRequestDto request = new AssetFundamentalsRequestDto();
        request.setAssets(Arrays.asList("test-asset-uid-sber", "test-asset-uid-gazp"));
        return request;
    }

    public static AssetFundamentalsRequestDto createAssetFundamentalsRequestDto(List<String> assets) {
        AssetFundamentalsRequestDto request = new AssetFundamentalsRequestDto();
        request.setAssets(assets);
        return request;
    }

    public static AssetFundamentalsRequestDto createSharesAssetFundamentalsRequestDto() {
        AssetFundamentalsRequestDto request = new AssetFundamentalsRequestDto();
        request.setAssets(Arrays.asList("shares"));
        return request;
    }

    public static AssetFundamentalsRequestDto createEmptyAssetFundamentalsRequestDto() {
        AssetFundamentalsRequestDto request = new AssetFundamentalsRequestDto();
        request.setAssets(Arrays.asList());
        return request;
    }

    public static AssetFundamentalsRequestDto createNullAssetFundamentalsRequestDto() {
        AssetFundamentalsRequestDto request = new AssetFundamentalsRequestDto();
        request.setAssets(null);
        return request;
    }

    // ==================== ASSET FUNDAMENTAL ENTITY ОБЪЕКТЫ ====================

    public static AssetFundamentalEntity createAssetFundamentalEntity() {
        AssetFundamentalEntity entity = new AssetFundamentalEntity();
        entity.setAssetUid("test-asset-uid-sber");
        entity.setCurrency("RUB");
        entity.setDomicileIndicatorCode("RU");
        entity.setMarketCapitalization(BigDecimal.valueOf(1000000000L));
        entity.setHighPriceLast52Weeks(BigDecimal.valueOf(300.0));
        entity.setLowPriceLast52Weeks(BigDecimal.valueOf(200.0));
        entity.setAverageDailyVolumeLast10Days(BigDecimal.valueOf(1000000L));
        entity.setAverageDailyVolumeLast4Weeks(BigDecimal.valueOf(1200000L));
        entity.setBeta(BigDecimal.valueOf(1.2));
        entity.setFreeFloat(BigDecimal.valueOf(0.8));
        entity.setForwardAnnualDividendYield(BigDecimal.valueOf(0.05));
        entity.setSharesOutstanding(BigDecimal.valueOf(1000000000L));
        entity.setRevenueTtm(BigDecimal.valueOf(5000000000L));
        entity.setEbitdaTtm(BigDecimal.valueOf(1000000000L));
        entity.setNetIncomeTtm(BigDecimal.valueOf(200000000L));
        entity.setEpsTtm(BigDecimal.valueOf(2.0));
        entity.setDilutedEpsTtm(BigDecimal.valueOf(1.95));
        entity.setFreeCashFlowTtm(BigDecimal.valueOf(300000000L));
        entity.setFiveYearAnnualRevenueGrowthRate(BigDecimal.valueOf(0.1));
        entity.setThreeYearAnnualRevenueGrowthRate(BigDecimal.valueOf(0.08));
        entity.setOneYearAnnualRevenueGrowthRate(BigDecimal.valueOf(0.05));
        entity.setPeRatioTtm(BigDecimal.valueOf(15.0));
        entity.setPriceToSalesTtm(BigDecimal.valueOf(2.0));
        entity.setPriceToBookTtm(BigDecimal.valueOf(1.5));
        entity.setPriceToFreeCashFlowTtm(BigDecimal.valueOf(20.0));
        entity.setTotalEnterpriseValueMrq(BigDecimal.valueOf(1200000000L));
        entity.setEvToEbitdaMrq(BigDecimal.valueOf(12.0));
        entity.setEvToSales(BigDecimal.valueOf(2.4));
        entity.setNetMarginMrq(BigDecimal.valueOf(0.04));
        entity.setNetInterestMarginMrq(BigDecimal.valueOf(0.03));
        entity.setRoe(BigDecimal.valueOf(0.15));
        entity.setRoa(BigDecimal.valueOf(0.08));
        entity.setRoic(BigDecimal.valueOf(0.12));
        entity.setTotalDebtMrq(BigDecimal.valueOf(200000000L));
        entity.setTotalDebtToEquityMrq(BigDecimal.valueOf(0.3));
        entity.setTotalDebtToEbitdaMrq(BigDecimal.valueOf(0.2));
        entity.setFreeCashFlowToPrice(BigDecimal.valueOf(0.05));
        entity.setNetDebtToEbitda(BigDecimal.valueOf(0.15));
        entity.setCurrentRatioMrq(BigDecimal.valueOf(1.5));
        entity.setFixedChargeCoverageRatioFy(BigDecimal.valueOf(5.0));
        entity.setDividendYieldDailyTtm(BigDecimal.valueOf(0.04));
        entity.setDividendRateTtm(BigDecimal.valueOf(0.8));
        entity.setDividendsPerShare(BigDecimal.valueOf(0.8));
        entity.setFiveYearsAverageDividendYield(BigDecimal.valueOf(0.06));
        entity.setFiveYearAnnualDividendGrowthRate(BigDecimal.valueOf(0.1));
        entity.setDividendPayoutRatioFy(BigDecimal.valueOf(0.4));
        entity.setBuyBackTtm(BigDecimal.valueOf(10000000L));
        entity.setAdrToCommonShareRatio(BigDecimal.valueOf(1.0));
        entity.setNumberOfEmployees(BigDecimal.valueOf(300000L));
        entity.setExDividendDate("2024-03-15T00:00:00.000Z");
        entity.setFiscalPeriodStartDate("2024-01-01T00:00:00.000Z");
        entity.setFiscalPeriodEndDate("2024-12-31T00:00:00.000Z");
        entity.setRevenueChangeFiveYears(BigDecimal.valueOf(0.5));
        entity.setEpsChangeFiveYears(BigDecimal.valueOf(0.3));
        entity.setEbitdaChangeFiveYears(BigDecimal.valueOf(0.4));
        entity.setTotalDebtChangeFiveYears(BigDecimal.valueOf(0.2));
        return entity;
    }

    public static AssetFundamentalEntity createAssetFundamentalEntity(String assetUid) {
        AssetFundamentalEntity entity = createAssetFundamentalEntity();
        entity.setAssetUid(assetUid);
        return entity;
    }

    public static List<AssetFundamentalEntity> createAssetFundamentalEntityList() {
        return Arrays.asList(
            createAssetFundamentalEntity("test-asset-uid-sber"),
            createAssetFundamentalEntity("test-asset-uid-gazp"),
            createAssetFundamentalEntity("test-asset-uid-lkoh")
        );
    }

    public static List<AssetFundamentalEntity> createEmptyAssetFundamentalEntityList() {
        return new ArrayList<>();
    }

    // ==================== ВАЛИДАЦИЯ ASSET FUNDAMENTAL DTO ====================

    public static boolean isAssetFundamentalDtoValid(AssetFundamentalDto dto) {
        return dto != null &&
               dto.getAssetUid() != null && !dto.getAssetUid().trim().isEmpty() &&
               dto.getCurrency() != null && !dto.getCurrency().trim().isEmpty();
    }
}