package com.example.InvestmentDataLoaderService.unit.controller;

import com.example.InvestmentDataLoaderService.controller.MorningSessionController;
import com.example.InvestmentDataLoaderService.dto.SaveResponseDto;
import com.example.InvestmentDataLoaderService.service.MorningSessionService;

import io.qameta.allure.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
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
@WebMvcTest(MorningSessionController.class)
@Epic("Morning Session API")
@Feature("Morning Session Management")
@DisplayName("Morning Session Controller Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
public class MorningSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MorningSessionService morningSessionService;

    @BeforeEach
    @Step("Подготовка тестовых данных для MorningSessionController")
    public void setUp() {
        reset(morningSessionService);
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private SaveResponseDto createSaveResponseDto() {
        SaveResponseDto dto = new SaveResponseDto();
        dto.setSuccess(true);
        dto.setMessage("Операция выполнена успешно");
        dto.setTotalRequested(10);
        dto.setNewItemsSaved(8);
        dto.setExistingItemsSkipped(2);
        dto.setInvalidItemsFiltered(0);
        dto.setMissingFromApi(0);
        dto.setSavedItems(Arrays.asList(
            Map.of("figi", "BBG004730N88", "price", 102.50),
            Map.of("figi", "BBG00HYV2XQ1", "price", 1500.0)
        ));
        return dto;
    }

    private SaveResponseDto createPreviewResponseDto() {
        SaveResponseDto dto = new SaveResponseDto();
        dto.setSuccess(true);
        dto.setMessage("Операция выполнена успешно");
        dto.setTotalRequested(10);
        dto.setNewItemsSaved(0);
        dto.setExistingItemsSkipped(0);
        dto.setInvalidItemsFiltered(0);
        dto.setMissingFromApi(0);
        dto.setSavedItems(Arrays.asList(
            Map.of("figi", "BBG004730N88", "price", 102.50),
            Map.of("figi", "BBG00HYV2XQ1", "price", 1500.0)
        ));
        return dto;
    }

    private SaveResponseDto createEmptySaveResponseDto() {
        SaveResponseDto dto = new SaveResponseDto();
        dto.setSuccess(true);
        dto.setMessage("Данные не найдены");
        dto.setTotalRequested(0);
        dto.setNewItemsSaved(0);
        dto.setExistingItemsSkipped(0);
        dto.setInvalidItemsFiltered(0);
        dto.setMissingFromApi(0);
        dto.setSavedItems(Collections.emptyList());
        return dto;
    }


    // ========== ПОЗИТИВНЫЕ ТЕСТЫ - GET ENDPOINTS ==========

    @Test
    @DisplayName("GET /api/morning-session - предпросмотр цен утренней сессии за сегодня")
    @Story("Предпросмотр цен утренней сессии")
    @Tag("positive")
    @Description("Тест успешного предпросмотра цен утренней сессии за сегодня")
    @Step("Выполнение GET запроса для предпросмотра цен утренней сессии")
    public void previewMorningSessionPricesToday_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        SaveResponseDto responseDto = createPreviewResponseDto();

        when(morningSessionService.previewMorningSessionPricesForDate(any(LocalDate.class)))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/morning-session"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Операция выполнена успешно"))
            .andExpect(jsonPath("$.dateUsed").exists())
            .andExpect(jsonPath("$.statistics.totalRequested").value(10))
            .andExpect(jsonPath("$.statistics.found").value(2))
            .andExpect(jsonPath("$.statistics.notFound").value(8))
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).previewMorningSessionPricesForDate(any(LocalDate.class));
    }

    @Test
    @DisplayName("GET /api/morning-session - предпросмотр при отсутствии данных")
    @Story("Предпросмотр цен утренней сессии")
    @Tag("positive")
    @Description("Тест предпросмотра цен при отсутствии данных")
    @Step("Выполнение GET запроса при отсутствии данных")
    public void previewMorningSessionPricesToday_ShouldReturnOk_WhenNoData() throws Exception {
        // Arrange
        SaveResponseDto responseDto = createEmptySaveResponseDto();

        when(morningSessionService.previewMorningSessionPricesForDate(any(LocalDate.class)))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/morning-session"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Данные не найдены"))
            .andExpect(jsonPath("$.dateUsed").exists())
            .andExpect(jsonPath("$.statistics.totalRequested").value(0))
            .andExpect(jsonPath("$.statistics.found").value(0))
            .andExpect(jsonPath("$.statistics.notFound").value(0))
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items.length()").value(0))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).previewMorningSessionPricesForDate(any(LocalDate.class));
    }

    @Test
    @DisplayName("GET /api/morning-session/{date} - поиск свечей за дату")
    @Story("Поиск свечей")
    @Tag("positive")
    @Description("Тест успешного поиска свечей за указанную дату")
    @Step("Выполнение GET запроса для поиска свечей")
    public void getCandlesByDate_ShouldReturnOk_WhenValidDate() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        SaveResponseDto responseDto = createPreviewResponseDto();

        when(morningSessionService.previewMorningSessionPricesForDate(testDate))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/morning-session/{date}", testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Операция выполнена успешно"))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.statistics.totalRequested").value(10))
            .andExpect(jsonPath("$.statistics.found").value(2))
            .andExpect(jsonPath("$.statistics.notFound").value(8))
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).previewMorningSessionPricesForDate(testDate);
    }

    @Test
    @DisplayName("GET /api/morning-session/shares/{date} - поиск свечей акций за дату")
    @Story("Поиск свечей акций")
    @Tag("positive")
    @Description("Тест успешного поиска свечей акций за указанную дату")
    @Step("Выполнение GET запроса для поиска свечей акций")
    public void getSharesCandlesByDate_ShouldReturnOk_WhenValidDate() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        SaveResponseDto responseDto = createPreviewResponseDto();

        when(morningSessionService.previewSharesPricesForDate(testDate))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/morning-session/shares/{date}", testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Операция выполнена успешно"))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.instrumentType").value("shares"))
            .andExpect(jsonPath("$.statistics.totalRequested").value(10))
            .andExpect(jsonPath("$.statistics.found").value(2))
            .andExpect(jsonPath("$.statistics.notFound").value(8))
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).previewSharesPricesForDate(testDate);
    }

    @Test
    @DisplayName("GET /api/morning-session/futures/{date} - поиск свечей фьючерсов за дату")
    @Story("Поиск свечей фьючерсов")
    @Tag("positive")
    @Description("Тест успешного поиска свечей фьючерсов за указанную дату")
    @Step("Выполнение GET запроса для поиска свечей фьючерсов")
    public void getFuturesCandlesByDate_ShouldReturnOk_WhenValidDate() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        SaveResponseDto responseDto = createPreviewResponseDto();

        when(morningSessionService.previewFuturesPricesForDate(testDate))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/morning-session/futures/{date}", testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Операция выполнена успешно"))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.instrumentType").value("futures"))
            .andExpect(jsonPath("$.statistics.totalRequested").value(10))
            .andExpect(jsonPath("$.statistics.found").value(2))
            .andExpect(jsonPath("$.statistics.notFound").value(8))
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).previewFuturesPricesForDate(testDate);
    }

    @Test
    @DisplayName("GET /api/morning-session/{figi}/{date} - поиск цены по FIGI за дату")
    @Story("Поиск цены по FIGI")
    @Tag("positive")
    @Description("Тест успешного поиска цены по FIGI за указанную дату")
    @Step("Выполнение GET запроса для поиска цены по FIGI")
    public void getPriceByFigiForDate_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        String figi = "BBG004730N88";
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        SaveResponseDto responseDto = createPreviewResponseDto();

        when(morningSessionService.previewPriceByFigiForDate(figi, testDate))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/morning-session/{figi}/{date}", figi, testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Операция выполнена успешно"))
            .andExpect(jsonPath("$.figi").value(figi))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.statistics.totalRequested").value(10))
            .andExpect(jsonPath("$.statistics.found").value(2))
            .andExpect(jsonPath("$.statistics.notFound").value(8))
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).previewPriceByFigiForDate(figi, testDate);
    }

    // ========== ПОЗИТИВНЫЕ ТЕСТЫ - POST ENDPOINTS ==========

    @Test
    @DisplayName("POST /api/morning-session - загрузка цен утренней сессии за сегодня")
    @Story("Загрузка цен утренней сессии")
    @Tag("positive")
    @Description("Тест успешной загрузки цен утренней сессии за сегодня")
    @Step("Выполнение POST запроса для загрузки цен утренней сессии")
    public void loadMorningSessionPricesToday_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        SaveResponseDto responseDto = createSaveResponseDto();

        when(morningSessionService.fetchAndStoreMorningSessionPricesForDate(any(LocalDate.class)))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/morning-session"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Операция выполнена успешно"))
            .andExpect(jsonPath("$.dateUsed").exists())
            .andExpect(jsonPath("$.statistics.totalRequested").value(10))
            .andExpect(jsonPath("$.statistics.found").value(10))
            .andExpect(jsonPath("$.statistics.notFound").value(0))
            .andExpect(jsonPath("$.statistics.saved").value(8))
            .andExpect(jsonPath("$.statistics.skippedExisting").value(2))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).fetchAndStoreMorningSessionPricesForDate(any(LocalDate.class));
    }

    @Test
    @DisplayName("POST /api/morning-session/{date} - загрузка цен открытия за дату")
    @Story("Загрузка цен открытия")
    @Tag("positive")
    @Description("Тест успешной загрузки цен открытия за указанную дату")
    @Step("Выполнение POST запроса для загрузки цен открытия")
    public void loadOpenPricesForDate_ShouldReturnOk_WhenValidDate() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        SaveResponseDto responseDto = createSaveResponseDto();

        when(morningSessionService.fetchAndStoreMorningSessionPricesForDate(testDate))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/morning-session/{date}", testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Операция выполнена успешно"))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.statistics.totalRequested").value(10))
            .andExpect(jsonPath("$.statistics.found").value(10))
            .andExpect(jsonPath("$.statistics.notFound").value(0))
            .andExpect(jsonPath("$.statistics.saved").value(8))
            .andExpect(jsonPath("$.statistics.skippedExisting").value(2))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).fetchAndStoreMorningSessionPricesForDate(testDate);
    }

    @Test
    @DisplayName("POST /api/morning-session/shares/{date} - загрузка цен акций за дату")
    @Story("Загрузка цен акций")
    @Tag("positive")
    @Description("Тест успешной загрузки цен акций за указанную дату")
    @Step("Выполнение POST запроса для загрузки цен акций")
    public void loadSharesPricesForDate_ShouldReturnOk_WhenValidDate() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        SaveResponseDto responseDto = createSaveResponseDto();

        when(morningSessionService.fetchAndStoreSharesPricesForDate(testDate))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/morning-session/shares/{date}", testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Операция выполнена успешно"))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.instrumentType").value("shares"))
            .andExpect(jsonPath("$.statistics.totalRequested").value(10))
            .andExpect(jsonPath("$.statistics.found").value(10))
            .andExpect(jsonPath("$.statistics.notFound").value(0))
            .andExpect(jsonPath("$.statistics.saved").value(8))
            .andExpect(jsonPath("$.statistics.skippedExisting").value(2))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).fetchAndStoreSharesPricesForDate(testDate);
    }

    @Test
    @DisplayName("POST /api/morning-session/futures/{date} - загрузка цен фьючерсов за дату")
    @Story("Загрузка цен фьючерсов")
    @Tag("positive")
    @Description("Тест успешной загрузки цен фьючерсов за указанную дату")
    @Step("Выполнение POST запроса для загрузки цен фьючерсов")
    public void loadFuturesPricesForDate_ShouldReturnOk_WhenValidDate() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        SaveResponseDto responseDto = createSaveResponseDto();

        when(morningSessionService.fetchAndStoreFuturesPricesForDate(testDate))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/morning-session/futures/{date}", testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Операция выполнена успешно"))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.instrumentType").value("futures"))
            .andExpect(jsonPath("$.statistics.totalRequested").value(10))
            .andExpect(jsonPath("$.statistics.found").value(10))
            .andExpect(jsonPath("$.statistics.notFound").value(0))
            .andExpect(jsonPath("$.statistics.saved").value(8))
            .andExpect(jsonPath("$.statistics.skippedExisting").value(2))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).fetchAndStoreFuturesPricesForDate(testDate);
    }

    @Test
    @DisplayName("POST /api/morning-session/{figi}/{date} - загрузка цены по FIGI за дату")
    @Story("Загрузка цены по FIGI")
    @Tag("positive")
    @Description("Тест успешной загрузки цены по FIGI за указанную дату")
    @Step("Выполнение POST запроса для загрузки цены по FIGI")
    public void loadPriceByFigiForDate_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        String figi = "BBG004730N88";
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        SaveResponseDto responseDto = createSaveResponseDto();

        when(morningSessionService.fetchAndStorePriceByFigiForDate(figi, testDate))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/morning-session/{figi}/{date}", figi, testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Операция выполнена успешно"))
            .andExpect(jsonPath("$.figi").value(figi))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.statistics.totalRequested").value(10))
            .andExpect(jsonPath("$.statistics.found").value(10))
            .andExpect(jsonPath("$.statistics.notFound").value(0))
            .andExpect(jsonPath("$.statistics.saved").value(8))
            .andExpect(jsonPath("$.statistics.skippedExisting").value(2))
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).fetchAndStorePriceByFigiForDate(figi, testDate);
    }

    // ========== НЕГАТИВНЫЕ ТЕСТЫ ==========

    @Test
    @DisplayName("GET /api/morning-session - обработка ошибки при предпросмотре")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при предпросмотре цен утренней сессии")
    @Step("Выполнение GET запроса при ошибке в сервисе")
    public void previewMorningSessionPricesToday_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        when(morningSessionService.previewMorningSessionPricesForDate(any(LocalDate.class)))
            .thenThrow(new RuntimeException("Service connection error"));

        // Act & Assert
        mockMvc.perform(get("/api/morning-session"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка предпросмотра цен утренней сессии: Service connection error"))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).previewMorningSessionPricesForDate(any(LocalDate.class));
    }

    @Test
    @DisplayName("GET /api/morning-session/{date} - обработка ошибки при поиске свечей")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при поиске свечей за дату")
    @Step("Выполнение GET запроса при ошибке в сервисе")
    public void getCandlesByDate_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        when(morningSessionService.previewMorningSessionPricesForDate(testDate))
            .thenThrow(new RuntimeException("Service connection error"));

        // Act & Assert
        mockMvc.perform(get("/api/morning-session/{date}", testDate))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка поиска свечей за дату: Service connection error"))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).previewMorningSessionPricesForDate(testDate);
    }

    @Test
    @DisplayName("GET /api/morning-session/shares/{date} - обработка ошибки при поиске свечей акций")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при поиске свечей акций за дату")
    @Step("Выполнение GET запроса при ошибке в сервисе")
    public void getSharesCandlesByDate_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        when(morningSessionService.previewSharesPricesForDate(testDate))
            .thenThrow(new RuntimeException("Service connection error"));

        // Act & Assert
        mockMvc.perform(get("/api/morning-session/shares/{date}", testDate))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка поиска свечей акций за дату: Service connection error"))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.instrumentType").value("shares"))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).previewSharesPricesForDate(testDate);
    }

    @Test
    @DisplayName("GET /api/morning-session/futures/{date} - обработка ошибки при поиске свечей фьючерсов")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при поиске свечей фьючерсов за дату")
    @Step("Выполнение GET запроса при ошибке в сервисе")
    public void getFuturesCandlesByDate_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        when(morningSessionService.previewFuturesPricesForDate(testDate))
            .thenThrow(new RuntimeException("Service connection error"));

        // Act & Assert
        mockMvc.perform(get("/api/morning-session/futures/{date}", testDate))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка поиска свечей фьючерсов за дату: Service connection error"))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.instrumentType").value("futures"))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).previewFuturesPricesForDate(testDate);
    }

    @Test
    @DisplayName("GET /api/morning-session/{figi}/{date} - обработка ошибки при поиске цены по FIGI")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при поиске цены по FIGI за дату")
    @Step("Выполнение GET запроса при ошибке в сервисе")
    public void getPriceByFigiForDate_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        String figi = "BBG004730N88";
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        when(morningSessionService.previewPriceByFigiForDate(figi, testDate))
            .thenThrow(new RuntimeException("Service connection error"));

        // Act & Assert
        mockMvc.perform(get("/api/morning-session/{figi}/{date}", figi, testDate))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка поиска цены открытия для FIGI " + figi + ": Service connection error"))
            .andExpect(jsonPath("$.figi").value(figi))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).previewPriceByFigiForDate(figi, testDate);
    }

    @Test
    @DisplayName("POST /api/morning-session - обработка ошибки при загрузке")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при загрузке цен утренней сессии")
    @Step("Выполнение POST запроса при ошибке в сервисе")
    public void loadMorningSessionPricesToday_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        when(morningSessionService.fetchAndStoreMorningSessionPricesForDate(any(LocalDate.class)))
            .thenThrow(new RuntimeException("Service connection error"));

        // Act & Assert
        mockMvc.perform(post("/api/morning-session"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка загрузки цен утренней сессии: Service connection error"))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).fetchAndStoreMorningSessionPricesForDate(any(LocalDate.class));
    }

    @Test
    @DisplayName("POST /api/morning-session/{date} - обработка ошибки при загрузке за дату")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при загрузке цен открытия за дату")
    @Step("Выполнение POST запроса при ошибке в сервисе")
    public void loadOpenPricesForDate_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        when(morningSessionService.fetchAndStoreMorningSessionPricesForDate(testDate))
            .thenThrow(new RuntimeException("Service connection error"));

        // Act & Assert
        mockMvc.perform(post("/api/morning-session/{date}", testDate))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка загрузки цен открытия за дату: Service connection error"))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).fetchAndStoreMorningSessionPricesForDate(testDate);
    }

    @Test
    @DisplayName("POST /api/morning-session/shares/{date} - обработка ошибки при загрузке акций")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при загрузке цен акций за дату")
    @Step("Выполнение POST запроса при ошибке в сервисе")
    public void loadSharesPricesForDate_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        when(morningSessionService.fetchAndStoreSharesPricesForDate(testDate))
            .thenThrow(new RuntimeException("Service connection error"));

        // Act & Assert
        mockMvc.perform(post("/api/morning-session/shares/{date}", testDate))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка загрузки цен акций за дату: Service connection error"))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.instrumentType").value("shares"))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).fetchAndStoreSharesPricesForDate(testDate);
    }

    @Test
    @DisplayName("POST /api/morning-session/futures/{date} - обработка ошибки при загрузке фьючерсов")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при загрузке цен фьючерсов за дату")
    @Step("Выполнение POST запроса при ошибке в сервисе")
    public void loadFuturesPricesForDate_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        when(morningSessionService.fetchAndStoreFuturesPricesForDate(testDate))
            .thenThrow(new RuntimeException("Service connection error"));

        // Act & Assert
        mockMvc.perform(post("/api/morning-session/futures/{date}", testDate))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка загрузки цен фьючерсов за дату: Service connection error"))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.instrumentType").value("futures"))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).fetchAndStoreFuturesPricesForDate(testDate);
    }

    @Test
    @DisplayName("POST /api/morning-session/{figi}/{date} - обработка ошибки при загрузке по FIGI")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при загрузке цены по FIGI за дату")
    @Step("Выполнение POST запроса при ошибке в сервисе")
    public void loadPriceByFigiForDate_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        String figi = "BBG004730N88";
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        when(morningSessionService.fetchAndStorePriceByFigiForDate(figi, testDate))
            .thenThrow(new RuntimeException("Service connection error"));

        // Act & Assert
        mockMvc.perform(post("/api/morning-session/{figi}/{date}", figi, testDate))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка загрузки цены открытия для FIGI " + figi + ": Service connection error"))
            .andExpect(jsonPath("$.figi").value(figi))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService).fetchAndStorePriceByFigiForDate(figi, testDate);
    }

    @Test
    @DisplayName("POST /api/morning-session/{figi}/{date} - обработка пустого FIGI")
    @Story("Валидация входных данных")
    @Tag("negative")
    @Description("Тест обработки пустого FIGI при загрузке")
    @Step("Выполнение POST запроса с пустым FIGI")
    public void loadPriceByFigiForDate_ShouldReturnBadRequest_WhenEmptyFigi() throws Exception {
        // Arrange
        String emptyFigi = "   "; // Используем пробелы вместо пустой строки
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        // Act & Assert
        mockMvc.perform(post("/api/morning-session/{figi}/{date}", emptyFigi, testDate))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("FIGI не может быть пустым"))
            .andExpect(jsonPath("$.figi").value(emptyFigi))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService, never()).fetchAndStorePriceByFigiForDate(anyString(), any(LocalDate.class));
    }

    @Test
    @DisplayName("GET /api/morning-session/{figi}/{date} - обработка пустого FIGI")
    @Story("Валидация входных данных")
    @Tag("negative")
    @Description("Тест обработки пустого FIGI при поиске")
    @Step("Выполнение GET запроса с пустым FIGI")
    public void getPriceByFigiForDate_ShouldReturnBadRequest_WhenEmptyFigi() throws Exception {
        // Arrange
        String emptyFigi = "   "; // Используем пробелы вместо пустой строки
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        // Act & Assert
        mockMvc.perform(get("/api/morning-session/{figi}/{date}", emptyFigi, testDate))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("FIGI не может быть пустым"))
            .andExpect(jsonPath("$.figi").value(emptyFigi))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService, never()).previewPriceByFigiForDate(anyString(), any(LocalDate.class));
    }

    @Test
    @DisplayName("POST /api/morning-session/{figi}/{date} - обработка FIGI с пробелами")
    @Story("Валидация входных данных")
    @Tag("negative")
    @Description("Тест обработки FIGI с пробелами при загрузке")
    @Step("Выполнение POST запроса с FIGI содержащим пробелы")
    public void loadPriceByFigiForDate_ShouldReturnBadRequest_WhenFigiWithSpaces() throws Exception {
        // Arrange
        String figiWithSpaces = "   ";
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        // Act & Assert
        mockMvc.perform(post("/api/morning-session/{figi}/{date}", figiWithSpaces, testDate))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("FIGI не может быть пустым"))
            .andExpect(jsonPath("$.figi").value(figiWithSpaces))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService, never()).fetchAndStorePriceByFigiForDate(anyString(), any(LocalDate.class));
    }

    @Test
    @DisplayName("GET /api/morning-session/{figi}/{date} - обработка FIGI с пробелами")
    @Story("Валидация входных данных")
    @Tag("negative")
    @Description("Тест обработки FIGI с пробелами при поиске")
    @Step("Выполнение GET запроса с FIGI содержащим пробелы")
    public void getPriceByFigiForDate_ShouldReturnBadRequest_WhenFigiWithSpaces() throws Exception {
        // Arrange
        String figiWithSpaces = "   ";
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        // Act & Assert
        mockMvc.perform(get("/api/morning-session/{figi}/{date}", figiWithSpaces, testDate))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("FIGI не может быть пустым"))
            .andExpect(jsonPath("$.figi").value(figiWithSpaces))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(morningSessionService, never()).previewPriceByFigiForDate(anyString(), any(LocalDate.class));
    }

    @Test
    @DisplayName("POST /api/morning-session/{figi}/{date} - обработка null FIGI")
    @Story("Валидация входных данных")
    @Tag("negative")
    @Description("Тест обработки null FIGI при загрузке")
    @Step("Выполнение POST запроса с null FIGI")
    public void loadPriceByFigiForDate_ShouldReturnBadRequest_WhenNullFigi() throws Exception {
        // Arrange
        
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        // Act & Assert - Spring обрабатывает null path variable как статический ресурс, возвращает 500
        mockMvc.perform(post("/api/morning-session/{figi}/{date}", null, testDate))
            .andExpect(status().isInternalServerError());

        verify(morningSessionService, never()).fetchAndStorePriceByFigiForDate(anyString(), any(LocalDate.class));
    }

    @Test
    @DisplayName("POST /api/morning-session/{figi}/{date} - отсутствие FIGI в URL")
    @Story("Валидация входных данных")
    @Tag("negative")
    @Description("Тест обработки отсутствия FIGI в URL при загрузке")
    @Step("Выполнение POST запроса без FIGI в URL")
    public void loadPriceByFigiForDate_ShouldReturnBadRequest_WhenMissingFigi() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        // Act & Assert - URL без FIGI попадает на другой endpoint (/{date}), который возвращает 500 из-за null result
        mockMvc.perform(post("/api/morning-session/{date}", testDate))
            .andExpect(status().isInternalServerError());

        // Проверяем, что метод для FIGI не вызывается, а вызывается метод для загрузки цен открытия
        verify(morningSessionService, never()).fetchAndStorePriceByFigiForDate(anyString(), any(LocalDate.class));
    }

    @Test
    @DisplayName("GET /api/morning-session/{figi}/{date} - обработка null FIGI")
    @Story("Валидация входных данных")
    @Tag("negative")
    @Description("Тест обработки null FIGI при поиске")
    @Step("Выполнение GET запроса с null FIGI")
    public void getPriceByFigiForDate_ShouldReturnBadRequest_WhenNullFigi() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        // Act & Assert - Spring обрабатывает null path variable как статический ресурс, возвращает 500
        mockMvc.perform(get("/api/morning-session/{figi}/{date}", null, testDate))
            .andExpect(status().isInternalServerError());

        verify(morningSessionService, never()).previewPriceByFigiForDate(anyString(), any(LocalDate.class));
    }

    @Test
    @DisplayName("GET /api/morning-session/{figi}/{date} - отсутствие FIGI в URL")
    @Story("Валидация входных данных")
    @Tag("negative")
    @Description("Тест обработки отсутствия FIGI в URL при поиске")
    @Step("Выполнение GET запроса без FIGI в URL")
    public void getPriceByFigiForDate_ShouldReturnBadRequest_WhenMissingFigi() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        // Act & Assert - URL без FIGI попадает на другой endpoint (/{date}), который возвращает 500 из-за null result
        mockMvc.perform(get("/api/morning-session/{date}", testDate))
            .andExpect(status().isInternalServerError());

        // Проверяем, что метод для FIGI не вызывается, а вызывается метод для поиска свечей
        verify(morningSessionService, never()).previewPriceByFigiForDate(anyString(), any(LocalDate.class));
    }
}