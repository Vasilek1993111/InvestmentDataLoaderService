# Исправление утечек подключений к базе данных

## 🔍 Выявленные проблемы

### 1. **Отсутствие транзакционных аннотаций**
- Методы в сервисах выполняли операции с БД без `@Transactional`
- Это приводило к созданию новых подключений для каждой операции
- Отсутствие управления транзакциями в асинхронных методах

### 2. **Неэффективное использование пула подключений**
- Множественные вызовы `findAll()` без транзакций
- Пакетные операции без транзакционного контекста
- Отсутствие оптимизации для асинхронных задач

### 3. **Проблемы с конфигурацией HikariCP**
- Слишком высокий порог обнаружения утечек (60 секунд)
- Отсутствие тестовых запросов для проверки подключений
- Неоптимальные настройки для высоконагруженных операций

## 🛠️ Внесенные исправления

### 1. **Добавлены транзакционные аннотации**

#### LastTradesService
```java
@Transactional(readOnly = true)
public SaveResponseDto processLastTrades(LocalDate date, String taskId)

@Transactional
public void saveLastPriceEntity(LastPriceEntity entity)
```

#### MinuteCandleService
```java
@Transactional(readOnly = true)
private List<String> getAllInstrumentIds(List<String> assetTypes)

@Transactional
public void saveMinuteCandlesBatch(List<MinuteCandleEntity> entities)

@Transactional
private void logFigiProcessing(...)
```

#### DailyCandleService
```java
@Transactional(readOnly = true)
private List<String> getAllInstrumentIds(List<String> assetTypes)

@Transactional
public void saveDailyCandlesBatch(List<DailyCandleEntity> entities)

@Transactional
private void logFigiProcessing(...)
```

#### CachedInstrumentService
```java
@Transactional(readOnly = true)
public List<ShareEntity> getAllShares()

@Transactional(readOnly = true)
public List<FutureEntity> getAllFutures()
```

#### InstrumentService
```java
@Transactional
public SaveResponseDto saveShares(ShareFilterDto filter)

@Transactional
public SaveResponseDto saveFutures(FutureFilterDto filter)

@Transactional
public SaveResponseDto saveIndicatives(IndicativeFilterDto filter)
```

### 2. **Улучшена конфигурация HikariCP**

#### Основные настройки (application.properties)
```properties
# Снижен порог обнаружения утечек с 60 до 30 секунд
spring.datasource.hikari.leak-detection-threshold=30000

# Добавлены тестовые запросы
spring.datasource.hikari.connection-test-query=SELECT 1
spring.datasource.hikari.auto-commit=true
spring.datasource.hikari.pool-name=InvestmentDataLoaderPool
```

#### Продакшн настройки
```properties
spring.datasource.hikari.leak-detection-threshold=30000
spring.datasource.hikari.connection-test-query=SELECT 1
spring.datasource.hikari.auto-commit=true
spring.datasource.hikari.pool-name=InvestmentDataLoaderProdPool
```

#### Тестовые настройки
```properties
spring.datasource.hikari.leak-detection-threshold=15000
spring.datasource.hikari.connection-test-query=SELECT 1
spring.datasource.hikari.auto-commit=true
spring.datasource.hikari.pool-name=InvestmentDataLoaderTestPool
```

### 3. **Добавлен мониторинг подключений**

Создан `DatabaseMonitoringConfig` с функциями:
- **Health Indicator** для мониторинга состояния пула
- **Периодическая проверка** каждые 5 минут
- **Автоматическая очистка** неактивных подключений каждые 30 минут
- **Предупреждения** о потенциальных утечках

## 📊 Ожидаемые результаты

### 1. **Снижение утечек подключений**
- Транзакционные аннотации обеспечат правильное управление подключениями
- Пакетные операции будут выполняться в рамках одной транзакции
- Автоматическое освобождение подключений после завершения транзакций

### 2. **Улучшение производительности**
- Меньше накладных расходов на создание/закрытие подключений
- Более эффективное использование пула подключений
- Снижение времени ожидания подключений

### 3. **Повышение стабильности**
- Раннее обнаружение утечек (30 секунд вместо 60)
- Автоматический мониторинг состояния пула
- Проактивная очистка неактивных подключений

## 🔧 Рекомендации по мониторингу

### 1. **Проверка логов**
Ищите сообщения типа:
```
⚠️  ПРЕДУПРЕЖДЕНИЕ: Много потоков ожидают подключение (X)
⚠️  ПРЕДУПРЕЖДЕНИЕ: Все подключения заняты, возможна утечка
```

### 2. **Мониторинг метрик**
- Количество активных подключений
- Количество потоков, ожидающих подключение
- Время жизни подключений

### 3. **Health Check**
Используйте endpoint `/actuator/health` для проверки состояния пула подключений.

## 🚀 Дополнительные рекомендации

### 1. **Оптимизация запросов**
- Используйте `@Query` с `@Modifying` для массовых операций
- Применяйте `@BatchSize` для оптимизации загрузки связанных сущностей
- Рассмотрите использование `@EntityGraph` для уменьшения количества запросов

### 2. **Настройка пула подключений**
- Мониторьте метрики HikariCP
- Настройте размер пула в зависимости от нагрузки
- Используйте connection validation для обнаружения "мертвых" подключений

### 3. **Асинхронная обработка**
- Ограничьте количество параллельных асинхронных задач
- Используйте `@Async` с настроенными thread pool'ами
- Применяйте circuit breaker для внешних API

## 📈 Метрики для отслеживания

1. **HikariCP метрики:**
   - `hikaricp.connections.active`
   - `hikaricp.connections.idle`
   - `hikaricp.connections.pending`
   - `hikaricp.connections.max`

2. **JVM метрики:**
   - Количество открытых файловых дескрипторов
   - Использование памяти
   - Количество потоков

3. **База данных:**
   - Количество активных подключений
   - Время выполнения запросов
   - Количество блокировок

Эти исправления должны значительно снизить количество утечек подключений и улучшить общую стабильность системы.
