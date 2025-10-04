package com.example.InvestmentDataLoaderService.integration;

import com.example.InvestmentDataLoaderService.dto.ShareDto;
import com.example.InvestmentDataLoaderService.dto.FutureDto;
import com.example.InvestmentDataLoaderService.dto.IndicativeDto;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.IndicativeRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.service.InstrumentService;
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
 *   <li>Количество акций и их FIGI совпадает с количеством акций и FIGI в БД</li>
 *   <li>Количество фьючерсов и их FIGI совпадает с количеством фьючерсов и FIGI в БД</li>
 *   <li>Количество индикативов и их FIGI совпадает с количеством индикативов и FIGI в БД</li>
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
    private ShareRepository shareRepository;
    
    @Autowired
    private FutureRepository futureRepository;
    
    @Autowired
    private IndicativeRepository indicativeRepository;

    /**
     * Тест проверяет соответствие акций из API Т-Инвест с данными в БД
     * 
     * <p>Проверки:</p>
     * <ul>
     *   <li>Количество акций из API равно количеству акций в БД</li>
     *   <li>Все FIGI из API присутствуют в БД</li>
     *   <li>Все FIGI из БД присутствуют в API</li>
     * </ul>
     */
    @Test
    public void testSharesDataConsistency() {
        // Получаем акции из API Т-Инвест
        List<ShareDto> apiShares = instrumentService.getShares(
            "INSTRUMENT_STATUS_BASE", 
            null, 
            null, 
            null, 
            null
        );
        
        // Получаем FIGI акций из БД
        List<String> dbFigis = shareRepository.findAllFigis();
        
        // Проверяем количество
        assertEquals(apiShares.size(), dbFigis.size(), 
            "Количество акций из API должно совпадать с количеством в БД");
        
        // Извлекаем FIGI из API
        Set<String> apiFigis = apiShares.stream()
            .map(ShareDto::figi)
            .collect(Collectors.toSet());
        
        // Преобразуем список FIGI из БД в Set для удобства сравнения
        Set<String> dbFigisSet = dbFigis.stream()
            .collect(Collectors.toSet());
        
        // Проверяем, что все FIGI из API присутствуют в БД
        assertTrue(apiFigis.containsAll(dbFigisSet), 
            "Все FIGI из БД должны присутствовать в API");
        
        // Проверяем, что все FIGI из БД присутствуют в API
        assertTrue(dbFigisSet.containsAll(apiFigis), 
            "Все FIGI из API должны присутствовать в БД");
        
        // Дополнительная проверка: количество уникальных FIGI
        assertEquals(apiFigis.size(), dbFigisSet.size(), 
            "Количество уникальных FIGI должно совпадать");
    }

    /**
     * Тест проверяет соответствие фьючерсов из API Т-Инвест с данными в БД
     * 
     * <p>Проверки:</p>
     * <ul>
     *   <li>Количество фьючерсов из API равно количеству фьючерсов в БД</li>
     *   <li>Все FIGI из API присутствуют в БД</li>
     *   <li>Все FIGI из БД присутствуют в API</li>
     * </ul>
     */
    @Test
    public void testFuturesDataConsistency() {
        // Получаем фьючерсы из API Т-Инвест
        List<FutureDto> apiFutures = instrumentService.getFutures(
            "INSTRUMENT_STATUS_BASE", 
            null, 
            null, 
            null, 
            null
        );
        
        // Получаем все фьючерсы из БД
        List<com.example.InvestmentDataLoaderService.entity.FutureEntity> dbFutures = 
            futureRepository.findAll();
        
        // Проверяем количество
        assertEquals(apiFutures.size(), dbFutures.size(), 
            "Количество фьючерсов из API должно совпадать с количеством в БД");
        
        // Извлекаем FIGI из API
        Set<String> apiFigis = apiFutures.stream()
            .map(FutureDto::figi)
            .collect(Collectors.toSet());
        
        // Извлекаем FIGI из БД
        Set<String> dbFigis = dbFutures.stream()
            .map(com.example.InvestmentDataLoaderService.entity.FutureEntity::getFigi)
            .collect(Collectors.toSet());
        
        // Проверяем, что все FIGI из API присутствуют в БД
        assertTrue(apiFigis.containsAll(dbFigis), 
            "Все FIGI из БД должны присутствовать в API");
        
        // Проверяем, что все FIGI из БД присутствуют в API
        assertTrue(dbFigis.containsAll(apiFigis), 
            "Все FIGI из API должны присутствовать в БД");
        
        // Дополнительная проверка: количество уникальных FIGI
        assertEquals(apiFigis.size(), dbFigis.size(), 
            "Количество уникальных FIGI должно совпадать");
    }

    /**
     * Тест проверяет соответствие индикативов из API Т-Инвест с данными в БД
     * 
     * <p>Проверки:</p>
     * <ul>
     *   <li>Количество индикативов из API равно количеству индикативов в БД</li>
     *   <li>Все FIGI из API присутствуют в БД</li>
     *   <li>Все FIGI из БД присутствуют в API</li>
     * </ul>
     */
    @Test
    public void testIndicativesDataConsistency() {
        // Получаем индикативы из API Т-Инвест
        List<IndicativeDto> apiIndicatives = instrumentService.getIndicatives(
            null, 
            null, 
            null, 
            null
        );
        System.out.println("apiIndicatives: " + apiIndicatives);
        
        // Получаем все индикативы из БД
        List<com.example.InvestmentDataLoaderService.entity.IndicativeEntity> dbIndicatives = 
            indicativeRepository.findAll();
        
        // Проверяем количество
        assertEquals(apiIndicatives.size(), dbIndicatives.size(), 
            "Количество индикативов из API должно совпадать с количеством в БД");
        
        // Извлекаем FIGI из API
        Set<String> apiFigis = apiIndicatives.stream()
            .map(IndicativeDto::figi)
            .collect(Collectors.toSet());
        
        // Извлекаем FIGI из БД
        Set<String> dbFigis = dbIndicatives.stream()
            .map(com.example.InvestmentDataLoaderService.entity.IndicativeEntity::getFigi)
            .collect(Collectors.toSet());
        
        // Проверяем, что все FIGI из API присутствуют в БД
        assertTrue(apiFigis.containsAll(dbFigis), 
            "Все FIGI из БД должны присутствовать в API");
        
        // Проверяем, что все FIGI из БД присутствуют в API
        assertTrue(dbFigis.containsAll(apiFigis), 
            "Все FIGI из API должны присутствовать в БД");
        
        // Дополнительная проверка: количество уникальных FIGI
        assertEquals(apiFigis.size(), dbFigis.size(), 
            "Количество уникальных FIGI должно совпадать");
    }

    /**
     * Комплексный тест проверяет общую статистику по всем типам инструментов
     * 
     * <p>Проверяет соответствие общего количества инструментов и их распределения по типам</p>
     */
    @Test
    public void testOverallInstrumentsConsistency() {
        // Получаем статистику из сервиса
        var counts = instrumentService.getInstrumentCounts();
        
        // Получаем актуальные данные из API и БД
        List<ShareDto> apiShares = instrumentService.getShares(null, null, null, null, null);
        List<FutureDto> apiFutures = instrumentService.getFutures(null, null, null, null, null);
        List<IndicativeDto> apiIndicatives = instrumentService.getIndicatives(null, null, null, null);
        
        long dbSharesCount = shareRepository.count();
        long dbFuturesCount = futureRepository.count();
        long dbIndicativesCount = indicativeRepository.count();
        
        // Проверяем соответствие статистики
        assertEquals(apiShares.size(), counts.get("shares"), 
            "Статистика по акциям должна соответствовать данным из API");
        assertEquals(apiFutures.size(), counts.get("futures"), 
            "Статистика по фьючерсам должна соответствовать данным из API");
        assertEquals(apiIndicatives.size(), counts.get("indicatives"), 
            "Статистика по индикативам должна соответствовать данным из API");
        
        // Проверяем соответствие с БД
        assertEquals(dbSharesCount, counts.get("shares"), 
            "Статистика по акциям должна соответствовать данным из БД");
        assertEquals(dbFuturesCount, counts.get("futures"), 
            "Статистика по фьючерсам должна соответствовать данным из БД");
        assertEquals(dbIndicativesCount, counts.get("indicatives"), 
            "Статистика по индикативам должна соответствовать данным из БД");
        
        // Проверяем общее количество
        long expectedTotal = apiShares.size() + apiFutures.size() + apiIndicatives.size();
        assertEquals(expectedTotal, counts.get("total"), 
            "Общее количество инструментов должно соответствовать сумме всех типов");
    }
}
