package com.example.InvestmentDataLoaderService.unit.controller;

import com.example.InvestmentDataLoaderService.controller.InstrumentsController;
import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.service.TInvestService;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.IndicativeRepository;
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

import java.util.Arrays;
import java.util.List;

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
    private TInvestService tInvestService;

    @MockBean
    private ShareRepository shareRepository;

    @MockBean
    private FutureRepository futureRepository;

    @MockBean
    private IndicativeRepository indicativeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ShareDto testShare;
    private FutureDto testFuture;
    private IndicativeDto testIndicative;
    private SaveResponseDto testSaveResponse;

    @BeforeEach
    @Step("Подготовка тестовых данных для инструментов")
    @DisplayName("Подготовка тестовых данных")
    @Description("Инициализация тестовых данных для тестов инструментов")
    @Tag("setup")
    void setUp() {
        testShare = new ShareDto(
            "BBG004730N88",
            "SBER",
            "Сбербанк",
            "RUB",
            "moex_mrng_evng_e_wknd_dlr",
            "Financials",
            "SECURITY_TRADING_STATUS_NORMAL_TRADING"
        );

        testFuture = new FutureDto(
            "FUTSBER0324",
            "SBER-3.24",
            "FUTURES",
            "SBER",
            "RUB",
            "moex_mrng_evng_e_wknd_dlr"
        );

        testIndicative = new IndicativeDto(
            "BBG004730ZJ9",
            "RTSI",
            "Индекс РТС",
            "RUB",
            "moex_mrng_evng_e_wknd_dlr",
            "SPBXM",
            "test-uid",
            true,
            true
        );

        testSaveResponse = new SaveResponseDto(
            true,
            "Успешно загружено 1 новых акций",
            1,
            1,
            0,
            0, // invalidItemsFiltered
            0, // missingFromApi
            Arrays.asList(testShare)
        );
    }

    // ==================== ТЕСТЫ ДЛЯ АКЦИЙ ====================

    @Test
    @DisplayName("Получение акций - валидные параметры")
    @Description("Тест проверяет корректность получения акций с валидными параметрами фильтрации")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares Management")
    @Tag("api")
    @Tag("instruments")
    @Tag("shares")
    @Tag("get")
    @Tag("valid-parameters")
    void getShares_ShouldReturnShares_WhenValidParameters() throws Exception {
        // Given
        List<ShareDto> expectedShares = Arrays.asList(testShare);
        when(tInvestService.getShares(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(expectedShares);

        // When & Then
        mockMvc.perform(get("/api/instruments/shares")
                .param("status", "INSTRUMENT_STATUS_BASE")
                .param("exchange", "moex_mrng_evng_e_wknd_dlr")
                .param("currency", "RUB")
                .param("ticker", "SBER")
                .param("figi", "BBG004730N88"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].figi").value("BBG004730N88"))
            .andExpect(jsonPath("$[0].ticker").value("SBER"))
            .andExpect(jsonPath("$[0].name").value("Сбербанк"))
            .andExpect(jsonPath("$[0].currency").value("RUB"))
            .andExpect(jsonPath("$[0].exchange").value("moex_mrng_evng_e_wknd_dlr"))
            .andExpect(jsonPath("$[0].sector").value("Financials"))
            .andExpect(jsonPath("$[0].tradingStatus").value("SECURITY_TRADING_STATUS_NORMAL_TRADING"));

        verify(tInvestService).getShares("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", 
            "RUB", "SBER", "BBG004730N88");
    }

    @Test
    @DisplayName("Получение акций - без параметров")
    @Description("Тест проверяет корректность получения всех акций без параметров фильтрации")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares Management")
    @Tag("api")
    @Tag("instruments")
    @Tag("shares")
    @Tag("get")
    @Tag("no-parameters")
    void getShares_ShouldReturnShares_WhenNoParameters() throws Exception {
        // Given
        List<ShareDto> expectedShares = Arrays.asList(testShare);
        when(tInvestService.getShares(isNull(), isNull(), isNull(), isNull(), isNull()))
            .thenReturn(expectedShares);

        // When & Then
        mockMvc.perform(get("/api/instruments/shares"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].figi").value("BBG004730N88"));

        verify(tInvestService).getShares(null, null, null, null, null);
    }

    @Test
    @DisplayName("Получение акции по идентификатору - найден по FIGI")
    @Description("Тест проверяет корректность получения акции по FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares Management")
    @Tag("api")
    @Tag("instruments")
    @Tag("shares")
    @Tag("get-by-id")
    @Tag("figi")
    @Tag("success")
    void getShareByIdentifier_ShouldReturnShare_WhenFoundByFigi() throws Exception {
        // Given
        when(tInvestService.getShareByFigi("BBG004730N88")).thenReturn(testShare);
        when(tInvestService.getShareByTicker(anyString())).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/instruments/shares/BBG004730N88"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.figi").value("BBG004730N88"))
            .andExpect(jsonPath("$.ticker").value("SBER"));

        verify(tInvestService).getShareByFigi("BBG004730N88");
        verify(tInvestService, never()).getShareByTicker(anyString());
    }

    @Test
    @DisplayName("Получение акции по идентификатору - найден по тикеру")
    @Description("Тест проверяет корректность получения акции по тикеру")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares Management")
    @Tag("api")
    @Tag("instruments")
    @Tag("shares")
    @Tag("get-by-id")
    @Tag("ticker")
    @Tag("success")
    void getShareByIdentifier_ShouldReturnShare_WhenFoundByTicker() throws Exception {
        // Given
        when(tInvestService.getShareByTicker("SBER")).thenReturn(testShare);

        // When & Then
        mockMvc.perform(get("/api/instruments/shares/SBER"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.figi").value("BBG004730N88"))
            .andExpect(jsonPath("$.ticker").value("SBER"));

        verify(tInvestService, never()).getShareByFigi(anyString());
        verify(tInvestService).getShareByTicker("SBER");
    }

    @Test
    @DisplayName("Получение акции по идентификатору - не найдена")
    @Description("Тест проверяет корректность обработки случая когда акция не найдена")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares Management")
    @Tag("api")
    @Tag("instruments")
    @Tag("shares")
    @Tag("get-by-id")
    @Tag("not-found")
    void getShareByIdentifier_ShouldReturnNotFound_WhenShareNotFound() throws Exception {
        // Given
        when(tInvestService.getShareByTicker("UNKNOWN")).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/instruments/shares/UNKNOWN"))
            .andExpect(status().isNotFound());

        verify(tInvestService, never()).getShareByFigi(anyString());
        verify(tInvestService).getShareByTicker("UNKNOWN");
    }

    @Test
    @DisplayName("Сохранение акций - валидный фильтр")
    @Description("Тест проверяет корректность сохранения акций с валидным фильтром")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares Management")
    @Tag("api")
    @Tag("instruments")
    @Tag("shares")
    @Tag("save")
    @Tag("valid-filter")
    void saveShares_ShouldReturnSaveResponse_WhenValidFilter() throws Exception {
        // Given
        ShareFilterDto filter = new ShareFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_BASE");
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("SBER");

        when(tInvestService.saveShares(any(ShareFilterDto.class))).thenReturn(testSaveResponse);

        // When & Then
        mockMvc.perform(post("/api/instruments/shares")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Успешно загружено 1 новых акций"))
            .andExpect(jsonPath("$.totalRequested").value(1))
            .andExpect(jsonPath("$.newItemsSaved").value(1))
            .andExpect(jsonPath("$.existingItemsSkipped").value(0))
            .andExpect(jsonPath("$.savedItems").isArray())
            .andExpect(jsonPath("$.savedItems[0].figi").value("BBG004730N88"));

        verify(tInvestService).saveShares(any(ShareFilterDto.class));
    }

    // ==================== ТЕСТЫ ДЛЯ ФЬЮЧЕРСОВ ====================

    @Test
    @DisplayName("Получение фьючерсов - валидные параметры")
    @Description("Тест проверяет корректность получения фьючерсов с валидными параметрами фильтрации")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures Management")
    @Tag("api")
    @Tag("instruments")
    @Tag("futures")
    @Tag("get")
    @Tag("valid-parameters")
    void getFutures_ShouldReturnFutures_WhenValidParameters() throws Exception {
        // Given
        List<FutureDto> expectedFutures = Arrays.asList(testFuture);
        when(tInvestService.getFutures(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(expectedFutures);

        // When & Then
        mockMvc.perform(get("/api/instruments/futures")
                .param("status", "INSTRUMENT_STATUS_BASE")
                .param("exchange", "moex_mrng_evng_e_wknd_dlr")
                .param("currency", "RUB")
                .param("ticker", "SBER-3.24")
                .param("assetType", "FUTURES"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].figi").value("FUTSBER0324"))
            .andExpect(jsonPath("$[0].ticker").value("SBER-3.24"))
            .andExpect(jsonPath("$[0].assetType").value("FUTURES"))
            .andExpect(jsonPath("$[0].basicAsset").value("SBER"))
            .andExpect(jsonPath("$[0].currency").value("RUB"))
            .andExpect(jsonPath("$[0].exchange").value("moex_mrng_evng_e_wknd_dlr"));

        verify(tInvestService).getFutures("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", 
            "RUB", "SBER-3.24", "FUTURES");
    }

    @Test
    @DisplayName("Получение фьючерса по идентификатору - найден по FIGI")
    @Description("Тест проверяет корректность получения фьючерса по FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures Management")
    @Tag("api")
    @Tag("instruments")
    @Tag("futures")
    @Tag("get-by-id")
    @Tag("figi")
    @Tag("success")
    void getFutureByIdentifier_ShouldReturnFuture_WhenFoundByFigi() throws Exception {
        // Given
        when(tInvestService.getFutureByFigi("FUTSBER0324")).thenReturn(testFuture);
        when(tInvestService.getFutureByTicker(anyString())).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/instruments/futures/FUTSBER0324"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.figi").value("FUTSBER0324"))
            .andExpect(jsonPath("$.ticker").value("SBER-3.24"));

        verify(tInvestService).getFutureByFigi("FUTSBER0324");
        verify(tInvestService, never()).getFutureByTicker(anyString());
    }

    @Test
    @DisplayName("Получение фьючерса по идентификатору - найден по тикеру с дефисом")
    @Description("Тест проверяет корректность получения фьючерса по тикеру с дефисом")
    @Severity(SeverityLevel.NORMAL)
    @Story("Futures Management")
    @Tag("api")
    @Tag("instruments")
    @Tag("futures")
    @Tag("get-by-id")
    @Tag("ticker-with-dash")
    @Tag("success")
    void getFutureByIdentifier_ShouldReturnFuture_WhenFoundByTickerWithDash() throws Exception {
        // Given
        when(tInvestService.getFutureByFigi("SBER-3.24")).thenReturn(testFuture);

        // When & Then
        mockMvc.perform(get("/api/instruments/futures/SBER-3.24"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.figi").value("FUTSBER0324"))
            .andExpect(jsonPath("$.ticker").value("SBER-3.24"));

        verify(tInvestService).getFutureByFigi("SBER-3.24");
        verify(tInvestService, never()).getFutureByTicker(anyString());
    }

    @Test
    @DisplayName("Получение фьючерса по идентификатору - найден по тикеру")
    @Description("Тест проверяет корректность получения фьючерса по тикеру")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures Management")
    @Tag("api")
    @Tag("instruments")
    @Tag("futures")
    @Tag("get-by-id")
    @Tag("ticker")
    @Tag("success")
    void getFutureByIdentifier_ShouldReturnFuture_WhenFoundByTicker() throws Exception {
        // Given
        when(tInvestService.getFutureByTicker("SBER0324")).thenReturn(testFuture);

        // When & Then
        mockMvc.perform(get("/api/instruments/futures/SBER0324"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.figi").value("FUTSBER0324"))
            .andExpect(jsonPath("$.ticker").value("SBER-3.24"));

        verify(tInvestService, never()).getFutureByFigi(anyString());
        verify(tInvestService).getFutureByTicker("SBER0324");
    }

    @Test
    @DisplayName("Получение фьючерса по идентификатору - не найден")
    @Description("Тест проверяет корректность обработки случая когда фьючерс не найден")
    @Severity(SeverityLevel.NORMAL)
    @Story("Futures Management")
    @Tag("api")
    @Tag("instruments")
    @Tag("futures")
    @Tag("get-by-id")
    @Tag("not-found")
    void getFutureByIdentifier_ShouldReturnNotFound_WhenFutureNotFound() throws Exception {
        // Given
        when(tInvestService.getFutureByTicker("UNKNOWN")).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/instruments/futures/UNKNOWN"))
            .andExpect(status().isNotFound());

        verify(tInvestService, never()).getFutureByFigi(anyString());
        verify(tInvestService).getFutureByTicker("UNKNOWN");
    }

    @Test
    @DisplayName("Сохранение фьючерсов - валидный фильтр")
    @Description("Тест проверяет корректность сохранения фьючерсов с валидным фильтром")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures Management")
    @Tag("api")
    @Tag("instruments")
    @Tag("futures")
    @Tag("save")
    @Tag("valid-filter")
    void saveFutures_ShouldReturnSaveResponse_WhenValidFilter() throws Exception {
        // Given
        FutureFilterDto filter = new FutureFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_BASE");
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("SBER-3.24");
        filter.setAssetType("FUTURES");

        SaveResponseDto futureSaveResponse = new SaveResponseDto(
            true,
            "Успешно загружено 1 новых фьючерсов",
            1,
            1,
            0,
            0, // invalidItemsFiltered
            0, // missingFromApi
            Arrays.asList(testFuture)
        );

        when(tInvestService.saveFutures(any(FutureFilterDto.class))).thenReturn(futureSaveResponse);

        // When & Then
        mockMvc.perform(post("/api/instruments/futures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Успешно загружено 1 новых фьючерсов"))
            .andExpect(jsonPath("$.totalRequested").value(1))
            .andExpect(jsonPath("$.newItemsSaved").value(1))
            .andExpect(jsonPath("$.existingItemsSkipped").value(0))
            .andExpect(jsonPath("$.savedItems").isArray())
            .andExpect(jsonPath("$.savedItems[0].figi").value("FUTSBER0324"));

        verify(tInvestService).saveFutures(any(FutureFilterDto.class));
    }

    // ==================== ТЕСТЫ ДЛЯ ИНДИКАТИВОВ ====================

    @Test
    @DisplayName("Получение индикативов - валидные параметры")
    @Description("Тест проверяет корректность получения индикативов с валидными параметрами фильтрации")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives Management")
    @Tag("api")
    @Tag("instruments")
    @Tag("indicatives")
    @Tag("get")
    @Tag("valid-parameters")
    void getIndicatives_ShouldReturnIndicatives_WhenValidParameters() throws Exception {
        // Given
        List<IndicativeDto> expectedIndicatives = Arrays.asList(testIndicative);
        when(tInvestService.getIndicatives(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(expectedIndicatives);

        // When & Then
        mockMvc.perform(get("/api/instruments/indicatives")
                .param("exchange", "moex_mrng_evng_e_wknd_dlr")
                .param("currency", "RUB")
                .param("ticker", "RTSI")
                .param("figi", "BBG004730ZJ9"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].figi").value("BBG004730ZJ9"))
            .andExpect(jsonPath("$[0].ticker").value("RTSI"))
            .andExpect(jsonPath("$[0].name").value("Индекс РТС"))
            .andExpect(jsonPath("$[0].currency").value("RUB"))
            .andExpect(jsonPath("$[0].exchange").value("moex_mrng_evng_e_wknd_dlr"))
            .andExpect(jsonPath("$[0].classCode").value("SPBXM"))
            .andExpect(jsonPath("$[0].uid").value("test-uid"))
            .andExpect(jsonPath("$[0].sellAvailableFlag").value(true))
            .andExpect(jsonPath("$[0].buyAvailableFlag").value(true));

        verify(tInvestService).getIndicatives("moex_mrng_evng_e_wknd_dlr", "RUB", "RTSI", "BBG004730ZJ9");
    }

    @Test
    @DisplayName("Получение индикатива по идентификатору - найден по FIGI")
    @Description("Тест проверяет корректность получения индикатива по FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives Management")
    @Tag("api")
    @Tag("instruments")
    @Tag("indicatives")
    @Tag("get-by-id")
    @Tag("figi")
    @Tag("success")
    void getIndicativeByIdentifier_ShouldReturnIndicative_WhenFoundByFigi() throws Exception {
        // Given
        when(tInvestService.getIndicativeBy("BBG004730ZJ9")).thenReturn(testIndicative);
        when(tInvestService.getIndicativeByTicker(anyString())).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/instruments/indicatives/BBG004730ZJ9"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.figi").value("BBG004730ZJ9"))
            .andExpect(jsonPath("$.ticker").value("RTSI"));

        verify(tInvestService).getIndicativeBy("BBG004730ZJ9");
        verify(tInvestService, never()).getIndicativeByTicker(anyString());
    }

    @Test
    @DisplayName("Получение индикатива по идентификатору - найден по тикеру")
    @Description("Тест проверяет корректность получения индикатива по тикеру")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives Management")
    @Tag("api")
    @Tag("instruments")
    @Tag("indicatives")
    @Tag("get-by-id")
    @Tag("ticker")
    @Tag("success")
    void getIndicativeByIdentifier_ShouldReturnIndicative_WhenFoundByTicker() throws Exception {
        // Given
        when(tInvestService.getIndicativeByTicker("RTSI")).thenReturn(testIndicative);

        // When & Then
        mockMvc.perform(get("/api/instruments/indicatives/RTSI"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.figi").value("BBG004730ZJ9"))
            .andExpect(jsonPath("$.ticker").value("RTSI"));

        verify(tInvestService, never()).getIndicativeBy(anyString());
        verify(tInvestService).getIndicativeByTicker("RTSI");
    }

    @Test
    @DisplayName("Получение индикатива по идентификатору - не найден")
    @Description("Тест проверяет корректность обработки случая когда индикатив не найден")
    @Severity(SeverityLevel.NORMAL)
    @Story("Indicatives Management")
    @Tag("api")
    @Tag("instruments")
    @Tag("indicatives")
    @Tag("get-by-id")
    @Tag("not-found")
    void getIndicativeByIdentifier_ShouldReturnNotFound_WhenIndicativeNotFound() throws Exception {
        // Given
        when(tInvestService.getIndicativeByTicker("UNKNOWN")).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/instruments/indicatives/UNKNOWN"))
            .andExpect(status().isNotFound());

        verify(tInvestService, never()).getIndicativeBy(anyString());
        verify(tInvestService).getIndicativeByTicker("UNKNOWN");
    }

    @Test
    @DisplayName("Сохранение индикативов - валидный фильтр")
    @Description("Тест проверяет корректность сохранения индикативов с валидным фильтром")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives Management")
    @Tag("api")
    @Tag("instruments")
    @Tag("indicatives")
    @Tag("save")
    @Tag("valid-filter")
    void saveIndicatives_ShouldReturnSaveResponse_WhenValidFilter() throws Exception {
        // Given
        IndicativeFilterDto filter = new IndicativeFilterDto();
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("RTSI");
        filter.setFigi("BBG004730ZJ9");

        SaveResponseDto indicativeSaveResponse = new SaveResponseDto(
            true,
            "Успешно загружено 1 новых индикативных инструментов",
            1,
            1,
            0,
            0, // invalidItemsFiltered
            0, // missingFromApi
            Arrays.asList(testIndicative)
        );

        when(tInvestService.saveIndicatives(any(IndicativeFilterDto.class))).thenReturn(indicativeSaveResponse);

        // When & Then
        mockMvc.perform(post("/api/instruments/indicatives")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Успешно загружено 1 новых индикативных инструментов"))
            .andExpect(jsonPath("$.totalRequested").value(1))
            .andExpect(jsonPath("$.newItemsSaved").value(1))
            .andExpect(jsonPath("$.existingItemsSkipped").value(0))
            .andExpect(jsonPath("$.savedItems").isArray())
            .andExpect(jsonPath("$.savedItems[0].figi").value("BBG004730ZJ9"));

        verify(tInvestService).saveIndicatives(any(IndicativeFilterDto.class));
    }

    // ==================== ТЕСТЫ ДЛЯ СТАТИСТИКИ ====================

    @Test
    @DisplayName("Получение статистики инструментов - с данными")
    @Description("Тест проверяет корректность получения статистики количества инструментов")
    @Severity(SeverityLevel.NORMAL)
    @Story("Instruments Statistics")
    @Tag("api")
    @Tag("instruments")
    @Tag("statistics")
    @Tag("count")
    @Tag("with-data")
    void getInstrumentsCount_ShouldReturnCounts_WhenRepositoriesHaveData() throws Exception {
        // Given
        when(shareRepository.count()).thenReturn(100L);
        when(futureRepository.count()).thenReturn(50L);
        when(indicativeRepository.count()).thenReturn(25L);

        // When & Then
        mockMvc.perform(get("/api/instruments/count"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.shares").value(100))
            .andExpect(jsonPath("$.futures").value(50))
            .andExpect(jsonPath("$.indicatives").value(25))
            .andExpect(jsonPath("$.total").value(175));

        verify(shareRepository).count();
        verify(futureRepository).count();
        verify(indicativeRepository).count();
    }

    @Test
    @DisplayName("Получение статистики инструментов - пустые репозитории")
    @Description("Тест проверяет корректность получения статистики когда репозитории пусты")
    @Severity(SeverityLevel.NORMAL)
    @Story("Instruments Statistics")
    @Tag("api")
    @Tag("instruments")
    @Tag("statistics")
    @Tag("count")
    @Tag("empty-repositories")
    void getInstrumentsCount_ShouldReturnZeroCounts_WhenRepositoriesEmpty() throws Exception {
        // Given
        when(shareRepository.count()).thenReturn(0L);
        when(futureRepository.count()).thenReturn(0L);
        when(indicativeRepository.count()).thenReturn(0L);

        // When & Then
        mockMvc.perform(get("/api/instruments/count"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.shares").value(0))
            .andExpect(jsonPath("$.futures").value(0))
            .andExpect(jsonPath("$.indicatives").value(0))
            .andExpect(jsonPath("$.total").value(0));

        verify(shareRepository).count();
        verify(futureRepository).count();
        verify(indicativeRepository).count();
    }
}
