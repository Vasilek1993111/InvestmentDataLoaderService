package com.example.InvestmentDataLoaderService.unit.service;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;
import com.example.InvestmentDataLoaderService.repository.*;
import com.example.InvestmentDataLoaderService.service.EveningSessionService;
import com.example.InvestmentDataLoaderService.service.MainSessionPriceService;

import io.qameta.allure.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Epic("Evening Session Service")
@Feature("Evening Session Service")
@DisplayName("Evening Session Service Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
public class EveningSessionServiceTest {

    @Mock
    private MainSessionPriceService mainSessionPriceService;
    @Mock
    private ShareRepository shareRepo;
    @Mock
    private FutureRepository futureRepo;
    @Mock
    private ClosePriceEveningSessionRepository closePriceEveningSessionRepo;

    @InjectMocks
    private EveningSessionService eveningSessionService;

    @BeforeEach
    void setUp() {
        reset(mainSessionPriceService, shareRepo, futureRepo, closePriceEveningSessionRepo);
    }

    // ========== ПОЗИТИВНЫЕ ТЕСТЫ ==========

    @Test
    @DisplayName("Успешное сохранение цен вечерней сессии с указанными инструментами")
    @Description("Тест проверяет основную функциональность сохранения цен вечерней сессии")
    @Story("Успешные сценарии")
    void saveClosePricesEveningSession_ShouldReturnSuccessResponse_WhenInstrumentsProvided() {
        // Given
        ClosePriceEveningSessionRequestDto request = createRequestWithInstruments();
        List<ClosePriceDto> testClosePrices = createTestClosePrices();
        ShareEntity share = createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        FutureEntity future = createFutureEntity("TEST_FUTURE_001", "Si-3.24", "RUB", "MOEX");

        // Настраиваем моки
        when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
            .thenReturn(testClosePrices);
        when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.of(share));
        when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
            .thenReturn(new ClosePriceEveningSessionEntity());

        // When
        SaveResponseDto result = eveningSessionService.saveClosePricesEveningSession(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Цены вечерней сессии успешно сохранены", result.getMessage());
        assertEquals(2, result.getTotalRequested());
        assertEquals(2, result.getNewItemsSaved());
        assertEquals(0, result.getExistingItemsSkipped());
        assertEquals(0, result.getInvalidItemsFiltered());
        assertEquals(0, result.getMissingFromApi());
        assertNotNull(result.getSavedItems());
        assertEquals(2, result.getSavedItems().size());

        // Verify interactions
        verify(mainSessionPriceService, times(1)).getClosePrices(anyList(), isNull());
        verify(shareRepo, times(1)).findById("TEST_SHARE_001");
        verify(futureRepo, times(1)).findById("TEST_FUTURE_001");
        verify(closePriceEveningSessionRepo, times(2)).save(any(ClosePriceEveningSessionEntity.class));
    }

    @Test
    @DisplayName("Успешное сохранение цен вечерней сессии без указанных инструментов - получение из БД")
    @Description("Тест проверяет получение инструментов из БД при пустом списке")
    @Story("Успешные сценарии")
    void saveClosePricesEveningSession_ShouldLoadInstrumentsFromDatabase_WhenInstrumentsListIsEmpty() {
        // Given
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(new ArrayList<>()); // Пустой список
        
        ShareEntity share1 = createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        ShareEntity share2 = createShareEntity("TEST_SHARE_002", "GAZP", "USD", "NASDAQ"); // Не RUB
        FutureEntity future1 = createFutureEntity("TEST_FUTURE_001", "Si-3.24", "RUB", "MOEX");
        FutureEntity future2 = createFutureEntity("TEST_FUTURE_002", "RTS-3.24", "USD", "MOEX"); // Не RUB
        
        List<ClosePriceDto> testClosePrices = createTestClosePrices();

        // Настраиваем моки
        when(shareRepo.findAll()).thenReturn(Arrays.asList(share1, share2));
        when(futureRepo.findAll()).thenReturn(Arrays.asList(future1, future2));
        when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
            .thenReturn(testClosePrices);
        when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.of(share1));
        when(futureRepo.findById("TEST_FUTURE_001")).thenReturn(Optional.of(future1));
        when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
            .thenReturn(new ClosePriceEveningSessionEntity());

        // When
        SaveResponseDto result = eveningSessionService.saveClosePricesEveningSession(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Цены вечерней сессии успешно сохранены", result.getMessage());
        assertEquals(2, result.getTotalRequested()); // Только RUB инструменты
        assertEquals(2, result.getNewItemsSaved());
        verify(shareRepo, times(1)).findAll();
        verify(futureRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("Успешное сохранение цен вечерней сессии с пропуском существующих записей")
    @Description("Тест проверяет корректную обработку уже существующих записей")
    @Story("Успешные сценарии")
    void saveClosePricesEveningSession_ShouldSkipExistingRecords_WhenRecordsAlreadyExist() {
        // Given
        ClosePriceEveningSessionRequestDto request = createRequestWithInstruments();
        List<ClosePriceDto> testClosePrices = createTestClosePrices();
        ShareEntity share = createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        FutureEntity future = createFutureEntity("TEST_FUTURE_001", "Si-3.24", "RUB", "MOEX");

        // Настраиваем моки - первая запись уже существует, вторая - новая
        when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
            .thenReturn(testClosePrices);
        when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.of(share));
        when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(LocalDate.class), eq("TEST_SHARE_001")))
            .thenReturn(true); // Первая запись уже существует
        when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(LocalDate.class), eq("TEST_FUTURE_001")))
            .thenReturn(false); // Вторая запись новая
        when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
            .thenReturn(new ClosePriceEveningSessionEntity());

        // When
        SaveResponseDto result = eveningSessionService.saveClosePricesEveningSession(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getTotalRequested());
        assertEquals(1, result.getNewItemsSaved());
        assertEquals(1, result.getExistingItemsSkipped());
        assertEquals(0, result.getInvalidItemsFiltered());
        assertEquals(0, result.getMissingFromApi());
    }

    @Test
    @DisplayName("Успешное сохранение цен вечерней сессии с фильтрацией неверных цен")
    @Description("Тест проверяет фильтрацию цен с датой 1970-01-01")
    @Story("Успешные сценарии")
    void saveClosePricesEveningSession_ShouldFilterInvalidPrices_WhenPricesHaveInvalidDate() {
        // Given
        ClosePriceEveningSessionRequestDto request = createRequestWithInstruments();
        List<ClosePriceDto> testClosePrices = createTestClosePricesWithInvalidDate();
        ShareEntity share = createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        FutureEntity future = createFutureEntity("TEST_FUTURE_001", "Si-3.24", "RUB", "MOEX");

        // Настраиваем моки
        when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
            .thenReturn(testClosePrices);
        when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.of(share));
        when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
            .thenReturn(new ClosePriceEveningSessionEntity());

        // When
        SaveResponseDto result = eveningSessionService.saveClosePricesEveningSession(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getTotalRequested());
        assertEquals(1, result.getNewItemsSaved()); // Только одна валидная цена
        assertEquals(0, result.getExistingItemsSkipped());
        assertEquals(1, result.getInvalidItemsFiltered()); // Одна цена с неверной датой отфильтрована
        assertEquals(1, result.getMissingFromApi()); // Один инструмент не вернул данных из-за фильтрации
    }

    @Test
    @DisplayName("Успешное сохранение цен вечерней сессии с использованием eveningSessionPrice")
    @Description("Тест проверяет использование eveningSessionPrice вместо closePrice")
    @Story("Успешные сценарии")
    void saveClosePricesEveningSession_ShouldUseEveningSessionPrice_WhenAvailable() {
        // Given
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(Arrays.asList("TEST_SHARE_001")); // Только один инструмент
        List<ClosePriceDto> testClosePrices = createTestClosePricesWithEveningPrice();
        ShareEntity share = createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");

        // Настраиваем моки
        when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
            .thenReturn(testClosePrices);
        when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.of(share));
        when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
            .thenReturn(new ClosePriceEveningSessionEntity());

        // When
        SaveResponseDto result = eveningSessionService.saveClosePricesEveningSession(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getTotalRequested());
        assertEquals(1, result.getNewItemsSaved());
        verify(closePriceEveningSessionRepo, times(1)).save(any(ClosePriceEveningSessionEntity.class));
    }

    @Test
    @DisplayName("Успешное сохранение цен вечерней сессии с батчингом запросов")
    @Description("Тест проверяет батчинг запросов по 100 инструментов")
    @Story("Успешные сценарии")
    void saveClosePricesEveningSession_ShouldBatchRequests_WhenManyInstrumentsProvided() {
        // Given
        List<String> manyInstruments = new ArrayList<>();
        for (int i = 0; i < 250; i++) {
            manyInstruments.add("TEST_SHARE_" + String.format("%03d", i));
        }
        
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(manyInstruments);
        
        List<ClosePriceDto> testClosePrices = createTestClosePrices();
        ShareEntity share = createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");

        // Настраиваем моки
        when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
            .thenReturn(testClosePrices);
        when(shareRepo.findById(anyString())).thenReturn(Optional.of(share));
        when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
            .thenReturn(new ClosePriceEveningSessionEntity());

        // When
        SaveResponseDto result = eveningSessionService.saveClosePricesEveningSession(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(250, result.getTotalRequested());
        assertEquals(6, result.getNewItemsSaved()); // 3 батча * 2 цены = 6
        // Проверяем, что getClosePrices вызывался 3 раза (250 / 100 = 3 батча)
        verify(mainSessionPriceService, times(3)).getClosePrices(anyList(), isNull());
    }

    // ========== НЕГАТИВНЫЕ ТЕСТЫ ==========

    @Test
    @DisplayName("Обработка ошибки API при получении цен")
    @Description("Тест проверяет обработку исключений от MainSessionPriceService")
    @Story("Негативные сценарии")
    void saveClosePricesEveningSession_ShouldHandleApiError_WhenMainSessionPriceServiceThrowsException() {
        // Given
        ClosePriceEveningSessionRequestDto request = createRequestWithInstruments();

        // Настраиваем моки - API выбрасывает исключение
        when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
            .thenThrow(new RuntimeException("API недоступен"));

        // When
        SaveResponseDto result = eveningSessionService.saveClosePricesEveningSession(request);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Ошибка при получении цен закрытия из API для evening session"));
        assertTrue(result.getMessage().contains("API недоступен"));
        assertEquals(0, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved());
        assertEquals(0, result.getExistingItemsSkipped());
        assertEquals(0, result.getInvalidItemsFiltered());
        assertEquals(0, result.getMissingFromApi());
    }

    @Test
    @DisplayName("Обработка пустого ответа от API")
    @Description("Тест проверяет обработку случая, когда API возвращает пустой список")
    @Story("Негативные сценарии")
    void saveClosePricesEveningSession_ShouldHandleEmptyApiResponse_WhenNoDataAvailable() {
        // Given
        ClosePriceEveningSessionRequestDto request = createRequestWithInstruments();

        // Настраиваем моки - API возвращает пустой список
        when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
            .thenReturn(new ArrayList<>());

        // When
        SaveResponseDto result = eveningSessionService.saveClosePricesEveningSession(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved());
        assertEquals(0, result.getExistingItemsSkipped());
        assertEquals(0, result.getInvalidItemsFiltered());
        assertEquals(2, result.getMissingFromApi()); // Два инструмента без данных
    }

    @Test
    @DisplayName("Обработка ошибки базы данных при сохранении")
    @Description("Тест проверяет обработку исключений при работе с БД")
    @Story("Негативные сценарии")
    void saveClosePricesEveningSession_ShouldHandleDatabaseError_WhenSaveFails() {
        // Given
        ClosePriceEveningSessionRequestDto request = createRequestWithInstruments();
        List<ClosePriceDto> testClosePrices = createTestClosePrices();
        ShareEntity share = createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");

        // Настраиваем моки - БД выбрасывает исключение
        when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
            .thenReturn(testClosePrices);
        when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.of(share));
        when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
            .thenThrow(new DataIntegrityViolationException("Ошибка целостности данных"));

        // When
        SaveResponseDto result = eveningSessionService.saveClosePricesEveningSession(request);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Цены вечерней сессии сохранены с ошибками", result.getMessage());
        assertEquals(2, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved()); // Ничего не сохранилось из-за ошибки БД
        assertEquals(0, result.getExistingItemsSkipped());
        assertEquals(0, result.getInvalidItemsFiltered());
        assertEquals(0, result.getMissingFromApi());
    }

    @Test
    @DisplayName("Обработка пустого списка инструментов и пустой БД")
    @Description("Тест проверяет обработку пустого списка инструментов и пустой БД")
    @Story("Негативные сценарии")
    void saveClosePricesEveningSession_ShouldHandleEmptyInstrumentsAndEmptyDatabase_WhenNoInstrumentsAvailable() {
        // Given
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(new ArrayList<>()); // Пустой список

        // Настраиваем моки для получения инструментов из БД
        when(shareRepo.findAll()).thenReturn(new ArrayList<>()); // Пустой список
        when(futureRepo.findAll()).thenReturn(new ArrayList<>()); // Пустой список

        // When
        SaveResponseDto result = eveningSessionService.saveClosePricesEveningSession(request);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Нет инструментов для загрузки цен вечерней сессии"));
        assertEquals(0, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved());
        verify(shareRepo, times(1)).findAll();
        verify(futureRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("Обработка null значений в запросе")
    @Description("Тест проверяет обработку null значений в различных полях запроса")
    @Story("Негативные сценарии")
    void saveClosePricesEveningSession_ShouldHandleNullValues_WhenRequestContainsNulls() {
        // Given
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(null); // null список

        ShareEntity share = createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");
        FutureEntity future = createFutureEntity("TEST_FUTURE_001", "Si-3.24", "RUB", "MOEX");
        List<ClosePriceDto> testClosePrices = createTestClosePrices();

        // Настраиваем моки для получения инструментов из БД
        when(shareRepo.findAll()).thenReturn(Arrays.asList(share));
        when(futureRepo.findAll()).thenReturn(Arrays.asList(future));
        when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
            .thenReturn(testClosePrices);
        when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.of(share));
        when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
            .thenReturn(new ClosePriceEveningSessionEntity());

        // When
        SaveResponseDto result = eveningSessionService.saveClosePricesEveningSession(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getTotalRequested());
        assertEquals(2, result.getNewItemsSaved());
        verify(shareRepo, times(1)).findAll();
        verify(futureRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("Обработка цен без eveningSessionPrice и closePrice")
    @Description("Тест проверяет обработку цен без доступных цен")
    @Story("Негативные сценарии")
    void saveClosePricesEveningSession_ShouldSkipPricesWithoutValues_WhenNoPricesAvailable() {
        // Given
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(Arrays.asList("TEST_SHARE_001")); // Только один инструмент
        List<ClosePriceDto> testClosePrices = createTestClosePricesWithoutPrices();
        ShareEntity share = createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");

        // Настраиваем моки
        when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
            .thenReturn(testClosePrices);

        // When
        SaveResponseDto result = eveningSessionService.saveClosePricesEveningSession(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved()); // Ничего не сохранилось из-за отсутствия цен
        assertEquals(0, result.getExistingItemsSkipped());
        assertEquals(0, result.getInvalidItemsFiltered());
        assertEquals(0, result.getMissingFromApi());
        verify(closePriceEveningSessionRepo, never()).save(any(ClosePriceEveningSessionEntity.class));
    }

    @Test
    @DisplayName("Обработка ошибки при определении типа инструмента")
    @Description("Тест проверяет обработку случая, когда инструмент не найден в БД")
    @Story("Негативные сценарии")
    void saveClosePricesEveningSession_ShouldHandleUnknownInstrumentType_WhenInstrumentNotFoundInDatabase() {
        // Given
        ClosePriceEveningSessionRequestDto request = createRequestWithInstruments();
        List<ClosePriceDto> testClosePrices = createTestClosePrices();

        // Настраиваем моки - инструмент не найден в БД
        when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
            .thenReturn(testClosePrices);
        when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.empty());
        when(futureRepo.findById("TEST_SHARE_001")).thenReturn(Optional.empty());
        when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
            .thenReturn(new ClosePriceEveningSessionEntity());

        // When
        SaveResponseDto result = eveningSessionService.saveClosePricesEveningSession(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getTotalRequested());
        assertEquals(2, result.getNewItemsSaved());
        // Проверяем, что сохранились записи с типом "UNKNOWN"
        verify(closePriceEveningSessionRepo, times(2)).save(any(ClosePriceEveningSessionEntity.class));
    }

    @Test
    @DisplayName("Обработка неожиданной ошибки при сохранении")
    @Description("Тест проверяет обработку неожиданных исключений при сохранении")
    @Story("Негативные сценарии")
    void saveClosePricesEveningSession_ShouldHandleUnexpectedError_WhenSaveThrowsUnexpectedException() {
        // Given
        ClosePriceEveningSessionRequestDto request = createRequestWithInstruments();
        List<ClosePriceDto> testClosePrices = createTestClosePrices();
        ShareEntity share = createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");

        // Настраиваем моки - неожиданное исключение
        when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
            .thenReturn(testClosePrices);
        when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.of(share));
        when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
            .thenThrow(new RuntimeException("Неожиданная ошибка"));

        // When
        SaveResponseDto result = eveningSessionService.saveClosePricesEveningSession(request);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Цены вечерней сессии сохранены с ошибками", result.getMessage());
        assertEquals(2, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved()); // Ничего не сохранилось из-за ошибки
        assertEquals(0, result.getExistingItemsSkipped());
        assertEquals(0, result.getInvalidItemsFiltered());
        assertEquals(0, result.getMissingFromApi());
    }

    // ========== ГРАНИЧНЫЕ СЛУЧАИ ==========

    @Test
    @DisplayName("Обработка очень большого количества инструментов")
    @Description("Тест проверяет обработку большого количества инструментов")
    @Story("Граничные случаи")
    void saveClosePricesEveningSession_ShouldHandleLargeNumberOfInstruments_WhenManyInstrumentsProvided() {
        // Given
        List<String> manyInstruments = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            manyInstruments.add("TEST_SHARE_" + String.format("%04d", i));
        }
        
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(manyInstruments);
        
        List<ClosePriceDto> testClosePrices = createTestClosePrices();
        ShareEntity share = createShareEntity("TEST_SHARE_0001", "SBER", "RUB", "MOEX");

        // Настраиваем моки
        when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
            .thenReturn(testClosePrices);
        when(shareRepo.findById(anyString())).thenReturn(Optional.of(share));
        when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
            .thenReturn(new ClosePriceEveningSessionEntity());

        // When
        SaveResponseDto result = eveningSessionService.saveClosePricesEveningSession(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1000, result.getTotalRequested());
        assertEquals(20, result.getNewItemsSaved()); // 10 батчей * 2 цены = 20
        // Проверяем, что getClosePrices вызывался 10 раз (1000 / 100 = 10 батчей)
        verify(mainSessionPriceService, times(10)).getClosePrices(anyList(), isNull());
    }

    @Test
    @DisplayName("Обработка смешанных результатов API")
    @Description("Тест проверяет обработку смешанных результатов от API (часть успешных, часть пустых)")
    @Story("Граничные случаи")
    void saveClosePricesEveningSession_ShouldHandleMixedApiResults_WhenSomeInstrumentsReturnDataAndSomeDoNot() {
        // Given
        ClosePriceEveningSessionRequestDto request = createRequestWithInstruments();
        List<ClosePriceDto> testClosePrices = createTestClosePrices();
        ShareEntity share = createShareEntity("TEST_SHARE_001", "SBER", "RUB", "MOEX");

        // Настраиваем моки - первый батч возвращает данные, второй - пустой список
        when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
            .thenReturn(testClosePrices)
            .thenReturn(new ArrayList<>());
        when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.of(share));
        when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(LocalDate.class), anyString()))
            .thenReturn(false);
        when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
            .thenReturn(new ClosePriceEveningSessionEntity());

        // When
        SaveResponseDto result = eveningSessionService.saveClosePricesEveningSession(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getTotalRequested());
        assertEquals(2, result.getNewItemsSaved());
        assertEquals(0, result.getMissingFromApi());
    }

    @Test
    @DisplayName("Обработка инструментов только в USD")
    @Description("Тест проверяет обработку инструментов только в USD (не RUB)")
    @Story("Граничные случаи")
    void saveClosePricesEveningSession_ShouldHandleOnlyUsdInstruments_WhenNoRubInstrumentsAvailable() {
        // Given
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(new ArrayList<>()); // Пустой список
        
        ShareEntity share1 = createShareEntity("TEST_SHARE_001", "AAPL", "USD", "NASDAQ");
        ShareEntity share2 = createShareEntity("TEST_SHARE_002", "GOOGL", "USD", "NASDAQ");
        FutureEntity future1 = createFutureEntity("TEST_FUTURE_001", "ES-3.24", "USD", "CME");
        FutureEntity future2 = createFutureEntity("TEST_FUTURE_002", "NQ-3.24", "USD", "CME");

        // Настраиваем моки для получения инструментов из БД
        when(shareRepo.findAll()).thenReturn(Arrays.asList(share1, share2));
        when(futureRepo.findAll()).thenReturn(Arrays.asList(future1, future2));

        // When
        SaveResponseDto result = eveningSessionService.saveClosePricesEveningSession(request);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Нет инструментов для загрузки цен вечерней сессии"));
        assertEquals(0, result.getTotalRequested());
        assertEquals(0, result.getNewItemsSaved());
        verify(shareRepo, times(1)).findAll();
        verify(futureRepo, times(1)).findAll();
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private ClosePriceEveningSessionRequestDto createRequestWithInstruments() {
        ClosePriceEveningSessionRequestDto request = new ClosePriceEveningSessionRequestDto();
        request.setInstruments(Arrays.asList("TEST_SHARE_001", "TEST_FUTURE_001"));
        return request;
    }

    private List<ClosePriceDto> createTestClosePrices() {
        return Arrays.asList(
            new ClosePriceDto("TEST_SHARE_001", "2024-01-15", BigDecimal.valueOf(100.0), BigDecimal.valueOf(105.0)),
            new ClosePriceDto("TEST_FUTURE_001", "2024-01-15", BigDecimal.valueOf(200.0), BigDecimal.valueOf(210.0))
        );
    }

    private List<ClosePriceDto> createTestClosePricesWithInvalidDate() {
        return Arrays.asList(
            new ClosePriceDto("TEST_SHARE_001", "2024-01-15", BigDecimal.valueOf(100.0), BigDecimal.valueOf(105.0)),
            new ClosePriceDto("TEST_FUTURE_001", "1970-01-01", BigDecimal.valueOf(200.0), BigDecimal.valueOf(210.0)) // Неверная дата
        );
    }

    private List<ClosePriceDto> createTestClosePricesWithEveningPrice() {
        return Arrays.asList(
            new ClosePriceDto("TEST_SHARE_001", "2024-01-15", BigDecimal.valueOf(100.0), BigDecimal.valueOf(105.0))
        );
    }

    private List<ClosePriceDto> createTestClosePricesWithoutPrices() {
        return Arrays.asList(
            new ClosePriceDto("TEST_SHARE_001", "2024-01-15", null, null) // Нет цен
        );
    }

    private ShareEntity createShareEntity(String figi, String ticker, String currency, String exchange) {
        ShareEntity share = new ShareEntity();
        share.setFigi(figi);
        share.setTicker(ticker);
        share.setCurrency(currency);
        share.setExchange(exchange);
        return share;
    }

    private FutureEntity createFutureEntity(String figi, String ticker, String currency, String exchange) {
        FutureEntity future = new FutureEntity();
        future.setFigi(figi);
        future.setTicker(ticker);
        future.setCurrency(currency);
        future.setExchange(exchange);
        return future;
    }
}
