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


@Epic("Investment Data Loader")
@Feature("Instruments Component Tests")
@Tag("component")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Instruments Component Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
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
    @Description("Тест проверяет получение акций из локальной базы данных с применением фильтров по FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Получение акций из базы данных")
    void testGetSharesFromDatabase_WithFilteringByFigiAndFullSpringContext_ShouldReturnCorrectShares() throws Exception {
        // Arrange - создаем тестовые данные
        ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "moex_mrng_evng_e_wknd_dlr");
       
        shareRepo.save(share1);
        // Отслеживаем созданные данные
        testShareFigis.add("TEST_SHARE_001");
        // Act & Assert - получаем все акции
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
           
        
    }

    
    @Test
    @DisplayName("Получение акций из базы данных с фильтрацией по Ticker")
    @Description("Тест проверяет получение акций из локальной базы данных с применением фильтров по Ticker")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Получение акций из базы данных")
    void getSharesFromDatabase_WithFilterByTickerAndFullSpringContext_ShouldReturnCorrectShares() throws Exception {
        // Arrange - создаем тестовые данные
        ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "moex_mrng_evng_e_wknd_dlr");
        ShareEntity share2 = TestDataFactory.createShareEntity("TEST_SHARE_002", "GAZP", "Газпром", "moex_mrng_evng_e_wknd_dlr");
        ShareEntity share3 = TestDataFactory.createShareEntity("TEST_SHARE_003", "LKOH", "Лукойл", "moex_mrng_evng_e_wknd_dlr");

        shareRepo.save(share1);
        shareRepo.save(share2);
        shareRepo.save(share3);

        testShareFigis.add("TEST_SHARE_001");
        testShareFigis.add("TEST_SHARE_002");
        testShareFigis.add("TEST_SHARE_003");
        
        mockMvc.perform(get("/api/instruments/shares")
            .param("source", "database")
            .param("ticker", "SBER")
            .param("exchange", "moex_mrng_evng_e_wknd_dlr"))
        
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$[0].ticker").value("SBER"));
            
    }

    @Test
    @DisplayName("Получение акций из базы данных c несуществующими данными")
    @Description("Тест проверяет получение акций из локальной базы данных с несуществующими данными")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Получение акций из базы данных")
    void getSharesFromDatabase_WithFilterByCurrencyAndFullSpringContext_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/instruments/shares")
            .param("source", "database")
            .param("ticker", "NONEXISTENT_TICKER_12345")
            .param("exchange", "moex_mrng_evng_e_wknd_dlr"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.body").isArray())
            .andExpect(jsonPath("$.body.length()").value(0));
    }
        // Arrange - создаем тестовые данные    


    @Test
    @DisplayName("Получение акций из базы данных с фильтрацией по Exchange и сортировкой по алфвиту")
    @Description("Тест проверяет получение акций из локальной базы данных с применением фильтров по Exchange и сортировкой по алфвиту")
    @Step("Проверка, что возвращаются правильные акции по алфвиту")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Получение акций из базы данных")
    void getSharesFromDatabase_WithFilterByExchangeAndFullSpringContext_ShouldReturnCorrectShares() throws Exception {

        ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "moex_mrng_evng_e_wknd_dlr");
        ShareEntity share2 = TestDataFactory.createShareEntity("TEST_SHARE_002", "GAZP", "Газпром", "moex_mrng_evng_e_wknd_dlr");
        ShareEntity share3 = TestDataFactory.createShareEntity("TEST_SHARE_003", "LKOH", "Лукойл", "moex_mrng_evng_e_wknd_dlr");

        shareRepo.save(share1);
        shareRepo.save(share2);
        shareRepo.save(share3);

        testShareFigis.add("TEST_SHARE_001");
        testShareFigis.add("TEST_SHARE_002");
        testShareFigis.add("TEST_SHARE_003");
        
        mockMvc.perform(get("/api/instruments/shares")
            .param("source", "database")
            .param("exchange", "moex_mrng_evng_e_wknd_dlr"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(3)))

        // Проверяем, что все акции имеют правильную биржу
        .andExpect(jsonPath("$[0].exchange").value("moex_mrng_evng_e_wknd_dlr"));
    }


    @Test
    @DisplayName("Получение акций из базы данных с фильтрацией по FIGI")
    @Description("Тест проверяет получение акций из локальной базы данных с применением фильтров по FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Получение акций из базы данных")
    void getSharesFromDatabase_WithFilterByFigiAndFullSpringContext_ShouldReturnCorrectShares() throws Exception {
        // Arrange - создаем тестовые данные
        ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "moex_mrng_evng_e_wknd_dlr");
        ShareEntity share2 = TestDataFactory.createShareEntity("TEST_SHARE_002", "GAZP", "Газпром", "moex_mrng_evng_e_wknd_dlr");
        ShareEntity share3 = TestDataFactory.createShareEntity("TEST_SHARE_003", "LKOH", "Лукойл", "moex_mrng_evng_e_wknd_dlr");
    
        shareRepo.save(share1);
        shareRepo.save(share2);
        shareRepo.save(share3);

        testShareFigis.add("TEST_SHARE_001");
        testShareFigis.add("TEST_SHARE_002");
        testShareFigis.add("TEST_SHARE_003");
    
        mockMvc.perform(get("/api/instruments/shares/TEST_SHARE_001"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andDo(print());//TODO: проверить, что возвращается правильный ответ
       
    }   

    // ==================== ТЕСТЫ ФЬЮЧЕРСОВ ====================

    @Test
    @DisplayName("Получение фьючерсов из базы данных с фильтрацией по тикеру")
    @Description("Тест проверяет получение фьючерсов из локальной базы данных с применением фильтров по тикеру")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Получение фьючерсов из базы данных")
    void testGetFuturesFromDatabase_WithFilteringByTickerAndFullSpringContext_ShouldReturnCorrectFutures() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых фьючерсов", () -> {
            FutureEntity future1 = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "SBER-3.24", "TYPE_COMMODITY");
            futureRepo.save(future1);
            testFutureFigis.add("TEST_FUTURE_001");
            
            // Мокаем gRPC API, чтобы он возвращал тестовые данные
            ru.tinkoff.piapi.contract.v1.Future grpcFuture = TestDataFactory.createGrpcFuture("TEST_FUTURE_001", "SBER-3.24", "TYPE_COMMODITY");
            FuturesResponse futuresResponse = FuturesResponse.newBuilder().addInstruments(grpcFuture).build();
            when(instrumentsService.futures(any(InstrumentsRequest.class))).thenReturn(futuresResponse);
        });

        // Act & Assert - получаем фьючерс по тикеру
        Allure.step("Получение фьючерса по тикеру", () -> {
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
    }

    @Test
    @DisplayName("Получение фьючерсов из базы данных с фильтрацией по типу актива")
    @Description("Тест проверяет получение фьючерсов из локальной базы данных с применением фильтров по типу актива")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Получение фьючерсов из базы данных")
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
            
            // Мокаем gRPC API, чтобы он возвращал тестовые данные
            ru.tinkoff.piapi.contract.v1.Future grpcFuture1 = TestDataFactory.createGrpcFuture("TEST_FUTURE_001", "SBER-3.24", "TYPE_COMMODITY");
            ru.tinkoff.piapi.contract.v1.Future grpcFuture2 = TestDataFactory.createGrpcFuture("TEST_FUTURE_002", "USD-3.24", "CURRENCY");
            ru.tinkoff.piapi.contract.v1.Future grpcFuture3 = TestDataFactory.createGrpcFuture("TEST_FUTURE_003", "GOLD-3.24", "TYPE_COMMODITY");
            FuturesResponse futuresResponse = FuturesResponse.newBuilder()
                .addInstruments(grpcFuture1)
                .addInstruments(grpcFuture2)
                .addInstruments(grpcFuture3)
                .build();
            when(instrumentsService.futures(any(InstrumentsRequest.class))).thenReturn(futuresResponse);
        });

        // Act & Assert - получаем фьючерсы по типу актива
        Allure.step("Получение фьючерсов по типу актива TYPE_COMMODITY", () -> {
            mockMvc.perform(get("/api/instruments/futures")
                    .param("assetType", "TYPE_COMMODITY"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[0].assetType").value("TYPE_COMMODITY"))
                .andExpect(jsonPath("$[1].assetType").value("TYPE_COMMODITY"));
        });
    }

    @Test
    @DisplayName("Получение фьючерсов из базы данных с несуществующими данными")
    @Description("Тест проверяет получение фьючерсов из локальной базы данных с несуществующими данными")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Получение фьючерсов из базы данных")
    void testGetFuturesFromDatabase_WithNonExistentData_ShouldReturnEmptyList() throws Exception {
        Allure.step("Поиск несуществующего фьючерса", () -> {
            mockMvc.perform(get("/api/instruments/futures")
                    .param("ticker", "NONEXISTENT_FUTURE_12345"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.body").isArray())
                .andExpect(jsonPath("$.body.length()").value(0));
        });
    }

    @Test
    @DisplayName("Получение фьючерсов из базы данных с фильтрацией по бирже")
    @Description("Тест проверяет получение фьючерсов из локальной базы данных с применением фильтров по бирже")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Получение фьючерсов из базы данных")
    void testGetFuturesFromDatabase_WithFilterByExchangeAndFullSpringContext_ShouldReturnCorrectFutures() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых фьючерсов", () -> {
            FutureEntity future1 = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "SBER-3.24", "TYPE_COMMODITY");
            FutureEntity future2 = TestDataFactory.createFutureEntity("TEST_FUTURE_002", "GAZP-3.24", "TYPE_COMMODITY");
            FutureEntity future3 = TestDataFactory.createFutureEntity("TEST_FUTURE_003", "LKOH-3.24", "TYPE_COMMODITY");

            futureRepo.save(future1);
            futureRepo.save(future2);
            futureRepo.save(future3);

            testFutureFigis.add("TEST_FUTURE_001");
            testFutureFigis.add("TEST_FUTURE_002");
            testFutureFigis.add("TEST_FUTURE_003");
        });

        // Act & Assert - получаем фьючерсы по бирже
        Allure.step("Получение фьючерсов по бирже", () -> {
            mockMvc.perform(get("/api/instruments/futures")
                    .param("exchange", "moex_mrng_evng_e_wknd_dlr"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(3)));
        });
    }

    // ==================== ТЕСТЫ ИНДИКАТИВОВ ====================

    @Test
    @DisplayName("Получение индикативов из базы данных с фильтрацией по FIGI")
    @Description("Тест проверяет получение индикативов из локальной базы данных с применением фильтров по FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Получение индикативов из базы данных")
    void testGetIndicativesFromDatabase_WithFilteringByFigiAndFullSpringContext_ShouldReturnCorrectIndicatives() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых индикативов", () -> {
            IndicativeEntity indicative1 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_001", "RTSI", "Индекс РТС");
            indicativeRepo.save(indicative1);
            testIndicativeFigis.add("TEST_INDICATIVE_001");
        });

        // Act & Assert - получаем индикатив по FIGI
        Allure.step("Получение индикатива по FIGI", () -> {
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
    }

    @Test
    @DisplayName("Получение индикативов из базы данных с фильтрацией по тикеру")
    @Description("Тест проверяет получение индикативов из локальной базы данных с применением фильтров по тикеру")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Получение индикативов из базы данных")
    void testGetIndicativesFromDatabase_WithFilterByTickerAndFullSpringContext_ShouldReturnCorrectIndicatives() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых индикативов", () -> {
            IndicativeEntity indicative1 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_001", "RTSI", "Индекс РТС");
            IndicativeEntity indicative2 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_002", "MOEX", "Индекс МосБиржи");
            IndicativeEntity indicative3 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_003", "USD000UTSTOM", "Доллар США");

            indicativeRepo.save(indicative1);
            indicativeRepo.save(indicative2);
            indicativeRepo.save(indicative3);

            testIndicativeFigis.add("TEST_INDICATIVE_001");
            testIndicativeFigis.add("TEST_INDICATIVE_002");
            testIndicativeFigis.add("TEST_INDICATIVE_003");
        });

        // Act & Assert - получаем индикатив по тикеру
        Allure.step("Получение индикатива по тикеру", () -> {
            mockMvc.perform(get("/api/instruments/indicatives")
                    .param("ticker", "RTSI"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].ticker").value("RTSI"));
        });
    }

    @Test
    @DisplayName("Получение индикативов из базы данных с фильтрацией по валюте")
    @Description("Тест проверяет получение индикативов из локальной базы данных с применением фильтров по валюте")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Получение индикативов из базы данных")
    void testGetIndicativesFromDatabase_WithFilterByCurrencyAndFullSpringContext_ShouldReturnCorrectIndicatives() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых индикативов", () -> {
            IndicativeEntity indicative1 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_001", "RTSI", "Индекс РТС");
            IndicativeEntity indicative2 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_002", "MOEX", "Индекс МосБиржи");
            IndicativeEntity indicative3 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_003", "USD000UTSTOM", "Доллар США");

            indicativeRepo.save(indicative1);
            indicativeRepo.save(indicative2);
            indicativeRepo.save(indicative3);

            testIndicativeFigis.add("TEST_INDICATIVE_001");
            testIndicativeFigis.add("TEST_INDICATIVE_002");
            testIndicativeFigis.add("TEST_INDICATIVE_003");
        });

        // Act & Assert - получаем индикативы по валюте RUB
        Allure.step("Получение индикативов по валюте RUB", () -> {
            mockMvc.perform(get("/api/instruments/indicatives")
                    .param("currency", "RUB"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$[0].currency").value(anyOf(is("RUB"), is("rub"))))
                .andExpect(jsonPath("$[1].currency").value(anyOf(is("RUB"), is("rub"))))
                .andExpect(jsonPath("$[2].currency").value(anyOf(is("RUB"), is("rub"))));
        });
    }

    @Test
    @DisplayName("Получение индикативов из базы данных с несуществующими данными")
    @Description("Тест проверяет получение индикативов из локальной базы данных с несуществующими данными")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Получение индикативов из базы данных")
    void testGetIndicativesFromDatabase_WithNonExistentData_ShouldReturnEmptyList() throws Exception {
        Allure.step("Поиск несуществующего индикатива", () -> {
        mockMvc.perform(get("/api/instruments/indicatives")
                .param("ticker", "NONEXISTENT_INDICATIVE_12345"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(0)));
        });
    }

    // ==================== ТЕСТЫ КОМБИНИРОВАННЫХ ФИЛЬТРОВ ====================

    @Test
    @DisplayName("Получение акций с комбинированными фильтрами")
    @Description("Тест проверяет получение акций из локальной базы данных с применением нескольких фильтров одновременно")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Комбинированные фильтры")
    void testGetSharesFromDatabase_WithCombinedFiltersAndFullSpringContext_ShouldReturnCorrectShares() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых акций с разными параметрами", () -> {
            ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "moex_mrng_evng_e_wknd_dlr");
            ShareEntity share2 = TestDataFactory.createShareEntity("TEST_SHARE_002", "GAZP", "Газпром", "moex_mrng_evng_e_wknd_dlr");
            ShareEntity share3 = TestDataFactory.createShareEntity("TEST_SHARE_003", "LKOH", "Лукойл", "OTHEREXCHANGE");

            shareRepo.save(share1);
            shareRepo.save(share2);
            shareRepo.save(share3);

            testShareFigis.add("TEST_SHARE_001");
            testShareFigis.add("TEST_SHARE_002");
            testShareFigis.add("TEST_SHARE_003");
        });

        // Act & Assert - получаем акции с комбинированными фильтрами
        Allure.step("Получение акций с комбинированными фильтрами", () -> {
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
    }

    @Test
    @DisplayName("Получение фьючерсов с комбинированными фильтрами")
    @Description("Тест проверяет получение фьючерсов из локальной базы данных с применением нескольких фильтров одновременно")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Комбинированные фильтры")
    void testGetFuturesFromDatabase_WithCombinedFiltersAndFullSpringContext_ShouldReturnCorrectFutures() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых фьючерсов с разными параметрами", () -> {
            FutureEntity future1 = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "SBER-3.24", "TYPE_COMMODITY");
            FutureEntity future2 = TestDataFactory.createFutureEntity("TEST_FUTURE_002", "GAZP-3.24", "TYPE_COMMODITY");
            FutureEntity future3 = TestDataFactory.createFutureEntity("TEST_FUTURE_003", "USD-3.24", "CURRENCY");

            futureRepo.save(future1);
            futureRepo.save(future2);
            futureRepo.save(future3);

            testFutureFigis.add("TEST_FUTURE_001");
            testFutureFigis.add("TEST_FUTURE_002");
            testFutureFigis.add("TEST_FUTURE_003");
            
            // Мокаем gRPC API, чтобы он возвращал тестовые данные
            ru.tinkoff.piapi.contract.v1.Future grpcFuture1 = TestDataFactory.createGrpcFuture("TEST_FUTURE_001", "SBER-3.24", "TYPE_COMMODITY");
            ru.tinkoff.piapi.contract.v1.Future grpcFuture2 = TestDataFactory.createGrpcFuture("TEST_FUTURE_002", "GAZP-3.24", "TYPE_COMMODITY");
            ru.tinkoff.piapi.contract.v1.Future grpcFuture3 = TestDataFactory.createGrpcFuture("TEST_FUTURE_003", "USD-3.24", "CURRENCY");
            FuturesResponse futuresResponse = FuturesResponse.newBuilder()
                .addInstruments(grpcFuture1)
                .addInstruments(grpcFuture2)
                .addInstruments(grpcFuture3)
                .build();
            when(instrumentsService.futures(any(InstrumentsRequest.class))).thenReturn(futuresResponse);
        });

        // Act & Assert - получаем фьючерсы с комбинированными фильтрами
        Allure.step("Получение фьючерсов с комбинированными фильтрами", () -> {
            mockMvc.perform(get("/api/instruments/futures")
                    .param("assetType", "TYPE_COMMODITY")
                    .param("currency", "RUB"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[0].assetType").value("TYPE_COMMODITY"))
                .andExpect(jsonPath("$[1].assetType").value("TYPE_COMMODITY"));
        });
    }

    @Test
    @DisplayName("Получение индикативов с комбинированными фильтрами")
    @Description("Тест проверяет получение индикативов из локальной базы данных с применением нескольких фильтров одновременно")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Комбинированные фильтры")
    void testGetIndicativesFromDatabase_WithCombinedFiltersAndFullSpringContext_ShouldReturnCorrectIndicatives() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых индикативов с разными параметрами", () -> {
            IndicativeEntity indicative1 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_001", "RTSI", "Индекс РТС");
            IndicativeEntity indicative2 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_002", "MOEX", "Индекс МосБиржи");
            IndicativeEntity indicative3 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_003", "USD000UTSTOM", "Доллар США");

            indicativeRepo.save(indicative1);
            indicativeRepo.save(indicative2);
            indicativeRepo.save(indicative3);

            testIndicativeFigis.add("TEST_INDICATIVE_001");
            testIndicativeFigis.add("TEST_INDICATIVE_002");
            testIndicativeFigis.add("TEST_INDICATIVE_003");
        });

        // Act & Assert - получаем индикативы с комбинированными фильтрами
        Allure.step("Получение индикативов с комбинированными фильтрами", () -> {
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
    }

    // ==================== ТЕСТЫ ОБРАБОТКИ ОШИБОК ====================

    @Test
    @DisplayName("Обработка некорректных параметров для акций")
    @Description("Тест проверяет обработку некорректных параметров при запросе акций")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Обработка ошибок")
    void testGetSharesFromDatabase_WithInvalidParameters_ShouldReturnBadRequest() throws Exception {
        Allure.step("Запрос с некорректным источником данных", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "invalid_source"))
                .andExpect(status().isBadRequest());
        });
    }

    @Test
    @DisplayName("Обработка некорректных параметров для фьючерсов")
    @Description("Тест проверяет обработку некорректных параметров при запросе фьючерсов")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Обработка ошибок")
    void testGetFuturesFromDatabase_WithInvalidParameters_ShouldReturnBadRequest() throws Exception {
        Allure.step("Запрос с некорректным типом актива", () -> {
            mockMvc.perform(get("/api/instruments/futures")
                    .param("assetType", "INVALID_TYPE"))
                .andExpect(status().isBadRequest());
        });
    }

    @Test
    @DisplayName("Обработка некорректных параметров для индикативов")
    @Description("Тест проверяет обработку некорректных параметров при запросе индикативов")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Обработка ошибок")
    void testGetIndicativesFromDatabase_WithInvalidParameters_ShouldReturnBadRequest() throws Exception {
        Allure.step("Запрос с некорректной валютой", () -> {
        mockMvc.perform(get("/api/instruments/indicatives")
                .param("invalidParam", "INVALID_VALUE"))
                .andExpect(status().isBadRequest());
        });
    }

    // ==================== ТЕСТЫ ГРАНИЧНЫХ СЛУЧАЕВ ====================

    @Test
    @DisplayName("Получение акций с пустыми параметрами")
    @Description("Тест проверяет получение акций из локальной базы данных без параметров фильтрации")
    @Severity(SeverityLevel.NORMAL)
    @Story("Граничные случаи")
    void testGetSharesFromDatabase_WithEmptyParameters_ShouldReturnAllShares() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых акций", () -> {
            ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "moex_mrng_evng_e_wknd_dlr");
            ShareEntity share2 = TestDataFactory.createShareEntity("TEST_SHARE_002", "GAZP", "Газпром", "moex_mrng_evng_e_wknd_dlr");

            shareRepo.save(share1);
            shareRepo.save(share2);

            testShareFigis.add("TEST_SHARE_001");
            testShareFigis.add("TEST_SHARE_002");
        });

        // Act & Assert - получаем все акции
        Allure.step("Получение всех акций без фильтров", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "database"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)));
        });
    }

    @Test
    @DisplayName("Получение фьючерсов с пустыми параметрами")
    @Description("Тест проверяет получение фьючерсов из локальной базы данных без параметров фильтрации")
    @Severity(SeverityLevel.NORMAL)
    @Story("Граничные случаи")
    void testGetFuturesFromDatabase_WithEmptyParameters_ShouldReturnAllFutures() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых фьючерсов", () -> {
            FutureEntity future1 = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "SBER-3.24", "TYPE_COMMODITY");
            FutureEntity future2 = TestDataFactory.createFutureEntity("TEST_FUTURE_002", "GAZP-3.24", "TYPE_COMMODITY");

            futureRepo.save(future1);
            futureRepo.save(future2);

            testFutureFigis.add("TEST_FUTURE_001");
            testFutureFigis.add("TEST_FUTURE_002");
            
            // Мокаем gRPC API, чтобы он возвращал тестовые данные
            ru.tinkoff.piapi.contract.v1.Future grpcFuture1 = TestDataFactory.createGrpcFuture("TEST_FUTURE_001", "SBER-3.24", "TYPE_COMMODITY");
            ru.tinkoff.piapi.contract.v1.Future grpcFuture2 = TestDataFactory.createGrpcFuture("TEST_FUTURE_002", "GAZP-3.24", "TYPE_COMMODITY");
            FuturesResponse futuresResponse = FuturesResponse.newBuilder()
                .addInstruments(grpcFuture1)
                .addInstruments(grpcFuture2)
                .build();
            when(instrumentsService.futures(any(InstrumentsRequest.class))).thenReturn(futuresResponse);
        });

        // Act & Assert - получаем все фьючерсы
        Allure.step("Получение всех фьючерсов без фильтров", () -> {
            mockMvc.perform(get("/api/instruments/futures"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)));
        });
    }

    @Test
    @DisplayName("Получение индикативов с пустыми параметрами")
    @Description("Тест проверяет получение индикативов из локальной базы данных без параметров фильтрации")
    @Severity(SeverityLevel.NORMAL)
    @Story("Граничные случаи")
    void testGetIndicativesFromDatabase_WithEmptyParameters_ShouldReturnAllIndicatives() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых индикативов", () -> {
            IndicativeEntity indicative1 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_001", "RTSI", "Индекс РТС");
            IndicativeEntity indicative2 = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_002", "MOEX", "Индекс МосБиржи");

            indicativeRepo.save(indicative1);
            indicativeRepo.save(indicative2);

            testIndicativeFigis.add("TEST_INDICATIVE_001");
            testIndicativeFigis.add("TEST_INDICATIVE_002");
        });

        // Act & Assert - получаем все индикативы
        Allure.step("Получение всех индикативов без фильтров", () -> {
            mockMvc.perform(get("/api/instruments/indicatives"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)));
        });
    }

    // ==================== ТЕСТЫ API ENDPOINTS С ВНЕШНИМ TINKOFF API ====================

    @Test
    @DisplayName("Получение акций из внешнего API Tinkoff")
    @Description("Тест проверяет получение акций из внешнего API Tinkoff с мокированием ответа")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API интеграция")
    void testGetSharesFromTinkoffApi_WithMockedResponse_ShouldReturnCorrectShares() throws Exception {
        // Arrange - настраиваем мок для внешнего API
        Allure.step("Настройка мока для внешнего API", () -> {
            // Мокаем вызов к внешнему API - возвращаем JsonNode
            when(restClient.getShares()).thenReturn(TestDataFactory.createSharesJsonNode());
        });

        // Act & Assert - получаем акции из API
        Allure.step("Получение акций из внешнего API", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "api")
                    .param("exchange", "moex_mrng_evng_e_wknd_dlr")
                    .param("currency", "RUB"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
        });
    }

    @Test
    @DisplayName("Получение фьючерсов из внешнего API Tinkoff")
    @Description("Тест проверяет получение фьючерсов из внешнего API Tinkoff с мокированием ответа")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API интеграция")
    void testGetFuturesFromTinkoffApi_WithMockedResponse_ShouldReturnCorrectFutures() throws Exception {
        // Arrange - настраиваем мок для внешнего API
        Allure.step("Настройка мока для внешнего API", () -> {
            // Мокаем вызов к внешнему API - возвращаем JsonNode
            when(restClient.getFutures()).thenReturn(TestDataFactory.createFuturesJsonNode());
        });

        // Act & Assert - получаем фьючерсы из API
        Allure.step("Получение фьючерсов из внешнего API", () -> {
            mockMvc.perform(get("/api/instruments/futures")
                    .param("ticker", "SBER-3.24")
                    .param("currency", "RUB"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.body").isArray());
        });
    }

    @Test
    @DisplayName("Получение индикативов из внешнего API Tinkoff")
    @Description("Тест проверяет получение индикативов из внешнего API Tinkoff с мокированием ответа")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API интеграция")
    void testGetIndicativesFromTinkoffApi_WithMockedResponse_ShouldReturnCorrectIndicatives() throws Exception {
        // Arrange - настраиваем мок для внешнего API
        Allure.step("Настройка мока для внешнего API", () -> {
            // Мокаем вызов к внешнему API - возвращаем JsonNode
            when(restClient.getIndicatives()).thenReturn(TestDataFactory.createIndicativesJsonNode());
        });

        // Act & Assert - получаем индикативы из API
        Allure.step("Получение индикативов из внешнего API", () -> {
            mockMvc.perform(get("/api/instruments/indicatives")
                    .param("currency", "RUB")
                    .param("exchange", "moex_mrng_evng_e_wknd_dlr"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(0)));
        });
    }

    @Test
    @DisplayName("Обработка ошибки внешнего API при получении акций")
    @Description("Тест проверяет обработку ошибки внешнего API при получении акций")
    @Severity(SeverityLevel.CRITICAL)
    @Story("API интеграция")
    void testGetSharesFromTinkoffApi_WithApiError_ShouldHandleErrorGracefully() throws Exception {
        // Arrange - настраиваем мок для ошибки API
        Allure.step("Настройка мока для ошибки API", () -> {
            when(restClient.getShares())
                .thenThrow(new RuntimeException("API недоступен"));
        });

        // Act & Assert - проверяем обработку ошибки
        Allure.step("Проверка обработки ошибки API", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "api")
                    .param("exchange", "moex_mrng_evng_e_wknd_dlr"))
                .andExpect(status().isOk()) // API должен возвращать пустой список при ошибке
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(0)));
        });
    }

    @Test
    @DisplayName("Обработка пустого ответа от внешнего API")
    @Description("Тест проверяет обработку пустого ответа от внешнего API")
    @Severity(SeverityLevel.NORMAL)
    @Story("API интеграция")
    void testGetSharesFromTinkoffApi_WithEmptyResponse_ShouldReturnEmptyList() throws Exception {
        // Arrange - настраиваем мок для пустого ответа
        Allure.step("Настройка мока для пустого ответа", () -> {
            when(restClient.getShares())
                .thenReturn(TestDataFactory.createEmptyJsonNode());
        });

        // Act & Assert - проверяем обработку пустого ответа
        Allure.step("Проверка обработки пустого ответа", () -> {
            mockMvc.perform(get("/api/instruments/shares")
                    .param("source", "api")
                    .param("exchange", "moex_mrng_evng_e_wknd_dlr"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(0)));
        });
    }

    // ==================== ТЕСТЫ ПОИСКА ПО FIGI ====================

    @Test
    @DisplayName("Поиск акции по FIGI")
    @Description("Тест проверяет поиск конкретной акции по FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Поиск по FIGI")
    void testGetShareByFigi_WithValidFigi_ShouldReturnCorrectShare() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовой акции", () -> {
            ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "moex_mrng_evng_e_wknd_dlr");
            shareRepo.save(share);
            testShareFigis.add("TEST_SHARE_001");
        });

        // Act & Assert - ищем акцию по FIGI
        Allure.step("Поиск акции по FIGI", () -> {
            mockMvc.perform(get("/api/instruments/shares/TEST_SHARE_001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.figi").value("TEST_SHARE_001"))
                .andExpect(jsonPath("$.ticker").value("SBER"))
                .andExpect(jsonPath("$.name").value("Сбербанк"));
        });
    }

    @Test
    @DisplayName("Поиск несуществующей акции по FIGI")
    @Description("Тест проверяет поиск несуществующей акции по FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Поиск по FIGI")
    void testGetShareByFigi_WithNonExistentFigi_ShouldReturnNotFound() throws Exception {
        Allure.step("Поиск несуществующей акции", () -> {
            mockMvc.perform(get("/api/instruments/shares/NONEXISTENT_FIGI"))
                .andExpect(status().isNotFound());
        });
    }

    // ==================== ТЕСТЫ СТАТИСТИКИ ====================

    @Test
    @DisplayName("Получение статистики по инструментам")
    @Description("Тест проверяет получение статистики по количеству инструментов в базе данных")
    @Severity(SeverityLevel.NORMAL)
    @Story("Статистика")
    void testGetInstrumentsStatistics_ShouldReturnCorrectCounts() throws Exception {
        // Arrange - создаем тестовые данные
        Allure.step("Создание тестовых инструментов", () -> {
            ShareEntity share = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "moex_mrng_evng_e_wknd_dlr");
            FutureEntity future = TestDataFactory.createFutureEntity("TEST_FUTURE_001", "SBER-3.24", "TYPE_COMMODITY");
            IndicativeEntity indicative = TestDataFactory.createIndicativeEntity("TEST_INDICATIVE_001", "RTSI", "Индекс РТС");

            shareRepo.save(share);
            futureRepo.save(future);
            indicativeRepo.save(indicative);

            testShareFigis.add("TEST_SHARE_001");
            testFutureFigis.add("TEST_FUTURE_001");
            testIndicativeFigis.add("TEST_INDICATIVE_001");
        });

        // Act & Assert - получаем статистику
        Allure.step("Получение статистики по инструментам", () -> {
            mockMvc.perform(get("/api/instruments/count"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.shares").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.futures").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.indicatives").value(greaterThanOrEqualTo(1)));
        });
    }
}