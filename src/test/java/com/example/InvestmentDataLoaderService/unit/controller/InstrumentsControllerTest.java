package com.example.InvestmentDataLoaderService.unit.controller;

import com.example.InvestmentDataLoaderService.controller.InstrumentsController;
import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.service.InstrumentService;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit-тесты для InstrumentsController
 */
@WebMvcTest(InstrumentsController.class)
@Epic("Instruments API")
@Feature("Instruments Management")
@DisplayName("Instruments Controller Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
class InstrumentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InstrumentService instrumentService;

    @BeforeEach
    @Step("Подготовка тестовых данных для InstrumentsController")
    void setUp() {
        
        reset(instrumentService);
    }

   
    private List<ShareDto> createMockShares() {
        return Arrays.asList(
            new ShareDto(
                "BBG004730N88", 
                "SBER", 
                "ПАО Сбербанк", 
                "RUB", 
                "MOEX", 
                "Financial", 
                "SECURITY_TRADING_STATUS_NORMAL_TRADING"
            ),
            new ShareDto(
                "BBG004730ZJ9", 
                "GAZP", 
                "ПАО Газпром", 
                "RUB", 
                "MOEX", 
                "Energy", 
                "SECURITY_TRADING_STATUS_NORMAL_TRADING"
            ),
            new ShareDto(
                "BBG004730N88", 
                "LKOH", 
                "ПАО Лукойл", 
                "RUB", 
                "MOEX", 
                "Energy", 
                "SECURITY_TRADING_STATUS_NORMAL_TRADING"
            )
        );
    }

    private ShareDto createMockShare() {
        return new ShareDto(
            "BBG004730N88", 
            "SBER", 
            "ПАО Сбербанк", 
            "RUB", 
            "MOEX", 
            "Financial", 
            "SECURITY_TRADING_STATUS_NORMAL_TRADING"
        );
    }

   
    private List<FutureDto> createMockFutures() {
        return Arrays.asList(
            new FutureDto(
                "FUTSI0624000", 
                "SI0624", 
                "COMMODITY", 
                "Silver", 
                "USD", 
                "MOEX"
            ),
            new FutureDto(
                "FUTGZ0624000", 
                "GZ0624", 
                "COMMODITY", 
                "Gold", 
                "USD", 
                "MOEX"
            )
        );
    }

   
    private FutureDto createMockFuture() {
        return new FutureDto(
            "FUTSI0624000", 
            "SI0624", 
            "COMMODITY", 
            "Silver", 
            "USD", 
            "MOEX"
        );
    }

    private List<IndicativeDto> createMockIndicatives() {
        return Arrays.asList(
            new IndicativeDto(
                "BBG0013HGFT4", 
                "USD000UTSTOM", 
                "Доллар США / Российский рубль", 
                "RUB", 
                "MOEX", 
                "CURRENCY", 
                "USD000UTSTOM", 
                true, 
                true
            ),
            new IndicativeDto(
                "BBG0013HGFT5", 
                "EUR000UTSTOM", 
                "Евро / Российский рубль", 
                "RUB", 
                "MOEX", 
                "CURRENCY", 
                "EUR000UTSTOM", 
                true, 
                true
            )
        );
    }

   
    private IndicativeDto createMockIndicative() {
        return new IndicativeDto(
            "BBG0013HGFT4", 
            "USD000UTSTOM", 
            "Доллар США / Российский рубль", 
            "RUB", 
            "MOEX", 
            "CURRENCY", 
            "USD000UTSTOM", 
            true, 
            true
        );
    }

    
    private SaveResponseDto createMockSaveResponse() {
        return new SaveResponseDto(
            true,
            "Успешно сохранено 5 новых инструментов из 10 найденных",
            10,
            5,
            5,
            0,
            0,
            Arrays.asList("item1", "item2", "item3", "item4", "item5")
        );
    }

    
    private Map<String, Long> createMockInstrumentCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("shares", 150L);
        counts.put("futures", 45L);
        counts.put("indicatives", 12L);
        counts.put("total", 207L);
        return counts;
    }

    // ==================== ТЕСТЫ ДЛЯ АКЦИЙ ====================

    @Test
    @DisplayName("Получение списка акций через API - успешный случай")
    @Description("Тест проверяет корректность получения списка акций через API с применением фильтров")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("unit")
    void getShares_ShouldReturnSharesList_WhenApiSourceIsUsed() throws Exception {
        // Given 
        when(instrumentService.getShares(
            eq("INSTRUMENT_STATUS_ACTIVE"),
            eq("MOEX"),
            eq("RUB"),
            isNull(),
            isNull()
        )).thenReturn(createMockShares());
        
        // When & Then
        mockMvc.perform(get("/api/instruments/shares")
                .param("source", "api")
                .param("exchange", "MOEX")
                .param("currency", "RUB")
                .param("status", "INSTRUMENT_STATUS_ACTIVE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].figi").value("BBG004730N88"))
                .andExpect(jsonPath("$[0].ticker").value("SBER"))
                .andExpect(jsonPath("$[0].name").value("ПАО Сбербанк"))
                .andExpect(jsonPath("$[0].currency").value("RUB"))
                .andExpect(jsonPath("$[0].exchange").value("MOEX"))
                .andExpect(jsonPath("$[0].sector").value("Financial"))
                .andExpect(jsonPath("$[0].tradingStatus").value("SECURITY_TRADING_STATUS_NORMAL_TRADING"))
                .andExpect(jsonPath("$[1].ticker").value("GAZP"))
                .andExpect(jsonPath("$[2].ticker").value("LKOH"));

        // Verify 
        verify(instrumentService).getShares(
            eq("INSTRUMENT_STATUS_ACTIVE"),
            eq("MOEX"),
            eq("RUB"),
            isNull(),
            isNull()
        );
    }

    @Test
    @DisplayName("Получение списка акций через API - без параметров")
    @Description("Тест проверяет поведение API при передаче пустых параметров запроса")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("unit")
    void getShares_ShouldReturnSharesList_WhenNoParametersProvided() throws Exception {
        // Given 
        when(instrumentService.getShares(isNull(), isNull(), isNull(), isNull(), isNull()))
            .thenReturn(createMockShares());

        // When & Then
        mockMvc.perform(get("/api/instruments/shares")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].ticker").value("SBER"))
                .andExpect(jsonPath("$[1].ticker").value("GAZP"))
                .andExpect(jsonPath("$[2].ticker").value("LKOH"));

        // Verify
        verify(instrumentService).getShares(isNull(), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    @DisplayName("Получение списка акций с несуществующим источником данных")
    @Description("Тест проверяет поведение контроллера при передаче несуществующего источника данных")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("negative")
    void getShares_ShouldReturnSharesList_WhenInvalidSourceProvided() throws Exception {
        // Given - настройка мока для некорректного источника (по умолчанию используется API)
        when(instrumentService.getShares(isNull(), isNull(), isNull(), isNull(), isNull()))
            .thenReturn(createMockShares());

        // When & Then
        mockMvc.perform(get("/api/instruments/shares")
                .param("source", "invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].ticker").value("SBER"));

        // Verify
        verify(instrumentService).getShares(isNull(), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    @DisplayName("Получение списка акций через API с параметром в верхнем регистре")
    @Description("Тест проверяет поведение контроллера при передаче параметра API в верхнем регистре")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("case-sensitivity")
    void getShares_ShouldReturnSharesList_WhenApiParameterInUpperCase() throws Exception {
        // Given - настройка мока для API в верхнем регистре
        when(instrumentService.getShares(isNull(), isNull(), isNull(), isNull(), isNull()))
            .thenReturn(createMockShares());

        // When & Then
        mockMvc.perform(get("/api/instruments/shares")
                .param("source", "API")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].ticker").value("SBER"));

        // Verify
        verify(instrumentService).getShares(isNull(), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    @DisplayName("Получение списка акций через API с несуществующим FIGI")
    @Description("Тест проверяет поведение контроллера при передаче несуществующего FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("negative")
    @Tag("figi")
    void getShares_ShouldReturnEmptyList_WhenInvalidFigiProvided() throws Exception {
        // Given - настройка мока для возврата пустого списка при некорректном FIGI
        when(instrumentService.getShares(isNull(), isNull(), isNull(), isNull(), eq("INVALID_FIGI")))
            .thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/instruments/shares")
                .param("figi", "INVALID_FIGI")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        // Verify
        verify(instrumentService).getShares(isNull(), isNull(), isNull(), isNull(), eq("INVALID_FIGI"));
    }

    @Test
    @DisplayName("Получение списка акций через API с FIGI в некорректном формате")
    @Description("Тест проверяет поведение контроллера при передаче FIGI в некорректном формате")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("negative")
    @Tag("validation")
    @Tag("figi")
    void getShares_ShouldHandleInvalidFigiFormat_WhenFigiIsMalformed() throws Exception {
        // Given - настройка мока для возврата пустого списка при некорректном формате FIGI
        when(instrumentService.getShares(isNull(), isNull(), isNull(), isNull(), eq("MALFORMED_FIGI_123!@#")))
            .thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/instruments/shares")
                .param("source", "api")
                .param("figi", "MALFORMED_FIGI_123!@#")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        // Verify
        verify(instrumentService).getShares(isNull(), isNull(), isNull(), isNull(), eq("MALFORMED_FIGI_123!@#"));
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
        // Given - создание очень длинного FIGI (более 1000 символов)
        String veryLongFigi = "BBG004730N88" + "A".repeat(1000);
        when(instrumentService.getShares(isNull(), isNull(), isNull(), isNull(), eq(veryLongFigi)))
            .thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/instruments/shares")
                .param("source", "api")
                .param("figi", veryLongFigi)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        // Verify
        verify(instrumentService).getShares(isNull(), isNull(), isNull(), isNull(), eq(veryLongFigi));
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
        // Given - настройка мока для возврата всех акций при пустом FIGI
        when(instrumentService.getShares(isNull(), isNull(), isNull(), isNull(), eq("")))
            .thenReturn(createMockShares());

        // When & Then
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

        // Verify
        verify(instrumentService).getShares(isNull(), isNull(), isNull(), isNull(), eq(""));
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
        // Given - настройка мока для возврата всех акций при invalid ticker
        when(instrumentService.getShares(isNull(), isNull(), isNull(), eq("INVALID_TICKER"), isNull()))
            .thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/instruments/shares")
                .param("source", "api")
                .param("ticker", "INVALID_TICKER")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        // Verify
        verify(instrumentService).getShares(isNull(), isNull(), isNull(), eq("INVALID_TICKER"), isNull());
    }
    // ==================== ТЕСТЫ ДЛЯ Получение акции из базы данных ====================

    @Test
    @DisplayName("Получение акции из базы данных через database - успешный случай")
    @Description("Тест проверяет корректность получения акции из базы данных через database")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares database")
    @Tag("database")
    @Tag("shares")
    @Tag("unit")
    void getSharesFromDatabase_ShouldReturnSharesList_WhenApiSourceIsUsed() throws Exception {
        // Given - настройка мока для акций
        when(instrumentService.getSharesFromDatabase(eq(new ShareFilterDto(
            null,
            "MOEX",
            null,
            null,
            null,
            null,
            null
        ))))
            .thenReturn(createMockShares());

            mockMvc.perform(get("/api/instruments/shares")
                .param("source", "database")
                .param("exchange", "MOEX"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].figi").value("BBG004730N88"))
                .andExpect(jsonPath("$[0].ticker").value("SBER"))
                .andExpect(jsonPath("$[0].name").value("ПАО Сбербанк"))
                .andExpect(jsonPath("$[0].currency").value("RUB"))
                .andExpect(jsonPath("$[0].exchange").value("MOEX"))
                .andExpect(jsonPath("$[1].ticker").value("GAZP"))
                .andExpect(jsonPath("$[2].ticker").value("LKOH"))
                .andExpect(jsonPath("$[1].exchange").value("MOEX"))
                .andExpect(jsonPath("$[2].exchange").value("MOEX"));

   

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
        // Given - настройка мока для поиска по FIGI
        when(instrumentService.getShareByFigi("BBG004730N88"))
            .thenReturn(createMockShare());

        // When & Then
        mockMvc.perform(get("/api/instruments/shares/BBG004730N88")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.figi").value("BBG004730N88"))
                .andExpect(jsonPath("$.ticker").value("SBER"))
                .andExpect(jsonPath("$.name").value("ПАО Сбербанк"))
                .andExpect(jsonPath("$.currency").value("RUB"))
                .andExpect(jsonPath("$.exchange").value("MOEX"));

        // Verify
        verify(instrumentService).getShareByFigi("BBG004730N88");
        verify(instrumentService, never()).getShareByTicker(any());
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
        // Given - настройка мока для поиска по тикеру (тикер короткий, поэтому сначала поиск по FIGI не выполняется)
        when(instrumentService.getShareByTicker("SBER"))
            .thenReturn(createMockShare());

        // When & Then
        mockMvc.perform(get("/api/instruments/shares/SBER")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.figi").value("BBG004730N88"))
                .andExpect(jsonPath("$.ticker").value("SBER"))
                .andExpect(jsonPath("$.name").value("ПАО Сбербанк"));

        // Verify - тикер короткий, поэтому поиск по FIGI не выполняется
        verify(instrumentService, never()).getShareByFigi(any());
        verify(instrumentService).getShareByTicker("SBER");
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
        // Given - настройка мока для несуществующей акции
        when(instrumentService.getShareByFigi("INVALID_FIGI"))
            .thenReturn(null);
        when(instrumentService.getShareByTicker("INVALID_TICKER"))
            .thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/instruments/shares/INVALID_FIGI")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());


        mockMvc.perform(get("/api/instruments/shares/INVALID_TICKER")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

        // Verify
        verify(instrumentService).getShareByFigi("INVALID_FIGI");
        verify(instrumentService).getShareByTicker("INVALID_TICKER");
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
        // Given - настройка мока для FIGI с подчеркиванием
        when(instrumentService.getShareByFigi("BBG_004730_N88"))
            .thenReturn(createMockShare());

        // When & Then
        mockMvc.perform(get("/api/instruments/shares/BBG_004730_N88")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.figi").value("BBG004730N88"))
                .andExpect(jsonPath("$.ticker").value("SBER"));

        // Verify
        verify(instrumentService).getShareByFigi("BBG_004730_N88");
        verify(instrumentService, never()).getShareByTicker(any());
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
        // Given - настройка мока для сохранения акций
        ShareFilterDto filter = new ShareFilterDto();
        filter.setExchange("MOEX");
        filter.setCurrency("RUB");
        filter.setStatus("INSTRUMENT_STATUS_ACTIVE");
        
        when(instrumentService.saveShares(any(ShareFilterDto.class)))
            .thenReturn(createMockSaveResponse());

        // When & Then
        mockMvc.perform(post("/api/instruments/shares")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"exchange\":\"MOEX\",\"currency\":\"RUB\",\"status\":\"INSTRUMENT_STATUS_ACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Успешно сохранено 5 новых инструментов из 10 найденных"))
                .andExpect(jsonPath("$.totalRequested").value(10))
                .andExpect(jsonPath("$.newItemsSaved").value(5))
                .andExpect(jsonPath("$.existingItemsSkipped").value(5));

        // Verify
        verify(instrumentService).saveShares(any(ShareFilterDto.class));
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
        // Given - настройка мока для пустого фильтра
        when(instrumentService.saveShares(any(ShareFilterDto.class)))
            .thenReturn(createMockSaveResponse());

        // When & Then
        mockMvc.perform(post("/api/instruments/shares")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));

        // Verify
        verify(instrumentService).saveShares(any(ShareFilterDto.class));
    }

    // ==================== ТЕСТЫ ДЛЯ ФЬЮЧЕРСОВ ====================

    @Test
    @DisplayName("Получение списка фьючерсов через API - успешный случай")
    @Description("Тест проверяет корректность получения списка фьючерсов через API")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures API")
    @Tag("api")
    @Tag("futures")
    @Tag("unit")
    void getFutures_ShouldReturnFuturesList_WhenApiSourceIsUsed() throws Exception {
        
        when(instrumentService.getFutures(
            eq("INSTRUMENT_STATUS_ACTIVE"),
            eq("MOEX"),
            eq("USD"),
            isNull(),
            eq("COMMODITY")
        )).thenReturn(createMockFutures());

        // When & Then
        mockMvc.perform(get("/api/instruments/futures")
                .param("status", "INSTRUMENT_STATUS_ACTIVE")
                .param("exchange", "MOEX")
                .param("currency", "USD")
                .param("assetType", "COMMODITY")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].figi").value("FUTSI0624000"))
                .andExpect(jsonPath("$[0].ticker").value("SI0624"))
                .andExpect(jsonPath("$[0].assetType").value("COMMODITY"))
                .andExpect(jsonPath("$[1].ticker").value("GZ0624"));

        // Verify
        verify(instrumentService).getFutures(
            eq("INSTRUMENT_STATUS_ACTIVE"),
            eq("MOEX"),
            eq("USD"),
            isNull(),
            eq("COMMODITY")
        );
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
        // Given - настройка мока для поиска фьючерса по FIGI
        when(instrumentService.getFutureByFigi("FUTSI0624000"))
            .thenReturn(createMockFuture());

        // When & Then
        mockMvc.perform(get("/api/instruments/futures/FUTSI0624000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.figi").value("FUTSI0624000"))
                .andExpect(jsonPath("$.ticker").value("SI0624"))
                .andExpect(jsonPath("$.assetType").value("COMMODITY"))
                .andExpect(jsonPath("$.basicAsset").value("Silver"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.exchange").value("MOEX"));

        // Verify
        verify(instrumentService).getFutureByFigi("FUTSI0624000");
        verify(instrumentService, never()).getFutureByTicker(any());
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
        // Given - настройка мока для поиска фьючерса по тикеру (тикер короткий, поэтому сначала поиск по FIGI не выполняется)
        when(instrumentService.getFutureByTicker("SI0624"))
            .thenReturn(createMockFuture());

        // When & Then
        mockMvc.perform(get("/api/instruments/futures/SI0624")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.figi").value("FUTSI0624000"))
                .andExpect(jsonPath("$.ticker").value("SI0624"))
                .andExpect(jsonPath("$.assetType").value("COMMODITY"));

        // Verify - тикер короткий, поэтому поиск по FIGI не выполняется
        verify(instrumentService, never()).getFutureByFigi(any());
        verify(instrumentService).getFutureByTicker("SI0624");
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
        // Given - настройка мока для несуществующего фьючерса
        when(instrumentService.getFutureByFigi("INVALID_FUTURE"))
            .thenReturn(null);
        when(instrumentService.getFutureByTicker("INVALID_FUTURE"))
            .thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/instruments/futures/INVALID_FUTURE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Verify
        verify(instrumentService).getFutureByFigi("INVALID_FUTURE");
        verify(instrumentService).getFutureByTicker("INVALID_FUTURE");
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
        // Given - настройка мока для сохранения фьючерсов
        when(instrumentService.saveFutures(any(FutureFilterDto.class)))
            .thenReturn(createMockSaveResponse());

        // When & Then
        mockMvc.perform(post("/api/instruments/futures")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"exchange\":\"MOEX\",\"currency\":\"USD\",\"assetType\":\"COMMODITY\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Успешно сохранено 5 новых инструментов из 10 найденных"))
                .andExpect(jsonPath("$.totalRequested").value(10))
                .andExpect(jsonPath("$.newItemsSaved").value(5));

        // Verify
        verify(instrumentService).saveFutures(any(FutureFilterDto.class));
    }

    // ==================== ТЕСТЫ ДЛЯ ИНДИКАТИВОВ ====================

    @Test
    @DisplayName("Получение списка индикативов через API - успешный случай")
    @Description("Тест проверяет корректность получения списка индикативов через API")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("unit")
    void getIndicatives_ShouldReturnIndicativesList_WhenApiSourceIsUsed() throws Exception {
        // Given - настройка мока для индикативов
        when(instrumentService.getIndicatives(
            eq("MOEX"),
            eq("RUB"),
            isNull(),
            isNull()
        )).thenReturn(createMockIndicatives());

        // When & Then
        mockMvc.perform(get("/api/instruments/indicatives")
                .param("exchange", "MOEX")
                .param("currency", "RUB")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].figi").value("BBG0013HGFT4"))
                .andExpect(jsonPath("$[0].ticker").value("USD000UTSTOM"))
                .andExpect(jsonPath("$[1].ticker").value("EUR000UTSTOM"));

        // Verify
        verify(instrumentService).getIndicatives(
            eq("MOEX"),
            eq("RUB"),
            isNull(),
            isNull()
        );
    }

    @Test
    @DisplayName("Получение индикатива по FIGI - успешный случай")
    @Description("Тест проверяет корректность получения индикатива по FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("identifier")
    @Tag("figi")
    void getIndicativeByIdentifier_ShouldReturnIndicative_WhenValidFigiProvided() throws Exception {
        // Given - настройка мока для поиска индикатива по FIGI
        when(instrumentService.getIndicativeBy("BBG0013HGFT4"))
            .thenReturn(createMockIndicative());

        // When & Then
        mockMvc.perform(get("/api/instruments/indicatives/BBG0013HGFT4")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.figi").value("BBG0013HGFT4"))
                .andExpect(jsonPath("$.ticker").value("USD000UTSTOM"))
                .andExpect(jsonPath("$.name").value("Доллар США / Российский рубль"))
                .andExpect(jsonPath("$.currency").value("RUB"))
                .andExpect(jsonPath("$.exchange").value("MOEX"))
                .andExpect(jsonPath("$.classCode").value("CURRENCY"))
                .andExpect(jsonPath("$.sellAvailableFlag").value(true))
                .andExpect(jsonPath("$.buyAvailableFlag").value(true));

        // Verify
        verify(instrumentService).getIndicativeBy("BBG0013HGFT4");
        verify(instrumentService, never()).getIndicativeByTicker(any());
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
        // Given - настройка мока для поиска индикатива по тикеру
        when(instrumentService.getIndicativeBy("USD000UTSTOM"))
            .thenReturn(null);
        when(instrumentService.getIndicativeByTicker("USD000UTSTOM"))
            .thenReturn(createMockIndicative());

        // When & Then
        mockMvc.perform(get("/api/instruments/indicatives/USD000UTSTOM")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.figi").value("BBG0013HGFT4"))
                .andExpect(jsonPath("$.ticker").value("USD000UTSTOM"))
                .andExpect(jsonPath("$.name").value("Доллар США / Российский рубль"));

        // Verify
        verify(instrumentService).getIndicativeBy("USD000UTSTOM");
        verify(instrumentService).getIndicativeByTicker("USD000UTSTOM");
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
        // Given - настройка мока для несуществующего индикатива
        when(instrumentService.getIndicativeBy("INVALID_INDICATIVE"))
            .thenReturn(null);
        when(instrumentService.getIndicativeByTicker("INVALID_INDICATIVE"))
            .thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/instruments/indicatives/INVALID_INDICATIVE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Verify
        verify(instrumentService).getIndicativeBy("INVALID_INDICATIVE");
        verify(instrumentService).getIndicativeByTicker("INVALID_INDICATIVE");
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
        // Given - настройка мока для сохранения индикативов
        when(instrumentService.saveIndicatives(any(IndicativeFilterDto.class)))
            .thenReturn(createMockSaveResponse());

        // When & Then
        mockMvc.perform(post("/api/instruments/indicatives")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"exchange\":\"MOEX\",\"currency\":\"RUB\",\"ticker\":\"USD000UTSTOM\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Успешно сохранено 5 новых инструментов из 10 найденных"))
                .andExpect(jsonPath("$.totalRequested").value(10))
                .andExpect(jsonPath("$.newItemsSaved").value(5));

        // Verify
        verify(instrumentService).saveIndicatives(any(IndicativeFilterDto.class));
    }

    // ==================== ТЕСТЫ ДЛЯ СТАТИСТИКИ ====================

    @Test
    @DisplayName("Получение статистики инструментов")
    @Description("Тест проверяет корректность получения статистики по количеству инструментов")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Statistics API")
    @Tag("api")
    @Tag("statistics")
    @Tag("unit")
    void getInstrumentCounts_ShouldReturnStatistics_WhenRequested() throws Exception {
        // Given - настройка мока для статистики
        when(instrumentService.getInstrumentCounts())
            .thenReturn(createMockInstrumentCounts());

        // When & Then
        mockMvc.perform(get("/api/instruments/count")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.shares").value(150))
                .andExpect(jsonPath("$.futures").value(45))
                .andExpect(jsonPath("$.indicatives").value(12))
                .andExpect(jsonPath("$.total").value(207));

        // Verify
        verify(instrumentService).getInstrumentCounts();
    }

    // ==================== ДОПОЛНИТЕЛЬНЫЕ НЕГАТИВНЫЕ ТЕСТЫ ====================

    @Test
    @DisplayName("Получение списка акций с некорректным статусом")
    @Description("Тест проверяет поведение контроллера при передаче некорректного статуса")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("negative")
    @Tag("validation")
    @Tag("status")
    void getShares_ShouldHandleInvalidStatus_WhenInvalidStatusProvided() throws Exception {
        // Given - настройка мока для некорректного статуса
        when(instrumentService.getShares(
            eq("INVALID_STATUS"),
            isNull(),
            isNull(),
            isNull(),
            isNull()
        )).thenReturn(createMockShares());

        // When & Then
        mockMvc.perform(get("/api/instruments/shares")
                .param("source", "api")
                .param("status", "INVALID_STATUS")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));

        // Verify
        verify(instrumentService).getShares(
            eq("INVALID_STATUS"),
            isNull(),
            isNull(),
            isNull(),
            isNull()
        );
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
        // Given - настройка мока для некорректной биржи
        when(instrumentService.getShares(
            isNull(),
            eq("INVALID_EXCHANGE"),
            isNull(),
            isNull(),
            isNull()
        )).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/instruments/shares")
                .param("source", "api")
                .param("exchange", "INVALID_EXCHANGE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        // Verify
        verify(instrumentService).getShares(
            isNull(),
            eq("INVALID_EXCHANGE"),
            isNull(),
            isNull(),
            isNull()
        );
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
        // Given - настройка мока для некорректной валюты
        when(instrumentService.getShares(
            isNull(),
            isNull(),
            eq("INVALID_CURRENCY"),
            isNull(),
            isNull()
        )).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/instruments/shares")
                .param("source", "api")
                .param("currency", "INVALID_CURRENCY")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        // Verify
        verify(instrumentService).getShares(
            isNull(),
            isNull(),
            eq("INVALID_CURRENCY"),
            isNull(),
            isNull()
        );
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
        // Given - настройка мока для БД с параметрами
        when(instrumentService.getSharesFromDatabase(any(ShareFilterDto.class)))
            .thenReturn(createMockShares());

        // When & Then
        mockMvc.perform(get("/api/instruments/shares")
                .param("source", "database")
                .param("exchange", "MOEX")
                .param("currency", "RUB")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].ticker").value("SBER"))
                .andExpect(jsonPath("$[1].ticker").value("GAZP"))
                .andExpect(jsonPath("$[2].ticker").value("LKOH"));

        // Verify
        verify(instrumentService).getSharesFromDatabase(any(ShareFilterDto.class));
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
        // Given - настройка мока для фьючерсов без параметров
        when(instrumentService.getFutures(isNull(), isNull(), isNull(), isNull(), isNull()))
            .thenReturn(createMockFutures());

        // When & Then
        mockMvc.perform(get("/api/instruments/futures")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ticker").value("SI0624"))
                .andExpect(jsonPath("$[1].ticker").value("GZ0624"));

        // Verify
        verify(instrumentService).getFutures(isNull(), isNull(), isNull(), isNull(), isNull());
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
        // Given - настройка мока для индикативов без параметров
        when(instrumentService.getIndicatives(isNull(), isNull(), isNull(), isNull()))
            .thenReturn(createMockIndicatives());

        // When & Then
        mockMvc.perform(get("/api/instruments/indicatives")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ticker").value("USD000UTSTOM"))
                .andExpect(jsonPath("$[1].ticker").value("EUR000UTSTOM"));

        // Verify
        verify(instrumentService).getIndicatives(isNull(), isNull(), isNull(), isNull());
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
        // Given - настройка мока для FIGI с дефисом
        when(instrumentService.getShareByFigi("BBG-004730-N88"))
            .thenReturn(createMockShare());

        // When & Then
        mockMvc.perform(get("/api/instruments/shares/BBG-004730-N88")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.figi").value("BBG004730N88"))
                .andExpect(jsonPath("$.ticker").value("SBER"));

        // Verify
        verify(instrumentService).getShareByFigi("BBG-004730-N88");
        verify(instrumentService, never()).getShareByTicker(any());
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
        // Given - настройка мока для FIGI фьючерса с дефисом
        when(instrumentService.getFutureByFigi("FUT-SI0624-000"))
            .thenReturn(createMockFuture());

        // When & Then
        mockMvc.perform(get("/api/instruments/futures/FUT-SI0624-000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.figi").value("FUTSI0624000"))
                .andExpect(jsonPath("$.ticker").value("SI0624"))
                .andExpect(jsonPath("$.assetType").value("COMMODITY"));

        // Verify
        verify(instrumentService).getFutureByFigi("FUT-SI0624-000");
        verify(instrumentService, never()).getFutureByTicker(any());
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
        // Given - настройка мока для FIGI индикатива с дефисом
        when(instrumentService.getIndicativeBy("BBG-0013HG-FT4"))
            .thenReturn(createMockIndicative());

        // When & Then
        mockMvc.perform(get("/api/instruments/indicatives/BBG-0013HG-FT4")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.figi").value("BBG0013HGFT4"))
                .andExpect(jsonPath("$.ticker").value("USD000UTSTOM"))
                .andExpect(jsonPath("$.name").value("Доллар США / Российский рубль"));

        // Verify
        verify(instrumentService).getIndicativeBy("BBG-0013HG-FT4");
        verify(instrumentService, never()).getIndicativeByTicker(any());
    }
}