package com.example.InvestmentDataLoaderService.component;

import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

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

import java.util.Set;
import java.util.HashSet;


@Epic("Investment Data Loader")
@Feature("Instruments Component Tests")
@Tag("component")
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
        // Очистка моков
        reset(restClient, instrumentsService);
     
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
        ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "TESTMOEX");
       
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
        ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "TESTMOEX");
        ShareEntity share2 = TestDataFactory.createShareEntity("TEST_SHARE_002", "GAZP", "Газпром", "TESTMOEX");
        ShareEntity share3 = TestDataFactory.createShareEntity("TEST_SHARE_003", "LKOH", "Лукойл", "TESTMOEX");

        shareRepo.save(share1);
        shareRepo.save(share2);
        shareRepo.save(share3);

        testShareFigis.add("TEST_SHARE_001");
        testShareFigis.add("TEST_SHARE_002");
        testShareFigis.add("TEST_SHARE_003");
        
        mockMvc.perform(get("/api/instruments/shares")
            .param("source", "database")
            .param("ticker", "SBER")
            .param("exchange", "TESTMOEX"))
        
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].figi").value("TEST_SHARE_001"))
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
            .param("ticker", "UNKNOWNTICKER")
            .param("exchange", "TESTMOEX"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(0));
    }
        // Arrange - создаем тестовые данные    


    @Test
    @DisplayName("Получение акций из базы данных с фильтрацией по Exchange и сортировкой по алфвиту")
    @Description("Тест проверяет получение акций из локальной базы данных с применением фильтров по Exchange и сортировкой по алфвиту")
    @Step("Проверка, что возвращаются правильные акции по алфвиту")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Получение акций из базы данных")
    void getSharesFromDatabase_WithFilterByExchangeAndFullSpringContext_ShouldReturnCorrectShares() throws Exception {

        ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "TESTMOEX");
        ShareEntity share2 = TestDataFactory.createShareEntity("TEST_SHARE_002", "GAZP", "Газпром", "TESTMOEX");
        ShareEntity share3 = TestDataFactory.createShareEntity("TEST_SHARE_003", "LKOH", "Лукойл", "TESTMOEX");

        shareRepo.save(share1);
        shareRepo.save(share2);
        shareRepo.save(share3);

        testShareFigis.add("TEST_SHARE_001");
        testShareFigis.add("TEST_SHARE_002");
        testShareFigis.add("TEST_SHARE_003");
        
        mockMvc.perform(get("/api/instruments/shares")
            .param("source", "database")
            .param("exchange", "TESTMOEX"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(3))

            // Проверяем, что акции получены в правильном порядке
            .andExpect(jsonPath("$[0].figi").value("TEST_SHARE_002"))
            .andExpect(jsonPath("$[1].figi").value("TEST_SHARE_003"))
            .andExpect(jsonPath("$[2].figi").value("TEST_SHARE_001"))
            .andExpect(jsonPath("$[0].ticker").value("GAZP"))
            .andExpect(jsonPath("$[1].ticker").value("LKOH"))
            .andExpect(jsonPath("$[2].ticker").value("SBER"));
    }


    @Test
    @DisplayName("Получение акций из базы данных с фильтрацией по FIGI")
    @Description("Тест проверяет получение акций из локальной базы данных с применением фильтров по FIGI")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Получение акций из базы данных")
    void getSharesFromDatabase_WithFilterByFigiAndFullSpringContext_ShouldReturnCorrectShares() throws Exception {
        // Arrange - создаем тестовые данные
        ShareEntity share1 = TestDataFactory.createShareEntity("TEST_SHARE_001", "SBER", "Сбербанк", "TESTMOEX");
        ShareEntity share2 = TestDataFactory.createShareEntity("TEST_SHARE_002", "GAZP", "Газпром", "TESTMOEX");
        ShareEntity share3 = TestDataFactory.createShareEntity("TEST_SHARE_003", "LKOH", "Лукойл", "TESTMOEX");
    
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

    
}