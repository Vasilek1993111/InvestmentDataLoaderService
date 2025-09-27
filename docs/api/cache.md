# API — Управление кэшем (`/api/cache`)

## Обзор

API для управления кэшем финансовых инструментов предоставляет полный набор операций для работы с кэшем:

- **Прогрев кэша** - загрузка инструментов в кэш
- **Просмотр кэша** - получение информации о содержимом кэша
- **Очистка кэша** - удаление данных из кэша

**Базовый URL:** `http://localhost:8083/api/cache` (PROD) / `http://localhost:8087/api/cache` (TEST)

### Поддерживаемые кэши:
- `sharesCache` - кэш акций
- `futuresCache` - кэш фьючерсов
- `indicativesCache` - кэш индикативных инструментов
- `closePricesCache` - кэш цен закрытия

**Особенности:**
- **Caffeine Cache** - высокопроизводительный кэш на основе Caffeine
- **Автоматический прогрев** - кэш автоматически прогревается при запуске
- **Безопасная очистка** - очистка не влияет на данные в БД
- **Детальная статистика** - подробная информация о каждом кэше

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

---

## 🔧 Технические детали

### Архитектура
- **CacheController** - основной контроллер для управления кэшем
- **CacheWarmupService** - сервис для прогрева кэша
- **CacheManager** - Spring Cache Manager для управления кэшами
- **Caffeine Cache** - высокопроизводительный кэш

### Производительность
- **Caffeine Cache** - быстрый доступ к данным
- **Автоматическое истечение** - TTL для записей кэша
- **Ограничение размера** - максимальное количество записей
- **Асинхронная загрузка** - неблокирующий прогрев кэша

### Мониторинг
- **Детальная статистика** - количество записей, типы данных
- **Образцы записей** - примеры содержимого кэша
- **Временные метки** - отслеживание времени операций
- **Обработка ошибок** - структурированные сообщения об ошибках

---

## 💡 Примеры использования

### Базовые операции
```bash
# Прогрев кэша
curl -X POST "http://localhost:8083/api/cache/warmup"

# Просмотр всех кэшей
curl "http://localhost:8083/api/cache/content"

# Просмотр конкретного кэша
curl "http://localhost:8083/api/cache/content?cacheName=sharesCache"

# Очистка всех кэшей
curl -X DELETE "http://localhost:8083/api/cache/clear"

# Очистка конкретного кэша
curl -X DELETE "http://localhost:8083/api/cache/clear?cacheName=sharesCache"
```

### Мониторинг и отладка
```bash
# Проверка состояния кэша
curl "http://localhost:8083/api/cache/content" | jq '.caches.sharesCache.entryCount'

# Очистка и прогрев
curl -X DELETE "http://localhost:8083/api/cache/clear" && \
curl -X POST "http://localhost:8083/api/cache/warmup"
```

### Автоматизация
```bash
# Скрипт мониторинга кэша
#!/bin/bash
while true; do
  echo "Проверка кэша $(date)"
  curl -s "http://localhost:8083/api/cache/content" | jq '.caches | keys[] as $k | "\($k): \(.[$k].entryCount) записей"'  
  sleep 60
done
```

---

## ⚠️ Коды ответов

### Успешные ответы
- **200 OK** - операция выполнена успешно

### Ошибки
- **400 Bad Request** - некорректные параметры запроса
- **500 Internal Server Error** - внутренняя ошибка сервера

### Примеры ошибок
```json
{
  "success": false,
  "message": "Ошибка при прогреве кэша: Connection timeout",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## 🔄 Жизненный цикл кэша

### Автоматический прогрев
1. **Запуск приложения** - кэш автоматически прогревается
2. **Загрузка инструментов** - акции, фьючерсы, индикативы
3. **Кэширование** - данные сохраняются в Caffeine Cache
4. **Готовность** - кэш готов к использованию

### Ручное управление
1. **Прогрев** - принудительная загрузка данных
2. **Просмотр** - анализ содержимого кэша
3. **Очистка** - удаление данных из кэша
4. **Восстановление** - повторный прогрев после очистки

### Обработка ошибок
1. **Валидация** - проверка параметров запроса
2. **Логирование** - детальная информация об ошибках
3. **Уведомление** - возврат ошибки клиенту
4. **Восстановление** - автоматический прогрев при ошибках
