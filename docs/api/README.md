# API Documentation

## Обзор

Система предоставляет REST API для работы с инвестиционными данными, включая загрузку и получение свечей различных типов, работу с инструментами, ценами сессий и последними сделками. Все POST методы используют оптимизированную параллельную обработку для максимальной производительности.

**Базовые URL:**
- **PROD**: `http://localhost:8083/api`
- **TEST**: `http://localhost:8087/api`

**Основные возможности:**
- **Свечи** - минутные и дневные свечи различных инструментов
- **Инструменты** - акции, фьючерсы, индикативы
- **Цены сессий** - утренние, основные, вечерние сессии
- **Последние сделки** - обезличенные сделки
- **Кэширование** - оптимизированная работа с данными
- **Мониторинг** - детальное логирование и отслеживание

## Контроллеры

### 1. Минутные свечи (`CandlesMinuteController`)
- **Базовый путь**: `/api/candles/minute`
- **Функции**: Асинхронная загрузка и получение минутных свечей
- **Особенности**: Параллельная обработка через `MinuteCandleService`

**Основные эндпоинты:**
- `POST /api/candles/minute` - асинхронная загрузка за сегодня
- `POST /api/candles/minute/{date}` - асинхронная загрузка за дату
- `POST /api/candles/minute/shares/{date}` - асинхронная загрузка акций
- `POST /api/candles/minute/futures/{date}` - асинхронная загрузка фьючерсов
- `POST /api/candles/minute/indicatives/{date}` - асинхронная загрузка индикативов
- `GET /api/candles/minute/shares/{date}` - получение акций
- `GET /api/candles/minute/futures/{date}` - получение фьючерсов
- `GET /api/candles/minute/indicatives/{date}` - получение индикативов

### 2. Дневные свечи (`CandlesDailyController`)
- **Базовый путь**: `/api/candles/daily`
- **Функции**: Асинхронная загрузка и получение дневных свечей
- **Особенности**: Параллельная обработка через `DailyCandleService`

**Основные эндпоинты:**
- `POST /api/candles/daily` - асинхронная загрузка за сегодня
- `POST /api/candles/daily/{date}` - асинхронная загрузка за дату
- `POST /api/candles/daily/shares/{date}` - асинхронная загрузка акций
- `POST /api/candles/daily/futures/{date}` - асинхронная загрузка фьючерсов
- `POST /api/candles/daily/indicatives/{date}` - асинхронная загрузка индикативов
- `GET /api/candles/daily/shares/{date}` - получение акций
- `GET /api/candles/daily/futures/{date}` - получение фьючерсов
- `GET /api/candles/daily/indicatives/{date}` - получение индикативов

### 3. Свечи инструментов (`CandlesInstrumentController`)
- **Базовый путь**: `/api/candles/instrument`
- **Функции**: Загрузка и получение свечей конкретных инструментов с расширенной статистикой
- **Особенности**: Поддержка как минутных, так и дневных свечей с автоматическими вычислениями

**Основные эндпоинты:**
- `GET /api/candles/instrument/minute/{figi}/{date}` - получение минутных свечей
- `POST /api/candles/instrument/minute/{figi}/{date}` - асинхронная загрузка минутных свечей
- `GET /api/candles/instrument/daily/{figi}/{date}` - получение дневных свечей
- `POST /api/candles/instrument/daily/{figi}/{date}` - асинхронная загрузка дневных свечей

### 4. Инструменты (`InstrumentsController`)
- **Базовый путь**: `/api/instruments`
- **Функции**: Управление финансовыми инструментами (акции, фьючерсы, индикативы)
- **Особенности**: Кэширование, автоматический прогрев, защита от дубликатов

**Основные эндпоинты:**
- `GET /api/instruments/shares` - получение акций с фильтрацией
- `GET /api/instruments/shares/{identifier}` - поиск акции по FIGI или тикеру
- `POST /api/instruments/shares` - сохранение акций в БД
- `GET /api/instruments/futures` - получение фьючерсов с фильтрацией
- `GET /api/instruments/futures/{identifier}` - поиск фьючерса по FIGI или тикеру
- `POST /api/instruments/futures` - сохранение фьючерсов в БД
- `GET /api/instruments/indicatives` - получение индикативов с фильтрацией
- `GET /api/instruments/indicatives/{identifier}` - поиск индикатива по FIGI или тикеру
- `POST /api/instruments/indicatives` - сохранение индикативов в БД
- `GET /api/instruments/count` - статистика по количеству инструментов

### 5. Цены сессий

#### Утренняя сессия (`MorningSessionController`)
- **Базовый путь**: `/api/morning-session`
- **Функции**: Загрузка и получение цен открытия утренней сессии
- **Особенности**: Московское время, детальная статистика

**Основные эндпоинты:**
- `POST /api/morning-session` - загрузка цен за сегодня
- `GET /api/morning-session` - предпросмотр цен за сегодня
- `POST /api/morning-session/{date}` - загрузка цен за дату
- `GET /api/morning-session/{date}` - поиск цен за дату

#### Основная сессия (`MainSessionPricesController`)
- **Базовый путь**: `/api/main-session-prices`
- **Функции**: Загрузка и получение цен основной торговой сессии
- **Особенности**: Валидация рабочих дней, кэширование инструментов

**Основные эндпоинты:**
- `POST /api/main-session-prices/` - загрузка цен закрытия за сегодня
- `GET /api/main-session-prices/shares` - получение цен акций
- `POST /api/main-session-prices/shares` - загрузка цен акций
- `GET /api/main-session-prices/futures` - получение цен фьючерсов
- `POST /api/main-session-prices/futures` - загрузка цен фьючерсов
- `GET /api/main-session-prices/{figi}` - получение цены по инструменту
- `POST /api/main-session-prices/{figi}` - загрузка цены по инструменту
- `POST /api/main-session-prices/{date}` - загрузка цен за дату

#### Вечерняя сессия (`EveningSessionController`)
- **Базовый путь**: `/api/evening-session-prices`
- **Функции**: Загрузка и получение цен закрытия вечерней сессии
- **Особенности**: Использование данных из minute_candles, транзакционность

**Основные эндпоинты:**
- `GET /api/evening-session-prices` - получение цен за вчера
- `POST /api/evening-session-prices` - загрузка цен за вчера
- `GET /api/evening-session-prices/{date}` - получение цен за дату
- `POST /api/evening-session-prices/{date}` - загрузка цен за дату

### 6. Последние сделки (`LastTradesController`)
- **Базовый путь**: `/api/last-trades`
- **Функции**: Загрузка последних обезличенных сделок
- **Особенности**: Оптимизированная обработка с пулами потоков

**Основные эндпоинты:**
- `POST /api/last-trades` - асинхронная загрузка с JSON телом
- `GET /api/last-trades` - асинхронная загрузка с параметрами запроса
- `GET /api/last-trades/shares` - загрузка только акций
- `GET /api/last-trades/futures` - загрузка только фьючерсов
- `GET /api/last-trades/performance` - статистика производительности

### 7. Системные контроллеры

#### Статус (`StatusController`)
- **Базовый путь**: `/api/status`
- **Функции**: Отслеживание статуса асинхронных операций

**Основные эндпоинты:**
- `GET /api/status/{taskId}` - получение статуса задачи

#### Система (`SystemController`)
- **Базовый путь**: `/api/system`
- **Функции**: Мониторинг и диагностика системы

**Основные эндпоинты:**
- `GET /api/health` - проверка здоровья системы
- `GET /api/diagnostics` - диагностика системы
- `GET /api/stats` - статистика системы
- `GET /api/info` - информация о системе
- `GET /api/external-services` - статус внешних сервисов

#### Кэш (`CacheController`)
- **Базовый путь**: `/api/cache`
- **Функции**: Управление кэшем системы

**Основные эндпоинты:**
- `POST /api/cache/warmup` - прогрев кэша
- `GET /api/cache/content` - содержимое кэша
- `DELETE /api/cache/clear` - очистка кэша

#### Агрегация объемов (`VolumeAggregationController`)
- **Базовый путь**: `/api/volume-aggregation`
- **Функции**: Управление агрегацией объемов торгов

**Основные эндпоинты:**
- `POST /api/volume-aggregation/refresh` - обновление агрегации
- `GET /api/volume-aggregation/check` - проверка статуса агрегации
- `GET /api/volume-aggregation/schedule-info` - информация о расписании

## Архитектура

### Параллельная обработка

Все POST методы используют специализированные сервисы с параллельной обработкой:

#### MinuteCandleService
- **minuteCandleExecutor** - основной пул для обработки инструментов
- **apiDataExecutor** - пул для API запросов
- **batchWriteExecutor** - пул для пакетной записи в БД

#### DailyCandleService
- **dailyCandleExecutor** - основной пул для обработки инструментов
- **dailyApiDataExecutor** - пул для API запросов
- **dailyBatchWriteExecutor** - пул для пакетной записи в БД

#### LastTradesService
- **lastTradesApiExecutor** - пул для API запросов
- **lastTradesBatchExecutor** - пул для пакетной обработки
- **lastTradesProcessingExecutor** - пул для обработки данных

### Алгоритм обработки

1. **Валидация параметров** - проверка входных данных
2. **Создание запроса** - формирование DTO для сервиса
3. **Параллельная загрузка** - асинхронная обработка через специализированные сервисы
4. **Логирование** - детальное логирование всех операций в system_logs
5. **Возврат результата** - немедленный возврат taskId без ожидания завершения

### Кэширование

- **CachedInstrumentService** - кэширование инструментов для повышения производительности
- **Автоматический прогрев** - кэш автоматически прогревается при запуске приложения
- **Ручное управление** - возможность принудительного прогрева через API
- **TTL** - время жизни кэша настраивается через конфигурацию

## Типы данных

### Запросы

#### MinuteCandleRequestDto
```json
{
  "instruments": ["BBG004730N88", "BBG004730ZJ9"],
  "assetType": ["SHARES", "FUTURES"],
  "date": "2024-01-01"
}
```

#### DailyCandleRequestDto
```json
{
  "instruments": ["BBG004730N88", "BBG004730ZJ9"],
  "assetType": ["SHARES", "FUTURES"],
  "date": "2024-01-01"
}
```

#### LastTradesRequestDto
```json
{
  "figis": ["BBG004730N88", "BBG004730ZJ9"],
  "tradeSource": "TRADE_SOURCE_ALL"
}
```

#### ShareFilterDto
```json
{
  "status": "INSTRUMENT_STATUS_ACTIVE",
  "exchange": "MOEX",
  "currency": "RUB",
  "ticker": "SBER"
}
```

#### FutureFilterDto
```json
{
  "status": "INSTRUMENT_STATUS_ACTIVE",
  "exchange": "MOEX",
  "currency": "RUB",
  "assetType": "CURRENCY"
}
```

#### IndicativeFilterDto
```json
{
  "exchange": "MOEX",
  "currency": "RUB",
  "ticker": "IMOEX"
}
```

### Ответы

#### Стандартный ответ POST методов (асинхронные операции)
```json
{
  "success": true,
  "message": "Операция запущена",
  "taskId": "uuid-task-id",
  "endpoint": "/api/endpoint",
  "status": "STARTED",
  "startTime": "2024-01-01T10:00:00Z"
}
```

#### Стандартный ответ GET методов (свечи)
```json
{
  "date": "2024-01-01",
  "assetType": "SHARES",
  "candles": [...],
  "totalCandles": 1000,
  "totalInstruments": 150,
  "processedInstruments": 150,
  "successfulInstruments": 145,
  "noDataInstruments": 3,
  "errorInstruments": 2,
  "totalVolume": 1000000,
  "averagePrice": 250.75
}
```

#### Расширенные свечи (MinuteCandleExtendedDto)
```json
{
  "figi": "BBG004730N88",
  "ticker": "SBER",
  "name": "Сбербанк",
  "time": "2024-01-01T09:00:00Z",
  "open": 250.50,
  "close": 251.20,
  "high": 251.50,
  "low": 250.30,
  "volume": 1000,
  "isComplete": true,
  "priceChange": 0.70,
  "priceChangePercent": 0.28,
  "candleType": "BULLISH",
  "bodySize": 0.70,
  "upperShadow": 0.30,
  "lowerShadow": 0.20,
  "highLowRange": 1.20,
  "averagePrice": 250.88
}
```

#### Инструменты (ShareDto)
```json
{
  "figi": "BBG004730N88",
  "ticker": "SBER",
  "name": "Сбербанк",
  "currency": "RUB",
  "exchange": "MOEX",
  "sector": "Financial Services",
  "tradingStatus": "SECURITY_TRADING_STATUS_NORMAL_TRADING"
}
```

#### Статистика сохранения (SaveResponseDto)
```json
{
  "success": true,
  "message": "Успешно загружено 5 новых инструментов из 10 найденных.",
  "totalRequested": 10,
  "newItemsSaved": 5,
  "existingItemsSkipped": 5,
  "invalidItemsFiltered": 0,
  "missingFromApi": 0,
  "savedItems": [...]
}
```

## Мониторинг

### Логирование

Все операции логируются в таблицу `system_logs` с детальной информацией:
- Статус обработки каждого инструмента
- Количество обработанных свечей
- Время выполнения операций
- Ошибки и исключения
- TaskId для отслеживания асинхронных операций

### Статусы операций

- **STARTED** - операция запущена
- **PROCESSING** - операция выполняется
- **SUCCESS** - успешное завершение
- **COMPLETED** - операция завершена
- **ERROR** - ошибка выполнения
- **FAILED** - критическая ошибка
- **NO_DATA** - отсутствие данных
- **CANCELLED** - операция отменена
- **TIMEOUT** - превышено время ожидания

### Отслеживание задач

- **TaskId** - уникальный идентификатор для каждой асинхронной операции
- **Status API** - `/api/status/{taskId}` для проверки статуса
- **System Logs** - детальные логи всех операций
- **Performance** - статистика производительности executor'ов

## Коды ответов

- **200 OK** - Успешное получение данных (GET методы)
- **202 Accepted** - Асинхронная операция запущена (POST методы)
- **400 Bad Request** - Неверные параметры запроса
- **404 Not Found** - Ресурс не найден
- **500 Internal Server Error** - Внутренняя ошибка сервера

### Специфичные коды

- **200 OK** - Данные получены успешно
- **202 Accepted** - Асинхронная операция запущена
- **400 Bad Request** - Некорректные параметры запроса
- **404 Not Found** - Инструмент или данные не найдены
- **500 Internal Server Error** - Внутренняя ошибка сервера

## Примеры использования

### Загрузка данных

```bash
# Загрузка минутных свечей за сегодня
curl -X POST http://localhost:8083/api/candles/minute \
  -H "Content-Type: application/json" \
  -d '{"assetType": ["SHARES", "FUTURES"]}'

# Загрузка дневных свечей акций за дату
curl -X POST http://localhost:8083/api/candles/daily/shares/2024-01-15

# Загрузка минутных свечей конкретного инструмента
curl -X POST http://localhost:8083/api/candles/instrument/minute/BBG004730N88/2024-01-15

# Загрузка последних сделок
curl -X POST http://localhost:8083/api/last-trades \
  -H "Content-Type: application/json" \
  -d '{"figis": ["BBG004730N88"], "tradeSource": "TRADE_SOURCE_ALL"}'

# Сохранение инструментов
curl -X POST http://localhost:8083/api/instruments/shares \
  -H "Content-Type: application/json" \
  -d '{"exchange": "MOEX", "currency": "RUB"}'
```

### Получение данных

```bash
# Получение минутных свечей акций за дату
curl http://localhost:8083/api/candles/minute/shares/2024-01-15

# Получение дневных свечей фьючерсов за дату
curl http://localhost:8083/api/candles/daily/futures/2024-01-15

# Получение дневных свечей конкретного инструмента
curl http://localhost:8083/api/candles/instrument/daily/BBG004730N88/2024-01-15

# Получение инструментов
curl "http://localhost:8083/api/instruments/shares?exchange=MOEX&currency=RUB"

# Поиск инструмента по тикеру
curl "http://localhost:8083/api/instruments/shares/SBER"

# Получение цен сессий
curl "http://localhost:8083/api/main-session-prices/shares"
curl "http://localhost:8083/api/evening-session-prices/2024-01-15"
```

### Мониторинг

```bash
# Проверка статуса задачи
curl "http://localhost:8083/api/status/{taskId}"

# Проверка здоровья системы
curl "http://localhost:8083/api/health"

# Статистика системы
curl "http://localhost:8083/api/stats"

# Содержимое кэша
curl "http://localhost:8083/api/cache/content"

# Статистика производительности
curl "http://localhost:8083/api/last-trades/performance"
```

## Обработка ошибок

Все ошибки логируются и возвращаются в стандартном формате:

```json
{
  "success": false,
  "message": "Ошибка выполнения: [описание ошибки]",
  "taskId": "uuid-task-id",
  "status": "ERROR"
}
```

## Производительность

### Оптимизации

- **Параллельная обработка** - множественные потоки для обработки инструментов
- **Пакетная запись** - группировка операций записи в БД
- **Асинхронные API запросы** - неблокирующие запросы к внешним сервисам
- **Настраиваемые пулы потоков** - автоматическая настройка под количество процессоров

### Рекомендации

1. **Для больших объемов данных** используйте POST методы с параллельной обработкой
2. **Для небольших наборов инструментов** можно использовать GET методы
3. **Мониторинг производительности** через логи системы
4. **Настройка пулов потоков** в зависимости от нагрузки сервера

## Дополнительные ресурсы

### API Документация
- [API минутных свечей](minute-candles.md)
- [API дневных свечей](daily-candles.md)
- [API свечей инструментов](instrument-candles.md)
- [API расширенных минутных свечей](minute-candles-extended.md)
- [API инструментов](instruments.md)
- [API утренней сессии](morning-session.md)
- [API основной сессии](main-session-prices.md)
- [API вечерней сессии](evening-session.md)
- [API последних сделок](last-trades.md)
- [API статуса](status.md)
- [API системы](system.md)
- [API кэша](cache.md)
- [API агрегации объемов](volume-aggregation.md)

### Системная документация
- [Планировщики](../schedulers.md)
- [Архитектура системы](../architecture.md)
- [Конфигурация](../configuration.md)
- [База данных](../database.md)

### Быстрый старт
- [Allure отчеты](../ALLURE_QUICK_START.md)
- [Docker разработка](../docker-development.md)
- [Использование часовых поясов](../timezone-usage.md)
