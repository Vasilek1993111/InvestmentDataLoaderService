package com.example.InvestmentDataLoaderService.integration;

import com.example.InvestmentDataLoaderService.dto.ShareDto;
import com.example.InvestmentDataLoaderService.dto.ShareFilterDto;
import com.example.InvestmentDataLoaderService.dto.FutureDto;
import com.example.InvestmentDataLoaderService.dto.IndicativeDto;
import com.example.InvestmentDataLoaderService.service.InstrumentService;

import jakarta.annotation.PostConstruct;

import com.example.InvestmentDataLoaderService.service.CachedInstrumentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    void compareSharesFromTinkoffToDatabase() {
        // Создаем фильтры для поиска акций
        ShareFilterDto filter = new ShareFilterDto();

        List<ShareDto> sharesFromTinkoff = instrumentService.getShares(null, null, "RUB", null, null);
        List<ShareDto> sharesFromDatabase = instrumentService.getSharesFromDatabase(filter);
        
       
        // Проверяем, что количество акций в БД больше или равно количеству из API
        assertTrue(sharesFromDatabase.size() >= sharesFromTinkoff.size(), 
            String.format("Количество акций в БД (%d) должно быть больше или равно количеству из API (%d)", 
            sharesFromDatabase.size(), sharesFromTinkoff.size()));
    }

    /**
     * Проверяет, что все FIGI акций из API присутствуют в БД
     */
    @Test
    void compareSharesFigis(){
        ShareFilterDto filter = new ShareFilterDto();
        filter.setCurrency("RUB");

        List<ShareDto> sharesFromTinkoff = instrumentService.getShares(null, null, "RUB", null, null);
        List<ShareDto> sharesFromDatabase = instrumentService.getSharesFromDatabase(filter);
        Set<String> figisFromTinkoff = sharesFromTinkoff.stream().map(ShareDto::figi).collect(Collectors.toSet());
        Set<String> figisFromDatabase = sharesFromDatabase.stream().map(ShareDto::figi).collect(Collectors.toSet());

        assertTrue(figisFromDatabase.containsAll(figisFromTinkoff), 
        String.format("Все FIGI из API должны присутствовать в БД. Отсутствуют: %s", 
            figisFromTinkoff.stream()
                .filter(figi -> !figisFromDatabase.contains(figi))
                .collect(Collectors.toSet())));
    }

    // ==================== ТЕСТЫ ФЬЮЧЕРСОВ ====================

    /**
     * Проверяет, что количество фьючерсов в БД больше или равно количеству фьючерсов из API
     */
    @Test
    void compareFuturesFromTinkoffToDatabase() {
        // Получаем фьючерсы из API
        List<FutureDto> futuresFromTinkoff = instrumentService.getFutures(null, null, "RUB", null, null);
        
        // Получаем фьючерсы из БД (используем метод, который получает все фьючерсы)
        List<FutureDto> futuresFromDatabase = instrumentService.getFuturesFromDatabase();
        
      
        // Проверяем, что количество фьючерсов в БД больше или равно количеству из API
        assertTrue(futuresFromDatabase.size() >= futuresFromTinkoff.size(), 
            String.format("Количество фьючерсов в БД (%d) должно быть больше или равно количеству из API (%d)", 
            futuresFromDatabase.size(), futuresFromTinkoff.size()));
    }

    /**
     * Проверяет, что все FIGI фьючерсов из API присутствуют в БД
     */
    @Test
    void compareFuturesFigis(){
        // Получаем фьючерсы из API
        List<FutureDto> futuresFromTinkoff = instrumentService.getFutures(null, null, "RUB", null, null);
        
        // Получаем фьючерсы из БД
        List<FutureDto> futuresFromDatabase = instrumentService.getFuturesFromDatabase();
        
        Set<String> figisFromTinkoff = futuresFromTinkoff.stream().map(FutureDto::figi).collect(Collectors.toSet());
        Set<String> figisFromDatabase = futuresFromDatabase.stream().map(FutureDto::figi).collect(Collectors.toSet());

      
        assertTrue(figisFromDatabase.containsAll(figisFromTinkoff), 
            String.format("Все FIGI фьючерсов из API должны присутствовать в БД. Отсутствуют: %s", 
                figisFromTinkoff.stream()
                    .filter(figi -> !figisFromDatabase.contains(figi))
                    .collect(Collectors.toSet())));
    }

    // ==================== ТЕСТЫ ИНДИКАТИВОВ ====================

    /**
     * Проверяет, что количество индикативов в БД больше или равно количеству индикативов из API
     */
    @Test
    void compareIndicativesFromTinkoffToDatabase() {
        // Получаем индикативы из API
        List<IndicativeDto> indicativesFromTinkoff = instrumentService.getIndicatives(null, "RUB", null, null);
        
        // Получаем индикативы из БД
        List<IndicativeDto> indicativesFromDatabase = instrumentService.getIndicativesFromDatabase();
        
      
        
        // Проверяем, что количество индикативов в БД больше или равно количеству из API
        assertTrue(indicativesFromDatabase.size() >= indicativesFromTinkoff.size(), 
            String.format("Количество индикативов в БД (%d) должно быть больше или равно количеству из API (%d)", 
            indicativesFromDatabase.size(), indicativesFromTinkoff.size()));
    }

    /**
     * Проверяет, что все FIGI индикативов из API присутствуют в БД
     */
    @Test
    void compareIndicativesFigis(){
        // Получаем индикативы из API
        List<IndicativeDto> indicativesFromTinkoff = instrumentService.getIndicatives(null, "RUB", null, null);
        
        // Получаем индикативы из БД
        List<IndicativeDto> indicativesFromDatabase = instrumentService.getIndicativesFromDatabase();
        
        Set<String> figisFromTinkoff = indicativesFromTinkoff.stream().map(IndicativeDto::figi).collect(Collectors.toSet());
        Set<String> figisFromDatabase = indicativesFromDatabase.stream().map(IndicativeDto::figi).collect(Collectors.toSet());

     

        assertTrue(figisFromDatabase.containsAll(figisFromTinkoff), 
            String.format("Все FIGI индикативов из API должны присутствовать в БД. Отсутствуют: %s", 
                figisFromTinkoff.stream()
                    .filter(figi -> !figisFromDatabase.contains(figi))
                    .collect(Collectors.toSet())));
    }

    // ==================== ТЕСТЫ КЭША ====================

    /**
     * Проверяет, что количество акций в кэше больше или равно количеству акций из API
     */
    @Test
    void compareSharesFromTinkoffToCache() {
        // Получаем акции из API
        List<ShareDto> sharesFromTinkoff = instrumentService.getShares(null, null, "RUB", null, null);
        
        // Получаем акции из кэша
        List<ShareDto> sharesFromCache = cachedInstrumentService.getSharesFromCache();
        
     
        
        // Проверяем, что количество акций в кэше больше или равно количеству из API
        assertTrue(sharesFromCache.size() >= sharesFromTinkoff.size(), 
            String.format("Количество акций в кэше (%d) должно быть больше или равно количеству из API (%d)", 
            sharesFromCache.size(), sharesFromTinkoff.size()));
    }

    /**
     * Проверяет, что количество фьючерсов в кэше больше или равно количеству фьючерсов из API
     */
    @Test
    void compareFuturesFromTinkoffToCache() {
        // Получаем фьючерсы из API
        List<FutureDto> futuresFromTinkoff = instrumentService.getFutures(null, null, "RUB", null, null);
        
        // Получаем фьючерсы из кэша
        List<FutureDto> futuresFromCache = cachedInstrumentService.getFuturesFromCache();
        
        
        // Проверяем, что количество фьючерсов в кэше больше или равно количеству из API
        assertTrue(futuresFromCache.size() >= futuresFromTinkoff.size(), 
            String.format("Количество фьючерсов в кэше (%d) должно быть больше или равно количеству из API (%d)", 
            futuresFromCache.size(), futuresFromTinkoff.size()));
    }

}