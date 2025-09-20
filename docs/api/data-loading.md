# API — Загрузка данных (`/api/data-loading`)

## POST /api/data-loading/candles/minute
Асинхронная загрузка минутных свечей в таблицу `invest.minute_candles`. Тело запроса опционально.
```json
{
  "instruments": ["BBG004730N88", "BBG004730ZJ9"],
  "date": "2024-01-15",
  "assetType": ["SHARES", "FUTURES"]
}
```
Примеры:
```bash
# Все инструменты из БД, сегодняшняя дата по умолчанию
curl -X POST "http://localhost:8083/api/data-loading/candles/minute" -H "Content-Type: application/json" -d '{}'

# Конкретные инструменты и дата
curl -X POST "http://localhost:8083/api/data-loading/candles/minute" -H "Content-Type: application/json" -d '{
  "instruments": ["BBG004730N88"],
  "date": "2024-01-15",
  "assetType": ["SHARES"]
}'
```
Ответ (пример):
```json
{
  "success": true,
  "message": "Загрузка минутных свечей запущена в фоновом режиме",
  "startTime": "2024-01-15T10:30:00",
  "taskId": "1c7a3f6e-0e1b-4a51-9d79-0f3cbe2c8a77"
}
```

## POST /api/data-loading/candles/daily
Асинхронная загрузка дневных свечей в таблицу `invest.daily_candles`. Тело запроса опционально.
```json
{
  "instruments": ["BBG004730N88", "BBG004730ZJ9"],
  "date": "2024-01-15",
  "assetType": ["SHARES", "FUTURES"]
}
```
Примеры:
```bash
# Все инструменты из БД, сегодняшняя дата по умолчанию
curl -X POST "http://localhost:8083/api/data-loading/candles/daily" -H "Content-Type: application/json" -d '{}'

# Конкретные инструменты и дата
curl -X POST "http://localhost:8083/api/data-loading/candles/daily" -H "Content-Type: application/json" -d '{
  "instruments": ["BBG004730N88"],
  "date": "2024-01-15",
  "assetType": ["SHARES"]
}'
```
Ответ (пример):
```json
{
  "success": true,
  "message": "Загрузка дневных свечей запущена в фоновом режиме",
  "startTime": "2024-01-15T10:30:00",
  "taskId": "2d8b4g7f-1f2c-5b62-ae80-1g4dce3d9b88"
}
```

**Примечание:** 
- Минутные свечи сохраняются в таблицу `invest.minute_candles` с интервалом CANDLE_INTERVAL_1_MIN
- Дневные свечи сохраняются в таблицу `invest.daily_candles` с интервалом CANDLE_INTERVAL_DAY
- Каждый эндпоинт работает независимо и возвращает уникальный taskId для отслеживания прогресса

## POST /api/data-loading/candles/minute/{date}
Запуск загрузки минутных свечей за конкретную дату.
```bash
curl -X POST "http://localhost:8083/api/data-loading/candles/minute/2024-01-15"
```
Ответ (пример):
```json
{
  "success": true,
  "message": "Загрузка минутных свечей запущена для 2024-01-15",
  "startTime": "2024-01-15T10:30:00",
  "taskId": "1c7a3f6e-0e1b-4a51-9d79-0f3cbe2c8a77"
}
```

## POST /api/data-loading/candles/daily/{date}
Запуск загрузки дневных свечей за конкретную дату.
```bash
curl -X POST "http://localhost:8083/api/data-loading/candles/daily/2024-01-15"
```
Ответ (пример):
```json
{
  "success": true,
  "message": "Загрузка дневных свечей запущена для 2024-01-15",
  "startTime": "2024-01-15T10:30:00",
  "taskId": "2d8b4g7f-1f2c-5b62-ae80-1g4dce3d9b88"
}
```

## POST /api/data-loading/candles/shares/minute/{date}
Загрузка минутных свечей только для акций за дату.
```bash
curl -X POST "http://localhost:8083/api/data-loading/candles/shares/minute/2024-01-15"
```
Ответ (пример):
```json
{
  "success": true,
  "message": "Загрузка минутных свечей акций запущена для 2024-01-15",
  "startTime": "2024-01-15T10:30:00",
  "taskId": "1c7a3f6e-0e1b-4a51-9d79-0f3cbe2c8a77"
}
```

## POST /api/data-loading/candles/shares/daily/{date}
Загрузка дневных свечей только для акций за дату.
```bash
curl -X POST "http://localhost:8083/api/data-loading/candles/shares/daily/2024-01-15"
```
Ответ (пример):
```json
{
  "success": true,
  "message": "Загрузка дневных свечей акций запущена для 2024-01-15",
  "startTime": "2024-01-15T10:30:00",
  "taskId": "2d8b4g7f-1f2c-5b62-ae80-1g4dce3d9b88"
}
```

## POST /api/data-loading/candles/futures/minute/{date}
Загрузка минутных свечей только для фьючерсов за дату.
```bash
curl -X POST "http://localhost:8083/api/data-loading/candles/futures/minute/2024-01-15"
```
Ответ (пример):
```json
{
  "success": true,
  "message": "Загрузка минутных свечей фьючерсов запущена для 2024-01-15",
  "startTime": "2024-01-15T10:30:00",
  "taskId": "3e9c5h8g-2g3d-6c73-bf91-2h5edf4e0c99"
}
```

## POST /api/data-loading/candles/futures/daily/{date}
Загрузка дневных свечей только для фьючерсов за дату.
```bash
curl -X POST "http://localhost:8083/api/data-loading/candles/futures/daily/2024-01-15"
```
Ответ (пример):
```json
{
  "success": true,
  "message": "Загрузка дневных свечей фьючерсов запущена для 2024-01-15",
  "startTime": "2024-01-15T10:30:00",
  "taskId": "4f0d6i9h-3h4e-7d84-cg02-3i6feg5f1d00"
}
```

## POST /api/data-loading/candles/indicatives/minute/{date}
Загрузка минутных свечей только для индикативных инструментов за дату.
```bash
curl -X POST "http://localhost:8083/api/data-loading/candles/indicatives/minute/2024-01-15"
```
Ответ (пример):
```json
{
  "success": true,
  "message": "Загрузка минутных свечей индикативов запущена для 2024-01-15",
  "startTime": "2024-01-15T10:30:00",
  "taskId": "5g1e7j0i-4i5f-8e95-dh13-4j7gfh6g2e11"
}
```

## GET /api/data-loading/candles/indicatives/minute/{date}
Получение минутных свечей индикативных инструментов за дату без сохранения в базу данных. Данные получаются напрямую от API T-Invest.
```bash
curl -X GET "http://localhost:8083/api/data-loading/candles/indicatives/minute/2024-01-15"
```
Ответ (пример):
```json
{
  "success": true,
  "message": "Данные получены успешно",
  "date": "2024-01-15",
  "statistics": {
    "totalInstruments": 25,
    "processedInstruments": 25,
    "successfulInstruments": 23,
    "noDataInstruments": 1,
    "errorInstruments": 1,
    "successRate": 92.0,
    "totalProcessingTimeMs": 15420,
    "averageProcessingTimePerInstrumentMs": 616
  },
  "totalCandles": 1840,
  "candles": [
    {
      "figi": "BBG004730N88",
      "instrumentName": "Сбербанк",
      "volume": 1000000,
      "high": 250.50,
      "low": 248.30,
      "open": 249.00,
      "close": 250.20,
      "time": "2024-01-15T10:00:00Z",
      "isComplete": true
    }
  ],
  "failedInstruments": [],
  "noDataInstruments": [
    {
      "figi": "BBG004730ZJ9",
      "name": "Газпром",
      "reason": "Нет торговых данных за указанную дату",
      "processingTimeMs": 450
    }
  ],
  "errorInstruments": [
    {
      "figi": "BBG004730ABC",
      "name": "Неизвестный инструмент",
      "errorType": "TIMEOUT",
      "reason": "Превышено время ожидания ответа от API",
      "processingTimeMs": 5000
    }
  ]
}
```

### Поля ответа:

**Основные поля:**
- `success` - статус операции (boolean)
- `message` - сообщение о результате (string)
- `date` - запрашиваемая дата (string)
- `totalCandles` - общее количество полученных свечей (number)
- `candles` - массив свечей с данными (array)

**Статистика (`statistics`):**
- `totalInstruments` - общее количество инструментов в базе (number)
- `processedInstruments` - количество обработанных инструментов (number)
- `successfulInstruments` - количество успешно обработанных инструментов (number)
- `noDataInstruments` - количество инструментов без данных (number)
- `errorInstruments` - количество инструментов с ошибками (number)
- `successRate` - процент успешной обработки (number, %)
- `totalProcessingTimeMs` - общее время обработки в миллисекундах (number)
- `averageProcessingTimePerInstrumentMs` - среднее время обработки одного инструмента (number)

**Детали неудач:**
- `noDataInstruments` - массив инструментов без данных с причинами
- `errorInstruments` - массив инструментов с ошибками и типами ошибок
- `failedInstruments` - общий массив неудачных инструментов (для совместимости)

## POST /api/data-loading/candles/indicatives/daily/{date}
Загрузка дневных свечей только для индикативных инструментов за дату.
```bash
curl -X POST "http://localhost:8083/api/data-loading/candles/indicatives/daily/2024-01-15"
```
Ответ (пример):
```json
{
  "success": true,
  "message": "Загрузка дневных свечей индикативов запущена для 2024-01-15",
  "startTime": "2024-01-15T10:30:00",
  "taskId": "6h2f8k1j-5j6g-9f06-ei24-5k8hgi7h3f22"
}
```

## GET /api/data-loading/candles/indicatives/daily/{date}
Получение дневных свечей индикативных инструментов за дату без сохранения в базу данных. Данные получаются напрямую от API T-Invest.
```bash
curl -X GET "http://localhost:8083/api/data-loading/candles/indicatives/daily/2024-01-15"
```
Ответ (пример):
```json
{
  "success": true,
  "message": "Данные получены успешно",
  "date": "2024-01-15",
  "statistics": {
    "totalInstruments": 15,
    "processedInstruments": 15,
    "successfulInstruments": 14,
    "noDataInstruments": 0,
    "errorInstruments": 1,
    "successRate": 93.33,
    "totalProcessingTimeMs": 8200,
    "averageProcessingTimePerInstrumentMs": 546
  },
  "totalCandles": 14,
  "candles": [
    {
      "figi": "BBG004730N88",
      "instrumentName": "Сбербанк",
      "volume": 1500000,
      "high": 102.30,
      "low": 99.80,
      "open": 100.50,
      "close": 101.20,
      "time": "2024-01-15T00:00:00Z",
      "isComplete": true
    }
  ],
  "failedInstruments": [],
  "noDataInstruments": [],
  "errorInstruments": [
    {
      "figi": "BBG004730ABC",
      "name": "Неизвестный инструмент",
      "errorType": "TIMEOUT",
      "reason": "Превышено время ожидания ответа от API",
      "processingTimeMs": 5000
    }
  ]
}
```

### Поля ответа:

**Основные поля:**
- `success` - статус операции (boolean)
- `message` - сообщение о результате (string)
- `date` - запрашиваемая дата (string)
- `totalCandles` - общее количество полученных свечей (number)
- `candles` - массив свечей с данными (array)

**Статистика (`statistics`):**
- `totalInstruments` - общее количество инструментов в базе (number)
- `processedInstruments` - количество обработанных инструментов (number)
- `successfulInstruments` - количество успешно обработанных инструментов (number)
- `noDataInstruments` - количество инструментов без данных (number)
- `errorInstruments` - количество инструментов с ошибками (number)
- `successRate` - процент успешной обработки (number, %)
- `totalProcessingTimeMs` - общее время обработки в миллисекундах (number)
- `averageProcessingTimePerInstrumentMs` - среднее время обработки одного инструмента (number)

**Детали неудач:**
- `noDataInstruments` - массив инструментов без данных с причинами
- `errorInstruments` - массив инструментов с ошибками и типами ошибок
- `failedInstruments` - общий массив неудачных инструментов (для совместимости)

## GET /api/data-loading/candles/shares/minute/{date}
Получение минутных свечей акций за дату без сохранения в базу данных. Данные получаются напрямую от API T-Invest.
```bash
curl -X GET "http://localhost:8083/api/data-loading/candles/shares/minute/2024-01-15"
```
Ответ имеет ту же структуру, что и для индикативов, но содержит данные по акциям.

## GET /api/data-loading/candles/shares/daily/{date}
Получение дневных свечей акций за дату без сохранения в базу данных. Данные получаются напрямую от API T-Invest.
```bash
curl -X GET "http://localhost:8083/api/data-loading/candles/shares/daily/2024-01-15"
```
Ответ имеет ту же структуру, что и для индикативов, но содержит данные по акциям.

## GET /api/data-loading/candles/futures/minute/{date}
Получение минутных свечей фьючерсов за дату без сохранения в базу данных. Данные получаются напрямую от API T-Invest.
```bash
curl -X GET "http://localhost:8083/api/data-loading/candles/futures/minute/2024-01-15"
```
Ответ имеет ту же структуру, что и для индикативов, но содержит данные по фьючерсам.

## GET /api/data-loading/candles/futures/daily/{date}
Получение дневных свечей фьючерсов за дату без сохранения в базу данных. Данные получаются напрямую от API T-Invest.
```bash
curl -X GET "http://localhost:8083/api/data-loading/candles/futures/daily/2024-01-15"
```
Ответ имеет ту же структуру, что и для индикативов, но содержит данные по фьючерсам.

**Примечание:** Все GET методы для свечей (акции, фьючерсы, индикативы) имеют одинаковую структуру ответа с расширенной статистикой, включая:
- Детальную статистику обработки инструментов
- Классификацию ошибок (TIMEOUT, SERVICE_UNAVAILABLE, PERMISSION_DENIED, INVALID_ARGUMENT)
- Время обработки каждого инструмента
- Процент успешной обработки
- Детали по неудачным инструментам

## POST /api/data-loading/close-prices
Загрузка и сохранение цен закрытия за сегодня (только акции и фьючерсы, без indicatives).
```bash
curl -X POST "http://localhost:8083/api/data-loading/close-prices"
```
Ответ (пример):
```json
{ "success": true, "message": "Загрузка цен закрытия запущена для сегодня" }
```

## POST /api/data-loading/close-prices/save
Сохранение цен закрытия по указанным инструментам (только акции и фьючерсы, без indicatives).

Тело запроса (пример):
```json
{ "instruments": ["BBG004730N88", "BBG004730ZJ9"] }
```
Пример запроса:
```bash
curl -X POST "http://localhost:8083/api/data-loading/close-prices/save" -H "Content-Type: application/json" -d '{
  "instruments": ["BBG004730N88", "BBG004730ZJ9"]
}'
```
Ответ (пример):
```json
{
  "success": true,
  "message": "Успешно загружено 15 новых цен закрытия из 20 найденных.",
  "totalRequested": 20,
  "newItemsSaved": 15,
  "existingItemsSkipped": 5,
  "savedItems": [ { "figi": "BBG004730N88", "tradingDate": "2024-01-15", "closePrice": 250.75 } ]
}
```

**Примечание:** Цены закрытия сохраняются в таблице `invest.close_prices`. Эндпоинт загружает только акции и фьючерсы, исключая indicatives.

## GET /api/data-loading/close-prices/shares
Получение цен закрытия для всех акций из T-INVEST API (без сохранения в БД). Загружает только акции в рублях из БД.

Пример запроса:
```bash
curl "http://localhost:8083/api/data-loading/close-prices/shares"
```

Ответ (пример):
```json
{
  "success": true,
  "message": "Цены закрытия для акций получены успешно",
  "data": [
    {
      "figi": "BBG004730N88",
      "tradingDate": "2024-01-15",
      "closePrice": 250.75,
      "eveningSessionPrice": 251.00
    },
    {
      "figi": "BBG004730ZJ9",
      "tradingDate": "2024-01-15",
      "closePrice": 180.50,
      "eveningSessionPrice": 181.00
    }
  ],
  "count": 2,
  "timestamp": "2024-01-15T10:30:00"
}
```

## GET /api/data-loading/close-prices/futures
Получение цен закрытия для всех фьючерсов из T-INVEST API (без сохранения в БД). Загружает только фьючерсы в рублях из БД.

Пример запроса:
```bash
curl "http://localhost:8083/api/data-loading/close-prices/futures"
```

Ответ (пример):
```json
{
  "success": true,
  "message": "Цены закрытия для фьючерсов получены успешно",
  "data": [
    {
      "figi": "FUTSILV-3.24",
      "tradingDate": "2024-01-15",
      "closePrice": 75000.00,
      "eveningSessionPrice": 75100.00
    },
    {
      "figi": "FUTGOLD-3.24",
      "tradingDate": "2024-01-15",
      "closePrice": 250000.00,
      "eveningSessionPrice": 250500.00
    }
  ],
  "count": 2,
  "timestamp": "2024-01-15T10:30:00"
}
```

## GET /api/data-loading/close-prices/{figi}
Получение цены закрытия по конкретному инструменту из T-INVEST API (без сохранения в БД). Работает только с акциями и фьючерсами.

Пример запроса:
```bash
curl "http://localhost:8083/api/data-loading/close-prices/BBG0063FKTD1"
```
Ответ (пример):
```json
{
  "success": true,
  "message": "Цена закрытия получена успешно",
  "data": {
    "figi": "BBG0063FKTD1",
    "tradingDate": "2024-01-15",
    "closePrice": 1250.50,
    "eveningSessionPrice": null
  },
  "timestamp": "2024-01-15T10:30:00"
}
```
Ответ при отсутствии данных:
```json
{
  "success": false,
  "message": "Цена закрытия не найдена для инструмента: BBG0063FKTD1",
  "figi": "BBG0063FKTD1",
  "timestamp": "2024-01-15T10:30:00"
}
```

## Цены вечерней сессии

**Обзор:** Вечерняя сессия на Московской бирже проводится с 19:05 до 23:50 по московскому времени. API предоставляет функциональность для загрузки, получения и управления ценами закрытия вечерней сессии.

**Время работы:**
- Вечерняя сессия: 19:05-23:50 МСК
- Автоматическая загрузка: 02:00 МСК (ежедневно, кроме выходных)
- Поддерживаемые инструменты: Акции и фьючерсы в рублях
- Выходные дни: Суббота и воскресенье (загрузка не производится)

### POST /api/data-loading/evening-session-prices
Синхронная загрузка цен вечерней сессии за сегодня по всем инструментам (акции и фьючерсы).

```bash
curl -X POST "http://localhost:8083/api/data-loading/evening-session-prices"
```

Ответ (пример):
```json
{
  "success": true,
  "message": "Успешно загружено 25 новых цен вечерней сессии из 30 найденных.",
  "totalRequested": 30,
  "newItemsSaved": 25,
  "existingItemsSkipped": 5,
  "invalidItemsFiltered": 0,
  "missingFromApi": 0,
  "savedItems": [
    {
      "priceDate": "2024-01-15",
      "figi": "BBG004730N88",
      "closePrice": 251.00,
      "instrumentType": "SHARES",
      "currency": "RUB",
      "exchange": "MOEX"
    }
  ],
  "timestamp": "2024-01-15T10:30:00"
}
```

### POST /api/data-loading/evening-session-prices/save
Синхронная точечная загрузка цен вечерней сессии по указанным инструментам.

Тело запроса (опционально):
```json
{
  "instruments": ["BBG004730N88", "BBG004730ZJ9"]
}
```

Пример запроса:
```bash
# Загрузка всех инструментов
curl -X POST "http://localhost:8083/api/data-loading/evening-session-prices/save"

# Загрузка конкретных инструментов
curl -X POST "http://localhost:8083/api/data-loading/evening-session-prices/save" \
  -H "Content-Type: application/json" \
  -d '{"instruments": ["BBG004730N88", "BBG004730ZJ9"]}'
```

Ответ (пример):
```json
{
  "success": true,
  "message": "Успешно загружено 2 новых цены вечерней сессии из 2 найденных.",
  "totalRequested": 2,
  "newItemsSaved": 2,
  "existingItemsSkipped": 0,
  "invalidItemsFiltered": 0,
  "missingFromApi": 0,
  "savedItems": [
    {
      "priceDate": "2024-01-15",
      "figi": "BBG004730N88",
      "closePrice": 251.00,
      "instrumentType": "SHARES",
      "currency": "RUB",
      "exchange": "MOEX"
    }
  ]
}
```

### POST /api/data-loading/evening-session-prices/{date}
Загрузка цен вечерней сессии за конкретную дату.

```bash
curl -X POST "http://localhost:8083/api/data-loading/evening-session-prices/2024-01-15"
```

Ответ (пример):
```json
{
  "success": true,
  "message": "Успешно загружено 25 новых цен вечерней сессии из 30 найденных.",
  "totalRequested": 30,
  "newItemsSaved": 25,
  "existingItemsSkipped": 5,
  "invalidItemsFiltered": 0,
  "missingFromApi": 0,
  "savedItems": [...]
}
```

**Примечание:** В выходные дни (суббота и воскресенье) вечерняя сессия не проводится. При попытке загрузки за выходной день вернется сообщение об ошибке.

### GET /api/data-loading/evening-session-prices/shares
Получение цен вечерней сессии для всех акций из T-INVEST API (без сохранения в БД). Загружает только акции в рублях из БД.

```bash
curl "http://localhost:8083/api/data-loading/evening-session-prices/shares"
```

Ответ (пример):
```json
{
  "success": true,
  "message": "Цены вечерней сессии для акций получены успешно",
  "data": [
    {
      "figi": "BBG004730N88",
      "tradingDate": "2024-01-15",
      "closePrice": 250.75,
      "eveningSessionPrice": 251.00
    },
    {
      "figi": "BBG004730ZJ9",
      "tradingDate": "2024-01-15",
      "closePrice": 180.50,
      "eveningSessionPrice": 181.00
    }
  ],
  "count": 2,
  "timestamp": "2024-01-15T10:30:00"
}
```

### GET /api/data-loading/evening-session-prices/futures
Получение цен вечерней сессии для всех фьючерсов из T-INVEST API (без сохранения в БД). Загружает только фьючерсы в рублях из БД.

```bash
curl "http://localhost:8083/api/data-loading/evening-session-prices/futures"
```

Ответ (пример):
```json
{
  "success": true,
  "message": "Цены вечерней сессии для фьючерсов получены успешно",
  "data": [
    {
      "figi": "FUTSILV-3.24",
      "tradingDate": "2024-01-15",
      "closePrice": 75000.00,
      "eveningSessionPrice": 75100.00
    },
    {
      "figi": "FUTGOLD-3.24",
      "tradingDate": "2024-01-15",
      "closePrice": 250000.00,
      "eveningSessionPrice": 250500.00
    }
  ],
  "count": 2,
  "timestamp": "2024-01-15T10:30:00"
}
```

### GET /api/data-loading/evening-session-prices/{figi}
Получение цены вечерней сессии по конкретному инструменту из T-INVEST API (без сохранения в БД). Работает только с акциями и фьючерсами.

```bash
curl "http://localhost:8083/api/data-loading/evening-session-prices/BBG004730N88"
```

Ответ (пример):
```json
{
  "success": true,
  "message": "Цена вечерней сессии получена успешно",
  "data": {
    "figi": "BBG004730N88",
    "tradingDate": "2024-01-15",
    "closePrice": 250.75,
    "eveningSessionPrice": 251.00
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

Ответ при отсутствии данных:
```json
{
  "success": false,
  "message": "Цена вечерней сессии не найдена для инструмента: BBG004730N88",
  "figi": "BBG004730N88",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Примечание:** Цены вечерней сессии сохраняются в таблице `invest.close_prices_evening_session`. Эндпоинты загружают только акции и фьючерсы, исключая indicatives. Вечерняя сессия проводится с 19:05 до 23:50 по московскому времени.

### Структуры данных

**ClosePriceEveningSessionRequestDto:**
```json
{
  "instruments": ["BBG004730N88", "BBG004730ZJ9"]
}
```

**ClosePriceEveningSessionDto (для сохранения):**
```json
{
  "priceDate": "2024-01-15",
  "figi": "BBG004730N88",
  "closePrice": 251.00,
  "instrumentType": "SHARES",
  "currency": "RUB",
  "exchange": "MOEX"
}
```

### Обработка ошибок

**Выходные дни:**
```json
{
  "success": false,
  "message": "В выходные дни (суббота и воскресенье) вечерняя сессия не проводится. Дата: 2024-01-13"
}
```

**Ошибки API:**
```json
{
  "success": false,
  "message": "Ошибка получения цены вечерней сессии: [описание ошибки]",
  "figi": "BBG004730N88",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Ограничения

1. **Инструменты:** Только акции и фьючерсы в рублях
2. **Выходные дни:** Загрузка не производится в субботу и воскресенье
3. **Время сессии:** Данные доступны только для периода 19:05-23:50 МСК
4. **Дублирование:** Существующие записи не перезаписываются
5. **Валидация:** Фильтруются записи с некорректными датами (1970-01-01)

## POST /api/data-loading/morning-session-prices
Загрузка цен утренней сессии за сегодня.
```bash
curl -X POST "http://localhost:8083/api/data-loading/morning-session-prices"
```
Ответ (пример):
```json
{ "success": true, "message": "Загрузка цен утренней сессии запущена для сегодня" }
```

## POST /api/data-loading/morning-session-prices/{date}
Загрузка цен утренней сессии за дату.
```bash
curl -X POST "http://localhost:8083/api/data-loading/morning-session-prices/2024-01-15"
```
Ответ (пример):
```json
{ "success": true, "message": "Загрузка цен утренней сессии запущена для 2024-01-15", "date": "2024-01-15" }
```

## POST /api/data-loading/last-trades
Асинхронная загрузка обезличенных сделок за последний час.
```bash
curl -X POST "http://localhost:8083/api/data-loading/last-trades" -H "Content-Type: application/json" -d '{
  "figis": ["ALL_SHARES"],
  "tradeSource": "TRADE_SOURCE_ALL"
}'
```
Ответ (пример):
```json
{ "success": true, "message": "Загрузка обезличенных сделок запущена в фоновом режиме" }
```

## GET /api/data-loading/status/{taskId}
Получение статуса задачи загрузки.
```bash
curl "http://localhost:8083/api/data-loading/status/1c7a3f6e-0e1b-4a51-9d79-0f3cbe2c8a77"
```
Ответ (пример):
```json
{ "success": true, "taskId": "1c7a3f6e-0e1b-4a51-9d79-0f3cbe2c8a77", "status": "processing", "message": "Задача выполняется" }
```
