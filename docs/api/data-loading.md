# API — Загрузка данных (`/api/data-loading`)

## POST /api/data-loading/candles
Асинхронная загрузка свечей. Тело запроса опционально.
```json
{
  "instruments": ["BBG004730N88", "BBG004730ZJ9"],
  "date": "2024-01-15",
  "interval": "CANDLE_INTERVAL_1_MIN",
  "assetType": ["SHARES", "FUTURES"]
}
```
Примеры:
```bash
# Все инструменты из БД, вчерашняя дата по умолчанию
curl -X POST "http://localhost:8083/api/data-loading/candles" -H "Content-Type: application/json" -d '{}'

# Конкретные инструменты и дата
curl -X POST "http://localhost:8083/api/data-loading/candles" -H "Content-Type: application/json" -d '{
  "instruments": ["BBG004730N88"],
  "date": "2024-01-15",
  "interval": "CANDLE_INTERVAL_5_MIN"
}'
```
Ответ (пример):
```json
{
  "success": true,
  "message": "Загрузка свечей запущена в фоновом режиме",
  "taskId": "1c7a3f6e-0e1b-4a51-9d79-0f3cbe2c8a77",
  "startTime": "2024-01-15T10:30:00"
}
```

## POST /api/data-loading/candles/{date}
Запуск загрузки свечей за дату (все типы).
```bash
curl -X POST "http://localhost:8083/api/data-loading/candles/2024-01-15"
```
Ответ (пример):
```json
{ "success": true, "message": "Загрузка свечей запущена для 2024-01-15", "date": "2024-01-15" }
```

## POST /api/data-loading/candles/shares/{date}
Загрузка свечей только для акций за дату.
```bash
curl -X POST "http://localhost:8083/api/data-loading/candles/shares/2024-01-15"
```
Ответ (пример):
```json
{ "success": true, "type": "shares", "message": "Загрузка свечей акций запущена для 2024-01-15" }
```

## POST /api/data-loading/candles/futures/{date}
Загрузка свечей только для фьючерсов за дату.
```bash
curl -X POST "http://localhost:8083/api/data-loading/candles/futures/2024-01-15"
```
Ответ (пример):
```json
{ "success": true, "type": "futures", "message": "Загрузка свечей фьючерсов запущена для 2024-01-15" }
```

## POST /api/data-loading/candles/indicatives/{date}
Загрузка свечей только для индикативов за дату.
```bash
curl -X POST "http://localhost:8083/api/data-loading/candles/indicatives/2024-01-15"
```
Ответ (пример):
```json
{ "success": true, "type": "indicatives", "message": "Загрузка свечей индикативов запущена для 2024-01-15" }
```

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

## POST /api/data-loading/evening-session-prices
Загрузка цен вечерней сессии за сегодня.
```bash
curl -X POST "http://localhost:8083/api/data-loading/evening-session-prices"
```
Ответ (пример):
```json
{ "success": true, "message": "Загрузка цен вечерней сессии запущена для сегодня" }
```

## POST /api/data-loading/evening-session-prices/{date}
Загрузка цен вечерней сессии за дату.
```bash
curl -X POST "http://localhost:8083/api/data-loading/evening-session-prices/2024-01-15"
```
Ответ (пример):
```json
{ "success": true, "message": "Загрузка цен вечерней сессии запущена для 2024-01-15", "date": "2024-01-15" }
```

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
