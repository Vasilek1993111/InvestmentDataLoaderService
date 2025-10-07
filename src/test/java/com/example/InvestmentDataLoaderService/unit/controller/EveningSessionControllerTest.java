package com.example.InvestmentDataLoaderService.unit.controller;

import com.example.InvestmentDataLoaderService.controller.EveningSessionController;
import com.example.InvestmentDataLoaderService.entity.*;
import com.example.InvestmentDataLoaderService.repository.*;
import com.example.InvestmentDataLoaderService.fixtures.TestDataFactory;

import io.qameta.allure.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit-тесты для EveningSessionController
 * 
 * <p>Этот класс содержит изолированные unit-тесты контроллера вечерней сессии.
 * Все тестовые данные создаются через TestDataFactory для обеспечения
 * консистентности и переиспользования.</p>
 * 
 * <p>Тесты структурированы с использованием Allure.step() для создания
 * подробных отчетов в Allure с последовательностью выполнения шагов.</p>
 */
@WebMvcTest(EveningSessionController.class)
@Epic("API Evening Session")
@Feature("Evening Session Management")
@DisplayName("Evening Session Controller Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)

class EveningSessionControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShareRepository shareRepository;
    @MockitoBean
    private FutureRepository futureRepository;
    @MockitoBean
    private MinuteCandleRepository minuteCandleRepository;
    @MockitoBean
    private ClosePriceEveningSessionRepository closePriceEveningSessionRepository;

    @BeforeEach
    public void setUp() {
        reset(shareRepository);
        reset(futureRepository);
        reset(minuteCandleRepository);
        reset(closePriceEveningSessionRepository);
    }

    // ==================== ТЕСТЫ ДЛЯ GET /api/evening-session-prices ====================

    @Test
    @DisplayName("Получение цен вечерней сессии за вчера - успешный случай")
    @Description("Тест проверяет корректность получения цен вечерней сессии за вчерашний день")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("get")
    @Tag("positive")
    void getEveningSessionClosePricesYesterday_ShouldReturnOk_WhenValidRequest() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных", () -> {
            List<ShareEntity> shares = Arrays.asList(TestDataFactory.createShareEntity());
            List<FutureEntity> futures = Arrays.asList(TestDataFactory.createFutureEntity());
            when(shareRepository.findAll()).thenReturn(shares);
            when(futureRepository.findAll()).thenReturn(futures);
            when(minuteCandleRepository.findLastCandleForDate(anyString(), any(LocalDate.class)))
                .thenReturn(TestDataFactory.createMinuteCandleEntity("BBG004730N88", LocalDate.now().minusDays(1), BigDecimal.valueOf(100.0)));
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(get("/api/evening-session-prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.count").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.statistics").exists())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса", () -> {
            verify(shareRepository).findAll();
            verify(futureRepository).findAll();
            verify(minuteCandleRepository, atLeastOnce()).findLastCandleForDate(anyString(), any(LocalDate.class));
        });
    }

    @Test
    @DisplayName("Получение цен вечерней сессии за вчера - пустой результат")
    @Description("Тест проверяет поведение API при отсутствии данных за вчерашний день")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("get")
    @Tag("empty-data")
    @Tag("positive")
    void getEveningSessionClosePricesYesterday_ShouldReturnOk_WhenNoData() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных для пустого результата", () -> {
            when(shareRepository.findAll()).thenReturn(Collections.emptyList());
            when(futureRepository.findAll()).thenReturn(Collections.emptyList());
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка пустого ответа", () -> {
            mockMvc.perform(get("/api/evening-session-prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.count").value(0));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для пустого результата", () -> {
            verify(shareRepository).findAll();
            verify(futureRepository).findAll();
        });
    }

    @Test
    @DisplayName("Получение цен вечерней сессии за вчера - ошибка репозитория")
    @Description("Тест проверяет обработку ошибок при сбое репозитория")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("get")
    @Tag("negative")
    @Tag("error-handling")
    void getEveningSessionClosePricesYesterday_ShouldReturnInternalServerError_WhenRepositoryThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(shareRepository.findAll()).thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(get("/api/evening-session-prices"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(shareRepository).findAll();
        });
    }

    // ==================== ТЕСТЫ ДЛЯ POST /api/evening-session-prices ====================

    @Test
    @DisplayName("Загрузка цен вечерней сессии за вчера - успешный случай")
    @Description("Тест проверяет корректность загрузки цен вечерней сессии за вчерашний день")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("post")
    @Tag("load")
    @Tag("positive")
    void loadEveningSessionPrices_ShouldReturnOk_WhenValidRequest() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных для загрузки", () -> {
            List<ShareEntity> shares = Arrays.asList(TestDataFactory.createShareEntity());
            List<FutureEntity> futures = Arrays.asList(TestDataFactory.createFutureEntity());
            MinuteCandleEntity candle = TestDataFactory.createMinuteCandleEntity("BBG004730N88", LocalDate.now().minusDays(1), BigDecimal.valueOf(100.0));

            when(shareRepository.findAll()).thenReturn(shares);
            when(futureRepository.findAll()).thenReturn(futures);
            when(closePriceEveningSessionRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString())).thenReturn(false);
            when(minuteCandleRepository.findLastCandleForDate(anyString(), any(LocalDate.class))).thenReturn(candle);
            when(closePriceEveningSessionRepository.save(any(ClosePriceEveningSessionEntity.class))).thenReturn(TestDataFactory.createClosePriceEveningSessionEntity());
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение POST запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/evening-session-prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.statistics").exists())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для загрузки", () -> {
            verify(shareRepository).findAll();
            verify(futureRepository).findAll();
            verify(closePriceEveningSessionRepository, atLeastOnce()).save(any(ClosePriceEveningSessionEntity.class));
        });
    }

    @Test
    @DisplayName("Загрузка цен вечерней сессии за вчера - отсутствие инструментов")
    @Description("Тест проверяет поведение API при отсутствии инструментов в базе данных")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("post")
    @Tag("load")
    @Tag("no-instruments")
    @Tag("business-logic")
    @Tag("positive")
    void loadEveningSessionPrices_ShouldReturnOk_WhenNoInstruments() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(shareRepository.findAll()).thenReturn(Collections.emptyList());
            when(futureRepository.findAll()).thenReturn(Collections.emptyList());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/evening-session-prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.statistics").exists())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(shareRepository).findAll();
            verify(futureRepository).findAll();
        });
    }

    @Test
    @DisplayName("Загрузка цен вечерней сессии за вчера - ошибка репозитория")
    @Description("Тест проверяет обработку ошибок при сбое репозитория при загрузке")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("post")
    @Tag("load")
    @Tag("negative")
    @Tag("error-handling")
    void loadEveningSessionPrices_ShouldReturnInternalServerError_WhenRepositoryThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(shareRepository.findAll())
                .thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(post("/api/evening-session-prices"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(shareRepository).findAll();
        });
    }

    // ==================== ТЕСТЫ ДЛЯ GET /api/evening-session-prices/by-date/{date} ====================

    @Test
    @DisplayName("Получение цен вечерней сессии по дате - успешный случай")
    @Description("Тест проверяет корректность получения цен вечерней сессии по указанной дате")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("get")
    @Tag("date-parameter")
    @Tag("positive")
    void getEveningSessionPricesForAllInstruments_ShouldReturnOk_WhenValidDate() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных для указанной даты", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            List<ShareEntity> shares = Arrays.asList(TestDataFactory.createShareEntity());
            List<FutureEntity> futures = Arrays.asList(TestDataFactory.createFutureEntity());
            when(shareRepository.findAll()).thenReturn(shares);
            when(futureRepository.findAll()).thenReturn(futures);
            when(minuteCandleRepository.findLastCandleForDate(anyString(), eq(testDate)))
                .thenReturn(TestDataFactory.createMinuteCandleEntity("BBG004730N88", testDate, BigDecimal.valueOf(100.0)));
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса по дате и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/evening-session-prices/by-date/{date}", testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.count").exists())
                .andExpect(jsonPath("$.totalProcessed").exists())
                .andExpect(jsonPath("$.foundPrices").exists())
                .andExpect(jsonPath("$.missingData").exists())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для указанной даты", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(shareRepository).findAll();
            verify(futureRepository).findAll();
            verify(minuteCandleRepository, atLeastOnce()).findLastCandleForDate(anyString(), eq(testDate));
        });
    }

    @Test
    @DisplayName("Получение цен вечерней сессии по дате - пустой результат")
    @Description("Тест проверяет поведение API при отсутствии данных на указанную дату")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("get")
    @Tag("date-parameter")
    @Tag("empty-data")
    @Tag("positive")
    void getEveningSessionPricesForAllInstruments_ShouldReturnOk_WhenNoData() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(shareRepository.findAll()).thenReturn(Collections.emptyList());
            when(futureRepository.findAll()).thenReturn(Collections.emptyList());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/evening-session-prices/by-date/{date}", testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.count").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(shareRepository).findAll();
            verify(futureRepository).findAll();
        });
    }

    @Test
    @DisplayName("Получение цен вечерней сессии по дате - ошибка репозитория")
    @Description("Тест проверяет обработку ошибок при сбое репозитория при запросе по дате")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("get")
    @Tag("date-parameter")
    @Tag("negative")
    @Tag("error-handling")
    void getEveningSessionPricesForAllInstruments_ShouldReturnInternalServerError_WhenRepositoryThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(shareRepository.findAll()).thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/evening-session-prices/by-date/{date}", testDate))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(shareRepository).findAll();
        });
    }

    // ==================== ТЕСТЫ ДЛЯ POST /api/evening-session-prices/by-date/{date} ====================

    @Test
    @DisplayName("Загрузка цен вечерней сессии по дате - успешный случай")
    @Description("Тест проверяет корректность загрузки цен вечерней сессии по указанной дате")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("post")
    @Tag("load")
    @Tag("date-parameter")
    @Tag("positive")
    void loadEveningSessionPricesForDate_ShouldReturnOk_WhenValidDate() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            List<ShareEntity> shares = Arrays.asList(TestDataFactory.createShareEntity());
            List<FutureEntity> futures = Arrays.asList(TestDataFactory.createFutureEntity());
            MinuteCandleEntity candle = TestDataFactory.createMinuteCandleEntity("BBG004730N88", testDate, BigDecimal.valueOf(100.0));

            when(shareRepository.findAll()).thenReturn(shares);
            when(futureRepository.findAll()).thenReturn(futures);
            when(closePriceEveningSessionRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString())).thenReturn(false);
            when(minuteCandleRepository.findLastCandleForDate(anyString(), any(LocalDate.class))).thenReturn(candle);
            when(closePriceEveningSessionRepository.save(any(ClosePriceEveningSessionEntity.class))).thenReturn(TestDataFactory.createClosePriceEveningSessionEntity());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/evening-session-prices/by-date/{date}", testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.totalRequested").exists())
                .andExpect(jsonPath("$.newItemsSaved").exists())
                .andExpect(jsonPath("$.existingItemsSkipped").exists())
                .andExpect(jsonPath("$.invalidItemsFiltered").exists())
                .andExpect(jsonPath("$.missingFromApi").exists())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(shareRepository).findAll();
            verify(futureRepository).findAll();
            verify(closePriceEveningSessionRepository, atLeastOnce()).save(any(ClosePriceEveningSessionEntity.class));
        });
    }

    @Test
    @DisplayName("Загрузка цен вечерней сессии по дате - ошибка репозитория")
    @Description("Тест проверяет обработку ошибок при сбое репозитория при загрузке по дате")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("post")
    @Tag("load")
    @Tag("date-parameter")
    @Tag("negative")
    @Tag("error-handling")
    void loadEveningSessionPricesForDate_ShouldReturnInternalServerError_WhenRepositoryThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(shareRepository.findAll())
                .thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/evening-session-prices/by-date/{date}", testDate))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(shareRepository).findAll();
        });
    }

    // ==================== ТЕСТЫ ДЛЯ GET /api/evening-session-prices/shares/{date} ====================

    @Test
    @DisplayName("Получение цен вечерней сессии для акций по дате - успешный случай")
    @Description("Тест проверяет корректность получения цен вечерней сессии для акций по указанной дате")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("get")
    @Tag("shares")
    @Tag("date-parameter")
    @Tag("positive")
    void getEveningSessionPricesForShares_ShouldReturnOk_WhenValidDate() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных для акций", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            List<ShareEntity> shares = Arrays.asList(TestDataFactory.createShareEntity());
            when(shareRepository.findAll()).thenReturn(shares);
            when(minuteCandleRepository.findLastCandleForDate(anyString(), eq(testDate)))
                .thenReturn(TestDataFactory.createMinuteCandleEntity("BBG004730N88", testDate, BigDecimal.valueOf(100.0)));
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса для акций и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/evening-session-prices/shares/{date}", testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.count").exists())
                .andExpect(jsonPath("$.totalProcessed").exists())
                .andExpect(jsonPath("$.foundPrices").exists())
                .andExpect(jsonPath("$.missingData").exists())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для акций", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(shareRepository).findAll();
            verify(minuteCandleRepository, atLeastOnce()).findLastCandleForDate(anyString(), eq(testDate));
        });
    }

    @Test
    @DisplayName("Получение цен вечерней сессии для акций по дате - пустой результат")
    @Description("Тест проверяет поведение API при отсутствии акций в базе данных")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("get")
    @Tag("shares")
    @Tag("empty-data")
    @Tag("positive")
    void getEveningSessionPricesForShares_ShouldReturnOk_WhenNoShares() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(shareRepository.findAll()).thenReturn(Collections.emptyList());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/evening-session-prices/shares/{date}", testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.count").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(shareRepository).findAll();
        });
    }

    @Test
    @DisplayName("Получение цен вечерней сессии для акций по дате - ошибка репозитория")
    @Description("Тест проверяет обработку ошибок при сбое репозитория для акций")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("get")
    @Tag("shares")
    @Tag("negative")
    @Tag("error-handling")
    void getEveningSessionPricesForShares_ShouldReturnInternalServerError_WhenRepositoryThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(shareRepository.findAll()).thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/evening-session-prices/shares/{date}", testDate))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(shareRepository).findAll();
        });
    }

    // ==================== ТЕСТЫ ДЛЯ POST /api/evening-session-prices/shares/{date} ====================

    @Test
    @DisplayName("Сохранение цен вечерней сессии для акций по дате - успешный случай")
    @Description("Тест проверяет корректность сохранения цен вечерней сессии для акций по указанной дате")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("post")
    @Tag("save")
    @Tag("shares")
    @Tag("date-parameter")
    @Tag("positive")
    void saveEveningSessionPricesForShares_ShouldReturnOk_WhenValidDate() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            List<ShareEntity> shares = Arrays.asList(TestDataFactory.createShareEntity());
            MinuteCandleEntity candle = TestDataFactory.createMinuteCandleEntity("BBG004730N88", testDate, BigDecimal.valueOf(100.0));

            when(shareRepository.findAll()).thenReturn(shares);
            when(closePriceEveningSessionRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString())).thenReturn(false);
            when(minuteCandleRepository.findLastCandleForDate(anyString(), any(LocalDate.class))).thenReturn(candle);
            when(closePriceEveningSessionRepository.save(any(ClosePriceEveningSessionEntity.class))).thenReturn(TestDataFactory.createClosePriceEveningSessionEntity());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/evening-session-prices/shares/{date}", testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.totalProcessed").exists())
                .andExpect(jsonPath("$.newItemsSaved").exists())
                .andExpect(jsonPath("$.existingItemsSkipped").exists())
                .andExpect(jsonPath("$.invalidItemsFiltered").exists())
                .andExpect(jsonPath("$.missingData").exists())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(shareRepository).findAll();
            verify(closePriceEveningSessionRepository, atLeastOnce()).save(any(ClosePriceEveningSessionEntity.class));
        });
    }

    @Test
    @DisplayName("Сохранение цен вечерней сессии для акций по дате - ошибка репозитория")
    @Description("Тест проверяет обработку ошибок при сбое репозитория при сохранении акций")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("post")
    @Tag("save")
    @Tag("shares")
    @Tag("negative")
    @Tag("error-handling")
    void saveEveningSessionPricesForShares_ShouldReturnInternalServerError_WhenRepositoryThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(shareRepository.findAll()).thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/evening-session-prices/shares/{date}", testDate))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(shareRepository).findAll();
        });
    }

    // ==================== ТЕСТЫ ДЛЯ GET /api/evening-session-prices/futures/{date} ====================

    @Test
    @DisplayName("Получение цен вечерней сессии для фьючерсов по дате - успешный случай")
    @Description("Тест проверяет корректность получения цен вечерней сессии для фьючерсов по указанной дате")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("get")
    @Tag("futures")
    @Tag("date-parameter")
    @Tag("positive")
    void getEveningSessionPricesForFutures_ShouldReturnOk_WhenValidDate() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных для фьючерсов", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            List<FutureEntity> futures = Arrays.asList(TestDataFactory.createFutureEntity());
            when(futureRepository.findAll()).thenReturn(futures);
            when(minuteCandleRepository.findLastCandleForDate(anyString(), eq(testDate)))
                .thenReturn(TestDataFactory.createMinuteCandleEntity("FUTSBRF-3.24", testDate, BigDecimal.valueOf(100.0)));
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса для фьючерсов и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/evening-session-prices/futures/{date}", testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.count").exists())
                .andExpect(jsonPath("$.totalProcessed").exists())
                .andExpect(jsonPath("$.foundPrices").exists())
                .andExpect(jsonPath("$.missingData").exists())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для фьючерсов", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(futureRepository).findAll();
            verify(minuteCandleRepository, atLeastOnce()).findLastCandleForDate(anyString(), eq(testDate));
        });
    }

    @Test
    @DisplayName("Получение цен вечерней сессии для фьючерсов по дате - пустой результат")
    @Description("Тест проверяет поведение API при отсутствии фьючерсов в базе данных")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("get")
    @Tag("futures")
    @Tag("empty-data")
    @Tag("positive")
    void getEveningSessionPricesForFutures_ShouldReturnOk_WhenNoFutures() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(futureRepository.findAll()).thenReturn(Collections.emptyList());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/evening-session-prices/futures/{date}", testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.count").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(futureRepository).findAll();
        });
    }

    @Test
    @DisplayName("Получение цен вечерней сессии для фьючерсов по дате - ошибка репозитория")
    @Description("Тест проверяет обработку ошибок при сбое репозитория для фьючерсов")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("get")
    @Tag("futures")
    @Tag("negative")
    @Tag("error-handling")
    void getEveningSessionPricesForFutures_ShouldReturnInternalServerError_WhenRepositoryThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(futureRepository.findAll()).thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/evening-session-prices/futures/{date}", testDate))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(futureRepository).findAll();
        });
    }

    // ==================== ТЕСТЫ ДЛЯ POST /api/evening-session-prices/futures/{date} ====================

    @Test
    @DisplayName("Сохранение цен вечерней сессии для фьючерсов по дате - успешный случай")
    @Description("Тест проверяет корректность сохранения цен вечерней сессии для фьючерсов по указанной дате")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("post")
    @Tag("save")
    @Tag("futures")
    @Tag("date-parameter")
    @Tag("positive")
    void saveEveningSessionPricesForFutures_ShouldReturnOk_WhenValidDate() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            List<FutureEntity> futures = Arrays.asList(TestDataFactory.createFutureEntity());
            MinuteCandleEntity candle = TestDataFactory.createMinuteCandleEntity("FUTSBRF-3.24", testDate, BigDecimal.valueOf(100.0));

            when(futureRepository.findAll()).thenReturn(futures);
            when(closePriceEveningSessionRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString())).thenReturn(false);
            when(minuteCandleRepository.findLastCandleForDate(anyString(), any(LocalDate.class))).thenReturn(candle);
            when(closePriceEveningSessionRepository.save(any(ClosePriceEveningSessionEntity.class))).thenReturn(TestDataFactory.createClosePriceEveningSessionEntity());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/evening-session-prices/futures/{date}", testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.totalProcessed").exists())
                .andExpect(jsonPath("$.newItemsSaved").exists())
                .andExpect(jsonPath("$.existingItemsSkipped").exists())
                .andExpect(jsonPath("$.invalidItemsFiltered").exists())
                .andExpect(jsonPath("$.missingData").exists())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(futureRepository).findAll();
            verify(closePriceEveningSessionRepository, atLeastOnce()).save(any(ClosePriceEveningSessionEntity.class));
        });
    }

    @Test
    @DisplayName("Сохранение цен вечерней сессии для фьючерсов по дате - ошибка репозитория")
    @Description("Тест проверяет обработку ошибок при сбое репозитория при сохранении фьючерсов")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("post")
    @Tag("save")
    @Tag("futures")
    @Tag("negative")
    @Tag("error-handling")
    void saveEveningSessionPricesForFutures_ShouldReturnInternalServerError_WhenRepositoryThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            when(futureRepository.findAll()).thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/evening-session-prices/futures/{date}", testDate))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            verify(futureRepository).findAll();
        });
    }

    // ==================== ТЕСТЫ ДЛЯ GET /api/evening-session-prices/by-figi-date/{figi}/{date} ====================

    @Test
    @DisplayName("Получение цены вечерней сессии по FIGI и дате - успешный случай")
    @Description("Тест проверяет корректность получения цены вечерней сессии по FIGI и дате")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("get")
    @Tag("figi-date")
    @Tag("positive")
    void getEveningSessionPriceByFigiAndDate_ShouldReturnOk_WhenValidFigiAndDate() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Allure.step("Подготовка тестовых данных для FIGI и даты", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            MinuteCandleEntity candle = TestDataFactory.createMinuteCandleEntity(testFigi, testDate, BigDecimal.valueOf(100.0));
            when(minuteCandleRepository.findLastCandleForDate(testFigi, testDate)).thenReturn(candle);
        });

        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса по FIGI и дате и проверка ответа", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/evening-session-prices/by-figi-date/{figi}/{date}", testFigi, testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.figi").value(testFigi))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для FIGI и даты", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(minuteCandleRepository).findLastCandleForDate(testFigi, testDate);
        });
    }

    @Test
    @DisplayName("Получение цены вечерней сессии по FIGI и дате - данные не найдены")
    @Description("Тест проверяет поведение API при отсутствии данных для указанного FIGI и даты")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("get")
    @Tag("figi-date")
    @Tag("empty-data")
    @Tag("positive")
    void getEveningSessionPriceByFigiAndDate_ShouldReturnOk_WhenNoData() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            when(minuteCandleRepository.findLastCandleForDate(testFigi, testDate)).thenReturn(null);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/evening-session-prices/by-figi-date/{figi}/{date}", testFigi, testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.figi").value(testFigi))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.message").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(minuteCandleRepository).findLastCandleForDate(testFigi, testDate);
        });
    }

    @Test
    @DisplayName("Получение цены вечерней сессии по FIGI и дате - ошибка репозитория")
    @Description("Тест проверяет обработку ошибок при сбое репозитория при запросе по FIGI и дате")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("get")
    @Tag("figi-date")
    @Tag("negative")
    @Tag("error-handling")
    void getEveningSessionPriceByFigiAndDate_ShouldReturnInternalServerError_WhenRepositoryThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            when(minuteCandleRepository.findLastCandleForDate(testFigi, testDate))
                .thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(get("/api/evening-session-prices/by-figi-date/{figi}/{date}", testFigi, testDate))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(minuteCandleRepository).findLastCandleForDate(testFigi, testDate);
        });
    }

    // ==================== ТЕСТЫ ДЛЯ POST /api/evening-session-prices/by-figi-date/{figi}/{date} ====================

    @Test
    @DisplayName("Сохранение цены вечерней сессии по FIGI и дате - успешный случай")
    @Description("Тест проверяет корректность сохранения цены вечерней сессии по FIGI и дате")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("post")
    @Tag("save")
    @Tag("figi-date")
    @Tag("positive")
    void saveEveningSessionPriceByFigiAndDate_ShouldReturnOk_WhenValidFigiAndDate() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            MinuteCandleEntity candle = TestDataFactory.createMinuteCandleEntity(testFigi, testDate, BigDecimal.valueOf(100.0));

            when(closePriceEveningSessionRepository.existsByPriceDateAndFigi(testDate, testFigi)).thenReturn(false);
            when(minuteCandleRepository.findLastCandleForDate(testFigi, testDate)).thenReturn(candle);
            when(closePriceEveningSessionRepository.save(any(ClosePriceEveningSessionEntity.class))).thenReturn(TestDataFactory.createClosePriceEveningSessionEntity());
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/evening-session-prices/by-figi-date/{figi}/{date}", testFigi, testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.figi").value(testFigi))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.timestamp").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(closePriceEveningSessionRepository).existsByPriceDateAndFigi(testDate, testFigi);
            verify(minuteCandleRepository).findLastCandleForDate(testFigi, testDate);
            verify(closePriceEveningSessionRepository).save(any(ClosePriceEveningSessionEntity.class));
        });
    }

    @Test
    @DisplayName("Сохранение цены вечерней сессии по FIGI и дате - данные уже существуют")
    @Description("Тест проверяет поведение API при попытке сохранить уже существующие данные")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("post")
    @Tag("save")
    @Tag("figi-date")
    @Tag("duplicate")
    @Tag("positive")
    void saveEveningSessionPriceByFigiAndDate_ShouldReturnOk_WhenDataAlreadyExists() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            when(closePriceEveningSessionRepository.existsByPriceDateAndFigi(testDate, testFigi)).thenReturn(true);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/evening-session-prices/by-figi-date/{figi}/{date}", testFigi, testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.figi").value(testFigi))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.message").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(closePriceEveningSessionRepository).existsByPriceDateAndFigi(testDate, testFigi);
            verify(minuteCandleRepository, never()).findLastCandleForDate(anyString(), any(LocalDate.class));
            verify(closePriceEveningSessionRepository, never()).save(any(ClosePriceEveningSessionEntity.class));
        });
    }

    @Test
    @DisplayName("Сохранение цены вечерней сессии по FIGI и дате - данные не найдены в API")
    @Description("Тест проверяет поведение API при отсутствии данных в API для указанного FIGI и даты")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("post")
    @Tag("save")
    @Tag("figi-date")
    @Tag("no-api-data")
    @Tag("positive")
    void saveEveningSessionPriceByFigiAndDate_ShouldReturnOk_WhenNoApiData() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            when(closePriceEveningSessionRepository.existsByPriceDateAndFigi(testDate, testFigi)).thenReturn(false);
            when(minuteCandleRepository.findLastCandleForDate(testFigi, testDate)).thenReturn(null);
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/evening-session-prices/by-figi-date/{figi}/{date}", testFigi, testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.figi").value(testFigi))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.message").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(closePriceEveningSessionRepository).existsByPriceDateAndFigi(testDate, testFigi);
            verify(minuteCandleRepository).findLastCandleForDate(testFigi, testDate);
            verify(closePriceEveningSessionRepository, never()).save(any(ClosePriceEveningSessionEntity.class));
        });
    }

    @Test
    @DisplayName("Сохранение цены вечерней сессии по FIGI и дате - ошибка репозитория")
    @Description("Тест проверяет обработку ошибок при сбое репозитория при сохранении по FIGI и дате")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Вечерней Сессии")
    @Tag("unit")
    @Tag("evening-session")
    @Tag("post")
    @Tag("save")
    @Tag("figi-date")
    @Tag("negative")
    @Tag("error-handling")
    void saveEveningSessionPriceByFigiAndDate_ShouldReturnInternalServerError_WhenRepositoryThrowsException() throws Exception {
        Allure.step("Подготовка тестовых данных", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            when(closePriceEveningSessionRepository.existsByPriceDateAndFigi(testDate, testFigi))
                .thenThrow(new RuntimeException("Database connection error"));
        });

        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            mockMvc.perform(post("/api/evening-session-prices/by-figi-date/{figi}/{date}", testFigi, testDate))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
        });

        Allure.step("Проверка вызовов сервиса", () -> {
            String testFigi = "BBG004730N88";
            LocalDate testDate = LocalDate.of(2024, 1, 15);
            verify(closePriceEveningSessionRepository).existsByPriceDateAndFigi(testDate, testFigi);
        });
    }
}
