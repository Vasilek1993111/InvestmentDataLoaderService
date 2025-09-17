package com.example.InvestmentDataLoaderService.integration;

import com.example.InvestmentDataLoaderService.dto.ShareDto;
import com.example.InvestmentDataLoaderService.dto.FutureDto;
import com.example.InvestmentDataLoaderService.dto.IndicativeDto;
import com.example.InvestmentDataLoaderService.entity.ShareEntity;
import com.example.InvestmentDataLoaderService.entity.FutureEntity;
import com.example.InvestmentDataLoaderService.entity.IndicativeEntity;
import com.example.InvestmentDataLoaderService.repository.ShareRepository;
import com.example.InvestmentDataLoaderService.repository.FutureRepository;
import com.example.InvestmentDataLoaderService.repository.IndicativeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционный тест для InstrumentService
 * Тестирует реальный запрос к API и сверяет данные с существующими записями в БД
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class InstrumentServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ShareRepository shareRepository;

    @Autowired
    private FutureRepository futureRepository;

    @Autowired
    private IndicativeRepository indicativeRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/instruments";
        // НЕ очищаем БД - работаем с существующими данными
    }

    @Test
    void shouldLoadSharesFromApiAndVerifyInDatabase() {
        // Given
        String url = baseUrl + "/shares?status=INSTRUMENT_STATUS_BASE&exchange=moex_mrng_evng_e_wknd_dlr&currency=RUB";

        // When - делаем запрос к T-API
        ResponseEntity<ShareDto[]> response = restTemplate.getForEntity(url, ShareDto[].class);

        // Then - проверяем ответ API
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);

        List<ShareDto> sharesFromApi = List.of(response.getBody());
        System.out.println("Акций получено из API: " + sharesFromApi.size());

        // Получаем все существующие записи из БД
        List<ShareEntity> sharesInDb = shareRepository.findAll();
        assertThat(sharesInDb).isNotEmpty();
        System.out.println("Акций найдено в БД: " + sharesInDb.size());

        // Проверяем, что все FIGI и Ticker из API есть в БД
        Set<String> apiFigis = sharesFromApi.stream().map(ShareDto::figi).collect(Collectors.toSet());
        Set<String> dbFigis = sharesInDb.stream().map(ShareEntity::getFigi).collect(Collectors.toSet());
        assertThat(dbFigis).containsAll(apiFigis);

        Set<String> apiTickers = sharesFromApi.stream().map(ShareDto::ticker).collect(Collectors.toSet());
        Set<String> dbTickers = sharesInDb.stream().map(ShareEntity::getTicker).collect(Collectors.toSet());
        assertThat(dbTickers).containsAll(apiTickers);

        // Дополнительная проверка на соответствие полей для каждой акции из API
        for (ShareDto apiShare : sharesFromApi) {
            ShareEntity dbShare = shareRepository.findById(apiShare.figi()).orElse(null);
            assertThat(dbShare).isNotNull();
            assertThat(dbShare.getTicker()).isEqualTo(apiShare.ticker());
            assertThat(dbShare.getName()).isEqualTo(apiShare.name());
            assertThat(dbShare.getCurrency()).isEqualTo(apiShare.currency());
            assertThat(dbShare.getExchange()).isEqualTo(apiShare.exchange());
        }

        // Выводим статистику для наглядности
        System.out.println("=== Результаты теста ===");
        System.out.println("Акций получено из API: " + sharesFromApi.size());
        System.out.println("Акций найдено в БД: " + sharesInDb.size());
        System.out.println("Все акции из API найдены в БД: " + (dbFigis.containsAll(apiFigis) ? "ДА" : "НЕТ"));
    }

    @Test
    void shouldHandleEmptyResponse() {
        // Given: Запрос, который предположительно вернет пустой список
        String url = baseUrl + "/shares?status=INSTRUMENT_STATUS_UNSPECIFIED&exchange=NON_EXISTENT&currency=XYZ";

        // When
        ResponseEntity<ShareDto[]> response = restTemplate.getForEntity(url, ShareDto[].class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);

        // Проверяем, что в БД есть данные (не очищали)
        List<ShareEntity> sharesInDb = shareRepository.findAll();
        assertThat(sharesInDb).isNotEmpty();
    }

    @Test
    void shouldValidateShareDataIntegrity() {
        // Given
        String url = baseUrl + "/shares?status=INSTRUMENT_STATUS_BASE&exchange=moex_mrng_evng_e_wknd_dlr&currency=RUB";

        // When
        ResponseEntity<ShareDto[]> response = restTemplate.getForEntity(url, ShareDto[].class);
        List<ShareDto> sharesFromApi = List.of(response.getBody());
        assertThat(sharesFromApi).isNotNull().isNotEmpty();

        // Получаем данные из БД
        List<ShareEntity> sharesInDb = shareRepository.findAll();
        assertThat(sharesInDb).isNotEmpty();

        // Проверяем целостность данных - все акции из API должны быть в БД
        Set<String> apiFigis = sharesFromApi.stream().map(ShareDto::figi).collect(Collectors.toSet());
        Set<String> dbFigis = sharesInDb.stream().map(ShareEntity::getFigi).collect(Collectors.toSet());
        assertThat(dbFigis).containsAll(apiFigis);

        // Проверяем, что количество записей в БД не изменилось (не сохраняли)
        List<ShareEntity> sharesInDbAfter = shareRepository.findAll();
        assertThat(sharesInDbAfter).hasSameSizeAs(sharesInDb);
    }

    // ==================== ТЕСТЫ ДЛЯ ФЬЮЧЕРСОВ ====================

    @Test
    void shouldLoadFuturesFromApiAndVerifyInDatabase() {
        // Given
        String url = baseUrl + "/futures?status=INSTRUMENT_STATUS_BASE";

        // When - делаем запрос к T-API
        ResponseEntity<FutureDto[]> response = restTemplate.getForEntity(url, FutureDto[].class);

        // Then - проверяем ответ API
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);

        List<FutureDto> futuresFromApi = List.of(response.getBody());
        System.out.println("Фьючерсов получено из API: " + futuresFromApi.size());

        // Получаем все существующие записи из БД
        List<FutureEntity> futuresInDb = futureRepository.findAll();
        assertThat(futuresInDb).isNotEmpty();
        System.out.println("Фьючерсов найдено в БД: " + futuresInDb.size());

        // Проверяем, что все FIGI и Ticker из API есть в БД
        Set<String> apiFigis = futuresFromApi.stream().map(FutureDto::figi).collect(Collectors.toSet());
        Set<String> dbFigis = futuresInDb.stream().map(FutureEntity::getFigi).collect(Collectors.toSet());
        assertThat(dbFigis).containsAll(apiFigis);

        Set<String> apiTickers = futuresFromApi.stream().map(FutureDto::ticker).collect(Collectors.toSet());
        Set<String> dbTickers = futuresInDb.stream().map(FutureEntity::getTicker).collect(Collectors.toSet());
        assertThat(dbTickers).containsAll(apiTickers);

        // Дополнительная проверка на соответствие полей для каждого фьючерса из API
        for (FutureDto apiFuture : futuresFromApi) {
            FutureEntity dbFuture = futureRepository.findById(apiFuture.figi()).orElse(null);
            assertThat(dbFuture).isNotNull();
            assertThat(dbFuture.getTicker()).isEqualTo(apiFuture.ticker());
            assertThat(dbFuture.getAssetType()).isEqualTo(apiFuture.assetType());
            assertThat(dbFuture.getBasicAsset()).isEqualTo(apiFuture.basicAsset());
            assertThat(dbFuture.getCurrency()).isEqualTo(apiFuture.currency());
            assertThat(dbFuture.getExchange()).isEqualTo(apiFuture.exchange());
        }

        // Выводим статистику для наглядности
        System.out.println("=== Результаты теста фьючерсов ===");
        System.out.println("Фьючерсов получено из API: " + futuresFromApi.size());
        System.out.println("Фьючерсов найдено в БД: " + futuresInDb.size());
        System.out.println("Все фьючерсы из API найдены в БД: " + (dbFigis.containsAll(apiFigis) ? "ДА" : "НЕТ"));
    }

    @Test
    void shouldHandleEmptyFuturesResponse() {
        // Given: Запрос, который предположительно вернет пустой список
        String url = baseUrl + "/futures?status=INSTRUMENT_STATUS_UNSPECIFIED&exchange=NON_EXISTENT&currency=XYZ";

        // When
        ResponseEntity<FutureDto[]> response = restTemplate.getForEntity(url, FutureDto[].class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);

        // Проверяем, что в БД есть данные (не очищали)
        List<FutureEntity> futuresInDb = futureRepository.findAll();
        assertThat(futuresInDb).isNotEmpty();
    }

    @Test
    void shouldValidateFuturesDataIntegrity() {
        // Given
        String url = baseUrl + "/futures?status=INSTRUMENT_STATUS_BASE";

        // When
        ResponseEntity<FutureDto[]> response = restTemplate.getForEntity(url, FutureDto[].class);
        List<FutureDto> futuresFromApi = List.of(response.getBody());
        assertThat(futuresFromApi).isNotNull().isNotEmpty();

        // Получаем данные из БД
        List<FutureEntity> futuresInDb = futureRepository.findAll();
        assertThat(futuresInDb).isNotEmpty();

        // Проверяем целостность данных - все фьючерсы из API должны быть в БД
        Set<String> apiFigis = futuresFromApi.stream().map(FutureDto::figi).collect(Collectors.toSet());
        Set<String> dbFigis = futuresInDb.stream().map(FutureEntity::getFigi).collect(Collectors.toSet());
        assertThat(dbFigis).containsAll(apiFigis);

        // Проверяем, что количество записей в БД не изменилось (не сохраняли)
        List<FutureEntity> futuresInDbAfter = futureRepository.findAll();
        assertThat(futuresInDbAfter).hasSameSizeAs(futuresInDb);
    }

    @Test
    void shouldValidateFuturesFigiAndTickerCombination() {
        // Given
        String url = baseUrl + "/futures?status=INSTRUMENT_STATUS_BASE";

        // When
        ResponseEntity<FutureDto[]> response = restTemplate.getForEntity(url, FutureDto[].class);
        List<FutureDto> futuresFromApi = List.of(response.getBody());
        assertThat(futuresFromApi).isNotNull().isNotEmpty();

        // Получаем данные из БД
        List<FutureEntity> futuresInDb = futureRepository.findAll();
        assertThat(futuresInDb).isNotEmpty();

        // Создаем комбинации FIGI+Ticker для проверки
        Set<String> apiFigiTickerCombinations = futuresFromApi.stream()
                .map(f -> f.figi() + "|" + f.ticker())
                .collect(Collectors.toSet());
        
        Set<String> dbFigiTickerCombinations = futuresInDb.stream()
                .map(f -> f.getFigi() + "|" + f.getTicker())
                .collect(Collectors.toSet());

        // Проверяем, что все комбинации FIGI+Ticker из API есть в БД
        assertThat(dbFigiTickerCombinations).containsAll(apiFigiTickerCombinations);

        System.out.println("=== Проверка комбинаций FIGI+Ticker ===");
        System.out.println("Комбинаций из API: " + apiFigiTickerCombinations.size());
        System.out.println("Комбинаций в БД: " + dbFigiTickerCombinations.size());
        System.out.println("Все комбинации из API найдены в БД: " + 
                (dbFigiTickerCombinations.containsAll(apiFigiTickerCombinations) ? "ДА" : "НЕТ"));
    }

    // ==================== ТЕСТЫ ДЛЯ ИНДИКАТИВОВ ====================

    @Test
    void shouldGetIndicativesFromApi() {
        // Given
        String url = baseUrl + "/indicatives";

        // When
        ResponseEntity<IndicativeDto[]> response = restTemplate.getForEntity(url, IndicativeDto[].class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);

        // Проверяем, что в БД есть данные (не очищали)
        List<IndicativeEntity> indicativesInDb = indicativeRepository.findAll();
        assertThat(indicativesInDb).isNotEmpty();

        System.out.println("=== Проверка API индикативов ===");
        System.out.println("Индикативов из API: " + response.getBody().length);
        System.out.println("Индикативов в БД: " + indicativesInDb.size());
    }

    @Test
    void shouldValidateIndicativesDataIntegrity() {
        // Given
        String url = baseUrl + "/indicatives";

        // When
        ResponseEntity<IndicativeDto[]> response = restTemplate.getForEntity(url, IndicativeDto[].class);
        List<IndicativeDto> indicativesFromApi = List.of(response.getBody());
        assertThat(indicativesFromApi).isNotNull().isNotEmpty();

        // Фильтруем индикативы с пустым figi (как в реальном коде)
        List<IndicativeDto> filteredIndicatives = indicativesFromApi.stream()
                .filter(indicative -> indicative.figi() != null && !indicative.figi().trim().isEmpty())
                .collect(Collectors.toList());

        // Получаем данные из БД
        List<IndicativeEntity> indicativesInDb = indicativeRepository.findAll();
        assertThat(indicativesInDb).isNotEmpty();

        // Проверяем целостность данных - все отфильтрованные индикативы из API должны быть в БД
        Set<String> apiFigis = filteredIndicatives.stream().map(IndicativeDto::figi).collect(Collectors.toSet());
        Set<String> dbFigis = indicativesInDb.stream().map(IndicativeEntity::getFigi).collect(Collectors.toSet());
        assertThat(dbFigis).containsAll(apiFigis);

        // Проверяем, что количество записей в БД не изменилось (не сохраняли)
        List<IndicativeEntity> indicativesInDbAfter = indicativeRepository.findAll();
        assertThat(indicativesInDbAfter).hasSameSizeAs(indicativesInDb);

        System.out.println("=== Проверка целостности данных индикативов ===");
        System.out.println("Индикативов из API (все): " + indicativesFromApi.size());
        System.out.println("Индикативов из API (отфильтрованных): " + filteredIndicatives.size());
        System.out.println("Индикативов в БД: " + indicativesInDb.size());
        System.out.println("Все отфильтрованные индикативы найдены в БД: " + 
                (dbFigis.containsAll(apiFigis) ? "ДА" : "НЕТ"));
    }

    @Test
    void shouldValidateIndicativesFigiAndTickerCombination() {
        // Given
        String url = baseUrl + "/indicatives";

        // When
        ResponseEntity<IndicativeDto[]> response = restTemplate.getForEntity(url, IndicativeDto[].class);
        List<IndicativeDto> indicativesFromApi = List.of(response.getBody());
        assertThat(indicativesFromApi).isNotNull().isNotEmpty();

        // Фильтруем индикативы с пустым figi (как в реальном коде)
        List<IndicativeDto> filteredIndicatives = indicativesFromApi.stream()
                .filter(indicative -> indicative.figi() != null && !indicative.figi().trim().isEmpty())
                .collect(Collectors.toList());

        // Получаем данные из БД
        List<IndicativeEntity> indicativesInDb = indicativeRepository.findAll();
        assertThat(indicativesInDb).isNotEmpty();

        // Создаем комбинации FIGI+Ticker для проверки
        Set<String> apiFigiTickerCombinations = filteredIndicatives.stream()
                .map(i -> i.figi() + "|" + i.ticker())
                .collect(Collectors.toSet());
        
        Set<String> dbFigiTickerCombinations = indicativesInDb.stream()
                .map(i -> i.getFigi() + "|" + i.getTicker())
                .collect(Collectors.toSet());

        // Проверяем, что все комбинации FIGI+Ticker из API есть в БД
        assertThat(dbFigiTickerCombinations).containsAll(apiFigiTickerCombinations);

        System.out.println("=== Проверка комбинаций FIGI+Ticker для индикативов ===");
        System.out.println("Комбинаций из API (все): " + indicativesFromApi.stream()
                .map(i -> i.figi() + "|" + i.ticker()).collect(Collectors.toSet()).size());
        System.out.println("Комбинаций из API (отфильтрованных): " + apiFigiTickerCombinations.size());
        System.out.println("Комбинаций в БД: " + dbFigiTickerCombinations.size());
        System.out.println("Все отфильтрованные комбинации из API найдены в БД: " + 
                (dbFigiTickerCombinations.containsAll(apiFigiTickerCombinations) ? "ДА" : "НЕТ"));
    }
}