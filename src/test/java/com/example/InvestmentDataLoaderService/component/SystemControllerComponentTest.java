package com.example.InvestmentDataLoaderService.component;

import com.example.InvestmentDataLoaderService.scheduler.VolumeAggregationSchedulerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Компонентные тесты для SystemController
 * Тестирует интеграцию с реальным Spring контекстом
 */
@SpringBootTest
@ActiveProfiles("test")
class SystemControllerComponentTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private VolumeAggregationSchedulerService volumeAggregationService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Настройка моков по умолчанию
        when(volumeAggregationService.isMaterializedViewExists()).thenReturn(true);
        when(volumeAggregationService.getDetailedStats()).thenReturn(createMockDetailedStats());
    }

    // ==================== КОМПОНЕНТНЫЕ ТЕСТЫ ДЛЯ HEALTH ====================

    @Test
    void healthCheck_ShouldReturnHealthyStatus_WithRealSpringContext() throws Exception {
        // Given
        when(volumeAggregationService.isMaterializedViewExists()).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/system/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("healthy"))
                .andExpect(jsonPath("$.components.database").value("healthy"))
                .andExpect(jsonPath("$.components.materialized_views").value("healthy"))
                .andExpect(jsonPath("$.components.schedulers").value("healthy"))
                .andExpect(jsonPath("$.components.api").value("healthy"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.uptime").exists());

        verify(volumeAggregationService).isMaterializedViewExists();
    }

    @Test
    void healthCheck_ShouldHandleServiceFailure_WithRealSpringContext() throws Exception {
        // Given
        when(volumeAggregationService.isMaterializedViewExists())
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/system/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("unhealthy"))
                .andExpect(jsonPath("$.message").value("Ошибка проверки здоровья системы: Database connection failed"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(volumeAggregationService).isMaterializedViewExists();
    }

    // ==================== КОМПОНЕНТНЫЕ ТЕСТЫ ДЛЯ DIAGNOSTICS ====================

    @Test
    void diagnostics_ShouldReturnCompleteSystemInfo_WithRealSpringContext() throws Exception {
        // Given
        when(volumeAggregationService.isMaterializedViewExists()).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/system/diagnostics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.materialized_views.exists").value(true))
                .andExpect(jsonPath("$.data.materialized_views.status").value("ok"))
                .andExpect(jsonPath("$.data.system.java_version").exists())
                .andExpect(jsonPath("$.data.system.os_name").exists())
                .andExpect(jsonPath("$.data.system.os_version").exists())
                .andExpect(jsonPath("$.data.system.available_processors").exists())
                .andExpect(jsonPath("$.data.system.max_memory").exists())
                .andExpect(jsonPath("$.data.system.total_memory").exists())
                .andExpect(jsonPath("$.data.system.free_memory").exists())
                .andExpect(jsonPath("$.data.schedules.daily_refresh").value("0 * * * * * (каждую минуту)"))
                .andExpect(jsonPath("$.data.schedules.full_refresh").value("0 20 2 * * * (каждый день в 2:20)"))
                .andExpect(jsonPath("$.data.schedules.timezone").value("Europe/Moscow"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(volumeAggregationService).isMaterializedViewExists();
    }

    @Test
    void diagnostics_ShouldHandleServiceException_WithRealSpringContext() throws Exception {
        // Given
        when(volumeAggregationService.isMaterializedViewExists())
                .thenThrow(new RuntimeException("Database connection error"));

        // When & Then
        mockMvc.perform(get("/api/system/diagnostics"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ошибка диагностики системы: Database connection error"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(volumeAggregationService).isMaterializedViewExists();
    }

    // ==================== КОМПОНЕНТНЫЕ ТЕСТЫ ДЛЯ VOLUME AGGREGATION ====================

    @Test
    void checkMaterializedViews_ShouldReturnCorrectStatus_WithRealSpringContext() throws Exception {
        // Given
        when(volumeAggregationService.isMaterializedViewExists()).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/system/volume-aggregation/check"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.exists").value(true))
                .andExpect(jsonPath("$.message").value("Материализованные представления существуют"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(volumeAggregationService).isMaterializedViewExists();
    }

    @Test
    void checkMaterializedViews_ShouldReturnMissingStatus_WithRealSpringContext() throws Exception {
        // Given
        when(volumeAggregationService.isMaterializedViewExists()).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/system/volume-aggregation/check"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.exists").value(false))
                .andExpect(jsonPath("$.message").value("Материализованные представления не найдены"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(volumeAggregationService).isMaterializedViewExists();
    }

    @Test
    void getScheduleInfo_ShouldReturnScheduleData_WithRealSpringContext() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/system/volume-aggregation/schedule-info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.daily_refresh").value("0 * * * * * (каждую минуту)"))
                .andExpect(jsonPath("$.data.full_refresh").value("0 20 2 * * * (каждый день в 2:20)"))
                .andExpect(jsonPath("$.data.timezone").value("Europe/Moscow"))
                .andExpect(jsonPath("$.data.description").value("Дневное представление обновляется каждую минуту, общее - в 2:20"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ==================== КОМПОНЕНТНЫЕ ТЕСТЫ ДЛЯ SYSTEM STATS ====================

    @Test
    void getSystemStats_ShouldReturnCompleteStats_WithRealSpringContext() throws Exception {
        // Given
        Map<String, Object> mockStats = createMockDetailedStats();
        when(volumeAggregationService.getDetailedStats()).thenReturn(mockStats);

        // When & Then
        mockMvc.perform(get("/api/system/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.memory.max_memory_mb").exists())
                .andExpect(jsonPath("$.data.memory.total_memory_mb").exists())
                .andExpect(jsonPath("$.data.memory.free_memory_mb").exists())
                .andExpect(jsonPath("$.data.memory.used_memory_mb").exists())
                .andExpect(jsonPath("$.data.processor.available_processors").exists())
                .andExpect(jsonPath("$.data.uptime.start_time").exists())
                .andExpect(jsonPath("$.data.uptime.uptime_ms").exists())
                .andExpect(jsonPath("$.data.volume_aggregation.test_key").value("test_value"))
                .andExpect(jsonPath("$.data.volume_aggregation.total_records").value(1000))
                .andExpect(jsonPath("$.data.volume_aggregation.last_updated").value("2024-01-01T00:00:00"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(volumeAggregationService).getDetailedStats();
    }

    @Test
    void getSystemStats_ShouldHandleServiceError_WithRealSpringContext() throws Exception {
        // Given
        when(volumeAggregationService.getDetailedStats())
                .thenThrow(new RuntimeException("Stats service unavailable"));

        // When & Then
        mockMvc.perform(get("/api/system/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.memory").exists())
                .andExpect(jsonPath("$.data.processor").exists())
                .andExpect(jsonPath("$.data.uptime").exists())
                .andExpect(jsonPath("$.data.volume_aggregation.error").value("Stats service unavailable"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(volumeAggregationService).getDetailedStats();
    }

    @Test
    void getSystemStats_ShouldHandleNullResponse_WithRealSpringContext() throws Exception {
        // Given
        when(volumeAggregationService.getDetailedStats()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/system/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.memory").exists())
                .andExpect(jsonPath("$.data.processor").exists())
                .andExpect(jsonPath("$.data.uptime").exists())
                .andExpect(jsonPath("$.data.volume_aggregation").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(volumeAggregationService).getDetailedStats();
    }

    // ==================== КОМПОНЕНТНЫЕ ТЕСТЫ ДЛЯ SYSTEM INFO ====================

    @Test
    void getSystemInfo_ShouldReturnSystemInformation_WithRealSpringContext() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/system/info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.java.version").exists())
                .andExpect(jsonPath("$.data.java.vendor").exists())
                .andExpect(jsonPath("$.data.java.home").exists())
                .andExpect(jsonPath("$.data.os.name").exists())
                .andExpect(jsonPath("$.data.os.version").exists())
                .andExpect(jsonPath("$.data.os.arch").exists())
                .andExpect(jsonPath("$.data.user.name").exists())
                .andExpect(jsonPath("$.data.user.home").exists())
                .andExpect(jsonPath("$.data.user.dir").exists())
                .andExpect(jsonPath("$.data.application.name").value("Investment Data Loader Service"))
                .andExpect(jsonPath("$.data.application.version").value("1.0.0"))
                .andExpect(jsonPath("$.data.application.description").value("Сервис загрузки инвестиционных данных"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ==================== КОМПОНЕНТНЫЕ ТЕСТЫ ДЛЯ EXTERNAL SERVICES ====================

    @Test
    void checkExternalServices_ShouldReturnServicesStatus_WithRealSpringContext() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/system/external-services"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tinkoff_api.status").value("unknown"))
                .andExpect(jsonPath("$.data.tinkoff_api.message").value("Проверка не реализована"))
                .andExpect(jsonPath("$.data.database.status").value("healthy"))
                .andExpect(jsonPath("$.data.database.message").value("Подключение к базе данных работает"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ==================== ИНТЕГРАЦИОННЫЕ ТЕСТЫ ====================

    @Test
    void allEndpoints_ShouldBeAccessible_WithRealSpringContext() throws Exception {
        // Test all endpoints are accessible and return proper JSON structure
        String[] endpoints = {
            "/api/system/health",
            "/api/system/diagnostics", 
            "/api/system/volume-aggregation/check",
            "/api/system/volume-aggregation/schedule-info",
            "/api/system/stats",
            "/api/system/info",
            "/api/system/external-services"
        };

        for (String endpoint : endpoints) {
            mockMvc.perform(get(endpoint))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Test
    void errorHandling_ShouldBeConsistent_AcrossAllEndpoints() throws Exception {
        // Given - Force an exception in the service
        when(volumeAggregationService.isMaterializedViewExists())
                .thenThrow(new RuntimeException("Test exception"));

        // Test that error handling is consistent across endpoints that use the service
        // Health endpoint returns 503 on error
        mockMvc.perform(get("/api/system/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists());

        // Diagnostics endpoint returns 500 on error
        mockMvc.perform(get("/api/system/diagnostics"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists());

        // Volume aggregation check returns 500 on error
        mockMvc.perform(get("/api/system/volume-aggregation/check"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private Map<String, Object> createMockDetailedStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("test_key", "test_value");
        stats.put("total_records", 1000);
        stats.put("last_updated", "2024-01-01T00:00:00");
        stats.put("memory_usage_mb", 512);
        stats.put("cpu_usage_percent", 25.5);
        return stats;
    }
}
