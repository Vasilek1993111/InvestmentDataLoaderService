package com.example.InvestmentDataLoaderService.unit.controller;

import com.example.InvestmentDataLoaderService.controller.MainSessionPricesController;
import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;
import com.example.InvestmentDataLoaderService.repository.*;
import com.example.InvestmentDataLoaderService.service.MainSessionPriceService;
import com.example.InvestmentDataLoaderService.service.InstrumentService;
import com.example.InvestmentDataLoaderService.exception.DataLoadException;

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
import java.time.LocalDateTime;
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
    @Step("Подготовка тестовых данных для MainSessionPricesController")
    public void setUp() {
        reset(mainSessionPriceService);
        reset(instrumentService);
        reset(shareRepository);
        reset(futureRepository);
        reset(minuteCandleRepository);
        reset(closePriceRepository);
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private ShareDto createShareDto() {
        return new ShareDto(
            "BBG004730N88",
            "SBER",
            "Сбербанк",
            "RUB",
            "MOEX",
            "Financial",
            "ACTIVE",
            true
        );
    }

    private FutureDto createFutureDto() {
        return new FutureDto(
            "FUTSBER0324",
            "SBER-3.24",
            "FUTURES",
            "SBER",
            "RUB",
            "MOEX",
            true,
            LocalDateTime.of(2024, 3, 15, 18, 45)
        );
    }


    private ClosePriceDto createClosePriceDto() {
        return new ClosePriceDto(
            "BBG004730N88",
            "2024-01-15",
            BigDecimal.valueOf(102.50)
        );
    }

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

    private ClosePriceEntity createClosePriceEntity() {
        return new ClosePriceEntity(
            LocalDate.of(2024, 1, 15),
            "BBG004730N88",
            "SHARE",
            BigDecimal.valueOf(102.50),
            "RUB",
            "MOEX"
        );
    }


    // ========== ПОЗИТИВНЫЕ ТЕСТЫ - GET ENDPOINTS ==========

    @Test
    @DisplayName("GET /api/main-session-prices/shares - получение цен закрытия для акций")
    @Story("Получение цен закрытия")
    @Tag("positive")
    @Description("Тест успешного получения цен закрытия для акций")
    @Step("Выполнение GET запроса для получения цен закрытия акций")
    public void getClosePricesForShares_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        List<ClosePriceDto> closePrices = Arrays.asList(createClosePriceDto());

        when(mainSessionPriceService.getClosePricesForAllShares())
            .thenReturn(closePrices);

        // Act & Assert
        mockMvc.perform(get("/api/main-session-prices/shares"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Цены закрытия для акций получены успешно"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(mainSessionPriceService).getClosePricesForAllShares();
    }

    @Test
    @DisplayName("GET /api/main-session-prices/shares - получение цен при пустом ответе")
    @Story("Получение цен закрытия")
    @Tag("positive")
    @Description("Тест получения цен закрытия при пустом ответе")
    @Step("Выполнение GET запроса при отсутствии данных")
    public void getClosePricesForShares_ShouldReturnOk_WhenEmptyResponse() throws Exception {
        // Arrange
        when(mainSessionPriceService.getClosePricesForAllShares())
            .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/main-session-prices/shares"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0))
            .andExpect(jsonPath("$.count").value(0));

        verify(mainSessionPriceService).getClosePricesForAllShares();
    }

    @Test
    @DisplayName("GET /api/main-session-prices/futures - получение цен закрытия для фьючерсов")
    @Story("Получение цен закрытия")
    @Tag("positive")
    @Description("Тест успешного получения цен закрытия для фьючерсов")
    @Step("Выполнение GET запроса для получения цен закрытия фьючерсов")
    public void getClosePricesForFutures_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        List<ClosePriceDto> closePrices = Arrays.asList(createClosePriceDto());

        when(mainSessionPriceService.getClosePricesForAllFutures())
            .thenReturn(closePrices);

        // Act & Assert
        mockMvc.perform(get("/api/main-session-prices/futures"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Цены закрытия для фьючерсов получены успешно"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(mainSessionPriceService).getClosePricesForAllFutures();
    }

    @Test
    @DisplayName("GET /api/main-session-prices/{figi} - получение цены закрытия по инструменту")
    @Story("Получение цен закрытия")
    @Tag("positive")
    @Description("Тест успешного получения цены закрытия для конкретного инструмента")
    @Step("Выполнение GET запроса для получения цены закрытия конкретного инструмента")
    public void getClosePriceByFigi_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        String figi = "BBG004730N88";
        List<ClosePriceDto> closePrices = Arrays.asList(createClosePriceDto());

        when(mainSessionPriceService.getClosePrices(List.of(figi), null))
            .thenReturn(closePrices);

        // Act & Assert
        mockMvc.perform(get("/api/main-session-prices/{figi}", figi))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Цена закрытия получена успешно"))
            .andExpect(jsonPath("$.data.figi").value(figi))
            .andExpect(jsonPath("$.data.tradingDate").value("2024-01-15"))
            .andExpect(jsonPath("$.data.closePrice").value(102.50))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(mainSessionPriceService).getClosePrices(List.of(figi), null);
    }

    @Test
    @DisplayName("GET /api/main-session-prices/{figi} - обработка отсутствия данных")
    @Story("Получение цен закрытия")
    @Tag("positive")
    @Description("Тест обработки случая, когда данные не найдены")
    @Step("Выполнение GET запроса при отсутствии данных")
    public void getClosePriceByFigi_ShouldReturnOk_WhenNoData() throws Exception {
        // Arrange
        String figi = "BBG004730N88";

        when(mainSessionPriceService.getClosePrices(List.of(figi), null))
            .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/main-session-prices/{figi}", figi))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Цена закрытия не найдена для инструмента: " + figi))
            .andExpect(jsonPath("$.figi").value(figi))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(mainSessionPriceService).getClosePrices(List.of(figi), null);
    }

    // ========== ПОЗИТИВНЫЕ ТЕСТЫ - POST ENDPOINTS ==========

    @Test
    @DisplayName("POST /api/main-session-prices/ - загрузка цен закрытия для всех инструментов")
    @Story("Загрузка цен закрытия")
    @Tag("positive")
    @Description("Тест успешной загрузки цен закрытия для всех инструментов")
    @Step("Выполнение POST запроса для загрузки цен закрытия")
    public void loadClosePricesToday_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        List<ShareDto> shares = Arrays.asList(createShareDto());
        List<FutureDto> futures = Arrays.asList(createFutureDto());
        List<ClosePriceDto> closePrices = Arrays.asList(createClosePriceDto());

        when(instrumentService.getShares(null, null, "RUB", null, null))
            .thenReturn(shares);
        when(instrumentService.getFutures(null, null, "RUB", null, null))
            .thenReturn(futures);
        when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
            .thenReturn(closePrices);
        when(closePriceRepository.existsById(any(ClosePriceKey.class)))
            .thenReturn(false);
        when(instrumentService.getShareByFigi(anyString()))
            .thenReturn(createShareDto());
        when(closePriceRepository.save(any(ClosePriceEntity.class)))
            .thenReturn(createClosePriceEntity());

        // Act & Assert
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

        verify(instrumentService).getShares(null, null, "RUB", null, null);
        verify(instrumentService).getFutures(null, null, "RUB", null, null);
        verify(mainSessionPriceService).getClosePrices(anyList(), isNull());
        verify(closePriceRepository).save(any(ClosePriceEntity.class));
    }

    @Test
    @DisplayName("POST /api/main-session-prices/ - обработка отсутствия инструментов")
    @Story("Загрузка цен закрытия")
    @Tag("positive")
    @Description("Тест обработки случая, когда инструменты не найдены")
    @Step("Выполнение POST запроса при отсутствии инструментов")
    public void loadClosePricesToday_ShouldReturnOk_WhenNoInstruments() throws Exception {
        // Arrange
        when(instrumentService.getShares(null, null, "RUB", null, null))
            .thenReturn(Collections.emptyList());
        when(instrumentService.getFutures(null, null, "RUB", null, null))
            .thenReturn(Collections.emptyList());

        // Act & Assert
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

        verify(instrumentService).getShares(null, null, "RUB", null, null);
        verify(instrumentService).getFutures(null, null, "RUB", null, null);
    }

    @Test
    @DisplayName("POST /api/main-session-prices/shares - загрузка цен закрытия для акций")
    @Story("Загрузка цен закрытия")
    @Tag("positive")
    @Description("Тест успешной загрузки цен закрытия для акций")
    @Step("Выполнение POST запроса для загрузки цен закрытия акций")
    public void loadClosePricesForShares_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        List<ShareDto> shares = Arrays.asList(createShareDto());
        List<ClosePriceDto> closePrices = Arrays.asList(createClosePriceDto());

        when(instrumentService.getShares(null, null, "RUB", null, null))
            .thenReturn(shares);
        when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
            .thenReturn(closePrices);
        when(closePriceRepository.existsById(any(ClosePriceKey.class)))
            .thenReturn(false);
        when(instrumentService.getShareByFigi(anyString()))
            .thenReturn(createShareDto());
        when(closePriceRepository.save(any(ClosePriceEntity.class)))
            .thenReturn(createClosePriceEntity());

        // Act & Assert
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

        verify(instrumentService).getShares(null, null, "RUB", null, null);
        verify(mainSessionPriceService).getClosePrices(anyList(), isNull());
        verify(closePriceRepository).save(any(ClosePriceEntity.class));
    }

    @Test
    @DisplayName("POST /api/main-session-prices/futures - загрузка цен закрытия для фьючерсов")
    @Story("Загрузка цен закрытия")
    @Tag("positive")
    @Description("Тест успешной загрузки цен закрытия для фьючерсов")
    @Step("Выполнение POST запроса для загрузки цен закрытия фьючерсов")
    public void loadClosePricesForFutures_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        List<FutureDto> futures = Arrays.asList(createFutureDto());
        List<ClosePriceDto> closePrices = Arrays.asList(createClosePriceDto());

        when(instrumentService.getFutures(null, null, "RUB", null, null))
            .thenReturn(futures);
        when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
            .thenReturn(closePrices);
        when(closePriceRepository.existsById(any(ClosePriceKey.class)))
            .thenReturn(false);
        when(instrumentService.getFutureByFigi(anyString()))
            .thenReturn(createFutureDto());
        when(closePriceRepository.save(any(ClosePriceEntity.class)))
            .thenReturn(createClosePriceEntity());

        // Act & Assert
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

        verify(instrumentService).getFutures(null, null, "RUB", null, null);
        verify(mainSessionPriceService).getClosePrices(anyList(), isNull());
        verify(closePriceRepository).save(any(ClosePriceEntity.class));
    }

    @Test
    @DisplayName("POST /api/main-session-prices/instrument/{figi} - загрузка цены закрытия по инструменту")
    @Story("Загрузка цен закрытия")
    @Tag("positive")
    @Description("Тест успешной загрузки цены закрытия для конкретного инструмента")
    @Step("Выполнение POST запроса для загрузки цены закрытия конкретного инструмента")
    public void loadClosePriceByFigi_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        String figi = "BBG004730N88";
        List<ClosePriceDto> closePrices = Arrays.asList(createClosePriceDto());

        when(mainSessionPriceService.getClosePrices(List.of(figi), null))
            .thenReturn(closePrices);
        when(closePriceRepository.existsById(any(ClosePriceKey.class)))
            .thenReturn(false);
        when(instrumentService.getShareByFigi(figi))
            .thenReturn(createShareDto());
        when(closePriceRepository.save(any(ClosePriceEntity.class)))
            .thenReturn(createClosePriceEntity());

        // Act & Assert
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

        verify(mainSessionPriceService).getClosePrices(List.of(figi), null);
        verify(closePriceRepository).save(any(ClosePriceEntity.class));
    }

    @Test
    @DisplayName("POST /api/main-session-prices/{date} - загрузка цен основной сессии за дату")
    @Story("Загрузка цен основной сессии")
    @Tag("positive")
    @Description("Тест успешной загрузки цен основной сессии за рабочий день")
    @Step("Выполнение POST запроса для загрузки цен основной сессии")
    public void loadMainSessionPricesForDate_ShouldReturnOk_WhenValidDate() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15); // Понедельник
        List<ShareEntity> shares = Arrays.asList(createShareEntity());
        List<FutureEntity> futures = Arrays.asList(createFutureEntity());
        MinuteCandleEntity candle = createMinuteCandleEntity();

        when(shareRepository.findAll()).thenReturn(shares);
        when(futureRepository.findAll()).thenReturn(futures);
        when(closePriceRepository.existsById(any(ClosePriceKey.class)))
            .thenReturn(false);
        when(minuteCandleRepository.findByFigiAndTimeBetween(anyString(), any(Instant.class), any(Instant.class)))
            .thenReturn(Arrays.asList(candle));
        when(closePriceRepository.save(any(ClosePriceEntity.class)))
            .thenReturn(createClosePriceEntity());

        // Act & Assert
        mockMvc.perform(post("/api/main-session-prices/{date}", testDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.date").value(testDate.toString()))
            .andExpect(jsonPath("$.totalRequested").value(2))
            .andExpect(jsonPath("$.newItemsSaved").value(2))
            .andExpect(jsonPath("$.existingItemsSkipped").value(0))
            .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
            .andExpect(jsonPath("$.missingFromApi").value(0))
            .andExpect(jsonPath("$.savedItems").isArray());

        verify(shareRepository).findAll();
        verify(futureRepository).findAll();
        verify(closePriceRepository, times(2)).save(any(ClosePriceEntity.class));
    }

    @Test
    @DisplayName("POST /api/main-session-prices/{date} - обработка выходного дня")
    @Story("Загрузка цен основной сессии")
    @Tag("positive")
    @Description("Тест обработки запроса в выходной день")
    @Step("Выполнение POST запроса в выходной день")
    public void loadMainSessionPricesForDate_ShouldReturnOk_WhenWeekend() throws Exception {
        // Arrange
        LocalDate weekendDate = LocalDate.of(2024, 1, 13); // Суббота

        // Act & Assert
        mockMvc.perform(post("/api/main-session-prices/{date}", weekendDate))
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

        verify(shareRepository, never()).findAll();
        verify(futureRepository, never()).findAll();
    }

    // ========== НЕГАТИВНЫЕ ТЕСТЫ ==========

    @Test
    @DisplayName("GET /api/main-session-prices/shares - обработка ошибки при получении данных")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при получении цен закрытия для акций")
    @Step("Выполнение GET запроса при ошибке в сервисе")
    public void getClosePricesForShares_ShouldThrowException_WhenServiceThrowsException() throws Exception {
        // Arrange
        when(mainSessionPriceService.getClosePricesForAllShares())
            .thenThrow(new DataLoadException("API connection error"));

        // Act & Assert
        mockMvc.perform(get("/api/main-session-prices/shares"))
            .andExpect(status().isInternalServerError());

        verify(mainSessionPriceService).getClosePricesForAllShares();
    }

    @Test
    @DisplayName("GET /api/main-session-prices/futures - обработка ошибки при получении данных")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при получении цен закрытия для фьючерсов")
    @Step("Выполнение GET запроса при ошибке в сервисе")
    public void getClosePricesForFutures_ShouldThrowException_WhenServiceThrowsException() throws Exception {
        // Arrange
        when(mainSessionPriceService.getClosePricesForAllFutures())
            .thenThrow(new DataLoadException("API connection error"));

        // Act & Assert
        mockMvc.perform(get("/api/main-session-prices/futures"))
            .andExpect(status().isInternalServerError());

        verify(mainSessionPriceService).getClosePricesForAllFutures();
    }

    @Test
    @DisplayName("GET /api/main-session-prices/{figi} - обработка ошибки при получении данных")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при получении цены закрытия для инструмента")
    @Step("Выполнение GET запроса при ошибке в сервисе")
    public void getClosePriceByFigi_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        String figi = "BBG004730N88";
        when(mainSessionPriceService.getClosePrices(List.of(figi), null))
            .thenThrow(new RuntimeException("API connection error"));

        // Act & Assert
        mockMvc.perform(get("/api/main-session-prices/{figi}", figi))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Ошибка получения цены закрытия: API connection error"))
            .andExpect(jsonPath("$.figi").value(figi))
            .andExpect(jsonPath("$.timestamp").exists());

        verify(mainSessionPriceService).getClosePrices(List.of(figi), null);
    }

    @Test
    @DisplayName("POST /api/main-session-prices/ - обработка ошибки при загрузке данных")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при загрузке цен закрытия")
    @Step("Выполнение POST запроса при ошибке в сервисе")
    public void loadClosePricesToday_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        when(instrumentService.getShares(null, null, "RUB", null, null))
            .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
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

        verify(instrumentService).getShares(null, null, "RUB", null, null);
    }

    @Test
    @DisplayName("POST /api/main-session-prices/shares - обработка ошибки при загрузке данных")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при загрузке цен закрытия для акций")
    @Step("Выполнение POST запроса при ошибке в сервисе")
    public void loadClosePricesForShares_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        when(instrumentService.getShares(null, null, "RUB", null, null))
            .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
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

        verify(instrumentService).getShares(null, null, "RUB", null, null);
    }

    @Test
    @DisplayName("POST /api/main-session-prices/futures - обработка ошибки при загрузке данных")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при загрузке цен закрытия для фьючерсов")
    @Step("Выполнение POST запроса при ошибке в сервисе")
    public void loadClosePricesForFutures_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        when(instrumentService.getFutures(null, null, "RUB", null, null))
            .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
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

        verify(instrumentService).getFutures(null, null, "RUB", null, null);
    }

    @Test
    @DisplayName("POST /api/main-session-prices/instrument/{figi} - обработка ошибки при загрузке данных")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при загрузке цены закрытия для инструмента")
    @Step("Выполнение POST запроса при ошибке в сервисе")
    public void loadClosePriceByFigi_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        String figi = "BBG004730N88";
        when(mainSessionPriceService.getClosePrices(List.of(figi), null))
            .thenThrow(new RuntimeException("API connection error"));

        // Act & Assert
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

        verify(mainSessionPriceService).getClosePrices(List.of(figi), null);
    }

    @Test
    @DisplayName("POST /api/main-session-prices/{date} - обработка ошибки при загрузке данных")
    @Story("Обработка ошибок")
    @Tag("negative")
    @Description("Тест обработки ошибки при загрузке цен основной сессии")
    @Step("Выполнение POST запроса при ошибке в репозитории")
    public void loadMainSessionPricesForDate_ShouldReturnInternalServerError_WhenRepositoryThrowsException() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        when(shareRepository.findAll())
            .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        mockMvc.perform(post("/api/main-session-prices/{date}", testDate))
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

        verify(shareRepository).findAll();
    }

    @Test
    @DisplayName("POST /api/main-session-prices/instrument/{figi} - обработка существующей записи")
    @Story("Обработка дублирования данных")
    @Tag("negative")
    @Description("Тест обработки случая, когда запись уже существует")
    @Step("Выполнение POST запроса для существующей записи")
    public void loadClosePriceByFigi_ShouldReturnOk_WhenRecordExists() throws Exception {
        // Arrange
        String figi = "BBG004730N88";
        List<ClosePriceDto> closePrices = Arrays.asList(createClosePriceDto());

        when(mainSessionPriceService.getClosePrices(List.of(figi), null))
            .thenReturn(closePrices);
        when(closePriceRepository.existsById(any(ClosePriceKey.class)))
            .thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/main-session-prices/instrument/{figi}", figi))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Цена закрытия уже существует для инструмента " + figi + " за " + LocalDate.parse("2024-01-15")))
            .andExpect(jsonPath("$.figi").value(figi))
            .andExpect(jsonPath("$.priceDate").value("2024-01-15"))
            .andExpect(jsonPath("$.closePrice").value(102.50))
            .andExpect(jsonPath("$.totalRequested").value(1))
            .andExpect(jsonPath("$.newItemsSaved").value(0))
            .andExpect(jsonPath("$.existingItemsSkipped").value(1))
            .andExpect(jsonPath("$.invalidItemsFiltered").value(0))
            .andExpect(jsonPath("$.missingFromApi").value(0))
            .andExpect(jsonPath("$.savedItems").isArray())
            .andExpect(jsonPath("$.timestamp").exists());

        verify(mainSessionPriceService).getClosePrices(List.of(figi), null);
        verify(closePriceRepository).existsById(any(ClosePriceKey.class));
        verify(closePriceRepository, never()).save(any(ClosePriceEntity.class));
    }

    @Test
    @DisplayName("POST /api/main-session-prices/instrument/{figi} - обработка отсутствия данных")
    @Story("Обработка отсутствия данных")
    @Tag("negative")
    @Description("Тест обработки случая, когда данные не найдены")
    @Step("Выполнение POST запроса при отсутствии данных")
    public void loadClosePriceByFigi_ShouldReturnOk_WhenNoData() throws Exception {
        // Arrange
        String figi = "BBG004730N88";

        when(mainSessionPriceService.getClosePrices(List.of(figi), null))
            .thenReturn(Collections.emptyList());

        // Act & Assert
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

        verify(mainSessionPriceService).getClosePrices(List.of(figi), null);
        verify(closePriceRepository, never()).existsById(any(ClosePriceKey.class));
        verify(closePriceRepository, never()).save(any(ClosePriceEntity.class));
    }

    @Test
    @DisplayName("POST /api/main-session-prices/{date} - обработка отсутствия инструментов")
    @Story("Обработка отсутствия данных")
    @Tag("negative")
    @Description("Тест обработки случая, когда инструменты не найдены")
    @Step("Выполнение POST запроса при отсутствии инструментов")
    public void loadMainSessionPricesForDate_ShouldReturnOk_WhenNoInstruments() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        when(shareRepository.findAll()).thenReturn(Collections.emptyList());
        when(futureRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(post("/api/main-session-prices/{date}", testDate))
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

        verify(shareRepository).findAll();
        verify(futureRepository).findAll();
    }
}