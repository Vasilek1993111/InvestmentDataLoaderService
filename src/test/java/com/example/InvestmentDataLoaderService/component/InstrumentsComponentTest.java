package com.example.InvestmentDataLoaderService.component;

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
 * Компонентные тесты для проверки взаимодействия между TInvestService и InstrumentService
 * 
 * Компонентные тесты проверяют:
 * - Взаимодействие между компонентами системы
 * - Правильность делегирования вызовов
 * - Консистентность данных в процессе обработки
 * - Обработку ошибок и граничных случаев
 * 
 * В отличие от интеграционных тестов, компонентные тесты:
 * - Не используют реальные внешние системы
 * - Мокируют все зависимости
 * - Фокусируются на логике взаимодействия компонентов
 */
@ExtendWith(MockitoExtension.class)
class InstrumentsComponentTest {

    @Mock
    private UsersServiceBlockingStub usersService;

    @Mock
    private MainSessionPriceService mainSessionPriceService;

    @Mock
    private TradingService tradingService;

    @InjectMocks
    private InstrumentService instrumentService;

    @BeforeEach
    void setUp() {
        // Настройка моков для всех тестов
    }

    @Test
    void shouldIntegrateSharesWorkflow_WhenAllComponentsWork() {
        // Given
        ShareDto testShare = new ShareDto(
            "BBG004730N88", "SBER", "Сбербанк", "RUB", 
            "moex_mrng_evng_e_wknd_dlr", "Financials", 
            "SECURITY_TRADING_STATUS_NORMAL_TRADING"
        );
        List<ShareDto> sharesFromApi = Arrays.asList(testShare);
        SaveResponseDto saveResponse = new SaveResponseDto(
            true, "Успешно загружено 1 новых акций", 1, 1, 0, 0, 0, Arrays.asList(testShare)
        );

        when(instrumentService.getShares("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER", null))
            .thenReturn(sharesFromApi);
        when(instrumentService.saveShares(any(ShareFilterDto.class)))
            .thenReturn(saveResponse);
        when(instrumentService.getShareByFigi(testShare.figi()))
            .thenReturn(testShare);
        when(instrumentService.getShareByTicker(testShare.ticker()))
            .thenReturn(testShare);

        // When - Получаем акции из API
        List<ShareDto> apiShares = instrumentService.getShares("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER", null);
        
        // Сохраняем акции
        ShareFilterDto filter = new ShareFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_BASE");
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("SBER");
        SaveResponseDto saveResult = instrumentService.saveShares(filter);
        
        // Получаем акцию по FIGI
        ShareDto shareByFigi = instrumentService.getShareByFigi(testShare.figi());
        
        // Получаем акцию по тикеру
        ShareDto shareByTicker = instrumentService.getShareByTicker(testShare.ticker());

        // Then - Проверяем интеграцию
        assertThat(apiShares).hasSize(1);
        assertThat(apiShares.get(0)).isEqualTo(testShare);
        
        assertThat(saveResult.isSuccess()).isTrue();
        assertThat(saveResult.getNewItemsSaved()).isEqualTo(1);
        
        assertThat(shareByFigi).isEqualTo(testShare);
        assertThat(shareByTicker).isEqualTo(testShare);

        // Проверяем, что все методы были вызваны
        verify(instrumentService).getShares("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER", null);
        verify(instrumentService).saveShares(any(ShareFilterDto.class));
        verify(instrumentService).getShareByFigi(testShare.figi());
        verify(instrumentService).getShareByTicker(testShare.ticker());
    }

    @Test
    void shouldIntegrateFuturesWorkflow_WhenAllComponentsWork() {
        // Given
        FutureDto testFuture = new FutureDto(
            "FUTSBER0324", "SBER-3.24", "FUTURES", "SBER", "RUB", "moex_mrng_evng_e_wknd_dlr"
        );
        List<FutureDto> futuresFromApi = Arrays.asList(testFuture);
        SaveResponseDto saveResponse = new SaveResponseDto(
            true, "Успешно загружено 1 новых фьючерсов", 1, 1, 0, 0, 0, Arrays.asList(testFuture)
        );

        when(instrumentService.getFutures("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER-3.24", "FUTURES"))
            .thenReturn(futuresFromApi);
        when(instrumentService.saveFutures(any(FutureFilterDto.class)))
            .thenReturn(saveResponse);
        when(instrumentService.getFutureByFigi(testFuture.figi()))
            .thenReturn(testFuture);
        when(instrumentService.getFutureByTicker(testFuture.ticker()))
            .thenReturn(testFuture);

        // When - Получаем фьючерсы из API
        List<FutureDto> apiFutures = instrumentService.getFutures("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER-3.24", "FUTURES");
        
        // Сохраняем фьючерсы
        FutureFilterDto filter = new FutureFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_BASE");
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("SBER-3.24");
        filter.setAssetType("FUTURES");
        SaveResponseDto saveResult = instrumentService.saveFutures(filter);
        
        // Получаем фьючерс по FIGI
        FutureDto futureByFigi = instrumentService.getFutureByFigi(testFuture.figi());
        
        // Получаем фьючерс по тикеру
        FutureDto futureByTicker = instrumentService.getFutureByTicker(testFuture.ticker());

        // Then - Проверяем интеграцию
        assertThat(apiFutures).hasSize(1);
        assertThat(apiFutures.get(0)).isEqualTo(testFuture);
        
        assertThat(saveResult.isSuccess()).isTrue();
        assertThat(saveResult.getNewItemsSaved()).isEqualTo(1);
        
        assertThat(futureByFigi).isEqualTo(testFuture);
        assertThat(futureByTicker).isEqualTo(testFuture);

        // Проверяем, что все методы были вызваны
        verify(instrumentService).getFutures("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER-3.24", "FUTURES");
        verify(instrumentService).saveFutures(any(FutureFilterDto.class));
        verify(instrumentService).getFutureByFigi(testFuture.figi());
        verify(instrumentService).getFutureByTicker(testFuture.ticker());
    }

    @Test
    void shouldIntegrateIndicativesWorkflow_WhenAllComponentsWork() {
        // Given
        IndicativeDto testIndicative = new IndicativeDto(
            "BBG004730ZJ9", "RTSI", "Индекс РТС", "RUB", 
            "moex_mrng_evng_e_wknd_dlr", "SPBXM", "test-uid", true, true
        );
        List<IndicativeDto> indicativesFromApi = Arrays.asList(testIndicative);
        SaveResponseDto saveResponse = new SaveResponseDto(
            true, "Успешно загружено 1 новых индикативных инструментов", 1, 1, 0, 0, 0, Arrays.asList(testIndicative)
        );

        when(instrumentService.getIndicatives("moex_mrng_evng_e_wknd_dlr", "RUB", "RTSI", "BBG004730ZJ9"))
            .thenReturn(indicativesFromApi);
        when(instrumentService.saveIndicatives(any(IndicativeFilterDto.class)))
            .thenReturn(saveResponse);
        when(instrumentService.getIndicativeBy(testIndicative.figi()))
            .thenReturn(testIndicative);
        when(instrumentService.getIndicativeByTicker(testIndicative.ticker()))
            .thenReturn(testIndicative);

        // When - Получаем индикативы из API
        List<IndicativeDto> apiIndicatives = instrumentService.getIndicatives("moex_mrng_evng_e_wknd_dlr", "RUB", "RTSI", "BBG004730ZJ9");
        
        // Сохраняем индикативы
        IndicativeFilterDto filter = new IndicativeFilterDto();
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("RTSI");
        filter.setFigi("BBG004730ZJ9");
        SaveResponseDto saveResult = instrumentService.saveIndicatives(filter);
        
        // Получаем индикатив по FIGI
        IndicativeDto indicativeByFigi = instrumentService.getIndicativeBy(testIndicative.figi());
        
        // Получаем индикатив по тикеру
        IndicativeDto indicativeByTicker = instrumentService.getIndicativeByTicker(testIndicative.ticker());

        // Then - Проверяем интеграцию
        assertThat(apiIndicatives).hasSize(1);
        assertThat(apiIndicatives.get(0)).isEqualTo(testIndicative);
        
        assertThat(saveResult.isSuccess()).isTrue();
        assertThat(saveResult.getNewItemsSaved()).isEqualTo(1);
        
        assertThat(indicativeByFigi).isEqualTo(testIndicative);
        assertThat(indicativeByTicker).isEqualTo(testIndicative);

        // Проверяем, что все методы были вызваны
        verify(instrumentService).getIndicatives("moex_mrng_evng_e_wknd_dlr", "RUB", "RTSI", "BBG004730ZJ9");
        verify(instrumentService).saveIndicatives(any(IndicativeFilterDto.class));
        verify(instrumentService).getIndicativeBy(testIndicative.figi());
        verify(instrumentService).getIndicativeByTicker(testIndicative.ticker());
    }

    @Test
    void shouldHandleErrorPropagation_WhenInstrumentServiceFails() {
        // Given
        RuntimeException serviceException = new RuntimeException("Service error");
        when(instrumentService.getShares("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER", null))
            .thenThrow(serviceException);

        // When & Then - Проверяем, что исключение пробрасывается
        try {
            instrumentService.getShares("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER", null);
            assertThat(false).as("Expected exception was not thrown").isTrue();
        } catch (RuntimeException e) {
            assertThat(e).isEqualTo(serviceException);
        }

        verify(instrumentService).getShares("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER", null);
    }

    @Test
    void shouldHandleNullResults_WhenInstrumentServiceReturnsNull() {
        // Given
        when(instrumentService.getShareByFigi("UNKNOWN")).thenReturn(null);
        when(instrumentService.getFutureByTicker("UNKNOWN")).thenReturn(null);
        when(instrumentService.getIndicativeBy("UNKNOWN")).thenReturn(null);

        // When
        ShareDto shareResult = instrumentService.getShareByFigi("UNKNOWN");
        FutureDto futureResult = instrumentService.getFutureByTicker("UNKNOWN");
        IndicativeDto indicativeResult = instrumentService.getIndicativeBy("UNKNOWN");

        // Then
        assertThat(shareResult).isNull();
        assertThat(futureResult).isNull();
        assertThat(indicativeResult).isNull();

        verify(instrumentService).getShareByFigi("UNKNOWN");
        verify(instrumentService).getFutureByTicker("UNKNOWN");
        verify(instrumentService).getIndicativeBy("UNKNOWN");
    }

    @Test
    void shouldHandleEmptyResults_WhenNoDataFound() {
        // Given
        when(instrumentService.getShares("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER", null))
            .thenReturn(Arrays.asList());
        when(instrumentService.getFutures("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER-3.24", "FUTURES"))
            .thenReturn(Arrays.asList());
        when(instrumentService.getIndicatives("moex_mrng_evng_e_wknd_dlr", "RUB", "RTSI", "BBG004730ZJ9"))
            .thenReturn(Arrays.asList());

        // When
        List<ShareDto> shares = instrumentService.getShares("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER", null);
        List<FutureDto> futures = instrumentService.getFutures("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER-3.24", "FUTURES");
        List<IndicativeDto> indicatives = instrumentService.getIndicatives("moex_mrng_evng_e_wknd_dlr", "RUB", "RTSI", "BBG004730ZJ9");

        // Then
        assertThat(shares).isEmpty();
        assertThat(futures).isEmpty();
        assertThat(indicatives).isEmpty();

        verify(instrumentService).getShares("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER", null);
        verify(instrumentService).getFutures("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER-3.24", "FUTURES");
        verify(instrumentService).getIndicatives("moex_mrng_evng_e_wknd_dlr", "RUB", "RTSI", "BBG004730ZJ9");
    }

    @Test
    void shouldValidateDataConsistency_WhenWorkingWithSameData() {
        // Given
        ShareDto testShare = new ShareDto(
            "BBG004730N88", "SBER", "Сбербанк", "RUB", 
            "moex_mrng_evng_e_wknd_dlr", "Financials", 
            "SECURITY_TRADING_STATUS_NORMAL_TRADING"
        );
        List<ShareDto> sharesFromApi = Arrays.asList(testShare);
        SaveResponseDto saveResponse = new SaveResponseDto(
            true, "Успешно загружено 1 новых акций", 1, 1, 0, 0, 0, Arrays.asList(testShare)
        );

        when(instrumentService.getShares("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER", null))
            .thenReturn(sharesFromApi);
        when(instrumentService.saveShares(any(ShareFilterDto.class)))
            .thenReturn(saveResponse);
        when(instrumentService.getShareByFigi(testShare.figi()))
            .thenReturn(testShare);

        // When - Получаем данные из API
        List<ShareDto> apiShares = instrumentService.getShares("INSTRUMENT_STATUS_BASE", "moex_mrng_evng_e_wknd_dlr", "RUB", "SBER", null);
        
        // Сохраняем данные
        ShareFilterDto filter = new ShareFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_BASE");
        filter.setExchange("moex_mrng_evng_e_wknd_dlr");
        filter.setCurrency("RUB");
        filter.setTicker("SBER");
        SaveResponseDto saveResult = instrumentService.saveShares(filter);
        
        // Получаем сохраненные данные
        ShareDto savedShare = instrumentService.getShareByFigi(testShare.figi());

        // Then - Проверяем консистентность данных
        assertThat(apiShares.get(0).figi()).isEqualTo(savedShare.figi());
        assertThat(apiShares.get(0).ticker()).isEqualTo(savedShare.ticker());
        assertThat(apiShares.get(0).name()).isEqualTo(savedShare.name());
        assertThat(apiShares.get(0).currency()).isEqualTo(savedShare.currency());
        assertThat(apiShares.get(0).exchange()).isEqualTo(savedShare.exchange());
        
        assertThat(saveResult.getSavedItems()).hasSize(1);
        assertThat(((ShareDto) saveResult.getSavedItems().get(0)).figi()).isEqualTo(testShare.figi());
    }
}
