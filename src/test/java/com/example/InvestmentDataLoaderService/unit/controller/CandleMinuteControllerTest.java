package com.example.InvestmentDataLoaderService.unit.controller;

import com.example.InvestmentDataLoaderService.client.TinkoffApiClient;
import com.example.InvestmentDataLoaderService.controller.CandlesMinuteController;
import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;
import com.example.InvestmentDataLoaderService.repository.*;
import com.example.InvestmentDataLoaderService.service.MinuteCandleService;
import com.example.InvestmentDataLoaderService.fixtures.TestDataFactory;

import io.qameta.allure.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CandlesMinuteController.class)
@Epic("Minute Candles API")
@Feature("Minute Candles Management")
@DisplayName("Candles Minute Controller Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
public class CandleMinuteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MinuteCandleService minuteCandleService;
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
    @Step("Подготовка тестовых данных для CandlesMinuteController")
    public void setUp() {
        reset(minuteCandleService, tinkoffApiClient, shareRepository, futureRepository, 
              indicativeRepository, systemLogRepository);
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    // ========== ПОЗИТИВНЫЕ ТЕСТЫ ==========

    @Test
    @DisplayName("Загрузка минутных свечей за сегодня - успешный случай")
    @Description("Тест проверяет успешную загрузку минутных свечей за сегодняшний день")
    @Story("Minute Candles Loading")
    @Tag("positive")
    @Tag("loading")
    @Tag("today")
    void loadMinuteCandlesToday_ShouldReturnAccepted_WhenValidRequest() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId("test-task-123");
            log.setEndpoint("/api/candles/minute");
            log.setMethod("POST");
            log.setStatus("STARTED");
            log.setMessage("Test log message");
            log.setStartTime(Instant.now());
            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/candles/minute")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"instruments\":[\"BBG004730N88\",\"BBG004730ZJ29\"],\"assetType\":[\"SHARES\"],\"date\":\"2024-01-15\"}"))
                    .andExpect(status().isAccepted())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Загрузка минутных свечей за сегодня запущена"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.endpoint").value("/api/candles/minute"))
                    .andExpect(jsonPath("$.instruments").isArray())
                    .andExpect(jsonPath("$.assetTypes").isArray())
                    .andExpect(jsonPath("$.status").value("STARTED"))
                    .andExpect(jsonPath("$.startTime").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(minuteCandleService).saveMinuteCandlesAsync(any(MinuteCandleRequestDto.class), anyString());
        });
    }

    @Test
    @DisplayName("Загрузка минутных свечей за конкретную дату - успешный случай")
    @Description("Тест проверяет успешную загрузку минутных свечей за указанную дату")
    @Story("Minute Candles Loading")
    @Tag("positive")
    @Tag("loading")
    @Tag("specific-date")
    void loadMinuteCandlesForDate_ShouldReturnAccepted_WhenValidRequest() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId("test-task-123");
            log.setEndpoint("/api/candles/minute/2024-01-15");
            log.setMethod("POST");
            log.setStatus("STARTED");
            log.setMessage("Test log message");
            log.setStartTime(Instant.now());
            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/candles/minute/2024-01-15")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"instruments\":[\"BBG004730N88\",\"BBG004730ZJ29\"],\"assetType\":[\"SHARES\"],\"date\":\"2024-01-15\"}"))
                    .andExpect(status().isAccepted())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Загрузка минутных свечей за 2024-01-15 запущена"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.endpoint").value("/api/candles/minute/2024-01-15"))
                    .andExpect(jsonPath("$.date").value("2024-01-15"))
                    .andExpect(jsonPath("$.status").value("STARTED"));
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(minuteCandleService).saveMinuteCandlesAsync(any(MinuteCandleRequestDto.class), anyString());
        });
    }

    @Test
    @DisplayName("Получение минутных свечей акций за дату - успешный случай")
    @Description("Тест проверяет успешное получение минутных свечей акций за указанную дату")
    @Story("Minute Candles Retrieval")
    @Tag("positive")
    @Tag("retrieval")
    @Tag("shares")
    void getSharesMinuteCandlesForDate_ShouldReturnOk_WhenValidDate() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate date = LocalDate.of(2024, 1, 15);
            List<ShareEntity> shares = TestDataFactory.createShareEntityList();
            List<CandleDto> candles = TestDataFactory.createCandleDtoList();

            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId("test-task-123");
            log.setEndpoint("/api/candles/minute/shares/2024-01-15");
            log.setMethod("GET");
            log.setStatus("STARTED");
            log.setMessage("Test log message");
            log.setStartTime(Instant.now());

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
            when(shareRepository.findAll()).thenReturn(shares);
            when(tinkoffApiClient.getCandles(anyString(), eq(date), eq("CANDLE_INTERVAL_1_MIN")))
                .thenReturn(candles);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(get("/api/candles/minute/shares/2024-01-15")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.date").value("2024-01-15"))
                    .andExpect(jsonPath("$.assetType").value("SHARES"))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").exists())
                    .andExpect(jsonPath("$.totalInstruments").value(3))
                    .andExpect(jsonPath("$.processedInstruments").exists())
                    .andExpect(jsonPath("$.successfulInstruments").exists())
                    .andExpect(jsonPath("$.noDataInstruments").exists())
                    .andExpect(jsonPath("$.errorInstruments").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(shareRepository).findAll();
            verify(tinkoffApiClient, atLeastOnce()).getCandles(anyString(), eq(LocalDate.of(2024, 1, 15)), eq("CANDLE_INTERVAL_1_MIN"));
        });
    }

    @Test
    @DisplayName("Загрузка минутных свечей акций за дату - успешный случай")
    @Description("Тест проверяет успешную загрузку минутных свечей акций за указанную дату")
    @Story("Minute Candles Loading")
    @Tag("positive")
    @Tag("loading")
    @Tag("shares")
    void loadSharesMinuteCandlesForDate_ShouldReturnAccepted_WhenValidDate() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            List<ShareEntity> shares = TestDataFactory.createShareEntityList();

            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId("test-task-123");
            log.setEndpoint("/api/candles/minute/shares/2024-01-15");
            log.setMethod("POST");
            log.setStatus("STARTED");
            log.setMessage("Test log message");
            log.setStartTime(Instant.now());

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
            when(shareRepository.findAll()).thenReturn(shares);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/candles/minute/shares/2024-01-15")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isAccepted())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Загрузка минутных свечей акций запущена"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.endpoint").value("/api/candles/minute/shares/2024-01-15"))
                    .andExpect(jsonPath("$.date").value("2024-01-15"))
                    .andExpect(jsonPath("$.instrumentsCount").value(3))
                    .andExpect(jsonPath("$.status").value("STARTED"));
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(shareRepository).findAll();
            verify(minuteCandleService).saveMinuteCandlesAsync(any(MinuteCandleRequestDto.class), anyString());
        });
    }

    @Test
    @DisplayName("Загрузка минутных свечей фьючерсов за дату - успешный случай")
    @Description("Тест проверяет успешную загрузку минутных свечей фьючерсов за указанную дату")
    @Story("Minute Candles Loading")
    @Tag("positive")
    @Tag("loading")
    @Tag("futures")
    void loadFuturesMinuteCandlesForDate_ShouldReturnAccepted_WhenValidDate() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            List<FutureEntity> futures = TestDataFactory.createFutureEntityList();

            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId("test-task-123");
            log.setEndpoint("/api/candles/minute/futures/2024-01-15");
            log.setMethod("POST");
            log.setStatus("STARTED");
            log.setMessage("Test log message");
            log.setStartTime(Instant.now());

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
            when(futureRepository.findAll()).thenReturn(futures);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/candles/minute/futures/2024-01-15")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isAccepted())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Загрузка минутных свечей фьючерсов за 2024-01-15 запущена"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.endpoint").value("/api/candles/minute/futures/2024-01-15"))
                    .andExpect(jsonPath("$.date").value("2024-01-15"))
                    .andExpect(jsonPath("$.instrumentsCount").value(3))
                    .andExpect(jsonPath("$.status").value("STARTED"));
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(futureRepository).findAll();
            verify(minuteCandleService).saveMinuteCandlesAsync(any(MinuteCandleRequestDto.class), anyString());
        });
    }

    @Test
    @DisplayName("Получение минутных свечей фьючерсов за дату - успешный случай")
    @Description("Тест проверяет успешное получение минутных свечей фьючерсов за указанную дату")
    @Story("Minute Candles Retrieval")
    @Tag("positive")
    @Tag("retrieval")
    @Tag("futures")
    void getFuturesMinuteCandlesForDate_ShouldReturnOk_WhenValidDate() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate date = LocalDate.of(2024, 1, 15);
            List<FutureEntity> futures = TestDataFactory.createFutureEntityList();
            List<CandleDto> candles = TestDataFactory.createCandleDtoList();

            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId("test-task-123");
            log.setEndpoint("/api/candles/minute/futures/2024-01-15");
            log.setMethod("GET");
            log.setStatus("STARTED");
            log.setMessage("Test log message");
            log.setStartTime(Instant.now());

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
            when(futureRepository.findAll()).thenReturn(futures);
            when(tinkoffApiClient.getCandles(anyString(), eq(date), eq("CANDLE_INTERVAL_1_MIN")))
                .thenReturn(candles);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(get("/api/candles/minute/futures/2024-01-15")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.date").value("2024-01-15"))
                    .andExpect(jsonPath("$.assetType").value("FUTURES"))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").exists())
                    .andExpect(jsonPath("$.totalInstruments").value(3));
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(futureRepository).findAll();
            verify(tinkoffApiClient, atLeastOnce()).getCandles(anyString(), eq(LocalDate.of(2024, 1, 15)), eq("CANDLE_INTERVAL_1_MIN"));
        });
    }

    @Test
    @DisplayName("Загрузка минутных свечей индикативов за дату - успешный случай")
    @Description("Тест проверяет успешную загрузку минутных свечей индикативов за указанную дату")
    @Story("Minute Candles Loading")
    @Tag("positive")
    @Tag("loading")
    @Tag("indicatives")
    void loadIndicativesMinuteCandlesForDate_ShouldReturnAccepted_WhenValidDate() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            List<IndicativeEntity> indicatives = TestDataFactory.createIndicativeEntityList();

            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId("test-task-123");
            log.setEndpoint("/api/candles/minute/indicatives/2024-01-15");
            log.setMethod("POST");
            log.setStatus("STARTED");
            log.setMessage("Test log message");
            log.setStartTime(Instant.now());

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
            when(indicativeRepository.findAll()).thenReturn(indicatives);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/candles/minute/indicatives/2024-01-15")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isAccepted())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Загрузка минутных свечей индикативов за 2024-01-15 запущена"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.endpoint").value("/api/candles/minute/indicatives/2024-01-15"))
                    .andExpect(jsonPath("$.date").value("2024-01-15"))
                    .andExpect(jsonPath("$.instrumentsCount").value(3))
                    .andExpect(jsonPath("$.status").value("STARTED"));
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(indicativeRepository).findAll();
            verify(minuteCandleService).saveMinuteCandlesAsync(any(MinuteCandleRequestDto.class), anyString());
        });
    }

    @Test
    @DisplayName("Получение минутных свечей индикативов за дату - успешный случай")
    @Description("Тест проверяет успешное получение минутных свечей индикативов за указанную дату")
    @Story("Minute Candles Retrieval")
    @Tag("positive")
    @Tag("retrieval")
    @Tag("indicatives")
    void getIndicativesMinuteCandlesForDate_ShouldReturnOk_WhenValidDate() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate date = LocalDate.of(2024, 1, 15);
            List<IndicativeEntity> indicatives = TestDataFactory.createIndicativeEntityList();
            List<CandleDto> candles = TestDataFactory.createCandleDtoList();

            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId("test-task-123");
            log.setEndpoint("/api/candles/minute/indicatives/2024-01-15");
            log.setMethod("GET");
            log.setStatus("STARTED");
            log.setMessage("Test log message");
            log.setStartTime(Instant.now());

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
            when(indicativeRepository.findAll()).thenReturn(indicatives);
            when(tinkoffApiClient.getCandles(anyString(), eq(date), eq("CANDLE_INTERVAL_1_MIN")))
                .thenReturn(candles);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(get("/api/candles/minute/indicatives/2024-01-15")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.date").value("2024-01-15"))
                    .andExpect(jsonPath("$.assetType").value("INDICATIVES"))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").exists())
                    .andExpect(jsonPath("$.totalInstruments").value(3));
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(indicativeRepository).findAll();
            verify(tinkoffApiClient, atLeastOnce()).getCandles(anyString(), eq(LocalDate.of(2024, 1, 15)), eq("CANDLE_INTERVAL_1_MIN"));
        });
    }

    // ========== НЕГАТИВНЫЕ ТЕСТЫ ==========

    @Test
    @DisplayName("Загрузка минутных свечей за сегодня - ошибка сервиса")
    @Description("Тест проверяет обработку ошибки сервиса при загрузке минутных свечей")
    @Story("Minute Candles Loading")
    @Tag("negative")
    @Tag("error-handling")
    void loadMinuteCandlesToday_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId("test-task-123");
            log.setEndpoint("/api/candles/minute");
            log.setMethod("POST");
            log.setStatus("STARTED");
            log.setMessage("Test log message");
            log.setStartTime(Instant.now());

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
            doThrow(new RuntimeException("Service error")).when(minuteCandleService)
                .saveMinuteCandlesAsync(any(MinuteCandleRequestDto.class), anyString());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/candles/minute")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"instruments\":[\"BBG004730N88\"],\"assetType\":[\"SHARES\"],\"date\":\"2024-01-15\"}"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Ошибка запуска загрузки: Service error"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.status").value("ERROR"));
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(minuteCandleService).saveMinuteCandlesAsync(any(MinuteCandleRequestDto.class), anyString());
        });
    }

    @Test
    @DisplayName("Загрузка минутных свечей за дату - некорректная дата")
    @Description("Тест проверяет обработку некорректной даты")
    @Story("Minute Candles Loading")
    @Tag("negative")
    @Tag("validation")
    @Tag("date")
    void loadMinuteCandlesForDate_ShouldReturnBadRequest_WhenInvalidDate() throws Exception {
        Allure.step("Выполнение HTTP запроса с некорректной датой", () -> {
            mockMvc.perform(post("/api/candles/minute/invalid-date")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"instruments\":[\"BBG004730N88\"],\"assetType\":[\"SHARES\"]}"))
                    .andExpect(status().isBadRequest());
        });
    }

    @Test
    @DisplayName("Получение минутных свечей акций - ошибка API")
    @Description("Тест проверяет обработку ошибки API при получении минутных свечей акций")
    @Story("Minute Candles Retrieval")
    @Tag("negative")
    @Tag("error-handling")
    @Tag("api-error")
    void getSharesMinuteCandlesForDate_ShouldReturnOk_WhenApiThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate date = LocalDate.of(2024, 1, 15);
            List<ShareEntity> shares = TestDataFactory.createShareEntityList();

            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId("test-task-123");
            log.setEndpoint("/api/candles/minute/shares/2024-01-15");
            log.setMethod("GET");
            log.setStatus("STARTED");
            log.setMessage("Test log message");
            log.setStartTime(Instant.now());

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
            when(shareRepository.findAll()).thenReturn(shares);
            when(tinkoffApiClient.getCandles(anyString(), eq(date), eq("CANDLE_INTERVAL_1_MIN")))
                .thenThrow(new RuntimeException("API error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(get("/api/candles/minute/shares/2024-01-15")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.date").value("2024-01-15"))
                    .andExpect(jsonPath("$.assetType").value("SHARES"))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").value(0))
                    .andExpect(jsonPath("$.totalInstruments").value(3));
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(shareRepository).findAll();
            verify(tinkoffApiClient, atLeastOnce()).getCandles(anyString(), eq(LocalDate.of(2024, 1, 15)), eq("CANDLE_INTERVAL_1_MIN"));
        });
    }

    @Test
    @DisplayName("Получение минутных свечей акций - пустой ответ API")
    @Description("Тест проверяет обработку пустого ответа от API")
    @Story("Minute Candles Retrieval")
    @Tag("negative")
    @Tag("empty-response")
    @Tag("api")
    void getSharesMinuteCandlesForDate_ShouldReturnOk_WhenApiReturnsEmptyList() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate date = LocalDate.of(2024, 1, 15);
            List<ShareEntity> shares = TestDataFactory.createShareEntityList();

            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId("test-task-123");
            log.setEndpoint("/api/candles/minute/shares/2024-01-15");
            log.setMethod("GET");
            log.setStatus("STARTED");
            log.setMessage("Test log message");
            log.setStartTime(Instant.now());

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
            when(shareRepository.findAll()).thenReturn(shares);
            when(tinkoffApiClient.getCandles(anyString(), eq(date), eq("CANDLE_INTERVAL_1_MIN")))
                .thenReturn(Arrays.asList());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(get("/api/candles/minute/shares/2024-01-15")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.date").value("2024-01-15"))
                    .andExpect(jsonPath("$.assetType").value("SHARES"))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").value(0))
                    .andExpect(jsonPath("$.totalInstruments").value(3))
                    .andExpect(jsonPath("$.noDataInstruments").value(3));
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(shareRepository).findAll();
            verify(tinkoffApiClient, atLeastOnce()).getCandles(anyString(), eq(LocalDate.of(2024, 1, 15)), eq("CANDLE_INTERVAL_1_MIN"));
        });
    }

    @Test
    @DisplayName("Загрузка минутных свечей акций - ошибка базы данных")
    @Description("Тест проверяет обработку ошибки базы данных при загрузке минутных свечей акций")
    @Story("Minute Candles Loading")
    @Tag("negative")
    @Tag("error-handling")
    @Tag("database-error")
    void loadSharesMinuteCandlesForDate_ShouldReturnInternalServerError_WhenDatabaseError() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId("test-task-123");
            log.setEndpoint("/api/candles/minute/shares/2024-01-15");
            log.setMethod("POST");
            log.setStatus("STARTED");
            log.setMessage("Test log message");
            log.setStartTime(Instant.now());

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
            when(shareRepository.findAll()).thenThrow(new RuntimeException("Database error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/candles/minute/shares/2024-01-15")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Ошибка запуска загрузки: Database error"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.status").value("ERROR"));
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(shareRepository).findAll();
        });
    }

    @Test
    @DisplayName("Получение минутных свечей фьючерсов - некорректная дата")
    @Description("Тест проверяет обработку некорректной даты при получении минутных свечей фьючерсов")
    @Story("Minute Candles Retrieval")
    @Tag("negative")
    @Tag("validation")
    @Tag("date")
    void getFuturesMinuteCandlesForDate_ShouldReturnBadRequest_WhenInvalidDate() throws Exception {
        Allure.step("Выполнение HTTP запроса с некорректной датой", () -> {
            mockMvc.perform(get("/api/candles/minute/futures/invalid-date")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        });
    }

    @Test
    @DisplayName("Получение минутных свечей индикативов - некорректная дата")
    @Description("Тест проверяет обработку некорректной даты при получении минутных свечей индикативов")
    @Story("Minute Candles Retrieval")
    @Tag("negative")
    @Tag("validation")
    @Tag("date")
    void getIndicativesMinuteCandlesForDate_ShouldReturnBadRequest_WhenInvalidDate() throws Exception {
        Allure.step("Выполнение HTTP запроса с некорректной датой", () -> {
            mockMvc.perform(get("/api/candles/minute/indicatives/invalid-date")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        });
    }

    @Test
    @DisplayName("Загрузка минутных свечей фьючерсов - ошибка сервиса")
    @Description("Тест проверяет обработку ошибки сервиса при загрузке минутных свечей фьючерсов")
    @Story("Minute Candles Loading")
    @Tag("negative")
    @Tag("error-handling")
    @Tag("service-error")
    void loadFuturesMinuteCandlesForDate_ShouldReturnInternalServerError_WhenServiceError() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            List<FutureEntity> futures = TestDataFactory.createFutureEntityList();

            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId("test-task-123");
            log.setEndpoint("/api/candles/minute/futures/2024-01-15");
            log.setMethod("POST");
            log.setStatus("STARTED");
            log.setMessage("Test log message");
            log.setStartTime(Instant.now());

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
            when(futureRepository.findAll()).thenReturn(futures);
            doThrow(new RuntimeException("Service error")).when(minuteCandleService)
                .saveMinuteCandlesAsync(any(MinuteCandleRequestDto.class), anyString());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/candles/minute/futures/2024-01-15")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Ошибка запуска загрузки: Service error"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.status").value("ERROR"));
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(futureRepository).findAll();
            verify(minuteCandleService).saveMinuteCandlesAsync(any(MinuteCandleRequestDto.class), anyString());
        });
    }

    @Test
    @DisplayName("Загрузка минутных свечей индикативов - ошибка сервиса")
    @Description("Тест проверяет обработку ошибки сервиса при загрузке минутных свечей индикативов")
    @Story("Minute Candles Loading")
    @Tag("negative")
    @Tag("error-handling")
    @Tag("service-error")
    void loadIndicativesMinuteCandlesForDate_ShouldReturnInternalServerError_WhenServiceError() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            List<IndicativeEntity> indicatives = TestDataFactory.createIndicativeEntityList();

            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId("test-task-123");
            log.setEndpoint("/api/candles/minute/indicatives/2024-01-15");
            log.setMethod("POST");
            log.setStatus("STARTED");
            log.setMessage("Test log message");
            log.setStartTime(Instant.now());

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
            when(indicativeRepository.findAll()).thenReturn(indicatives);
            doThrow(new RuntimeException("Service error")).when(minuteCandleService)
                .saveMinuteCandlesAsync(any(MinuteCandleRequestDto.class), anyString());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/candles/minute/indicatives/2024-01-15")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Ошибка запуска загрузки: Service error"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.status").value("ERROR"));
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(indicativeRepository).findAll();
            verify(minuteCandleService).saveMinuteCandlesAsync(any(MinuteCandleRequestDto.class), anyString());
        });
    }

    @Test
    @DisplayName("Получение минутных свечей акций - будущая дата")
    @Description("Тест проверяет обработку будущей даты при получении минутных свечей акций")
    @Story("Minute Candles Retrieval")
    @Tag("negative")
    @Tag("validation")
    @Tag("future-date")
    void getSharesMinuteCandlesForDate_ShouldReturnOk_WhenFutureDate() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate futureDate = LocalDate.now().plusDays(1);
            List<ShareEntity> shares = TestDataFactory.createShareEntityList();

            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId("test-task-123");
            log.setEndpoint("/api/candles/minute/shares/" + futureDate);
            log.setMethod("GET");
            log.setStatus("STARTED");
            log.setMessage("Test log message");
            log.setStartTime(Instant.now());

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
            when(shareRepository.findAll()).thenReturn(shares);
            when(tinkoffApiClient.getCandles(anyString(), eq(futureDate), eq("CANDLE_INTERVAL_1_MIN")))
                .thenReturn(Arrays.asList()); // API не возвращает данных для будущих дат
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate futureDate = LocalDate.now().plusDays(1);
            mockMvc.perform(get("/api/candles/minute/shares/" + futureDate)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.date").value(futureDate.toString()))
                    .andExpect(jsonPath("$.assetType").value("SHARES"))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").value(0))
                    .andExpect(jsonPath("$.totalInstruments").value(3));
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            LocalDate futureDate = LocalDate.now().plusDays(1);
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(shareRepository).findAll();
            verify(tinkoffApiClient, atLeastOnce()).getCandles(anyString(), eq(futureDate), eq("CANDLE_INTERVAL_1_MIN"));
        });
    }

    @Test
    @DisplayName("Получение минутных свечей акций - пустая база данных")
    @Description("Тест проверяет обработку пустой базы данных при получении минутных свечей акций")
    @Story("Minute Candles Retrieval")
    @Tag("negative")
    @Tag("empty-database")
    void getSharesMinuteCandlesForDate_ShouldReturnOk_WhenEmptyDatabase() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            SystemLogEntity log = new SystemLogEntity();
            log.setTaskId("test-task-123");
            log.setEndpoint("/api/candles/minute/shares/2024-01-15");
            log.setMethod("GET");
            log.setStatus("STARTED");
            log.setMessage("Test log message");
            log.setStartTime(Instant.now());

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(log);
            when(shareRepository.findAll()).thenReturn(Arrays.asList());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(get("/api/candles/minute/shares/2024-01-15")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.date").value("2024-01-15"))
                    .andExpect(jsonPath("$.assetType").value("SHARES"))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").value(0))
                    .andExpect(jsonPath("$.totalInstruments").value(0));
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(shareRepository).findAll();
            verify(tinkoffApiClient, never()).getCandles(anyString(), any(), anyString());
        });
    }

    @Test
    @DisplayName("Логирование ошибок - ошибка сохранения лога")
    @Description("Тест проверяет обработку ошибки сохранения лога")
    @Story("Logging")
    @Tag("negative")
    @Tag("error-handling")
    @Tag("logging")
    void loadMinuteCandlesToday_ShouldStillWork_WhenLoggingFails() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(systemLogRepository.save(any(SystemLogEntity.class)))
                .thenThrow(new RuntimeException("Logging error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/candles/minute")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"instruments\":[\"BBG004730N88\"],\"assetType\":[\"SHARES\"],\"date\":\"2024-01-15\"}"))
                    .andExpect(status().isAccepted())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Загрузка минутных свечей за сегодня запущена"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.status").value("STARTED"));
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(minuteCandleService).saveMinuteCandlesAsync(any(MinuteCandleRequestDto.class), anyString());
        });
    }
}
