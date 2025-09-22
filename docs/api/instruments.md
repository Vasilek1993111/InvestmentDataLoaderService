# API — Инструменты (`/api/instruments`)

## Обзор

API для работы с финансовыми инструментами предоставляет единый интерфейс для работы с тремя типами инструментов:
- **Акции (Shares)** - обыкновенные и привилегированные акции
- **Фьючерсы (Futures)** - производные финансовые инструменты  
- **Индикативы (Indicatives)** - индикативные инструменты

### Основные возможности:
- Получение инструментов из внешнего API Tinkoff или локальной БД
- Фильтрация по различным параметрам (биржа, валюта, тикер, FIGI)
- Сохранение инструментов в БД с защитой от дубликатов
- Поиск инструментов по FIGI или тикеру
- Получение статистики по количеству инструментов

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

## Коды ошибок

- `200 OK` - Успешный запрос
- `404 Not Found` - Инструмент не найден
- `400 Bad Request` - Некорректные параметры запроса
- `500 Internal Server Error` - Внутренняя ошибка сервера

## Особенности

1. **Кэширование** - данные из API кэшируются для повышения производительности
2. **Защита от дубликатов** - при сохранении инструменты не дублируются в БД
3. **Автоматическое определение типа идентификатора** - API сам определяет FIGI или тикер
4. **Fallback для индикативов** - при недоступности REST API используются данные из БД
5. **Сортировка результатов** - все списки инструментов сортируются по тикеру