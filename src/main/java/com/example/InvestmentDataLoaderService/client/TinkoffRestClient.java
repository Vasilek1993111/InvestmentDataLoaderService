package com.example.InvestmentDataLoaderService.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * REST клиент для вызова Tinkoff Invest API
 * Используется когда gRPC методы недоступны
 */
@Service
public class TinkoffRestClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiToken;
    private final String baseUrl;

    public TinkoffRestClient(@Value("${tinkoff.api.token:}") String apiToken) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        // Приоритет: переменная окружения > значение из application.properties
        this.apiToken = System.getenv("T_INVEST_TOKEN") != null ? System.getenv("T_INVEST_TOKEN") : apiToken;
        this.baseUrl = "https://invest-public-api.tinkoff.ru/rest";
    }

    /**
     * Получение индикативных инструментов через REST API
     * Согласно документации: https://developer.tbank.ru/invest/services/instruments/methods
     */
    public JsonNode getIndicatives() {
        try {
            String url = baseUrl + "/tinkoff.public.invest.api.contract.v1.InstrumentsService/Indicatives";
            
            // Заголовки для авторизации
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiToken);
            
            // Тело запроса (пустое для метода Indicatives)
            Map<String, Object> requestBody = new HashMap<>();
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return objectMapper.readTree(response.getBody());
            } else {
                throw new RuntimeException("API request failed with status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get indicatives from Tinkoff API", e);
        }
    }

    /**
     * Получение акций через REST API
     */
    public JsonNode getShares() {
        try {
            String url = baseUrl + "/tinkoff.public.invest.api.contract.v1.InstrumentsService/Shares";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiToken);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("instrumentStatus", "INSTRUMENT_STATUS_BASE");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return objectMapper.readTree(response.getBody());
            } else {
                throw new RuntimeException("API request failed with status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get shares from Tinkoff API", e);
        }
    }

    /**
     * Получение фьючерсов через REST API
     */
    public JsonNode getFutures() {
        try {
            String url = baseUrl + "/tinkoff.public.invest.api.contract.v1.InstrumentsService/Futures";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiToken);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("instrumentStatus", "INSTRUMENT_STATUS_BASE");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return objectMapper.readTree(response.getBody());
            } else {
                throw new RuntimeException("API request failed with status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get futures from Tinkoff API", e);
        }
    }

    /**
     * Получение индикативного инструмента по FIGI через REST API
     * Аналог метода ShareBy для индикативных инструментов
     */
    public JsonNode getIndicativeBy(String figi) {
        try {
            String url = baseUrl + "/tinkoff.public.invest.api.contract.v1.InstrumentsService/GetInstrumentBy";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiToken);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("idType", "INSTRUMENT_ID_TYPE_FIGI");
            requestBody.put("id", figi);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return objectMapper.readTree(response.getBody());
            } else {
                throw new RuntimeException("API request failed with status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get indicative by FIGI from Tinkoff API", e);
        }
    }
}
