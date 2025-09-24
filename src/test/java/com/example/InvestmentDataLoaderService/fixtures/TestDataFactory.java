package com.example.InvestmentDataLoaderService.fixtures;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import ru.tinkoff.piapi.contract.v1.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


/**
 * Фабрика тестовых данных для unit-тестов instruments
 * 
 * <p>Этот класс предоставляет статические методы для создания тестовых данных
 * различных типов: DTO, Entity, gRPC объекты, JSON ответы и утилиты валидации.</p>
 * 
 * <p>Все методы помечены аннотациями Allure для улучшения отчетности тестов.</p>
 */

 @Epic("Test Infrastructure")
 @Feature("Test Data Factory")
 @DisplayName("Test Data Factory")
 @Owner("Investment Data Loader Service Team")
 @Severity(SeverityLevel.TRIVIAL)
public class TestDataFactory {

    // ==================== DTO ОБЪЕКТЫ ====================

    @Step("Создание ShareDto с дефолтными значениями")
    @DisplayName("Создание ShareDto (дефолтные значения)")
    @Description("Создает объект ShareDto с предустановленными тестовыми данными для Сбербанка")
    @Tag("dto")
    @Tag("share")
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

    @Step("Создание ShareDto с параметрами: figi={figi}, ticker={ticker}, name={name}")
    @DisplayName("Создание ShareDto (с параметрами)")
    @Description("Создает объект ShareDto с переданными параметрами figi, ticker и name")
    @Tag("dto")
    @Tag("share")
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

    @Step("Создание FutureDto с дефолтными значениями")
    @DisplayName("Создание FutureDto (дефолтные значения)")
    @Description("Создает объект FutureDto с предустановленными тестовыми данными для фьючерса на Сбербанк")
    @Tag("dto")
    @Tag("future")
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

    @Step("Создание FutureDto с параметрами: figi={figi}, ticker={ticker}, assetType={assetType}")
    @DisplayName("Создание FutureDto (с параметрами)")
    @Description("Создает объект FutureDto с переданными параметрами figi, ticker и assetType")
    @Tag("dto")
    @Tag("future")
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

    @Step("Создание IndicativeDto с дефолтными значениями")
    @DisplayName("Создание IndicativeDto (дефолтные значения)")
    @Description("Создает объект IndicativeDto с предустановленными тестовыми данными для индекса РТС")
    @Tag("dto")
    @Tag("indicative")
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

    @Step("Создание IndicativeDto с параметрами: figi={figi}, ticker={ticker}, name={name}")
    @DisplayName("Создание IndicativeDto (с параметрами)")
    @Description("Создает объект IndicativeDto с переданными параметрами figi, ticker и name")
    @Tag("dto")
    @Tag("indicative")
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

    @Step("Создание ShareFilterDto с дефолтными значениями")
    @DisplayName("Создание ShareFilterDto (дефолтные значения)")
    @Description("Создает объект ShareFilterDto с предустановленными фильтрами для акций")
    @Tag("dto")
    @Tag("filter")
    @Tag("share")
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

    @Step("Создание FutureFilterDto с дефолтными значениями")
    @DisplayName("Создание FutureFilterDto (дефолтные значения)")
    @Description("Создает объект FutureFilterDto с предустановленными фильтрами для фьючерсов")
    @Tag("dto")
    @Tag("filter")
    @Tag("future")
    public static FutureFilterDto createFutureFilterDto() {
        FutureFilterDto filter = new FutureFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_BASE");
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("SBER-3.24");
        filter.setAssetType("FUTURES");
        return filter;
    }

    @Step("Создание IndicativeFilterDto с дефолтными значениями")
    @DisplayName("Создание IndicativeFilterDto (дефолтные значения)")
    @Description("Создает объект IndicativeFilterDto с предустановленными фильтрами для индикативов")
    @Tag("dto")
    @Tag("filter")
    @Tag("indicative")
    public static IndicativeFilterDto createIndicativeFilterDto() {
        IndicativeFilterDto filter = new IndicativeFilterDto();
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("RTSI");
        filter.setFigi("BBG004730ZJ9");
        return filter;
    }

    @Step("Создание SaveResponseDto с параметрами: success={success}, message={message}, totalRequested={totalRequested}, newItemsSaved={newItemsSaved}, existingItemsSkipped={existingItemsSkipped}")
    @DisplayName("Создание SaveResponseDto (с параметрами)")
    @Description("Создает объект SaveResponseDto с переданными параметрами для ответа о сохранении данных")
    @Tag("dto")
    @Tag("response")
    public static SaveResponseDto createSaveResponseDto(boolean success, String message, int totalRequested, int newItemsSaved, int existingItemsSkipped, List<?> savedItems) {
        return new SaveResponseDto(success, message, totalRequested, newItemsSaved, existingItemsSkipped, 0, 0, savedItems);
    }

    @Step("Создание успешного SaveResponseDto для {savedItems.size()} элементов")
    @DisplayName("Создание успешного SaveResponseDto")
    @Description("Создает объект SaveResponseDto с успешным статусом и информацией о сохраненных элементах")
    @Tag("dto")
    @Tag("response")
    @Tag("success")
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

    @Step("Создание пустого SaveResponseDto")
    @DisplayName("Создание пустого SaveResponseDto")
    @Description("Создает объект SaveResponseDto с пустым результатом сохранения")
    @Tag("dto")
    @Tag("response")
    @Tag("empty")
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

    // ==================== ENTITY ОБЪЕКТЫ ====================

    @Step("Создание ShareEntity с дефолтными значениями")
    @DisplayName("Создание ShareEntity (дефолтные значения)")
    @Description("Создает объект ShareEntity с предустановленными тестовыми данными для Сбербанка")
    @Tag("entity")
    @Tag("share")
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

    @Step("Создание ShareEntity с параметрами: figi={figi}, ticker={ticker}, name={name}")
    @DisplayName("Создание ShareEntity (с параметрами)")
    @Description("Создает объект ShareEntity с переданными параметрами figi, ticker и name")
    @Tag("entity")
    @Tag("share")
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

    @Step("Создание FutureEntity с дефолтными значениями")
    @DisplayName("Создание FutureEntity (дефолтные значения)")
    @Description("Создает объект FutureEntity с предустановленными тестовыми данными для фьючерса на Сбербанк")
    @Tag("entity")
    @Tag("future")
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

    @Step("Создание FutureEntity с параметрами: figi={figi}, ticker={ticker}, assetType={assetType}")
    @DisplayName("Создание FutureEntity (с параметрами)")
    @Description("Создает объект FutureEntity с переданными параметрами figi, ticker и assetType")
    @Tag("entity")
    @Tag("future")
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

    @Step("Создание IndicativeEntity с дефолтными значениями")
    @DisplayName("Создание IndicativeEntity (дефолтные значения)")
    @Description("Создает объект IndicativeEntity с предустановленными тестовыми данными для индекса РТС")
    @Tag("entity")
    @Tag("indicative")
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

    @Step("Создание IndicativeEntity с параметрами: figi={figi}, ticker={ticker}, name={name}")
    @DisplayName("Создание IndicativeEntity (с параметрами)")
    @Description("Создает объект IndicativeEntity с переданными параметрами figi, ticker и name")
    @Tag("entity")
    @Tag("indicative")
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

    @Step("Создание gRPC Share с дефолтными значениями")
    @DisplayName("Создание gRPC Share (дефолтные значения)")
    @Description("Создает gRPC объект Share с предустановленными тестовыми данными для Сбербанка")
    @Tag("grpc")
    @Tag("share")
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

    @Step("Создание gRPC Share с параметрами: figi={figi}, ticker={ticker}, name={name}")
    @DisplayName("Создание gRPC Share (с параметрами)")
    @Description("Создает gRPC объект Share с переданными параметрами figi, ticker и name")
    @Tag("grpc")
    @Tag("share")
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

    @Step("Создание gRPC Future с дефолтными значениями")
    @DisplayName("Создание gRPC Future (дефолтные значения)")
    @Description("Создает gRPC объект Future с предустановленными тестовыми данными для фьючерса на Сбербанк")
    @Tag("grpc")
    @Tag("future")
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

    @Step("Создание gRPC Future с параметрами: figi={figi}, ticker={ticker}, assetType={assetType}")
    @DisplayName("Создание gRPC Future (с параметрами)")
    @Description("Создает gRPC объект Future с переданными параметрами figi, ticker и assetType")
    @Tag("grpc")
    @Tag("future")
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

    @Step("Создание SharesResponse с {shares.length} акциями")
    @DisplayName("Создание SharesResponse")
    @Description("Создает gRPC ответ SharesResponse с переданными акциями")
    @Tag("grpc")
    @Tag("response")
    @Tag("shares")
    public static SharesResponse createSharesResponse(Share... shares) {
        SharesResponse.Builder builder = SharesResponse.newBuilder();
        for (Share share : shares) {
            builder.addInstruments(share);
        }
        return builder.build();
    }

    @Step("Создание FuturesResponse с {futures.length} фьючерсами")
    @DisplayName("Создание FuturesResponse")
    @Description("Создает gRPC ответ FuturesResponse с переданными фьючерсами")
    @Tag("grpc")
    @Tag("response")
    @Tag("futures")
    public static FuturesResponse createFuturesResponse(Future... futures) {
        FuturesResponse.Builder builder = FuturesResponse.newBuilder();
        for (Future future : futures) {
            builder.addInstruments(future);
        }
        return builder.build();
    }

    // ==================== JSON ОБЪЕКТЫ ДЛЯ REST API ====================

    @Step("Создание JSON ответа с массивом индикативов")
    @DisplayName("Создание JSON ответа (массив индикативов)")
    @Description("Создает JSON строку с массивом индикативов для REST API")
    @Tag("json")
    @Tag("rest")
    @Tag("indicative")
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

    @Step("Создание JSON ответа с одним индикативом")
    @DisplayName("Создание JSON ответа (один индикатив)")
    @Description("Создает JSON строку с одним индикативом для REST API")
    @Tag("json")
    @Tag("rest")
    @Tag("indicative")
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

    @Step("Создание списка ShareDto с 3 акциями")
    @DisplayName("Создание списка ShareDto")
    @Description("Создает список из 3 объектов ShareDto с тестовыми данными")
    @Tag("list")
    @Tag("dto")
    @Tag("share")
    public static List<ShareDto> createShareDtoList() {
        return Arrays.asList(
            createShareDto("BBG004730N88", "SBER", "Сбербанк"),
            createShareDto("BBG004730ZJ9", "GAZP", "Газпром"),
            createShareDto("BBG004730N88", "LKOH", "Лукойл")
        );
    }

    @Step("Создание списка FutureDto с 3 фьючерсами")
    @DisplayName("Создание списка FutureDto")
    @Description("Создает список из 3 объектов FutureDto с тестовыми данными")
    @Tag("list")
    @Tag("dto")
    @Tag("future")
    public static List<FutureDto> createFutureDtoList() {
        return Arrays.asList(
            createFutureDto("FUTSBER0324", "SBER-3.24", "FUTURES"),
            createFutureDto("FUTGAZP0324", "GAZP-3.24", "FUTURES"),
            createFutureDto("FUTLKOH0324", "LKOH-3.24", "FUTURES")
        );
    }

    @Step("Создание списка IndicativeDto с 3 индикативами")
    @DisplayName("Создание списка IndicativeDto")
    @Description("Создает список из 3 объектов IndicativeDto с тестовыми данными")
    @Tag("list")
    @Tag("dto")
    @Tag("indicative")
    public static List<IndicativeDto> createIndicativeDtoList() {
        return Arrays.asList(
            createIndicativeDto("BBG004730ZJ9", "RTSI", "Индекс РТС"),
            createIndicativeDto("BBG004730ZJ9", "MOEX", "Индекс МосБиржи"),
            createIndicativeDto("BBG004730ZJ9", "USD000UTSTOM", "Доллар США")
        );
    }

    @Step("Создание списка ShareEntity с 3 акциями")
    @DisplayName("Создание списка ShareEntity")
    @Description("Создает список из 3 объектов ShareEntity с тестовыми данными")
    @Tag("list")
    @Tag("entity")
    @Tag("share")
    public static List<ShareEntity> createShareEntityList() {
        return Arrays.asList(
            createShareEntity("BBG004730N88", "SBER", "Сбербанк"),
            createShareEntity("BBG004730ZJ9", "GAZP", "Газпром"),
            createShareEntity("BBG004730N88", "LKOH", "Лукойл")
        );
    }

    @Step("Создание списка FutureEntity с 3 фьючерсами")
    @DisplayName("Создание списка FutureEntity")
    @Description("Создает список из 3 объектов FutureEntity с тестовыми данными")
    @Tag("list")
    @Tag("entity")
    @Tag("future")
    public static List<FutureEntity> createFutureEntityList() {
        return Arrays.asList(
            createFutureEntity("FUTSBER0324", "SBER-3.24", "FUTURES"),
            createFutureEntity("FUTGAZP0324", "GAZP-3.24", "FUTURES"),
            createFutureEntity("FUTLKOH0324", "LKOH-3.24", "FUTURES")
        );
    }

    @Step("Создание списка IndicativeEntity с 3 индикативами")
    @DisplayName("Создание списка IndicativeEntity")
    @Description("Создает список из 3 объектов IndicativeEntity с тестовыми данными")
    @Tag("list")
    @Tag("entity")
    @Tag("indicative")
    public static List<IndicativeEntity> createIndicativeEntityList() {
        return Arrays.asList(
            createIndicativeEntity("BBG004730ZJ9", "RTSI", "Индекс РТС"),
            createIndicativeEntity("BBG004730ZJ9", "MOEX", "Индекс МосБиржи"),
            createIndicativeEntity("BBG004730ZJ9", "USD000UTSTOM", "Доллар США")
        );
    }

    // ==================== УТИЛИТЫ ДЛЯ ТЕСТИРОВАНИЯ ====================

    @Step("Валидация ShareDto")
    @DisplayName("Валидация ShareDto")
    @Description("Проверяет валидность объекта ShareDto (не null и все обязательные поля заполнены)")
    @Tag("validation")
    @Tag("share")
    public static boolean isShareDtoValid(ShareDto share) {
        return share != null &&
               share.figi() != null && !share.figi().trim().isEmpty() &&
               share.ticker() != null && !share.ticker().trim().isEmpty() &&
               share.name() != null && !share.name().trim().isEmpty() &&
               share.currency() != null && !share.currency().trim().isEmpty() &&
               share.exchange() != null && !share.exchange().trim().isEmpty();
    }

    @Step("Валидация FutureDto")
    @DisplayName("Валидация FutureDto")
    @Description("Проверяет валидность объекта FutureDto (не null и все обязательные поля заполнены)")
    @Tag("validation")
    @Tag("future")
    public static boolean isFutureDtoValid(FutureDto future) {
        return future != null &&
               future.figi() != null && !future.figi().trim().isEmpty() &&
               future.ticker() != null && !future.ticker().trim().isEmpty() &&
               future.assetType() != null && !future.assetType().trim().isEmpty() &&
               future.basicAsset() != null && !future.basicAsset().trim().isEmpty() &&
               future.currency() != null && !future.currency().trim().isEmpty() &&
               future.exchange() != null && !future.exchange().trim().isEmpty();
    }

    @Step("Валидация IndicativeDto")
    @DisplayName("Валидация IndicativeDto")
    @Description("Проверяет валидность объекта IndicativeDto (не null и все обязательные поля заполнены)")
    @Tag("validation")
    @Tag("indicative")
    public static boolean isIndicativeDtoValid(IndicativeDto indicative) {
        return indicative != null &&
               indicative.figi() != null && !indicative.figi().trim().isEmpty() &&
               indicative.ticker() != null && !indicative.ticker().trim().isEmpty() &&
               indicative.name() != null && !indicative.name().trim().isEmpty() &&
               indicative.currency() != null && !indicative.currency().trim().isEmpty() &&
               indicative.exchange() != null && !indicative.exchange().trim().isEmpty();
    }

    @Step("Валидация SaveResponseDto")
    @DisplayName("Валидация SaveResponseDto")
    @Description("Проверяет валидность объекта SaveResponseDto (не null и все обязательные поля заполнены)")
    @Tag("validation")
    @Tag("response")
    public static boolean isSaveResponseDtoValid(SaveResponseDto response) {
        return response != null &&
               response.getMessage() != null && !response.getMessage().trim().isEmpty() &&
               response.getTotalRequested() >= 0 &&
               response.getNewItemsSaved() >= 0 &&
               response.getExistingItemsSkipped() >= 0 &&
               response.getSavedItems() != null;
    }
}
