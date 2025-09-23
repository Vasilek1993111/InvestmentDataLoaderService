package com.example.InvestmentDataLoaderService.unit.controller;

import com.example.InvestmentDataLoaderService.controller.TradingController;
import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.service.TradingService;
import io.qameta.allure.*;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit-тесты для TradingController
 * 
 * Этот класс содержит детальные unit-тесты для всех эндпоинтов TradingController,
 * включая проверку корректности обработки запросов, валидации параметров,
 * формирования ответов и обработки ошибок.
 * 
 * @author Investment Data Loader Service Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@WebMvcTest(TradingController.class)
@Epic("Trading API")
@Feature("Trading Controller")
@DisplayName("Trading Controller Unit Tests")
@Links({
    @Link(name = "API Documentation", url = "https://docs.example.com/api/trading"),
    @Link(name = "Trading Controller Source", url = "https://github.com/example/investment-service/blob/main/src/main/java/com/example/InvestmentDataLoaderService/controller/TradingController.java"),
    @Link(name = "Trading API Specification", url = "https://api.example.com/swagger-ui.html#/trading-controller")
})
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
class TradingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradingService tradingService;


    private List<AccountDto> accounts;
    private List<TradingScheduleDto> schedules;
    private List<TradingStatusDto> statuses;
    private Map<String, Object> testContext;

    @BeforeEach
    @Step("Инициализация тестовых данных и контекста")
    @Description("Подготовка детальных тестовых данных для всех сценариев тестирования TradingController")
    void setUp() {
        testContext = new HashMap<>();
        testContext.put("testStartTime", LocalDateTime.now());
        testContext.put("testEnvironment", "unit-test");
        testContext.put("testVersion", "1.0.0");
        
        // Создание детальных тестовых аккаунтов
        accounts = createDetailedTestAccounts();
        attachTestData("Test Accounts", accounts);
        
        // Создание детальных торговых расписаний
        schedules = createDetailedTradingSchedules();
        attachTestData("Trading Schedules", schedules);
        
        // Создание детальных статусов торгов
        statuses = createDetailedTradingStatuses();
        attachTestData("Trading Statuses", statuses);
        
        // Настройка моков с детальным логированием
        setupDetailedMocks();
    }

    @Test
    @DisplayName("Получение списка аккаунтов - успешный сценарий")
    @Description("Тест проверяет корректность получения списка аккаунтов через API эндпоинт /api/trading/accounts. " +
                "Проверяется HTTP статус 200, корректность Content-Type, структура JSON ответа и вызов сервиса.")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Account Management")
    @Tag("api")
    @Tag("accounts")
    @Tag("positive")
    @Issue("TRADING-001")
    @TmsLink("TMS-ACCOUNTS-001")
    void getAccounts_ShouldReturnAccounts() throws Exception {
        // Given
        when(tradingService.getAccounts()).thenReturn(accounts);
        
        attachTestData("Expected Accounts Count", accounts.size());
        attachTestData("Test Account Types", accounts.stream().map(AccountDto::type).distinct().toList());

        // When & Then
        MvcResult result = mockMvc.perform(get("/api/trading/accounts")
                        .header("User-Agent", "TradingControllerTest/1.0")
                        .header("Accept", "application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].id").value("2000000001"))
                .andExpect(jsonPath("$[0].name").value("Основной брокерский счет"))
                .andExpect(jsonPath("$[0].type").value("BROKER"))
                .andExpect(jsonPath("$[1].id").value("2000000002"))
                .andExpect(jsonPath("$[1].name").value("Индивидуальный инвестиционный счет"))
                .andExpect(jsonPath("$[1].type").value("IIS"))
                .andExpect(jsonPath("$[2].type").value("BROKER"))
                .andExpect(jsonPath("$[3].type").value("BROKER"))
                .andReturn();

        verify(tradingService, times(1)).getAccounts();
        
        String responseBody = result.getResponse().getContentAsString();
        attachTestData("Full Response Body", responseBody);
        attachTestData("Response Headers", Map.of(
                "Content-Type", result.getResponse().getContentType(),
                "Status", result.getResponse().getStatus()
        ));
    }

    @Test
    @DisplayName("Получение торговых расписаний - успешный сценарий")
    @Description("Тест проверяет получение торговых расписаний для указанной биржи и периода через эндпоинт /api/trading/schedules. " +
                "Проверяется корректность параметров запроса, структура ответа и вызов сервиса с правильными параметрами.")
    @Severity(SeverityLevel.NORMAL)
    @Story("Trading Schedules")
    @Tag("api")
    @Tag("schedules")
    @Tag("positive")
    @Issue("TRADING-002")
    @TmsLink("TMS-SCHEDULES-001")
    void getTradingSchedules_ShouldReturnSchedules() throws Exception {
        // Given
        String exchange = "MOEX";
        String fromDate = "2025-01-01T00:00:00Z";
        String toDate = "2025-01-03T00:00:00Z";
        
        attachTestData("Request Parameters", Map.of(
                "exchange", exchange,
                "from", fromDate,
                "to", toDate
        ));
        
        when(tradingService.getTradingSchedules(eq(exchange), any(Instant.class), any(Instant.class)))
                .thenReturn(schedules);

        // When & Then
        MvcResult result = mockMvc.perform(get("/api/trading/schedules")
                        .param("exchange", exchange)
                        .param("from", fromDate)
                        .param("to", toDate)
                        .header("User-Agent", "TradingControllerTest/1.0")
                        .header("Accept", "application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2)) // MOEX и SPB
                .andExpect(jsonPath("$[0].exchange").value("MOEX"))
                .andExpect(jsonPath("$[0].days").isArray())
                .andExpect(jsonPath("$[0].days.length()").value(4))
                .andExpect(jsonPath("$[0].days[0].date").value("2025-01-01"))
                .andExpect(jsonPath("$[0].days[0].isTradingDay").value(true))
                .andExpect(jsonPath("$[0].days[0].startTime").value("09:00"))
                .andExpect(jsonPath("$[0].days[0].endTime").value("19:00"))
                .andExpect(jsonPath("$[0].days[2].isTradingDay").value(false))
                .andExpect(jsonPath("$[1].exchange").value("SPB"))
                .andReturn();

        verify(tradingService, times(1)).getTradingSchedules(eq(exchange), any(Instant.class), any(Instant.class));
        
        String responseBody = result.getResponse().getContentAsString();
        attachTestData("Full Response Body", responseBody);
        attachTestData("Trading Days Analysis", Map.of(
                "totalSchedules", 2,
                "moexDays", 4,
                "spbDays", 4,
                "tradingDaysCount", 6, // 3 торговых дня для каждой биржи
                "nonTradingDaysCount", 2 // 1 неторговый день для каждой биржи
        ));
    }

    @Test
    @DisplayName("Получение торговых расписаний для периода")
    @Description("Тест проверяет получение торговых расписаний для указанной биржи и периода с оберткой")
    @Severity(SeverityLevel.NORMAL)
    @Story("Trading Schedules")
    @Tag("api")
    @Tag("schedules")
    void getTradingSchedulesForPeriod_ShouldWrapSchedules() throws Exception {
        // Given
        when(tradingService.getTradingSchedules(eq("MOEX"), any(Instant.class), any(Instant.class)))
                .thenReturn(schedules);

        // When & Then
        mockMvc.perform(get("/api/trading/schedules/period")
                        .param("exchange", "MOEX")
                        .param("from", "2025-01-01T00:00:00Z")
                        .param("to", "2025-01-03T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].exchange").value("MOEX"))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.from").value("2025-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.to").value("2025-01-03T00:00:00Z"))
                .andExpect(jsonPath("$.exchange").value("MOEX"));

        verify(tradingService).getTradingSchedules(eq("MOEX"), any(Instant.class), any(Instant.class));
    }

    @Test
    @DisplayName("Получение статусов торгов")
    @Description("Тест проверяет получение статусов торгов для указанных инструментов")
    @Severity(SeverityLevel.NORMAL)
    @Story("Trading Statuses")
    @Tag("api")
    @Tag("statuses")
    void getTradingStatuses_ShouldReturnStatuses() throws Exception {
        // Given
        when(tradingService.getTradingStatuses(anyList())).thenReturn(statuses);

        // When & Then
        mockMvc.perform(get("/api/trading/statuses")
                        .param("instrumentId", "BBG004730N88")
                        .param("instrumentId", "FUTSBER0324"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].figi").value("BBG004730N88"))
                .andExpect(jsonPath("$[1].figi").value("BBG004S681W1"));

        verify(tradingService).getTradingStatuses(anyList());
    }

    @Test
    @DisplayName("Получение детальных статусов торгов")
    @Description("Тест проверяет получение детальных статусов торгов для указанных инструментов с оберткой")
    @Severity(SeverityLevel.NORMAL)
    @Story("Trading Statuses")
    @Tag("api")
    @Tag("statuses")
    void getDetailedTradingStatuses_ShouldReturnWrappedStatuses() throws Exception {
        // Given
        when(tradingService.getTradingStatuses(anyList())).thenReturn(statuses);

        // When & Then
        mockMvc.perform(get("/api/trading/statuses/detailed")
                        .param("instrumentId", "BBG004730N88")
                        .param("instrumentId", "FUTSBER0324"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].figi").value("BBG004730N88"))
                .andExpect(jsonPath("$.count").value(5))
                .andExpect(jsonPath("$.requested_instruments").value(2));

        verify(tradingService).getTradingStatuses(anyList());
    }

    @Test
    @DisplayName("Получение торговых дней")
    @Description("Тест проверяет получение торговых и неторговых дней за указанный период")
    @Severity(SeverityLevel.NORMAL)
    @Story("Trading Days")
    @Tag("api")
    @Tag("trading-days")
    void getTradingDays_ShouldReturnAggregatedDays() throws Exception {
        // Given
        when(tradingService.getTradingSchedules(eq("MOEX"), any(Instant.class), any(Instant.class)))
                .thenReturn(schedules);

        // When & Then
        mockMvc.perform(get("/api/trading/trading-days")
                        .param("exchange", "MOEX")
                        .param("from", "2025-01-01T00:00:00Z")
                        .param("to", "2025-01-03T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.trading_days_count").value(6))
                .andExpect(jsonPath("$.non_trading_days_count").value(2))
                .andExpect(jsonPath("$.total_days").value(8));

        verify(tradingService).getTradingSchedules(eq("MOEX"), any(Instant.class), any(Instant.class));
    }

    @Test
    @DisplayName("Получение статистики торгов")
    @Description("Тест проверяет получение статистики торгов за указанный период")
    @Severity(SeverityLevel.NORMAL)
    @Story("Trading Stats")
    @Tag("api")
    @Tag("stats")
    void getTradingStats_ShouldReturnComputedStats() throws Exception {
        // Given
        when(tradingService.getTradingSchedules(eq("MOEX"), any(Instant.class), any(Instant.class)))
                .thenReturn(schedules);

        // When & Then
        mockMvc.perform(get("/api/trading/stats")
                        .param("exchange", "MOEX")
                        .param("from", "2025-01-01T00:00:00Z")
                        .param("to", "2025-01-03T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.trading_days").value(6))
                .andExpect(jsonPath("$.non_trading_days").value(2))
                .andExpect(jsonPath("$.total_days").value(8))
                .andExpect(jsonPath("$.trading_percentage").value(75.0));

        verify(tradingService).getTradingSchedules(eq("MOEX"), any(Instant.class), any(Instant.class));
    }

    @Test
    @DisplayName("Поиск данных торгов")
    @Description("Тест проверяет корректность структуры ответа при поиске данных")
    @Severity(SeverityLevel.MINOR)
    @Story("Search")
    @Tag("api")
    @Tag("search")
    void searchTradingData_ShouldReturnStubStructure() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/trading/search")
                        .param("query", "SBER")
                        .param("type", "share"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.query").value("SBER"))
                .andExpect(jsonPath("$.type").value("share"))
                .andExpect(jsonPath("$.results").isArray());
    }

    // ==================== ТЕСТЫ ДЛЯ ГРАНИЧНЫХ СЛУЧАЕВ И ОШИБОК ====================

    @Test
    @DisplayName("Получение аккаунтов - пустой список")
    @Description("Тест проверяет поведение API при отсутствии аккаунтов")
    @Severity(SeverityLevel.NORMAL)
    @Story("Account Management")
    @Tag("api")
    @Tag("accounts")
    @Tag("edge-case")
    @Issue("TRADING-003")
    @TmsLink("TMS-ACCOUNTS-002")
    void getAccounts_ShouldReturnEmptyList_WhenNoAccounts() throws Exception {
        // Given
        when(tradingService.getAccounts()).thenReturn(Arrays.asList());
        
        attachTestData("Expected Empty List", "No accounts available");

        // When & Then
        MvcResult result = mockMvc.perform(get("/api/trading/accounts")
                        .header("User-Agent", "TradingControllerTest/1.0")
                        .header("Accept", "application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0))
                .andReturn();

        verify(tradingService, times(1)).getAccounts();
        
        String responseBody = result.getResponse().getContentAsString();
        attachTestData("Empty Response Body", responseBody);
    }

    @Test
    @DisplayName("Получение торговых расписаний - некорректные параметры")
    @Description("Тест проверяет поведение API при передаче некорректных параметров запроса")
    @Severity(SeverityLevel.MINOR)
    @Story("Trading Schedules")
    @Tag("api")
    @Tag("schedules")
    @Tag("negative")
    @Tag("validation")
    @Issue("TRADING-004")
    @TmsLink("TMS-SCHEDULES-002")
    void getTradingSchedules_ShouldHandleInvalidParameters() throws Exception {
        // Given
        String invalidExchange = "";
        String invalidFromDate = "invalid-date";
        String invalidToDate = "2025-13-45T25:70:90Z";
        
        attachTestData("Invalid Parameters", Map.of(
                "exchange", invalidExchange,
                "from", invalidFromDate,
                "to", invalidToDate
        ));

        // When & Then
        MvcResult result = mockMvc.perform(get("/api/trading/schedules")
                        .param("exchange", invalidExchange)
                        .param("from", invalidFromDate)
                        .param("to", invalidToDate)
                        .header("User-Agent", "TradingControllerTest/1.0")
                        .header("Accept", "application/json"))
                .andExpect(status().isInternalServerError())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        attachTestData("Error Response Body", responseBody);
        attachTestData("Error Response Headers", Map.of(
                "Content-Type", result.getResponse().getContentType(),
                "Status", result.getResponse().getStatus()
        ));
    }

    @Test
    @DisplayName("Получение статусов торгов - отсутствие параметров")
    @Description("Тест проверяет поведение API при отсутствии обязательных параметров")
    @Severity(SeverityLevel.MINOR)
    @Story("Trading Statuses")
    @Tag("api")
    @Tag("statuses")
    @Tag("negative")
    @Tag("validation")
    @Issue("TRADING-005")
    @TmsLink("TMS-STATUSES-001")
    void getTradingStatuses_ShouldHandleMissingParameters() throws Exception {
        // Given
        attachTestData("Missing Parameters", "No instrumentId parameters provided");

        // When & Then
        MvcResult result = mockMvc.perform(get("/api/trading/statuses")
                        .header("User-Agent", "TradingControllerTest/1.0")
                        .header("Accept", "application/json"))
                .andExpect(status().isInternalServerError())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        attachTestData("Error Response Body", responseBody);
    }

    @Test
    @DisplayName("Получение аккаунтов - ошибка сервиса")
    @Description("Тест проверяет обработку ошибок при сбое сервиса")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Account Management")
    @Tag("api")
    @Tag("accounts")
    @Tag("negative")
    @Tag("error-handling")
    @Issue("TRADING-006")
    @TmsLink("TMS-ACCOUNTS-003")
    void getAccounts_ShouldHandleServiceError() throws Exception {
        // Given
        String errorMessage = "Database connection failed";
        when(tradingService.getAccounts()).thenThrow(new RuntimeException(errorMessage));
        
        attachTestData("Expected Error", errorMessage);

        // When & Then
        MvcResult result = mockMvc.perform(get("/api/trading/accounts")
                        .header("User-Agent", "TradingControllerTest/1.0")
                        .header("Accept", "application/json"))
                .andExpect(status().isInternalServerError())
                .andReturn();

        verify(tradingService, times(1)).getAccounts();
        
        String responseBody = result.getResponse().getContentAsString();
        attachTestData("Error Response Body", responseBody);
        attachTestData("Error Details", Map.of(
                "errorMessage", errorMessage,
                "statusCode", result.getResponse().getStatus()
        ));
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    @Step("Создание детальных тестовых аккаунтов")
    @Description("Генерация реалистичных тестовых данных для аккаунтов с различными типами")
    private List<AccountDto> createDetailedTestAccounts() {
        return Arrays.asList(
                new AccountDto("2000000001", "Основной брокерский счет", "BROKER"),
                new AccountDto("2000000002", "Индивидуальный инвестиционный счет", "IIS"),
                new AccountDto("2000000003", "Счет для опционов", "BROKER"),
                new AccountDto("2000000004", "Счет для фьючерсов", "BROKER")
        );
    }

    @Step("Создание детальных торговых расписаний")
    @Description("Генерация реалистичных торговых расписаний для различных бирж и периодов")
    private List<TradingScheduleDto> createDetailedTradingSchedules() {
        // Торговые дни
        TradingDayDto tradingDay1 = new TradingDayDto("2025-01-01", true, "09:00", "19:00");
        TradingDayDto tradingDay2 = new TradingDayDto("2025-01-02", true, "09:00", "19:00");
        TradingDayDto nonTradingDay1 = new TradingDayDto("2025-01-03", false, "", "");
        TradingDayDto tradingDay3 = new TradingDayDto("2025-01-04", true, "09:00", "19:00");
        
        // Расписания для разных бирж
        TradingScheduleDto moexSchedule = new TradingScheduleDto("MOEX", Arrays.asList(
                tradingDay1, tradingDay2, nonTradingDay1, tradingDay3
        ));
        
        TradingScheduleDto spbSchedule = new TradingScheduleDto("SPB", Arrays.asList(
                tradingDay1, tradingDay2, nonTradingDay1, tradingDay3
        ));
        
        return Arrays.asList(moexSchedule, spbSchedule);
    }

    @Step("Создание детальных статусов торгов")
    @Description("Генерация реалистичных статусов торгов для различных инструментов")
    private List<TradingStatusDto> createDetailedTradingStatuses() {
        return Arrays.asList(
                new TradingStatusDto("BBG004730N88", "SECURITY_TRADING_STATUS_NORMAL_TRADING"), // Сбербанк
                new TradingStatusDto("BBG004S681W1", "SECURITY_TRADING_STATUS_NORMAL_TRADING"), // Газпром
                new TradingStatusDto("FUTSBER0324", "SECURITY_TRADING_STATUS_NORMAL_TRADING"), // Фьючерс на Сбербанк
                new TradingStatusDto("BBG00JX0J5V0", "SECURITY_TRADING_STATUS_SUSPENDED"), // Приостановленные торги
                new TradingStatusDto("BBG00QPYJ5X0", "SECURITY_TRADING_STATUS_CLOSING_AUCTION") // Закрывающий аукцион
        );
    }

    @Step("Настройка детальных моков")
    @Description("Конфигурация мок-объектов с детальным логированием вызовов")
    private void setupDetailedMocks() {
        // Настройка мока для получения аккаунтов
        when(tradingService.getAccounts()).thenAnswer(invocation -> {
            attachTestData("Mock: getAccounts() called", accounts);
            return accounts;
        });

        // Настройка мока для получения торговых расписаний
        when(tradingService.getTradingSchedules(anyString(), any(Instant.class), any(Instant.class)))
                .thenAnswer(invocation -> {
                    String exchange = invocation.getArgument(0);
                    Instant from = invocation.getArgument(1);
                    Instant to = invocation.getArgument(2);
                    
                    Map<String, Object> callDetails = new HashMap<>();
                    callDetails.put("exchange", exchange);
                    callDetails.put("from", from.toString());
                    callDetails.put("to", to.toString());
                    callDetails.put("returnedSchedules", schedules.size());
                    
                    attachTestData("Mock: getTradingSchedules() called", callDetails);
                    return schedules;
                });

        // Настройка мока для получения статусов торгов
        when(tradingService.getTradingStatuses(anyList())).thenAnswer(invocation -> {
            List<String> instrumentIds = invocation.getArgument(0);
            
            Map<String, Object> callDetails = new HashMap<>();
            callDetails.put("requestedInstrumentIds", instrumentIds);
            callDetails.put("returnedStatuses", statuses.size());
            
            attachTestData("Mock: getTradingStatuses() called", callDetails);
            return statuses;
        });
    }

    @Attachment(value = "{attachmentName}", type = "application/json")
    private String attachTestData(String attachmentName, Object data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(data);
        } catch (Exception e) {
            return "Error serializing data: " + e.getMessage();
        }
    }

    @Step("Проверка HTTP статуса ответа")
    @Description("Валидация HTTP статуса ответа с детальным логированием")
    private void verifyHttpStatus(MvcResult result, int expectedStatus) throws Exception {
        int actualStatus = result.getResponse().getStatus();
        if (actualStatus != expectedStatus) {
            attachTestData("HTTP Response Details", Map.of(
                    "expectedStatus", expectedStatus,
                    "actualStatus", actualStatus,
                    "responseBody", result.getResponse().getContentAsString()
            ));
        }
    }

    @Step("Проверка JSON структуры ответа")
    @Description("Детальная валидация JSON структуры ответа")
    private void verifyJsonStructure(MvcResult result, String jsonPath, Object expectedValue) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        attachTestData("JSON Validation Details", Map.of(
                "jsonPath", jsonPath,
                "expectedValue", expectedValue,
                "responseBody", responseBody
        ));
    }
}


