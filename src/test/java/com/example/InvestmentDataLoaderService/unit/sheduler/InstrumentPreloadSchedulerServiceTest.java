package com.example.InvestmentDataLoaderService.unit.sheduler;

import com.example.InvestmentDataLoaderService.dto.*;
import com.example.InvestmentDataLoaderService.service.InstrumentService;
import com.example.InvestmentDataLoaderService.scheduler.InstrumentPreloadSchedulerService;

import io.qameta.allure.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Epic("Scheduler Services")
@Feature("Instrument Preload Scheduler")
@DisplayName("Instrument Preload Scheduler Service Tests")
@Owner("Investment Data Loader Service Team")
@Severity(SeverityLevel.CRITICAL)
public class InstrumentPreloadSchedulerServiceTest {

    @Mock
    private InstrumentService instrumentService;

    @InjectMocks
    private InstrumentPreloadSchedulerService schedulerService;

    @BeforeEach
    void setUp() {
        reset(instrumentService);
    }

    // ========== ПОЗИТИВНЫЕ ТЕСТЫ ==========

    @Test
    @DisplayName("Успешное выполнение полного цикла прогрева и сохранения всех инструментов")
    @Description("Тест проверяет успешное выполнение полного цикла: загрузка акций, фьючерсов и индикативов с последующим сохранением")
    @Story("Успешные сценарии")
    @Severity(SeverityLevel.CRITICAL)
    void preloadAndPersistInstruments_ShouldCompleteSuccessfully_WhenAllServicesWork() {
        
        // Шаг 1: Подготовка тестовых данных
        List<ShareDto> testShares = Allure.step("Подготовка тестовых данных для акций", () -> {
            return createTestShares();
        });
        
        List<FutureDto> testFutures = Allure.step("Подготовка тестовых данных для фьючерсов", () -> {
            return createTestFutures();
        });
        
        List<IndicativeDto> testIndicatives = Allure.step("Подготовка тестовых данных для индикативов", () -> {
            return createTestIndicatives();
        });

        // Шаг 2: Создание ответов сервисов
        SaveResponseDto shareSaveResponse = Allure.step("Создание ответа сервиса для акций", () -> {
            return createSuccessSaveResponse("Акции успешно сохранены", 10, 5, 5, 0, 0);
        });
        
        SaveResponseDto futureSaveResponse = Allure.step("Создание ответа сервиса для фьючерсов", () -> {
            return createSuccessSaveResponse("Фьючерсы успешно сохранены", 8, 3, 5, 0, 0);
        });
        
        SaveResponseDto indicativeSaveResponse = Allure.step("Создание ответа сервиса для индикативов", () -> {
            return createSuccessSaveResponse("Индикативы успешно сохранены", 5, 2, 3, 0, 0);
        });

        // Шаг 3: Настройка моков сервиса
        Allure.step("Настройка моков сервиса", () -> {
            when(instrumentService.getShares(any(), any(), any(), any(), any())).thenReturn(testShares);
            when(instrumentService.saveShares(any(ShareFilterDto.class))).thenReturn(shareSaveResponse);
            when(instrumentService.getFutures(any(), any(), any(), any(), any())).thenReturn(testFutures);
            when(instrumentService.saveFutures(any(FutureFilterDto.class))).thenReturn(futureSaveResponse);
            when(instrumentService.getIndicatives(any(), any(), any(), any())).thenReturn(testIndicatives);
            when(instrumentService.saveIndicatives(any(IndicativeFilterDto.class))).thenReturn(indicativeSaveResponse);
        });

        // Шаг 4: Выполнение метода планировщика
        Allure.step("Выполнение метода планировщика", () -> {
            assertDoesNotThrow(() -> schedulerService.preloadAndPersistInstruments());
        });

        // Шаг 5: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса", () -> {
            verify(instrumentService, times(1)).getShares(null, "moex_mrng_evng_e_wknd_dlr", null, null, null);
            verify(instrumentService, times(1)).saveShares(any(ShareFilterDto.class));
            verify(instrumentService, times(1)).getFutures(null, null, null, null, null);
            verify(instrumentService, times(1)).saveFutures(any(FutureFilterDto.class));
            verify(instrumentService, times(1)).getIndicatives(null, null, null, null);
            verify(instrumentService, times(1)).saveIndicatives(any(IndicativeFilterDto.class));
        });
    }

    @Test
    @DisplayName("Успешное выполнение с пустыми результатами")
    @Description("Тест проверяет корректную обработку ситуации, когда API возвращает пустые списки инструментов")
    @Story("Граничные случаи")
    @Severity(SeverityLevel.NORMAL)
    void preloadAndPersistInstruments_ShouldHandleEmptyResults_WhenNoInstrumentsFound() {
        
        // Шаг 1: Создание пустого ответа сервиса
        SaveResponseDto emptySaveResponse = Allure.step("Создание пустого ответа сервиса", () -> {
            return createSuccessSaveResponse("Нет новых инструментов для сохранения", 0, 0, 0, 0, 0);
        });

        // Шаг 2: Настройка моков сервиса для пустых результатов
        Allure.step("Настройка моков сервиса для пустых результатов", () -> {
            when(instrumentService.getShares(any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());
            when(instrumentService.saveShares(any(ShareFilterDto.class))).thenReturn(emptySaveResponse);
            when(instrumentService.getFutures(any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());
            when(instrumentService.saveFutures(any(FutureFilterDto.class))).thenReturn(emptySaveResponse);
            when(instrumentService.getIndicatives(any(), any(), any(), any())).thenReturn(Collections.emptyList());
            when(instrumentService.saveIndicatives(any(IndicativeFilterDto.class))).thenReturn(emptySaveResponse);
        });

        // Шаг 3: Выполнение метода планировщика
        Allure.step("Выполнение метода планировщика", () -> {
            assertDoesNotThrow(() -> schedulerService.preloadAndPersistInstruments());
        });

        // Шаг 4: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса", () -> {
            verify(instrumentService, times(1)).getShares(any(), any(), any(), any(), any());
            verify(instrumentService, times(1)).saveShares(any(ShareFilterDto.class));
            verify(instrumentService, times(1)).getFutures(any(), any(), any(), any(), any());
            verify(instrumentService, times(1)).saveFutures(any(FutureFilterDto.class));
            verify(instrumentService, times(1)).getIndicatives(any(), any(), any(), any());
            verify(instrumentService, times(1)).saveIndicatives(any(IndicativeFilterDto.class));
        });
    }

    @Test
    @DisplayName("Успешное выполнение с частичными результатами")
    @Description("Тест проверяет корректную обработку ситуации, когда некоторые типы инструментов возвращают данные, а другие - нет")
    @Story("Граничные случаи")
    @Severity(SeverityLevel.NORMAL)
    void preloadAndPersistInstruments_ShouldHandlePartialResults_WhenSomeTypesEmpty() {
        
        // Шаг 1: Подготовка тестовых данных с частичными результатами
        List<ShareDto> testShares = Allure.step("Подготовка тестовых данных для акций", () -> {
            return createTestShares();
        });
        
        List<FutureDto> testFutures = Allure.step("Подготовка пустого списка фьючерсов", () -> {
            return Collections.emptyList();
        });
        
        List<IndicativeDto> testIndicatives = Allure.step("Подготовка тестовых данных для индикативов", () -> {
            return createTestIndicatives();
        });

        // Шаг 2: Создание ответов сервисов
        SaveResponseDto shareSaveResponse = Allure.step("Создание ответа сервиса для акций", () -> {
            return createSuccessSaveResponse("Акции сохранены", 5, 3, 2, 0, 0);
        });
        
        SaveResponseDto futureSaveResponse = Allure.step("Создание пустого ответа сервиса для фьючерсов", () -> {
            return createSuccessSaveResponse("Нет фьючерсов", 0, 0, 0, 0, 0);
        });
        
        SaveResponseDto indicativeSaveResponse = Allure.step("Создание ответа сервиса для индикативов", () -> {
            return createSuccessSaveResponse("Индикативы сохранены", 3, 1, 2, 0, 0);
        });

        // Шаг 3: Настройка моков сервиса
        Allure.step("Настройка моков сервиса", () -> {
            when(instrumentService.getShares(any(), any(), any(), any(), any())).thenReturn(testShares);
            when(instrumentService.saveShares(any(ShareFilterDto.class))).thenReturn(shareSaveResponse);
            when(instrumentService.getFutures(any(), any(), any(), any(), any())).thenReturn(testFutures);
            when(instrumentService.saveFutures(any(FutureFilterDto.class))).thenReturn(futureSaveResponse);
            when(instrumentService.getIndicatives(any(), any(), any(), any())).thenReturn(testIndicatives);
            when(instrumentService.saveIndicatives(any(IndicativeFilterDto.class))).thenReturn(indicativeSaveResponse);
        });

        // Шаг 4: Выполнение метода планировщика
        Allure.step("Выполнение метода планировщика", () -> {
            assertDoesNotThrow(() -> schedulerService.preloadAndPersistInstruments());
        });

        // Шаг 5: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса", () -> {
            verify(instrumentService, times(1)).getShares(any(), any(), any(), any(), any());
            verify(instrumentService, times(1)).saveShares(any(ShareFilterDto.class));
            verify(instrumentService, times(1)).getFutures(any(), any(), any(), any(), any());
            verify(instrumentService, times(1)).saveFutures(any(FutureFilterDto.class));
            verify(instrumentService, times(1)).getIndicatives(any(), any(), any(), any());
            verify(instrumentService, times(1)).saveIndicatives(any(IndicativeFilterDto.class));
        });
    }

    // ========== ТЕСТЫ ОШИБОК ==========

    @Test
    @DisplayName("Обработка ошибки при загрузке акций")
    @Description("Тест проверяет корректную обработку исключения при загрузке акций из API")
    @Story("Обработка ошибок")
    @Severity(SeverityLevel.CRITICAL)
    void preloadAndPersistInstruments_ShouldHandleSharesError_WhenSharesServiceFails() {
        
        // Шаг 1: Настройка моков сервиса для ошибки загрузки акций
        Allure.step("Настройка моков сервиса для ошибки загрузки акций", () -> {
            when(instrumentService.getShares(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Ошибка загрузки акций"));
        });

        // Шаг 2: Выполнение метода планировщика и проверка отсутствия исключений
        Allure.step("Выполнение метода планировщика и проверка отсутствия исключений", () -> {
            assertDoesNotThrow(() -> schedulerService.preloadAndPersistInstruments());
        });
        
        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса", () -> {
            verify(instrumentService, times(1)).getShares(any(), any(), any(), any(), any());
            verify(instrumentService, never()).saveShares(any(ShareFilterDto.class));
            verify(instrumentService, never()).getFutures(any(), any(), any(), any(), any());
            verify(instrumentService, never()).saveFutures(any(FutureFilterDto.class));
            verify(instrumentService, never()).getIndicatives(any(), any(), any(), any());
            verify(instrumentService, never()).saveIndicatives(any(IndicativeFilterDto.class));
        });
    }

    @Test
    @DisplayName("Обработка ошибки при сохранении фьючерсов")
    @Description("Тест проверяет корректную обработку исключения при сохранении фьючерсов в БД")
    @Story("Обработка ошибок")
    @Severity(SeverityLevel.CRITICAL)
    void preloadAndPersistInstruments_ShouldHandleFuturesSaveError_WhenFuturesSaveFails() {
        
        // Шаг 1: Подготовка тестовых данных
        List<ShareDto> testShares = Allure.step("Подготовка тестовых данных для акций", () -> {
            return createTestShares();
        });
        
        List<FutureDto> testFutures = Allure.step("Подготовка тестовых данных для фьючерсов", () -> {
            return createTestFutures();
        });

        SaveResponseDto shareSaveResponse = Allure.step("Создание ответа сервиса для акций", () -> {
            return createSuccessSaveResponse("Акции сохранены", 5, 3, 2, 0, 0);
        });

        // Шаг 2: Настройка моков сервиса с ошибкой сохранения фьючерсов
        Allure.step("Настройка моков сервиса с ошибкой сохранения фьючерсов", () -> {
            when(instrumentService.getShares(any(), any(), any(), any(), any())).thenReturn(testShares);
            when(instrumentService.saveShares(any(ShareFilterDto.class))).thenReturn(shareSaveResponse);
            when(instrumentService.getFutures(any(), any(), any(), any(), any())).thenReturn(testFutures);
            when(instrumentService.saveFutures(any(FutureFilterDto.class)))
                .thenThrow(new RuntimeException("Ошибка сохранения фьючерсов"));
        });

        // Шаг 3: Выполнение метода планировщика и проверка отсутствия исключений
        Allure.step("Выполнение метода планировщика и проверка отсутствия исключений", () -> {
            assertDoesNotThrow(() -> schedulerService.preloadAndPersistInstruments());
        });
        
        // Шаг 4: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса", () -> {
            verify(instrumentService, times(1)).getShares(any(), any(), any(), any(), any());
            verify(instrumentService, times(1)).saveShares(any(ShareFilterDto.class));
            verify(instrumentService, times(1)).getFutures(any(), any(), any(), any(), any());
            verify(instrumentService, times(1)).saveFutures(any(FutureFilterDto.class));
            // После ошибки сохранения фьючерсов, код не продолжает выполнение
            verify(instrumentService, never()).getIndicatives(any(), any(), any(), any());
            verify(instrumentService, never()).saveIndicatives(any(IndicativeFilterDto.class));
        });
    }

    @Test
    @DisplayName("Обработка ошибки при загрузке индикативов")
    @Description("Тест проверяет корректную обработку исключения при загрузке индикативных инструментов")
    @Story("Обработка ошибок")
    @Severity(SeverityLevel.CRITICAL)
    void preloadAndPersistInstruments_ShouldHandleIndicativesError_WhenIndicativesServiceFails() {
        
        // Шаг 1: Подготовка тестовых данных
        List<ShareDto> testShares = Allure.step("Подготовка тестовых данных для акций", () -> {
            return createTestShares();
        });
        
        List<FutureDto> testFutures = Allure.step("Подготовка тестовых данных для фьючерсов", () -> {
            return createTestFutures();
        });

        SaveResponseDto shareSaveResponse = Allure.step("Создание ответа сервиса для акций", () -> {
            return createSuccessSaveResponse("Акции сохранены", 5, 3, 2, 0, 0);
        });
        
        SaveResponseDto futureSaveResponse = Allure.step("Создание ответа сервиса для фьючерсов", () -> {
            return createSuccessSaveResponse("Фьючерсы сохранены", 3, 1, 2, 0, 0);
        });

        // Шаг 2: Настройка моков сервиса с ошибкой загрузки индикативов
        Allure.step("Настройка моков сервиса с ошибкой загрузки индикативов", () -> {
            when(instrumentService.getShares(any(), any(), any(), any(), any())).thenReturn(testShares);
            when(instrumentService.saveShares(any(ShareFilterDto.class))).thenReturn(shareSaveResponse);
            when(instrumentService.getFutures(any(), any(), any(), any(), any())).thenReturn(testFutures);
            when(instrumentService.saveFutures(any(FutureFilterDto.class))).thenReturn(futureSaveResponse);
            when(instrumentService.getIndicatives(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Ошибка загрузки индикативов"));
        });

        // Шаг 3: Выполнение метода планировщика и проверка отсутствия исключений
        Allure.step("Выполнение метода планировщика и проверка отсутствия исключений", () -> {
            assertDoesNotThrow(() -> schedulerService.preloadAndPersistInstruments());
        });
        
        // Шаг 4: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса", () -> {
            verify(instrumentService, times(1)).getShares(any(), any(), any(), any(), any());
            verify(instrumentService, times(1)).saveShares(any(ShareFilterDto.class));
            verify(instrumentService, times(1)).getFutures(any(), any(), any(), any(), any());
            verify(instrumentService, times(1)).saveFutures(any(FutureFilterDto.class));
            verify(instrumentService, times(1)).getIndicatives(any(), any(), any(), any());
            verify(instrumentService, never()).saveIndicatives(any(IndicativeFilterDto.class));
        });
    }

    @Test
    @DisplayName("Обработка множественных ошибок")
    @Description("Тест проверяет корректную обработку ситуации, когда несколько сервисов возвращают ошибки")
    @Story("Обработка ошибок")
    @Severity(SeverityLevel.CRITICAL)
    void preloadAndPersistInstruments_ShouldHandleMultipleErrors_WhenMultipleServicesFail() {
        
        // Шаг 1: Настройка моков сервиса для ошибки загрузки акций
        Allure.step("Настройка моков сервиса для ошибки загрузки акций", () -> {
            when(instrumentService.getShares(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Ошибка загрузки акций"));
        });

        // Шаг 2: Выполнение метода планировщика и проверка отсутствия исключений
        Allure.step("Выполнение метода планировщика и проверка отсутствия исключений", () -> {
            assertDoesNotThrow(() -> schedulerService.preloadAndPersistInstruments());
        });
        
        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса", () -> {
            verify(instrumentService, times(1)).getShares(any(), any(), any(), any(), any());
            verify(instrumentService, never()).saveShares(any(ShareFilterDto.class));
            // После ошибки загрузки акций, код не продолжает выполнение
            verify(instrumentService, never()).getFutures(any(), any(), any(), any(), any());
            verify(instrumentService, never()).saveFutures(any(FutureFilterDto.class));
            verify(instrumentService, never()).getIndicatives(any(), any(), any(), any());
            verify(instrumentService, never()).saveIndicatives(any(IndicativeFilterDto.class));
        });
    }

    // ========== ГРАНИЧНЫЕ СЛУЧАИ ==========

    @Test
    @DisplayName("Обработка null результатов от сервисов")
    @Description("Тест проверяет корректную обработку ситуации, когда сервисы возвращают null вместо списков")
    @Story("Граничные случаи")
    @Severity(SeverityLevel.NORMAL)
    void preloadAndPersistInstruments_ShouldHandleNullResults_WhenServicesReturnNull() {
        
        // Шаг 1: Настройка моков сервиса для возврата null
        Allure.step("Настройка моков сервиса для возврата null", () -> {
            when(instrumentService.getShares(any(), any(), any(), any(), any())).thenReturn(null);
        });

        // Шаг 2: Выполнение метода планировщика и проверка отсутствия исключений
        Allure.step("Выполнение метода планировщика и проверка отсутствия исключений", () -> {
            // Код падает с NullPointerException при попытке вызвать .size() на null
            assertDoesNotThrow(() -> schedulerService.preloadAndPersistInstruments());
        });
        
        // Шаг 3: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса", () -> {
            verify(instrumentService, times(1)).getShares(any(), any(), any(), any(), any());
            // После NullPointerException код не продолжает выполнение
            verify(instrumentService, never()).saveShares(any(ShareFilterDto.class));
            verify(instrumentService, never()).getFutures(any(), any(), any(), any(), any());
            verify(instrumentService, never()).saveFutures(any(FutureFilterDto.class));
            verify(instrumentService, never()).getIndicatives(any(), any(), any(), any());
            verify(instrumentService, never()).saveIndicatives(any(IndicativeFilterDto.class));
        });
    }

    @Test
    @DisplayName("Обработка больших объемов данных")
    @Description("Тест проверяет корректную обработку ситуации с большим количеством инструментов")
    @Story("Граничные случаи")
    @Severity(SeverityLevel.NORMAL)
    void preloadAndPersistInstruments_ShouldHandleLargeDataVolumes_WhenManyInstrumentsReturned() {
        
        // Шаг 1: Подготовка больших объемов тестовых данных
        List<ShareDto> largeSharesList = Allure.step("Подготовка большого списка акций", () -> {
            return createLargeSharesList(1000);
        });
        
        List<FutureDto> largeFuturesList = Allure.step("Подготовка большого списка фьючерсов", () -> {
            return createLargeFuturesList(500);
        });
        
        List<IndicativeDto> largeIndicativesList = Allure.step("Подготовка большого списка индикативов", () -> {
            return createLargeIndicativesList(200);
        });

        SaveResponseDto shareSaveResponse = Allure.step("Создание ответа сервиса для большого количества акций", () -> {
            return createSuccessSaveResponse("Много акций сохранено", 1000, 800, 200, 0, 0);
        });
        
        SaveResponseDto futureSaveResponse = Allure.step("Создание ответа сервиса для большого количества фьючерсов", () -> {
            return createSuccessSaveResponse("Много фьючерсов сохранено", 500, 300, 200, 0, 0);
        });
        
        SaveResponseDto indicativeSaveResponse = Allure.step("Создание ответа сервиса для большого количества индикативов", () -> {
            return createSuccessSaveResponse("Много индикативов сохранено", 200, 150, 50, 0, 0);
        });

        // Шаг 2: Настройка моков сервиса
        Allure.step("Настройка моков сервиса", () -> {
            when(instrumentService.getShares(any(), any(), any(), any(), any())).thenReturn(largeSharesList);
            when(instrumentService.saveShares(any(ShareFilterDto.class))).thenReturn(shareSaveResponse);
            when(instrumentService.getFutures(any(), any(), any(), any(), any())).thenReturn(largeFuturesList);
            when(instrumentService.saveFutures(any(FutureFilterDto.class))).thenReturn(futureSaveResponse);
            when(instrumentService.getIndicatives(any(), any(), any(), any())).thenReturn(largeIndicativesList);
            when(instrumentService.saveIndicatives(any(IndicativeFilterDto.class))).thenReturn(indicativeSaveResponse);
        });

        // Шаг 3: Выполнение метода планировщика
        Allure.step("Выполнение метода планировщика", () -> {
            assertDoesNotThrow(() -> schedulerService.preloadAndPersistInstruments());
        });

        // Шаг 4: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса", () -> {
            verify(instrumentService, times(1)).getShares(any(), any(), any(), any(), any());
            verify(instrumentService, times(1)).saveShares(any(ShareFilterDto.class));
            verify(instrumentService, times(1)).getFutures(any(), any(), any(), any(), any());
            verify(instrumentService, times(1)).saveFutures(any(FutureFilterDto.class));
            verify(instrumentService, times(1)).getIndicatives(any(), any(), any(), any());
            verify(instrumentService, times(1)).saveIndicatives(any(IndicativeFilterDto.class));
        });
    }

    @Test
    @DisplayName("Обработка некорректных ответов от сервисов")
    @Description("Тест проверяет корректную обработку ситуации, когда сервисы возвращают некорректные данные")
    @Story("Граничные случаи")
    @Severity(SeverityLevel.NORMAL)
    void preloadAndPersistInstruments_ShouldHandleInvalidResponses_WhenServicesReturnInvalidData() {
        
        // Шаг 1: Подготовка тестовых данных с некорректными полями
        List<ShareDto> sharesWithNullFields = Allure.step("Подготовка акций с null полями", () -> {
            return createSharesWithNullFields();
        });
        
        List<FutureDto> futuresWithNullFields = Allure.step("Подготовка фьючерсов с null полями", () -> {
            return createFuturesWithNullFields();
        });
        
        List<IndicativeDto> indicativesWithNullFields = Allure.step("Подготовка индикативов с null полями", () -> {
            return createIndicativesWithNullFields();
        });

        SaveResponseDto saveResponse = Allure.step("Создание ответа сервиса с предупреждениями", () -> {
            return createSuccessSaveResponse("Обработано с предупреждениями", 5, 3, 2, 0, 0);
        });

        // Шаг 2: Настройка моков сервиса
        Allure.step("Настройка моков сервиса", () -> {
            when(instrumentService.getShares(any(), any(), any(), any(), any())).thenReturn(sharesWithNullFields);
            when(instrumentService.saveShares(any(ShareFilterDto.class))).thenReturn(saveResponse);
            when(instrumentService.getFutures(any(), any(), any(), any(), any())).thenReturn(futuresWithNullFields);
            when(instrumentService.saveFutures(any(FutureFilterDto.class))).thenReturn(saveResponse);
            when(instrumentService.getIndicatives(any(), any(), any(), any())).thenReturn(indicativesWithNullFields);
            when(instrumentService.saveIndicatives(any(IndicativeFilterDto.class))).thenReturn(saveResponse);
        });

        // Шаг 3: Выполнение метода планировщика и проверка отсутствия исключений
        Allure.step("Выполнение метода планировщика и проверка отсутствия исключений", () -> {
            assertDoesNotThrow(() -> schedulerService.preloadAndPersistInstruments());
        });
        
        // Шаг 4: Проверка вызовов сервиса
        Allure.step("Проверка вызовов сервиса", () -> {
            verify(instrumentService, times(1)).getShares(any(), any(), any(), any(), any());
            verify(instrumentService, times(1)).saveShares(any(ShareFilterDto.class));
            verify(instrumentService, times(1)).getFutures(any(), any(), any(), any(), any());
            verify(instrumentService, times(1)).saveFutures(any(FutureFilterDto.class));
            verify(instrumentService, times(1)).getIndicatives(any(), any(), any(), any());
            verify(instrumentService, times(1)).saveIndicatives(any(IndicativeFilterDto.class));
        });
    }

    // ========== ТЕСТЫ ПОВЕДЕНИЯ ПЛАНИРОВЩИКА ==========

    @Test
    @DisplayName("Проверка правильности фильтров для акций")
    @Description("Тест проверяет, что для акций используется правильный фильтр с биржей MOEX")
    @Story("Поведение планировщика")
    @Severity(SeverityLevel.CRITICAL)
    void preloadAndPersistInstruments_ShouldUseCorrectShareFilter_WhenLoadingShares() {
        
        // Шаг 1: Подготовка тестовых данных
        List<ShareDto> testShares = Allure.step("Подготовка тестовых данных для акций", () -> {
            return createTestShares();
        });
        
        SaveResponseDto saveResponse = Allure.step("Создание ответа сервиса", () -> {
            return createSuccessSaveResponse("Акции сохранены", 5, 3, 2, 0, 0);
        });

        // Шаг 2: Настройка моков сервиса
        Allure.step("Настройка моков сервиса", () -> {
            when(instrumentService.getShares(any(), any(), any(), any(), any())).thenReturn(testShares);
            when(instrumentService.saveShares(any(ShareFilterDto.class))).thenReturn(saveResponse);
            when(instrumentService.getFutures(any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());
            when(instrumentService.saveFutures(any(FutureFilterDto.class))).thenReturn(saveResponse);
            when(instrumentService.getIndicatives(any(), any(), any(), any())).thenReturn(Collections.emptyList());
            when(instrumentService.saveIndicatives(any(IndicativeFilterDto.class))).thenReturn(saveResponse);
        });

        // Шаг 3: Выполнение метода планировщика
        Allure.step("Выполнение метода планировщика", () -> {
            schedulerService.preloadAndPersistInstruments();
        });

        // Шаг 4: Проверка правильности фильтров
        Allure.step("Проверка правильности фильтров", () -> {
            verify(instrumentService, times(1)).getShares(null, "moex_mrng_evng_e_wknd_dlr", null, null, null);
            verify(instrumentService, times(1)).saveShares(argThat(filter -> 
                filter.getExchange().equals("moex_mrng_evng_e_wknd_dlr") &&
                filter.getStatus() == null &&
                filter.getCurrency() == null &&
                filter.getTicker() == null &&
                filter.getFigi() == null
            ));
        });
    }

    @Test
    @DisplayName("Проверка правильности фильтров для фьючерсов")
    @Description("Тест проверяет, что для фьючерсов используется фильтр без ограничений")
    @Story("Поведение планировщика")
    @Severity(SeverityLevel.CRITICAL)
    void preloadAndPersistInstruments_ShouldUseCorrectFutureFilter_WhenLoadingFutures() {
        
        // Шаг 1: Подготовка тестовых данных
        List<FutureDto> testFutures = Allure.step("Подготовка тестовых данных для фьючерсов", () -> {
            return createTestFutures();
        });
        
        SaveResponseDto saveResponse = Allure.step("Создание ответа сервиса", () -> {
            return createSuccessSaveResponse("Фьючерсы сохранены", 5, 3, 2, 0, 0);
        });

        // Шаг 2: Настройка моков сервиса
        Allure.step("Настройка моков сервиса", () -> {
            when(instrumentService.getShares(any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());
            when(instrumentService.saveShares(any(ShareFilterDto.class))).thenReturn(saveResponse);
            when(instrumentService.getFutures(any(), any(), any(), any(), any())).thenReturn(testFutures);
            when(instrumentService.saveFutures(any(FutureFilterDto.class))).thenReturn(saveResponse);
            when(instrumentService.getIndicatives(any(), any(), any(), any())).thenReturn(Collections.emptyList());
            when(instrumentService.saveIndicatives(any(IndicativeFilterDto.class))).thenReturn(saveResponse);
        });

        // Шаг 3: Выполнение метода планировщика
        Allure.step("Выполнение метода планировщика", () -> {
            schedulerService.preloadAndPersistInstruments();
        });

        // Шаг 4: Проверка правильности фильтров
        Allure.step("Проверка правильности фильтров", () -> {
            verify(instrumentService, times(1)).getFutures(null, null, null, null, null);
            verify(instrumentService, times(1)).saveFutures(argThat(filter -> 
                filter.getStatus() == null &&
                filter.getExchange() == null &&
                filter.getCurrency() == null &&
                filter.getTicker() == null &&
                filter.getAssetType() == null
            ));
        });
    }

    @Test
    @DisplayName("Проверка правильности фильтров для индикативов")
    @Description("Тест проверяет, что для индикативов используется фильтр без ограничений")
    @Story("Поведение планировщика")
    @Severity(SeverityLevel.CRITICAL)
    void preloadAndPersistInstruments_ShouldUseCorrectIndicativeFilter_WhenLoadingIndicatives() {
        
        // Шаг 1: Подготовка тестовых данных
        List<IndicativeDto> testIndicatives = Allure.step("Подготовка тестовых данных для индикативов", () -> {
            return createTestIndicatives();
        });
        
        SaveResponseDto saveResponse = Allure.step("Создание ответа сервиса", () -> {
            return createSuccessSaveResponse("Индикативы сохранены", 5, 3, 2, 0, 0);
        });

        // Шаг 2: Настройка моков сервиса
        Allure.step("Настройка моков сервиса", () -> {
            when(instrumentService.getShares(any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());
            when(instrumentService.saveShares(any(ShareFilterDto.class))).thenReturn(saveResponse);
            when(instrumentService.getFutures(any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());
            when(instrumentService.saveFutures(any(FutureFilterDto.class))).thenReturn(saveResponse);
            when(instrumentService.getIndicatives(any(), any(), any(), any())).thenReturn(testIndicatives);
            when(instrumentService.saveIndicatives(any(IndicativeFilterDto.class))).thenReturn(saveResponse);
        });

        // Шаг 3: Выполнение метода планировщика
        Allure.step("Выполнение метода планировщика", () -> {
            schedulerService.preloadAndPersistInstruments();
        });

        // Шаг 4: Проверка правильности фильтров
        Allure.step("Проверка правильности фильтров", () -> {
            verify(instrumentService, times(1)).getIndicatives(null, null, null, null);
            verify(instrumentService, times(1)).saveIndicatives(argThat(filter -> 
                filter.getExchange() == null &&
                filter.getCurrency() == null &&
                filter.getTicker() == null &&
                filter.getFigi() == null
            ));
        });
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private List<ShareDto> createTestShares() {
        return Arrays.asList(
            new ShareDto("BBG004730N88", "SBER", "Сбербанк", "RUB", "moex_mrng_evng_e_wknd_dlr", "Financials", "SECURITY_TRADING_STATUS_NORMAL_TRADING", true, "test-asset-uid-sber", new BigDecimal("0.01"), 1),      
            new ShareDto("BBG004730ZJ9", "GAZP", "Газпром", "RUB", "moex_mrng_evng_e_wknd_dlr", "Energy", "SECURITY_TRADING_STATUS_NORMAL_TRADING", true, "test-asset-uid-gazp", new BigDecimal("0.01"), 1), 
            new ShareDto("BBG004730N88", "LKOH", "Лукойл", "RUB", "moex_mrng_evng_e_wknd_dlr", "Energy", "SECURITY_TRADING_STATUS_NORMAL_TRADING", true, "test-asset-uid-lkoh", new BigDecimal("0.01"), 1)
        );
    }

    private List<FutureDto> createTestFutures() {
        return Arrays.asList(
            new FutureDto("FUTSBER0324", "SBER-3.24", "COMMODITY", "SBER", "RUB", "moex", true, LocalDateTime.of(2024, 3, 15, 18, 45), new BigDecimal("0.01"), 1),
            new FutureDto("FUTGAZP0324", "GAZP-3.24", "COMMODITY", "GAZP", "RUB", "moex", true, LocalDateTime.of(2024, 3, 15, 18, 45), new BigDecimal("0.01"), 1),
            new FutureDto("FUTLKOH0324", "LKOH-3.24", "COMMODITY", "LKOH", "RUB", "moex", true, LocalDateTime.of(2024, 3, 15, 18, 45), new BigDecimal("0.01"), 1)
        );
    }

    private List<IndicativeDto> createTestIndicatives() {
        return Arrays.asList(
            new IndicativeDto("BBG00M5X5S47", "IMOEX", "Индекс МосБиржи", "RUB", "moex", "SPBXM", "imoex", true, true),
            new IndicativeDto("BBG00M5X5S48", "RTSI", "Индекс РТС", "RUB", "moex", "SPBXM", "rtsi", true, true)
        );
    }

    private List<ShareDto> createLargeSharesList(int count) {
        return Arrays.asList(new ShareDto[count]).stream()
            .map(i -> new ShareDto("BBG" + i, "TICKER" + i, "Name" + i, "RUB", "moex_mrng_evng_e_wknd_dlr", "Sector", "STATUS", true, "test-asset-uid-" + i, new BigDecimal("0.01"), 1))
            .toList();
    }

    private List<FutureDto> createLargeFuturesList(int count) {
        return Arrays.asList(new FutureDto[count]).stream()
            .map(i -> new FutureDto("FUT" + i, "TICKER" + i, "COMMODITY", "ASSET" + i, "RUB", "moex", true, LocalDateTime.of(2024, 3, 15, 18, 45), new BigDecimal("0.01"), 1))
            .toList();
    }

    private List<IndicativeDto> createLargeIndicativesList(int count) {
        return Arrays.asList(new IndicativeDto[count]).stream()
            .map(i -> new IndicativeDto("BBG" + i, "TICKER" + i, "Name" + i, "RUB", "moex", "CLASS", "uid" + i, true, true))
            .toList();
    }

    private List<ShareDto> createSharesWithNullFields() {
        return Arrays.asList(
            new ShareDto(null, "SBER", "Сбербанк", "RUB", "moex_mrng_evng_e_wknd_dlr", "Financials", "STATUS", true, "test-asset-uid-sber", new BigDecimal("0.01"), 1),
            new ShareDto("BBG004730N88", null, "Газпром", "RUB", "moex_mrng_evng_e_wknd_dlr", "Energy", "STATUS", true, "test-asset-uid-gazp", new BigDecimal("0.01"), 1),
            new ShareDto("BBG004730N88", "LKOH", null, "RUB", "moex_mrng_evng_e_wknd_dlr", "Energy", "STATUS", true, "test-asset-uid-lkoh", new BigDecimal("0.01"), 1)
        );
    }

    private List<FutureDto> createFuturesWithNullFields() {
        return Arrays.asList(
            new FutureDto(null, "SBER-3.24", "COMMODITY", "SBER", "RUB", "moex", true, LocalDateTime.of(2024, 3, 15, 18, 45), new BigDecimal("0.01"), 1),
            new FutureDto("FUTGAZP0324", null, "COMMODITY", "GAZP", "RUB", "moex", true, LocalDateTime.of(2024, 3, 15, 18, 45), new BigDecimal("0.01"), 1),
            new FutureDto("FUTLKOH0324", "LKOH-3.24", null, "LKOH", "RUB", "moex", true, LocalDateTime.of(2024, 3, 15, 18, 45), new BigDecimal("0.01"), 1)
        );
    }

    private List<IndicativeDto> createIndicativesWithNullFields() {
        return Arrays.asList(
            new IndicativeDto(null, "IMOEX", "Индекс МосБиржи", "RUB", "moex", "SPBXM", "imoex", true, true),
            new IndicativeDto("BBG00M5X5S48", null, "Индекс РТС", "RUB", "moex", "SPBXM", "rtsi", true, true),
            new IndicativeDto("BBG00M5X5S49", "RTSI", null, "RUB", "moex", "SPBXM", "rtsi", true, true)
        );
    }

    private SaveResponseDto createSuccessSaveResponse(String message, int totalRequested, int newItemsSaved, 
                                                     int existingItemsSkipped, int invalidItemsFiltered, int missingFromApi) {
        SaveResponseDto response = new SaveResponseDto();
        response.setSuccess(true);
        response.setMessage(message);
        response.setTotalRequested(totalRequested);
        response.setNewItemsSaved(newItemsSaved);
        response.setExistingItemsSkipped(existingItemsSkipped);
        response.setInvalidItemsFiltered(invalidItemsFiltered);
        response.setMissingFromApi(missingFromApi);
        response.setSavedItems(Collections.emptyList());
        return response;
    }
}
