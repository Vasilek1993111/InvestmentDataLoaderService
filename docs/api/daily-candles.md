# API — Дневные свечи (`/api/candles/daily`)

## Обзор

API для работы с дневными свечами финансовых инструментов.

Предоставляет возможности для:
- **Асинхронной загрузки** - сохранение дневных свечей в базу данных
- **Синхронного получения** - получение данных без сохранения
- **Параллельной обработки** - оптимизированная производительность
- **Мониторинга** - отслеживание выполнения через taskId

**Базовый URL:** `http://localhost:8083/api/candles/daily` (PROD) / `http://localhost:8087/api/candles/daily` (TEST)

**Особенности:**
- **Асинхронная обработка** - POST методы возвращают taskId для отслеживания
- **Транзакционность** - все операции выполняются в транзакциях
- **Логирование** - детальные логи в system_logs
- **Параллельная обработка** - оптимизированная производительность

---

## POST /api/candles/daily

Асинхронная загрузка дневных свечей за сегодня.

Запускает асинхронную загрузку дневных свечей для указанных инструментов и типов активов.

**Параметры запроса:**
- `instruments` (опционально) - список FIGI инструментов
- `assetType` (опционально) - типы активов (SHARES, FUTURES, INDICATIVES)

**Примеры использования:**
```bash
# Загрузка всех акций за сегодня
curl -X POST "http://localhost:8083/api/candles/daily" \
  -H "Content-Type: application/json" \
  -d '{"assetType": ["SHARES"]}'

# Загрузка конкретных инструментов
curl -X POST "http://localhost:8083/api/candles/daily" \
  -H "Content-Type: application/json" \
  -d '{"instruments": ["BBG004730N88", "BBG004730ZJ9"]}'
```

**Ответ (пример):**
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

**Поля ответа:**
- `success` - статус операции (true/false)
- `message` - описание операции
- `taskId` - уникальный идентификатор задачи
- `endpoint` - эндпоинт API
- `instruments` - список инструментов
- `assetTypes` - типы активов
- `status` - статус выполнения
- `startTime` - время начала операции

---

## POST /api/candles/daily/{date}

Асинхронная загрузка дневных свечей за конкретную дату.

**Параметры запроса:**
- `date` (path) - дата в формате YYYY-MM-DD
- `instruments` (body, опционально) - список FIGI инструментов
- `assetType` (body, опционально) - типы активов

**Примеры использования:**
```bash
# Загрузка за конкретную дату
curl -X POST "http://localhost:8083/api/candles/daily/2024-01-15" \
  -H "Content-Type: application/json" \
  -d '{"assetType": ["SHARES", "FUTURES"]}'
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Загрузка дневных свечей за 2024-01-15 запущена",
  "taskId": "uuid-task-id",
  "endpoint": "/api/candles/daily/2024-01-15",
  "instruments": ["BBG004730N88"],
  "assetTypes": ["SHARES"],
  "status": "STARTED",
  "startTime": "2024-01-15T10:00:00Z"
}
```

---

## POST /api/candles/daily/shares/{date}

Асинхронная загрузка дневных свечей всех акций за дату.

**Параметры запроса:**
- `date` (path) - дата в формате YYYY-MM-DD

**Примеры использования:**
```bash
# Загрузка всех акций за дату
curl -X POST "http://localhost:8083/api/candles/daily/shares/2024-01-15"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Загрузка дневных свечей акций за 2024-01-15 запущена",
  "taskId": "uuid-task-id",
  "endpoint": "/api/candles/daily/shares/2024-01-15",
  "date": "2024-01-15",
  "instrumentsCount": 150,
  "status": "STARTED",
  "startTime": "2024-01-15T10:00:00Z"
}
```

---

## POST /api/candles/daily/futures/{date}

Асинхронная загрузка дневных свечей всех фьючерсов за дату.

**Параметры запроса:**
- `date` (path) - дата в формате YYYY-MM-DD

**Примеры использования:**
```bash
# Загрузка всех фьючерсов за дату
curl -X POST "http://localhost:8083/api/candles/daily/futures/2024-01-15"
```

---

## POST /api/candles/daily/indicatives/{date}

Асинхронная загрузка дневных свечей всех индикативов за дату.

**Параметры запроса:**
- `date` (path) - дата в формате YYYY-MM-DD

**Примеры использования:**
```bash
# Загрузка всех индикативов за дату
curl -X POST "http://localhost:8083/api/candles/daily/indicatives/2024-01-15"
```

---

## POST /api/candles/instrument/daily/{figi}/{date}

Асинхронная загрузка дневных свечей конкретного инструмента за дату.

**Параметры запроса:**
- `figi` (path) - идентификатор инструмента
- `date` (path) - дата в формате YYYY-MM-DD

**Примеры использования:**
```bash
# Загрузка конкретного инструмента
curl -X POST "http://localhost:8083/api/candles/instrument/daily/BBG004730N88/2024-01-15"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Сохранение дневных свечей инструмента BBG004730N88 за 2024-01-15 запущено",
  "taskId": "uuid-task-id",
  "endpoint": "/api/candles/instrument/daily/BBG004730N88/2024-01-15",
  "figi": "BBG004730N88",
  "date": "2024-01-15",
  "status": "STARTED",
  "startTime": "2024-01-15T10:00:00Z"
}
```

---

## GET /api/candles/daily/shares/{date}

Получение дневных свечей всех акций за дату без сохранения.

**Параметры запроса:**
- `date` (path) - дата в формате YYYY-MM-DD

**Примеры использования:**
```bash
# Получение свечей акций за дату
curl "http://localhost:8083/api/candles/daily/shares/2024-01-15"
```

**Ответ (пример):**
```json
{
  "date": "2024-01-15",
  "assetType": "SHARES",
  "candles": [
    {
      "figi": "BBG004730N88",
      "ticker": "SBER",
      "name": "Сбербанк",
      "time": "2024-01-15T00:00:00Z",
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

---

## GET /api/candles/daily/futures/{date}

Получение дневных свечей всех фьючерсов за дату без сохранения.

**Параметры запроса:**
- `date` (path) - дата в формате YYYY-MM-DD

**Примеры использования:**
```bash
# Получение свечей фьючерсов за дату
curl "http://localhost:8083/api/candles/daily/futures/2024-01-15"
```

---

## GET /api/candles/daily/indicatives/{date}

Получение дневных свечей всех индикативов за дату без сохранения.

**Параметры запроса:**
- `date` (path) - дата в формате YYYY-MM-DD

**Примеры использования:**
```bash
# Получение свечей индикативов за дату
curl "http://localhost:8083/api/candles/daily/indicatives/2024-01-15"
```

---

## GET /api/candles/instrument/daily/{figi}/{date}

Получение дневных свечей конкретного инструмента за дату без сохранения.

**Параметры запроса:**
- `figi` (path) - идентификатор инструмента
- `date` (path) - дата в формате YYYY-MM-DD

**Примеры использования:**
```bash
# Получение свечей конкретного инструмента
curl "http://localhost:8083/api/candles/instrument/daily/BBG004730N88/2024-01-15"
```

**Ответ (пример):**
```json
{
  "figi": "BBG004730N88",
  "date": "2024-01-15",
  "candles": [
    {
      "figi": "BBG004730N88",
      "ticker": null,
      "name": null,
      "time": "2024-01-15T00:00:00Z",
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

---

## POST /api/candles/daily/shares/{date}/sync

Синхронная загрузка дневных свечей акций за дату.

**Параметры запроса:**
- `date` (path) - дата в формате YYYY-MM-DD

**Примеры использования:**
```bash
# Синхронная загрузка акций
curl -X POST "http://localhost:8083/api/candles/daily/shares/2024-01-15/sync"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Загружено дневных свечей акций за 2024-01-15: Всего инструментов=150, Обработано=150, Новых=1200, Существующих=300, Невалидных=5, Отсутствующих=10",
  "taskId": "uuid-task-id",
  "startTime": "2024-01-15T10:00:00"
}
```

---

## 🔧 Технические детали

### Архитектура
- **CandlesDailyController** - основной контроллер для дневных свечей
- **DailyCandleService** - сервис для обработки свечей
- **TinkoffApiClient** - клиент для работы с Tinkoff API
- **@Transactional** - все операции выполняются в транзакциях

### Асинхронная обработка
- **TaskId** - уникальный идентификатор для отслеживания
- **Параллельная обработка** - оптимизированная производительность
- **Логирование** - детальные логи в system_logs
- **Мониторинг** - отслеживание статуса выполнения

### Типы активов
- **SHARES** - акции
- **FUTURES** - фьючерсы
- **INDICATIVES** - индикативные инструменты

### Форматы данных
- **DailyCandleRequestDto** - запрос для загрузки
- **DailyCandleExtendedDto** - расширенная информация о свече
- **SystemLogEntity** - логирование операций

---

## 💡 Примеры использования

### Асинхронная загрузка
```bash
# Загрузка всех акций за сегодня
curl -X POST "http://localhost:8083/api/candles/daily" \
  -H "Content-Type: application/json" \
  -d '{"assetType": ["SHARES"]}'

# Загрузка за конкретную дату
curl -X POST "http://localhost:8083/api/candles/daily/2024-01-15" \
  -H "Content-Type: application/json" \
  -d '{"assetType": ["SHARES", "FUTURES"]}'

# Загрузка конкретного инструмента
curl -X POST "http://localhost:8083/api/candles/instrument/daily/BBG004730N88/2024-01-15"
```

### Получение данных
```bash
# Получение свечей акций
curl "http://localhost:8083/api/candles/daily/shares/2024-01-15"

# Получение свечей фьючерсов
curl "http://localhost:8083/api/candles/daily/futures/2024-01-15"

# Получение свечей конкретного инструмента
curl "http://localhost:8083/api/candles/instrument/daily/BBG004730N88/2024-01-15"
```

### Мониторинг выполнения
```bash
# Проверка статуса задачи
curl "http://localhost:8083/api/status/{taskId}"

# Просмотр логов
curl "http://localhost:8083/api/system/logs?taskId={taskId}"
```

---

## ⚠️ Коды ответов

### Успешные ответы
- **200 OK** - данные получены успешно
- **202 Accepted** - асинхронная операция запущена

### Ошибки
- **400 Bad Request** - некорректные параметры запроса
- **500 Internal Server Error** - внутренняя ошибка сервера

### Примеры ошибок
```json
{
  "success": false,
  "message": "Ошибка запуска асинхронной загрузки: Connection timeout",
  "taskId": "uuid-task-id",
  "status": "ERROR"
}
```

---

## 🔄 Жизненный цикл обработки

### Асинхронная загрузка
1. **Запрос** - POST запрос с параметрами
2. **Генерация taskId** - уникальный идентификатор
3. **Логирование** - запись в system_logs
4. **Запуск сервиса** - асинхронная обработка
5. **Ответ** - возврат taskId клиенту
6. **Обработка** - параллельная загрузка данных
7. **Завершение** - обновление статуса в логах

### Синхронное получение
1. **Запрос** - GET запрос с параметрами
2. **Обработка** - получение данных из API
3. **Форматирование** - преобразование в DTO
4. **Ответ** - возврат данных клиенту

### Обработка ошибок
1. **Валидация** - проверка параметров запроса
2. **Логирование** - запись ошибок в system_logs
3. **Уведомление** - возврат ошибки клиенту
4. **Мониторинг** - отслеживание частоты ошибок
