package com.example.InvestmentDataLoaderService.unit.service;

import com.example.InvestmentDataLoaderService.client.TinkoffApiClient;
import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;
import com.example.InvestmentDataLoaderService.repository.*;
import com.example.InvestmentDataLoaderService.service.DailyCandleService;

import io.qameta.allure.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@Epic("Candles Daily Service")
@Feature("Candles Daily Service")
@DisplayName("Candles Daily Service Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)           
public class CandlesDailyServiceTest {  
    
    @Mock
    private DailyCandleRepository dailyCandleRepository;
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
    private Executor dailyCandleExecutor;
    @Mock
    private Executor dailyApiDataExecutor;
    @Mock
    private Executor dailyBatchWriteExecutor;

    @InjectMocks
    private DailyCandleService dailyCandleService;

    @BeforeEach
    void setUp() {
        reset(dailyCandleRepository, shareRepository, futureRepository, indicativeRepository, 
              tinkoffApiClient, systemLogRepository, dailyCandleExecutor, dailyApiDataExecutor, dailyBatchWriteExecutor);
    }

    // ========== ПОЗИТИВНЫЕ ТЕСТЫ ==========

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisplayName("Успешная загрузка дневных свечей с указанной датой")
    @Description("Тест проверяет основную функциональность загрузки дневных свечей")
    @Story("Успешные сценарии")
    void saveDailyCandlesAsync_ShouldReturnSuccessResponse_WhenNewCandlesAreLoaded() throws Exception {
        // Given
        DailyCandleRequestDto request = createRequestWithDate(LocalDate.of(2024, 1, 15));
        String taskId = "test-task-123";
        List<CandleDto> testCandles = createTestCandles();

        // Настраиваем моки
        when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY")))
            .thenReturn(testCandles);
        when(dailyCandleRepository.existsByFigiAndTime(anyString(), any(Instant.class)))
            .thenReturn(false);
        when(dailyCandleRepository.saveAll(anyList()))
            .thenReturn(Arrays.asList());
        when(systemLogRepository.save(any(SystemLogEntity.class)))
            .thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When
        CompletableFuture<SaveResponseDto> futureResult = dailyCandleService.saveDailyCandlesAsync(request, taskId);
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Загрузка дневных свечей завершена успешно", result.getMessage());
        assertEquals(2, result.getTotalRequested());
        assertEquals(2, result.getNewItemsSaved());
        assertEquals(0, result.getExistingItemsSkipped());
        assertEquals(0, result.getInvalidItemsFiltered());
        assertEquals(0, result.getMissingFromApi());
        assertNotNull(result.getSavedItems());
        assertEquals(2, result.getSavedItems().size());

        // Verify interactions
        verify(tinkoffApiClient, atLeastOnce()).getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY"));
        verify(dailyCandleRepository, atLeastOnce()).existsByFigiAndTime(anyString(), any(Instant.class));
        verify(dailyCandleRepository, atLeastOnce()).saveAll(anyList());
        verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
    }

    @Test
    @DisplayName("Успешная загрузка дневных свечей без указания даты")
    @Description("Тест проверяет автоматическую установку текущей даты при null")
    @Story("Успешные сценарии")
    void saveDailyCandlesAsync_ShouldReturnSuccessResponse_WhenDateIsNull() throws Exception {
        // Given
        DailyCandleRequestDto request = createRequestWithDate(null);
        String taskId = "test-task-123";
        List<CandleDto> testCandles = createTestCandles();

        // Настраиваем моки
        when(tinkoffApiClient.getCandles(eq("BBG004730N88"), any(LocalDate.class), eq("CANDLE_INTERVAL_DAY")))
            .thenReturn(testCandles);
        when(dailyCandleRepository.existsByFigiAndTime(anyString(), any(Instant.class)))
            .thenReturn(false);
        when(dailyCandleRepository.saveAll(anyList()))
            .thenReturn(Arrays.asList());
        when(systemLogRepository.save(any(SystemLogEntity.class)))
            .thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When
        CompletableFuture<SaveResponseDto> futureResult = dailyCandleService.saveDailyCandlesAsync(request, taskId);
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Загрузка дневных свечей завершена успешно", result.getMessage());
        assertEquals(2, result.getTotalRequested());
        assertEquals(2, result.getNewItemsSaved());
        verify(tinkoffApiClient, atLeastOnce()).getCandles(eq("BBG004730N88"), any(LocalDate.class), eq("CANDLE_INTERVAL_DAY"));
    }

    @Test
    @DisplayName("Успешная загрузка дневных свечей для множественных инструментов")
    @Description("Тест проверяет обработку нескольких инструментов одновременно")
    @Story("Успешные сценарии")
    void saveDailyCandlesAsync_ShouldReturnSuccessResponse_WhenMultipleInstruments() throws Exception {
        // Given
        DailyCandleRequestDto request = createRequestWithMultipleInstruments();
        String taskId = "test-task-123";
        List<CandleDto> testCandles = createTestCandles();

        // Настраиваем моки
        when(tinkoffApiClient.getCandles(anyString(), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY")))
            .thenReturn(testCandles);
        when(dailyCandleRepository.existsByFigiAndTime(anyString(), any(Instant.class)))
            .thenReturn(false);
        when(dailyCandleRepository.saveAll(anyList()))
            .thenReturn(Arrays.asList());
        when(systemLogRepository.save(any(SystemLogEntity.class)))
            .thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When
        CompletableFuture<SaveResponseDto> futureResult = dailyCandleService.saveDailyCandlesAsync(request, taskId);
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Загрузка дневных свечей завершена успешно", result.getMessage());
        assertEquals(4, result.getTotalRequested()); // 2 инструмента * 2 свечи
        assertEquals(4, result.getNewItemsSaved());
        verify(tinkoffApiClient, atLeastOnce()).getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY"));
        verify(tinkoffApiClient, atLeastOnce()).getCandles(eq("BBG004730ZJ29"), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY"));
    }

    @Test
    @DisplayName("Успешная загрузка дневных свечей с пропуском существующих")
    @Description("Тест проверяет корректную обработку уже существующих свечей")
    @Story("Успешные сценарии")
    void saveDailyCandlesAsync_ShouldSkipExistingCandles_WhenCandlesAlreadyExist() throws Exception {
        // Given
        DailyCandleRequestDto request = createRequestWithDate(LocalDate.of(2024, 1, 15));
        String taskId = "test-task-123";
        List<CandleDto> testCandles = createTestCandles();

        // Настраиваем моки - некоторые свечи уже существуют
        when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY")))
            .thenReturn(testCandles);
        when(dailyCandleRepository.existsByFigiAndTime(eq("BBG004730N88"), any(Instant.class)))
            .thenReturn(true, false); // Первая свеча существует, вторая - новая
        when(dailyCandleRepository.saveAll(anyList()))
            .thenReturn(Arrays.asList());
        when(systemLogRepository.save(any(SystemLogEntity.class)))
            .thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When
        CompletableFuture<SaveResponseDto> futureResult = dailyCandleService.saveDailyCandlesAsync(request, taskId);
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getTotalRequested());
        assertEquals(1, result.getNewItemsSaved());
        assertEquals(1, result.getExistingItemsSkipped());
        assertEquals(0, result.getInvalidItemsFiltered());
        assertEquals(0, result.getMissingFromApi());
    }

    @Test
    @DisplayName("Успешная загрузка дневных свечей с пустым списком инструментов - получение из БД")
    @Description("Тест проверяет получение инструментов из БД при пустом списке")
    @Story("Успешные сценарии")
    void saveDailyCandlesAsync_ShouldLoadInstrumentsFromDatabase_WhenInstrumentsListIsEmpty() throws Exception {
        // Given
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(Arrays.asList()); // Пустой список
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        String taskId = "test-task-123";
        
        ShareEntity share1 = createShareEntity("BBG004730N88", "SBER");
        ShareEntity share2 = createShareEntity("BBG004730ZJ29", "GAZP");
        List<CandleDto> testCandles = createTestCandles();

        // Настраиваем моки
        when(shareRepository.findAll()).thenReturn(Arrays.asList(share1, share2));
        when(tinkoffApiClient.getCandles(anyString(), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY")))
            .thenReturn(testCandles);
        when(dailyCandleRepository.existsByFigiAndTime(anyString(), any(Instant.class)))
            .thenReturn(false);
        when(dailyCandleRepository.saveAll(anyList()))
            .thenReturn(Arrays.asList());
        when(systemLogRepository.save(any(SystemLogEntity.class)))
            .thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When
        CompletableFuture<SaveResponseDto> futureResult = dailyCandleService.saveDailyCandlesAsync(request, taskId);
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(4, result.getTotalRequested()); // 2 инструмента * 2 свечи
        assertEquals(4, result.getNewItemsSaved());
        verify(shareRepository, atLeastOnce()).findAll();
        verify(tinkoffApiClient, atLeastOnce()).getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY"));
        verify(tinkoffApiClient, atLeastOnce()).getCandles(eq("BBG004730ZJ29"), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY"));
    }

    @Test
    @DisplayName("Успешная загрузка дневных свечей для разных типов активов")
    @Description("Тест проверяет обработку разных типов активов (SHARES, FUTURES, INDICATIVES)")
    @Story("Успешные сценарии")
    void saveDailyCandlesAsync_ShouldLoadAllAssetTypes_WhenMultipleAssetTypesProvided() throws Exception {
        // Given
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(Arrays.asList());
        request.setAssetType(Arrays.asList("SHARES", "FUTURES", "INDICATIVES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        String taskId = "test-task-123";
        
        ShareEntity share = createShareEntity("BBG004730N88", "SBER");
        FutureEntity future = createFutureEntity("BBG004730ZJ29", "Si-3.24");
        IndicativeEntity indicative = createIndicativeEntity("BBG004730ABC1", "USD000UTSTOM");
        List<CandleDto> testCandles = createTestCandles();

        // Настраиваем моки
        when(shareRepository.findAll()).thenReturn(Arrays.asList(share));
        when(futureRepository.findAll()).thenReturn(Arrays.asList(future));
        when(indicativeRepository.findAll()).thenReturn(Arrays.asList(indicative));
        when(tinkoffApiClient.getCandles(anyString(), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY")))
            .thenReturn(testCandles);
        when(dailyCandleRepository.existsByFigiAndTime(anyString(), any(Instant.class)))
            .thenReturn(false);
        when(dailyCandleRepository.saveAll(anyList()))
            .thenReturn(Arrays.asList());
        when(systemLogRepository.save(any(SystemLogEntity.class)))
            .thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When
        CompletableFuture<SaveResponseDto> futureResult = dailyCandleService.saveDailyCandlesAsync(request, taskId);
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(6, result.getTotalRequested()); // 3 инструмента * 2 свечи
        assertEquals(6, result.getNewItemsSaved());
        verify(shareRepository, atLeastOnce()).findAll();
        verify(futureRepository, atLeastOnce()).findAll();
        verify(indicativeRepository, atLeastOnce()).findAll();
    }

    @Test
    @DisplayName("Успешная загрузка дневных свечей с фильтрацией незакрытых свечей")
    @Description("Тест проверяет фильтрацию незакрытых свечей (is_complete=false)")
    @Story("Успешные сценарии")
    void saveDailyCandlesAsync_ShouldFilterIncompleteCandles_WhenCandlesAreNotComplete() throws Exception {
        // Given
        DailyCandleRequestDto request = createRequestWithDate(LocalDate.of(2024, 1, 15));
        String taskId = "test-task-123";
        List<CandleDto> testCandles = createTestCandlesWithIncomplete();

        // Настраиваем моки
        when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY")))
            .thenReturn(testCandles);
        when(dailyCandleRepository.existsByFigiAndTime(anyString(), any(Instant.class)))
            .thenReturn(false);
        when(dailyCandleRepository.saveAll(anyList()))
            .thenReturn(Arrays.asList());
        when(systemLogRepository.save(any(SystemLogEntity.class)))
            .thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When
        CompletableFuture<SaveResponseDto> futureResult = dailyCandleService.saveDailyCandlesAsync(request, taskId);
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(3, result.getTotalRequested());
        assertEquals(2, result.getNewItemsSaved()); // Только закрытые свечи
        assertEquals(1, result.getInvalidItemsFiltered()); // Одна незакрытая свеча отфильтрована
        assertEquals(0, result.getMissingFromApi());
    }

    // ========== НЕГАТИВНЫЕ ТЕСТЫ ==========

    @Test
    @DisplayName("Обработка ошибки API при загрузке свечей")
    @Description("Тест проверяет обработку исключений от Tinkoff API")
    @Story("Негативные сценарии")
    void saveDailyCandlesAsync_ShouldHandleApiError_WhenApiThrowsException() throws Exception {
        // Given
        DailyCandleRequestDto request = createRequestWithDate(LocalDate.of(2024, 1, 15));
        String taskId = "test-task-123";

        // Настраиваем моки - API выбрасывает исключение
        when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY")))
            .thenThrow(new RuntimeException("API недоступен"));
        when(systemLogRepository.save(any(SystemLogEntity.class)))
            .thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When
        CompletableFuture<SaveResponseDto> futureResult = dailyCandleService.saveDailyCandlesAsync(request, taskId);
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess()); // Сервис должен обработать ошибку gracefully
        assertEquals(0, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved());
        assertEquals(0, result.getInvalidItemsFiltered()); // Ошибка API не засчитывается как неверный элемент
    }

    @Test
    @DisplayName("Обработка пустого ответа от API")
    @Description("Тест проверяет обработку случая, когда API возвращает пустой список")
    @Story("Негативные сценарии")
    void saveDailyCandlesAsync_ShouldHandleEmptyApiResponse_WhenNoDataAvailable() throws Exception {
        // Given
        DailyCandleRequestDto request = createRequestWithDate(LocalDate.of(2024, 1, 15));
        String taskId = "test-task-123";

        // Настраиваем моки - API возвращает пустой список
        when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY")))
            .thenReturn(Arrays.asList());
        when(systemLogRepository.save(any(SystemLogEntity.class)))
            .thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When
        CompletableFuture<SaveResponseDto> futureResult = dailyCandleService.saveDailyCandlesAsync(request, taskId);
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved());
        assertEquals(1, result.getMissingFromApi()); // Один инструмент без данных
    }

    @Test
    @DisplayName("Обработка ошибки базы данных при сохранении")
    @Description("Тест проверяет обработку исключений при работе с БД")
    @Story("Негативные сценарии")
    void saveDailyCandlesAsync_ShouldHandleDatabaseError_WhenSaveFails() throws Exception {
        // Given
        DailyCandleRequestDto request = createRequestWithDate(LocalDate.of(2024, 1, 15));
        String taskId = "test-task-123";
        List<CandleDto> testCandles = createTestCandles();

        // Настраиваем моки - БД выбрасывает исключение
        when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY")))
            .thenReturn(testCandles);
        when(dailyCandleRepository.existsByFigiAndTime(anyString(), any(Instant.class)))
            .thenReturn(false);
        when(dailyCandleRepository.saveAll(anyList()))
            .thenThrow(new RuntimeException("Ошибка базы данных"));
        when(systemLogRepository.save(any(SystemLogEntity.class)))
            .thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When
        CompletableFuture<SaveResponseDto> futureResult = dailyCandleService.saveDailyCandlesAsync(request, taskId);
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess()); // Сервис должен обработать ошибку gracefully
        assertEquals(2, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved()); // Ничего не сохранилось из-за ошибки БД
    }

    @Test
    @DisplayName("Обработка пустого списка инструментов и пустой БД")
    @Description("Тест проверяет обработку пустого списка инструментов и пустой БД")
    @Story("Негативные сценарии")
    void saveDailyCandlesAsync_ShouldHandleEmptyInstrumentsAndEmptyDatabase_WhenNoInstrumentsAvailable() throws Exception {
        // Given
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(Arrays.asList()); // Пустой список
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        String taskId = "test-task-123";

        // Настраиваем моки для получения инструментов из БД
        when(shareRepository.findAll()).thenReturn(Arrays.asList()); // Пустой список
        setupExecutorMocks();

        // When
        CompletableFuture<SaveResponseDto> futureResult = dailyCandleService.saveDailyCandlesAsync(request, taskId);
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved());
        verify(shareRepository, atLeastOnce()).findAll();
    }

    @Test
    @DisplayName("Обработка будущей даты")
    @Description("Тест проверяет обработку запроса с будущей датой")
    @Story("Негативные сценарии")
    void saveDailyCandlesAsync_ShouldHandleFutureDate_WhenDateIsInFuture() throws Exception {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        DailyCandleRequestDto request = createRequestWithDate(futureDate);
        String taskId = "test-task-123";

        // Настраиваем моки
        when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(futureDate), eq("CANDLE_INTERVAL_DAY")))
            .thenReturn(Arrays.asList()); // API не возвращает данных для будущих дат
        when(systemLogRepository.save(any(SystemLogEntity.class)))
            .thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When
        CompletableFuture<SaveResponseDto> futureResult = dailyCandleService.saveDailyCandlesAsync(request, taskId);
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved());
        assertEquals(1, result.getMissingFromApi()); // Данных для будущей даты нет
    }

    @Test
    @DisplayName("Обработка ошибки при конвертации свечи")
    @Description("Тест проверяет обработку ошибок при конвертации DTO в Entity")
    @Story("Негативные сценарии")
    void saveDailyCandlesAsync_ShouldHandleConversionError_WhenCandleConversionFails() throws Exception {
        // Given
        DailyCandleRequestDto request = createRequestWithDate(LocalDate.of(2024, 1, 15));
        String taskId = "test-task-123";
        List<CandleDto> testCandles = createTestCandlesWithInvalidData();

        // Настраиваем моки
        when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY")))
            .thenReturn(testCandles);
        when(dailyCandleRepository.existsByFigiAndTime(anyString(), any(Instant.class)))
            .thenReturn(false);
        when(dailyCandleRepository.saveAll(anyList()))
            .thenReturn(Arrays.asList());
        when(systemLogRepository.save(any(SystemLogEntity.class)))
            .thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When
        CompletableFuture<SaveResponseDto> futureResult = dailyCandleService.saveDailyCandlesAsync(request, taskId);
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getTotalRequested());
        assertEquals(2, result.getNewItemsSaved()); // Обе свечи сохранились (даже с нулевыми значениями)
        assertEquals(0, result.getInvalidItemsFiltered()); // Никаких свечей не было отфильтровано
    }

    @Test
    @DisplayName("Обработка критической ошибки в основном потоке")
    @Description("Тест проверяет обработку критических ошибок в основном методе")
    @Story("Негативные сценарии")
    void saveDailyCandlesAsync_ShouldHandleCriticalError_WhenMainThreadFails() throws Exception {
        // Given
        DailyCandleRequestDto request = createRequestWithDate(LocalDate.of(2024, 1, 15));
        String taskId = "test-task-123";

        // Настраиваем моки для выброса исключения в основном потоке
        when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY")))
            .thenThrow(new RuntimeException("Критическая ошибка"));
        when(systemLogRepository.save(any(SystemLogEntity.class)))
            .thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When
        CompletableFuture<SaveResponseDto> futureResult = dailyCandleService.saveDailyCandlesAsync(request, taskId);
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess()); // Сервис должен обработать ошибку gracefully
        assertEquals(0, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved());
        assertEquals(0, result.getInvalidItemsFiltered()); // Ошибка API не засчитывается как неверный элемент
    }

    @Test
    @DisplayName("Обработка null значений в запросе")
    @Description("Тест проверяет обработку null значений в различных полях запроса")
    @Story("Негативные сценарии")
    void saveDailyCandlesAsync_ShouldHandleNullValues_WhenRequestContainsNulls() throws Exception {
        // Given
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(null);
        request.setAssetType(null);
        request.setDate(null);
        String taskId = "test-task-123";

        // Настраиваем моки для получения инструментов из БД
        when(shareRepository.findAll()).thenReturn(Arrays.asList());
        when(futureRepository.findAll()).thenReturn(Arrays.asList());
        when(indicativeRepository.findAll()).thenReturn(Arrays.asList());
        setupExecutorMocks();

        // When
        CompletableFuture<SaveResponseDto> futureResult = dailyCandleService.saveDailyCandlesAsync(request, taskId);
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved());
        verify(shareRepository, atLeastOnce()).findAll();
        verify(futureRepository, atLeastOnce()).findAll();
        verify(indicativeRepository, atLeastOnce()).findAll();
    }

    // ========== ГРАНИЧНЫЕ СЛУЧАИ ==========

    @Test
    @DisplayName("Обработка очень большого количества инструментов")
    @Description("Тест проверяет обработку большого количества инструментов")
    @Story("Граничные случаи")
    void saveDailyCandlesAsync_ShouldHandleLargeNumberOfInstruments_WhenManyInstrumentsProvided() throws Exception {
        // Given
        List<String> manyInstruments = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            manyInstruments.add("BBG004730N8" + String.format("%02d", i));
        }
        
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(manyInstruments);
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        String taskId = "test-task-123";
        
        List<CandleDto> testCandles = createTestCandles();

        // Настраиваем моки
        when(tinkoffApiClient.getCandles(anyString(), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY")))
            .thenReturn(testCandles);
        when(dailyCandleRepository.existsByFigiAndTime(anyString(), any(Instant.class)))
            .thenReturn(false);
        when(dailyCandleRepository.saveAll(anyList()))
            .thenReturn(Arrays.asList());
        when(systemLogRepository.save(any(SystemLogEntity.class)))
            .thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When
        CompletableFuture<SaveResponseDto> futureResult = dailyCandleService.saveDailyCandlesAsync(request, taskId);
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getTotalRequested()); // 100 инструментов * 2 свечи
        assertEquals(200, result.getNewItemsSaved());
        verify(tinkoffApiClient, times(100)).getCandles(anyString(), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY"));
    }

    @Test
    @DisplayName("Обработка смешанных результатов API")
    @Description("Тест проверяет обработку смешанных результатов от API (часть успешных, часть пустых)")
    @Story("Граничные случаи")
    void saveDailyCandlesAsync_ShouldHandleMixedApiResults_WhenSomeInstrumentsReturnDataAndSomeDoNot() throws Exception {
        // Given
        DailyCandleRequestDto request = createRequestWithMultipleInstruments();
        String taskId = "test-task-123";
        List<CandleDto> testCandles = createTestCandles();

        // Настраиваем моки - первый инструмент возвращает данные, второй - пустой список
        when(tinkoffApiClient.getCandles(eq("BBG004730N88"), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY")))
            .thenReturn(testCandles);
        when(tinkoffApiClient.getCandles(eq("BBG004730ZJ29"), eq(request.getDate()), eq("CANDLE_INTERVAL_DAY")))
            .thenReturn(Arrays.asList());
        when(dailyCandleRepository.existsByFigiAndTime(anyString(), any(Instant.class)))
            .thenReturn(false);
        when(dailyCandleRepository.saveAll(anyList()))
            .thenReturn(Arrays.asList());
        when(systemLogRepository.save(any(SystemLogEntity.class)))
            .thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When
        CompletableFuture<SaveResponseDto> futureResult = dailyCandleService.saveDailyCandlesAsync(request, taskId);
        SaveResponseDto result = futureResult.get(2, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getTotalRequested()); // Только первый инструмент вернул данные
        assertEquals(2, result.getNewItemsSaved());
        assertEquals(1, result.getMissingFromApi()); // Второй инструмент не вернул данных
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private DailyCandleRequestDto createRequestWithDate(LocalDate date) {
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(Arrays.asList("BBG004730N88"));
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(date);
        return request;
    }

    private DailyCandleRequestDto createRequestWithMultipleInstruments() {
        DailyCandleRequestDto request = new DailyCandleRequestDto();
        request.setInstruments(Arrays.asList("BBG004730N88", "BBG004730ZJ29"));
        request.setAssetType(Arrays.asList("SHARES"));
        request.setDate(LocalDate.of(2024, 1, 15));
        return request;
    }

    private List<CandleDto> createTestCandles() {
        return Arrays.asList(
            new CandleDto("BBG004730N88", 1000L, BigDecimal.valueOf(105.0), BigDecimal.valueOf(95.0), 
                         Instant.now(), BigDecimal.valueOf(102.0), BigDecimal.valueOf(100.0), true),
            new CandleDto("BBG004730N88", 1200L, BigDecimal.valueOf(108.0), BigDecimal.valueOf(98.0), 
                         Instant.now().minusSeconds(86400), BigDecimal.valueOf(106.0), BigDecimal.valueOf(103.0), true)
        );
    }

    private List<CandleDto> createTestCandlesWithIncomplete() {
        return Arrays.asList(
            new CandleDto("BBG004730N88", 1000L, BigDecimal.valueOf(105.0), BigDecimal.valueOf(95.0), 
                         Instant.now(), BigDecimal.valueOf(102.0), BigDecimal.valueOf(100.0), true),
            new CandleDto("BBG004730N88", 1200L, BigDecimal.valueOf(108.0), BigDecimal.valueOf(98.0), 
                         Instant.now().minusSeconds(86400), BigDecimal.valueOf(106.0), BigDecimal.valueOf(103.0), false), // Незакрытая свеча
            new CandleDto("BBG004730N88", 1500L, BigDecimal.valueOf(110.0), BigDecimal.valueOf(100.0), 
                         Instant.now().minusSeconds(172800), BigDecimal.valueOf(108.0), BigDecimal.valueOf(105.0), true)
        );
    }

    private List<CandleDto> createTestCandlesWithInvalidData() {
        return Arrays.asList(
            new CandleDto("BBG004730N88", 1000L, BigDecimal.valueOf(105.0), BigDecimal.valueOf(95.0), 
                         Instant.now(), BigDecimal.valueOf(102.0), BigDecimal.valueOf(100.0), true),
            new CandleDto("BBG004730N88", 0L, BigDecimal.ZERO, BigDecimal.ZERO, 
                         Instant.EPOCH, BigDecimal.ZERO, BigDecimal.ZERO, true) // Неверные данные
        );
    }

    private ShareEntity createShareEntity(String figi, String ticker) {
        ShareEntity share = new ShareEntity();
        share.setFigi(figi);
        share.setTicker(ticker);
        return share;
    }

    private FutureEntity createFutureEntity(String figi, String ticker) {
        FutureEntity future = new FutureEntity();
        future.setFigi(figi);
        future.setTicker(ticker);
        return future;
    }

    private IndicativeEntity createIndicativeEntity(String figi, String ticker) {
        IndicativeEntity indicative = new IndicativeEntity();
        indicative.setFigi(figi);
        indicative.setTicker(ticker);
        return indicative;
    }

    private void setupExecutorMocks() {
        // Настраиваем executor'ы для синхронного выполнения в тестах
        lenient().doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(dailyCandleExecutor).execute(any(Runnable.class));
        
        lenient().doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(dailyApiDataExecutor).execute(any(Runnable.class));
        
        lenient().doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(dailyBatchWriteExecutor).execute(any(Runnable.class));
    }
}