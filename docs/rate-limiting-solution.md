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

### 2. **Retry Service** (`RetryService`)
- **Автоматические повторные попытки** при ошибках HTTP 429
- **Экспоненциальный backoff** с jitter для избежания "thundering herd"
- **Умная логика повторения** - повторяет только при временных ошибках

### 3. **Интеграция в MainSessionPriceService**
- Все API вызовы теперь используют `rate limiting` и `retry`
- Параллельная обработка инструментов с контролем скорости
- Graceful handling ошибок - возвращает пустой список вместо исключений

## 🔧 Конфигурация

### Настройки в `application.properties`:
```properties
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

1. **Запрос поступает** в `MainSessionPriceService`
2. **Rate Limiting** проверяет доступность семафора
3. **Интервал** - ожидание минимального времени между запросами
4. **API вызов** выполняется с retry логикой
5. **При ошибке 429** - автоматический retry с экспоненциальным backoff
6. **Освобождение** семафора после завершения

## 📈 Производительность

- **Параллельность**: до 5 одновременных запросов
- **Скорость**: минимум 100ms между запросами
- **Надежность**: до 3 попыток при ошибках
- **Масштабируемость**: настраиваемые параметры

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
- Начало и завершение операций
- Ошибки и повторные попытки
- Статистику rate limiting
- Время ожидания между запросами

```
[processFutureClosePrice_FUTSNGP03260] Попытка 1/3
Rate limiting: ожидание 150ms перед следующим запросом для close_prices
[processFutureClosePrice_FUTSNGP03260] Ошибка на попытке 1: UNAVAILABLE: HTTP status code 429
[processFutureClosePrice_FUTSNGP03260] Повтор через 1250ms
[processFutureClosePrice_FUTSNGP03260] Успешно выполнено с попытки 2
```

Теперь система надежно обрабатывает большие объемы данных без превышения лимитов API! 🎉

