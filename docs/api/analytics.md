# API — Аналитика и агрегация (`/api/analytics`)

## POST /api/analytics/volume-aggregation/refresh-today
Ручное обновление дневной агрегации (`today_volume_aggregation`).

Пример:
```bash
curl -X POST "http://localhost:8087/api/analytics/volume-aggregation/refresh-today"
```
Ответ (пример):
```json
{ "success": true, "message": "Дневная агрегация объемов успешно обновлена", "type": "today", "timestamp": "2024-01-15T10:30:00" }
```

## POST /api/analytics/volume-aggregation/refresh-full
Полное обновление общего представления (`daily_volume_aggregation`).

Пример:
```bash
curl -X POST "http://localhost:8087/api/analytics/volume-aggregation/refresh-full"
```
Ответ (пример):
```json
{ "success": true, "message": "Полная агрегация объемов успешно обновлена", "type": "full", "timestamp": "2024-01-15T10:30:00" }
```

## POST /api/analytics/volume-aggregation/refresh
Маршрут совместимости (эквивалент `refresh-today`).

Пример:
```bash
curl -X POST "http://localhost:8087/api/analytics/volume-aggregation/refresh"
```
Ответ (пример):
```json
{ "success": true, "message": "Дневная агрегация объемов успешно обновлена", "type": "today", "timestamp": "2024-01-15T10:30:00" }
```

## GET /api/analytics/volume-aggregation/stats-today
Статистика за сегодня (печатается в лог).

Пример:
```bash
curl "http://localhost:8087/api/analytics/volume-aggregation/stats-today"
```
Ответ (пример):
```json
{ "success": true, "message": "Статистика за сегодня выведена в консоль", "type": "today", "timestamp": "2024-01-15T10:30:00" }
```

## GET /api/analytics/volume-aggregation/stats
Общая статистика (печатается в лог).

Пример:
```bash
curl "http://localhost:8087/api/analytics/volume-aggregation/stats"
```
Ответ (пример):
```json
{ "success": true, "message": "Общая статистика агрегации выведена в консоль", "type": "general", "timestamp": "2024-01-15T10:30:00" }
```

## GET /api/analytics/volume-aggregation/detailed-stats
Детальная статистика по сессиям.

Пример:
```bash
curl "http://localhost:8087/api/analytics/volume-aggregation/detailed-stats"
```
Ответ (пример):
```json
{
  "success": true,
  "data": {
    "total_records": 15000,
    "unique_instruments": 500,
    "earliest_date": "2023-01-01",
    "latest_date": "2024-01-15",
    "total_morning_volume": 1000000,
    "total_main_volume": 5000000,
    "total_evening_volume": 800000,
    "total_weekend_volume": 200000,
    "total_volume_all_time": 7000000,
    "avg_morning_volume": 66.67,
    "avg_main_volume": 333.33,
    "avg_evening_volume": 53.33,
    "avg_weekend_volume": 13.33,
    "avg_daily_volume": 466.67,
    "last_updated": "2024-01-15T10:29:50"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

## GET /api/analytics/volume-aggregation/check
Проверка существования материализованных представлений.

Пример:
```bash
curl "http://localhost:8087/api/analytics/volume-aggregation/check"
```
Ответ (пример):
```json
{ "success": true, "exists": true, "message": "Материализованные представления существуют", "timestamp": "2024-01-15T10:30:00" }
```

## GET /api/analytics/volume-aggregation/schedule-info
Информация о расписаниях обновлений.

Пример:
```bash
curl "http://localhost:8087/api/analytics/volume-aggregation/schedule-info"
```
Ответ (пример):
```json
{
  "success": true,
  "data": {
    "daily_refresh": "0 * * * * * (каждую минуту)",
    "full_refresh": "0 20 2 * * * (каждый день в 2:20)",
    "timezone": "Europe/Moscow",
    "description": "Дневное представление обновляется каждую минуту, общее - в 2:20"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

## POST /api/analytics/recalculate-aggregation
Пересчёт агрегированных данных по типам инструментов.

Тело запроса:
```json
{ "types": ["SHARES", "FUTURES"] }
```
Пример:
```bash
curl -X POST "http://localhost:8087/api/analytics/recalculate-aggregation" -H "Content-Type: application/json" -d '{
  "types": ["SHARES", "FUTURES"]
}'
```
Ответ (пример):
```json
{
  "success": true,
  "message": "Пересчет агрегации выполнен успешно",
  "results": [
    { "type": "SHARES", "status": "OK" },
    { "type": "FUTURES", "status": "OK" }
  ],
  "timestamp": "2024-01-15T10:30:00"
}
```

## GET /api/analytics/session-analytics/overall
Получение общей аналитики по торговым сессиям за все время.

Параметры запроса:
- `figi` — FIGI инструмента (опционально)

```bash
curl "http://localhost:8087/api/analytics/session-analytics/overall?figi=BBG004730N88"
```

Ответ (пример):
```json
{
  "success": true,
  "data": [
    {
      "figi": "BBG004730N88",
      "trade_date": null,
      "total_volume": 15000000,
      "total_candles": 12000,
      "avg_volume_per_candle": 1250.00,
      "morning_session_volume": 1500000,
      "morning_session_candles": 1200,
      "morning_avg_volume_per_candle": 1250.00,
      "main_session_volume": 12000000,
      "main_session_candles": 9600,
      "main_avg_volume_per_candle": 1250.00,
      "evening_session_volume": 1500000,
      "evening_session_candles": 1200,
      "evening_avg_volume_per_candle": 1250.00,
      "weekend_session_volume": 0,
      "weekend_session_candles": 0,
      "weekend_avg_volume_per_candle": 0.00
    }
  ],
  "count": 1,
  "timestamp": "2024-01-15T10:30:00"
}
```

## POST /api/analytics/session-analytics
Получение аналитики по торговым сессиям за период.

Параметры запроса:
- `figi` — FIGI инструмента (опционально)
- `start_date` — дата начала периода (опционально)
- `end_date` — дата окончания периода (опционально)

```bash
curl -X POST "http://localhost:8087/api/analytics/session-analytics" \
  -H "Content-Type: application/json" \
  -d '{
    "figi": "BBG004730N88",
    "start_date": "2024-01-01",
    "end_date": "2024-01-15"
  }'
```

Ответ (пример):
```json
{
  "success": true,
  "data": [
    {
      "figi": "BBG004730N88",
      "trade_date": "2024-01-15",
      "total_volume": 1500000,
      "total_candles": 1200,
      "avg_volume_per_candle": 1250.00,
      "morning_session_volume": 150000,
      "morning_session_candles": 120,
      "morning_avg_volume_per_candle": 1250.00,
      "main_session_volume": 1200000,
      "main_session_candles": 960,
      "main_avg_volume_per_candle": 1250.00,
      "evening_session_volume": 150000,
      "evening_session_candles": 120,
      "evening_avg_volume_per_candle": 1250.00,
      "weekend_session_volume": 0,
      "weekend_session_candles": 0,
      "weekend_avg_volume_per_candle": 0.00
    }
  ],
  "count": 1,
  "timestamp": "2024-01-15T10:30:00"
}
```

## GET /api/analytics/session-analytics/today
Получение аналитики по торговым сессиям за сегодня.

Параметры запроса:
- `figi` — FIGI инструмента (опционально)

```bash
curl "http://localhost:8087/api/analytics/session-analytics/today?figi=BBG004730N88"
```

Ответ (пример):
```json
{
  "success": true,
  "data": [
    {
      "figi": "BBG004730N88",
      "trade_date": "2024-01-15",
      "total_volume": 75000,
      "total_candles": 60,
      "avg_volume_per_candle": 1250.00,
      "morning_session_volume": 7500,
      "morning_session_candles": 6,
      "morning_avg_volume_per_candle": 1250.00,
      "main_session_volume": 60000,
      "main_session_candles": 48,
      "main_avg_volume_per_candle": 1250.00,
      "evening_session_volume": 7500,
      "evening_session_candles": 6,
      "evening_avg_volume_per_candle": 1250.00,
      "weekend_session_volume": 0,
      "weekend_session_candles": 0,
      "weekend_avg_volume_per_candle": 0.00
    }
  ],
  "count": 1,
  "timestamp": "2024-01-15T10:30:00"
}
```

## POST /api/analytics/session-analytics/summary
Получение сводной статистики по сессиям за период.

Параметры запроса:
- `figi` — FIGI инструмента (опционально)
- `start_date` — дата начала периода (опционально)
- `end_date` — дата окончания периода (опционально)

```bash
curl -X POST "http://localhost:8087/api/analytics/session-analytics/summary" \
  -H "Content-Type: application/json" \
  -d '{
    "figi": "BBG004730N88",
    "start_date": "2024-01-01",
    "end_date": "2024-01-15"
  }'
```

Ответ (пример):
```json
{
  "success": true,
  "data": {
    "period": {
      "start_date": "2024-01-01",
      "end_date": "2024-01-15",
      "figi": "BBG004730N88"
    },
    "total": {
      "volume": 1500000,
      "candles": 1200,
      "avg_volume_per_candle": 1250.00
    },
    "sessions": {
      "morning": {
        "volume": 150000,
        "candles": 120,
        "avg_volume_per_candle": 1250.00,
        "percentage_of_total": 10.00
      },
      "main": {
        "volume": 1200000,
        "candles": 960,
        "avg_volume_per_candle": 1250.00,
        "percentage_of_total": 80.00
      },
      "evening": {
        "volume": 150000,
        "candles": 120,
        "avg_volume_per_candle": 1250.00,
        "percentage_of_total": 10.00
      },
      "weekend": {
        "volume": 0,
        "candles": 0,
        "avg_volume_per_candle": 0.00,
        "percentage_of_total": 0.00
      }
    },
    "instruments_count": 1,
    "generated_at": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```
