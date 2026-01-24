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
- **InstrumentsController** - REST API для работы с инструментами
- **CandlesMinuteController** - работа с минутными свечами
- **CandlesDailyController** - работа с дневными свечами
- **TradingController** - торговые данные и расписания
- **SystemController** - системные эндпоинты и мониторинг
- **CacheController** - управление кэшем
- **MainSessionPricesController** - цены основной сессии
- **MorningSessionController** - цены утренней сессии
- **LastTradesController** - последние сделки

### 2. Services (Бизнес-логика)
- **InstrumentService** - управление инструментами (акции, фьючерсы, индикативы)
- **TradingService** - торговые расписания и статусы
- **MinuteCandleService** - работа с минутными свечами
- **DailyCandleService** - работа с дневными свечами
- **LastTradesService** - последние сделки
- **MainSessionPriceService** - цены основной сессии
- **MorningSessionService** - цены утренней сессии
- **CacheWarmupService** - прогрев кэша при запуске
- **CachedInstrumentService** - кэшированные инструменты

### 3. Repositories (Слой данных)
- **ShareRepository** - работа с акциями
- **FutureRepository** - работа с фьючерсами
- **IndicativeRepository** - работа с индикативными инструментами
- **ClosePriceRepository** - цены закрытия
- **OpenPriceRepository** - цены открытия
- **MinuteCandleRepository** - минутные свечи
- **DailyCandleRepository** - дневные свечи
- **LastPriceRepository** - последние цены
- **ClosePriceEveningSessionRepository** - цены вечерней сессии
- **SystemLogRepository** - системные логи

### 4. Schedulers (Планировщики)
- **CandleSchedulerService** - загрузка свечей (1:10 МСК)
- **MorningSessionScheduler** - цены утренней сессии (1:50 МСК)
- **EveningSessionSchedulerService** - цены вечерней сессии (1:40 МСК)
- **ClosePriceSchedulerService** - цены закрытия (1:30 МСК)
- **LastTradesSchedulerService** - последние сделки (каждые 30 мин)
- **VolumeAggregationSchedulerService** - агрегация объемов (2:00 МСК)
- **InstrumentPreloadSchedulerService** - предзагрузка инструментов (0:45 МСК)

### 5. External Clients
- **Tinkoff gRPC клиенты** - для получения инструментов и рыночных данных
- **Tinkoff REST клиент** - для дополнительных API вызовов

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
- **ClosePriceEntity** - цены закрытия
- **OpenPriceEntity** - цены открытия
- **MinuteCandleEntity** - минутные свечи
- **DailyCandleEntity** - дневные свечи
- **LastPriceEntity** - последние цены
- **ClosePriceEveningSessionEntity** - цены вечерней сессии
- **SystemLogEntity** - системные логи

### Связи между сущностями
- Инструменты связаны с ценами через FIGI
- Свечи связаны с инструментами через FIGI
- Временные ряды данных индексированы по дате и времени

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
- **Параллельная обработка** в сервисах

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
| 00:45 | InstrumentPreloadSchedulerService | Предзагрузка инструментов |
| 01:00 | LastTradesSchedulerService | Загрузка последних сделок |
| 01:10 | CandleSchedulerService | Загрузка свечей |
| 01:30 | ClosePriceSchedulerService | Загрузка цен закрытия |
| 01:40 | EveningSessionSchedulerService | Цены вечерней сессии |
| 01:50 | MorningSessionScheduler | Цены утренней сессии |
| 02:00 | VolumeAggregationSchedulerService | Агрегация объемов |

### Особенности планировщиков
- **Московское время** - все расписания в Europe/Moscow
- **Логирование** - каждая задача получает уникальный Task ID
- **Обработка ошибок** - graceful handling с детальным логированием
- **Параллельная обработка** - оптимизированная загрузка данных
