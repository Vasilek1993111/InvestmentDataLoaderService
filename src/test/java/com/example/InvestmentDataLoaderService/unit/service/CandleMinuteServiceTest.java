package com.example.InvestmentDataLoaderService.unit.service;

import com.example.InvestmentDataLoaderService.client.TinkoffApiClient;
import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;
import com.example.InvestmentDataLoaderService.fixtures.TestDataFactory;
import com.example.InvestmentDataLoaderService.repository.*;
import com.example.InvestmentDataLoaderService.service.MinuteCandleService;

import io.qameta.allure.*;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Timeout(value = 5, unit = TimeUnit.SECONDS)
@Epic("Candles Minute Service")
@Feature("Candles Minute Service")
@DisplayName("Candles Minute Service Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
@Tag("unit")
@Tag("service")
@Tag("candles")
@Tag("minute")
public class CandleMinuteServiceTest {  
    
    @Mock
    private MinuteCandleRepository minuteCandleRepository;
    @Mock
    private ShareRepository shareRepository;
    @Mock
    private FutureRepository futureRepository;
    @Mock
    private IndicativeRepository indicativeRepository;
    @Mock
    private TinkoffApiClient tinkoffApiClient;
    @Mock
    private SystemLogRepository systemLogRepository;
    @Mock
    private Executor minuteCandleExecutor;
    @Mock
    private Executor apiDataExecutor;
    @Mock
    private Executor batchWriteExecutor;

    @InjectMocks
    private MinuteCandleService minuteCandleService;

    @BeforeEach
    @Step("Инициализация тестового окружения")
    @DisplayName("Инициализация тестового окружения")
    @Description("Сброс всех моков перед каждым тестом")
    void setUp() {
        reset(minuteCandleRepository, shareRepository, futureRepository, indicativeRepository, 
              tinkoffApiClient, systemLogRepository, minuteCandleExecutor, apiDataExecutor, batchWriteExecutor);
    }

    // ========== ПОЗИТИВНЫЕ ТЕСТЫ ==========

    @Test
    @DisplayName("Успешная загрузка минутных свечей с указанной датой")
    @Description("Тест проверяет основную функциональность загрузки минутных свечей с корректными данными")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("positive")
    @Tag("candles")
    @Tag("success")
    @Tag("async")
    @Tag("date-specified")
    void saveMinuteCandlesAsync_ShouldReturnSuccessResponse_WhenNewCandlesAreLoaded() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        MinuteCandleRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createMinuteCandleRequestDto();
        });
        String taskId = "test-task-123";
        List<CandleDto> testCandles = TestDataFactory.createCandleDtoList();

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN")))
                .thenReturn(testCandles);
            when(minuteCandleRepository.existsByFigiAndTime(anyString(), any()))
                .thenReturn(false);
            when(minuteCandleRepository.saveAll(anyList()))
                .thenReturn(Arrays.asList());
            when(systemLogRepository.save(any(SystemLogEntity.class)))
                .thenReturn(new SystemLogEntity());
            setupExecutorMocks();
        });

        // Шаг 3: Выполнение асинхронной загрузки
        CompletableFuture<SaveResponseDto> futureResult = Allure.step("Выполнение асинхронной загрузки", () -> {
            return minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        });
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("Загрузка минутных свечей завершена успешно", result.getMessage());
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
            verify(tinkoffApiClient, atLeastOnce()).getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN"));
            verify(minuteCandleRepository, atLeastOnce()).existsByFigiAndTime(anyString(), any());
            verify(minuteCandleRepository, atLeastOnce()).saveAll(anyList());
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
        });
    }

    @Test
    @DisplayName("Успешная загрузка минутных свечей без указания даты")
    @Description("Тест проверяет автоматическую установку текущей даты при null")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("positive")
    @Tag("candles")
    @Tag("date")
    @Tag("async")
    @Tag("auto-date")
    void saveMinuteCandlesAsync_ShouldReturnSuccessResponse_WhenDateIsNull() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных с null датой
        MinuteCandleRequestDto request = Allure.step("Подготовка тестовых данных с null датой", () -> {
            return TestDataFactory.createMinuteCandleRequestDto(
                Arrays.asList("BBG004730N88"), 
                Arrays.asList("SHARES"), 
                null
            );
        });
        String taskId = "test-task-123";
        List<CandleDto> testCandles = TestDataFactory.createCandleDtoList();

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(tinkoffApiClient.getCandles(eq("BBG004730N88"), any(LocalDate.class), eq("CANDLE_INTERVAL_1_MIN")))
                .thenReturn(testCandles);
            when(minuteCandleRepository.existsByFigiAndTime(anyString(), any()))
                .thenReturn(false);
            when(minuteCandleRepository.saveAll(anyList()))
                .thenReturn(Arrays.asList());
            when(systemLogRepository.save(any(SystemLogEntity.class)))
                .thenReturn(new SystemLogEntity());
            setupExecutorMocks();
        });

        // Шаг 3: Выполнение асинхронной загрузки
        CompletableFuture<SaveResponseDto> futureResult = Allure.step("Выполнение асинхронной загрузки", () -> {
            return minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        });
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("Загрузка минутных свечей завершена успешно", result.getMessage());
            assertEquals(2, result.getTotalRequested());
            assertEquals(2, result.getNewItemsSaved());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(tinkoffApiClient, atLeastOnce()).getCandles(eq("BBG004730N88"), any(LocalDate.class), eq("CANDLE_INTERVAL_1_MIN"));
        });
    }

    @Test
    @DisplayName("Успешная загрузка минутных свечей для множественных инструментов")
    @Description("Тест проверяет обработку нескольких инструментов одновременно")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("positive")
    @Tag("candles")
    @Tag("multiple")
    @Tag("async")
    @Tag("batch-processing")
    void saveMinuteCandlesAsync_ShouldReturnSuccessResponse_WhenMultipleInstruments() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных для множественных инструментов
        MinuteCandleRequestDto request = Allure.step("Подготовка тестовых данных для множественных инструментов", () -> {
            return TestDataFactory.createMultipleInstrumentsRequest();
        });
        String taskId = "test-task-123";
        List<CandleDto> testCandles = TestDataFactory.createCandleDtoList();

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(tinkoffApiClient.getCandles(anyString(), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN")))
                .thenReturn(testCandles);
            when(minuteCandleRepository.existsByFigiAndTime(anyString(), any()))
                .thenReturn(false);
            when(minuteCandleRepository.saveAll(anyList()))
                .thenReturn(Arrays.asList());
            when(systemLogRepository.save(any(SystemLogEntity.class)))
                .thenReturn(new SystemLogEntity());
            setupExecutorMocks();
        });

        // Шаг 3: Выполнение асинхронной загрузки
        CompletableFuture<SaveResponseDto> futureResult = Allure.step("Выполнение асинхронной загрузки", () -> {
            return minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        });
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("Загрузка минутных свечей завершена успешно", result.getMessage());
            assertEquals(4, result.getTotalRequested()); // 2 инструмента * 2 свечи
            assertEquals(4, result.getNewItemsSaved());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(tinkoffApiClient, atLeastOnce()).getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN"));
            verify(tinkoffApiClient, atLeastOnce()).getCandles(eq("BBG004730ZJ29"), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN"));
        });
    }

    @Test
    @DisplayName("Успешная загрузка минутных свечей с пропуском существующих")
    @Description("Тест проверяет корректную обработку уже существующих свечей")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("positive")
    @Tag("candles")
    @Tag("existing")
    @Tag("async")
    @Tag("duplicate-handling")
    void saveMinuteCandlesAsync_ShouldSkipExistingCandles_WhenCandlesAlreadyExist() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        MinuteCandleRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createMinuteCandleRequestDto();
        });
        String taskId = "test-task-123";
        List<CandleDto> testCandles = TestDataFactory.createCandleDtoList();

        // Шаг 2: Настройка моков - некоторые свечи уже существуют
        Allure.step("Настройка моков - некоторые свечи уже существуют", () -> {
            when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN")))
                .thenReturn(testCandles);
            when(minuteCandleRepository.existsByFigiAndTime(eq("BBG004730N88"), any()))
                .thenReturn(true, false); // Первая свеча существует, вторая - новая
            when(minuteCandleRepository.saveAll(anyList()))
                .thenReturn(Arrays.asList());
            when(systemLogRepository.save(any(SystemLogEntity.class)))
                .thenReturn(new SystemLogEntity());
            setupExecutorMocks();
        });

        // Шаг 3: Выполнение асинхронной загрузки
        CompletableFuture<SaveResponseDto> futureResult = Allure.step("Выполнение асинхронной загрузки", () -> {
            return minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        });
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getTotalRequested());
            assertEquals(1, result.getNewItemsSaved());
            assertEquals(1, result.getExistingItemsSkipped());
            assertEquals(0, result.getInvalidItemsFiltered());
            assertEquals(0, result.getMissingFromApi());
        });
    }

    @Test
    @DisplayName("Успешная загрузка минутных свечей с пустым списком инструментов - получение из БД")
    @Description("Тест проверяет получение инструментов из БД при пустом списке")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("positive")
    @Tag("candles")
    @Tag("database")
    @Tag("async")
    @Tag("auto-instruments")
    void saveMinuteCandlesAsync_ShouldLoadInstrumentsFromDatabase_WhenInstrumentsListIsEmpty() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        MinuteCandleRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createEmptyInstrumentsRequest();
        });
        String taskId = "test-task-123";
        
        ShareEntity share1 = TestDataFactory.createShareEntity("BBG004730N88", "SBER", "Сбербанк", "TESTMOEX");
        ShareEntity share2 = TestDataFactory.createShareEntity("BBG004730ZJ29", "GAZP", "Газпром", "TESTMOEX");
        List<CandleDto> testCandles = TestDataFactory.createCandleDtoList();

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(shareRepository.findAll()).thenReturn(Arrays.asList(share1, share2));
            when(tinkoffApiClient.getCandles(anyString(), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN")))
                .thenReturn(testCandles);
            when(minuteCandleRepository.existsByFigiAndTime(anyString(), any()))
                .thenReturn(false);
            when(minuteCandleRepository.saveAll(anyList()))
                .thenReturn(Arrays.asList());
            when(systemLogRepository.save(any(SystemLogEntity.class)))
                .thenReturn(new SystemLogEntity());
            setupExecutorMocks();
        });

        // Шаг 3: Выполнение асинхронной загрузки
        CompletableFuture<SaveResponseDto> futureResult = Allure.step("Выполнение асинхронной загрузки", () -> {
            return minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        });
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(4, result.getTotalRequested()); // 2 инструмента * 2 свечи
            assertEquals(4, result.getNewItemsSaved());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(shareRepository, atLeastOnce()).findAll();
            verify(tinkoffApiClient, atLeastOnce()).getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN"));
            verify(tinkoffApiClient, atLeastOnce()).getCandles(eq("BBG004730ZJ29"), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN"));
        });
    }

    @Test
    @DisplayName("Успешная загрузка минутных свечей для разных типов активов")
    @Description("Тест проверяет обработку разных типов активов (SHARES, FUTURES, INDICATIVES)")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("positive")
    @Tag("candles")
    @Tag("mixed")
    @Tag("async")
    @Tag("multi-asset")
    void saveMinuteCandlesAsync_ShouldLoadAllAssetTypes_WhenMultipleAssetTypesProvided() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных для разных типов активов
        MinuteCandleRequestDto request = Allure.step("Подготовка тестовых данных для разных типов активов", () -> {
            return TestDataFactory.createMixedAssetTypesRequest();
        });
        String taskId = "test-task-123";
        
        ShareEntity share = TestDataFactory.createShareEntity("BBG004730N88", "SBER", "Сбербанк", "TESTMOEX");
        FutureEntity future = TestDataFactory.createFutureEntity("BBG004730ZJ29", "Si-3.24", "FUTURES");
        IndicativeEntity indicative = TestDataFactory.createIndicativeEntity("BBG004730ABC1", "USD000UTSTOM", "Доллар США");
        List<CandleDto> testCandles = TestDataFactory.createCandleDtoList();

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(shareRepository.findAll()).thenReturn(Arrays.asList(share));
            when(futureRepository.findAll()).thenReturn(Arrays.asList(future));
            when(indicativeRepository.findAll()).thenReturn(Arrays.asList(indicative));
            when(tinkoffApiClient.getCandles(anyString(), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN")))
                .thenReturn(testCandles);
            when(minuteCandleRepository.existsByFigiAndTime(anyString(), any()))
                .thenReturn(false);
            when(minuteCandleRepository.saveAll(anyList()))
                .thenReturn(Arrays.asList());
            when(systemLogRepository.save(any(SystemLogEntity.class)))
                .thenReturn(new SystemLogEntity());
            setupExecutorMocks();
        });

        // Шаг 3: Выполнение асинхронной загрузки
        CompletableFuture<SaveResponseDto> futureResult = Allure.step("Выполнение асинхронной загрузки", () -> {
            return minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        });
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(6, result.getTotalRequested()); // 3 инструмента * 2 свечи
            assertEquals(6, result.getNewItemsSaved());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(shareRepository, atLeastOnce()).findAll();
            verify(futureRepository, atLeastOnce()).findAll();
            verify(indicativeRepository, atLeastOnce()).findAll();
        });
    }

    @Test
    @DisplayName("Успешная загрузка минутных свечей с фильтрацией незакрытых свечей")
    @Description("Тест проверяет фильтрацию незакрытых свечей (is_complete=false)")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("positive")
    @Tag("candles")
    @Tag("filter")
    @Tag("async")
    @Tag("data-validation")
    void saveMinuteCandlesAsync_ShouldFilterIncompleteCandles_WhenCandlesAreNotComplete() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных с незакрытыми свечами
        MinuteCandleRequestDto request = Allure.step("Подготовка тестовых данных с незакрытыми свечами", () -> {
            return TestDataFactory.createMinuteCandleRequestDto();
        });
        String taskId = "test-task-123";
        List<CandleDto> testCandles = TestDataFactory.createIncompleteCandleDtoList();

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN")))
                .thenReturn(testCandles);
            when(minuteCandleRepository.existsByFigiAndTime(anyString(), any()))
                .thenReturn(false);
            when(minuteCandleRepository.saveAll(anyList()))
                .thenReturn(Arrays.asList());
            when(systemLogRepository.save(any(SystemLogEntity.class)))
                .thenReturn(new SystemLogEntity());
            setupExecutorMocks();
        });

        // Шаг 3: Выполнение асинхронной загрузки
        CompletableFuture<SaveResponseDto> futureResult = Allure.step("Выполнение асинхронной загрузки", () -> {
            return minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        });
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(3, result.getTotalRequested());
            assertEquals(2, result.getNewItemsSaved()); // Только закрытые свечи
            assertEquals(1, result.getInvalidItemsFiltered()); // Одна незакрытая свеча отфильтрована
            assertEquals(0, result.getMissingFromApi());
        });
    }

    // ========== НЕГАТИВНЫЕ ТЕСТЫ ==========

    @Test
    @DisplayName("Обработка ошибки API при загрузке свечей")
    @Description("Тест проверяет обработку исключений от Tinkoff API")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("candles")
    @Tag("api-error")
    @Tag("async")
    @Tag("error-handling")
    void saveMinuteCandlesAsync_ShouldHandleApiError_WhenApiThrowsException() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        MinuteCandleRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createMinuteCandleRequestDto();
        });
        String taskId = "test-task-123";

        // Шаг 2: Настройка моков - API выбрасывает исключение
        Allure.step("Настройка моков - API выбрасывает исключение", () -> {
            when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN")))
                .thenThrow(new RuntimeException("API недоступен"));
            when(systemLogRepository.save(any(SystemLogEntity.class)))
                .thenReturn(new SystemLogEntity());
            setupExecutorMocks();
        });

        // Шаг 3: Выполнение асинхронной загрузки
        CompletableFuture<SaveResponseDto> futureResult = Allure.step("Выполнение асинхронной загрузки", () -> {
            return minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        });
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess()); // Сервис должен обработать ошибку gracefully
            assertEquals(0, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved());
            assertEquals(0, result.getInvalidItemsFiltered()); // Ошибка API не засчитывается как неверный элемент
        });
    }

    @Test
    @DisplayName("Обработка пустого ответа от API")
    @Description("Тест проверяет обработку случая, когда API возвращает пустой список")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("candles")
    @Tag("empty-response")
    @Tag("async")
    @Tag("no-data")
    void saveMinuteCandlesAsync_ShouldHandleEmptyApiResponse_WhenNoDataAvailable() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        MinuteCandleRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createMinuteCandleRequestDto();
        });
        String taskId = "test-task-123";

        // Шаг 2: Настройка моков - API возвращает пустой список
        Allure.step("Настройка моков - API возвращает пустой список", () -> {
            when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN")))
                .thenReturn(TestDataFactory.createEmptyCandleDtoList());
            when(systemLogRepository.save(any(SystemLogEntity.class)))
                .thenReturn(new SystemLogEntity());
            setupExecutorMocks();
        });

        // Шаг 3: Выполнение асинхронной загрузки
        CompletableFuture<SaveResponseDto> futureResult = Allure.step("Выполнение асинхронной загрузки", () -> {
            return minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        });
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(0, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved());
            assertEquals(1, result.getMissingFromApi()); // Один инструмент без данных
        });
    }

    @Test
    @DisplayName("Обработка ошибки базы данных при сохранении")
    @Description("Тест проверяет обработку исключений при работе с БД")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("candles")
    @Tag("database-error")
    @Tag("async")
    @Tag("persistence-error")
    void saveMinuteCandlesAsync_ShouldHandleDatabaseError_WhenSaveFails() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        MinuteCandleRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createMinuteCandleRequestDto();
        });
        String taskId = "test-task-123";
        List<CandleDto> testCandles = TestDataFactory.createCandleDtoList();

        // Шаг 2: Настройка моков - БД выбрасывает исключение
        Allure.step("Настройка моков - БД выбрасывает исключение", () -> {
            when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN")))
                .thenReturn(testCandles);
            when(minuteCandleRepository.existsByFigiAndTime(anyString(), any()))
                .thenReturn(false);
            when(minuteCandleRepository.saveAll(anyList()))
                .thenThrow(new RuntimeException("Ошибка базы данных"));
            when(systemLogRepository.save(any(SystemLogEntity.class)))
                .thenReturn(new SystemLogEntity());
            setupExecutorMocks();
        });

        // Шаг 3: Выполнение асинхронной загрузки
        CompletableFuture<SaveResponseDto> futureResult = Allure.step("Выполнение асинхронной загрузки", () -> {
            return minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        });
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess()); // Сервис должен обработать ошибку gracefully
            assertEquals(2, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved()); // Ничего не сохранилось из-за ошибки БД
        });
    }

    @Test
    @DisplayName("Обработка пустого списка инструментов и пустой БД")
    @Description("Тест проверяет обработку пустого списка инструментов и пустой БД")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("candles")
    @Tag("empty-database")
    @Tag("async")
    @Tag("no-instruments")
    void saveMinuteCandlesAsync_ShouldHandleEmptyInstrumentsAndEmptyDatabase_WhenNoInstrumentsAvailable() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        MinuteCandleRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createEmptyInstrumentsRequest();
        });
        String taskId = "test-task-123";

        // Шаг 2: Настройка моков для получения инструментов из БД
        Allure.step("Настройка моков для получения инструментов из БД", () -> {
            when(shareRepository.findAll()).thenReturn(Arrays.asList()); // Пустой список
            setupExecutorMocks();
        });

        // Шаг 3: Выполнение асинхронной загрузки
        CompletableFuture<SaveResponseDto> futureResult = Allure.step("Выполнение асинхронной загрузки", () -> {
            return minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        });
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(0, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(shareRepository, atLeastOnce()).findAll();
        });
    }

    @Test
    @DisplayName("Обработка будущей даты")
    @Description("Тест проверяет обработку запроса с будущей датой")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("candles")
    @Tag("future-date")
    @Tag("async")
    @Tag("date-validation")
    void saveMinuteCandlesAsync_ShouldHandleFutureDate_WhenDateIsInFuture() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных с будущей датой
        MinuteCandleRequestDto request = Allure.step("Подготовка тестовых данных с будущей датой", () -> {
            return TestDataFactory.createFutureDateRequest();
        });
        String taskId = "test-task-123";

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN")))
                .thenReturn(TestDataFactory.createEmptyCandleDtoList()); // API не возвращает данных для будущих дат
            when(systemLogRepository.save(any(SystemLogEntity.class)))
                .thenReturn(new SystemLogEntity());
            setupExecutorMocks();
        });

        // Шаг 3: Выполнение асинхронной загрузки
        CompletableFuture<SaveResponseDto> futureResult = Allure.step("Выполнение асинхронной загрузки", () -> {
            return minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        });
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(0, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved());
            assertEquals(1, result.getMissingFromApi()); // Данных для будущей даты нет
        });
    }

    @Test
    @DisplayName("Обработка ошибки при конвертации свечи")
    @Description("Тест проверяет обработку ошибок при конвертации DTO в Entity")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("candles")
    @Tag("conversion-error")
    @Tag("async")
    @Tag("data-validation")
    void saveMinuteCandlesAsync_ShouldHandleConversionError_WhenCandleConversionFails() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных с невалидными свечами
        MinuteCandleRequestDto request = Allure.step("Подготовка тестовых данных с невалидными свечами", () -> {
            return TestDataFactory.createMinuteCandleRequestDto();
        });
        String taskId = "test-task-123";
        List<CandleDto> testCandles = TestDataFactory.createInvalidCandleDtoList();

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN")))
                .thenReturn(testCandles);
            when(minuteCandleRepository.existsByFigiAndTime(anyString(), any()))
                .thenReturn(false);
            when(minuteCandleRepository.saveAll(anyList()))
                .thenReturn(Arrays.asList());
            when(systemLogRepository.save(any(SystemLogEntity.class)))
                .thenReturn(new SystemLogEntity());
            setupExecutorMocks();
        });

        // Шаг 3: Выполнение асинхронной загрузки
        CompletableFuture<SaveResponseDto> futureResult = Allure.step("Выполнение асинхронной загрузки", () -> {
            return minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        });
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getTotalRequested());
            assertEquals(2, result.getNewItemsSaved()); // Обе свечи сохранились (даже с нулевыми значениями)
            assertEquals(0, result.getInvalidItemsFiltered()); // Никаких свечей не было отфильтровано
        });
    }

    @Test
    @DisplayName("Обработка критической ошибки в основном потоке")
    @Description("Тест проверяет обработку критических ошибок в основном методе")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("negative")
    @Tag("candles")
    @Tag("critical-error")
    @Tag("async")
    @Tag("error-handling")
    void saveMinuteCandlesAsync_ShouldHandleCriticalError_WhenMainThreadFails() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        MinuteCandleRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createMinuteCandleRequestDto();
        });
        String taskId = "test-task-123";

        // Шаг 2: Настройка моков для выброса исключения в основном потоке
        Allure.step("Настройка моков для выброса исключения в основном потоке", () -> {
            when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN")))
                .thenThrow(new RuntimeException("Критическая ошибка"));
            when(systemLogRepository.save(any(SystemLogEntity.class)))
                .thenReturn(new SystemLogEntity());
            setupExecutorMocks();
        });

        // Шаг 3: Выполнение асинхронной загрузки
        CompletableFuture<SaveResponseDto> futureResult = Allure.step("Выполнение асинхронной загрузки", () -> {
            return minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        });
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess()); // Сервис должен обработать ошибку gracefully
            assertEquals(0, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved());
            assertEquals(0, result.getInvalidItemsFiltered()); // Ошибка API не засчитывается как неверный элемент
        });
    }

    @Test
    @DisplayName("Обработка null значений в запросе")
    @Description("Тест проверяет обработку null значений в различных полях запроса")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("candles")
    @Tag("null-values")
    @Tag("async")
    @Tag("data-validation")
    void saveMinuteCandlesAsync_ShouldHandleNullValues_WhenRequestContainsNulls() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных с null значениями
        MinuteCandleRequestDto request = Allure.step("Подготовка тестовых данных с null значениями", () -> {
            return TestDataFactory.createNullValuesRequest();
        });
        String taskId = "test-task-123";

        // Шаг 2: Настройка моков для получения инструментов из БД
        Allure.step("Настройка моков для получения инструментов из БД", () -> {
            when(shareRepository.findAll()).thenReturn(Arrays.asList());
            when(futureRepository.findAll()).thenReturn(Arrays.asList());
            when(indicativeRepository.findAll()).thenReturn(Arrays.asList());
            setupExecutorMocks();
        });

        // Шаг 3: Выполнение асинхронной загрузки
        CompletableFuture<SaveResponseDto> futureResult = Allure.step("Выполнение асинхронной загрузки", () -> {
            return minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        });
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(0, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(shareRepository, atLeastOnce()).findAll();
            verify(futureRepository, atLeastOnce()).findAll();
            verify(indicativeRepository, atLeastOnce()).findAll();
        });
    }

    // ========== ГРАНИЧНЫЕ СЛУЧАИ ==========

    @Test
    @DisplayName("Обработка очень большого количества инструментов")
    @Description("Тест проверяет обработку большого количества инструментов")
    @Story("Граничные случаи")
    @Severity(SeverityLevel.NORMAL)
    @Tag("boundary")
    @Tag("candles")
    @Tag("large-scale")
    @Tag("async")
    @Tag("performance")
    void saveMinuteCandlesAsync_ShouldHandleLargeNumberOfInstruments_WhenManyInstrumentsProvided() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных с большим количеством инструментов
        MinuteCandleRequestDto request = Allure.step("Подготовка тестовых данных с большим количеством инструментов", () -> {
            return TestDataFactory.createLargeInstrumentsRequest();
        });
        String taskId = "test-task-123";
        
        List<CandleDto> testCandles = TestDataFactory.createCandleDtoList();

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(tinkoffApiClient.getCandles(anyString(), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN")))
                .thenReturn(testCandles);
            when(minuteCandleRepository.existsByFigiAndTime(anyString(), any()))
                .thenReturn(false);
            when(minuteCandleRepository.saveAll(anyList()))
                .thenReturn(Arrays.asList());
            when(systemLogRepository.save(any(SystemLogEntity.class)))
                .thenReturn(new SystemLogEntity());
            setupExecutorMocks();
        });

        // Шаг 3: Выполнение асинхронной загрузки
        CompletableFuture<SaveResponseDto> futureResult = Allure.step("Выполнение асинхронной загрузки", () -> {
            return minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        });
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(200, result.getTotalRequested()); // 100 инструментов * 2 свечи
            assertEquals(200, result.getNewItemsSaved());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(tinkoffApiClient, times(100)).getCandles(anyString(), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN"));
        });
    }

    @Test
    @DisplayName("Обработка смешанных результатов API")
    @Description("Тест проверяет обработку смешанных результатов от API (часть успешных, часть пустых)")
    @Story("Граничные случаи")
    @Severity(SeverityLevel.NORMAL)
    @Tag("boundary")
    @Tag("candles")
    @Tag("mixed-results")
    @Tag("async")
    @Tag("partial-success")
    void saveMinuteCandlesAsync_ShouldHandleMixedApiResults_WhenSomeInstrumentsReturnDataAndSomeDoNot() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        MinuteCandleRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createMultipleInstrumentsRequest();
        });
        String taskId = "test-task-123";
        List<CandleDto> testCandles = TestDataFactory.createCandleDtoList();

        // Шаг 2: Настройка моков - первый инструмент возвращает данные, второй - пустой список
        Allure.step("Настройка моков - первый инструмент возвращает данные, второй - пустой список", () -> {
            when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN")))
                .thenReturn(testCandles);
            when(tinkoffApiClient.getCandles(eq("BBG004730ZJ29"), eq(request.getDate()), eq("CANDLE_INTERVAL_1_MIN")))
                .thenReturn(TestDataFactory.createEmptyCandleDtoList());
            when(minuteCandleRepository.existsByFigiAndTime(anyString(), any()))
                .thenReturn(false);
            when(minuteCandleRepository.saveAll(anyList()))
                .thenReturn(Arrays.asList());
            when(systemLogRepository.save(any(SystemLogEntity.class)))
                .thenReturn(new SystemLogEntity());
            setupExecutorMocks();
        });

        // Шаг 3: Выполнение асинхронной загрузки
        CompletableFuture<SaveResponseDto> futureResult = Allure.step("Выполнение асинхронной загрузки", () -> {
            return minuteCandleService.saveMinuteCandlesAsync(request, taskId);
        });
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getTotalRequested()); // Только первый инструмент вернул данные
            assertEquals(2, result.getNewItemsSaved());
            assertEquals(1, result.getMissingFromApi()); // Второй инструмент не вернул данных
        });
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    @Step("Настройка моков для executor'ов")
    @DisplayName("Настройка моков для executor'ов")
    @Description("Настраивает executor'ы для синхронного выполнения в тестах")
    @Tag("helper")
    @Tag("setup")
    private void setupExecutorMocks() {
        Allure.step("Настройка executor'ов для синхронного выполнения", () -> {
            // Настраиваем executor'ы для синхронного выполнения в тестах
            lenient().doAnswer(invocation -> {
                Runnable task = invocation.getArgument(0);
                task.run();
                return null;
            }).when(minuteCandleExecutor).execute(any(Runnable.class));
            
            lenient().doAnswer(invocation -> {
                Runnable task = invocation.getArgument(0);
                task.run();
                return null;
            }).when(apiDataExecutor).execute(any(Runnable.class));
            
            lenient().doAnswer(invocation -> {
                Runnable task = invocation.getArgument(0);
                task.run();
                return null;
            }).when(batchWriteExecutor).execute(any(Runnable.class));
        });
    }
}