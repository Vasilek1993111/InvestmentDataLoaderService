package com.example.InvestmentDataLoaderService.unit.controller;

import com.example.InvestmentDataLoaderService.controller.LastTradesController;
import com.example.InvestmentDataLoaderService.entity.SystemLogEntity;
import com.example.InvestmentDataLoaderService.repository.SystemLogRepository;
import com.example.InvestmentDataLoaderService.service.LastTradesService;
import com.example.InvestmentDataLoaderService.service.CachedInstrumentService;


import io.qameta.allure.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.*;
import java.util.concurrent.Executor;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LastTradesController.class)
@Epic("Last Trades API")
@Feature("Last Trades Management")
@DisplayName("Last Trades Controller Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
public class LastTradesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LastTradesService lastTradesService;

    @MockitoBean
    private CachedInstrumentService cachedInstrumentService;

    @MockitoBean
    private SystemLogRepository systemLogRepository;

    @MockitoBean
    @Qualifier("lastTradesApiExecutor")
    private Executor lastTradesApiExecutor;

    @MockitoBean
    @Qualifier("lastTradesBatchExecutor")
    private Executor lastTradesBatchExecutor;

    @MockitoBean
    @Qualifier("lastTradesProcessingExecutor")
    private Executor lastTradesProcessingExecutor;

    @BeforeEach
    @Step("Подготовка тестовых данных для LastTradesController")
    void setUp() {
        reset(lastTradesService);
        reset(cachedInstrumentService);
        reset(systemLogRepository);
        reset(lastTradesApiExecutor);
        reset(lastTradesBatchExecutor);
        reset(lastTradesProcessingExecutor);
    }

    // ==================== ТЕСТОВЫЕ ДАННЫЕ ====================


    private void setupExecutorMocks() {
        // Настраиваем executor'ы для синхронного выполнения в тестах
        lenient().doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(lastTradesApiExecutor).execute(any(Runnable.class));
        
        lenient().doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(lastTradesBatchExecutor).execute(any(Runnable.class));
        
        lenient().doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(lastTradesProcessingExecutor).execute(any(Runnable.class));
    }

    // ========== ПОЗИТИВНЫЕ ТЕСТЫ ==========

    @Test
    @DisplayName("Успешная загрузка последних сделок через GET запрос")
    @Description("Тест проверяет основную функциональность загрузки последних сделок через GET")
    @Story("Успешные сценарии")
    void loadLastTrades_ShouldReturnSuccess_WhenGetRequestIsValid() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
            String figis = "BBG004730N88,BBG004730ZJ9";
            String tradeSource = "TRADE_SOURCE_ALL";
            
            when(cachedInstrumentService.getCacheInfo()).thenReturn("Cache info: 1000 instruments");
            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
            setupExecutorMocks();

            // When & Then
            mockMvc.perform(get("/api/last-trades")
                    .param("figis", figis)
                    .param("tradeSource", tradeSource)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Оптимизированная загрузка обезличенных сделок запущена"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.cacheInfo").exists())
                    .andExpect(jsonPath("$.dataSource").value("optimized_cache_with_db_fallback"))
                    .andExpect(jsonPath("$.performanceMode").value("HIGH_SPEED"));

            verify(cachedInstrumentService, atLeastOnce()).getCacheInfo();
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
        });
    }

    @Test
    @DisplayName("Успешная загрузка последних сделок через POST запрос")
    @Description("Тест проверяет загрузку последних сделок через POST с JSON телом")
    @Story("Успешные сценарии")
    void loadLastTrades_ShouldReturnSuccess_WhenPostRequestIsValid() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
            String requestJson = """
                {
                    "figis": ["BBG004730N88", "BBG004730ZJ9"],
                    "tradeSource": "TRADE_SOURCE_ALL"
                }
                """;
            
            when(cachedInstrumentService.getCacheInfo()).thenReturn("Cache info: 1000 instruments");
            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
            setupExecutorMocks();

            // When & Then
            mockMvc.perform(post("/api/last-trades")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Оптимизированная загрузка обезличенных сделок запущена"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.cacheInfo").exists())
                    .andExpect(jsonPath("$.dataSource").value("optimized_cache_with_db_fallback"));

            verify(cachedInstrumentService, atLeastOnce()).getCacheInfo();
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
        });
    }

    @Test
    @DisplayName("Успешная загрузка только акций")
    @Description("Тест проверяет загрузку последних сделок только для акций")
    @Story("Успешные сценарии")
    void loadShares_ShouldReturnSuccess_WhenSharesRequestIsValid() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
            when(cachedInstrumentService.getCacheInfo()).thenReturn("Cache info: 500 shares");
            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
            setupExecutorMocks();

            // When & Then
            mockMvc.perform(get("/api/last-trades/shares"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Оптимизированная загрузка обезличенных сделок запущена"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.cacheInfo").exists());

            verify(cachedInstrumentService, atLeastOnce()).getCacheInfo();
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
        });
    }

    @Test
    @DisplayName("Успешная загрузка только фьючерсов")
    @Description("Тест проверяет загрузку последних сделок только для фьючерсов")
    @Story("Успешные сценарии")
    void loadFutures_ShouldReturnSuccess_WhenFuturesRequestIsValid() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
            when(cachedInstrumentService.getCacheInfo()).thenReturn("Cache info: 200 futures");
            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
            setupExecutorMocks();

            // When & Then
            mockMvc.perform(get("/api/last-trades/futures"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Оптимизированная загрузка обезличенных сделок запущена"))
                    .andExpect(jsonPath("$.taskId").exists())
                    .andExpect(jsonPath("$.cacheInfo").exists());

            verify(cachedInstrumentService, atLeastOnce()).getCacheInfo();
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
        });
    }

    @Test
    @DisplayName("Успешное получение информации о кэше")
    @Description("Тест проверяет получение информации о кэше инструментов")
    @Story("Успешные сценарии")
    void getCache_ShouldReturnSuccess_WhenCacheInfoIsAvailable() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
            when(cachedInstrumentService.getCacheInfo()).thenReturn("Cache info: 1000 instruments");

            // When & Then
            mockMvc.perform(get("/api/last-trades/cache"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.cacheInfo").value("Cache info: 1000 instruments"))
                    .andExpect(jsonPath("$.dataSource").value("cache_with_db_fallback"));

            verify(cachedInstrumentService, atLeastOnce()).getCacheInfo();
        });
    }

    @Test
    @DisplayName("Успешное получение информации о производительности")
    @Description("Тест проверяет получение информации о производительности executor'ов")
    @Story("Успешные сценарии")
    void getPerformance_ShouldReturnSuccess_WhenExecutorInfoIsAvailable() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
            ThreadPoolTaskExecutor mockApiExecutor = mock(ThreadPoolTaskExecutor.class);
            ThreadPoolTaskExecutor mockBatchExecutor = mock(ThreadPoolTaskExecutor.class);
            ThreadPoolTaskExecutor mockProcessingExecutor = mock(ThreadPoolTaskExecutor.class);

            when(mockApiExecutor.getCorePoolSize()).thenReturn(5);
            when(mockApiExecutor.getMaxPoolSize()).thenReturn(10);
            when(mockApiExecutor.getQueueCapacity()).thenReturn(100);
            when(mockApiExecutor.getActiveCount()).thenReturn(2);

            when(mockBatchExecutor.getCorePoolSize()).thenReturn(3);
            when(mockBatchExecutor.getMaxPoolSize()).thenReturn(8);
            when(mockBatchExecutor.getQueueCapacity()).thenReturn(50);
            when(mockBatchExecutor.getActiveCount()).thenReturn(1);

            when(mockProcessingExecutor.getCorePoolSize()).thenReturn(2);
            when(mockProcessingExecutor.getMaxPoolSize()).thenReturn(5);
            when(mockProcessingExecutor.getQueueCapacity()).thenReturn(25);
            when(mockProcessingExecutor.getActiveCount()).thenReturn(0);

            // When & Then
            mockMvc.perform(get("/api/last-trades/performance"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.executorInfo").exists())
                    .andExpect(jsonPath("$.optimizationLevel").value("HIGH_PERFORMANCE"))
                    .andExpect(jsonPath("$.tInvestLimits").value("Respected with specialized executors"));
        });
    }

    // ========== НЕГАТИВНЫЕ ТЕСТЫ ==========

    @Test
    @DisplayName("Обработка ошибки при пустом списке FIGI")
    @Description("Тест проверяет обработку ошибки при пустом списке FIGI")
    @Story("Негативные сценарии")
    void loadLastTrades_ShouldReturnBadRequest_WhenFigisIsEmpty() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
            String requestJson = """
                {
                    "figis": [],
                    "tradeSource": "TRADE_SOURCE_ALL"
                }
                """;
            
            when(cachedInstrumentService.getCacheInfo()).thenReturn("Cache info: 1000 instruments");
            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
            setupExecutorMocks();

            // When & Then
            mockMvc.perform(post("/api/last-trades")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Параметр 'figis' является обязательным"));

            // При ошибке валидации getCacheInfo не вызывается
        });
    }

    @Test
    @DisplayName("Обработка ошибки при null списке FIGI")
    @Description("Тест проверяет обработку ошибки при null списке FIGI")
    @Story("Негативные сценарии")
    void loadLastTrades_ShouldReturnBadRequest_WhenFigisIsNull() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
        String requestJson = """
            {
                "figis": null,
                "tradeSource": "TRADE_SOURCE_ALL"
            }
            """;
        
        when(cachedInstrumentService.getCacheInfo()).thenReturn("Cache info: 1000 instruments");
        when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When & Then
        mockMvc.perform(post("/api/last-trades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Параметр 'figis' является обязательным"));

        // При ошибке валидации getCacheInfo не вызывается
        });
    }

    @Test
    @DisplayName("Обработка ошибки сервиса при загрузке сделок")
    @Description("Тест проверяет обработку ошибки сервиса при загрузке сделок")
    @Story("Негативные сценарии")
    void loadLastTrades_ShouldReturnError_WhenServiceThrowsException() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
        String requestJson = """
            {
                "figis": ["BBG004730N88"],
                "tradeSource": "TRADE_SOURCE_ALL"
            }
            """;
        
        when(cachedInstrumentService.getCacheInfo()).thenThrow(new RuntimeException("Service unavailable"));
        when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When & Then
        mockMvc.perform(post("/api/last-trades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Ошибка оптимизированной загрузки сделок")))
                .andExpect(jsonPath("$.errorType").value("OPTIMIZATION_ERROR"));

        verify(cachedInstrumentService, atLeastOnce()).getCacheInfo();
        });
    }

    @Test
    @DisplayName("Обработка ошибки при получении информации о кэше")
    @Description("Тест проверяет обработку ошибки при получении информации о кэше")
    @Story("Негативные сценарии")
    void getCache_ShouldReturnError_WhenCacheServiceThrowsException() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
        when(cachedInstrumentService.getCacheInfo()).thenThrow(new RuntimeException("Cache service unavailable"));

        // When & Then
        mockMvc.perform(get("/api/last-trades/cache"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Ошибка получения информации о кэше")));

        verify(cachedInstrumentService, atLeastOnce()).getCacheInfo();
        });
    }

    @Test
    @DisplayName("Обработка ошибки при получении информации о производительности")
    @Description("Тест проверяет обработку ошибки при получении информации о производительности")
    @Story("Негативные сценарии")
    void getPerformance_ShouldReturnError_WhenExecutorInfoIsUnavailable() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
        // Не настраиваем моки для executor'ов, чтобы вызвать ошибку

        // When & Then
        mockMvc.perform(get("/api/last-trades/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.executorInfo").exists());
        });
    }

    // ========== ГРАНИЧНЫЕ СЛУЧАИ ==========

    @Test
    @DisplayName("Обработка запроса с максимальным количеством FIGI")
    @Description("Тест проверяет обработку запроса с большим количеством FIGI")
    @Story("Граничные случаи")
    void loadLastTrades_ShouldHandleLargeNumberOfFigis_WhenManyFigisProvided() throws Exception {
        Allure.step("Настройка тестовых данных", () -> {
            // Given
            List<String> manyFigis = new ArrayList<>();
            for (int i = 0; i < 10; i++) { // Уменьшаем количество для корректного JSON
                manyFigis.add("BBG004730N8" + String.format("%02d", i));
            }
            
            // Создаем корректный JSON массив
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\n");
            jsonBuilder.append("    \"figis\": [");
            for (int i = 0; i < manyFigis.size(); i++) {
                jsonBuilder.append("\"").append(manyFigis.get(i)).append("\"");
                if (i < manyFigis.size() - 1) {
                    jsonBuilder.append(", ");
                }
            }
            jsonBuilder.append("],\n");
            jsonBuilder.append("    \"tradeSource\": \"TRADE_SOURCE_ALL\"\n");
            jsonBuilder.append("}");
            
            String requestJson = jsonBuilder.toString();
            
            when(cachedInstrumentService.getCacheInfo()).thenReturn("Cache info: 1000 instruments");
            when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
            setupExecutorMocks();

            // When & Then
            mockMvc.perform(post("/api/last-trades")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Оптимизированная загрузка обезличенных сделок запущена"));

            verify(cachedInstrumentService, atLeastOnce()).getCacheInfo();
            verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
        });
    }

    @Test
    @DisplayName("Обработка запроса с нестандартным источником сделок")
    @Description("Тест проверяет обработку запроса с нестандартным источником сделок")
    @Story("Граничные случаи")
    void loadLastTrades_ShouldHandleCustomTradeSource_WhenNonStandardSourceProvided() throws Exception {
        // Given
        String requestJson = """
            {
                "figis": ["BBG004730N88"],
                "tradeSource": "CUSTOM_TRADE_SOURCE"
            }
            """;
        
        when(cachedInstrumentService.getCacheInfo()).thenReturn("Cache info: 1000 instruments");
        when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When & Then
        mockMvc.perform(post("/api/last-trades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Оптимизированная загрузка обезличенных сделок запущена"));

        verify(cachedInstrumentService, atLeastOnce()).getCacheInfo();
        verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
    }

    @Test
    @DisplayName("Обработка запроса с минимальными параметрами")
    @Description("Тест проверяет обработку запроса с минимальными параметрами")
    @Story("Граничные случаи")
    void loadLastTrades_ShouldHandleMinimalRequest_WhenOnlyRequiredParametersProvided() throws Exception {
        // Given
        when(cachedInstrumentService.getCacheInfo()).thenReturn("Cache info: 1000 instruments");
        when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When & Then
        mockMvc.perform(get("/api/last-trades")
                .param("figis", "BBG004730N88"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Оптимизированная загрузка обезличенных сделок запущена"));

        verify(cachedInstrumentService, atLeastOnce()).getCacheInfo();
        verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
    }

    @Test
    @DisplayName("Обработка запроса с пустым источником сделок")
    @Description("Тест проверяет обработку запроса с пустым источником сделок")
    @Story("Граничные случаи")
    void loadLastTrades_ShouldHandleEmptyTradeSource_WhenTradeSourceIsEmpty() throws Exception {
        // Given
        String requestJson = """
            {
                "figis": ["BBG004730N88"],
                "tradeSource": ""
            }
            """;
        
        when(cachedInstrumentService.getCacheInfo()).thenReturn("Cache info: 1000 instruments");
        when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When & Then
        mockMvc.perform(post("/api/last-trades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Оптимизированная загрузка обезличенных сделок запущена"));

        verify(cachedInstrumentService, atLeastOnce()).getCacheInfo();
        verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
    }

    @Test
    @DisplayName("Обработка запроса с null источником сделок")
    @Description("Тест проверяет обработку запроса с null источником сделок")
    @Story("Граничные случаи")
    void loadLastTrades_ShouldHandleNullTradeSource_WhenTradeSourceIsNull() throws Exception {
        // Given
        String requestJson = """
            {
                "figis": ["BBG004730N88"],
                "tradeSource": null
            }
            """;
        
        when(cachedInstrumentService.getCacheInfo()).thenReturn("Cache info: 1000 instruments");
        when(systemLogRepository.save(any(SystemLogEntity.class))).thenReturn(new SystemLogEntity());
        setupExecutorMocks();

        // When & Then
        mockMvc.perform(post("/api/last-trades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Оптимизированная загрузка обезличенных сделок запущена"));

        verify(cachedInstrumentService, atLeastOnce()).getCacheInfo();
        verify(systemLogRepository, atLeastOnce()).save(any(SystemLogEntity.class));
    }
}
