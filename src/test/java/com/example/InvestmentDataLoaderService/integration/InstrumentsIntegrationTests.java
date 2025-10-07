package com.example.InvestmentDataLoaderService.integration;

import com.example.InvestmentDataLoaderService.dto.ShareDto;
import com.example.InvestmentDataLoaderService.dto.ShareFilterDto;
import com.example.InvestmentDataLoaderService.dto.FutureDto;
import com.example.InvestmentDataLoaderService.dto.IndicativeDto;
import com.example.InvestmentDataLoaderService.service.InstrumentService;

import com.example.InvestmentDataLoaderService.service.CachedInstrumentService;

import io.qameta.allure.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для проверки соответствия данных из API Т-Инвест с данными в БД
 * 
 * <p>Тесты проверяют:</p>
 * <ul>
 *   <li>Количество акций в БД больше или равно количеству акций из API</li>
 *   <li>Все FIGI из API присутствуют в БД</li>
 *   <li>Количество фьючерсов в БД больше или равно количеству фьючерсов из API</li>
 *   <li>Все FIGI фьючерсов из API присутствуют в БД</li>
 *   <li>Количество индикативов в БД больше или равно количеству индикативов из API</li>
 *   <li>Все FIGI индикативов из API присутствуют в БД</li>
 *   <li>Аналогичные проверки для кэша</li>
 * </ul>
 * 
 * <p>Тесты выполняются в полном контексте Spring с подключением к реальной БД и API.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@Epic("Integration Tests")
@Feature("Instruments Data Consistency")
@DisplayName("Instruments Integration Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
public class InstrumentsIntegrationTests {

    @Autowired
    private InstrumentService instrumentService;
    
    @Autowired
    private CachedInstrumentService cachedInstrumentService;
    



    // ==================== ТЕСТЫ АКЦИЙ ====================

    /**
     * Проверяет, что количество акций в БД больше или равно количеству акций из API
     */
    @Test
    @DisplayName("Сравнение количества акций из API с БД")
    @Description("Интеграционный тест проверяет, что количество акций в БД больше или равно количеству акций из API Т-Инвест")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares Data Consistency")
    @Tag("integration")
    @Tag("shares")
    @Tag("api")
    @Tag("database")
    @Tag("positive")
    void compareSharesFromTinkoffToDatabase() {
        // Шаг 1: Получение данных из API
        List<ShareDto> sharesFromTinkoff = Allure.step("Получение акций из API Т-Инвест", () -> {
            return instrumentService.getShares(null, null, "RUB", null, null);
        });
        
        // Шаг 2: Получение данных из БД
        List<ShareDto> sharesFromDatabase = Allure.step("Получение акций из базы данных", () -> {
            ShareFilterDto filter = new ShareFilterDto();
            return instrumentService.getSharesFromDatabase(filter);
        });
        
        // Шаг 3: Проверка соответствия количества
        Allure.step("Проверка соответствия количества акций", () -> {
            assertTrue(sharesFromDatabase.size() >= sharesFromTinkoff.size(), 
                String.format("Количество акций в БД (%d) должно быть больше или равно количеству из API (%d)", 
                sharesFromDatabase.size(), sharesFromTinkoff.size()));
        });
    }

    /**
     * Проверяет, что все FIGI акций из API присутствуют в БД
     */
    @Test
    @DisplayName("Сравнение FIGI акций из API с БД")
    @Description("Интеграционный тест проверяет, что все FIGI акций из API Т-Инвест присутствуют в БД")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares Data Consistency")
    @Tag("integration")
    @Tag("shares")
    @Tag("api")
    @Tag("database")
    @Tag("figi")
    @Tag("positive")
    void compareSharesFigis(){
        // Шаг 1: Получение данных из API
        List<ShareDto> sharesFromTinkoff = Allure.step("Получение акций из API Т-Инвест", () -> {
            return instrumentService.getShares(null, null, "RUB", null, null);
        });
        
        // Шаг 2: Получение данных из БД
        List<ShareDto> sharesFromDatabase = Allure.step("Получение акций из базы данных", () -> {
            ShareFilterDto filter = new ShareFilterDto();
            filter.setCurrency("RUB");
            return instrumentService.getSharesFromDatabase(filter);
        });
        
        // Шаг 3: Извлечение FIGI
        Set<String> figisFromTinkoff = Allure.step("Извлечение FIGI из данных API", () -> {
            return sharesFromTinkoff.stream().map(ShareDto::figi).collect(Collectors.toSet());
        });
        
        Set<String> figisFromDatabase = Allure.step("Извлечение FIGI из данных БД", () -> {
            return sharesFromDatabase.stream().map(ShareDto::figi).collect(Collectors.toSet());
        });
        
        // Шаг 4: Проверка соответствия FIGI
        Allure.step("Проверка соответствия FIGI акций", () -> {
            assertTrue(figisFromDatabase.containsAll(figisFromTinkoff), 
            String.format("Все FIGI из API должны присутствовать в БД. Отсутствуют: %s", 
                figisFromTinkoff.stream()
                    .filter(figi -> !figisFromDatabase.contains(figi))
                    .collect(Collectors.toSet())));
        });
    }

    // ==================== ТЕСТЫ ФЬЮЧЕРСОВ ====================

    /**
     * Проверяет, что количество фьючерсов в БД больше или равно количеству фьючерсов из API
     */
    @Test
    @DisplayName("Сравнение количества фьючерсов из API с БД")
    @Description("Интеграционный тест проверяет, что количество фьючерсов в БД больше или равно количеству фьючерсов из API Т-Инвест")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures Data Consistency")
    @Tag("integration")
    @Tag("futures")
    @Tag("api")
    @Tag("database")
    @Tag("positive")
    void compareFuturesFromTinkoffToDatabase() {
        // Шаг 1: Получение данных из API
        List<FutureDto> futuresFromTinkoff = Allure.step("Получение фьючерсов из API Т-Инвест", () -> {
            return instrumentService.getFutures(null, null, "RUB", null, null);
        });
        
        // Шаг 2: Получение данных из БД
        List<FutureDto> futuresFromDatabase = Allure.step("Получение фьючерсов из базы данных", () -> {
            return instrumentService.getFuturesFromDatabase();
        });
        
        // Шаг 3: Проверка соответствия количества
        Allure.step("Проверка соответствия количества фьючерсов", () -> {
            assertTrue(futuresFromDatabase.size() >= futuresFromTinkoff.size(), 
                String.format("Количество фьючерсов в БД (%d) должно быть больше или равно количеству из API (%d)", 
                futuresFromDatabase.size(), futuresFromTinkoff.size()));
        });
    }

    /**
     * Проверяет, что все FIGI фьючерсов из API присутствуют в БД
     */
    @Test
    @DisplayName("Сравнение FIGI фьючерсов из API с БД")
    @Description("Интеграционный тест проверяет, что все FIGI фьючерсов из API Т-Инвест присутствуют в БД")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures Data Consistency")
    @Tag("integration")
    @Tag("futures")
    @Tag("api")
    @Tag("database")
    @Tag("figi")
    @Tag("positive")
    void compareFuturesFigis(){
        // Шаг 1: Получение данных из API
        List<FutureDto> futuresFromTinkoff = Allure.step("Получение фьючерсов из API Т-Инвест", () -> {
            return instrumentService.getFutures(null, null, "RUB", null, null);
        });
        
        // Шаг 2: Получение данных из БД
        List<FutureDto> futuresFromDatabase = Allure.step("Получение фьючерсов из базы данных", () -> {
            return instrumentService.getFuturesFromDatabase();
        });
        
        // Шаг 3: Извлечение FIGI
        Set<String> figisFromTinkoff = Allure.step("Извлечение FIGI из данных API", () -> {
            return futuresFromTinkoff.stream().map(FutureDto::figi).collect(Collectors.toSet());
        });
        
        Set<String> figisFromDatabase = Allure.step("Извлечение FIGI из данных БД", () -> {
            return futuresFromDatabase.stream().map(FutureDto::figi).collect(Collectors.toSet());
        });
        
        // Шаг 4: Проверка соответствия FIGI
        Allure.step("Проверка соответствия FIGI фьючерсов", () -> {
            assertTrue(figisFromDatabase.containsAll(figisFromTinkoff), 
                String.format("Все FIGI фьючерсов из API должны присутствовать в БД. Отсутствуют: %s", 
                    figisFromTinkoff.stream()
                        .filter(figi -> !figisFromDatabase.contains(figi))
                        .collect(Collectors.toSet())));
        });
    }

    // ==================== ТЕСТЫ ИНДИКАТИВОВ ====================

    /**
     * Проверяет, что количество индикативов в БД больше или равно количеству индикативов из API
     */
    @Test
    @DisplayName("Сравнение количества индикативов из API с БД")
    @Description("Интеграционный тест проверяет, что количество индикативов в БД больше или равно количеству индикативов из API Т-Инвест")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives Data Consistency")
    @Tag("integration")
    @Tag("indicatives")
    @Tag("api")
    @Tag("database")
    @Tag("positive")
    void compareIndicativesFromTinkoffToDatabase() {
        // Шаг 1: Получение данных из API
        List<IndicativeDto> indicativesFromTinkoff = Allure.step("Получение индикативов из API Т-Инвест", () -> {
            return instrumentService.getIndicatives(null, "RUB", null, null);
        });
        
        // Шаг 2: Получение данных из БД
        List<IndicativeDto> indicativesFromDatabase = Allure.step("Получение индикативов из базы данных", () -> {
            return instrumentService.getIndicativesFromDatabase();
        });
        
        // Шаг 3: Проверка соответствия количества
        Allure.step("Проверка соответствия количества индикативов", () -> {
            assertTrue(indicativesFromDatabase.size() >= indicativesFromTinkoff.size(), 
                String.format("Количество индикативов в БД (%d) должно быть больше или равно количеству из API (%d)", 
                indicativesFromDatabase.size(), indicativesFromTinkoff.size()));
        });
    }

    /**
     * Проверяет, что все FIGI индикативов из API присутствуют в БД
     */
    @Test
    @DisplayName("Сравнение FIGI индикативов из API с БД")
    @Description("Интеграционный тест проверяет, что все FIGI индикативов из API Т-Инвест присутствуют в БД")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives Data Consistency")
    @Tag("integration")
    @Tag("indicatives")
    @Tag("api")
    @Tag("database")
    @Tag("figi")
    @Tag("positive")
    void compareIndicativesFigis(){
        // Шаг 1: Получение данных из API
        List<IndicativeDto> indicativesFromTinkoff = Allure.step("Получение индикативов из API Т-Инвест", () -> {
            return instrumentService.getIndicatives(null, "RUB", null, null);
        });
        
        // Шаг 2: Получение данных из БД
        List<IndicativeDto> indicativesFromDatabase = Allure.step("Получение индикативов из базы данных", () -> {
            return instrumentService.getIndicativesFromDatabase();
        });
        
        // Шаг 3: Извлечение FIGI
        Set<String> figisFromTinkoff = Allure.step("Извлечение FIGI из данных API", () -> {
            return indicativesFromTinkoff.stream().map(IndicativeDto::figi).collect(Collectors.toSet());
        });
        
        Set<String> figisFromDatabase = Allure.step("Извлечение FIGI из данных БД", () -> {
            return indicativesFromDatabase.stream().map(IndicativeDto::figi).collect(Collectors.toSet());
        });
        
        // Шаг 4: Проверка соответствия FIGI
        Allure.step("Проверка соответствия FIGI индикативов", () -> {
            assertTrue(figisFromDatabase.containsAll(figisFromTinkoff), 
                String.format("Все FIGI индикативов из API должны присутствовать в БД. Отсутствуют: %s", 
                    figisFromTinkoff.stream()
                        .filter(figi -> !figisFromDatabase.contains(figi))
                        .collect(Collectors.toSet())));
        });
    }

    // ==================== ТЕСТЫ КЭША ====================

    /**
     * Проверяет, что количество акций в кэше больше или равно количеству акций из API
     */
    @Test
    @DisplayName("Сравнение количества акций из API с кэшем")
    @Description("Интеграционный тест проверяет, что количество акций в кэше больше или равно количеству акций из API Т-Инвест")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Cache Data Consistency")
    @Tag("integration")
    @Tag("shares")
    @Tag("api")
    @Tag("cache")
    @Tag("positive")
    void compareSharesFromTinkoffToCache() {
        // Шаг 1: Получение данных из API
        List<ShareDto> sharesFromTinkoff = Allure.step("Получение акций из API Т-Инвест", () -> {
            return instrumentService.getShares(null, "moex_mrng_evng_e_wknd_dlr", null, null, null);
        });
        
        // Шаг 2: Получение данных из кэша
        List<ShareDto> sharesFromCache = Allure.step("Получение акций из кэша", () -> {
            return cachedInstrumentService.getSharesFromCache();
        });
        
        // Шаг 3: Проверка соответствия количества
        Allure.step("Проверка соответствия количества акций в кэше", () -> {
            assertTrue(sharesFromCache.size() >= sharesFromTinkoff.size(), 
                String.format("Количество акций в кэше (%d) должно быть больше или равно количеству из API (%d)", 
                sharesFromCache.size(), sharesFromTinkoff.size()));
        });
    }

    /**
     * Проверяет, что количество фьючерсов в кэше больше или равно количеству фьючерсов из API
     */
    @Test
    @DisplayName("Сравнение количества фьючерсов из API с кэшем")
    @Description("Интеграционный тест проверяет, что количество фьючерсов в кэше больше или равно количеству фьючерсов из API Т-Инвест")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Cache Data Consistency")
    @Tag("integration")
    @Tag("futures")
    @Tag("api")
    @Tag("cache")
    @Tag("positive")
    void compareFuturesFromTinkoffToCache() {
        // Шаг 1: Получение данных из API
        List<FutureDto> futuresFromTinkoff = Allure.step("Получение фьючерсов из API Т-Инвест", () -> {
            return instrumentService.getFutures(null, null, "RUB", null, null);
        });
        
        // Шаг 2: Получение данных из кэша
        List<FutureDto> futuresFromCache = Allure.step("Получение фьючерсов из кэша", () -> {
            return cachedInstrumentService.getFuturesFromCache();
        });
        
        // Шаг 3: Проверка соответствия количества
        Allure.step("Проверка соответствия количества фьючерсов в кэше", () -> {
            assertTrue(futuresFromCache.size() >= futuresFromTinkoff.size(), 
                String.format("Количество фьючерсов в кэше (%d) должно быть больше или равно количеству из API (%d)", 
                futuresFromCache.size(), futuresFromTinkoff.size()));
        });
    }

    @Test
    @DisplayName("Сравнение количества индикативов из API с кэшем")
    @Description("Интеграционный тест проверяет, что количество индикативов в кэше больше или равно количеству индикативов из API Т-Инвест")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Cache Data Consistency")
    @Tag("integration")
    @Tag("indicatives")
    @Tag("api")
    @Tag("cache")
    @Tag("positive")
    void compareIndicativesFromTinkoffToCache() {
        // Шаг 1: Получение данных из API
        List<IndicativeDto> indicativesFromTinkoff = Allure.step("Получение индикативов из API Т-Инвест", () -> {
            return instrumentService.getIndicatives(null, null, null, null);
        });
        
        // Шаг 2: Получение данных из кэша
        List<IndicativeDto> indicativesFromCache = Allure.step("Получение индикативов из кэша", () -> {
            return cachedInstrumentService.getIndicativesFromCache();
        });
        
        // Шаг 3: Проверка соответствия количества
        Allure.step("Проверка соответствия количества индикативов в кэше", () -> {
            assertTrue(indicativesFromCache.size() >= indicativesFromTinkoff.size(), 
                String.format("Количество индикативов в кэше (%d) должно быть больше или равно количеству из API (%d)", 
                indicativesFromCache.size(), indicativesFromTinkoff.size()));
        });
    }

}