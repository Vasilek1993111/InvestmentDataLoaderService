package com.example.InvestmentDataLoaderService.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для TinkoffRestClient
 */
@ExtendWith(MockitoExtension.class)
class TinkoffRestClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TinkoffRestClient tinkoffRestClient;

    @Test
    void testGetIndicatives_Success() {
        // Arrange
        String mockResponse = """
            {
                "instruments": [
                    {
                        "figi": "BBG00QPYJ5X0",
                        "ticker": "IMOEX",
                        "name": "Индекс МосБиржи",
                        "currency": "RUB",
                        "exchange": "moex_mrng_evng_e_wknd_dlr",
                        "classCode": "SPBXM",
                        "uid": "e6123145-9665-43e0-8413-cd61d8e6e372",
                        "sellAvailableFlag": true,
                        "buyAvailableFlag": true
                    }
                ]
            }
            """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(responseEntity);

        // Устанавливаем токен через рефлексию
        ReflectionTestUtils.setField(tinkoffRestClient, "apiToken", "test-token");

        // Act
        JsonNode result = tinkoffRestClient.getIndicatives();

        // Assert
        assertNotNull(result);
        assertTrue(result.has("instruments"));
        assertTrue(result.get("instruments").isArray());
        assertEquals(1, result.get("instruments").size());

        JsonNode instrument = result.get("instruments").get(0);
        assertEquals("BBG00QPYJ5X0", instrument.get("figi").asText());
        assertEquals("IMOEX", instrument.get("ticker").asText());
        assertEquals("Индекс МосБиржи", instrument.get("name").asText());
    }

    @Test
    void testGetIndicativeBy_Success() {
        // Arrange
        String mockResponse = """
            {
                "instrument": {
                    "figi": "BBG00QPYJ5X0",
                    "ticker": "IMOEX",
                    "name": "Индекс МосБиржи",
                    "currency": "RUB",
                    "exchange": "moex_mrng_evng_e_wknd_dlr",
                    "classCode": "SPBXM",
                    "uid": "e6123145-9665-43e0-8413-cd61d8e6e372",
                    "sellAvailableFlag": true,
                    "buyAvailableFlag": true
                }
            }
            """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(responseEntity);

        ReflectionTestUtils.setField(tinkoffRestClient, "apiToken", "test-token");

        // Act
        JsonNode result = tinkoffRestClient.getIndicativeBy("BBG00QPYJ5X0");

        // Assert
        assertNotNull(result);
        assertTrue(result.has("instrument"));

        JsonNode instrument = result.get("instrument");
        assertEquals("BBG00QPYJ5X0", instrument.get("figi").asText());
        assertEquals("IMOEX", instrument.get("ticker").asText());
        assertEquals("Индекс МосБиржи", instrument.get("name").asText());
    }

    @Test
    void testGetIndicatives_ApiError() {
        // Arrange
        ResponseEntity<String> responseEntity = new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(responseEntity);

        ReflectionTestUtils.setField(tinkoffRestClient, "apiToken", "test-token");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> tinkoffRestClient.getIndicatives());
    }

    @Test
    void testGetIndicativeByTicker_Success() {
        // Arrange
        String mockResponse = """
            {
                "instruments": [
                    {
                        "figi": "BBG00QPYJ5X0",
                        "ticker": "IMOEX",
                        "name": "Индекс МосБиржи",
                        "currency": "RUB",
                        "exchange": "moex_mrng_evng_e_wknd_dlr",
                        "classCode": "SPBXM",
                        "uid": "e6123145-9665-43e0-8413-cd61d8e6e372",
                        "sellAvailableFlag": true,
                        "buyAvailableFlag": true
                    }
                ]
            }
            """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(responseEntity);

        ReflectionTestUtils.setField(tinkoffRestClient, "apiToken", "test-token");

        // Act
        JsonNode result = tinkoffRestClient.getIndicatives();

        // Assert
        assertNotNull(result);
        assertTrue(result.has("instruments"));
        assertTrue(result.get("instruments").isArray());
        assertEquals(1, result.get("instruments").size());

        JsonNode instrument = result.get("instruments").get(0);
        assertEquals("IMOEX", instrument.get("ticker").asText());
    }
}
