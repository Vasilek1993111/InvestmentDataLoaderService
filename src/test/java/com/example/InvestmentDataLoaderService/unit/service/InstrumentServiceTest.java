package com.example.InvestmentDataLoaderService.unit.service;

import com.example.InvestmentDataLoaderService.client.TinkoffRestClient;
import com.example.InvestmentDataLoaderService.client.TinkoffApiClient;
import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;
import com.example.InvestmentDataLoaderService.fixtures.TestDataFactory;
import com.example.InvestmentDataLoaderService.repository.*;
import com.example.InvestmentDataLoaderService.service.InstrumentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc.InstrumentsServiceBlockingStub;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для InstrumentService
 * 
 * <p>Тестирует бизнес-логику сервиса для работы с финансовыми инструментами:</p>
 * <ul>
 *   <li>Акции (Shares)</li>
 *   <li>Фьючерсы (Futures)</li>
 *   <li>Индикативные инструменты (Indicatives)</li>
 * </ul>
 * 
 * <p>Покрывает основные сценарии:</p>
 * <ul>
 *   <li>Получение инструментов из внешнего API</li>
 *   <li>Получение инструментов из базы данных</li>
 *   <li>Сохранение инструментов в БД</li>
 *   <li>Фильтрация и поиск инструментов</li>
 *   <li>Обработка ошибок и исключений</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@Epic("Instrument Service")
@Feature("Financial Instruments Management")
@DisplayName("Instrument Service Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
class InstrumentServiceTest {

    @Mock
    private InstrumentsServiceBlockingStub instrumentsService;

    @Mock
    private ShareRepository shareRepo;

    @Mock
    private FutureRepository futureRepo;

    @Mock
    private IndicativeRepository indicativeRepo;

    @Mock
    private TinkoffRestClient restClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TinkoffApiClient tinkoffApiClient;

    @InjectMocks
    private InstrumentService instrumentService;

    @BeforeEach
    @Step("Подготовка тестовых данных для InstrumentService")
    @DisplayName("Инициализация тестового окружения")
    @Description("Сброс всех моков перед каждым тестом")
    void setUp() {
        // Очистка моков перед каждым тестом
        reset(instrumentsService, shareRepo, futureRepo, indicativeRepo, restClient, objectMapper, tinkoffApiClient);
    }

    // ==================== HELPER METHODS ====================



    /**
     * Создание тестовых данных для фьючерсов из API
     */
    private FuturesResponse createMockFuturesResponse() {
        Future silver = Future.newBuilder()
                .setFigi("FUTSI0624000")
                .setTicker("SI0624")
                .setAssetType("COMMODITY")
                .setBasicAsset("Silver")
                .setCurrency("USD")
                .setExchange("MOEX")
                .setShortEnabledFlag(true)
                .setExpirationDate(com.google.protobuf.Timestamp.newBuilder()
                    .setSeconds(1719254400) // 2024-06-24 18:40:00 UTC
                    .setNanos(0)
                    .build())
                .build();

        Future gold = Future.newBuilder()
                .setFigi("FUTGZ0624000")
                .setTicker("GZ0624")
                .setAssetType("COMMODITY")
                .setBasicAsset("Gold")
                .setCurrency("USD")
                .setExchange("MOEX")
                .setShortEnabledFlag(true)
                .setExpirationDate(com.google.protobuf.Timestamp.newBuilder()
                    .setSeconds(1719254400) // 2024-06-24 18:40:00 UTC
                    .setNanos(0)
                    .build())
                .build();

        return FuturesResponse.newBuilder()
                .addInstruments(silver)
                .addInstruments(gold)
                .build();
    }



    /**
     * Создание тестового JSON ответа для REST API акций
     */
    private JsonNode createMockSharesJsonResponse() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = """
            {
                "instruments": [
                    {
                        "figi": "BBG004730N88",
                        "ticker": "SBER",
                        "name": "ПАО Сбербанк",
                        "currency": "RUB",
                        "exchange": "MOEX",
                        "sector": "Financial",
                        "tradingStatus": "SECURITY_TRADING_STATUS_NORMAL_TRADING",
                        "shortEnabledFlag": true,
                        "assetUid": "test-asset-uid-sber"
                    },
                    {
                        "figi": "BBG004730ZJ9",
                        "ticker": "GAZP",
                        "name": "ПАО Газпром",
                        "currency": "RUB",
                        "exchange": "MOEX",
                        "sector": "Energy",
                        "tradingStatus": "SECURITY_TRADING_STATUS_NORMAL_TRADING",
                        "shortEnabledFlag": true,
                        "assetUid": "test-asset-uid-gazp"
                    }
                ]
            }
            """;
        return mapper.readTree(json);
    }

    /**
     * Создание тестового JSON ответа для REST API индикативов
     */
    private JsonNode createMockJsonResponse() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = """
            {
                "instruments": [
                    {
                        "figi": "BBG0013HGFT4",
                        "ticker": "USD000UTSTOM",
                        "name": "Доллар США / Российский рубль",
                        "currency": "RUB",
                        "exchange": "MOEX",
                        "classCode": "CURRENCY",
                        "uid": "USD000UTSTOM",
                        "sellAvailableFlag": true,
                        "buyAvailableFlag": true
                    },
                    {
                        "figi": "BBG0013HGFT5",
                        "ticker": "EUR000UTSTOM",
                        "name": "Евро / Российский рубль",
                        "currency": "RUB",
                        "exchange": "MOEX",
                        "classCode": "CURRENCY",
                        "uid": "EUR000UTSTOM",
                        "sellAvailableFlag": true,
                        "buyAvailableFlag": true
                    }
                ]
            }
            """;
        return mapper.readTree(json);
    }

    // ==================== ТЕСТЫ ДЛЯ АКЦИЙ ====================

    @Test
    @DisplayName("Получение списка акций из API - успешный случай")
    @Description("Тест проверяет корректность получения акций из Tinkoff API с применением фильтров")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("unit")
    @Tag("positive")
    void getShares_ShouldReturnSharesList_WhenValidParametersProvided() throws Exception {
        // Шаг 1: Подготовка тестовых данных
        JsonNode mockResponse = Allure.step("Подготовка тестовых данных", () -> {
            return createMockSharesJsonResponse();
        });

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(restClient.getShares()).thenReturn(mockResponse);
        });

        // Шаг 3: Выполнение запроса
        List<ShareDto> result = Allure.step("Выполнение запроса", () -> {
            return instrumentService.getShares(
                "SECURITY_TRADING_STATUS_NORMAL_TRADING",
                "MOEX",
                "RUB",
                null,
                null
            );
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            
            // Проверяем первую акцию (GAZP - идет первой по алфавиту)
            ShareDto gazp = result.get(0);
            assertThat(gazp.figi()).isEqualTo("BBG004730ZJ9");
            assertThat(gazp.ticker()).isEqualTo("GAZP");
            assertThat(gazp.name()).isEqualTo("ПАО Газпром");
            assertThat(gazp.currency()).isEqualTo("RUB");
            assertThat(gazp.exchange()).isEqualTo("MOEX");
            assertThat(gazp.sector()).isEqualTo("Energy");
            assertThat(gazp.tradingStatus()).isEqualTo("SECURITY_TRADING_STATUS_NORMAL_TRADING");
            assertThat(gazp.shortEnabled()).isTrue();

            // Проверяем вторую акцию (SBER - идет второй по алфавиту)
            ShareDto sber = result.get(1);
            assertThat(sber.figi()).isEqualTo("BBG004730N88");
            assertThat(sber.ticker()).isEqualTo("SBER");
            assertThat(sber.name()).isEqualTo("ПАО Сбербанк");
            assertThat(sber.sector()).isEqualTo("Financial");
            assertThat(sber.shortEnabled()).isTrue();

            // Проверяем сортировку по тикеру
            assertThat(result.get(0).ticker()).isEqualTo("GAZP"); 
            assertThat(result.get(1).ticker()).isEqualTo("SBER");
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(restClient).getShares();
        });
    }

    @Test
    @DisplayName("Получение списка акций из API - некорректная валюта")
    @Description("Тест проверяет корректность получения списка акций из Tinkoff API с некорректной валютой")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("unit")
    @Tag("negative")
    void getShares_ShouldReturnEmptyList_WhenInvalidCurrency() throws Exception {
        // Шаг 1: Подготовка тестовых данных
        JsonNode mockResponse = Allure.step("Подготовка тестовых данных", () -> {
            return createMockSharesJsonResponse();
        });

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(restClient.getShares()).thenReturn(mockResponse);
        });

        // Шаг 3: Выполнение запроса
        List<ShareDto> result = Allure.step("Выполнение запроса", () -> {
            return instrumentService.getShares(
                "SECURITY_TRADING_STATUS_NORMAL_TRADING",
                "MOEX",
                "UNCORRECT_CURRENCY",
                null,
                null
            );
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertThat(result).isNotNull();
            assertThat(result).isEmpty(); // Пустой список при некорректной валюте
        });
        
        // Шаг 4: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(restClient).getShares();
        });
    }

    @Test
    @DisplayName("Получение списка акций из API - некорректный статус")
    @Description("Тест проверяет корректность получения списка акций из Tinkoff API с некорректным статусом")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("unit")
    @Tag("negative")
    void getShares_ShouldReturnEmptyList_WhenInvalidStatus() throws Exception {
        // Шаг 1: Подготовка тестовых данных
        JsonNode mockResponse = Allure.step("Подготовка тестовых данных", () -> {
            return createMockSharesJsonResponse();
        });

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(restClient.getShares()).thenReturn(mockResponse);
        });
            
        // Шаг 3: Выполнение запроса
        List<ShareDto> result = Allure.step("Выполнение запроса", () -> {
            return instrumentService.getShares(
                "INVALID_STATUS",
                "MOEX",
                "RUB",
                null,
                null
            );
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertThat(result).isNotNull();
            assertThat(result).isEmpty(); // Пустой список при некорректном статусе
        });
        
        // Шаг 4: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(restClient).getShares();
        });
    }

 
   @Test
   @DisplayName("Получение списка акций из API - некорректная биржа")
   @Description("Тест проверяет корректность получения списка акций из Tinkoff API с некорректной биржей")
   @Severity(SeverityLevel.CRITICAL)
   @Story("Shares API")
   @Tag("api")
   @Tag("shares")
   @Tag("unit")
   @Tag("negative")
   void getShares_ShouldReturnEmptyList_WhenInvalidExchange() throws Exception {
        // Шаг 1: Подготовка тестовых данных
        JsonNode mockResponse = Allure.step("Подготовка тестовых данных", () -> {
            return createMockSharesJsonResponse();
        });

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(restClient.getShares()).thenReturn(mockResponse);
        });
            
        // Шаг 3: Выполнение запроса
        List<ShareDto> result = Allure.step("Выполнение запроса", () -> {
            return instrumentService.getShares(
                "SECURITY_TRADING_STATUS_NORMAL_TRADING",
                "INVALID_EXCHANGE",
                "RUB",
                null,
                null
            );
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertThat(result).isNotNull();
            assertThat(result).isEmpty(); // Пустой список при некорректной бирже
        });
        
        // Шаг 4: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(restClient).getShares();
        });
    }

    @Test
    @DisplayName("Сохранение акций в базу данных с защитой от дубликатов")
    @Description("Тест проверяет корректность сохранения акций в базу данных с защитой от дубликатов")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares Save")
    @Tag("save")
    @Tag("shares")
    @Tag("database")
    @Tag("unit")
    @Tag("positive")
    void saveShares_ShouldSaveShares_WhenValidParametersProvided() throws Exception {
        // Шаг 1: Подготовка тестовых данных
        JsonNode mockResponse = Allure.step("Подготовка тестовых данных", () -> {
            return createMockSharesJsonResponse();
        });
        ShareFilterDto filter = TestDataFactory.createShareFilterDto();
        filter.setExchange("MOEX"); // Используем биржу из тестовых данных
        filter.setTicker(null); // Убираем фильтр по тикеру, чтобы получить все акции
        filter.setStatus(null); // Убираем фильтр по статусу, чтобы получить все акции

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(restClient.getShares()).thenReturn(mockResponse);
            
            // Настраиваем моки для проверки существования акций в БД
            when(shareRepo.existsById("BBG004730N88")).thenReturn(false); // SBER не существует
            
            // Настраиваем мок для сохранения акций
            when(shareRepo.save(any(ShareEntity.class))).thenAnswer(invocation -> {
                ShareEntity entity = invocation.getArgument(0);
                return entity; // Возвращаем ту же сущность
            });
        });

        // Шаг 3: Выполнение сохранения
        SaveResponseDto result = Allure.step("Выполнение сохранения", () -> {
            return instrumentService.saveShares(filter);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getTotalRequested()).isEqualTo(1); // Только одна акция проходит фильтрацию
            assertThat(result.getNewItemsSaved()).isEqualTo(1);
            assertThat(result.getExistingItemsSkipped()).isEqualTo(0);
            assertThat(result.getInvalidItemsFiltered()).isEqualTo(0);
            assertThat(result.getMissingFromApi()).isEqualTo(0);
            assertThat(result.getMessage()).contains("Успешно загружено 1 новых акций из 1 найденных");
            
            // Проверяем, что сохраненные элементы содержат правильные данные
            assertThat(result.getSavedItems()).hasSize(1);
            assertThat(result.getSavedItems()).extracting("ticker").containsExactlyInAnyOrder("SBER");
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(restClient).getShares();
            verify(shareRepo).existsById("BBG004730N88");
            verify(shareRepo, times(1)).save(any(ShareEntity.class));
        });
    }

    @Test
    @DisplayName("Получение акций из базы данных - успешный случай")
    @Description("Тест проверяет корректность получения акций из локальной БД")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares Database")
    @Tag("database")
    @Tag("shares")
    @Tag("unit")
    @Tag("positive")
    void getSharesFromDatabase_ShouldReturnSharesList_WhenEntitiesExist() {
        // Шаг 1: Подготовка тестовых данных
        List<ShareEntity> mockEntities = Allure.step("Подготовка тестовых данных", () -> {
            return TestDataFactory.createShareEntityList();
        });
        ShareFilterDto filter = TestDataFactory.createShareFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_ACTIVE");
        filter.setExchange("TESTMOEX"); // Используем биржу из TestDataFactory
        filter.setCurrency("RUB");
        filter.setTicker(null); // Убираем фильтр по тикеру, чтобы получить все акции
        filter.setFigi(null); // Убираем фильтр по FIGI, чтобы получить все акции
        filter.setSector(null); // Убираем фильтр по сектору, чтобы получить все акции
        filter.setTradingStatus(null); // Убираем фильтр по статусу, чтобы получить все акции

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(shareRepo.findAll()).thenReturn(mockEntities);
        });

        // Шаг 3: Выполнение запроса
        List<ShareDto> result = Allure.step("Выполнение запроса", () -> {
            return instrumentService.getSharesFromDatabase(filter);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertThat(result).isNotNull();
            assertThat(result).hasSize(3); // Все 3 акции из TestDataFactory
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(shareRepo).findAll();
        });
    }

    @Test
    @DisplayName("Получение акции по FIGI из базы данных - успешный случай")
    @Description("Тест проверяет корректность получения акции по FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares Database")
    @Tag("database")
    @Tag("shares")
    @Tag("search")
    @Tag("unit")
    @Tag("positive")
    void getShareByFigi_ShouldReturnShare_WhenShareExists() {
        // Given - настройка мока для получения акции по FIGI
        ShareEntity mockEntity = TestDataFactory.createShareEntity();
        when(shareRepo.findById("BBG004730N88"))
            .thenReturn(Optional.of(mockEntity));

        // When - вызов метода сервиса
        ShareDto result = instrumentService.getShareByFigi("BBG004730N88");

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.figi()).isEqualTo("BBG004730N88");
        assertThat(result.ticker()).isEqualTo("SBER");
        assertThat(result.name()).isEqualTo("Сбербанк");
        assertThat(result.currency()).isEqualTo("RUB");
        assertThat(result.exchange()).isEqualTo("moex_mrng_evng_e_wknd_dlr");
        assertThat(result.sector()).isEqualTo("Financials");
        assertThat(result.tradingStatus()).isEqualTo("SECURITY_TRADING_STATUS_NORMAL_TRADING");
        assertThat(result.shortEnabled()).isTrue();

        // Verify
        verify(shareRepo).findById("BBG004730N88");
    }

    @Test
    @DisplayName("Получение акции по FIGI - акция не найдена")
    @Description("Тест проверяет поведение сервиса при поиске несуществующей акции")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares Database")
    @Tag("database")
    @Tag("shares")
    @Tag("negative")
    @Tag("unit")
    @Tag("not-found")
    void getShareByFigi_ShouldReturnNull_WhenShareNotFound() {
        // Given - настройка мока для несуществующей акции
        when(shareRepo.findById("INVALID_FIGI"))
            .thenReturn(Optional.empty());

        // When - вызов метода сервиса
        ShareDto result = instrumentService.getShareByFigi("INVALID_FIGI");

        // Then - проверка результата
        assertThat(result).isNull();

        // Verify
        verify(shareRepo).findById("INVALID_FIGI");
    }

    @Test
    @DisplayName("Получение акции по тикеру из базы данных - успешный случай")
    @Description("Тест проверяет корректность получения акции по тикеру")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares Database")
    @Tag("database")
    @Tag("shares")
    @Tag("search")
    @Tag("unit")
    @Tag("positive")
    void getShareByTicker_ShouldReturnShare_WhenShareExists() {
        // Given - настройка мока для получения акции по тикеру
        ShareEntity mockEntity = TestDataFactory.createShareEntity();
        when(shareRepo.findByTickerIgnoreCase("SBER"))
            .thenReturn(Optional.of(mockEntity));

        // When - вызов метода сервиса
        ShareDto result = instrumentService.getShareByTicker("SBER");

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.figi()).isEqualTo("BBG004730N88");
        assertThat(result.ticker()).isEqualTo("SBER");
        assertThat(result.name()).isEqualTo("Сбербанк");
        assertThat(result.shortEnabled()).isTrue();

        // Verify
        verify(shareRepo).findByTickerIgnoreCase("SBER");
    }

    @Test
    @DisplayName("Получение акции по тикеру - акция не найдена")
    @Description("Тест проверяет поведение сервиса при поиске несуществующей акции по тикеру")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares Database")
    @Tag("database")
    @Tag("shares")
    @Tag("negative")
    @Tag("unit")
    @Tag("not-found")
    void getShareByTicker_ShouldReturnNull_WhenShareNotFound() {
        // Given - настройка мока для несуществующей акции
        when(shareRepo.findByTickerIgnoreCase("INVALID_TICKER"))
            .thenReturn(Optional.empty());

        // When - вызов метода сервиса
        ShareDto result = instrumentService.getShareByTicker("INVALID_TICKER");

        // Then - проверка результата
        assertThat(result).isNull();

        // Verify
        verify(shareRepo).findByTickerIgnoreCase("INVALID_TICKER");
    }

    // ==================== ТЕСТЫ ДЛЯ ФЬЮЧЕРСОВ ====================

    @Test
    @DisplayName("Получение списка фьючерсов из API - успешный случай")
    @Description("Тест проверяет корректность получения фьючерсов из Tinkoff API")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures API")
    @Tag("api")
    @Tag("futures")
    @Tag("unit")
    @Tag("positive")
    void getFutures_ShouldReturnFuturesList_WhenValidParametersProvided() {
        // Шаг 1: Подготовка тестовых данных
        FuturesResponse mockResponse = Allure.step("Подготовка тестовых данных", () -> {
            return createMockFuturesResponse();
        });

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(instrumentsService.futures(any(InstrumentsRequest.class)))
                .thenReturn(mockResponse);
            when(tinkoffApiClient.convertTimestampToLocalDateTime(any(com.google.protobuf.Timestamp.class)))
                .thenReturn(java.time.LocalDateTime.of(2024, 6, 24, 18, 40));
        });

        // Шаг 3: Выполнение запроса
        List<FutureDto> result = Allure.step("Выполнение запроса", () -> {
            return instrumentService.getFutures(
                "INSTRUMENT_STATUS_BASE",
                "MOEX",
                "USD",
                null,
                "COMMODITY"
            );
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            
            // Проверяем первый фьючерс (GZ0624 - идет первой по алфавиту)
            FutureDto gold = result.get(0);
            assertThat(gold.figi()).isEqualTo("FUTGZ0624000");
            assertThat(gold.ticker()).isEqualTo("GZ0624");
            assertThat(gold.assetType()).isEqualTo("COMMODITY");
            assertThat(gold.basicAsset()).isEqualTo("Gold");
            assertThat(gold.currency()).isEqualTo("USD");
            assertThat(gold.exchange()).isEqualTo("MOEX");
            assertThat(gold.shortEnabled()).isTrue();
            assertThat(gold.expirationDate()).isEqualTo(java.time.LocalDateTime.of(2024, 6, 24, 18, 40));

            // Проверяем второй фьючерс (SI0624 - идет второй по алфавиту)
            FutureDto silver = result.get(1);
            assertThat(silver.figi()).isEqualTo("FUTSI0624000");
            assertThat(silver.ticker()).isEqualTo("SI0624");
            assertThat(silver.basicAsset()).isEqualTo("Silver");
            assertThat(silver.shortEnabled()).isTrue();
            assertThat(silver.expirationDate()).isEqualTo(java.time.LocalDateTime.of(2024, 6, 24, 18, 40));
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(instrumentsService).futures(any(InstrumentsRequest.class));
        });
    }

    @Test
    @DisplayName("Получение фьючерса по FIGI из базы данных - успешный случай")
    @Description("Тест проверяет корректность получения фьючерса по FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures Database")
    @Tag("database")
    @Tag("futures")
    @Tag("search")
    @Tag("unit")
    @Tag("positive")
    void getFutureByFigi_ShouldReturnFuture_WhenFutureExists() {
        // Given - настройка мока для получения фьючерса по FIGI
        FutureEntity mockEntity = TestDataFactory.createFutureEntity();
        when(futureRepo.findById("FUTSI0624000"))
            .thenReturn(Optional.of(mockEntity));

        // When - вызов метода сервиса
        FutureDto result = instrumentService.getFutureByFigi("FUTSI0624000");

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.figi()).isEqualTo("FUTSBER0324");
        assertThat(result.ticker()).isEqualTo("SBER-3.24");
        assertThat(result.assetType()).isEqualTo("FUTURES");
        assertThat(result.basicAsset()).isEqualTo("SBER");
        assertThat(result.currency()).isEqualTo("RUB");
        assertThat(result.exchange()).isEqualTo("moex_mrng_evng_e_wknd_dlr");
        assertThat(result.shortEnabled()).isTrue();
        assertThat(result.expirationDate()).isEqualTo(java.time.LocalDateTime.of(2024, 3, 15, 18, 45));

        // Verify
        verify(futureRepo).findById("FUTSI0624000");
    }

    @Test
    @DisplayName("Получение фьючерса по тикеру из базы данных - успешный случай")
    @Description("Тест проверяет корректность получения фьючерса по тикеру")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures Database")
    @Tag("database")
    @Tag("futures")
    @Tag("search")
    @Tag("unit")
    @Tag("positive")
    void getFutureByTicker_ShouldReturnFuture_WhenFutureExists() {
        // Given - настройка мока для получения фьючерса по тикеру
        FutureEntity mockEntity = TestDataFactory.createFutureEntity();
        when(futureRepo.findByTickerIgnoreCase("SI0624"))
            .thenReturn(Optional.of(mockEntity));

        // When - вызов метода сервиса
        FutureDto result = instrumentService.getFutureByTicker("SI0624");

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.figi()).isEqualTo("FUTSBER0324");
        assertThat(result.ticker()).isEqualTo("SBER-3.24");
        assertThat(result.assetType()).isEqualTo("FUTURES");
        assertThat(result.shortEnabled()).isTrue();
        assertThat(result.expirationDate()).isEqualTo(java.time.LocalDateTime.of(2024, 3, 15, 18, 45));

        // Verify
        verify(futureRepo).findByTickerIgnoreCase("SI0624");
    }

    @Test
    @DisplayName("Сохранение фьючерсов в базу данных - успешный случай")
    @Description("Тест проверяет корректность сохранения фьючерсов в БД")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures Save")
    @Tag("save")
    @Tag("futures")
    @Tag("database")
    @Tag("unit")
    @Tag("positive")
    void saveFutures_ShouldSaveFutures_WhenValidParametersProvided() {
        // Given - настройка моков для сохранения фьючерсов
        FuturesResponse mockResponse = createMockFuturesResponse();
        when(instrumentsService.futures(any(InstrumentsRequest.class)))
            .thenReturn(mockResponse);
        when(tinkoffApiClient.convertTimestampToLocalDateTime(any(com.google.protobuf.Timestamp.class)))
            .thenReturn(java.time.LocalDateTime.of(2024, 6, 24, 18, 40));
        
        // Настраиваем моки для проверки существования фьючерсов в БД
        when(futureRepo.existsById("FUTSI0624000")).thenReturn(false);
        when(futureRepo.existsById("FUTGZ0624000")).thenReturn(false);
        
        // Настраиваем мок для сохранения фьючерсов
        when(futureRepo.save(any(FutureEntity.class))).thenAnswer(invocation -> {
            FutureEntity entity = invocation.getArgument(0);
            return entity;
        });

        FutureFilterDto filter = TestDataFactory.createFutureFilterDto();
        filter.setExchange("MOEX"); // Используем биржу из тестовых данных
        filter.setCurrency("USD"); // Используем валюту из тестовых данных
        filter.setAssetType("COMMODITY"); // Используем тип актива из тестовых данных
        filter.setTicker(null); // Убираем фильтр по тикеру, чтобы получить все фьючерсы

        // When - вызов метода сервиса
        SaveResponseDto result = instrumentService.saveFutures(filter);

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRequested()).isEqualTo(2); // SI0624 и GZ0624
        assertThat(result.getNewItemsSaved()).isEqualTo(2);
        assertThat(result.getExistingItemsSkipped()).isEqualTo(0);
        assertThat(result.getMessage()).contains("Успешно загружено 2 новых фьючерсов из 2 найденных");
        
        // Проверяем, что сохраненные элементы содержат правильные данные
        assertThat(result.getSavedItems()).hasSize(2);
        assertThat(result.getSavedItems()).extracting("ticker").containsExactlyInAnyOrder("SI0624", "GZ0624");

        // Verify
        verify(instrumentsService).futures(any(InstrumentsRequest.class));
        verify(futureRepo).existsById("FUTSI0624000");
        verify(futureRepo).existsById("FUTGZ0624000");
        verify(futureRepo, times(2)).save(any(FutureEntity.class));
    }

    // ==================== ТЕСТЫ ДЛЯ ИНДИКАТИВОВ ====================

    @Test
    @DisplayName("Получение индикативов из REST API - успешный случай")
    @Description("Тест проверяет корректность получения индикативных инструментов из REST API")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("rest")
    @Tag("unit")
    @Tag("positive")
    void getIndicatives_ShouldReturnIndicativesList_WhenRestApiAvailable() throws Exception {
        // Шаг 1: Подготовка тестовых данных
        JsonNode mockJsonResponse = Allure.step("Подготовка тестовых данных", () -> {
            return createMockJsonResponse();
        });

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(restClient.getIndicatives()).thenReturn(mockJsonResponse);
        });

        // Шаг 3: Выполнение запроса
        List<IndicativeDto> result = Allure.step("Выполнение запроса", () -> {
            return instrumentService.getIndicatives(
                "MOEX",
                "RUB",
                null,
                null
            );
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            
            // Проверяем первый индикатив (EUR000UTSTOM - идет первой по алфавиту)
            IndicativeDto eurRub = result.get(0);
            assertThat(eurRub.figi()).isEqualTo("BBG0013HGFT5");
            assertThat(eurRub.ticker()).isEqualTo("EUR000UTSTOM");
            assertThat(eurRub.name()).isEqualTo("Евро / Российский рубль");
            assertThat(eurRub.currency()).isEqualTo("RUB");
            assertThat(eurRub.exchange()).isEqualTo("MOEX");
            assertThat(eurRub.classCode()).isEqualTo("CURRENCY");
            assertThat(eurRub.uid()).isEqualTo("EUR000UTSTOM");
            assertThat(eurRub.sellAvailableFlag()).isTrue();
            assertThat(eurRub.buyAvailableFlag()).isTrue();

            // Проверяем второй индикатив (USD000UTSTOM - идет второй по алфавиту)
            IndicativeDto usdRub = result.get(1);
            assertThat(usdRub.figi()).isEqualTo("BBG0013HGFT4");
            assertThat(usdRub.ticker()).isEqualTo("USD000UTSTOM");
            assertThat(usdRub.name()).isEqualTo("Доллар США / Российский рубль");
            assertThat(usdRub.sellAvailableFlag()).isTrue();
            assertThat(usdRub.buyAvailableFlag()).isTrue();
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(restClient).getIndicatives();
        });
    }

    @Test
    @DisplayName("Получение индикативов из базы данных при недоступности REST API")
    @Description("Тест проверяет fallback на БД при недоступности REST API")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("fallback")
    @Tag("database")
    @Tag("unit")
    @Tag("positive")
    void getIndicatives_ShouldUseDatabaseFallback_WhenRestApiUnavailable() {
        // Given - настройка мока для недоступности REST API и наличия данных в БД
        when(restClient.getIndicatives()).thenThrow(new RuntimeException("API unavailable"));
        List<IndicativeEntity> mockEntities = TestDataFactory.createIndicativeEntityList();
        when(indicativeRepo.findAll()).thenReturn(mockEntities);

        // When - вызов метода сервиса
        List<IndicativeDto> result = instrumentService.getIndicatives(
            "MOEX",
            "RUB",
            null,
            null
        );

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result).isEmpty(); // Пустой список при недоступности API

        // Verify
        verify(restClient).getIndicatives();
        verify(indicativeRepo).findAll();
    }

    @Test
    @DisplayName("Получение индикатива по FIGI - успешный случай")
    @Description("Тест проверяет корректность получения индикатива по FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("search")
    @Tag("unit")
    @Tag("positive")
    void getIndicativeBy_ShouldReturnIndicative_WhenRestApiAvailable() throws Exception {
        // Given - настройка мока для REST API
        ObjectMapper mapper = new ObjectMapper();
        String json = """
            {
                "instrument": {
                    "figi": "BBG0013HGFT4",
                    "ticker": "USD000UTSTOM",
                    "name": "Доллар США / Российский рубль",
                    "currency": "RUB",
                    "exchange": "MOEX",
                    "classCode": "CURRENCY",
                    "uid": "USD000UTSTOM",
                    "sellAvailableFlag": true,
                    "buyAvailableFlag": true
                }
            }
            """;
        JsonNode mockResponse = mapper.readTree(json);
        when(restClient.getIndicativeBy("BBG0013HGFT4")).thenReturn(mockResponse);

        // When - вызов метода сервиса
        IndicativeDto result = instrumentService.getIndicativeBy("BBG0013HGFT4");

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.figi()).isEqualTo("BBG0013HGFT4");
        assertThat(result.ticker()).isEqualTo("USD000UTSTOM");
        assertThat(result.name()).isEqualTo("Доллар США / Российский рубль");
        assertThat(result.currency()).isEqualTo("RUB");
        assertThat(result.exchange()).isEqualTo("MOEX");
        assertThat(result.classCode()).isEqualTo("CURRENCY");
        assertThat(result.uid()).isEqualTo("USD000UTSTOM");
        assertThat(result.sellAvailableFlag()).isTrue();
        assertThat(result.buyAvailableFlag()).isTrue();

        // Verify
        verify(restClient).getIndicativeBy("BBG0013HGFT4");
    }

    @Test
    @DisplayName("Получение индикатива по тикеру - успешный случай")
    @Description("Тест проверяет корректность получения индикатива по тикеру")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("search")
    @Tag("unit")
    @Tag("positive")
    void getIndicativeByTicker_ShouldReturnIndicative_WhenRestApiAvailable() throws Exception {
        // Given - настройка мока для REST API
        JsonNode mockJsonResponse = createMockJsonResponse();
        when(restClient.getIndicatives()).thenReturn(mockJsonResponse);

        // When - вызов метода сервиса
        IndicativeDto result = instrumentService.getIndicativeByTicker("USD000UTSTOM");

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.figi()).isEqualTo("BBG0013HGFT4");
        assertThat(result.ticker()).isEqualTo("USD000UTSTOM");
        assertThat(result.name()).isEqualTo("Доллар США / Российский рубль");
        assertThat(result.sellAvailableFlag()).isTrue();
        assertThat(result.buyAvailableFlag()).isTrue();

        // Verify
        verify(restClient).getIndicatives();
    }

    @Test
    @DisplayName("Сохранение индикативов в базу данных - успешный случай")
    @Description("Тест проверяет корректность сохранения индикативов в БД")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives Save")
    @Tag("save")
    @Tag("indicatives")
    @Tag("database")
    @Tag("unit")
    @Tag("positive")
    void saveIndicatives_ShouldSaveIndicatives_WhenValidParametersProvided() throws Exception {
        // Given - настройка моков для сохранения индикативов
        JsonNode mockJsonResponse = createMockJsonResponse();
        when(restClient.getIndicatives()).thenReturn(mockJsonResponse);
        
        // Настраиваем моки для проверки существования индикативов в БД
        when(indicativeRepo.existsById("BBG0013HGFT4")).thenReturn(false);
        when(indicativeRepo.existsById("BBG0013HGFT5")).thenReturn(false);
        
        // Настраиваем мок для сохранения индикативов
        when(indicativeRepo.save(any(IndicativeEntity.class))).thenAnswer(invocation -> {
            IndicativeEntity entity = invocation.getArgument(0);
            return entity;
        });

        IndicativeFilterDto filter = TestDataFactory.createIndicativeFilterDto();
        filter.setExchange("MOEX");
        filter.setCurrency("RUB");
        filter.setTicker(null); // Убираем фильтр по тикеру, чтобы получить все индикативы
        filter.setFigi(null); // Убираем фильтр по FIGI, чтобы получить все индикативы

        // When - вызов метода сервиса
        SaveResponseDto result = instrumentService.saveIndicatives(filter);

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRequested()).isEqualTo(2);
        assertThat(result.getNewItemsSaved()).isEqualTo(2);
        assertThat(result.getExistingItemsSkipped()).isEqualTo(0);
        assertThat(result.getMessage()).contains("Успешно загружено 2 новых индикативных инструментов из 2 найденных");
        
        // Проверяем, что сохраненные элементы содержат правильные данные
        assertThat(result.getSavedItems()).hasSize(2);
        assertThat(result.getSavedItems()).extracting("ticker").containsExactlyInAnyOrder("USD000UTSTOM", "EUR000UTSTOM");

        // Verify
        verify(restClient).getIndicatives();
        verify(indicativeRepo).existsById("BBG0013HGFT4");
        verify(indicativeRepo).existsById("BBG0013HGFT5");
        verify(indicativeRepo, times(2)).save(any(IndicativeEntity.class));
    }

    // ==================== ТЕСТЫ ДЛЯ СТАТИСТИКИ ====================

    @Test
    @DisplayName("Получение статистики инструментов - успешный случай")
    @Description("Тест проверяет корректность подсчета количества инструментов по типам")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Statistics")
    @Tag("statistics")
    @Tag("count")
    @Tag("unit")
    @Tag("positive")
    void getInstrumentCounts_ShouldReturnCorrectCounts_WhenInstrumentsExist() {
        // Шаг 1: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(shareRepo.count()).thenReturn(150L);
            when(futureRepo.count()).thenReturn(45L);
            when(indicativeRepo.count()).thenReturn(12L);
        });

        // Шаг 2: Выполнение запроса
        Map<String, Long> result = Allure.step("Выполнение запроса", () -> {
            return instrumentService.getInstrumentCounts();
        });

        // Шаг 3: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertThat(result).isNotNull();
            assertThat(result).hasSize(4);
            assertThat(result.get("shares")).isEqualTo(150L);
            assertThat(result.get("futures")).isEqualTo(45L);
            assertThat(result.get("indicatives")).isEqualTo(12L);
            assertThat(result.get("total")).isEqualTo(207L); // 150 + 45 + 12
        });

        // Шаг 4: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(shareRepo).count();
            verify(futureRepo).count();
            verify(indicativeRepo).count();
        });
    }

    // ==================== НЕГАТИВНЫЕ ТЕСТЫ ====================

    @Test
    @DisplayName("Сохранение акций - все акции уже существуют")
    @Description("Тест проверяет поведение при попытке сохранить уже существующие акции")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares Save")
    @Tag("save")
    @Tag("shares")
    @Tag("negative")
    @Tag("unit")
    @Tag("duplicate")
    void saveShares_ShouldSkipExistingShares_WhenSharesAlreadyExist() throws Exception {
        // Шаг 1: Подготовка тестовых данных
        JsonNode mockResponse = Allure.step("Подготовка тестовых данных", () -> {
            return createMockSharesJsonResponse();
        });
        ShareFilterDto filter = TestDataFactory.createShareFilterDto();
        filter.setStatus("SECURITY_TRADING_STATUS_NORMAL_TRADING");
        filter.setExchange("MOEX");
        filter.setCurrency("RUB");

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков", () -> {
            when(restClient.getShares()).thenReturn(mockResponse);
            when(shareRepo.existsById(anyString())).thenReturn(true);
        });

        // Шаг 3: Выполнение сохранения
        SaveResponseDto result = Allure.step("Выполнение сохранения", () -> {
            return instrumentService.saveShares(filter);
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка результата", () -> {
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getTotalRequested()).isEqualTo(1); // только одна акция обрабатывается
            assertThat(result.getNewItemsSaved()).isEqualTo(0);
            assertThat(result.getExistingItemsSkipped()).isEqualTo(1); // одна акция пропускается
            assertThat(result.getMessage()).contains("Все найденные акции уже существуют в базе данных");
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка взаимодействий", () -> {
            verify(restClient).getShares();
            verify(shareRepo, times(1)).existsById(any()); // проверяем одну акцию
            verify(shareRepo, never()).save(any(ShareEntity.class));
        });
    }

    @Test
    @DisplayName("Получение акций из API с фильтрацией по тикеру")
    @Description("Тест проверяет корректность фильтрации акций по тикеру")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("filtering")
    @Tag("unit")
    @Tag("positive")
    void getShares_ShouldReturnFilteredShares_WhenTickerFilterApplied() throws Exception {
        // Given - настройка мока для получения акций из API
        JsonNode mockResponse = createMockSharesJsonResponse();
        when(restClient.getShares()).thenReturn(mockResponse);

        // When - вызов метода сервиса с фильтром по тикеру
        List<ShareDto> result = instrumentService.getShares(
            "SECURITY_TRADING_STATUS_NORMAL_TRADING",
            null,
            null,
            "SBER",
            null
        );

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        
        ShareDto share = result.get(0);
        assertThat(share.ticker()).isEqualTo("SBER");
        assertThat(share.name()).isEqualTo("ПАО Сбербанк");
        assertThat(share.shortEnabled()).isTrue();

        // Verify
        verify(restClient).getShares();
    }

    @Test
    @DisplayName("Получение фьючерсов из API без параметров")
    @Description("Тест проверяет поведение API фьючерсов при отсутствии параметров")
    @Severity(SeverityLevel.NORMAL)
    @Story("Futures API")
    @Tag("api")
    @Tag("futures")
    @Tag("no-params")
    @Tag("unit")
    @Tag("positive")
    void getFutures_ShouldReturnFuturesList_WhenNoParametersProvided() {
        // Given - настройка мока для фьючерсов без параметров
        FuturesResponse mockResponse = createMockFuturesResponse();
        when(instrumentsService.futures(any(InstrumentsRequest.class)))
            .thenReturn(mockResponse);
        when(tinkoffApiClient.convertTimestampToLocalDateTime(any(com.google.protobuf.Timestamp.class)))
            .thenReturn(java.time.LocalDateTime.of(2024, 6, 24, 18, 40));

        // When - вызов метода сервиса
        List<FutureDto> result = instrumentService.getFutures(
            null,
            null,
            null,
            null,
            null
        );

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).ticker()).isEqualTo("GZ0624");
        assertThat(result.get(0).shortEnabled()).isTrue();
        assertThat(result.get(0).expirationDate()).isEqualTo(java.time.LocalDateTime.of(2024, 6, 24, 18, 40));
        assertThat(result.get(1).ticker()).isEqualTo("SI0624");
        assertThat(result.get(1).shortEnabled()).isTrue();
        assertThat(result.get(1).expirationDate()).isEqualTo(java.time.LocalDateTime.of(2024, 6, 24, 18, 40));

        // Verify
        verify(instrumentsService).futures(any(InstrumentsRequest.class));
    }

    @Test
    @DisplayName("Получение индикативов из API без параметров")
    @Description("Тест проверяет поведение API индикативов при отсутствии параметров")
    @Severity(SeverityLevel.NORMAL)
    @Story("Indicatives API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("no-params")
    @Tag("unit")
    @Tag("positive")
    void getIndicatives_ShouldReturnIndicativesList_WhenNoParametersProvided() throws Exception {
        // Given - настройка мока для индикативов без параметров
        JsonNode mockJsonResponse = createMockJsonResponse();
        when(restClient.getIndicatives()).thenReturn(mockJsonResponse);

        // When - вызов метода сервиса
        List<IndicativeDto> result = instrumentService.getIndicatives(
            null,
            null,
            null,
            null
        );

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).ticker()).isEqualTo("EUR000UTSTOM");
        assertThat(result.get(0).sellAvailableFlag()).isTrue();
        assertThat(result.get(0).buyAvailableFlag()).isTrue();
        assertThat(result.get(1).ticker()).isEqualTo("USD000UTSTOM");
        assertThat(result.get(1).sellAvailableFlag()).isTrue();
        assertThat(result.get(1).buyAvailableFlag()).isTrue();

        // Verify
        verify(restClient).getIndicatives();
    }

    // ==================== ДОПОЛНИТЕЛЬНЫЕ ТЕСТЫ ДЛЯ ИДЕАЛЬНОГО ПОКРЫТИЯ ====================

    @Test
    @DisplayName("Получение акций из API с фильтрацией по FIGI")
    @Description("Тест проверяет корректность фильтрации акций по FIGI")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("filtering")
    @Tag("edge-case")
    @Tag("unit")
    @Tag("positive")
    void getShares_ShouldReturnFilteredShares_WhenFigiFilterApplied() throws Exception {
        // Given - настройка мока для получения акций из API
        JsonNode mockResponse = createMockSharesJsonResponse();
        when(restClient.getShares()).thenReturn(mockResponse);

        // When - вызов метода сервиса с фильтром по FIGI
        List<ShareDto> result = instrumentService.getShares(
            "SECURITY_TRADING_STATUS_NORMAL_TRADING",
            null,
            null,
            null,
            "BBG004730N88"
        );

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        
        ShareDto share = result.get(0);
        assertThat(share.figi()).isEqualTo("BBG004730N88");
        assertThat(share.ticker()).isEqualTo("SBER");
        assertThat(share.shortEnabled()).isTrue();

        // Verify
        verify(restClient).getShares();
    }

    @Test
    @DisplayName("Получение акций из базы данных с пустым фильтром")
    @Description("Тест проверяет поведение при получении акций из БД с пустым фильтром")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares Database")
    @Tag("database")
    @Tag("shares")
    @Tag("edge-case")
    @Tag("unit")
    @Tag("positive")
    void getSharesFromDatabase_ShouldReturnAllShares_WhenEmptyFilter() {
        // Given - настройка мока для получения всех акций из БД
        List<ShareEntity> mockEntities = TestDataFactory.createShareEntityList();
        when(shareRepo.findAll()).thenReturn(mockEntities);

        ShareFilterDto filter = new ShareFilterDto(); // Пустой фильтр

        // When - вызов метода сервиса
        List<ShareDto> result = instrumentService.getSharesFromDatabase(filter);

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);

        // Verify
        verify(shareRepo).findAll();
    }

    @Test
    @DisplayName("Сохранение акций - частично существующие акции")
    @Description("Тест проверяет поведение при сохранении смеси новых и существующих акций")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares Save")
    @Tag("save")
    @Tag("shares")
    @Tag("mixed")
    @Tag("unit")
    @Tag("positive")
    void saveShares_ShouldSaveOnlyNewShares_WhenSomeSharesExist() throws Exception {
        // Given - настройка моков для частично существующих акций
        JsonNode mockResponse = createMockSharesJsonResponse();
        when(restClient.getShares()).thenReturn(mockResponse);
        
        // Одна акция существует, другая нет
        when(shareRepo.existsById(anyString())).thenReturn(true);  // все акции существуют

        ShareFilterDto filter = TestDataFactory.createShareFilterDto();
        filter.setStatus("SECURITY_TRADING_STATUS_NORMAL_TRADING");
        filter.setExchange("MOEX");
        filter.setCurrency("RUB");

        // When - вызов метода сервиса
        SaveResponseDto result = instrumentService.saveShares(filter);

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRequested()).isEqualTo(1); // одна акция обрабатывается
        assertThat(result.getNewItemsSaved()).isEqualTo(0);  // ни одна акция не сохраняется
        assertThat(result.getExistingItemsSkipped()).isEqualTo(1); // одна акция пропускается
        assertThat(result.getMessage()).contains("Все найденные акции уже существуют в базе данных");
        
        // Проверяем, что сохраненных элементов нет
        assertThat(result.getSavedItems()).hasSize(0);

        // Verify
        verify(restClient).getShares();
        verify(shareRepo, times(1)).existsById(any());
        verify(shareRepo, never()).save(any(ShareEntity.class));
    }

    @Test
    @DisplayName("Получение фьючерса по FIGI - фьючерс не найден")
    @Description("Тест проверяет поведение при поиске несуществующего фьючерса")
    @Severity(SeverityLevel.NORMAL)
    @Story("Futures Database")
    @Tag("database")
    @Tag("futures")
    @Tag("negative")
    @Tag("unit")
    @Tag("not-found")
    void getFutureByFigi_ShouldReturnNull_WhenFutureNotFound() {
        // Given - настройка мока для несуществующего фьючерса
        when(futureRepo.findById("INVALID_FIGI"))
            .thenReturn(Optional.empty());

        // When - вызов метода сервиса
        FutureDto result = instrumentService.getFutureByFigi("INVALID_FIGI");

        // Then - проверка результата
        assertThat(result).isNull();

        // Verify
        verify(futureRepo).findById("INVALID_FIGI");
    }

    @Test
    @DisplayName("Получение фьючерса по тикеру - фьючерс не найден")
    @Description("Тест проверяет поведение при поиске несуществующего фьючерса по тикеру")
    @Severity(SeverityLevel.NORMAL)
    @Story("Futures Database")
    @Tag("database")
    @Tag("futures")
    @Tag("negative")
    @Tag("unit")
    @Tag("not-found")
    void getFutureByTicker_ShouldReturnNull_WhenFutureNotFound() {
        // Given - настройка мока для несуществующего фьючерса
        when(futureRepo.findByTickerIgnoreCase("INVALID_TICKER"))
            .thenReturn(Optional.empty());

        // When - вызов метода сервиса
        FutureDto result = instrumentService.getFutureByTicker("INVALID_TICKER");

        // Then - проверка результата
        assertThat(result).isNull();

        // Verify
        verify(futureRepo).findByTickerIgnoreCase("INVALID_TICKER");
    }

    @Test
    @DisplayName("Сохранение фьючерсов - все фьючерсы уже существуют")
    @Description("Тест проверяет поведение при попытке сохранить уже существующие фьючерсы")
    @Severity(SeverityLevel.NORMAL)
    @Story("Futures Save")
    @Tag("save")
    @Tag("futures")
    @Tag("negative")
    @Tag("unit")
    @Tag("duplicate")
    void saveFutures_ShouldSkipExistingFutures_WhenFuturesAlreadyExist() {
        // Given - настройка моков для уже существующих фьючерсов
        FuturesResponse mockResponse = createMockFuturesResponse();
        when(instrumentsService.futures(any(InstrumentsRequest.class)))
            .thenReturn(mockResponse);
        when(tinkoffApiClient.convertTimestampToLocalDateTime(any(com.google.protobuf.Timestamp.class)))
            .thenReturn(java.time.LocalDateTime.of(2024, 6, 24, 18, 40));
        when(futureRepo.existsById("FUTSI0624000")).thenReturn(true);
        when(futureRepo.existsById("FUTGZ0624000")).thenReturn(true);

        FutureFilterDto filter = TestDataFactory.createFutureFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_BASE");
        filter.setExchange("MOEX");
        filter.setCurrency("USD");
        filter.setAssetType("COMMODITY");
        filter.setTicker(null); // Убираем фильтр по тикеру, чтобы получить все фьючерсы

        // When - вызов метода сервиса
        SaveResponseDto result = instrumentService.saveFutures(filter);

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRequested()).isEqualTo(2); // SI0624 и GZ0624
        assertThat(result.getNewItemsSaved()).isEqualTo(0);
        assertThat(result.getExistingItemsSkipped()).isEqualTo(2);
        assertThat(result.getMessage()).contains("Все найденные фьючерсы уже существуют в базе данных");

        // Verify
        verify(instrumentsService).futures(any(InstrumentsRequest.class));
        verify(futureRepo, times(2)).existsById(any());
        verify(futureRepo, never()).save(any(FutureEntity.class));
    }

    @Test
    @DisplayName("Получение индикатива по FIGI - индикатив не найден")
    @Description("Тест проверяет поведение при поиске несуществующего индикатива")
    @Severity(SeverityLevel.NORMAL)
    @Story("Indicatives API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("negative")
    @Tag("unit")
    @Tag("not-found")
    void getIndicativeBy_ShouldReturnNull_WhenIndicativeNotFound() throws Exception {
        // Given - настройка мока для несуществующего индикатива
        ObjectMapper mapper = new ObjectMapper();
        String json = """
            {
                "instrument": null
            }
            """;
        JsonNode mockResponse = mapper.readTree(json);
        when(restClient.getIndicativeBy("INVALID_FIGI")).thenReturn(mockResponse);

        // When - вызов метода сервиса
        IndicativeDto result = instrumentService.getIndicativeBy("INVALID_FIGI");

        // Then - проверка результата
        assertThat(result).isNull();

        // Verify
        verify(restClient).getIndicativeBy("INVALID_FIGI");
    }

    @Test
    @DisplayName("Получение индикатива по тикеру - индикатив не найден")
    @Description("Тест проверяет поведение при поиске несуществующего индикатива по тикеру")
    @Severity(SeverityLevel.NORMAL)
    @Story("Indicatives API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("negative")
    @Tag("unit")
    @Tag("not-found")
    void getIndicativeByTicker_ShouldReturnNull_WhenIndicativeNotFound() throws Exception {
        // Given - настройка мока для пустого ответа API
        ObjectMapper mapper = new ObjectMapper();
        String json = """
            {
                "instruments": []
            }
            """;
        JsonNode mockResponse = mapper.readTree(json);
        when(restClient.getIndicatives()).thenReturn(mockResponse);

        // When - вызов метода сервиса
        IndicativeDto result = instrumentService.getIndicativeByTicker("INVALID_TICKER");

        // Then - проверка результата
        assertThat(result).isNull();

        // Verify
        verify(restClient).getIndicatives();
    }

    @Test
    @DisplayName("Сохранение индикативов - все индикативы уже существуют")
    @Description("Тест проверяет поведение при попытке сохранить уже существующие индикативы")
    @Severity(SeverityLevel.NORMAL)
    @Story("Indicatives Save")
    @Tag("save")
    @Tag("indicatives")
    @Tag("negative")
    @Tag("unit")
    @Tag("duplicate")
    void saveIndicatives_ShouldSkipExistingIndicatives_WhenIndicativesAlreadyExist() throws Exception {
        // Given - настройка моков для уже существующих индикативов
        JsonNode mockJsonResponse = createMockJsonResponse();
        when(restClient.getIndicatives()).thenReturn(mockJsonResponse);
        
        // Настраиваем моки для проверки существования индикативов в БД
        when(indicativeRepo.existsById("BBG0013HGFT4")).thenReturn(true);
        when(indicativeRepo.existsById("BBG0013HGFT5")).thenReturn(true);

        IndicativeFilterDto filter = TestDataFactory.createIndicativeFilterDto();
        filter.setExchange("MOEX");
        filter.setCurrency("RUB");
        filter.setTicker(null); // Убираем фильтр по тикеру, чтобы получить все индикативы
        filter.setFigi(null); // Убираем фильтр по FIGI, чтобы получить все индикативы

        // When - вызов метода сервиса
        SaveResponseDto result = instrumentService.saveIndicatives(filter);

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRequested()).isEqualTo(2);
        assertThat(result.getNewItemsSaved()).isEqualTo(0);
        assertThat(result.getExistingItemsSkipped()).isEqualTo(2);
        assertThat(result.getMessage()).contains("уже существуют в базе данных");

        // Verify
        verify(restClient).getIndicatives();
        verify(indicativeRepo).existsById("BBG0013HGFT4");
        verify(indicativeRepo).existsById("BBG0013HGFT5");
        verify(indicativeRepo, never()).save(any(IndicativeEntity.class));
    }

    @Test
    @DisplayName("Получение статистики инструментов - пустая база данных")
    @Description("Тест проверяет поведение статистики при пустой базе данных")
    @Severity(SeverityLevel.NORMAL)
    @Story("Statistics")
    @Tag("statistics")
    @Tag("empty")
    @Tag("unit")
    @Tag("positive")
    void getInstrumentCounts_ShouldReturnZeroCounts_WhenDatabaseIsEmpty() {
        // Given - настройка моков для пустой БД
        when(shareRepo.count()).thenReturn(0L);
        when(futureRepo.count()).thenReturn(0L);
        when(indicativeRepo.count()).thenReturn(0L);

        // When - вызов метода сервиса
        Map<String, Long> result = instrumentService.getInstrumentCounts();

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result).hasSize(4);
        assertThat(result.get("shares")).isEqualTo(0L);
        assertThat(result.get("futures")).isEqualTo(0L);
        assertThat(result.get("indicatives")).isEqualTo(0L);
        assertThat(result.get("total")).isEqualTo(0L);

        // Verify
        verify(shareRepo).count();
        verify(futureRepo).count();
        verify(indicativeRepo).count();
    }

    @Test
    @DisplayName("Получение акций из API - пустой ответ от API")
    @Description("Тест проверяет поведение при получении пустого ответа от API")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("empty-response")
    @Tag("unit")
    @Tag("positive")
    void getShares_ShouldReturnEmptyList_WhenApiReturnsEmptyResponse() throws Exception {
        // Given - настройка мока для пустого ответа API
        ObjectMapper mapper = new ObjectMapper();
        String json = """
            {
                "instruments": []
            }
            """;
        JsonNode emptyResponse = mapper.readTree(json);
        when(restClient.getShares()).thenReturn(emptyResponse);

        // When - вызов метода сервиса
        List<ShareDto> result = instrumentService.getShares(
            "SECURITY_TRADING_STATUS_NORMAL_TRADING",
            "MOEX",
            "RUB",
            null,
            null
        );

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        // Verify
        verify(restClient).getShares();
    }

    @Test
    @DisplayName("Получение фьючерсов из API - пустой ответ от API")
    @Description("Тест проверяет поведение при получении пустого ответа от API фьючерсов")
    @Severity(SeverityLevel.NORMAL)
    @Story("Futures API")
    @Tag("api")
    @Tag("futures")
    @Tag("empty-response")
    @Tag("unit")
    @Tag("positive")
    void getFutures_ShouldReturnEmptyList_WhenApiReturnsEmptyResponse() {
        // Given - настройка мока для пустого ответа API
        FuturesResponse emptyResponse = FuturesResponse.newBuilder().build();
        when(instrumentsService.futures(any(InstrumentsRequest.class)))
            .thenReturn(emptyResponse);

        // When - вызов метода сервиса
        List<FutureDto> result = instrumentService.getFutures(
            "INSTRUMENT_STATUS_BASE",
            "MOEX",
            "USD",
            null,
            "COMMODITY"
        );

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        // Verify
        verify(instrumentsService).futures(any(InstrumentsRequest.class));
    }

    @Test
    @DisplayName("Получение индикативов из API - пустой ответ от API")
    @Description("Тест проверяет поведение при получении пустого ответа от REST API")
    @Severity(SeverityLevel.NORMAL)
    @Story("Indicatives API")
    @Tag("api")
    @Tag("indicatives")
    @Tag("empty-response")
    @Tag("unit")
    @Tag("positive")
    void getIndicatives_ShouldReturnEmptyList_WhenApiReturnsEmptyResponse() throws Exception {
        // Given - настройка мока для пустого ответа API
        ObjectMapper mapper = new ObjectMapper();
        String json = """
            {
                "instruments": []
            }
            """;
        JsonNode emptyResponse = mapper.readTree(json);
        when(restClient.getIndicatives()).thenReturn(emptyResponse);

        // When - вызов метода сервиса
        List<IndicativeDto> result = instrumentService.getIndicatives(
            "MOEX",
            "RUB",
            null,
            null
        );

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        // Verify
        verify(restClient).getIndicatives();
    }

    @Test
    @DisplayName("Сохранение акций - API возвращает пустой список")
    @Description("Тест проверяет поведение при сохранении, когда API возвращает пустой список")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares Save")
    @Tag("save")
    @Tag("shares")
    @Tag("empty-api")
    @Tag("unit")
    @Tag("negative")
    void saveShares_ShouldHandleEmptyApiResponse_WhenApiReturnsEmptyList() throws Exception {
        // Given - настройка мока для пустого ответа API
        ObjectMapper mapper = new ObjectMapper();
        String json = """
            {
                "instruments": []
            }
            """;
        JsonNode emptyResponse = mapper.readTree(json);
        when(restClient.getShares()).thenReturn(emptyResponse);

        ShareFilterDto filter = TestDataFactory.createShareFilterDto();
        filter.setStatus("SECURITY_TRADING_STATUS_NORMAL_TRADING");
        filter.setExchange("MOEX");
        filter.setCurrency("RUB");

        // When - вызов метода сервиса
        SaveResponseDto result = instrumentService.saveShares(filter);

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getTotalRequested()).isEqualTo(0);
        assertThat(result.getNewItemsSaved()).isEqualTo(0);
        assertThat(result.getMessage()).contains("Новых акций не обнаружено");

        // Verify
        verify(restClient).getShares();
        verify(shareRepo, never()).existsById(any());
        verify(shareRepo, never()).save(any(ShareEntity.class));
    }

    @Test
    @DisplayName("Получение акций из базы данных - база данных пуста")
    @Description("Тест проверяет поведение при получении акций из пустой БД")
    @Severity(SeverityLevel.NORMAL)
    @Story("Shares Database")
    @Tag("database")
    @Tag("shares")
    @Tag("empty-database")
    @Tag("unit")
    @Tag("positive")
    void getSharesFromDatabase_ShouldReturnEmptyList_WhenDatabaseIsEmpty() {
        // Given - настройка мока для пустой БД
        when(shareRepo.findAll()).thenReturn(Collections.emptyList());

        ShareFilterDto filter = TestDataFactory.createShareFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_ACTIVE");
        filter.setExchange("MOEX");
        filter.setCurrency("RUB");

        // When - вызов метода сервиса
        List<ShareDto> result = instrumentService.getSharesFromDatabase(filter);

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        // Verify
        verify(shareRepo).findAll();
    }
}

    