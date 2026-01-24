# 🚀 Решение проблемы с лимитами API (HTTP 429)

## 📋 Проблема
При параллельной обработке большого количества инструментов возникала ошибка:
```
UNAVAILABLE: HTTP status code 429
invalid content-type: text/html
headers: Metadata(:status=429,cache-control=no-cache,content-type=text/html,content-length=117)
DATA-----------------------------
<html><body><h1>429 Too Many Requests</h1>
You have sent too many requests in a given amount of time.
</body></html>
```

## ✅ Решение

### 1. **Rate Limiting Service** (`RateLimitService`)
- **Семафор** для ограничения количества одновременных запросов (по умолчанию: 5)
- **Минимальный интервал** между запросами (по умолчанию: 100ms)
- **Отслеживание времени** последнего запроса для каждого типа операции
- **Автоматическое управление** разрешениями через `acquirePermission()` и `releasePermission()`
- **Функциональный интерфейс** `RateLimitedOperation<T>` для удобного использования
- **Статистика** использования через `getStats()` метод

### 2. **Retry Service** (`RetryService`)
- **Автоматические повторные попытки** при ошибках HTTP 429 и других временных ошибках
- **Экспоненциальный backoff** с jitter (±25%) для избежания "thundering herd"
- **Умная логика повторения** - повторяет только при временных ошибках:
  - HTTP 429 (Too Many Requests)
  - UNAVAILABLE, DEADLINE_EXCEEDED, RESOURCE_EXHAUSTED
  - Сетевые ошибки (Connection, Timeout, Network)
- **Не повторяет** при критических ошибках (INVALID_ARGUMENT, UNAUTHENTICATED, PERMISSION_DENIED)
- **Максимум 3 попытки** по умолчанию

### 3. **Интеграция в сервисы**
- **MainSessionPriceService** - все API вызовы используют `executeWithRetryAndRateLimit()`
- **Параллельная обработка** инструментов с контролем скорости
- **Graceful handling** ошибок - возвращает пустой список вместо исключений
- **Комбинированное использование** - rate limiting + retry для максимальной надежности

### 4. **Rate Limit Controller** (`RateLimitController`)
- **Мониторинг** статистики через `/api/rate-limit/stats`
- **Проверка статуса** через `/api/rate-limit/status`
- **REST API** для отслеживания состояния rate limiting в реальном времени

## 🔧 Конфигурация

### Настройки в `application.properties`:
```properties
# RATE LIMITING CONFIGURATION
# Максимальное количество одновременных запросов к API
rate-limit.max-concurrent-requests=5

# Минимальный интервал между запросами (в миллисекундах)
rate-limit.min-request-interval-ms=100

# Максимальное количество попыток при ошибках
rate-limit.max-retry-attempts=3

# Базовая задержка между попытками (в миллисекундах)
rate-limit.base-retry-delay-ms=1000

# Максимальная задержка между попытками (в миллисекундах)
rate-limit.max-retry-delay-ms=10000
```

### Текущая реализация
**Важно**: В текущей версии `RateLimitService` и `RetryService` используют хардкодные значения вместо конфигурации из `application.properties`. Конфигурация `RateLimitConfig` существует, но не используется напрямую в сервисах.

**Текущие хардкодные значения:**
- `MAX_CONCURRENT_REQUESTS = 5` (в RateLimitService)
- `MIN_REQUEST_INTERVAL_MS = 100` (в RateLimitService)
- `MAX_RETRY_ATTEMPTS = 3` (в RetryService)
- `BASE_RETRY_DELAY_MS = 1000` (в RetryService)
- `MAX_RETRY_DELAY_MS = 10000` (в RetryService)

**Рекомендация**: Для использования конфигурации из `application.properties` необходимо внедрить `RateLimitProperties` в `RateLimitService` и `RetryService`.

## 📊 Мониторинг

### API Endpoints для мониторинга:

#### `GET /api/rate-limit/stats`
```json
{
  "success": true,
  "message": "Статистика rate limiting получена",
  "availablePermits": 3,
  "maxPermits": 5,
  "usedPermits": 2,
  "activeOperationTypes": 1,
  "utilizationPercent": 40.0
}
```

#### `GET /api/rate-limit/status`
```json
{
  "success": true,
  "status": "AVAILABLE",
  "message": "API запросы доступны",
  "availablePermits": 3,
  "maxPermits": 5
}
```

## 🎯 Результат

### До внедрения:
- ❌ HTTP 429 ошибки при параллельной обработке
- ❌ Прерывание обработки при превышении лимитов
- ❌ Нет контроля скорости запросов

### После внедрения:
- ✅ Автоматическое управление скоростью запросов
- ✅ Повторные попытки при временных ошибках
- ✅ Graceful handling ошибок
- ✅ Мониторинг состояния rate limiting
- ✅ Настраиваемые параметры через конфигурацию

## 🔄 Алгоритм работы

### Базовый поток выполнения:
1. **Запрос поступает** в сервис (например, `MainSessionPriceService`)
2. **RetryService.executeWithRetryAndRateLimit()** вызывается с операцией
3. **Rate Limiting**:
   - Проверяет доступность семафора (`acquirePermission()`)
   - Ожидает минимальный интервал между запросами для данного типа операции
   - Обновляет время последнего запроса
4. **API вызов** выполняется через `RateLimitService.executeWithRateLimit()`
5. **При ошибке**:
   - Проверяется тип ошибки (`shouldRetry()`)
   - Если ошибка временная (429, UNAVAILABLE и т.д.) - выполняется retry
   - Вычисляется задержка с экспоненциальным backoff и jitter
   - Повторяется до 3 раз
6. **Освобождение** семафора после завершения (`releasePermission()`)

### Детальный алгоритм Rate Limiting:
```
1. acquirePermission(operationType)
   ├─ apiSemaphore.acquire() - получение разрешения
   ├─ waitForMinimumInterval(operationType) - ожидание интервала
   └─ lastRequestTimes.put(operationType, now) - обновление времени

2. executeWithRateLimit(operationType, operation)
   ├─ acquirePermission(operationType)
   ├─ operation.execute() - выполнение операции
   └─ releasePermission() - освобождение разрешения
```

### Детальный алгоритм Retry:
```
1. executeWithRetry(operation, operationName)
   ├─ Попытка 1: выполнение операции
   ├─ При ошибке:
   │  ├─ shouldRetry() - проверка типа ошибки
   │  ├─ calculateRetryDelay() - вычисление задержки
   │  └─ Thread.sleep(delay) - ожидание
   ├─ Попытка 2: повторное выполнение
   ├─ При ошибке: повтор шага выше
   └─ Попытка 3: финальная попытка
```

## 📈 Производительность

### Текущие характеристики:
- **Параллельность**: до 5 одновременных запросов (семафор)
- **Скорость**: минимум 100ms между запросами одного типа операции
- **Надежность**: до 3 попыток при временных ошибках
- **Backoff**: экспоненциальный с jitter (±25%)
  - Попытка 1 → Попытка 2: ~1000ms (750-1250ms с jitter)
  - Попытка 2 → Попытка 3: ~2000ms (1500-2500ms с jitter)
  - Максимальная задержка: 10000ms

### Оптимизация:
- **Разделение по типам операций** - каждый тип операции имеет свой интервал
- **Неблокирующая обработка** - семафор не блокирует другие типы операций
- **Автоматическое освобождение** - гарантированное освобождение ресурсов через try-finally
- **Минимальная задержка** - ожидание только при необходимости

## 🛠️ Настройка под ваши нужды

### Для более агрессивной обработки:
```properties
rate-limit.max-concurrent-requests=10
rate-limit.min-request-interval-ms=50
```

### Для более консервативной обработки:
```properties
rate-limit.max-concurrent-requests=3
rate-limit.min-request-interval-ms=200
```

## 📝 Логирование

Система автоматически логирует:
- **Начало и завершение операций** - каждая попытка логируется с номером
- **Ошибки и повторные попытки** - детальная информация об ошибках
- **Статистику rate limiting** - доступна через API эндпоинты
- **Время ожидания** между запросами (выводится в System.out)
- **Успешные повторные попытки** - логируется номер успешной попытки

### Примеры логов:

#### Успешное выполнение:
```
[processFutureClosePrice_FUTSNGP03260] Попытка 1/3
Rate limiting: ожидание 150ms перед следующим запросом для close_prices
[processFutureClosePrice_FUTSNGP03260] Успешно выполнено
```

#### С повторной попыткой:
```
[processFutureClosePrice_FUTSNGP03260] Попытка 1/3
Rate limiting: ожидание 150ms перед следующим запросом для close_prices
[processFutureClosePrice_FUTSNGP03260] Ошибка на попытке 1: UNAVAILABLE: HTTP status code 429
[processFutureClosePrice_FUTSNGP03260] Повтор через 1250ms
[processFutureClosePrice_FUTSNGP03260] Попытка 2/3
Rate limiting: ожидание 100ms перед следующим запросом для close_prices
[processFutureClosePrice_FUTSNGP03260] Успешно выполнено с попытки 2
```

#### Критическая ошибка (без повтора):
```
[processFutureClosePrice_FUTSNGP03260] Попытка 1/3
[processFutureClosePrice_FUTSNGP03260] Критическая ошибка, повтор не требуется: INVALID_ARGUMENT: Invalid FIGI
```

## 🔍 Мониторинг и отладка

### API эндпоинты для мониторинга:

#### GET /api/rate-limit/stats
Получение детальной статистики:
```bash
curl http://localhost:8083/api/rate-limit/stats
```

Ответ:
```json
{
  "success": true,
  "message": "Статистика rate limiting получена",
  "availablePermits": 3,
  "maxPermits": 5,
  "usedPermits": 2,
  "activeOperationTypes": 2,
  "utilizationPercent": 40.0
}
```

#### GET /api/rate-limit/status
Проверка текущего статуса:
```bash
curl http://localhost:8083/api/rate-limit/status
```

Ответ:
```json
{
  "success": true,
  "status": "AVAILABLE",
  "message": "API запросы доступны",
  "availablePermits": 3,
  "maxPermits": 5
}
```

### Метрики для отслеживания:
- **utilizationPercent** - процент использования разрешений (0-100%)
- **usedPermits** - количество активных запросов
- **activeOperationTypes** - количество типов операций с активными запросами
- **availablePermits** - доступные разрешения для новых запросов

## 🎯 Результат

Теперь система надежно обрабатывает большие объемы данных без превышения лимитов API! 🎉

### Ключевые преимущества:
- ✅ **Автоматическое управление** скоростью запросов
- ✅ **Умные повторные попытки** только при временных ошибках
- ✅ **Graceful handling** ошибок без прерывания обработки
- ✅ **Мониторинг** состояния rate limiting в реальном времени
- ✅ **Масштабируемость** через настраиваемые параметры
- ✅ **Разделение по типам операций** для оптимальной производительности

