package com.example.InvestmentDataLoaderService.fixtures;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;
import ru.tinkoff.piapi.contract.v1.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Фабрика тестовых данных для unit-тестов instruments
 */
public class TestDataFactory {

    // ==================== DTO ОБЪЕКТЫ ====================

    public static ShareDto createShareDto() {
        return new ShareDto(
            "BBG004730N88",
            "SBER",
            "Сбербанк",
            "RUB",
            "moex_mrng_evng_e_wknd_dlr",
            "Financials",
            "SECURITY_TRADING_STATUS_NORMAL_TRADING"
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
            "SECURITY_TRADING_STATUS_NORMAL_TRADING"
        );
    }

    public static FutureDto createFutureDto() {
        return new FutureDto(
            "FUTSBER0324",
            "SBER-3.24",
            "FUTURES",
            "SBER",
            "RUB",
            "moex_mrng_evng_e_wknd_dlr"
        );
    }

    public static FutureDto createFutureDto(String figi, String ticker, String assetType) {
        return new FutureDto(
            figi,
            ticker,
            assetType,
            "SBER",
            "RUB",
            "moex_mrng_evng_e_wknd_dlr"
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
        return new SaveResponseDto(success, message, totalRequested, newItemsSaved, existingItemsSkipped, savedItems);
    }

    public static SaveResponseDto createSuccessfulSaveResponse(List<?> savedItems) {
        return new SaveResponseDto(
            true,
            "Успешно загружено " + savedItems.size() + " новых инструментов",
            savedItems.size(),
            savedItems.size(),
            0,
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
            Arrays.asList()
        );
    }

    // ==================== ENTITY ОБЪЕКТЫ ====================

    public static ShareEntity createShareEntity() {
        return new ShareEntity(
            "BBG004730N88",
            "SBER",
            "Сбербанк",
            "RUB",
            "moex_mrng_evng_e_wknd_dlr",
            "Financials",
            "SECURITY_TRADING_STATUS_NORMAL_TRADING",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    public static ShareEntity createShareEntity(String figi, String ticker, String name) {
        return new ShareEntity(
            figi,
            ticker,
            name,
            "RUB",
            "moex_mrng_evng_e_wknd_dlr",
            "Financials",
            "SECURITY_TRADING_STATUS_NORMAL_TRADING",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    public static FutureEntity createFutureEntity() {
        return new FutureEntity(
            "FUTSBER0324",
            "SBER-3.24",
            "FUTURES",
            "SBER",
            "RUB",
            "moex_mrng_evng_e_wknd_dlr"
        );
    }

    public static FutureEntity createFutureEntity(String figi, String ticker, String assetType) {
        return new FutureEntity(
            figi,
            ticker,
            assetType,
            "SBER",
            "RUB",
            "moex_mrng_evng_e_wknd_dlr"
        );
    }

    public static IndicativeEntity createIndicativeEntity() {
        return new IndicativeEntity(
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

    public static IndicativeEntity createIndicativeEntity(String figi, String ticker, String name) {
        return new IndicativeEntity(
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
            createShareEntity("BBG004730N88", "SBER", "Сбербанк"),
            createShareEntity("BBG004730ZJ9", "GAZP", "Газпром"),
            createShareEntity("BBG004730N88", "LKOH", "Лукойл")
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
}
