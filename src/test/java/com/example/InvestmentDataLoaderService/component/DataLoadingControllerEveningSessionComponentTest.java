package com.example.InvestmentDataLoaderService.component;

import com.example.InvestmentDataLoaderService.dto.ClosePriceDto;
import com.example.InvestmentDataLoaderService.dto.ClosePriceEveningSessionRequestDto;
import com.example.InvestmentDataLoaderService.dto.ClosePriceEveningSessionDto;
import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.service.MainSessionPriceService;
import com.example.InvestmentDataLoaderService.scheduler.EveningSessionSchedulerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class DataLoadingControllerEveningSessionComponentTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private MainSessionPriceService mainSessionPriceService;

    @MockBean
    private EveningSessionSchedulerService eveningSessionService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private ClosePriceDto sampleClosePrice;
    private ClosePriceEveningSessionDto sampleEveningSessionDto;
    private SaveResponseDto sampleSaveResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

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
    void loadEveningSessionPricesToday_WithFullSpringContext_ShouldReturnSuccessResponse() throws Exception {
        // Given
        when(eveningSessionService.fetchAndStoreEveningSessionPricesForDate(any(LocalDate.class)))
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
    void loadEveningSessionPricesToday_WithServiceError_ShouldHandleGracefully() throws Exception {
        // Given
        when(eveningSessionService.fetchAndStoreEveningSessionPricesForDate(any(LocalDate.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/data-loading/evening-session-prices"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка загрузки данных: Ошибка загрузки цен вечерней сессии за сегодня: Database connection failed"))
            .andExpect(jsonPath("$.error").value("DataLoadException"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    // ==================== POST /api/data-loading/evening-session-prices/save ====================

    @Test
    void saveEveningSessionPrices_WithValidRequest_ShouldProcessCorrectly() throws Exception {
        // Given
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(List.of("BBG004730N88", "BBG004730ZJ9"));

        when(eveningSessionService.fetchAndStoreEveningSessionPricesForDate(any(LocalDate.class)))
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
    void saveEveningSessionPrices_WithNullRequest_ShouldCreateEmptyRequest() throws Exception {
        // Given
        when(eveningSessionService.fetchAndStoreEveningSessionPricesForDate(any(LocalDate.class)))
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
    void saveEveningSessionPrices_WithEmptyRequest_ShouldProcessCorrectly() throws Exception {
        // Given
        when(eveningSessionService.fetchAndStoreEveningSessionPricesForDate(any(LocalDate.class)))
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
    void saveEveningSessionPrices_WithLargeInstrumentList_ShouldHandleCorrectly() throws Exception {
        // Given
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(List.of(
            "BBG004730N88", "BBG004730ZJ9", "BBG004S685M2", "BBG004S68JR9", "BBG0063FKTD1"
        ));

        SaveResponseDto largeResponse = new SaveResponseDto(
            true,
            "Успешно загружено 25 новых цен вечерней сессии из 50 найденных.",
            50,
            25,
            25,
            0, // invalidItemsFiltered
            0, // missingFromApi
            List.of(sampleEveningSessionDto)
        );

        when(eveningSessionService.fetchAndStoreEveningSessionPricesForDate(any(LocalDate.class)))
            .thenReturn(largeResponse);

        // When & Then
        mockMvc.perform(post("/api/data-loading/evening-session-prices/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Успешно загружено 25 новых цен вечерней сессии из 50 найденных."))
            .andExpect(jsonPath("$.totalRequested").value(50))
            .andExpect(jsonPath("$.newItemsSaved").value(25))
            .andExpect(jsonPath("$.existingItemsSkipped").value(25))
            .andExpect(jsonPath("$.invalidItemsFiltered").value(0));
    }

    // ==================== POST /api/data-loading/evening-session-prices/{date} ====================

    @Test
    void loadEveningSessionPricesForDate_WithValidDate_ShouldProcessCorrectly() throws Exception {
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
    void getEveningSessionPricesForShares_ShouldReturnSuccessResponse() throws Exception {
        // Given
        List<ClosePriceDto> mockClosePrices = List.of(
            new ClosePriceDto("BBG004730N88", "2024-01-15", BigDecimal.valueOf(250.75), BigDecimal.valueOf(251.00)),
            new ClosePriceDto("BBG004730ZJ9", "2024-01-15", BigDecimal.valueOf(180.50), BigDecimal.valueOf(181.00))
        );
        when(mainSessionPriceService.getClosePricesForAllShares()).thenReturn(mockClosePrices);

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
    void getEveningSessionPricesForShares_WithServiceError_ShouldHandleGracefully() throws Exception {
        // Given
        when(mainSessionPriceService.getClosePricesForAllShares())
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

    // ==================== GET /api/data-loading/evening-session-prices/futures ====================

    @Test
    void getEveningSessionPricesForFutures_ShouldReturnSuccessResponse() throws Exception {
        // Given
        List<ClosePriceDto> mockClosePrices = List.of(
            new ClosePriceDto("FUTSILV-3.24", "2024-01-15", BigDecimal.valueOf(75000.00), BigDecimal.valueOf(75100.00)),
            new ClosePriceDto("FUTGOLD-3.24", "2024-01-15", BigDecimal.valueOf(250000.00), BigDecimal.valueOf(250500.00))
        );
        when(mainSessionPriceService.getClosePricesForAllFutures()).thenReturn(mockClosePrices);

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
    void getEveningSessionPricesForFutures_WithServiceError_ShouldHandleGracefully() throws Exception {
        // Given
        when(mainSessionPriceService.getClosePricesForAllFutures())
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

    // ==================== GET /api/data-loading/evening-session-prices/{figi} ====================

    @Test
    void getEveningSessionPriceByFigi_WithValidFigi_ShouldReturnSuccessResponse() throws Exception {
        // Given
        String figi = "BBG004730N88";
        when(mainSessionPriceService.getClosePrices(eq(List.of(figi)), eq(null)))
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
    void getEveningSessionPriceByFigi_WithEmptyResult_ShouldReturnNotFoundResponse() throws Exception {
        // Given
        String figi = "INVALID_FIGI";
        when(mainSessionPriceService.getClosePrices(eq(List.of(figi)), eq(null)))
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
    void getEveningSessionPriceByFigi_WithServiceException_ShouldReturnErrorResponse() throws Exception {
        // Given
        String figi = "BBG004730N88";
        when(mainSessionPriceService.getClosePrices(eq(List.of(figi)), eq(null)))
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

    // ==================== INTEGRATION SCENARIOS ====================

    @Test
    void eveningSessionEndpoints_ShouldBeConsistent_AcrossAllMethods() throws Exception {
        // Given
        String figi = "BBG004730N88";
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(List.of(figi));

        when(mainSessionPriceService.getClosePrices(eq(List.of(figi)), eq(null)))
            .thenReturn(List.of(sampleClosePrice));
        when(eveningSessionService.fetchAndStoreEveningSessionPricesForDate(any(LocalDate.class)))
            .thenReturn(sampleSaveResponse);

        // Test GET endpoint
        mockMvc.perform(get("/api/data-loading/evening-session-prices/{figi}", figi))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.figi").value(figi));

        // Test POST /evening-session-prices endpoint
        mockMvc.perform(post("/api/data-loading/evening-session-prices"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        // Test POST /evening-session-prices/save endpoint
        mockMvc.perform(post("/api/data-loading/evening-session-prices/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void saveEveningSessionPrices_WithServiceError_ShouldHandleGracefully() throws Exception {
        // Given
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(List.of("BBG004730N88"));

        when(eveningSessionService.fetchAndStoreEveningSessionPricesForDate(any(LocalDate.class)))
            .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/api/data-loading/evening-session-prices/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка загрузки данных: Ошибка сохранения цен вечерней сессии: Service error"))
            .andExpect(jsonPath("$.error").value("DataLoadException"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void eveningSessionEndpoints_ShouldValidateInput_Appropriately() throws Exception {
        // Test with invalid JSON
        mockMvc.perform(post("/api/data-loading/evening-session-prices/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("RuntimeException"));

        // Test with empty FIGI
        when(mainSessionPriceService.getClosePrices(eq(List.of("")), eq(null)))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/data-loading/evening-session-prices/{figi}", ""))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Exception"));
    }

    @Test
    void eveningSessionEndpoints_ShouldReturnProperTimestamps() throws Exception {
        // Given
        when(mainSessionPriceService.getClosePrices(eq(List.of("BBG004730N88")), eq(null)))
            .thenReturn(List.of(sampleClosePrice));

        // When & Then
        mockMvc.perform(get("/api/data-loading/evening-session-prices/{figi}", "BBG004730N88"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    void getEveningSessionPricesForShares_ShouldFilterInvalidPrices() throws Exception {
        // Given - смесь валидных и невалидных цен
        List<ClosePriceDto> mockClosePrices = List.of(
            new ClosePriceDto("BBG004730N88", "2024-01-15", BigDecimal.valueOf(250.75), BigDecimal.valueOf(251.00)), // валидная
            new ClosePriceDto("BBG004730ZJ9", "1970-01-01", BigDecimal.valueOf(180.50), BigDecimal.valueOf(181.00)), // невалидная (1970-01-01)
            new ClosePriceDto("BBG004S685M2", "2024-01-15", BigDecimal.valueOf(320.25), BigDecimal.valueOf(321.00)), // валидная
            new ClosePriceDto("BBG004S68JR9", "1970-01-01", BigDecimal.valueOf(150.00), BigDecimal.valueOf(151.00))  // невалидная (1970-01-01)
        );
        when(mainSessionPriceService.getClosePricesForAllShares()).thenReturn(mockClosePrices);

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

    @Test
    void getEveningSessionPricesForFutures_ShouldFilterInvalidPrices() throws Exception {
        // Given - смесь валидных и невалидных цен
        List<ClosePriceDto> mockClosePrices = List.of(
            new ClosePriceDto("FUTSBRF-3.24", "2024-01-15", BigDecimal.valueOf(95.50), BigDecimal.valueOf(96.00)), // валидная
            new ClosePriceDto("FUTSBRF-6.24", "1970-01-01", BigDecimal.valueOf(96.25), BigDecimal.valueOf(97.00)), // невалидная (1970-01-01)
            new ClosePriceDto("FUTSBRF-9.24", "2024-01-15", BigDecimal.valueOf(97.00), BigDecimal.valueOf(98.00)), // валидная
            new ClosePriceDto("FUTSBRF-12.24", "1970-01-01", BigDecimal.valueOf(98.75), BigDecimal.valueOf(99.00)) // невалидная (1970-01-01)
        );
        when(mainSessionPriceService.getClosePricesForAllFutures()).thenReturn(mockClosePrices);

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

    @Test
    void getEveningSessionPriceByFigi_WithInvalidPriceDate_ShouldReturnNotFound() throws Exception {
        // Given - цена с неверной датой 1970-01-01 (будет отфильтрована)
        String figi = "BBG004730N88";
        List<ClosePriceDto> invalidPrices = List.of(
            new ClosePriceDto(figi, "1970-01-01", BigDecimal.valueOf(250.75), BigDecimal.valueOf(251.00)) // невалидная дата
        );
        when(mainSessionPriceService.getClosePrices(eq(List.of(figi)), eq(null)))
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
    void getEveningSessionPriceByFigi_WithNullEveningSessionPrice_ShouldReturnNotFound() throws Exception {
        // Given - цена без eveningSessionPrice (будет отфильтрована)
        String figi = "BBG004730N88";
        List<ClosePriceDto> pricesWithoutEveningSession = List.of(
            new ClosePriceDto(figi, "2024-01-15", BigDecimal.valueOf(250.75), null) // без eveningSessionPrice
        );
        when(mainSessionPriceService.getClosePrices(eq(List.of(figi)), eq(null)))
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
}
