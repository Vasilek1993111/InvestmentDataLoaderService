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

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/candles/minute` | Загрузка минутных свечей |
| GET | `/candles/minute` | Получение минутных свечей |
| POST | `/candles/daily` | Загрузка дневных свечей |
| GET | `/candles/daily` | Получение дневных свечей |
| GET | `/candles/instrument` | Свечи по инструменту |

### 3. Торговые данные (`/api/trading`)
Торговые расписания, статусы и счета.

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/trading/accounts` | Торговые счета |
| GET | `/trading/schedules` | Торговые расписания |
| GET | `/trading/statuses` | Статусы торговли |
| GET | `/trading/trading-days` | Торговые дни |
| GET | `/trading/stats` | Статистика торгов |

### 4. Цены сессий (`/api/sessions`)
Цены открытия и закрытия торговых сессий.

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/sessions/morning` | Цены утренней сессии |
| POST | `/sessions/morning` | Загрузка цен утренней сессии |
| GET | `/sessions/main` | Цены основной сессии |
| POST | `/sessions/main` | Загрузка цен основной сессии |
| GET | `/sessions/evening` | Цены вечерней сессии |

### 5. Последние сделки (`/api/last-trades`)
Обезличенные сделки и последние цены.

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/last-trades` | Последние сделки |
| POST | `/last-trades` | Загрузка последних сделок |
| GET | `/last-trades/shares` | Сделки по акциям |
| GET | `/last-trades/futures` | Сделки по фьючерсам |

### 6. Кэш (`/api/cache`)
Управление кэшем инструментов.

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/cache/warmup` | Прогрев кэша |
| GET | `/cache/info` | Информация о кэше |
| DELETE | `/cache/clear` | Очистка кэша |
| GET | `/cache/stats` | Статистика кэша |

### 7. Агрегация (`/api/aggregation`)
Аналитические данные и агрегация объемов.

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/aggregation/volume` | Агрегация объемов |
| POST | `/aggregation/refresh` | Обновление агрегации |
| GET | `/aggregation/shares` | Агрегация по акциям |
| GET | `/aggregation/futures` | Агрегация по фьючерсам |

### 8. Система (`/api/system`)
Системная информация и мониторинг.

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/system/health` | Состояние системы |
| GET | `/system/info` | Информация о системе |
| GET | `/system/logs` | Системные логи |
| GET | `/system/status` | Статус системы |

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

### Свечи

#### POST /api/candles/minute
Загрузка минутных свечей за сегодня.

**Тело запроса:**
```json
{
  "instruments": ["BBG004730N88", "BBG004730ZJ9"],
  "assetType": ["share", "future"]
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

#### GET /api/candles/minute
Получение минутных свечей.

**Параметры:**
- `figi` (string) - FIGI инструмента
- `from` (string) - начальная дата (ISO 8601)
- `to` (string) - конечная дата (ISO 8601)

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
Прогрев кэша инструментов.

**Ответ:**
```json
{
  "success": true,
  "message": "Кэш успешно прогрет",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

#### GET /api/cache/info
Информация о кэше.

**Параметры:**
- `cacheName` (string, optional) - имя кэша
- `limit` (number, optional) - лимит записей

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
GET /api/system/logs?taskId=550e8400-e29b-41d4-a716-446655440000
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
- [Детальная документация по инструментам](api/instruments.md)
- [Документация по свечам](api/candles.md)
- [Документация по торговым данным](api/trading.md)
- [Системная документация](api/system.md)
- [Документация по кэшу](api/cache.md)

### Настройка и развертывание
- [Полная настройка проекта](../SETUP.md)
- [Быстрый старт](../QUICK_START.md)
- [Архитектура системы](architecture.md)
- [Конфигурация базы данных](database.md)
- [Docker разработка](docker-development.md)

### Безопасность
- [Защита секретных данных](../SECURITY.md)
- [Переменные окружения](../ENV_VARIABLES.md)
- [Конфигурация для продакшна](../PRODUCTION.md)

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
