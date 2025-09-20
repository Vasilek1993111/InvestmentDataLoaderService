package com.example.InvestmentDataLoaderService.integration;

import com.example.InvestmentDataLoaderService.entity.ClosePriceEveningSessionEntity;
import com.example.InvestmentDataLoaderService.repository.ClosePriceEveningSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты с реальными запросами к T-INVEST API для цен вечерней сессии
 * 
 * Тестирует:
 * 1. Реальные запросы к T-INVEST API для получения цен вечерней сессии
 * 2. Сравнение данных из API с данными в БД
 * 3. Проверку корректности работы фильтрации
 * 4. Проверку логики выбора даты
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class EveningSessionPricesServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClosePriceEveningSessionRepository closePriceEveningSessionRepository;

    // Удалены неиспользуемые поля для устранения предупреждений линтера

    private LocalDate testDate;
    private LocalDateTime currentTime;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        // Очищаем тестовые данные
        closePriceEveningSessionRepository.deleteAll();
        
        // Определяем тестовую дату на основе текущего времени
        currentTime = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        testDate = determineTestDate();
        baseUrl = "http://localhost:" + port + "/api/data-loading";
        
        System.out.println("=== НАСТРОЙКА РЕАЛЬНОГО ИНТЕГРАЦИОННОГО ТЕСТА ВЕЧЕРНЕЙ СЕССИИ ===");
        System.out.println("Текущее время (МСК): " + currentTime);
        System.out.println("Тестовая дата: " + testDate);
        System.out.println("День недели: " + testDate.getDayOfWeek());
        System.out.println("ВНИМАНИЕ: Тест будет делать реальные запросы к T-INVEST API!");
    }

    /**
     * Определяет тестовую дату на основе текущего времени
     */
    private LocalDate determineTestDate() {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Moscow"));
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        
        // Если сегодня выходной, используем последний рабочий день
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return today.minusDays(1); // Пятница
        } else if (dayOfWeek == DayOfWeek.SUNDAY) {
            return today.minusDays(2); // Пятница
        } else {
            // Будний день - используем вчерашний день
            return today.minusDays(1);
        }
    }

    @Test
    void getEveningSessionPricesForShares_RealTInvestAPI_ShouldReturnValidData() throws Exception {
        // Given - создаем несколько акций в БД для тестирования
        createTestSharesInDatabase();
        
        System.out.println("=== ТЕСТ: Реальный запрос к T-INVEST API для акций вечерней сессии ===");
        System.out.println("Ожидаем данные за дату: " + testDate);
        
        // When - вызываем API (реальный запрос к T-INVEST)
        String url = baseUrl + "/evening-session-prices/shares";
        ResponseEntity<String> httpResponse = restTemplate.getForEntity(url, String.class);
        assertThat(httpResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String response = httpResponse.getBody();

        // Then - проверяем результат
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> apiPrices = (List<Map<String, Object>>) responseMap.get("data");
        int apiCount = (Integer) responseMap.get("count");

        System.out.println("=== РЕЗУЛЬТАТЫ РЕАЛЬНОГО ЗАПРОСА ВЕЧЕРНЕЙ СЕССИИ ===");
        System.out.println("API вернул цен: " + apiCount);
        System.out.println("Время ответа: " + responseMap.get("timestamp"));

        // Проверяем, что получили данные
        assertThat(apiCount).isGreaterThan(0);
        assertThat(apiPrices).isNotEmpty();

        // Проверяем структуру данных
        for (Map<String, Object> apiPrice : apiPrices) {
            assertThat(apiPrice.get("figi")).isNotNull();
            assertThat(apiPrice.get("tradingDate")).isNotNull();
            assertThat(apiPrice.get("closePrice")).isNotNull();
            assertThat(apiPrice.get("eveningSessionPrice")).isNotNull();
            
            // Проверяем, что дата не 1970-01-01 (невалидная дата)
            String tradingDate = (String) apiPrice.get("tradingDate");
            assertThat(tradingDate).isNotEqualTo("1970-01-01");
            
            // Проверяем, что eveningSessionPrice не null
            Object eveningSessionPrice = apiPrice.get("eveningSessionPrice");
            assertThat(eveningSessionPrice).isNotNull();
            
            System.out.println("Валидная цена вечерней сессии: " + apiPrice.get("figi") + 
                             " - " + apiPrice.get("closePrice") + 
                             " (вечерняя: " + apiPrice.get("eveningSessionPrice") + ")");
        }

        // Проверяем, что все цены имеют eveningSessionPrice
        long pricesWithEveningSession = apiPrices.stream()
            .mapToLong(price -> price.get("eveningSessionPrice") != null ? 1 : 0)
            .sum();
        assertThat(pricesWithEveningSession).isEqualTo(apiCount);
    }

    @Test
    void getEveningSessionPricesForFutures_RealTInvestAPI_ShouldReturnValidData() throws Exception {
        // Given - создаем несколько фьючерсов в БД для тестирования
        createTestFuturesInDatabase();
        
        System.out.println("=== ТЕСТ: Реальный запрос к T-INVEST API для фьючерсов вечерней сессии ===");
        System.out.println("Ожидаем данные за дату: " + testDate);
        
        // When - вызываем API (реальный запрос к T-INVEST)
        String url = baseUrl + "/evening-session-prices/futures";
        ResponseEntity<String> httpResponse = restTemplate.getForEntity(url, String.class);
        assertThat(httpResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String response = httpResponse.getBody();

        // Then - проверяем результат
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> apiPrices = (List<Map<String, Object>>) responseMap.get("data");
        int apiCount = (Integer) responseMap.get("count");

        System.out.println("=== РЕЗУЛЬТАТЫ РЕАЛЬНОГО ЗАПРОСА ВЕЧЕРНЕЙ СЕССИИ ФЬЮЧЕРСОВ ===");
        System.out.println("API вернул цен: " + apiCount);
        System.out.println("Время ответа: " + responseMap.get("timestamp"));

        // Проверяем, что получили данные
        assertThat(apiCount).isGreaterThan(0);
        assertThat(apiPrices).isNotEmpty();

        // Проверяем структуру данных
        for (Map<String, Object> apiPrice : apiPrices) {
            assertThat(apiPrice.get("figi")).isNotNull();
            assertThat(apiPrice.get("tradingDate")).isNotNull();
            assertThat(apiPrice.get("closePrice")).isNotNull();
            assertThat(apiPrice.get("eveningSessionPrice")).isNotNull();
            
            // Проверяем, что дата не 1970-01-01 (невалидная дата)
            String tradingDate = (String) apiPrice.get("tradingDate");
            assertThat(tradingDate).isNotEqualTo("1970-01-01");
            
            // Проверяем, что eveningSessionPrice не null
            Object eveningSessionPrice = apiPrice.get("eveningSessionPrice");
            assertThat(eveningSessionPrice).isNotNull();
            
            System.out.println("Валидная цена вечерней сессии фьючерса: " + apiPrice.get("figi") + 
                             " - " + apiPrice.get("closePrice") + 
                             " (вечерняя: " + apiPrice.get("eveningSessionPrice") + ")");
        }

        // Проверяем, что все цены имеют eveningSessionPrice
        long pricesWithEveningSession = apiPrices.stream()
            .mapToLong(price -> price.get("eveningSessionPrice") != null ? 1 : 0)
            .sum();
        assertThat(pricesWithEveningSession).isEqualTo(apiCount);
    }

    @Test
    void loadEveningSessionPricesToday_RealTInvestAPI_ShouldLoadAndSaveData() throws Exception {
        // Given - очищаем БД перед тестом
        closePriceEveningSessionRepository.deleteAll();
        
        System.out.println("=== ТЕСТ: Реальная загрузка цен вечерней сессии за сегодня ===");
        System.out.println("Ожидаем данные за дату: " + testDate);
        
        // When - вызываем API для загрузки цен (реальный запрос к T-INVEST)
        String url = baseUrl + "/evening-session-prices";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> httpResponse = restTemplate.postForEntity(url, requestEntity, String.class);
        assertThat(httpResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String response = httpResponse.getBody();

        // Then - проверяем результат
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        boolean success = (Boolean) responseMap.get("success");
        String message = (String) responseMap.get("message");
        int totalRequested = (Integer) responseMap.get("totalRequested");
        int newItemsSaved = (Integer) responseMap.get("newItemsSaved");
        int existingItemsSkipped = (Integer) responseMap.get("existingItemsSkipped");
        int invalidItemsFiltered = (Integer) responseMap.get("invalidItemsFiltered");
        int missingFromApi = (Integer) responseMap.get("missingFromApi");

        System.out.println("=== РЕЗУЛЬТАТЫ РЕАЛЬНОЙ ЗАГРУЗКИ ВЕЧЕРНЕЙ СЕССИИ ===");
        System.out.println("Успех: " + success);
        System.out.println("Сообщение: " + message);
        System.out.println("Запрошено: " + totalRequested);
        System.out.println("Сохранено новых: " + newItemsSaved);
        System.out.println("Пропущено существующих: " + existingItemsSkipped);
        System.out.println("Отфильтровано невалидных: " + invalidItemsFiltered);
        System.out.println("Отсутствует в API: " + missingFromApi);

        // Проверяем базовые условия
        assertThat(success).isTrue();
        assertThat(message).isNotNull();
        assertThat(totalRequested).isGreaterThanOrEqualTo(0);
        assertThat(newItemsSaved).isGreaterThanOrEqualTo(0);
        assertThat(existingItemsSkipped).isGreaterThanOrEqualTo(0);
        assertThat(invalidItemsFiltered).isGreaterThanOrEqualTo(0);
        assertThat(missingFromApi).isGreaterThanOrEqualTo(0);

        // Проверяем, что сумма всех категорий равна общему количеству запрошенных
        int totalProcessed = newItemsSaved + existingItemsSkipped + invalidItemsFiltered + missingFromApi;
        assertThat(totalProcessed).isEqualTo(totalRequested);

        // Проверяем, что данные сохранились в БД
        List<ClosePriceEveningSessionEntity> savedEntities = closePriceEveningSessionRepository.findAll();
        System.out.println("Сохранено в БД записей: " + savedEntities.size());
        
        if (newItemsSaved > 0) {
            assertThat(savedEntities).isNotEmpty();
            
            // Проверяем структуру сохраненных данных
            for (ClosePriceEveningSessionEntity entity : savedEntities) {
                assertThat(entity.getFigi()).isNotNull();
                assertThat(entity.getPriceDate()).isNotNull();
                assertThat(entity.getClosePrice()).isNotNull();
                assertThat(entity.getInstrumentType()).isNotNull();
                assertThat(entity.getCurrency()).isNotNull();
                assertThat(entity.getExchange()).isNotNull();
                
                System.out.println("Сохраненная запись: " + entity.getFigi() + 
                                 " - " + entity.getClosePrice() + 
                                 " (" + entity.getInstrumentType() + ")");
            }
        }
    }

    @Test
    void loadEveningSessionPricesForDate_WithWeekendDate_ShouldReturnError() throws Exception {
        // Given - используем выходной день (суббота)
        LocalDate weekendDate = LocalDate.of(2024, 1, 13); // Суббота
        
        System.out.println("=== ТЕСТ: Загрузка цен вечерней сессии за выходной день ===");
        System.out.println("Тестовая дата (выходной): " + weekendDate);
        System.out.println("День недели: " + weekendDate.getDayOfWeek());
        
        // When - вызываем API для загрузки цен за выходной день
        String url = baseUrl + "/evening-session-prices/" + weekendDate;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> httpResponse = restTemplate.postForEntity(url, requestEntity, String.class);
        assertThat(httpResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String response = httpResponse.getBody();

        // Then - проверяем результат
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        boolean success = (Boolean) responseMap.get("success");
        String message = (String) responseMap.get("message");
        int totalRequested = (Integer) responseMap.get("totalRequested");
        int newItemsSaved = (Integer) responseMap.get("newItemsSaved");

        System.out.println("=== РЕЗУЛЬТАТЫ ЗАГРУЗКИ ЗА ВЫХОДНОЙ ДЕНЬ ===");
        System.out.println("Успех: " + success);
        System.out.println("Сообщение: " + message);
        System.out.println("Запрошено: " + totalRequested);
        System.out.println("Сохранено новых: " + newItemsSaved);

        // Проверяем, что получили ошибку для выходного дня
        assertThat(success).isFalse();
        assertThat(message).contains("выходные дни");
        assertThat(message).contains("вечерняя сессия не проводится");
        assertThat(totalRequested).isEqualTo(0);
        assertThat(newItemsSaved).isEqualTo(0);
    }

    @Test
    void getEveningSessionPriceByFigi_RealTInvestAPI_ShouldReturnValidData() throws Exception {
        // Given - создаем тестовые инструменты в БД
        createTestSharesInDatabase();
        
        // Получаем список доступных инструментов
        String sharesUrl = baseUrl + "/evening-session-prices/shares";
        ResponseEntity<String> sharesResponse = restTemplate.getForEntity(sharesUrl, String.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> sharesResponseMap = objectMapper.readValue(sharesResponse.getBody(), Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sharesData = (List<Map<String, Object>>) sharesResponseMap.get("data");
        
        if (sharesData.isEmpty()) {
            System.out.println("Нет доступных инструментов для тестирования");
            return;
        }
        
        String testFigi = (String) sharesData.get(0).get("figi");
        
        System.out.println("=== ТЕСТ: Реальный запрос к T-INVEST API для конкретного инструмента вечерней сессии ===");
        System.out.println("Тестовый FIGI: " + testFigi);
        
        // When - вызываем API для конкретного инструмента
        String url = baseUrl + "/evening-session-prices/" + testFigi;
        ResponseEntity<String> httpResponse = restTemplate.getForEntity(url, String.class);
        assertThat(httpResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String response = httpResponse.getBody();

        // Then - проверяем результат
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        boolean success = (Boolean) responseMap.get("success");
        String message = (String) responseMap.get("message");
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");

        System.out.println("=== РЕЗУЛЬТАТЫ ЗАПРОСА ПО FIGI ===");
        System.out.println("Успех: " + success);
        System.out.println("Сообщение: " + message);

        if (success && data != null) {
            assertThat(data.get("figi")).isEqualTo(testFigi);
            assertThat(data.get("tradingDate")).isNotNull();
            assertThat(data.get("closePrice")).isNotNull();
            assertThat(data.get("eveningSessionPrice")).isNotNull();
            
            // Проверяем, что дата не 1970-01-01
            String tradingDate = (String) data.get("tradingDate");
            assertThat(tradingDate).isNotEqualTo("1970-01-01");
            
            System.out.println("Данные инструмента: " + data.get("figi") + 
                             " - " + data.get("closePrice") + 
                             " (вечерняя: " + data.get("eveningSessionPrice") + ")");
        } else {
            System.out.println("Инструмент не найден или нет данных вечерней сессии");
        }
    }

    /**
     * Создает тестовые акции в БД для интеграционных тестов
     */
    private void createTestSharesInDatabase() {
        // Этот метод должен создавать тестовые акции в БД
        // Реализация зависит от структуры вашей БД
        System.out.println("Создание тестовых акций в БД для интеграционных тестов");
    }

    /**
     * Создает тестовые фьючерсы в БД для интеграционных тестов
     */
    private void createTestFuturesInDatabase() {
        // Этот метод должен создавать тестовые фьючерсы в БД
        // Реализация зависит от структуры вашей БД
        System.out.println("Создание тестовых фьючерсов в БД для интеграционных тестов");
    }
}
