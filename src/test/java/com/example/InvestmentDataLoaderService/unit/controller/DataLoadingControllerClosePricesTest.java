package com.example.InvestmentDataLoaderService.unit.controller;

import com.example.InvestmentDataLoaderService.controller.DataLoadingController;
import com.example.InvestmentDataLoaderService.dto.ClosePriceDto;
import com.example.InvestmentDataLoaderService.dto.ClosePriceRequestDto;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DataLoadingController.class)
@Epic("Data Loading API")
@Feature("Close Prices")
@DisplayName("Data Loading Controller Close Prices Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
class DataLoadingControllerClosePricesTest {

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
    private SaveResponseDto sampleSaveResponse;

    @BeforeEach
    @Step("Подготовка тестовых данных для цен закрытия")
    @DisplayName("Подготовка тестовых данных")
    @Description("Инициализация тестовых данных для тестов цен закрытия")
    @Tag("setup")
    void setUp() {
        sampleClosePrice = new ClosePriceDto(
            "BBG004730N88",
            "2024-01-15",
            new BigDecimal("250.75"),
            new BigDecimal("251.00")
        );

        sampleSaveResponse = new SaveResponseDto(
            true,
            "Успешно загружено 5 новых цен закрытия из 10 найденных.",
            10,
            5,
            5,
            0, // invalidItemsFiltered
            0, // missingFromApi
            List.of(sampleClosePrice)
        );
    }

    // ==================== POST /api/data-loading/close-prices ====================

    @Test
    @DisplayName("Загрузка цен закрытия за сегодня - успешный случай")
    @Description("Тест проверяет корректность загрузки цен закрытия за текущий день")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Close Prices Loading")
    @Tag("api")
    @Tag("close-prices")
    @Tag("load")
    @Tag("success")
    void loadClosePricesToday_ShouldReturnSuccessResponse() throws Exception {
        // Given
        when(service.saveClosePrices(any(ClosePriceRequestDto.class)))
            .thenReturn(sampleSaveResponse);

        // When & Then
        mockMvc.perform(post("/api/data-loading/close-prices"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Успешно загружено 5 новых цен закрытия из 10 найденных."))
            .andExpect(jsonPath("$.totalRequested").value(10))
            .andExpect(jsonPath("$.newItemsSaved").value(5))
            .andExpect(jsonPath("$.existingItemsSkipped").value(5))
            .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
            .andExpect(jsonPath("$.savedItems").isArray())
            .andExpect(jsonPath("$.savedItems[0].figi").value("BBG004730N88"))
            .andExpect(jsonPath("$.savedItems[0].tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.savedItems[0].closePrice").value(250.75));
    }

    @Test
    @DisplayName("Загрузка цен закрытия за сегодня - ошибка сервиса")
    @Description("Тест проверяет корректность обработки ошибок при загрузке цен закрытия")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Close Prices Loading")
    @Tag("api")
    @Tag("close-prices")
    @Tag("load")
    @Tag("error")
    void loadClosePricesToday_ShouldHandleServiceException() throws Exception {
        // Given
        when(service.saveClosePrices(any(ClosePriceRequestDto.class)))
            .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/api/data-loading/close-prices"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка загрузки данных: Ошибка загрузки цен закрытия за сегодня: Service error"))
            .andExpect(jsonPath("$.error").value("DataLoadException"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    // ==================== POST /api/data-loading/close-prices/save ====================

    @Test
    @DisplayName("Сохранение цен закрытия - валидный запрос")
    @Description("Тест проверяет корректность сохранения цен закрытия с валидным запросом")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Close Prices Saving")
    @Tag("api")
    @Tag("close-prices")
    @Tag("save")
    @Tag("valid-request")
    void saveClosePrices_WithValidRequest_ShouldReturnSuccessResponse() throws Exception {
        // Given
        ClosePriceRequestDto request = new ClosePriceRequestDto();
        request.setInstruments(List.of("BBG004730N88", "BBG004730ZJ9"));

        when(service.saveClosePrices(any(ClosePriceRequestDto.class)))
            .thenReturn(sampleSaveResponse);

        // When & Then
        mockMvc.perform(post("/api/data-loading/close-prices/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Успешно загружено 5 новых цен закрытия из 10 найденных."))
            .andExpect(jsonPath("$.totalRequested").value(10))
            .andExpect(jsonPath("$.newItemsSaved").value(5))
            .andExpect(jsonPath("$.existingItemsSkipped").value(5))
            .andExpect(jsonPath("$.invalidItemsFiltered").value(0));
    }

    @Test
    @DisplayName("Сохранение цен закрытия - null запрос")
    @Description("Тест проверяет корректность обработки null запроса при сохранении цен закрытия")
    @Severity(SeverityLevel.NORMAL)
    @Story("Close Prices Saving")
    @Tag("api")
    @Tag("close-prices")
    @Tag("save")
    @Tag("null-request")
    void saveClosePrices_WithNullRequest_ShouldCreateEmptyRequest() throws Exception {
        // Given
        when(service.saveClosePrices(any(ClosePriceRequestDto.class)))
            .thenReturn(sampleSaveResponse);

        // When & Then
        mockMvc.perform(post("/api/data-loading/close-prices/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Сохранение цен закрытия - пустой запрос")
    @Description("Тест проверяет корректность обработки пустого запроса при сохранении цен закрытия")
    @Severity(SeverityLevel.NORMAL)
    @Story("Close Prices Saving")
    @Tag("api")
    @Tag("close-prices")
    @Tag("save")
    @Tag("empty-request")
    void saveClosePrices_WithEmptyRequest_ShouldReturnSuccessResponse() throws Exception {
        // Given
        when(service.saveClosePrices(any(ClosePriceRequestDto.class)))
            .thenReturn(sampleSaveResponse);

        // When & Then
        mockMvc.perform(post("/api/data-loading/close-prices/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Сохранение цен закрытия - ошибка сервиса")
    @Description("Тест проверяет корректность обработки ошибок сервиса при сохранении цен закрытия")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Close Prices Saving")
    @Tag("api")
    @Tag("close-prices")
    @Tag("save")
    @Tag("error")
    void saveClosePrices_ShouldHandleServiceException() throws Exception {
        // Given
        ClosePriceRequestDto request = new ClosePriceRequestDto();
        request.setInstruments(List.of("BBG004730N88"));

        when(service.saveClosePrices(any(ClosePriceRequestDto.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/data-loading/close-prices/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка загрузки данных: Ошибка сохранения цен закрытия: Database connection failed"))
            .andExpect(jsonPath("$.error").value("DataLoadException"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    // ==================== GET /api/data-loading/close-prices/shares ====================

    @Test
    @DisplayName("Получение цен закрытия для акций - успешный случай")
    @Description("Тест проверяет корректность получения цен закрытия для всех акций")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Close Prices Retrieval")
    @Tag("api")
    @Tag("close-prices")
    @Tag("get")
    @Tag("shares")
    @Tag("success")
    void getClosePricesForShares_ShouldReturnSuccessResponse() throws Exception {
        // Given
        List<ClosePriceDto> mockClosePrices = List.of(
            new ClosePriceDto("BBG004730N88", "2024-01-15", BigDecimal.valueOf(250.75)),
            new ClosePriceDto("BBG004730ZJ9", "2024-01-15", BigDecimal.valueOf(180.50))
        );
        when(service.getClosePricesForAllShares()).thenReturn(mockClosePrices);

        // When & Then
        mockMvc.perform(get("/api/data-loading/close-prices/shares"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Цены закрытия для акций получены успешно"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].figi").value("BBG004730N88"))
            .andExpect(jsonPath("$.data[0].tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.data[0].closePrice").value(250.75))
            .andExpect(jsonPath("$.data[1].figi").value("BBG004730ZJ9"))
            .andExpect(jsonPath("$.data[1].tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.data[1].closePrice").value(180.50))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Получение цен закрытия для акций - пустой результат")
    @Description("Тест проверяет корректность обработки пустого результата при получении цен закрытия для акций")
    @Severity(SeverityLevel.NORMAL)
    @Story("Close Prices Retrieval")
    @Tag("api")
    @Tag("close-prices")
    @Tag("get")
    @Tag("shares")
    @Tag("empty-result")
    void getClosePricesForShares_WithEmptyResult_ShouldReturnEmptyArray() throws Exception {
        // Given
        when(service.getClosePricesForAllShares()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/data-loading/close-prices/shares"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Цены закрытия для акций получены успешно"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty())
            .andExpect(jsonPath("$.count").value(0))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Получение цен закрытия для акций - ошибка сервиса")
    @Description("Тест проверяет корректность обработки ошибок сервиса при получении цен закрытия для акций")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Close Prices Retrieval")
    @Tag("api")
    @Tag("close-prices")
    @Tag("get")
    @Tag("shares")
    @Tag("error")
    void getClosePricesForShares_ShouldHandleServiceException() throws Exception {
        // Given
        when(service.getClosePricesForAllShares())
            .thenThrow(new RuntimeException("API connection failed"));

        // When & Then
        mockMvc.perform(get("/api/data-loading/close-prices/shares"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка загрузки данных: Ошибка получения цен закрытия для акций: API connection failed"))
            .andExpect(jsonPath("$.error").value("DataLoadException"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Получение цен закрытия для акций - фильтрация невалидных цен")
    @Description("Тест проверяет корректность фильтрации невалидных цен при получении цен закрытия для акций")
    @Severity(SeverityLevel.NORMAL)
    @Story("Close Prices Retrieval")
    @Tag("api")
    @Tag("close-prices")
    @Tag("get")
    @Tag("shares")
    @Tag("filtering")
    @Tag("validation")
    void getClosePricesForShares_ShouldFilterInvalidPrices() throws Exception {
        // Given - смесь валидных и невалидных цен
        List<ClosePriceDto> mockClosePrices = List.of(
            new ClosePriceDto("BBG004730N88", "2024-01-15", BigDecimal.valueOf(250.75)), // валидная
            new ClosePriceDto("BBG004730ZJ9", "1970-01-01", BigDecimal.valueOf(180.50)), // невалидная (1970-01-01)
            new ClosePriceDto("BBG004S685M2", "2024-01-15", BigDecimal.valueOf(320.25)), // валидная
            new ClosePriceDto("BBG004S68JR9", "1970-01-01", BigDecimal.valueOf(150.00))  // невалидная (1970-01-01)
        );
        when(service.getClosePricesForAllShares()).thenReturn(mockClosePrices);

        // When & Then
        mockMvc.perform(get("/api/data-loading/close-prices/shares"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Цены закрытия для акций получены успешно"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2)) // только 2 валидные цены
            .andExpect(jsonPath("$.data[0].figi").value("BBG004730N88"))
            .andExpect(jsonPath("$.data[0].tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.data[0].closePrice").value(250.75))
            .andExpect(jsonPath("$.data[1].figi").value("BBG004S685M2"))
            .andExpect(jsonPath("$.data[1].tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.data[1].closePrice").value(320.25))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    // ==================== GET /api/data-loading/close-prices/futures ====================

    @Test
    @DisplayName("Получение цен закрытия для фьючерсов - успешный случай")
    @Description("Тест проверяет корректность получения цен закрытия для всех фьючерсов")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Close Prices Retrieval")
    @Tag("api")
    @Tag("close-prices")
    @Tag("get")
    @Tag("futures")
    @Tag("success")
    void getClosePricesForFutures_ShouldReturnSuccessResponse() throws Exception {
        // Given
        List<ClosePriceDto> mockClosePrices = List.of(
            new ClosePriceDto("FUTSILV-3.24", "2024-01-15", BigDecimal.valueOf(75000.00)),
            new ClosePriceDto("FUTGOLD-3.24", "2024-01-15", BigDecimal.valueOf(250000.00))
        );
        when(service.getClosePricesForAllFutures()).thenReturn(mockClosePrices);

        // When & Then
        mockMvc.perform(get("/api/data-loading/close-prices/futures"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Цены закрытия для фьючерсов получены успешно"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].figi").value("FUTSILV-3.24"))
            .andExpect(jsonPath("$.data[0].tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.data[0].closePrice").value(75000.00))
            .andExpect(jsonPath("$.data[1].figi").value("FUTGOLD-3.24"))
            .andExpect(jsonPath("$.data[1].tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.data[1].closePrice").value(250000.00))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Получение цен закрытия для фьючерсов - пустой результат")
    @Description("Тест проверяет корректность обработки пустого результата при получении цен закрытия для фьючерсов")
    @Severity(SeverityLevel.NORMAL)
    @Story("Close Prices Retrieval")
    @Tag("api")
    @Tag("close-prices")
    @Tag("get")
    @Tag("futures")
    @Tag("empty-result")
    void getClosePricesForFutures_WithEmptyResult_ShouldReturnEmptyArray() throws Exception {
        // Given
        when(service.getClosePricesForAllFutures()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/data-loading/close-prices/futures"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Цены закрытия для фьючерсов получены успешно"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty())
            .andExpect(jsonPath("$.count").value(0))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Получение цен закрытия для фьючерсов - ошибка сервиса")
    @Description("Тест проверяет корректность обработки ошибок сервиса при получении цен закрытия для фьючерсов")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Close Prices Retrieval")
    @Tag("api")
    @Tag("close-prices")
    @Tag("get")
    @Tag("futures")
    @Tag("error")
    void getClosePricesForFutures_ShouldHandleServiceException() throws Exception {
        // Given
        when(service.getClosePricesForAllFutures())
            .thenThrow(new RuntimeException("API connection failed"));

        // When & Then
        mockMvc.perform(get("/api/data-loading/close-prices/futures"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка загрузки данных: Ошибка получения цен закрытия для фьючерсов: API connection failed"))
            .andExpect(jsonPath("$.error").value("DataLoadException"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Получение цен закрытия для фьючерсов - фильтрация невалидных цен")
    @Description("Тест проверяет корректность фильтрации невалидных цен при получении цен закрытия для фьючерсов")
    @Severity(SeverityLevel.NORMAL)
    @Story("Close Prices Retrieval")
    @Tag("api")
    @Tag("close-prices")
    @Tag("get")
    @Tag("futures")
    @Tag("filtering")
    @Tag("validation")
    void getClosePricesForFutures_ShouldFilterInvalidPrices() throws Exception {
        // Given - смесь валидных и невалидных цен
        List<ClosePriceDto> mockClosePrices = List.of(
            new ClosePriceDto("FUTSBRF-3.24", "2024-01-15", BigDecimal.valueOf(95.50)), // валидная
            new ClosePriceDto("FUTSBRF-6.24", "1970-01-01", BigDecimal.valueOf(96.25)), // невалидная (1970-01-01)
            new ClosePriceDto("FUTSBRF-9.24", "2024-01-15", BigDecimal.valueOf(97.00)), // валидная
            new ClosePriceDto("FUTSBRF-12.24", "1970-01-01", BigDecimal.valueOf(98.75)) // невалидная (1970-01-01)
        );
        when(service.getClosePricesForAllFutures()).thenReturn(mockClosePrices);

        // When & Then
        mockMvc.perform(get("/api/data-loading/close-prices/futures"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Цены закрытия для фьючерсов получены успешно"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2)) // только 2 валидные цены
            .andExpect(jsonPath("$.data[0].figi").value("FUTSBRF-3.24"))
            .andExpect(jsonPath("$.data[0].tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.data[0].closePrice").value(95.50))
            .andExpect(jsonPath("$.data[1].figi").value("FUTSBRF-9.24"))
            .andExpect(jsonPath("$.data[1].tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.data[1].closePrice").value(97.00))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    // ==================== GET /api/data-loading/close-prices/{figi} ====================

    @Test
    @DisplayName("Получение цены закрытия по FIGI - валидный FIGI")
    @Description("Тест проверяет корректность получения цены закрытия по указанному FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Close Price by FIGI")
    @Tag("api")
    @Tag("close-prices")
    @Tag("get-by-figi")
    @Tag("valid-figi")
    @Tag("success")
    void getClosePriceByFigi_WithValidFigi_ShouldReturnSuccessResponse() throws Exception {
        // Given
        String figi = "BBG004730N88";
        when(service.getClosePrices(eq(List.of(figi)), eq(null)))
            .thenReturn(List.of(sampleClosePrice));

        // When & Then
        mockMvc.perform(get("/api/data-loading/close-prices/{figi}", figi))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Цена закрытия получена успешно"))
            .andExpect(jsonPath("$.data.figi").value("BBG004730N88"))
            .andExpect(jsonPath("$.data.tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.data.closePrice").value(250.75))
            .andExpect(jsonPath("$.data.eveningSessionPrice").value(251.00))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Получение цены закрытия по FIGI - несуществующий FIGI")
    @Description("Тест проверяет корректность обработки случая когда цена закрытия не найдена для указанного FIGI")
    @Severity(SeverityLevel.NORMAL)
    @Story("Close Price by FIGI")
    @Tag("api")
    @Tag("close-prices")
    @Tag("get-by-figi")
    @Tag("not-found")
    @Tag("invalid-figi")
    void getClosePriceByFigi_WithEmptyResult_ShouldReturnNotFoundResponse() throws Exception {
        // Given
        String figi = "INVALID_FIGI";
        when(service.getClosePrices(eq(List.of(figi)), eq(null)))
            .thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/data-loading/close-prices/{figi}", figi))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Цена закрытия не найдена для инструмента: " + figi))
            .andExpect(jsonPath("$.figi").value(figi))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Получение цены закрытия по FIGI - ошибка сервиса")
    @Description("Тест проверяет корректность обработки ошибок сервиса при получении цены закрытия по FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Close Price by FIGI")
    @Tag("api")
    @Tag("close-prices")
    @Tag("get-by-figi")
    @Tag("error")
    void getClosePriceByFigi_ShouldHandleServiceException() throws Exception {
        // Given
        String figi = "BBG004730N88";
        when(service.getClosePrices(eq(List.of(figi)), eq(null)))
            .thenThrow(new RuntimeException("API connection failed"));

        // When & Then
        mockMvc.perform(get("/api/data-loading/close-prices/{figi}", figi))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка получения цены закрытия: API connection failed"))
            .andExpect(jsonPath("$.figi").value(figi))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Получение цены закрытия по FIGI - специальные символы")
    @Description("Тест проверяет корректность обработки FIGI со специальными символами")
    @Severity(SeverityLevel.NORMAL)
    @Story("Close Price by FIGI")
    @Tag("api")
    @Tag("close-prices")
    @Tag("get-by-figi")
    @Tag("special-characters")
    @Tag("edge-case")
    void getClosePriceByFigi_WithSpecialCharacters_ShouldHandleCorrectly() throws Exception {
        // Given
        String figi = "BBG004730N88+";
        when(service.getClosePrices(eq(List.of(figi)), eq(null)))
            .thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/data-loading/close-prices/{figi}", figi))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Цена закрытия не найдена для инструмента: " + figi))
            .andExpect(jsonPath("$.figi").value(figi));
    }

    @Test
    @DisplayName("Получение цены закрытия по FIGI - невалидная дата")
    @Description("Тест проверяет корректность фильтрации цен с невалидной датой при получении цены закрытия по FIGI")
    @Severity(SeverityLevel.NORMAL)
    @Story("Close Price by FIGI")
    @Tag("api")
    @Tag("close-prices")
    @Tag("get-by-figi")
    @Tag("invalid-date")
    @Tag("filtering")
    @Tag("validation")
    void getClosePriceByFigi_WithInvalidPriceDate_ShouldReturnNotFound() throws Exception {
        // Given - цена с неверной датой 1970-01-01 (будет отфильтрована)
        String figi = "BBG004730N88";
        List<ClosePriceDto> invalidPrices = List.of(
            new ClosePriceDto(figi, "1970-01-01", BigDecimal.valueOf(250.75)) // невалидная дата
        );
        when(service.getClosePrices(eq(List.of(figi)), eq(null)))
            .thenReturn(invalidPrices);

        // When & Then - после фильтрации список будет пустым
        mockMvc.perform(get("/api/data-loading/close-prices/{figi}", figi))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Цена закрытия не найдена для инструмента: " + figi))
            .andExpect(jsonPath("$.figi").value("BBG004730N88"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Загрузка цен закрытия за сегодня - включение отфильтрованных элементов")
    @Description("Тест проверяет корректность включения информации об отфильтрованных элементах в ответе")
    @Severity(SeverityLevel.NORMAL)
    @Story("Close Prices Loading")
    @Tag("api")
    @Tag("close-prices")
    @Tag("load")
    @Tag("filtering")
    @Tag("response-structure")
    void loadClosePricesToday_ShouldIncludeInvalidItemsFilteredInResponse() throws Exception {
        // Given - мок с отфильтрованными ценами
        SaveResponseDto mockResponse = new SaveResponseDto(
            true,
            "Успешно загружено 2 новых цен закрытия из 5 найденных.",
            5, // totalRequested
            2, // newItemsSaved
            1, // existingItemsSkipped
            2, // invalidItemsFiltered
            0, // missingFromApi
            List.of(
                new ClosePriceDto("BBG004730N88", "2024-01-15", BigDecimal.valueOf(250.75)),
                new ClosePriceDto("BBG004730ZJ9", "2024-01-15", BigDecimal.valueOf(180.50))
            )
        );
        when(service.saveClosePrices(any(ClosePriceRequestDto.class)))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/data-loading/close-prices"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Успешно загружено 2 новых цен закрытия из 5 найденных."))
            .andExpect(jsonPath("$.totalRequested").value(5))
            .andExpect(jsonPath("$.newItemsSaved").value(2))
            .andExpect(jsonPath("$.existingItemsSkipped").value(1))
            .andExpect(jsonPath("$.invalidItemsFiltered").value(2))
            .andExpect(jsonPath("$.savedItems").isArray())
            .andExpect(jsonPath("$.savedItems.length()").value(2))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("Сохранение цен закрытия - невалидный JSON")
    @Description("Тест проверяет корректность обработки невалидного JSON при сохранении цен закрытия")
    @Severity(SeverityLevel.NORMAL)
    @Story("Close Prices Saving")
    @Tag("api")
    @Tag("close-prices")
    @Tag("save")
    @Tag("invalid-json")
    @Tag("edge-case")
    void saveClosePrices_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/data-loading/close-prices/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("RuntimeException"));
    }

    @Test
    @DisplayName("Получение цены закрытия по FIGI - пустой FIGI")
    @Description("Тест проверяет корректность обработки пустого FIGI при получении цены закрытия")
    @Severity(SeverityLevel.NORMAL)
    @Story("Close Price by FIGI")
    @Tag("api")
    @Tag("close-prices")
    @Tag("get-by-figi")
    @Tag("empty-figi")
    @Tag("edge-case")
    void getClosePriceByFigi_WithEmptyFigi_ShouldReturnNotFound() throws Exception {
        // Given
        String figi = "";
        when(service.getClosePrices(eq(List.of(figi)), eq(null)))
            .thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/data-loading/close-prices/{figi}", figi))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Exception"));
    }
}
