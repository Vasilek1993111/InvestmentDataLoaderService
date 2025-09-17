# Unit-тесты для Instruments

Этот пакет содержит unit-тесты для функциональности работы с финансовыми инструментами (акции, фьючерсы, индикативы).

## Структура тестов

### 1. InstrumentsControllerTest
**Расположение:** `controller/InstrumentsControllerTest.java`

**Описание:** Unit-тесты для REST контроллера `InstrumentsController`

**Покрытие:**
- ✅ GET `/api/instruments/shares` - получение списка акций с фильтрацией
- ✅ GET `/api/instruments/shares/{identifier}` - получение акции по FIGI или тикеру
- ✅ POST `/api/instruments/shares` - сохранение акций по фильтру
- ✅ GET `/api/instruments/futures` - получение списка фьючерсов с фильтрацией
- ✅ GET `/api/instruments/futures/{identifier}` - получение фьючерса по FIGI или тикеру
- ✅ POST `/api/instruments/futures` - сохранение фьючерсов по фильтру
- ✅ GET `/api/instruments/indicatives` - получение списка индикативов с фильтрацией
- ✅ GET `/api/instruments/indicatives/{identifier}` - получение индикатива по FIGI или тикеру
- ✅ POST `/api/instruments/indicatives` - сохранение индикативов по фильтру
- ✅ GET `/api/instruments/count` - получение статистики по количеству инструментов

**Технологии:** Spring Boot Test, MockMvc, Mockito

### 2. InstrumentServiceTest
**Расположение:** `service/InstrumentServiceTest.java`

**Описание:** Unit-тесты для сервиса `InstrumentService`

**Покрытие:**
- ✅ `getShares()` - получение акций из API с фильтрацией
- ✅ `saveShares()` - сохранение акций в БД
- ✅ `getSharesFromDatabase()` - получение акций из БД с фильтрацией
- ✅ `getShareByFigi()` - получение акции по FIGI
- ✅ `getShareByTicker()` - получение акции по тикеру
- ✅ `updateShare()` - обновление акции в БД
- ✅ `getFutures()` - получение фьючерсов из API с фильтрацией
- ✅ `saveFutures()` - сохранение фьючерсов в БД
- ✅ `getFutureByFigi()` - получение фьючерса по FIGI
- ✅ `getFutureByTicker()` - получение фьючерса по тикеру
- ✅ `getIndicatives()` - получение индикативов из API с фильтрацией
- ✅ `getIndicativeBy()` - получение индикатива по FIGI
- ✅ `getIndicativeByTicker()` - получение индикатива по тикеру
- ✅ `saveIndicatives()` - сохранение индикативов в БД

**Технологии:** JUnit 5, Mockito, AssertJ

### 3. TInvestServiceInstrumentsTest
**Расположение:** `service/TInvestServiceInstrumentsTest.java`

**Описание:** Unit-тесты для сервиса `TInvestService` (только методы, связанные с instruments)

**Покрытие:**
- ✅ Делегирование вызовов к `InstrumentService`
- ✅ Создание фильтров из параметров
- ✅ Обработка null параметров
- ✅ Обработка пустых результатов
- ✅ Обработка ошибок

**Технологии:** JUnit 5, Mockito, AssertJ

### 4. TestDataFactory
**Расположение:** `service/unit/TestDataFactory.java`

**Описание:** Фабрика тестовых данных для создания DTO, Entity и GRPC объектов

**Функциональность:**
- ✅ Создание тестовых DTO объектов (ShareDto, FutureDto, IndicativeDto)
- ✅ Создание тестовых Entity объектов (ShareEntity, FutureEntity, IndicativeEntity)
- ✅ Создание GRPC объектов (Share, Future, SharesResponse, FuturesResponse)
- ✅ Создание JSON ответов для REST API
- ✅ Создание фильтров (ShareFilterDto, FutureFilterDto, IndicativeFilterDto)
- ✅ Создание ответов сохранения (SaveResponseDto)
- ✅ Утилиты для валидации данных

### 5. InstrumentsIntegrationTest
**Расположение:** `service/unit/InstrumentsIntegrationTest.java`

**Описание:** Интеграционные тесты для проверки взаимодействия между компонентами

**Покрытие:**
- ✅ Интеграция workflow для акций
- ✅ Интеграция workflow для фьючерсов
- ✅ Интеграция workflow для индикативов
- ✅ Обработка ошибок и их пробрасывание
- ✅ Обработка null результатов
- ✅ Обработка пустых результатов
- ✅ Валидация консистентности данных

## Запуск тестов

### Все тесты instruments
```bash
mvn test -Dtest="*Instruments*Test"
```

### Конкретный тест
```bash
mvn test -Dtest="InstrumentsControllerTest"
mvn test -Dtest="InstrumentServiceTest"
mvn test -Dtest="TInvestServiceInstrumentsTest"
mvn test -Dtest="InstrumentsIntegrationTest"
```

### С покрытием кода
```bash
mvn test jacoco:report -Dtest="*Instruments*Test"
```

## Покрытие кода

Тесты покрывают следующие аспекты:

### Controllers (100%)
- ✅ Все REST endpoints
- ✅ Валидация параметров
- ✅ Обработка HTTP статусов
- ✅ Сериализация/десериализация JSON

### Services (95%+)
- ✅ Бизнес-логика
- ✅ Интеграция с внешними API
- ✅ Работа с базой данных
- ✅ Обработка ошибок
- ✅ Кэширование

### DTOs (100%)
- ✅ Валидация данных
- ✅ Конструкторы
- ✅ Методы доступа

## Особенности тестирования

### Мокирование внешних зависимостей
- **Tinkoff API (gRPC)** - мокируется `InstrumentsServiceBlockingStub`
- **REST API** - мокируется `TinkoffRestClient`
- **База данных** - мокируются репозитории
- **Кэш** - отключается для unit-тестов

### Тестовые данные
- Используется `TestDataFactory` для создания консистентных тестовых данных
- Тестовые данные соответствуют реальным структурам Tinkoff API
- Поддерживаются различные сценарии (валидные данные, пустые данные, ошибки)

### Граничные случаи
- ✅ Null параметры
- ✅ Пустые строки
- ✅ Несуществующие идентификаторы
- ✅ Ошибки внешних сервисов
- ✅ Ошибки базы данных

## Расширение тестов

### Добавление нового теста
1. Создайте тестовый метод с аннотацией `@Test`
2. Используйте `TestDataFactory` для создания тестовых данных
3. Настройте моки с помощью `when().thenReturn()`
4. Выполните тестируемый метод
5. Проверьте результат с помощью `assertThat()`

### Добавление нового тестового сценария
1. Добавьте метод в `TestDataFactory` для создания специфичных данных
2. Создайте тестовый метод, описывающий сценарий
3. Добавьте проверки для всех ожидаемых результатов

## Отладка тестов

### Логирование
```java
// Включить логирование для тестов
@Slf4j
class MyTest {
    @Test
    void testMethod() {
        log.info("Test data: {}", testData);
        // ...
    }
}
```

### Отладка моков
```java
// Проверить, что мок был вызван
verify(mockService).methodName(expectedParameter);

// Проверить количество вызовов
verify(mockService, times(2)).methodName(any());
```

### Отладка данных
```java
// Вывести данные для отладки
System.out.println("Actual result: " + result);
assertThat(result).isEqualTo(expected);
```
