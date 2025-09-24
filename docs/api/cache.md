# API — Управление кэшем (`/api/cache`)

## Обзор

API для управления кэшем финансовых инструментов предоставляет полный набор операций для работы с кэшем:

- **Прогрев кэша** - загрузка инструментов в кэш
- **Просмотр кэша** - получение информации о содержимом кэша
- **Очистка кэша** - удаление данных из кэша
- **Статистика кэша** - получение метрик кэша

### Поддерживаемые кэши:
- `sharesCache` - кэш акций
- `futuresCache` - кэш фьючерсов
- `indicativesCache` - кэш индикативных инструментов
- `closePricesCache` - кэш цен закрытия

---

## Прогрев кэша

### POST /api/cache/warmup

Принудительно загружает все основные инструменты в кэш.

**Что загружается в кэш:**
- Акции с биржи MOEX
- Все фьючерсы
- Все индикативные инструменты

**Когда использовать:**
- Принудительное обновление кэша без перезапуска приложения
- Тестирование производительности
- Восстановление кэша после очистки

**Пример запроса:**
```bash
curl -X POST "http://localhost:8083/api/cache/warmup"
```

**Пример успешного ответа:**
```json
{
  "success": true,
  "message": "Кэш успешно прогрет",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Пример ответа при ошибке:**
```json
{
  "success": false,
  "message": "Ошибка при прогреве кэша: Connection timeout",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Коды ответов:**
- `200 OK` - Кэш успешно прогрет
- `500 Internal Server Error` - Ошибка при прогреве кэша

---

## Просмотр содержимого кэша

### GET /api/cache/content

Возвращает информацию о содержимом кэша.

**Параметры запроса:**
- `cacheName` (опционально) - имя конкретного кэша для просмотра
- `limit` (опционально) - максимальное количество записей для отображения (по умолчанию 100)

**Примеры запросов:**

```bash
# Просмотр всех кэшей
curl "http://localhost:8083/api/cache/content"

# Просмотр конкретного кэша
curl "http://localhost:8083/api/cache/content?cacheName=sharesCache"

# Просмотр с ограничением количества записей
curl "http://localhost:8083/api/cache/content?limit=50"
```

**Пример ответа (все кэши):**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "caches": {
    "sharesCache": {
      "name": "sharesCache",
      "nativeCache": "CaffeineCache",
      "entryCount": 150,
      "sampleEntries": [
        {
          "key": "|MOEX|RUB||",
          "valueType": "ArrayList",
          "valueSize": 150
        }
      ],
      "sampleLimit": 100
    },
    "futuresCache": {
      "name": "futuresCache",
      "nativeCache": "CaffeineCache",
      "entryCount": 45,
      "sampleEntries": [
        {
          "key": "||RUB||",
          "valueType": "ArrayList",
          "valueSize": 45
        }
      ],
      "sampleLimit": 45
    }
  }
}
```

**Пример ответа (конкретный кэш):**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "cacheName": "sharesCache",
  "name": "sharesCache",
  "nativeCache": "CaffeineCache",
  "entryCount": 150,
  "sampleEntries": [
    {
      "key": "|MOEX|RUB||",
      "valueType": "ArrayList",
      "valueSize": 150
    },
    {
      "key": "|SPB|USD||",
      "valueType": "ArrayList",
      "valueSize": 25
    }
  ],
  "sampleLimit": 100
}
```

**Коды ответов:**
- `200 OK` - Содержимое кэша получено
- `400 Bad Request` - Некорректное имя кэша
- `500 Internal Server Error` - Ошибка при получении содержимого

---

## Статистика кэша

### GET /api/cache/stats

Возвращает общую статистику по всем кэшам.

**Пример запроса:**
```bash
curl "http://localhost:8083/api/cache/stats"
```

**Пример ответа:**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "totalCaches": 4,
  "activeCaches": 3,
  "totalEntries": 195,
  "cacheDetails": {
    "sharesCache": {
      "name": "sharesCache",
      "nativeCache": "CaffeineCache",
      "entryCount": 150,
      "sampleEntries": [],
      "sampleLimit": 0
    },
    "futuresCache": {
      "name": "futuresCache",
      "nativeCache": "CaffeineCache",
      "entryCount": 45,
      "sampleEntries": [],
      "sampleLimit": 0
    },
    "indicativesCache": {
      "name": "indicativesCache",
      "nativeCache": "CaffeineCache",
      "entryCount": 0,
      "sampleEntries": [],
      "sampleLimit": 0
    }
  }
}
```

**Коды ответов:**
- `200 OK` - Статистика получена
- `500 Internal Server Error` - Ошибка при получении статистики

---

## Очистка кэша

### DELETE /api/cache/clear

Очищает содержимое кэша.

**Параметры запроса:**
- `cacheName` (опционально) - имя конкретного кэша для очистки

**Примеры запросов:**

```bash
# Очистка всех кэшей
curl -X DELETE "http://localhost:8083/api/cache/clear"

# Очистка конкретного кэша
curl -X DELETE "http://localhost:8083/api/cache/clear?cacheName=sharesCache"
```

**Пример ответа (очистка всех кэшей):**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "success": true,
  "message": "Все кэши успешно очищены",
  "clearedCaches": ["sharesCache", "futuresCache", "indicativesCache", "closePricesCache"]
}
```

**Пример ответа (очистка конкретного кэша):**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "success": true,
  "message": "Кэш 'sharesCache' успешно очищен",
  "clearedCache": "sharesCache"
}
```

**Пример ответа при ошибке:**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "success": false,
  "error": "Ошибка при очистке кэша: Cache not found",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Коды ответов:**
- `200 OK` - Кэш успешно очищен
- `400 Bad Request` - Некорректное имя кэша
- `500 Internal Server Error` - Ошибка при очистке кэша

---

## Коды ошибок

- `200 OK` - Успешный запрос
- `400 Bad Request` - Некорректные параметры запроса
- `500 Internal Server Error` - Внутренняя ошибка сервера

## Особенности

1. **Автоматический прогрев** - кэш автоматически прогревается при запуске приложения
2. **Безопасная очистка** - очистка кэша не влияет на данные в базе данных
3. **Ограничение выборки** - для больших кэшей показывается только образец записей
4. **Детальная статистика** - предоставляется подробная информация о каждом кэше
5. **Временные метки** - все ответы содержат временные метки для отслеживания
6. **Обработка ошибок** - все операции включают обработку ошибок с информативными сообщениями
