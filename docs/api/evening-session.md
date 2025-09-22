# API — Цены вечерней сессии (`/api/evening-session-prices`)

Контроллер для работы с ценами вечерней сессии. Все методы работают с данными из T-Invest API и сохраняют результаты в таблицу `close_price_evening_session`.

## Получение цен вечерней сессии

### GET /api/evening-session-prices/shares/{date}
Получение цен вечерней сессии для всех акций за конкретную дату. Работает только с рабочими днями, использует данные из `minute_candles`, обрабатывает только акции.

**Параметры:**
- `date` (path) - дата в формате ISO (YYYY-MM-DD)

```bash
curl -X GET "http://localhost:8087/api/evening-session-prices/shares/2024-01-15"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Цены вечерней сессии для акций за 2024-01-15 получены успешно",
  "data": [
    {
      "figi": "BBG004730N88",
      "ticker": "SBER",
      "name": "Сбербанк",
      "priceDate": "2024-01-15",
      "closePrice": 251.20,
      "instrumentType": "SHARE",
      "currency": "RUB",
      "exchange": "MOEX"
    }
  ],
  "count": 1,
  "date": "2024-01-15",
  "totalProcessed": 30,
  "foundPrices": 25,
  "missingData": 5,
  "timestamp": "2024-01-15T18:30:00"
}
```

### GET /api/evening-session-prices/futures/{date}
Получение цен вечерней сессии для всех фьючерсов за конкретную дату. Работает только с рабочими днями, использует данные из `minute_candles`, обрабатывает только фьючерсы.

**Параметры:**
- `date` (path) - дата в формате ISO (YYYY-MM-DD)

```bash
curl -X GET "http://localhost:8087/api/evening-session-prices/futures/2024-01-15"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Цены вечерней сессии для фьючерсов за 2024-01-15 получены успешно",
  "data": [
    {
      "figi": "FUTSILV0324",
      "ticker": "SILV-3.24",
      "name": "SILV-3.24",
      "priceDate": "2024-01-15",
      "closePrice": 85.75,
      "instrumentType": "FUTURE",
      "currency": "RUB",
      "exchange": "MOEX"
    }
  ],
  "count": 1,
  "date": "2024-01-15",
  "totalProcessed": 15,
  "foundPrices": 12,
  "missingData": 3,
  "timestamp": "2024-01-15T18:30:00"
}
```

### GET /api/evening-session-prices/{date}
Получение цен вечерней сессии для всех инструментов (акции + фьючерсы) за конкретную дату. Работает только с рабочими днями, использует данные из `minute_candles`.

**Параметры:**
- `date` (path) - дата в формате ISO (YYYY-MM-DD)

```bash
curl -X GET "http://localhost:8087/api/evening-session-prices/2024-01-15"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Цены вечерней сессии для всех инструментов за 2024-01-15 получены успешно",
  "data": [
    {
      "figi": "BBG004730N88",
      "ticker": "SBER",
      "name": "Сбербанк",
      "priceDate": "2024-01-15",
      "closePrice": 251.20,
      "instrumentType": "SHARE",
      "currency": "RUB",
      "exchange": "MOEX"
    },
    {
      "figi": "FUTSILV0324",
      "ticker": "SILV-3.24",
      "name": "SILV-3.24",
      "priceDate": "2024-01-15",
      "closePrice": 85.75,
      "instrumentType": "FUTURE",
      "currency": "RUB",
      "exchange": "MOEX"
    }
  ],
  "count": 2,
  "date": "2024-01-15",
  "totalProcessed": 45,
  "foundPrices": 37,
  "missingData": 8,
  "timestamp": "2024-01-15T18:30:00"
}
```

### GET /api/evening-session-prices/{figi}/{date}
Получение цены вечерней сессии по конкретному инструменту за конкретную дату. Работает только с рабочими днями, использует данные из `minute_candles`.

**Параметры:**
- `figi` (path) - идентификатор инструмента
- `date` (path) - дата в формате ISO (YYYY-MM-DD)

```bash
curl -X GET "http://localhost:8087/api/evening-session-prices/BBG004730N88/2024-01-15"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Цена вечерней сессии для инструмента BBG004730N88 за 2024-01-15 получена успешно",
  "data": {
    "figi": "BBG004730N88",
    "priceDate": "2024-01-15",
    "closePrice": 251.20,
    "instrumentType": "UNKNOWN",
    "currency": "RUB",
    "exchange": "MOEX"
  },
  "figi": "BBG004730N88",
  "date": "2024-01-15",
  "timestamp": "2024-01-15T18:30:00"
}
```

### POST /api/evening-session-prices/{figi}/{date}
Сохранение цены вечерней сессии по конкретному инструменту за конкретную дату. Работает только с рабочими днями, использует данные из `minute_candles` и сохраняет в БД.

**Параметры:**
- `figi` (path) - идентификатор инструмента
- `date` (path) - дата в формате ISO (YYYY-MM-DD)

```bash
curl -X POST "http://localhost:8087/api/evening-session-prices/BBG004730N88/2024-01-15"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Цена вечерней сессии для инструмента BBG004730N88 за 2024-01-15 сохранена успешно",
  "data": {
    "figi": "BBG004730N88",
    "priceDate": "2024-01-15",
    "closePrice": 251.20,
    "instrumentType": "SHARE",
    "currency": "RUB",
    "exchange": "MOEX"
  },
  "figi": "BBG004730N88",
  "date": "2024-01-15",
  "timestamp": "2024-01-15T18:30:00"
}
```

## Загрузка цен вечерней сессии

### POST /api/evening-session-prices
Загрузка цен вечерней сессии за сегодня. Работает только с акциями и фьючерсами, использует данные из `minute_candles`.

```bash
curl -X POST "http://localhost:8087/api/evening-session-prices"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Успешно загружено 25 новых цен вечерней сессии из 30 найденных.",
  "date": "2024-01-15",
  "totalRequested": 30,
  "newItemsSaved": 25,
  "existingItemsSkipped": 5,
  "invalidItemsFiltered": 0,
  "missingFromApi": 0,
  "savedItems": [
    {
      "figi": "BBG004730N88",
      "ticker": "SBER",
      "name": "Сбербанк",
      "priceDate": "2024-01-15",
      "closePrice": 251.20,
      "instrumentType": "SHARE",
      "currency": "RUB",
      "exchange": "MOEX"
    }
  ],
  "timestamp": "2024-01-15T18:30:00"
}
```

### POST /api/evening-session-prices/{date}
Загрузка цен вечерней сессии за конкретную дату. Работает только с рабочими днями, использует данные из `minute_candles`.

**Параметры:**
- `date` (path) - дата в формате ISO (YYYY-MM-DD)

```bash
curl -X POST "http://localhost:8087/api/evening-session-prices/2024-01-15"
```

**Ответ:** аналогичен POST `/api/evening-session-prices`

### POST /api/evening-session-prices/shares/{date}
Сохранение цен вечерней сессии для акций за конкретную дату. Работает только с рабочими днями, использует данные из `minute_candles`, обрабатывает только акции и сохраняет в БД.

**Параметры:**
- `date` (path) - дата в формате ISO (YYYY-MM-DD)

```bash
curl -X POST "http://localhost:8087/api/evening-session-prices/shares/2024-01-15"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Успешно сохранено 25 новых цен вечерней сессии для акций за 2024-01-15",
  "date": "2024-01-15",
  "totalProcessed": 30,
  "newItemsSaved": 25,
  "existingItemsSkipped": 3,
  "invalidItemsFiltered": 1,
  "missingData": 1,
  "savedItems": [
    {
      "figi": "BBG004730N88",
      "ticker": "SBER",
      "name": "Сбербанк",
      "priceDate": "2024-01-15",
      "closePrice": 251.20,
      "instrumentType": "SHARE"
    }
  ],
  "timestamp": "2024-01-15T18:30:00"
}
```

### POST /api/evening-session-prices/futures/{date}
Сохранение цен вечерней сессии для фьючерсов за конкретную дату. Работает только с рабочими днями, использует данные из `minute_candles`, обрабатывает только фьючерсы и сохраняет в БД.

**Параметры:**
- `date` (path) - дата в формате ISO (YYYY-MM-DD)

```bash
curl -X POST "http://localhost:8087/api/evening-session-prices/futures/2024-01-15"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Успешно сохранено 12 новых цен вечерней сессии для фьючерсов за 2024-01-15",
  "date": "2024-01-15",
  "totalProcessed": 15,
  "newItemsSaved": 12,
  "existingItemsSkipped": 2,
  "invalidItemsFiltered": 0,
  "missingData": 1,
  "savedItems": [
    {
      "figi": "FUTSILV0324",
      "ticker": "SILV-3.24",
      "name": "SILV-3.24",
      "priceDate": "2024-01-15",
      "closePrice": 85.75,
      "instrumentType": "FUTURE"
    }
  ],
  "timestamp": "2024-01-15T18:30:00"
}
```

### POST /api/evening-session-prices/save
Синхронная точечная загрузка цен вечерней сессии по указанным инструментам.

**Тело запроса (опционально):**
```json
{
  "instruments": ["BBG004730N88", "FUTSILV0324"],
  "date": "2024-01-15"
}
```

```bash
curl -X POST "http://localhost:8087/api/evening-session-prices/save" \
  -H "Content-Type: application/json" \
  -d '{"instruments": ["BBG004730N88"], "date": "2024-01-15"}'
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Успешно сохранено 1 записей",
  "savedCount": 1,
  "skippedCount": 0,
  "errorCount": 0
}
```

## Особенности работы

### Валидация дат
- Все методы проверяют, что дата является рабочим днем
- В выходные дни (суббота и воскресенье) возвращается ошибка
- Используется московское время для определения дня недели

### Источники данных
- **GET методы с датой** (`/{date}`, `/shares/{date}`, `/futures/{date}`, `/{figi}/{date}`) - используют данные из таблицы `minute_candles`
- **POST методы** - используют данные из таблицы `minute_candles`
- Все данные сохраняются в таблицу `close_price_evening_session`

### Фильтрация данных
- **GET методы с датой** (`/{date}`, `/shares/{date}`, `/futures/{date}`, `/{figi}/{date}`) - проверяется валидность цен (больше 0), исключаются инструменты без данных
- **POST методы** - проверяется валидность цен (больше 0), исключаются инструменты без данных, проверяется существование записей, определяется тип инструмента

### Обработка ошибок
- Все методы возвращают структурированные ответы с полем `success`
- В случае ошибки возвращается HTTP 500 с описанием проблемы
- Логируются детали ошибок в консоль
