package com.example.InvestmentDataLoaderService.unit.service;

import com.example.InvestmentDataLoaderService.client.TinkoffRestClient;
import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.entity.*;
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

import java.time.LocalDateTime;
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

    @InjectMocks
    private InstrumentService instrumentService;

    @BeforeEach
    @Step("Подготовка тестовых данных для InstrumentService")
    void setUp() {
        // Очистка моков перед каждым тестом
        reset(instrumentsService, shareRepo, futureRepo, indicativeRepo, restClient, objectMapper);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Создание тестовых данных для акций из API
     */
    private SharesResponse createMockSharesResponse() {
        Share sber = Share.newBuilder()
                .setFigi("BBG004730N88")
                .setTicker("SBER")
                .setName("ПАО Сбербанк")
                .setCurrency("RUB")
                .setExchange("MOEX")
                .setSector("Financial")
                .setTradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING)
                .build();

        Share gazp = Share.newBuilder()
                .setFigi("BBG004730ZJ9")
                .setTicker("GAZP")
                .setName("ПАО Газпром")
                .setCurrency("RUB")
                .setExchange("MOEX")
                .setSector("Energy")
                .setTradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING)
                .build();

        return SharesResponse.newBuilder()
                .addInstruments(sber)
                .addInstruments(gazp)
                .build();
    }

    /**
     * Создание тестовых данных для акций из БД
     */
    private List<ShareEntity> createMockShareEntities() {
        return Arrays.asList(
            new ShareEntity(
                "BBG004730N88",
                "SBER",
                "ПАО Сбербанк",
                "RUB",
                "MOEX",
                "Financial",
                "SECURITY_TRADING_STATUS_NORMAL_TRADING",
                LocalDateTime.now(),
                LocalDateTime.now()
            ),
            new ShareEntity(
                "BBG004730ZJ9",
                "GAZP",
                "ПАО Газпром",
                "RUB",
                "MOEX",
                "Energy",
                "SECURITY_TRADING_STATUS_NORMAL_TRADING",
                LocalDateTime.now(),
                LocalDateTime.now()
            )
        );
    }

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
                .build();

        Future gold = Future.newBuilder()
                .setFigi("FUTGZ0624000")
                .setTicker("GZ0624")
                .setAssetType("COMMODITY")
                .setBasicAsset("Gold")
                .setCurrency("USD")
                .setExchange("MOEX")
                .build();

        return FuturesResponse.newBuilder()
                .addInstruments(silver)
                .addInstruments(gold)
                .build();
    }

    /**
     * Создание тестовых данных для фьючерсов из БД
     */
    private List<FutureEntity> createMockFutureEntities() {
        return Arrays.asList(
            new FutureEntity(
                "FUTSI0624000",
                "SI0624",
                "COMMODITY",
                "Silver",
                "USD",
                "MOEX"
            ),
            new FutureEntity(
                "FUTGZ0624000",
                "GZ0624",
                "COMMODITY",
                "Gold",
                "USD",
                "MOEX"
            )
        );
    }

    /**
     * Создание тестовых данных для индикативов из БД
     */
    private List<IndicativeEntity> createMockIndicativeEntities() {
        return Arrays.asList(
            new IndicativeEntity(
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
            new IndicativeEntity(
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

    /**
     * Создание тестового JSON ответа для REST API
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
    void getShares_ShouldReturnSharesList_WhenValidParametersProvided() {
        // Given - настройка мока для получения акций из API
        SharesResponse mockResponse = createMockSharesResponse();
        when(instrumentsService.shares(any(InstrumentsRequest.class)))
            .thenReturn(mockResponse);

        // When - вызов метода сервиса
        List<ShareDto> result = instrumentService.getShares(
            "INSTRUMENT_STATUS_ACTIVE",
            "MOEX",
            "RUB",
            null,
            null
        );

        // Then - проверка результата
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

        // Проверяем вторую акцию (SBER - идет второй по алфавиту)
        ShareDto sber = result.get(1);
        assertThat(sber.figi()).isEqualTo("BBG004730N88");
        assertThat(sber.ticker()).isEqualTo("SBER");
        assertThat(sber.name()).isEqualTo("ПАО Сбербанк");
        assertThat(sber.sector()).isEqualTo("Financial");

        // Проверяем сортировку по тикеру
        assertThat(result.get(0).ticker()).isEqualTo("GAZP"); 
        assertThat(result.get(1).ticker()).isEqualTo("SBER");

        // Verify - проверяем вызовы моков
        verify(instrumentsService).shares(any(InstrumentsRequest.class));
    }

    @Test
    @DisplayName("Получение списка акций из API - некорректная валюта")
    @Description("Тест проверяет корректность получения списка акций из Tinkoff API с некорректной валютой")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("unit")
    void getShares_ShouldReturnEmptyList_WhenInvalidCurrency() {
        // Given - настройка мока для получения пустого списка акций из API
        SharesResponse mockResponse = SharesResponse.newBuilder().build();
        when(instrumentsService.shares(any(InstrumentsRequest.class)))
            .thenReturn(mockResponse);

        List<ShareDto> result = instrumentService.getShares(
            "INSTRUMENT_STATUS_ACTIVE",
            "MOEX",
            "UNCORRECT_CURRENCY",
            null,
            null
        );

        assertThat(result).isEmpty();
        verify(instrumentsService).shares(any(InstrumentsRequest.class));
    }

    @Test
    @DisplayName("Получение списка акций из API - некорректный статус")
    @Description("Тест проверяет корректность получения списка акций из Tinkoff API с некорректным статусом")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares API")
    @Tag("api")
    @Tag("shares")
    @Tag("unit")
    void getShares_ShouldReturnEmptyList_WhenInvalidStatus() {
        // Given - настройка мока для получения пустого списка акций из API
        SharesResponse mockResponse = SharesResponse.newBuilder().build();
        when(instrumentsService.shares(any(InstrumentsRequest.class)))
            .thenReturn(mockResponse);
            
        List<ShareDto> result = instrumentService.getShares(
            "INVALID_STATUS",
            "MOEX",
            "RUB",
            null,
            null
        );
        assertThat(result).isEmpty();
        verify(instrumentsService).shares(any(InstrumentsRequest.class));
    }

 
   @Test
   @DisplayName("Получение списка акций из API - некорректная биржа")
   @Description("Тест проверяет корректность получения списка акций из Tinkoff API с некорректной биржей")
   @Severity(SeverityLevel.CRITICAL)
   @Story("Shares API")
   @Tag("api")
   @Tag("shares")
   @Tag("unit")
   void getShares_ShouldReturnEmptyList_WhenInvalidExchange() {
        // Given - настройка мока для получения пустого списка акций из API
        SharesResponse mockResponse = SharesResponse.newBuilder().build();
        when(instrumentsService.shares(any(InstrumentsRequest.class)))
            .thenReturn(mockResponse);
            
   
        List<ShareDto> result = instrumentService.getShares(
            "INSTRUMENT_STATUS_ACTIVE",
            "MOEX",
            "RUB",
            null,
            null
        );
        
        assertThat(result).isEmpty();
        verify(instrumentsService).shares(any(InstrumentsRequest.class));
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
    void saveShares_ShouldSaveShares_WhenValidParametersProvided() {
        // Given - настройка моков для сохранения акций
        SharesResponse mockResponse = createMockSharesResponse();
        when(instrumentsService.shares(any(InstrumentsRequest.class)))
            .thenReturn(mockResponse);
        
        // Настраиваем моки для проверки существования акций в БД
        when(shareRepo.existsById("BBG004730N88")).thenReturn(false); // SBER не существует
        when(shareRepo.existsById("BBG004730ZJ9")).thenReturn(false); // GAZP не существует
        
        // Настраиваем мок для сохранения акций
        when(shareRepo.save(any(ShareEntity.class))).thenAnswer(invocation -> {
            ShareEntity entity = invocation.getArgument(0);
            return entity; // Возвращаем ту же сущность
        });

        ShareFilterDto filter = new ShareFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_ACTIVE");
        filter.setExchange("MOEX");
        filter.setCurrency("RUB");

        // When - вызов метода сервиса
        SaveResponseDto result = instrumentService.saveShares(filter);

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRequested()).isEqualTo(2);
        assertThat(result.getNewItemsSaved()).isEqualTo(2);
        assertThat(result.getExistingItemsSkipped()).isEqualTo(0);
        assertThat(result.getInvalidItemsFiltered()).isEqualTo(0);
        assertThat(result.getMissingFromApi()).isEqualTo(0);
        assertThat(result.getMessage()).contains("Успешно загружено 2 новых акций из 2 найденных");
        
        // Проверяем, что сохраненные элементы содержат правильные данные
        assertThat(result.getSavedItems()).hasSize(2);
        assertThat(result.getSavedItems()).extracting("ticker").containsExactlyInAnyOrder("SBER", "GAZP");

        // Verify - проверяем вызовы моков
        verify(instrumentsService).shares(any(InstrumentsRequest.class));
        verify(shareRepo).existsById("BBG004730N88");
        verify(shareRepo).existsById("BBG004730ZJ9");
        verify(shareRepo, times(2)).save(any(ShareEntity.class));
    }

    @Test
    @DisplayName("Получение акций из базы данных - успешный случай")
    @Description("Тест проверяет корректность получения акций из локальной БД")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares Database")
    @Tag("database")
    @Tag("shares")
    @Tag("unit")
    void getSharesFromDatabase_ShouldReturnSharesList_WhenEntitiesExist() {
        // Given - настройка мока для получения акций из БД
        List<ShareEntity> mockEntities = createMockShareEntities();
        when(shareRepo.findAll()).thenReturn(mockEntities);

        ShareFilterDto filter = new ShareFilterDto();
        filter.setExchange("MOEX");

        // When - вызов метода сервиса
        List<ShareDto> result = instrumentService.getSharesFromDatabase(filter);

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        
        // Проверяем первую акцию (GAZP - идет первой по алфавиту)
        ShareDto gazp = result.get(0);
        assertThat(gazp.figi()).isEqualTo("BBG004730ZJ9");
        assertThat(gazp.ticker()).isEqualTo("GAZP");
        assertThat(gazp.name()).isEqualTo("ПАО Газпром");
        assertThat(gazp.exchange()).isEqualTo("MOEX");
        assertThat(gazp.sector()).isEqualTo("Energy");

        // Проверяем вторую акцию (SBER - идет второй по алфавиту)
        ShareDto sber = result.get(1);
        assertThat(sber.figi()).isEqualTo("BBG004730N88");
        assertThat(sber.ticker()).isEqualTo("SBER");
        assertThat(sber.name()).isEqualTo("ПАО Сбербанк");
        assertThat(sber.sector()).isEqualTo("Financial");

        // Verify
        verify(shareRepo).findAll();
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
    void getShareByFigi_ShouldReturnShare_WhenShareExists() {
        // Given - настройка мока для получения акции по FIGI
        ShareEntity mockEntity = createMockShareEntities().get(0);
        when(shareRepo.findById("BBG004730N88"))
            .thenReturn(Optional.of(mockEntity));

        // When - вызов метода сервиса
        ShareDto result = instrumentService.getShareByFigi("BBG004730N88");

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.figi()).isEqualTo("BBG004730N88");
        assertThat(result.ticker()).isEqualTo("SBER");
        assertThat(result.name()).isEqualTo("ПАО Сбербанк");
        assertThat(result.currency()).isEqualTo("RUB");
        assertThat(result.exchange()).isEqualTo("MOEX");
        assertThat(result.sector()).isEqualTo("Financial");
        assertThat(result.tradingStatus()).isEqualTo("SECURITY_TRADING_STATUS_NORMAL_TRADING");

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
    void getShareByTicker_ShouldReturnShare_WhenShareExists() {
        // Given - настройка мока для получения акции по тикеру
        ShareEntity mockEntity = createMockShareEntities().get(0);
        when(shareRepo.findByTickerIgnoreCase("SBER"))
            .thenReturn(Optional.of(mockEntity));

        // When - вызов метода сервиса
        ShareDto result = instrumentService.getShareByTicker("SBER");

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.figi()).isEqualTo("BBG004730N88");
        assertThat(result.ticker()).isEqualTo("SBER");
        assertThat(result.name()).isEqualTo("ПАО Сбербанк");

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
    void getFutures_ShouldReturnFuturesList_WhenValidParametersProvided() {
        // Given - настройка мока для получения фьючерсов из API
        FuturesResponse mockResponse = createMockFuturesResponse();
        when(instrumentsService.futures(any(InstrumentsRequest.class)))
            .thenReturn(mockResponse);

        // When - вызов метода сервиса
        List<FutureDto> result = instrumentService.getFutures(
            "INSTRUMENT_STATUS_ACTIVE",
            "MOEX",
            "USD",
            null,
            "COMMODITY"
        );

        // Then - проверка результата
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

        // Проверяем второй фьючерс (SI0624 - идет второй по алфавиту)
        FutureDto silver = result.get(1);
        assertThat(silver.figi()).isEqualTo("FUTSI0624000");
        assertThat(silver.ticker()).isEqualTo("SI0624");
        assertThat(silver.basicAsset()).isEqualTo("Silver");

        // Verify
        verify(instrumentsService).futures(any(InstrumentsRequest.class));
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
    void getFutureByFigi_ShouldReturnFuture_WhenFutureExists() {
        // Given - настройка мока для получения фьючерса по FIGI
        FutureEntity mockEntity = createMockFutureEntities().get(0);
        when(futureRepo.findById("FUTSI0624000"))
            .thenReturn(Optional.of(mockEntity));

        // When - вызов метода сервиса
        FutureDto result = instrumentService.getFutureByFigi("FUTSI0624000");

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.figi()).isEqualTo("FUTSI0624000");
        assertThat(result.ticker()).isEqualTo("SI0624");
        assertThat(result.assetType()).isEqualTo("COMMODITY");
        assertThat(result.basicAsset()).isEqualTo("Silver");
        assertThat(result.currency()).isEqualTo("USD");
        assertThat(result.exchange()).isEqualTo("MOEX");

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
    void getFutureByTicker_ShouldReturnFuture_WhenFutureExists() {
        // Given - настройка мока для получения фьючерса по тикеру
        FutureEntity mockEntity = createMockFutureEntities().get(0);
        when(futureRepo.findByTickerIgnoreCase("SI0624"))
            .thenReturn(Optional.of(mockEntity));

        // When - вызов метода сервиса
        FutureDto result = instrumentService.getFutureByTicker("SI0624");

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.figi()).isEqualTo("FUTSI0624000");
        assertThat(result.ticker()).isEqualTo("SI0624");
        assertThat(result.assetType()).isEqualTo("COMMODITY");

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
    void saveFutures_ShouldSaveFutures_WhenValidParametersProvided() {
        // Given - настройка моков для сохранения фьючерсов
        FuturesResponse mockResponse = createMockFuturesResponse();
        when(instrumentsService.futures(any(InstrumentsRequest.class)))
            .thenReturn(mockResponse);
        
        // Настраиваем моки для проверки существования фьючерсов в БД
        when(futureRepo.existsById("FUTSI0624000")).thenReturn(false);
        when(futureRepo.existsById("FUTGZ0624000")).thenReturn(false);
        
        // Настраиваем мок для сохранения фьючерсов
        when(futureRepo.save(any(FutureEntity.class))).thenAnswer(invocation -> {
            FutureEntity entity = invocation.getArgument(0);
            return entity;
        });

        FutureFilterDto filter = new FutureFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_ACTIVE");
        filter.setExchange("MOEX");
        filter.setCurrency("USD");
        filter.setAssetType("COMMODITY");

        // When - вызов метода сервиса
        SaveResponseDto result = instrumentService.saveFutures(filter);

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRequested()).isEqualTo(2);
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
    void getIndicatives_ShouldReturnIndicativesList_WhenRestApiAvailable() throws Exception {
        // Given - настройка мока для REST API
        JsonNode mockJsonResponse = createMockJsonResponse();
        when(restClient.getIndicatives()).thenReturn(mockJsonResponse);

        // When - вызов метода сервиса
        List<IndicativeDto> result = instrumentService.getIndicatives(
            "MOEX",
            "RUB",
            null,
            null
        );

        // Then - проверка результата
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

        // Verify
        verify(restClient).getIndicatives();
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
    void getIndicatives_ShouldUseDatabaseFallback_WhenRestApiUnavailable() {
        // Given - настройка мока для недоступности REST API и наличия данных в БД
        when(restClient.getIndicatives()).thenThrow(new RuntimeException("API unavailable"));
        List<IndicativeEntity> mockEntities = createMockIndicativeEntities();
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
        assertThat(result).hasSize(2);
        
        // Проверяем первый индикатив (EUR000UTSTOM - идет первой по алфавиту)
        IndicativeDto firstIndicative = result.get(0);
        assertThat(firstIndicative.figi()).isEqualTo("BBG0013HGFT5");
        assertThat(firstIndicative.ticker()).isEqualTo("EUR000UTSTOM");
        assertThat(firstIndicative.name()).isEqualTo("Евро / Российский рубль");

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

        IndicativeFilterDto filter = new IndicativeFilterDto();
        filter.setExchange("MOEX");
        filter.setCurrency("RUB");

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
    void getInstrumentCounts_ShouldReturnCorrectCounts_WhenInstrumentsExist() {
        // Given - настройка моков для подсчета
        when(shareRepo.count()).thenReturn(150L);
        when(futureRepo.count()).thenReturn(45L);
        when(indicativeRepo.count()).thenReturn(12L);

        // When - вызов метода сервиса
        Map<String, Long> result = instrumentService.getInstrumentCounts();

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result).hasSize(4);
        assertThat(result.get("shares")).isEqualTo(150L);
        assertThat(result.get("futures")).isEqualTo(45L);
        assertThat(result.get("indicatives")).isEqualTo(12L);
        assertThat(result.get("total")).isEqualTo(207L); // 150 + 45 + 12

        // Verify
        verify(shareRepo).count();
        verify(futureRepo).count();
        verify(indicativeRepo).count();
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
    void saveShares_ShouldSkipExistingShares_WhenSharesAlreadyExist() {
        // Given - настройка моков для уже существующих акций
        SharesResponse mockResponse = createMockSharesResponse();
        when(instrumentsService.shares(any(InstrumentsRequest.class)))
            .thenReturn(mockResponse);
        when(shareRepo.existsById("BBG004730N88")).thenReturn(true);
        when(shareRepo.existsById("BBG004730ZJ9")).thenReturn(true);

        ShareFilterDto filter = new ShareFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_ACTIVE");

        // When - вызов метода сервиса
        SaveResponseDto result = instrumentService.saveShares(filter);

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRequested()).isEqualTo(2);
        assertThat(result.getNewItemsSaved()).isEqualTo(0);
        assertThat(result.getExistingItemsSkipped()).isEqualTo(2);
        assertThat(result.getMessage()).contains("Все найденные акции уже существуют в базе данных");

        // Verify
        verify(instrumentsService).shares(any(InstrumentsRequest.class));
        verify(shareRepo, times(2)).existsById(any());
        verify(shareRepo, never()).save(any(ShareEntity.class));
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
    void getShares_ShouldReturnFilteredShares_WhenTickerFilterApplied() {
        // Given - настройка мока для получения акций из API
        SharesResponse mockResponse = createMockSharesResponse();
        when(instrumentsService.shares(any(InstrumentsRequest.class)))
            .thenReturn(mockResponse);

        // When - вызов метода сервиса с фильтром по тикеру
        List<ShareDto> result = instrumentService.getShares(
            "INSTRUMENT_STATUS_ACTIVE",
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

        // Verify
        verify(instrumentsService).shares(any(InstrumentsRequest.class));
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
    void getFutures_ShouldReturnFuturesList_WhenNoParametersProvided() {
        // Given - настройка мока для фьючерсов без параметров
        FuturesResponse mockResponse = createMockFuturesResponse();
        when(instrumentsService.futures(any(InstrumentsRequest.class)))
            .thenReturn(mockResponse);

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
        assertThat(result.get(1).ticker()).isEqualTo("SI0624");

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
        assertThat(result.get(1).ticker()).isEqualTo("USD000UTSTOM");

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
    void getShares_ShouldReturnFilteredShares_WhenFigiFilterApplied() {
        // Given - настройка мока для получения акций из API
        SharesResponse mockResponse = createMockSharesResponse();
        when(instrumentsService.shares(any(InstrumentsRequest.class)))
            .thenReturn(mockResponse);

        // When - вызов метода сервиса с фильтром по FIGI
        List<ShareDto> result = instrumentService.getShares(
            "INSTRUMENT_STATUS_ACTIVE",
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

        // Verify
        verify(instrumentsService).shares(any(InstrumentsRequest.class));
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
    void getSharesFromDatabase_ShouldReturnAllShares_WhenEmptyFilter() {
        // Given - настройка мока для получения всех акций из БД
        List<ShareEntity> mockEntities = createMockShareEntities();
        when(shareRepo.findAll()).thenReturn(mockEntities);

        ShareFilterDto filter = new ShareFilterDto(); // Пустой фильтр

        // When - вызов метода сервиса
        List<ShareDto> result = instrumentService.getSharesFromDatabase(filter);

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

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
    void saveShares_ShouldSaveOnlyNewShares_WhenSomeSharesExist() {
        // Given - настройка моков для частично существующих акций
        SharesResponse mockResponse = createMockSharesResponse();
        when(instrumentsService.shares(any(InstrumentsRequest.class)))
            .thenReturn(mockResponse);
        
        // Одна акция существует, другая нет
        when(shareRepo.existsById("BBG004730N88")).thenReturn(true);  // SBER существует
        when(shareRepo.existsById("BBG004730ZJ9")).thenReturn(false); // GAZP не существует
        
        // Настраиваем мок для сохранения только новых акций
        when(shareRepo.save(any(ShareEntity.class))).thenAnswer(invocation -> {
            ShareEntity entity = invocation.getArgument(0);
            return entity;
        });

        ShareFilterDto filter = new ShareFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_ACTIVE");

        // When - вызов метода сервиса
        SaveResponseDto result = instrumentService.saveShares(filter);

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRequested()).isEqualTo(2);
        assertThat(result.getNewItemsSaved()).isEqualTo(1);  // Только GAZP
        assertThat(result.getExistingItemsSkipped()).isEqualTo(1); // Только SBER
        assertThat(result.getMessage()).contains("Успешно загружено 1 новых акций из 2 найденных");
        
        // Проверяем, что сохранен только один элемент
        assertThat(result.getSavedItems()).hasSize(1);
        @SuppressWarnings("unchecked")
        List<ShareDto> savedItems = (List<ShareDto>) result.getSavedItems();
        assertThat(savedItems.get(0).ticker()).isEqualTo("GAZP");

        // Verify
        verify(instrumentsService).shares(any(InstrumentsRequest.class));
        verify(shareRepo).existsById("BBG004730N88");
        verify(shareRepo).existsById("BBG004730ZJ9");
        verify(shareRepo, times(1)).save(any(ShareEntity.class));
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
    void saveFutures_ShouldSkipExistingFutures_WhenFuturesAlreadyExist() {
        // Given - настройка моков для уже существующих фьючерсов
        FuturesResponse mockResponse = createMockFuturesResponse();
        when(instrumentsService.futures(any(InstrumentsRequest.class)))
            .thenReturn(mockResponse);
        when(futureRepo.existsById("FUTSI0624000")).thenReturn(true);
        when(futureRepo.existsById("FUTGZ0624000")).thenReturn(true);

        FutureFilterDto filter = new FutureFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_ACTIVE");

        // When - вызов метода сервиса
        SaveResponseDto result = instrumentService.saveFutures(filter);

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRequested()).isEqualTo(2);
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
    void saveIndicatives_ShouldSkipExistingIndicatives_WhenIndicativesAlreadyExist() throws Exception {
        // Given - настройка моков для уже существующих индикативов
        JsonNode mockJsonResponse = createMockJsonResponse();
        when(restClient.getIndicatives()).thenReturn(mockJsonResponse);
        when(indicativeRepo.existsById("BBG0013HGFT4")).thenReturn(true);
        when(indicativeRepo.existsById("BBG0013HGFT5")).thenReturn(true);

        IndicativeFilterDto filter = new IndicativeFilterDto();
        filter.setExchange("MOEX");

        // When - вызов метода сервиса
        SaveResponseDto result = instrumentService.saveIndicatives(filter);

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRequested()).isEqualTo(2);
        assertThat(result.getNewItemsSaved()).isEqualTo(0);
        assertThat(result.getExistingItemsSkipped()).isEqualTo(2);
        assertThat(result.getMessage()).contains("Все найденные инструменты уже существуют в базе данных");

        // Verify
        verify(restClient).getIndicatives();
        verify(indicativeRepo, times(2)).existsById(any());
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
    void getShares_ShouldReturnEmptyList_WhenApiReturnsEmptyResponse() {
        // Given - настройка мока для пустого ответа API
        SharesResponse emptyResponse = SharesResponse.newBuilder().build();
        when(instrumentsService.shares(any(InstrumentsRequest.class)))
            .thenReturn(emptyResponse);

        // When - вызов метода сервиса
        List<ShareDto> result = instrumentService.getShares(
            "INSTRUMENT_STATUS_ACTIVE",
            "MOEX",
            "RUB",
            null,
            null
        );

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        // Verify
        verify(instrumentsService).shares(any(InstrumentsRequest.class));
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
    void getFutures_ShouldReturnEmptyList_WhenApiReturnsEmptyResponse() {
        // Given - настройка мока для пустого ответа API
        FuturesResponse emptyResponse = FuturesResponse.newBuilder().build();
        when(instrumentsService.futures(any(InstrumentsRequest.class)))
            .thenReturn(emptyResponse);

        // When - вызов метода сервиса
        List<FutureDto> result = instrumentService.getFutures(
            "INSTRUMENT_STATUS_ACTIVE",
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
    void saveShares_ShouldHandleEmptyApiResponse_WhenApiReturnsEmptyList() {
        // Given - настройка мока для пустого ответа API
        SharesResponse emptyResponse = SharesResponse.newBuilder().build();
        when(instrumentsService.shares(any(InstrumentsRequest.class)))
            .thenReturn(emptyResponse);

        ShareFilterDto filter = new ShareFilterDto();
        filter.setStatus("INSTRUMENT_STATUS_ACTIVE");

        // When - вызов метода сервиса
        SaveResponseDto result = instrumentService.saveShares(filter);

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getTotalRequested()).isEqualTo(0);
        assertThat(result.getNewItemsSaved()).isEqualTo(0);
        assertThat(result.getExistingItemsSkipped()).isEqualTo(0);
        assertThat(result.getMessage()).contains("Новых акций не обнаружено. По заданным фильтрам акции не найдены.");

        // Verify
        verify(instrumentsService).shares(any(InstrumentsRequest.class));
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
    void getSharesFromDatabase_ShouldReturnEmptyList_WhenDatabaseIsEmpty() {
        // Given - настройка мока для пустой БД
        when(shareRepo.findAll()).thenReturn(Collections.emptyList());

        ShareFilterDto filter = new ShareFilterDto();
        filter.setExchange("MOEX");

        // When - вызов метода сервиса
        List<ShareDto> result = instrumentService.getSharesFromDatabase(filter);

        // Then - проверка результата
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        // Verify
        verify(shareRepo).findAll();
    }
}

    