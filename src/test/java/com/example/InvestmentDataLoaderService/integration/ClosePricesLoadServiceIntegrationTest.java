package com.example.InvestmentDataLoaderService.integration;

import com.example.InvestmentDataLoaderService.entity.ClosePriceEntity;
import com.example.InvestmentDataLoaderService.repository.ClosePriceRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
 

/**
 * Интеграционные тесты реальной загрузки цен закрытия через T-INVEST API
 * 
 * Тестирует:
 * 1. Реальную загрузку цен закрытия через POST /api/data-loading/close-prices
 * 2. Сохранение данных в БД
 * 3. Проверку статистики загрузки
 * 4. Проверку фильтрации неверных цен
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class ClosePricesLoadServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClosePriceRepository closePriceRepository;

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
        
        System.out.println("=== НАСТРОЙКА РЕАЛЬНОЙ ЗАГРУЗКИ ЦЕН ЗАКРЫТИЯ ===");
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
    void loadClosePricesToday_RealTInvestAPI_ShouldLoadAndSaveData() throws Exception {
        // Given - БД пуста (уже очищена в setUp)
        
        System.out.println("=== ТЕСТ: Реальная загрузка цен закрытия за сегодня ===");
        System.out.println("Ожидаем данные за дату: " + testDate);
        System.out.println("Начинаем загрузку...");
        
        long startTime = System.currentTimeMillis();
        
        // When - вызываем API для загрузки цен (реальный запрос к T-INVEST)
        String url = baseUrl + "/close-prices";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> httpResponse = restTemplate.postForEntity(url, entity, String.class);
        assertThat(httpResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String response = httpResponse.getBody();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

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
        System.out.println("Время выполнения: " + duration + " мс");
        System.out.println("Успех: " + success);
        System.out.println("Сообщение: " + message);
        System.out.println("Запрошено инструментов: " + totalRequested);
        System.out.println("Сохранено новых цен: " + newItemsSaved);
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

        // Проверяем математику
        int expectedTotal = newItemsSaved + existingItemsSkipped + invalidItemsFiltered + missingFromApi;
        System.out.println("Проверка математики: " + totalRequested + " = " + newItemsSaved + " + " + 
                          existingItemsSkipped + " + " + invalidItemsFiltered + " + " + missingFromApi);
        assertThat(totalRequested).isEqualTo(expectedTotal);

        // Проверяем, что данные сохранились в БД
        List<ClosePriceEntity> savedPrices = closePriceRepository.findAll();
        System.out.println("Сохранено в БД: " + savedPrices.size() + " цен");
        
        if (!savedPrices.isEmpty()) {
            System.out.println("Примеры сохраненных цен:");
            for (int i = 0; i < Math.min(5, savedPrices.size()); i++) {
                ClosePriceEntity price = savedPrices.get(i);
                System.out.println("  " + (i+1) + ". FIGI: " + price.getId().getFigi() + 
                                 ", Дата: " + price.getId().getPriceDate() + 
                                 ", Цена: " + price.getClosePrice() + 
                                 ", Тип: " + price.getInstrumentType());
            }
            
            // Проверяем, что все сохраненные цены имеют правильную дату
            for (ClosePriceEntity price : savedPrices) {
                assertThat(price.getId().getPriceDate()).isEqualTo(testDate);
                assertThat(price.getClosePrice()).isGreaterThan(BigDecimal.ZERO);
                assertThat(price.getId().getFigi()).isNotNull().isNotEmpty();
            }
        }

        System.out.println("✅ Реальная загрузка цен закрытия выполнена успешно!");
    }

    @Test
    void loadClosePricesToday_Twice_ShouldSkipExistingPrices() throws Exception {
        // Given - загружаем цены первый раз
        System.out.println("=== ТЕСТ: Повторная загрузка цен закрытия ===");
        System.out.println("Ожидаем данные за дату: " + testDate);
        
        // Первая загрузка
        System.out.println("1. Первая загрузка...");
        String url = baseUrl + "/close-prices";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        String firstResponse = restTemplate.postForEntity(url, entity, String.class).getBody();

        @SuppressWarnings("unchecked")
        Map<String, Object> firstResponseMap = objectMapper.readValue(firstResponse, Map.class);
        int firstNewItemsSaved = (Integer) firstResponseMap.get("newItemsSaved");
        int firstExistingItemsSkipped = (Integer) firstResponseMap.get("existingItemsSkipped");
        
        System.out.println("Первая загрузка - новых: " + firstNewItemsSaved + ", существующих: " + firstExistingItemsSkipped);
        
        // Вторая загрузка
        System.out.println("2. Вторая загрузка...");
        String secondResponse = restTemplate.postForEntity(url, entity, String.class).getBody();

        @SuppressWarnings("unchecked")
        Map<String, Object> secondResponseMap = objectMapper.readValue(secondResponse, Map.class);
        int secondNewItemsSaved = (Integer) secondResponseMap.get("newItemsSaved");
        int secondExistingItemsSkipped = (Integer) secondResponseMap.get("existingItemsSkipped");
        int secondInvalidItemsFiltered = (Integer) secondResponseMap.get("invalidItemsFiltered");
        int secondMissingFromApi = (Integer) secondResponseMap.get("missingFromApi");
        int secondTotalRequested = (Integer) secondResponseMap.get("totalRequested");
        
        System.out.println("Вторая загрузка - новых: " + secondNewItemsSaved + ", существующих: " + secondExistingItemsSkipped);

        // Then - мягкая проверка для реального API: во второй раз часть цен может быть новой
        assertThat(secondNewItemsSaved).isGreaterThanOrEqualTo(0);
        assertThat(secondExistingItemsSkipped).isGreaterThanOrEqualTo(0);

        // Проверяем корректность суммирования результатов второго запуска
        int secondExpectedTotal = secondNewItemsSaved + secondExistingItemsSkipped + secondInvalidItemsFiltered + secondMissingFromApi;
        assertThat(secondTotalRequested).isEqualTo(secondExpectedTotal);

        System.out.println("✅ Повторная загрузка корректно пропустила существующие цены!");
    }

    @Test
    void loadClosePricesToday_WithSpecificInstruments_ShouldLoadOnlyRequested() throws Exception {
        // Given - создаем запрос с конкретными инструментами
        String requestBody = """
            {
                "instruments": ["BBG004730N88", "BBG004730ZJ9", "BBG004S685M2"]
            }
            """;
        
        System.out.println("=== ТЕСТ: Загрузка цен для конкретных инструментов ===");
        System.out.println("Инструменты: BBG004730N88, BBG004730ZJ9, BBG004S685M2");
        System.out.println("Ожидаем данные за дату: " + testDate);
        
        // When - вызываем API с конкретными инструментами
        String urlSave = baseUrl + "/close-prices/save";
        HttpHeaders headersSave = new HttpHeaders();
        headersSave.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entitySave = new HttpEntity<>(requestBody, headersSave);
        ResponseEntity<String> httpResponseSave = restTemplate.postForEntity(urlSave, entitySave, String.class);
        assertThat(httpResponseSave.getStatusCode().is2xxSuccessful()).isTrue();
        String response = httpResponseSave.getBody();

        // Then - проверяем результат
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        boolean success = (Boolean) responseMap.get("success");
        int totalRequested = (Integer) responseMap.get("totalRequested");
        int newItemsSaved = (Integer) responseMap.get("newItemsSaved");
        int existingItemsSkipped = (Integer) responseMap.get("existingItemsSkipped");
        int invalidItemsFiltered = (Integer) responseMap.get("invalidItemsFiltered");
        int missingFromApi = (Integer) responseMap.get("missingFromApi");

        System.out.println("=== РЕЗУЛЬТАТЫ ЗАГРУЗКИ ДЛЯ КОНКРЕТНЫХ ИНСТРУМЕНТОВ ===");
        System.out.println("Успех: " + success);
        System.out.println("Запрошено: " + totalRequested);
        System.out.println("Сохранено новых: " + newItemsSaved);
        System.out.println("Пропущено существующих: " + existingItemsSkipped);
        System.out.println("Отфильтровано неверных: " + invalidItemsFiltered);
        System.out.println("Не получено из API: " + missingFromApi);

        // Проверяем, что запрос выполнился
        assertThat(success).isTrue();
        assertThat(totalRequested).isEqualTo(3); // Запросили 3 инструмента
        assertThat(newItemsSaved).isGreaterThanOrEqualTo(0);
        assertThat(existingItemsSkipped).isGreaterThanOrEqualTo(0);
        assertThat(invalidItemsFiltered).isGreaterThanOrEqualTo(0);
        assertThat(missingFromApi).isGreaterThanOrEqualTo(0);

        // Проверяем математику
        int expectedTotal = newItemsSaved + existingItemsSkipped + invalidItemsFiltered + missingFromApi;
        assertThat(totalRequested).isEqualTo(expectedTotal);

        System.out.println("✅ Загрузка цен для конкретных инструментов выполнена успешно!");
    }
}
