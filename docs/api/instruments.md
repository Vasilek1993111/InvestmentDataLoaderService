# API — Инструменты (`/api/instruments`)

## Обзор

API для работы с финансовыми инструментами предоставляет единый интерфейс для работы с тремя типами инструментов:
- **Акции (Shares)** - обыкновенные и привилегированные акции
- **Фьючерсы (Futures)** - производные финансовые инструменты  
- **Индикативы (Indicatives)** - индикативные инструменты

### Основные возможности:
- **Получение инструментов** - из внешнего API Tinkoff или локальной БД
- **Фильтрация** - по различным параметрам (биржа, валюта, тикер, FIGI)
- **Сохранение** - инструментов в БД с защитой от дубликатов
- **Поиск** - инструментов по FIGI или тикеру
- **Статистика** - по количеству инструментов

**Базовый URL:** `http://localhost:8083/api/instruments` (PROD) / `http://localhost:8087/api/instruments` (TEST)

**Особенности:**
- **Кэширование** - данные из API кэшируются для повышения производительности
- **Автоматический прогрев** - кэш автоматически прогревается при запуске
- **Защита от дубликатов** - инструменты не дублируются в БД
- **Автоматическое определение** - API сам определяет FIGI или тикер
- **Fallback** - при недоступности REST API используются данные из БД
- **Асинхронная обработка** - все POST методы выполняются асинхронно с возвратом `taskId`
- **Детальное логирование** - все операции логируются в БД с уникальным `taskId`
- **Валидация параметров** - двухуровневая валидация всех входных параметров

---

## Акции (Shares)

### GET /api/instruments/shares

Получение списка акций с фильтрацией. Поддерживает два источника данных:

**Параметры запроса:**
- `source` (опционально, по умолчанию `"api"`) - источник данных: `"api"` или `"database"`
- `status` (опционально, только для `source=api`) - статус инструмента: `INSTRUMENT_STATUS_UNSPECIFIED`, `INSTRUMENT_STATUS_BASE`, `INSTRUMENT_STATUS_ALL`
- `exchange` (опционально) - биржа: `MOEX`, `moex`, `SPB`, `FORTS_MAIN`, `UNKNOWN` и др.
- `currency` (опционально) - валюта: `RUB`, `USD`, `rub`, `usd`
- `ticker` (опционально) - тикер инструмента (например: `SBER`, `GAZP`)
- `figi` (опционально) - уникальный идентификатор инструмента
- `filter` (опционально, в теле запроса) - расширенный фильтр для базы данных (только для `source=database`), включает: `sector`, `tradingStatus`

**Примеры использования:**

```bash
# Получение из API (по умолчанию)
curl "http://localhost:8083/api/instruments/shares?exchange=MOEX&currency=RUB"

# Получение из базы данных
curl "http://localhost:8083/api/instruments/shares?source=database&exchange=MOEX"

# Получение из базы данных с расширенным фильтром
curl -X POST "http://localhost:8083/api/instruments/shares?source=database" \
  -H "Content-Type: application/json" \
  -d '{
    "exchange": "MOEX",
    "currency": "RUB",
    "sector": "Technology",
    "tradingStatus": "TRADING_STATUS_ACTIVE"
  }'
```

**Ответ (пример):**
```json
[
  {
    "figi": "BBG004730N88",
    "ticker": "SBER",
    "name": "Сбербанк",
    "currency": "RUB",
    "exchange": "MOEX",
    "sector": "Financial Services",
    "tradingStatus": "SECURITY_TRADING_STATUS_NORMAL_TRADING"
  }
]
```

### GET /api/instruments/shares/{identifier}

Получение акции по FIGI или тикеру из базы данных.

API автоматически определяет тип идентификатора:
- Если идентификатор длиннее 10 символов или содержит `-` или `_` → поиск по FIGI
- Иначе → поиск по тикеру

**Примеры:**
```bash
# По FIGI
curl "http://localhost:8083/api/instruments/shares/BBG004730N88"

# По тикеру
curl "http://localhost:8083/api/instruments/shares/SBER"
```

**Ответ (пример):**
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

### POST /api/instruments/shares

**Асинхронное** сохранение акций в базу данных по фильтрам с защитой от дубликатов.

⚠️ **Важно:** Операция выполняется асинхронно. Метод возвращает `taskId` для отслеживания статуса операции. Результат можно получить через System API по `taskId`.

**Тело запроса:**
```json
{
  "status": "INSTRUMENT_STATUS_BASE",
  "exchange": "MOEX",
  "currency": "RUB",
  "ticker": "SBER",
  "figi": "BBG004730N88",
  "sector": "Financial Services",
  "tradingStatus": "SECURITY_TRADING_STATUS_NORMAL_TRADING"
}
```

**Параметры фильтра:**
- `status` (опционально) - статус инструмента: `INSTRUMENT_STATUS_UNSPECIFIED`, `INSTRUMENT_STATUS_BASE`, `INSTRUMENT_STATUS_ALL`
- `exchange` (опционально) - биржа: `MOEX`, `moex`, `SPB`, `FORTS_MAIN`, `UNKNOWN` и др.
- `currency` (опционально) - валюта: `RUB`, `USD`, `rub`, `usd`
- `ticker` (опционально) - тикер инструмента
- `figi` (опционально) - уникальный идентификатор инструмента
- `sector` (опционально) - сектор экономики
- `tradingStatus` (опционально) - статус торговли

**Пример:**
```bash
curl -X POST "http://localhost:8083/api/instruments/shares" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "INSTRUMENT_STATUS_BASE",
    "exchange": "MOEX",
    "currency": "RUB"
  }'
```

**Ответ (202 Accepted):**
```json
{
  "success": true,
  "message": "Асинхронное сохранение акций запущено",
  "taskId": "550e8400-e29b-41d4-a716-446655440000",
  "endpoint": "/api/instruments/shares",
  "filter": {
    "status": "INSTRUMENT_STATUS_BASE",
    "exchange": "MOEX",
    "currency": "RUB"
  },
  "status": "STARTED",
  "startTime": "2024-01-15T18:30:00.123Z"
}
```

**Отслеживание статуса:**
Используйте `taskId` для проверки статуса операции через System API:
```bash
GET /api/system/logs/{taskId}
```

**Статусы операции:**
- `STARTED` - операция запущена
- `PROCESSING` - операция выполняется
- `COMPLETED` - операция завершена успешно
- `FAILED` - операция завершилась с ошибкой

---

## Фьючерсы (Futures)

### GET /api/instruments/futures

Получение списка фьючерсов из Tinkoff API с фильтрацией. Данные кэшируются для повышения производительности.

**Параметры запроса:**
- `status` (опционально) - статус инструмента: `INSTRUMENT_STATUS_ACTIVE`, `INSTRUMENT_STATUS_BASE`
- `exchange` (опционально) - биржа (например: `MOEX`, `SPB`)
- `currency` (опционально) - валюта (например: `RUB`, `USD`, `EUR`)
- `ticker` (опционально) - тикер фьючерса
- `assetType` (опционально) - тип базового актива (например: `COMMODITY`, `CURRENCY`, `EQUITY`)

**Пример:**
```bash
curl "http://localhost:8083/api/instruments/futures?exchange=MOEX&currency=RUB&assetType=COMMODITY"
```

**Ответ (пример):**
```json
[
  {
    "figi": "FUTSI1224000",
    "ticker": "Si-12.24",
    "assetType": "CURRENCY",
    "basicAsset": "USD/RUB",
    "currency": "RUB",
    "exchange": "MOEX"
  }
]
```

### GET /api/instruments/futures/{identifier}

Получение фьючерса по FIGI или тикеру из базы данных.

API автоматически определяет тип идентификатора аналогично методу для акций.

**Примеры:**
```bash
# По FIGI
curl "http://localhost:8083/api/instruments/futures/FUTSI1224000"

# По тикеру
curl "http://localhost:8083/api/instruments/futures/Si-12.24"
```

**Ответ (пример):**
```json
{
  "figi": "FUTSI1224000",
  "ticker": "Si-12.24",
  "assetType": "CURRENCY",
  "basicAsset": "USD/RUB",
  "currency": "RUB",
  "exchange": "MOEX"
}
```

### POST /api/instruments/futures

**Асинхронное** сохранение фьючерсов в базу данных по фильтрам с защитой от дубликатов.

⚠️ **Важно:** Операция выполняется асинхронно. Метод возвращает `taskId` для отслеживания статуса операции.

**Тело запроса:**
```json
{
  "status": "INSTRUMENT_STATUS_BASE",
  "exchange": "MOEX",
  "currency": "RUB",
  "ticker": "Si-12.24",
  "assetType": "CURRENCY"
}
```

**Параметры фильтра:**
- `status` (опционально) - статус инструмента: `INSTRUMENT_STATUS_UNSPECIFIED`, `INSTRUMENT_STATUS_BASE`, `INSTRUMENT_STATUS_ALL`
- `exchange` (опционально) - биржа: `MOEX`, `moex`, `SPB`, `FORTS_MAIN`, `FORTS_EVENING`, `forts_futures_weekend`, `UNKNOWN` и др.
- `currency` (опционально) - валюта: `RUB`, `USD`, `rub`, `usd`
- `ticker` (опционально) - тикер фьючерса
- `assetType` (опционально) - тип базового актива: `COMMODITY`, `CURRENCY`, `EQUITY`, `BOND`, `INDEX`, `INTEREST_RATE`, `CRYPTO`, `UNKNOWN`

**Пример:**
```bash
curl -X POST "http://localhost:8083/api/instruments/futures" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "INSTRUMENT_STATUS_BASE",
    "exchange": "MOEX",
    "currency": "RUB",
    "assetType": "CURRENCY"
  }'
```

**Ответ (202 Accepted):**
```json
{
  "success": true,
  "message": "Асинхронное сохранение фьючерсов запущено",
  "taskId": "550e8400-e29b-41d4-a716-446655440001",
  "endpoint": "/api/instruments/futures",
  "filter": {
    "status": "INSTRUMENT_STATUS_BASE",
    "exchange": "MOEX",
    "currency": "RUB",
    "assetType": "CURRENCY"
  },
  "status": "STARTED",
  "startTime": "2024-01-15T18:30:00.123Z"
}
```

**Отслеживание статуса:**
Используйте `taskId` для проверки статуса операции через System API:
```bash
GET /api/system/logs/{taskId}
```

---

## Индикативные инструменты (Indicatives)

### GET /api/instruments/indicatives

Получение индикативных инструментов из Tinkoff REST API или БД (fallback). Данные кэшируются для повышения производительности.

**Параметры запроса:**
- `exchange` (опционально) - биржа (например: `MOEX`, `SPB`)
- `currency` (опционально) - валюта (например: `RUB`, `USD`, `EUR`)
- `ticker` (опционально) - тикер индикативного инструмента
- `figi` (опционально) - уникальный идентификатор инструмента

**Пример:**
```bash
curl "http://localhost:8083/api/instruments/indicatives?exchange=MOEX&currency=RUB"
```

**Ответ (пример):**
```json
[
  {
    "figi": "BBG00QPYJ5X0",
    "ticker": "IMOEX",
    "name": "Индекс МосБиржи",
    "currency": "RUB",
    "exchange": "MOEX",
    "classCode": "SPBXM",
    "uid": "e6123145-9665-43e0-8413-cd61d8e6e372",
    "sellAvailableFlag": true,
    "buyAvailableFlag": true
  }
]
```

### GET /api/instruments/indicatives/{identifier}

Получение индикативного инструмента по FIGI или тикеру.

API автоматически определяет тип идентификатора аналогично методам для акций и фьючерсов.

**Примеры:**
```bash
# По FIGI
curl "http://localhost:8083/api/instruments/indicatives/BBG00QPYJ5X0"

# По тикеру
curl "http://localhost:8083/api/instruments/indicatives/IMOEX"
```

**Ответ (пример):**
```json
{
  "figi": "BBG00QPYJ5X0",
  "ticker": "IMOEX",
  "name": "Индекс МосБиржи",
  "currency": "RUB",
  "exchange": "MOEX",
  "classCode": "SPBXM",
  "uid": "e6123145-9665-43e0-8413-cd61d8e6e372",
  "sellAvailableFlag": true,
  "buyAvailableFlag": true
}
```

### POST /api/instruments/indicatives

**Асинхронное** сохранение индикативных инструментов в базу данных по фильтрам с защитой от дубликатов.

⚠️ **Важно:** Операция выполняется асинхронно. Метод возвращает `taskId` для отслеживания статуса операции.

**Тело запроса:**
```json
{
  "exchange": "MOEX",
  "currency": "RUB",
  "ticker": "IMOEX",
  "figi": "BBG00QPYJ5X0"
}
```

**Параметры фильтра:**
- `exchange` (опционально) - биржа: `MOEX`, `moex`, `SPB`, `FORTS_MAIN`, `UNKNOWN` и др.
- `currency` (опционально) - валюта: `RUB`, `USD`, `rub`, `usd`
- `ticker` (опционально) - тикер индикативного инструмента
- `figi` (опционально) - уникальный идентификатор инструмента

**Пример:**
```bash
curl -X POST "http://localhost:8083/api/instruments/indicatives" \
  -H "Content-Type: application/json" \
  -d '{
    "exchange": "MOEX",
    "currency": "RUB",
    "ticker": "IMOEX"
  }'
```

**Ответ (202 Accepted):**
```json
{
  "success": true,
  "message": "Асинхронное сохранение индикативов запущено",
  "taskId": "550e8400-e29b-41d4-a716-446655440002",
  "endpoint": "/api/instruments/indicatives",
  "filter": {
    "exchange": "MOEX",
    "currency": "RUB",
    "ticker": "IMOEX"
  },
  "status": "STARTED",
  "startTime": "2024-01-15T18:30:00.123Z"
}
```

**Отслеживание статуса:**
Используйте `taskId` для проверки статуса операции через System API:
```bash
GET /api/system/logs/{taskId}
```

---

## Статистика

### GET /api/instruments/count

Получение количества инструментов в БД по типам.

**Пример:**
```bash
curl "http://localhost:8083/api/instruments/count"
```

**Ответ (пример):**
```json
{
  "shares": 150,
  "futures": 45,
  "indicatives": 12,
  "total": 207
}
```

---

## Связанные API

#### Управление кэшем
Для работы с кэшем инструментов используйте отдельный API: **[Cache API](docs/api/cache.md)**

Включает операции:
- Прогрев кэша (`POST /api/cache/warmup`)
- Просмотр содержимого кэша (`GET /api/cache/content`)
- Статистика кэша (`GET /api/cache/stats`)
- Очистка кэша (`DELETE /api/cache/clear`)

#### Отслеживание задач
Для проверки статуса асинхронных операций используйте: **[System API](docs/api/system.md)**

Основные эндпоинты:
- Получение лога по taskId (`GET /api/system/logs/{taskId}`)
- Получение всех логов (`GET /api/system/logs`)
- Статистика системы (`GET /api/system/stats`)

---

## Коды ответов

### Успешные ответы
- `200 OK` - данные получены успешно
- `202 Accepted` - асинхронная операция запущена (для POST методов)

### Ошибки
- `400 Bad Request` - некорректные параметры запроса, валидация не пройдена
- `404 Not Found` - инструмент не найден
- `500 Internal Server Error` - внутренняя ошибка сервера

### Примеры ошибок

**400 Bad Request (невалидные параметры):**
```json
{
  "success": false,
  "message": "Ошибка валидации: Невалидная биржа: INVALID_EXCHANGE",
  "timestamp": "2024-01-15T18:30:00",
  "error": "ValidationException",
  "field": "exchange"
}
```

**404 Not Found (инструмент не найден):**
```json
{
  "success": false,
  "message": "Акция с тикером 'INVALID' не найдена в базе данных",
  "error": "NotFound",
  "timestamp": "2024-01-15T18:30:00",
  "identifier": "INVALID",
  "type": "TICKER"
}
```

**500 Internal Server Error:**
```json
{
  "success": false,
  "message": "Ошибка запуска асинхронного сохранения акций: ...",
  "taskId": "550e8400-e29b-41d4-a716-446655440000",
  "error": "InternalServerError"
}
```

---

## 🔧 Технические детали

### Архитектура
- **InstrumentsController** - основной контроллер для инструментов
- **InstrumentService** - сервис для работы с инструментами
- **@Transactional** - все операции сохранения выполняются в транзакциях
- **Кэширование** - использование CachedInstrumentService

### Типы инструментов
- **SHARES** - акции (обыкновенные и привилегированные)
- **FUTURES** - фьючерсы (производные финансовые инструменты)
- **INDICATIVES** - индикативные инструменты

### Источники данных
- **Tinkoff API** - получение данных из внешнего API
- **База данных** - локальное хранение инструментов
- **Кэширование** - промежуточное хранение для производительности

### Фильтрация
- **По бирже** - MOEX, SPB и другие
- **По валюте** - RUB, USD, EUR и другие
- **По тикеру** - точный поиск по символу
- **По FIGI** - уникальный идентификатор
- **По статусу** - активные, базовые и другие

### Автоматические функции
- **Определение типа идентификатора** - FIGI или тикер
- **Прогрев кэша** - автоматический при запуске
- **Защита от дубликатов** - проверка существования
- **Сортировка** - по тикеру по умолчанию

---

## 💡 Примеры использования

### Получение инструментов
```bash
# Получение акций
curl "http://localhost:8083/api/instruments/shares?exchange=MOEX&currency=RUB"

# Получение фьючерсов
curl "http://localhost:8083/api/instruments/futures?exchange=MOEX&currency=RUB"

# Получение индикативов
curl "http://localhost:8083/api/instruments/indicatives?exchange=MOEX&currency=RUB"
```

### Поиск по идентификатору
```bash
# Поиск акции по FIGI
curl "http://localhost:8083/api/instruments/shares/BBG004730N88"

# Поиск акции по тикеру
curl "http://localhost:8083/api/instruments/shares/SBER"

# Поиск фьючерса
curl "http://localhost:8083/api/instruments/futures/FUTSI1224000"

# Поиск индикатива
curl "http://localhost:8083/api/instruments/indicatives/BBG00QPYJ5X0"
```

### Сохранение инструментов (асинхронное)
```bash
# Сохранение акций
curl -X POST "http://localhost:8083/api/instruments/shares" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "INSTRUMENT_STATUS_BASE",
    "exchange": "MOEX",
    "currency": "RUB"
  }'

# Ответ: 202 Accepted с taskId
# Проверка статуса:
curl "http://localhost:8083/api/system/logs/{taskId}"

# Сохранение фьючерсов
curl -X POST "http://localhost:8083/api/instruments/futures" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "INSTRUMENT_STATUS_BASE",
    "exchange": "MOEX",
    "currency": "RUB",
    "assetType": "CURRENCY"
  }'

# Сохранение индикативов
curl -X POST "http://localhost:8083/api/instruments/indicatives" \
  -H "Content-Type: application/json" \
  -d '{
    "exchange": "MOEX",
    "currency": "RUB"
  }'
```

### Статистика
```bash
# Получение количества инструментов
curl "http://localhost:8083/api/instruments/count"
```

---

## ⚠️ Валидация параметров

Все параметры запросов проходят двухуровневую валидацию:

1. **Проверка разрешенных параметров** - проверяется, что переданы только допустимые query-параметры
2. **Валидация значений** - проверяется корректность значений параметров (enum, форматы)

При передаче невалидных параметров возвращается `400 Bad Request` с детальным описанием ошибки.

### Допустимые значения

**Статус инструмента:**
- `INSTRUMENT_STATUS_UNSPECIFIED`
- `INSTRUMENT_STATUS_BASE`
- `INSTRUMENT_STATUS_ALL`

**Биржи:**
- `MOEX`, `moex`, `moex_mrng_evng_e_wknd_dlr`
- `SPB`
- `FORTS_MAIN`, `FORTS_EVENING`, `forts_futures_weekend`
- `UNKNOWN`

**Валюты:**
- `RUB`, `rub`
- `USD`, `usd`
- `EUR`, `eur`

**Типы активов (для фьючерсов):**
- `COMMODITY`
- `CURRENCY`
- `EQUITY`
- `BOND`
- `INDEX`
- `INTEREST_RATE`
- `CRYPTO`
- `UNKNOWN`

---

## 🔄 Жизненный цикл обработки

### Получение инструментов
1. **Запрос** - GET запрос с параметрами фильтрации
2. **Определение источника** - API или база данных
3. **Обработка** - получение данных из выбранного источника
4. **Фильтрация** - применение параметров запроса
5. **Сортировка** - упорядочивание по тикеру
6. **Ответ** - возврат данных клиенту

### Сохранение инструментов (асинхронное)
1. **Запрос** - POST запрос с фильтром
2. **Валидация** - проверка параметров фильтра
3. **Генерация taskId** - создание уникального идентификатора задачи
4. **Логирование** - сохранение записи о начале операции в БД
5. **Запуск асинхронной задачи** - запуск фоновой обработки
6. **Немедленный ответ** - возврат `taskId` клиенту (202 Accepted)
7. **Фоновая обработка** (асинхронно):
   - Получение данных из Tinkoff API
   - Фильтрация по параметрам запроса
   - Проверка дубликатов в БД
   - Сохранение новых инструментов
   - Логирование результата в БД
8. **Отслеживание** - клиент может проверить статус через System API по `taskId`

### Поиск по идентификатору
1. **Запрос** - GET запрос с идентификатором
2. **Определение типа** - FIGI или тикер
3. **Поиск** - в базе данных
4. **Ответ** - возврат найденного инструмента

### Обработка ошибок
1. **Валидация** - проверка параметров запроса
2. **Логирование** - детальная информация об ошибках
3. **Уведомление** - возврат ошибки клиенту
4. **Мониторинг** - отслеживание частоты ошибок