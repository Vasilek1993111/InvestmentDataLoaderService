# API дневных свечей

## Обзор

Система предоставляет REST API для работы с дневными свечами, включая загрузку в базу данных с параллельной обработкой и получение данных без сохранения. Все POST методы используют оптимизированную параллельную обработку для максимальной производительности.

## Эндпоинты

### POST методы (загрузка в БД с параллельной обработкой)

#### POST /api/candles/daily
Загрузка дневных свечей за сегодня

**Запрос:**
```json
{
  "instruments": ["BBG004730N88", "BBG004730ZJ9"],
  "assetType": ["SHARES", "FUTURES"],
  "date": "2024-01-01"
}
```

**Ответ:**
```json
{
  "success": true,
  "message": "Загрузка дневных свечей за сегодня запущена",
  "taskId": "uuid-task-id",
  "endpoint": "/api/candles/daily",
  "instruments": ["BBG004730N88", "BBG004730ZJ9"],
  "assetTypes": ["SHARES", "FUTURES"],
  "status": "STARTED",
  "startTime": "2024-01-01T10:00:00Z"
}
```

#### POST /api/candles/daily/{date}
Загрузка дневных свечей за конкретную дату

**Параметры:**
- `date` - дата в формате YYYY-MM-DD

**Запрос и ответ:** аналогично предыдущему методу

#### POST /api/candles/daily/shares/{date}
Загрузка дневных свечей всех акций за дату

**Параметры:**
- `date` - дата в формате YYYY-MM-DD

**Ответ:**
```json
{
  "success": true,
  "message": "Загрузка дневных свечей акций запущена",
  "taskId": "uuid-task-id",
  "endpoint": "/api/candles/daily/shares/2024-01-01",
  "date": "2024-01-01",
  "instrumentsCount": 150,
  "status": "STARTED",
  "startTime": "2024-01-01T10:00:00Z"
}
```

#### POST /api/candles/daily/futures/{date}
Загрузка дневных свечей всех фьючерсов за дату

#### POST /api/candles/daily/indicatives/{date}
Загрузка дневных свечей всех индикативов за дату

#### POST /api/candles/instrument/daily/{figi}/{date}
Загрузка дневных свечей конкретного инструмента за дату

**Параметры:**
- `figi` - идентификатор инструмента
- `date` - дата в формате YYYY-MM-DD

**Ответ:**
```json
{
  "success": true,
  "message": "Сохранение дневных свечей инструмента BBG004730N88 за 2024-01-01 запущено",
  "taskId": "uuid-task-id",
  "endpoint": "/api/candles/instrument/daily/BBG004730N88/2024-01-01",
  "figi": "BBG004730N88",
  "date": "2024-01-01",
  "status": "STARTED",
  "startTime": "2024-01-01T10:00:00Z"
}
```

### GET методы (получение данных без сохранения)

#### GET /api/candles/daily/shares/{date}
Получение дневных свечей всех акций за дату

**Параметры:**
- `date` - дата в формате YYYY-MM-DD

**Ответ:**
```json
{
  "date": "2024-01-01",
  "assetType": "SHARES",
  "candles": [
    {
      "figi": "BBG004730N88",
      "ticker": "SBER",
      "name": "Сбербанк",
      "time": "2024-01-01T00:00:00Z",
      "open": 250.50,
      "close": 255.30,
      "high": 256.00,
      "low": 249.80,
      "volume": 1000000,
      "isComplete": true,
      "priceChange": 4.80,
      "priceChangePercent": 1.92,
      "candleType": "BULLISH",
      "bodySize": 4.80,
      "upperShadow": 0.70,
      "lowerShadow": 0.70,
      "highLowRange": 6.20,
      "averagePrice": 252.90
    }
  ],
  "totalCandles": 150,
  "totalInstruments": 150,
  "processedInstruments": 150,
  "successfulInstruments": 145,
  "noDataInstruments": 3,
  "errorInstruments": 2,
  "totalVolume": 150000000,
  "averagePrice": 250.75
}
```

#### GET /api/candles/daily/futures/{date}
Получение дневных свечей всех фьючерсов за дату

#### GET /api/candles/daily/indicatives/{date}
Получение дневных свечей всех индикативов за дату

#### GET /api/candles/instrument/daily/{figi}/{date}
Получение дневных свечей конкретного инструмента за дату

**Параметры:**
- `figi` - идентификатор инструмента
- `date` - дата в формате YYYY-MM-DD

**Ответ:**
```json
{
  "figi": "BBG004730N88",
  "date": "2024-01-01",
  "candles": [
    {
      "figi": "BBG004730N88",
      "ticker": null,
      "name": null,
      "time": "2024-01-01T00:00:00Z",
      "open": 250.50,
      "close": 255.30,
      "high": 256.00,
      "low": 249.80,
      "volume": 1000000,
      "isComplete": true,
      "priceChange": 4.80,
      "priceChangePercent": 1.92,
      "candleType": "BULLISH",
      "bodySize": 4.80,
      "upperShadow": 0.70,
      "lowerShadow": 0.70,
      "highLowRange": 6.20,
      "averagePrice": 252.90
    }
  ],
  "totalCandles": 1,
  "totalVolume": 1000000,
  "averagePrice": 252.90
}
```

### Синхронные методы (для обратной совместимости)

#### POST /api/candles/daily/shares/{date}/sync
Синхронная загрузка дневных свечей акций за дату

**Параметры:**
- `date` - дата в формате YYYY-MM-DD

**Ответ:**
```json
{
  "success": true,
  "message": "Загружено дневных свечей акций за 2024-01-01: Всего инструментов=150, Обработано=150, Новых=1200, Существующих=300, Невалидных=5, Отсутствующих=10",
  "taskId": "uuid-task-id",
  "startTime": "2024-01-01T10:00:00"
}
```

## Архитектура обработки

### Пул потоков

Система использует несколько специализированных пулов потоков:

1. **dailyCandleExecutor** - основной пул для обработки инструментов
   - Core: max(4, processors)
   - Max: max(8, processors * 2)
   - Queue: 500 задач

2. **dailyApiDataExecutor** - пул для API запросов
   - Core: max(3, processors / 2)
   - Max: max(6, processors)
   - Queue: 300 задач

3. **dailyBatchWriteExecutor** - пул для пакетной записи в БД
   - Core: max(2, processors / 2)
   - Max: max(4, processors)
   - Queue: 50 задач

### Алгоритм обработки

1. **Разбиение на батчи**: Инструменты разбиваются на батчи по 12.5% от общего количества
2. **Обработка батчей**: Каждый батч обрабатывается в отдельном потоке
3. **Обработка инструментов**: Внутри батча инструменты обрабатываются параллельно
4. **Асинхронные API запросы**: Запросы к внешнему API выполняются асинхронно
5. **Пакетная запись в БД**: Свечи сохраняются пакетами для оптимизации производительности

### Преимущества

- **Высокая производительность**: Обработка множества инструментов с оптимизацией
- **Масштабируемость**: Автоматическая настройка пулов потоков под количество процессоров
- **Отказоустойчивость**: Обработка ошибок на уровне отдельных инструментов
- **Мониторинг**: Подробное логирование процесса обработки

### Мониторинг

Все операции логируются в таблицу `system_logs` с детальной информацией:
- Статус обработки каждого инструмента
- Количество обработанных свечей
- Время выполнения операций
- Ошибки и исключения

### Обратная совместимость

Все существующие GET методы остались без изменений и продолжают работать в синхронном режиме для получения данных без сохранения.

### Примеры использования

```bash
# Загрузка всех дневных свечей за сегодня
curl -X POST http://localhost:8080/api/candles/daily \
  -H "Content-Type: application/json" \
  -d '{"assetType": ["SHARES", "FUTURES"]}'

# Загрузка дневных свечей акций за конкретную дату
curl -X POST http://localhost:8080/api/candles/daily/shares/2024-01-01

# Загрузка дневных свечей конкретного инструмента
curl -X POST http://localhost:8080/api/candles/instrument/daily/BBG004730N88/2024-01-01
```
