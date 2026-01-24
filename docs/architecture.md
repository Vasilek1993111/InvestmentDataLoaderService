# Архитектура системы

## Обзор

Investment Data Loader Service - это микросервис для загрузки и агрегации инвестиционных данных из Tinkoff Invest API с сохранением в PostgreSQL. Система обеспечивает автоматическую загрузку рыночных данных, их обработку и предоставление через REST API.

## 🏗️ Высокоуровневая архитектура

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Tinkoff API   │    │   Application   │    │   PostgreSQL    │
│                 │    │                 │    │                 │
│ • gRPC API      │◄──►│ • Spring Boot   │◄──►│ • Instruments   │
│ • REST API      │    │ • Controllers   │    │ • Market Data   │
│ • WebSocket     │    │ • Services      │    │ • Analytics     │
│                 │    │ • Schedulers    │    │ • Aggregations  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 📦 Компоненты системы

### 1. Controllers (Слой представления)
- **InstrumentsController** - REST API для работы с инструментами (акции, фьючерсы, индикативы)
- **CandlesMinuteController** - работа с минутными свечами
- **CandlesDailyController** - работа с дневными свечами
- **CandlesInstrumentController** - свечи конкретных инструментов с расширенной статистикой
- **MainSessionPricesController** - цены основной сессии (закрытия)
- **MorningSessionController** - цены утренней сессии (открытия)
- **EveningSessionController** - цены вечерней сессии (закрытия)
- **LastTradesController** - обезличенные сделки (last trades)
- **DividendController** - работа с дивидендами
- **AssetFundamentalsController** - фундаментальные показатели активов
- **VolumeAggregationController** - управление агрегацией объемов
- **StatusController** - отслеживание статуса асинхронных операций
- **TradingController** - торговые данные и расписания
- **SystemController** - системные эндпоинты и мониторинг
- **CacheController** - управление кэшем
- **RateLimitController** - управление лимитами API запросов

### 2. Services (Бизнес-логика)
- **InstrumentService** - управление инструментами (акции, фьючерсы, индикативы)
- **CachedInstrumentService** - кэширование инструментов с fallback на БД
- **MinuteCandleService** - работа с минутными свечами (параллельная обработка)
- **DailyCandleService** - работа с дневными свечами (параллельная обработка)
- **MainSessionPriceService** - цены основной сессии (закрытия)
- **MorningSessionService** - цены утренней сессии (открытия)
- **EveningSessionService** - цены вечерней сессии (закрытия)
- **LastTradesService** - обезличенные сделки (last trades)
- **LastTradeService** - работа с отдельными сделками
- **DividendService** - работа с дивидендами
- **AssetFundamentalService** - фундаментальные показатели активов
- **TradingService** - торговые расписания и статусы
- **CacheWarmupService** - прогрев кэша при запуске
- **RateLimitService** - управление лимитами API запросов
- **RetryService** - повторные попытки при ошибках

### 3. Repositories (Слой данных)
- **ShareRepository** - работа с акциями
- **FutureRepository** - работа с фьючерсами
- **IndicativeRepository** - работа с индикативными инструментами
- **ClosePriceRepository** - цены закрытия основной сессии
- **OpenPriceRepository** - цены открытия
- **MinuteCandleRepository** - минутные свечи
- **DailyCandleRepository** - дневные свечи
- **LastPriceRepository** - последние цены (обезличенные сделки)
- **ClosePriceEveningSessionRepository** - цены закрытия вечерней сессии
- **DividendRepository** - дивиденды
- **AssetFundamentalsRepository** - фундаментальные показатели
- **SystemLogRepository** - системные логи (отслеживание операций)

### 4. Schedulers (Планировщики)
- **InstrumentPreloadSchedulerService** - предзагрузка инструментов (00:45 МСК)
- **DividendSchedulerService** - загрузка дивидендов (00:50 МСК)
- **ClosePriceSchedulerService** - цены закрытия (01:00 МСК)
- **CandleSchedulerService** - загрузка минутных и дневных свечей (01:10 МСК)
- **EveningSessionSchedulerService** - цены вечерней сессии (01:40 МСК)
- **MorningSessionScheduler** - цены утренней сессии (01:50 МСК, 02:01, 07:01, 09:01, 10:01 МСК)
- **VolumeAggregationSchedulerService** - агрегация объемов (02:00 МСК)
- **LastTradesService** - обезличенные сделки (03:00 МСК) - планировщик в сервисе
- **AssetFundamentalsSchedulerService** - обновление фундаментальных показателей (03:00 МСК)

### 5. External Clients
- **TinkoffApiClient** - gRPC клиент для получения инструментов и рыночных данных
- **TinkoffRestClient** - REST клиент для дополнительных API вызовов

### 6. Configuration (Конфигурация)
- **AsyncConfig** - настройка асинхронных пулов потоков
- **CacheConfig** - конфигурация кэширования (Caffeine)
- **DatabaseMonitoringConfig** - мониторинг соединений с БД
- **GrpcConfig** - настройка gRPC клиентов
- **RateLimitConfig** - управление лимитами API запросов
- **WebConfig** - конфигурация веб-слоя
- **JacksonConfig** - настройка JSON сериализации
- **DotenvConfig** - загрузка переменных окружения
- **EnvironmentConfig** - конфигурация окружения

### 7. DTOs (Data Transfer Objects)
- **CandleDto**, **DailyCandleExtendedDto** - свечи
- **ClosePriceDto**, **ClosePriceRequestDto** - цены
- **AssetFundamentalDto**, **AssetFundamentalsRequestDto** - фундаментальные показатели
- **AccountDto** - торговые счета
- **AggregationRequestDto**, **AggregationResult** - агрегация данных

### 8. Entities (Сущности)
- **ShareEntity**, **FutureEntity**, **IndicativeEntity** - инструменты
- **MinuteCandleEntity**, **DailyCandleEntity** - свечи
- **ClosePriceEntity**, **ClosePriceEveningSessionEntity** - цены
- **LastPriceEntity** - последние цены
- **DividendEntity** - дивиденды
- **AssetFundamentalEntity** - фундаментальные показатели
- **SystemLogEntity** - системные логи

### 9. Utilities (Утилиты)
- **MinuteCandleMapper**, **DailyCandleMapper** - маппинг свечей
- **AssetFundamentalMapper** - маппинг фундаментальных показателей
- **TimeZoneUtils** - работа с часовыми поясами
- **QueryParamValidator** - валидация параметров запросов

### 10. Exception Handling (Обработка ошибок)
- **GlobalExceptionHandler** - глобальная обработка исключений
- **ApiException**, **DataLoadException** - специфичные исключения
- **ValidationException** - ошибки валидации
- **InstrumentsNotFoundException** - инструменты не найдены
- **SchedulerException** - ошибки планировщиков

## 🔄 Поток данных

### Загрузка инструментов
```
1. REST API запрос → InstrumentsController
2. InstrumentsController → InstrumentService
3. InstrumentService → Tinkoff API (gRPC/REST)
4. InstrumentService → Repository → PostgreSQL
5. Repository → InstrumentService → Controller → JSON ответ
```

### Загрузка рыночных данных
```
1. REST API запрос → CandlesMinuteController/CandlesDailyController
2. Controller → MinuteCandleService/DailyCandleService
3. Service → Tinkoff API (gRPC)
4. Service → Repository → PostgreSQL
5. Repository → Service → Controller → JSON ответ
```

### Автоматическая загрузка (Schedulers)
```
1. Scheduler → Service (по расписанию)
2. Service → Tinkoff API (gRPC)
3. Service → Repository → PostgreSQL
4. Service → SystemLogRepository (логирование)
```

## 🗄️ Модель данных

### Основные сущности
- **ShareEntity** - акции
- **FutureEntity** - фьючерсы
- **IndicativeEntity** - индикативные инструменты
- **ClosePriceEntity** - цены закрытия основной сессии
- **ClosePriceEveningSessionEntity** - цены закрытия вечерней сессии
- **MinuteCandleEntity** - минутные свечи
- **DailyCandleEntity** - дневные свечи
- **LastPriceEntity** - последние цены (обезличенные сделки)
- **DividendEntity** - дивиденды
- **AssetFundamentalEntity** - фундаментальные показатели активов
- **SystemLogEntity** - системные логи (отслеживание операций)

### Связи между сущностями
- Инструменты связаны с ценами через FIGI
- Свечи связаны с инструментами через FIGI
- Дивиденды связаны с акциями через FIGI
- Фундаментальные показатели связаны с активами через assetUid
- Временные ряды данных индексированы по дате и времени
- Все операции логируются в SystemLogEntity с уникальным taskId

## ⚡ Производительность

### Кэширование
- **Spring Cache** для API ответов
- **Caffeine** - высокопроизводительный кэш
- **Кэш инструментов** - акции, фьючерсы, индикативы
- **TTL кэша** - настраивается через конфигурацию
- **Прогрев кэша** при запуске приложения

### Асинхронность
- **@Async** методы для длительных операций
- **CompletableFuture** для неблокирующих вызовов
- **Планировщики** для автоматических задач
- **Параллельная обработка** через пулы потоков (ExecutorService)
- **Специализированные пулы** для разных типов операций:
  - `minuteCandleExecutor` - обработка минутных свечей
  - `dailyCandleExecutor` - обработка дневных свечей
  - `apiDataExecutor` - запросы к внешним API
  - `batchWriteExecutor` - пакетная запись в БД

### Оптимизация БД
- **Индексы** по FIGI, дате, тикеру
- **Партиционирование** по датам для больших таблиц
- **Материализованные представления** для аналитики
- **Пакетная обработка** (batch_size=100)
- **Оптимизированные запросы** с использованием JPA

## 🔒 Безопасность

### Аутентификация
- **Tinkoff API токен** для внешних вызовов
- **Переменные окружения** для конфиденциальных данных

### Валидация
- **Spring Validation** для входных данных
- **Проверка типов** инструментов
- **Валидация дат** и временных диапазонов

## 🚀 Масштабируемость

### Горизонтальное масштабирование
- **Stateless** архитектура
- **Внешняя БД** для состояния
- **Load Balancer** готовность

### Вертикальное масштабирование
- **Настраиваемые пулы** соединений
- **Кэширование** для снижения нагрузки
- **Асинхронная обработка** для I/O операций

## 📊 Мониторинг

### Логирование
- **SLF4J + Logback** для структурированных логов
- **Уровни логирования** по компонентам
- **Метрики производительности**

### Health Checks
- **Spring Actuator** для мониторинга
- **Проверка БД** соединения
- **Проверка внешних API**

## 🔧 Конфигурация

### Профили окружений
- **application.properties** - базовая конфигурация
- **application-prod.properties** - продакшн
- **application-test.properties** - тестирование
- **application-docker.properties** - Docker

### Внешняя конфигурация
- **Переменные окружения** для секретов
- **Docker secrets** для контейнеров
- **Конфигурационные файлы** для настроек

## 🧪 Тестирование

### Типы тестов
- **Unit тесты** - изолированное тестирование компонентов
- **Integration тесты** - тестирование интеграций
- **Component тесты** - тестирование API эндпоинтов
- **Allure отчеты** - детальная отчетность по тестам

### Test Containers
- **PostgreSQL** контейнер для тестов
- **Изолированная среда** для каждого теста
- **Автоматическая очистка** после тестов
- **Тестовые профили** (application-test.properties, application-unit.properties)

### Покрытие тестами
- **Контроллеры** - полное покрытие API эндпоинтов
- **Сервисы** - тестирование бизнес-логики
- **Репозитории** - тестирование работы с БД
- **Планировщики** - тестирование автоматических задач

## 🚀 Развертывание

### Docker
- **Multi-stage build** для оптимизации образа
- **Health checks** в контейнере
- **Volume mapping** для данных

### Kubernetes (планируется)
- **Deployment** для приложения
- **Service** для внутреннего трафика
- **ConfigMap** для конфигурации
- **Secret** для токенов

## 📈 Метрики и мониторинг

### Ключевые метрики
- **Время ответа API** эндпоинтов
- **Количество запросов** к внешним API
- **Использование памяти** и CPU
- **Размер БД** и производительность запросов
- **Статистика кэша** (hit/miss ratio)
- **Производительность планировщиков**

### Системные логи
- **SystemLogEntity** - логирование всех операций
- **Task ID** - уникальные идентификаторы задач
- **Временные метки** - точное время выполнения
- **Статусы операций** - STARTED, PROCESSING, COMPLETED, FAILED

### Алерты
- **Ошибки API** > 5%
- **Время ответа** > 5 секунд
- **Недоступность БД**
- **Превышение лимитов** Tinkoff API
- **Сбои планировщиков**
- **Проблемы с кэшем**

## 🔄 Планировщики

### Расписание выполнения
| Время (МСК) | Планировщик | Описание |
|-------------|-------------|----------|
| 00:45 | InstrumentPreloadSchedulerService | Предзагрузка инструментов (акции, фьючерсы, индикативы) |
| 00:50 | DividendSchedulerService | Загрузка дивидендов по всем акциям |
| 01:00 | ClosePriceSchedulerService | Загрузка цен закрытия (акции, фьючерсы) |
| 01:10 | CandleSchedulerService | Загрузка минутных и дневных свечей |
| 01:40 | EveningSessionSchedulerService | Цены закрытия вечерней сессии |
| 01:50 | MorningSessionScheduler | Цены открытия утренней сессии за предыдущий день |
| 02:00 | VolumeAggregationSchedulerService | Обновление агрегации объемов |
| 02:01 | MorningSessionScheduler | Цены открытия в выходные дни (суббота, воскресенье) |
| 03:00 | LastTradesService | Загрузка обезличенных сделок за предыдущий день |
| 03:00 | AssetFundamentalsSchedulerService | Обновление фундаментальных показателей всех акций |
| 07:01 | MorningSessionScheduler | Цены открытия в рабочие дни (понедельник-пятница) |
| 09:01 | MorningSessionScheduler | Цены открытия в рабочие дни (понедельник-пятница) |
| 10:01 | MorningSessionScheduler | Цены открытия в рабочие дни (понедельник-пятница) |

### Особенности планировщиков
- **Московское время** - все расписания в Europe/Moscow (zone = "Europe/Moscow")
- **Логирование** - каждая задача получает уникальный Task ID
- **Системные логи** - все операции логируются в SystemLogEntity
- **Обработка ошибок** - graceful handling с детальным логированием
- **Параллельная обработка** - оптимизированная загрузка данных через пулы потоков
- **Асинхронность** - большинство операций выполняется асинхронно
- **Проверка выходных дней** - некоторые планировщики пропускают выполнение в выходные
- **Детальная статистика** - логирование количества обработанных элементов
