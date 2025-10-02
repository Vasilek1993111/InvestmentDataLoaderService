package com.example.InvestmentDataLoaderService.fixtures;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import ru.tinkoff.piapi.contract.v1.*;

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
            "SECURITY_TRADING_STATUS_NORMAL_TRADING",
            true
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
            "SECURITY_TRADING_STATUS_NORMAL_TRADING",
            true
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
            "moex_mrng_evng_e_wknd_dlr",
            true,
            LocalDateTime.of(2024, 3, 15, 18, 45)
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
            "moex_mrng_evng_e_wknd_dlr",
            true,
            LocalDateTime.of(2024, 3, 15, 18, 45)
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

    @Step("Создание тестового SaveResponseDto для InstrumentsController")
    @DisplayName("Создание тестового SaveResponseDto")
    @Description("Создает объект SaveResponseDto с тестовыми данными для InstrumentsController")
    @Tag("dto")
    @Tag("response")
    @Tag("test")
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

    @Step("Создание тестовой статистики инструментов")
    @DisplayName("Создание тестовой статистики")
    @Description("Создает Map с тестовой статистикой по количеству инструментов")
    @Tag("statistics")
    @Tag("test")
    public static Map<String, Long> createTestInstrumentCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("shares", 150L);
        counts.put("futures", 45L);
        counts.put("indicatives", 12L);
        counts.put("total", 207L);
        return counts;
    }

    // ==================== СПЕЦИФИЧНЫЕ МЕТОДЫ ДЛЯ INSTRUMENTSCONTROLLERTEST ====================

    @Step("Создание ShareDto для Сбербанка (MOEX)")
    @DisplayName("Создание ShareDto для Сбербанка")
    @Description("Создает ShareDto с данными Сбербанка для MOEX")
    @Tag("dto")
    @Tag("share")
    @Tag("sber")
    public static ShareDto createSberShare() {
        return new ShareDto(
            "BBG004730N88", 
            "SBER", 
            "ПАО Сбербанк", 
            "RUB", 
            "moex_mrng_evng_e_wknd_dlr", 
            "Financial", 
            "SECURITY_TRADING_STATUS_NORMAL_TRADING",
            true
        );
    }

    @Step("Создание ShareDto для Газпрома (MOEX)")
    @DisplayName("Создание ShareDto для Газпрома")
    @Description("Создает ShareDto с данными Газпрома для MOEX")
    @Tag("dto")
    @Tag("share")
    @Tag("gazprom")
    public static ShareDto createGazpromShare() {
        return new ShareDto(
            "BBG004730ZJ9", 
            "GAZP", 
            "ПАО Газпром", 
            "RUB", 
            "moex_mrng_evng_e_wknd_dlr", 
            "Energy", 
            "SECURITY_TRADING_STATUS_NORMAL_TRADING",
            true
        );
    }

    @Step("Создание ShareDto для Лукойла (MOEX)")
    @DisplayName("Создание ShareDto для Лукойла")
    @Description("Создает ShareDto с данными Лукойла для MOEX")
    @Tag("dto")
    @Tag("share")
    @Tag("lukoil")
    public static ShareDto createLukoilShare() {
        return new ShareDto(
            "BBG004730N88", 
            "LKOH", 
            "ПАО Лукойл", 
            "RUB", 
            "moex_mrng_evng_e_wknd_dlr", 
            "Energy", 
            "SECURITY_TRADING_STATUS_NORMAL_TRADING",
            true
        );
    }

    @Step("Создание списка ShareDto для MOEX (Сбербанк, Газпром, Лукойл)")
    @DisplayName("Создание списка ShareDto для MOEX")
    @Description("Создает список из 3 ShareDto с данными Сбербанка, Газпрома и Лукойла для MOEX")
    @Tag("list")
    @Tag("dto")
    @Tag("share")
    @Tag("moex")
    public static List<ShareDto> createMoexSharesList() {
        return Arrays.asList(
            createSberShare(),
            createGazpromShare(),
            createLukoilShare()
        );
    }

    @Step("Создание FutureDto для фьючерса на серебро")
    @DisplayName("Создание FutureDto для серебра")
    @Description("Создает FutureDto с данными фьючерса на серебро")
    @Tag("dto")
    @Tag("future")
    @Tag("silver")
    public static FutureDto createSilverFuture() {
        return new FutureDto(
            "FUTSI0624000", 
            "SI0624", 
            "COMMODITY", 
            "Silver", 
            "USD", 
            "moex_mrng_evng_e_wknd_dlr",
            true,
            LocalDateTime.of(2024, 6, 24, 18, 45)
        );
    }

    @Step("Создание FutureDto для фьючерса на золото")
    @DisplayName("Создание FutureDto для золота")
    @Description("Создает FutureDto с данными фьючерса на золото")
    @Tag("dto")
    @Tag("future")
    @Tag("gold")
    public static FutureDto createGoldFuture() {
        return new FutureDto(
            "FUTGZ0624000", 
            "GZ0624", 
            "COMMODITY", 
            "Gold", 
            "USD", 
            "moex_mrng_evng_e_wknd_dlr",
            true,
            LocalDateTime.of(2024, 6, 24, 18, 45)
        );
    }

    @Step("Создание списка FutureDto для товарных фьючерсов")
    @DisplayName("Создание списка FutureDto для товарных фьючерсов")
    @Description("Создает список из 2 FutureDto с данными фьючерсов на серебро и золото")
    @Tag("list")
    @Tag("dto")
    @Tag("future")
    @Tag("commodity")
    public static List<FutureDto> createCommodityFuturesList() {
        return Arrays.asList(
            createSilverFuture(),
            createGoldFuture()
        );
    }

    @Step("Создание IndicativeDto для USD/RUB")
    @DisplayName("Создание IndicativeDto для USD/RUB")
    @Description("Создает IndicativeDto с данными валютной пары USD/RUB")
    @Tag("dto")
    @Tag("indicative")
    @Tag("usd-rub")
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

    @Step("Создание IndicativeDto для EUR/RUB")
    @DisplayName("Создание IndicativeDto для EUR/RUB")
    @Description("Создает IndicativeDto с данными валютной пары EUR/RUB")
    @Tag("dto")
    @Tag("indicative")
    @Tag("eur-rub")
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

    @Step("Создание списка IndicativeDto для валютных пар")
    @DisplayName("Создание списка IndicativeDto для валютных пар")
    @Description("Создает список из 2 IndicativeDto с данными валютных пар USD/RUB и EUR/RUB")
    @Tag("list")
    @Tag("dto")
    @Tag("indicative")
    @Tag("currency")
    public static List<IndicativeDto> createCurrencyIndicativesList() {
        return Arrays.asList(
            createUsdRubIndicative(),
            createEurRubIndicative()
        );
    }

    @Step("Создание ShareFilterDto для MOEX")
    @DisplayName("Создание ShareFilterDto для MOEX")
    @Description("Создает ShareFilterDto с фильтрами для MOEX")
    @Tag("dto")
    @Tag("filter")
    @Tag("share")
    @Tag("moex")
    public static ShareFilterDto createMoexShareFilter() {
        ShareFilterDto filter = new ShareFilterDto();
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setStatus("INSTRUMENT_STATUS_BASE");
        return filter;
    }

    @Step("Создание FutureFilterDto для товарных фьючерсов")
    @DisplayName("Создание FutureFilterDto для товарных фьючерсов")
    @Description("Создает FutureFilterDto с фильтрами для товарных фьючерсов")
    @Tag("dto")
    @Tag("filter")
    @Tag("future")
    @Tag("commodity")
    public static FutureFilterDto createCommodityFutureFilter() {
        FutureFilterDto filter = new FutureFilterDto();
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("USD");
        filter.setAssetType("COMMODITY");
        return filter;
    }

    @Step("Создание IndicativeFilterDto для валютных пар")
    @DisplayName("Создание IndicativeFilterDto для валютных пар")
    @Description("Создает IndicativeFilterDto с фильтрами для валютных пар")
    @Tag("dto")
    @Tag("filter")
    @Tag("indicative")
    @Tag("currency")
    public static IndicativeFilterDto createCurrencyIndicativeFilter() {
        IndicativeFilterDto filter = new IndicativeFilterDto();
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("USD000UTSTOM");
        return filter;
    }

    // ==================== ENTITY ОБЪЕКТЫ ====================

    @Step("Создание ShareEntity с дефолтными значениями")
    @DisplayName("Создание ShareEntity (дефолтные значения)")
    @Description("Создает объект ShareEntity с предустановленными тестовыми данными для Сбербанка")
    @Tag("entity")
    @Tag("share")
    public static ShareEntity createShareEntity() {
        ShareEntity entity = new ShareEntity();
        entity.setFigi("BBG004730N88");
        entity.setTicker("SBER");
        entity.setName("Сбербанк");
        entity.setCurrency("RUB");
        entity.setExchange("moex_mrng_evng_e_wknd_dlr");
        entity.setSector("Financials");
        entity.setTradingStatus("SECURITY_TRADING_STATUS_NORMAL_TRADING");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    @Step("Создание ShareEntity с параметрами: figi={figi}, ticker={ticker}, name={name}")
    @DisplayName("Создание ShareEntity (с параметрами)")
    @Description("Создает объект ShareEntity с переданными параметрами figi, ticker и name")
    @Tag("entity")
    @Tag("share")
    public static ShareEntity createShareEntity(String figi, String ticker, String name, String exchange) {
        ShareEntity entity = new ShareEntity();
        entity.setFigi(figi);
        entity.setTicker(ticker);
        entity.setName(name);
        entity.setCurrency("RUB");
        entity.setExchange(exchange);
        entity.setSector("Financials");
        entity.setTradingStatus("SECURITY_TRADING_STATUS_NORMAL_TRADING");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
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

    @Step("Создание IndicativeEntity с параметрами: figi={figi}, ticker={ticker}, name={name}")
    @DisplayName("Создание IndicativeEntity (с параметрами)")
    @Description("Создает объект IndicativeEntity с переданными параметрами figi, ticker и name")
    @Tag("entity")
    @Tag("indicative")
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
            createShareEntity("BBG004730N88", "SBER", "Сбербанк","TESTMOEX"),
            createShareEntity("BBG004730ZJ9", "GAZP", "Газпром","TESTMOEX"),
            createShareEntity("BBG004730N88", "LKOH", "Лукойл","TESTMOEX")
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

    // ==================== CANDLE DTO ОБЪЕКТЫ ====================

    @Step("Создание CandleDto с дефолтными значениями")
    @DisplayName("Создание CandleDto (дефолтные значения)")
    @Description("Создает объект CandleDto с предустановленными тестовыми данными для Сбербанка")
    @Tag("dto")
    @Tag("candle")
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

    @Step("Создание CandleDto с параметрами: figi={figi}, volume={volume}, high={high}, low={low}")
    @DisplayName("Создание CandleDto (с параметрами)")
    @Description("Создает объект CandleDto с переданными параметрами")
    @Tag("dto")
    @Tag("candle")
    public static CandleDto createCandleDto(String figi, long volume, BigDecimal high, BigDecimal low, 
                                          Instant time, BigDecimal open, BigDecimal close, boolean isComplete) {
        return new CandleDto(figi, volume, high, low, time, open, close, isComplete);
    }

    @Step("Создание MinuteCandleRequestDto с дефолтными значениями")
    @DisplayName("Создание MinuteCandleRequestDto (дефолтные значения)")
    @Description("Создает объект MinuteCandleRequestDto с предустановленными тестовыми данными")
    @Tag("dto")
    @Tag("request")
    @Tag("candle")
    public static MinuteCandleRequestDto createMinuteCandleRequestDto() {
        MinuteCandleRequestDto request = new MinuteCandleRequestDto();
        request.setInstruments(Arrays.asList("BBG004730N88"));
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        return request;
    }

    @Step("Создание MinuteCandleRequestDto с параметрами: instruments={instruments}, assetType={assetType}, date={date}")
    @DisplayName("Создание MinuteCandleRequestDto (с параметрами)")
    @Description("Создает объект MinuteCandleRequestDto с переданными параметрами")
    @Tag("dto")
    @Tag("request")
    @Tag("candle")
    public static MinuteCandleRequestDto createMinuteCandleRequestDto(List<String> instruments, 
                                                                    List<String> assetType, 
                                                                    LocalDate date) {
        MinuteCandleRequestDto request = new MinuteCandleRequestDto();
        request.setInstruments(instruments);
        request.setAssetType(assetType);
        request.setDate(date);
        return request;
    }

    @Step("Создание MinuteCandleRequestDto для множественных инструментов")
    @DisplayName("Создание MinuteCandleRequestDto для множественных инструментов")
    @Description("Создает объект MinuteCandleRequestDto с несколькими инструментами")
    @Tag("dto")
    @Tag("request")
    @Tag("candle")
    @Tag("multiple")
    public static MinuteCandleRequestDto createMultipleInstrumentsRequest() {
        MinuteCandleRequestDto request = new MinuteCandleRequestDto();
        request.setInstruments(Arrays.asList("BBG004730N88", "BBG004730ZJ29"));
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        return request;
    }

    @Step("Создание MinuteCandleRequestDto с пустым списком инструментов")
    @DisplayName("Создание MinuteCandleRequestDto с пустым списком инструментов")
    @Description("Создает объект MinuteCandleRequestDto с пустым списком инструментов для загрузки из БД")
    @Tag("dto")
    @Tag("request")
    @Tag("candle")
    @Tag("empty")
    public static MinuteCandleRequestDto createEmptyInstrumentsRequest() {
        MinuteCandleRequestDto request = new MinuteCandleRequestDto();
        request.setInstruments(Arrays.asList());
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        return request;
    }

    @Step("Создание MinuteCandleRequestDto для разных типов активов")
    @DisplayName("Создание MinuteCandleRequestDto для разных типов активов")
    @Description("Создает объект MinuteCandleRequestDto с разными типами активов")
    @Tag("dto")
    @Tag("request")
    @Tag("candle")
    @Tag("mixed")
    public static MinuteCandleRequestDto createMixedAssetTypesRequest() {
        MinuteCandleRequestDto request = new MinuteCandleRequestDto();
        request.setInstruments(Arrays.asList());
        request.setAssetType(Arrays.asList("SHARES", "FUTURES", "INDICATIVES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        return request;
    }

    @Step("Создание MinuteCandleRequestDto с null значениями")
    @DisplayName("Создание MinuteCandleRequestDto с null значениями")
    @Description("Создает объект MinuteCandleRequestDto с null значениями для тестирования обработки ошибок")
    @Tag("dto")
    @Tag("request")
    @Tag("candle")
    @Tag("null")
    public static MinuteCandleRequestDto createNullValuesRequest() {
        MinuteCandleRequestDto request = new MinuteCandleRequestDto();
        request.setInstruments(null);
        request.setAssetType(null);
        request.setDate(null);
        return request;
    }

    @Step("Создание MinuteCandleRequestDto с будущей датой")
    @DisplayName("Создание MinuteCandleRequestDto с будущей датой")
    @Description("Создает объект MinuteCandleRequestDto с будущей датой для тестирования граничных случаев")
    @Tag("dto")
    @Tag("request")
    @Tag("candle")
    @Tag("future")
    public static MinuteCandleRequestDto createFutureDateRequest() {
        MinuteCandleRequestDto request = new MinuteCandleRequestDto();
        request.setInstruments(Arrays.asList("BBG004730N88"));
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.now().plusDays(1));
        return request;
    }

    @Step("Создание MinuteCandleRequestDto с большим количеством инструментов")
    @DisplayName("Создание MinuteCandleRequestDto с большим количеством инструментов")
    @Description("Создает объект MinuteCandleRequestDto с большим количеством инструментов для тестирования производительности")
    @Tag("dto")
    @Tag("request")
    @Tag("candle")
    @Tag("large")
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

    @Step("Создание списка CandleDto с 2 свечами")
    @DisplayName("Создание списка CandleDto")
    @Description("Создает список из 2 объектов CandleDto с тестовыми данными")
    @Tag("list")
    @Tag("dto")
    @Tag("candle")
    public static List<CandleDto> createCandleDtoList() {
        return Arrays.asList(
            createCandleDto("BBG004730N88", 1000L, BigDecimal.valueOf(105.0), BigDecimal.valueOf(95.0), 
                         Instant.now(), BigDecimal.valueOf(102.0), BigDecimal.valueOf(100.0), true),
            createCandleDto("BBG004730N88", 1200L, BigDecimal.valueOf(108.0), BigDecimal.valueOf(98.0), 
                         Instant.now().minusSeconds(3600), BigDecimal.valueOf(106.0), BigDecimal.valueOf(103.0), true)
        );
    }

    @Step("Создание списка CandleDto с незакрытыми свечами")
    @DisplayName("Создание списка CandleDto с незакрытыми свечами")
    @Description("Создает список CandleDto с незакрытыми свечами для тестирования фильтрации")
    @Tag("list")
    @Tag("dto")
    @Tag("candle")
    @Tag("incomplete")
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

    @Step("Создание списка CandleDto с неверными данными")
    @DisplayName("Создание списка CandleDto с неверными данными")
    @Description("Создает список CandleDto с неверными данными для тестирования обработки ошибок")
    @Tag("list")
    @Tag("dto")
    @Tag("candle")
    @Tag("invalid")
    public static List<CandleDto> createInvalidCandleDtoList() {
        return Arrays.asList(
            createCandleDto("BBG004730N88", 1000L, BigDecimal.valueOf(105.0), BigDecimal.valueOf(95.0), 
                         Instant.now(), BigDecimal.valueOf(102.0), BigDecimal.valueOf(100.0), true),
            createCandleDto("BBG004730N88", 0L, BigDecimal.ZERO, BigDecimal.ZERO, 
                         Instant.EPOCH, BigDecimal.ZERO, BigDecimal.ZERO, true) // Неверные данные
        );
    }

    @Step("Создание пустого списка CandleDto")
    @DisplayName("Создание пустого списка CandleDto")
    @Description("Создает пустой список CandleDto для тестирования обработки пустых ответов")
    @Tag("list")
    @Tag("dto")
    @Tag("candle")
    @Tag("empty")
    public static List<CandleDto> createEmptyCandleDtoList() {
        return Arrays.asList();
    }

    // ==================== DAILY CANDLE DTO ОБЪЕКТЫ ====================

    @Step("Создание DailyCandleRequestDto с дефолтными значениями")
    @DisplayName("Создание DailyCandleRequestDto (дефолтные значения)")
    @Description("Создает объект DailyCandleRequestDto с предустановленными тестовыми данными")
    @Tag("dto")
    @Tag("request")
    @Tag("daily-candle")
    public static DailyCandleRequestDto createDailyCandleRequestDto() {
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(Arrays.asList("BBG004730N88"));
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        return request;
    }

    @Step("Создание DailyCandleRequestDto с параметрами: instruments={instruments}, assetType={assetType}, date={date}")
    @DisplayName("Создание DailyCandleRequestDto (с параметрами)")
    @Description("Создает объект DailyCandleRequestDto с переданными параметрами")
    @Tag("dto")
    @Tag("request")
    @Tag("daily-candle")
    public static DailyCandleRequestDto createDailyCandleRequestDto(List<String> instruments, 
                                                                  List<String> assetType, 
                                                                  LocalDate date) {
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(instruments);
        request.setAssetType(assetType);
        request.setDate(date);
        return request;
    }

    @Step("Создание DailyCandleRequestDto для множественных инструментов")
    @DisplayName("Создание DailyCandleRequestDto для множественных инструментов")
    @Description("Создает объект DailyCandleRequestDto с несколькими инструментами")
    @Tag("dto")
    @Tag("request")
    @Tag("daily-candle")
    @Tag("multiple")
    public static DailyCandleRequestDto createMultipleInstrumentsDailyRequest() {
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(Arrays.asList("BBG004730N88", "BBG004730ZJ29"));
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        return request;
    }

    @Step("Создание DailyCandleRequestDto с пустым списком инструментов")
    @DisplayName("Создание DailyCandleRequestDto с пустым списком инструментов")
    @Description("Создает объект DailyCandleRequestDto с пустым списком инструментов для загрузки из БД")
    @Tag("dto")
    @Tag("request")
    @Tag("daily-candle")
    @Tag("empty")
    public static DailyCandleRequestDto createEmptyInstrumentsDailyRequest() {
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(Arrays.asList());
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        return request;
    }

    @Step("Создание DailyCandleRequestDto для разных типов активов")
    @DisplayName("Создание DailyCandleRequestDto для разных типов активов")
    @Description("Создает объект DailyCandleRequestDto с разными типами активов")
    @Tag("dto")
    @Tag("request")
    @Tag("daily-candle")
    @Tag("mixed")
    public static DailyCandleRequestDto createMixedAssetTypesDailyRequest() {
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(Arrays.asList());
        request.setAssetType(Arrays.asList("SHARES", "FUTURES", "INDICATIVES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        return request;
    }

    @Step("Создание DailyCandleRequestDto с null значениями")
    @DisplayName("Создание DailyCandleRequestDto с null значениями")
    @Description("Создает объект DailyCandleRequestDto с null значениями для тестирования обработки ошибок")
    @Tag("dto")
    @Tag("request")
    @Tag("daily-candle")
    @Tag("null")
    public static DailyCandleRequestDto createNullValuesDailyRequest() {
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(null);
        request.setAssetType(null);
        request.setDate(null);
        return request;
    }

    @Step("Создание DailyCandleRequestDto с будущей датой")
    @DisplayName("Создание DailyCandleRequestDto с будущей датой")
    @Description("Создает объект DailyCandleRequestDto с будущей датой для тестирования граничных случаев")
    @Tag("dto")
    @Tag("request")
    @Tag("daily-candle")
    @Tag("future")
    public static DailyCandleRequestDto createFutureDateDailyRequest() {
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(Arrays.asList("BBG004730N88"));
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.now().plusDays(1));
        return request;
    }

    @Step("Создание DailyCandleRequestDto с большим количеством инструментов")
    @DisplayName("Создание DailyCandleRequestDto с большим количеством инструментов")
    @Description("Создает объект DailyCandleRequestDto с большим количеством инструментов для тестирования производительности")
    @Tag("dto")
    @Tag("request")
    @Tag("daily-candle")
    @Tag("large")
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

    @Step("Создание списка CandleDto для дневных свечей с 2 свечами")
    @DisplayName("Создание списка CandleDto для дневных свечей")
    @Description("Создает список из 2 объектов CandleDto с тестовыми данными для дневных свечей")
    @Tag("list")
    @Tag("dto")
    @Tag("daily-candle")
    public static List<CandleDto> createDailyCandleDtoList() {
        return Arrays.asList(
            createCandleDto("BBG004730N88", 1000L, BigDecimal.valueOf(105.0), BigDecimal.valueOf(95.0), 
                         Instant.now(), BigDecimal.valueOf(102.0), BigDecimal.valueOf(100.0), true),
            createCandleDto("BBG004730N88", 1200L, BigDecimal.valueOf(108.0), BigDecimal.valueOf(98.0), 
                         Instant.now().minusSeconds(86400), BigDecimal.valueOf(106.0), BigDecimal.valueOf(103.0), true)
        );
    }

    @Step("Создание списка CandleDto для дневных свечей с незакрытыми свечами")
    @DisplayName("Создание списка CandleDto для дневных свечей с незакрытыми свечами")
    @Description("Создает список CandleDto для дневных свечей с незакрытыми свечами для тестирования фильтрации")
    @Tag("list")
    @Tag("dto")
    @Tag("daily-candle")
    @Tag("incomplete")
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

    @Step("Создание списка CandleDto для дневных свечей с неверными данными")
    @DisplayName("Создание списка CandleDto для дневных свечей с неверными данными")
    @Description("Создает список CandleDto для дневных свечей с неверными данными для тестирования обработки ошибок")
    @Tag("list")
    @Tag("dto")
    @Tag("daily-candle")
    @Tag("invalid")
    public static List<CandleDto> createInvalidDailyCandleDtoList() {
        return Arrays.asList(
            createCandleDto("BBG004730N88", 1000L, BigDecimal.valueOf(105.0), BigDecimal.valueOf(95.0), 
                         Instant.now(), BigDecimal.valueOf(102.0), BigDecimal.valueOf(100.0), true),
            createCandleDto("BBG004730N88", 0L, BigDecimal.ZERO, BigDecimal.ZERO, 
                         Instant.EPOCH, BigDecimal.ZERO, BigDecimal.ZERO, true) // Неверные данные
        );
    }

    // ==================== EVENING SESSION DTO ОБЪЕКТЫ ====================

    @Step("Создание ClosePriceEveningSessionRequestDto с дефолтными значениями")
    @DisplayName("Создание ClosePriceEveningSessionRequestDto (дефолтные значения)")
    @Description("Создает объект ClosePriceEveningSessionRequestDto с предустановленными тестовыми данными")
    @Tag("dto")
    @Tag("request")
    @Tag("evening-session")
    public static ClosePriceEveningSessionRequestDto createEveningSessionRequestDto() {
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(Arrays.asList("TEST_SHARE_001", "TEST_FUTURE_001"));
        return request;
    }

    @Step("Создание ClosePriceEveningSessionRequestDto с параметрами: instruments={instruments}")
    @DisplayName("Создание ClosePriceEveningSessionRequestDto (с параметрами)")
    @Description("Создает объект ClosePriceEveningSessionRequestDto с переданными параметрами")
    @Tag("dto")
    @Tag("request")
    @Tag("evening-session")
    public static ClosePriceEveningSessionRequestDto createEveningSessionRequestDto(List<String> instruments) {
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(instruments);
        return request;
    }

    @Step("Создание ClosePriceEveningSessionRequestDto с пустым списком инструментов")
    @DisplayName("Создание ClosePriceEveningSessionRequestDto с пустым списком инструментов")
    @Description("Создает объект ClosePriceEveningSessionRequestDto с пустым списком инструментов для загрузки из БД")
    @Tag("dto")
    @Tag("request")
    @Tag("evening-session")
    @Tag("empty")
    public static ClosePriceEveningSessionRequestDto createEmptyInstrumentsEveningSessionRequest() {
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(Arrays.asList());
        return request;
    }

    @Step("Создание ClosePriceEveningSessionRequestDto с null значениями")
    @DisplayName("Создание ClosePriceEveningSessionRequestDto с null значениями")
    @Description("Создает объект ClosePriceEveningSessionRequestDto с null значениями для тестирования обработки ошибок")
    @Tag("dto")
    @Tag("request")
    @Tag("evening-session")
    @Tag("null")
    public static ClosePriceEveningSessionRequestDto createNullValuesEveningSessionRequest() {
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(null);
        return request;
    }

    @Step("Создание ClosePriceEveningSessionRequestDto с большим количеством инструментов")
    @DisplayName("Создание ClosePriceEveningSessionRequestDto с большим количеством инструментов")
    @Description("Создает объект ClosePriceEveningSessionRequestDto с большим количеством инструментов для тестирования производительности")
    @Tag("dto")
    @Tag("request")
    @Tag("evening-session")
    @Tag("large")
    public static ClosePriceEveningSessionRequestDto createLargeInstrumentsEveningSessionRequest() {
        List<String> manyInstruments = new ArrayList<>();
        for (int i = 0; i < 250; i++) {
            manyInstruments.add("TEST_SHARE_" + String.format("%03d", i));
        }
        
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(manyInstruments);
        return request;
    }

    @Step("Создание ClosePriceEveningSessionRequestDto с очень большим количеством инструментов")
    @DisplayName("Создание ClosePriceEveningSessionRequestDto с очень большим количеством инструментов")
    @Description("Создает объект ClosePriceEveningSessionRequestDto с очень большим количеством инструментов для тестирования граничных случаев")
    @Tag("dto")
    @Tag("request")
    @Tag("evening-session")
    @Tag("very-large")
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

    @Step("Создание списка ClosePriceDto для вечерней сессии с 2 ценами")
    @DisplayName("Создание списка ClosePriceDto для вечерней сессии")
    @Description("Создает список из 2 объектов ClosePriceDto с тестовыми данными для вечерней сессии")
    @Tag("list")
    @Tag("dto")
    @Tag("evening-session")
    public static List<ClosePriceDto> createEveningSessionClosePriceDtoList() {
        return Arrays.asList(
            createClosePriceDto("TEST_SHARE_001", "2024-01-15", BigDecimal.valueOf(100.0), BigDecimal.valueOf(105.0)),
            createClosePriceDto("TEST_FUTURE_001", "2024-01-15", BigDecimal.valueOf(200.0), BigDecimal.valueOf(210.0))
        );
    }

    @Step("Создание списка ClosePriceDto для вечерней сессии с неверными датами")
    @DisplayName("Создание списка ClosePriceDto для вечерней сессии с неверными датами")
    @Description("Создает список ClosePriceDto для вечерней сессии с неверными датами для тестирования фильтрации")
    @Tag("list")
    @Tag("dto")
    @Tag("evening-session")
    @Tag("invalid-date")
    public static List<ClosePriceDto> createInvalidDateEveningSessionClosePriceDtoList() {
        return Arrays.asList(
            createClosePriceDto("TEST_SHARE_001", "2024-01-15", BigDecimal.valueOf(100.0), BigDecimal.valueOf(105.0)),
            createClosePriceDto("TEST_FUTURE_001", "1970-01-01", BigDecimal.valueOf(200.0), BigDecimal.valueOf(210.0)) // Неверная дата
        );
    }

    @Step("Создание списка ClosePriceDto для вечерней сессии с вечерними ценами")
    @DisplayName("Создание списка ClosePriceDto для вечерней сессии с вечерними ценами")
    @Description("Создает список ClosePriceDto для вечерней сессии с вечерними ценами для тестирования приоритета")
    @Tag("list")
    @Tag("dto")
    @Tag("evening-session")
    @Tag("evening-price")
    public static List<ClosePriceDto> createEveningPriceClosePriceDtoList() {
        return Arrays.asList(
            createClosePriceDto("TEST_SHARE_001", "2024-01-15", BigDecimal.valueOf(100.0), BigDecimal.valueOf(105.0))
        );
    }

    @Step("Создание списка ClosePriceDto для вечерней сессии без цен")
    @DisplayName("Создание списка ClosePriceDto для вечерней сессии без цен")
    @Description("Создает список ClosePriceDto для вечерней сессии без цен для тестирования обработки пустых данных")
    @Tag("list")
    @Tag("dto")
    @Tag("evening-session")
    @Tag("no-prices")
    public static List<ClosePriceDto> createNoPricesEveningSessionClosePriceDtoList() {
        return Arrays.asList(
            createClosePriceDto("TEST_SHARE_001", "2024-01-15", null, null) // Нет цен
        );
    }

    @Step("Создание пустого списка ClosePriceDto для вечерней сессии")
    @DisplayName("Создание пустого списка ClosePriceDto для вечерней сессии")
    @Description("Создает пустой список ClosePriceDto для тестирования обработки пустых ответов")
    @Tag("list")
    @Tag("dto")
    @Tag("evening-session")
    @Tag("empty")
    public static List<ClosePriceDto> createEmptyEveningSessionClosePriceDtoList() {
        return Arrays.asList();
    }

    // ==================== CLOSE PRICE DTO ОБЪЕКТЫ ====================

    @Step("Создание ClosePriceDto с параметрами: figi={figi}, date={date}, closePrice={closePrice}, eveningSessionPrice={eveningSessionPrice}")
    @DisplayName("Создание ClosePriceDto (с параметрами)")
    @Description("Создает объект ClosePriceDto с переданными параметрами")
    @Tag("dto")
    @Tag("close-price")
    public static ClosePriceDto createClosePriceDto(String figi, String date, BigDecimal closePrice, BigDecimal eveningSessionPrice) {
        return new ClosePriceDto(figi, date, closePrice, eveningSessionPrice);
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

    // ==================== MORNING SESSION DTO ОБЪЕКТЫ ====================

    @Step("Создание OpenPriceDto с дефолтными значениями")
    @DisplayName("Создание OpenPriceDto с дефолтными значениями")
    @Description("Создает объект OpenPriceDto с предустановленными значениями для тестирования")
    @Tag("dto")
    @Tag("morning-session")
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

    @Step("Создание OpenPriceDto с заданными параметрами")
    @DisplayName("Создание OpenPriceDto с заданными параметрами")
    @Description("Создает объект OpenPriceDto с указанными параметрами")
    @Tag("dto")
    @Tag("morning-session")
    public static OpenPriceDto createOpenPriceDto(String figi, LocalDate priceDate, BigDecimal openPrice, 
                                                  String instrumentType, String currency, String exchange) {
        return new OpenPriceDto(figi, priceDate, openPrice, instrumentType, currency, exchange);
    }

    @Step("Создание списка OpenPriceDto")
    @DisplayName("Создание списка OpenPriceDto")
    @Description("Создает список объектов OpenPriceDto указанного размера")
    @Tag("dto")
    @Tag("morning-session")
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

    @Step("Создание пустого списка OpenPriceDto")
    @DisplayName("Создание пустого списка OpenPriceDto")
    @Description("Создает пустой список объектов OpenPriceDto")
    @Tag("dto")
    @Tag("morning-session")
    public static List<OpenPriceDto> createEmptyOpenPriceDtoList() {
        return new ArrayList<>();
    }

    // ==================== MORNING SESSION ENTITY ОБЪЕКТЫ ====================

    @Step("Создание OpenPriceEntity с дефолтными значениями")
    @DisplayName("Создание OpenPriceEntity с дефолтными значениями")
    @Description("Создает объект OpenPriceEntity с предустановленными значениями для тестирования")
    @Tag("entity")
    @Tag("morning-session")
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

    @Step("Создание OpenPriceEntity с заданными параметрами")
    @DisplayName("Создание OpenPriceEntity с заданными параметрами")
    @Description("Создает объект OpenPriceEntity с указанными параметрами")
    @Tag("entity")
    @Tag("morning-session")
    public static OpenPriceEntity createOpenPriceEntity(String figi, LocalDate date, BigDecimal price,
                                                        String instrumentType, String currency, String exchange) {
        return new OpenPriceEntity(date, figi, instrumentType, price, currency, exchange);
    }

    @Step("Создание MinuteCandleEntity с заданными параметрами")
    @DisplayName("Создание MinuteCandleEntity с заданными параметрами")
    @Description("Создает объект MinuteCandleEntity с указанными параметрами")
    @Tag("entity")
    @Tag("morning-session")
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

    @Step("Создание пустого списка MinuteCandleEntity")
    @DisplayName("Создание пустого списка MinuteCandleEntity")
    @Description("Создает пустой список объектов MinuteCandleEntity")
    @Tag("entity")
    @Tag("morning-session")
    public static List<MinuteCandleEntity> createEmptyMinuteCandleEntityList() {
        return new ArrayList<>();
    }

    @Step("Создание списка ShareEntity")
    @DisplayName("Создание списка ShareEntity")
    @Description("Создает список объектов ShareEntity указанного размера")
    @Tag("entity")
    @Tag("morning-session")
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

    @Step("Создание пустого списка ShareEntity")
    @DisplayName("Создание пустого списка ShareEntity")
    @Description("Создает пустой список объектов ShareEntity")
    @Tag("entity")
    @Tag("morning-session")
    public static List<ShareEntity> createEmptyShareEntityList() {
        return new ArrayList<>();
    }

    @Step("Создание пустого списка FutureEntity")
    @DisplayName("Создание пустого списка FutureEntity")
    @Description("Создает пустой список объектов FutureEntity")
    @Tag("entity")
    @Tag("morning-session")
    public static List<FutureEntity> createEmptyFutureEntityList() {
        return new ArrayList<>();
    }

    @Step("Создание пустого списка IndicativeEntity")
    @DisplayName("Создание пустого списка IndicativeEntity")
    @Description("Создает пустой список объектов IndicativeEntity")
    @Tag("entity")
    @Tag("morning-session")
    public static List<IndicativeEntity> createEmptyIndicativeEntityList() {
        return new ArrayList<>();
    }

    @Step("Создание списка OpenPriceEntity")
    @DisplayName("Создание списка OpenPriceEntity")
    @Description("Создает список объектов OpenPriceEntity указанного размера")
    @Tag("entity")
    @Tag("morning-session")
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

    @Step("Создание пустого списка OpenPriceEntity")
    @DisplayName("Создание пустого списка OpenPriceEntity")
    @Description("Создает пустой список объектов OpenPriceEntity")
    @Tag("entity")
    @Tag("morning-session")
    public static List<OpenPriceEntity> createEmptyOpenPriceEntityList() {
        return new ArrayList<>();
    }

    // ==================== MORNING SESSION REQUEST DTO ОБЪЕКТЫ ====================
    // Примечание: MorningSessionRequestDto не существует в проекте, поэтому методы удалены

    // ==================== ВАЛИДАЦИЯ MORNING SESSION DTO ====================

    @Step("Проверка валидности OpenPriceDto")
    @DisplayName("Проверка валидности OpenPriceDto")
    @Description("Проверяет валидность объекта OpenPriceDto (не null и все обязательные поля заполнены)")
    @Tag("validation")
    @Tag("morning-session")
    public static boolean isOpenPriceDtoValid(OpenPriceDto dto) {
        return dto != null &&
               dto.figi() != null && !dto.figi().trim().isEmpty() &&
               dto.priceDate() != null &&
               dto.openPrice() != null && dto.openPrice().compareTo(BigDecimal.ZERO) > 0 &&
               dto.instrumentType() != null && !dto.instrumentType().trim().isEmpty() &&
               dto.currency() != null && !dto.currency().trim().isEmpty() &&
               dto.exchange() != null && !dto.exchange().trim().isEmpty();
    }

    // Примечание: MorningSessionRequestDto не существует в проекте, поэтому метод валидации удален
}
