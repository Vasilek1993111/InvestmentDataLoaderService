package com.example.InvestmentDataLoaderService.unit.controller;

import com.example.InvestmentDataLoaderService.controller.MainSessionPricesController;
import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;
import com.example.InvestmentDataLoaderService.repository.*;
import com.example.InvestmentDataLoaderService.service.MainSessionPriceService;
import com.example.InvestmentDataLoaderService.service.InstrumentService;
import com.example.InvestmentDataLoaderService.exception.DataLoadException;
import com.example.InvestmentDataLoaderService.fixtures.TestDataFactory;

import io.qameta.allure.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit-тесты для MainSessionPricesController
 */
@WebMvcTest(MainSessionPricesController.class)
@Epic("Main Session Prices API")
@Feature("Main Session Prices Management")
@DisplayName("Main Session Prices Controller Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
public class MainSessionPriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MainSessionPriceService mainSessionPriceService;
    @MockitoBean
    private InstrumentService instrumentService;
    @MockitoBean
    private ShareRepository shareRepository;
    @MockitoBean
    private FutureRepository futureRepository;
    @MockitoBean
    private MinuteCandleRepository minuteCandleRepository;
    @MockitoBean
    private ClosePriceRepository closePriceRepository;

    @BeforeEach
    public void setUp() {
        reset(mainSessionPriceService);
        reset(instrumentService);
        reset(shareRepository);
        reset(futureRepository);
        reset(minuteCandleRepository);
        reset(closePriceRepository);
    }



    // ========== ПОЗИТИВНЫЕ ТЕСТЫ - GET ENDPOINTS ==========

    @Test
    public void getClosePricesForShares_ShouldReturnOk_WhenValidRequest() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            List<ClosePriceDto> closePrices = Arrays.asList(TestDataFactory.createClosePriceDto("BBG004730N88", "2024-01-15", BigDecimal.valueOf(102.50), BigDecimal.valueOf(102.50)));
            when(mainSessionPriceService.getClosePricesForAllShares()).thenReturn(closePrices);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(get("/api/main-session-prices/shares"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Цены закрытия для акций получены успешно"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(mainSessionPriceService).getClosePricesForAllShares();
        });
    }

    @Test
    public void getClosePricesForShares_ShouldReturnOk_WhenEmptyResponse() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(mainSessionPriceService.getClosePricesForAllShares()).thenReturn(Collections.emptyList());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(get("/api/main-session-prices/shares"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.count").value(0));
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(mainSessionPriceService).getClosePricesForAllShares();
        });
    }

    @Test
    public void getClosePricesForFutures_ShouldReturnOk_WhenValidRequest() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            List<ClosePriceDto> closePrices = Arrays.asList(TestDataFactory.createClosePriceDto("BBG004730N88", "2024-01-15", BigDecimal.valueOf(102.50), BigDecimal.valueOf(102.50)));
            when(mainSessionPriceService.getClosePricesForAllFutures()).thenReturn(closePrices);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(get("/api/main-session-prices/futures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Цены закрытия для фьючерсов получены успешно"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(mainSessionPriceService).getClosePricesForAllFutures();
        });
    }

    @Test
    public void getClosePriceByFigi_ShouldReturnOk_WhenValidRequest() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            String figi = "BBG004730N88";
            List<ClosePriceDto> closePrices = Arrays.asList(TestDataFactory.createClosePriceDto("BBG004730N88", "2024-01-15", BigDecimal.valueOf(102.50), BigDecimal.valueOf(102.50)));
            when(mainSessionPriceService.getClosePrices(List.of(figi), null)).thenReturn(closePrices);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            String figi = "BBG004730N88";
            mockMvc.perform(get("/api/main-session-prices/by-figi/{figi}", figi))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Цена закрытия получена успешно"))
                .andExpect(jsonPath("$.data.figi").value(figi))
                .andExpect(jsonPath("$.data.tradingDate").value("2024-01-15"))
                .andExpect(jsonPath("$.data.closePrice").value(102.50))
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            String figi = "BBG004730N88";
            verify(mainSessionPriceService).getClosePrices(List.of(figi), null);
        });
    }

    @Test
    public void getClosePriceByFigi_ShouldReturnOk_WhenNoData() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            String figi = "BBG004730N88";
            when(mainSessionPriceService.getClosePrices(List.of(figi), null)).thenReturn(Collections.emptyList());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            String figi = "BBG004730N88";
            mockMvc.perform(get("/api/main-session-prices/by-figi/{figi}", figi))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Цена закрытия не найдена для инструмента: " + figi))
                .andExpect(jsonPath("$.figi").value(figi))
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            String figi = "BBG004730N88";
            verify(mainSessionPriceService).getClosePrices(List.of(figi), null);
        });
    }

    // ========== ПОЗИТИВНЫЕ ТЕСТЫ - POST ENDPOINTS ==========

    @Test
    public void loadClosePricesToday_ShouldReturnOk_WhenValidRequest() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            List<ShareDto> shares = Arrays.asList(TestDataFactory.createShareDto());
            List<FutureDto> futures = Arrays.asList(TestDataFactory.createFutureDto());
            List<ClosePriceDto> closePrices = Arrays.asList(TestDataFactory.createClosePriceDto("BBG004730N88", "2024-01-15", BigDecimal.valueOf(102.50), BigDecimal.valueOf(102.50)));

            when(instrumentService.getShares(null, null, "RUB", null, null)).thenReturn(shares);
            when(instrumentService.getFutures(null, null, "RUB", null, null)).thenReturn(futures);
            when(mainSessionPriceService.getClosePrices(anyList(), isNull())).thenReturn(closePrices);
            when(closePriceRepository.existsById(any(ClosePriceKey.class))).thenReturn(false);
            when(instrumentService.getShareByFigi(anyString())).thenReturn(TestDataFactory.createShareDto());
            when(closePriceRepository.save(any(ClosePriceEntity.class))).thenReturn(TestDataFactory.createClosePriceEntity());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/main-session-prices/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalRequested").value(2))
                .andExpect(jsonPath("$.newItemsSaved").value(1))
                .andExpect(jsonPath("$.existingItemsSkipped").value(0))
                .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
                .andExpect(jsonPath("$.missingFromApi").value(1))
                .andExpect(jsonPath("$.savedItems").isArray())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(instrumentService).getShares(null, null, "RUB", null, null);
            verify(instrumentService).getFutures(null, null, "RUB", null, null);
            verify(mainSessionPriceService).getClosePrices(anyList(), isNull());
            verify(closePriceRepository).save(any(ClosePriceEntity.class));
        });
    }

    @Test
    public void loadClosePricesToday_ShouldReturnOk_WhenNoInstruments() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(instrumentService.getShares(null, null, "RUB", null, null)).thenReturn(Collections.emptyList());
            when(instrumentService.getFutures(null, null, "RUB", null, null)).thenReturn(Collections.emptyList());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/main-session-prices/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Не найдено акций и фьючерсов в кэше"))
                .andExpect(jsonPath("$.totalRequested").value(0))
                .andExpect(jsonPath("$.newItemsSaved").value(0))
                .andExpect(jsonPath("$.existingItemsSkipped").value(0))
                .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
                .andExpect(jsonPath("$.missingFromApi").value(0))
                .andExpect(jsonPath("$.savedItems").isArray())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(instrumentService).getShares(null, null, "RUB", null, null);
            verify(instrumentService).getFutures(null, null, "RUB", null, null);
        });
    }

    @Test
    public void loadClosePricesForShares_ShouldReturnOk_WhenValidRequest() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            List<ShareDto> shares = Arrays.asList(TestDataFactory.createShareDto());
            List<ClosePriceDto> closePrices = Arrays.asList(TestDataFactory.createClosePriceDto("BBG004730N88", "2024-01-15", BigDecimal.valueOf(102.50), BigDecimal.valueOf(102.50)));

            when(instrumentService.getShares(null, null, "RUB", null, null)).thenReturn(shares);
            when(mainSessionPriceService.getClosePrices(anyList(), isNull())).thenReturn(closePrices);
            when(closePriceRepository.existsById(any(ClosePriceKey.class))).thenReturn(false);
            when(instrumentService.getShareByFigi(anyString())).thenReturn(TestDataFactory.createShareDto());
            when(closePriceRepository.save(any(ClosePriceEntity.class))).thenReturn(TestDataFactory.createClosePriceEntity());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/main-session-prices/shares"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalRequested").value(1))
                .andExpect(jsonPath("$.newItemsSaved").value(1))
                .andExpect(jsonPath("$.existingItemsSkipped").value(0))
                .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
                .andExpect(jsonPath("$.missingFromApi").value(0))
                .andExpect(jsonPath("$.savedItems").isArray())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(instrumentService).getShares(null, null, "RUB", null, null);
            verify(mainSessionPriceService).getClosePrices(anyList(), isNull());
            verify(closePriceRepository).save(any(ClosePriceEntity.class));
        });
    }

    @Test
    public void loadClosePricesForFutures_ShouldReturnOk_WhenValidRequest() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            List<FutureDto> futures = Arrays.asList(TestDataFactory.createFutureDto());
            List<ClosePriceDto> closePrices = Arrays.asList(TestDataFactory.createClosePriceDto("BBG004730N88", "2024-01-15", BigDecimal.valueOf(102.50), BigDecimal.valueOf(102.50)));

            when(instrumentService.getFutures(null, null, "RUB", null, null)).thenReturn(futures);
            when(mainSessionPriceService.getClosePrices(anyList(), isNull())).thenReturn(closePrices);
            when(closePriceRepository.existsById(any(ClosePriceKey.class))).thenReturn(false);
            when(instrumentService.getFutureByFigi(anyString())).thenReturn(TestDataFactory.createFutureDto());
            when(closePriceRepository.save(any(ClosePriceEntity.class))).thenReturn(TestDataFactory.createClosePriceEntity());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/main-session-prices/futures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalRequested").value(1))
                .andExpect(jsonPath("$.newItemsSaved").value(1))
                .andExpect(jsonPath("$.existingItemsSkipped").value(0))
                .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
                .andExpect(jsonPath("$.missingFromApi").value(0))
                .andExpect(jsonPath("$.savedItems").isArray())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(instrumentService).getFutures(null, null, "RUB", null, null);
            verify(mainSessionPriceService).getClosePrices(anyList(), isNull());
            verify(closePriceRepository).save(any(ClosePriceEntity.class));
        });
    }

    @Test
    public void loadClosePriceByFigi_ShouldReturnOk_WhenValidRequest() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            String figi = "BBG004730N88";
            List<ClosePriceDto> closePrices = Arrays.asList(TestDataFactory.createClosePriceDto("BBG004730N88", "2024-01-15", BigDecimal.valueOf(102.50), BigDecimal.valueOf(102.50)));

            when(mainSessionPriceService.getClosePrices(List.of(figi), null)).thenReturn(closePrices);
            when(closePriceRepository.existsById(any(ClosePriceKey.class))).thenReturn(false);
            when(instrumentService.getShareByFigi(figi)).thenReturn(TestDataFactory.createShareDto());
            when(closePriceRepository.save(any(ClosePriceEntity.class))).thenReturn(TestDataFactory.createClosePriceEntity());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            String figi = "BBG004730N88";
            mockMvc.perform(post("/api/main-session-prices/instrument/{figi}", figi))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Цена закрытия успешно загружена для инструмента: " + figi))
                .andExpect(jsonPath("$.figi").value(figi))
                .andExpect(jsonPath("$.totalRequested").value(1))
                .andExpect(jsonPath("$.newItemsSaved").value(1))
                .andExpect(jsonPath("$.existingItemsSkipped").value(0))
                .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
                .andExpect(jsonPath("$.missingFromApi").value(0))
                .andExpect(jsonPath("$.savedItems").isArray())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            String figi = "BBG004730N88";
            verify(mainSessionPriceService).getClosePrices(List.of(figi), null);
            verify(closePriceRepository).save(any(ClosePriceEntity.class));
        });
    }

    @Test
    public void loadMainSessionPricesForDate_ShouldReturnOk_WhenValidDate() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            List<ShareEntity> shares = Arrays.asList(TestDataFactory.createShareEntity());
            List<FutureEntity> futures = Arrays.asList(TestDataFactory.createFutureEntity());
            MinuteCandleEntity candle = TestDataFactory.createMinuteCandleEntity("BBG004730N88", LocalDate.of(2024, 1, 15), BigDecimal.valueOf(100.0));

            when(shareRepository.findAll()).thenReturn(shares);
            when(futureRepository.findAll()).thenReturn(futures);
            when(closePriceRepository.existsById(any(ClosePriceKey.class))).thenReturn(false);
            when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class))).thenReturn(Arrays.asList(candle));
            when(closePriceRepository.save(any(ClosePriceEntity.class))).thenReturn(TestDataFactory.createClosePriceEntity());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/main-session-prices/by-date/{date}", testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.totalRequested").value(2))
                .andExpect(jsonPath("$.newItemsSaved").value(2))
                .andExpect(jsonPath("$.existingItemsSkipped").value(0))
                .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
                .andExpect(jsonPath("$.missingFromApi").value(0))
                .andExpect(jsonPath("$.savedItems").isArray());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(shareRepository).findAll();
            verify(futureRepository).findAll();
            verify(closePriceRepository, times(2)).save(any(ClosePriceEntity.class));
        });
    }

    @Test
    public void loadMainSessionPricesForDate_ShouldReturnOk_WhenWeekend() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            // Выходной день - суббота
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate weekendDate = LocalDate.of(2024, 1, 13); // Суббота
            mockMvc.perform(post("/api/main-session-prices/by-date/{date}", weekendDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("В выходные дни (суббота и воскресенье) основная сессия не проводится. Дата: " + weekendDate))
                .andExpect(jsonPath("$.date").value(weekendDate.toString()))
                .andExpect(jsonPath("$.totalRequested").value(0))
                .andExpect(jsonPath("$.newItemsSaved").value(0))
                .andExpect(jsonPath("$.existingItemsSkipped").value(0))
                .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
                .andExpect(jsonPath("$.missingFromApi").value(0))
                .andExpect(jsonPath("$.savedItems").isArray());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(shareRepository, never()).findAll();
            verify(futureRepository, never()).findAll();
        });
    }

    // ========== НЕГАТИВНЫЕ ТЕСТЫ ==========

    @Test
    public void getClosePricesForShares_ShouldThrowException_WhenServiceThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(mainSessionPriceService.getClosePricesForAllShares()).thenThrow(new DataLoadException("API connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(get("/api/main-session-prices/shares"))
                .andExpect(status().isInternalServerError());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(mainSessionPriceService).getClosePricesForAllShares();
        });
    }

    @Test
    public void getClosePricesForFutures_ShouldThrowException_WhenServiceThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(mainSessionPriceService.getClosePricesForAllFutures()).thenThrow(new DataLoadException("API connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(get("/api/main-session-prices/futures"))
                .andExpect(status().isInternalServerError());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(mainSessionPriceService).getClosePricesForAllFutures();
        });
    }

    @Test
    public void getClosePriceByFigi_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            String figi = "BBG004730N88";
            when(mainSessionPriceService.getClosePrices(List.of(figi), null)).thenThrow(new RuntimeException("API connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            String figi = "BBG004730N88";
            mockMvc.perform(get("/api/main-session-prices/by-figi/{figi}", figi))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ошибка получения цены закрытия: API connection error"))
                .andExpect(jsonPath("$.figi").value(figi))
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            String figi = "BBG004730N88";
            verify(mainSessionPriceService).getClosePrices(List.of(figi), null);
        });
    }

    @Test
    public void loadClosePricesToday_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(instrumentService.getShares(null, null, "RUB", null, null)).thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/main-session-prices/"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.totalRequested").value(0))
                .andExpect(jsonPath("$.newItemsSaved").value(0))
                .andExpect(jsonPath("$.existingItemsSkipped").value(0))
                .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
                .andExpect(jsonPath("$.missingFromApi").value(0))
                .andExpect(jsonPath("$.savedItems").isArray())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(instrumentService).getShares(null, null, "RUB", null, null);
        });
    }

    @Test
    public void loadClosePricesForShares_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(instrumentService.getShares(null, null, "RUB", null, null)).thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/main-session-prices/shares"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.totalRequested").value(0))
                .andExpect(jsonPath("$.newItemsSaved").value(0))
                .andExpect(jsonPath("$.existingItemsSkipped").value(0))
                .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
                .andExpect(jsonPath("$.missingFromApi").value(0))
                .andExpect(jsonPath("$.savedItems").isArray())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(instrumentService).getShares(null, null, "RUB", null, null);
        });
    }

    @Test
    public void loadClosePricesForFutures_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(instrumentService.getFutures(null, null, "RUB", null, null)).thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/main-session-prices/futures"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.totalRequested").value(0))
                .andExpect(jsonPath("$.newItemsSaved").value(0))
                .andExpect(jsonPath("$.existingItemsSkipped").value(0))
                .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
                .andExpect(jsonPath("$.missingFromApi").value(0))
                .andExpect(jsonPath("$.savedItems").isArray())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(instrumentService).getFutures(null, null, "RUB", null, null);
        });
    }

    @Test
    public void loadClosePriceByFigi_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            String figi = "BBG004730N88";
            when(mainSessionPriceService.getClosePrices(List.of(figi), null)).thenThrow(new RuntimeException("API connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            String figi = "BBG004730N88";
            mockMvc.perform(post("/api/main-session-prices/instrument/{figi}", figi))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ошибка при загрузке цены закрытия для инструмента " + figi + ": API connection error"))
                .andExpect(jsonPath("$.figi").value(figi))
                .andExpect(jsonPath("$.totalRequested").value(1))
                .andExpect(jsonPath("$.newItemsSaved").value(0))
                .andExpect(jsonPath("$.existingItemsSkipped").value(0))
                .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
                .andExpect(jsonPath("$.missingFromApi").value(0))
                .andExpect(jsonPath("$.savedItems").isArray())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            String figi = "BBG004730N88";
            verify(mainSessionPriceService).getClosePrices(List.of(figi), null);
        });
    }

    @Test
    public void loadMainSessionPricesForDate_ShouldReturnInternalServerError_WhenRepositoryThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(shareRepository.findAll()).thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/main-session-prices/by-date/{date}", testDate))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ошибка при загрузке цен основной сессии: Database connection error"))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.totalRequested").value(0))
                .andExpect(jsonPath("$.newItemsSaved").value(0))
                .andExpect(jsonPath("$.existingItemsSkipped").value(0))
                .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
                .andExpect(jsonPath("$.missingFromApi").value(0))
                .andExpect(jsonPath("$.savedItems").isArray());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(shareRepository).findAll();
        });
    }

    @Test
    public void loadClosePriceByFigi_ShouldReturnOk_WhenRecordExists() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            String figi = "BBG004730N88";
            List<ClosePriceDto> closePrices = Arrays.asList(TestDataFactory.createClosePriceDto("BBG004730N88", "2024-01-15", BigDecimal.valueOf(102.50), BigDecimal.valueOf(102.50)));

            when(mainSessionPriceService.getClosePrices(List.of(figi), null)).thenReturn(closePrices);
            when(closePriceRepository.existsById(any(ClosePriceKey.class))).thenReturn(true);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            String figi = "BBG004730N88";
            mockMvc.perform(post("/api/main-session-prices/instrument/{figi}", figi))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Цена закрытия уже существует для инструмента " + figi + " за " + LocalDate.parse("2024-01-15")))
                .andExpect(jsonPath("$.figi").value(figi))
                .andExpect(jsonPath("$.closePrice").value(102.50))
                .andExpect(jsonPath("$.totalRequested").value(1))
                .andExpect(jsonPath("$.newItemsSaved").value(0))
                .andExpect(jsonPath("$.existingItemsSkipped").value(1))
                .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
                .andExpect(jsonPath("$.missingFromApi").value(0))
                .andExpect(jsonPath("$.savedItems").isArray())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            String figi = "BBG004730N88";
            verify(mainSessionPriceService).getClosePrices(List.of(figi), null);
            verify(closePriceRepository).existsById(any(ClosePriceKey.class));
            verify(closePriceRepository, never()).save(any(ClosePriceEntity.class));
        });
    }

    @Test
    public void loadClosePriceByFigi_ShouldReturnOk_WhenNoData() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            String figi = "BBG004730N88";
            when(mainSessionPriceService.getClosePrices(List.of(figi), null)).thenReturn(Collections.emptyList());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            String figi = "BBG004730N88";
            mockMvc.perform(post("/api/main-session-prices/instrument/{figi}", figi))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Цена закрытия не найдена для инструмента: " + figi))
                .andExpect(jsonPath("$.figi").value(figi))
                .andExpect(jsonPath("$.totalRequested").value(1))
                .andExpect(jsonPath("$.newItemsSaved").value(0))
                .andExpect(jsonPath("$.existingItemsSkipped").value(0))
                .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
                .andExpect(jsonPath("$.missingFromApi").value(1))
                .andExpect(jsonPath("$.savedItems").isArray())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            String figi = "BBG004730N88";
            verify(mainSessionPriceService).getClosePrices(List.of(figi), null);
            verify(closePriceRepository, never()).existsById(any(ClosePriceKey.class));
            verify(closePriceRepository, never()).save(any(ClosePriceEntity.class));
        });
    }

    @Test
    public void loadMainSessionPricesForDate_ShouldReturnOk_WhenNoInstruments() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(shareRepository.findAll()).thenReturn(Collections.emptyList());
            when(futureRepository.findAll()).thenReturn(Collections.emptyList());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/main-session-prices/by-date/{date}", testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Не найдено акций и фьючерсов в базе данных"))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.totalRequested").value(0))
                .andExpect(jsonPath("$.newItemsSaved").value(0))
                .andExpect(jsonPath("$.existingItemsSkipped").value(0))
                .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
                .andExpect(jsonPath("$.missingFromApi").value(0))
                .andExpect(jsonPath("$.savedItems").isArray());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(shareRepository).findAll();
            verify(futureRepository).findAll();
        });
    }
}