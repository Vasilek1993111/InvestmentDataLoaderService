package com.example.InvestmentDataLoaderService.component;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.service.TradingService;
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

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Компонентные тесты для TradingController
 */
@SpringBootTest
@ActiveProfiles("test")
class TradingControllerComponentTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private TradingService tradingService;


    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void accountsEndpoint_ShouldWork_WithRealContext() throws Exception {
        // Given
        List<AccountDto> accounts = Arrays.asList(
                new AccountDto("acc1", "Account 1", "BROKER")
        );
        when(tradingService.getAccounts()).thenReturn(accounts);

        // When & Then
        mockMvc.perform(get("/api/trading/accounts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("acc1"));
    }

    @Test
    void schedulesEndpoints_ShouldWork_WithRealContext() throws Exception {
        // Given
        TradingDayDto day = new TradingDayDto("2025-01-01", true, "09:00", "19:00");
        List<TradingScheduleDto> schedules = Arrays.asList(new TradingScheduleDto("MOEX", Arrays.asList(day)));
        when(tradingService.getTradingSchedules(eq("MOEX"), any(Instant.class), any(Instant.class)))
                .thenReturn(schedules);

        // When & Then
        mockMvc.perform(get("/api/trading/schedules")
                        .param("exchange", "MOEX")
                        .param("from", "2025-01-01T00:00:00Z")
                        .param("to", "2025-01-02T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].exchange").value("MOEX"));

        mockMvc.perform(get("/api/trading/schedules/period")
                        .param("exchange", "MOEX")
                        .param("from", "2025-01-01T00:00:00Z")
                        .param("to", "2025-01-02T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void statusesEndpoints_ShouldWork_WithRealContext() throws Exception {
        // Given
        List<TradingStatusDto> statuses = Arrays.asList(
                new TradingStatusDto("BBG004730N88", "SECURITY_TRADING_STATUS_NORMAL_TRADING")
        );
        when(tradingService.getTradingStatuses(anyList())).thenReturn(statuses);

        // When & Then
        mockMvc.perform(get("/api/trading/statuses")
                        .param("instrumentId", "BBG004730N88"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].figi").value("BBG004730N88"));

        mockMvc.perform(get("/api/trading/statuses/detailed")
                        .param("instrumentId", "BBG004730N88"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1));
    }
}


