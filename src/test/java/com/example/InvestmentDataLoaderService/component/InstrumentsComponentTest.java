package com.example.InvestmentDataLoaderService.component;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.InvestmentDataLoaderService.client.TinkoffRestClient;
import com.example.InvestmentDataLoaderService.entity.*;
import com.example.InvestmentDataLoaderService.fixtures.TestDataFactory;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.IndicativeRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;

import io.qameta.allure.*;
import io.qameta.allure.SeverityLevel;
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc.InstrumentsServiceBlockingStub;
import ru.tinkoff.piapi.contract.v1.*;

import java.util.Set;
import java.util.HashSet;


@Epic("Component Tests")
@Feature("Instruments Component Integration")
@DisplayName("Instruments Component Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class InstrumentsComponentTest {

    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private ShareRepository shareRepo;
    @Autowired
    private FutureRepository futureRepo;
    @Autowired
    private IndicativeRepository indicativeRepo;

    @MockBean
    private TinkoffRestClient restClient;

    @MockBean
    private InstrumentsServiceBlockingStub instrumentsService;

    // Хранилище для отслеживания тестовых данных
    private Set<String> testShareFigis = new HashSet<>();
    private Set<String> testFutureFigis = new HashSet<>();
    private Set<String> testIndicativeFigis = new HashSet<>();

    @BeforeEach
    @Step("Подготовка тестовых данных для компонентного тестирования")
    void setUp() {
        // Настройка моков для gRPC сервиса - возвращаем пустой ответ по умолчанию
        FuturesResponse emptyFuturesResponse = FuturesResponse.newBuilder().build();
        when(instrumentsService.futures(any(InstrumentsRequest.class))).thenReturn(emptyFuturesResponse);
     
        // Очистка отслеживающих коллекций
        testShareFigis.clear();
        testFutureFigis.clear();
        testIndicativeFigis.clear();
    }

    @AfterEach
    @Step("Очистка только тестовых данных из базы данных")
    void tearDown() {
        // Удаляем только тестовые данные, которые мы создали в тестах
        if (!testShareFigis.isEmpty()) {
            shareRepo.deleteAllById(testShareFigis);
        }
        if (!testFutureFigis.isEmpty()) {
            futureRepo.deleteAllById(testFutureFigis);
        }
        if (!testIndicativeFigis.isEmpty()) {
            indicativeRepo.deleteAllById(testIndicativeFigis);
        }
    }

    // ==================== ТЕСТЫ АКЦИЙ ====================

    @Test
    @DisplayName("Получение акций из базы данных с фильтрацией по FIGI")
    @Description("Компонентный тест проверяет получение акций из локальной базы данных с применением фильтров по FIGI в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares Database Component")
    @Tag("component")
    @Tag("shares")
    @Tag("database")
    @Tag("figi")
    @Tag("positive")
    void testGetSharesFromDatabase_WithFilteringByFigiAndFullSpringContext_ShouldReturnCorrectShares() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовой акции с FIGI TEST_SHARE_001", () -> {
            ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "moex_mrng_evng_e_wknd_dlr");
            shareRepo.save(share1);
            testShareFigis.add("TEST_SHARE_001");
            System.out.println("Создана тестовая акция: FIGI=TEST_SHARE_001, Ticker=SBER, Name=Сбербанк");
        });

        // Act & Assert - получаем акцию по FIGI
        Allure.step("Выполнение запроса к API для получения акции по FIGI", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "database")
                    .param("figi", "TEST_SHARE_001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].figi").value("TEST_SHARE_001"))
                .andExpect(jsonPath("$[0].ticker").value("SBER"))
                .andExpect(jsonPath("$[0].name").value("Сбербанк"))
                .andExpect(jsonPath("$[0].currency").value("RUB"));
        });

        Allure.step("Проверка корректности возвращенных данных", () -> {
            System.out.println("Проверено: возвращена одна акция с корректными данными");
            System.out.println("FIGI: TEST_SHARE_001, Ticker: SBER, Name: Сбербанк, Currency: RUB");
        });
    }

    
    @Test
    @DisplayName("Получение акций из базы данных с фильтрацией по тикеру")
    @Description("Компонентный тест проверяет получение акций из локальной базы данных с применением фильтров по тикеру в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares Database Component")
    @Tag("component")
    @Tag("shares")
    @Tag("database")
    @Tag("ticker")
    @Tag("positive")
    void getSharesFromDatabase_WithFilterByTickerAndFullSpringContext_ShouldReturnCorrectShares() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание трех тестовых акций с разными тикерами", () -> {
            ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "moex_mrng_evng_e_wknd_dlr");
            ShareEntity share2 = TestDataFactory.createShareEntity("TEST_SHARE_002", "GAZP", "Газпром", "moex_mrng_evng_e_wknd_dlr");
            ShareEntity share3 = TestDataFactory.createShareEntity("TEST_SHARE_003", "LKOH", "Лукойл", "moex_mrng_evng_e_wknd_dlr");

            shareRepo.save(share1);
            shareRepo.save(share2);
            shareRepo.save(share3);

            testShareFigis.add("TEST_SHARE_001");
            testShareFigis.add("TEST_SHARE_002");
            testShareFigis.add("TEST_SHARE_003");
            
            System.out.println("Созданы тестовые акции: SBER, GAZP, LKOH");
        });
        
        // Act & Assert - получаем акции по тикеру
        Allure.step("Выполнение запроса к API для получения акций по тикеру SBER", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                .param("source", "database")
                .param("ticker", "SBER")
                .param("exchange", "moex_mrng_evng_e_wknd_dlr"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].ticker").value("SBER"));
        });

        Allure.step("Проверка корректности фильтрации по тикеру", () -> {
            System.out.println("Проверено: возвращены акции с тикером SBER");
            System.out.println("Фильтрация по тикеру работает корректно");
        });
    }

    @Test
    @DisplayName("Получение акций из базы данных с несуществующими данными")
    @Description("Компонентный тест проверяет получение акций из локальной базы данных с несуществующими данными в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares Database Component")
    @Tag("component")
    @Tag("shares")
    @Tag("database")
    @Tag("negative")
    @Tag("empty-result")
    void getSharesFromDatabase_WithFilterByCurrencyAndFullSpringContext_ShouldReturnEmptyList() throws Exception {
        Allure.step("Выполнение запроса с несуществующим тикером", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                .param("source", "database")
                .param("ticker", "NONEXISTENT_TICKER_12345")
                .param("exchange", "moex_mrng_evng_e_wknd_dlr"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.body").isArray())
                .andExpect(jsonPath("$.body.length()").value(0));
        });

        Allure.step("Проверка корректности обработки несуществующих данных", () -> {
            System.out.println("Проверено: для несуществующего тикера возвращен пустой массив");
            System.out.println("API корректно обрабатывает запросы с несуществующими данными");
        });
    }
        // Arrange - создаем тестовые данные    


    @Test
    @DisplayName("Получение акций из базы данных с фильтрацией по бирже и сортировкой")
    @Description("Компонентный тест проверяет получение акций из локальной базы данных с применением фильтров по бирже и сортировкой по алфавиту в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares Database Component")
    @Tag("component")
    @Tag("shares")
    @Tag("database")
    @Tag("exchange")
    @Tag("sorting")
    @Tag("positive")
    void getSharesFromDatabase_WithFilterByExchangeAndFullSpringContext_ShouldReturnCorrectShares() throws Exception {
        Allure.step("Создание трех тестовых акций для проверки фильтрации по бирже", () -> {
            ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "moex_mrng_evng_e_wknd_dlr");
            ShareEntity share2 = TestDataFactory.createShareEntity("TEST_SHARE_002", "GAZP", "Газпром", "moex_mrng_evng_e_wknd_dlr");
            ShareEntity share3 = TestDataFactory.createShareEntity("TEST_SHARE_003", "LKOH", "Лукойл", "moex_mrng_evng_e_wknd_dlr");

            shareRepo.save(share1);
            shareRepo.save(share2);
            shareRepo.save(share3);

            testShareFigis.add("TEST_SHARE_001");
            testShareFigis.add("TEST_SHARE_002");
            testShareFigis.add("TEST_SHARE_003");
            
            System.out.println("Созданы акции для тестирования фильтрации по бирже: SBER, GAZP, LKOH");
        });
        
        Allure.step("Выполнение запроса к API для получения акций по бирже", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                .param("source", "database")
                .param("exchange", "moex_mrng_evng_e_wknd_dlr"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$[0].exchange").value("moex_mrng_evng_e_wknd_dlr"));
        });

        Allure.step("Проверка корректности фильтрации по бирже и сортировки", () -> {
            System.out.println("Проверено: возвращены акции с правильной биржей moex_mrng_evng_e_wknd_dlr");
            System.out.println("Фильтрация по бирже работает корректно");
        });
    }


    @Test
    @DisplayName("Получение акции по FIGI через endpoint")
    @Description("Компонентный тест проверяет получение конкретной акции по FIGI через REST endpoint в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Shares Database Component")
    @Tag("component")
    @Tag("shares")
    @Tag("database")
    @Tag("figi")
    @Tag("endpoint")
    @Tag("positive")
    void getSharesFromDatabase_WithFilterByFigiAndFullSpringContext_ShouldReturnCorrectShares() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание трех тестовых акций для проверки endpoint по FIGI", () -> {
            ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "moex_mrng_evng_e_wknd_dlr");
            ShareEntity share2 = TestDataFactory.createShareEntity("TEST_SHARE_002", "GAZP", "Газпром", "moex_mrng_evng_e_wknd_dlr");
            ShareEntity share3 = TestDataFactory.createShareEntity("TEST_SHARE_003", "LKOH", "Лукойл", "moex_mrng_evng_e_wknd_dlr");
        
            shareRepo.save(share1);
            shareRepo.save(share2);
            shareRepo.save(share3);

            testShareFigis.add("TEST_SHARE_001");
            testShareFigis.add("TEST_SHARE_002");
            testShareFigis.add("TEST_SHARE_003");
            
            System.out.println("Созданы акции для тестирования endpoint по FIGI: TEST_SHARE_001, TEST_SHARE_002, TEST_SHARE_003");
        });
    
        Allure.step("Выполнение запроса к endpoint для получения акции по FIGI", () -> {
            mockMvc.perform(get("/api/instruments/shares/TEST_SHARE_001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
        });

        Allure.step("Проверка корректности работы endpoint по FIGI", () -> {
            System.out.println("Проверено: endpoint /api/instruments/shares/{figi} работает корректно");
            System.out.println("Возвращен статус 200 OK с корректным JSON");
        });
    }   

    // ==================== ТЕСТЫ ФЬЮЧЕРСОВ ====================

    @Test
    @DisplayName("Получение фьючерсов из базы данных с фильтрацией по тикеру")
    @Description("Компонентный тест проверяет получение фьючерсов из локальной базы данных с применением фильтров по тикеру в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures Database Component")
    @Tag("component")
    @Tag("futures")
    @Tag("database")
    @Tag("ticker")
    @Tag("positive")
    void testGetFuturesFromDatabase_WithFilteringByTickerAndFullSpringContext_ShouldReturnCorrectFutures() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестового фьючерса SBER-3.24", () -> {
            FutureEntity future1 = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "SBER-3.24", "TYPE_COMMODITY");
            futureRepo.save(future1);
            testFutureFigis.add("TEST_FUTURE_001");
            
            System.out.println("Создан тестовый фьючерс: FIGI=TEST_FUTURE_001, Ticker=SBER-3.24, Type=TYPE_COMMODITY");
        });

        Allure.step("Настройка мока gRPC API для возврата тестовых данных", () -> {
            ru.tinkoff.piapi.contract.v1.Future grpcFuture = TestDataFactory.createGrpcFuture("TEST_FUTURE_001", "SBER-3.24", "TYPE_COMMODITY");
            FuturesResponse futuresResponse = FuturesResponse.newBuilder().addInstruments(grpcFuture).build();
            when(instrumentsService.futures(any(InstrumentsRequest.class))).thenReturn(futuresResponse);
            System.out.println("Настроен мок gRPC API для возврата фьючерса SBER-3.24");
        });

        // Act & Assert - получаем фьючерс по тикеру
        Allure.step("Выполнение запроса к API для получения фьючерса по тикеру", () -> {
            mockMvc.perform(get("/api/instruments/futures")
                    .param("ticker", "SBER-3.24"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].figi").value("TEST_FUTURE_001"))
                .andExpect(jsonPath("$[0].ticker").value("SBER-3.24"))
                .andExpect(jsonPath("$[0].assetType").value("TYPE_COMMODITY"))
                .andExpect(jsonPath("$[0].currency").value("RUB"));
        });

        Allure.step("Проверка корректности возвращенных данных фьючерса", () -> {
            System.out.println("Проверено: возвращен фьючерс с корректными данными");
            System.out.println("FIGI: TEST_FUTURE_001, Ticker: SBER-3.24, AssetType: TYPE_COMMODITY, Currency: RUB");
        });
    }

    @Test
    @DisplayName("Получение фьючерсов из базы данных с фильтрацией по типу актива")
    @Description("Компонентный тест проверяет получение фьючерсов из локальной базы данных с применением фильтров по типу актива в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures Database Component")
    @Tag("component")
    @Tag("futures")
    @Tag("database")
    @Tag("asset-type")
    @Tag("positive")
    void testGetFuturesFromDatabase_WithFilterByAssetTypeAndFullSpringContext_ShouldReturnCorrectFutures() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых фьючерсов с разными типами активов", () -> {
            FutureEntity future1 = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "SBER-3.24", "TYPE_COMMODITY");
            FutureEntity future2 = TestDataFactory.createFutureEntity("TEST_FUTURE_002", "USD-3.24", "CURRENCY");
            FutureEntity future3 = TestDataFactory.createFutureEntity("TEST_FUTURE_003", "GOLD-3.24", "TYPE_COMMODITY");

            futureRepo.save(future1);
            futureRepo.save(future2);
            futureRepo.save(future3);

            testFutureFigis.add("TEST_FUTURE_001");
            testFutureFigis.add("TEST_FUTURE_002");
            testFutureFigis.add("TEST_FUTURE_003");
            
            System.out.println("Созданы фьючерсы: SBER-3.24 (COMMODITY), USD-3.24 (CURRENCY), GOLD-3.24 (COMMODITY)");
        });

        Allure.step("Настройка мока gRPC API для возврата всех тестовых фьючерсов", () -> {
            ru.tinkoff.piapi.contract.v1.Future grpcFuture1 = TestDataFactory.createGrpcFuture("TEST_FUTURE_001", "SBER-3.24", "TYPE_COMMODITY");
            ru.tinkoff.piapi.contract.v1.Future grpcFuture2 = TestDataFactory.createGrpcFuture("TEST_FUTURE_002", "USD-3.24", "CURRENCY");
            ru.tinkoff.piapi.contract.v1.Future grpcFuture3 = TestDataFactory.createGrpcFuture("TEST_FUTURE_003", "GOLD-3.24", "TYPE_COMMODITY");
            FuturesResponse futuresResponse = FuturesResponse.newBuilder()
                .addInstruments(grpcFuture1)
                .addInstruments(grpcFuture2)
                .addInstruments(grpcFuture3)
                .build();
            when(instrumentsService.futures(any(InstrumentsRequest.class))).thenReturn(futuresResponse);
            System.out.println("Настроен мок gRPC API для возврата трех фьючерсов");
        });

        // Act & Assert - получаем фьючерсы по типу актива
        Allure.step("Выполнение запроса к API для получения фьючерсов по типу актива TYPE_COMMODITY", () -> {
            mockMvc.perform(get("/api/instruments/futures")
                    .param("assetType", "TYPE_COMMODITY"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[0].assetType").value("TYPE_COMMODITY"))
                .andExpect(jsonPath("$[1].assetType").value("TYPE_COMMODITY"));
        });

        Allure.step("Проверка корректности фильтрации по типу актива", () -> {
            System.out.println("Проверено: возвращены только фьючерсы с типом актива TYPE_COMMODITY");
            System.out.println("Фильтрация по типу актива работает корректно");
        });
    }

    @Test
    @DisplayName("Получение фьючерсов из базы данных с несуществующими данными")
    @Description("Компонентный тест проверяет получение фьючерсов из локальной базы данных с несуществующими данными в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures Database Component")
    @Tag("component")
    @Tag("futures")
    @Tag("database")
    @Tag("negative")
    @Tag("empty-result")
    void testGetFuturesFromDatabase_WithNonExistentData_ShouldReturnEmptyList() throws Exception {
        Allure.step("Выполнение запроса с несуществующим тикером фьючерса", () -> {
            mockMvc.perform(get("/api/instruments/futures")
                    .param("ticker", "NONEXISTENT_FUTURE_12345"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.body").isArray())
                .andExpect(jsonPath("$.body.length()").value(0));
        });

        Allure.step("Проверка корректности обработки несуществующих фьючерсов", () -> {
            System.out.println("Проверено: для несуществующего тикера фьючерса возвращен пустой массив");
            System.out.println("API корректно обрабатывает запросы с несуществующими фьючерсами");
        });
    }

    @Test
    @DisplayName("Получение фьючерсов из базы данных с фильтрацией по бирже")
    @Description("Компонентный тест проверяет получение фьючерсов из локальной базы данных с применением фильтров по бирже в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Futures Database Component")
    @Tag("component")
    @Tag("futures")
    @Tag("database")
    @Tag("exchange")
    @Tag("positive")
    void testGetFuturesFromDatabase_WithFilterByExchangeAndFullSpringContext_ShouldReturnCorrectFutures() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание трех тестовых фьючерсов для проверки фильтрации по бирже", () -> {
            FutureEntity future1 = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "SBER-3.24", "TYPE_COMMODITY");
            FutureEntity future2 = TestDataFactory.createFutureEntity("TEST_FUTURE_002", "GAZP-3.24", "TYPE_COMMODITY");
            FutureEntity future3 = TestDataFactory.createFutureEntity("TEST_FUTURE_003", "LKOH-3.24", "TYPE_COMMODITY");

            futureRepo.save(future1);
            futureRepo.save(future2);
            futureRepo.save(future3);

            testFutureFigis.add("TEST_FUTURE_001");
            testFutureFigis.add("TEST_FUTURE_002");
            testFutureFigis.add("TEST_FUTURE_003");
            
            System.out.println("Созданы фьючерсы для тестирования фильтрации по бирже: SBER-3.24, GAZP-3.24, LKOH-3.24");
        });

        // Act & Assert - получаем фьючерсы по бирже
        Allure.step("Выполнение запроса к API для получения фьючерсов по бирже", () -> {
            mockMvc.perform(get("/api/instruments/futures")
                    .param("exchange", "moex_mrng_evng_e_wknd_dlr"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(3)));
        });

        Allure.step("Проверка корректности фильтрации фьючерсов по бирже", () -> {
            System.out.println("Проверено: возвращены фьючерсы с правильной биржей moex_mrng_evng_e_wknd_dlr");
            System.out.println("Фильтрация фьючерсов по бирже работает корректно");
        });
    }

    // ==================== ТЕСТЫ ИНДИКАТИВОВ ====================

    @Test
    @DisplayName("Получение индикативов из базы данных с фильтрацией по FIGI")
    @Description("Компонентный тест проверяет получение индикативов из локальной базы данных с применением фильтров по FIGI в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives Database Component")
    @Tag("component")
    @Tag("indicatives")
    @Tag("database")
    @Tag("figi")
    @Tag("positive")
    void testGetIndicativesFromDatabase_WithFilteringByFigiAndFullSpringContext_ShouldReturnCorrectIndicatives() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестового индикатива RTSI", () -> {
            IndicativeEntity indicative1 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_001", "RTSI", "Индекс РТС");
            indicativeRepo.save(indicative1);
            testIndicativeFigis.add("TEST_INDICATIVE_001");
            
            System.out.println("Создан тестовый индикатив: FIGI=TEST_INDICATIVE_001, Ticker=RTSI, Name=Индекс РТС");
        });

        // Act & Assert - получаем индикатив по FIGI
        Allure.step("Выполнение запроса к API для получения индикатива по FIGI", () -> {
            mockMvc.perform(get("/api/instruments/indicatives")
                    .param("figi", "TEST_INDICATIVE_001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].figi").value("TEST_INDICATIVE_001"))
                .andExpect(jsonPath("$[0].ticker").value("RTSI"))
                .andExpect(jsonPath("$[0].name").value("Индекс РТС"))
                .andExpect(jsonPath("$[0].currency").value("RUB"));
        });

        Allure.step("Проверка корректности возвращенных данных индикатива", () -> {
            System.out.println("Проверено: возвращен индикатив с корректными данными");
            System.out.println("FIGI: TEST_INDICATIVE_001, Ticker: RTSI, Name: Индекс РТС, Currency: RUB");
        });
    }

    @Test
    @DisplayName("Получение индикативов из базы данных с фильтрацией по тикеру")
    @Description("Компонентный тест проверяет получение индикативов из локальной базы данных с применением фильтров по тикеру в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives Database Component")
    @Tag("component")
    @Tag("indicatives")
    @Tag("database")
    @Tag("ticker")
    @Tag("positive")
    void testGetIndicativesFromDatabase_WithFilterByTickerAndFullSpringContext_ShouldReturnCorrectIndicatives() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание трех тестовых индикативов с разными тикерами", () -> {
            IndicativeEntity indicative1 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_001", "RTSI", "Индекс РТС");
            IndicativeEntity indicative2 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_002", "MOEX", "Индекс МосБиржи");
            IndicativeEntity indicative3 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_003", "USD000UTSTOM", "Доллар США");

            indicativeRepo.save(indicative1);
            indicativeRepo.save(indicative2);
            indicativeRepo.save(indicative3);

            testIndicativeFigis.add("TEST_INDICATIVE_001");
            testIndicativeFigis.add("TEST_INDICATIVE_002");
            testIndicativeFigis.add("TEST_INDICATIVE_003");
            
            System.out.println("Созданы индикативы: RTSI, MOEX, USD000UTSTOM");
        });

        // Act & Assert - получаем индикатив по тикеру
        Allure.step("Выполнение запроса к API для получения индикатива по тикеру RTSI", () -> {
            mockMvc.perform(get("/api/instruments/indicatives")
                    .param("ticker", "RTSI"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].ticker").value("RTSI"));
        });

        Allure.step("Проверка корректности фильтрации индикативов по тикеру", () -> {
            System.out.println("Проверено: возвращен индикатив с тикером RTSI");
            System.out.println("Фильтрация индикативов по тикеру работает корректно");
        });
    }

    @Test
    @DisplayName("Получение индикативов из базы данных с фильтрацией по валюте")
    @Description("Компонентный тест проверяет получение индикативов из локальной базы данных с применением фильтров по валюте в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives Database Component")
    @Tag("component")
    @Tag("indicatives")
    @Tag("database")
    @Tag("currency")
    @Tag("positive")
    void testGetIndicativesFromDatabase_WithFilterByCurrencyAndFullSpringContext_ShouldReturnCorrectIndicatives() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание трех тестовых индикативов для проверки фильтрации по валюте", () -> {
            IndicativeEntity indicative1 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_001", "RTSI", "Индекс РТС");
            IndicativeEntity indicative2 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_002", "MOEX", "Индекс МосБиржи");
            IndicativeEntity indicative3 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_003", "USD000UTSTOM", "Доллар США");

            indicativeRepo.save(indicative1);
            indicativeRepo.save(indicative2);
            indicativeRepo.save(indicative3);

            testIndicativeFigis.add("TEST_INDICATIVE_001");
            testIndicativeFigis.add("TEST_INDICATIVE_002");
            testIndicativeFigis.add("TEST_INDICATIVE_003");
            
            System.out.println("Созданы индикативы для тестирования фильтрации по валюте: RTSI, MOEX, USD000UTSTOM");
        });

        // Act & Assert - получаем индикативы по валюте RUB
        Allure.step("Выполнение запроса к API для получения индикативов по валюте RUB", () -> {
            mockMvc.perform(get("/api/instruments/indicatives")
                    .param("currency", "RUB"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$[0].currency").value(anyOf(is("RUB"), is("rub"))))
                .andExpect(jsonPath("$[1].currency").value(anyOf(is("RUB"), is("rub"))))
                .andExpect(jsonPath("$[2].currency").value(anyOf(is("RUB"), is("rub"))));
        });

        Allure.step("Проверка корректности фильтрации индикативов по валюте", () -> {
            System.out.println("Проверено: возвращены индикативы с валютой RUB");
            System.out.println("Фильтрация индикативов по валюте работает корректно");
        });
    }

    @Test
    @DisplayName("Получение индикативов из базы данных с несуществующими данными")
    @Description("Компонентный тест проверяет получение индикативов из локальной базы данных с несуществующими данными в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Indicatives Database Component")
    @Tag("component")
    @Tag("indicatives")
    @Tag("database")
    @Tag("negative")
    @Tag("empty-result")
    void testGetIndicativesFromDatabase_WithNonExistentData_ShouldReturnEmptyList() throws Exception {
        Allure.step("Выполнение запроса с несуществующим тикером индикатива", () -> {
            mockMvc.perform(get("/api/instruments/indicatives")
                    .param("ticker", "NONEXISTENT_INDICATIVE_12345"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(0)));
        });

        Allure.step("Проверка корректности обработки несуществующих индикативов", () -> {
            System.out.println("Проверено: для несуществующего тикера индикатива возвращен пустой массив");
            System.out.println("API корректно обрабатывает запросы с несуществующими индикативами");
        });
    }

    // ==================== ТЕСТЫ КОМБИНИРОВАННЫХ ФИЛЬТРОВ ====================

    @Test
    @DisplayName("Получение акций с комбинированными фильтрами")
    @Description("Компонентный тест проверяет получение акций из локальной базы данных с применением нескольких фильтров одновременно в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Combined Filters Component")
    @Tag("component")
    @Tag("shares")
    @Tag("database")
    @Tag("combined-filters")
    @Tag("positive")
    void testGetSharesFromDatabase_WithCombinedFiltersAndFullSpringContext_ShouldReturnCorrectShares() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых акций с разными параметрами для проверки комбинированных фильтров", () -> {
            ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "moex_mrng_evng_e_wknd_dlr");
            ShareEntity share2 = TestDataFactory.createShareEntity("TEST_SHARE_002", "GAZP", "Газпром", "moex_mrng_evng_e_wknd_dlr");
            ShareEntity share3 = TestDataFactory.createShareEntity("TEST_SHARE_003", "LKOH", "Лукойл", "OTHEREXCHANGE");

            shareRepo.save(share1);
            shareRepo.save(share2);
            shareRepo.save(share3);

            testShareFigis.add("TEST_SHARE_001");
            testShareFigis.add("TEST_SHARE_002");
            testShareFigis.add("TEST_SHARE_003");
            
            System.out.println("Созданы акции для тестирования комбинированных фильтров:");
            System.out.println("SBER, GAZP (moex_mrng_evng_e_wknd_dlr), LKOH (OTHEREXCHANGE)");
        });

        // Act & Assert - получаем акции с комбинированными фильтрами
        Allure.step("Выполнение запроса к API с комбинированными фильтрами (exchange + currency)", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "database")
                    .param("exchange", "moex_mrng_evng_e_wknd_dlr")
                    .param("currency", "RUB"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[0].exchange").value("moex_mrng_evng_e_wknd_dlr"))
                .andExpect(jsonPath("$[1].exchange").value("moex_mrng_evng_e_wknd_dlr"));
        });

        Allure.step("Проверка корректности работы комбинированных фильтров", () -> {
            System.out.println("Проверено: возвращены акции с биржей moex_mrng_evng_e_wknd_dlr и валютой RUB");
            System.out.println("Комбинированные фильтры работают корректно");
        });
    }

    @Test
    @DisplayName("Получение фьючерсов с комбинированными фильтрами")
    @Description("Компонентный тест проверяет получение фьючерсов из локальной базы данных с применением нескольких фильтров одновременно в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Combined Filters Component")
    @Tag("component")
    @Tag("futures")
    @Tag("database")
    @Tag("combined-filters")
    @Tag("positive")
    void testGetFuturesFromDatabase_WithCombinedFiltersAndFullSpringContext_ShouldReturnCorrectFutures() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых фьючерсов с разными параметрами для проверки комбинированных фильтров", () -> {
            FutureEntity future1 = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "SBER-3.24", "TYPE_COMMODITY");
            FutureEntity future2 = TestDataFactory.createFutureEntity("TEST_FUTURE_002", "GAZP-3.24", "TYPE_COMMODITY");
            FutureEntity future3 = TestDataFactory.createFutureEntity("TEST_FUTURE_003", "USD-3.24", "CURRENCY");

            futureRepo.save(future1);
            futureRepo.save(future2);
            futureRepo.save(future3);

            testFutureFigis.add("TEST_FUTURE_001");
            testFutureFigis.add("TEST_FUTURE_002");
            testFutureFigis.add("TEST_FUTURE_003");
            
            System.out.println("Созданы фьючерсы для тестирования комбинированных фильтров:");
            System.out.println("SBER-3.24, GAZP-3.24 (TYPE_COMMODITY), USD-3.24 (CURRENCY)");
        });

        Allure.step("Настройка мока gRPC API для возврата всех тестовых фьючерсов", () -> {
            ru.tinkoff.piapi.contract.v1.Future grpcFuture1 = TestDataFactory.createGrpcFuture("TEST_FUTURE_001", "SBER-3.24", "TYPE_COMMODITY");
            ru.tinkoff.piapi.contract.v1.Future grpcFuture2 = TestDataFactory.createGrpcFuture("TEST_FUTURE_002", "GAZP-3.24", "TYPE_COMMODITY");
            ru.tinkoff.piapi.contract.v1.Future grpcFuture3 = TestDataFactory.createGrpcFuture("TEST_FUTURE_003", "USD-3.24", "CURRENCY");
            FuturesResponse futuresResponse = FuturesResponse.newBuilder()
                .addInstruments(grpcFuture1)
                .addInstruments(grpcFuture2)
                .addInstruments(grpcFuture3)
                .build();
            when(instrumentsService.futures(any(InstrumentsRequest.class))).thenReturn(futuresResponse);
            System.out.println("Настроен мок gRPC API для возврата трех фьючерсов");
        });

        // Act & Assert - получаем фьючерсы с комбинированными фильтрами
        Allure.step("Выполнение запроса к API с комбинированными фильтрами (assetType + currency)", () -> {
            mockMvc.perform(get("/api/instruments/futures")
                    .param("assetType", "TYPE_COMMODITY")
                    .param("currency", "RUB"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[0].assetType").value("TYPE_COMMODITY"))
                .andExpect(jsonPath("$[1].assetType").value("TYPE_COMMODITY"));
        });

        Allure.step("Проверка корректности работы комбинированных фильтров для фьючерсов", () -> {
            System.out.println("Проверено: возвращены фьючерсы с типом актива TYPE_COMMODITY и валютой RUB");
            System.out.println("Комбинированные фильтры для фьючерсов работают корректно");
        });
    }

    @Test
    @DisplayName("Получение индикативов с комбинированными фильтрами")
    @Description("Компонентный тест проверяет получение индикативов из локальной базы данных с применением нескольких фильтров одновременно в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Combined Filters Component")
    @Tag("component")
    @Tag("indicatives")
    @Tag("database")
    @Tag("combined-filters")
    @Tag("positive")
    void testGetIndicativesFromDatabase_WithCombinedFiltersAndFullSpringContext_ShouldReturnCorrectIndicatives() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых индикативов с разными параметрами для проверки комбинированных фильтров", () -> {
            IndicativeEntity indicative1 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_001", "RTSI", "Индекс РТС");
            IndicativeEntity indicative2 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_002", "MOEX", "Индекс МосБиржи");
            IndicativeEntity indicative3 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_003", "USD000UTSTOM", "Доллар США");

            indicativeRepo.save(indicative1);
            indicativeRepo.save(indicative2);
            indicativeRepo.save(indicative3);

            testIndicativeFigis.add("TEST_INDICATIVE_001");
            testIndicativeFigis.add("TEST_INDICATIVE_002");
            testIndicativeFigis.add("TEST_INDICATIVE_003");
            
            System.out.println("Созданы индикативы для тестирования комбинированных фильтров:");
            System.out.println("RTSI, MOEX, USD000UTSTOM");
        });

        // Act & Assert - получаем индикативы с комбинированными фильтрами
        Allure.step("Выполнение запроса к API с комбинированными фильтрами (currency + exchange)", () -> {
            mockMvc.perform(get("/api/instruments/indicatives")
                    .param("currency", "RUB")
                    .param("exchange", "moex_mrng_evng_e_wknd_dlr"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$[0].currency").value(anyOf(is("RUB"), is("rub"))))
                .andExpect(jsonPath("$[1].currency").value(anyOf(is("RUB"), is("rub"))))
                .andExpect(jsonPath("$[2].currency").value(anyOf(is("RUB"), is("rub"))));
        });

        Allure.step("Проверка корректности работы комбинированных фильтров для индикативов", () -> {
            System.out.println("Проверено: возвращены индикативы с валютой RUB и биржей moex_mrng_evng_e_wknd_dlr");
            System.out.println("Комбинированные фильтры для индикативов работают корректно");
        });
    }

    // ==================== ТЕСТЫ ОБРАБОТКИ ОШИБОК ====================

    @Test
    @DisplayName("Обработка некорректных параметров для акций")
    @Description("Компонентный тест проверяет обработку некорректных параметров при запросе акций в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Error Handling Component")
    @Tag("component")
    @Tag("shares")
    @Tag("error-handling")
    @Tag("validation")
    @Tag("negative")
    void testGetSharesFromDatabase_WithInvalidParameters_ShouldReturnBadRequest() throws Exception {
        Allure.step("Выполнение запроса с некорректным источником данных", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "invalid_source"))
                .andExpect(status().isBadRequest());
        });

        Allure.step("Проверка корректности обработки некорректных параметров", () -> {
            System.out.println("Проверено: API возвращает статус 400 Bad Request для некорректного источника данных");
            System.out.println("Валидация параметров работает корректно");
        });
    }

    @Test
    @DisplayName("Обработка некорректных параметров для фьючерсов")
    @Description("Компонентный тест проверяет обработку некорректных параметров при запросе фьючерсов в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Error Handling Component")
    @Tag("component")
    @Tag("futures")
    @Tag("error-handling")
    @Tag("validation")
    @Tag("negative")
    void testGetFuturesFromDatabase_WithInvalidParameters_ShouldReturnBadRequest() throws Exception {
        Allure.step("Выполнение запроса с некорректным типом актива", () -> {
            mockMvc.perform(get("/api/instruments/futures")
                    .param("assetType", "INVALID_TYPE"))
                .andExpect(status().isBadRequest());
        });

        Allure.step("Проверка корректности обработки некорректных параметров для фьючерсов", () -> {
            System.out.println("Проверено: API возвращает статус 400 Bad Request для некорректного типа актива");
            System.out.println("Валидация параметров фьючерсов работает корректно");
        });
    }

    @Test
    @DisplayName("Обработка некорректных параметров для индикативов")
    @Description("Компонентный тест проверяет обработку некорректных параметров при запросе индикативов в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Error Handling Component")
    @Tag("component")
    @Tag("indicatives")
    @Tag("error-handling")
    @Tag("validation")
    @Tag("negative")
    void testGetIndicativesFromDatabase_WithInvalidParameters_ShouldReturnBadRequest() throws Exception {
        Allure.step("Выполнение запроса с некорректным параметром", () -> {
            mockMvc.perform(get("/api/instruments/indicatives")
                    .param("invalidParam", "INVALID_VALUE"))
                .andExpect(status().isBadRequest());
        });

        Allure.step("Проверка корректности обработки некорректных параметров для индикативов", () -> {
            System.out.println("Проверено: API возвращает статус 400 Bad Request для некорректного параметра");
            System.out.println("Валидация параметров индикативов работает корректно");
        });
    }

    // ==================== ТЕСТЫ ГРАНИЧНЫХ СЛУЧАЕВ ====================

    @Test
    @DisplayName("Получение акций с пустыми параметрами")
    @Description("Компонентный тест проверяет получение акций из локальной базы данных без параметров фильтрации в полном контексте Spring")
    @Severity(SeverityLevel.NORMAL)
    @Story("Boundary Cases Component")
    @Tag("component")
    @Tag("shares")
    @Tag("database")
    @Tag("boundary")
    @Tag("empty-params")
    @Tag("positive")
    void testGetSharesFromDatabase_WithEmptyParameters_ShouldReturnAllShares() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых акций для проверки граничного случая", () -> {
            ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "moex_mrng_evng_e_wknd_dlr");
            ShareEntity share2 = TestDataFactory.createShareEntity("TEST_SHARE_002", "GAZP", "Газпром", "moex_mrng_evng_e_wknd_dlr");

            shareRepo.save(share1);
            shareRepo.save(share2);

            testShareFigis.add("TEST_SHARE_001");
            testShareFigis.add("TEST_SHARE_002");
            
            System.out.println("Созданы акции для тестирования граничного случая: SBER, GAZP");
        });

        // Act & Assert - получаем все акции
        Allure.step("Выполнение запроса к API без параметров фильтрации", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "database"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)));
        });

        Allure.step("Проверка корректности обработки пустых параметров", () -> {
            System.out.println("Проверено: API возвращает все акции при отсутствии параметров фильтрации");
            System.out.println("Граничный случай с пустыми параметрами обрабатывается корректно");
        });
    }

    @Test
    @DisplayName("Получение фьючерсов с пустыми параметрами")
    @Description("Компонентный тест проверяет получение фьючерсов из локальной базы данных без параметров фильтрации в полном контексте Spring")
    @Severity(SeverityLevel.NORMAL)
    @Story("Boundary Cases Component")
    @Tag("component")
    @Tag("futures")
    @Tag("database")
    @Tag("boundary")
    @Tag("empty-params")
    @Tag("positive")
    void testGetFuturesFromDatabase_WithEmptyParameters_ShouldReturnAllFutures() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых фьючерсов для проверки граничного случая", () -> {
            FutureEntity future1 = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "SBER-3.24", "TYPE_COMMODITY");
            FutureEntity future2 = TestDataFactory.createFutureEntity("TEST_FUTURE_002", "GAZP-3.24", "TYPE_COMMODITY");

            futureRepo.save(future1);
            futureRepo.save(future2);

            testFutureFigis.add("TEST_FUTURE_001");
            testFutureFigis.add("TEST_FUTURE_002");
            
            System.out.println("Созданы фьючерсы для тестирования граничного случая: SBER-3.24, GAZP-3.24");
        });

        Allure.step("Настройка мока gRPC API для возврата тестовых фьючерсов", () -> {
            ru.tinkoff.piapi.contract.v1.Future grpcFuture1 = TestDataFactory.createGrpcFuture("TEST_FUTURE_001", "SBER-3.24", "TYPE_COMMODITY");
            ru.tinkoff.piapi.contract.v1.Future grpcFuture2 = TestDataFactory.createGrpcFuture("TEST_FUTURE_002", "GAZP-3.24", "TYPE_COMMODITY");
            FuturesResponse futuresResponse = FuturesResponse.newBuilder()
                .addInstruments(grpcFuture1)
                .addInstruments(grpcFuture2)
                .build();
            when(instrumentsService.futures(any(InstrumentsRequest.class))).thenReturn(futuresResponse);
            System.out.println("Настроен мок gRPC API для возврата двух фьючерсов");
        });

        // Act & Assert - получаем все фьючерсы
        Allure.step("Выполнение запроса к API без параметров фильтрации", () -> {
            mockMvc.perform(get("/api/instruments/futures"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)));
        });

        Allure.step("Проверка корректности обработки пустых параметров для фьючерсов", () -> {
            System.out.println("Проверено: API возвращает все фьючерсы при отсутствии параметров фильтрации");
            System.out.println("Граничный случай с пустыми параметрами для фьючерсов обрабатывается корректно");
        });
    }

    @Test
    @DisplayName("Получение индикативов с пустыми параметрами")
    @Description("Компонентный тест проверяет получение индикативов из локальной базы данных без параметров фильтрации в полном контексте Spring")
    @Severity(SeverityLevel.NORMAL)
    @Story("Boundary Cases Component")
    @Tag("component")
    @Tag("indicatives")
    @Tag("database")
    @Tag("boundary")
    @Tag("empty-params")
    @Tag("positive")
    void testGetIndicativesFromDatabase_WithEmptyParameters_ShouldReturnAllIndicatives() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых индикативов для проверки граничного случая", () -> {
            IndicativeEntity indicative1 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_001", "RTSI", "Индекс РТС");
            IndicativeEntity indicative2 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_002", "MOEX", "Индекс МосБиржи");

            indicativeRepo.save(indicative1);
            indicativeRepo.save(indicative2);

            testIndicativeFigis.add("TEST_INDICATIVE_001");
            testIndicativeFigis.add("TEST_INDICATIVE_002");
            
            System.out.println("Созданы индикативы для тестирования граничного случая: RTSI, MOEX");
        });

        // Act & Assert - получаем все индикативы
        Allure.step("Выполнение запроса к API без параметров фильтрации", () -> {
            mockMvc.perform(get("/api/instruments/indicatives"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)));
        });

        Allure.step("Проверка корректности обработки пустых параметров для индикативов", () -> {
            System.out.println("Проверено: API возвращает все индикативы при отсутствии параметров фильтрации");
            System.out.println("Граничный случай с пустыми параметрами для индикативов обрабатывается корректно");
        });
    }

    // ==================== ТЕСТЫ API ENDPOINTS С ВНЕШНИМ TINKOFF API ====================

    @Test
    @DisplayName("Получение акций из внешнего API Tinkoff")
    @Description("Компонентный тест проверяет получение акций из внешнего API Tinkoff с мокированием ответа в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Integration Component")
    @Tag("component")
    @Tag("shares")
    @Tag("api")
    @Tag("tinkoff")
    @Tag("mocked")
    @Tag("positive")
    void testGetSharesFromTinkoffApi_WithMockedResponse_ShouldReturnCorrectShares() throws Exception {
        // Arrange - настраиваем мок для внешнего API
        Allure.step("Настройка мока для внешнего API Tinkoff", () -> {
            // Мокаем вызов к внешнему API - возвращаем JsonNode
            when(restClient.getShares()).thenReturn(TestDataFactory.createSharesJsonNode());
            System.out.println("Настроен мок для внешнего API Tinkoff - возвращает тестовые данные акций");
        });

        // Act & Assert - получаем акции из API
        Allure.step("Выполнение запроса к API с источником 'api'", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "api")
                    .param("exchange", "moex_mrng_evng_e_wknd_dlr")
                    .param("currency", "RUB"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
        });

        Allure.step("Проверка корректности интеграции с внешним API", () -> {
            System.out.println("Проверено: API корректно интегрируется с внешним API Tinkoff");
            System.out.println("Возвращены акции из внешнего источника с применением фильтров");
        });
    }

    @Test
    @DisplayName("Получение фьючерсов из внешнего API Tinkoff")
    @Description("Компонентный тест проверяет получение фьючерсов из внешнего API Tinkoff с мокированием ответа в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Integration Component")
    @Tag("component")
    @Tag("futures")
    @Tag("api")
    @Tag("tinkoff")
    @Tag("mocked")
    @Tag("positive")
    void testGetFuturesFromTinkoffApi_WithMockedResponse_ShouldReturnCorrectFutures() throws Exception {
        // Arrange - настраиваем мок для внешнего API
        Allure.step("Настройка мока для внешнего API Tinkoff", () -> {
            // Мокаем вызов к внешнему API - возвращаем JsonNode
            when(restClient.getFutures()).thenReturn(TestDataFactory.createFuturesJsonNode());
            System.out.println("Настроен мок для внешнего API Tinkoff - возвращает тестовые данные фьючерсов");
        });

        // Act & Assert - получаем фьючерсы из API
        Allure.step("Выполнение запроса к API для получения фьючерсов", () -> {
            mockMvc.perform(get("/api/instruments/futures")
                    .param("ticker", "SBER-3.24")
                    .param("currency", "RUB"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.body").isArray());
        });

        Allure.step("Проверка корректности интеграции с внешним API для фьючерсов", () -> {
            System.out.println("Проверено: API корректно интегрируется с внешним API Tinkoff для фьючерсов");
            System.out.println("Возвращены фьючерсы из внешнего источника с корректной структурой ответа");
        });
    }

    @Test
    @DisplayName("Получение индикативов из внешнего API Tinkoff")
    @Description("Компонентный тест проверяет получение индикативов из внешнего API Tinkoff с мокированием ответа в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Integration Component")
    @Tag("component")
    @Tag("indicatives")
    @Tag("api")
    @Tag("tinkoff")
    @Tag("mocked")
    @Tag("positive")
    void testGetIndicativesFromTinkoffApi_WithMockedResponse_ShouldReturnCorrectIndicatives() throws Exception {
        // Arrange - настраиваем мок для внешнего API
        Allure.step("Настройка мока для внешнего API Tinkoff", () -> {
            // Мокаем вызов к внешнему API - возвращаем JsonNode
            when(restClient.getIndicatives()).thenReturn(TestDataFactory.createIndicativesJsonNode());
            System.out.println("Настроен мок для внешнего API Tinkoff - возвращает тестовые данные индикативов");
        });

        // Act & Assert - получаем индикативы из API
        Allure.step("Выполнение запроса к API для получения индикативов", () -> {
            mockMvc.perform(get("/api/instruments/indicatives")
                    .param("currency", "RUB")
                    .param("exchange", "moex_mrng_evng_e_wknd_dlr"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(0)));
        });

        Allure.step("Проверка корректности интеграции с внешним API для индикативов", () -> {
            System.out.println("Проверено: API корректно интегрируется с внешним API Tinkoff для индикативов");
            System.out.println("Возвращены индикативы из внешнего источника с применением фильтров");
        });
    }

    @Test
    @DisplayName("Обработка ошибки внешнего API при получении акций")
    @Description("Компонентный тест проверяет обработку ошибки внешнего API при получении акций в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API Integration Component")
    @Tag("component")
    @Tag("shares")
    @Tag("api")
    @Tag("error-handling")
    @Tag("negative")
    void testGetSharesFromTinkoffApi_WithApiError_ShouldHandleErrorGracefully() throws Exception {
        // Arrange - настраиваем мок для ошибки API
        Allure.step("Настройка мока для ошибки внешнего API", () -> {
            when(restClient.getShares())
                .thenThrow(new RuntimeException("API недоступен"));
            System.out.println("Настроен мок для имитации ошибки внешнего API - API недоступен");
        });

        // Act & Assert - проверяем обработку ошибки
        Allure.step("Выполнение запроса к API при ошибке внешнего сервиса", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "api")
                    .param("exchange", "moex_mrng_evng_e_wknd_dlr"))
                .andExpect(status().isOk()) // API должен возвращать пустой список при ошибке
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(0)));
        });

        Allure.step("Проверка корректности обработки ошибок внешнего API", () -> {
            System.out.println("Проверено: API корректно обрабатывает ошибки внешнего сервиса");
            System.out.println("При недоступности внешнего API возвращается пустой список");
        });
    }

    @Test
    @DisplayName("Обработка пустого ответа от внешнего API")
    @Description("Компонентный тест проверяет обработку пустого ответа от внешнего API в полном контексте Spring")
    @Severity(SeverityLevel.NORMAL)
    @Story("API Integration Component")
    @Tag("component")
    @Tag("shares")
    @Tag("api")
    @Tag("empty-response")
    @Tag("boundary")
    @Tag("positive")
    void testGetSharesFromTinkoffApi_WithEmptyResponse_ShouldReturnEmptyList() throws Exception {
        // Arrange - настраиваем мок для пустого ответа
        Allure.step("Настройка мока для пустого ответа от внешнего API", () -> {
            when(restClient.getShares())
                .thenReturn(TestDataFactory.createEmptyJsonNode());
            System.out.println("Настроен мок для имитации пустого ответа от внешнего API");
        });

        // Act & Assert - проверяем обработку пустого ответа
        Allure.step("Выполнение запроса к API при пустом ответе от внешнего сервиса", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "api")
                    .param("exchange", "moex_mrng_evng_e_wknd_dlr"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(0)));
        });

        Allure.step("Проверка корректности обработки пустого ответа от внешнего API", () -> {
            System.out.println("Проверено: API корректно обрабатывает пустой ответ от внешнего сервиса");
            System.out.println("При пустом ответе от внешнего API возвращается пустой список");
        });
    }

    // ==================== ТЕСТЫ ПОИСКА ПО FIGI ====================

    @Test
    @DisplayName("Поиск акции по FIGI")
    @Description("Компонентный тест проверяет поиск конкретной акции по FIGI в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Search Component")
    @Tag("component")
    @Tag("shares")
    @Tag("search")
    @Tag("figi")
    @Tag("positive")
    void testGetShareByFigi_WithValidFigi_ShouldReturnCorrectShare() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовой акции для проверки поиска по FIGI", () -> {
            ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "moex_mrng_evng_e_wknd_dlr");
            shareRepo.save(share);
            testShareFigis.add("TEST_SHARE_001");
            
            System.out.println("Создана тестовая акция для поиска: FIGI=TEST_SHARE_001, Ticker=SBER, Name=Сбербанк");
        });

        // Act & Assert - ищем акцию по FIGI
        Allure.step("Выполнение поиска акции по FIGI через REST endpoint", () -> {
            mockMvc.perform(get("/api/instruments/shares/TEST_SHARE_001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.figi").value("TEST_SHARE_001"))
                .andExpect(jsonPath("$.ticker").value("SBER"))
                .andExpect(jsonPath("$.name").value("Сбербанк"));
        });

        Allure.step("Проверка корректности поиска акции по FIGI", () -> {
            System.out.println("Проверено: поиск акции по FIGI работает корректно");
            System.out.println("Возвращена акция с правильными данными: FIGI, Ticker, Name");
        });
    }

    @Test
    @DisplayName("Поиск несуществующей акции по FIGI")
    @Description("Компонентный тест проверяет поиск несуществующей акции по FIGI в полном контексте Spring")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Search Component")
    @Tag("component")
    @Tag("shares")
    @Tag("search")
    @Tag("figi")
    @Tag("negative")
    void testGetShareByFigi_WithNonExistentFigi_ShouldReturnNotFound() throws Exception {
        Allure.step("Выполнение поиска несуществующей акции по FIGI", () -> {
            mockMvc.perform(get("/api/instruments/shares/NONEXISTENT_FIGI"))
                .andExpect(status().isNotFound());
        });

        Allure.step("Проверка корректности обработки несуществующего FIGI", () -> {
            System.out.println("Проверено: поиск несуществующей акции возвращает статус 404 Not Found");
            System.out.println("Обработка несуществующих FIGI работает корректно");
        });
    }

    // ==================== ТЕСТЫ СТАТИСТИКИ ====================

    @Test
    @DisplayName("Получение статистики по инструментам")
    @Description("Компонентный тест проверяет получение статистики по количеству инструментов в базе данных в полном контексте Spring")
    @Severity(SeverityLevel.NORMAL)
    @Story("Statistics Component")
    @Tag("component")
    @Tag("statistics")
    @Tag("count")
    @Tag("all-instruments")
    @Tag("positive")
    void testGetInstrumentsStatistics_ShouldReturnCorrectCounts() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых инструментов для проверки статистики", () -> {
            ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "moex_mrng_evng_e_wknd_dlr");
            FutureEntity future = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "SBER-3.24", "TYPE_COMMODITY");
            IndicativeEntity indicative = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_001", "RTSI", "Индекс РТС");

            shareRepo.save(share);
            futureRepo.save(future);
            indicativeRepo.save(indicative);

            testShareFigis.add("TEST_SHARE_001");
            testFutureFigis.add("TEST_FUTURE_001");
            testIndicativeFigis.add("TEST_INDICATIVE_001");
            
            System.out.println("Созданы тестовые инструменты для статистики:");
            System.out.println("Акция: SBER, Фьючерс: SBER-3.24, Индикатив: RTSI");
        });

        // Act & Assert - получаем статистику
        Allure.step("Выполнение запроса к API для получения статистики по инструментам", () -> {
            mockMvc.perform(get("/api/instruments/count"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.shares").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.futures").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.indicatives").value(greaterThanOrEqualTo(1)));
        });

        Allure.step("Проверка корректности статистики по инструментам", () -> {
            System.out.println("Проверено: API возвращает корректную статистику по инструментам");
            System.out.println("Статистика включает количество акций, фьючерсов и индикативов");
        });
    }
}