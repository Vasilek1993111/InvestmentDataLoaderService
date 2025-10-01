package com.example.InvestmentDataLoaderService.unit.service;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;
import com.example.InvestmentDataLoaderService.fixtures.TestDataFactory;
import com.example.InvestmentDataLoaderService.repository.*;
import com.example.InvestmentDataLoaderService.service.MorningSessionService;

import io.qameta.allure.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Tag;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Epic("Morning Session Service")
@Feature("Morning Session Service")
@DisplayName("Morning Session Service Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
@Link(name = "Morning Session Service", url = "https://example.com/morning-session-service")
@Issue("MORNING-SESSION-001")
@TmsLink("MORNING-SESSION-TMS-001")
public class MorningSessionServiceTest {

    @Mock
    private ShareRepository shareRepository;
    @Mock
    private FutureRepository futureRepository;
    @Mock
    private IndicativeRepository indicativeRepository;
    @Mock
    private OpenPriceRepository openPriceRepository;
    @Mock
    private MinuteCandleRepository minuteCandleRepository;

    @InjectMocks
    private MorningSessionService morningSessionService;

    @BeforeEach
    @DisplayName("Настройка тестового окружения")
    @Description("Сброс всех моков перед каждым тестом")
    @Step("Настройка моков")
    void setUp() {
        reset(shareRepository, futureRepository, indicativeRepository, openPriceRepository, minuteCandleRepository);
    }

    // ========== ПОЗИТИВНЫЕ ТЕСТЫ ==========

    @Test
    @DisplayName("Успешная обработка цен утренней сессии для акций и фьючерсов")
    @Description("Тест проверяет основную функциональность обработки цен утренней сессии")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Успешные сценарии")
    @Tag("positive")
    @Tag("morning-session")
    @Tag("unit")
    @Step("Обработка цен утренней сессии")
    void processMorningSessionPrices_ShouldReturnSuccessResponse_WhenInstrumentsAvailable() {
        // Given
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        String taskId = "TEST_TASK_001";
        
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        FutureEntity future = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "Si-3.24", "RUB");
        MinuteCandleEntity candle1 = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));
        MinuteCandleEntity candle2 = TestDataFactory.createMinuteCandleEntity("TEST_FUTURE_001", testDate, BigDecimal.valueOf(200.0));

        // Настраиваем моки
        when(shareRepository.findAll()).thenReturn(Arrays.asList(share));
        when(futureRepository.findAll()).thenReturn(Arrays.asList(future));
        when(indicativeRepository.findAll()).thenReturn(TestDataFactory.createEmptyIndicativeEntityList());
        when(openPriceRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
            .thenReturn(Arrays.asList(candle1))
            .thenReturn(Arrays.asList(candle2));
        when(openPriceRepository.save(any(OpenPriceEntity.class)))
            .thenReturn(TestDataFactory.createOpenPriceEntity());

        // When
        SaveResponseDto result = morningSessionService.processMorningSessionPrices(testDate, taskId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("Цены открытия утренней сессии загружены"));
        assertEquals(2, result.getTotalRequested());
        assertEquals(2, result.getNewItemsSaved());
        assertEquals(0, result.getExistingItemsSkipped());
        assertEquals(0, result.getInvalidItemsFiltered());
        assertEquals(0, result.getMissingFromApi());
        assertNotNull(result.getSavedItems());
        assertEquals(2, result.getSavedItems().size());

        // Verify interactions
        verify(shareRepository, times(1)).findAll();
        verify(futureRepository, times(1)).findAll();
        verify(openPriceRepository, times(2)).save(any(OpenPriceEntity.class));
    }

    @Test
    @DisplayName("Успешная обработка цен утренней сессии с индикативными инструментами")
    @Description("Тест проверяет обработку индикативных инструментов")
    @Severity(SeverityLevel.NORMAL)
    @Story("Успешные сценарии")
    @Tag("positive")
    @Tag("morning-session")
    @Tag("indicatives")
    @Tag("unit")
    @Step("Обработка цен с индикативными инструментами")
    void processMorningSessionPrices_ShouldIncludeIndicatives_WhenIncludeIndicativesIsTrue() {
        // Given
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        String taskId = "TEST_TASK_002";
        
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        FutureEntity future = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "Si-3.24", "RUB");
        IndicativeEntity indicative = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_001", "RTSI", "RUB");
        MinuteCandleEntity candle1 = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));
        MinuteCandleEntity candle2 = TestDataFactory.createMinuteCandleEntity("TEST_FUTURE_001", testDate, BigDecimal.valueOf(200.0));
        MinuteCandleEntity candle3 = TestDataFactory.createMinuteCandleEntity("TEST_INDICATIVE_001", testDate, BigDecimal.valueOf(300.0));

        // Настраиваем моки
        when(shareRepository.findAll()).thenReturn(Arrays.asList(share));
        when(futureRepository.findAll()).thenReturn(Arrays.asList(future));
        when(indicativeRepository.findAll()).thenReturn(Arrays.asList(indicative));
        when(openPriceRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
            .thenReturn(Arrays.asList(candle1))
            .thenReturn(Arrays.asList(candle2))
            .thenReturn(Arrays.asList(candle3));
        when(openPriceRepository.save(any(OpenPriceEntity.class)))
            .thenReturn(TestDataFactory.createOpenPriceEntity());

        // When
        SaveResponseDto result = morningSessionService.processMorningSessionPrices(testDate, taskId, true);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(3, result.getTotalRequested());
        assertEquals(3, result.getNewItemsSaved());
        verify(indicativeRepository, times(1)).findAll();
        verify(openPriceRepository, times(3)).save(any(OpenPriceEntity.class));
    }

    @Test
    @DisplayName("Успешная обработка цен утренней сессии с пропуском существующих записей")
    @Description("Тест проверяет корректную обработку уже существующих записей")
    @Severity(SeverityLevel.NORMAL)
    @Story("Успешные сценарии")
    @Tag("positive")
    @Tag("morning-session")
    @Tag("duplicate")
    @Tag("unit")
    @Step("Обработка существующих записей")
    void processMorningSessionPrices_ShouldSkipExistingRecords_WhenRecordsAlreadyExist() {
        // Given
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        String taskId = "TEST_TASK_003";
        
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        FutureEntity future = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "Si-3.24", "RUB");
        MinuteCandleEntity candle = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));

        // Настраиваем моки - первая запись уже существует, вторая - новая
        when(shareRepository.findAll()).thenReturn(Arrays.asList(share));
        when(futureRepository.findAll()).thenReturn(Arrays.asList(future));
        when(indicativeRepository.findAll()).thenReturn(TestDataFactory.createEmptyIndicativeEntityList());
        when(openPriceRepository.existsByPriceDateAndFigi(any(LocalDate.class), eq("TEST_SHARE_001")))
            .thenReturn(true); // Первая запись уже существует
        when(openPriceRepository.existsByPriceDateAndFigi(any(LocalDate.class), eq("TEST_FUTURE_001")))
            .thenReturn(false); // Вторая запись новая
        when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
            .thenReturn(Arrays.asList(candle));
        when(openPriceRepository.save(any(OpenPriceEntity.class)))
            .thenReturn(TestDataFactory.createOpenPriceEntity());

        // When
        SaveResponseDto result = morningSessionService.processMorningSessionPrices(testDate, taskId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getTotalRequested());
        assertEquals(1, result.getNewItemsSaved());
        assertEquals(1, result.getExistingItemsSkipped());
        assertEquals(0, result.getInvalidItemsFiltered());
        assertEquals(0, result.getMissingFromApi());
    }

    @Test
    @DisplayName("Успешная обработка цен утренней сессии без индикативных инструментов")
    @Description("Тест проверяет обработку без индикативных инструментов")
    @Severity(SeverityLevel.NORMAL)
    @Story("Успешные сценарии")
    @Tag("positive")
    @Tag("morning-session")
    @Tag("exclude-indicatives")
    @Tag("unit")
    @Step("Обработка без индикативных инструментов")
    void processMorningSessionPrices_ShouldExcludeIndicatives_WhenIncludeIndicativesIsFalse() {
        // Given
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        String taskId = "TEST_TASK_004";
        
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        FutureEntity future = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "Si-3.24", "RUB");
        MinuteCandleEntity candle1 = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));
        MinuteCandleEntity candle2 = TestDataFactory.createMinuteCandleEntity("TEST_FUTURE_001", testDate, BigDecimal.valueOf(200.0));

        // Настраиваем моки
        when(shareRepository.findAll()).thenReturn(Arrays.asList(share));
        when(futureRepository.findAll()).thenReturn(Arrays.asList(future));
        when(openPriceRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
            .thenReturn(Arrays.asList(candle1))
            .thenReturn(Arrays.asList(candle2));
        when(openPriceRepository.save(any(OpenPriceEntity.class)))
            .thenReturn(TestDataFactory.createOpenPriceEntity());

        // When
        SaveResponseDto result = morningSessionService.processMorningSessionPrices(testDate, taskId, false);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getTotalRequested()); // Только акции и фьючерсы
        assertEquals(2, result.getNewItemsSaved());
        verify(indicativeRepository, never()).findAll(); // Индикативы не должны обрабатываться
        verify(openPriceRepository, times(2)).save(any(OpenPriceEntity.class));
    }

    @Test
    @DisplayName("Успешная обработка цен утренней сессии с пустыми свечами")
    @Description("Тест проверяет обработку инструментов без доступных свечей")
    @Severity(SeverityLevel.NORMAL)
    @Story("Успешные сценарии")
    @Tag("positive")
    @Tag("morning-session")
    @Tag("empty-candles")
    @Tag("unit")
    @Step("Обработка пустых свечей")
    void processMorningSessionPrices_ShouldHandleEmptyCandles_WhenNoCandlesAvailable() {
        // Given
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        String taskId = "TEST_TASK_005";
        
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        FutureEntity future = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "Si-3.24", "RUB");

        // Настраиваем моки - нет свечей
        when(shareRepository.findAll()).thenReturn(Arrays.asList(share));
        when(futureRepository.findAll()).thenReturn(Arrays.asList(future));
        when(indicativeRepository.findAll()).thenReturn(TestDataFactory.createEmptyIndicativeEntityList());
        when(openPriceRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
            .thenReturn(TestDataFactory.createEmptyMinuteCandleEntityList()); // Пустой список свечей

        // When
        SaveResponseDto result = morningSessionService.processMorningSessionPrices(testDate, taskId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved());
        assertEquals(0, result.getExistingItemsSkipped());
        assertEquals(0, result.getInvalidItemsFiltered());
        assertEquals(0, result.getMissingFromApi());
        verify(openPriceRepository, never()).save(any(OpenPriceEntity.class));
    }

    @Test
    @DisplayName("Успешная загрузка цен по конкретному FIGI")
    @Description("Тест проверяет загрузку цен по конкретному FIGI")
    @Severity(SeverityLevel.NORMAL)
    @Story("Успешные сценарии")
    @Tag("positive")
    @Tag("morning-session")
    @Tag("single-figi")
    @Tag("unit")
    @Step("Загрузка цен по FIGI")
    void fetchAndStorePriceByFigiForDate_ShouldReturnSuccessResponse_WhenValidFigiProvided() {
        // Given
        String figi = "TEST_SHARE_001";
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        MinuteCandleEntity candle = TestDataFactory.createMinuteCandleEntity(figi, testDate, BigDecimal.valueOf(100.0));

        // Настраиваем моки
        when(shareRepository.existsById(figi)).thenReturn(true);
        when(openPriceRepository.existsByPriceDateAndFigi(testDate, figi)).thenReturn(false);
        when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
            .thenReturn(Arrays.asList(candle));
        when(openPriceRepository.save(any(OpenPriceEntity.class)))
            .thenReturn(TestDataFactory.createOpenPriceEntity());

        // When
        SaveResponseDto result = morningSessionService.fetchAndStorePriceByFigiForDate(figi, testDate);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("Цена открытия для FIGI " + figi + " успешно сохранена"));
        assertEquals(1, result.getTotalRequested());
        assertEquals(1, result.getNewItemsSaved());
        assertEquals(0, result.getExistingItemsSkipped());
        verify(openPriceRepository, times(1)).save(any(OpenPriceEntity.class));
    }

    @Test
    @DisplayName("Успешный предпросмотр цен утренней сессии")
    @Description("Тест проверяет предпросмотр цен без сохранения в БД")
    @Severity(SeverityLevel.NORMAL)
    @Story("Успешные сценарии")
    @Tag("positive")
    @Tag("morning-session")
    @Tag("preview")
    @Tag("unit")
    @Step("Предпросмотр цен")
    void previewMorningSessionPricesForDate_ShouldReturnSuccessResponse_WhenInstrumentsAvailable() {
        // Given
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        FutureEntity future = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "Si-3.24", "RUB");
        MinuteCandleEntity candle1 = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));
        MinuteCandleEntity candle2 = TestDataFactory.createMinuteCandleEntity("TEST_FUTURE_001", testDate, BigDecimal.valueOf(200.0));

        // Настраиваем моки
        when(shareRepository.findAll()).thenReturn(Arrays.asList(share));
        when(futureRepository.findAll()).thenReturn(Arrays.asList(future));
        when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
            .thenReturn(Arrays.asList(candle1))
            .thenReturn(Arrays.asList(candle2));

        // When
        SaveResponseDto result = morningSessionService.previewMorningSessionPricesForDate(testDate);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("Предпросмотр цен открытия без сохранения"));
        assertEquals(2, result.getTotalRequested());
        assertEquals(2, result.getNewItemsSaved());
        assertNotNull(result.getSavedItems());
        assertEquals(2, result.getSavedItems().size());
        verify(openPriceRepository, never()).save(any(OpenPriceEntity.class));
    }

    // ========== НЕГАТИВНЫЕ ТЕСТЫ ==========

    @Test
    @DisplayName("Обработка ошибки базы данных при сохранении")
    @Description("Тест проверяет обработку исключений при работе с БД")
    @Severity(SeverityLevel.NORMAL)
    @Story("Негативные сценарии")
    @Tag("negative")
    @Tag("morning-session")
    @Tag("database-error")
    @Tag("unit")
    @Step("Обработка ошибки БД")
    void processMorningSessionPrices_ShouldHandleDatabaseError_WhenSaveFails() {
        // Given
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        String taskId = "TEST_TASK_006";
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        MinuteCandleEntity candle = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));

        // Настраиваем моки - БД выбрасывает исключение
        when(shareRepository.findAll()).thenReturn(Arrays.asList(share));
        when(futureRepository.findAll()).thenReturn(TestDataFactory.createEmptyFutureEntityList());
        when(indicativeRepository.findAll()).thenReturn(TestDataFactory.createEmptyIndicativeEntityList());
        when(openPriceRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
            .thenReturn(Arrays.asList(candle));
        when(openPriceRepository.save(any(OpenPriceEntity.class)))
            .thenThrow(new DataIntegrityViolationException("Ошибка целостности данных"));

        // When
        SaveResponseDto result = morningSessionService.processMorningSessionPrices(testDate, taskId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess()); // Сервис обрабатывает исключения и продолжает работу
        assertTrue(result.getMessage().contains("Цены открытия утренней сессии загружены"));
        assertEquals(1, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved()); // Ничего не сохранилось из-за ошибки БД
    }

    @Test
    @DisplayName("Обработка пустой базы данных")
    @Description("Тест проверяет обработку пустой базы данных")
    @Severity(SeverityLevel.NORMAL)
    @Story("Негативные сценарии")
    @Tag("negative")
    @Tag("morning-session")
    @Tag("empty-database")
    @Tag("unit")
    @Step("Обработка пустой БД")
    void processMorningSessionPrices_ShouldHandleEmptyDatabase_WhenNoInstrumentsAvailable() {
        // Given
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        String taskId = "TEST_TASK_007";

        // Настраиваем моки для пустой БД
        when(shareRepository.findAll()).thenReturn(TestDataFactory.createEmptyShareEntityList());
        when(futureRepository.findAll()).thenReturn(TestDataFactory.createEmptyFutureEntityList());
        when(indicativeRepository.findAll()).thenReturn(TestDataFactory.createEmptyIndicativeEntityList());

        // When
        SaveResponseDto result = morningSessionService.processMorningSessionPrices(testDate, taskId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved());
        assertEquals(0, result.getExistingItemsSkipped());
        verify(openPriceRepository, never()).save(any(OpenPriceEntity.class));
    }

    @Test
    @DisplayName("Обработка несуществующего FIGI")
    @Description("Тест проверяет обработку несуществующего FIGI")
    @Severity(SeverityLevel.NORMAL)
    @Story("Негативные сценарии")
    @Tag("negative")
    @Tag("morning-session")
    @Tag("unknown-figi")
    @Tag("unit")
    @Step("Обработка несуществующего FIGI")
    void fetchAndStorePriceByFigiForDate_ShouldHandleUnknownFigi_WhenFigiNotFound() {
        // Given
        String figi = "UNKNOWN_FIGI";
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        // Настраиваем моки - FIGI не найден
        when(shareRepository.existsById(figi)).thenReturn(false);
        when(futureRepository.existsById(figi)).thenReturn(false);
        when(indicativeRepository.existsById(figi)).thenReturn(false);

        // When
        SaveResponseDto result = morningSessionService.fetchAndStorePriceByFigiForDate(figi, testDate);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Инструмент с FIGI " + figi + " не найден в базе данных"));
        assertEquals(0, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved());
    }

    @Test
    @DisplayName("Обработка уже существующей записи для FIGI")
    @Description("Тест проверяет обработку уже существующей записи")
    @Severity(SeverityLevel.NORMAL)
    @Story("Негативные сценарии")
    @Tag("negative")
    @Tag("morning-session")
    @Tag("existing-record")
    @Tag("unit")
    @Step("Обработка существующей записи")
    void fetchAndStorePriceByFigiForDate_ShouldHandleExistingRecord_WhenRecordAlreadyExists() {
        // Given
        String figi = "TEST_SHARE_001";
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        // Настраиваем моки - запись уже существует
        when(shareRepository.existsById(figi)).thenReturn(true);
        when(openPriceRepository.existsByPriceDateAndFigi(testDate, figi)).thenReturn(true);

        // When
        SaveResponseDto result = morningSessionService.fetchAndStorePriceByFigiForDate(figi, testDate);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("Цена открытия для FIGI " + figi + " за дату " + testDate + " уже существует"));
        assertEquals(1, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved());
        assertEquals(1, result.getExistingItemsSkipped());
        verify(openPriceRepository, never()).save(any(OpenPriceEntity.class));
    }

    @Test
    @DisplayName("Обработка неожиданной ошибки при сохранении")
    @Description("Тест проверяет обработку неожиданных исключений при сохранении")
    @Severity(SeverityLevel.NORMAL)
    @Story("Негативные сценарии")
    @Tag("negative")
    @Tag("morning-session")
    @Tag("unexpected-error")
    @Tag("unit")
    @Step("Обработка неожиданной ошибки")
    void processMorningSessionPrices_ShouldHandleUnexpectedError_WhenSaveThrowsUnexpectedException() {
        // Given
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        String taskId = "TEST_TASK_008";
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        MinuteCandleEntity candle = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));

        // Настраиваем моки - неожиданное исключение
        when(shareRepository.findAll()).thenReturn(Arrays.asList(share));
        when(futureRepository.findAll()).thenReturn(TestDataFactory.createEmptyFutureEntityList());
        when(indicativeRepository.findAll()).thenReturn(TestDataFactory.createEmptyIndicativeEntityList());
        when(openPriceRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
            .thenReturn(Arrays.asList(candle));
        when(openPriceRepository.save(any(OpenPriceEntity.class)))
            .thenThrow(new RuntimeException("Неожиданная ошибка"));

        // When
        SaveResponseDto result = morningSessionService.processMorningSessionPrices(testDate, taskId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess()); // Сервис обрабатывает исключения и продолжает работу
        assertTrue(result.getMessage().contains("Цены открытия утренней сессии загружены"));
        assertEquals(1, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved()); // Ничего не сохранилось из-за ошибки
    }

    @Test
    @DisplayName("Обработка ошибки при поиске свечей")
    @Description("Тест проверяет обработку ошибок при поиске свечей")
    @Severity(SeverityLevel.NORMAL)
    @Story("Негативные сценарии")
    @Tag("negative")
    @Tag("morning-session")
    @Tag("candle-search-error")
    @Tag("unit")
    @Step("Обработка ошибки поиска свечей")
    void processMorningSessionPrices_ShouldHandleCandleSearchError_WhenCandleRepositoryThrowsException() {
        // Given
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        String taskId = "TEST_TASK_009";
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");

        // Настраиваем моки - ошибка при поиске свечей
        when(shareRepository.findAll()).thenReturn(Arrays.asList(share));
        when(futureRepository.findAll()).thenReturn(TestDataFactory.createEmptyFutureEntityList());
        when(indicativeRepository.findAll()).thenReturn(TestDataFactory.createEmptyIndicativeEntityList());
        when(openPriceRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
            .thenThrow(new RuntimeException("Ошибка поиска свечей"));

        // When
        SaveResponseDto result = morningSessionService.processMorningSessionPrices(testDate, taskId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess()); // Ошибка поиска свечей не должна прерывать весь процесс
        assertEquals(0, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved());
        verify(openPriceRepository, never()).save(any(OpenPriceEntity.class));
    }

    // ========== ГРАНИЧНЫЕ СЛУЧАИ ==========

    @Test
    @DisplayName("Обработка очень большого количества инструментов")
    @Description("Тест проверяет обработку большого количества инструментов")
    @Severity(SeverityLevel.NORMAL)
    @Story("Граничные случаи")
    @Tag("boundary")
    @Tag("morning-session")
    @Tag("large-dataset")
    @Tag("unit")
    @Step("Обработка большого количества инструментов")
    void processMorningSessionPrices_ShouldHandleLargeNumberOfInstruments_WhenManyInstrumentsProvided() {
        // Given
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        String taskId = "TEST_TASK_010";
        
        List<ShareEntity> manyShares = TestDataFactory.createShareEntityList(100);
        MinuteCandleEntity candle = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));

        // Настраиваем моки
        when(shareRepository.findAll()).thenReturn(manyShares);
        when(futureRepository.findAll()).thenReturn(TestDataFactory.createEmptyFutureEntityList());
        when(indicativeRepository.findAll()).thenReturn(TestDataFactory.createEmptyIndicativeEntityList());
        when(openPriceRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
            .thenReturn(Arrays.asList(candle));
        when(openPriceRepository.save(any(OpenPriceEntity.class)))
            .thenReturn(TestDataFactory.createOpenPriceEntity());

        // When
        SaveResponseDto result = morningSessionService.processMorningSessionPrices(testDate, taskId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(100, result.getTotalRequested());
        assertEquals(100, result.getNewItemsSaved());
        verify(openPriceRepository, times(100)).save(any(OpenPriceEntity.class));
    }

    @Test
    @DisplayName("Обработка индикативных инструментов с пустыми FIGI")
    @Description("Тест проверяет обработку индикативных инструментов с пустыми FIGI")
    @Severity(SeverityLevel.NORMAL)
    @Story("Граничные случаи")
    @Tag("boundary")
    @Tag("morning-session")
    @Tag("empty-figi")
    @Tag("unit")
    @Step("Обработка пустых FIGI")
    void processMorningSessionPrices_ShouldSkipIndicativesWithEmptyFigi_WhenIndicativesHaveEmptyFigi() {
        // Given
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        String taskId = "TEST_TASK_011";
        
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        IndicativeEntity indicative1 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_001", "RTSI", "RUB");
        IndicativeEntity indicative2 = TestDataFactory.createIndicativeEntity("", "RTSI2", "RUB"); // Пустой FIGI
        IndicativeEntity indicative3 = TestDataFactory.createIndicativeEntity(null, "RTSI3", "RUB"); // null FIGI
        MinuteCandleEntity candle1 = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));
        MinuteCandleEntity candle2 = TestDataFactory.createMinuteCandleEntity("TEST_INDICATIVE_001", testDate, BigDecimal.valueOf(100.0));

        // Настраиваем моки
        when(shareRepository.findAll()).thenReturn(Arrays.asList(share));
        when(futureRepository.findAll()).thenReturn(TestDataFactory.createEmptyFutureEntityList());
        when(indicativeRepository.findAll()).thenReturn(Arrays.asList(indicative1, indicative2, indicative3));
        when(openPriceRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(minuteCandleRepository.findByFigiAndTimeBetween(eq("TEST_SHARE_001"), any(Instant.class), any(Instant.class)))
            .thenReturn(Arrays.asList(candle1));
        when(minuteCandleRepository.findByFigiAndTimeBetween(eq("TEST_INDICATIVE_001"), any(Instant.class), any(Instant.class)))
            .thenReturn(Arrays.asList(candle2));
        when(openPriceRepository.save(any(OpenPriceEntity.class)))
            .thenReturn(TestDataFactory.createOpenPriceEntity());

        // When
        SaveResponseDto result = morningSessionService.processMorningSessionPrices(testDate, taskId, true);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getTotalRequested()); // Акция + один индикатив с валидным FIGI
        assertEquals(2, result.getNewItemsSaved());
        verify(openPriceRepository, times(2)).save(any(OpenPriceEntity.class));
    }

    @Test
    @DisplayName("Обработка смешанных результатов с разными типами инструментов")
    @Description("Тест проверяет обработку смешанных результатов с разными типами инструментов")
    @Severity(SeverityLevel.NORMAL)
    @Story("Граничные случаи")
    @Tag("boundary")
    @Tag("morning-session")
    @Tag("mixed-results")
    @Tag("unit")
    @Step("Обработка смешанных результатов")
    void processMorningSessionPrices_ShouldHandleMixedResults_WhenSomeInstrumentsHaveCandlesAndSomeDoNot() {
        // Given
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        String taskId = "TEST_TASK_012";
        
        ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        ShareEntity share2 = TestDataFactory.createShareEntity("TEST_SHARE_002", "GAZP", "RUB", "MOEX");
        FutureEntity future1 = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "Si-3.24", "RUB");
        FutureEntity future2 = TestDataFactory.createFutureEntity("TEST_FUTURE_002", "RTS-3.24", "RUB");
        
        MinuteCandleEntity candle1 = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));
        MinuteCandleEntity candle2 = TestDataFactory.createMinuteCandleEntity("TEST_FUTURE_001", testDate, BigDecimal.valueOf(200.0));
        // Нет свечей для share2 и future2

        // Настраиваем моки
        when(shareRepository.findAll()).thenReturn(Arrays.asList(share1, share2));
        when(futureRepository.findAll()).thenReturn(Arrays.asList(future1, future2));
        when(indicativeRepository.findAll()).thenReturn(TestDataFactory.createEmptyIndicativeEntityList());
        when(openPriceRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(minuteCandleRepository.findByFigiAndTimeBetween(eq("TEST_SHARE_001"), any(Instant.class), any(Instant.class)))
            .thenReturn(Arrays.asList(candle1));
        when(minuteCandleRepository.findByFigiAndTimeBetween(eq("TEST_SHARE_002"), any(Instant.class), any(Instant.class)))
            .thenReturn(TestDataFactory.createEmptyMinuteCandleEntityList());
        when(minuteCandleRepository.findByFigiAndTimeBetween(eq("TEST_FUTURE_001"), any(Instant.class), any(Instant.class)))
            .thenReturn(Arrays.asList(candle2));
        when(minuteCandleRepository.findByFigiAndTimeBetween(eq("TEST_FUTURE_002"), any(Instant.class), any(Instant.class)))
            .thenReturn(TestDataFactory.createEmptyMinuteCandleEntityList());
        when(openPriceRepository.save(any(OpenPriceEntity.class)))
            .thenReturn(TestDataFactory.createOpenPriceEntity());

        // When
        SaveResponseDto result = morningSessionService.processMorningSessionPrices(testDate, taskId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getTotalRequested()); // Только те, для которых есть свечи
        assertEquals(2, result.getNewItemsSaved());
        verify(openPriceRepository, times(2)).save(any(OpenPriceEntity.class));
    }
}