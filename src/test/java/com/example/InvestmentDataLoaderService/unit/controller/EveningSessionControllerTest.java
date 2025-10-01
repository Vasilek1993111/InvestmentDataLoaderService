package com.example.InvestmentDataLoaderService.unit.controller;

import com.example.InvestmentDataLoaderService.controller.EveningSessionController;
import com.example.InvestmentDataLoaderService.entity.*;
import com.example.InvestmentDataLoaderService.repository.*;

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
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit-тесты для EveningSessionController
 */
@WebMvcTest(EveningSessionController.class)
@Epic("Evening Session API")
@Feature("Evening Session Management")
@DisplayName("Evening Session Controller Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
public class EveningSessionControllerTest {
    
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
    @Step("Подготовка тестовых данных для EveningSessionController")
    public void setUp() {
        reset(shareRepository);
        reset(futureRepository);
        reset(minuteCandleRepository);
        reset(closePriceEveningSessionRepository);
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private ShareEntity createShareEntity() {
        ShareEntity share = new ShareEntity();
        share.setFigi("BBG004730N88");
        share.setTicker("SBER");
        share.setName("Сбербанк");
        share.setCurrency("RUB");
        share.setExchange("MOEX");
        share.setSector("Financial");
        share.setTradingStatus("ACTIVE");
        return share;
    }

    private FutureEntity createFutureEntity() {
        return new FutureEntity(
            "FUTSBER0324",
            "SBER-3.24",
            "FUTURES",
            "SBER",
            "RUB",
            "MOEX"
        );
    }

    private MinuteCandleEntity createMinuteCandleEntity() {
        MinuteCandleEntity candle = new MinuteCandleEntity();
        candle.setFigi("BBG004730N88");
        candle.setVolume(1000L);
        candle.setHigh(BigDecimal.valueOf(105.0));
        candle.setLow(BigDecimal.valueOf(95.0));
        candle.setTime(Instant.now());
        candle.setClose(BigDecimal.valueOf(102.0));
        candle.setOpen(BigDecimal.valueOf(100.0));
        candle.setComplete(true);
        return candle;
    }

    private ClosePriceEveningSessionEntity createClosePriceEveningSessionEntity() {
        ClosePriceEveningSessionEntity entity = new ClosePriceEveningSessionEntity();
        entity.setFigi("BBG004730N88");
        entity.setPriceDate(LocalDate.now().minusDays(1));
        entity.setClosePrice(BigDecimal.valueOf(102.0));
        entity.setInstrumentType("SHARE");
        entity.setCurrency("RUB");
        entity.setExchange("MOEX");
        return entity;
    }

    // ========== ПОЗИТИВНЫЕ ТЕСТЫ - GET ENDPOINTS ==========

    @Test
    @DisplayName("GET /api/evening-session-prices - получение цен закрытия вечерней сессии за вчера")
    @Story("Получение цен вечерней сессии")
    @Tag("positive")
    @Description("Тест успешного получения цен закрытия вечерней сессии за вчерашний день")
    @Step("Выполнение GET запроса для получения цен вечерней сессии за вчера")
    public void getEveningSessionClosePricesYesterday_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        List<ShareEntity> shares = Arrays.asList(createShareEntity());
        List<FutureEntity> futures = Arrays.asList(createFutureEntity());
        MinuteCandleEntity candle = createMinuteCandleEntity();

        when(shareRepository.findAll()).thenReturn(shares);
        when(futureRepository.findAll()).thenReturn(futures);
        when(minuteCandleRepository.findLastCandleForDate(anyString(), any(LocalDate.class)))
            .thenReturn(candle);

        // Act & Assert
        mockMvc.perform(get("/api/evening-session-prices"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.statistics.totalProcessed").value(2))
            .andExpect(jsonPath("$.statistics.foundPrices").value(2))
            .andExpect(jsonPath("$.statistics.missingData").value(0));

        verify(shareRepository).findAll();
        verify(futureRepository).findAll();
        verify(minuteCandleRepository, times(2)).findLastCandleForDate(anyString(), any(LocalDate.class));
    }

    @Test
    @DisplayName("GET /api/evening-session-prices - получение цен при отсутствии данных")
    @Story("Получение цен вечерней сессии")
    @Tag("positive")
    @Description("Тест получения цен при отсутствии инструментов")
    @Step("Выполнение GET запроса при пустых репозиториях")
    public void getEveningSessionClosePricesYesterday_ShouldReturnOk_WhenNoData() throws Exception {
        // Arrange
        when(shareRepository.findAll()).thenReturn(Collections.emptyList());
        when(futureRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/evening-session-prices"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0))
            .andExpect(jsonPath("$.count").value(0))
            .andExpect(jsonPath("$.statistics.totalProcessed").value(0))
            .andExpect(jsonPath("$.statistics.foundPrices").value(0))
            .andExpect(jsonPath("$.statistics.missingData").value(0));

        verify(shareRepository).findAll();
        verify(futureRepository).findAll();
    }

    @Test
    @DisplayName("GET /api/evening-session-prices/{date} - получение цен за конкретную дату")
    @Story("Получение цен вечерней сессии")
    @Tag("positive")
    @Description("Тест получения цен вечерней сессии за конкретную дату")
    @Step("Выполнение GET запроса с параметром даты")
    public void getEveningSessionPricesForAllInstruments_ShouldReturnOk_WhenValidDate() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        List<ShareEntity> shares = Arrays.asList(createShareEntity());
        List<FutureEntity> futures = Arrays.asList(createFutureEntity());
        MinuteCandleEntity candle = createMinuteCandleEntity();

        when(shareRepository.findAll()).thenReturn(shares);
        when(futureRepository.findAll()).thenReturn(futures);
        when(minuteCandleRepository.findLastCandleForDate(anyString(), eq(testDate)))
            .thenReturn(candle);

        // Act & Assert
        mockMvc.perform(get("/api/evening-session-prices/{date}", testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.totalProcessed").value(2))
            .andExpect(jsonPath("$.foundPrices").value(2))
            .andExpect(jsonPath("$.missingData").value(0));

        verify(shareRepository).findAll();
        verify(futureRepository).findAll();
        verify(minuteCandleRepository, times(2)).findLastCandleForDate(anyString(), eq(testDate));
    }

    @Test
    @DisplayName("GET /api/evening-session-prices/shares/{date} - получение цен для акций")
    @Story("Получение цен вечерней сессии")
    @Tag("positive")
    @Description("Тест получения цен вечерней сессии только для акций")
    @Step("Выполнение GET запроса для акций")
    public void getEveningSessionPricesForShares_ShouldReturnOk_WhenValidDate() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        List<ShareEntity> shares = Arrays.asList(createShareEntity());
        MinuteCandleEntity candle = createMinuteCandleEntity();

        when(shareRepository.findAll()).thenReturn(shares);
        when(minuteCandleRepository.findLastCandleForDate(anyString(), eq(testDate)))
            .thenReturn(candle);

        // Act & Assert
        mockMvc.perform(get("/api/evening-session-prices/shares/{date}", testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.totalProcessed").value(1))
            .andExpect(jsonPath("$.foundPrices").value(1))
            .andExpect(jsonPath("$.missingData").value(0));

        verify(shareRepository).findAll();
        verify(minuteCandleRepository).findLastCandleForDate(anyString(), eq(testDate));
    }

    @Test
    @DisplayName("GET /api/evening-session-prices/futures/{date} - получение цен для фьючерсов")
    @Story("Получение цен вечерней сессии")
    @Tag("positive")
    @Description("Тест получения цен вечерней сессии только для фьючерсов")
    @Step("Выполнение GET запроса для фьючерсов")
    public void getEveningSessionPricesForFutures_ShouldReturnOk_WhenValidDate() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        List<FutureEntity> futures = Arrays.asList(createFutureEntity());
        MinuteCandleEntity candle = createMinuteCandleEntity();

        when(futureRepository.findAll()).thenReturn(futures);
        when(minuteCandleRepository.findLastCandleForDate(anyString(), eq(testDate)))
            .thenReturn(candle);

        // Act & Assert
        mockMvc.perform(get("/api/evening-session-prices/futures/{date}", testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.totalProcessed").value(1))
            .andExpect(jsonPath("$.foundPrices").value(1))
            .andExpect(jsonPath("$.missingData").value(0));

        verify(futureRepository).findAll();
        verify(minuteCandleRepository).findLastCandleForDate(anyString(), eq(testDate));
    }

    @Test
    @DisplayName("GET /api/evening-session-prices/{figi}/{date} - получение цены по инструменту")
    @Story("Получение цен вечерней сессии")
    @Tag("positive")
    @Description("Тест получения цены вечерней сессии для конкретного инструмента")
    @Step("Выполнение GET запроса для конкретного инструмента")
    public void getEveningSessionPriceByFigiAndDate_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        String figi = "BBG004730N88";
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        MinuteCandleEntity candle = createMinuteCandleEntity();

        when(minuteCandleRepository.findLastCandleForDate(figi, testDate))
            .thenReturn(candle);

        // Act & Assert
        mockMvc.perform(get("/api/evening-session-prices/{figi}/{date}", figi, testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.figi").value(figi))
            .andExpect(jsonPath("$.data.priceDate").value(testDate.toString()))
            .andExpect(jsonPath("$.data.closePrice").value(102.0))
            .andExpect(jsonPath("$.figi").value(figi))
            .andExpect(jsonPath("$.date").value(testDate.toString()));

        verify(minuteCandleRepository).findLastCandleForDate(figi, testDate);
    }

    // ========== ПОЗИТИВНЫЕ ТЕСТЫ - POST ENDPOINTS ==========

    @Test
    @DisplayName("POST /api/evening-session-prices - загрузка цен за вчера")
    @Story("Загрузка цен вечерней сессии")
    @Tag("positive")
    @Description("Тест успешной загрузки цен закрытия вечерней сессии за вчерашний день")
    @Step("Выполнение POST запроса для загрузки цен за вчера")
    public void loadEveningSessionPrices_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        List<ShareEntity> shares = Arrays.asList(createShareEntity());
        List<FutureEntity> futures = Arrays.asList(createFutureEntity());
        MinuteCandleEntity candle = createMinuteCandleEntity();

        when(shareRepository.findAll()).thenReturn(shares);
        when(futureRepository.findAll()).thenReturn(futures);
        when(minuteCandleRepository.findLastCandleForDate(anyString(), any(LocalDate.class)))
            .thenReturn(candle);
        when(closePriceEveningSessionRepository.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(closePriceEveningSessionRepository.save(any(ClosePriceEveningSessionEntity.class)))
            .thenReturn(createClosePriceEveningSessionEntity());

        // Act & Assert
        mockMvc.perform(post("/api/evening-session-prices"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.statistics.totalRequested").value(2))
            .andExpect(jsonPath("$.statistics.newItemsSaved").value(2))
            .andExpect(jsonPath("$.statistics.existingItemsSkipped").value(0))
            .andExpect(jsonPath("$.statistics.invalidItemsFiltered").value(0))
            .andExpect(jsonPath("$.statistics.missingFromApi").value(0));

        verify(shareRepository).findAll();
        verify(futureRepository).findAll();
        verify(minuteCandleRepository, times(2)).findLastCandleForDate(anyString(), any(LocalDate.class));
        verify(closePriceEveningSessionRepository, times(2)).save(any(ClosePriceEveningSessionEntity.class));
    }

    @Test
    @DisplayName("POST /api/evening-session-prices/{date} - загрузка цен за конкретную дату")
    @Story("Загрузка цен вечерней сессии")
    @Tag("positive")
    @Description("Тест успешной загрузки цен вечерней сессии за конкретную дату")
    @Step("Выполнение POST запроса с параметром даты")
    public void loadEveningSessionPricesForDate_ShouldReturnOk_WhenValidDate() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        List<ShareEntity> shares = Arrays.asList(createShareEntity());
        List<FutureEntity> futures = Arrays.asList(createFutureEntity());
        MinuteCandleEntity candle = createMinuteCandleEntity();

        when(shareRepository.findAll()).thenReturn(shares);
        when(futureRepository.findAll()).thenReturn(futures);
        when(minuteCandleRepository.findLastCandleForDate(anyString(), eq(testDate)))
            .thenReturn(candle);
        when(closePriceEveningSessionRepository.existsByPriceDateAndFigi(eq(testDate), anyString()))
            .thenReturn(false);
        when(closePriceEveningSessionRepository.save(any(ClosePriceEveningSessionEntity.class)))
            .thenReturn(createClosePriceEveningSessionEntity());

        // Act & Assert
        mockMvc.perform(post("/api/evening-session-prices/{date}", testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.totalRequested").value(2))
            .andExpect(jsonPath("$.newItemsSaved").value(2))
            .andExpect(jsonPath("$.existingItemsSkipped").value(0))
            .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
            .andExpect(jsonPath("$.missingFromApi").value(0));

        verify(shareRepository).findAll();
        verify(futureRepository).findAll();
        verify(minuteCandleRepository, times(2)).findLastCandleForDate(anyString(), eq(testDate));
        verify(closePriceEveningSessionRepository, times(2)).save(any(ClosePriceEveningSessionEntity.class));
    }

    @Test
    @DisplayName("POST /api/evening-session-prices/shares/{date} - сохранение цен для акций")
    @Story("Сохранение цен вечерней сессии")
    @Tag("positive")
    @Description("Тест успешного сохранения цен вечерней сессии для акций")
    @Step("Выполнение POST запроса для сохранения цен акций")
    public void saveEveningSessionPricesForShares_ShouldReturnOk_WhenValidDate() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        List<ShareEntity> shares = Arrays.asList(createShareEntity());
        MinuteCandleEntity candle = createMinuteCandleEntity();

        when(shareRepository.findAll()).thenReturn(shares);
        when(minuteCandleRepository.findLastCandleForDate(anyString(), eq(testDate)))
            .thenReturn(candle);
        when(closePriceEveningSessionRepository.existsByPriceDateAndFigi(eq(testDate), anyString()))
            .thenReturn(false);
        when(closePriceEveningSessionRepository.save(any(ClosePriceEveningSessionEntity.class)))
            .thenReturn(createClosePriceEveningSessionEntity());

        // Act & Assert
        mockMvc.perform(post("/api/evening-session-prices/shares/{date}", testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.totalProcessed").value(1))
            .andExpect(jsonPath("$.newItemsSaved").value(1))
            .andExpect(jsonPath("$.existingItemsSkipped").value(0))
            .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
            .andExpect(jsonPath("$.missingData").value(0));

        verify(shareRepository).findAll();
        verify(minuteCandleRepository).findLastCandleForDate(anyString(), eq(testDate));
        verify(closePriceEveningSessionRepository).save(any(ClosePriceEveningSessionEntity.class));
    }

    @Test
    @DisplayName("POST /api/evening-session-prices/futures/{date} - сохранение цен для фьючерсов")
    @Story("Сохранение цен вечерней сессии")
    @Tag("positive")
    @Description("Тест успешного сохранения цен вечерней сессии для фьючерсов")
    @Step("Выполнение POST запроса для сохранения цен фьючерсов")
    public void saveEveningSessionPricesForFutures_ShouldReturnOk_WhenValidDate() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        List<FutureEntity> futures = Arrays.asList(createFutureEntity());
        MinuteCandleEntity candle = createMinuteCandleEntity();

        when(futureRepository.findAll()).thenReturn(futures);
        when(minuteCandleRepository.findLastCandleForDate(anyString(), eq(testDate)))
            .thenReturn(candle);
        when(closePriceEveningSessionRepository.existsByPriceDateAndFigi(eq(testDate), anyString()))
            .thenReturn(false);
        when(closePriceEveningSessionRepository.save(any(ClosePriceEveningSessionEntity.class)))
            .thenReturn(createClosePriceEveningSessionEntity());

        // Act & Assert
        mockMvc.perform(post("/api/evening-session-prices/futures/{date}", testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.totalProcessed").value(1))
            .andExpect(jsonPath("$.newItemsSaved").value(1))
            .andExpect(jsonPath("$.existingItemsSkipped").value(0))
            .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
            .andExpect(jsonPath("$.missingData").value(0));

        verify(futureRepository).findAll();
        verify(minuteCandleRepository).findLastCandleForDate(anyString(), eq(testDate));
        verify(closePriceEveningSessionRepository).save(any(ClosePriceEveningSessionEntity.class));
    }

    @Test
    @DisplayName("POST /api/evening-session-prices/{figi}/{date} - сохранение цены по инструменту")
    @Story("Сохранение цен вечерней сессии")
    @Tag("positive")
    @Description("Тест успешного сохранения цены вечерней сессии для конкретного инструмента")
    @Step("Выполнение POST запроса для сохранения цены конкретного инструмента")
    public void saveEveningSessionPriceByFigiAndDate_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        String figi = "BBG004730N88";
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        MinuteCandleEntity candle = createMinuteCandleEntity();
        List<ShareEntity> shares = Arrays.asList(createShareEntity());

        when(closePriceEveningSessionRepository.existsByPriceDateAndFigi(testDate, figi))
            .thenReturn(false);
        when(minuteCandleRepository.findLastCandleForDate(figi, testDate))
            .thenReturn(candle);
        when(shareRepository.findAll()).thenReturn(shares);
        when(futureRepository.findAll()).thenReturn(Collections.emptyList());
        when(closePriceEveningSessionRepository.save(any(ClosePriceEveningSessionEntity.class)))
            .thenReturn(createClosePriceEveningSessionEntity());

        // Act & Assert
        mockMvc.perform(post("/api/evening-session-prices/{figi}/{date}", figi, testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.figi").value(figi))
            .andExpect(jsonPath("$.data.priceDate").value(testDate.toString()))
            .andExpect(jsonPath("$.data.closePrice").value(102.0))
            .andExpect(jsonPath("$.figi").value(figi))
            .andExpect(jsonPath("$.date").value(testDate.toString()));

        verify(closePriceEveningSessionRepository).existsByPriceDateAndFigi(testDate, figi);
        verify(minuteCandleRepository).findLastCandleForDate(figi, testDate);
        verify(closePriceEveningSessionRepository).save(any(ClosePriceEveningSessionEntity.class));
    }

    // ========== НЕГАТИВНЫЕ ТЕСТЫ ==========

    @Test
    @DisplayName("GET /api/evening-session-prices - обработка ошибки при получении данных")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при получении цен вечерней сессии")
    @Step("Выполнение GET запроса при ошибке в репозитории")
    public void getEveningSessionClosePricesYesterday_ShouldReturnInternalServerError_WhenRepositoryThrowsException() throws Exception {
        // Arrange
        when(shareRepository.findAll()).thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        mockMvc.perform(get("/api/evening-session-prices"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").exists());

        verify(shareRepository).findAll();
    }

    @Test
    @DisplayName("POST /api/evening-session-prices - обработка ошибки при сохранении")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при сохранении цен вечерней сессии")
    @Step("Выполнение POST запроса при ошибке в репозитории")
    public void loadEveningSessionPrices_ShouldReturnInternalServerError_WhenRepositoryThrowsException() throws Exception {
        // Arrange
        when(shareRepository.findAll()).thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        mockMvc.perform(post("/api/evening-session-prices"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").exists());

        verify(shareRepository).findAll();
    }

    @Test
    @DisplayName("GET /api/evening-session-prices/{figi}/{date} - обработка отсутствия данных")
    @Story("Обработка отсутствия данных")
    @Tag("negative")
    @Description("Тест обработки случая, когда данные не найдены")
    @Step("Выполнение GET запроса при отсутствии данных")
    public void getEveningSessionPriceByFigiAndDate_ShouldReturnOk_WhenNoData() throws Exception {
        // Arrange
        String figi = "BBG004730N88";
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        when(minuteCandleRepository.findLastCandleForDate(figi, testDate))
            .thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/evening-session-prices/{figi}/{date}", figi, testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Цена вечерней сессии не найдена для инструмента " + figi + " за " + testDate))
            .andExpect(jsonPath("$.figi").value(figi))
            .andExpect(jsonPath("$.date").value(testDate.toString()));

        verify(minuteCandleRepository).findLastCandleForDate(figi, testDate);
    }

    @Test
    @DisplayName("POST /api/evening-session-prices/{figi}/{date} - обработка существующей записи")
    @Story("Обработка дублирования данных")
    @Tag("negative")
    @Description("Тест обработки случая, когда запись уже существует")
    @Step("Выполнение POST запроса для существующей записи")
    public void saveEveningSessionPriceByFigiAndDate_ShouldReturnOk_WhenRecordExists() throws Exception {
        // Arrange
        String figi = "BBG004730N88";
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        when(closePriceEveningSessionRepository.existsByPriceDateAndFigi(testDate, figi))
            .thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/evening-session-prices/{figi}/{date}", figi, testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Запись уже существует для инструмента " + figi + " за " + testDate))
            .andExpect(jsonPath("$.figi").value(figi))
            .andExpect(jsonPath("$.date").value(testDate.toString()));

        verify(closePriceEveningSessionRepository).existsByPriceDateAndFigi(testDate, figi);
        verify(minuteCandleRepository, never()).findLastCandleForDate(anyString(), any(LocalDate.class));
        verify(closePriceEveningSessionRepository, never()).save(any(ClosePriceEveningSessionEntity.class));
    }

    @Test
    @DisplayName("GET /api/evening-session-prices/{figi}/{date} - обработка невалидной цены")
    @Story("Обработка невалидных данных")
    @Tag("negative")
    @Description("Тест обработки случая с невалидной ценой (ноль)")
    @Step("Выполнение GET запроса с невалидной ценой")
    public void getEveningSessionPriceByFigiAndDate_ShouldReturnOk_WhenInvalidPrice() throws Exception {
        // Arrange
        String figi = "BBG004730N88";
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        MinuteCandleEntity candle = createMinuteCandleEntity();
        candle.setClose(BigDecimal.ZERO); // Невалидная цена

        when(minuteCandleRepository.findLastCandleForDate(figi, testDate))
            .thenReturn(candle);

        // Act & Assert
        mockMvc.perform(get("/api/evening-session-prices/{figi}/{date}", figi, testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Невалидная цена закрытия для инструмента " + figi + " за " + testDate))
            .andExpect(jsonPath("$.figi").value(figi))
            .andExpect(jsonPath("$.date").value(testDate.toString()));

        verify(minuteCandleRepository).findLastCandleForDate(figi, testDate);
    }

    @Test
    @DisplayName("POST /api/evening-session-prices/{figi}/{date} - обработка невалидной цены")
    @Story("Обработка невалидных данных")
    @Tag("negative")
    @Description("Тест обработки случая с невалидной ценой при сохранении")
    @Step("Выполнение POST запроса с невалидной ценой")
    public void saveEveningSessionPriceByFigiAndDate_ShouldReturnOk_WhenInvalidPrice() throws Exception {
        // Arrange
        String figi = "BBG004730N88";
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        MinuteCandleEntity candle = createMinuteCandleEntity();
        candle.setClose(BigDecimal.ZERO); // Невалидная цена

        when(closePriceEveningSessionRepository.existsByPriceDateAndFigi(testDate, figi))
            .thenReturn(false);
        when(minuteCandleRepository.findLastCandleForDate(figi, testDate))
            .thenReturn(candle);

        // Act & Assert
        mockMvc.perform(post("/api/evening-session-prices/{figi}/{date}", figi, testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Невалидная цена закрытия для инструмента " + figi + " за " + testDate))
            .andExpect(jsonPath("$.figi").value(figi))
            .andExpect(jsonPath("$.date").value(testDate.toString()));

        verify(closePriceEveningSessionRepository).existsByPriceDateAndFigi(testDate, figi);
        verify(minuteCandleRepository).findLastCandleForDate(figi, testDate);
        verify(closePriceEveningSessionRepository, never()).save(any(ClosePriceEveningSessionEntity.class));
    }
}