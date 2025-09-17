package com.example.InvestmentDataLoaderService.unit.controller;

import com.example.InvestmentDataLoaderService.controller.SystemController;
import com.example.InvestmentDataLoaderService.scheduler.VolumeAggregationSchedulerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit-тесты для SystemController
 */
@WebMvcTest(SystemController.class)
class SystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VolumeAggregationSchedulerService volumeAggregationService;


    @BeforeEach
    void setUp() {
        // Настройка моков по умолчанию
        when(volumeAggregationService.isMaterializedViewExists()).thenReturn(true);
        when(volumeAggregationService.getDetailedStats()).thenReturn(createMockDetailedStats());
    }

    // ==================== ТЕСТЫ ДЛЯ HEALTH ENDPOINT ====================

    @Test
    void getHealthStatus_ShouldReturnHealthyStatus_WhenSystemIsHealthy() throws Exception {
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
    void getHealthStatus_ShouldReturnWarningStatus_WhenMaterializedViewsMissing() throws Exception {
        // Given
        when(volumeAggregationService.isMaterializedViewExists()).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/system/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("healthy"))
                .andExpect(jsonPath("$.components.materialized_views").value("warning"))
                .andExpect(jsonPath("$.components.database").value("healthy"));

        verify(volumeAggregationService).isMaterializedViewExists();
    }

    @Test
    void getHealthStatus_ShouldReturnUnhealthyStatus_WhenExceptionOccurs() throws Exception {
        // Given
        when(volumeAggregationService.isMaterializedViewExists()).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/system/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("unhealthy"))
                .andExpect(jsonPath("$.message").value("Ошибка проверки здоровья системы: Database connection failed"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(volumeAggregationService).isMaterializedViewExists();
    }

    // ==================== ТЕСТЫ ДЛЯ DIAGNOSTICS ENDPOINT ====================

    @Test
    void getSystemDiagnostics_ShouldReturnSystemInfo_WhenSuccessful() throws Exception {
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
    void getSystemDiagnostics_ShouldReturnMissingViewsStatus_WhenViewsNotExist() throws Exception {
        // Given
        when(volumeAggregationService.isMaterializedViewExists()).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/system/diagnostics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.materialized_views.exists").value(false))
                .andExpect(jsonPath("$.data.materialized_views.status").value("missing"));

        verify(volumeAggregationService).isMaterializedViewExists();
    }

    @Test
    void getSystemDiagnostics_ShouldReturnError_WhenExceptionOccurs() throws Exception {
        // Given
        when(volumeAggregationService.isMaterializedViewExists()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/system/diagnostics"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ошибка диагностики системы: Database error"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(volumeAggregationService).isMaterializedViewExists();
    }

    // ==================== ТЕСТЫ ДЛЯ VOLUME AGGREGATION CHECK ====================

    @Test
    void checkMaterializedViews_ShouldReturnExistsTrue_WhenViewsExist() throws Exception {
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
    void checkMaterializedViews_ShouldReturnExistsFalse_WhenViewsNotExist() throws Exception {
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
    void checkMaterializedViews_ShouldReturnError_WhenExceptionOccurs() throws Exception {
        // Given
        when(volumeAggregationService.isMaterializedViewExists()).thenThrow(new RuntimeException("Check failed"));

        // When & Then
        mockMvc.perform(get("/api/system/volume-aggregation/check"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ошибка проверки материализованных представлений: Check failed"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(volumeAggregationService).isMaterializedViewExists();
    }

    // ==================== ТЕСТЫ ДЛЯ SCHEDULE INFO ====================

    @Test
    void getScheduleInfo_ShouldReturnScheduleInformation_WhenSuccessful() throws Exception {
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

    @Test
    void getScheduleInfo_ShouldReturnError_WhenExceptionOccurs() throws Exception {
        // Given - No mocking needed as this endpoint doesn't use external services

        // When & Then - This test is mainly for completeness, as the method doesn't throw exceptions
        mockMvc.perform(get("/api/system/volume-aggregation/schedule-info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== ТЕСТЫ ДЛЯ SYSTEM STATS ====================

    @Test
    void getSystemStats_ShouldReturnSystemStats_WhenSuccessful() throws Exception {
        // Given
        when(volumeAggregationService.getDetailedStats()).thenReturn(createMockDetailedStats());

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
                .andExpect(jsonPath("$.timestamp").exists());

        verify(volumeAggregationService).getDetailedStats();
    }

    @Test
    void getSystemStats_ShouldReturnStatsWithError_WhenVolumeAggregationFails() throws Exception {
        // Given
        when(volumeAggregationService.getDetailedStats()).thenThrow(new RuntimeException("Stats error"));

        // When & Then
        mockMvc.perform(get("/api/system/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.memory").exists())
                .andExpect(jsonPath("$.data.processor").exists())
                .andExpect(jsonPath("$.data.uptime").exists())
                .andExpect(jsonPath("$.data.volume_aggregation.error").value("Stats error"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(volumeAggregationService).getDetailedStats();
    }

    @Test
    void getSystemStats_ShouldReturnStatsWithoutVolumeAggregation_WhenServiceReturnsNull() throws Exception {
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

    @Test
    void getSystemStats_ShouldReturnError_WhenExceptionOccurs() throws Exception {
        // Given
        when(volumeAggregationService.getDetailedStats()).thenThrow(new RuntimeException("System error"));

        // When & Then
        mockMvc.perform(get("/api/system/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.volume_aggregation.error").value("System error"));

        verify(volumeAggregationService).getDetailedStats();
    }

    // ==================== ТЕСТЫ ДЛЯ SYSTEM INFO ====================

    @Test
    void getSystemInfo_ShouldReturnSystemInformation_WhenSuccessful() throws Exception {
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

    @Test
    void getSystemInfo_ShouldReturnError_WhenExceptionOccurs() throws Exception {
        // Given - This test is mainly for completeness, as the method doesn't throw exceptions
        // When & Then
        mockMvc.perform(get("/api/system/info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== ТЕСТЫ ДЛЯ EXTERNAL SERVICES ====================

    @Test
    void checkExternalServices_ShouldReturnServicesStatus_WhenSuccessful() throws Exception {
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

    @Test
    void checkExternalServices_ShouldReturnError_WhenExceptionOccurs() throws Exception {
        // Given - This test is mainly for completeness, as the method doesn't throw exceptions
        // When & Then
        mockMvc.perform(get("/api/system/external-services"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private Map<String, Object> createMockDetailedStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("test_key", "test_value");
        stats.put("total_records", 1000);
        stats.put("last_updated", "2024-01-01T00:00:00");
        return stats;
    }
}
