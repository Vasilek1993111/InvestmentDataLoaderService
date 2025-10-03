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
@Tag("unit")
@Tag("service")
@Tag("morning-session")
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
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("positive")
    @Tag("morning-session")
    @Tag("success")
    @Tag("shares-futures")
    void processMorningSessionPrices_ShouldReturnSuccessResponse_WhenInstrumentsAvailable() {
        
        // Шаг 1: Подготовка тестовых данных
        LocalDate testDate = Allure.step("Подготовка тестовых данных", () -> {
            return LocalDate.of(2024, 1, 15);
        });
        String taskId = "TEST_TASK_001";
        
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        FutureEntity future = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "Si-3.24", "RUB");
        MinuteCandleEntity candle1 = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));
        MinuteCandleEntity candle2 = TestDataFactory.createMinuteCandleEntity("TEST_FUTURE_001", testDate, BigDecimal.valueOf(200.0));

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
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
        });

        // Шаг 3: Выполнение обработки цен утренней сессии
        SaveResponseDto result = Allure.step("Выполнение обработки цен утренней сессии", () -> {
            return morningSessionService.processMorningSessionPrices(testDate, taskId);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
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
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(shareRepository, times(1)).findAll();
            verify(futureRepository, times(1)).findAll();
            verify(openPriceRepository, times(2)).save(any(OpenPriceEntity.class));
        });
    }

    @Test
    @DisplayName("Успешная обработка цен утренней сессии с индикативными инструментами")
    @Description("Тест проверяет обработку индикативных инструментов")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("positive")
    @Tag("morning-session")
    @Tag("indicatives")
    @Tag("multi-asset")
    void processMorningSessionPrices_ShouldIncludeIndicatives_WhenIncludeIndicativesIsTrue() {
        
        // Шаг 1: Подготовка тестовых данных
        LocalDate testDate = Allure.step("Подготовка тестовых данных", () -> {
            return LocalDate.of(2024, 1, 15);
        });
        String taskId = "TEST_TASK_002";
        
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        FutureEntity future = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "Si-3.24", "RUB");
        IndicativeEntity indicative = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_001", "RTSI", "RUB");
        MinuteCandleEntity candle1 = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));
        MinuteCandleEntity candle2 = TestDataFactory.createMinuteCandleEntity("TEST_FUTURE_001", testDate, BigDecimal.valueOf(200.0));
        MinuteCandleEntity candle3 = TestDataFactory.createMinuteCandleEntity("TEST_INDICATIVE_001", testDate, BigDecimal.valueOf(300.0));

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
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
        });

        // Шаг 3: Выполнение обработки с индикативными инструментами
        SaveResponseDto result = Allure.step("Выполнение обработки с индикативными инструментами", () -> {
            return morningSessionService.processMorningSessionPrices(testDate, taskId, true);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(3, result.getTotalRequested());
            assertEquals(3, result.getNewItemsSaved());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(indicativeRepository, times(1)).findAll();
            verify(openPriceRepository, times(3)).save(any(OpenPriceEntity.class));
        });
    }

    @Test
    @DisplayName("Успешная обработка цен утренней сессии с пропуском существующих записей")
    @Description("Тест проверяет корректную обработку уже существующих записей")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("positive")
    @Tag("morning-session")
    @Tag("duplicate")
    @Tag("existing-records")
    void processMorningSessionPrices_ShouldSkipExistingRecords_WhenRecordsAlreadyExist() {
        
        // Шаг 1: Подготовка тестовых данных
        LocalDate testDate = Allure.step("Подготовка тестовых данных", () -> {
            return LocalDate.of(2024, 1, 15);
        });
        String taskId = "TEST_TASK_003";
        
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        FutureEntity future = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "Si-3.24", "RUB");
        MinuteCandleEntity candle = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));

        // Шаг 2: Настройка моков - первая запись уже существует, вторая - новая
        Allure.step("Настройка моков - первая запись уже существует, вторая - новая", () -> {
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
        });

        // Шаг 3: Выполнение обработки с существующими записями
        SaveResponseDto result = Allure.step("Выполнение обработки с существующими записями", () -> {
            return morningSessionService.processMorningSessionPrices(testDate, taskId);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getTotalRequested());
            assertEquals(1, result.getNewItemsSaved());
            assertEquals(1, result.getExistingItemsSkipped());
            assertEquals(0, result.getInvalidItemsFiltered());
            assertEquals(0, result.getMissingFromApi());
        });
    }

    @Test
    @DisplayName("Успешная обработка цен утренней сессии без индикативных инструментов")
    @Description("Тест проверяет обработку без индикативных инструментов")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("positive")
    @Tag("morning-session")
    @Tag("exclude-indicatives")
    @Tag("filtering")
    void processMorningSessionPrices_ShouldExcludeIndicatives_WhenIncludeIndicativesIsFalse() {
        
        // Шаг 1: Подготовка тестовых данных
        LocalDate testDate = Allure.step("Подготовка тестовых данных", () -> {
            return LocalDate.of(2024, 1, 15);
        });
        String taskId = "TEST_TASK_004";
        
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        FutureEntity future = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "Si-3.24", "RUB");
        MinuteCandleEntity candle1 = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));
        MinuteCandleEntity candle2 = TestDataFactory.createMinuteCandleEntity("TEST_FUTURE_001", testDate, BigDecimal.valueOf(200.0));

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(shareRepository.findAll()).thenReturn(Arrays.asList(share));
            when(futureRepository.findAll()).thenReturn(Arrays.asList(future));
            when(openPriceRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
                .thenReturn(false);
            when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
                .thenReturn(Arrays.asList(candle1))
                .thenReturn(Arrays.asList(candle2));
            when(openPriceRepository.save(any(OpenPriceEntity.class)))
                .thenReturn(TestDataFactory.createOpenPriceEntity());
        });

        // Шаг 3: Выполнение обработки без индикативных инструментов
        SaveResponseDto result = Allure.step("Выполнение обработки без индикативных инструментов", () -> {
            return morningSessionService.processMorningSessionPrices(testDate, taskId, false);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getTotalRequested()); // Только акции и фьючерсы
            assertEquals(2, result.getNewItemsSaved());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(indicativeRepository, never()).findAll(); // Индикативы не должны обрабатываться
            verify(openPriceRepository, times(2)).save(any(OpenPriceEntity.class));
        });
    }

    @Test
    @DisplayName("Успешная обработка цен утренней сессии с пустыми свечами")
    @Description("Тест проверяет обработку инструментов без доступных свечей")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("positive")
    @Tag("morning-session")
    @Tag("empty-candles")
    @Tag("no-data")
    void processMorningSessionPrices_ShouldHandleEmptyCandles_WhenNoCandlesAvailable() {
        
        // Шаг 1: Подготовка тестовых данных
        LocalDate testDate = Allure.step("Подготовка тестовых данных", () -> {
            return LocalDate.of(2024, 1, 15);
        });
        String taskId = "TEST_TASK_005";
        
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        FutureEntity future = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "Si-3.24", "RUB");

        // Шаг 2: Настройка моков - нет свечей
        Allure.step("Настройка моков - нет свечей", () -> {
            when(shareRepository.findAll()).thenReturn(Arrays.asList(share));
            when(futureRepository.findAll()).thenReturn(Arrays.asList(future));
            when(indicativeRepository.findAll()).thenReturn(TestDataFactory.createEmptyIndicativeEntityList());
            when(openPriceRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
                .thenReturn(false);
            when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
                .thenReturn(TestDataFactory.createEmptyMinuteCandleEntityList()); // Пустой список свечей
        });

        // Шаг 3: Выполнение обработки с пустыми свечами
        SaveResponseDto result = Allure.step("Выполнение обработки с пустыми свечами", () -> {
            return morningSessionService.processMorningSessionPrices(testDate, taskId);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(0, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved());
            assertEquals(0, result.getExistingItemsSkipped());
            assertEquals(0, result.getInvalidItemsFiltered());
            assertEquals(0, result.getMissingFromApi());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(openPriceRepository, never()).save(any(OpenPriceEntity.class));
        });
    }

    @Test
    @DisplayName("Успешная загрузка цен по конкретному FIGI")
    @Description("Тест проверяет загрузку цен по конкретному FIGI")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("positive")
    @Tag("morning-session")
    @Tag("single-figi")
    @Tag("individual-processing")
    void fetchAndStorePriceByFigiForDate_ShouldReturnSuccessResponse_WhenValidFigiProvided() {
        
        // Шаг 1: Подготовка тестовых данных
        String figi = Allure.step("Подготовка тестовых данных", () -> {
            return "TEST_SHARE_001";
        });
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        MinuteCandleEntity candle = TestDataFactory.createMinuteCandleEntity(figi, testDate, BigDecimal.valueOf(100.0));

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(shareRepository.existsById(figi)).thenReturn(true);
            when(openPriceRepository.existsByPriceDateAndFigi(testDate, figi)).thenReturn(false);
            when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
                .thenReturn(Arrays.asList(candle));
            when(openPriceRepository.save(any(OpenPriceEntity.class)))
                .thenReturn(TestDataFactory.createOpenPriceEntity());
        });

        // Шаг 3: Выполнение загрузки цен по FIGI
        SaveResponseDto result = Allure.step("Выполнение загрузки цен по FIGI", () -> {
            return morningSessionService.fetchAndStorePriceByFigiForDate(figi, testDate);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getMessage().contains("Цена открытия для FIGI " + figi + " успешно сохранена"));
            assertEquals(1, result.getTotalRequested());
            assertEquals(1, result.getNewItemsSaved());
            assertEquals(0, result.getExistingItemsSkipped());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(openPriceRepository, times(1)).save(any(OpenPriceEntity.class));
        });
    }

    @Test
    @DisplayName("Успешный предпросмотр цен утренней сессии")
    @Description("Тест проверяет предпросмотр цен без сохранения в БД")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("positive")
    @Tag("morning-session")
    @Tag("preview")
    @Tag("read-only")
    void previewMorningSessionPricesForDate_ShouldReturnSuccessResponse_WhenInstrumentsAvailable() {
        
        // Шаг 1: Подготовка тестовых данных
        LocalDate testDate = Allure.step("Подготовка тестовых данных", () -> {
            return LocalDate.of(2024, 1, 15);
        });
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        FutureEntity future = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "Si-3.24", "RUB");
        MinuteCandleEntity candle1 = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));
        MinuteCandleEntity candle2 = TestDataFactory.createMinuteCandleEntity("TEST_FUTURE_001", testDate, BigDecimal.valueOf(200.0));

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(shareRepository.findAll()).thenReturn(Arrays.asList(share));
            when(futureRepository.findAll()).thenReturn(Arrays.asList(future));
            when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
                .thenReturn(Arrays.asList(candle1))
                .thenReturn(Arrays.asList(candle2));
        });

        // Шаг 3: Выполнение предпросмотра цен
        SaveResponseDto result = Allure.step("Выполнение предпросмотра цен", () -> {
            return morningSessionService.previewMorningSessionPricesForDate(testDate);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getMessage().contains("Предпросмотр цен открытия без сохранения"));
            assertEquals(2, result.getTotalRequested());
            assertEquals(2, result.getNewItemsSaved());
            assertNotNull(result.getSavedItems());
            assertEquals(2, result.getSavedItems().size());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(openPriceRepository, never()).save(any(OpenPriceEntity.class));
        });
    }

    // ========== НЕГАТИВНЫЕ ТЕСТЫ ==========

    @Test
    @DisplayName("Обработка ошибки базы данных при сохранении")
    @Description("Тест проверяет обработку исключений при работе с БД")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("morning-session")
    @Tag("database-error")
    @Tag("error-handling")
    void processMorningSessionPrices_ShouldHandleDatabaseError_WhenSaveFails() {
        
        // Шаг 1: Подготовка тестовых данных
        LocalDate testDate = Allure.step("Подготовка тестовых данных", () -> {
            return LocalDate.of(2024, 1, 15);
        });
        String taskId = "TEST_TASK_006";
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        MinuteCandleEntity candle = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));

        // Шаг 2: Настройка моков - БД выбрасывает исключение
        Allure.step("Настройка моков - БД выбрасывает исключение", () -> {
            when(shareRepository.findAll()).thenReturn(Arrays.asList(share));
            when(futureRepository.findAll()).thenReturn(TestDataFactory.createEmptyFutureEntityList());
            when(indicativeRepository.findAll()).thenReturn(TestDataFactory.createEmptyIndicativeEntityList());
            when(openPriceRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
                .thenReturn(false);
            when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
                .thenReturn(Arrays.asList(candle));
            when(openPriceRepository.save(any(OpenPriceEntity.class)))
                .thenThrow(new DataIntegrityViolationException("Ошибка целостности данных"));
        });

        // Шаг 3: Выполнение обработки с ошибкой БД
        SaveResponseDto result = Allure.step("Выполнение обработки с ошибкой БД", () -> {
            return morningSessionService.processMorningSessionPrices(testDate, taskId);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess()); // Сервис обрабатывает исключения и продолжает работу
            assertTrue(result.getMessage().contains("Цены открытия утренней сессии загружены"));
            assertEquals(1, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved()); // Ничего не сохранилось из-за ошибки БД
        });
    }

    @Test
    @DisplayName("Обработка пустой базы данных")
    @Description("Тест проверяет обработку пустой базы данных")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("morning-session")
    @Tag("empty-database")
    @Tag("no-instruments")
    void processMorningSessionPrices_ShouldHandleEmptyDatabase_WhenNoInstrumentsAvailable() {
        
        // Шаг 1: Подготовка тестовых данных
        LocalDate testDate = Allure.step("Подготовка тестовых данных", () -> {
            return LocalDate.of(2024, 1, 15);
        });
        String taskId = "TEST_TASK_007";

        // Шаг 2: Настройка моков для пустой БД
        Allure.step("Настройка моков для пустой БД", () -> {
            when(shareRepository.findAll()).thenReturn(TestDataFactory.createEmptyShareEntityList());
            when(futureRepository.findAll()).thenReturn(TestDataFactory.createEmptyFutureEntityList());
            when(indicativeRepository.findAll()).thenReturn(TestDataFactory.createEmptyIndicativeEntityList());
        });

        // Шаг 3: Выполнение обработки с пустой БД
        SaveResponseDto result = Allure.step("Выполнение обработки с пустой БД", () -> {
            return morningSessionService.processMorningSessionPrices(testDate, taskId);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(0, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved());
            assertEquals(0, result.getExistingItemsSkipped());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(openPriceRepository, never()).save(any(OpenPriceEntity.class));
        });
    }

    @Test
    @DisplayName("Обработка несуществующего FIGI")
    @Description("Тест проверяет обработку несуществующего FIGI")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("morning-session")
    @Tag("unknown-figi")
    @Tag("validation")
    void fetchAndStorePriceByFigiForDate_ShouldHandleUnknownFigi_WhenFigiNotFound() {
        
        // Шаг 1: Подготовка тестовых данных
        String figi = Allure.step("Подготовка тестовых данных", () -> {
            return "UNKNOWN_FIGI";
        });
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        // Шаг 2: Настройка моков - FIGI не найден
        Allure.step("Настройка моков - FIGI не найден", () -> {
            when(shareRepository.existsById(figi)).thenReturn(false);
            when(futureRepository.existsById(figi)).thenReturn(false);
            when(indicativeRepository.existsById(figi)).thenReturn(false);
        });

        // Шаг 3: Выполнение обработки несуществующего FIGI
        SaveResponseDto result = Allure.step("Выполнение обработки несуществующего FIGI", () -> {
            return morningSessionService.fetchAndStorePriceByFigiForDate(figi, testDate);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMessage().contains("Инструмент с FIGI " + figi + " не найден в базе данных"));
            assertEquals(0, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved());
        });
    }

    @Test
    @DisplayName("Обработка уже существующей записи для FIGI")
    @Description("Тест проверяет обработку уже существующей записи")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("morning-session")
    @Tag("existing-record")
    @Tag("duplicate-handling")
    void fetchAndStorePriceByFigiForDate_ShouldHandleExistingRecord_WhenRecordAlreadyExists() {
        
        // Шаг 1: Подготовка тестовых данных
        String figi = Allure.step("Подготовка тестовых данных", () -> {
            return "TEST_SHARE_001";
        });
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        // Шаг 2: Настройка моков - запись уже существует
        Allure.step("Настройка моков - запись уже существует", () -> {
            when(shareRepository.existsById(figi)).thenReturn(true);
            when(openPriceRepository.existsByPriceDateAndFigi(testDate, figi)).thenReturn(true);
        });

        // Шаг 3: Выполнение обработки существующей записи
        SaveResponseDto result = Allure.step("Выполнение обработки существующей записи", () -> {
            return morningSessionService.fetchAndStorePriceByFigiForDate(figi, testDate);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getMessage().contains("Цена открытия для FIGI " + figi + " за дату " + testDate + " уже существует"));
            assertEquals(1, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved());
            assertEquals(1, result.getExistingItemsSkipped());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(openPriceRepository, never()).save(any(OpenPriceEntity.class));
        });
    }

    @Test
    @DisplayName("Обработка неожиданной ошибки при сохранении")
    @Description("Тест проверяет обработку неожиданных исключений при сохранении")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("morning-session")
    @Tag("unexpected-error")
    @Tag("exception-handling")
    void processMorningSessionPrices_ShouldHandleUnexpectedError_WhenSaveThrowsUnexpectedException() {
        
        // Шаг 1: Подготовка тестовых данных
        LocalDate testDate = Allure.step("Подготовка тестовых данных", () -> {
            return LocalDate.of(2024, 1, 15);
        });
        String taskId = "TEST_TASK_008";
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        MinuteCandleEntity candle = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));

        // Шаг 2: Настройка моков - неожиданное исключение
        Allure.step("Настройка моков - неожиданное исключение", () -> {
            when(shareRepository.findAll()).thenReturn(Arrays.asList(share));
            when(futureRepository.findAll()).thenReturn(TestDataFactory.createEmptyFutureEntityList());
            when(indicativeRepository.findAll()).thenReturn(TestDataFactory.createEmptyIndicativeEntityList());
            when(openPriceRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
                .thenReturn(false);
            when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
                .thenReturn(Arrays.asList(candle));
            when(openPriceRepository.save(any(OpenPriceEntity.class)))
                .thenThrow(new RuntimeException("Неожиданная ошибка"));
        });

        // Шаг 3: Выполнение обработки с неожиданной ошибкой
        SaveResponseDto result = Allure.step("Выполнение обработки с неожиданной ошибкой", () -> {
            return morningSessionService.processMorningSessionPrices(testDate, taskId);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess()); // Сервис обрабатывает исключения и продолжает работу
            assertTrue(result.getMessage().contains("Цены открытия утренней сессии загружены"));
            assertEquals(1, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved()); // Ничего не сохранилось из-за ошибки
        });
    }

    @Test
    @DisplayName("Обработка ошибки при поиске свечей")
    @Description("Тест проверяет обработку ошибок при поиске свечей")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("morning-session")
    @Tag("candle-search-error")
    @Tag("repository-error")
    void processMorningSessionPrices_ShouldHandleCandleSearchError_WhenCandleRepositoryThrowsException() {
        
        // Шаг 1: Подготовка тестовых данных
        LocalDate testDate = Allure.step("Подготовка тестовых данных", () -> {
            return LocalDate.of(2024, 1, 15);
        });
        String taskId = "TEST_TASK_009";
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");

        // Шаг 2: Настройка моков - ошибка при поиске свечей
        Allure.step("Настройка моков - ошибка при поиске свечей", () -> {
            when(shareRepository.findAll()).thenReturn(Arrays.asList(share));
            when(futureRepository.findAll()).thenReturn(TestDataFactory.createEmptyFutureEntityList());
            when(indicativeRepository.findAll()).thenReturn(TestDataFactory.createEmptyIndicativeEntityList());
            when(openPriceRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
                .thenReturn(false);
            when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
                .thenThrow(new RuntimeException("Ошибка поиска свечей"));
        });

        // Шаг 3: Выполнение обработки с ошибкой поиска свечей
        SaveResponseDto result = Allure.step("Выполнение обработки с ошибкой поиска свечей", () -> {
            return morningSessionService.processMorningSessionPrices(testDate, taskId);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess()); // Ошибка поиска свечей не должна прерывать весь процесс
            assertEquals(0, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(openPriceRepository, never()).save(any(OpenPriceEntity.class));
        });
    }

    // ========== ГРАНИЧНЫЕ СЛУЧАИ ==========

    @Test
    @DisplayName("Обработка очень большого количества инструментов")
    @Description("Тест проверяет обработку большого количества инструментов")
    @Story("Граничные случаи")
    @Severity(SeverityLevel.NORMAL)
    @Tag("boundary")
    @Tag("morning-session")
    @Tag("large-dataset")
    @Tag("performance")
    void processMorningSessionPrices_ShouldHandleLargeNumberOfInstruments_WhenManyInstrumentsProvided() {
        
        // Шаг 1: Подготовка тестовых данных
        LocalDate testDate = Allure.step("Подготовка тестовых данных", () -> {
            return LocalDate.of(2024, 1, 15);
        });
        String taskId = "TEST_TASK_010";
        
        List<ShareEntity> manyShares = TestDataFactory.createShareEntityList(100);
        MinuteCandleEntity candle = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(shareRepository.findAll()).thenReturn(manyShares);
            when(futureRepository.findAll()).thenReturn(TestDataFactory.createEmptyFutureEntityList());
            when(indicativeRepository.findAll()).thenReturn(TestDataFactory.createEmptyIndicativeEntityList());
            when(openPriceRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
                .thenReturn(false);
            when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
                .thenReturn(Arrays.asList(candle));
            when(openPriceRepository.save(any(OpenPriceEntity.class)))
                .thenReturn(TestDataFactory.createOpenPriceEntity());
        });

        // Шаг 3: Выполнение обработки большого количества инструментов
        SaveResponseDto result = Allure.step("Выполнение обработки большого количества инструментов", () -> {
            return morningSessionService.processMorningSessionPrices(testDate, taskId);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(100, result.getTotalRequested());
            assertEquals(100, result.getNewItemsSaved());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(openPriceRepository, times(100)).save(any(OpenPriceEntity.class));
        });
    }

    @Test
    @DisplayName("Обработка индикативных инструментов с пустыми FIGI")
    @Description("Тест проверяет обработку индикативных инструментов с пустыми FIGI")
    @Story("Граничные случаи")
    @Severity(SeverityLevel.NORMAL)
    @Tag("boundary")
    @Tag("morning-session")
    @Tag("empty-figi")
    @Tag("data-validation")
    void processMorningSessionPrices_ShouldSkipIndicativesWithEmptyFigi_WhenIndicativesHaveEmptyFigi() {
        
        // Шаг 1: Подготовка тестовых данных
        LocalDate testDate = Allure.step("Подготовка тестовых данных", () -> {
            return LocalDate.of(2024, 1, 15);
        });
        String taskId = "TEST_TASK_011";
        
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        IndicativeEntity indicative1 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_001", "RTSI", "RUB");
        IndicativeEntity indicative2 = TestDataFactory.createIndicativeEntity("", "RTSI2", "RUB"); // Пустой FIGI
        IndicativeEntity indicative3 = TestDataFactory.createIndicativeEntity(null, "RTSI3", "RUB"); // null FIGI
        MinuteCandleEntity candle1 = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));
        MinuteCandleEntity candle2 = TestDataFactory.createMinuteCandleEntity("TEST_INDICATIVE_001", testDate, BigDecimal.valueOf(100.0));

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
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
        });

        // Шаг 3: Выполнение обработки с пустыми FIGI
        SaveResponseDto result = Allure.step("Выполнение обработки с пустыми FIGI", () -> {
            return morningSessionService.processMorningSessionPrices(testDate, taskId, true);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getTotalRequested()); // Акция + один индикатив с валидным FIGI
            assertEquals(2, result.getNewItemsSaved());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(openPriceRepository, times(2)).save(any(OpenPriceEntity.class));
        });
    }

    @Test
    @DisplayName("Обработка смешанных результатов с разными типами инструментов")
    @Description("Тест проверяет обработку смешанных результатов с разными типами инструментов")
    @Story("Граничные случаи")
    @Severity(SeverityLevel.NORMAL)
    @Tag("boundary")
    @Tag("morning-session")
    @Tag("mixed-results")
    @Tag("partial-success")
    void processMorningSessionPrices_ShouldHandleMixedResults_WhenSomeInstrumentsHaveCandlesAndSomeDoNot() {
        
        // Шаг 1: Подготовка тестовых данных
        LocalDate testDate = Allure.step("Подготовка тестовых данных", () -> {
            return LocalDate.of(2024, 1, 15);
        });
        String taskId = "TEST_TASK_012";
        
        ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        ShareEntity share2 = TestDataFactory.createShareEntity("TEST_SHARE_002", "GAZP", "RUB", "MOEX");
        FutureEntity future1 = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "Si-3.24", "RUB");
        FutureEntity future2 = TestDataFactory.createFutureEntity("TEST_FUTURE_002", "RTS-3.24", "RUB");
        
        MinuteCandleEntity candle1 = TestDataFactory.createMinuteCandleEntity("TEST_SHARE_001", testDate, BigDecimal.valueOf(100.0));
        MinuteCandleEntity candle2 = TestDataFactory.createMinuteCandleEntity("TEST_FUTURE_001", testDate, BigDecimal.valueOf(200.0));
        // Нет свечей для share2 и future2

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
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
        });

        // Шаг 3: Выполнение обработки смешанных результатов
        SaveResponseDto result = Allure.step("Выполнение обработки смешанных результатов", () -> {
            return morningSessionService.processMorningSessionPrices(testDate, taskId);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getTotalRequested()); // Только те, для которых есть свечи
            assertEquals(2, result.getNewItemsSaved());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(openPriceRepository, times(2)).save(any(OpenPriceEntity.class));
        });
    }
}