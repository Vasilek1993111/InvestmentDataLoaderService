# API Overview

## Обзор API

Investment Data Loader Service предоставляет RESTful API для работы с инвестиционными данными. Все эндпоинты возвращают JSON и используют стандартные HTTP статус-коды.

**Базовый URL**: `http://localhost:8083/api` (PROD) / `http://localhost:8087/api` (TEST)

## 🔧 Быстрая настройка

### Переменные окружения
```bash
# Tinkoff API токен
T_INVEST_TEST_TOKEN=your_test_token_here

# База данных
SPRING_DATASOURCE_PASSWORD=your_password
```

### Запуск
```bash
# Загрузка переменных окружения
export $(cat .env | xargs)

# Запуск приложения
mvn spring-boot:run
```

**Документация по настройке**: [SETUP.md](../SETUP.md) | [Быстрый старт](../QUICK_START.md)

## 📊 Основные группы API

### 1. Инструменты (`/api/instruments`)
Управление финансовыми инструментами (акции, фьючерсы, индикативы).

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/instruments/shares` | Получение списка акций |
| POST | `/instruments/shares` | Сохранение акций |
| GET | `/instruments/futures` | Получение списка фьючерсов |
| POST | `/instruments/futures` | Сохранение фьючерсов |
| GET | `/instruments/indicatives` | Получение индикативов |
| POST | `/instruments/indicatives` | Сохранение индикативов |
| GET | `/instruments/count` | Статистика по инструментам |

### 2. Свечи (`/api/candles`)
Работа с историческими свечами (минутные и дневные).

#### Минутные свечи (`/api/candles/minute`)
| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/candles/minute` | Асинхронная загрузка минутных свечей за сегодня |
| POST | `/candles/minute/{date}` | Асинхронная загрузка минутных свечей за дату |
| POST | `/candles/minute/shares/{date}` | Асинхронная загрузка минутных свечей акций |
| POST | `/candles/minute/futures/{date}` | Асинхронная загрузка минутных свечей фьючерсов |
| POST | `/candles/minute/indicatives/{date}` | Асинхронная загрузка минутных свечей индикативов |
| GET | `/candles/minute/shares/{date}` | Получение минутных свечей акций за дату |
| GET | `/candles/minute/futures/{date}` | Получение минутных свечей фьючерсов за дату |
| GET | `/candles/minute/indicatives/{date}` | Получение минутных свечей индикативов за дату |

#### Дневные свечи (`/api/candles/daily`)
| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/candles/daily` | Асинхронная загрузка дневных свечей за сегодня |
| POST | `/candles/daily/{date}` | Асинхронная загрузка дневных свечей за дату |
| POST | `/candles/daily/shares/{date}` | Асинхронная загрузка дневных свечей акций |
| POST | `/candles/daily/futures/{date}` | Асинхронная загрузка дневных свечей фьючерсов |
| POST | `/candles/daily/indicatives/{date}` | Асинхронная загрузка дневных свечей индикативов |
| GET | `/candles/daily/shares/{date}` | Получение дневных свечей акций за дату |
| GET | `/candles/daily/futures/{date}` | Получение дневных свечей фьючерсов за дату |
| GET | `/candles/daily/indicatives/{date}` | Получение дневных свечей индикативов за дату |

#### Свечи инструментов (`/api/candles/instrument`)
| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/candles/instrument/minute/{figi}/{date}` | Получение минутных свечей конкретного инструмента |
| POST | `/candles/instrument/minute/{figi}/{date}` | Асинхронная загрузка минутных свечей инструмента |
| GET | `/candles/instrument/daily/{figi}/{date}` | Получение дневных свечей конкретного инструмента |
| POST | `/candles/instrument/daily/{figi}/{date}` | Асинхронная загрузка дневных свечей инструмента |

### 3. Торговые данные (`/api/trading`)
Торговые расписания, статусы и счета.

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/trading/accounts` | Торговые счета |
| GET | `/trading/schedules` | Торговые расписания |
| GET | `/trading/statuses` | Статусы торговли |
| GET | `/trading/trading-days` | Торговые дни |
| GET | `/trading/stats` | Статистика торгов |

### 4. Цены сессий
Цены открытия и закрытия торговых сессий.

#### Утренняя сессия (`/api/morning-session`)
| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/morning-session` | Загрузка цен утренней сессии за сегодня |
| GET | `/morning-session` | Предпросмотр цен утренней сессии за сегодня |
| POST | `/morning-session/by-date/{date}` | Загрузка цен утренней сессии за дату |
| GET | `/morning-session/by-date/{date}` | Получение цен утренней сессии за дату |
| POST | `/morning-session/shares/{date}` | Загрузка цен утренней сессии акций |
| GET | `/morning-session/shares/{date}` | Получение цен утренней сессии акций |
| POST | `/morning-session/futures/{date}` | Загрузка цен утренней сессии фьючерсов |
| GET | `/morning-session/futures/{date}` | Получение цен утренней сессии фьючерсов |
| POST | `/morning-session/by-figi-date/{figi}/{date}` | Загрузка цены утренней сессии по инструменту |
| GET | `/morning-session/by-figi-date/{figi}/{date}` | Получение цены утренней сессии по инструменту |

#### Основная сессия (`/api/main-session-prices`)
| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/main-session-prices/` | Загрузка цен закрытия за сегодня |
| GET | `/main-session-prices/shares` | Получение цен закрытия акций |
| POST | `/main-session-prices/shares` | Загрузка цен закрытия акций |
| GET | `/main-session-prices/futures` | Получение цен закрытия фьючерсов |
| POST | `/main-session-prices/futures` | Загрузка цен закрытия фьючерсов |
| GET | `/main-session-prices/by-figi/{figi}` | Получение цены закрытия по инструменту |
| POST | `/main-session-prices/instrument/{figi}` | Загрузка цены закрытия по инструменту |
| POST | `/main-session-prices/by-date/{date}` | Загрузка цен закрытия за дату |

#### Вечерняя сессия (`/api/evening-session-prices`)
| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/evening-session-prices` | Загрузка цен вечерней сессии за вчера |
| GET | `/evening-session-prices` | Получение цен вечерней сессии за вчера |
| POST | `/evening-session-prices/by-date/{date}` | Загрузка цен вечерней сессии за дату |
| GET | `/evening-session-prices/by-date/{date}` | Получение цен вечерней сессии за дату |
| POST | `/evening-session-prices/shares/{date}` | Загрузка цен вечерней сессии акций |
| GET | `/evening-session-prices/shares/{date}` | Получение цен вечерней сессии акций |
| POST | `/evening-session-prices/futures/{date}` | Загрузка цен вечерней сессии фьючерсов |
| GET | `/evening-session-prices/futures/{date}` | Получение цен вечерней сессии фьючерсов |
| POST | `/evening-session-prices/by-figi-date/{figi}/{date}` | Загрузка цены вечерней сессии по инструменту |
| GET | `/evening-session-prices/by-figi-date/{figi}/{date}` | Получение цены вечерней сессии по инструменту |

### 5. Последние сделки (`/api/last-trades`)
Обезличенные сделки и последние цены.

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/last-trades` | Асинхронная загрузка последних сделок (с параметрами запроса) |
| POST | `/last-trades` | Асинхронная загрузка последних сделок (с JSON телом) |
| GET | `/last-trades/cache` | Информация о кэше последних сделок |
| GET | `/last-trades/shares` | Загрузка последних сделок по акциям |
| GET | `/last-trades/futures` | Загрузка последних сделок по фьючерсам |
| GET | `/last-trades/performance` | Статистика производительности загрузки сделок |

### 6. Кэш (`/api/cache`)
Управление кэшем инструментов.

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/cache/warmup` | Прогрев кэша |
| GET | `/cache/content` | Содержимое кэша |
| DELETE | `/cache/clear` | Очистка кэша |
| GET | `/cache/stats` | Статистика кэша |

### 7. Дивиденды (`/api/dividends`)
Работа с дивидендами по акциям.

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/dividends/load?from={from}&to={to}` | Получение дивидендов по всем акциям за период (из API) |
| POST | `/dividends/load?from={from}&to={to}` | Загрузка дивидендов по всем акциям в БД |
| GET | `/dividends/by-figi/{figi}` | Получение дивидендов по FIGI (из API) |
| POST | `/dividends/by-figi/{figi}` | Загрузка дивидендов по FIGI в БД |

### 8. Фундаментальные показатели (`/api/asset-fundamentals`)
Работа с фундаментальными показателями активов.

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/asset-fundamentals/{assetUid}` | Получение фундаментальных показателей актива |
| POST | `/asset-fundamentals/{assetUid}` | Обновление фундаментальных показателей актива |
| POST | `/asset-fundamentals` | Обновление фундаментальных показателей по списку активов |
| POST | `/asset-fundamentals/load` | Загрузка фундаментальных показателей всех акций |

### 9. Агрегация объемов (`/api/volume-aggregation`)
Аналитические данные и агрегация объемов.

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/volume-aggregation/refresh` | Ручное обновление материализованного представления агрегации объемов |

### 10. Статус операций (`/api/status`)
Отслеживание статуса асинхронных операций.

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/status/{taskId}` | Получение статуса операции по taskId |
| GET | `/status/active` | Список активных операций |
| GET | `/status/stats` | Статистика операций |

### 11. Rate Limiting (`/api/rate-limit`)
Управление лимитами API запросов.

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/rate-limit/stats` | Статистика использования лимитов |
| GET | `/rate-limit/status` | Текущий статус лимитов |

### 12. Система (`/api/system`)
Системная информация и мониторинг.

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/system/health` | Проверка здоровья системы |
| GET | `/system/info` | Информация о системе |
| GET | `/system/diagnostics` | Диагностика системы |
| GET | `/system/stats` | Статистика системы |
| GET | `/system/external-services` | Статус внешних сервисов |
| GET | `/system/volume-aggregation/check` | Проверка статуса агрегации объемов |
| GET | `/system/volume-aggregation/schedule-info` | Информация о расписании агрегации |

## 🔍 Детальное описание

### Инструменты

#### GET /api/instruments/shares
Получение списка акций с фильтрацией. Использует кэш для повышения производительности.

**Параметры:**
- `source` (string, optional) - источник данных: "api" или "database" (по умолчанию: "api")
- `status` (string, optional) - статус инструмента (например: "INSTRUMENT_STATUS_ACTIVE")
- `exchange` (string, optional) - биржа (например: "MOEX")
- `currency` (string, optional) - валюта (например: "RUB")
- `ticker` (string, optional) - тикер (например: "SBER")
- `figi` (string, optional) - FIGI инструмента

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

#### GET /api/instruments/shares/{identifier}
Поиск акции по FIGI или тикеру.

**Пример запроса:**
```bash
curl "http://localhost:8083/api/instruments/shares/SBER"
curl "http://localhost:8083/api/instruments/shares/BBG004730N88"
```

#### POST /api/instruments/shares
Асинхронное сохранение акций в БД с защитой от дубликатов. Использует параллельную обработку.

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
  "message": "Операция запущена",
  "taskId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "STARTED",
  "startTime": "2024-01-15T10:30:00Z"
}
```

Аналогичные эндпоинты доступны для `/api/instruments/futures` и `/api/instruments/indicatives`.

### Свечи

#### POST /api/candles/minute
Асинхронная загрузка минутных свечей за сегодня. Использует параллельную обработку через пулы потоков.

**Тело запроса:**
```json
{
  "instruments": ["BBG004730N88", "BBG004730ZJ9"],
  "assetType": ["SHARES", "FUTURES", "INDICATIVES"],
  "date": "2024-01-15"
}
```

**Ответ:**
```json
{
  "success": true,
  "message": "Загрузка минутных свечей за сегодня запущена",
  "taskId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "STARTED",
  "startTime": "2024-01-15T10:30:00Z"
}
```

#### POST /api/candles/minute/{date}
Асинхронная загрузка минутных свечей за конкретную дату.

**Пример:**
```bash
curl -X POST "http://localhost:8083/api/candles/minute/2024-01-15" \
  -H "Content-Type: application/json" \
  -d '{"assetType": ["SHARES", "FUTURES"]}'
```

#### GET /api/candles/minute/shares/{date}
Получение минутных свечей акций за дату.

**Пример:**
```bash
curl "http://localhost:8083/api/candles/minute/shares/2024-01-15"
```

**Ответ:**
```json
{
  "date": "2024-01-15",
  "assetType": "SHARES",
  "candles": [...],
  "totalCandles": 1000,
  "totalInstruments": 150,
  "processedInstruments": 150,
  "successfulInstruments": 145,
  "noDataInstruments": 3,
  "errorInstruments": 2
}
```

Аналогичные эндпоинты доступны для дневных свечей (`/api/candles/daily`) и свечей конкретных инструментов (`/api/candles/instrument`).

### Торговые данные

#### GET /api/trading/accounts
Получение торговых счетов.

**Ответ:**
```json
[
  {
    "id": "2000123456",
    "name": "Брокерский счет",
    "type": "ACCOUNT_TYPE_TINKOFF"
  }
]
```

#### GET /api/trading/schedules
Получение торговых расписаний.

**Параметры:**
- `exchange` (string, optional) - биржа
- `from` (string, optional) - начальная дата
- `to` (string, optional) - конечная дата

### Кэш

#### POST /api/cache/warmup
Прогрев кэша инструментов. Автоматически выполняется при запуске приложения (00:45 МСК).

**Ответ:**
```json
{
  "success": true,
  "message": "Кэш успешно прогрет",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

#### GET /api/cache/content
Содержимое кэша.

**Параметры:**
- `cacheName` (string, optional) - имя кэша (sharesCache, futuresCache, indicativesCache)
- `limit` (number, optional) - лимит записей для отображения

**Пример:**
```bash
curl "http://localhost:8083/api/cache/content?cacheName=sharesCache&limit=10"
```

#### GET /api/cache/stats
Статистика кэша.

**Ответ:**
```json
{
  "totalCaches": 4,
  "activeCaches": 4,
  "totalEntries": 1247,
  "cacheDetails": {
    "sharesCache": {
      "entryCount": 150,
      "sampleEntries": [...]
    }
  }
}
```

#### DELETE /api/cache/clear
Очистка кэша.

**Параметры:**
- `cacheName` (string, optional) - имя кэша для очистки (если не указано, очищаются все кэши)

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
- **Асинхронная операция**: HTTP 202 с taskId для отслеживания
- **Ошибка клиента**: HTTP 400/404 с описанием ошибки
- **Ошибка сервера**: HTTP 500 с минимальной информацией

### Асинхронные операции
Многие операции выполняются асинхронно и возвращают `taskId`:
```json
{
  "success": true,
  "taskId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "STARTED",
  "message": "Операция запущена"
}
```

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
- FIGI: точное совпадение

### Сортировка
По умолчанию результаты сортируются по тикеру (для инструментов) или по дате (для временных рядов).

## 🔐 Аутентификация и безопасность

### Текущая версия
- **Аутентификация**: не требуется
- **Доступ**: открытый для локальной сети
- **Токены**: используются только для внешних API (Tinkoff)

### Защита секретных данных
```bash
# ✅ Правильно - используйте переменные окружения
export T_INVEST_TEST_TOKEN=your_real_token
export SPRING_DATASOURCE_PASSWORD=your_real_password

# ❌ Неправильно - не коммитьте реальные токены
echo "T_INVEST_TEST_TOKEN=real_token" >> .env
```

### Планируемые улучшения
- API ключи для внешних клиентов
- JWT токены для веб-интерфейса
- Rate limiting для защиты от злоупотреблений
- HTTPS для продакшн окружения

## 📊 Лимиты и производительность

### Технические лимиты
- **Размер запроса**: максимум 10MB
- **Количество инструментов в запросе**: максимум 1000
- **Временной диапазон**: максимум 1 год
- **Rate limiting**: 100 запросов в минуту (планируется)
- **Асинхронные операции**: максимум 10 одновременных задач

### Кэширование
- **Инструменты**: кэшируются на 1 день (Caffeine Cache)
- **Размер кэша**: максимум 10,000 записей на кэш
- **Автоматический прогрев**: при запуске приложения
- **Fallback**: автоматическое обращение к БД при отсутствии в кэше

### Профили окружения
| Профиль | Порт | Логирование | DevTools | Кэширование |
|---------|------|-------------|----------|-------------|
| `test` | 8087 | DEBUG | Включены | Отключено |
| `prod` | 8083 | INFO | Отключены | Включено |
| `docker` | 8083 | INFO | Отключены | Включено |

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

### Отслеживание задач
Для асинхронных операций используйте `taskId`:
```bash
# Проверка статуса задачи
GET /api/status/550e8400-e29b-41d4-a716-446655440000

# Список активных операций
GET /api/status/active

# Статистика операций
GET /api/status/stats
```

## 🔄 Кэширование

### Архитектура кэша
- **Технология**: Caffeine Cache (высокопроизводительный in-memory cache)
- **Стратегия**: Cache-Aside с fallback на БД
- **TTL**: 1 день (expireAfterWrite)
- **Размер**: максимум 10,000 записей на кэш
- **Алгоритм вытеснения**: LRU (Least Recently Used)

### Типы кэшей
- **sharesCache** - кэш акций
- **futuresCache** - кэш фьючерсов  
- **indicativesCache** - кэш индикативных инструментов
- **closePricesCache** - кэш цен закрытия

### Управление кэшем
```bash
# Прогрев кэша (автоматически при запуске)
POST /api/cache/warmup

# Просмотр содержимого кэша
GET /api/cache/content?cacheName=sharesCache&limit=100

# Статистика кэша
GET /api/cache/stats

# Очистка кэша
DELETE /api/cache/clear?cacheName=sharesCache
```

### Мониторинг кэша
```json
{
  "totalCaches": 4,
  "activeCaches": 4,
  "totalEntries": 1247,
  "cacheDetails": {
    "sharesCache": {
      "entryCount": 150,
      "sampleEntries": [...]
    }
  }
}
```

## 📚 Дополнительная документация

### API документация
- [Обзор API](api/README.md) - общая документация по REST API
- [Детальная документация по инструментам](api/instruments.md)
- [Документация по минутным свечам](api/minute-candles.md)
- [Документация по дневным свечам](api/daily-candles.md)
- [Свечи инструментов](api/instrument-candles.md) - свечи конкретных инструментов
- [Расширенные минутные свечи](api/minute-candles-extended.md) - расширенные данные свечей
- [Утренняя сессия](api/morning-session.md) - цены открытия утренней сессии
- [Основная сессия](api/main-session-prices.md) - цены закрытия основной сессии
- [Вечерняя сессия](api/evening-session.md) - цены закрытия вечерней сессии
- [Последние сделки](api/last-trades.md) - обезличенные сделки
- [Дивиденды](api/dividends.md) - работа с дивидендами
- [Агрегация объемов](api/volume-aggregation.md) - управление агрегацией объемов
- [Документация по торговым данным](api/trading.md)
- [Статус операций](api/status.md) - отслеживание статуса асинхронных операций
- [Системная документация](api/system.md)
- [Документация по кэшу](api/cache.md)
- [Обработка ошибок](api/error-handling.md) - стандарты обработки ошибок

### Настройка и развертывание
- [Планировщики](../schedulers.md) - автоматические задачи и расписания
- [Архитектура системы](../architecture.md)
- [Docker разработка](../docker-development.md)
- [Rate Limiting](../rate-limiting-solution.md) - решение проблем с лимитами API

## 🚨 Важные замечания

### Безопасность
- **Никогда не коммитьте** реальные токены API или пароли БД
- **Используйте** переменные окружения для секретных данных
- **Проверьте** .gitignore файл для исключения конфиденциальных файлов

### Конфигурация
- **Тестовый профиль** использует порт 8087 и тестовый токен Tinkoff
- **Продакшн профиль** использует порт 8083 и продакшн токен Tinkoff
- **Docker профиль** оптимизирован для контейнеризации

### Производительность
- **Кэш автоматически прогревается** при запуске приложения
- **Fallback на БД** обеспечивает надежность при проблемах с кэшем
- **Асинхронные операции** не блокируют основной поток
