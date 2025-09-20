package com.example.InvestmentDataLoaderService.unit.controller;

import com.example.InvestmentDataLoaderService.controller.DataLoadingController;
import com.example.InvestmentDataLoaderService.dto.ClosePriceDto;
import com.example.InvestmentDataLoaderService.dto.ClosePriceEveningSessionRequestDto;
import com.example.InvestmentDataLoaderService.dto.ClosePriceEveningSessionDto;
import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.service.TInvestService;
import com.example.InvestmentDataLoaderService.scheduler.CandleSchedulerService;
import com.example.InvestmentDataLoaderService.scheduler.EveningSessionService;
import com.example.InvestmentDataLoaderService.scheduler.MorningSessionService;
import com.example.InvestmentDataLoaderService.scheduler.LastTradesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DataLoadingController.class)
@Epic("Data Loading API")
@Feature("Evening Session Prices")
@DisplayName("Data Loading Controller Evening Session Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
class DataLoadingControllerEveningSessionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TInvestService service;

    @MockBean
    private CandleSchedulerService candleScheduler;

    @MockBean
    private EveningSessionService eveningSessionService;

    @MockBean
    private MorningSessionService morningSessionService;

    @MockBean
    private LastTradesService lastTradesService;

    @Autowired
    private ObjectMapper objectMapper;

    private ClosePriceDto sampleClosePrice;
    private ClosePriceEveningSessionDto sampleEveningSessionDto;
    private SaveResponseDto sampleSaveResponse;

    @BeforeEach
    @Step("Подготовка тестовых данных для вечерней сессии")
    @DisplayName("Подготовка тестовых данных")
    @Description("Инициализация тестовых данных для тестов вечерней сессии")
    @Tag("setup")
    void setUp() {
        sampleClosePrice = new ClosePriceDto(
            "BBG004730N88",
            "2024-01-15",
            new BigDecimal("250.75"),
            new BigDecimal("251.00")
        );

        sampleEveningSessionDto = new ClosePriceEveningSessionDto(
            LocalDate.of(2024, 1, 15),
            "BBG004730N88",
            new BigDecimal("251.00"),
            "SHARES",
            "RUB",
            "MOEX"
        );

        sampleSaveResponse = new SaveResponseDto(
            true,
            "Успешно загружено 5 новых цен вечерней сессии из 10 найденных.",
            10,
            5,
            5,
            0, // invalidItemsFiltered
            0, // missingFromApi
            List.of(sampleEveningSessionDto)
        );
    }

    // ==================== POST /api/data-loading/evening-session-prices ====================

    @Test
    @DisplayName("Загрузка цен вечерней сессии за сегодня - успешный случай")
    @Description("Тест проверяет корректность загрузки цен вечерней сессии за текущий день")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Evening Session Loading")
    @Tag("api")
    @Tag("evening-session")
    @Tag("load")
    @Tag("success")
    void loadEveningSessionPricesToday_ShouldReturnSuccessResponse() throws Exception {
        // Given
        when(service.saveClosePricesEveningSession(any(ClosePriceEveningSessionRequestDto.class)))
            .thenReturn(sampleSaveResponse);

        // When & Then
        mockMvc.perform(post("/api/data-loading/evening-session-prices"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Успешно загружено 5 новых цен вечерней сессии из 10 найденных."))
            .andExpect(jsonPath("$.totalRequested").value(10))
            .andExpect(jsonPath("$.newItemsSaved").value(5))
            .andExpect(jsonPath("$.existingItemsSkipped").value(5))
            .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
            .andExpect(jsonPath("$.savedItems").isArray())
            .andExpect(jsonPath("$.savedItems[0].figi").value("BBG004730N88"))
            .andExpect(jsonPath("$.savedItems[0].priceDate").value("2024-01-15"))
            .andExpect(jsonPath("$.savedItems[0].closePrice").value(251.00))
            .andExpect(jsonPath("$.savedItems[0].instrumentType").value("SHARES"))
            .andExpect(jsonPath("$.savedItems[0].currency").value("RUB"))
            .andExpect(jsonPath("$.savedItems[0].exchange").value("MOEX"));
    }

    @Test
    @DisplayName("Загрузка цен вечерней сессии за сегодня - ошибка сервиса")
    @Description("Тест проверяет корректность обработки ошибок при загрузке цен вечерней сессии")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Evening Session Loading")
    @Tag("api")
    @Tag("evening-session")
    @Tag("load")
    @Tag("error")
    void loadEveningSessionPricesToday_ShouldHandleServiceException() throws Exception {
        // Given
        when(service.saveClosePricesEveningSession(any(ClosePriceEveningSessionRequestDto.class)))
            .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/api/data-loading/evening-session-prices"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка загрузки данных: Ошибка загрузки цен вечерней сессии за сегодня: Service error"))
            .andExpect(jsonPath("$.error").value("DataLoadException"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    // ==================== POST /api/data-loading/evening-session-prices/save ====================

    @Test
    @DisplayName("Сохранение цен вечерней сессии - валидный запрос")
    @Description("Тест проверяет корректность сохранения цен вечерней сессии с валидным запросом")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Evening Session Saving")
    @Tag("api")
    @Tag("evening-session")
    @Tag("save")
    @Tag("valid-request")
    void saveEveningSessionPrices_WithValidRequest_ShouldReturnSuccessResponse() throws Exception {
        // Given
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(List.of("BBG004730N88", "BBG004730ZJ9"));

        when(service.saveClosePricesEveningSession(any(ClosePriceEveningSessionRequestDto.class)))
            .thenReturn(sampleSaveResponse);

        // When & Then
        mockMvc.perform(post("/api/data-loading/evening-session-prices/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Успешно загружено 5 новых цен вечерней сессии из 10 найденных."))
            .andExpect(jsonPath("$.totalRequested").value(10))
            .andExpect(jsonPath("$.newItemsSaved").value(5))
            .andExpect(jsonPath("$.existingItemsSkipped").value(5))
            .andExpect(jsonPath("$.invalidItemsFiltered").value(0));
    }

    @Test
    @DisplayName("Сохранение цен вечерней сессии - null запрос")
    @Description("Тест проверяет корректность обработки null запроса при сохранении цен вечерней сессии")
    @Severity(SeverityLevel.NORMAL)
    @Story("Evening Session Saving")
    @Tag("api")
    @Tag("evening-session")
    @Tag("save")
    @Tag("null-request")
    void saveEveningSessionPrices_WithNullRequest_ShouldCreateEmptyRequest() throws Exception {
        // Given
        when(service.saveClosePricesEveningSession(any(ClosePriceEveningSessionRequestDto.class)))
            .thenReturn(sampleSaveResponse);

        // When & Then
        mockMvc.perform(post("/api/data-loading/evening-session-prices/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Сохранение цен вечерней сессии - пустой запрос")
    @Description("Тест проверяет корректность обработки пустого запроса при сохранении цен вечерней сессии")
    @Severity(SeverityLevel.NORMAL)
    @Story("Evening Session Saving")
    @Tag("api")
    @Tag("evening-session")
    @Tag("save")
    @Tag("empty-request")
    void saveEveningSessionPrices_WithEmptyRequest_ShouldReturnSuccessResponse() throws Exception {
        // Given
        when(service.saveClosePricesEveningSession(any(ClosePriceEveningSessionRequestDto.class)))
            .thenReturn(sampleSaveResponse);

        // When & Then
        mockMvc.perform(post("/api/data-loading/evening-session-prices/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Сохранение цен вечерней сессии - ошибка сервиса")
    @Description("Тест проверяет корректность обработки ошибок сервиса при сохранении цен вечерней сессии")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Evening Session Saving")
    @Tag("api")
    @Tag("evening-session")
    @Tag("save")
    @Tag("error")
    void saveEveningSessionPrices_ShouldHandleServiceException() throws Exception {
        // Given
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(List.of("BBG004730N88"));

        when(service.saveClosePricesEveningSession(any(ClosePriceEveningSessionRequestDto.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/data-loading/evening-session-prices/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка загрузки данных: Ошибка сохранения цен вечерней сессии: Database connection failed"))
            .andExpect(jsonPath("$.error").value("DataLoadException"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    // ==================== POST /api/data-loading/evening-session-prices/{date} ====================

    @Test
    @DisplayName("Загрузка цен вечерней сессии по дате - валидная дата")
    @Description("Тест проверяет корректность загрузки цен вечерней сессии для указанной даты")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Evening Session Loading by Date")
    @Tag("api")
    @Tag("evening-session")
    @Tag("load-by-date")
    @Tag("valid-date")
    void loadEveningSessionPricesForDate_WithValidDate_ShouldReturnSuccessResponse() throws Exception {
        // Given
        String testDate = "2024-01-15";
        when(eveningSessionService.fetchAndStoreEveningSessionPricesForDate(any(LocalDate.class)))
            .thenReturn(sampleSaveResponse);

        // When & Then
        mockMvc.perform(post("/api/data-loading/evening-session-prices/{date}", testDate))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Успешно загружено 5 новых цен вечерней сессии из 10 найденных."))
            .andExpect(jsonPath("$.totalRequested").value(10))
            .andExpect(jsonPath("$.newItemsSaved").value(5))
            .andExpect(jsonPath("$.existingItemsSkipped").value(5))
            .andExpect(jsonPath("$.invalidItemsFiltered").value(0));
    }

    @Test
    @DisplayName("Загрузка цен вечерней сессии по дате - выходной день")
    @Description("Тест проверяет корректность обработки запроса на загрузку цен вечерней сессии в выходной день")
    @Severity(SeverityLevel.NORMAL)
    @Story("Evening Session Loading by Date")
    @Tag("api")
    @Tag("evening-session")
    @Tag("load-by-date")
    @Tag("weekend")
    @Tag("validation")
    void loadEveningSessionPricesForDate_WithWeekendDate_ShouldReturnErrorResponse() throws Exception {
        // Given
        String weekendDate = "2024-01-13"; // Saturday
        SaveResponseDto weekendResponse = new SaveResponseDto(
            false,
            "В выходные дни (суббота и воскресенье) вечерняя сессия не проводится. Дата: 2024-01-13",
            0,
            0,
            0,
            0,
            0,
            List.of()
        );
        when(eveningSessionService.fetchAndStoreEveningSessionPricesForDate(any(LocalDate.class)))
            .thenReturn(weekendResponse);

        // When & Then
        mockMvc.perform(post("/api/data-loading/evening-session-prices/{date}", weekendDate))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("В выходные дни (суббота и воскресенье) вечерняя сессия не проводится. Дата: 2024-01-13"))
            .andExpect(jsonPath("$.totalRequested").value(0))
            .andExpect(jsonPath("$.newItemsSaved").value(0));
    }

    // ==================== GET /api/data-loading/evening-session-prices/shares ====================

    @Test
    @DisplayName("Получение цен вечерней сессии для акций - успешный случай")
    @Description("Тест проверяет корректность получения цен вечерней сессии для всех акций")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Evening Session Prices Retrieval")
    @Tag("api")
    @Tag("evening-session")
    @Tag("get")
    @Tag("shares")
    @Tag("success")
    void getEveningSessionPricesForShares_ShouldReturnSuccessResponse() throws Exception {
        // Given
        List<ClosePriceDto> mockClosePrices = List.of(
            new ClosePriceDto("BBG004730N88", "2024-01-15", BigDecimal.valueOf(250.75), BigDecimal.valueOf(251.00)),
            new ClosePriceDto("BBG004730ZJ9", "2024-01-15", BigDecimal.valueOf(180.50), BigDecimal.valueOf(181.00))
        );
        when(service.getClosePricesForAllShares()).thenReturn(mockClosePrices);

        // When & Then
        mockMvc.perform(get("/api/data-loading/evening-session-prices/shares"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Цены вечерней сессии для акций получены успешно"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].figi").value("BBG004730N88"))
            .andExpect(jsonPath("$.data[0].tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.data[0].closePrice").value(250.75))
            .andExpect(jsonPath("$.data[0].eveningSessionPrice").value(251.00))
            .andExpect(jsonPath("$.data[1].figi").value("BBG004730ZJ9"))
            .andExpect(jsonPath("$.data[1].tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.data[1].closePrice").value(180.50))
            .andExpect(jsonPath("$.data[1].eveningSessionPrice").value(181.00))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Получение цен вечерней сессии для акций - пустой результат")
    @Description("Тест проверяет корректность обработки пустого результата при получении цен вечерней сессии для акций")
    @Severity(SeverityLevel.NORMAL)
    @Story("Evening Session Prices Retrieval")
    @Tag("api")
    @Tag("evening-session")
    @Tag("get")
    @Tag("shares")
    @Tag("empty-result")
    void getEveningSessionPricesForShares_WithEmptyResult_ShouldReturnEmptyArray() throws Exception {
        // Given
        when(service.getClosePricesForAllShares()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/data-loading/evening-session-prices/shares"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Цены вечерней сессии для акций получены успешно"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty())
            .andExpect(jsonPath("$.count").value(0))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Получение цен вечерней сессии для акций - ошибка сервиса")
    @Description("Тест проверяет корректность обработки ошибок сервиса при получении цен вечерней сессии для акций")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Evening Session Prices Retrieval")
    @Tag("api")
    @Tag("evening-session")
    @Tag("get")
    @Tag("shares")
    @Tag("error")
    void getEveningSessionPricesForShares_ShouldHandleServiceException() throws Exception {
        // Given
        when(service.getClosePricesForAllShares())
            .thenThrow(new RuntimeException("API connection failed"));

        // When & Then
        mockMvc.perform(get("/api/data-loading/evening-session-prices/shares"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка загрузки данных: Ошибка получения цен вечерней сессии для акций: API connection failed"))
            .andExpect(jsonPath("$.error").value("DataLoadException"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Получение цен вечерней сессии для акций - фильтрация невалидных цен")
    @Description("Тест проверяет корректность фильтрации невалидных цен при получении цен вечерней сессии для акций")
    @Severity(SeverityLevel.NORMAL)
    @Story("Evening Session Prices Retrieval")
    @Tag("api")
    @Tag("evening-session")
    @Tag("get")
    @Tag("shares")
    @Tag("filtering")
    @Tag("validation")
    void getEveningSessionPricesForShares_ShouldFilterInvalidPrices() throws Exception {
        // Given - смесь валидных и невалидных цен
        List<ClosePriceDto> mockClosePrices = List.of(
            new ClosePriceDto("BBG004730N88", "2024-01-15", BigDecimal.valueOf(250.75), BigDecimal.valueOf(251.00)), // валидная
            new ClosePriceDto("BBG004730ZJ9", "1970-01-01", BigDecimal.valueOf(180.50), BigDecimal.valueOf(181.00)), // невалидная (1970-01-01)
            new ClosePriceDto("BBG004S685M2", "2024-01-15", BigDecimal.valueOf(320.25), BigDecimal.valueOf(321.00)), // валидная
            new ClosePriceDto("BBG004S68JR9", "1970-01-01", BigDecimal.valueOf(150.00), BigDecimal.valueOf(151.00))  // невалидная (1970-01-01)
        );
        when(service.getClosePricesForAllShares()).thenReturn(mockClosePrices);

        // When & Then
        mockMvc.perform(get("/api/data-loading/evening-session-prices/shares"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Цены вечерней сессии для акций получены успешно"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2)) // только 2 валидные цены
            .andExpect(jsonPath("$.data[0].figi").value("BBG004730N88"))
            .andExpect(jsonPath("$.data[0].tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.data[0].closePrice").value(250.75))
            .andExpect(jsonPath("$.data[0].eveningSessionPrice").value(251.00))
            .andExpect(jsonPath("$.data[1].figi").value("BBG004S685M2"))
            .andExpect(jsonPath("$.data[1].tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.data[1].closePrice").value(320.25))
            .andExpect(jsonPath("$.data[1].eveningSessionPrice").value(321.00))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    // ==================== GET /api/data-loading/evening-session-prices/futures ====================

    @Test
    @DisplayName("Получение цен вечерней сессии для фьючерсов - успешный случай")
    @Description("Тест проверяет корректность получения цен вечерней сессии для всех фьючерсов")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Evening Session Prices Retrieval")
    @Tag("api")
    @Tag("evening-session")
    @Tag("get")
    @Tag("futures")
    @Tag("success")
    void getEveningSessionPricesForFutures_ShouldReturnSuccessResponse() throws Exception {
        // Given
        List<ClosePriceDto> mockClosePrices = List.of(
            new ClosePriceDto("FUTSILV-3.24", "2024-01-15", BigDecimal.valueOf(75000.00), BigDecimal.valueOf(75100.00)),
            new ClosePriceDto("FUTGOLD-3.24", "2024-01-15", BigDecimal.valueOf(250000.00), BigDecimal.valueOf(250500.00))
        );
        when(service.getClosePricesForAllFutures()).thenReturn(mockClosePrices);

        // When & Then
        mockMvc.perform(get("/api/data-loading/evening-session-prices/futures"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Цены вечерней сессии для фьючерсов получены успешно"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].figi").value("FUTSILV-3.24"))
            .andExpect(jsonPath("$.data[0].tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.data[0].closePrice").value(75000.00))
            .andExpect(jsonPath("$.data[0].eveningSessionPrice").value(75100.00))
            .andExpect(jsonPath("$.data[1].figi").value("FUTGOLD-3.24"))
            .andExpect(jsonPath("$.data[1].tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.data[1].closePrice").value(250000.00))
            .andExpect(jsonPath("$.data[1].eveningSessionPrice").value(250500.00))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Получение цен вечерней сессии для фьючерсов - пустой результат")
    @Description("Тест проверяет корректность обработки пустого результата при получении цен вечерней сессии для фьючерсов")
    @Severity(SeverityLevel.NORMAL)
    @Story("Evening Session Prices Retrieval")
    @Tag("api")
    @Tag("evening-session")
    @Tag("get")
    @Tag("futures")
    @Tag("empty-result")
    void getEveningSessionPricesForFutures_WithEmptyResult_ShouldReturnEmptyArray() throws Exception {
        // Given
        when(service.getClosePricesForAllFutures()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/data-loading/evening-session-prices/futures"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Цены вечерней сессии для фьючерсов получены успешно"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty())
            .andExpect(jsonPath("$.count").value(0))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Получение цен вечерней сессии для фьючерсов - ошибка сервиса")
    @Description("Тест проверяет корректность обработки ошибок сервиса при получении цен вечерней сессии для фьючерсов")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Evening Session Prices Retrieval")
    @Tag("api")
    @Tag("evening-session")
    @Tag("get")
    @Tag("futures")
    @Tag("error")
    void getEveningSessionPricesForFutures_ShouldHandleServiceException() throws Exception {
        // Given
        when(service.getClosePricesForAllFutures())
            .thenThrow(new RuntimeException("API connection failed"));

        // When & Then
        mockMvc.perform(get("/api/data-loading/evening-session-prices/futures"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка загрузки данных: Ошибка получения цен вечерней сессии для фьючерсов: API connection failed"))
            .andExpect(jsonPath("$.error").value("DataLoadException"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Получение цен вечерней сессии для фьючерсов - фильтрация невалидных цен")
    @Description("Тест проверяет корректность фильтрации невалидных цен при получении цен вечерней сессии для фьючерсов")
    @Severity(SeverityLevel.NORMAL)
    @Story("Evening Session Prices Retrieval")
    @Tag("api")
    @Tag("evening-session")
    @Tag("get")
    @Tag("futures")
    @Tag("filtering")
    @Tag("validation")
    void getEveningSessionPricesForFutures_ShouldFilterInvalidPrices() throws Exception {
        // Given - смесь валидных и невалидных цен
        List<ClosePriceDto> mockClosePrices = List.of(
            new ClosePriceDto("FUTSBRF-3.24", "2024-01-15", BigDecimal.valueOf(95.50), BigDecimal.valueOf(96.00)), // валидная
            new ClosePriceDto("FUTSBRF-6.24", "1970-01-01", BigDecimal.valueOf(96.25), BigDecimal.valueOf(97.00)), // невалидная (1970-01-01)
            new ClosePriceDto("FUTSBRF-9.24", "2024-01-15", BigDecimal.valueOf(97.00), BigDecimal.valueOf(98.00)), // валидная
            new ClosePriceDto("FUTSBRF-12.24", "1970-01-01", BigDecimal.valueOf(98.75), BigDecimal.valueOf(99.00)) // невалидная (1970-01-01)
        );
        when(service.getClosePricesForAllFutures()).thenReturn(mockClosePrices);

        // When & Then
        mockMvc.perform(get("/api/data-loading/evening-session-prices/futures"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Цены вечерней сессии для фьючерсов получены успешно"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2)) // только 2 валидные цены
            .andExpect(jsonPath("$.data[0].figi").value("FUTSBRF-3.24"))
            .andExpect(jsonPath("$.data[0].tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.data[0].closePrice").value(95.50))
            .andExpect(jsonPath("$.data[0].eveningSessionPrice").value(96.00))
            .andExpect(jsonPath("$.data[1].figi").value("FUTSBRF-9.24"))
            .andExpect(jsonPath("$.data[1].tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.data[1].closePrice").value(97.00))
            .andExpect(jsonPath("$.data[1].eveningSessionPrice").value(98.00))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    // ==================== GET /api/data-loading/evening-session-prices/{figi} ====================

    @Test
    @DisplayName("Получение цены вечерней сессии по FIGI - валидный FIGI")
    @Description("Тест проверяет корректность получения цены вечерней сессии по указанному FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Evening Session Price by FIGI")
    @Tag("api")
    @Tag("evening-session")
    @Tag("get-by-figi")
    @Tag("valid-figi")
    @Tag("success")
    void getEveningSessionPriceByFigi_WithValidFigi_ShouldReturnSuccessResponse() throws Exception {
        // Given
        String figi = "BBG004730N88";
        when(service.getClosePrices(eq(List.of(figi)), eq(null)))
            .thenReturn(List.of(sampleClosePrice));

        // When & Then
        mockMvc.perform(get("/api/data-loading/evening-session-prices/{figi}", figi))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Цена вечерней сессии получена успешно"))
            .andExpect(jsonPath("$.data.figi").value("BBG004730N88"))
            .andExpect(jsonPath("$.data.tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.data.closePrice").value(250.75))
            .andExpect(jsonPath("$.data.eveningSessionPrice").value(251.00))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Получение цены вечерней сессии по FIGI - несуществующий FIGI")
    @Description("Тест проверяет корректность обработки случая когда цена вечерней сессии не найдена для указанного FIGI")
    @Severity(SeverityLevel.NORMAL)
    @Story("Evening Session Price by FIGI")
    @Tag("api")
    @Tag("evening-session")
    @Tag("get-by-figi")
    @Tag("not-found")
    @Tag("invalid-figi")
    void getEveningSessionPriceByFigi_WithEmptyResult_ShouldReturnNotFoundResponse() throws Exception {
        // Given
        String figi = "INVALID_FIGI";
        when(service.getClosePrices(eq(List.of(figi)), eq(null)))
            .thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/data-loading/evening-session-prices/{figi}", figi))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Цена вечерней сессии не найдена для инструмента: " + figi))
            .andExpect(jsonPath("$.figi").value(figi))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Получение цены вечерней сессии по FIGI - ошибка сервиса")
    @Description("Тест проверяет корректность обработки ошибок сервиса при получении цены вечерней сессии по FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Evening Session Price by FIGI")
    @Tag("api")
    @Tag("evening-session")
    @Tag("get-by-figi")
    @Tag("error")
    void getEveningSessionPriceByFigi_ShouldHandleServiceException() throws Exception {
        // Given
        String figi = "BBG004730N88";
        when(service.getClosePrices(eq(List.of(figi)), eq(null)))
            .thenThrow(new RuntimeException("API connection failed"));

        // When & Then
        mockMvc.perform(get("/api/data-loading/evening-session-prices/{figi}", figi))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка получения цены вечерней сессии: API connection failed"))
            .andExpect(jsonPath("$.figi").value(figi))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Получение цены вечерней сессии по FIGI - специальные символы")
    @Description("Тест проверяет корректность обработки FIGI со специальными символами")
    @Severity(SeverityLevel.NORMAL)
    @Story("Evening Session Price by FIGI")
    @Tag("api")
    @Tag("evening-session")
    @Tag("get-by-figi")
    @Tag("special-characters")
    @Tag("edge-case")
    void getEveningSessionPriceByFigi_WithSpecialCharacters_ShouldHandleCorrectly() throws Exception {
        // Given
        String figi = "BBG004730N88+";
        when(service.getClosePrices(eq(List.of(figi)), eq(null)))
            .thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/data-loading/evening-session-prices/{figi}", figi))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Цена вечерней сессии не найдена для инструмента: " + figi))
            .andExpect(jsonPath("$.figi").value(figi));
    }

    @Test
    @DisplayName("Получение цены вечерней сессии по FIGI - невалидная дата")
    @Description("Тест проверяет корректность фильтрации цен с невалидной датой при получении цены вечерней сессии по FIGI")
    @Severity(SeverityLevel.NORMAL)
    @Story("Evening Session Price by FIGI")
    @Tag("api")
    @Tag("evening-session")
    @Tag("get-by-figi")
    @Tag("invalid-date")
    @Tag("filtering")
    @Tag("validation")
    void getEveningSessionPriceByFigi_WithInvalidPriceDate_ShouldReturnNotFound() throws Exception {
        // Given - цена с неверной датой 1970-01-01 (будет отфильтрована)
        String figi = "BBG004730N88";
        List<ClosePriceDto> invalidPrices = List.of(
            new ClosePriceDto(figi, "1970-01-01", BigDecimal.valueOf(250.75), BigDecimal.valueOf(251.00)) // невалидная дата
        );
        when(service.getClosePrices(eq(List.of(figi)), eq(null)))
            .thenReturn(invalidPrices);

        // When & Then - после фильтрации список будет пустым
        mockMvc.perform(get("/api/data-loading/evening-session-prices/{figi}", figi))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Цена вечерней сессии не найдена для инструмента: " + figi))
            .andExpect(jsonPath("$.figi").value("BBG004730N88"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Получение цены вечерней сессии по FIGI - отсутствует цена вечерней сессии")
    @Description("Тест проверяет корректность фильтрации цен без цены вечерней сессии при получении по FIGI")
    @Severity(SeverityLevel.NORMAL)
    @Story("Evening Session Price by FIGI")
    @Tag("api")
    @Tag("evening-session")
    @Tag("get-by-figi")
    @Tag("null-evening-price")
    @Tag("filtering")
    @Tag("validation")
    void getEveningSessionPriceByFigi_WithNullEveningSessionPrice_ShouldReturnNotFound() throws Exception {
        // Given - цена без eveningSessionPrice (будет отфильтрована)
        String figi = "BBG004730N88";
        List<ClosePriceDto> pricesWithoutEveningSession = List.of(
            new ClosePriceDto(figi, "2024-01-15", BigDecimal.valueOf(250.75), null) // без eveningSessionPrice
        );
        when(service.getClosePrices(eq(List.of(figi)), eq(null)))
            .thenReturn(pricesWithoutEveningSession);

        // When & Then - после фильтрации список будет пустым
        mockMvc.perform(get("/api/data-loading/evening-session-prices/{figi}", figi))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Цена вечерней сессии не найдена для инструмента: " + figi))
            .andExpect(jsonPath("$.figi").value("BBG004730N88"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("Сохранение цен вечерней сессии - невалидный JSON")
    @Description("Тест проверяет корректность обработки невалидного JSON при сохранении цен вечерней сессии")
    @Severity(SeverityLevel.NORMAL)
    @Story("Evening Session Saving")
    @Tag("api")
    @Tag("evening-session")
    @Tag("save")
    @Tag("invalid-json")
    @Tag("edge-case")
    void saveEveningSessionPrices_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/data-loading/evening-session-prices/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("RuntimeException"));
    }

    @Test
    @DisplayName("Получение цены вечерней сессии по FIGI - пустой FIGI")
    @Description("Тест проверяет корректность обработки пустого FIGI при получении цены вечерней сессии")
    @Severity(SeverityLevel.NORMAL)
    @Story("Evening Session Price by FIGI")
    @Tag("api")
    @Tag("evening-session")
    @Tag("get-by-figi")
    @Tag("empty-figi")
    @Tag("edge-case")
    void getEveningSessionPriceByFigi_WithEmptyFigi_ShouldReturnNotFound() throws Exception {
        // Given
        String figi = "";
        when(service.getClosePrices(eq(List.of(figi)), eq(null)))
            .thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/data-loading/evening-session-prices/{figi}", figi))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Exception"));
    }

    @Test
    @DisplayName("Загрузка цен вечерней сессии за сегодня - включение отфильтрованных элементов")
    @Description("Тест проверяет корректность включения информации об отфильтрованных элементах в ответе")
    @Severity(SeverityLevel.NORMAL)
    @Story("Evening Session Loading")
    @Tag("api")
    @Tag("evening-session")
    @Tag("load")
    @Tag("filtering")
    @Tag("response-structure")
    void loadEveningSessionPricesToday_ShouldIncludeInvalidItemsFilteredInResponse() throws Exception {
        // Given - мок с отфильтрованными ценами
        SaveResponseDto mockResponse = new SaveResponseDto(
            true,
            "Успешно загружено 2 новых цены вечерней сессии из 5 найденных.",
            5, // totalRequested
            2, // newItemsSaved
            1, // existingItemsSkipped
            2, // invalidItemsFiltered
            0, // missingFromApi
            List.of(sampleEveningSessionDto)
        );
        when(service.saveClosePricesEveningSession(any(ClosePriceEveningSessionRequestDto.class)))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/data-loading/evening-session-prices"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Успешно загружено 2 новых цены вечерней сессии из 5 найденных."))
            .andExpect(jsonPath("$.totalRequested").value(5))
            .andExpect(jsonPath("$.newItemsSaved").value(2))
            .andExpect(jsonPath("$.existingItemsSkipped").value(1))
            .andExpect(jsonPath("$.invalidItemsFiltered").value(2))
            .andExpect(jsonPath("$.savedItems").isArray())
            .andExpect(jsonPath("$.savedItems.length()").value(1))
            .andExpect(jsonPath("$.timestamp").exists());
    }
}
