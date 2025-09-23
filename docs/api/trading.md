# API — Торговые данные (`/api/trading`)

## Обзор

API для работы с торговой информацией через Tinkoff Invest API предоставляет следующие возможности:

- **Счета** - получение списка торговых счетов пользователя
- **Торговые расписания** - информация о торговых сессиях и расписании
- **Торговые статусы** - текущие статусы торговли по инструментам
- **Торговые дни** - анализ торговых и неторговых дней
- **Статистика торгов** - аналитика по торговой активности
- **Поиск** - поиск по торговым данным (заглушка)

---

## GET /api/trading/accounts

Получение списка торговых счетов пользователя.

Возвращает все доступные торговые счета, связанные с токеном авторизации.

**Примеры использования:**
```bash
curl "http://localhost:8083/api/trading/accounts"
```

**Ответ (пример):**
```json
[
  {
    "id": "2000123456",
    "name": "Брокерский счет",
    "type": "ACCOUNT_TYPE_TINKOFF"
  }
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
curl "http://localhost:8083/api/trading/close-prices"

# Конкретные FIGI
curl "http://localhost:8083/api/trading/close-prices?instrumentId=BBG004730N88&instrumentId=BBG004730ZJ9"

# С фильтром по статусу
curl "http://localhost:8083/api/trading/close-prices?instrumentStatus=INSTRUMENT_STATUS_BASE"
```

Ответ (пример):
```json
[
  { "figi": "BBG004730N88", "tradingDate": "2024-01-15", "closePrice": 250.75, "eveningSessionPrice": 251.20 }
]
```

## GET /api/trading/schedules

Получение торговых расписаний за указанный период.

Возвращает информацию о торговых сессиях для указанной биржи и периода времени.

**Параметры запроса:**
- `exchange` (опционально) - биржа (например: MOEX, SPB)
- `from` (опционально) - начальная дата в формате ISO 8601 (по умолчанию: текущее время)
- `to` (опционально) - конечная дата в формате ISO 8601 (по умолчанию: текущее время + 1 день)

**Ограничения:**
- Период между from и to: минимум 1 день, максимум 14 дней
- Даты не могут быть более чем на 1 год в будущем

**Примеры использования:**
```bash
# Без параметров (использует значения по умолчанию)
curl "http://localhost:8083/api/trading/schedules"

# Только биржа
curl "http://localhost:8083/api/trading/schedules?exchange=MOEX"

# С периодом
curl "http://localhost:8083/api/trading/schedules?from=2024-01-01T00:00:00Z&to=2024-01-10T00:00:00Z"

# Полный запрос
curl "http://localhost:8083/api/trading/schedules?exchange=MOEX&from=2024-01-01T00:00:00Z&to=2024-01-10T00:00:00Z"
```

**Ответ (пример):**
```json
[
  {
    "exchange": "MOEX",
    "days": [
      {
        "date": "2024-01-02",
        "isTradingDay": true,
        "startTime": "2024-01-02T06:50:00+03:00",
        "endTime": "2024-01-02T23:59:59+03:00"
      },
      {
        "date": "2024-01-06",
        "isTradingDay": false,
        "startTime": null,
        "endTime": null
      }
    ]
  }
]
```

## GET /api/trading/statuses

Получение торговых статусов инструментов с детальной информацией.

Возвращает текущие торговые статусы для указанных финансовых инструментов.

**Параметры запроса:**
- `instrumentId` (опционально) - список FIGI идентификаторов инструментов
- `figi` (опционально) - один FIGI идентификатор инструмента

**Примечания:**
- Необходимо указать хотя бы один параметр (instrumentId или figi)
- Можно комбинировать оба параметра для запроса нескольких инструментов
- Используйте FIGI идентификаторы, а не тикеры (например: BBG004730N88 вместо SBER)

**Примеры использования:**
```bash
# Один инструмент через figi
curl "http://localhost:8083/api/trading/statuses?figi=BBG004730N88"

# Несколько инструментов через instrumentId
curl "http://localhost:8083/api/trading/statuses?instrumentId=BBG004730N88&instrumentId=BBG004730ZJ9"

# Комбинированный запрос
curl "http://localhost:8083/api/trading/statuses?figi=BBG004730N88&instrumentId=BBG004730ZJ9"
```

**Ответ (пример):**
```json
{
  "success": true,
  "data": [
    {
      "figi": "BBG004730N88",
      "status": "SECURITY_TRADING_STATUS_NORMAL_TRADING"
    }
  ],
  "count": 1,
  "requested_instruments": 1,
  "instruments": ["BBG004730N88"]
}
```

## GET /api/trading/trading-days

Получение информации о торговых и неторговых днях за период.

Анализирует торговые расписания и группирует дни по типам торговой активности.

**Параметры запроса:**
- `exchange` (опционально) - биржа (например: MOEX, SPB)
- `from` (опционально) - начальная дата в формате ISO 8601 (по умолчанию: текущее время)
- `to` (опционально) - конечная дата в формате ISO 8601 (по умолчанию: текущее время + 1 день)

**Примеры использования:**
```bash
# Без параметров (использует значения по умолчанию)
curl "http://localhost:8083/api/trading/trading-days"

# Только биржа
curl "http://localhost:8083/api/trading/trading-days?exchange=MOEX"

# С периодом
curl "http://localhost:8083/api/trading/trading-days?from=2024-01-01T00:00:00Z&to=2024-01-10T00:00:00Z"

# Полный запрос
curl "http://localhost:8083/api/trading/trading-days?exchange=MOEX&from=2024-01-01T00:00:00Z&to=2024-01-10T00:00:00Z"
```

**Ответ (пример):**
```json
{
  "success": true,
  "trading_days": {
    "2024-01-02": "trading",
    "2024-01-06": "non-trading"
  },
  "trading_days_count": 6,
  "non_trading_days_count": 4,
  "total_days": 10,
  "from": "2024-01-01T00:00:00Z",
  "to": "2024-01-10T00:00:00Z",
  "exchange": "MOEX"
}
```

## GET /api/trading/stats

Получение статистики торговой активности за период.

Вычисляет процентное соотношение торговых и неторговых дней, а также общую статистику.

**Параметры запроса:**
- `exchange` (опционально) - биржа (например: MOEX, SPB)
- `from` (опционально) - начальная дата в формате ISO 8601 (по умолчанию: текущее время)
- `to` (опционально) - конечная дата в формате ISO 8601 (по умолчанию: текущее время + 1 день)

**Примеры использования:**
```bash
# Без параметров (использует значения по умолчанию)
curl "http://localhost:8083/api/trading/stats"

# Только биржа
curl "http://localhost:8083/api/trading/stats?exchange=MOEX"

# С периодом
curl "http://localhost:8083/api/trading/stats?from=2024-01-01T00:00:00Z&to=2024-01-31T00:00:00Z"

# Полный запрос
curl "http://localhost:8083/api/trading/stats?exchange=MOEX&from=2024-01-01T00:00:00Z&to=2024-01-31T00:00:00Z"
```

**Ответ (пример):**
```json
{
  "success": true,
  "period": {
    "from": "2024-01-01T00:00:00Z",
    "to": "2024-01-31T00:00:00Z",
    "exchange": "MOEX"
  },
  "trading_days": 20,
  "non_trading_days": 11,
  "total_days": 31,
  "trading_percentage": 64.52
}
```

## GET /api/trading/search

Поиск по торговым данным (заглушка для будущего расширения).

Предоставляет базовую структуру для поиска по торговым данным. В текущей версии возвращает заглушку.

**Параметры запроса:**
- `query` (обязательно) - поисковый запрос
- `type` (опционально) - тип поиска (например: orders, instruments, schedules)

**Примеры использования:**
```bash
# Простой поиск
curl "http://localhost:8083/api/trading/search?query=SBER"

# Поиск с типом
curl "http://localhost:8083/api/trading/search?query=SBER&type=instruments"

# Поиск по бирже
curl "http://localhost:8083/api/trading/search?query=MOEX&type=schedules"
```

**Ответ (пример):**
```json
{
  "success": true,
  "query": "SBER",
  "type": "instruments",
  "message": "Поиск по торговым данным выполнен",
  "results": []
}
```

---

## Ошибки и коды ответов

API использует стандартные HTTP статус-коды:

- **200 OK** - успешный запрос
- **400 Bad Request** - некорректные параметры запроса
- **500 Internal Server Error** - внутренняя ошибка сервера

При ошибках API возвращает JSON с полями:
- `success` - статус операции (true/false)
- `message` - описание ошибки
- `error` - код ошибки (опционально)
- `details` - дополнительная информация (опционально)

**Пример ошибки:**
```json
{
  "success": false,
  "message": "Необходимо указать хотя бы один инструмент через параметр 'instrumentId' или 'figi'",
  "error": "MISSING_PARAMETER"
}
```
