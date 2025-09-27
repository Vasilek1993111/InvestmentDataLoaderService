# API — Минутные свечи (`/api/candles/minute`)

## Обзор

API для работы с минутными свечами финансовых инструментов.

Предоставляет возможности для:
- **Асинхронной загрузки** - сохранение минутных свечей в базу данных
- **Синхронного получения** - получение данных без сохранения
- **Параллельной обработки** - оптимизированная производительность
- **Мониторинга** - отслеживание выполнения через taskId

**Базовый URL:** `http://localhost:8083/api/candles/minute` (PROD) / `http://localhost:8087/api/candles/minute` (TEST)

**Особенности:**
- **Асинхронная обработка** - POST методы возвращают taskId для отслеживания
- **Транзакционность** - все операции выполняются в транзакциях
- **Логирование** - детальные логи в system_logs
- **Параллельная обработка** - оптимизированная производительность

---

## POST /api/candles/minute

Асинхронная загрузка минутных свечей за сегодня.

Запускает асинхронную загрузку минутных свечей для указанных инструментов и типов активов.

**Параметры запроса:**
- `instruments` (опционально) - список FIGI инструментов
- `assetType` (опционально) - типы активов (SHARES, FUTURES, INDICATIVES)

**Примеры использования:**
```bash
# Загрузка всех акций за сегодня
curl -X POST "http://localhost:8083/api/candles/minute" \
  -H "Content-Type: application/json" \
  -d '{"assetType": ["SHARES"]}'

# Загрузка конкретных инструментов
curl -X POST "http://localhost:8083/api/candles/minute" \
  -H "Content-Type: application/json" \
  -d '{"instruments": ["BBG004730N88", "BBG004730ZJ9"]}'
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Загрузка минутных свечей за сегодня запущена",
  "taskId": "uuid-task-id",
  "endpoint": "/api/candles/minute",
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

## POST /api/candles/minute/{date}

Асинхронная загрузка минутных свечей за конкретную дату.

**Параметры запроса:**
- `date` (path) - дата в формате YYYY-MM-DD
- `instruments` (body, опционально) - список FIGI инструментов
- `assetType` (body, опционально) - типы активов

**Примеры использования:**
```bash
# Загрузка за конкретную дату
curl -X POST "http://localhost:8083/api/candles/minute/2024-01-15" \
  -H "Content-Type: application/json" \
  -d '{"assetType": ["SHARES", "FUTURES"]}'
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Загрузка минутных свечей за 2024-01-15 запущена",
  "taskId": "uuid-task-id",
  "endpoint": "/api/candles/minute/2024-01-15",
  "instruments": ["BBG004730N88"],
  "assetTypes": ["SHARES"],
  "status": "STARTED",
  "startTime": "2024-01-15T10:00:00Z"
}
```

---

## POST /api/candles/minute/shares/{date}

Асинхронная загрузка минутных свечей всех акций за дату.

**Параметры запроса:**
- `date` (path) - дата в формате YYYY-MM-DD

**Примеры использования:**
```bash
# Загрузка всех акций за дату
curl -X POST "http://localhost:8083/api/candles/minute/shares/2024-01-15"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Загрузка минутных свечей акций запущена",
  "taskId": "uuid-task-id",
  "endpoint": "/api/candles/minute/shares/2024-01-15",
  "date": "2024-01-15",
  "instrumentsCount": 150,
  "status": "STARTED",
  "startTime": "2024-01-15T10:00:00Z"
}
```

---

## POST /api/candles/minute/futures/{date}

Асинхронная загрузка минутных свечей всех фьючерсов за дату.

**Параметры запроса:**
- `date` (path) - дата в формате YYYY-MM-DD

**Примеры использования:**
```bash
# Загрузка всех фьючерсов за дату
curl -X POST "http://localhost:8083/api/candles/minute/futures/2024-01-15"
```

---

## POST /api/candles/minute/indicatives/{date}

Асинхронная загрузка минутных свечей всех индикативных инструментов за дату.

**Параметры запроса:**
- `date` (path) - дата в формате YYYY-MM-DD

**Примеры использования:**
```bash
# Загрузка всех индикативов за дату
curl -X POST "http://localhost:8083/api/candles/minute/indicatives/2024-01-15"
```

---

## GET /api/candles/minute/shares/{date}

Получение минутных свечей всех акций за дату без сохранения.

**Параметры запроса:**
- `date` (path) - дата в формате YYYY-MM-DD

**Примеры использования:**
```bash
# Получение свечей акций за дату
curl "http://localhost:8083/api/candles/minute/shares/2024-01-15"
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
      "open": 250.50,
      "close": 251.20,
      "high": 251.50,
      "low": 250.30,
      "volume": 1000,
      "time": "2024-01-15T09:00:00Z",
      "isComplete": true,
      "priceChange": 0.70,
      "priceChangePercent": 0.28,
      "candleType": "BULLISH",
      "bodySize": 0.70,
      "upperShadow": 0.30,
      "lowerShadow": 0.20,
      "highLowRange": 1.20,
      "averagePrice": 250.88
    }
  ],
  "totalCandles": 58500,
  "totalInstruments": 150,
  "processedInstruments": 150,
  "successfulInstruments": 145,
  "noDataInstruments": 3,
  "errorInstruments": 2,
  "totalVolume": 58500000,
  "averagePrice": 250.75
}
```

---

## GET /api/candles/minute/futures/{date}

Получение минутных свечей всех фьючерсов за дату без сохранения.

**Параметры запроса:**
- `date` (path) - дата в формате YYYY-MM-DD

**Примеры использования:**
```bash
# Получение свечей фьючерсов за дату
curl "http://localhost:8083/api/candles/minute/futures/2024-01-15"
```

---

## GET /api/candles/minute/indicatives/{date}

Получение минутных свечей всех индикативных инструментов за дату без сохранения.

**Параметры запроса:**
- `date` (path) - дата в формате YYYY-MM-DD

**Примеры использования:**
```bash
# Получение свечей индикативов за дату
curl "http://localhost:8083/api/candles/minute/indicatives/2024-01-15"
```

---

## 🔧 Технические детали

### Архитектура
- **CandlesMinuteController** - основной контроллер для минутных свечей
- **MinuteCandleService** - сервис для обработки свечей
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
- **MinuteCandleRequestDto** - запрос для загрузки
- **MinuteCandleExtendedDto** - расширенная информация о свече
- **SystemLogEntity** - логирование операций

---

## 💡 Примеры использования

### Асинхронная загрузка
```bash
# Загрузка всех акций за сегодня
curl -X POST "http://localhost:8083/api/candles/minute" \
  -H "Content-Type: application/json" \
  -d '{"assetType": ["SHARES"]}'

# Загрузка за конкретную дату
curl -X POST "http://localhost:8083/api/candles/minute/2024-01-15" \
  -H "Content-Type: application/json" \
  -d '{"assetType": ["SHARES", "FUTURES"]}'

# Загрузка конкретных инструментов
curl -X POST "http://localhost:8083/api/candles/minute" \
  -H "Content-Type: application/json" \
  -d '{"instruments": ["BBG004730N88", "BBG004730ZJ9"]}'
```

### Получение данных
```bash
# Получение свечей акций
curl "http://localhost:8083/api/candles/minute/shares/2024-01-15"

# Получение свечей фьючерсов
curl "http://localhost:8083/api/candles/minute/futures/2024-01-15"

# Получение свечей индикативов
curl "http://localhost:8083/api/candles/minute/indicatives/2024-01-15"
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