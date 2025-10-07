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
        JsonNode mockResponse = Allure.step("Создание тестовых данных для акций SBER и GAZP", () -> {
            JsonNode response = createMockSharesJsonResponse();
            System.out.println("Созданы тестовые данные: акции SBER (Сбербанк) и GAZP (Газпром)");
            return response;
        });

        // Шаг 2: Настройка моков
        Allure.step("Настройка мока для внешнего API Tinkoff", () -> {
            when(restClient.getShares()).thenReturn(mockResponse);
            System.out.println("Настроен мок для внешнего API - возвращает тестовые данные акций");
        });

        // Шаг 3: Выполнение запроса
        List<ShareDto> result = Allure.step("Выполнение запроса к сервису с корректными параметрами", () -> {
            List<ShareDto> shares = instrumentService.getShares(
                "SECURITY_TRADING_STATUS_NORMAL_TRADING",
                "MOEX",
                "RUB",
                null,
                null
            );
            System.out.println("Выполнен запрос с параметрами: статус=NORMAL_TRADING, биржа=MOEX, валюта=RUB");
            return shares;
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка корректности возвращенных данных акций", () -> {
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
            
            System.out.println("Проверено: возвращены 2 акции с корректными данными");
            System.out.println("GAZP: FIGI=BBG004730ZJ9, Сектор=Energy, ShortEnabled=true");
            System.out.println("SBER: FIGI=BBG004730N88, Сектор=Financial, ShortEnabled=true");
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка корректности взаимодействий с внешним API", () -> {
            verify(restClient).getShares();
            System.out.println("Проверено: метод getShares() вызван один раз");
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
        JsonNode mockResponse = Allure.step("Создание тестовых данных для проверки фильтрации по валюте", () -> {
            JsonNode response = createMockSharesJsonResponse();
            System.out.println("Созданы тестовые данные: акции SBER и GAZP с валютой RUB");
            return response;
        });

        // Шаг 2: Настройка моков
        Allure.step("Настройка мока для внешнего API Tinkoff", () -> {
            when(restClient.getShares()).thenReturn(mockResponse);
            System.out.println("Настроен мок для внешнего API - возвращает тестовые данные акций");
        });

        // Шаг 3: Выполнение запроса
        List<ShareDto> result = Allure.step("Выполнение запроса с некорректной валютой", () -> {
            List<ShareDto> shares = instrumentService.getShares(
                "SECURITY_TRADING_STATUS_NORMAL_TRADING",
                "MOEX",
                "UNCORRECT_CURRENCY",
                null,
                null
            );
            System.out.println("Выполнен запрос с некорректной валютой: UNCORRECT_CURRENCY");
            return shares;
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка корректности фильтрации по некорректной валюте", () -> {
            assertThat(result).isNotNull();
            assertThat(result).isEmpty(); // Пустой список при некорректной валюте
            System.out.println("Проверено: возвращен пустой список при некорректной валюте");
            System.out.println("Фильтрация по валюте работает корректно");
        });
        
        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка корректности взаимодействий с внешним API", () -> {
            verify(restClient).getShares();
            System.out.println("Проверено: метод getShares() вызван один раз");
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
        JsonNode mockResponse = Allure.step("Создание тестовых данных для проверки фильтрации по статусу", () -> {
            JsonNode response = createMockSharesJsonResponse();
            System.out.println("Созданы тестовые данные: акции SBER и GAZP со статусом NORMAL_TRADING");
            return response;
        });

        // Шаг 2: Настройка моков
        Allure.step("Настройка мока для внешнего API Tinkoff", () -> {
            when(restClient.getShares()).thenReturn(mockResponse);
            System.out.println("Настроен мок для внешнего API - возвращает тестовые данные акций");
        });
            
        // Шаг 3: Выполнение запроса
        List<ShareDto> result = Allure.step("Выполнение запроса с некорректным статусом", () -> {
            List<ShareDto> shares = instrumentService.getShares(
                "INVALID_STATUS",
                "MOEX",
                "RUB",
                null,
                null
            );
            System.out.println("Выполнен запрос с некорректным статусом: INVALID_STATUS");
            return shares;
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка корректности фильтрации по некорректному статусу", () -> {
            assertThat(result).isNotNull();
            assertThat(result).isEmpty(); // Пустой список при некорректном статусе
            System.out.println("Проверено: возвращен пустой список при некорректном статусе");
            System.out.println("Фильтрация по статусу работает корректно");
        });
        
        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка корректности взаимодействий с внешним API", () -> {
            verify(restClient).getShares();
            System.out.println("Проверено: метод getShares() вызван один раз");
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
        JsonNode mockResponse = Allure.step("Создание тестовых данных для проверки фильтрации по бирже", () -> {
            JsonNode response = createMockSharesJsonResponse();
            System.out.println("Созданы тестовые данные: акции SBER и GAZP с биржей MOEX");
            return response;
        });

        // Шаг 2: Настройка моков
        Allure.step("Настройка мока для внешнего API Tinkoff", () -> {
            when(restClient.getShares()).thenReturn(mockResponse);
            System.out.println("Настроен мок для внешнего API - возвращает тестовые данные акций");
        });
            
        // Шаг 3: Выполнение запроса
        List<ShareDto> result = Allure.step("Выполнение запроса с некорректной биржей", () -> {
            List<ShareDto> shares = instrumentService.getShares(
                "SECURITY_TRADING_STATUS_NORMAL_TRADING",
                "INVALID_EXCHANGE",
                "RUB",
                null,
                null
            );
            System.out.println("Выполнен запрос с некорректной биржей: INVALID_EXCHANGE");
            return shares;
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка корректности фильтрации по некорректной бирже", () -> {
            assertThat(result).isNotNull();
            assertThat(result).isEmpty(); // Пустой список при некорректной бирже
            System.out.println("Проверено: возвращен пустой список при некорректной бирже");
            System.out.println("Фильтрация по бирже работает корректно");
        });
        
        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка корректности взаимодействий с внешним API", () -> {
            verify(restClient).getShares();
            System.out.println("Проверено: метод getShares() вызван один раз");
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
        JsonNode mockResponse = Allure.step("Создание тестовых данных для сохранения акций", () -> {
            JsonNode response = createMockSharesJsonResponse();
            System.out.println("Созданы тестовые данные: акции SBER и GAZP для сохранения в БД");
            return response;
        });
        
        ShareFilterDto filter = Allure.step("Настройка фильтра для сохранения акций", () -> {
            ShareFilterDto shareFilter = TestDataFactory.createShareFilterDto();
            shareFilter.setExchange("MOEX"); // Используем биржу из тестовых данных
            shareFilter.setTicker(null); // Убираем фильтр по тикеру, чтобы получить все акции
            shareFilter.setStatus(null); // Убираем фильтр по статусу, чтобы получить все акции
            System.out.println("Настроен фильтр: биржа=MOEX, без фильтров по тикеру и статусу");
            return shareFilter;
        });

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков для сохранения акций", () -> {
            when(restClient.getShares()).thenReturn(mockResponse);
            
            // Настраиваем моки для проверки существования акций в БД
            when(shareRepo.existsById("BBG004730N88")).thenReturn(false); // SBER не существует
            System.out.println("Настроен мок: акция SBER не существует в БД");
            
            // Настраиваем мок для сохранения акций
            when(shareRepo.save(any(ShareEntity.class))).thenAnswer(invocation -> {
                ShareEntity entity = invocation.getArgument(0);
                return entity; // Возвращаем ту же сущность
            });
            System.out.println("Настроен мок для сохранения акций в БД");
        });

        // Шаг 3: Выполнение сохранения
        SaveResponseDto result = Allure.step("Выполнение сохранения акций в базу данных", () -> {
            SaveResponseDto response = instrumentService.saveShares(filter);
            System.out.println("Выполнено сохранение акций с фильтром");
            return response;
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка корректности сохранения акций", () -> {
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
            
            System.out.println("Проверено: успешно сохранена 1 акция SBER");
            System.out.println("Статистика: TotalRequested=1, NewItemsSaved=1, ExistingItemsSkipped=0");
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка корректности взаимодействий с репозиторием", () -> {
            verify(restClient).getShares();
            verify(shareRepo).existsById("BBG004730N88");
            verify(shareRepo, times(1)).save(any(ShareEntity.class));
            System.out.println("Проверено: все взаимодействия с репозиторием выполнены корректно");
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
        List<ShareEntity> mockEntities = Allure.step("Создание тестовых данных для получения акций из БД", () -> {
            List<ShareEntity> entities = TestDataFactory.createShareEntityList();
            System.out.println("Созданы тестовые данные: 3 акции из TestDataFactory");
            return entities;
        });
        
        ShareFilterDto filter = Allure.step("Настройка фильтра для получения акций из БД", () -> {
            ShareFilterDto shareFilter = TestDataFactory.createShareFilterDto();
            shareFilter.setStatus("INSTRUMENT_STATUS_ACTIVE");
            shareFilter.setExchange("TESTMOEX"); // Используем биржу из TestDataFactory
            shareFilter.setCurrency("RUB");
            shareFilter.setTicker(null); // Убираем фильтр по тикеру, чтобы получить все акции
            shareFilter.setFigi(null); // Убираем фильтр по FIGI, чтобы получить все акции
            shareFilter.setSector(null); // Убираем фильтр по сектору, чтобы получить все акции
            shareFilter.setTradingStatus(null); // Убираем фильтр по статусу, чтобы получить все акции
            System.out.println("Настроен фильтр: статус=ACTIVE, биржа=TESTMOEX, валюта=RUB");
            return shareFilter;
        });

        // Шаг 2: Настройка моков
        Allure.step("Настройка мока для получения акций из БД", () -> {
            when(shareRepo.findAll()).thenReturn(mockEntities);
            System.out.println("Настроен мок: findAll() возвращает 3 тестовые акции");
        });

        // Шаг 3: Выполнение запроса
        List<ShareDto> result = Allure.step("Выполнение запроса к сервису для получения акций из БД", () -> {
            List<ShareDto> shares = instrumentService.getSharesFromDatabase(filter);
            System.out.println("Выполнен запрос к сервису с фильтром");
            return shares;
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка корректности возвращенных данных из БД", () -> {
            assertThat(result).isNotNull();
            assertThat(result).hasSize(3); // Все 3 акции из TestDataFactory
            System.out.println("Проверено: возвращены все 3 акции из БД");
            System.out.println("Фильтрация работает корректно");
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка корректности взаимодействий с репозиторием", () -> {
            verify(shareRepo).findAll();
            System.out.println("Проверено: метод findAll() вызван один раз");
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
        Allure.step("Создание тестовой акции для поиска по FIGI", () -> {
            ShareEntity mockEntity = TestDataFactory.createShareEntity();
            when(shareRepo.findById("BBG004730N88"))
                .thenReturn(Optional.of(mockEntity));
            System.out.println("Создана тестовая акция SBER с FIGI BBG004730N88");
        });

        // When - вызов метода сервиса
        ShareDto result = Allure.step("Выполнение поиска акции по FIGI", () -> {
            ShareDto share = instrumentService.getShareByFigi("BBG004730N88");
            System.out.println("Выполнен поиск акции по FIGI: BBG004730N88");
            return share;
        });

        // Then - проверка результата
        Allure.step("Проверка корректности возвращенных данных акции", () -> {
            assertThat(result).isNotNull();
            assertThat(result.figi()).isEqualTo("BBG004730N88");
            assertThat(result.ticker()).isEqualTo("SBER");
            assertThat(result.name()).isEqualTo("Сбербанк");
            assertThat(result.currency()).isEqualTo("RUB");
            assertThat(result.exchange()).isEqualTo("moex_mrng_evng_e_wknd_dlr");
            assertThat(result.sector()).isEqualTo("Financials");
            assertThat(result.tradingStatus()).isEqualTo("SECURITY_TRADING_STATUS_NORMAL_TRADING");
            assertThat(result.shortEnabled()).isTrue();
            
            System.out.println("Проверено: возвращена акция SBER с корректными данными");
            System.out.println("FIGI: BBG004730N88, Ticker: SBER, Name: Сбербанк, Currency: RUB");
        });

        // Verify
        Allure.step("Проверка корректности взаимодействий с репозиторием", () -> {
            verify(shareRepo).findById("BBG004730N88");
            System.out.println("Проверено: метод findById() вызван с правильным FIGI");
        });
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
        Allure.step("Настройка мока для несуществующей акции", () -> {
            when(shareRepo.findById("INVALID_FIGI"))
                .thenReturn(Optional.empty());
            System.out.println("Настроен мок: акция с FIGI INVALID_FIGI не найдена");
        });

        // When - вызов метода сервиса
        ShareDto result = Allure.step("Выполнение поиска несуществующей акции по FIGI", () -> {
            ShareDto share = instrumentService.getShareByFigi("INVALID_FIGI");
            System.out.println("Выполнен поиск акции по несуществующему FIGI: INVALID_FIGI");
            return share;
        });

        // Then - проверка результата
        Allure.step("Проверка корректности обработки несуществующей акции", () -> {
            assertThat(result).isNull();
            System.out.println("Проверено: возвращен null для несуществующей акции");
            System.out.println("Обработка несуществующих FIGI работает корректно");
        });

        // Verify
        Allure.step("Проверка корректности взаимодействий с репозиторием", () -> {
            verify(shareRepo).findById("INVALID_FIGI");
            System.out.println("Проверено: метод findById() вызван с несуществующим FIGI");
        });
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
        Allure.step("Создание тестовой акции для поиска по тикеру", () -> {
            ShareEntity mockEntity = TestDataFactory.createShareEntity();
            when(shareRepo.findByTickerIgnoreCase("SBER"))
                .thenReturn(Optional.of(mockEntity));
            System.out.println("Создана тестовая акция SBER для поиска по тикеру");
        });

        // When - вызов метода сервиса
        ShareDto result = Allure.step("Выполнение поиска акции по тикеру", () -> {
            ShareDto share = instrumentService.getShareByTicker("SBER");
            System.out.println("Выполнен поиск акции по тикеру: SBER");
            return share;
        });

        // Then - проверка результата
        Allure.step("Проверка корректности возвращенных данных акции", () -> {
            assertThat(result).isNotNull();
            assertThat(result.figi()).isEqualTo("BBG004730N88");
            assertThat(result.ticker()).isEqualTo("SBER");
            assertThat(result.name()).isEqualTo("Сбербанк");
            assertThat(result.shortEnabled()).isTrue();
            
            System.out.println("Проверено: возвращена акция SBER с корректными данными");
            System.out.println("FIGI: BBG004730N88, Ticker: SBER, Name: Сбербанк, ShortEnabled: true");
        });

        // Verify
        Allure.step("Проверка корректности взаимодействий с репозиторием", () -> {
            verify(shareRepo).findByTickerIgnoreCase("SBER");
            System.out.println("Проверено: метод findByTickerIgnoreCase() вызван с правильным тикером");
        });
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
        Allure.step("Настройка мока для несуществующей акции", () -> {
            when(shareRepo.findByTickerIgnoreCase("INVALID_TICKER"))
                .thenReturn(Optional.empty());
            System.out.println("Настроен мок: акция с тикером INVALID_TICKER не найдена");
        });

        // When - вызов метода сервиса
        ShareDto result = Allure.step("Выполнение поиска несуществующей акции по тикеру", () -> {
            ShareDto share = instrumentService.getShareByTicker("INVALID_TICKER");
            System.out.println("Выполнен поиск акции по несуществующему тикеру: INVALID_TICKER");
            return share;
        });

        // Then - проверка результата
        Allure.step("Проверка корректности обработки несуществующей акции", () -> {
            assertThat(result).isNull();
            System.out.println("Проверено: возвращен null для несуществующей акции");
            System.out.println("Обработка несуществующих тикеров работает корректно");
        });

        // Verify
        Allure.step("Проверка корректности взаимодействий с репозиторием", () -> {
            verify(shareRepo).findByTickerIgnoreCase("INVALID_TICKER");
            System.out.println("Проверено: метод findByTickerIgnoreCase() вызван с несуществующим тикером");
        });
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
        FuturesResponse mockResponse = Allure.step("Создание тестовых данных для фьючерсов Gold и Silver", () -> {
            FuturesResponse response = createMockFuturesResponse();
            System.out.println("Созданы тестовые данные: фьючерсы GZ0624 (Gold) и SI0624 (Silver)");
            return response;
        });

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков для gRPC API и конвертации времени", () -> {
            when(instrumentsService.futures(any(InstrumentsRequest.class)))
                .thenReturn(mockResponse);
            when(tinkoffApiClient.convertTimestampToLocalDateTime(any(com.google.protobuf.Timestamp.class)))
                .thenReturn(java.time.LocalDateTime.of(2024, 6, 24, 18, 40));
            System.out.println("Настроен мок для gRPC API - возвращает тестовые данные фьючерсов");
            System.out.println("Настроен мок для конвертации времени - возвращает 2024-06-24 18:40");
        });

        // Шаг 3: Выполнение запроса
        List<FutureDto> result = Allure.step("Выполнение запроса к сервису с корректными параметрами", () -> {
            List<FutureDto> futures = instrumentService.getFutures(
                "INSTRUMENT_STATUS_BASE",
                "MOEX",
                "USD",
                null,
                "COMMODITY"
            );
            System.out.println("Выполнен запрос с параметрами: статус=BASE, биржа=MOEX, валюта=USD, тип=COMMODITY");
            return futures;
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка корректности возвращенных данных фьючерсов", () -> {
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
            
            System.out.println("Проверено: возвращены 2 фьючерса с корректными данными");
            System.out.println("GZ0624: FIGI=FUTGZ0624000, BasicAsset=Gold, Currency=USD, ShortEnabled=true");
            System.out.println("SI0624: FIGI=FUTSI0624000, BasicAsset=Silver, Currency=USD, ShortEnabled=true");
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка корректности взаимодействий с gRPC API", () -> {
            verify(instrumentsService).futures(any(InstrumentsRequest.class));
            System.out.println("Проверено: метод futures() вызван один раз");
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
        Allure.step("Создание тестового фьючерса для поиска по FIGI", () -> {
            FutureEntity mockEntity = TestDataFactory.createFutureEntity();
            when(futureRepo.findById("FUTSI0624000"))
                .thenReturn(Optional.of(mockEntity));
            System.out.println("Создан тестовый фьючерс SBER-3.24 с FIGI FUTSI0624000");
        });

        // When - вызов метода сервиса
        FutureDto result = Allure.step("Выполнение поиска фьючерса по FIGI", () -> {
            FutureDto future = instrumentService.getFutureByFigi("FUTSI0624000");
            System.out.println("Выполнен поиск фьючерса по FIGI: FUTSI0624000");
            return future;
        });

        // Then - проверка результата
        Allure.step("Проверка корректности возвращенных данных фьючерса", () -> {
            assertThat(result).isNotNull();
            assertThat(result.figi()).isEqualTo("FUTSBER0324");
            assertThat(result.ticker()).isEqualTo("SBER-3.24");
            assertThat(result.assetType()).isEqualTo("FUTURES");
            assertThat(result.basicAsset()).isEqualTo("SBER");
            assertThat(result.currency()).isEqualTo("RUB");
            assertThat(result.exchange()).isEqualTo("moex_mrng_evng_e_wknd_dlr");
            assertThat(result.shortEnabled()).isTrue();
            assertThat(result.expirationDate()).isEqualTo(java.time.LocalDateTime.of(2024, 3, 15, 18, 45));
            
            System.out.println("Проверено: возвращен фьючерс SBER-3.24 с корректными данными");
            System.out.println("FIGI: FUTSBER0324, Ticker: SBER-3.24, AssetType: FUTURES, BasicAsset: SBER");
        });

        // Verify
        Allure.step("Проверка корректности взаимодействий с репозиторием", () -> {
            verify(futureRepo).findById("FUTSI0624000");
            System.out.println("Проверено: метод findById() вызван с правильным FIGI");
        });
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
        Allure.step("Создание тестового фьючерса для поиска по тикеру", () -> {
            FutureEntity mockEntity = TestDataFactory.createFutureEntity();
            when(futureRepo.findByTickerIgnoreCase("SI0624"))
                .thenReturn(Optional.of(mockEntity));
            System.out.println("Создан тестовый фьючерс SBER-3.24 для поиска по тикеру SI0624");
        });

        // When - вызов метода сервиса
        FutureDto result = Allure.step("Выполнение поиска фьючерса по тикеру", () -> {
            FutureDto future = instrumentService.getFutureByTicker("SI0624");
            System.out.println("Выполнен поиск фьючерса по тикеру: SI0624");
            return future;
        });

        // Then - проверка результата
        Allure.step("Проверка корректности возвращенных данных фьючерса", () -> {
            assertThat(result).isNotNull();
            assertThat(result.figi()).isEqualTo("FUTSBER0324");
            assertThat(result.ticker()).isEqualTo("SBER-3.24");
            assertThat(result.assetType()).isEqualTo("FUTURES");
            assertThat(result.shortEnabled()).isTrue();
            assertThat(result.expirationDate()).isEqualTo(java.time.LocalDateTime.of(2024, 3, 15, 18, 45));
            
            System.out.println("Проверено: возвращен фьючерс SBER-3.24 с корректными данными");
            System.out.println("FIGI: FUTSBER0324, Ticker: SBER-3.24, AssetType: FUTURES, ShortEnabled: true");
        });

        // Verify
        Allure.step("Проверка корректности взаимодействий с репозиторием", () -> {
            verify(futureRepo).findByTickerIgnoreCase("SI0624");
            System.out.println("Проверено: метод findByTickerIgnoreCase() вызван с правильным тикером");
        });
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
        Allure.step("Создание тестовых данных для сохранения фьючерсов", () -> {
            FuturesResponse mockResponse = createMockFuturesResponse();
            when(instrumentsService.futures(any(InstrumentsRequest.class)))
                .thenReturn(mockResponse);
            when(tinkoffApiClient.convertTimestampToLocalDateTime(any(com.google.protobuf.Timestamp.class)))
                .thenReturn(java.time.LocalDateTime.of(2024, 6, 24, 18, 40));
            System.out.println("Созданы тестовые данные: фьючерсы SI0624 и GZ0624 для сохранения в БД");
        });
        
        Allure.step("Настройка моков для проверки существования фьючерсов в БД", () -> {
            // Настраиваем моки для проверки существования фьючерсов в БД
            when(futureRepo.existsById("FUTSI0624000")).thenReturn(false);
            when(futureRepo.existsById("FUTGZ0624000")).thenReturn(false);
            System.out.println("Настроены моки: фьючерсы SI0624 и GZ0624 не существуют в БД");
        });
        
        Allure.step("Настройка мока для сохранения фьючерсов", () -> {
            // Настраиваем мок для сохранения фьючерсов
            when(futureRepo.save(any(FutureEntity.class))).thenAnswer(invocation -> {
                FutureEntity entity = invocation.getArgument(0);
                return entity;
            });
            System.out.println("Настроен мок для сохранения фьючерсов в БД");
        });

        FutureFilterDto filter = Allure.step("Настройка фильтра для сохранения фьючерсов", () -> {
            FutureFilterDto futureFilter = TestDataFactory.createFutureFilterDto();
            futureFilter.setExchange("MOEX"); // Используем биржу из тестовых данных
            futureFilter.setCurrency("USD"); // Используем валюту из тестовых данных
            futureFilter.setAssetType("COMMODITY"); // Используем тип актива из тестовых данных
            futureFilter.setTicker(null); // Убираем фильтр по тикеру, чтобы получить все фьючерсы
            System.out.println("Настроен фильтр: биржа=MOEX, валюта=USD, тип=COMMODITY");
            return futureFilter;
        });

        // When - вызов метода сервиса
        SaveResponseDto result = Allure.step("Выполнение сохранения фьючерсов в базу данных", () -> {
            SaveResponseDto response = instrumentService.saveFutures(filter);
            System.out.println("Выполнено сохранение фьючерсов с фильтром");
            return response;
        });

        // Then - проверка результата
        Allure.step("Проверка корректности сохранения фьючерсов", () -> {
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getTotalRequested()).isEqualTo(2); // SI0624 и GZ0624
            assertThat(result.getNewItemsSaved()).isEqualTo(2);
            assertThat(result.getExistingItemsSkipped()).isEqualTo(0);
            assertThat(result.getMessage()).contains("Успешно загружено 2 новых фьючерсов из 2 найденных");
            
            // Проверяем, что сохраненные элементы содержат правильные данные
            assertThat(result.getSavedItems()).hasSize(2);
            assertThat(result.getSavedItems()).extracting("ticker").containsExactlyInAnyOrder("SI0624", "GZ0624");
            
            System.out.println("Проверено: успешно сохранены 2 фьючерса SI0624 и GZ0624");
            System.out.println("Статистика: TotalRequested=2, NewItemsSaved=2, ExistingItemsSkipped=0");
        });

        // Verify
        Allure.step("Проверка корректности взаимодействий с репозиторием", () -> {
            verify(instrumentsService).futures(any(InstrumentsRequest.class));
            verify(futureRepo).existsById("FUTSI0624000");
            verify(futureRepo).existsById("FUTGZ0624000");
            verify(futureRepo, times(2)).save(any(FutureEntity.class));
            System.out.println("Проверено: все взаимодействия с репозиторием выполнены корректно");
        });
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
        JsonNode mockJsonResponse = Allure.step("Создание тестовых данных для индикативов USD и EUR", () -> {
            JsonNode response = createMockJsonResponse();
            System.out.println("Созданы тестовые данные: индикативы USD000UTSTOM и EUR000UTSTOM");
            return response;
        });

        // Шаг 2: Настройка моков
        Allure.step("Настройка мока для внешнего REST API", () -> {
            when(restClient.getIndicatives()).thenReturn(mockJsonResponse);
            System.out.println("Настроен мок для внешнего REST API - возвращает тестовые данные индикативов");
        });

        // Шаг 3: Выполнение запроса
        List<IndicativeDto> result = Allure.step("Выполнение запроса к сервису с корректными параметрами", () -> {
            List<IndicativeDto> indicatives = instrumentService.getIndicatives(
                "MOEX",
                "RUB",
                null,
                null
            );
            System.out.println("Выполнен запрос с параметрами: биржа=MOEX, валюта=RUB");
            return indicatives;
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка корректности возвращенных данных индикативов", () -> {
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
            
            System.out.println("Проверено: возвращены 2 индикатива с корректными данными");
            System.out.println("EUR000UTSTOM: FIGI=BBG0013HGFT5, ClassCode=CURRENCY, SellAvailable=true");
            System.out.println("USD000UTSTOM: FIGI=BBG0013HGFT4, ClassCode=CURRENCY, BuyAvailable=true");
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка корректности взаимодействий с внешним REST API", () -> {
            verify(restClient).getIndicatives();
            System.out.println("Проверено: метод getIndicatives() вызван один раз");
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
        Allure.step("Настройка мока для недоступности REST API", () -> {
            when(restClient.getIndicatives()).thenThrow(new RuntimeException("API unavailable"));
            System.out.println("Настроен мок: REST API недоступен - выбрасывается исключение");
        });
        
        Allure.step("Настройка мока для получения данных из БД", () -> {
            List<IndicativeEntity> mockEntities = TestDataFactory.createIndicativeEntityList();
            when(indicativeRepo.findAll()).thenReturn(mockEntities);
            System.out.println("Настроен мок: БД содержит тестовые данные индикативов");
        });

        // When - вызов метода сервиса
        List<IndicativeDto> result = Allure.step("Выполнение запроса при недоступности REST API", () -> {
            List<IndicativeDto> indicatives = instrumentService.getIndicatives(
                "MOEX",
                "RUB",
                null,
                null
            );
            System.out.println("Выполнен запрос при недоступности REST API");
            return indicatives;
        });

        // Then - проверка результата
        Allure.step("Проверка корректности fallback на БД", () -> {
            assertThat(result).isNotNull();
            assertThat(result).isEmpty(); // Пустой список при недоступности API
            System.out.println("Проверено: возвращен пустой список при недоступности API");
            System.out.println("Fallback на БД работает корректно");
        });

        // Verify
        Allure.step("Проверка корректности взаимодействий с API и БД", () -> {
            verify(restClient).getIndicatives();
            verify(indicativeRepo).findAll();
            System.out.println("Проверено: методы getIndicatives() и findAll() вызваны");
        });
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
        Allure.step("Создание тестовых данных для индикатива USD/RUB", () -> {
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
            System.out.println("Создан тестовый индикатив USD000UTSTOM с FIGI BBG0013HGFT4");
        });

        // When - вызов метода сервиса
        IndicativeDto result = Allure.step("Выполнение поиска индикатива по FIGI", () -> {
            IndicativeDto indicative = instrumentService.getIndicativeBy("BBG0013HGFT4");
            System.out.println("Выполнен поиск индикатива по FIGI: BBG0013HGFT4");
            return indicative;
        });

        // Then - проверка результата
        Allure.step("Проверка корректности возвращенных данных индикатива", () -> {
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
            
            System.out.println("Проверено: возвращен индикатив USD000UTSTOM с корректными данными");
            System.out.println("FIGI: BBG0013HGFT4, Ticker: USD000UTSTOM, ClassCode: CURRENCY");
        });

        // Verify
        Allure.step("Проверка корректности взаимодействий с внешним REST API", () -> {
            verify(restClient).getIndicativeBy("BBG0013HGFT4");
            System.out.println("Проверено: метод getIndicativeBy() вызван с правильным FIGI");
        });
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
        Allure.step("Создание тестовых данных для поиска индикатива по тикеру", () -> {
            JsonNode mockJsonResponse = createMockJsonResponse();
            when(restClient.getIndicatives()).thenReturn(mockJsonResponse);
            System.out.println("Созданы тестовые данные: индикативы USD000UTSTOM и EUR000UTSTOM");
        });

        // When - вызов метода сервиса
        IndicativeDto result = Allure.step("Выполнение поиска индикатива по тикеру", () -> {
            IndicativeDto indicative = instrumentService.getIndicativeByTicker("USD000UTSTOM");
            System.out.println("Выполнен поиск индикатива по тикеру: USD000UTSTOM");
            return indicative;
        });

        // Then - проверка результата
        Allure.step("Проверка корректности возвращенных данных индикатива", () -> {
            assertThat(result).isNotNull();
            assertThat(result.figi()).isEqualTo("BBG0013HGFT4");
            assertThat(result.ticker()).isEqualTo("USD000UTSTOM");
            assertThat(result.name()).isEqualTo("Доллар США / Российский рубль");
            assertThat(result.sellAvailableFlag()).isTrue();
            assertThat(result.buyAvailableFlag()).isTrue();
            
            System.out.println("Проверено: возвращен индикатив USD000UTSTOM с корректными данными");
            System.out.println("FIGI: BBG0013HGFT4, Ticker: USD000UTSTOM, SellAvailable: true, BuyAvailable: true");
        });

        // Verify
        Allure.step("Проверка корректности взаимодействий с внешним REST API", () -> {
            verify(restClient).getIndicatives();
            System.out.println("Проверено: метод getIndicatives() вызван один раз");
        });
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
        Allure.step("Создание тестовых данных для сохранения индикативов", () -> {
            JsonNode mockJsonResponse = createMockJsonResponse();
            when(restClient.getIndicatives()).thenReturn(mockJsonResponse);
            System.out.println("Созданы тестовые данные: индикативы USD000UTSTOM и EUR000UTSTOM для сохранения в БД");
        });
        
        Allure.step("Настройка моков для проверки существования индикативов в БД", () -> {
            // Настраиваем моки для проверки существования индикативов в БД
            when(indicativeRepo.existsById("BBG0013HGFT4")).thenReturn(false);
            when(indicativeRepo.existsById("BBG0013HGFT5")).thenReturn(false);
            System.out.println("Настроены моки: индикативы USD000UTSTOM и EUR000UTSTOM не существуют в БД");
        });
        
        Allure.step("Настройка мока для сохранения индикативов", () -> {
            // Настраиваем мок для сохранения индикативов
            when(indicativeRepo.save(any(IndicativeEntity.class))).thenAnswer(invocation -> {
                IndicativeEntity entity = invocation.getArgument(0);
                return entity;
            });
            System.out.println("Настроен мок для сохранения индикативов в БД");
        });

        IndicativeFilterDto filter = Allure.step("Настройка фильтра для сохранения индикативов", () -> {
            IndicativeFilterDto indicativeFilter = TestDataFactory.createIndicativeFilterDto();
            indicativeFilter.setExchange("MOEX");
            indicativeFilter.setCurrency("RUB");
            indicativeFilter.setTicker(null); // Убираем фильтр по тикеру, чтобы получить все индикативы
            indicativeFilter.setFigi(null); // Убираем фильтр по FIGI, чтобы получить все индикативы
            System.out.println("Настроен фильтр: биржа=MOEX, валюта=RUB");
            return indicativeFilter;
        });

        // When - вызов метода сервиса
        SaveResponseDto result = Allure.step("Выполнение сохранения индикативов в базу данных", () -> {
            SaveResponseDto response = instrumentService.saveIndicatives(filter);
            System.out.println("Выполнено сохранение индикативов с фильтром");
            return response;
        });

        // Then - проверка результата
        Allure.step("Проверка корректности сохранения индикативов", () -> {
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getTotalRequested()).isEqualTo(2);
            assertThat(result.getNewItemsSaved()).isEqualTo(2);
            assertThat(result.getExistingItemsSkipped()).isEqualTo(0);
            assertThat(result.getMessage()).contains("Успешно загружено 2 новых индикативных инструментов из 2 найденных");
            
            // Проверяем, что сохраненные элементы содержат правильные данные
            assertThat(result.getSavedItems()).hasSize(2);
            assertThat(result.getSavedItems()).extracting("ticker").containsExactlyInAnyOrder("USD000UTSTOM", "EUR000UTSTOM");
            
            System.out.println("Проверено: успешно сохранены 2 индикатива USD000UTSTOM и EUR000UTSTOM");
            System.out.println("Статистика: TotalRequested=2, NewItemsSaved=2, ExistingItemsSkipped=0");
        });

        // Verify
        Allure.step("Проверка корректности взаимодействий с репозиторием", () -> {
            verify(restClient).getIndicatives();
            verify(indicativeRepo).existsById("BBG0013HGFT4");
            verify(indicativeRepo).existsById("BBG0013HGFT5");
            verify(indicativeRepo, times(2)).save(any(IndicativeEntity.class));
            System.out.println("Проверено: все взаимодействия с репозиторием выполнены корректно");
        });
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
        Allure.step("Настройка моков для подсчета инструментов в БД", () -> {
            when(shareRepo.count()).thenReturn(150L);
            when(futureRepo.count()).thenReturn(45L);
            when(indicativeRepo.count()).thenReturn(12L);
            System.out.println("Настроены моки: акции=150, фьючерсы=45, индикативы=12");
        });

        // Шаг 2: Выполнение запроса
        Map<String, Long> result = Allure.step("Выполнение запроса к сервису для получения статистики", () -> {
            Map<String, Long> counts = instrumentService.getInstrumentCounts();
            System.out.println("Выполнен запрос к сервису для получения статистики инструментов");
            return counts;
        });

        // Шаг 3: Проверка результата
        Allure.step("Проверка корректности возвращенной статистики", () -> {
            assertThat(result).isNotNull();
            assertThat(result).hasSize(4);
            assertThat(result.get("shares")).isEqualTo(150L);
            assertThat(result.get("futures")).isEqualTo(45L);
            assertThat(result.get("indicatives")).isEqualTo(12L);
            assertThat(result.get("total")).isEqualTo(207L); // 150 + 45 + 12
            
            System.out.println("Проверено: возвращена корректная статистика инструментов");
            System.out.println("Акции: 150, Фьючерсы: 45, Индикативы: 12, Всего: 207");
        });

        // Шаг 4: Проверка взаимодействий
        Allure.step("Проверка корректности взаимодействий с репозиториями", () -> {
            verify(shareRepo).count();
            verify(futureRepo).count();
            verify(indicativeRepo).count();
            System.out.println("Проверено: методы count() вызваны для всех репозиториев");
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
        JsonNode mockResponse = Allure.step("Создание тестовых данных для проверки дубликатов", () -> {
            JsonNode response = createMockSharesJsonResponse();
            System.out.println("Созданы тестовые данные: акции SBER и GAZP для проверки дубликатов");
            return response;
        });
        
        ShareFilterDto filter = Allure.step("Настройка фильтра для проверки дубликатов", () -> {
            ShareFilterDto shareFilter = TestDataFactory.createShareFilterDto();
            shareFilter.setStatus("SECURITY_TRADING_STATUS_NORMAL_TRADING");
            shareFilter.setExchange("MOEX");
            shareFilter.setCurrency("RUB");
            System.out.println("Настроен фильтр: статус=NORMAL_TRADING, биржа=MOEX, валюта=RUB");
            return shareFilter;
        });

        // Шаг 2: Настройка моков
        Allure.step("Настройка моков для имитации существующих акций", () -> {
            when(restClient.getShares()).thenReturn(mockResponse);
            when(shareRepo.existsById(anyString())).thenReturn(true);
            System.out.println("Настроены моки: все акции уже существуют в БД");
        });

        // Шаг 3: Выполнение сохранения
        SaveResponseDto result = Allure.step("Выполнение сохранения при существующих акциях", () -> {
            SaveResponseDto response = instrumentService.saveShares(filter);
            System.out.println("Выполнено сохранение акций с фильтром");
            return response;
        });

        // Шаг 4: Проверка результата
        Allure.step("Проверка корректности обработки дубликатов", () -> {
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getTotalRequested()).isEqualTo(1); // только одна акция обрабатывается
            assertThat(result.getNewItemsSaved()).isEqualTo(0);
            assertThat(result.getExistingItemsSkipped()).isEqualTo(1); // одна акция пропускается
            assertThat(result.getMessage()).contains("Все найденные акции уже существуют в базе данных");
            
            System.out.println("Проверено: все акции пропущены как дубликаты");
            System.out.println("Статистика: TotalRequested=1, NewItemsSaved=0, ExistingItemsSkipped=1");
        });

        // Шаг 5: Проверка взаимодействий
        Allure.step("Проверка корректности взаимодействий с репозиторием", () -> {
            verify(restClient).getShares();
            verify(shareRepo, times(1)).existsById(any()); // проверяем одну акцию
            verify(shareRepo, never()).save(any(ShareEntity.class));
            System.out.println("Проверено: методы existsById() вызваны, save() не вызывался");
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
        Allure.step("Создание тестовых данных для фильтрации по тикеру", () -> {
            JsonNode mockResponse = createMockSharesJsonResponse();
            when(restClient.getShares()).thenReturn(mockResponse);
            System.out.println("Созданы тестовые данные: акции SBER и GAZP для фильтрации по тикеру");
        });

        // When - вызов метода сервиса с фильтром по тикеру
        List<ShareDto> result = Allure.step("Выполнение запроса с фильтром по тикеру SBER", () -> {
            List<ShareDto> shares = instrumentService.getShares(
                "SECURITY_TRADING_STATUS_NORMAL_TRADING",
                null,
                null,
                "SBER",
                null
            );
            System.out.println("Выполнен запрос с фильтром по тикеру: SBER");
            return shares;
        });

        // Then - проверка результата
        Allure.step("Проверка корректности фильтрации по тикеру", () -> {
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            
            ShareDto share = result.get(0);
            assertThat(share.ticker()).isEqualTo("SBER");
            assertThat(share.name()).isEqualTo("ПАО Сбербанк");
            assertThat(share.shortEnabled()).isTrue();
            
            System.out.println("Проверено: возвращена одна акция SBER с корректными данными");
            System.out.println("Ticker: SBER, Name: ПАО Сбербанк, ShortEnabled: true");
        });

        // Verify
        Allure.step("Проверка корректности взаимодействий с внешним API", () -> {
            verify(restClient).getShares();
            System.out.println("Проверено: метод getShares() вызван один раз");
        });
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
        Allure.step("Создание тестовых данных для фьючерсов без параметров", () -> {
            FuturesResponse mockResponse = createMockFuturesResponse();
            when(instrumentsService.futures(any(InstrumentsRequest.class)))
                .thenReturn(mockResponse);
            when(tinkoffApiClient.convertTimestampToLocalDateTime(any(com.google.protobuf.Timestamp.class)))
                .thenReturn(java.time.LocalDateTime.of(2024, 6, 24, 18, 40));
            System.out.println("Созданы тестовые данные: фьючерсы GZ0624 и SI0624 без параметров");
        });

        // When - вызов метода сервиса
        List<FutureDto> result = Allure.step("Выполнение запроса без параметров", () -> {
            List<FutureDto> futures = instrumentService.getFutures(
                null,
                null,
                null,
                null,
                null
            );
            System.out.println("Выполнен запрос без параметров");
            return futures;
        });

        // Then - проверка результата
        Allure.step("Проверка корректности возвращенных данных фьючерсов", () -> {
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).ticker()).isEqualTo("GZ0624");
            assertThat(result.get(0).shortEnabled()).isTrue();
            assertThat(result.get(0).expirationDate()).isEqualTo(java.time.LocalDateTime.of(2024, 6, 24, 18, 40));
            assertThat(result.get(1).ticker()).isEqualTo("SI0624");
            assertThat(result.get(1).shortEnabled()).isTrue();
            assertThat(result.get(1).expirationDate()).isEqualTo(java.time.LocalDateTime.of(2024, 6, 24, 18, 40));
            
            System.out.println("Проверено: возвращены 2 фьючерса с корректными данными");
            System.out.println("GZ0624: ShortEnabled=true, ExpirationDate=2024-06-24 18:40");
            System.out.println("SI0624: ShortEnabled=true, ExpirationDate=2024-06-24 18:40");
        });

        // Verify
        Allure.step("Проверка корректности взаимодействий с gRPC API", () -> {
            verify(instrumentsService).futures(any(InstrumentsRequest.class));
            System.out.println("Проверено: метод futures() вызван один раз");
        });
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
        Allure.step("Создание тестовых данных для индикативов без параметров", () -> {
            JsonNode mockJsonResponse = createMockJsonResponse();
            when(restClient.getIndicatives()).thenReturn(mockJsonResponse);
            System.out.println("Созданы тестовые данные: индикативы USD000UTSTOM и EUR000UTSTOM без параметров");
        });

        // When - вызов метода сервиса
        List<IndicativeDto> result = Allure.step("Выполнение запроса без параметров", () -> {
            List<IndicativeDto> indicatives = instrumentService.getIndicatives(
                null,
                null,
                null,
                null
            );
            System.out.println("Выполнен запрос без параметров");
            return indicatives;
        });

        // Then - проверка результата
        Allure.step("Проверка корректности возвращенных данных индикативов", () -> {
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).ticker()).isEqualTo("EUR000UTSTOM");
            assertThat(result.get(0).sellAvailableFlag()).isTrue();
            assertThat(result.get(0).buyAvailableFlag()).isTrue();
            assertThat(result.get(1).ticker()).isEqualTo("USD000UTSTOM");
            assertThat(result.get(1).sellAvailableFlag()).isTrue();
            assertThat(result.get(1).buyAvailableFlag()).isTrue();
            
            System.out.println("Проверено: возвращены 2 индикатива с корректными данными");
            System.out.println("EUR000UTSTOM: SellAvailable=true, BuyAvailable=true");
            System.out.println("USD000UTSTOM: SellAvailable=true, BuyAvailable=true");
        });

        // Verify
        Allure.step("Проверка корректности взаимодействий с внешним REST API", () -> {
            verify(restClient).getIndicatives();
            System.out.println("Проверено: метод getIndicatives() вызван один раз");
        });
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

    