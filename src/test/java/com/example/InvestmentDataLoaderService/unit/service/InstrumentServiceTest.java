package com.example.InvestmentDataLoaderService.unit.service;

import com.example.InvestmentDataLoaderService.client.TinkoffRestClient;
import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;
import com.example.InvestmentDataLoaderService.repository.*;
import com.example.InvestmentDataLoaderService.service.InstrumentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc.InstrumentsServiceBlockingStub;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для InstrumentService
 */
@ExtendWith(MockitoExtension.class)
class InstrumentServiceTest {

    @Mock
    private InstrumentsServiceBlockingStub instrumentsService;

    @Mock
    private ShareRepository shareRepository;

    @Mock
    private FutureRepository futureRepository;

    @Mock
    private IndicativeRepository indicativeRepository;

    @Mock
    private TinkoffRestClient restClient;

    @InjectMocks
    private InstrumentService instrumentService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    // ==================== ТЕСТЫ ДЛЯ АКЦИЙ ====================

    @Test
    void getShares_ShouldReturnFilteredShares_WhenValidParameters() {
        // Given
        Share share1 = Share.newBuilder()
            .setFigi("BBG004730N88")
            .setTicker("SBER")
            .setName("Сбербанк")
            .setCurrency("RUB")
            .setExchange("moex_mrng_evng_e_wknd_dlr")
            .setSector("Financials")
            .setTradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING)
            .build();

        Share share2 = Share.newBuilder()
            .setFigi("BBG004730ZJ9")
            .setTicker("GAZP")
            .setName("Газпром")
            .setCurrency("RUB")
            .setExchange("moex_mrng_evng_e_wknd_dlr")
            .setSector("Energy")
            .setTradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING)
            .build();

        SharesResponse response = SharesResponse.newBuilder()
            .addInstruments(share1)
            .addInstruments(share2)
            .build();

        when(instrumentsService.shares(any(InstrumentsRequest.class))).thenReturn(response);

        // When
        List<ShareDto> result = instrumentService.getShares("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER", null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).figi()).isEqualTo("BBG004730N88");
        assertThat(result.get(0).ticker()).isEqualTo("SBER");
        assertThat(result.get(0).name()).isEqualTo("Сбербанк");
        assertThat(result.get(0).currency()).isEqualTo("RUB");
        assertThat(result.get(0).exchange()).isEqualTo("moex_mrng_evng_e_wknd_dlr");
        assertThat(result.get(0).sector()).isEqualTo("Financials");
        assertThat(result.get(0).tradingStatus()).isEqualTo("SECURITY_TRADING_STATUS_NORMAL_TRADING");

        verify(instrumentsService).shares(any(InstrumentsRequest.class));
    }

    @Test
    void getShares_ShouldReturnAllShares_WhenNoFilters() {
        // Given
        Share share1 = Share.newBuilder()
            .setFigi("BBG004730N88")
            .setTicker("SBER")
            .setName("Сбербанк")
            .setCurrency("RUB")
            .setExchange("moex_mrng_evng_e_wknd_dlr")
            .setSector("Financials")
            .setTradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING)
            .build();

        SharesResponse response = SharesResponse.newBuilder()
            .addInstruments(share1)
            .build();

        when(instrumentsService.shares(any(InstrumentsRequest.class))).thenReturn(response);

        // When
        List<ShareDto> result = instrumentService.getShares(null, null, null, null, null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).figi()).isEqualTo("BBG004730N88");
        assertThat(result.get(0).ticker()).isEqualTo("SBER");
    }

    @Test
    void getShares_ShouldHandleInvalidStatus_WhenStatusIsInvalid() {
        // Given
        Share share1 = Share.newBuilder()
            .setFigi("BBG004730N88")
            .setTicker("SBER")
            .setName("Сбербанк")
            .setCurrency("RUB")
            .setExchange("moex_mrng_evng_e_wknd_dlr")
            .setSector("Financials")
            .setTradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING)
            .build();

        SharesResponse response = SharesResponse.newBuilder()
            .addInstruments(share1)
            .build();

        when(instrumentsService.shares(any(InstrumentsRequest.class))).thenReturn(response);

        // When
        List<ShareDto> result = instrumentService.getShares("INVALID_STATUS", null, null, null, null);

        // Then
        assertThat(result).hasSize(1);
        verify(instrumentsService).shares(any(InstrumentsRequest.class));
    }

    @Test
    void saveShares_ShouldSaveNewShares_WhenSharesNotExist() {
        // Given
        ShareFilterDto filter = new ShareFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_BASE");
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("SBER");

        Share share1 = Share.newBuilder()
            .setFigi("BBG004730N88")
            .setTicker("SBER")
            .setName("Сбербанк")
            .setCurrency("RUB")
            .setExchange("moex_mrng_evng_e_wknd_dlr")
            .setSector("Financials")
            .setTradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING)
            .build();

        SharesResponse response = SharesResponse.newBuilder()
            .addInstruments(share1)
            .build();

        when(instrumentsService.shares(any(InstrumentsRequest.class))).thenReturn(response);
        when(shareRepository.existsById("BBG004730N88")).thenReturn(false);
        when(shareRepository.save(any(ShareEntity.class))).thenReturn(new ShareEntity());

        // When
        SaveResponseDto result = instrumentService.saveShares(filter);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getNewItemsSaved()).isEqualTo(1);
        assertThat(result.getExistingItemsSkipped()).isEqualTo(0);
        assertThat(result.getSavedItems()).hasSize(1);

        verify(shareRepository).existsById("BBG004730N88");
        verify(shareRepository).save(any(ShareEntity.class));
    }

    @Test
    void saveShares_ShouldSkipExistingShares_WhenSharesAlreadyExist() {
        // Given
        ShareFilterDto filter = new ShareFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_BASE");
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("SBER");

        Share share1 = Share.newBuilder()
            .setFigi("BBG004730N88")
            .setTicker("SBER")
            .setName("Сбербанк")
            .setCurrency("RUB")
            .setExchange("moex_mrng_evng_e_wknd_dlr")
            .setSector("Financials")
            .setTradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING)
            .build();

        SharesResponse response = SharesResponse.newBuilder()
            .addInstruments(share1)
            .build();

        when(instrumentsService.shares(any(InstrumentsRequest.class))).thenReturn(response);
        when(shareRepository.existsById("BBG004730N88")).thenReturn(true);

        // When
        SaveResponseDto result = instrumentService.saveShares(filter);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getNewItemsSaved()).isEqualTo(0);
        assertThat(result.getExistingItemsSkipped()).isEqualTo(1);
        assertThat(result.getSavedItems()).isEmpty();

        verify(shareRepository).existsById("BBG004730N88");
        verify(shareRepository, never()).save(any(ShareEntity.class));
    }

    @Test
    void getSharesFromDatabase_ShouldReturnFilteredShares_WhenValidFilter() {
        // Given
        ShareEntity entity1 = new ShareEntity(
            "BBG004730N88", "SBER", "Сбербанк", "RUB", 
            "moex_mrng_evng_e_wknd_dlr", "Financials", 
            "SECURITY_TRADING_STATUS_NORMAL_TRADING", 
            LocalDateTime.now(), LocalDateTime.now()
        );

        ShareEntity entity2 = new ShareEntity(
            "BBG004730ZJ9", "GAZP", "Газпром", "RUB", 
            "moex_mrng_evng_e_wknd_dlr", "Energy", 
            "SECURITY_TRADING_STATUS_NORMAL_TRADING", 
            LocalDateTime.now(), LocalDateTime.now()
        );

        ShareFilterDto filter = new ShareFilterDto();
        filter.setTicker("SBER");

        when(shareRepository.findAll()).thenReturn(Arrays.asList(entity1, entity2));

        // When
        List<ShareDto> result = instrumentService.getSharesFromDatabase(filter);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).figi()).isEqualTo("BBG004730N88");
        assertThat(result.get(0).ticker()).isEqualTo("SBER");
        assertThat(result.get(0).name()).isEqualTo("Сбербанк");
    }

    @Test
    void getShareByFigi_ShouldReturnShare_WhenFound() {
        // Given
        ShareEntity entity = new ShareEntity(
            "BBG004730N88", "SBER", "Сбербанк", "RUB", 
            "moex_mrng_evng_e_wknd_dlr", "Financials", 
            "SECURITY_TRADING_STATUS_NORMAL_TRADING", 
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(shareRepository.findById("BBG004730N88")).thenReturn(Optional.of(entity));

        // When
        ShareDto result = instrumentService.getShareByFigi("BBG004730N88");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.figi()).isEqualTo("BBG004730N88");
        assertThat(result.ticker()).isEqualTo("SBER");
        assertThat(result.name()).isEqualTo("Сбербанк");

        verify(shareRepository).findById("BBG004730N88");
    }

    @Test
    void getShareByFigi_ShouldReturnNull_WhenNotFound() {
        // Given
        when(shareRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        // When
        ShareDto result = instrumentService.getShareByFigi("UNKNOWN");

        // Then
        assertThat(result).isNull();

        verify(shareRepository).findById("UNKNOWN");
    }

    @Test
    void getShareByTicker_ShouldReturnShare_WhenFound() {
        // Given
        ShareEntity entity = new ShareEntity(
            "BBG004730N88", "SBER", "Сбербанк", "RUB", 
            "moex_mrng_evng_e_wknd_dlr", "Financials", 
            "SECURITY_TRADING_STATUS_NORMAL_TRADING", 
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(shareRepository.findByTickerIgnoreCase("SBER")).thenReturn(Optional.of(entity));

        // When
        ShareDto result = instrumentService.getShareByTicker("SBER");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.figi()).isEqualTo("BBG004730N88");
        assertThat(result.ticker()).isEqualTo("SBER");

        verify(shareRepository).findByTickerIgnoreCase("SBER");
    }

    @Test
    void getShareByTicker_ShouldReturnNull_WhenNotFound() {
        // Given
        when(shareRepository.findByTickerIgnoreCase("UNKNOWN")).thenReturn(Optional.empty());

        // When
        ShareDto result = instrumentService.getShareByTicker("UNKNOWN");

        // Then
        assertThat(result).isNull();

        verify(shareRepository).findByTickerIgnoreCase("UNKNOWN");
    }

    @Test
    void updateShare_ShouldUpdateShare_WhenShareExists() {
        // Given

        ShareEntity existingEntity = new ShareEntity(
            "BBG004730N88", "SBER_OLD", "Сбербанк Старый", "RUB", 
            "moex_mrng_evng_e_wknd_dlr", "Financials", 
            "SECURITY_TRADING_STATUS_NORMAL_TRADING", 
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(shareRepository.findById("BBG004730N88")).thenReturn(Optional.of(existingEntity));
        when(shareRepository.save(any(ShareEntity.class))).thenReturn(existingEntity);

        // When
        SaveResponseDto result = instrumentService.saveShares(new ShareFilterDto("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER", "BBG004730N88", "Financials", "SECURITY_TRADING_STATUS_NORMAL_TRADING"));

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Акция успешно обновлена");
        assertThat(result.getNewItemsSaved()).isEqualTo(1);

        verify(shareRepository).findById("BBG004730N88");
        verify(shareRepository).save(any(ShareEntity.class));
    }

    @Test
    void updateShare_ShouldReturnError_WhenShareNotFound() {
        // Given

        when(shareRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        // When
        SaveResponseDto result = instrumentService.saveShares(new ShareFilterDto("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER", "BBG004730N88", "Financials", "SECURITY_TRADING_STATUS_NORMAL_TRADING"));

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Акция с FIGI UNKNOWN не найдена");
        assertThat(result.getNewItemsSaved()).isEqualTo(0);

        verify(shareRepository).findById("UNKNOWN");
        verify(shareRepository, never()).save(any(ShareEntity.class));
    }

    // ==================== ТЕСТЫ ДЛЯ ФЬЮЧЕРСОВ ====================

    @Test
    void getFutures_ShouldReturnFilteredFutures_WhenValidParameters() {
        // Given
        Future future1 = Future.newBuilder()
            .setFigi("FUTSBER0324")
            .setTicker("SBER-3.24")
            .setAssetType("FUTURES")
            .setBasicAsset("SBER")
            .setCurrency("RUB")
            .setExchange("moex_mrng_evng_e_wknd_dlr")
            .build();

        FuturesResponse response = FuturesResponse.newBuilder()
            .addInstruments(future1)
            .build();

        when(instrumentsService.futures(any(InstrumentsRequest.class))).thenReturn(response);

        // When
        List<FutureDto> result = instrumentService.getFutures("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER-3.24", "FUTURES");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).figi()).isEqualTo("FUTSBER0324");
        assertThat(result.get(0).ticker()).isEqualTo("SBER-3.24");
        assertThat(result.get(0).assetType()).isEqualTo("FUTURES");
        assertThat(result.get(0).basicAsset()).isEqualTo("SBER");
        assertThat(result.get(0).currency()).isEqualTo("RUB");
        assertThat(result.get(0).exchange()).isEqualTo("moex_mrng_evng_e_wknd_dlr");

        verify(instrumentsService).futures(any(InstrumentsRequest.class));
    }

    @Test
    void saveFutures_ShouldSaveNewFutures_WhenFuturesNotExist() {
        // Given
        FutureFilterDto filter = new FutureFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_BASE");
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("SBER-3.24");
        filter.setAssetType("FUTURES");

        Future future1 = Future.newBuilder()
            .setFigi("FUTSBER0324")
            .setTicker("SBER-3.24")
            .setAssetType("FUTURES")
            .setBasicAsset("SBER")
            .setCurrency("RUB")
            .setExchange("moex_mrng_evng_e_wknd_dlr")
            .build();

        FuturesResponse response = FuturesResponse.newBuilder()
            .addInstruments(future1)
            .build();

        when(instrumentsService.futures(any(InstrumentsRequest.class))).thenReturn(response);
        when(futureRepository.existsById("FUTSBER0324")).thenReturn(false);
        when(futureRepository.save(any(FutureEntity.class))).thenReturn(new FutureEntity());

        // When
        SaveResponseDto result = instrumentService.saveFutures(filter);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getNewItemsSaved()).isEqualTo(1);
        assertThat(result.getExistingItemsSkipped()).isEqualTo(0);
        assertThat(result.getSavedItems()).hasSize(1);

        verify(futureRepository).existsById("FUTSBER0324");
        verify(futureRepository).save(any(FutureEntity.class));
    }

    @Test
    void getFutureByFigi_ShouldReturnFuture_WhenFound() {
        // Given
        FutureEntity entity = new FutureEntity(
            "FUTSBER0324", "SBER-3.24", "FUTURES", "SBER", "RUB", "moex_mrng_evng_e_wknd_dlr"
        );

        when(futureRepository.findById("FUTSBER0324")).thenReturn(Optional.of(entity));

        // When
        FutureDto result = instrumentService.getFutureByFigi("FUTSBER0324");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.figi()).isEqualTo("FUTSBER0324");
        assertThat(result.ticker()).isEqualTo("SBER-3.24");
        assertThat(result.assetType()).isEqualTo("FUTURES");

        verify(futureRepository).findById("FUTSBER0324");
    }

    @Test
    void getFutureByTicker_ShouldReturnFuture_WhenFound() {
        // Given
        FutureEntity entity = new FutureEntity(
            "FUTSBER0324", "SBER-3.24", "FUTURES", "SBER", "RUB", "moex_mrng_evng_e_wknd_dlr"
        );

        when(futureRepository.findByTickerIgnoreCase("SBER-3.24")).thenReturn(Optional.of(entity));

        // When
        FutureDto result = instrumentService.getFutureByTicker("SBER-3.24");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.figi()).isEqualTo("FUTSBER0324");
        assertThat(result.ticker()).isEqualTo("SBER-3.24");

        verify(futureRepository).findByTickerIgnoreCase("SBER-3.24");
    }

    // ==================== ТЕСТЫ ДЛЯ ИНДИКАТИВОВ ====================

    @Test
    void getIndicatives_ShouldReturnIndicatives_WhenRestApiAvailable() throws Exception {
        // Given
        String jsonResponse = """
            {
                "instruments": [
                    {
                        "figi": "BBG004730ZJ9",
                        "ticker": "RTSI",
                        "name": "Индекс РТС",
                        "currency": "RUB",
                        "exchange": "moex_mrng_evng_e_wknd_dlr",
                        "classCode": "SPBXM",
                        "uid": "test-uid",
                        "sellAvailableFlag": true,
                        "buyAvailableFlag": true
                    }
                ]
            }
            """;

        JsonNode response = objectMapper.readTree(jsonResponse);
        when(restClient.getIndicatives()).thenReturn(response);

        // When
        List<IndicativeDto> result = instrumentService.getIndicatives("moex_mrng_evng_e_wknd_dlr", "RUB", "RTSI", "BBG004730ZJ9");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).figi()).isEqualTo("BBG004730ZJ9");
        assertThat(result.get(0).ticker()).isEqualTo("RTSI");
        assertThat(result.get(0).name()).isEqualTo("Индекс РТС");
        assertThat(result.get(0).currency()).isEqualTo("RUB");
        assertThat(result.get(0).exchange()).isEqualTo("moex_mrng_evng_e_wknd_dlr");
        assertThat(result.get(0).classCode()).isEqualTo("SPBXM");
        assertThat(result.get(0).uid()).isEqualTo("test-uid");
        assertThat(result.get(0).sellAvailableFlag()).isTrue();
        assertThat(result.get(0).buyAvailableFlag()).isTrue();

        verify(restClient).getIndicatives();
    }

    @Test
    void getIndicatives_ShouldFallbackToDatabase_WhenRestApiFails() {
        // Given
        IndicativeEntity entity = new IndicativeEntity(
            "BBG004730ZJ9", "RTSI", "Индекс РТС", "RUB", 
            "moex_mrng_evng_e_wknd_dlr", "SPBXM", "test-uid", true, true
        );

        when(restClient.getIndicatives()).thenThrow(new RuntimeException("API Error"));
        when(indicativeRepository.findAll()).thenReturn(Arrays.asList(entity));

        // When
        List<IndicativeDto> result = instrumentService.getIndicatives(null, null, null, null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).figi()).isEqualTo("BBG004730ZJ9");
        assertThat(result.get(0).ticker()).isEqualTo("RTSI");

        verify(restClient).getIndicatives();
        verify(indicativeRepository).findAll();
    }

    @Test
    void getIndicativeBy_ShouldReturnIndicative_WhenFoundViaRestApi() throws Exception {
        // Given
        String jsonResponse = """
            {
                "instrument": {
                    "figi": "BBG004730ZJ9",
                    "ticker": "RTSI",
                    "name": "Индекс РТС",
                    "currency": "RUB",
                    "exchange": "moex_mrng_evng_e_wknd_dlr",
                    "classCode": "SPBXM",
                    "uid": "test-uid",
                    "sellAvailableFlag": true,
                    "buyAvailableFlag": true
                }
            }
            """;

        JsonNode response = objectMapper.readTree(jsonResponse);
        when(restClient.getIndicativeBy("BBG004730ZJ9")).thenReturn(response);

        // When
        IndicativeDto result = instrumentService.getIndicativeBy("BBG004730ZJ9");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.figi()).isEqualTo("BBG004730ZJ9");
        assertThat(result.ticker()).isEqualTo("RTSI");
        assertThat(result.name()).isEqualTo("Индекс РТС");

        verify(restClient).getIndicativeBy("BBG004730ZJ9");
    }

    @Test
    void getIndicativeBy_ShouldFallbackToDatabase_WhenRestApiFails() {
        // Given
        IndicativeEntity entity = new IndicativeEntity(
            "BBG004730ZJ9", "RTSI", "Индекс РТС", "RUB", 
            "moex_mrng_evng_e_wknd_dlr", "SPBXM", "test-uid", true, true
        );

        when(restClient.getIndicativeBy("BBG004730ZJ9")).thenThrow(new RuntimeException("API Error"));
        when(indicativeRepository.findById("BBG004730ZJ9")).thenReturn(Optional.of(entity));

        // When
        IndicativeDto result = instrumentService.getIndicativeBy("BBG004730ZJ9");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.figi()).isEqualTo("BBG004730ZJ9");
        assertThat(result.ticker()).isEqualTo("RTSI");

        verify(restClient).getIndicativeBy("BBG004730ZJ9");
        verify(indicativeRepository).findById("BBG004730ZJ9");
    }

    @Test
    void getIndicativeByTicker_ShouldReturnIndicative_WhenFound() throws Exception {
        // Given
        String jsonResponse = """
            {
                "instruments": [
                    {
                        "figi": "BBG004730ZJ9",
                        "ticker": "RTSI",
                        "name": "Индекс РТС",
                        "currency": "RUB",
                        "exchange": "moex_mrng_evng_e_wknd_dlr",
                        "classCode": "SPBXM",
                        "uid": "test-uid",
                        "sellAvailableFlag": true,
                        "buyAvailableFlag": true
                    }
                ]
            }
            """;

        JsonNode response = objectMapper.readTree(jsonResponse);
        when(restClient.getIndicatives()).thenReturn(response);

        // When
        IndicativeDto result = instrumentService.getIndicativeByTicker("RTSI");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.figi()).isEqualTo("BBG004730ZJ9");
        assertThat(result.ticker()).isEqualTo("RTSI");
        assertThat(result.name()).isEqualTo("Индекс РТС");

        verify(restClient).getIndicatives();
    }

    @Test
    void saveIndicatives_ShouldSaveNewIndicatives_WhenIndicativesNotExist() throws Exception {
        // Given
        IndicativeFilterDto filter = new IndicativeFilterDto();
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("RTSI");
        filter.setFigi("BBG004730ZJ9");

        String jsonResponse = """
            {
                "instruments": [
                    {
                        "figi": "BBG004730ZJ9",
                        "ticker": "RTSI",
                        "name": "Индекс РТС",
                        "currency": "RUB",
                        "exchange": "moex_mrng_evng_e_wknd_dlr",
                        "classCode": "SPBXM",
                        "uid": "test-uid",
                        "sellAvailableFlag": true,
                        "buyAvailableFlag": true
                    }
                ]
            }
            """;

        JsonNode response = objectMapper.readTree(jsonResponse);
        when(restClient.getIndicatives()).thenReturn(response);
        when(indicativeRepository.existsById("BBG004730ZJ9")).thenReturn(false);
        when(indicativeRepository.save(any(IndicativeEntity.class))).thenReturn(new IndicativeEntity());

        // When
        SaveResponseDto result = instrumentService.saveIndicatives(filter);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getNewItemsSaved()).isEqualTo(1);
        assertThat(result.getExistingItemsSkipped()).isEqualTo(0);
        assertThat(result.getSavedItems()).hasSize(1);

        verify(restClient).getIndicatives();
        verify(indicativeRepository).existsById("BBG004730ZJ9");
        verify(indicativeRepository).save(any(IndicativeEntity.class));
    }

    @Test
    void saveIndicatives_ShouldSkipIndicativesWithEmptyFigi() throws Exception {
        // Given
        IndicativeFilterDto filter = new IndicativeFilterDto();
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("RTSI");

        String jsonResponse = """
            {
                "instruments": [
                    {
                        "figi": "",
                        "ticker": "RTSI",
                        "name": "Индекс РТС",
                        "currency": "RUB",
                        "exchange": "moex_mrng_evng_e_wknd_dlr",
                        "classCode": "SPBXM",
                        "uid": "test-uid",
                        "sellAvailableFlag": true,
                        "buyAvailableFlag": true
                    }
                ]
            }
            """;

        JsonNode response = objectMapper.readTree(jsonResponse);
        when(restClient.getIndicatives()).thenReturn(response);

        // When
        SaveResponseDto result = instrumentService.saveIndicatives(filter);

        // Then
        assertThat(result.isSuccess()).isFalse(); // success = false, потому что indicativesFromApi пустой после фильтрации
        assertThat(result.getNewItemsSaved()).isEqualTo(0);
        assertThat(result.getExistingItemsSkipped()).isEqualTo(0);
        assertThat(result.getSavedItems()).isEmpty();

        verify(restClient).getIndicatives();
        verify(indicativeRepository, never()).existsById(anyString());
        verify(indicativeRepository, never()).save(any(IndicativeEntity.class));
    }
}
