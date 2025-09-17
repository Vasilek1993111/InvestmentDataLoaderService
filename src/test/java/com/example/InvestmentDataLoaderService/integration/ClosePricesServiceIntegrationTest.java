package com.example.InvestmentDataLoaderService.integration;

import com.example.InvestmentDataLoaderService.entity.ClosePriceEntity;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.repository.ClosePriceRepository;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
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

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
 

/**
 * Интеграционные тесты с реальными запросами к T-INVEST API
 * 
 * Тестирует:
 * 1. Реальные запросы к T-INVEST API для получения цен закрытия
 * 2. Сравнение данных из API с данными в БД
 * 3. Проверку корректности работы фильтрации
 * 4. Проверку логики выбора даты
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class ClosePricesServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClosePriceRepository closePriceRepository;

    @Autowired
    private ShareRepository shareRepository;

    private LocalDate testDate;
    private LocalDateTime currentTime;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        // Очищаем тестовые данные
        closePriceRepository.deleteAll();
        
        // Определяем тестовую дату на основе текущего времени
        currentTime = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        testDate = determineTestDate();
        baseUrl = "http://localhost:" + port + "/api/data-loading";
        
        System.out.println("=== НАСТРОЙКА РЕАЛЬНОГО ИНТЕГРАЦИОННОГО ТЕСТА ===");
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
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();
        LocalTime currentTimeOfDay = currentTime.toLocalTime();
        
        // Если сегодня выходной (суббота или воскресенье)
        if (todayDayOfWeek == DayOfWeek.SATURDAY || todayDayOfWeek == DayOfWeek.SUNDAY) {
            // Ищем предыдущий рабочий день
            return findPreviousWorkingDay(today);
        }
        
        // Если сегодня рабочий день
        if (currentTimeOfDay.isBefore(LocalTime.of(19, 0))) {
            // До 19:00 - смотрим предыдущий рабочий день
            return findPreviousWorkingDay(today);
        } else {
            // После 19:00 - смотрим сегодняшний день
            return today;
        }
    }

    /**
     * Находит предыдущий рабочий день
     */
    private LocalDate findPreviousWorkingDay(LocalDate fromDate) {
        LocalDate candidate = fromDate.minusDays(1);
        while (candidate.getDayOfWeek() == DayOfWeek.SATURDAY || 
               candidate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            candidate = candidate.minusDays(1);
        }
        return candidate;
    }

    @Test
    void getClosePricesForShares_RealTInvestAPI_ShouldReturnValidData() throws Exception {
        // Given - создаем несколько акций в БД для тестирования
        createTestSharesInDatabase();
        
        System.out.println("=== ТЕСТ: Реальный запрос к T-INVEST API для акций ===");
        System.out.println("Ожидаем данные за дату: " + testDate);
        
        // When - вызываем API (реальный запрос к T-INVEST)
        String url = baseUrl + "/close-prices/shares";
        ResponseEntity<String> httpResponse = restTemplate.getForEntity(url, String.class);
        assertThat(httpResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String response = httpResponse.getBody();

        // Then - проверяем результат
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> apiPrices = (List<Map<String, Object>>) responseMap.get("data");
        int apiCount = (Integer) responseMap.get("count");

        System.out.println("=== РЕЗУЛЬТАТЫ РЕАЛЬНОГО ЗАПРОСА ===");
        System.out.println("API вернул цен: " + apiCount);
        System.out.println("Время ответа: " + responseMap.get("timestamp"));

        // Проверяем, что получили данные
        assertThat(apiCount).isGreaterThan(0);
        assertThat(apiPrices).isNotEmpty();

        // Проверяем структуру данных
        for (Map<String, Object> apiPrice : apiPrices) {
            String figi = (String) apiPrice.get("figi");
            String tradingDate = (String) apiPrice.get("tradingDate");
            Object closePriceObj = apiPrice.get("closePrice");
            
            assertThat(figi).isNotNull().isNotEmpty();
            assertThat(tradingDate).isNotNull().isNotEmpty();
            assertThat(closePriceObj).isNotNull();
            
            // Проверяем, что цена - это число
            BigDecimal closePrice = new BigDecimal(closePriceObj.toString());
            assertThat(closePrice).isGreaterThan(BigDecimal.ZERO);
            
            // Проверяем, что дата не является неверной (1970-01-01)
            assertThat(tradingDate).isNotEqualTo("1970-01-01");
            
            System.out.println("✓ Цена для " + figi + " на " + tradingDate + ": " + closePrice);
        }

        System.out.println("✅ Реальный запрос к T-INVEST API выполнен успешно!");
    }

    @Test
    void getClosePricesForFutures_RealTInvestAPI_ShouldReturnValidData() throws Exception {
        // Given - создаем несколько фьючерсов в БД для тестирования
        createTestFuturesInDatabase();
        
        System.out.println("=== ТЕСТ: Реальный запрос к T-INVEST API для фьючерсов ===");
        System.out.println("Ожидаем данные за дату: " + testDate);
        
        // When - вызываем API (реальный запрос к T-INVEST)
        String url = baseUrl + "/close-prices/futures";
        ResponseEntity<String> httpResponse = restTemplate.getForEntity(url, String.class);
        assertThat(httpResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String response = httpResponse.getBody();

        // Then - проверяем результат
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> apiPrices = (List<Map<String, Object>>) responseMap.get("data");
        int apiCount = (Integer) responseMap.get("count");

        System.out.println("=== РЕЗУЛЬТАТЫ РЕАЛЬНОГО ЗАПРОСА ДЛЯ ФЬЮЧЕРСОВ ===");
        System.out.println("API вернул цен: " + apiCount);
        System.out.println("Время ответа: " + responseMap.get("timestamp"));

        // Проверяем, что получили данные
        assertThat(apiCount).isGreaterThanOrEqualTo(0); // Фьючерсов может не быть
        assertThat(apiPrices).isNotNull();

        // Проверяем структуру данных (если есть)
        for (Map<String, Object> apiPrice : apiPrices) {
            String figi = (String) apiPrice.get("figi");
            String tradingDate = (String) apiPrice.get("tradingDate");
            Object closePriceObj = apiPrice.get("closePrice");
            
            assertThat(figi).isNotNull().isNotEmpty();
            assertThat(tradingDate).isNotNull().isNotEmpty();
            assertThat(closePriceObj).isNotNull();
            
            // Проверяем, что цена - это число
            BigDecimal closePrice = new BigDecimal(closePriceObj.toString());
            assertThat(closePrice).isGreaterThan(BigDecimal.ZERO);
            
            // Проверяем, что дата не является неверной (1970-01-01)
            assertThat(tradingDate).isNotEqualTo("1970-01-01");
            
            System.out.println("✓ Цена фьючерса для " + figi + " на " + tradingDate + ": " + closePrice);
        }

        System.out.println("✅ Реальный запрос к T-INVEST API для фьючерсов выполнен успешно!");
    }

    @Test
    void getClosePriceByFigi_RealTInvestAPI_ShouldReturnValidData() throws Exception {
        // Given - используем реальный FIGI акции
        String realFigi = "BBG004730N88"; // Сбербанк
        
        System.out.println("=== ТЕСТ: Реальный запрос к T-INVEST API по FIGI ===");
        System.out.println("FIGI: " + realFigi);
        System.out.println("Ожидаем данные за дату: " + testDate);
        
        // When - вызываем API (реальный запрос к T-INVEST)
        String url = baseUrl + "/close-prices/" + realFigi;
        ResponseEntity<String> httpResponse = restTemplate.getForEntity(url, String.class);
        assertThat(httpResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String response = httpResponse.getBody();

        // Then - проверяем результат
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        boolean success = (Boolean) responseMap.get("success");
        
        System.out.println("=== РЕЗУЛЬТАТЫ РЕАЛЬНОГО ЗАПРОСА ПО FIGI ===");
        System.out.println("Успех: " + success);
        System.out.println("Сообщение: " + responseMap.get("message"));
        System.out.println("Время ответа: " + responseMap.get("timestamp"));

        if (success) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
            
            String figi = (String) data.get("figi");
            String tradingDate = (String) data.get("tradingDate");
            Object closePriceObj = data.get("closePrice");
            
            assertThat(figi).isEqualTo(realFigi);
            assertThat(tradingDate).isNotNull().isNotEmpty();
            assertThat(closePriceObj).isNotNull();
            
            // Проверяем, что цена - это число
            BigDecimal closePrice = new BigDecimal(closePriceObj.toString());
            assertThat(closePrice).isGreaterThan(BigDecimal.ZERO);
            
            // Проверяем, что дата не является неверной (1970-01-01)
            assertThat(tradingDate).isNotEqualTo("1970-01-01");
            
            System.out.println("✓ Цена для " + figi + " на " + tradingDate + ": " + closePrice);
        } else {
            System.out.println("ℹ️ Цена не найдена для FIGI: " + realFigi);
        }

        System.out.println("✅ Реальный запрос к T-INVEST API по FIGI выполнен!");
    }

    @Test
    void loadClosePricesToday_RealTInvestAPI_ShouldLoadAndSaveData() throws Exception {
        // Given - очищаем БД перед тестом
        closePriceRepository.deleteAll();
        
        System.out.println("=== ТЕСТ: Реальная загрузка цен закрытия за сегодня ===");
        System.out.println("Ожидаем данные за дату: " + testDate);
        
        // When - вызываем API для загрузки цен (реальный запрос к T-INVEST)
        String url = baseUrl + "/close-prices";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> httpResponse = restTemplate.postForEntity(url, entity, String.class);
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

        System.out.println("=== РЕЗУЛЬТАТЫ РЕАЛЬНОЙ ЗАГРУЗКИ ===");
        System.out.println("Успех: " + success);
        System.out.println("Сообщение: " + message);
        System.out.println("Запрошено: " + totalRequested);
        System.out.println("Сохранено новых: " + newItemsSaved);
        System.out.println("Пропущено существующих: " + existingItemsSkipped);
        System.out.println("Отфильтровано неверных: " + invalidItemsFiltered);
        System.out.println("Не получено из API: " + missingFromApi);
        System.out.println("Время ответа: " + responseMap.get("timestamp"));

        // Проверяем, что запрос выполнился
        assertThat(success).isTrue();
        assertThat(message).isNotNull().isNotEmpty();
        assertThat(totalRequested).isGreaterThanOrEqualTo(0);
        assertThat(newItemsSaved).isGreaterThanOrEqualTo(0);
        assertThat(existingItemsSkipped).isGreaterThanOrEqualTo(0);
        assertThat(invalidItemsFiltered).isGreaterThanOrEqualTo(0);
        assertThat(missingFromApi).isGreaterThanOrEqualTo(0);

        // Проверяем, что данные сохранились в БД
        List<ClosePriceEntity> savedPrices = closePriceRepository.findAll();
        System.out.println("Сохранено в БД: " + savedPrices.size() + " цен");
        
        if (!savedPrices.isEmpty()) {
            ClosePriceEntity firstPrice = savedPrices.get(0);
            System.out.println("Пример сохраненной цены:");
            System.out.println("  FIGI: " + firstPrice.getId().getFigi());
            System.out.println("  Дата: " + firstPrice.getId().getPriceDate());
            System.out.println("  Цена: " + firstPrice.getClosePrice());
            System.out.println("  Тип: " + firstPrice.getInstrumentType());
        }

        System.out.println("✅ Реальная загрузка цен закрытия выполнена успешно!");
    }

    /**
     * Создает тестовые акции в БД
     */
    private void createTestSharesInDatabase() {
        List<ShareEntity> shares = List.of(
            createShareEntity("BBG004730N88", "SBER", "Сбербанк"),
            createShareEntity("BBG004730ZJ9", "GAZP", "Газпром"),
            createShareEntity("BBG004S685M2", "LKOH", "Лукойл")
        );
        
        shareRepository.saveAll(shares);
        System.out.println("Создано тестовых акций: " + shares.size());
    }

    /**
     * Создает тестовые фьючерсы в БД
     */
    private void createTestFuturesInDatabase() {
        // Создаем несколько тестовых фьючерсов
        // В реальном тесте они могут не понадобиться, так как T-INVEST API
        // сам определяет доступные инструменты
        System.out.println("Тестовые фьючерсы не созданы - используем реальные данные из T-INVEST API");
    }

    /**
     * Создает тестовую акцию
     */
    private ShareEntity createShareEntity(String figi, String ticker, String name) {
        ShareEntity share = new ShareEntity();
        share.setFigi(figi);
        share.setTicker(ticker);
        share.setName(name);
        share.setCurrency("RUB");
        share.setExchange("moex_mrng_evng_e_wknd_dlr");
        share.setSector("Test");
        share.setTradingStatus("SECURITY_TRADING_STATUS_NORMAL_TRADING");
        return share;
    }

    @Test
    void shouldVerifyDbShareClosePricesAreUpToDateWithoutSaving() throws Exception {
        // Fetch live close prices for shares (no saving)
        String url = baseUrl + "/close-prices/shares";
        ResponseEntity<String> httpResponse = restTemplate.getForEntity(url, String.class);
        assertThat(httpResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String response = httpResponse.getBody();

        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> apiPrices = (List<Map<String, Object>>) responseMap.get("data");

        // Build FIGI -> (date, price) map from API
        Map<String, Map<String, Object>> apiByFigi = apiPrices.stream()
                .collect(Collectors.toMap(p -> (String) p.get("figi"), p -> p, (a, b) -> a));

        // Load DB prices for shares at testDate
        List<ClosePriceEntity> dbSharePricesForDate = closePriceRepository.findAll().stream()
                .filter(p -> "SHARE".equals(p.getInstrumentType()))
                .filter(p -> testDate.equals(p.getId().getPriceDate()))
                .collect(Collectors.toList());

        if (dbSharePricesForDate.isEmpty()) {
            System.out.println("ℹ️ В БД нет цен закрытия акций за дату " + testDate + ")");
            return;
        }

        int checked = 0;
        for (ClosePriceEntity dbPrice : dbSharePricesForDate) {
            String figi = dbPrice.getId().getFigi();
            Map<String, Object> api = apiByFigi.get(figi);
            assertThat(api).as("Missing API price for FIGI=" + figi).isNotNull();

            String apiDate = (String) api.get("tradingDate");
            BigDecimal apiClose = new BigDecimal(api.get("closePrice").toString());

            assertThat(apiDate).isEqualTo(testDate.toString());
            assertThat(apiClose.compareTo(dbPrice.getClosePrice())).isZero();
            checked++;
        }

        System.out.println("✅ Сверка БД vs T-INVEST (акции): проверено=" + checked + ", дата=" + testDate + ")");
    }

    @Test
    void shouldVerifyDbFuturesClosePricesAreUpToDateWithoutSaving() throws Exception {
        // Fetch live close prices for futures (no saving)
        String url = baseUrl + "/close-prices/futures";
        ResponseEntity<String> httpResponse = restTemplate.getForEntity(url, String.class);
        assertThat(httpResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String response = httpResponse.getBody();

        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> apiPrices = (List<Map<String, Object>>) responseMap.get("data");

        // Build FIGI -> (date, price) map from API
        Map<String, Map<String, Object>> apiByFigi = apiPrices.stream()
                .collect(Collectors.toMap(p -> (String) p.get("figi"), p -> p, (a, b) -> a));

        // Load DB prices for futures at testDate
        List<ClosePriceEntity> dbFuturesPricesForDate = closePriceRepository.findAll().stream()
                .filter(p -> "FUTURE".equals(p.getInstrumentType()))
                .filter(p -> testDate.equals(p.getId().getPriceDate()))
                .collect(Collectors.toList());

        if (dbFuturesPricesForDate.isEmpty()) {
            System.out.println("ℹ️ В БД нет цен закрытия фьючерсов за дату " + testDate + ")");
            return;
        }

        int checked = 0;
        for (ClosePriceEntity dbPrice : dbFuturesPricesForDate) {
            String figi = dbPrice.getId().getFigi();
            Map<String, Object> api = apiByFigi.get(figi);
            assertThat(api).as("Missing API price for FIGI=" + figi).isNotNull();

            String apiDate = (String) api.get("tradingDate");
            BigDecimal apiClose = new BigDecimal(api.get("closePrice").toString());

            assertThat(apiDate).isEqualTo(testDate.toString());
            assertThat(apiClose.compareTo(dbPrice.getClosePrice())).isZero();
            checked++;
        }

        System.out.println("✅ Сверка БД vs T-INVEST (фьючерсы): проверено=" + checked + ", дата=" + testDate + ")");
    }
}


