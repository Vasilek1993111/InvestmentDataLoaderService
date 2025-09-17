# API — Инструменты (`/api/instruments`)

## GET /api/instruments/shares
Получение списка акций из T‑Bank API с возможностью фильтрации (без сохранения в БД).

Параметры запроса:
- `status` — INSTRUMENT_STATUS_BASE | INSTRUMENT_STATUS_ALL
- `exchange` — биржа (например, moex_mrng_evng_e_wknd_dlr)
- `currency` — RUB | USD | EUR | ...
- `ticker` — тикер (SBER, GAZP, ...)
- `figi` — FIGI инструмента

Пример:
```bash
curl "http://localhost:8083/api/instruments/shares?status=INSTRUMENT_STATUS_BASE&exchange=moex_mrng_evng_e_wknd_dlr&currency=RUB"
```

Ответ (пример):
```json
[
  {
    "figi": "BBG004730N88",
    "ticker": "SBER",
    "name": "Сбербанк",
    "currency": "RUB",
    "exchange": "moex_mrng_evng_e_wknd_dlr",
    "sector": "Financial Services",
    "tradingStatus": "SECURITY_TRADING_STATUS_NORMAL_TRADING"
  }
]
```

## GET /api/instruments/shares/{identifier}
Получение акции по FIGI или тикеру из базы данных.

API автоматически определяет тип идентификатора:
- Если идентификатор длиннее 10 символов или содержит `-` или `_` → поиск по FIGI
- Иначе → поиск по тикеру

Примеры:
```bash
# По FIGI
curl "http://localhost:8083/api/instruments/shares/BBG004730N88"

# По тикеру
curl "http://localhost:8083/api/instruments/shares/SBER"
```

Ответ (пример):
```json
{
  "figi": "BBG004730N88",
  "ticker": "SBER",
  "name": "Сбербанк",
  "currency": "RUB",
  "exchange": "moex_mrng_evng_e_wknd_dlr",
  "sector": "Financial Services",
  "tradingStatus": "SECURITY_TRADING_STATUS_NORMAL_TRADING"
}
```

## POST /api/instruments/shares
Сохранение акций в базу данных по фильтрам.
```bash
curl -X POST "http://localhost:8083/api/instruments/shares" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "INSTRUMENT_STATUS_BASE",
    "exchange": "moex_mrng_evng_e_wknd_dlr",
    "currency": "RUB",
    "ticker": "SBER"
  }'
```

Ответ (пример):
```json
{
  "success": true,
  "message": "Успешно загружено 5 новых акций из 10 найденных.",
  "totalRequested": 10,
  "newItemsSaved": 5,
  "existingItemsSkipped": 5,
  "savedItems": [ { "figi": "BBG004730N88", "ticker": "SBER" } ]
}
```

---

## GET /api/instruments/futures
Получение списка фьючерсов из T‑Bank API с фильтрами (без сохранения в БД).

Параметры:
- `status`, `exchange`, `currency`, `ticker`, `assetType` (TYPE_SECURITY | TYPE_COMMODITY | TYPE_CURRENCY | TYPE_INDEX)

Пример:
```bash
curl "http://localhost:8083/api/instruments/futures?status=INSTRUMENT_STATUS_BASE&exchange=FORTS_EVENING&currency=RUB&assetType=TYPE_CURRENCY"
```

Ответ (пример):
```json
[
  {
    "figi": "FUTSI1224000",
    "ticker": "Si-12.24",
    "assetType": "TYPE_CURRENCY",
    "basicAsset": "USD/RUB",
    "currency": "RUB",
    "exchange": "FORTS_EVENING"
  }
]
```

## GET /api/instruments/futures/{identifier}
Получение фьючерса по FIGI или тикеру из базы данных.

API автоматически определяет тип идентификатора:
- Если идентификатор длиннее 10 символов или содержит `-` или `_` → поиск по FIGI
- Иначе → поиск по тикеру

Примеры:
```bash
# По FIGI
curl "http://localhost:8083/api/instruments/futures/FUTSI1224000"

# По тикеру
curl "http://localhost:8083/api/instruments/futures/Si-12.24"
```

Ответ (пример):
```json
{
  "figi": "FUTSI1224000",
  "ticker": "Si-12.24",
  "assetType": "TYPE_CURRENCY",
  "basicAsset": "USD/RUB",
  "currency": "RUB",
  "exchange": "FORTS_EVENING"
}
```

## POST /api/instruments/futures
Сохранение фьючерсов по фильтрам.
```bash
curl -X POST "http://localhost:8083/api/instruments/futures" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "INSTRUMENT_STATUS_BASE",
    "exchange": "FORTS_EVENING",
    "currency": "RUB",
    "ticker": "Si-12.24",
    "assetType": "TYPE_CURRENCY"
  }'
```

Ответ (пример):
```json
{
  "success": true,
  "message": "Успешно загружено 3 новых фьючерса из 8 найденных.",
  "totalRequested": 8,
  "newItemsSaved": 3,
  "existingItemsSkipped": 5,
  "savedItems": [ { "figi": "FUTSI1224000", "ticker": "Si-12.24" } ]
}
```

---

## GET /api/instruments/indicatives
Получение индикативных инструментов (индексы/товары) с фильтрами (без сохранения в БД).

Параметры: `exchange`, `currency`, `ticker`, `figi`

Пример:
```bash
curl "http://localhost:8083/api/instruments/indicatives?exchange=moex_mrng_evng_e_wknd_dlr&currency=RUB"
```

Ответ (пример):
```json
[
  {
    "figi": "BBG00QPYJ5X0",
    "ticker": "IMOEX",
    "name": "Индекс МосБиржи",
    "currency": "RUB",
    "exchange": "moex_mrng_evng_e_wknd_dlr",
    "classCode": "SPBXM",
    "uid": "e6123145-9665-43e0-8413-cd61d8e6e372",
    "sellAvailableFlag": true,
    "buyAvailableFlag": true
  }
]
```

## POST /api/instruments/indicatives
Сохранение индикативных инструментов по фильтрам.
```bash
curl -X POST "http://localhost:8083/api/instruments/indicatives" \
  -H "Content-Type: application/json" \
  -d '{
    "exchange": "moex_mrng_evng_e_wknd_dlr",
    "currency": "RUB",
    "ticker": "IMOEX"
  }'
```

Ответ (пример):
```json
{
  "success": true,
  "message": "Успешно загружено 2 новых индикативных инструмента из 5 найденных.",
  "totalRequested": 5,
  "newItemsSaved": 2,
  "existingItemsSkipped": 3,
  "savedItems": [ { "figi": "BBG00QPYJ5X0", "ticker": "IMOEX" } ]
}
```

## GET /api/instruments/indicatives/{identifier}
Получение индикатива по FIGI или тикеру.

API автоматически определяет тип идентификатора:
- Если идентификатор длиннее 10 символов или содержит `-` или `_` → поиск по FIGI
- Иначе → поиск по тикеру

Примеры:
```bash
# По FIGI
curl "http://localhost:8083/api/instruments/indicatives/BBG00QPYJ5X0"

# По тикеру
curl "http://localhost:8083/api/instruments/indicatives/IMOEX"
```

Ответ (пример):
```json
{
  "figi": "BBG00QPYJ5X0",
  "ticker": "IMOEX",
  "name": "Индекс МосБиржи",
  "currency": "RUB",
  "exchange": "moex_mrng_evng_e_wknd_dlr",
  "classCode": "SPBXM",
  "uid": "e6123145-9665-43e0-8413-cd61d8e6e372",
  "sellAvailableFlag": true,
  "buyAvailableFlag": true
}
```

---

## GET /api/instruments/count
Количество инструментов в БД по типам и сумма total.

Пример:
```bash
curl "http://localhost:8083/api/instruments/count"
```

Ответ (пример):
```json
{ "shares": 150, "futures": 200, "indicatives": 50, "total": 400 }
```
