package com.example.InvestmentDataLoaderService.unit.service;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.service.InstrumentService;
import com.example.InvestmentDataLoaderService.service.MainSessionPriceService;
import com.example.InvestmentDataLoaderService.service.TradingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tinkoff.piapi.contract.v1.UsersServiceGrpc.UsersServiceBlockingStub;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для InstrumentService (только методы, связанные с instruments)
 */
@ExtendWith(MockitoExtension.class)
class InstrumentServiceInstrumentsTest {

    @Mock
    private UsersServiceBlockingStub usersService;

    @Mock
    private MainSessionPriceService marketDataService;

    @Mock
    private TradingService tradingService;

    @InjectMocks
    private InstrumentService instrumentService;

    private ShareDto testShare;
    private FutureDto testFuture;
    private IndicativeDto testIndicative;
    private SaveResponseDto testSaveResponse;

    @BeforeEach
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
    void getShares_ShouldDelegateToInstrumentService() {
        // Given
        List<ShareDto> expectedShares = Arrays.asList(testShare);
        when(instrumentService.getShares(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(expectedShares);

        // When
        List<ShareDto> result = instrumentService.getShares("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER", "BBG004730N88");

        // Then
        assertThat(result).isEqualTo(expectedShares);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).figi()).isEqualTo("BBG004730N88");
        assertThat(result.get(0).ticker()).isEqualTo("SBER");

        verify(instrumentService).getShares("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER", "BBG004730N88");
    }

    @Test
    void saveShares_WithParameters_ShouldCreateFilterAndDelegate() {
        // Given
        when(instrumentService.saveShares(any(ShareFilterDto.class))).thenReturn(testSaveResponse);

        // When
        SaveResponseDto result = instrumentService.saveShares(new ShareFilterDto("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER", null, null, null));

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSavedItems()).hasSize(1);

        verify(instrumentService).saveShares(argThat(filter -> 
            "INSTRUMENT_STATUS_BASE".equals(filter.getStatus()) &&
            "moex_mrng_evng_e_wknd_dlr".equals(filter.getExchange()) &&
            "RUB".equals(filter.getCurrency()) &&
            "SBER".equals(filter.getTicker())
        ));
    }

    @Test
    void saveShares_WithFilter_ShouldDelegateDirectly() {
        // Given
        ShareFilterDto filter = new ShareFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_BASE");
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("SBER");

        when(instrumentService.saveShares(filter)).thenReturn(testSaveResponse);

        // When
        SaveResponseDto result = instrumentService.saveShares(filter);

        // Then
        assertThat(result).isEqualTo(testSaveResponse);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getNewItemsSaved()).isEqualTo(1);

        verify(instrumentService).saveShares(filter);
    }

    @Test
    void getSharesFromDatabase_ShouldDelegateToInstrumentService() {
        // Given
        ShareFilterDto filter = new ShareFilterDto();
        filter.setTicker("SBER");

        List<ShareDto> expectedShares = Arrays.asList(testShare);
        when(instrumentService.getSharesFromDatabase(filter)).thenReturn(expectedShares);

        // When
        List<ShareDto> result = instrumentService.getSharesFromDatabase(filter);

        // Then
        assertThat(result).isEqualTo(expectedShares);
        assertThat(result).hasSize(1);

        verify(instrumentService).getSharesFromDatabase(filter);
    }

    @Test
    void getShareByFigi_ShouldDelegateToInstrumentService() {
        // Given
        when(instrumentService.getShareByFigi("BBG004730N88")).thenReturn(testShare);

        // When
        ShareDto result = instrumentService.getShareByFigi("BBG004730N88");

        // Then
        assertThat(result).isEqualTo(testShare);
        assertThat(result.figi()).isEqualTo("BBG004730N88");
        assertThat(result.ticker()).isEqualTo("SBER");

        verify(instrumentService).getShareByFigi("BBG004730N88");
    }

    @Test
    void getShareByTicker_ShouldDelegateToInstrumentService() {
        // Given
        when(instrumentService.getShareByTicker("SBER")).thenReturn(testShare);

        // When
        ShareDto result = instrumentService.getShareByTicker("SBER");

        // Then
        assertThat(result).isEqualTo(testShare);
        assertThat(result.figi()).isEqualTo("BBG004730N88");
        assertThat(result.ticker()).isEqualTo("SBER");

        verify(instrumentService).getShareByTicker("SBER");
    }

    @Test
    void updateShare_ShouldDelegateToInstrumentService() {
        // Given
        when(instrumentService.saveShares(any(ShareFilterDto.class))).thenReturn(testSaveResponse);

        // When
        SaveResponseDto result = instrumentService.saveShares(any(ShareFilterDto.class));

        // Then
        assertThat(result).isEqualTo(testSaveResponse);
        assertThat(result.isSuccess()).isTrue();

        verify(instrumentService).saveShares(any(ShareFilterDto.class));
    }

    // ==================== ТЕСТЫ ДЛЯ ФЬЮЧЕРСОВ ====================

    @Test
    void getFutures_ShouldDelegateToInstrumentService() {
        // Given
        List<FutureDto> expectedFutures = Arrays.asList(testFuture);
        when(instrumentService.getFutures(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(expectedFutures);

        // When
        List<FutureDto> result = instrumentService.getFutures("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER-3.24", "FUTURES");

        // Then
        assertThat(result).isEqualTo(expectedFutures);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).figi()).isEqualTo("FUTSBER0324");
        assertThat(result.get(0).ticker()).isEqualTo("SBER-3.24");

        verify(instrumentService).getFutures("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER-3.24", "FUTURES");
    }

    @Test
    void saveFutures_WithParameters_ShouldCreateFilterAndDelegate() {
        // Given
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

        when(instrumentService.saveFutures(any(FutureFilterDto.class))).thenReturn(futureSaveResponse);

        // When
        SaveResponseDto result = instrumentService.saveFutures(new FutureFilterDto("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER-3.24", "FUTURES"));

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSavedItems()).hasSize(1);

        verify(instrumentService).saveFutures(argThat(filter -> 
            "INSTRUMENT_STATUS_BASE".equals(filter.getStatus()) &&
            "moex_mrng_evng_e_wknd_dlr".equals(filter.getExchange()) &&
            "RUB".equals(filter.getCurrency()) &&
            "SBER-3.24".equals(filter.getTicker()) &&
            "FUTURES".equals(filter.getAssetType())
        ));
    }

    @Test
    void saveFutures_WithFilter_ShouldDelegateDirectly() {
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

        when(instrumentService.saveFutures(filter)).thenReturn(futureSaveResponse);

        // When
        SaveResponseDto result = instrumentService.saveFutures(filter);

        // Then
        assertThat(result).isEqualTo(futureSaveResponse);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getNewItemsSaved()).isEqualTo(1);

        verify(instrumentService).saveFutures(filter);
    }

    @Test
    void getFutureByFigi_ShouldDelegateToInstrumentService() {
        // Given
        when(instrumentService.getFutureByFigi("FUTSBER0324")).thenReturn(testFuture);

        // When
        FutureDto result = instrumentService.getFutureByFigi("FUTSBER0324");

        // Then
        assertThat(result).isEqualTo(testFuture);
        assertThat(result.figi()).isEqualTo("FUTSBER0324");
        assertThat(result.ticker()).isEqualTo("SBER-3.24");

        verify(instrumentService).getFutureByFigi("FUTSBER0324");
    }

    @Test
    void getFutureByTicker_ShouldDelegateToInstrumentService() {
        // Given
        when(instrumentService.getFutureByTicker("SBER-3.24")).thenReturn(testFuture);

        // When
        FutureDto result = instrumentService.getFutureByTicker("SBER-3.24");

        // Then
        assertThat(result).isEqualTo(testFuture);
        assertThat(result.figi()).isEqualTo("FUTSBER0324");
        assertThat(result.ticker()).isEqualTo("SBER-3.24");

        verify(instrumentService).getFutureByTicker("SBER-3.24");
    }

    // ==================== ТЕСТЫ ДЛЯ ИНДИКАТИВОВ ====================

    @Test
    void getIndicatives_ShouldDelegateToInstrumentService() {
        // Given
        List<IndicativeDto> expectedIndicatives = Arrays.asList(testIndicative);
        when(instrumentService.getIndicatives(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(expectedIndicatives);

        // When
        List<IndicativeDto> result = instrumentService.getIndicatives("moex_mrng_evng_e_wknd_dlr", "RUB", "RTSI", "BBG004730ZJ9");

        // Then
        assertThat(result).isEqualTo(expectedIndicatives);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).figi()).isEqualTo("BBG004730ZJ9");
        assertThat(result.get(0).ticker()).isEqualTo("RTSI");

        verify(instrumentService).getIndicatives("moex_mrng_evng_e_wknd_dlr", "RUB", "RTSI", "BBG004730ZJ9");
    }

    @Test
    void getIndicativeBy_ShouldDelegateToInstrumentService() {
        // Given
        when(instrumentService.getIndicativeBy("BBG004730ZJ9")).thenReturn(testIndicative);

        // When
        IndicativeDto result = instrumentService.getIndicativeBy("BBG004730ZJ9");

        // Then
        assertThat(result).isEqualTo(testIndicative);
        assertThat(result.figi()).isEqualTo("BBG004730ZJ9");
        assertThat(result.ticker()).isEqualTo("RTSI");

        verify(instrumentService).getIndicativeBy("BBG004730ZJ9");
    }

    @Test
    void getIndicativeByTicker_ShouldDelegateToInstrumentService() {
        // Given
        when(instrumentService.getIndicativeByTicker("RTSI")).thenReturn(testIndicative);

        // When
        IndicativeDto result = instrumentService.getIndicativeByTicker("RTSI");

        // Then
        assertThat(result).isEqualTo(testIndicative);
        assertThat(result.figi()).isEqualTo("BBG004730ZJ9");
        assertThat(result.ticker()).isEqualTo("RTSI");

        verify(instrumentService).getIndicativeByTicker("RTSI");
    }

    @Test
    void saveIndicatives_ShouldDelegateToInstrumentService() {
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

        when(instrumentService.saveIndicatives(filter)).thenReturn(indicativeSaveResponse);

        // When
        SaveResponseDto result = instrumentService.saveIndicatives(filter);

        // Then
        assertThat(result).isEqualTo(indicativeSaveResponse);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getNewItemsSaved()).isEqualTo(1);

        verify(instrumentService).saveIndicatives(filter);
    }

    // ==================== ТЕСТЫ ГРАНИЧНЫХ СЛУЧАЕВ ====================

    @Test
    void getShares_ShouldHandleNullParameters() {
        // Given
        List<ShareDto> expectedShares = Arrays.asList(testShare);
        when(instrumentService.getShares(isNull(), isNull(), isNull(), isNull(), isNull()))
            .thenReturn(expectedShares);

        // When
        List<ShareDto> result = instrumentService.getShares(null, null, null, null, null);

        // Then
        assertThat(result).isEqualTo(expectedShares);
        verify(instrumentService).getShares(null, null, null, null, null);
    }

    @Test
    void getFutures_ShouldHandleNullParameters() {
        // Given
        List<FutureDto> expectedFutures = Arrays.asList(testFuture);
        when(instrumentService.getFutures(isNull(), isNull(), isNull(), isNull(), isNull()))
            .thenReturn(expectedFutures);

        // When
        List<FutureDto> result = instrumentService.getFutures(null, null, null, null, null);

        // Then
        assertThat(result).isEqualTo(expectedFutures);
        verify(instrumentService).getFutures(null, null, null, null, null);
    }

    @Test
    void getIndicatives_ShouldHandleNullParameters() {
        // Given
        List<IndicativeDto> expectedIndicatives = Arrays.asList(testIndicative);
        when(instrumentService.getIndicatives(isNull(), isNull(), isNull(), isNull()))
            .thenReturn(expectedIndicatives);

        // When
        List<IndicativeDto> result = instrumentService.getIndicatives(null, null, null, null);

        // Then
        assertThat(result).isEqualTo(expectedIndicatives);
        verify(instrumentService).getIndicatives(null, null, null, null);
    }

    @Test
    void getShareByFigi_ShouldHandleNullResult() {
        // Given
        when(instrumentService.getShareByFigi("UNKNOWN")).thenReturn(null);

        // When
        ShareDto result = instrumentService.getShareByFigi("UNKNOWN");

        // Then
        assertThat(result).isNull();
        verify(instrumentService).getShareByFigi("UNKNOWN");
    }

    @Test
    void getFutureByTicker_ShouldHandleNullResult() {
        // Given
        when(instrumentService.getFutureByTicker("UNKNOWN")).thenReturn(null);

        // When
        FutureDto result = instrumentService.getFutureByTicker("UNKNOWN");

        // Then
        assertThat(result).isNull();
        verify(instrumentService).getFutureByTicker("UNKNOWN");
    }

    @Test
    void getIndicativeBy_ShouldHandleNullResult() {
        // Given
        when(instrumentService.getIndicativeBy("UNKNOWN")).thenReturn(null);

        // When
        IndicativeDto result = instrumentService.getIndicativeBy("UNKNOWN");

        // Then
        assertThat(result).isNull();
        verify(instrumentService).getIndicativeBy("UNKNOWN");
    }

    @Test
    void saveShares_WithParameters_ShouldHandleEmptyResult() {
        // Given
        SaveResponseDto emptyResponse = new SaveResponseDto(
            false,
            "Новых акций не обнаружено",
            0,
            0,
            0,
            0, // invalidItemsFiltered
            0, // missingFromApi
            Arrays.asList()
        );

        when(instrumentService.saveShares(any(ShareFilterDto.class))).thenReturn(emptyResponse);

        // When
        SaveResponseDto result = instrumentService.saveShares(new ShareFilterDto("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER", null, null, null));

        // Then
        assertThat(result.isSuccess()).isFalse();
        verify(instrumentService).saveShares(any(ShareFilterDto.class));
    }

    @Test
    void saveFutures_WithParameters_ShouldHandleEmptyResult() {
        // Given
        SaveResponseDto emptyResponse = new SaveResponseDto(
            false,
            "Новых фьючерсов не обнаружено",
            0,
            0,
            0,
            0, // invalidItemsFiltered
            0, // missingFromApi
            Arrays.asList()
        );

        when(instrumentService.saveFutures(any(FutureFilterDto.class))).thenReturn(emptyResponse);

        // When
        SaveResponseDto result = instrumentService.saveFutures(new FutureFilterDto("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER-3.24", "FUTURES"));

        // Then
        assertThat(result.isSuccess()).isFalse();
        verify(instrumentService).saveFutures(any(FutureFilterDto.class));
    }
}
