package com.example.InvestmentDataLoaderService.unit.service;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;
import com.example.InvestmentDataLoaderService.fixtures.TestDataFactory;
import com.example.InvestmentDataLoaderService.repository.*;
import com.example.InvestmentDataLoaderService.service.EveningSessionService;
import com.example.InvestmentDataLoaderService.service.MainSessionPriceService;

import io.qameta.allure.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
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
    @Step("Инициализация тестового окружения")
    @DisplayName("Инициализация тестового окружения")
    @Description("Сброс всех моков перед каждым тестом")
    void setUp() {
        reset(mainSessionPriceService, shareRepo, futureRepo, closePriceEveningSessionRepo);
    }

    // ========== ПОЗИТИВНЫЕ ТЕСТЫ ==========

    @Test
    @DisplayName("Успешное сохранение цен вечерней сессии с указанными инструментами")
    @Description("Тест проверяет основную функциональность сохранения цен вечерней сессии с корректными данными")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("positive")
    @Tag("evening-session")
    @Tag("success")
    @Tag("unit")
    @Tag("service")
    @Tag("evening-session")
    void saveClosePricesEveningSession_ShouldReturnSuccessResponse_WhenInstrumentsProvided() {
        
        // Шаг 1: Подготовка тестовых данных
        ClosePriceEveningSessionRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createEveningSessionRequestDto();
        });
        List<ClosePriceDto> testClosePrices = TestDataFactory.createEveningSessionClosePriceDtoList();
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "TESTMOEX");

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
                .thenReturn(testClosePrices);
            when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.of(share));
            when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(), anyString()))
                .thenReturn(false);
            when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
                .thenReturn(new ClosePriceEveningSessionEntity());
        });

        // Шаг 3: Выполнение операции
        SaveResponseDto result = Allure.step("Выполнение операции", () -> {
            return eveningSessionService.saveClosePricesEveningSession(request);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
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
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(mainSessionPriceService, times(1)).getClosePrices(anyList(), isNull());
            verify(shareRepo, times(1)).findById("TEST_SHARE_001");
            verify(futureRepo, times(1)).findById("TEST_FUTURE_001");
            verify(closePriceEveningSessionRepo, times(2)).save(any(ClosePriceEveningSessionEntity.class));
        });
    }

    @Test
    @DisplayName("Успешное сохранение цен вечерней сессии без указанных инструментов - получение из БД")
    @Description("Тест проверяет получение инструментов из БД при пустом списке")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("positive")
    @Tag("evening-session")
    @Tag("database")
    @Tag("unit")
    @Tag("service")
    @Tag("auto-instruments")
    void saveClosePricesEveningSession_ShouldLoadInstrumentsFromDatabase_WhenInstrumentsListIsEmpty() {
        
        // Шаг 1: Подготовка тестовых данных
        ClosePriceEveningSessionRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createEmptyInstrumentsEveningSessionRequest();
        });
        
        ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "TESTMOEX");
        ShareEntity share2 = TestDataFactory.createShareEntity("TEST_SHARE_002", "GAZP", "Газпром", "TESTNASDAQ"); // Не RUB
        FutureEntity future1 = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "Si-3.24", "FUTURES");
        FutureEntity future2 = TestDataFactory.createFutureEntity("TEST_FUTURE_002", "RTS-3.24", "FUTURES"); // Не RUB
        
        List<ClosePriceDto> testClosePrices = TestDataFactory.createEveningSessionClosePriceDtoList();

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(shareRepo.findAll()).thenReturn(Arrays.asList(share1, share2));
            when(futureRepo.findAll()).thenReturn(Arrays.asList(future1, future2));
            when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
                .thenReturn(testClosePrices);
            when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.of(share1));
            when(futureRepo.findById("TEST_FUTURE_001")).thenReturn(Optional.of(future1));
            when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(), anyString()))
                .thenReturn(false);
            when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
                .thenReturn(new ClosePriceEveningSessionEntity());
        });

        // Шаг 3: Выполнение операции
        SaveResponseDto result = Allure.step("Выполнение операции", () -> {
            return eveningSessionService.saveClosePricesEveningSession(request);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("Цены вечерней сессии успешно сохранены", result.getMessage());
            assertEquals(4, result.getTotalRequested()); // Все инструменты (2 shares + 2 futures)
            assertEquals(2, result.getNewItemsSaved()); // Только 2 цены из API
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(shareRepo, times(1)).findAll();
            verify(futureRepo, times(1)).findAll();
        });
    }

    @Test
    @DisplayName("Успешное сохранение цен вечерней сессии с пропуском существующих записей")
    @Description("Тест проверяет корректную обработку уже существующих записей")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("positive")
    @Tag("evening-session")
    @Tag("existing")
    @Tag("unit")
    @Tag("service")
    @Tag("duplicate-handling")
    void saveClosePricesEveningSession_ShouldSkipExistingRecords_WhenRecordsAlreadyExist() {
        
        // Шаг 1: Подготовка тестовых данных
        ClosePriceEveningSessionRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createEveningSessionRequestDto();
        });
        List<ClosePriceDto> testClosePrices = TestDataFactory.createEveningSessionClosePriceDtoList();
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "TESTMOEX");

        // Шаг 2: Настройка моков - первая запись уже существует, вторая - новая
        Allure.step("Настройка моков - первая запись уже существует, вторая - новая", () -> {
            when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
                .thenReturn(testClosePrices);
            when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.of(share));
            when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(), eq("TEST_SHARE_001")))
                .thenReturn(true); // Первая запись уже существует
            when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(), eq("TEST_FUTURE_001")))
                .thenReturn(false); // Вторая запись новая
            when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
                .thenReturn(new ClosePriceEveningSessionEntity());
        });

        // Шаг 3: Выполнение операции
        SaveResponseDto result = Allure.step("Выполнение операции", () -> {
            return eveningSessionService.saveClosePricesEveningSession(request);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getTotalRequested());
            assertEquals(1, result.getNewItemsSaved());
            assertEquals(1, result.getExistingItemsSkipped());
            assertEquals(0, result.getInvalidItemsFiltered());
            assertEquals(0, result.getMissingFromApi());
        });
    }

    @Test
    @DisplayName("Успешное сохранение цен вечерней сессии с фильтрацией неверных цен")
    @Description("Тест проверяет фильтрацию цен с датой 1970-01-01")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("positive")
    @Tag("evening-session")
    @Tag("filter")
    @Tag("unit")
    @Tag("service")
    @Tag("data-validation")
    void saveClosePricesEveningSession_ShouldFilterInvalidPrices_WhenPricesHaveInvalidDate() {
        
        // Шаг 1: Подготовка тестовых данных
        ClosePriceEveningSessionRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createEveningSessionRequestDto();
        });
        List<ClosePriceDto> testClosePrices = TestDataFactory.createInvalidDateEveningSessionClosePriceDtoList();
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "TESTMOEX");

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
                .thenReturn(testClosePrices);
            when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.of(share));
            when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(), anyString()))
                .thenReturn(false);
            when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
                .thenReturn(new ClosePriceEveningSessionEntity());
        });

        // Шаг 3: Выполнение операции
        SaveResponseDto result = Allure.step("Выполнение операции", () -> {
            return eveningSessionService.saveClosePricesEveningSession(request);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getTotalRequested());
            assertEquals(1, result.getNewItemsSaved()); // Только одна валидная цена
            assertEquals(0, result.getExistingItemsSkipped());
            assertEquals(1, result.getInvalidItemsFiltered()); // Одна цена с неверной датой отфильтрована
            assertEquals(1, result.getMissingFromApi()); // Один инструмент не вернул данных из-за фильтрации
        });
    }

    @Test
    @DisplayName("Успешное сохранение цен вечерней сессии с использованием eveningSessionPrice")
    @Description("Тест проверяет использование eveningSessionPrice вместо closePrice")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("positive")
    @Tag("evening-session")
    @Tag("evening-price")
    @Tag("unit")
    @Tag("service")
    @Tag("price-priority")
    void saveClosePricesEveningSession_ShouldUseEveningSessionPrice_WhenAvailable() {
        
        // Шаг 1: Подготовка тестовых данных
        ClosePriceEveningSessionRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createEveningSessionRequestDto(
                Arrays.asList("TEST_SHARE_001")
            );
        });
        List<ClosePriceDto> testClosePrices = TestDataFactory.createEveningPriceClosePriceDtoList();
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "TESTMOEX");

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
                .thenReturn(testClosePrices);
            when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.of(share));
            when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(), anyString()))
                .thenReturn(false);
            when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
                .thenReturn(new ClosePriceEveningSessionEntity());
        });

        // Шаг 3: Выполнение операции
        SaveResponseDto result = Allure.step("Выполнение операции", () -> {
            return eveningSessionService.saveClosePricesEveningSession(request);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getTotalRequested());
            assertEquals(1, result.getNewItemsSaved());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(closePriceEveningSessionRepo, times(1)).save(any(ClosePriceEveningSessionEntity.class));
        });
    }

    @Test
    @DisplayName("Успешное сохранение цен вечерней сессии с батчингом запросов")
    @Description("Тест проверяет батчинг запросов по 100 инструментов")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("positive")
    @Tag("evening-session")
    @Tag("batching")
    @Tag("unit")
    @Tag("service")
    @Tag("batch-processing")
    void saveClosePricesEveningSession_ShouldBatchRequests_WhenManyInstrumentsProvided() {
        
        // Шаг 1: Подготовка тестовых данных
        ClosePriceEveningSessionRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createLargeInstrumentsEveningSessionRequest();
        });
        
        List<ClosePriceDto> testClosePrices = TestDataFactory.createEveningSessionClosePriceDtoList();
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "TESTMOEX");

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
                .thenReturn(testClosePrices);
            when(shareRepo.findById(anyString())).thenReturn(Optional.of(share));
            when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(), anyString()))
                .thenReturn(false);
            when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
                .thenReturn(new ClosePriceEveningSessionEntity());
        });

        // Шаг 3: Выполнение операции
        SaveResponseDto result = Allure.step("Выполнение операции", () -> {
            return eveningSessionService.saveClosePricesEveningSession(request);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(250, result.getTotalRequested());
            assertEquals(6, result.getNewItemsSaved()); // 3 батча * 2 цены = 6
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            // Проверяем, что getClosePrices вызывался 3 раза (250 / 100 = 3 батча)
            verify(mainSessionPriceService, times(3)).getClosePrices(anyList(), isNull());
        });
    }

    // ========== НЕГАТИВНЫЕ ТЕСТЫ ==========

    @Test
    @DisplayName("Обработка ошибки API при получении цен")
    @Description("Тест проверяет обработку исключений от MainSessionPriceService")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("evening-session")
    @Tag("api-error")
    @Tag("unit")
    @Tag("service")
    @Tag("error-handling")
    void saveClosePricesEveningSession_ShouldHandleApiError_WhenMainSessionPriceServiceThrowsException() {
        
        // Шаг 1: Подготовка тестовых данных
        ClosePriceEveningSessionRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createEveningSessionRequestDto();
        });

        // Шаг 2: Настройка моков - API выбрасывает исключение
        Allure.step("Настройка моков - API выбрасывает исключение", () -> {
            when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
                .thenThrow(new RuntimeException("API недоступен"));
        });

        // Шаг 3: Выполнение операции
        SaveResponseDto result = Allure.step("Выполнение операции", () -> {
            return eveningSessionService.saveClosePricesEveningSession(request);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMessage().contains("Ошибка при получении цен закрытия из API для evening session"));
            assertTrue(result.getMessage().contains("API недоступен"));
            assertEquals(0, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved());
            assertEquals(0, result.getExistingItemsSkipped());
            assertEquals(0, result.getInvalidItemsFiltered());
            assertEquals(0, result.getMissingFromApi());
        });
    }

    @Test
    @DisplayName("Обработка пустого ответа от API")
    @Description("Тест проверяет обработку случая, когда API возвращает пустой список")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("evening-session")
    @Tag("empty-response")
    @Tag("unit")
    @Tag("service")
    @Tag("no-data")
    void saveClosePricesEveningSession_ShouldHandleEmptyApiResponse_WhenNoDataAvailable() {
        
        // Шаг 1: Подготовка тестовых данных
        ClosePriceEveningSessionRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createEveningSessionRequestDto();
        });

        // Шаг 2: Настройка моков - API возвращает пустой список
        Allure.step("Настройка моков - API возвращает пустой список", () -> {
            when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
                .thenReturn(TestDataFactory.createEmptyEveningSessionClosePriceDtoList());
        });

        // Шаг 3: Выполнение операции
        SaveResponseDto result = Allure.step("Выполнение операции", () -> {
            return eveningSessionService.saveClosePricesEveningSession(request);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved());
            assertEquals(0, result.getExistingItemsSkipped());
            assertEquals(0, result.getInvalidItemsFiltered());
            assertEquals(2, result.getMissingFromApi()); // Два инструмента без данных
        });
    }

    @Test
    @DisplayName("Обработка ошибки базы данных при сохранении")
    @Description("Тест проверяет обработку исключений при работе с БД")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("evening-session")
    @Tag("database-error")
    @Tag("unit")
    @Tag("service")
    @Tag("persistence-error")
    void saveClosePricesEveningSession_ShouldHandleDatabaseError_WhenSaveFails() {
        
        // Шаг 1: Подготовка тестовых данных
        ClosePriceEveningSessionRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createEveningSessionRequestDto();
        });
        List<ClosePriceDto> testClosePrices = TestDataFactory.createEveningSessionClosePriceDtoList();
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "TESTMOEX");

        // Шаг 2: Настройка моков - БД выбрасывает исключение
        Allure.step("Настройка моков - БД выбрасывает исключение", () -> {
            when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
                .thenReturn(testClosePrices);
            when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.of(share));
            when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(), anyString()))
                .thenReturn(false);
            when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
                .thenThrow(new DataIntegrityViolationException("Ошибка целостности данных"));
        });

        // Шаг 3: Выполнение операции
        SaveResponseDto result = Allure.step("Выполнение операции", () -> {
            return eveningSessionService.saveClosePricesEveningSession(request);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertEquals("Цены вечерней сессии сохранены с ошибками", result.getMessage());
            assertEquals(2, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved()); // Ничего не сохранилось из-за ошибки БД
            assertEquals(0, result.getExistingItemsSkipped());
            assertEquals(0, result.getInvalidItemsFiltered());
            assertEquals(0, result.getMissingFromApi());
        });
    }

    @Test
    @DisplayName("Обработка пустого списка инструментов и пустой БД")
    @Description("Тест проверяет обработку пустого списка инструментов и пустой БД")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("evening-session")
    @Tag("empty-database")
    @Tag("unit")
    @Tag("service")
    @Tag("no-instruments")
    void saveClosePricesEveningSession_ShouldHandleEmptyInstrumentsAndEmptyDatabase_WhenNoInstrumentsAvailable() {
        
        // Шаг 1: Подготовка тестовых данных
        ClosePriceEveningSessionRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createEmptyInstrumentsEveningSessionRequest();
        });

        // Шаг 2: Настройка моков для получения инструментов из БД
        Allure.step("Настройка моков для получения инструментов из БД", () -> {
            when(shareRepo.findAll()).thenReturn(Arrays.asList()); // Пустой список
            when(futureRepo.findAll()).thenReturn(Arrays.asList()); // Пустой список
        });

        // Шаг 3: Выполнение операции
        SaveResponseDto result = Allure.step("Выполнение операции", () -> {
            return eveningSessionService.saveClosePricesEveningSession(request);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMessage().contains("Нет инструментов для загрузки цен вечерней сессии"));
            assertEquals(0, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(shareRepo, times(1)).findAll();
            verify(futureRepo, times(1)).findAll();
        });
    }

    @Test
    @DisplayName("Обработка null значений в запросе")
    @Description("Тест проверяет обработку null значений в различных полях запроса")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("evening-session")
    @Tag("null-values")
    @Tag("unit")
    @Tag("service")
    @Tag("data-validation")
    void saveClosePricesEveningSession_ShouldHandleNullValues_WhenRequestContainsNulls() {
        
        // Шаг 1: Подготовка тестовых данных
        ClosePriceEveningSessionRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createNullValuesEveningSessionRequest();
        });

        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "TESTMOEX");
        FutureEntity future = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "Si-3.24", "FUTURES");
        List<ClosePriceDto> testClosePrices = TestDataFactory.createEveningSessionClosePriceDtoList();

        // Шаг 2: Настройка моков для получения инструментов из БД
        Allure.step("Настройка моков для получения инструментов из БД", () -> {
            when(shareRepo.findAll()).thenReturn(Arrays.asList(share));
            when(futureRepo.findAll()).thenReturn(Arrays.asList(future));
            when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
                .thenReturn(testClosePrices);
            when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.of(share));
            when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(), anyString()))
                .thenReturn(false);
            when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
                .thenReturn(new ClosePriceEveningSessionEntity());
        });

        // Шаг 3: Выполнение операции
        SaveResponseDto result = Allure.step("Выполнение операции", () -> {
            return eveningSessionService.saveClosePricesEveningSession(request);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getTotalRequested());
            assertEquals(2, result.getNewItemsSaved());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(shareRepo, times(1)).findAll();
            verify(futureRepo, times(1)).findAll();
        });
    }

    @Test
    @DisplayName("Обработка цен без eveningSessionPrice и closePrice")
    @Description("Тест проверяет обработку цен без доступных цен")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("evening-session")
    @Tag("no-prices")
    @Tag("unit")
    @Tag("service")
    @Tag("data-validation")
    void saveClosePricesEveningSession_ShouldSkipPricesWithoutValues_WhenNoPricesAvailable() {
        
        // Шаг 1: Подготовка тестовых данных
        ClosePriceEveningSessionRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createEveningSessionRequestDto(
                Arrays.asList("TEST_SHARE_001")
            );
        });
        List<ClosePriceDto> testClosePrices = TestDataFactory.createNoPricesEveningSessionClosePriceDtoList();

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
                .thenReturn(testClosePrices);
        });

        // Шаг 3: Выполнение операции
        SaveResponseDto result = Allure.step("Выполнение операции", () -> {
            return eveningSessionService.saveClosePricesEveningSession(request);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved()); // Ничего не сохранилось из-за отсутствия цен
            assertEquals(0, result.getExistingItemsSkipped());
            assertEquals(0, result.getInvalidItemsFiltered());
            assertEquals(0, result.getMissingFromApi());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(closePriceEveningSessionRepo, never()).save(any(ClosePriceEveningSessionEntity.class));
        });
    }

    @Test
    @DisplayName("Обработка ошибки при определении типа инструмента")
    @Description("Тест проверяет обработку случая, когда инструмент не найден в БД")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("evening-session")
    @Tag("unknown-instrument")
    @Tag("unit")
    @Tag("service")
    @Tag("data-validation")
    void saveClosePricesEveningSession_ShouldHandleUnknownInstrumentType_WhenInstrumentNotFoundInDatabase() {
        
        // Шаг 1: Подготовка тестовых данных
        ClosePriceEveningSessionRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createEveningSessionRequestDto();
        });
        List<ClosePriceDto> testClosePrices = TestDataFactory.createEveningSessionClosePriceDtoList();

        // Шаг 2: Настройка моков - инструмент не найден в БД
        Allure.step("Настройка моков - инструмент не найден в БД", () -> {
            when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
                .thenReturn(testClosePrices);
            when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.empty());
            when(futureRepo.findById("TEST_SHARE_001")).thenReturn(Optional.empty());
            when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(), anyString()))
                .thenReturn(false);
            when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
                .thenReturn(new ClosePriceEveningSessionEntity());
        });

        // Шаг 3: Выполнение операции
        SaveResponseDto result = Allure.step("Выполнение операции", () -> {
            return eveningSessionService.saveClosePricesEveningSession(request);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getTotalRequested());
            assertEquals(2, result.getNewItemsSaved());
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            // Проверяем, что сохранились записи с типом "UNKNOWN"
            verify(closePriceEveningSessionRepo, times(2)).save(any(ClosePriceEveningSessionEntity.class));
        });
    }

    @Test
    @DisplayName("Обработка неожиданной ошибки при сохранении")
    @Description("Тест проверяет обработку неожиданных исключений при сохранении")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("negative")
    @Tag("evening-session")
    @Tag("unexpected-error")
    @Tag("unit")
    @Tag("service")
    @Tag("error-handling")
    void saveClosePricesEveningSession_ShouldHandleUnexpectedError_WhenSaveThrowsUnexpectedException() {
        
        // Шаг 1: Подготовка тестовых данных
        ClosePriceEveningSessionRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createEveningSessionRequestDto();
        });
        List<ClosePriceDto> testClosePrices = TestDataFactory.createEveningSessionClosePriceDtoList();
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "TESTMOEX");

        // Шаг 2: Настройка моков - неожиданное исключение
        Allure.step("Настройка моков - неожиданное исключение", () -> {
            when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
                .thenReturn(testClosePrices);
            when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.of(share));
            when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(), anyString()))
                .thenReturn(false);
            when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
                .thenThrow(new RuntimeException("Неожиданная ошибка"));
        });

        // Шаг 3: Выполнение операции
        SaveResponseDto result = Allure.step("Выполнение операции", () -> {
            return eveningSessionService.saveClosePricesEveningSession(request);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertEquals("Цены вечерней сессии сохранены с ошибками", result.getMessage());
            assertEquals(2, result.getTotalRequested());
            assertEquals(0, result.getNewItemsSaved()); // Ничего не сохранилось из-за ошибки
            assertEquals(0, result.getExistingItemsSkipped());
            assertEquals(0, result.getInvalidItemsFiltered());
            assertEquals(0, result.getMissingFromApi());
        });
    }

    // ========== ГРАНИЧНЫЕ СЛУЧАИ ==========

    @Test
    @DisplayName("Обработка очень большого количества инструментов")
    @Description("Тест проверяет обработку большого количества инструментов")
    @Story("Граничные случаи")
    @Severity(SeverityLevel.NORMAL)
    @Tag("boundary")
    @Tag("evening-session")
    @Tag("large-scale")
    @Tag("unit")
    @Tag("service")
    @Tag("performance")
    void saveClosePricesEveningSession_ShouldHandleLargeNumberOfInstruments_WhenManyInstrumentsProvided() {
        
        // Шаг 1: Подготовка тестовых данных
        ClosePriceEveningSessionRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createVeryLargeInstrumentsEveningSessionRequest();
        });
        
        List<ClosePriceDto> testClosePrices = TestDataFactory.createEveningSessionClosePriceDtoList();
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_0001", "SBER", "Сбербанк", "TESTMOEX");

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
                .thenReturn(testClosePrices);
            when(shareRepo.findById(anyString())).thenReturn(Optional.of(share));
            when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(), anyString()))
                .thenReturn(false);
            when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
                .thenReturn(new ClosePriceEveningSessionEntity());
        });

        // Шаг 3: Выполнение операции
        SaveResponseDto result = Allure.step("Выполнение операции", () -> {
            return eveningSessionService.saveClosePricesEveningSession(request);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1000, result.getTotalRequested());
            assertEquals(20, result.getNewItemsSaved()); // 10 батчей * 2 цены = 20
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            // Проверяем, что getClosePrices вызывался 10 раз (1000 / 100 = 10 батчей)
            verify(mainSessionPriceService, times(10)).getClosePrices(anyList(), isNull());
        });
    }

    @Test
    @DisplayName("Обработка смешанных результатов API")
    @Description("Тест проверяет обработку смешанных результатов от API (часть успешных, часть пустых)")
    @Story("Граничные случаи")
    @Severity(SeverityLevel.NORMAL)
    @Tag("boundary")
    @Tag("evening-session")
    @Tag("mixed-results")
    @Tag("unit")
    @Tag("service")
    @Tag("partial-success")
    void saveClosePricesEveningSession_ShouldHandleMixedApiResults_WhenSomeInstrumentsReturnDataAndSomeDoNot() {
        
        // Шаг 1: Подготовка тестовых данных
        ClosePriceEveningSessionRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createEveningSessionRequestDto();
        });
        List<ClosePriceDto> testClosePrices = TestDataFactory.createEveningSessionClosePriceDtoList();
        ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "TESTMOEX");

        // Шаг 2: Настройка моков - первый батч возвращает данные, второй - пустой список
        Allure.step("Настройка моков - первый батч возвращает данные, второй - пустой список", () -> {
            when(mainSessionPriceService.getClosePrices(anyList(), isNull()))
                .thenReturn(testClosePrices)
                .thenReturn(TestDataFactory.createEmptyEveningSessionClosePriceDtoList());
            when(shareRepo.findById("TEST_SHARE_001")).thenReturn(Optional.of(share));
            when(closePriceEveningSessionRepo.existsByPriceDateAndFigi(any(), anyString()))
                .thenReturn(false);
            when(closePriceEveningSessionRepo.save(any(ClosePriceEveningSessionEntity.class)))
                .thenReturn(new ClosePriceEveningSessionEntity());
        });

        // Шаг 3: Выполнение операции
        SaveResponseDto result = Allure.step("Выполнение операции", () -> {
            return eveningSessionService.saveClosePricesEveningSession(request);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getTotalRequested());
            assertEquals(2, result.getNewItemsSaved());
            assertEquals(0, result.getMissingFromApi());
        });
    }

    @Test
    @DisplayName("Обработка инструментов только в USD")
    @Description("Тест проверяет обработку инструментов только в USD (не RUB)")
    @Story("Граничные случаи")
    @Severity(SeverityLevel.NORMAL)
    @Tag("boundary")
    @Tag("evening-session")
    @Tag("usd-only")
    @Tag("unit")
    @Tag("service")
    @Tag("currency-handling")
    void saveClosePricesEveningSession_ShouldHandleOnlyUsdInstruments_WhenNoRubInstrumentsAvailable() {
        
        // Шаг 1: Подготовка тестовых данных
        ClosePriceEveningSessionRequestDto request = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createEmptyInstrumentsEveningSessionRequest();
        });
        
        ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "AAPL", "Apple Inc", "TESTNASDAQ");
        ShareEntity share2 = TestDataFactory.createShareEntity("TEST_SHARE_002", "GOOGL", "Google Inc", "TESTNASDAQ");
        FutureEntity future1 = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "ES-3.24", "FUTURES");
        FutureEntity future2 = TestDataFactory.createFutureEntity("TEST_FUTURE_002", "NQ-3.24", "FUTURES");

        // Шаг 2: Настройка моков для получения инструментов из БД
        Allure.step("Настройка моков для получения инструментов из БД", () -> {
            when(shareRepo.findAll()).thenReturn(Arrays.asList(share1, share2));
            when(futureRepo.findAll()).thenReturn(Arrays.asList(future1, future2));
        });

        // Шаг 3: Выполнение операции
        SaveResponseDto result = Allure.step("Выполнение операции", () -> {
            return eveningSessionService.saveClosePricesEveningSession(request);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertNotNull(result);
            assertTrue(result.isSuccess()); // Сервис обрабатывает все инструменты, не только RUB
            assertEquals(4, result.getTotalRequested()); // Все инструменты (2 shares + 2 futures)
            assertEquals(0, result.getNewItemsSaved()); // Нет данных от API
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(shareRepo, times(1)).findAll();
            verify(futureRepo, times(1)).findAll();
        });
    }
}