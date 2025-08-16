# API Documentation

## Обзор

Ingestion Service предоставляет REST API для доступа к данным о финансовых инструментах, полученным через API Тинькофф Инвестиций.

**Базовый URL:** `http://localhost:8083`

## Аутентификация

В настоящее время API не требует аутентификации. Все endpoints доступны публично.

## Общие заголовки

```
Content-Type: application/json
Accept: application/json
```

## Endpoints

### 1. Получение аккаунтов

**GET** `/accounts`

Возвращает список аккаунтов пользователя в Тинькофф Инвестициях.

#### Пример запроса
```bash
curl -X GET "http://localhost:8083/accounts"
```

#### Пример ответа
```json
[
  {
    "id": "12345678",
    "name": "ИИС",
    "type": "ACCOUNT_TYPE_TINKOFF"
  },
  {
    "id": "87654321", 
    "name": "Брокерский счет",
    "type": "ACCOUNT_TYPE_TINKOFF"
  }
]
```

#### Структура ответа
| Поле | Тип | Описание |
|------|-----|----------|
| `id` | String | Уникальный идентификатор аккаунта |
| `name` | String | Название аккаунта |
| `type` | String | Тип аккаунта |

---

### 2. Получение списка акций

**GET** `/shares`

Возвращает список акций с возможностью фильтрации.

#### Параметры запроса

| Параметр | Тип | Обязательный | Описание |
|----------|-----|--------------|----------|
| `status` | String | Нет | Статус инструмента (INSTRUMENT_STATUS_BASE, INSTRUMENT_STATUS_ALL) |
| `exchange` | String | Нет | Биржа (MOEX, SPBEX) |
| `currency` | String | Нет | Валюта (RUB, USD, EUR) |

#### Примеры запросов

```bash
# Все акции
curl "http://localhost:8083/shares"

# Акции только с MOEX
curl "http://localhost:8083/shares?exchange=MOEX"

# Акции в рублях
curl "http://localhost:8083/shares?currency=RUB"

# Акции с фильтрацией по бирже и валюте
curl "http://localhost:8083/shares?exchange=MOEX&currency=RUB"
```

#### Пример ответа
```json
[
  {
    "figi": "BBG000B9XRY4",
    "ticker": "SBER",
    "name": "Сбербанк России ПАО ао",
    "currency": "RUB",
    "exchange": "MOEX"
  },
  {
    "figi": "BBG000B9XRY5", 
    "ticker": "GAZP",
    "name": "Газпром ПАО ао",
    "currency": "RUB",
    "exchange": "MOEX"
  }
]
```

#### Структура ответа
| Поле | Тип | Описание |
|------|-----|----------|
| `figi` | String | Уникальный идентификатор инструмента |
| `ticker` | String | Тикер акции |
| `name` | String | Полное название компании |
| `currency` | String | Валюта инструмента |
| `exchange` | String | Биржа |

---

### 3. Получение списка фьючерсов

**GET** `/futures`

Возвращает список фьючерсов с возможностью фильтрации.

#### Параметры запроса

| Параметр | Тип | Обязательный | Описание |
|----------|-----|--------------|----------|
| `status` | String | Нет | Статус инструмента |
| `exchange` | String | Нет | Биржа (MOEX, SPBEX) |

#### Примеры запросов

```bash
# Все фьючерсы
curl "http://localhost:8083/futures"

# Фьючерсы только с MOEX
curl "http://localhost:8083/futures?exchange=MOEX"
```

#### Пример ответа
```json
[
  {
    "figi": "BBG00ZGF4GX3",
    "ticker": "SBER-12.24",
    "assetType": "ASSET_TYPE_STOCK",
    "basicAsset": "SBER",
    "currency": "RUB",
    "exchange": "MOEX"
  }
]
```

#### Структура ответа
| Поле | Тип | Описание |
|------|-----|----------|
| `figi` | String | Уникальный идентификатор инструмента |
| `ticker` | String | Тикер фьючерса |
| `assetType` | String | Тип базового актива |
| `basicAsset` | String | Базовый актив |
| `currency` | String | Валюта инструмента |
| `exchange` | String | Биржа |

---

### 4. Получение торговых расписаний

**GET** `/trading-schedules`

Возвращает торговые расписания для указанного периода.

#### Параметры запроса

| Параметр | Тип | Обязательный | Описание |
|----------|-----|--------------|----------|
| `exchange` | String | Нет | Биржа (MOEX, SPBEX) |
| `from` | String | Да | Начало периода (ISO 8601) |
| `to` | String | Да | Конец периода (ISO 8601) |

#### Пример запроса
```bash
curl "http://localhost:8083/trading-schedules?exchange=MOEX&from=2024-01-01T00:00:00Z&to=2024-01-07T00:00:00Z"
```

#### Пример ответа
```json
[
  {
    "exchange": "MOEX",
    "days": [
      {
        "date": "2024-01-01",
        "isTradingDay": false,
        "startTime": "2024-01-01T00:00:00Z",
        "endTime": "2024-01-01T00:00:00Z"
      },
      {
        "date": "2024-01-02",
        "isTradingDay": true,
        "startTime": "2024-01-02T09:30:00Z",
        "endTime": "2024-01-02T18:45:00Z"
      }
    ]
  }
]
```

#### Структура ответа
| Поле | Тип | Описание |
|------|-----|----------|
| `exchange` | String | Биржа |
| `days` | Array | Массив торговых дней |
| `days[].date` | String | Дата в формате YYYY-MM-DD |
| `days[].isTradingDay` | Boolean | Является ли торговым днем |
| `days[].startTime` | String | Время начала торгов |
| `days[].endTime` | String | Время окончания торгов |

---

### 5. Получение статусов торговли

**GET** `/trading-statuses`

Возвращает статусы торговли для указанных инструментов.

#### Параметры запроса

| Параметр | Тип | Обязательный | Описание |
|----------|-----|--------------|----------|
| `instrumentId` | Array | Да | Список FIGI инструментов |

#### Пример запроса
```bash
curl "http://localhost:8083/trading-statuses?instrumentId=BBG000B9XRY4&instrumentId=BBG000B9XRY5"
```

#### Пример ответа
```json
[
  {
    "figi": "BBG000B9XRY4",
    "tradingStatus": "SECURITY_TRADING_STATUS_NORMAL_TRADING"
  },
  {
    "figi": "BBG000B9XRY5",
    "tradingStatus": "SECURITY_TRADING_STATUS_NOT_AVAILABLE_FOR_TRADING"
  }
]
```

#### Структура ответа
| Поле | Тип | Описание |
|------|-----|----------|
| `figi` | String | Уникальный идентификатор инструмента |
| `tradingStatus` | String | Статус торговли |

#### Возможные статусы торговли
- `SECURITY_TRADING_STATUS_NORMAL_TRADING` - Обычная торговля
- `SECURITY_TRADING_STATUS_NOT_AVAILABLE_FOR_TRADING` - Торговля недоступна
- `SECURITY_TRADING_STATUS_BREAK_IN_TRADING` - Перерыв в торговле
- `SECURITY_TRADING_STATUS_CLOSING_AUCTION` - Закрывающий аукцион
- `SECURITY_TRADING_STATUS_CLOSING_PERIOD` - Закрывающий период
- `SECURITY_TRADING_STATUS_DARK_POOL_AUCTION` - Аукцион в темном пуле
- `SECURITY_TRADING_STATUS_DISCRETE_AUCTION` - Дискретный аукцион
- `SECURITY_TRADING_STATUS_OPENING_AUCTION` - Открывающий аукцион
- `SECURITY_TRADING_STATUS_OPENING_PERIOD` - Открывающий период
- `SECURITY_TRADING_STATUS_PRE_CLOSING_PERIOD` - Предзакрывающий период
- `SECURITY_TRADING_STATUS_PRE_OPENING_PERIOD` - Предоткрывающий период

---

### 6. Получение цен закрытия

**GET** `/close-prices`

Возвращает цены закрытия для указанных инструментов.

#### Параметры запроса

| Параметр | Тип | Обязательный | Описание |
|----------|-----|--------------|----------|
| `instrumentId` | Array | Да | Список FIGI инструментов |
| `instrumentStatus` | String | Нет | Статус инструмента |

#### Пример запроса
```bash
curl "http://localhost:8083/close-prices?instrumentId=BBG000B9XRY4&instrumentId=BBG000B9XRY5"
```

#### Пример ответа
```json
[
  {
    "figi": "BBG000B9XRY4",
    "date": "2024-01-02",
    "closePrice": 275.50
  },
  {
    "figi": "BBG000B9XRY5",
    "date": "2024-01-02", 
    "closePrice": 165.75
  }
]
```

#### Структура ответа
| Поле | Тип | Описание |
|------|-----|----------|
| `figi` | String | Уникальный идентификатор инструмента |
| `date` | String | Дата в формате YYYY-MM-DD |
| `closePrice` | BigDecimal | Цена закрытия |

---

## Административные Endpoints

### 1. Загрузка цен закрытия за сегодня

**POST** `/admin/load-close-prices`

Принудительно загружает цены закрытия за текущий день для всех инструментов в базе данных.

#### Пример запроса
```bash
curl -X POST "http://localhost:8083/admin/load-close-prices"
```

#### Пример ответа
```json
"Close prices loaded for today"
```

---

### 2. Перезагрузка цен закрытия за указанную дату

**POST** `/admin/load-close-prices/{date}`

Перезагружает цены закрытия за указанную дату. Удаляет существующие данные и загружает новые.

#### Параметры пути

| Параметр | Тип | Описание |
|----------|-----|----------|
| `date` | String | Дата в формате YYYY-MM-DD |

#### Пример запроса
```bash
curl -X POST "http://localhost:8083/admin/load-close-prices/2024-01-02"
```

#### Пример ответа
```json
"Close prices reloaded for 2024-01-02"
```

---

## Коды ошибок

| Код | Описание |
|-----|----------|
| 200 | Успешный запрос |
| 400 | Неверный запрос (неверные параметры) |
| 404 | Ресурс не найден |
| 500 | Внутренняя ошибка сервера |

## Примеры ошибок

### 400 Bad Request
```json
{
  "timestamp": "2024-01-02T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Period between 'from' and 'to' must be at least 1 day"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2024-01-02T10:30:00.000+00:00", 
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to connect to Tinkoff API"
}
```

## Ограничения

- Максимальный период для торговых расписаний: 14 дней
- Минимальный период для торговых расписаний: 1 день
- Автоматический сбор цен закрытия происходит каждый день в 20:00 по МСК
- Все временные метки возвращаются в UTC

## Поддержка

При возникновении проблем с API обращайтесь к документации или создавайте Issues в репозитории проекта.