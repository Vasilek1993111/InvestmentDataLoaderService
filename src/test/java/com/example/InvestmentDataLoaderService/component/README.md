# Компонентные тесты (Component Tests)

## Описание

Компонентные тесты проверяют взаимодействие между компонентами системы без полной интеграции с внешними системами. Они находятся между unit-тестами и интеграционными тестами в пирамиде тестирования.

## Архитектура

```
┌─────────────────────────────────────────────────────────────┐
│                    TInvestService                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │ InstrumentService│  │ MarketDataService│  │TradingService│ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
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

**Назначение:** Проверка взаимодействия между `TInvestService` и `InstrumentService`

**Тестируемые сценарии:**

#### 1. **Workflow тесты** (3 теста)
- `shouldIntegrateSharesWorkflow_WhenAllComponentsWork` - полный workflow для акций
- `shouldIntegrateFuturesWorkflow_WhenAllComponentsWork` - полный workflow для фьючерсов  
- `shouldIntegrateIndicativesWorkflow_WhenAllComponentsWork` - полный workflow для индикативов

**Что проверяется:**
- Полный цикл: получение из API → сохранение → поиск по FIGI/тикеру
- Консистентность данных между операциями
- Правильность делегирования вызовов к `InstrumentService`

#### 2. **Обработка ошибок** (1 тест)
- `shouldHandleErrorPropagation_WhenInstrumentServiceFails` - пробрасывание исключений

**Что проверяется:**
- Исключения из `InstrumentService` корректно передаются наверх
- Система не "глотает" ошибки

#### 3. **Граничные случаи** (2 теста)
- `shouldHandleNullResults_WhenInstrumentServiceReturnsNull` - обработка null результатов
- `shouldHandleEmptyResults_WhenNoDataFound` - обработка пустых результатов

**Что проверяется:**
- Корректная обработка null и пустых результатов
- Система не падает при отсутствии данных

#### 4. **Консистентность данных** (1 тест)
- `shouldValidateDataConsistency_WhenWorkingWithSameData` - проверка целостности данных

**Что проверяется:**
- Данные не искажаются между операциями
- Сохраненные данные соответствуют исходным

## Пример теста

```java
@Test
void shouldIntegrateSharesWorkflow_WhenAllComponentsWork() {
    // Given - настраиваем моки для полного workflow
    ShareDto testShare = new ShareDto(/* ... */);
    when(instrumentService.getShares(...)).thenReturn(Arrays.asList(testShare));
    when(instrumentService.saveShares(...)).thenReturn(saveResponse);
    when(instrumentService.getShareByFigi(...)).thenReturn(testShare);

    // When - выполняем полный workflow
    List<ShareDto> apiShares = tInvestService.getShares(...);        // 1. Получаем из API
    SaveResponseDto saveResult = tInvestService.saveShares(filter);  // 2. Сохраняем
    ShareDto shareByFigi = tInvestService.getShareByFigi(...);       // 3. Ищем по FIGI

    // Then - проверяем интеграцию
    assertThat(apiShares).hasSize(1);
    assertThat(saveResult.isSuccess()).isTrue();
    assertThat(shareByFigi).isEqualTo(testShare);
    
    // Проверяем, что все методы были вызваны
    verify(instrumentService).getShares(...);
    verify(instrumentService).saveShares(...);
    verify(instrumentService).getShareByFigi(...);
}
```

## Запуск тестов

```bash
# Запуск всех компонентных тестов
mvn test -Dtest="*ComponentTest"

# Запуск конкретного теста
mvn test -Dtest="InstrumentsComponentTest"
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
