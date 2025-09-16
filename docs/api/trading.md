# API — Торговые данные (`/api/trading`)

## GET /api/trading/accounts
Список торговых счетов.
```bash
curl "http://localhost:8087/api/trading/accounts"
```
Ответ (пример):
```json
[
  { "id": "ACC-001", "name": "Primary", "accessLevel": "full" }
]
```

## GET /api/trading/close-prices
Получение цен закрытия торговой сессии по инструментам из T‑Bank API.

Параметры:
- `instrumentId` — список FIGI (повторяющийся query-параметр)
- `instrumentStatus` — INSTRUMENT_STATUS_UNSPECIFIED | INSTRUMENT_STATUS_BASE | INSTRUMENT_STATUS_ALL

Примеры:
```bash
# Все инструменты из БД
curl "http://localhost:8087/api/trading/close-prices"

# Конкретные FIGI
curl "http://localhost:8087/api/trading/close-prices?instrumentId=BBG004730N88&instrumentId=BBG004730ZJ9"

# С фильтром по статусу
curl "http://localhost:8087/api/trading/close-prices?instrumentStatus=INSTRUMENT_STATUS_BASE"
```

Ответ (пример):
```json
[
  { "figi": "BBG004730N88", "tradingDate": "2024-01-15", "closePrice": 250.75, "eveningSessionPrice": 251.20 }
]
```

## GET /api/trading/schedules
Торговые расписания за период.

Параметры: `exchange?`, `from` (Instant), `to` (Instant)

Пример:
```bash
curl "http://localhost:8087/api/trading/schedules?exchange=MOEX&from=2024-01-01T00:00:00Z&to=2024-01-10T00:00:00Z"
```
Ответ (пример):
```json
[
  {
    "exchange": "MOEX",
    "days": [
      { "date": "2024-01-02", "isTradingDay": true,  "startTime": "2024-01-02T06:50:00+03:00", "endTime": "2024-01-02T23:59:59+03:00" },
      { "date": "2024-01-06", "isTradingDay": false, "startTime": null,                         "endTime": null }
    ]
  }
]
```

## GET /api/trading/statuses
Торговые статусы по списку FIGI: `instrumentId` (List<String>).

Пример:
```bash
curl "http://localhost:8087/api/trading/statuses?instrumentId=BBG004730N88&instrumentId=BBG004730ZJ9"
```
Ответ (пример):
```json
[
  { "figi": "BBG004730N88", "status": "SECURITY_TRADING_STATUS_NORMAL_TRADING" }
]
```

## GET /api/trading/trading-days
Сводка по торговым/неторговым дням в периоде (группировка по `days`).

Пример:
```bash
curl "http://localhost:8087/api/trading/trading-days?exchange=MOEX&from=2024-01-01T00:00:00Z&to=2024-01-10T00:00:00Z"
```
Ответ (пример):
```json
{
  "success": true,
  "trading_days": { "2024-01-02": "trading", "2024-01-06": "non-trading" },
  "trading_days_count": 6,
  "non_trading_days_count": 4,
  "total_days": 10,
  "from": "2024-01-01T00:00:00Z",
  "to": "2024-01-10T00:00:00Z",
  "exchange": "MOEX"
}
```

## GET /api/trading/stats
Процент торговых дней и суммарные показатели за период.

Пример:
```bash
curl "http://localhost:8087/api/trading/stats?exchange=MOEX&from=2024-01-01T00:00:00Z&to=2024-01-31T00:00:00Z"
```
Ответ (пример):
```json
{
  "success": true,
  "period": { "from": "2024-01-01T00:00:00Z", "to": "2024-01-31T00:00:00Z", "exchange": "MOEX" },
  "trading_days": 20,
  "non_trading_days": 11,
  "total_days": 31,
  "trading_percentage": 64.52
}
```

## GET /api/trading/search
Поиск по торговым данным (placeholder для расширения).

Пример:
```bash
curl "http://localhost:8087/api/trading/search?query=SBER&type=orders"
```
Ответ (пример):
```json
{ "success": true, "query": "SBER", "type": "orders", "results": [] }
```
