# API — Работа со свечами

## Структура контроллеров

API для работы со свечами разделен на специализированные контроллеры:

- **`CandlesMinuteController`** (`/api/candles/minute`) - все минутные свечи
- **`CandlesDailyController`** (`/api/candles/daily`) - все дневные свечи  
- **`CandlesInstrumentController`** (`/api/candles/instrument`) - конкретные инструменты

## Минутные свечи (`/api/candles/minute`)

### Общие методы

#### POST /api/candles/minute
Асинхронная загрузка минутных свечей за сегодня. Тело запроса опционально.
```json
{
  "instruments": ["BBG004730N88", "BBG004730ZJ9"],
  "date": "2024-01-15",
  "assetType": ["SHARES", "FUTURES"]
}
```

#### POST /api/candles/minute/{date}
Загрузка минутных свечей за конкретную дату.
```bash
curl -X POST "http://localhost:8087/api/candles/minute/2024-01-15"
```

### Акции

#### GET /api/candles/minute/shares/{date}
Получение минутных свечей акций за дату без сохранения в БД.
```bash
curl -X GET "http://localhost:8087/api/candles/minute/shares/2024-01-15"
```

#### POST /api/candles/minute/shares/{date}
Загрузка минутных свечей акций за дату.
```bash
curl -X POST "http://localhost:8087/api/candles/minute/shares/2024-01-15"
```

### Фьючерсы

#### GET /api/candles/minute/futures/{date}
Получение минутных свечей фьючерсов за дату без сохранения в БД.
```bash
curl -X GET "http://localhost:8087/api/candles/minute/futures/2024-01-15"
```

#### POST /api/candles/minute/futures/{date}
Загрузка минутных свечей фьючерсов за дату.
```bash
curl -X POST "http://localhost:8087/api/candles/minute/futures/2024-01-15"
```

### Индикативы

#### GET /api/candles/minute/indicatives/{date}
Получение минутных свечей индикативов за дату без сохранения в БД.
```bash
curl -X GET "http://localhost:8087/api/candles/minute/indicatives/2024-01-15"
```

#### POST /api/candles/minute/indicatives/{date}
Загрузка минутных свечей индикативов за дату.
```bash
curl -X POST "http://localhost:8087/api/candles/minute/indicatives/2024-01-15"
```

## Дневные свечи (`/api/candles/daily`)

### Общие методы

#### POST /api/candles/daily
Асинхронная загрузка дневных свечей за сегодня. Тело запроса опционально.

#### POST /api/candles/daily/{date}
Загрузка дневных свечей за конкретную дату.
```bash
curl -X POST "http://localhost:8087/api/candles/daily/2024-01-15"
```

### Акции

#### GET /api/candles/daily/shares/{date}
Получение дневных свечей акций за дату без сохранения в БД.
```bash
curl -X GET "http://localhost:8087/api/candles/daily/shares/2024-01-15"
```

#### POST /api/candles/daily/shares/{date}
Загрузка дневных свечей акций за дату.
```bash
curl -X POST "http://localhost:8087/api/candles/daily/shares/2024-01-15"
```

### Фьючерсы

#### GET /api/candles/daily/futures/{date}
Получение дневных свечей фьючерсов за дату без сохранения в БД.
```bash
curl -X GET "http://localhost:8087/api/candles/daily/futures/2024-01-15"
```

#### POST /api/candles/daily/futures/{date}
Загрузка дневных свечей фьючерсов за дату.
```bash
curl -X POST "http://localhost:8087/api/candles/daily/futures/2024-01-15"
```

### Индикативы

#### GET /api/candles/daily/indicatives/{date}
Получение дневных свечей индикативов за дату без сохранения в БД.
```bash
curl -X GET "http://localhost:8087/api/candles/daily/indicatives/2024-01-15"
```

#### POST /api/candles/daily/indicatives/{date}
Загрузка дневных свечей индикативов за дату.
```bash
curl -X POST "http://localhost:8087/api/candles/daily/indicatives/2024-01-15"
```

## Конкретные инструменты (`/api/candles/instrument`)

### Минутные свечи

#### GET /api/candles/instrument/minute/{figi}/{date}
Получение минутных свечей конкретного инструмента за дату без сохранения в БД.
```bash
curl -X GET "http://localhost:8087/api/candles/instrument/minute/BBG004730N88/2024-01-15"
```

#### POST /api/candles/instrument/minute/{figi}/{date}
Сохранение минутных свечей конкретного инструмента за дату в БД.
```bash
curl -X POST "http://localhost:8087/api/candles/instrument/minute/BBG004730N88/2024-01-15"
```

### Дневные свечи

#### GET /api/candles/instrument/daily/{figi}/{date}
Получение дневных свечей конкретного инструмента за дату без сохранения в БД.
```bash
curl -X GET "http://localhost:8087/api/candles/instrument/daily/BBG004730N88/2024-01-15"
```

#### POST /api/candles/instrument/daily/{figi}/{date}
Сохранение дневных свечей конкретного инструмента за дату в БД.
```bash
curl -X POST "http://localhost:8087/api/candles/instrument/daily/BBG004730N88/2024-01-15"
```

## Формат ответов

### GET методы (получение данных)

Все GET методы возвращают данные в следующем формате:

```json
{
  "date": "2024-01-15",
  "assetType": "SHARES",
  "totalInstruments": 150,
  "totalCandles": 1250,
  "totalVolume": 1500000.50,
  "averagePrice": 125.75,
  "candles": [
    {
      "figi": "BBG004730N88",
      "ticker": "SBER",
      "name": "Сбербанк",
      "open": 125.50,
      "close": 126.00,
      "high": 126.25,
      "low": 125.30,
      "volume": 1000,
      "time": "2024-01-15T10:00:00Z",
      "isComplete": true,
      "priceChange": 0.50,
      "priceChangePercent": 0.40,
      "highLowRange": 0.95,
      "bodySize": 0.50,
      "upperShadow": 0.25,
      "lowerShadow": 0.20,
      "averagePrice": 125.75,
      "candleType": "BULLISH"
    }
  ]
}
```

### POST методы (загрузка данных)

POST методы возвращают результат операции:

```json
{
  "success": true,
  "message": "Загрузка минутных свечей завершена успешно за 2024-01-15: Всего инструментов=150, Обработано=150, Новых=1250, Существующих=0, Невалидных=0, Отсутствующих=0"
}
```

## Расширенная статистика

Все GET методы возвращают расширенную статистику для каждой свечи:

- `priceChange` - изменение цены (close - open)
- `priceChangePercent` - изменение цены в процентах
- `highLowRange` - диапазон (high - low)
- `bodySize` - размер тела свечи (|close - open|)
- `upperShadow` - верхняя тень
- `lowerShadow` - нижняя тень
- `averagePrice` - средняя цена (high + low + open + close) / 4
- `candleType` - тип свечи (BULLISH, BEARISH, DOJI)

## Логирование

### Система логирования

Все методы (GET и POST) записывают детальные логи в таблицу `invest.system_logs`:

#### Статусы логирования:
- `STARTED` - начало выполнения метода
- `COMPLETED` - успешное завершение
- `FAILED` - ошибка выполнения

#### Информация в логах:
- `taskId` - уникальный идентификатор задачи
- `endpoint` - путь к эндпоинту
- `method` - HTTP метод (GET/POST)
- `status` - статус выполнения
- `message` - детальное сообщение с результатами
- `startTime` - время начала
- `endTime` - время завершения
- `durationMs` - длительность выполнения в миллисекундах

### Примеры запросов к логам:

```sql
-- Просмотр логов по task_id
SELECT * FROM invest.system_logs WHERE task_id = 'your-task-id' ORDER BY created_at;

-- Просмотр всех активных задач
SELECT * FROM invest.system_logs WHERE status IN ('STARTED', 'PROCESSING') ORDER BY start_time DESC;

-- Просмотр логов по endpoint
SELECT * FROM invest.system_logs WHERE endpoint LIKE '%futures/minute%' ORDER BY created_at DESC;

-- Просмотр логов по методу
SELECT * FROM invest.system_logs WHERE method = 'GET' ORDER BY created_at DESC;

-- Статистика по статусам
SELECT status, COUNT(*) as count FROM invest.system_logs GROUP BY status;
```

## Коды ответов

- `200 OK` - успешное выполнение
- `400 Bad Request` - неверные параметры запроса
- `500 Internal Server Error` - внутренняя ошибка сервера

## Примечания

- Все даты должны быть в формате ISO (YYYY-MM-DD)
- FIGI должен быть валидным идентификатором инструмента
- Асинхронные операции возвращают `taskId` для отслеживания статуса
- GET методы не сохраняют данные в БД, только возвращают их
- POST методы запускают асинхронную загрузку в фоновом режиме
- Все методы имеют детальное логирование в `invest.system_logs`
- Контроллеры разделены по типам свечей для лучшей организации кода