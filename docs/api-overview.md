# API Overview

## Обзор API

Investment Data Loader Service предоставляет RESTful API для работы с инвестиционными данными. Все эндпоинты возвращают JSON и используют стандартные HTTP статус-коды.

**Базовый URL**: `http://localhost:8083/api`

## 📊 Основные группы API

### 1. Инструменты (`/api/instruments`)
Управление финансовыми инструментами (акции, фьючерсы, индикативы).

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/instruments/shares` | Получение списка акций |
| GET | `/instruments/shares/{id}` | Получение акции по ID |
| POST | `/instruments/shares` | Сохранение акций |
| GET | `/instruments/futures` | Получение списка фьючерсов |
| GET | `/instruments/futures/{id}` | Получение фьючерса по ID |
| POST | `/instruments/futures` | Сохранение фьючерсов |
| GET | `/instruments/indicatives` | Получение индикативов |
| GET | `/instruments/indicatives/{id}` | Получение индикатива по ID |
| POST | `/instruments/indicatives` | Сохранение индикативов |
| GET | `/instruments/count` | Статистика по инструментам |

### 2. Рыночные данные (`/api/market-data`)
Работа с ценами, свечами и рыночной информацией.

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/market-data/close-prices` | Цены закрытия |
| POST | `/market-data/close-prices` | Сохранение цен закрытия |
| GET | `/market-data/candles` | Исторические свечи |
| POST | `/market-data/candles` | Сохранение свечей |
| GET | `/market-data/last-trades` | Последние сделки |

### 3. Торговые данные (`/api/trading`)
Торговые расписания и статусы.

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/trading/schedules` | Торговые расписания |
| GET | `/trading/statuses` | Статусы торговли |

### 4. Аналитика (`/api/analytics`)
Аналитические данные и агрегация.

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/analytics/session-data` | Данные по сессиям |
| GET | `/analytics/statistics` | Статистика |

### 5. Система (`/api/system`)
Системная информация и мониторинг.

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/system/health` | Состояние системы |
| GET | `/system/info` | Информация о системе |

## 🔍 Детальное описание

### Инструменты

#### GET /api/instruments/shares
Получение списка акций с фильтрацией.

**Параметры:**
- `source` (string, optional) - источник данных: "api" или "database" (по умолчанию: "api")
- `status` (string, optional) - статус инструмента
- `exchange` (string, optional) - биржа
- `currency` (string, optional) - валюта
- `ticker` (string, optional) - тикер
- `figi` (string, optional) - FIGI

**Пример запроса:**
```bash
curl "http://localhost:8083/api/instruments/shares?exchange=MOEX&currency=RUB"
```

**Ответ:**
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

#### POST /api/instruments/shares
Сохранение акций в БД с защитой от дубликатов.

**Тело запроса:**
```json
{
  "status": "INSTRUMENT_STATUS_ACTIVE",
  "exchange": "MOEX",
  "currency": "RUB",
  "ticker": "SBER"
}
```

**Ответ:**
```json
{
  "success": true,
  "message": "Успешно загружено 5 новых акций из 10 найденных.",
  "totalRequested": 10,
  "newItemsSaved": 5,
  "existingItemsSkipped": 5,
  "invalidItemsFiltered": 0,
  "missingFromApi": 0,
  "savedItems": [...]
}
```

### Рыночные данные

#### GET /api/market-data/close-prices
Получение цен закрытия.

**Параметры:**
- `instrumentIds` (array) - список FIGI инструментов
- `status` (string, optional) - статус

**Пример запроса:**
```bash
curl "http://localhost:8083/api/market-data/close-prices?instrumentIds=BBG004730N88&status=ACTIVE"
```

#### GET /api/market-data/candles
Получение исторических свечей.

**Параметры:**
- `instrumentId` (string) - FIGI инструмента
- `date` (string) - дата в формате YYYY-MM-DD
- `interval` (string) - интервал (1m, 5m, 15m, 1h, 1d)

**Пример запроса:**
```bash
curl "http://localhost:8083/api/market-data/candles?instrumentId=BBG004730N88&date=2024-01-15&interval=1h"
```

### Система

#### GET /api/system/health
Проверка состояния системы.

**Ответ:**
```json
{
  "status": "UP",
  "components": {
    "database": "UP",
    "tinkoffApi": "UP"
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## 📝 Общие принципы

### Формат ответов
- **Успешный запрос**: HTTP 200 с данными в JSON
- **Ошибка клиента**: HTTP 400/404 с описанием ошибки
- **Ошибка сервера**: HTTP 500 с минимальной информацией

### Пагинация
Для больших списков используется пагинация:
```json
{
  "data": [...],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

### Фильтрация
Поддерживается фильтрация по основным полям:
- Строковые поля: точное совпадение (case-insensitive)
- Числовые поля: точное совпадение
- Даты: диапазон или точная дата

### Сортировка
По умолчанию результаты сортируются по тикеру (для инструментов) или по дате (для временных рядов).

## 🔐 Аутентификация

В текущей версии аутентификация не требуется. В будущих версиях планируется:
- API ключи для внешних клиентов
- JWT токены для веб-интерфейса
- Rate limiting для защиты от злоупотреблений

## 📊 Лимиты

- **Размер запроса**: максимум 10MB
- **Количество инструментов в запросе**: максимум 1000
- **Временной диапазон**: максимум 1 год
- **Rate limiting**: 100 запросов в минуту (планируется)

## 🚨 Обработка ошибок

### Коды ошибок
- `400 Bad Request` - некорректные параметры
- `404 Not Found` - ресурс не найден
- `429 Too Many Requests` - превышен лимит запросов
- `500 Internal Server Error` - внутренняя ошибка
- `503 Service Unavailable` - сервис недоступен

### Формат ошибки
```json
{
  "error": "Bad Request",
  "message": "Некорректный параметр 'date'",
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/instruments/shares"
}
```

## 📚 Дополнительная документация

- [Детальная документация по инструментам](instruments.md)
- [Документация по рыночным данным](candles.md)
- [Системная документация](system.md)
- [Примеры использования](examples.md)
