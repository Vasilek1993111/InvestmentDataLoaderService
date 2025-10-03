package com.example.InvestmentDataLoaderService.unit.controller;

import com.example.InvestmentDataLoaderService.client.TinkoffApiClient;
import com.example.InvestmentDataLoaderService.controller.CandlesInstrumentController;
import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;
import com.example.InvestmentDataLoaderService.repository.*;
import com.example.InvestmentDataLoaderService.service.MinuteCandleService;
import com.example.InvestmentDataLoaderService.service.DailyCandleService;
import com.example.InvestmentDataLoaderService.fixtures.TestDataFactory;

import io.qameta.allure.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit-тесты для CandlesInstrumentController
 */
@WebMvcTest(CandlesInstrumentController.class)
@Epic("Instrument Candles API")
@Feature("Instrument Candles Management")
@DisplayName("Candles Instrument Controller Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
public class CandlesInstrumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MinuteCandleService minuteCandleService;
    @MockitoBean
    private DailyCandleService dailyCandleService;
    @MockitoBean
    private TinkoffApiClient tinkoffApiClient;
    @MockitoBean
    private SystemLogRepository systemLogRepository;

    @BeforeEach
    @Step("Подготовка тестовых данных для CandlesInstrumentController")
    public void setUp() {
        reset(minuteCandleService, dailyCandleService, tinkoffApiClient, systemLogRepository);
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    // ========== ПОЗИТИВНЫЕ ТЕСТЫ ДЛЯ МИНУТНЫХ СВЕЧЕЙ ==========

    @Test
    @DisplayName("Получение минутных свечей инструмента за дату - успешный случай")
    @Description("Тест проверяет успешное получение минутных свечей конкретного инструмента за дату")
    @Story("Minute Candles Retrieval")
    @Tag("positive")
    @Tag("retrieval")
    @Tag("minute-candles")
    void getInstrumentMinuteCandlesForDate_ShouldReturnOk_WhenValidRequest() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
            String figi = "BBG004730N88";
            LocalDate date = LocalDate.of(2024, 1, 15);
            List<CandleDto> candles = TestDataFactory.createCandleDtoList();

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
            when(tinkoffApiClient.getCandles(figi, date, "CANDLE_INTERVAL_1_MIN")).thenReturn(candles);

            // When & Then
            mockMvc.perform(get("/api/candles/instrument/minute/{figi}/{date}", figi, date)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.figi").value(figi))
                    .andExpect(jsonPath("$.date").value(date.toString()))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").value(2))
                    .andExpect(jsonPath("$.totalVolume").exists())
                    .andExpect(jsonPath("$.averagePrice").exists());

            // Verify
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(tinkoffApiClient).getCandles(figi, date, "CANDLE_INTERVAL_1_MIN");
        });
    }

    @Test
    @DisplayName("Получение минутных свечей инструмента - пустой ответ")
    @Description("Тест проверяет обработку пустого ответа от API")
    @Story("Minute Candles Retrieval")
    @Tag("positive")
    @Tag("empty-response")
    @Tag("minute-candles")
    void getInstrumentMinuteCandlesForDate_ShouldReturnOk_WhenEmptyResponse() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
            String figi = "BBG004730N88";
            LocalDate date = LocalDate.of(2024, 1, 15);

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
            when(tinkoffApiClient.getCandles(figi, date, "CANDLE_INTERVAL_1_MIN")).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/candles/instrument/minute/{figi}/{date}", figi, date)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.figi").value(figi))
                    .andExpect(jsonPath("$.date").value(date.toString()))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").value(0))
                    .andExpect(jsonPath("$.message").value("Нет данных для инструмента " + figi + " за дату " + date));

            // Verify
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(tinkoffApiClient).getCandles(figi, date, "CANDLE_INTERVAL_1_MIN");
        });
    }

    @Test
    @DisplayName("Получение минутных свечей инструмента - null ответ")
    @Description("Тест проверяет обработку null ответа от API")
    @Story("Minute Candles Retrieval")
    @Tag("positive")
    @Tag("null-response")
    @Tag("minute-candles")
    void getInstrumentMinuteCandlesForDate_ShouldReturnOk_WhenNullResponse() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
            String figi = "BBG004730N88";
            LocalDate date = LocalDate.of(2024, 1, 15);

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
            when(tinkoffApiClient.getCandles(figi, date, "CANDLE_INTERVAL_1_MIN")).thenReturn(null);

            // When & Then
            mockMvc.perform(get("/api/candles/instrument/minute/{figi}/{date}", figi, date)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.figi").value(figi))
                    .andExpect(jsonPath("$.date").value(date.toString()))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").value(0))
                    .andExpect(jsonPath("$.message").value("Нет данных для инструмента " + figi + " за дату " + date));

            // Verify
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(tinkoffApiClient).getCandles(figi, date, "CANDLE_INTERVAL_1_MIN");
        });
    }

    @Test
    @DisplayName("Асинхронное сохранение минутных свечей инструмента - успешный случай")
    @Description("Тест проверяет успешный запуск асинхронного сохранения минутных свечей")
    @Story("Minute Candles Loading")
    @Tag("positive")
    @Tag("loading")
    @Tag("minute-candles")
    void saveInstrumentMinuteCandlesForDateAsync_ShouldReturnAccepted_WhenValidRequest() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
            String figi = "BBG004730N88";
            LocalDate date = LocalDate.of(2024, 1, 15);

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());

            // When & Then
            mockMvc.perform(post("/api/candles/instrument/minute/{figi}/{date}", figi, date)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isAccepted())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Сохранение минутных свечей инструмента " + figi + " за " + date + " запущено"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.endpoint").value("/api/candles/instrument/minute/" + figi + "/" + date))
                    .andExpect(jsonPath("$.figi").value(figi))
                    .andExpect(jsonPath("$.date").value(date.toString()))
                    .andExpect(jsonPath("$.status").value("STARTED"))
                    .andExpect(jsonPath("$.startTime").exists());

            // Verify
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(minuteCandleService).saveMinuteCandlesAsync(any(MinuteCandleRequestDto.class), anyString());
        });
    }

    // ========== ПОЗИТИВНЫЕ ТЕСТЫ ДЛЯ ДНЕВНЫХ СВЕЧЕЙ ==========

    @Test
    @DisplayName("Получение дневных свечей инструмента за дату - успешный случай")
    @Description("Тест проверяет успешное получение дневных свечей конкретного инструмента за дату")
    @Story("Daily Candles Retrieval")
    @Tag("positive")
    @Tag("retrieval")
    @Tag("daily-candles")
    void getInstrumentDailyCandlesForDate_ShouldReturnOk_WhenValidRequest() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
            String figi = "BBG004730N88";
            LocalDate date = LocalDate.of(2024, 1, 15);
            List<CandleDto> candles = TestDataFactory.createCandleDtoList();

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
            when(tinkoffApiClient.getCandles(figi, date, "CANDLE_INTERVAL_DAY")).thenReturn(candles);

            // When & Then
            mockMvc.perform(get("/api/candles/instrument/daily/{figi}/{date}", figi, date)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.figi").value(figi))
                    .andExpect(jsonPath("$.date").value(date.toString()))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").value(2))
                    .andExpect(jsonPath("$.totalVolume").exists())
                    .andExpect(jsonPath("$.averagePrice").exists());

            // Verify
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(tinkoffApiClient).getCandles(figi, date, "CANDLE_INTERVAL_DAY");
        });
    }

    @Test
    @DisplayName("Получение дневных свечей инструмента - пустой ответ")
    @Description("Тест проверяет обработку пустого ответа от API для дневных свечей")
    @Story("Daily Candles Retrieval")
    @Tag("positive")
    @Tag("empty-response")
    @Tag("daily-candles")
    void getInstrumentDailyCandlesForDate_ShouldReturnOk_WhenEmptyResponse() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
            String figi = "BBG004730N88";
            LocalDate date = LocalDate.of(2024, 1, 15);

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
            when(tinkoffApiClient.getCandles(figi, date, "CANDLE_INTERVAL_DAY")).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/candles/instrument/daily/{figi}/{date}", figi, date)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.figi").value(figi))
                    .andExpect(jsonPath("$.date").value(date.toString()))
                    .andExpect(jsonPath("$.candles").isArray())
                    .andExpect(jsonPath("$.totalCandles").value(0))
                    .andExpect(jsonPath("$.message").value("Нет данных для инструмента " + figi + " за дату " + date));

            // Verify
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(tinkoffApiClient).getCandles(figi, date, "CANDLE_INTERVAL_DAY");
        });
    }

    @Test
    @DisplayName("Асинхронное сохранение дневных свечей инструмента - успешный случай")
    @Description("Тест проверяет успешный запуск асинхронного сохранения дневных свечей")
    @Story("Daily Candles Loading")
    @Tag("positive")
    @Tag("loading")
    @Tag("daily-candles")
    void saveInstrumentDailyCandlesForDateAsync_ShouldReturnAccepted_WhenValidRequest() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
            String figi = "BBG004730N88";
            LocalDate date = LocalDate.of(2024, 1, 15);

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());

            // When & Then
            mockMvc.perform(post("/api/candles/instrument/daily/{figi}/{date}", figi, date)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isAccepted())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Сохранение дневных свечей инструмента " + figi + " за " + date + " запущено"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.endpoint").value("/api/candles/instrument/daily/" + figi + "/" + date))
                    .andExpect(jsonPath("$.figi").value(figi))
                    .andExpect(jsonPath("$.date").value(date.toString()))
                    .andExpect(jsonPath("$.status").value("STARTED"))
                    .andExpect(jsonPath("$.startTime").exists());

            // Verify
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(dailyCandleService).saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString());
        });
    }

    // ========== НЕГАТИВНЫЕ ТЕСТЫ ==========

    @Test
    @DisplayName("Получение минутных свечей инструмента - ошибка API")
    @Description("Тест проверяет обработку ошибки API при получении минутных свечей")
    @Story("Minute Candles Retrieval")
    @Tag("negative")
    @Tag("error-handling")
    @Tag("api-error")
    void getInstrumentMinuteCandlesForDate_ShouldReturnInternalServerError_WhenApiThrowsException() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
            String figi = "BBG004730N88";
            LocalDate date = LocalDate.of(2024, 1, 15);

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
            when(tinkoffApiClient.getCandles(figi, date, "CANDLE_INTERVAL_1_MIN"))
                .thenThrow(new RuntimeException("API error"));

            // When & Then
            mockMvc.perform(get("/api/candles/instrument/minute/{figi}/{date}", figi, date)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").value("Ошибка получения минутных свечей инструмента: API error"))
                    .andExpect(jsonPath("$.figi").value(figi))
                    .andExpect(jsonPath("$.date").value(date.toString()));

            // Verify
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(tinkoffApiClient).getCandles(figi, date, "CANDLE_INTERVAL_1_MIN");
        });
    }

    @Test
    @DisplayName("Асинхронное сохранение минутных свечей - ошибка сервиса")
    @Description("Тест проверяет обработку ошибки сервиса при асинхронном сохранении")
    @Story("Minute Candles Loading")
    @Tag("negative")
    @Tag("error-handling")
    @Tag("service-error")
    void saveInstrumentMinuteCandlesForDateAsync_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
            String figi = "BBG004730N88";
            LocalDate date = LocalDate.of(2024, 1, 15);

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
            doThrow(new RuntimeException("Service error")).when(minuteCandleService)
                .saveMinuteCandlesAsync(any(MinuteCandleRequestDto.class), anyString());

            // When & Then
            mockMvc.perform(post("/api/candles/instrument/minute/{figi}/{date}", figi, date)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Ошибка запуска асинхронного сохранения: Service error"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.figi").value(figi))
                    .andExpect(jsonPath("$.date").value(date.toString()))
                    .andExpect(jsonPath("$.status").value("ERROR"));

            // Verify
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(minuteCandleService).saveMinuteCandlesAsync(any(MinuteCandleRequestDto.class), anyString());
        });
    }

    @Test
    @DisplayName("Получение дневных свечей инструмента - ошибка API")
    @Description("Тест проверяет обработку ошибки API при получении дневных свечей")
    @Story("Daily Candles Retrieval")
    @Tag("negative")
    @Tag("error-handling")
    @Tag("api-error")
    void getInstrumentDailyCandlesForDate_ShouldReturnInternalServerError_WhenApiThrowsException() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
            String figi = "BBG004730N88";
            LocalDate date = LocalDate.of(2024, 1, 15);

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
            when(tinkoffApiClient.getCandles(figi, date, "CANDLE_INTERVAL_DAY"))
                .thenThrow(new RuntimeException("API error"));

            // When & Then
            mockMvc.perform(get("/api/candles/instrument/daily/{figi}/{date}", figi, date)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").value("Ошибка получения дневных свечей инструмента: API error"))
                    .andExpect(jsonPath("$.figi").value(figi))
                    .andExpect(jsonPath("$.date").value(date.toString()));

            // Verify
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(tinkoffApiClient).getCandles(figi, date, "CANDLE_INTERVAL_DAY");
        });
    }

    @Test
    @DisplayName("Асинхронное сохранение дневных свечей - ошибка сервиса")
    @Description("Тест проверяет обработку ошибки сервиса при асинхронном сохранении дневных свечей")
    @Story("Daily Candles Loading")
    @Tag("negative")
    @Tag("error-handling")
    @Tag("service-error")
    void saveInstrumentDailyCandlesForDateAsync_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
            String figi = "BBG004730N88";
            LocalDate date = LocalDate.of(2024, 1, 15);

            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
            doThrow(new RuntimeException("Service error")).when(dailyCandleService)
                .saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString());

            // When & Then
            mockMvc.perform(post("/api/candles/instrument/daily/{figi}/{date}", figi, date)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Ошибка запуска асинхронного сохранения: Service error"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.figi").value(figi))
                    .andExpect(jsonPath("$.date").value(date.toString()))
                    .andExpect(jsonPath("$.status").value("ERROR"));

            // Verify
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
            verify(dailyCandleService).saveDailyCandlesAsync(any(DailyCandleRequestDto.class), anyString());
        });
    }
}
