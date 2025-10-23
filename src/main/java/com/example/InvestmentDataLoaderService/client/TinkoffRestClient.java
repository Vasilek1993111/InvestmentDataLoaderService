package com.example.InvestmentDataLoaderService.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(TinkoffRestClient.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiToken;
    private final String baseUrl;

    public TinkoffRestClient(@Value("${tinkoff.api.token:}") String apiToken) {
        log.info("=== ИНИЦИАЛИЗАЦИЯ TINKOFF REST CLIENT ===");
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        // Приоритет: переменная окружения > значение из application.properties
        this.apiToken = System.getenv("T_INVEST_TOKEN") != null ? System.getenv("T_INVEST_TOKEN") : apiToken;
        this.baseUrl = "https://invest-public-api.tinkoff.ru/rest";
        
        log.info("TinkoffRestClient инициализирован");
        log.info("Base URL: {}", this.baseUrl);
        log.info("API Token настроен: {}", this.apiToken != null && !this.apiToken.trim().isEmpty() ? "Да" : "Нет");
    }

    /**
     * Получение индикативных инструментов через REST API
     * Согласно документации: https://developer.tbank.ru/invest/services/instruments/methods
     */
    public JsonNode getIndicatives() {
        log.info("=== GETTING INDICATIVE INSTRUMENTS ===");
        try {
            log.info("Checking API token configuration...");
            // Проверяем, что токен не пустой
            if (apiToken == null || apiToken.trim().isEmpty()) {
                log.error("T-Invest API token не настроен");
                throw new RuntimeException("T-Invest API token is not configured. Please set T_INVEST_TOKEN environment variable.");
            }
            
            String url = baseUrl + "/tinkoff.public.invest.api.contract.v1.InstrumentsService/Indicatives";
            log.info("Sending request to Tinkoff API: {}", url);
            
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
                log.info("Successfully received indicative instruments");
                return objectMapper.readTree(response.getBody());
            } else {
                log.error("API запрос завершился с ошибкой: {}", response.getStatusCode());
                throw new RuntimeException("API request failed with status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Ошибка при получении индикативных инструментов: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get indicatives from Tinkoff API", e);
        }
    }

    /**
     * Получение акций через REST API
     */
    public JsonNode getShares(String status) {
        log.info("=== ПОЛУЧЕНИЕ АКЦИЙ ===");
        try {
            log.info("Проверяем конфигурацию API токена...");
            if (apiToken == null || apiToken.trim().isEmpty()) {
                log.error("T-Invest API token не настроен");
                throw new RuntimeException("T-Invest API token is not configured. Please set T_INVEST_TOKEN environment variable.");
            }
            
            String url = baseUrl + "/tinkoff.public.invest.api.contract.v1.InstrumentsService/Shares";
            log.info("Sending request to Tinkoff API: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiToken);
            
            Map<String, Object> requestBody = new HashMap<>();
            
            // Передаем статус напрямую от пользователя
            String instrumentStatus = (status != null && !status.isEmpty()) ? status : "INSTRUMENT_STATUS_BASE";
            requestBody.put("instrumentStatus", instrumentStatus);
            requestBody.put("instrumentExchange","INSTRUMENT_EXCHANGE_UNSPECIFIED");
            
            log.debug("Тело запроса: {}", requestBody);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Успешно получены акции");
                return objectMapper.readTree(response.getBody());
            } else {
                log.error("API запрос завершился с ошибкой: {}", response.getStatusCode());
                throw new RuntimeException("API request failed with status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Ошибка при получении акций: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get shares from Tinkoff API", e);
        }
    }

    /**
     * Получение фьючерсов через REST API
     */
    public JsonNode getFutures() {
        log.info("=== ПОЛУЧЕНИЕ ФЬЮЧЕРСОВ ===");
        try {
            log.info("Проверяем конфигурацию API токена...");
            if (apiToken == null || apiToken.trim().isEmpty()) {
                log.error("T-Invest API token не настроен");
                throw new RuntimeException("T-Invest API token is not configured. Please set T_INVEST_TOKEN environment variable.");
            }
            
            String url = baseUrl + "/tinkoff.public.invest.api.contract.v1.InstrumentsService/Futures";
            log.info("Sending request to Tinkoff API: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiToken);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("instrumentStatus", "INSTRUMENT_STATUS_BASE");
            
            log.debug("Тело запроса: {}", requestBody);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Успешно получены фьючерсы");
                return objectMapper.readTree(response.getBody());
            } else {
                log.error("API запрос завершился с ошибкой: {}", response.getStatusCode());
                throw new RuntimeException("API request failed with status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Ошибка при получении фьючерсов: {}", e.getMessage(), e);
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
    
    public JsonNode getShareByFigi(String figi){
        try {
        String url = baseUrl + "/tinkoff.public.invest.api.contract.v1.InstrumentsService/ShareBy";
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

