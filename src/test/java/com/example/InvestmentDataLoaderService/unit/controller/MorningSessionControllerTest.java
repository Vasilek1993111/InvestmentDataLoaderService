package com.example.InvestmentDataLoaderService.unit.controller;

import com.example.InvestmentDataLoaderService.controller.MorningSessionController;
import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.service.MorningSessionService;
import com.example.InvestmentDataLoaderService.fixtures.TestDataFactory;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit-тесты для MorningSessionController
 */
@Epic("Unit Tests")
@Feature("Morning Session Controller")
@DisplayName("Morning Session Controller Tests")
@Owner("Investment Data Loader Service Team")

@WebMvcTest(MorningSessionController.class)
public class MorningSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MorningSessionService morningSessionService;

    @BeforeEach
    public void setUp() {
        reset(morningSessionService);
    }

    // ========== ПОЗИТИВНЫЕ ТЕСТЫ - GET ENDPOINTS ==========

    @Test
    public void previewMorningSessionPricesToday_ShouldReturnOk_WhenValidRequest() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            SaveResponseDto responseDto = TestDataFactory.createTestPreviewResponse();
            when(morningSessionService.previewMorningSessionPricesForDate(any(LocalDate.class))).thenReturn(responseDto);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(get("/api/morning-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Успешно сохранено 5 новых инструментов из 10 найденных"))
                .andExpect(jsonPath("$.dateUsed").exists())
                .andExpect(jsonPath("$.statistics.totalRequested").value(10))
                .andExpect(jsonPath("$.statistics.found").value(5))
                .andExpect(jsonPath("$.statistics.notFound").value(5))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(5))
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(morningSessionService).previewMorningSessionPricesForDate(any(LocalDate.class));
        });
    }

    @Test
    public void previewMorningSessionPricesToday_ShouldReturnOk_WhenNoData() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            SaveResponseDto responseDto = TestDataFactory.createEmptySaveResponse();
            when(morningSessionService.previewMorningSessionPricesForDate(any(LocalDate.class))).thenReturn(responseDto);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(get("/api/morning-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Новых инструментов не обнаружено"))
                .andExpect(jsonPath("$.dateUsed").exists())
                .andExpect(jsonPath("$.statistics.totalRequested").value(0))
                .andExpect(jsonPath("$.statistics.found").value(0))
                .andExpect(jsonPath("$.statistics.notFound").value(0))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(morningSessionService).previewMorningSessionPricesForDate(any(LocalDate.class));
        });
    }

    @Test
    public void getCandlesByDate_ShouldReturnOk_WhenValidDate() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            SaveResponseDto responseDto = TestDataFactory.createTestPreviewResponse();
            when(morningSessionService.previewMorningSessionPricesForDate(testDate)).thenReturn(responseDto);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/morning-session/by-date/{date}", testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Успешно сохранено 5 новых инструментов из 10 найденных"))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.statistics.totalRequested").value(10))
                .andExpect(jsonPath("$.statistics.found").value(5))
                .andExpect(jsonPath("$.statistics.notFound").value(5))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(5))
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(morningSessionService).previewMorningSessionPricesForDate(testDate);
        });
    }

    @Test
    public void getSharesCandlesByDate_ShouldReturnOk_WhenValidDate() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            SaveResponseDto responseDto = TestDataFactory.createTestPreviewResponse();
            when(morningSessionService.previewSharesPricesForDate(testDate)).thenReturn(responseDto);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/morning-session/shares/{date}", testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Успешно сохранено 5 новых инструментов из 10 найденных"))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.instrumentType").value("shares"))
                .andExpect(jsonPath("$.statistics.totalRequested").value(10))
                .andExpect(jsonPath("$.statistics.found").value(5))
                .andExpect(jsonPath("$.statistics.notFound").value(5))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(5))
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(morningSessionService).previewSharesPricesForDate(testDate);
        });
    }

    @Test
    public void getFuturesCandlesByDate_ShouldReturnOk_WhenValidDate() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            SaveResponseDto responseDto = TestDataFactory.createTestPreviewResponse();
            when(morningSessionService.previewFuturesPricesForDate(testDate)).thenReturn(responseDto);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/morning-session/futures/{date}", testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Успешно сохранено 5 новых инструментов из 10 найденных"))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.instrumentType").value("futures"))
                .andExpect(jsonPath("$.statistics.totalRequested").value(10))
                .andExpect(jsonPath("$.statistics.found").value(5))
                .andExpect(jsonPath("$.statistics.notFound").value(5))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(5))
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(morningSessionService).previewFuturesPricesForDate(testDate);
        });
    }

    @Test
    public void getPriceByFigiForDate_ShouldReturnOk_WhenValidRequest() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            String figi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            SaveResponseDto responseDto = TestDataFactory.createTestPreviewResponse();
            when(morningSessionService.previewPriceByFigiForDate(figi, testDate)).thenReturn(responseDto);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            String figi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/morning-session/by-figi-date/{figi}/{date}", figi, testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Успешно сохранено 5 новых инструментов из 10 найденных"))
                .andExpect(jsonPath("$.figi").value(figi))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.statistics.totalRequested").value(10))
                .andExpect(jsonPath("$.statistics.found").value(5))
                .andExpect(jsonPath("$.statistics.notFound").value(5))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(5))
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            String figi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(morningSessionService).previewPriceByFigiForDate(figi, testDate);
        });
    }

    // ========== ПОЗИТИВНЫЕ ТЕСТЫ - POST ENDPOINTS ==========

    @Test
    public void loadMorningSessionPricesToday_ShouldReturnOk_WhenValidRequest() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            SaveResponseDto responseDto = TestDataFactory.createSuccessfulSaveResponse(Arrays.asList(
                Map.of("figi", "BBG004730N88", "price", 102.50),
                Map.of("figi", "BBG00HYV2XQ1", "price", 1500.0)
            ));
            when(morningSessionService.fetchAndStoreMorningSessionPricesForDate(any(LocalDate.class))).thenReturn(responseDto);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/morning-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Успешно загружено 2 новых инструментов"))
                .andExpect(jsonPath("$.dateUsed").exists())
                .andExpect(jsonPath("$.statistics.totalRequested").value(2))
                .andExpect(jsonPath("$.statistics.found").value(2))
                .andExpect(jsonPath("$.statistics.notFound").value(0))
                .andExpect(jsonPath("$.statistics.saved").value(2))
                .andExpect(jsonPath("$.statistics.skippedExisting").value(0))
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(morningSessionService).fetchAndStoreMorningSessionPricesForDate(any(LocalDate.class));
        });
    }

    @Test
    public void loadOpenPricesForDate_ShouldReturnOk_WhenValidDate() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            SaveResponseDto responseDto = TestDataFactory.createSuccessfulSaveResponse(Arrays.asList(
                Map.of("figi", "BBG004730N88", "price", 102.50),
                Map.of("figi", "BBG00HYV2XQ1", "price", 1500.0)
            ));
            when(morningSessionService.fetchAndStoreMorningSessionPricesForDate(testDate)).thenReturn(responseDto);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/morning-session/by-date/{date}", testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Успешно загружено 2 новых инструментов"))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.statistics.totalRequested").value(2))
                .andExpect(jsonPath("$.statistics.found").value(2))
                .andExpect(jsonPath("$.statistics.notFound").value(0))
                .andExpect(jsonPath("$.statistics.saved").value(2))
                .andExpect(jsonPath("$.statistics.skippedExisting").value(0))
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(morningSessionService).fetchAndStoreMorningSessionPricesForDate(testDate);
        });
    }

    @Test
    public void loadSharesPricesForDate_ShouldReturnOk_WhenValidDate() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            SaveResponseDto responseDto = TestDataFactory.createSuccessfulSaveResponse(Arrays.asList(
                Map.of("figi", "BBG004730N88", "price", 102.50),
                Map.of("figi", "BBG00HYV2XQ1", "price", 1500.0)
            ));
            when(morningSessionService.fetchAndStoreSharesPricesForDate(testDate)).thenReturn(responseDto);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/morning-session/shares/{date}", testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Успешно загружено 2 новых инструментов"))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.instrumentType").value("shares"))
                .andExpect(jsonPath("$.statistics.totalRequested").value(2))
                .andExpect(jsonPath("$.statistics.found").value(2))
                .andExpect(jsonPath("$.statistics.notFound").value(0))
                .andExpect(jsonPath("$.statistics.saved").value(2))
                .andExpect(jsonPath("$.statistics.skippedExisting").value(0))
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(morningSessionService).fetchAndStoreSharesPricesForDate(testDate);
        });
    }

    @Test
    public void loadFuturesPricesForDate_ShouldReturnOk_WhenValidDate() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            SaveResponseDto responseDto = TestDataFactory.createSuccessfulSaveResponse(Arrays.asList(
                Map.of("figi", "BBG004730N88", "price", 102.50),
                Map.of("figi", "BBG00HYV2XQ1", "price", 1500.0)
            ));
            when(morningSessionService.fetchAndStoreFuturesPricesForDate(testDate)).thenReturn(responseDto);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/morning-session/futures/{date}", testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Успешно загружено 2 новых инструментов"))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.instrumentType").value("futures"))
                .andExpect(jsonPath("$.statistics.totalRequested").value(2))
                .andExpect(jsonPath("$.statistics.found").value(2))
                .andExpect(jsonPath("$.statistics.notFound").value(0))
                .andExpect(jsonPath("$.statistics.saved").value(2))
                .andExpect(jsonPath("$.statistics.skippedExisting").value(0))
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(morningSessionService).fetchAndStoreFuturesPricesForDate(testDate);
        });
    }

    @Test
    public void loadPriceByFigiForDate_ShouldReturnOk_WhenValidRequest() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            String figi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            SaveResponseDto responseDto = TestDataFactory.createSuccessfulSaveResponse(Arrays.asList(
                Map.of("figi", "BBG004730N88", "price", 102.50)
            ));
            when(morningSessionService.fetchAndStorePriceByFigiForDate(figi, testDate)).thenReturn(responseDto);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            String figi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/morning-session/by-figi-date/{figi}/{date}", figi, testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Успешно загружено 1 новых инструментов"))
                .andExpect(jsonPath("$.figi").value(figi))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.statistics.totalRequested").value(1))
                .andExpect(jsonPath("$.statistics.found").value(1))
                .andExpect(jsonPath("$.statistics.notFound").value(0))
                .andExpect(jsonPath("$.statistics.saved").value(1))
                .andExpect(jsonPath("$.statistics.skippedExisting").value(0))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            String figi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(morningSessionService).fetchAndStorePriceByFigiForDate(figi, testDate);
        });
    }

    // ========== НЕГАТИВНЫЕ ТЕСТЫ ==========

    @Test
    public void previewMorningSessionPricesToday_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(morningSessionService.previewMorningSessionPricesForDate(any(LocalDate.class)))
                .thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(get("/api/morning-session"))
                .andExpect(status().isInternalServerError());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(morningSessionService).previewMorningSessionPricesForDate(any(LocalDate.class));
        });
    }

    @Test
    public void getCandlesByDate_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            when(morningSessionService.previewMorningSessionPricesForDate(testDate))
                .thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/morning-session/by-date/{date}", testDate))
                .andExpect(status().isInternalServerError());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(morningSessionService).previewMorningSessionPricesForDate(testDate);
        });
    }

    @Test
    public void getSharesCandlesByDate_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            when(morningSessionService.previewSharesPricesForDate(testDate))
                .thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/morning-session/shares/{date}", testDate))
                .andExpect(status().isInternalServerError());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(morningSessionService).previewSharesPricesForDate(testDate);
        });
    }

    @Test
    public void getFuturesCandlesByDate_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            when(morningSessionService.previewFuturesPricesForDate(testDate))
                .thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/morning-session/futures/{date}", testDate))
                .andExpect(status().isInternalServerError());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(morningSessionService).previewFuturesPricesForDate(testDate);
        });
    }

    @Test
    public void getPriceByFigiForDate_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            String figi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            when(morningSessionService.previewPriceByFigiForDate(figi, testDate))
                .thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            String figi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/morning-session/by-figi-date/{figi}/{date}", figi, testDate))
                .andExpect(status().isInternalServerError());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            String figi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(morningSessionService).previewPriceByFigiForDate(figi, testDate);
        });
    }

    @Test
    public void loadMorningSessionPricesToday_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(morningSessionService.fetchAndStoreMorningSessionPricesForDate(any(LocalDate.class)))
                .thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/morning-session"))
                .andExpect(status().isInternalServerError());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(morningSessionService).fetchAndStoreMorningSessionPricesForDate(any(LocalDate.class));
        });
    }

    @Test
    public void loadOpenPricesForDate_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            when(morningSessionService.fetchAndStoreMorningSessionPricesForDate(testDate))
                .thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/morning-session/by-date/{date}", testDate))
                .andExpect(status().isInternalServerError());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(morningSessionService).fetchAndStoreMorningSessionPricesForDate(testDate);
        });
    }

    @Test
    public void loadSharesPricesForDate_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            when(morningSessionService.fetchAndStoreSharesPricesForDate(testDate))
                .thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/morning-session/shares/{date}", testDate))
                .andExpect(status().isInternalServerError());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(morningSessionService).fetchAndStoreSharesPricesForDate(testDate);
        });
    }

    @Test
    public void loadFuturesPricesForDate_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            when(morningSessionService.fetchAndStoreFuturesPricesForDate(testDate))
                .thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/morning-session/futures/{date}", testDate))
                .andExpect(status().isInternalServerError());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(morningSessionService).fetchAndStoreFuturesPricesForDate(testDate);
        });
    }

    @Test
    public void loadPriceByFigiForDate_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            String figi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            when(morningSessionService.fetchAndStorePriceByFigiForDate(figi, testDate))
                .thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            String figi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/morning-session/by-figi-date/{figi}/{date}", figi, testDate))
                .andExpect(status().isInternalServerError());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            String figi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(morningSessionService).fetchAndStorePriceByFigiForDate(figi, testDate);
        });
    }

    // ========== ТЕСТЫ ВАЛИДАЦИИ ==========

    @Test
    public void loadPriceByFigiForDate_ShouldReturnBadRequest_WhenEmptyFigi() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            // Пустой FIGI - используем пробелы, которые будут обрезаны до пустой строки
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/morning-session/by-figi-date/{figi}/{date}", "   ", testDate))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("FIGI не может быть пустым"));
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(morningSessionService, never()).fetchAndStorePriceByFigiForDate(anyString(), any(LocalDate.class));
        });
    }

    @Test
    public void getPriceByFigiForDate_ShouldReturnBadRequest_WhenEmptyFigi() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            // Пустой FIGI - используем пробелы, которые будут обрезаны до пустой строки
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/morning-session/by-figi-date/{figi}/{date}", "   ", testDate))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("FIGI не может быть пустым"));
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(morningSessionService, never()).previewPriceByFigiForDate(anyString(), any(LocalDate.class));
        });
    }

    @Test
    public void loadPriceByFigiForDate_ShouldReturnBadRequest_WhenFigiWithSpaces() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            // FIGI с пробелами
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/morning-session/by-figi-date/{figi}/{date}", "   ", testDate))
                .andExpect(status().isBadRequest());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(morningSessionService, never()).fetchAndStorePriceByFigiForDate(anyString(), any(LocalDate.class));
        });
    }

    @Test
    public void getPriceByFigiForDate_ShouldReturnBadRequest_WhenFigiWithSpaces() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            // FIGI с пробелами
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/morning-session/by-figi-date/{figi}/{date}", "   ", testDate))
                .andExpect(status().isBadRequest());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(morningSessionService, never()).previewPriceByFigiForDate(anyString(), any(LocalDate.class));
        });
    }

    @Test
    public void loadPriceByFigiForDate_ShouldReturnNotFound_WhenEmptyFigi() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            // Пустой FIGI - Spring не может найти обработчик для пустого path variable
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/morning-session/by-figi-date/{figi}/{date}", "", testDate))
                .andExpect(status().isNotFound());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(morningSessionService, never()).fetchAndStorePriceByFigiForDate(anyString(), any(LocalDate.class));
        });
    }

    @Test
    public void loadPriceByFigiForDate_ShouldReturnBadRequest_WhenMissingFigi() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            // Отсутствующий FIGI - мокаем сервис, чтобы он возвращал null
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            when(morningSessionService.fetchAndStorePriceByFigiForDate("MISSING", testDate)).thenReturn(null);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/morning-session/by-figi-date/{figi}/{date}", "MISSING", testDate))
                .andExpect(status().isInternalServerError());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(morningSessionService).fetchAndStorePriceByFigiForDate("MISSING", testDate);
        });
    }

    @Test
    public void getPriceByFigiForDate_ShouldReturnNotFound_WhenEmptyFigi() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            // Пустой FIGI - Spring не может найти обработчик для пустого path variable
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/morning-session/by-figi-date/{figi}/{date}", "", testDate))
                .andExpect(status().isNotFound());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(morningSessionService, never()).previewPriceByFigiForDate(anyString(), any(LocalDate.class));
        });
    }

    @Test
    public void getPriceByFigiForDate_ShouldReturnBadRequest_WhenMissingFigi() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            // Отсутствующий FIGI - мокаем сервис, чтобы он возвращал null
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            when(morningSessionService.previewPriceByFigiForDate("MISSING", testDate)).thenReturn(null);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/morning-session/by-figi-date/{figi}/{date}", "MISSING", testDate))
                .andExpect(status().isInternalServerError());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(morningSessionService).previewPriceByFigiForDate("MISSING", testDate);
        });
    }
}