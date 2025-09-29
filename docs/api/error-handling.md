# Обработка ошибок в API

## HTTP статус-коды

### 400 Bad Request - Ошибки клиента

**Когда используется:**
- Некорректные параметры запроса
- Невалидные значения в параметрах
- Некорректный формат JSON
- Отсутствующие обязательные параметры
- Неверный тип параметра
- Неподдерживаемый HTTP метод
- Некорректные URL
- Невалидные значения в теле POST запроса

**Примеры:**

```json
// Невалидная валюта
GET /api/instruments/shares?currency=EUR
{
    "success": false,
    "message": "Ошибка валидации: Невалидная валюта: EUR. Допустимые значения: [RUB, USD, rub, usd]",
    "timestamp": "2025-09-29T11:15:30.123456",
    "error": "ValidationException",
    "field": "currency"
}

// Некорректный JSON
POST /api/instruments/shares
{
    "exchange": "moex_mrng_evng_e_wknd_dlr"
    // Пропущена запятая
}
{
    "success": false,
    "message": "Некорректный формат JSON в теле запроса: ...",
    "timestamp": "2025-09-29T11:15:30.123456",
    "error": "BadRequest"
}

// Отсутствует обязательный параметр
GET /api/instruments/shares
// Если source был бы обязательным
{
    "success": false,
    "message": "Отсутствует обязательный параметр: source (String)",
    "timestamp": "2025-09-29T11:15:30.123456",
    "error": "BadRequest",
    "parameter": "source",
    "type": "String"
}

// Невалидные значения в POST запросе
POST /api/instruments/shares
{
    "status": "INVALID_STATUS",
    "exchange": "INVALID_EXCHANGE",
    "currency": "EUR"
}
{
    "success": false,
    "message": "Ошибка валидации: Невалидный статус инструмента: INVALID_STATUS. Допустимые значения: [INSTRUMENT_STATUS_UNSPECIFIED, INSTRUMENT_STATUS_BASE, INSTRUMENT_STATUS_ALL]",
    "timestamp": "2025-09-29T11:15:30.123456",
    "error": "ValidationException",
    "field": "status"
}

// Невалидная биржа в POST запросе
POST /api/instruments/futures
{
    "exchange": "INVALID_EXCHANGE",
    "currency": "RUB"
}
{
    "success": false,
    "message": "Ошибка валидации: Невалидная биржа: INVALID_EXCHANGE. Допустимые значения: [moex_mrng_evng_e_wknd_dlr, MOEX, moex, SPB, FORTS_MAIN, UNKNOWN]",
    "timestamp": "2025-09-29T11:15:30.123456",
    "error": "ValidationException",
    "field": "exchange"
}
```

### 404 Not Found - Ресурс не найден

**Когда используется:**
- Инструмент не найден в базе данных
- Эндпоинт не существует
- Статический ресурс не найден

**Примеры:**

```json
// Инструмент не найден
GET /api/instruments/shares/BBG002B9T6Y1
{
    "success": false,
    "message": "Акция с FIGI 'BBG002B9T6Y1' не найдена в базе данных",
    "timestamp": "2025-09-29T11:15:30.123456",
    "error": "NotFound",
    "identifier": "BBG002B9T6Y1",
    "type": "FIGI"
}

// Эндпоинт не существует
GET /api/nonexistent/endpoint
{
    "success": false,
    "message": "Эндпоинт не найден: http://localhost:8080/api/nonexistent/endpoint",
    "timestamp": "2025-09-29T11:15:30.123456",
    "error": "NotFound",
    "path": "http://localhost:8080/api/nonexistent/endpoint",
    "method": "GET"
}
```

### 500 Internal Server Error - Ошибки сервера

**Когда используется:**
- Ошибки загрузки данных
- Ошибки планировщика
- API ошибки
- Неожиданные ошибки

**Примеры:**

```json
// Ошибка загрузки данных
{
    "success": false,
    "message": "Ошибка загрузки данных: Не удалось подключиться к Tinkoff API",
    "timestamp": "2025-09-29T11:15:30.123456",
    "error": "DataLoadException"
}

// Ошибка планировщика
{
    "success": false,
    "message": "Ошибка планировщика: Не удалось выполнить задачу загрузки данных",
    "timestamp": "2025-09-29T11:15:30.123456",
    "error": "SchedulerException"
}

// Неожиданная ошибка
{
    "success": false,
    "message": "Неожиданная ошибка: NullPointerException in service method",
    "timestamp": "2025-09-29T11:15:30.123456",
    "error": "Exception"
}
```

## Структура ответа об ошибке

Все ошибки возвращаются в едином формате:

```json
{
    "success": false,
    "message": "Описание ошибки",
    "timestamp": "2025-09-29T11:15:30.123456",
    "error": "Тип ошибки",
    // Дополнительные поля в зависимости от типа ошибки
    "field": "название поля",
    "parameter": "название параметра",
    "identifier": "идентификатор",
    "type": "тип поиска"
}
```

## Рекомендации по использованию

1. **400 Bad Request** - используйте для всех ошибок, связанных с некорректными данными от клиента
2. **404 Not Found** - используйте когда запрашиваемый ресурс не существует
3. **500 Internal Server Error** - используйте для всех внутренних ошибок сервера

## Логирование

Все ошибки логируются на уровне сервера с соответствующим уровнем:
- 400/404 - WARN
- 500 - ERROR
