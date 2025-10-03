package com.example.InvestmentDataLoaderService.unit.controller;

import com.example.InvestmentDataLoaderService.controller.InstrumentsController;
import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.fixtures.TestDataFactory;
import com.example.InvestmentDataLoaderService.service.InstrumentService;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit-тесты для InstrumentsController
 * 
 * <p>Этот класс содержит изолированные unit-тесты контроллера инструментов.
 * Все тестовые данные создаются через TestDataFactory для обеспечения
 * консистентности и переиспользования.</p>
 * 
 * <p>Тесты структурированы с использованием Allure.step() для создания
 * подробных отчетов в Allure с последовательностью выполнения шагов.</p>
 */
@WebMvcTest(InstrumentsController.class)
@Epic("API Instruments")
@Feature("Instruments Management")
@DisplayName("Instruments Controller Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
class InstrumentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InstrumentService instrumentService;

    @BeforeEach
    void setUp() {
        reset(instrumentService);
    }


    // ==================== ТЕСТЫ ДЛЯ АКЦИЙ ====================

    @Test
    @DisplayName("Получение списка акций через API - успешный случай")
    @Description("Тест проверяет корректность получения списка акций через API с применением фильтров")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Акций")
    @Tag("unit")
    @Tag("shares")
    @Tag("api")
    @Tag("positive")
    void getShares_ShouldReturnSharesList_WhenApiSourceIsUsed() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        List<ShareDto> testShares = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createMoexSharesList();
        });
        
        // Шаг 2: Настройка моков сервиса
        Allure.step("Настройка моков сервиса", () -> {
            when(instrumentService.getShares(
                eq("INSTRUMENT_STATUS_BASE"),
                eq("moex_mrng_evng_e_wknd_dlr"),
                eq("RUB"),
                isNull(),
                isNull()
            )).thenReturn(testShares);
        });
        
        // Шаг 3: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "api")
                    .param("exchange", "moex_mrng_evng_e_wknd_dlr")
                    .param("currency", "RUB")
                    .param("status", "INSTRUMENT_STATUS_BASE")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].figi").value("BBG004730N88"))
                    .andExpect(jsonPath("$[0].ticker").value("SBER"))
                    .andExpect(jsonPath("$[0].name").value("ПАО Сбербанк"))
                    .andExpect(jsonPath("$[0].currency").value("RUB"))
                    .andExpect(jsonPath("$[0].exchange").value("moex_mrng_evng_e_wknd_dlr"))
                    .andExpect(jsonPath("$[0].sector").value("Financial"))
                    .andExpect(jsonPath("$[0].tradingStatus").value("SECURITY_TRADING_STATUS_NORMAL_TRADING"))
                    .andExpect(jsonPath("$[0].shortEnabled").value(true))
                    .andExpect(jsonPath("$[1].ticker").value("GAZP"))
                    .andExpect(jsonPath("$[2].ticker").value("LKOH"));
        });
        
        // Шаг 4: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса", () -> {
            verify(instrumentService).getShares(
                eq("INSTRUMENT_STATUS_BASE"),
                eq("moex_mrng_evng_e_wknd_dlr"),
                eq("RUB"),
                isNull(),
                isNull()
            );
        });
    }


    @Test
    @DisplayName("Получение списка акций через API - без параметров")
    @Description("Тест проверяет поведение API при передаче пустых параметров запроса")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Акций")
    @Tag("unit")
    @Tag("shares")
    @Tag("api")
    @Tag("positive")
    void getShares_ShouldReturnSharesList_WhenNoParametersProvided() throws Exception {
        
        // Шаг 1: Настройка моков сервиса
        Allure.step("Настройка моков сервиса для запроса без параметров", () -> {
            when(instrumentService.getShares(isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(TestDataFactory.createMoexSharesList());
        });
        
        // Шаг 2: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса без параметров и проверка ответа", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].ticker").value("SBER"))
                    .andExpect(jsonPath("$[1].ticker").value("GAZP"))
                    .andExpect(jsonPath("$[2].ticker").value("LKOH"));
        });
        
        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для запроса без параметров", () -> {
            verify(instrumentService).getShares(isNull(), isNull(), isNull(), isNull(), isNull());
        });
    }

    @Test
    @DisplayName("Получение списка акций с несуществующим источником данных")
    @Description("Тест проверяет поведение контроллера при передаче несуществующего источника данных")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Акций")
    @Tag("unit")
    @Tag("shares")
    @Tag("api")
    @Tag("negative")
    @Tag("validation")
    void getShares_ShouldReturn400_WhenInvalidSourceProvided() throws Exception {
        
        // Шаг 1: Выполнение HTTP запроса с невалидным источником
        Allure.step("Выполнение HTTP запроса с невалидным источником", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "invalid")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("ValidationException"))
                    .andExpect(jsonPath("$.message").value(containsString("Невалидный источник данных")));
        });
        
        // Шаг 2: Проверка отсутствия вызовов сервиса
        Allure.step("Проверка отсутствия вызовов сервиса", () -> {
            verify(instrumentService, never()).getShares(any(), any(), any(), any(), any());
        });
    }


    @Test
    @DisplayName("Получение списка акций через API с параметром в верхнем регистре")
    @Description("Тест проверяет поведение контроллера при передаче параметра API в верхнем регистре")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Акций")
    @Tag("unit")
    @Tag("shares")
    @Tag("api")
    @Tag("case-sensitivity")
    @Tag("positive")
    void getShares_ShouldReturnSharesList_WhenApiParameterInUpperCase() throws Exception {
        
        // Шаг 1: Настройка моков сервиса
        Allure.step("Настройка моков сервиса для параметра в верхнем регистре", () -> {
            when(instrumentService.getShares(isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(TestDataFactory.createMoexSharesList());
        });
        
        // Шаг 2: Выполнение HTTP запроса с параметром в верхнем регистре
        Allure.step("Выполнение HTTP запроса с параметром в верхнем регистре", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "API")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].ticker").value("SBER"));
        });
        
        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для параметра в верхнем регистре", () -> {
            verify(instrumentService).getShares(isNull(), isNull(), isNull(), isNull(), isNull());
        });
    }

    @Test
    @DisplayName("Получение списка акций через API с несуществующим FIGI")
    @Description("Тест проверяет поведение контроллера при передаче несуществующего FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Акций")
    @Tag("unit")
    @Tag("shares")
    @Tag("api")
    @Tag("negative")
    @Tag("figi")
    void getShares_ShouldReturn404_WhenInvalidFigiProvided() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для несуществующего FIGI
        Allure.step("Настройка моков сервиса для несуществующего FIGI", () -> {
            when(instrumentService.getShares(isNull(), isNull(), isNull(), isNull(), eq("INVALID_FIGI")))
                .thenReturn(Arrays.asList());
        });
        
        // Шаг 2: Выполнение HTTP запроса с несуществующим FIGI
        Allure.step("Выполнение HTTP запроса с несуществующим FIGI", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "api")
                    .param("figi", "INVALID_FIGI")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.body").isArray())
                    .andExpect(jsonPath("$.body.length()").value(0));
        });
        
        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для несуществующего FIGI", () -> {
            verify(instrumentService).getShares(isNull(), isNull(), isNull(), isNull(), eq("INVALID_FIGI"));
        });
    }

    @Test
    @DisplayName("Получение списка акций через API с FIGI в некорректном формате")
    @Description("Тест проверяет поведение контроллера при передаче FIGI в некорректном формате")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Акций")
    @Tag("unit")
    @Tag("shares")
    @Tag("api")
    @Tag("negative")
    @Tag("validation")
    @Tag("figi")
    void getShares_ShouldHandleInvalidFigiFormat_WhenFigiIsMalformed() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для некорректного формата FIGI
        Allure.step("Настройка моков сервиса для некорректного формата FIGI", () -> {
            when(instrumentService.getShares(isNull(), isNull(), isNull(), isNull(), eq("MALFORMED_FIGI_123!@#")))
                .thenReturn(Arrays.asList());
        });

        // Шаг 2: Выполнение HTTP запроса с некорректным форматом FIGI
        Allure.step("Выполнение HTTP запроса с некорректным форматом FIGI", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "api")
                    .param("figi", "MALFORMED_FIGI_123!@#")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.body").isArray())
                    .andExpect(jsonPath("$.body.length()").value(0));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для некорректного формата FIGI", () -> {
            verify(instrumentService).getShares(isNull(), isNull(), isNull(), isNull(), eq("MALFORMED_FIGI_123!@#"));
        });
    }

   

    @Test
    @DisplayName("Получение списка акций через API с очень длинным FIGI")
    @Description("Тест проверяет поведение контроллера при передаче FIGI превышающего допустимую длину")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("negative")
    @Tag("validation")
    @Tag("boundary")
    void getShares_ShouldHandleVeryLongFigi_WhenFigiExceedsMaxLength() throws Exception {
        
        // Шаг 1: Создание очень длинного FIGI и настройка моков
        String veryLongFigi = Allure.step("Создание очень длинного FIGI и настройка моков", () -> {
            String longFigi = "BBG004730N88" + "A".repeat(1000);
            when(instrumentService.getShares(isNull(), isNull(), isNull(), isNull(), eq(longFigi)))
                .thenReturn(Arrays.asList());
            return longFigi;
        });

        // Шаг 2: Выполнение HTTP запроса с очень длинным FIGI
        Allure.step("Выполнение HTTP запроса с очень длинным FIGI", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "api")
                    .param("figi", veryLongFigi)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.body").isArray())
                    .andExpect(jsonPath("$.body.length()").value(0));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для очень длинного FIGI", () -> {
            verify(instrumentService).getShares(isNull(), isNull(), isNull(), isNull(), eq(veryLongFigi));
        });
    }

    @Test
    @DisplayName("Получение списка акций через API с пустым FIGI")
    @Description("Тест проверяет поведение контроллера при передаче пустого FIGI")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("negative")
    @Tag("validation")
    @Tag("empty-value")
    void getShares_ShouldHandleEmptyFigi_WhenFigiIsEmpty() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для пустого FIGI
        Allure.step("Настройка моков сервиса для пустого FIGI", () -> {
            when(instrumentService.getShares(isNull(), isNull(), isNull(), isNull(), eq("")))
                .thenReturn(TestDataFactory.createMoexSharesList());
        });

        // Шаг 2: Выполнение HTTP запроса с пустым FIGI
        Allure.step("Выполнение HTTP запроса с пустым FIGI", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "api")
                    .param("figi", "")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].figi").value("BBG004730N88"))
                    .andExpect(jsonPath("$[1].figi").value("BBG004730ZJ9"))
                    .andExpect(jsonPath("$[2].figi").value("BBG004730N88"));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для пустого FIGI", () -> {
            verify(instrumentService).getShares(isNull(), isNull(), isNull(), isNull(), eq(""));
        });
    }

    @Test
    @DisplayName("Получение списка акций через API с некорректным ticker")
    @Description("Тест проверяет поведение контроллера при передаче некорректного ticker")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("negative")
    @Tag("validation")
    @Tag("invalid-value")
    void getShares_ShouldHandleInvalidTicker_WhenTickerIsEmpty() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для некорректного ticker
        Allure.step("Настройка моков сервиса для некорректного ticker", () -> {
            when(instrumentService.getShares(isNull(), isNull(), isNull(), eq("INVALID_TICKER"), isNull()))
                .thenReturn(Arrays.asList());
        });

        // Шаг 2: Выполнение HTTP запроса с некорректным ticker
        Allure.step("Выполнение HTTP запроса с некорректным ticker", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "api")
                    .param("ticker", "INVALID_TICKER")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.body").isArray())
                    .andExpect(jsonPath("$.body.length()").value(0));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для некорректного ticker", () -> {
            verify(instrumentService).getShares(isNull(), isNull(), isNull(), eq("INVALID_TICKER"), isNull());
        });
    }
    // ==================== ТЕСТЫ ДЛЯ ПОЛУЧЕНИЯ АКЦИЙ ИЗ БАЗЫ ДАННЫХ ====================

    @Test
    @DisplayName("Получение акций из базы данных через database - успешный случай")
    @Description("Тест проверяет корректность получения акций из базы данных через database")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares database")
    @Tag("database")
    @Tag("shares")
    @Tag("unit")
    void getSharesFromDatabase_ShouldReturnSharesList_WhenApiSourceIsUsed() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для базы данных
        Allure.step("Настройка моков сервиса для базы данных", () -> {
            when(instrumentService.getSharesFromDatabase(eq(new ShareFilterDto(
                null,
                "moex_mrng_evng_e_wknd_dlr",
                null,
                null,
                null,
                null,
                null
            ))))
                .thenReturn(TestDataFactory.createMoexSharesList());
        });

        // Шаг 2: Выполнение HTTP запроса к базе данных
        Allure.step("Выполнение HTTP запроса к базе данных", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "database")
                    .param("exchange", "moex_mrng_evng_e_wknd_dlr"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].figi").value("BBG004730N88"))
                    .andExpect(jsonPath("$[0].ticker").value("SBER"))
                    .andExpect(jsonPath("$[0].name").value("ПАО Сбербанк"))
                    .andExpect(jsonPath("$[0].currency").value("RUB"))
                    .andExpect(jsonPath("$[0].exchange").value("moex_mrng_evng_e_wknd_dlr"))
                    .andExpect(jsonPath("$[1].ticker").value("GAZP"))
                    .andExpect(jsonPath("$[2].ticker").value("LKOH"))
                    .andExpect(jsonPath("$[1].exchange").value("moex_mrng_evng_e_wknd_dlr"))
                    .andExpect(jsonPath("$[2].exchange").value("moex_mrng_evng_e_wknd_dlr"));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для базы данных", () -> {
            verify(instrumentService).getSharesFromDatabase(eq(new ShareFilterDto(
                null,
                "moex_mrng_evng_e_wknd_dlr",
                null,
                null,
                null,
                null,
                null
            )));
        });
    }
    // ==================== ТЕСТЫ ДЛЯ ПОЛУЧЕНИЯ АКЦИИ ПО IDENTIFIER ====================

    @Test
    @DisplayName("Получение акции по FIGI - успешный случай")
    @Description("Тест проверяет корректность получения акции по FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("identifier")
    @Tag("figi")
    void getShareByIdentifier_ShouldReturnShare_WhenValidFigiProvided() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для поиска по FIGI
        Allure.step("Настройка моков сервиса для поиска по FIGI", () -> {
            when(instrumentService.getShareByFigi("BBG004730N88"))
                .thenReturn(TestDataFactory.createSberShare());
        });

        // Шаг 2: Выполнение HTTP запроса по FIGI
        Allure.step("Выполнение HTTP запроса по FIGI", () -> {
            mockMvc.perform(get("/api/instruments/shares/BBG004730N88")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.figi").value("BBG004730N88"))
                    .andExpect(jsonPath("$.ticker").value("SBER"))
                    .andExpect(jsonPath("$.name").value("ПАО Сбербанк"))
                    .andExpect(jsonPath("$.currency").value("RUB"))
                    .andExpect(jsonPath("$.exchange").value("moex_mrng_evng_e_wknd_dlr"));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для поиска по FIGI", () -> {
            verify(instrumentService).getShareByFigi("BBG004730N88");
            verify(instrumentService, never()).getShareByTicker(any());
        });
    }

    @Test
    @DisplayName("Получение акции по тикеру - успешный случай")
    @Description("Тест проверяет корректность получения акции по тикеру")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("identifier")
    @Tag("ticker")
    void getShareByIdentifier_ShouldReturnShare_WhenValidTickerProvided() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для поиска по тикеру
        Allure.step("Настройка моков сервиса для поиска по тикеру", () -> {
            when(instrumentService.getShareByTicker("SBER"))
                .thenReturn(TestDataFactory.createSberShare());
        });

        // Шаг 2: Выполнение HTTP запроса по тикеру
        Allure.step("Выполнение HTTP запроса по тикеру", () -> {
            mockMvc.perform(get("/api/instruments/shares/SBER")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.figi").value("BBG004730N88"))
                    .andExpect(jsonPath("$.ticker").value("SBER"))
                    .andExpect(jsonPath("$.name").value("ПАО Сбербанк"));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для поиска по тикеру", () -> {
            verify(instrumentService, never()).getShareByFigi(any());
            verify(instrumentService).getShareByTicker("SBER");
        });
    }

    @Test
    @DisplayName("Получение акции по несуществующему identifier - 404")
    @Description("Тест проверяет поведение контроллера при поиске несуществующей акции")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("identifier")
    @Tag("negative")
    void getShareByIdentifier_ShouldReturn404_WhenShareNotFound() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для несуществующих акций
        Allure.step("Настройка моков сервиса для несуществующих акций", () -> {
            when(instrumentService.getShareByFigi("INVALID_FIGI"))
                .thenReturn(null);
            when(instrumentService.getShareByTicker("INVALID_TICKER"))
                .thenReturn(null);
        });

        // Шаг 2: Выполнение HTTP запроса с несуществующим FIGI
        Allure.step("Выполнение HTTP запроса с несуществующим FIGI", () -> {
            mockMvc.perform(get("/api/instruments/shares/INVALID_FIGI")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("NotFound"))
                    .andExpect(jsonPath("$.type").value("FIGI"))
                    .andExpect(jsonPath("$.identifier").value("INVALID_FIGI"));
        });

        // Шаг 3: Выполнение HTTP запроса с несуществующим тикером
        Allure.step("Выполнение HTTP запроса с несуществующим тикером", () -> {
            mockMvc.perform(get("/api/instruments/shares/INVALID_TICKER")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("NotFound"))
                    .andExpect(jsonPath("$.type").value("FIGI"))
                    .andExpect(jsonPath("$.identifier").value("INVALID_TICKER"));
        });

        // Шаг 4: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для несуществующих акций", () -> {
            verify(instrumentService).getShareByFigi("INVALID_FIGI");
            verify(instrumentService).getShareByFigi("INVALID_TICKER");
            verify(instrumentService, never()).getShareByTicker(any());
        });
    }

    @Test
    @DisplayName("Получение акции по FIGI с подчеркиванием")
    @Description("Тест проверяет определение FIGI по наличию подчеркивания")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("identifier")
    @Tag("figi")
    void getShareByIdentifier_ShouldSearchByFigi_WhenContainsUnderscore() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для FIGI с подчеркиванием
        Allure.step("Настройка моков сервиса для FIGI с подчеркиванием", () -> {
            when(instrumentService.getShareByFigi("BBG_004730_N88"))
                .thenReturn(TestDataFactory.createSberShare());
        });

        // Шаг 2: Выполнение HTTP запроса с FIGI с подчеркиванием
        Allure.step("Выполнение HTTP запроса с FIGI с подчеркиванием", () -> {
            mockMvc.perform(get("/api/instruments/shares/BBG_004730_N88")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.figi").value("BBG004730N88"))
                    .andExpect(jsonPath("$.ticker").value("SBER"));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для FIGI с подчеркиванием", () -> {
            verify(instrumentService).getShareByFigi("BBG_004730_N88");
            verify(instrumentService, never()).getShareByTicker(any());
        });
    }

    // ==================== ТЕСТЫ ДЛЯ СОХРАНЕНИЯ АКЦИЙ ====================

    @Test
    @DisplayName("Сохранение акций по фильтру - успешный случай")
    @Description("Тест проверяет корректность сохранения акций по фильтру")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("save")
    @Tag("post")
    void saveShares_ShouldReturnSaveResponse_WhenValidFilterProvided() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для сохранения акций
        Allure.step("Настройка моков сервиса для сохранения акций", () -> {
            when(instrumentService.saveShares(any(ShareFilterDto.class)))
                .thenReturn(TestDataFactory.createTestSaveResponse());
        });

        // Шаг 2: Выполнение POST запроса для сохранения акций
        Allure.step("Выполнение POST запроса для сохранения акций", () -> {
            mockMvc.perform(post("/api/instruments/shares")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"exchange\":\"moex_mrng_evng_e_wknd_dlr\",\"currency\":\"RUB\",\"status\":\"INSTRUMENT_STATUS_BASE\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Успешно сохранено 5 новых инструментов из 10 найденных"))
                    .andExpect(jsonPath("$.totalRequested").value(10))
                    .andExpect(jsonPath("$.newItemsSaved").value(5))
                    .andExpect(jsonPath("$.existingItemsSkipped").value(5));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для сохранения акций", () -> {
            verify(instrumentService).saveShares(any(ShareFilterDto.class));
        });
    }

    @Test
    @DisplayName("Сохранение акций с пустым фильтром")
    @Description("Тест проверяет поведение контроллера при сохранении с пустым фильтром")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("save")
    @Tag("negative")
    void saveShares_ShouldHandleEmptyFilter_WhenEmptyFilterProvided() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для пустого фильтра
        Allure.step("Настройка моков сервиса для пустого фильтра", () -> {
            when(instrumentService.saveShares(any(ShareFilterDto.class)))
                .thenReturn(TestDataFactory.createTestSaveResponse());
        });

        // Шаг 2: Выполнение POST запроса с пустым фильтром
        Allure.step("Выполнение POST запроса с пустым фильтром", () -> {
            mockMvc.perform(post("/api/instruments/shares")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для пустого фильтра", () -> {
            verify(instrumentService).saveShares(any(ShareFilterDto.class));
        });
    }

    // ==================== ТЕСТЫ ДЛЯ ФЬЮЧЕРСОВ ====================

    @Test
    @DisplayName("Получение списка фьючерсов через API - успешный случай")
    @Description("Тест проверяет корректность получения списка фьючерсов через API")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Фьючерсов")
    @Tag("unit")
    @Tag("futures")
    @Tag("api")
    @Tag("positive")
    void getFutures_ShouldReturnFuturesList_WhenApiSourceIsUsed() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        List<FutureDto> testFutures = Allure.step("Подготовка тестовых данных для фьючерсов", () -> {
            return TestDataFactory.createCommodityFuturesList();
        });
        
        // Шаг 2: Настройка моков сервиса
        Allure.step("Настройка моков сервиса для фьючерсов", () -> {
            when(instrumentService.getFutures(
                eq("INSTRUMENT_STATUS_BASE"),
                eq("moex_mrng_evng_e_wknd_dlr"),
                eq("USD"),
                isNull(),
                eq("TYPE_COMMODITY")
            )).thenReturn(testFutures);
        });
        
        // Шаг 3: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа для фьючерсов", () -> {
            mockMvc.perform(get("/api/instruments/futures")
                    .param("status", "INSTRUMENT_STATUS_BASE")
                    .param("exchange", "moex_mrng_evng_e_wknd_dlr")
                    .param("currency", "USD")
                    .param("assetType", "TYPE_COMMODITY")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].figi").value("FUTSI0624000"))
                    .andExpect(jsonPath("$[0].ticker").value("SI0624"))
                    .andExpect(jsonPath("$[0].assetType").value("COMMODITY"))
                    .andExpect(jsonPath("$[0].shortEnabled").value(true))
                    .andExpect(jsonPath("$[0].expirationDate").exists())
                    .andExpect(jsonPath("$[1].ticker").value("GZ0624"));
        });
        
        // Шаг 4: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для фьючерсов", () -> {
            verify(instrumentService).getFutures(
                eq("INSTRUMENT_STATUS_BASE"),
                eq("moex_mrng_evng_e_wknd_dlr"),
                eq("USD"),
                isNull(),
                eq("TYPE_COMMODITY")
            );
        });
    }


    @Test
    @DisplayName("Получение фьючерса по FIGI - успешный случай")
    @Description("Тест проверяет корректность получения фьючерса по FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures API")
    @Tag("api")
    @Tag("futures")
    @Tag("identifier")
    @Tag("figi")
    void getFutureByIdentifier_ShouldReturnFuture_WhenValidFigiProvided() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для поиска фьючерса по FIGI
        Allure.step("Настройка моков сервиса для поиска фьючерса по FIGI", () -> {
            when(instrumentService.getFutureByFigi("FUTSI0624000"))
                .thenReturn(TestDataFactory.createSilverFuture());
        });

        // Шаг 2: Выполнение HTTP запроса по FIGI фьючерса
        Allure.step("Выполнение HTTP запроса по FIGI фьючерса", () -> {
            mockMvc.perform(get("/api/instruments/futures/FUTSI0624000")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.figi").value("FUTSI0624000"))
                    .andExpect(jsonPath("$.ticker").value("SI0624"))
                    .andExpect(jsonPath("$.assetType").value("COMMODITY"))
                    .andExpect(jsonPath("$.basicAsset").value("Silver"))
                    .andExpect(jsonPath("$.currency").value("USD"))
                    .andExpect(jsonPath("$.exchange").value("moex_mrng_evng_e_wknd_dlr"));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для поиска фьючерса по FIGI", () -> {
            verify(instrumentService).getFutureByFigi("FUTSI0624000");
            verify(instrumentService, never()).getFutureByTicker(any());
        });
    }

    @Test
    @DisplayName("Получение фьючерса по тикеру - успешный случай")
    @Description("Тест проверяет корректность получения фьючерса по тикеру")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures API")
    @Tag("api")
    @Tag("futures")
    @Tag("identifier")
    @Tag("ticker")
    void getFutureByIdentifier_ShouldReturnFuture_WhenValidTickerProvided() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для поиска фьючерса по тикеру
        Allure.step("Настройка моков сервиса для поиска фьючерса по тикеру", () -> {
            when(instrumentService.getFutureByTicker("SI0624"))
                .thenReturn(TestDataFactory.createSilverFuture());
        });

        // Шаг 2: Выполнение HTTP запроса по тикеру фьючерса
        Allure.step("Выполнение HTTP запроса по тикеру фьючерса", () -> {
            mockMvc.perform(get("/api/instruments/futures/SI0624")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.figi").value("FUTSI0624000"))
                    .andExpect(jsonPath("$.ticker").value("SI0624"))
                    .andExpect(jsonPath("$.assetType").value("COMMODITY"));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для поиска фьючерса по тикеру", () -> {
            verify(instrumentService, never()).getFutureByFigi(any());
            verify(instrumentService).getFutureByTicker("SI0624");
        });
    }

    @Test
    @DisplayName("Получение фьючерса по несуществующему identifier - 404")
    @Description("Тест проверяет поведение контроллера при поиске несуществующего фьючерса")
    @Severity(SeverityLevel.NORMAL)
    @Story("Futures API")
    @Tag("api")
    @Tag("futures")
    @Tag("identifier")
    @Tag("negative")
    void getFutureByIdentifier_ShouldReturn404_WhenFutureNotFound() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для несуществующего фьючерса
        Allure.step("Настройка моков сервиса для несуществующего фьючерса", () -> {
            when(instrumentService.getFutureByFigi("INVALID_FUTURE"))
                .thenReturn(null);
        });

        // Шаг 2: Выполнение HTTP запроса с несуществующим фьючерсом
        Allure.step("Выполнение HTTP запроса с несуществующим фьючерсом", () -> {
            mockMvc.perform(get("/api/instruments/futures/INVALID_FUTURE")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("NotFound"))
                    .andExpect(jsonPath("$.type").value("FIGI"))
                    .andExpect(jsonPath("$.identifier").value("INVALID_FUTURE"));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для несуществующего фьючерса", () -> {
            verify(instrumentService).getFutureByFigi("INVALID_FUTURE");
            verify(instrumentService, never()).getFutureByTicker(any());
        });
    }

    @Test
    @DisplayName("Сохранение фьючерсов по фильтру - успешный случай")
    @Description("Тест проверяет корректность сохранения фьючерсов по фильтру")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures API")
    @Tag("api")
    @Tag("futures")
    @Tag("save")
    @Tag("post")
    void saveFutures_ShouldReturnSaveResponse_WhenValidFilterProvided() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для сохранения фьючерсов
        Allure.step("Настройка моков сервиса для сохранения фьючерсов", () -> {
            when(instrumentService.saveFutures(any(FutureFilterDto.class)))
                .thenReturn(TestDataFactory.createTestSaveResponse());
        });

        // Шаг 2: Выполнение POST запроса для сохранения фьючерсов
        Allure.step("Выполнение POST запроса для сохранения фьючерсов", () -> {
            mockMvc.perform(post("/api/instruments/futures")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"exchange\":\"moex_mrng_evng_e_wknd_dlr\",\"currency\":\"USD\",\"assetType\":\"COMMODITY\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Успешно сохранено 5 новых инструментов из 10 найденных"))
                    .andExpect(jsonPath("$.totalRequested").value(10))
                    .andExpect(jsonPath("$.newItemsSaved").value(5));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для сохранения фьючерсов", () -> {
            verify(instrumentService).saveFutures(any(FutureFilterDto.class));
        });
    }

    // ==================== ТЕСТЫ ДЛЯ ИНДИКАТИВОВ ====================

    @Test
    @DisplayName("Получение списка индикативов через API - успешный случай")
    @Description("Тест проверяет корректность получения списка индикативов через API")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Индикативов")
    @Tag("unit")
    @Tag("indicatives")
    @Tag("api")
    @Tag("positive")
    void getIndicatives_ShouldReturnIndicativesList_WhenApiSourceIsUsed() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        List<IndicativeDto> testIndicatives = Allure.step("Подготовка тестовых данных для индикативов", () -> {
            return TestDataFactory.createCurrencyIndicativesList();
        });
        
        // Шаг 2: Настройка моков сервиса
        Allure.step("Настройка моков сервиса для индикативов", () -> {
            when(instrumentService.getIndicatives(
                eq("moex_mrng_evng_e_wknd_dlr"),
                eq("RUB"),
                isNull(),
                isNull()
            )).thenReturn(testIndicatives);
        });
        
        // Шаг 3: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа для индикативов", () -> {
            mockMvc.perform(get("/api/instruments/indicatives")
                    .param("exchange", "moex_mrng_evng_e_wknd_dlr")
                    .param("currency", "RUB")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].figi").value("BBG0013HGFT4"))
                    .andExpect(jsonPath("$[0].ticker").value("USD000UTSTOM"))
                    .andExpect(jsonPath("$[0].sellAvailableFlag").value(true))
                    .andExpect(jsonPath("$[0].buyAvailableFlag").value(true))
                    .andExpect(jsonPath("$[1].ticker").value("EUR000UTSTOM"));
        });
        
        // Шаг 4: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для индикативов", () -> {
            verify(instrumentService).getIndicatives(
                eq("moex_mrng_evng_e_wknd_dlr"),
                eq("RUB"),
                isNull(),
                isNull()
            );
        });
    }

    @Test
    @DisplayName("Получение индикатива по FIGI - успешный случай (короткий FIGI)")
    @Description("Тест проверяет корректность получения индикатива по FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("identifier")
    @Tag("figi")
    void getIndicativeByIdentifier_ShouldReturnIndicative_WhenValidShortFigiProvided() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для поиска индикатива по FIGI
        Allure.step("Настройка моков сервиса для поиска индикатива по FIGI", () -> {
            when(instrumentService.getIndicativeBy("BBG0013HGFT4"))
                .thenReturn(TestDataFactory.createUsdRubIndicative());
        });

        // Шаг 2: Выполнение HTTP запроса по FIGI индикатива
        Allure.step("Выполнение HTTP запроса по FIGI индикатива", () -> {
            mockMvc.perform(get("/api/instruments/indicatives/BBG0013HGFT4")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.figi").value("BBG0013HGFT4"))
                    .andExpect(jsonPath("$.ticker").value("USD000UTSTOM"))
                    .andExpect(jsonPath("$.name").value("Доллар США / Российский рубль"))
                    .andExpect(jsonPath("$.currency").value("RUB"))
                    .andExpect(jsonPath("$.exchange").value("moex_mrng_evng_e_wknd_dlr"))
                    .andExpect(jsonPath("$.classCode").value("CURRENCY"))
                    .andExpect(jsonPath("$.sellAvailableFlag").value(true))
                    .andExpect(jsonPath("$.buyAvailableFlag").value(true));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для поиска индикатива по FIGI", () -> {
            verify(instrumentService).getIndicativeBy("BBG0013HGFT4");
            verify(instrumentService, never()).getIndicativeByTicker(any());
        });
    }

    @Test
    @DisplayName("Получение индикатива по FIGI - успешный случай (длинный идентификатор)")
    @Description("Тест проверяет корректность получения индикатива по FIGI (длинный идентификатор)")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("identifier")
    @Tag("figi")
    void getIndicativeByIdentifier_ShouldReturnIndicative_WhenValidFigiProvided() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для длинного идентификатора
        Allure.step("Настройка моков сервиса для длинного идентификатора", () -> {
            when(instrumentService.getIndicativeBy("USD000UTSTOM"))
                .thenReturn(TestDataFactory.createUsdRubIndicative());
        });

        // Шаг 2: Выполнение HTTP запроса с длинным идентификатором
        Allure.step("Выполнение HTTP запроса с длинным идентификатором", () -> {
            mockMvc.perform(get("/api/instruments/indicatives/USD000UTSTOM")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.figi").value("BBG0013HGFT4"))
                    .andExpect(jsonPath("$.ticker").value("USD000UTSTOM"))
                    .andExpect(jsonPath("$.name").value("Доллар США / Российский рубль"));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для длинного идентификатора", () -> {
            verify(instrumentService).getIndicativeBy("USD000UTSTOM");
            verify(instrumentService, never()).getIndicativeByTicker(any());
        });
    }

    @Test
    @DisplayName("Получение индикатива по тикеру - успешный случай")
    @Description("Тест проверяет корректность получения индикатива по тикеру")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("identifier")
    @Tag("ticker")
    void getIndicativeByIdentifier_ShouldReturnIndicative_WhenValidTickerProvided() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для поиска индикатива по тикеру
        Allure.step("Настройка моков сервиса для поиска индикатива по тикеру", () -> {
            when(instrumentService.getIndicativeByTicker("RTSI"))
                .thenReturn(TestDataFactory.createUsdRubIndicative());
        });

        // Шаг 2: Выполнение HTTP запроса по тикеру индикатива
        Allure.step("Выполнение HTTP запроса по тикеру индикатива", () -> {
            mockMvc.perform(get("/api/instruments/indicatives/RTSI")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.figi").value("BBG0013HGFT4"))
                    .andExpect(jsonPath("$.ticker").value("USD000UTSTOM"))
                    .andExpect(jsonPath("$.name").value("Доллар США / Российский рубль"));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для поиска индикатива по тикеру", () -> {
            verify(instrumentService, never()).getIndicativeBy(any());
            verify(instrumentService).getIndicativeByTicker("RTSI");
        });
    }

    @Test
    @DisplayName("Получение индикатива по несуществующему identifier - 404")
    @Description("Тест проверяет поведение контроллера при поиске несуществующего индикатива")
    @Severity(SeverityLevel.NORMAL)
    @Story("Indicatives API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("identifier")
    @Tag("negative")
    void getIndicativeByIdentifier_ShouldReturn404_WhenIndicativeNotFound() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для несуществующего индикатива
        Allure.step("Настройка моков сервиса для несуществующего индикатива", () -> {
            when(instrumentService.getIndicativeBy("INVALID_INDICATIVE"))
                .thenReturn(null);
        });

        // Шаг 2: Выполнение HTTP запроса с несуществующим индикативом
        Allure.step("Выполнение HTTP запроса с несуществующим индикативом", () -> {
            mockMvc.perform(get("/api/instruments/indicatives/INVALID_INDICATIVE")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("NotFound"))
                    .andExpect(jsonPath("$.type").value("FIGI"))
                    .andExpect(jsonPath("$.identifier").value("INVALID_INDICATIVE"));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для несуществующего индикатива", () -> {
            verify(instrumentService).getIndicativeBy("INVALID_INDICATIVE");
            verify(instrumentService, never()).getIndicativeByTicker(any());
        });
    }

    @Test
    @DisplayName("Сохранение индикативов по фильтру - успешный случай")
    @Description("Тест проверяет корректность сохранения индикативов по фильтру")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("save")
    @Tag("post")
    void saveIndicatives_ShouldReturnSaveResponse_WhenValidFilterProvided() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для сохранения индикативов
        Allure.step("Настройка моков сервиса для сохранения индикативов", () -> {
            when(instrumentService.saveIndicatives(any(IndicativeFilterDto.class)))
                .thenReturn(TestDataFactory.createTestSaveResponse());
        });

        // Шаг 2: Выполнение POST запроса для сохранения индикативов
        Allure.step("Выполнение POST запроса для сохранения индикативов", () -> {
            mockMvc.perform(post("/api/instruments/indicatives")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"exchange\":\"moex_mrng_evng_e_wknd_dlr\",\"currency\":\"RUB\",\"ticker\":\"USD000UTSTOM\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Успешно сохранено 5 новых инструментов из 10 найденных"))
                    .andExpect(jsonPath("$.totalRequested").value(10))
                    .andExpect(jsonPath("$.newItemsSaved").value(5));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для сохранения индикативов", () -> {
            verify(instrumentService).saveIndicatives(any(IndicativeFilterDto.class));
        });
    }

    // ==================== ТЕСТЫ ДЛЯ СТАТИСТИКИ ====================

    @Test
    @DisplayName("Получение статистики инструментов")
    @Description("Тест проверяет корректность получения статистики по количеству инструментов")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Статистики")
    @Tag("unit")
    @Tag("statistics")
    @Tag("api")
    @Tag("positive")
    void getInstrumentCounts_ShouldReturnStatistics_WhenRequested() throws Exception {
        
        // Шаг 1: Подготовка тестовых данных
        Map<String, Long> testCounts = Allure.step("Подготовка тестовой статистики", () -> {
            return TestDataFactory.createTestInstrumentCounts();
        });
        
        // Шаг 2: Настройка моков сервиса
        Allure.step("Настройка моков сервиса для статистики", () -> {
            when(instrumentService.getInstrumentCounts())
                .thenReturn(testCounts);
        });
        
        // Шаг 3: Выполнение HTTP запроса и проверка ответа
        Allure.step("Выполнение HTTP запроса и проверка ответа для статистики", () -> {
            mockMvc.perform(get("/api/instruments/count")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.shares").value(150))
                    .andExpect(jsonPath("$.futures").value(45))
                    .andExpect(jsonPath("$.indicatives").value(12))
                    .andExpect(jsonPath("$.total").value(207));
        });
        
        // Шаг 4: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для статистики", () -> {
            verify(instrumentService).getInstrumentCounts();
        });
    }


    // ==================== ДОПОЛНИТЕЛЬНЫЕ НЕГАТИВНЫЕ ТЕСТЫ ====================

    @Test
    @DisplayName("Получение списка акций с некорректным статусом")
    @Description("Тест проверяет поведение контроллера при передаче некорректного статуса")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Акций")
    @Tag("unit")
    @Tag("shares")
    @Tag("api")
    @Tag("negative")
    @Tag("validation")
    @Tag("status")
    void getShares_ShouldHandleInvalidStatus_WhenInvalidStatusProvided() throws Exception {
        
        // Шаг 1: Выполнение HTTP запроса с некорректным статусом
        Allure.step("Выполнение HTTP запроса с некорректным статусом", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "api")
                    .param("status", "INVALID_STATUS")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("ValidationException"))
                    .andExpect(jsonPath("$.message").value(containsString("Невалидный статус инструмента")));
        });
        
        // Шаг 2: Проверка отсутствия вызовов сервиса
        Allure.step("Проверка отсутствия вызовов сервиса для некорректного статуса", () -> {
            verify(instrumentService, never()).getShares(any(), any(), any(), any(), any());
        });
    }

    @Test
    @DisplayName("Получение списка акций с некорректной биржей")
    @Description("Тест проверяет поведение контроллера при передаче некорректной биржи")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("negative")
    @Tag("validation")
    @Tag("exchange")
    void getShares_ShouldHandleInvalidExchange_WhenInvalidExchangeProvided() throws Exception {
        
        // Шаг 1: Выполнение HTTP запроса с некорректной биржей
        Allure.step("Выполнение HTTP запроса с некорректной биржей", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "api")
                    .param("exchange", "INVALID_EXCHANGE")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("ValidationException"))
                    .andExpect(jsonPath("$.message").value(containsString("Невалидный тип биржи")));
        });

        // Шаг 2: Проверка отсутствия вызовов сервиса
        Allure.step("Проверка отсутствия вызовов сервиса для некорректной биржи", () -> {
            verify(instrumentService, never()).getShares(any(), any(), any(), any(), any());
        });
    }

    @Test
    @DisplayName("Получение списка акций с некорректной валютой")
    @Description("Тест проверяет поведение контроллера при передаче некорректной валюты")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("negative")
    @Tag("validation")
    @Tag("currency")
    void getShares_ShouldHandleInvalidCurrency_WhenInvalidCurrencyProvided() throws Exception {
        
        // Шаг 1: Выполнение HTTP запроса с некорректной валютой
        Allure.step("Выполнение HTTP запроса с некорректной валютой", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "api")
                    .param("currency", "INVALID_CURRENCY")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("ValidationException"))
                    .andExpect(jsonPath("$.message").value(containsString("Невалидная валюта")));
        });

        // Шаг 2: Проверка отсутствия вызовов сервиса
        Allure.step("Проверка отсутствия вызовов сервиса для некорректной валюты", () -> {
            verify(instrumentService, never()).getShares(any(), any(), any(), any(), any());
        });
    }

    @Test
    @DisplayName("Получение списка акций через database с параметрами")
    @Description("Тест проверяет корректность получения акций из БД с параметрами")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares database")
    @Tag("database")
    @Tag("shares")
    @Tag("filter")
    void getSharesFromDatabase_ShouldReturnSharesList_WhenParametersProvided() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для БД с параметрами
        Allure.step("Настройка моков сервиса для БД с параметрами", () -> {
            when(instrumentService.getSharesFromDatabase(any(ShareFilterDto.class)))
                .thenReturn(TestDataFactory.createMoexSharesList());
        });

        // Шаг 2: Выполнение HTTP запроса к БД с параметрами
        Allure.step("Выполнение HTTP запроса к БД с параметрами", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "database")
                    .param("exchange", "moex_mrng_evng_e_wknd_dlr")
                    .param("currency", "RUB")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].ticker").value("SBER"))
                    .andExpect(jsonPath("$[1].ticker").value("GAZP"))
                    .andExpect(jsonPath("$[2].ticker").value("LKOH"));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для БД с параметрами", () -> {
            verify(instrumentService).getSharesFromDatabase(any(ShareFilterDto.class));
        });
    }

    @Test
    @DisplayName("Получение списка фьючерсов без параметров")
    @Description("Тест проверяет поведение API фьючерсов при отсутствии параметров")
    @Severity(SeverityLevel.NORMAL)
    @Story("Futures API")
    @Tag("api")
    @Tag("futures")
    @Tag("no-params")
    void getFutures_ShouldReturnFuturesList_WhenNoParametersProvided() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для фьючерсов без параметров
        Allure.step("Настройка моков сервиса для фьючерсов без параметров", () -> {
            when(instrumentService.getFutures(isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(TestDataFactory.createCommodityFuturesList());
        });

        // Шаг 2: Выполнение HTTP запроса без параметров
        Allure.step("Выполнение HTTP запроса без параметров", () -> {
            mockMvc.perform(get("/api/instruments/futures")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].ticker").value("SI0624"))
                    .andExpect(jsonPath("$[1].ticker").value("GZ0624"));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для фьючерсов без параметров", () -> {
            verify(instrumentService).getFutures(isNull(), isNull(), isNull(), isNull(), isNull());
        });
    }

    @Test
    @DisplayName("Получение списка индикативов без параметров")
    @Description("Тест проверяет поведение API индикативов при отсутствии параметров")
    @Severity(SeverityLevel.NORMAL)
    @Story("Indicatives API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("no-params")
    void getIndicatives_ShouldReturnIndicativesList_WhenNoParametersProvided() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для индикативов без параметров
        Allure.step("Настройка моков сервиса для индикативов без параметров", () -> {
            when(instrumentService.getIndicatives(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(TestDataFactory.createCurrencyIndicativesList());
        });

        // Шаг 2: Выполнение HTTP запроса без параметров
        Allure.step("Выполнение HTTP запроса без параметров", () -> {
            mockMvc.perform(get("/api/instruments/indicatives")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].ticker").value("USD000UTSTOM"))
                    .andExpect(jsonPath("$[1].ticker").value("EUR000UTSTOM"));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для индикативов без параметров", () -> {
            verify(instrumentService).getIndicatives(isNull(), isNull(), isNull(), isNull());
        });
    }

    @Test
    @DisplayName("Получение акции по FIGI с дефисом")
    @Description("Тест проверяет определение FIGI по наличию дефиса")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("identifier")
    @Tag("figi")
    void getShareByIdentifier_ShouldSearchByFigi_WhenContainsDash() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для FIGI с дефисом
        Allure.step("Настройка моков сервиса для FIGI с дефисом", () -> {
            when(instrumentService.getShareByFigi("BBG-004730-N88"))
                .thenReturn(TestDataFactory.createSberShare());
        });

        // Шаг 2: Выполнение HTTP запроса с FIGI с дефисом
        Allure.step("Выполнение HTTP запроса с FIGI с дефисом", () -> {
            mockMvc.perform(get("/api/instruments/shares/BBG-004730-N88")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.figi").value("BBG004730N88"))
                    .andExpect(jsonPath("$.ticker").value("SBER"));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для FIGI с дефисом", () -> {
            verify(instrumentService).getShareByFigi("BBG-004730-N88");
            verify(instrumentService, never()).getShareByTicker(any());
        });
    }

    @Test
    @DisplayName("Получение фьючерса по FIGI с дефисом")
    @Description("Тест проверяет определение FIGI фьючерса по наличию дефиса")
    @Severity(SeverityLevel.NORMAL)
    @Story("Futures API")
    @Tag("api")
    @Tag("futures")
    @Tag("identifier")
    @Tag("figi")
    void getFutureByIdentifier_ShouldSearchByFigi_WhenContainsDash() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для FIGI фьючерса с дефисом
        Allure.step("Настройка моков сервиса для FIGI фьючерса с дефисом", () -> {
            when(instrumentService.getFutureByFigi("FUT-SI0624-000"))
                .thenReturn(TestDataFactory.createSilverFuture());
        });

        // Шаг 2: Выполнение HTTP запроса с FIGI фьючерса с дефисом
        Allure.step("Выполнение HTTP запроса с FIGI фьючерса с дефисом", () -> {
            mockMvc.perform(get("/api/instruments/futures/FUT-SI0624-000")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.figi").value("FUTSI0624000"))
                    .andExpect(jsonPath("$.ticker").value("SI0624"))
                    .andExpect(jsonPath("$.assetType").value("COMMODITY"));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для FIGI фьючерса с дефисом", () -> {
            verify(instrumentService).getFutureByFigi("FUT-SI0624-000");
            verify(instrumentService, never()).getFutureByTicker(any());
        });
    }

    @Test
    @DisplayName("Получение индикатива по FIGI с дефисом")
    @Description("Тест проверяет определение FIGI индикатива по наличию дефиса")
    @Severity(SeverityLevel.NORMAL)
    @Story("Indicatives API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("identifier")
    @Tag("figi")
    void getIndicativeByIdentifier_ShouldSearchByFigi_WhenContainsDash() throws Exception {
        
        // Шаг 1: Настройка моков сервиса для FIGI индикатива с дефисом
        Allure.step("Настройка моков сервиса для FIGI индикатива с дефисом", () -> {
            when(instrumentService.getIndicativeBy("BBG-0013HG-FT4"))
                .thenReturn(TestDataFactory.createUsdRubIndicative());
        });

        // Шаг 2: Выполнение HTTP запроса с FIGI индикатива с дефисом
        Allure.step("Выполнение HTTP запроса с FIGI индикатива с дефисом", () -> {
            mockMvc.perform(get("/api/instruments/indicatives/BBG-0013HG-FT4")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.figi").value("BBG0013HGFT4"))
                    .andExpect(jsonPath("$.ticker").value("USD000UTSTOM"))
                    .andExpect(jsonPath("$.name").value("Доллар США / Российский рубль"));
        });

        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса для FIGI индикатива с дефисом", () -> {
            verify(instrumentService).getIndicativeBy("BBG-0013HG-FT4");
            verify(instrumentService, never()).getIndicativeByTicker(any());
        });
    }
}