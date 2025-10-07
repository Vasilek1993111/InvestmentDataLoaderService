package com.example.InvestmentDataLoaderService.unit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import io.qameta.allure.Description;
import io.qameta.allure.Allure;
import static org.mockito.Mockito.*;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;


import java.time.LocalDate;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.InvestmentDataLoaderService.client.TinkoffApiClient;
import com.example.InvestmentDataLoaderService.controller.CandlesDailyController;
import com.example.InvestmentDataLoaderService.service.DailyCandleService;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.IndicativeRepository;
import com.example.InvestmentDataLoaderService.repository.SystemLogRepository;
import com.example.InvestmentDataLoaderService.fixtures.TestDataFactory;

/**
 * Unit-тесты для CandlesDailyController
 */
@WebMvcTest(CandlesDailyController.class)
@Epic("Candles Daily API")
@Feature("Daily Candles Management")
@DisplayName("Candles Daily Controller Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
public class CandlesDailyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DailyCandleService dailyCandleService;

    @MockitoBean
    private TinkoffApiClient tinkoffApiClient;

    @MockitoBean
    private ShareRepository shareRepository;

    @MockitoBean
    private FutureRepository futureRepository;

    @MockitoBean
    private IndicativeRepository indicativeRepository;

    @MockitoBean
    private SystemLogRepository systemLogRepository;



    @BeforeEach
    @Step("Подготовка тестовых данных для CandlesDailyController")
    void setUp() {
        reset(dailyCandleService);
        reset(tinkoffApiClient);
        reset(shareRepository);
        reset(futureRepository);    
        reset(indicativeRepository);
        reset(systemLogRepository);
    }

    // ==================== ТЕСТОВЫЕ ДАННЫЕ ====================


    private SaveResponseDto createMockSaveResponse() {
        return new SaveResponseDto(
            true,
            "Успешно сохранено 5 новых свечей из 10 найденных",
            10,
            5,
            5,
            0,
            0,
            Arrays.asList("candle1", "candle2", "candle3", "candle4", "candle5")
        );
    }

    // ==================== ТЕСТЫ ДЛЯ ОБЩИХ ДНЕВНЫХ СВЕЧЕЙ ====================

    @Test
    @DisplayName("Асинхронная загрузка дневных свечей за сегодня - успешный случай")
    @Description("Тест проверяет корректность запуска асинхронной загрузки дневных свечей за сегодня")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Daily Candles API")
    @Tag("api")
    @Tag("daily-candles")
    @Tag("async")
    @Tag("unit")
    @Tag("positive")
    void loadDailyCandlesTodayAsync_ShouldReturnTaskId_WhenValidRequestProvided() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            SystemLogEntity log = new SystemLogEntity();
            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
            when(dailyCandleService.saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString()))
                .thenReturn(CompletableFuture.completedFuture(createMockSaveResponse()));
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/candles/daily")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"instruments\":[\"BBG004730N88\",\"BBG004730ZJ9\"],\"assetType\":[\"SHARES\"],\"date\":\"" + LocalDate.now() + "\"}"))
                    .andExpect(status().isAccepted())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Загрузка дневных свечей за сегодня запущена"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.endpoint").value("/api/candles/daily"))
                    .andExpect(jsonPath("$.status").value("STARTED"))
                    .andExpect(jsonPath("$.startTime").exists());
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(dailyCandleService).saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString());
        });
    }

    @Test
    @DisplayName("Асинхронная загрузка дневных свечей за конкретную дату - успешный случай")
    @Description("Тест проверяет корректность запуска асинхронной загрузки дневных свечей за конкретную дату")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Daily Candles API")
    @Tag("api")
    @Tag("daily-candles")
    @Tag("async")
    @Tag("date")
    @Tag("unit")
    @Tag("positive")
    void loadDailyCandlesForDateAsync_ShouldReturnTaskId_WhenValidDateAndRequestProvided() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            SystemLogEntity log = new SystemLogEntity();
            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
            when(dailyCandleService.saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString()))
                .thenReturn(CompletableFuture.completedFuture(createMockSaveResponse()));
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/candles/daily/{date}", testDate)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"instruments\":[\"BBG004730N88\",\"BBG004730ZJ9\"],\"assetType\":[\"SHARES\"],\"date\":\"" + testDate + "\"}"))
                    .andExpect(status().isAccepted())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Загрузка дневных свечей за " + testDate + " запущена"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.endpoint").value("/api/candles/daily/" + testDate))
                    .andExpect(jsonPath("$.date").value(testDate.toString()))
                    .andExpect(jsonPath("$.status").value("STARTED"));
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(dailyCandleService).saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString());
        });
    }

    // ==================== ТЕСТЫ ДЛЯ АКЦИЙ ====================

    @Test
    @DisplayName("Получение дневных свечей акций за дату - успешный случай")
    @Description("Тест проверяет корректность получения дневных свечей акций за дату без сохранения")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares Daily Candles API")
    @Tag("api")
    @Tag("shares")
    @Tag("daily-candles")
    @Tag("get")
    @Tag("unit")
    @Tag("positive")
    void getSharesDailyCandlesForDate_ShouldReturnCandles_WhenValidDateProvided() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            List<ShareEntity> shares = TestDataFactory.createShareEntityList();
            List<CandleDto> candles = TestDataFactory.createCandleDtoList();
            
            SystemLogEntity log = new SystemLogEntity();
            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
            when(shareRepository.findAll()).thenReturn(shares);
            when(tinkoffApiClient.getCandles(anyString(), eq(testDate), eq("CANDLE_INTERVAL_DAY")))
                .thenReturn(candles);
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/candles/daily/shares/{date}", testDate)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.date").value(testDate.toString()))
                    .andExpect(jsonPath("$.assetType").value("SHARES"))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").exists())
                    .andExpect(jsonPath("$.totalInstruments").value(3))
                    .andExpect(jsonPath("$.processedInstruments").value(3))
                    .andExpect(jsonPath("$.successfulInstruments").exists())
                    .andExpect(jsonPath("$.noDataInstruments").exists())
                    .andExpect(jsonPath("$.errorInstruments").value(0));
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(shareRepository).findAll();
            verify(tinkoffApiClient, atLeastOnce()).getCandles(anyString(), eq(testDate), eq("CANDLE_INTERVAL_DAY"));
        });
    }

    @Test
    @DisplayName("Асинхронная загрузка дневных свечей акций за дату - успешный случай")
    @Description("Тест проверяет корректность запуска асинхронной загрузки дневных свечей акций за дату")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares Daily Candles API")
    @Tag("api")
    @Tag("shares")
    @Tag("daily-candles")
    @Tag("async")
    @Tag("post")
    @Tag("unit")
    @Tag("positive")
    void loadSharesDailyCandlesForDateAsync_ShouldReturnTaskId_WhenValidDateProvided() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            List<ShareEntity> shares = TestDataFactory.createShareEntityList();
            
            SystemLogEntity log = new SystemLogEntity();
            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
            when(shareRepository.findAll()).thenReturn(shares);
            when(dailyCandleService.saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString()))
                .thenReturn(CompletableFuture.completedFuture(createMockSaveResponse()));
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/candles/daily/shares/{date}", testDate)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isAccepted())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Загрузка дневных свечей акций запущена"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.endpoint").value("/api/candles/daily/shares/" + testDate))
                    .andExpect(jsonPath("$.date").value(testDate.toString()))
                    .andExpect(jsonPath("$.instrumentsCount").value(3))
                    .andExpect(jsonPath("$.status").value("STARTED"));
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(shareRepository).findAll();
            verify(dailyCandleService).saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString());
        });
    }

    // ==================== ТЕСТЫ ДЛЯ ФЬЮЧЕРСОВ ====================

    @Test
    @DisplayName("Получение дневных свечей фьючерсов за дату - успешный случай")
    @Description("Тест проверяет корректность получения дневных свечей фьючерсов за дату без сохранения")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures Daily Candles API")
    @Tag("api")
    @Tag("futures")
    @Tag("daily-candles")
    @Tag("get")
    @Tag("unit")
    @Tag("positive")
    void getFuturesDailyCandlesForDate_ShouldReturnCandles_WhenValidDateProvided() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            List<FutureEntity> futures = TestDataFactory.createFutureEntityList();
            List<CandleDto> candles = TestDataFactory.createCandleDtoList();
            
            when(systemLogRepository.save(any())).thenReturn(new SystemLogEntity());
            when(futureRepository.findAll()).thenReturn(futures);
            when(tinkoffApiClient.getCandles(anyString(), eq(testDate), eq("CANDLE_INTERVAL_DAY")))
                .thenReturn(candles);
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/candles/daily/futures/{date}", testDate)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.date").value(testDate.toString()))
                    .andExpect(jsonPath("$.assetType").value("FUTURES"))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").exists())
                    .andExpect(jsonPath("$.totalInstruments").value(3));
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(futureRepository).findAll();
            verify(tinkoffApiClient, atLeastOnce()).getCandles(anyString(), eq(LocalDate.of(2024, 1, 15)), eq("CANDLE_INTERVAL_DAY"));
        });
    }

    @Test
    @DisplayName("Асинхронная загрузка дневных свечей фьючерсов за дату - успешный случай")
    @Description("Тест проверяет корректность запуска асинхронной загрузки дневных свечей фьючерсов за дату")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures Daily Candles API")
    @Tag("api")
    @Tag("futures")
    @Tag("daily-candles")
    @Tag("async")
    @Tag("post")
    @Tag("unit")
    @Tag("positive")
    void loadFuturesDailyCandlesForDateAsync_ShouldReturnTaskId_WhenValidDateProvided() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            List<FutureEntity> futures = TestDataFactory.createFutureEntityList();
            
            when(systemLogRepository.save(any())).thenReturn(new SystemLogEntity());
            when(futureRepository.findAll()).thenReturn(futures);
            when(dailyCandleService.saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString()))
                .thenReturn(CompletableFuture.completedFuture(createMockSaveResponse()));
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/candles/daily/futures/{date}", testDate)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isAccepted())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Загрузка дневных свечей фьючерсов за " + testDate + " запущена"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.endpoint").value("/api/candles/daily/futures/" + testDate))
                    .andExpect(jsonPath("$.date").value(testDate.toString()))
                    .andExpect(jsonPath("$.instrumentsCount").value(3))
                    .andExpect(jsonPath("$.status").value("STARTED"));
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(futureRepository).findAll();
            verify(dailyCandleService).saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString());
        });
    }

    // ==================== ТЕСТЫ ДЛЯ ИНДИКАТИВОВ ====================

    @Test
    @DisplayName("Получение дневных свечей индикативов за дату - успешный случай")
    @Description("Тест проверяет корректность получения дневных свечей индикативов за дату без сохранения")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives Daily Candles API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("daily-candles")
    @Tag("get")
    @Tag("unit")
    @Tag("positive")
    void getIndicativesDailyCandlesForDate_ShouldReturnCandles_WhenValidDateProvided() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            List<IndicativeEntity> indicatives = TestDataFactory.createIndicativeEntityList();
            List<CandleDto> candles = TestDataFactory.createCandleDtoList();
            
            when(systemLogRepository.save(any())).thenReturn(new SystemLogEntity());
            when(indicativeRepository.findAll()).thenReturn(indicatives);
            when(tinkoffApiClient.getCandles(anyString(), eq(testDate), eq("CANDLE_INTERVAL_DAY")))
                .thenReturn(candles);
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/candles/daily/indicatives/{date}", testDate)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.date").value(testDate.toString()))
                    .andExpect(jsonPath("$.assetType").value("INDICATIVES"))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").exists())
                    .andExpect(jsonPath("$.totalInstruments").value(3));
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(indicativeRepository).findAll();
            verify(tinkoffApiClient, atLeastOnce()).getCandles(anyString(), eq(LocalDate.of(2024, 1, 15)), eq("CANDLE_INTERVAL_DAY"));
        });
    }

    @Test
    @DisplayName("Асинхронная загрузка дневных свечей индикативов за дату - успешный случай")
    @Description("Тест проверяет корректность запуска асинхронной загрузки дневных свечей индикативов за дату")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives Daily Candles API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("daily-candles")
    @Tag("async")
    @Tag("post")
    @Tag("unit")
    @Tag("positive")
    void loadIndicativesDailyCandlesForDateAsync_ShouldReturnTaskId_WhenValidDateProvided() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            List<IndicativeEntity> indicatives = TestDataFactory.createIndicativeEntityList();
            
            when(systemLogRepository.save(any())).thenReturn(new SystemLogEntity());
            when(indicativeRepository.findAll()).thenReturn(indicatives);
            when(dailyCandleService.saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString()))
                .thenReturn(CompletableFuture.completedFuture(createMockSaveResponse()));
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/candles/daily/indicatives/{date}", testDate)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isAccepted())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Загрузка дневных свечей индикативов за " + testDate + " запущена"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.endpoint").value("/api/candles/daily/indicatives/" + testDate))
                    .andExpect(jsonPath("$.date").value(testDate.toString()))
                    .andExpect(jsonPath("$.instrumentsCount").value(3))
                    .andExpect(jsonPath("$.status").value("STARTED"));
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(indicativeRepository).findAll();
            verify(dailyCandleService).saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString());
        });
    }

    // ==================== ТЕСТЫ ДЛЯ СИНХРОННОЙ ЗАГРУЗКИ АКЦИЙ ====================


    // ==================== НЕГАТИВНЫЕ ТЕСТЫ ====================

    @Test
    @DisplayName("Асинхронная загрузка дневных свечей с пустым запросом")
    @Description("Тест проверяет поведение контроллера при передаче пустого запроса")
    @Severity(SeverityLevel.NORMAL)
    @Story("Daily Candles API")
    @Tag("api")
    @Tag("daily-candles")
    @Tag("negative")
    @Tag("empty-request")
    @Tag("unit")
    void loadDailyCandlesTodayAsync_ShouldHandleEmptyRequest_WhenEmptyRequestProvided() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            when(systemLogRepository.save(any())).thenReturn(new SystemLogEntity());
            when(dailyCandleService.saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString()))
                .thenReturn(CompletableFuture.completedFuture(createMockSaveResponse()));
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/candles/daily")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isAccepted())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Загрузка дневных свечей за сегодня запущена"))
                    .andExpect(jsonPath("$.taskId").exists());
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(dailyCandleService).saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString());
        });
    }

    @Test
    @DisplayName("Получение дневных свечей акций с несуществующей датой")
    @Description("Тест проверяет поведение контроллера при передаче даты в будущем")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares Daily Candles API")
    @Tag("api")
    @Tag("shares")
    @Tag("daily-candles")
    @Tag("negative")
    @Tag("future-date")
    @Tag("unit")
    void getSharesDailyCandlesForDate_ShouldHandleFutureDate_WhenFutureDateProvided() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate futureDate = LocalDate.now().plusDays(30);
            List<ShareEntity> shares = TestDataFactory.createShareEntityList();
            
            when(systemLogRepository.save(any())).thenReturn(new SystemLogEntity());
            when(shareRepository.findAll()).thenReturn(shares);
            when(tinkoffApiClient.getCandles(anyString(), eq(futureDate), eq("CANDLE_INTERVAL_DAY")))
                .thenReturn(Arrays.asList());
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate futureDate = LocalDate.now().plusDays(30);
            mockMvc.perform(get("/api/candles/daily/shares/{date}", futureDate)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.date").value(futureDate.toString()))
                    .andExpect(jsonPath("$.assetType").value("SHARES"))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").value(0))
                    .andExpect(jsonPath("$.totalInstruments").value(3));
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            LocalDate futureDate = LocalDate.now().plusDays(30);
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(shareRepository).findAll();
            verify(tinkoffApiClient, atLeastOnce()).getCandles(anyString(), eq(futureDate), eq("CANDLE_INTERVAL_DAY"));
        });
    }

    @Test
    @DisplayName("Получение дневных свечей акций с некорректным форматом даты")
    @Description("Тест проверяет поведение контроллера при передаче некорректного формата даты")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares Daily Candles API")
    @Tag("api")
    @Tag("shares")
    @Tag("daily-candles")
    @Tag("negative")
    @Tag("invalid-date")
    @Tag("unit")
    void getSharesDailyCandlesForDate_ShouldReturnInternalServerError_WhenInvalidDateFormatProvided() throws Exception {
        
        // Шаг 1: Выполнение HTTP запроса с некорректным форматом даты
        Allure.step("Выполнение HTTP запроса с некорректным форматом даты", () -> {
            mockMvc.perform(get("/api/candles/daily/shares/{date}", "invalid-date")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.error").value("BadRequest"));
        });
    }

    @Test
    @DisplayName("Получение дневных свечей акций с датой в прошлом (более 1 года)")
    @Description("Тест проверяет поведение контроллера при передаче очень старой даты")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares Daily Candles API")
    @Tag("api")
    @Tag("shares")
    @Tag("daily-candles")
    @Tag("negative")
    @Tag("old-date")
    @Tag("unit")
    void getSharesDailyCandlesForDate_ShouldHandleOldDate_WhenVeryOldDateProvided() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate oldDate = LocalDate.now().minusYears(2);
            List<ShareEntity> shares = TestDataFactory.createShareEntityList();
            
            when(systemLogRepository.save(any())).thenReturn(new SystemLogEntity());
            when(shareRepository.findAll()).thenReturn(shares);
            when(tinkoffApiClient.getCandles(anyString(), eq(oldDate), eq("CANDLE_INTERVAL_DAY")))
                .thenReturn(Arrays.asList());
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate oldDate = LocalDate.now().minusYears(2);
            mockMvc.perform(get("/api/candles/daily/shares/{date}", oldDate)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.date").value(oldDate.toString()))
                    .andExpect(jsonPath("$.assetType").value("SHARES"))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").value(0))
                    .andExpect(jsonPath("$.totalInstruments").value(3));
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            LocalDate oldDate = LocalDate.now().minusYears(2);
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(shareRepository).findAll();
            verify(tinkoffApiClient, atLeastOnce()).getCandles(anyString(), eq(oldDate), eq("CANDLE_INTERVAL_DAY"));
        });
    }

    @Test
    @DisplayName("Получение дневных свечей фьючерсов с пустой базой данных")
    @Description("Тест проверяет поведение контроллера когда в базе нет фьючерсов")
    @Severity(SeverityLevel.NORMAL)
    @Story("Futures Daily Candles API")
    @Tag("api")
    @Tag("futures")
    @Tag("daily-candles")
    @Tag("negative")
    @Tag("empty-database")
    @Tag("unit")
    void getFuturesDailyCandlesForDate_ShouldHandleEmptyDatabase_WhenNoFuturesInDatabase() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            when(systemLogRepository.save(any())).thenReturn(new SystemLogEntity());
            when(futureRepository.findAll()).thenReturn(Arrays.asList());
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/candles/daily/futures/{date}", testDate)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.date").value(testDate.toString()))
                    .andExpect(jsonPath("$.assetType").value("FUTURES"))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").value(0))
                    .andExpect(jsonPath("$.totalInstruments").value(0));
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(futureRepository).findAll();
            verify(tinkoffApiClient, never()).getCandles(anyString(), any(), anyString());
        });
    }

    @Test
    @DisplayName("Получение дневных свечей индикативов с ошибкой API")
    @Description("Тест проверяет поведение контроллера при ошибке TinkoffApiClient")
    @Severity(SeverityLevel.NORMAL)
    @Story("Indicatives Daily Candles API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("daily-candles")
    @Tag("negative")
    @Tag("api-error")
    @Tag("unit")
    void getIndicativesDailyCandlesForDate_ShouldHandleApiError_WhenApiClientThrowsException() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            List<IndicativeEntity> indicatives = TestDataFactory.createIndicativeEntityList();
            
            when(systemLogRepository.save(any())).thenReturn(new SystemLogEntity());
            when(indicativeRepository.findAll()).thenReturn(indicatives);
            when(tinkoffApiClient.getCandles(anyString(), eq(testDate), eq("CANDLE_INTERVAL_DAY")))
                .thenThrow(new RuntimeException("API Error"));
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/candles/daily/indicatives/{date}", testDate)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.date").value(testDate.toString()))
                    .andExpect(jsonPath("$.assetType").value("INDICATIVES"))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").value(0))
                    .andExpect(jsonPath("$.totalInstruments").value(3));
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(indicativeRepository).findAll();
            verify(tinkoffApiClient, atLeastOnce()).getCandles(anyString(), eq(LocalDate.of(2024, 1, 15)), eq("CANDLE_INTERVAL_DAY"));
        });
    }

    @Test
    @DisplayName("Асинхронная загрузка дневных свечей с некорректным JSON")
    @Description("Тест проверяет поведение контроллера при передаче некорректного JSON")
    @Severity(SeverityLevel.NORMAL)
    @Story("Daily Candles API")
    @Tag("api")
    @Tag("daily-candles")
    @Tag("negative")
    @Tag("invalid-json")
    @Tag("unit")
    void loadDailyCandlesTodayAsync_ShouldReturnInternalServerError_WhenInvalidJsonProvided() throws Exception {
        
        // Шаг 1: Выполнение HTTP запроса с некорректным JSON
        Allure.step("Выполнение HTTP запроса с некорректным JSON", () -> {
            mockMvc.perform(post("/api/candles/daily")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.error").value("BadRequest"));
        });
    }

    @Test
    @DisplayName("Асинхронная загрузка дневных свечей с null значениями")
    @Description("Тест проверяет поведение контроллера при передаче null значений в запросе")
    @Severity(SeverityLevel.NORMAL)
    @Story("Daily Candles API")
    @Tag("api")
    @Tag("daily-candles")
    @Tag("negative")
    @Tag("null-values")
    @Tag("unit")
    void loadDailyCandlesTodayAsync_ShouldHandleNullValues_WhenNullValuesProvided() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            when(systemLogRepository.save(any())).thenReturn(new SystemLogEntity());
            when(dailyCandleService.saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString()))
                .thenReturn(CompletableFuture.completedFuture(createMockSaveResponse()));
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/candles/daily")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"instruments\":null,\"assetType\":null,\"date\":null}"))
                    .andExpect(status().isAccepted())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.taskId").exists());
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(dailyCandleService).saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString());
        });
    }

    // ==================== ТЕСТЫ ДЛЯ ГРАНИЧНЫХ СЛУЧАЕВ ====================

    @Test
    @DisplayName("Получение дневных свечей акций в выходной день")
    @Description("Тест проверяет поведение контроллера при запросе данных за выходной день")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares Daily Candles API")
    @Tag("api")
    @Tag("shares")
    @Tag("daily-candles")
    @Tag("edge-case")
    @Tag("weekend")
    @Tag("unit")
    void getSharesDailyCandlesForDate_ShouldHandleWeekend_WhenWeekendDateProvided() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate weekendDate = LocalDate.of(2024, 1, 13); // суббота
            List<ShareEntity> shares = TestDataFactory.createShareEntityList();
            
            when(systemLogRepository.save(any())).thenReturn(new SystemLogEntity());
            when(shareRepository.findAll()).thenReturn(shares);
            when(tinkoffApiClient.getCandles(anyString(), eq(weekendDate), eq("CANDLE_INTERVAL_DAY")))
                .thenReturn(Arrays.asList());
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate weekendDate = LocalDate.of(2024, 1, 13);
            mockMvc.perform(get("/api/candles/daily/shares/{date}", weekendDate)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.date").value(weekendDate.toString()))
                    .andExpect(jsonPath("$.assetType").value("SHARES"))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").value(0))
                    .andExpect(jsonPath("$.totalInstruments").value(3));
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            LocalDate weekendDate = LocalDate.of(2024, 1, 13);
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(shareRepository).findAll();
            verify(tinkoffApiClient, atLeastOnce()).getCandles(anyString(), eq(weekendDate), eq("CANDLE_INTERVAL_DAY"));
        });
    }

    @Test
    @DisplayName("Получение дневных свечей акций в праздничный день")
    @Description("Тест проверяет поведение контроллера при запросе данных за праздничный день")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares Daily Candles API")
    @Tag("api")
    @Tag("shares")
    @Tag("daily-candles")
    @Tag("edge-case")
    @Tag("holiday")
    @Tag("unit")
    void getSharesDailyCandlesForDate_ShouldHandleHoliday_WhenHolidayDateProvided() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate holidayDate = LocalDate.of(2024, 1, 1); // Новый год
            List<ShareEntity> shares = TestDataFactory.createShareEntityList();
            
            when(systemLogRepository.save(any())).thenReturn(new SystemLogEntity());
            when(shareRepository.findAll()).thenReturn(shares);
            when(tinkoffApiClient.getCandles(anyString(), eq(holidayDate), eq("CANDLE_INTERVAL_DAY")))
                .thenReturn(Arrays.asList());
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate holidayDate = LocalDate.of(2024, 1, 1);
            mockMvc.perform(get("/api/candles/daily/shares/{date}", holidayDate)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.date").value(holidayDate.toString()))
                    .andExpect(jsonPath("$.assetType").value("SHARES"))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").value(0))
                    .andExpect(jsonPath("$.totalInstruments").value(3));
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            LocalDate holidayDate = LocalDate.of(2024, 1, 1);
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(shareRepository).findAll();
            verify(tinkoffApiClient, atLeastOnce()).getCandles(anyString(), eq(holidayDate), eq("CANDLE_INTERVAL_DAY"));
        });
    }

    @Test
    @DisplayName("Получение дневных свечей акций с большим количеством инструментов")
    @Description("Тест проверяет поведение контроллера при большом количестве акций в базе")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares Daily Candles API")
    @Tag("api")
    @Tag("shares")
    @Tag("daily-candles")
    @Tag("edge-case")
    @Tag("large-dataset")
    @Tag("unit")
    void getSharesDailyCandlesForDate_ShouldHandleLargeDataset_WhenManySharesInDatabase() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            List<ShareEntity> largeShareList = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                largeShareList.add(TestDataFactory.createShareEntity("FIGI" + i, "TICKER" + i, "Name " + i, "RUB"));
            }
            List<CandleDto> candles = TestDataFactory.createCandleDtoList();
            
            when(systemLogRepository.save(any())).thenReturn(new SystemLogEntity());
            when(shareRepository.findAll()).thenReturn(largeShareList);
            when(tinkoffApiClient.getCandles(anyString(), eq(testDate), eq("CANDLE_INTERVAL_DAY")))
                .thenReturn(candles);
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/candles/daily/shares/{date}", testDate)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.date").value(testDate.toString()))
                    .andExpect(jsonPath("$.assetType").value("SHARES"))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalInstruments").value(100))
                    .andExpect(jsonPath("$.processedInstruments").value(100));
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(shareRepository).findAll();
            verify(tinkoffApiClient, atLeast(100)).getCandles(anyString(), eq(LocalDate.of(2024, 1, 15)), eq("CANDLE_INTERVAL_DAY"));
        });
    }

    // ==================== ТЕСТЫ ДЛЯ ОБРАБОТКИ ОШИБОК ====================

    @Test
    @DisplayName("Обработка ошибки при сохранении лога")
    @Description("Тест проверяет поведение контроллера при ошибке сохранения системного лога")
    @Severity(SeverityLevel.NORMAL)
    @Story("Daily Candles API")
    @Tag("api")
    @Tag("daily-candles")
    @Tag("error-handling")
    @Tag("logging-error")
    @Tag("unit")
    void loadDailyCandlesTodayAsync_ShouldHandleLoggingError_WhenSystemLogRepositoryThrowsException() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            when(systemLogRepository.save(any())).thenThrow(new RuntimeException("Database error"));
            when(dailyCandleService.saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString()))
                .thenReturn(CompletableFuture.completedFuture(createMockSaveResponse()));
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/candles/daily")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"instruments\":[\"BBG004730N88\"],\"assetType\":[\"SHARES\"]}"))
                    .andExpect(status().isAccepted())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.taskId").exists());
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(dailyCandleService).saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString());
        });
    }

    @Test
    @DisplayName("Обработка ошибки сервиса при асинхронной загрузке")
    @Description("Тест проверяет поведение контроллера при ошибке DailyCandleService")
    @Severity(SeverityLevel.NORMAL)
    @Story("Daily Candles API")
    @Tag("api")
    @Tag("daily-candles")
    @Tag("error-handling")
    @Tag("service-error")
    @Tag("unit")
    void loadDailyCandlesTodayAsync_ShouldHandleServiceError_WhenDailyCandleServiceThrowsException() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            when(systemLogRepository.save(any())).thenReturn(new SystemLogEntity());
            when(dailyCandleService.saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString()))
                .thenThrow(new RuntimeException("Service error"));
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/candles/daily")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"instruments\":[\"BBG004730N88\"],\"assetType\":[\"SHARES\"]}"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.status").value("ERROR"));
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(dailyCandleService).saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString());
        });
    }

    @Test
    @DisplayName("Обработка ошибки репозитория при получении акций")
    @Description("Тест проверяет поведение контроллера при ошибке ShareRepository")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares Daily Candles API")
    @Tag("api")
    @Tag("shares")
    @Tag("daily-candles")
    @Tag("error-handling")
    @Tag("repository-error")
    @Tag("unit")
    void getSharesDailyCandlesForDate_ShouldHandleRepositoryError_WhenShareRepositoryThrowsException() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            when(systemLogRepository.save(any())).thenReturn(new SystemLogEntity());
            when(shareRepository.findAll()).thenThrow(new RuntimeException("Database connection error"));
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/candles/daily/shares/{date}", testDate)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.date").value(testDate.toString()))
                    .andExpect(jsonPath("$.assetType").value("SHARES"));
        });

        // Шаг 3: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(shareRepository).findAll();
        });
    }

}
