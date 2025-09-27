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

---

## Акции (Shares)

### GET /api/instruments/shares

Получение списка акций с фильтрацией. Поддерживает два источника данных:

**Параметры запроса:**
- `source` (опционально) - источник данных: `"api"` (по умолчанию) или `"database"`
- `status` (опционально) - статус инструмента: `INSTRUMENT_STATUS_ACTIVE`, `INSTRUMENT_STATUS_BASE`
- `exchange` (опционально) - биржа (например: `MOEX`, `SPB`)
- `currency` (опционально) - валюта (например: `RUB`, `USD`, `EUR`)
- `ticker` (опционально) - тикер инструмента (например: `SBER`, `GAZP`)
- `figi` (опционально) - уникальный идентификатор инструмента
- `filter` (опционально) - расширенный фильтр для базы данных (только для `source=database`)

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

Сохранение акций в базу данных по фильтрам с защитой от дубликатов.

**Тело запроса:**
```json
{
  "status": "INSTRUMENT_STATUS_ACTIVE",
  "exchange": "MOEX",
  "currency": "RUB",
  "ticker": "SBER",
  "figi": "BBG004730N88"
}
```

**Пример:**
```bash
curl -X POST "http://localhost:8083/api/instruments/shares" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "INSTRUMENT_STATUS_ACTIVE",
    "exchange": "MOEX",
    "currency": "RUB"
  }'
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Успешно загружено 5 новых акций из 10 найденных.",
  "totalRequested": 10,
  "newItemsSaved": 5,
  "existingItemsSkipped": 5,
  "invalidItemsFiltered": 0,
  "missingFromApi": 0,
  "savedItems": [
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
}
```

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

Сохранение фьючерсов в базу данных по фильтрам с защитой от дубликатов.

**Тело запроса:**
```json
{
  "status": "INSTRUMENT_STATUS_ACTIVE",
  "exchange": "MOEX",
  "currency": "RUB",
  "ticker": "Si-12.24",
  "assetType": "CURRENCY"
}
```

**Пример:**
```bash
curl -X POST "http://localhost:8083/api/instruments/futures" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "INSTRUMENT_STATUS_ACTIVE",
    "exchange": "MOEX",
    "currency": "RUB",
    "assetType": "CURRENCY"
  }'
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Успешно загружено 3 новых фьючерса из 8 найденных.",
  "totalRequested": 8,
  "newItemsSaved": 3,
  "existingItemsSkipped": 5,
  "invalidItemsFiltered": 0,
  "missingFromApi": 0,
  "savedItems": [
    {
      "figi": "FUTSI1224000",
      "ticker": "Si-12.24",
      "assetType": "CURRENCY",
      "basicAsset": "USD/RUB",
      "currency": "RUB",
      "exchange": "MOEX"
    }
  ]
}
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

Сохранение индикативных инструментов в базу данных по фильтрам с защитой от дубликатов.

**Тело запроса:**
```json
{
  "exchange": "MOEX",
  "currency": "RUB",
  "ticker": "IMOEX",
  "figi": "BBG00QPYJ5X0"
}
```

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

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Успешно загружено 2 новых индикативных инструмента из 5 найденных.",
  "totalRequested": 5,
  "newItemsSaved": 2,
  "existingItemsSkipped": 3,
  "invalidItemsFiltered": 0,
  "missingFromApi": 0,
  "savedItems": [
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
}
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

### Управление кэшем
Для работы с кэшем инструментов используйте отдельный API: **[Cache API](/api/cache)**

Включает операции:
- Прогрев кэша (`POST /api/cache/warmup`)
- Просмотр содержимого кэша (`GET /api/cache/content`)
- Статистика кэша (`GET /api/cache/stats`)
- Очистка кэша (`DELETE /api/cache/clear`)

---

## Коды ошибок

- `200 OK` - Успешный запрос
- `404 Not Found` - Инструмент не найден
- `400 Bad Request` - Некорректные параметры запроса
- `500 Internal Server Error` - Внутренняя ошибка сервера

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

### Сохранение инструментов
```bash
# Сохранение акций
curl -X POST "http://localhost:8083/api/instruments/shares" \
  -H "Content-Type: application/json" \
  -d '{"exchange": "MOEX", "currency": "RUB"}'

# Сохранение фьючерсов
curl -X POST "http://localhost:8083/api/instruments/futures" \
  -H "Content-Type: application/json" \
  -d '{"exchange": "MOEX", "currency": "RUB", "assetType": "CURRENCY"}'

# Сохранение индикативов
curl -X POST "http://localhost:8083/api/instruments/indicatives" \
  -H "Content-Type: application/json" \
  -d '{"exchange": "MOEX", "currency": "RUB"}'
```

### Статистика
```bash
# Получение количества инструментов
curl "http://localhost:8083/api/instruments/count"
```

---

## ⚠️ Коды ответов

### Успешные ответы
- **200 OK** - данные получены или сохранены успешно

### Ошибки
- **404 Not Found** - инструмент не найден
- **400 Bad Request** - некорректные параметры запроса
- **500 Internal Server Error** - внутренняя ошибка сервера

### Примеры ошибок
```json
{
  "error": "Инструмент не найден",
  "message": "Инструмент с идентификатором SBER не найден в базе данных",
  "timestamp": "2024-01-15T18:30:00"
}
```

---

## 🔄 Жизненный цикл обработки

### Получение инструментов
1. **Запрос** - GET запрос с параметрами фильтрации
2. **Определение источника** - API или база данных
3. **Обработка** - получение данных из выбранного источника
4. **Фильтрация** - применение параметров запроса
5. **Сортировка** - упорядочивание по тикеру
6. **Ответ** - возврат данных клиенту

### Сохранение инструментов
1. **Запрос** - POST запрос с фильтром
2. **Получение из API** - загрузка данных из Tinkoff API
3. **Фильтрация** - применение параметров запроса
4. **Проверка дубликатов** - исключение существующих
5. **Сохранение** - запись в базу данных
6. **Ответ** - возврат статистики клиенту

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