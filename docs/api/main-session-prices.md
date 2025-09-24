# API Основной Сессии

Документация для работы с ценами основной торговой сессии и ценами закрытия.

## Базовый URL
```
http://localhost:8087/api/main-session-prices
```

## Эндпоинты

### Цены Закрытия Основной Сессии

#### POST /api/main-session-prices/
Загрузка цен закрытия основной сессии за сегодня. Работает только с акциями и фьючерсами, использует данные из `minute_candles`.

**Параметры:** Нет

```bash
curl -X POST "http://localhost:8087/api/main-session-prices/"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Успешно загружено 150 новых цен закрытия основной сессии из 200 найденных.",
  "date": "2024-01-15",
  "totalRequested": 200,
  "newItemsSaved": 150,
  "existingItemsSkipped": 30,
  "invalidItemsFiltered": 15,
  "missingFromApi": 5,
  "savedItems": [...],
  "timestamp": "2024-01-15T18:30:00"
}
```


#### GET /api/main-session-prices/shares
Получение цен закрытия для всех акций из T-INVEST API.

**Параметры:** Нет

```bash
curl -X GET "http://localhost:8087/api/main-session-prices/shares"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Цены закрытия для акций получены успешно",
  "data": [
    {
      "figi": "BBG004730N88",
      "ticker": "SBER",
      "tradingDate": "2024-01-15",
      "closePrice": 250.75
    }
  ],
  "count": 1,
  "timestamp": "2024-01-15T18:30:00"
}
```

#### GET /api/main-session-prices/futures
Получение цен закрытия для всех фьючерсов из T-INVEST API.

**Параметры:** Нет

```bash
curl -X GET "http://localhost:8087/api/main-session-prices/futures"
```

#### GET /api/main-session-prices/{figi}
Получение цены закрытия по конкретному инструменту из T-INVEST API.

**Параметры:**
- `figi` (path) - идентификатор инструмента

```bash
curl -X GET "http://localhost:8087/api/main-session-prices/BBG004730N88"
```

#### POST /api/main-session-prices/instrument/{figi}
Загрузка цены закрытия по конкретному инструменту из T-INVEST API в БД.

**Параметры:**
- `figi` (path) - идентификатор инструмента

```bash
curl -X POST "http://localhost:8087/api/main-session-prices/instrument/BBG004730N88"
```

### Цены Основной Сессии

#### POST /api/main-session-prices/{date}
Загрузка цен основной сессии за конкретную дату. Работает только с рабочими днями, использует данные из `minute_candles`, обрабатывает только акции и фьючерсы.

**Параметры:**
- `date` (path) - дата в формате ISO (YYYY-MM-DD)

```bash
curl -X POST "http://localhost:8087/api/main-session-prices/2024-01-15"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Успешно загружено 200 новых цен основной сессии из 250 найденных.",
  "date": "2024-01-15",
  "totalRequested": 250,
  "newItemsSaved": 200,
  "existingItemsSkipped": 30,
  "invalidItemsFiltered": 15,
  "missingFromApi": 5,
  "savedItems": [...]
}
```

## Особенности

### Валидация дат
- Все методы с датами проверяют, что дата является рабочим днем
- В выходные дни (суббота и воскресенье) возвращается ошибка
- Используется московское время для определения дня недели

### Источники данных
- **POST методы** - используют данные из таблицы `minute_candles`
- **GET методы** - получают данные из T-INVEST API
- Все данные сохраняются в таблицу `close_prices`

### Фильтрация данных
- Исключаются цены с датой 1970-01-01 (невалидные данные)
- Проверяется валидность цен (больше 0)
- Проверяется существование записей перед сохранением

### Обработка ошибок
- Все методы возвращают структурированные ответы с полем `success`
- В случае ошибки возвращается HTTP 500 с описанием проблемы
- Логируются детали ошибок в консоль

## Коды ответов

- `200 OK` - Успешное выполнение
- `400 Bad Request` - Некорректные параметры запроса
- `500 Internal Server Error` - Внутренняя ошибка сервера

## Примеры использования

### Загрузка цен закрытия основной сессии за сегодня
```bash
# Загрузить все цены закрытия основной сессии за сегодня
curl -X POST "http://localhost:8087/api/main-session-prices/"
```

### Загрузка цен основной сессии за дату
```bash
# Загрузить цены основной сессии за 15 января 2024
curl -X POST "http://localhost:8087/api/main-session-prices/2024-01-15"
```

### Получение цены закрытия по инструменту
```bash
# Получить цену закрытия для Сбербанка
curl -X GET "http://localhost:8087/api/main-session-prices/BBG004730N88"
```

### Загрузка цены закрытия по инструменту
```bash
# Загрузить цену закрытия для Сбербанка в БД
curl -X POST "http://localhost:8087/api/main-session-prices/instrument/BBG004730N88"
```
