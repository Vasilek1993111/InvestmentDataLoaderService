# API Documentation

## Обзор

Система предоставляет REST API для работы с инвестиционными данными, включая загрузку и получение свечей различных типов. Все POST методы используют оптимизированную параллельную обработку для максимальной производительности.

## Контроллеры

### 1. Минутные свечи (`CandlesMinuteController`)
- **Базовый путь**: `/api/candles/minute`
- **Функции**: Загрузка и получение минутных свечей
- **Особенности**: Параллельная обработка через `MinuteCandleService`

**Основные эндпоинты:**
- `POST /api/candles/minute` - загрузка за сегодня
- `POST /api/candles/minute/{date}` - загрузка за дату
- `POST /api/candles/minute/shares/{date}` - загрузка акций
- `POST /api/candles/minute/futures/{date}` - загрузка фьючерсов
- `POST /api/candles/minute/indicatives/{date}` - загрузка индикативов
- `GET /api/candles/minute/shares/{date}` - получение акций
- `GET /api/candles/minute/futures/{date}` - получение фьючерсов
- `GET /api/candles/minute/indicatives/{date}` - получение индикативов

### 2. Дневные свечи (`CandlesDailyController`)
- **Базовый путь**: `/api/candles/daily`
- **Функции**: Загрузка и получение дневных свечей
- **Особенности**: Параллельная обработка через `DailyCandleService`

**Основные эндпоинты:**
- `POST /api/candles/daily` - загрузка за сегодня
- `POST /api/candles/daily/{date}` - загрузка за дату
- `POST /api/candles/daily/shares/{date}` - загрузка акций
- `POST /api/candles/daily/futures/{date}` - загрузка фьючерсов
- `POST /api/candles/daily/indicatives/{date}` - загрузка индикативов
- `GET /api/candles/daily/shares/{date}` - получение акций
- `GET /api/candles/daily/futures/{date}` - получение фьючерсов
- `GET /api/candles/daily/indicatives/{date}` - получение индикативов
- `POST /api/candles/daily/shares/{date}/sync` - синхронная загрузка акций

### 3. Свечи инструментов (`CandlesInstrumentController`)
- **Базовый путь**: `/api/candles/instrument`
- **Функции**: Загрузка и получение свечей конкретных инструментов
- **Особенности**: Поддержка как минутных, так и дневных свечей

**Основные эндпоинты:**
- `GET /api/candles/instrument/minute/{figi}/{date}` - получение минутных свечей
- `POST /api/candles/instrument/minute/{figi}/{date}` - загрузка минутных свечей
- `GET /api/candles/instrument/daily/{figi}/{date}` - получение дневных свечей
- `POST /api/candles/instrument/daily/{figi}/{date}` - загрузка дневных свечей

## Архитектура

### Параллельная обработка

Все POST методы используют специализированные сервисы с параллельной обработкой:

#### MinuteCandleService
- **minuteCandleExecutor** - основной пул для обработки инструментов
- **apiDataExecutor** - пул для API запросов
- **batchWriteExecutor** - пул для пакетной записи в БД

#### DailyCandleService
- **dailyCandleExecutor** - основной пул для обработки инструментов
- **dailyApiDataExecutor** - пул для API запросов
- **dailyBatchWriteExecutor** - пул для пакетной записи в БД

### Алгоритм обработки

1. **Валидация параметров** - проверка входных данных
2. **Создание запроса** - формирование DTO для сервиса
3. **Параллельная загрузка** - асинхронная обработка через специализированные сервисы
4. **Логирование** - детальное логирование всех операций
5. **Возврат результата** - немедленный возврат taskId без ожидания завершения

## Типы данных

### Запросы

#### MinuteCandleRequestDto
```json
{
  "instruments": ["BBG004730N88", "BBG004730ZJ9"],
  "assetType": ["SHARES", "FUTURES"],
  "date": "2024-01-01"
}
```

#### DailyCandleRequestDto
```json
{
  "instruments": ["BBG004730N88", "BBG004730ZJ9"],
  "assetType": ["SHARES", "FUTURES"],
  "date": "2024-01-01"
}
```

### Ответы

#### Стандартный ответ POST методов
```json
{
  "success": true,
  "message": "Операция запущена",
  "taskId": "uuid-task-id",
  "endpoint": "/api/endpoint",
  "status": "STARTED",
  "startTime": "2024-01-01T10:00:00Z"
}
```

#### Стандартный ответ GET методов
```json
{
  "date": "2024-01-01",
  "assetType": "SHARES",
  "candles": [...],
  "totalCandles": 1000,
  "totalInstruments": 150,
  "processedInstruments": 150,
  "successfulInstruments": 145,
  "noDataInstruments": 3,
  "errorInstruments": 2,
  "totalVolume": 1000000,
  "averagePrice": 250.75
}
```

## Мониторинг

### Логирование

Все операции логируются в таблицу `system_logs` с детальной информацией:
- Статус обработки каждого инструмента
- Количество обработанных свечей
- Время выполнения операций
- Ошибки и исключения

### Статусы операций

- **STARTED** - операция запущена
- **SUCCESS** - успешное завершение
- **ERROR** - ошибка выполнения
- **NO_DATA** - отсутствие данных
- **COMPLETED** - операция завершена
- **FAILED** - критическая ошибка

## Коды ответов

- **200 OK** - Успешное получение данных (GET методы)
- **202 Accepted** - Загрузка запущена (POST методы)
- **400 Bad Request** - Неверные параметры запроса
- **500 Internal Server Error** - Внутренняя ошибка сервера

## Примеры использования

### Загрузка данных

```bash
# Загрузка минутных свечей за сегодня
curl -X POST http://localhost:8080/api/candles/minute \
  -H "Content-Type: application/json" \
  -d '{"assetType": ["SHARES", "FUTURES"]}'

# Загрузка дневных свечей акций за дату
curl -X POST http://localhost:8080/api/candles/daily/shares/2024-01-01

# Загрузка минутных свечей конкретного инструмента
curl -X POST http://localhost:8080/api/candles/instrument/minute/BBG004730N88/2024-01-01
```

### Получение данных

```bash
# Получение минутных свечей акций за дату
curl -X GET http://localhost:8080/api/candles/minute/shares/2024-01-01

# Получение дневных свечей фьючерсов за дату
curl -X GET http://localhost:8080/api/candles/daily/futures/2024-01-01

# Получение дневных свечей конкретного инструмента
curl -X GET http://localhost:8080/api/candles/instrument/daily/BBG004730N88/2024-01-01
```

## Обработка ошибок

Все ошибки логируются и возвращаются в стандартном формате:

```json
{
  "success": false,
  "message": "Ошибка выполнения: [описание ошибки]",
  "taskId": "uuid-task-id",
  "status": "ERROR"
}
```

## Производительность

### Оптимизации

- **Параллельная обработка** - множественные потоки для обработки инструментов
- **Пакетная запись** - группировка операций записи в БД
- **Асинхронные API запросы** - неблокирующие запросы к внешним сервисам
- **Настраиваемые пулы потоков** - автоматическая настройка под количество процессоров

### Рекомендации

1. **Для больших объемов данных** используйте POST методы с параллельной обработкой
2. **Для небольших наборов инструментов** можно использовать GET методы
3. **Мониторинг производительности** через логи системы
4. **Настройка пулов потоков** в зависимости от нагрузки сервера

## Дополнительные ресурсы

- [API минутных свечей](minute-candles.md)
- [API дневных свечей](daily-candles.md)
- [API свечей инструментов](instrument-candles.md)
- [Планировщики](schedulers.md)
- [Архитектура системы](architecture.md)
