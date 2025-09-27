# API — Последние сделки (`/api/last-trades`)

## Обзор

API для работы с последними обезличенными сделками финансовых инструментов.

Предоставляет возможности для:
- **Асинхронной загрузки** - сохранение сделок в базу данных
- **Фильтрации по типам** - акции, фьючерсы, все инструменты
- **Оптимизированной обработки** - параллельная обработка с пулами потоков
- **Мониторинга** - отслеживание выполнения через taskId

**Базовый URL:** `http://localhost:8083/api/last-trades` (PROD) / `http://localhost:8087/api/last-trades` (TEST)

**Особенности:**
- **Оптимизированная обработка** - несколько пулов потоков для разных задач
- **Гибкие запросы** - поддержка GET и POST методов
- **Логирование** - детальные логи в system_logs
- **Кэширование** - использование CachedInstrumentService

---

## POST /api/last-trades

Асинхронная загрузка последних сделок с поддержкой JSON тела запроса.

**Параметры запроса:**
- `figis` (body, опционально) - список FIGI инструментов
- `tradeSource` (body, опционально) - источник сделок

**Примеры использования:**
```bash
# Загрузка конкретных инструментов
curl -X POST "http://localhost:8083/api/last-trades" \
  -H "Content-Type: application/json" \
  -d '{"figis": ["BBG004730N88", "BBG004730ZJ9"], "tradeSource": "TRADE_SOURCE_ALL"}'

# Загрузка всех инструментов
curl -X POST "http://localhost:8083/api/last-trades" \
  -H "Content-Type: application/json" \
  -d '{"figis": ["ALL"], "tradeSource": "TRADE_SOURCE_ALL"}'
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Загрузка обезличенных сделок запущена в фоновом режиме",
  "taskId": "LAST_TRADES_12345678",
  "timestamp": "2024-01-15T18:30:00"
}
```

---

## GET /api/last-trades

Асинхронная загрузка последних сделок с поддержкой параметров запроса.

**Параметры запроса:**
- `figis` (query, опционально) - список FIGI инструментов через запятую
- `tradeSource` (query, опционально) - источник сделок

**Примеры использования:**
```bash
# Загрузка конкретных инструментов
curl "http://localhost:8083/api/last-trades?figis=BBG004730N88,BBG004730ZJ9&tradeSource=TRADE_SOURCE_ALL"

# Загрузка всех инструментов
curl "http://localhost:8083/api/last-trades?figis=ALL&tradeSource=TRADE_SOURCE_ALL"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Загрузка обезличенных сделок запущена в фоновом режиме",
  "taskId": "LAST_TRADES_87654321",
  "timestamp": "2024-01-15T18:30:00"
}
```

---

## GET /api/last-trades/shares

Асинхронная загрузка последних сделок только для акций.

**Примеры использования:**
```bash
# Загрузка сделок по акциям
curl "http://localhost:8083/api/last-trades/shares"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Загрузка обезличенных сделок запущена в фоновом режиме",
  "taskId": "SHARES_12345678",
  "timestamp": "2024-01-15T18:30:00"
}
```

---

## GET /api/last-trades/futures

Асинхронная загрузка последних сделок только для фьючерсов.

**Примеры использования:**
```bash
# Загрузка сделок по фьючерсам
curl "http://localhost:8083/api/last-trades/futures"
```

**Ответ (пример):**
```json
{
  "success": true,
  "message": "Загрузка обезличенных сделок запущена в фоновом режиме",
  "taskId": "FUTURES_87654321",
  "timestamp": "2024-01-15T18:30:00"
}
```

---

## GET /api/last-trades/performance

Получение информации о производительности executor'ов.

**Примеры использования:**
```bash
# Получение статистики производительности
curl "http://localhost:8083/api/last-trades/performance"
```

---

## 🔧 Технические детали

### Архитектура
- **LastTradesController** - основной контроллер для сделок
- **LastTradesService** - сервис для обработки сделок
- **CachedInstrumentService** - кэширование инструментов
- **SystemLogRepository** - логирование операций

### Пул потоков
- **lastTradesApiExecutor** - для API запросов
- **lastTradesBatchExecutor** - для пакетной обработки
- **lastTradesProcessingExecutor** - для обработки данных

### Асинхронная обработка
- **TaskId** - уникальный идентификатор для отслеживания
- **Фоновая обработка** - выполнение в отдельном потоке
- **Логирование** - детальные логи в system_logs
- **Мониторинг** - отслеживание статуса выполнения

### Источники данных
- **T-INVEST API** - получение данных о сделках
- **CachedInstrumentService** - кэширование инструментов
- **База данных** - сохранение результатов

---

## 💡 Примеры использования

### Загрузка сделок
```bash
# Загрузка конкретных инструментов (POST)
curl -X POST "http://localhost:8083/api/last-trades" \
  -H "Content-Type: application/json" \
  -d '{"figis": ["BBG004730N88", "BBG004730ZJ9"]}'

# Загрузка конкретных инструментов (GET)
curl "http://localhost:8083/api/last-trades?figis=BBG004730N88,BBG004730ZJ9"

# Загрузка только акций
curl "http://localhost:8083/api/last-trades/shares"

# Загрузка только фьючерсов
curl "http://localhost:8083/api/last-trades/futures"
```

### Мониторинг
```bash
# Проверка статуса задачи
curl "http://localhost:8083/api/status/{taskId}"

# Просмотр логов
curl "http://localhost:8083/api/system/logs?taskId={taskId}"

# Статистика производительности
curl "http://localhost:8083/api/last-trades/performance"
```

---

## ⚠️ Коды ответов

### Успешные ответы
- **200 OK** - операция запущена успешно

### Ошибки
- **500 Internal Server Error** - внутренняя ошибка сервера

### Примеры ошибок
```json
{
  "success": false,
  "message": "Ошибка загрузки обезличенных сделок: Connection timeout",
  "taskId": "LAST_TRADES_12345678",
  "timestamp": "2024-01-15T18:30:00"
}
```

---

## 🔄 Жизненный цикл обработки

### Асинхронная загрузка
1. **Запрос** - POST/GET запрос с параметрами
2. **Генерация taskId** - уникальный идентификатор
3. **Логирование** - запись в system_logs
4. **Запуск сервиса** - асинхронная обработка
5. **Ответ** - возврат taskId клиенту
6. **Обработка** - параллельная загрузка данных
7. **Завершение** - обновление статуса в логах

### Обработка ошибок
1. **Валидация** - проверка параметров запроса
2. **Логирование** - запись ошибок в system_logs
3. **Уведомление** - возврат ошибки клиенту
4. **Мониторинг** - отслеживание частоты ошибок
