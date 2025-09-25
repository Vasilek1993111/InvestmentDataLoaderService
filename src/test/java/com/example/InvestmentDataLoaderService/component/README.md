# Компонентные тесты (Component Tests)

## Описание

Компонентные тесты проверяют взаимодействие между компонентами системы без полной интеграции с внешними системами. Они находятся между unit-тестами и интеграционными тестами в пирамиде тестирования.

## Архитектура

```
┌─────────────────────────────────────────────────────────────┐
│                InstrumentsController                        │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              InstrumentService                          │ │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────┐ │ │
│  │  │ TinkoffRestClient│  │ ShareRepository │  │ FutureRepository│ │ │
│  │  └─────────────────┘  └─────────────────┘  └─────────┘ │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Принципы тестирования

### 1. **Мокирование зависимостей**
- Все внешние зависимости мокируются
- Фокус на логике взаимодействия между компонентами
- Быстрое выполнение тестов

### 2. **Проверка взаимодействия**
- Правильность делегирования вызовов
- Консистентность данных в процессе обработки
- Обработка ошибок и граничных случаев

### 3. **Полный workflow**
- Тестирование последовательности операций
- Проверка целостности данных между этапами
- Валидация бизнес-логики

## Структура тестов

### InstrumentsComponentTest

**Назначение:** Проверка взаимодействия между `InstrumentsController` и `InstrumentService`

**Тестируемые сценарии:**

#### 1. **Workflow тесты** (3 теста)
- `shouldIntegrateSharesWorkflow_WhenAllComponentsWork` - полный workflow для акций
- `shouldIntegrateFuturesWorkflow_WhenAllComponentsWork` - полный workflow для фьючерсов  
- `shouldIntegrateIndicativesWorkflow_WhenAllComponentsWork` - полный workflow для индикативов

**Что проверяется:**
- Полный цикл: получение из API → сохранение → поиск по FIGI/тикеру
- Консистентность данных между операциями
- Правильность делегирования вызовов к `InstrumentService`

#### 2. **Обработка ошибок** (2 теста)
- `shouldHandleErrorPropagation_WhenInstrumentServiceFails` - пробрасывание исключений для акций
- `shouldHandleSaveError_WhenFutureSaveFails` - обработка ошибок при сохранении фьючерсов

**Что проверяется:**
- Исключения из `InstrumentService` корректно передаются наверх
- Система не "глотает" ошибки

#### 3. **Граничные случаи** (2 теста)
- `shouldHandleNullResults_WhenInstrumentServiceReturnsNull` - обработка null результатов
- `shouldHandleEmptyResults_WhenNoDataFound` - обработка пустых результатов

**Что проверяется:**
- Корректная обработка null и пустых результатов
- Система не падает при отсутствии данных

#### 4. **Консистентность данных** (2 теста)
- `shouldValidateDataConsistency_WhenWorkingWithSameData` - проверка целостности данных
- `shouldGetCorrectStatistics_WhenRequestingInstrumentCounts` - проверка статистики

**Что проверяется:**
- Данные не искажаются между операциями
- Сохраненные данные соответствуют исходным

### InstrumentsServiceComponentTest

**Назначение:** Проверка взаимодействия между `InstrumentService` и его зависимостями

**Тестируемые сценарии:**

#### 1. **База данных интеграция** (3 теста)
- `shouldIntegrateWithDatabase_WhenSearchingShareByTicker` - поиск акций по тикеру
- `shouldIntegrateWithDatabase_WhenSearchingFutureByTicker` - поиск фьючерсов по тикеру
- `shouldIntegrateWithDatabase_WhenFilteringShares` - фильтрация акций

**Что проверяется:**
- Корректное взаимодействие с репозиториями
- Правильность трансформации Entity в DTO
- Корректность фильтрации данных

#### 2. **Статистика** (1 тест)
- `shouldIntegrateWithAllRepositories_WhenGettingStatistics` - получение статистики

**Что проверяется:**
- Взаимодействие со всеми репозиториями
- Корректность агрегации данных

#### 3. **Обработка ошибок** (1 тест)
- `shouldHandleDatabaseError_WhenSearchingShares` - обработка ошибок БД

**Что проверяется:**
- Корректная обработка исключений от репозиториев

#### 4. **Граничные случаи** (2 теста)
- `shouldHandleNullDatabaseResults_WhenEntityNotFound` - обработка null результатов
- `shouldHandleEmptyDatabaseResults_WhenNoDataFound` - обработка пустых результатов

**Что проверяется:**
- Корректная обработка отсутствующих данных

#### 5. **Консистентность данных** (2 теста)
- `shouldValidateDataConsistency_WhenWorkingWithSameData` - проверка целостности
- `shouldTransformEntityToDtoCorrectly_WhenConvertingData` - проверка трансформации

**Что проверяется:**
- Данные не искажаются при преобразовании
- Правильность маппинга Entity → DTO

## Пример теста

```java
@Test
@DisplayName("Полный workflow для акций: API → Сохранение → Поиск")
@Description("Тестирует полный цикл работы с акциями через контроллер и сервис")
@Story("Shares Workflow Integration")
@Severity(SeverityLevel.CRITICAL)
void shouldIntegrateSharesWorkflow_WhenAllComponentsWork() {
    // Given - настраиваем моки для полного workflow
    ShareDto testShare = createTestShare();
    List<ShareDto> apiShares = Arrays.asList(testShare);
    ShareFilterDto filter = createShareFilter();
    SaveResponseDto saveResponse = new SaveResponseDto(true, "Акции сохранены", 1, 1, 0, 0, 0, null);

    when(instrumentService.getShares(...)).thenReturn(apiShares);
    when(instrumentService.saveShares(filter)).thenReturn(saveResponse);
    when(instrumentService.getShareByFigi(...)).thenReturn(testShare);

    // When - выполняем полный workflow
    ResponseEntity<List<ShareDto>> apiResponse = instrumentsController.getShares(...);
    ResponseEntity<SaveResponseDto> saveResult = instrumentsController.saveShares(filter);
    ResponseEntity<ShareDto> shareByFigi = instrumentsController.getShareByIdentifier(...);

    // Then - проверяем интеграцию
    assertThat(apiResponse.getBody()).hasSize(1);
    assertThat(saveResult.getBody().isSuccess()).isTrue();
    assertThat(shareByFigi.getBody()).isEqualTo(testShare);
    
    // Проверяем, что все методы были вызваны
    verify(instrumentService).getShares(...);
    verify(instrumentService).saveShares(filter);
    verify(instrumentService).getShareByFigi(...);
}
```

## Запуск тестов

```bash
# Запуск всех компонентных тестов
mvn test -Dtest="*ComponentTest"

# Запуск конкретного теста
mvn test -Dtest="InstrumentsComponentTest"

# Запуск с генерацией Allure отчетов
mvn test -Dtest="*ComponentTest" allure:report
```

## Отличия от других типов тестов

| Тип теста | Фокус | Зависимости | Скорость | Покрытие |
|-----------|-------|-------------|----------|----------|
| **Unit** | Отдельные методы/классы | Все мокируются | Очень быстро | Высокое |
| **Component** | Взаимодействие компонентов | Мокируются внешние | Быстро | Среднее |
| **Integration** | Полная интеграция | Реальные системы | Медленно | Низкое |

## Преимущества компонентных тестов

1. **Быстрое выполнение** - нет реальных внешних вызовов
2. **Стабильность** - не зависят от внешних систем
3. **Детальная проверка** - можно проверить все сценарии
4. **Легкость отладки** - четкое понимание, что тестируется
5. **Изоляция** - тесты не влияют друг на друга

## Рекомендации

1. **Используйте точные моки** - избегайте `anyString()`, используйте конкретные значения
2. **Проверяйте делегирование** - убеждайтесь, что вызовы дошли до зависимостей
3. **Тестируйте полный workflow** - не только отдельные вызовы
4. **Проверяйте консистентность** - данные не должны искажаться
5. **Обрабатывайте ошибки** - тестируйте граничные случаи

## Allure отчеты

Все тесты содержат полную разметку Allure:

- `@Epic` - группа тестов (Instruments Component Integration)
- `@Feature` - функциональность (Instruments Controller & Service Integration)
- `@Story` - пользовательская история (Shares Workflow Integration)
- `@DisplayName` - читаемое название теста
- `@Description` - подробное описание теста
- `@Severity` - уровень важности (CRITICAL, NORMAL, MINOR)
- `@Step` - шаги выполнения
- `@Owner` - автор тестов

Отчеты генерируются автоматически при запуске тестов и доступны в папке `target/allure-results/`.