package com.example.InvestmentDataLoaderService.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
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
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
 

/**
 * Интеграционные тесты производительности реальных запросов к T-INVEST API
 * 
 * Тестирует:
 * 1. Время выполнения запросов к T-INVEST API
 * 2. Производительность загрузки цен закрытия
 * 3. Сравнение производительности разных endpoints
 * 4. Проверку стабильности работы с реальным API
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@Epic("Performance Testing")
@Feature("Close Prices Performance")
@DisplayName("Close Prices Performance Integration Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
class ClosePricesPerformanceServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private LocalDate testDate;
    private LocalDateTime currentTime;
    private String baseUrl;

    @BeforeEach
    @Step("Подготовка тестовых данных для тестов производительности")
    @DisplayName("Подготовка тестовых данных")
    @Description("Инициализация тестовых данных и определение тестовой даты для тестов производительности")
    @Tag("setup")
    void setUp() {
        // Определяем тестовую дату на основе текущего времени
        currentTime = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        testDate = determineTestDate();
        baseUrl = "http://localhost:" + port + "/api/data-loading";
        
        System.out.println("=== НАСТРОЙКА ТЕСТОВ ПРОИЗВОДИТЕЛЬНОСТИ ===");
        System.out.println("Текущее время (МСК): " + currentTime);
        System.out.println("Тестовая дата: " + testDate);
        System.out.println("День недели: " + testDate.getDayOfWeek());
        System.out.println("ВНИМАНИЕ: Тесты будут делать реальные запросы к T-INVEST API!");
    }

    /**
     * Определяет тестовую дату на основе текущего времени
     */
    @Step("Определение тестовой даты на основе текущего времени")
    @DisplayName("Определение тестовой даты")
    @Description("Определяет подходящую тестовую дату с учетом выходных дней и времени суток")
    @Tag("helper")
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
    @Step("Поиск предыдущего рабочего дня")
    @DisplayName("Поиск предыдущего рабочего дня")
    @Description("Находит предыдущий рабочий день, пропуская выходные")
    @Tag("helper")
    private LocalDate findPreviousWorkingDay(LocalDate fromDate) {
        LocalDate candidate = fromDate.minusDays(1);
        while (candidate.getDayOfWeek() == DayOfWeek.SATURDAY || 
               candidate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            candidate = candidate.minusDays(1);
        }
        return candidate;
    }

    @Test
    @DisplayName("Тест производительности - получение цен акций")
    @Description("Тест проверяет производительность получения цен закрытия для акций через реальный API")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Performance Testing")
    @Tag("api")
    @Tag("performance")
    @Tag("integration")
    @Tag("shares")
    @Tag("close-prices")
    @Tag("real-api")
    void getClosePricesForShares_PerformanceTest_ShouldCompleteWithinReasonableTime() throws Exception {
        // Given
        System.out.println("=== ТЕСТ ПРОИЗВОДИТЕЛЬНОСТИ: Получение цен акций ===");
        
        String url = baseUrl + "/close-prices/shares";
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> httpResponse = restTemplate.getForEntity(url, String.class);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        assertThat(httpResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String response = httpResponse.getBody();

        // Then - проверяем результат
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        int count = (Integer) responseMap.get("count");

        System.out.println("=== РЕЗУЛЬТАТЫ ПРОИЗВОДИТЕЛЬНОСТИ ===");
        System.out.println("Время выполнения: " + duration + " мс");
        System.out.println("Получено цен: " + count);
        System.out.println("Среднее время на цену: " + (count > 0 ? (double) duration / count : 0) + " мс");

        // Проверяем, что запрос выполнился за разумное время (не более 30 секунд)
        assertThat(duration).isLessThan(30000);
        assertThat(count).isGreaterThanOrEqualTo(0);

        System.out.println("✅ Тест производительности пройден!");
    }

    @Test
    @DisplayName("Тест производительности - получение цен фьючерсов")
    @Description("Тест проверяет производительность получения цен закрытия для фьючерсов через реальный API")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Performance Testing")
    @Tag("api")
    @Tag("performance")
    @Tag("integration")
    @Tag("futures")
    @Tag("close-prices")
    @Tag("real-api")
    void getClosePricesForFutures_PerformanceTest_ShouldCompleteWithinReasonableTime() throws Exception {
        // Given
        System.out.println("=== ТЕСТ ПРОИЗВОДИТЕЛЬНОСТИ: Получение цен фьючерсов ===");
        
        String url = baseUrl + "/close-prices/futures";
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> httpResponse = restTemplate.getForEntity(url, String.class);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        assertThat(httpResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String response = httpResponse.getBody();

        // Then - проверяем результат
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        int count = (Integer) responseMap.get("count");

        System.out.println("=== РЕЗУЛЬТАТЫ ПРОИЗВОДИТЕЛЬНОСТИ ===");
        System.out.println("Время выполнения: " + duration + " мс");
        System.out.println("Получено цен: " + count);
        System.out.println("Среднее время на цену: " + (count > 0 ? (double) duration / count : 0) + " мс");

        // Проверяем, что запрос выполнился за разумное время (не более 30 секунд)
        assertThat(duration).isLessThan(30000);
        assertThat(count).isGreaterThanOrEqualTo(0);

        System.out.println("✅ Тест производительности пройден!");
    }

    @Test
    @DisplayName("Тест производительности - получение цены по FIGI")
    @Description("Тест проверяет производительность получения цены закрытия по конкретному FIGI через реальный API")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Performance Testing")
    @Tag("api")
    @Tag("performance")
    @Tag("integration")
    @Tag("figi")
    @Tag("close-prices")
    @Tag("real-api")
    @Tag("single-request")
    void getClosePriceByFigi_PerformanceTest_ShouldCompleteWithinReasonableTime() throws Exception {
        // Given
        String realFigi = "BBG004730N88"; // Сбербанк
        System.out.println("=== ТЕСТ ПРОИЗВОДИТЕЛЬНОСТИ: Получение цены по FIGI ===");
        System.out.println("FIGI: " + realFigi);
        
        String url = baseUrl + "/close-prices/" + realFigi;
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> httpResponse = restTemplate.getForEntity(url, String.class);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        assertThat(httpResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String response = httpResponse.getBody();

        // Then - проверяем результат
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        boolean success = (Boolean) responseMap.get("success");

        System.out.println("=== РЕЗУЛЬТАТЫ ПРОИЗВОДИТЕЛЬНОСТИ ===");
        System.out.println("Время выполнения: " + duration + " мс");
        System.out.println("Успех: " + success);

        // Проверяем, что запрос выполнился за разумное время (не более 10 секунд)
        assertThat(duration).isLessThan(10000);
        assertThat(success).isNotNull();

        System.out.println("✅ Тест производительности пройден!");
    }

    @Test
    @DisplayName("Тест производительности - загрузка цен закрытия")
    @Description("Тест проверяет производительность загрузки и сохранения цен закрытия через реальный API")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Performance Testing")
    @Tag("api")
    @Tag("performance")
    @Tag("integration")
    @Tag("load")
    @Tag("close-prices")
    @Tag("real-api")
    @Tag("save")
    void loadClosePricesToday_PerformanceTest_ShouldCompleteWithinReasonableTime() throws Exception {
        // Given
        System.out.println("=== ТЕСТ ПРОИЗВОДИТЕЛЬНОСТИ: Загрузка цен закрытия ===");
        
        String url = baseUrl + "/close-prices";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> httpResponse = restTemplate.postForEntity(url, entity, String.class);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        assertThat(httpResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String response = httpResponse.getBody();

        // Then - проверяем результат
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        boolean success = (Boolean) responseMap.get("success");
        int totalRequested = (Integer) responseMap.get("totalRequested");
        int newItemsSaved = (Integer) responseMap.get("newItemsSaved");

        System.out.println("=== РЕЗУЛЬТАТЫ ПРОИЗВОДИТЕЛЬНОСТИ ===");
        System.out.println("Время выполнения: " + duration + " мс");
        System.out.println("Успех: " + success);
        System.out.println("Запрошено: " + totalRequested);
        System.out.println("Сохранено: " + newItemsSaved);
        System.out.println("Среднее время на инструмент: " + (totalRequested > 0 ? (double) duration / totalRequested : 0) + " мс");

        // Проверяем, что запрос выполнился за разумное время (не более 60 секунд)
        assertThat(duration).isLessThan(60000);
        assertThat(success).isTrue();
        assertThat(totalRequested).isGreaterThanOrEqualTo(0);

        System.out.println("✅ Тест производительности пройден!");
    }

    @Test
    @DisplayName("Тест производительности - множественные запросы")
    @Description("Тест проверяет производительность и стабильность при выполнении множественных запросов подряд")
    @Severity(SeverityLevel.NORMAL)
    @Story("Performance Testing")
    @Tag("api")
    @Tag("performance")
    @Tag("integration")
    @Tag("concurrent")
    @Tag("multiple-requests")
    @Tag("stability")
    @Tag("real-api")
    void multipleRequests_ConcurrentPerformanceTest_ShouldHandleMultipleRequests() throws Exception {
        // Given
        System.out.println("=== ТЕСТ ПРОИЗВОДИТЕЛЬНОСТИ: Множественные запросы ===");
        
        int numberOfRequests = 3;
        long[] durations = new long[numberOfRequests];
        
        // When - выполняем несколько запросов подряд
        for (int i = 0; i < numberOfRequests; i++) {
            System.out.println("Запрос " + (i + 1) + "/" + numberOfRequests + "...");
            
            String url = baseUrl + "/close-prices/shares";
            long startTime = System.currentTimeMillis();
            ResponseEntity<String> httpResponse = restTemplate.getForEntity(url, String.class);
            long endTime = System.currentTimeMillis();
            durations[i] = endTime - startTime;
            assertThat(httpResponse.getStatusCode().is2xxSuccessful()).isTrue();
            
            System.out.println("  Время выполнения: " + durations[i] + " мс");
        }

        // Then - анализируем результаты
        long totalDuration = 0;
        long minDuration = Long.MAX_VALUE;
        long maxDuration = 0;
        
        for (long duration : durations) {
            totalDuration += duration;
            minDuration = Math.min(minDuration, duration);
            maxDuration = Math.max(maxDuration, duration);
        }
        
        double avgDuration = (double) totalDuration / numberOfRequests;

        System.out.println("=== РЕЗУЛЬТАТЫ МНОЖЕСТВЕННЫХ ЗАПРОСОВ ===");
        System.out.println("Количество запросов: " + numberOfRequests);
        System.out.println("Общее время: " + totalDuration + " мс");
        System.out.println("Среднее время: " + String.format("%.2f", avgDuration) + " мс");
        System.out.println("Минимальное время: " + minDuration + " мс");
        System.out.println("Максимальное время: " + maxDuration + " мс");

        // Проверяем, что все запросы выполнились за разумное время
        for (long duration : durations) {
            assertThat(duration).isLessThan(30000);
        }

        // Проверяем, что разброс времени не слишком большой (стабильность)
        double variance = maxDuration - minDuration;
        assertThat(variance).isLessThan(20000); // Разброс не более 20 секунд

        System.out.println("✅ Тест множественных запросов пройден!");
    }

    @Test
    @DisplayName("Стресс-тест - повторные загрузки цен")
    @Description("Стресс-тест проверяет стабильность системы при повторных загрузках цен закрытия")
    @Severity(SeverityLevel.NORMAL)
    @Story("Performance Testing")
    @Tag("api")
    @Tag("performance")
    @Tag("integration")
    @Tag("stress-test")
    @Tag("repeated-requests")
    @Tag("load")
    @Tag("stability")
    @Tag("real-api")
    void loadClosePricesToday_StressTest_ShouldHandleRepeatedRequests() throws Exception {
        // Given
        System.out.println("=== СТРЕСС-ТЕСТ: Повторные загрузки цен ===");
        
        int numberOfLoads = 2; // Ограничиваем для тестирования
        long[] durations = new long[numberOfLoads];
        
        // When - выполняем несколько загрузок подряд
        for (int i = 0; i < numberOfLoads; i++) {
            System.out.println("Загрузка " + (i + 1) + "/" + numberOfLoads + "...");
            
            long startTime = System.currentTimeMillis();
            
            String url = baseUrl + "/close-prices";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            ResponseEntity<String> httpResponse = restTemplate.postForEntity(url, entity, String.class);
            assertThat(httpResponse.getStatusCode().is2xxSuccessful()).isTrue();
            String response = httpResponse.getBody();
            
            long endTime = System.currentTimeMillis();
            durations[i] = endTime - startTime;
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            boolean success = (Boolean) responseMap.get("success");
            int newItemsSaved = (Integer) responseMap.get("newItemsSaved");
            int existingItemsSkipped = (Integer) responseMap.get("existingItemsSkipped");
            
            System.out.println("  Время выполнения: " + durations[i] + " мс");
            System.out.println("  Успех: " + success);
            System.out.println("  Новых: " + newItemsSaved + ", существующих: " + existingItemsSkipped);
        }

        // Then - анализируем результаты
        long totalDuration = 0;
        for (long duration : durations) {
            totalDuration += duration;
        }
        
        double avgDuration = (double) totalDuration / numberOfLoads;

        System.out.println("=== РЕЗУЛЬТАТЫ СТРЕСС-ТЕСТА ===");
        System.out.println("Количество загрузок: " + numberOfLoads);
        System.out.println("Общее время: " + totalDuration + " мс");
        System.out.println("Среднее время: " + String.format("%.2f", avgDuration) + " мс");

        // Проверяем, что все загрузки выполнились за разумное время
        for (long duration : durations) {
            assertThat(duration).isLessThan(60000);
        }

        System.out.println("✅ Стресс-тест пройден!");
    }
}
